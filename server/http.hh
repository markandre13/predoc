#ifndef THTTPServer
#define THTTPServer THTTPServer

#include <ctype.h>
#include <string>
#include "thread.hh"

class THTTPServer:
	public TThread
{
		int _fd;
	public:
		THTTPServer(int fd, const string&);
		void main();
};

#endif
