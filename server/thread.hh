/*
 * TOAD -- A Simple and Powerful C++ GUI Toolkit for X-Windows
 * Copyright (C) 1996-98 by Mark-André Hopf
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

#ifndef TThread
#define TThread TThread

#include <pthread.h>

class TThread
{
		pthread_t _thread;
		static void* _thread_entry(void*);

	public:
		virtual ~TThread(){delete_on_exit=false;}
		void Start();
		void Join();
		void Exit(){pthread_exit(NULL);}
		
	protected:
		virtual void main() = 0;
		bool delete_on_exit;
};

class TThreadMutex
{
		pthread_mutex_t mutex;
	public:
		TThreadMutex() {
			pthread_mutex_init(&mutex, NULL);
		}
		void Lock() {
			pthread_mutex_lock(&mutex);
		}
		void Unlock() {
			pthread_mutex_unlock(&mutex);
		}
};

class TThreadLock
{
		TThreadMutex *mutex;
	public:
		TThreadLock(TThreadMutex *m) {
			mutex = m;
			mutex->Lock();
		}
		TThreadLock(TThreadMutex &m) {
			mutex = &m;
			mutex->Lock();
		}
		~TThreadLock() {
			mutex->Unlock();
		}
};

#endif
