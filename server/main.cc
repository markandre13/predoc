/*
	PReDoc Daemon
	Written and Copyright 1998 by Mark-André Hopf
	
	requirements:
		Linux 2.2.x
		EGCS 1.1.1
		GNU C Library 2.0.6 & LinuxThreads 2.0.6
		
	how the data directory is managed:
		*.ps							file				PostScript document
		*.raster					directory
		*.dat							file				server recovery after crash, reboot, etc.
			<number>.png		file				rastered document
			<user>					directory
				*							file				predoc correction file

	things to do:
	  - locking
	  - add a zero size file to indicate the end of a render process
	    just in case the server get killed during operation
	  - better parallelisation
		- should catch libpng errors (unlikely to occur but who knows..)
*/

enum {										// request from the clients point of view:
	CMD_LOGIN = 1,					// put login and password
	CMD_DIRECTORY_ENTRIES,	// get document & directory list for directory
	CMD_DOCUMENT_SIZE,			// get number of pages in document
	CMD_BITMAP,							// get PNG file for document & page
	CMD_DOCUMENT_ENTRIES,		// get correction list for document
	CMD_GET_CORRECTION,			// get correction file for document
	CMD_PUT_CORRECTION,			// put correction file for document
};

enum {
	ACK_FAILED = 0,		// request failed
	ACK_OK = 1,				// request 
	ACK_WAIT = 2			// request is being processed, wait for 2nd acknowledgement
										// (currently senseless since the output buffer is only
										// flushed after the op)
};

#include <cstdio>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>

// dir stuff
#include <dirent.h>
#include <sys/stat.h>

// exec stuff
#include <sys/wait.h>

#include <stdexcept>
#include <iostream>
#include <strstream>
#include <string>
#include <map>
#include <vector>
#include "config.h"
#include "thread.hh"
#include "http.hh"
#include "binfile.hh"

#include "types.hh"
#include "idandnamemap.hh"

#define DBM(A) A

int predoc_port = PREDOC_PORT;
int http_port   = HTTP_PORT;
string data_dir = DATA_DIR;
string java_dir = CLASS_DIR;

void SmoothPNGFile(const string&);

static int make_socket(int port);

class TDirectoryMap;

typedef vector<string> TStringVector;

class TNameAndId
{
	protected:
		qword _id;
		string _name;
	public:
		TNameAndId() {
			_id = 0;
		}
		TNameAndId(qword id, const string &name) {
			_id = id;
			_name = name;
		}
		TNameAndId(const TNameAndId &is) {
			_id = is._id;
			_name = is._name;
		}
		qword Id() const { return _id; }
		const string& Name() const { return _name; }
};

void WriteDoc2Server(TBinaryFile &out, const string _path, const string& username)
{
	string realpath = data_dir+"/"+_path+".raster/"+username;
	
	cout<< "send correction list:\n"
			<< "  document : \"" << _path << "\"\n"
			<< "  user     : " << username << endl
			<< "  path     : \"" << realpath << "\"\n";

	dirent *de;
	DIR *dd = opendir(realpath.c_str());
	if (dd==NULL) {
		cout << "couldn't open directory \"" << realpath << "\"\n";
		out.WriteByte(ACK_FAILED);
		return;
	}
	
	TStringVector buf;

	string file;
	while( (de=readdir(dd))!=NULL ) {
		if (de->d_name[0]!='.') {
			file=realpath+"/"; file+=de->d_name;
			cout << "found file :\"" << file << "\"\n";
			struct stat st;
			stat(file.c_str(), &st);
			if (!S_ISDIR(st.st_mode))
				buf.push_back(de->d_name);
		} 
	}
	closedir(dd);

	cout << "writing corrections\n";

	out.WriteByte(ACK_OK);

	out.WriteQWord(buf.size());
	for(unsigned i=0; i<buf.size(); i++) {
		cout << "\"" << buf[i] << "\"\n";
		out.WriteDWord(buf[i].size());
		out.WriteString(buf[i]);
	}
}

