/*
 *
 * PReDoc - an editor for proof-reading digital documents
 * Copyright (C) 1998 by Mark-André Hopf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package server.socket;

import java.net.Socket;
import java.io.*;

public class TSocketConnection
{
	Socket _sock;
	String _hostname;
	String _username;
	String _password;
	int _port;
	DataOutputStream _out;
	DataInputStream _in;

	public final static int CMD_LOGIN = 1;
	public final static int CMD_DIRECTORY_ENTRIES = 2;
	public final static int CMD_DOCUMENT_SIZE = 3;
	public final static int CMD_BITMAP = 4;
	public final static int CMD_DOCUMENT_ENTRIES = 5;
	public final static int CMD_GET_CORRECTION = 6;

	public final static int ACK_FAILED = 0;
	public final static int ACK_OK = 1;
	public final static int ACK_WAIT = 2;

	public TSocketConnection(String hostname, int port, String username, String password)
		throws Exception
	{
		_hostname = hostname;
		_port = port;
		_username = username;
		_password = password;
	 	_sock = new Socket(hostname, port);
	 	_out = new DataOutputStream(_sock.getOutputStream());
	 	_in  = new DataInputStream(_sock.getInputStream());

//		System.out.println("logging in as user \""+_username+"\"");
	 	SendCommand(CMD_LOGIN);
	 	WriteString(_username);
	 	WriteString(_password);
	 	int result = ReadByte();
	 	if (result!=ACK_OK) {
//	 		System.out.println("user validation failed");
	 		throw new Exception("user validation failed");
	 	}
//	 	System.out.println("login succeded");
	}
	
	public void SendCommand(int cmd) 
		throws IOException
	{
		WriteInt(cmd);
	}

	public void WriteInt(int n)
		throws IOException
	{
		_out.writeInt(n);
	}
	
	public void WriteLong(long n)
		throws IOException
	{
		_out.writeLong(n);
	}
	
	public void WriteString(String s) 
		throws IOException
	{
		_out.writeInt(s.length());
		_out.writeBytes(s);
	}

	public int ReadByte()
		throws IOException
	{
		return _in.readByte();
	}
	
	public int ReadInt() 
		throws IOException
	{
		return _in.readInt();
	}
	
	public long ReadLong() 
		throws IOException
	{
		return _in.readLong();
	}
	
	public String ReadString() 
		throws IOException
	{
		int l = _in.readInt();
		byte b[] = new byte[l];
		_in.readFully(b);
		return new String(b);
	}
	
	public byte[] ReadBytes()
		throws IOException
	{
//System.out.println("        read bytes: waiting for length");
		int l = (int)_in.readLong();
//System.out.println("        read bytes: length is "+l);
		if (l==0)
			return null;
//System.out.println("        read bytes: created array, receiving data");
		byte b[] = new byte[l];
		_in.readFully(b);
//System.out.println("        read bytes: got it!");
		return b;
	}
}
