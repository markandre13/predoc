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

// TODO:
// - better error messages
//   e.g: stream is read only, stream is write only, end of stream, ...


// #include <toad/io/binfile.hh>
#include "binfile.hh"

// - force problems with non IEEE 754 environments...
// - i guess it would be good to comile this file with GCCs -ffloat-store
//   option [MAH]
#include <ieee754.h>

#include <strstream>

//! TBinaryFile
//. This class might help you to read and write binary data from and to 
//. C++ iostreams.<BR>
//. After <CODE>SetEndian(TBinaryFile::BIG)</CODE> it's even possible
//. to exchange data with Java. See <CODE>java.io.DataOutputStream</CODE> 
//. and <CODE>java.io.DataInputStream</CODE> in the Java reference for
//. more information.
//. <P>
//. <H3>Attention!</H3>
//. <UL>
//.   <LI>This class is very alpha and only tested on x86 CPUs but
//.       fixed-point methods should work on common little- and big-endian
//.       CPUs.
//.   <LI>Floating-point methods will fail on non IEEE 754 compliant CPUs
//. </UL>

TBinaryFile::TBinaryFile(iostream *io)
{
	_endian = LITTLE;
	_io = io;
	_io->seekg(0);
	_io->seekp(0);
}

TBinaryFile::~TBinaryFile()
{
}

void TBinaryFile::Write(const signed char* buffer, size_t count)
{
	_io->write(buffer, count);
}

void TBinaryFile::Write(const unsigned char* buffer, size_t count)
{
	_io->write(buffer, count);
}

void TBinaryFile::Write(signed char* buffer, size_t count)
{
	_io->write(buffer, count);
}

void TBinaryFile::Write(unsigned char* buffer, size_t count)
{
	_io->write(buffer, count);
}

void TBinaryFile::WriteChar(signed char c)
{
	_io->put(c);
}

void TBinaryFile::WriteChar(unsigned char c)
{
	_io->put(c);
}

void TBinaryFile::WriteByte(unsigned char c)
{
	_io->put(c);
}

void TBinaryFile::WriteString(const char* str)
{
	_io->write(str, strlen(str));
}

void TBinaryFile::WriteString(const char* str, unsigned len)
{
	_io->write(str,len);
}

void TBinaryFile::WriteString(const string &s)
{
	_io->write(s.c_str(), s.size());
}

void TBinaryFile::WriteWord(unsigned int v)
{
	char buffer[2];
	switch(_endian) {
		case LITTLE:
			buffer[0]=v&255;
			buffer[1]=(v>>8)&255;
			break;
		case BIG:
			buffer[1]=v&255;
			buffer[0]=(v>>8)&255;
			break;
	}
	_io->write(buffer,2);
}

void TBinaryFile::WriteDWord(unsigned long v)
{
	char buffer[4];
	switch(_endian) {
		case LITTLE:
			buffer[0]=v&255;
			buffer[1]=(v>>8)&255;
			buffer[2]=(v>>16)&255;
			buffer[3]=(v>>24)&255;
			break;
		case BIG:
			buffer[3]=v&255;
			buffer[2]=(v>>8)&255;
			buffer[1]=(v>>16)&255;
			buffer[0]=(v>>24)&255;
			break;
	}
	_io->write(buffer, 4);
}

void TBinaryFile::WriteQWord(unsigned long long v)
{
	char buffer[8];
	switch(_endian) {
		case LITTLE:
			buffer[0]=v&255;
			buffer[1]=(v>>8)&255;
			buffer[2]=(v>>16)&255;
			buffer[3]=(v>>24)&255;
			buffer[4]=(v>>32)&255;
			buffer[5]=(v>>40)&255;
			buffer[6]=(v>>48)&255;
			buffer[7]=(v>>56)&255;
			break;
		case BIG:
			buffer[7]=v&255;
			buffer[6]=(v>>8)&255;
			buffer[5]=(v>>16)&255;
			buffer[4]=(v>>24)&255;
			buffer[3]=(v>>32)&255;
			buffer[2]=(v>>40)&255;
			buffer[1]=(v>>48)&255;
			buffer[0]=(v>>56)&255;
			break;
	}
	_io->write(buffer, 8);
}

void TBinaryFile::Read(signed char* buffer, size_t count)
{
	_io->read(buffer, count);
	if (_io->fail()) {
		throw io_error("Read");
	}
}

void TBinaryFile::Read(unsigned char* buffer, size_t count)
{
	_io->read(buffer, count);
	if (_io->fail()) {
		throw io_error("ReadString");
	}
}

void TBinaryFile::ReadString(string *str,int len)
{
	if (len==0) return;
	char buffer[len+1];
	_io->read(buffer,len);
	if (_io->fail()) {
		throw io_error("ReadString failed");
	}
	(*str).erase();
	(*str).append(buffer,len);
}

void TBinaryFile::ReadString(char *str,int len)
{
	_io->read(str, len);
	if (_io->fail()) {
		throw io_error("ReadString failed");
	}
}

string TBinaryFile::ReadString(int len)
{
	char buffer[len+1];
	_io->read(buffer,len);
	if (_io->fail()) {
		throw io_error("ReadString failed");
	}
	string s;
	s.append(buffer,len);
	return s;
}

char TBinaryFile::ReadChar()
{
	char c;
	_io->read(&c,1);
	if (_io->fail()) {
		throw io_error("ReadByteFailed");
	}
	return c;
}

byte TBinaryFile::ReadByte()
{
	byte c;
	_io->read(&c,1);
	if (_io->fail()) {
		throw io_error("ReadByte failed");
	}
	return c;
}

