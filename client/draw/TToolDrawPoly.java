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

public class TToolDrawPoly
	extends TTool
{
	TFigPoly _fig;
	int _x, _y, _mx, _my;

	public TToolDrawPoly(TDrawingArea c)
	{
		super(c);
	}

	public void start()
	{
		_fig = new TFigPoly();
	}

	public void mousePressed(MouseEvent e)
	{
		int x = e.getX(), y = e.getY();
		if (x==_x && y==_y) {
			Add(_fig);
			start();
			return;
		}
		_mx = _x = x; _my = _y = y;
		_fig.Add(new Point(x, y));
		Invalidate();
	}

	public void mouseMoved(MouseEvent e)
	{
		if (_fig.Size()>0) {
			_mx = e.getX();
			_my = e.getY();
			Invalidate();
		}
	}
	
	public void paint(Graphics pen)
	{
		_fig.paint(pen);
		if (_fig.Size()>0)
			pen.drawLine(_x, _y, _mx, _my);
	}
}
