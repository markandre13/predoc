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

package common;

import common.TPoint;

import java.util.Vector;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.*;

public class TPolygon
{
	Vector _data;
	
	public TPolygon()
	{
		_data = new Vector();
	}

	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		int n = _data.size();
		out.writeInt(n);
		for(int i=0; i<n; i++) {
			TPoint p = getPoint(i);
			out.writeDouble(p.x);
			out.writeDouble(p.y);
		}
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		_data.removeAllElements();
		int n = in.readInt();
		for(int i=0; i<n; i++) {
			double x = in.readDouble();
			double y = in.readDouble();
			addPoint(x,y);
		}
	}

	public void addPoint(double x, double y)
	{
		addPoint(new TPoint(x,y));
	}
	
	public void addPoint(TPoint p)
	{
		_data.addElement(p);
	}

	public TPoint getPoint(int i)
	{
		return (TPoint)_data.elementAt(i);
	}
	
	public Polygon GetAWTPolygon(int w, int h)
	{
		Polygon p = new Polygon();
		int n = _data.size();
		for(int i=0; i<n; i++) {
			TPoint pt = (TPoint)_data.elementAt(i);
			p.addPoint((int)(pt.x*w), (int)(pt.y*h));
		}
		
		if (p.npoints != _data.size()) {
			System.out.println("common/TPolygon: ARGH! THE AWT REDUCED MY BELOVED POLYGON FROM "+_data.size()+" POINTS TO "+p.npoints);
		}
		
		return p;
	}
	
	public Rectangle GetAWTRectangle(int w, int h)
	{
		double min_x=0, min_y=0, max_x=0, max_y=0;
		int n = _data.size();
		if (n>0) {
			TPoint pt = (TPoint)_data.elementAt(0);
			min_x = max_x = pt.x;
			min_y = max_y = pt.y;
		}
		for(int i=1; i<n; i++) {
			TPoint pt = (TPoint)_data.elementAt(i);
//System.out.println("Pt("+pt.x+","+pt.x+") GetAWTRectangle: ("+min_x+","+min_y+","+max_x+","+max_y+")");
			if (pt.x<min_x)
				min_x = pt.x;
			else if (pt.x>max_x)
				max_x = pt.x;
			if (pt.y<min_y)
				min_y = pt.y;
			else if (pt.y>max_y)
				max_y = pt.y;
		}
		return new Rectangle((int)(min_x*w), (int)(min_y*h), 
												 (int)((max_x-min_x)*w), (int)((max_y-min_y)*h));
	}
}
