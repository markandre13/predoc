#include "http.hh"
#include <cstdio>
#include <iostream>
#include <strstream>
#include <string>
#include <stdexcept>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>

extern int predoc_port;
extern string java_dir;

// Linux specific stuff (uhh, 2.2 kernel?)
#include <asm/unistd.h>

inline ssize_t sendfile(int out_fd, int in_fd, off_t *offset, size_t count)
{
	return syscall(__NR_sendfile, out_fd, in_fd, offset, count);
}

string upper(const string &str)
{
	string result;
	string::const_iterator p,e;
	p = str.begin();
	e = str.end();
	while(p!=e) {
		result += toupper(*p);
		p++;
	}
	return result;
}  


THTTPServer::THTTPServer(int fd, const string&)
{
	_fd = fd;
}

void THTTPServer::main()
{
	try {
		string file;
		string command;
		bool full = false;
		while(true) {
			char buffer[1025];
			int n = read(_fd, buffer, 1024);
			if (n<=0)
				break;
			command.append(buffer, n);
			if (!full) {
				unsigned i = command.find_first_of("\r\n");
				if (i!=string::npos) {
					// "GET" SP Request-URI CRLF
					// Method SP Request-URI SP HTTP-Version CRLF
					//-------------------------------------------
					static char s1[16], s2[2047], s3[64];
					int n = sscanf(command.substr(0,i).c_str(), 
										 		 "%15s %2048s %63s", s1, s2, s3);
					if (n==2 && strncasecmp(s1, "GET", 3)==0) {
						file = s2;
						break;
					} else if (n==3 && strncasecmp(s3, "HTTP", 4)==0) {
						file = s2;
						full = true;
					} else {
						throw runtime_error("Invalid-Request\n");
					}
				}
				if (full) {
					if (command.find("\r\n\r\n")!=string::npos)
						break;
				}
			}
		}
		
		if (file=="/") {
			strstream out;
			if (full) {
					out << "HTTP/1.0 200 OK\r\n"
							<< "Server: PReDoc eXperimental Daemon 0.1\r\n"
							<< "Connection: close\r\n"
							<< "Content-Type: text/html\r\n"
							<< "\r\n";
			}
			out	<< "<HTML><HEAD><TITLE>PReDoc</TITLE></HEAD>\n"
					<< "<BODY BGCOLOR=#4080FF TEXT=#800040>\n"
					<< "<H1>PReDoc is loading, please wait...</H1>\n"
					<< "<APPLET CODE=\"predoc.class\" ARCHIVE=\"predoc.jar\" WIDTH=2 HEIGHT=2>\n"
					<< "<PARAM name=\"server\" value=\"www.magicinc.home\">\n"
					<< "<PARAM name=\"port\" value=\"" << predoc_port << "\">\n"
					<< "</APPLET>\n"
					<< "</BODY>\n";
			write(_fd, out.str(), out.pcount());
		} else {
			file = java_dir + file;
			struct stat s;
			if (stat(file.c_str(), &s)) {
				strstream out;
				if (full) {
					out << "HTTP/1.1 404 File Not Found\r\n"
							   "Server: PReDoc eXperimental Daemon 0.1\r\n"
							   "Connection: close\r\n"
							   "Content-Type: text/html\r\n"
							   "\r\n";
				}
				out << "<HTML><HEAD>\n"
						   "<TITLE>404 File Not Found</TITLE>\n"
						   "</HEAD><BODY>\n"
						   "<H1>File Not Found</H1>\n"
						   "</BODY>\n";
				write(_fd, out.str(), out.pcount());
			} else {
				if (full) {
					strstream out;
					out << "HTTP/1.0 200 OK\r\n"
							<< "Server: PReDoc eXperimental Daemon 0.1\r\n"
							<< "Connection: close\r\n"
							<< "Content-Length: " << s.st_size << "\r\n"
							<< "Content-Type: ";
					cout << upper(file.substr(file.size()-4)) << endl;
					if (file.size()>4 && upper(file.substr(file.size()-4))==".GIF") {
						out << "image/gif";
					} else {
						out << "text/html";
					}
					out << "\r\n"
							<< "\r\n";
					write(_fd, out.str(), out.pcount());
				}
				off_t o = 0;
				int in = open(file.c_str(), O_RDONLY);
				sendfile(_fd, in, &o, s.st_size);
				close(in);
			}
			
		cout << "HTTP FILE:\n" << file << endl;
		}
		
	}
	catch(exception &e) {
		cout << "ERROR: " << e.what() << endl;
	}
	close(_fd);
}