void WriteFile2Server(TBinaryFile &out, const string &realpath)
{
	struct stat st;
	if (stat(realpath.c_str(), &st)) {
		out.WriteByte(ACK_FAILED);
		cout << "failed: couldn't get file status\n";
		return;
	}
	if (S_ISDIR(st.st_mode)) {
		out.WriteByte(ACK_FAILED);
		cout << "failed: path is a directory\n";
		return;
	}
//	ifstream fin(realpath.c_str(), ios::in | ios::bin);
	int fin = open(realpath.c_str(), O_RDONLY);
	if (!fin) {
		out.WriteByte(ACK_FAILED);
		cout << "failed: couldn't open file\n";
		return;
	}
	out.WriteByte(ACK_OK);
	out.WriteQWord(st.st_size);
	cout << "file size: " << st.st_size << endl;
	unsigned n = st.st_size;
	#warning "should use sendfile here..."
	byte buffer[8193];
	while(n>0) {
		int m = n>=8192 ? 8192 : n;
//		fin.read(buffer,m);
		int p = read(fin, buffer, m);
		if (p!=m) {
			cout << "wanted " << m << ", got " << p << endl;
			if (p<0)
				continue;
			m=p;
		}
		out.Write(buffer,m);
		n-=m;
	}
}

void WriteCor2Server(TBinaryFile &out, 
										 const string &path,
										 const string &username,
										 const string &file)
{
	string realpath = data_dir+"/"+path+".raster/"+username+"/"+file;
	
	cout<< "send correction data:\n"
			<< "  document : \"" << path << "\"\n"
			<< "  user     : " << username << endl
			<< "  file     : \"" << file << "\"\n"
			<< "  path     : \"" << realpath << "\"\n";

	WriteFile2Server(out, realpath);
}

void WriteSize2Server(TBinaryFile &out, 
											const string &path,
											const string &username)
{
	string realdoc  = data_dir+"/"+path;
	string realpath = realdoc+".raster/";
	
	cout<< "send document size:\n"
			<< "  document : \"" << path << "\"\n"
			<< "  user     : " << username << endl
			<< "  path     : \"" << realpath << "\"\n";
	
	dirent *de;
	DIR *dd;
	
	dd = opendir(realpath.c_str());
	if (dd==NULL) {
		cout << "couldn't open directory \"" << realpath << "\"\n";
		out.WriteByte(ACK_FAILED);
		return;
	}
	
	int n;
	
	n=-1;
	while( (de=readdir(dd))!=NULL ) {
		string file = de->d_name;
		if (file.size()>4) {
			if (file.substr(file.size()-4)==".png") {
				int m = atoi( file.substr(0,file.size()-4).c_str() );
				if (m>n) {
					n=m;
				}
			}
		}
	}
	closedir(dd);
	
	n++;
	if (n>0) {
		cout << "  document has " << n << " pages\n";
		out.WriteByte(ACK_OK);
		out.WriteQWord(n);
		return;
	}
	out.WriteByte(ACK_WAIT);
	cout << "  rendering document, client has to wait...\n";

	// render the file with GhostScript
	//----------------------------------
	#warning "locking needed here"

	string outp ="-sOutputFile="; outp+=realpath; outp+="/%d.png";

	int pid;
	pid = fork();
	if (pid==-1) {
		cout << "  failed to render the document (fork failed)\n";
		out.WriteByte(ACK_FAILED);
		return;
	}
	if (!pid) {
		if (execle(GHOSTSCRIPT, GHOSTSCRIPT,
							 "-sDEVICE=pnggray",
							 "-dNOPAUSE",
							 "-q",
#ifdef ENABLE_SMOOTHING
							 "-r300",
#else
							 "-r75",
#endif
							 outp.c_str(),
							 "--",
							 realdoc.c_str(), 
							 NULL, NULL)==-1)
		{
			cout << "  failed to render the document (exec failed)\n";
			out.WriteByte(ACK_FAILED);
			return;
		}
	}
	cout << "  started rendering..." << endl;
	int status;
	waitpid(pid, &status, 0);


	if (!WIFEXITED(status)) {
		cout << "  rendering failed" << endl;
		out.WriteByte(ACK_FAILED);
		return;
	}

	// scale the image down and do some smoothing to create an
	// antialiased bitmap
	//--------------------------------------------------------
	
	#warning "smoothing could be done with a background thread"

	dd = opendir(realpath.c_str());
	if (dd==NULL) {
		cout << "couldn't open directory \"" << realpath << "\"\n";
		out.WriteByte(ACK_FAILED);
		return;
	}
	
	n=-1;
	while( (de=readdir(dd))!=NULL ) {
		string s(de->d_name);
		string file = realpath + s;
		if (file.size()>4) {
			if (file.substr(file.size()-4)==".png") {
				int m = atoi( s.substr(0,s.size()-4).c_str() );
				if (m>n)
					n=m;
#ifdef ENABLE_SMOOTHING
				SmoothPNGFile(file);
#endif
			}
		}
	}
	closedir(dd);
	
	out.WriteByte(ACK_OK);
	out.WriteQWord(n+1);
}

