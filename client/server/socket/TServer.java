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

public class TServer
	implements server.TServer
{
	TSocketConnection _connection;
	
	public TServer(String hostname, int port)
		throws Exception
	{
		_connection = new TSocketConnection(hostname, port, "anonymous", "anonymous");
	}
	
	public String Name()
	{
		return _connection._hostname+":"+_connection._port;
	}
	
	public server.TDirectory RootDirectory()
	{
System.out.println("server.socket.TServer.RootDirectory()");
		return new server.socket.TDirectory(_connection);
	}
}
