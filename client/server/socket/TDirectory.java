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

class TDirectory
	implements server.TDirectory
{
	TSocketConnection _connection;
	String _path;
	
	String _doc_entries[];
	String _dir_entries[];

	TDirectory(TSocketConnection connection)
	{
		_doc_entries = null;
		_connection = connection;
		_path = "/";
	}

	TDirectory(TSocketConnection connection, String path)
	{
		_doc_entries = null;
		_connection = connection;
		_path = path;
		if ( _path.charAt(_path.length()-1) != '/' )
			_path+="/";
		System.out.println("new directory: \""+_path+"\"");
	}

	public String Name()
	{
		return "(not implemented)";
	}

	public String[] DocEntries()
	{
		_Fetch();
		return _doc_entries;
	}
	
	public server.TDocument Document(int n)
	{
System.out.println("server.socket.TDirectory.Document("+n+")");
		_Fetch();
		return new server.socket.TDocument(_connection, _path+_doc_entries[n]);
	}
	
	public String[] DirEntries()
	{
		_Fetch();
		return _dir_entries;
	}
	
	public server.TDirectory Directory(int n)
	{
		_Fetch();
		return new server.socket.TDirectory(_connection, _dir_entries[n]);
	}
	
	private void _Fetch()
	{
		if (_doc_entries!=null) {
			return;
		}
		try {
			System.out.println("reading directory: \""+_path+"\"");
			_connection.SendCommand(TSocketConnection.CMD_DIRECTORY_ENTRIES);
			_connection.WriteString(_path);
			
			int result = _connection.ReadByte();
			if (result!=TSocketConnection.ACK_OK) {
				throw new Exception("server couldn't send contents of directory \""+_path+"\"");
			}
			
			int n;
			n = (int)_connection.ReadLong();
			_doc_entries = new String[n];
			for(int i=0; i<n; i++) {
				_doc_entries[i] = _connection.ReadString();
			}

			n = (int)_connection.ReadLong();
			_dir_entries = new String[n];
			for(int i=0; i<n; i++) {
				_dir_entries[i] = _connection.ReadString();
			}
		}
		catch(Exception e) {
			System.out.println("server.socket.TDirectory._Fetch(): "+e.getMessage());
		}
	}
}