void WritePNG2Server(TBinaryFile &out, 
										 const string &path,
										 qword page)
{
	char number[255];
	sprintf(number, "%lu", (unsigned long) page+1);
	string realpath = data_dir+"/"+path+".raster/"+number; realpath+=".png";
	
	cout<< "send bitmap:\n"
			<< "  document : \"" << path << "\"\n"
			<< "  page     : \"" << page << "\"\n"
			<< "  file     : \"" << realpath << "\"\n";
	WriteFile2Server(out, realpath);
}


class TDirectory
{
		string _path;
	public:
		TDirectory(const string &path);
		void Write2Server(TBinaryFile &out);
};

class TDirectoryMap:
	protected map<string, TDirectory*>
{
	public:
		TDirectory* Find(const string &name) {
			iterator p = find(name);
			if (p==end()) {
				cout << "should try to create a new entry..." << endl;
				string realpath = data_dir + "/" + name;
				cout << "looking up directory \"" + realpath + "\"\n";
				DIR *dd = opendir(realpath.c_str());
				if (dd==NULL) {
					cout << "no such directory" << endl;
					return NULL;
				}
				closedir(dd);
				TDirectory *d = new TDirectory(name);
				Add(d, name);
				return d;
			}
			return (*p).second;
		}
		void Add(TDirectory *dir, const string &name) {
			(*this)[name]=dir;
		}
} dirmap;


TDirectory::TDirectory(const string &path)
{
	_path = path;
	if (path[path.size()-1]!='/')
		_path+="/";
	cout << "directory: new directory \"" << _path << "\"\n";
}

void TDirectory::Write2Server(TBinaryFile &out)
{
	dirent *de;
	string realdir = data_dir+"/"+_path;
	cout << "directory: reading directory \"" << realdir << "\"\n";
	DIR *dd = opendir(realdir.c_str());
	if (dd==NULL) {
		cerr << "ERROR: couldn't read directory\n";
		out.WriteByte(ACK_FAILED);
		return;
	}

	// retrieve directory contents
	//-----------------------------
	TStringVector dir_buf, doc_buf;
	while( (de=readdir(dd))!=NULL ) {
		if (de->d_name[0]!='.') {
			struct stat st;
			string file = realdir+de->d_name;

			stat(file.c_str(), &st);
			if (S_ISDIR(st.st_mode)) {
				if (file.size()>8 && file.substr(file.size()-7)!=".raster") {
					dir_buf.push_back(de->d_name);
				}
			} else {
				if (file.size()>4 && file.substr(file.size()-3)==".ps") {
					string simple = de->d_name;
					doc_buf.push_back(de->d_name);
				}
			}
		}
	}
	closedir(dd);

	// send directory contents
	//-------------------------
	out.WriteByte(ACK_OK);

	TStringVector::iterator bp, be;

	out.WriteQWord(doc_buf.size());
	bp = doc_buf.begin();
	be = doc_buf.end();
	while(bp!=be) {
		out.WriteDWord((*bp).size());
		out.WriteString(*bp);
		bp++;
	}

	out.WriteQWord(dir_buf.size());
	bp = dir_buf.begin();
	be = dir_buf.end();
	while(bp!=be) {
		out.WriteDWord((*bp).size());
		out.WriteString(*bp);
		bp++;
	}
}

