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

class TFigPoly
	extends TFigure
{
	Vector _points;
	
	public TFigPoly()
	{
		_points = new Vector();
	}

	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		int n = _points.size();
		out.writeInt(n);
		for(int i=0; i<n; i++) {
			Point p = (Point)_points.elementAt(i);
			out.writeInt(p.x);
			out.writeInt(p.y);
		}
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		_points.removeAllElements();
		int n = in.readInt();
		for(int i=0; i<n; i++) {
			_points.addElement(new Point(in.readInt(), in.readInt()));
		}
	}


	public void Add(Point p)
	{
		_points.addElement(p);
	}
	
	int Size()
	{
		return _points.size();
	}

	public void paint(Graphics pen)
	{
		int n = _points.size();
		if (n<1)
			return;
		Point p1,p2;
		p2=(Point)_points.elementAt(0);
		for(int i=1; i<n; i++) {
			p1=p2;
			p2=(Point)_points.elementAt(i);
			pen.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	public void paintHandles(Graphics pen)
	{
		int n = _points.size();
		for(int i=0; i<n; i++) {
			Point p=(Point)_points.elementAt(i);
			drawHandle(pen, p.x, p.y);
		}
	}
	
	public int getHandle(double x, double y)
	{
		int n = _points.size();
		for(int i=0; i<n; i++) {
			Point p=(Point)_points.elementAt(i);
			if (isInHandle(p.x, p.y, x,y))
				return i;
		}
		return -1;	
	}
	
	public void translateHandle(int handle, int nx, int ny)
	{
		Point p=(Point)_points.elementAt(handle);
		p.x = nx;
		p.y = ny;
	}

	public Point getRefPoint(int x, int y)
	{
		if (_points.size()==0)
			return null;
//		return (Point)_points.elementAt(0);
		return new Point(x,y);
	}

	public void getBounding(Point q1, Point q2)
	{
		if (_points.size()==0)
			return;
		Point p;

		p = (Point)_points.elementAt(0);
		q1.x = q2.x = p.x;
		q1.y = q2.y = p.y;
		int n = _points.size();
		for(int i=1; i<n; i++) {
			p = (Point)_points.elementAt(i);
			if (p.x < q1.x)
				q1.x = p.x;
			else if (p.x > q2.x)
				q2.x = p.x;
			if (p.y < q1.y)
				q1.y = p.y;
			else if (p.y > q2.y)
				q2.y = p.y;
		}
	}

	public void translate(Point ref, int x, int y)
	{
		int dx = ref.x-x;
		int dy = ref.y-y;
		int n = _points.size();
		for(int i=0; i<n; i++) {
			Point p=(Point)_points.elementAt(i);
			p.x-=dx;
			p.y-=dy;
		}
		ref.x = x;
		ref.y = y;
	}

	public double Distance(int x, int y)
	{
		double min = OUT_OF_RANGE;
		int n = _points.size();
		if (n<1)
			return min;
		Point p1,p2;
		p2=(Point)_points.elementAt(0);
		for(int i=1; i<n; i++) {
			p1=p2;
			p2=(Point)_points.elementAt(i);
			double m = getDistanceLine(x,y, p1.x,p1.y, p2.x,p2.y);
			if (m<min)
				min = m;
		}
		return min;
	}
}
