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

public class TToolDrawRect
	extends TTool
{
	Point p1,p2;
	
	TFigRect _fig;

	public TToolDrawRect(TDrawingArea c)
	{
		super(c);
		_fig = new TFigRect();
		p1 = new Point();
		p2 = new Point();
	}

	public void mousePressed(MouseEvent e)
	{
		p1.x = e.getX();
		p1.y = e.getY();
	}
	
	public void mouseDragged(MouseEvent e)
	{
		p2.x = e.getX();
		p2.y = e.getY();
		Set();
		Invalidate();
	}

	public void mouseReleased(MouseEvent e)
	{
		p2.x = e.getX();
		p2.y = e.getY();
		Set();
		Add(new TFigRect(_fig));
	}

	public void paint(Graphics pen)
	{
		_fig.paint(pen);
	}
	
	void Set()
	{
		int x,y,w,h;
		w=p2.x-p1.x+1;
		h=p2.y-p1.y+1;
		if (w>=0) {	x=p1.x;	} else { x=p2.x; w=-w; }
		if (h>=0) {	y=p1.y;	} else { y=p2.y; h=-h; }
		_fig.Set(x,y,w,h);
	}
}
