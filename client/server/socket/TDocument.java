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

import server.socket.TSocketConnection;

class TDocument
	implements server.TDocument
{
	TSocketConnection _connection;
	String _path;
	
	TDocument(TSocketConnection connection, String path)
	{
		_connection = connection;
		_path = path;
	}

	public int NumPages()
	{
System.out.println("server.socket.TDocument.NumPages()");
		try {
			_connection.SendCommand(TSocketConnection.CMD_DOCUMENT_SIZE);
			_connection.WriteString(_path);
			while(true) {
				int result = _connection.ReadByte();
				if (result==TSocketConnection.ACK_OK)
					break;
				if (result==TSocketConnection.ACK_WAIT) {
					System.out.println("Server is serving request, please wait");
					continue;
				}
				throw new Exception("server couldn't send document size");
			}
			return (int)_connection.ReadLong();
		}
		catch(Exception e) { 
			System.out.println("NumPages failed: "+e.getMessage());
		}
		return 0;
	}
	
	public server.TPage Page(int page)
	{
		return new server.socket.TPage(_connection, _path, page);
	}

	// this is almost the same code as TDirectory.Entries:
	// server.socket.TDocument.Corrections
	//----------------------------------------------------------------------
	public String[] Corrections()
	{
		System.out.println("getting list of corrections");
		String _entries[] = null;
		try {
			_connection.SendCommand(TSocketConnection.CMD_DOCUMENT_ENTRIES);
			_connection.WriteString(_path);
			System.out.println("send CMD_DOCUMENT_ENTRIES: \""+_path+"\"");
			if (_connection.ReadByte()!=TSocketConnection.ACK_OK)
				throw new Exception("server couldn't send list of corrections");
			int n = (int)_connection.ReadLong();
			_entries = new String[n];
			for(int i=0; i<n; i++) {
				_entries[i] = _connection.ReadString();
			}
			return _entries;
		}
		catch(Exception e) {
			System.out.println("Corrections failed: "+e.getLocalizedMessage());
		}
		return null;
	}

	public void SaveCorrection(String name, byte data[])
	{
		System.out.println("NOT IMPLEMENTED YET");
	}

	public byte[] LoadCorrection(String name)
	{
		try {
			System.out.println("server.socket.TDocument.LoadCorrection");
			_connection.SendCommand(TSocketConnection.CMD_GET_CORRECTION);
			_connection.WriteString(_path);
			_connection.WriteString(name);
			if (_connection.ReadByte()!=TSocketConnection.ACK_OK) {
				throw new Exception("server couldn't send correction data");
			}
			return _connection.ReadBytes();
		} catch (Exception e) {
			System.out.println("LoadCorrection failed: "+e.getLocalizedMessage());
		}
		return null;
	}
}