class TServer:
	public TThread
{
		int _fd;
		string _client;
		string _received;
		string _username;
		string _password;
		
	public:
		TServer(int fd, const string&);
		
	protected:
		void main();
		void command();
};

TServer::TServer(int fd, const string &client)
{
	_fd = fd;
	_client = client;
}

/*
 * This is the main message loop, it's job is to fetch the messages from
 * the socket and to call `command()' when new data is available.
 */
void TServer::main()
{
	fcntl(_fd, F_SETFL, O_NONBLOCK);
	delete_on_exit = true;
	
	while(true) {
		DBM(cout << _client << ": waiting\n";)
		fd_set rs, ws, xs;
		FD_ZERO(&rs); FD_ZERO(&ws); FD_ZERO(&xs);
		FD_SET(_fd, &rs);
		if (select(_fd+1, &rs, &ws, &xs, NULL)==-1) {
			DBM(perror("select");)
			continue;
		}
		if (FD_ISSET(_fd, &rs)) {
			int m=0;
			while(true) {
				char buffer[1025];
				int n = read(_fd, buffer, 1024);
				if (n<0) {
					DBM(perror("read");)
					break;
				}
				m+=n;
				DBM(cout << _client << ": got data " << n << endl;)
				if (m==0) {
					cout << _client << ": connection closed" << endl;
					Exit();
				}
				if (n<1)
					break;
				_received.append(buffer,n);
			}
			if (m>0) {
				cout << "command()" << endl;
				command();
			} else
				cout << "no command()\n";
		}
	}
}

void TServer::command()
{
	try {
		strstream is(const_cast<char*>(_received.c_str()), _received.size(), ios::in);
		TBinaryFile in(&is);
		in.SetEndian(TBinaryFile::BIG);
		strstream os;
		TBinaryFile out(&os);
		out.SetEndian(TBinaryFile::BIG);
		int cmd = in.ReadDWord();
		switch(cmd) {

			case CMD_LOGIN:
					{
						_username = in.ReadString(in.ReadDWord());
						_password = in.ReadString(in.ReadDWord());
						#warning "password check is missing"
						cout << _client << ": user \"" << _username << "\"" << endl;
						out.WriteByte(ACK_OK);
					}
					break;

			case CMD_DIRECTORY_ENTRIES: {
					cout << _client << ": cmd directory entries\n";
					string id = in.ReadString(in.ReadDWord());
					cout << _client << ":   directory \"" << id << "\"\n";
					TDirectory *d = dirmap.Find(id);
					if (!d) {
						cout << _client << ": illegal directory id: " << id << endl;
						out.WriteByte(ACK_FAILED);
					} else {
						d->Write2Server(out);
					}
				}	break;

			case CMD_DOCUMENT_ENTRIES: {
					cout << _client << ": cmd document entries\n";
					string id = in.ReadString(in.ReadDWord());
					cout << _client << ": document \"" << id << "\"\n";
					WriteDoc2Server(out, id, _username);
				} break;
				
			case CMD_GET_CORRECTION: {
					cout << _client << ": cmd get correction\n";
					string path = in.ReadString(in.ReadDWord());
					string file = in.ReadString(in.ReadDWord());
					WriteCor2Server(out, path, _username, file);
				} break;

			case CMD_DOCUMENT_SIZE: {
					cout << _client << ": cmd document size\n";
					string path = in.ReadString(in.ReadDWord());
					WriteSize2Server(out, path, _username);
				} break;

			case CMD_BITMAP: {
					cout << _client << ": cmd bitmap\n";
					string path = in.ReadString(in.ReadDWord());
					qword page = in.ReadQWord();
					WritePNG2Server(out, path, page);
				}
				break;

			default:
				cout << _client << ": unknown command [" << cmd << "]\n";
				return;
		}

		// command was successfull so transmit the output buffer and remove
		// last command from the `_received' buffer
		out.Flush();
		if (os.pcount()>0) {
			cout << "sending " << os.pcount() << " bytes to client\n";
			fcntl(_fd, F_SETFL, 0);
			cout << "write said: " << write(_fd, os.str(), os.pcount()) << endl;
			fcntl(_fd, F_SETFL, O_NONBLOCK);
			int n = in.TellRead();
			cout << "skipping " << n << " read bytes from " << _received.size() << endl;
			_received = _received.substr(n);
		}
	}
	catch(exception &e) {
		cout << "catched exception: " << e.what() << endl;
	}
}

