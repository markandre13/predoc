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

#ifndef TBinaryFile
#define	TBinaryFile TBinaryFile

typedef unsigned long ulong;
typedef unsigned char byte;

#include <stdexcept>
#include <fstream>
#include <iostream>
#include <string>

class io_error : public runtime_error {
public:
	io_error (const string& what_arg): runtime_error (what_arg) { }
};


class TBinaryFile
{
	public:
		enum EEndian { LITTLE, BIG };

	protected:
		iostream *_io;
		EEndian _endian;

	public:
		TBinaryFile(iostream*);
		~TBinaryFile();
		void Flush() { _io->flush(); }
		ulong TellRead()  { return _io->tellg(); }
		ulong TellWrite() { return _io->tellp(); }
		void SeekRead(ulong p) { _io->seekg(p); }
		void SeekWrite(ulong p) { _io->seekp(p); }
		bool Eof() { return _io->eof(); }
		void SetEndian(EEndian e) {
			_endian = e;
		}

		void Write(const signed char* buffer, size_t count);
		void Write(const unsigned char* buffer, size_t count);
		void Write(signed char* buffer, size_t count);
		void Write(unsigned char* buffer, size_t count);

		void WriteChar(signed char);				// 8bit	char
		void WriteChar(unsigned char);
		
		void WriteByte(unsigned char);			// 8bit unsigned integer
		
		void WriteWord(unsigned int);				// 16bit unsigned integer
		
		void WriteDWord(unsigned long);			// 32bit unsigned integer
		
		void WriteQWord(unsigned long long);// 64bit unsigned integer
		unsigned long long ReadQWord();

		void WriteDouble(double);						// 64bit IEEE 754 coded floating-point
		double ReadDouble();

		void WriteFloat(float);							// 32bit IEEE 754 coded floating-point
		float ReadFloat();

		void WriteString(const char*, unsigned len);
		void WriteString(const char*);
		void WriteString(const string&);

		void Read(signed char* buffer, size_t count);
		void Read(unsigned char* buffer, size_t count);
		void ReadString(char*,int len);
		void ReadString(unsigned char *buf,int len);
		void ReadString(string*,int len);
		string ReadString(int len);
		char ReadChar();
		byte ReadByte();
		unsigned ReadWord();
		ulong ReadDWord();
		bool CompareString(const char*,int len);
		//int CompareString(string&,int len);
		//int CompareString(const char*);
		//int CompareString(string&);
		void Unget(){_io->unget();}

		int ReadInt();						// 32bit signed integer
		
		void WriteInt(int);

};

#endif
