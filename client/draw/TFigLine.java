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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import common.*;
import draw.*;

class TFigLine
	extends TFigure
{
	Point p1,p2;

	public TFigLine()
	{
		p1 = null;
		p2 = null;
	}
	
	public TFigLine(Point a, Point b)
	{
		p1 = new Point(a);
		p2 = new Point(b);
	}

	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		out.writeInt(p1.x);
		out.writeInt(p1.y);
		out.writeInt(p2.x);
		out.writeInt(p2.y);
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		p1 = new Point(in.readInt(), in.readInt());
		p2 = new Point(in.readInt(), in.readInt());
	}

	public void paint(Graphics pen)
	{
		pen.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	public void paintHandles(Graphics pen)
	{
		drawHandle(pen, p1.x, p1.y);
		drawHandle(pen, p2.x, p2.y);
	}
	
	public int getHandle(double x, double y)
	{
		if (isInHandle(p1.x,p1.y, x,y))
			return 0;
		if (isInHandle(p2.x,p2.y, x,y))
			return 1;
		return -1;	
	}
	
	public void translateHandle(int handle, int nx, int ny)
	{
		switch(handle) {
			case 0:
				p1.x=nx; p1.y=ny;
				break;
			case 1:
				p2.x=nx; p2.y=ny;
				break;
		}
	}

	public Point getRefPoint(int x, int y)
	{
		return new Point(x-p1.x, y-p1.y);
	}

	public void getBounding(Point q1, Point q2)
	{
		q1.x = p1.x;
		q1.y = p1.y;
		q2.x = p2.x;
		q2.y = p2.y;
	}

	public void translate(Point ref, int x, int y)
	{
		int dx,dy;
		dx = p2.x-p1.x;
		dy = p2.y-p1.y;
		p1.x = x-ref.x;
		p1.y = y-ref.y;
		p2.x = p1.x+dx;
		p2.y = p1.y+dy;
	}

	public double Distance(int x, int y)
	{
		return getDistanceLine(x,y, p1.x,p1.y, p2.x,p2.y);
	}
}