int main(int argc, char *argv[])
{
	cout << "running\n";

	// parse arguments
	//-------------------------------------------------------------------------
	for(int i=1; i<argc; i++) {
		if (strcmp(argv[i], "--port")==0) {
			i++;
			predoc_port = atoi(argv[i]);
		}
		if (strcmp(argv[i], "--http")==0) {
			i++;
			http_port = atoi(argv[i]);
		}
	}

	TDirectory *d = new TDirectory("/");
	dirmap.Add(d, "/");

	// create server socket
	//-------------------------------------------------------------------------
	int predoc_sock = make_socket(predoc_port);
	int http_sock = make_socket(http_port);
	int max_sock = (predoc_sock > http_sock  ? predoc_sock : http_sock ) + 1;

	// wait for connections
	//-------------------------------------------------------------------------
	fd_set rfds, wfds, xfds;
	FD_ZERO(&rfds); FD_ZERO(&wfds); FD_ZERO(&xfds);
	FD_SET(predoc_sock, &rfds);
	FD_SET(http_sock, &rfds);
	
	while(true) {
		fd_set rs, ws, xs;
		rs=rfds; ws=wfds; xs=xfds;
		if (select(max_sock, &rs, &ws, &xs, NULL)==-1) {
			perror("select");
			continue;
		}
		if (FD_ISSET(predoc_sock, &rs)) {
			sockaddr_in new_name;
			unsigned new_len;
			int s = accept(predoc_sock, (sockaddr*)&new_name, &new_len);
			if (new_name.sin_family==AF_INET) {
				printf("new predoc connection from %s\n", inet_ntoa(new_name.sin_addr));
			}
			TServer *server = new TServer(s,inet_ntoa(new_name.sin_addr));
			printf("nobody knows what insane aliens will do next! so we launch at once\n");
			server->Start();
		}
		if (FD_ISSET(http_sock, &rs)) {
			sockaddr_in new_name;
			unsigned new_len;
			int s = accept(http_sock, (sockaddr*)&new_name, &new_len);
			if (new_name.sin_family==AF_INET) {
				printf("new http connection from %s\n", inet_ntoa(new_name.sin_addr));
			}
			THTTPServer *server = new THTTPServer(s,inet_ntoa(new_name.sin_addr));
			printf("nobody knows what insane aliens will do next! so we launch at once\n");
			server->Start();
		}
	}
}

int make_socket(int port)
{
	int sock = socket (AF_INET, SOCK_STREAM, 0);
	if ( sock < 0 ) {
		perror("socket");
		exit(EXIT_FAILURE);
	}
	
	int value = 1;
	setsockopt( sock, SOL_SOCKET, SO_REUSEADDR, &value, sizeof(int) );
	
	sockaddr_in name;
	name.sin_family = AF_INET;
	name.sin_port = htons(port);
	name.sin_addr.s_addr = htonl(INADDR_ANY);

	if ( bind(sock, (struct sockaddr*) &name, sizeof (name)) < 0 ) {
		perror("bind");
		exit(EXIT_FAILURE);
	}
	
	if ( listen(sock, 8) < 0 ) {
		perror ("listen");
		exit (EXIT_FAILURE);
	}
	return sock;
}
