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

class TFigRect
	extends TFigure
{
	protected Point p1, p2;
	
	public TFigRect()
	{
		p1 = new Point();
		p2 = new Point();
	}
	
	public TFigRect(TFigRect o)
	{
		p1 = new Point(o.p1);
		p2 = new Point(o.p2);
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

	public void Set(int x, int y, int w, int h)
	{
		p1.x = x;
		p1.y = y;
		p2.x = x+w-1;
		p2.y = y+h-1;
	}

	public void paint(Graphics pen)
	{
		int x1=p1.x, y1=p1.y, x2=p2.x, y2=p2.y;
		if (x1>x2) {
			int a = x1;
			x1 = x2;
			x2 = a;
		}
		if (y1>y2) {
			int a = y1;
			y1 = y2;
			y2 = a;
		}

		pen.drawRect(x1, y1, x2-x1+1, y2-y1+1);
	}
	
	public void paintHandles(Graphics pen)
	{
		drawHandle(pen, p1.x, p1.y);
		drawHandle(pen, p2.x, p1.y);
		drawHandle(pen, p2.x, p2.y);
		drawHandle(pen, p1.x, p2.y);
	}

	public int getHandle(double x, double y)
	{
		if (isInHandle(p1.x,p1.y, x,y))
			return 0;
		if (isInHandle(p2.x,p1.y, x,y))
			return 1;
		if (isInHandle(p2.x,p2.y, x,y))
			return 2;
		if (isInHandle(p1.x,p2.y, x,y))
			return 3;
		return -1;	
	}

	public void translateHandle(int handle, int nx, int ny)
	{
		switch(handle) {
			case 0:
				p1.x=nx; p1.y=ny;
				break;
			case 1:
				p2.x=nx; p1.y=ny;
				break;
			case 2:
				p2.x=nx; p2.y=ny;
				break;
			case 3:
				p1.x=nx; p2.y=ny;
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
		/*
		if (_fill_color!=null) {
			return (p1.x<=x || x<=p2.x || p1.y<=y || y<=p2.y) ? RANGE : OUT_OF_RANGE;
		} else {
		*/
			int i, x1,y1,x2,y2;
			double min, d;
			x1=y1=x2=y2=0;
			min=d=0.0;
		
			for(i=0; i<4; i++) {
				switch(i) {
				case 0:
					x1=p1.x; y1=p1.y; x2=p2.x; y2=p1.y;
					break;
				case 1:
					x1=p2.x; y1=p2.y;
					break;
				case 2:
					x2=p1.x; y2=p2.y;
					break;
				case 3:
					x1=p1.x; y1=p1.y;
					break;
				}
				d = getDistanceLine(x,y, x1,y1,x2,y2);
				if(i==0 || d<min)
					min = d;
			}
			return min;
		/*
		}	
		*/
	}
}
