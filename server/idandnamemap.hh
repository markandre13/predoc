#ifndef GIdAndNameMap
#define GIdAndNameMap GIdAndNameMap

#include <stdexcept>
#include <map>
#include <string>
#include "types.hh"
#include "thread.hh"

template <class T>
class GIdAndNameMap
{
		typedef map<qword, T*> TByNumber;
		typedef map<string, T*> TByName;
		TByNumber _byNumber;
		TByName _byName;
		qword _max_id;
		TThreadMutex _mutex;
		
	public:
		GIdAndNameMap();
		void Add(T*, qword, const string&);
		T* Find(qword);
		T* Find(const string&);
		qword MaxId() { return _max_id; }
};

template <class T>
GIdAndNameMap<T>::GIdAndNameMap()
{
	_max_id = 0;
}

template <class T>
void GIdAndNameMap<T>::Add(T *d, qword nid, const string &sid)
{
	TThreadLock lock(_mutex);
	TByNumber::const_iterator np = _byNumber.find(nid);
	TByName::const_iterator sp = _byName.find(sid);
	if (np!=_byNumber.end() || sp!=_byName.end()) {
		throw runtime_error("GIdAndName::Add: entry is already defined");
	}
	_byNumber[nid] = d;
	_byName[sid] = d;
	
	if (_max_id < nid)
		_max_id = nid;
}

template <class T>
T* GIdAndNameMap<T>::Find(qword id) {
	TThreadLock lock(_mutex);
	TByNumber::const_iterator p = _byNumber.find(id);
	if (p==_byNumber.end())
		return NULL;
	return (*p).second;
}

template <class T>
T* GIdAndNameMap<T>::Find(const string& id) {
	TThreadLock lock(_mutex);
	TByName::const_iterator p = _byName.find(id);
	if (p==_byName.end())
		return NULL;
	return (*p).second;
}

#endif
