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

public class TFigure
{
	public void paint(Graphics pen)	{}

	// interface for the selection tool
	//------------------------------------------------------
	public void paintHandles(Graphics pen) {}
	public int getHandle(double x, double y) { return -1;	}
	public Point getHandlePosition(int handle) { return null; }
	public void translateHandle(int handle, int nx, int ny)	{}
	public Point getRefPoint(int x, int y) { return null; }
	public void translate(Point ref, int x, int y) {}
	public void getBounding(Point p1, Point p2) {}

	public boolean mousePressed(MouseEvent e) { return false; }
	public boolean mouseDragged(MouseEvent e) { return false; }
	public boolean mouseReleased(MouseEvent e) { return false; }

	// here should be a method to start the figures modification process;
	// currently this is done for `TFigText' with special code in
	// `TToolSelect' but as we all know this tool shouldn't know more
	// of a figure as defined here in the `TFigure' class
	// one exception to this is the TFigCorrection object which needs
	// keyboad events	

	// distance utility methods
	//------------------------------------------------------
	public static final double OUT_OF_RANGE = 100.0;
	public static final double RANGE = 10.0;
	public static final double RANGE_DONT_MOVE = -1.0;

	public double Distance(int x, int y)
	{
		return OUT_OF_RANGE;
	}
	
	public static double getDistanceLine(double x, double y, 
																int _x1, int _y1, 
																int _x2, int _y2)
	{
		double bx = _x2  - _x1;
		double by = _y2 - _y1;
		double ax = x - _x1;
		double ay = y - _y1;
		double lb = bx*bx+by*by;
		double t = ((x-_x1) * bx + (y-_y1) * by) / lb;
		if (t<0.0 || t>1.0)
			return OUT_OF_RANGE;
		double d  = (by * ax - bx * ay) / Math.sqrt(lb);
		return Math.abs(d);
	}

	// handle utility methods
	//-------------------------------------------------------------------------
	public static void drawHandle(Graphics pen, int x, int y)
	{
		pen.fillRect(x-2,y-2,5,5);
	}

	public static boolean isInHandle(int hx,int hy, double x, double y)
	{
		return (hx-2<=x && x<=hx+2 && hy-2<=y && y<=hy+2);
	}

	// read/write figure from/to stream
	//-------------------------------------------------------------------------
	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{	}
	
	/**
	*	Write a `TFigure' to a stream and adds a type id for `SRestore'.
	*/
	public static void SStore(TFigure fig, DataOutputStream out, int version)
		throws java.io.IOException
	{
		int type=-1;
		if (fig instanceof TFigCorrection)
			type = 1;
		else if (fig instanceof TFigText)	// sub-class of TFigRect
			type = 6;
		else if (fig instanceof TFigCirc)	// sub-class of TFigRect
			type = 3;
		else if (fig instanceof TFigRect)
			type = 2;
		else if (fig instanceof TFigLine)
			type = 4;
		else if (fig instanceof TFigPoly)
			type = 5;
		if (type>=0) {
			out.writeInt(type);
			fig.Store(out, version);
		}
	}
	
	/**
	* Create a figure from the stream.
	*/
	public static TFigure SRestore(DataInputStream in, int version)
		throws java.io.IOException
	{
		int type = in.readInt();
		TFigure fig = null;
		switch(type) {
			case 1:
				fig = new TFigCorrection();
				break;
			case 2:
				fig = new TFigRect();
				break;
			case 3:
				fig = new TFigCirc();
				break;
			case 4:
				fig = new TFigLine();
				break;
			case 5:
				fig = new TFigPoly();
				break;
			case 6:
				fig = new TFigText();
				break;
		}
		if (fig!=null) {
			fig.Restore(in, version);
		}
		return fig;
	}
}
