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

import java.awt.Image;
import java.awt.MediaTracker;
import java.io.ByteArrayInputStream;
import java.awt.Component;

import server.socket.TSocketConnection;

// that's the PNG decoder:
import com.visualtek.png.*;

class TPage
	implements server.TPage
{
	TSocketConnection _connection;
	String _path;
	long _page;

	TPage(TSocketConnection connection, String path, long page)
	{
		_connection = connection;
		_path = path;
		_page = page;
	}

	public Image Image(Component wnd)
	{
		try {
			System.out.println("server.socket.TPage.Image");
			_connection.SendCommand(TSocketConnection.CMD_BITMAP);
			_connection.WriteString(_path);
			_connection.WriteLong(_page);
			System.out.println("  command send, waiting for reply");
			while(true) {
				int result = _connection.ReadByte();
				if (result==TSocketConnection.ACK_WAIT) {
					System.out.println("Server is serving request, please wait");
					continue;
				}
				if (result==TSocketConnection.ACK_OK)
					break;
				throw new Exception("server couldn't send bitmap for page "+_page);
			}

			System.out.println("  receiving data...");

			long start, stop;

			start = System.currentTimeMillis();
	    byte[] data = _connection.ReadBytes();
	    stop = System.currentTimeMillis();

	    System.out.println("  received "+data.length+" bytes in "+(stop-start)+"ms");

			start = System.currentTimeMillis();
	    ByteArrayInputStream stream = new ByteArrayInputStream(data);

	    Image _image = new PNGDecoder(stream).decode();

	    MediaTracker _tracker = new MediaTracker(wnd);
  	  _tracker.addImage(_image, 0);
	    _tracker.waitForAll();
	    stop = System.currentTimeMillis();

			System.out.println("  created image from data in "+(stop-start)+"ms");

	    return _image;
		} catch (Exception e) {
			System.out.println("server.socker.TPage.Image failed: "+e.getMessage());
		}
		
		return null;
  }
}