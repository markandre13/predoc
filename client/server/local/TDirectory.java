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

package server.local;
import java.io.*;

class TDirectory
	implements server.TDirectory
{
	String _path;
	int _size;
	
	String _title[], _file[];

	TDirectory(String path)
	{
		_path = path;
		
		_size = 0;
		try {
			BufferedReader in;
			for(int i=0; i<2; i++) {
				in = new BufferedReader(new FileReader(path+"/$"));
				while(true) {
					String title, file;
					title = in.readLine();
					if (title==null) break;
					file = in.readLine();
					if (file==null) break;
					if (_title!=null) {
						_title[_size]=title;
						_file [_size]=file;
					}
					_size++;
				}
				if (_title==null) {
					_title = new String[_size];
					_file  = new String[_size];
					_size  = 0;
				}
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public String Name()
	{
		return "(not implemented)";
	}

	public String[] DocEntries()
	{
		return _title;
	}

	public server.TDocument Document(int n)
	{
		return new server.local.TDocument(_path+"/"+_file[n]);
	}
	
	public String[] DirEntries()
	{
		return null;
	}
	
	public server.TDirectory Directory(int n)
	{
		return null;
	}
}
