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

import java.io.*;
import java.util.Vector;
import java.awt.Polygon;

import common.*;
import draw.TDrawingData;

/**
 * Stores all corrections for a single page.
 */
class TDPageCorrection
{
	Vector _data;

	class TNode
	{
		public TNode(TPolygon a, TDrawingData d)
		{
			area = a;
			drawing = d;
		}
	
		public TPolygon area;
		public TDrawingData drawing;
	}

	public TDPageCorrection()
	{
		_data = new Vector();
	}

	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		int n = _data.size();
		out.writeInt(n);					// number of entries in this correction
		for(int i=0; i<n; i++) {
			TNode node = (TNode)_data.elementAt(i);
			node.area.Store(out, version);
			node.drawing.Store(out, version);
		}
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		_data.removeAllElements();
		int n= in.readInt();
		for(int i=0; i<n; i++) {
			TPolygon poly = new TPolygon();
			poly.Restore(in, version);
			TDrawingData drawing = new TDrawingData();
			drawing.Restore(in, version);
			_data.addElement(new TNode(poly, drawing));
		}
	}
	
	public void Add(TPolygon poly, TDrawingData data)
	{
		_data.addElement(new TNode(poly, data));
	}

	public void Remove(int n)
	{
		_data.removeElementAt(n);
	}
	
	public int Size()
	{
		return _data.size();
	}

	Polygon Polygon(int n, int w, int h)
	{
		return ((TNode)_data.elementAt(n)).area.GetAWTPolygon(w,h);
	}

	TPolygon Polygon(int n)
	{
		return ((TNode)_data.elementAt(n)).area;
	}

	TDrawingData DrawingData(int n)
	{
		return ((TNode)_data.elementAt(n)).drawing;
	}
}