unsigned TBinaryFile::ReadWord()
{
	unsigned char buffer[2];
	_io->read(buffer, 2);
	if (_io->fail()) {
		throw io_error("failed to read 2 bytes");
	}
	switch(_endian) {
		case LITTLE:
			return buffer[0] + 
					    (buffer[1]<<8);
			break;
		case BIG:
			return buffer[1] +
       					(buffer[0]<<8);
			break;
	}
	return 0;
}

ulong TBinaryFile::ReadDWord()
{
	unsigned char buffer[4];
	_io->read(buffer, 4);

	if (_io->fail()) {
		throw io_error("failed to read 4 bytes");
	}
	switch(_endian) {
		case LITTLE:
			return buffer[0] + 
					(buffer[1]<<8) +
					(buffer[2]<<16) +
					(buffer[3]<<24);
		case BIG:
			return buffer[3] + 
					(buffer[2]<<8) +
					(buffer[1]<<16) +
					(buffer[0]<<24);
	}
	return 0;
}

unsigned long long TBinaryFile::ReadQWord()
{
	unsigned char buffer[8];

	unsigned long long reg;

	_io->read(buffer, 8);
	if (_io->fail()) {
		throw io_error("failed to read 8 bytes");
	}

	switch(_endian) {
		case BIG:
			reg = buffer[0]; reg<<=8;
			reg+= buffer[1]; reg<<=8;
			reg+= buffer[2]; reg<<=8;
			reg+= buffer[3]; reg<<=8;
			reg+= buffer[4]; reg<<=8;
			reg+= buffer[5]; reg<<=8;
			reg+= buffer[6]; reg<<=8;
			reg+= buffer[7];
			break;
		case LITTLE:
			reg = buffer[7]; reg<<=8;
			reg+= buffer[6]; reg<<=8;
			reg+= buffer[5]; reg<<=8;
			reg+= buffer[4]; reg<<=8;
			reg+= buffer[3]; reg<<=8;
			reg+= buffer[2]; reg<<=8;
			reg+= buffer[1]; reg<<=8;
			reg+= buffer[0];
			break;
	}
	return reg;
}

bool TBinaryFile::CompareString(const char* s,int len)
{
	char buffer[len+1];
	_io->read(buffer, len);
	return strncmp(buffer,s,len)==0?true:false;
}

//---------------------------------------------------------------------------

int TBinaryFile::ReadInt()
{
	return ReadDWord();
}

void TBinaryFile::WriteInt(int d)
{
	WriteDWord(d);
}

//---------------------------------------------------------------------------

double TBinaryFile::ReadDouble()
{
	union bug {
		ieee754_double ieee;
		byte buffer[8];
	} data;
	_io->read(data.buffer, 8);
	#if __BYTE_ORDER == __LITTLE_ENDIAN
	if (_endian==BIG) {
	#else
	if (_endian==LITTLE) {
	#endif
		byte a;
		a = data.buffer[0]; data.buffer[0] = data.buffer[7]; data.buffer[7] = a;
		a = data.buffer[1]; data.buffer[1] = data.buffer[6]; data.buffer[6] = a;
		a = data.buffer[2]; data.buffer[2] = data.buffer[5]; data.buffer[5] = a;
		a = data.buffer[3]; data.buffer[3] = data.buffer[4]; data.buffer[4] = a;
	}	
	return data.ieee.d;
}

void TBinaryFile::WriteDouble(double v)
{
	union bug {
		ieee754_double ieee;
		byte buffer[8];
	} data;
	
	data.ieee.d = v;
	
	#if __BYTE_ORDER == __LITTLE_ENDIAN
	if (_endian==BIG) {
	#else
	if (_endian==LITTLE) {
	#endif
		byte a;
		a = data.buffer[0]; data.buffer[0] = data.buffer[7]; data.buffer[7] = a;
		a = data.buffer[1]; data.buffer[1] = data.buffer[6]; data.buffer[6] = a;
		a = data.buffer[2]; data.buffer[2] = data.buffer[5]; data.buffer[5] = a;
		a = data.buffer[3]; data.buffer[3] = data.buffer[4]; data.buffer[4] = a;
	}
	_io->write(data.buffer, 8);
}

//---------------------------------------------------------------------------

float TBinaryFile::ReadFloat()
{
	union bug {
		ieee754_float ieee;
		byte buffer[4];
	} data;
	_io->read(data.buffer, 4);
	#if __BYTE_ORDER == __LITTLE_ENDIAN
	if (_endian==BIG) {
	#else
	if (_endian==LITTLE) {
	#endif
		byte a;
		a = data.buffer[0]; data.buffer[0] = data.buffer[3]; data.buffer[3] = a;
		a = data.buffer[1]; data.buffer[1] = data.buffer[2]; data.buffer[2] = a;
	}
	return data.ieee.f;
}

void TBinaryFile::WriteFloat(float v)
{
	union bug {
		ieee754_float ieee;
		byte buffer[4];
	} data;
	
	data.ieee.f = v;
	
	#if __BYTE_ORDER == __LITTLE_ENDIAN
	if (_endian==BIG) {
	#else
	if (_endian==LITTLE) {
	#endif
		byte a;
		a = data.buffer[0]; data.buffer[0] = data.buffer[3]; data.buffer[3] = a;
		a = data.buffer[1]; data.buffer[1] = data.buffer[2]; data.buffer[2] = a;
	}
	
	_io->write(data.buffer, 4);
}
