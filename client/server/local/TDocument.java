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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package server.local;

import java.io.*;

class TDocument
	implements server.TDocument
{
	String _dir;
	int _size;

	TDocument(String dir)
	{
		_dir = dir;
		_size = 0;
		try {
			File file = new File(_dir);
			String list[] = file.list(
				new FilenameFilter() {
					public boolean accept(File d, String name) {
						return name.startsWith("page") && name.endsWith(".gif");
					}
				}
			);
			_size = list.length;
		}
		catch (Exception e)
		{
		}
	}

	public int NumPages()
	{
		return _size;
	}
	
	public server.TPage Page(int page)
	{
		return new server.local.TPage(_dir+"/page"+(page+1)+".gif");
	}

	public String[] Corrections()
	{
		File file = new File(_dir);
		String[] lst = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".pjf");
			}
		});
		return lst;
	}
	
	public void SaveCorrection(String name, byte data[])
	{
	 	String file = _dir+"/"+name;
	 	if (!file.endsWith(".pjf"))
	 		file+=".pjf";
	 	try {
	 		FileOutputStream ostream = new FileOutputStream(file);
	 		ostream.write(data);
	 	}
	 	catch(Exception e) {
	 		// throw e;
	 	}
	}

	public byte[] LoadCorrection(String name)
	{
		byte a[] = null;
	 	String file = _dir+"/"+name;
	 	if (!file.endsWith(".pjf"))
	 		file+=".pjf";
		try {
			File f = new File(file);
			a = new byte[(int)f.length()];
			FileInputStream in = new FileInputStream(file);
			in.read(a);
		}
		catch(Exception e) {
			// throw e
			a = null;
		}
		return a;
	}
}