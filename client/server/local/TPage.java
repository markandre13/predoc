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

import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.*;

class TPage
	implements server.TPage
{
	String _file;
	
	TPage(String file)
	{
		_file = file;
	}
	
	public java.awt.Image Image(java.awt.Component wnd)
	{
		System.out.println("loading file "+_file);
		java.awt.Image img = null;
		try {
			img = Toolkit.getDefaultToolkit().getImage(_file);
			MediaTracker tracker = new MediaTracker(wnd);
			tracker.addImage(img, 0);
			tracker.waitForAll();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}	
		return img;
	}
}
