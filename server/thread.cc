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

#include "thread.hh"
#include <iostream>

void TThread::Join()
{
	void* dummy;
	pthread_join(_thread, &dummy);
}

void TThread::Start()
{
#if 0
	cout << "dummy thread start\n";
	main();
	cout << "dummy thread end\n";
#else
	pthread_create(&_thread, NULL, _thread_entry, (void*)this);
#endif
}

void* TThread::_thread_entry(void *obj)
{
	((TThread*)obj)->main();
	if (((TThread*)obj)->delete_on_exit)
		delete (TThread*)obj;
	pthread_exit(NULL);
}
