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

package draw;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import common.*;
import draw.*;

public class TFigImage
	extends TFigRect
{
	protected Image _img;	

	public TFigImage(Image img, int x, int y)
	{
		_img = img;
		int w = img.getWidth(null), h = img.getHeight(null);
		// System.out.println("TFigImage size:"+w+","+h);
		Set(x, y, w, h);
	}

	public void SetImage(Image img)
	{
		_img = img;
		if (_img!=null) {
			int w = img.getWidth(null), h = img.getHeight(null);
			Set(p1.x, p1.y, w, h);
		}
	}

	public void paint(Graphics pen)
	{
		if (_img!=null) {
			pen.drawImage(_img, p1.x, p1.y, null);
		}
	}

	public int getHandle(double x, double y)
	{
		return -1;
	}
	
	public double Distance(int x, int y)
	{
		// System.out.println(x+","+y+" in "+p1.x+"-"+p2.x+","+p1.y+"-"+p2.y);
		return (p1.x<=x && x<=p2.x && p1.y<=y && y<=p2.y) ? RANGE : OUT_OF_RANGE;
	}
}
