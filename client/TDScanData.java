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

import java.awt.Rectangle;
import java.awt.Polygon;
import java.util.Vector;

import common.*;

/**
 * Stores the information created by the Scanner.
 *
 * Rectangles must be added top-down, from the left to the right.
 * The current implementation of `GetPoly' depends on it.
 */
class TDScanData
{
	TRectangle _br;					// content bounding rectangle
	Vector _lr;							// letter bounding rectangles

	TLineBuffer _buffer;
	TLine _current;

	public TDScanData()
	{
		_br = null;
		_lr = new Vector();
		_current = null;
		_buffer = new TLineBuffer();
	}

	public void SetBR(TRectangle r)	{	_br = r; }
	TRectangle BR()	{	return _br;	}

  // normalized to page size
  //-------------------------
	public void AddLetterRect(TRectangle r)
	{
		_current.addElement(r);
	}

	public void NewLine()
	{
		_current = new TLine();
		_buffer.addElement(_current);
	}

	// debuging only (currently)	
	public TRectangle LetterRect(int n)
	{
		if (n<_lr.size()) {
			return (TRectangle)_lr.elementAt(n);
		}
		return null;
	}
	 
	public int GetIndex1(double x, double y)	{ 
		return GetIndex(x,y,-1);
	}

	public int GetIndex2(double x, double y, int idx1) { 
		return GetIndex(x,y, idx1);
	}

	protected int GetIndex(double x, double y, int idx1)
	{
		TLine line;

		// ouch, why haven't i coded this loop as a binary search???
		// (because it's fast enough, lines might intersect or
		// someday there might be scan data with more than just one
		// column and i don't like to implement a second array
		// to handle this yet)
		//----------------------------------------------------------
		int n = _buffer.size();
		for(int i=0; i<n; i++) {
			line = _buffer.elementAt(i);
			if (line.min_y<=y && y<=line.max_y) {
				int idx = line.GetIndex(x,y,true);	// ok, this is a binary search...
				if (idx>=0)
					return idx;
			}
		}
		
		if (idx1==-1)
			return -1;

		// binary search for line with index `idx1'
		//------------------------------------------
		int p1=0, p2=_buffer.size()-1, m;
		while(true) {
			m = ((p2-p1)>>1)+p1;
			line = _buffer.elementAt(m);
			if (line.min_idx<=idx1 && idx1<line.min_idx+line.size()) {
				break;
			}
			if (p1>p2) return -1;		// paranoia check
			if (idx1<line.min_idx) {
				p2=m-1;
			} else {
				p1=m+1;
			}
		}

		if (y<line.min_y) {
			// upwards
			for(int i=0; i<=m; i++) {
				line = _buffer.elementAt(i);
				if (line.min_y>=y) {
					return line.min_idx;
				}
			}
		} else {
			// downwards
			for(int i=n-1; i>m; i--) {
				line = _buffer.elementAt(i);
				if (line.max_y<=y) {
					return line.min_idx+line.size()-1;
				}
			}
		}
		return -1;
	}


	// each TPolygon will have 8 points which is necessary for TFigCorrection
	public TPolygon GetPoly(int i1, int i2, double x1, double x2, double fuzzy)
	{
// System.out.println("TDScanData.GetPoly("+i1+","+i2+","+x1+","+x2+","+fuzzy+")");
		TPolygon p = new TPolygon();

		// make (i1,x1) the upper and/or left pair
		//-----------------------------------------
		if (i1>i2) {
			int a;
			a=i1; i1=i2; i2=a;
			double b;
			b=x1; x1=x2; x2=b;
		}

		if (i1!=-1 && i2!=-1) {
			TRectangle r1 = (TRectangle)_lr.elementAt(i1);	// letter rect for i1
/*
System.out.println("range: "+Math.abs(r1.x-x1));
System.out.println("r1.x:"+r1.x+" x1:"+x1);
			if (Math.abs(r1.x-x1) > fuzzy) {
				r1.w = r1.x+r1.w-x1;
				r1.x = x1;
				if (r1.w<=0) {
					r1.w = 1;
				}
			}
*/
			// i1==i2:
			//--------

			if (i1==i2) {
//				System.out.println("i1==i2");
				double left = r1.x, right = r1.x+r1.w;
				boolean flat = true;
				if (Math.abs(left-x1)>fuzzy) {
					flat = false;
					left = x1;
				}
				if (Math.abs(right-x2)>fuzzy) {
					right= x2;
					flat = false;
				}
				if (flat) {
					p.addPoint(r1.x, r1.y);
					p.addPoint(r1.x, r1.y);
					p.addPoint(r1.x, r1.y);
					p.addPoint(r1.x, r1.y+r1.h);
					p.addPoint(r1.x, r1.y+r1.h);
					p.addPoint(r1.x, r1.y+r1.h);
					p.addPoint(r1.x, r1.y+r1.h);
					p.addPoint(r1.x, r1.y);
				} else {
					if (left>right) {
						double a = left; left = right; right = a;
					}
					p.addPoint(left,  r1.y);
					p.addPoint(left,  r1.y);
					p.addPoint(right, r1.y);
					p.addPoint(right, r1.y+r1.h);
					p.addPoint(right, r1.y+r1.h);
					p.addPoint(right, r1.y+r1.h);
					p.addPoint(left,  r1.y+r1.h);
					p.addPoint(left,  r1.y);
				}
				return p;
			}

			// i1!=i2 :
			//---------
			
			TRectangle r2 = (TRectangle)_lr.elementAt(i2);
/*
System.out.println("r2.x:"+r2.x+" x2:"+x2);
			if (Math.abs(r2.x-x2) > fuzzy) {
				r2.w = r2.x+r2.w-x2;
				r2.x = x2;
				if (r2.w<=0) {
					r2.w = 1;
				}
			}
*/
			// calculate left & right side
			double sright = r1.x + r1.w;
			double sleft  = r2.x;
			for(int i=i1+1; i<i2; i++) {
				TRectangle r = (TRectangle)_lr.elementAt(i);
				if (sright < r.x+r.w)
					sright = r.x+r.w;
				if (sleft > r.x)
					sleft = r.x;
			}

			double left = r1.x, right = r2.x+r2.w;
			if (Math.abs(left-x1)>fuzzy)
				left = x1;
			if (Math.abs(right-x2)>fuzzy)
				right= x2;

			// don't exceed the page limits
			if (left<0.0) left=0.0;
			if (left>1.0) left=1.0;
			if (right<0.0) right=0.0;
			if (right>1.0) right=1.0;

			// don't let the polygon intersect itself
			if (right<sleft)
				sleft = right;
	
			if (left>sright)
				sright = left;

			p.addPoint(left			, r1.y+r1.h);
			p.addPoint(left			, r1.y);
			p.addPoint(sright		, r1.y);
			p.addPoint(sright		, r2.y);
			p.addPoint(right		, r2.y);
			p.addPoint(right		, r2.y+r2.h);
			p.addPoint(sleft		, r2.y+r2.h);
			p.addPoint(sleft		, r1.y+r1.h);
			return p;
		}
		
		return null;
	}

	class TLine
	{
		int min_idx, _size;				// the lines window in the `_lr' array
		double min_y, max_y;			// vertical range of the line
		TLine() { _size=0; }
		void addElement(TRectangle rect)
		{ 
			if (_size==0) {
				min_idx = _lr.size();
				min_y = rect.y;
				max_y = rect.y+rect.h;
			} else {
				if (min_y > rect.y)
					min_y = rect.y;
				if (max_y < rect.y+rect.h)
					max_y = rect.y+rect.h;

				// add an extra space character (just to simplify the other methods)
				TRectangle last = elementAt(_size-1);
				double x,y,w,h, a1,a2;
				x = last.x+last.w;
				y = Math.min(rect.y, last.y);
				w = rect.x-(last.x+last.w);
				a1 = rect.y+rect.h;
				a2 = last.y+last.h;
				h = a1>a2 ? a1-y : a2-y;
				if (w>0 && h>0) {
					_lr.addElement(new TRectangle(x,y,w,h));
					_size++;
				}
			}
			_lr.addElement(rect);
			_size++;
		}
		
		TRectangle elementAt(int n) { return (TRectangle)_lr.elementAt(n+min_idx); }
		int size() { return _size; }
		
		// get an index for point (x,y)
		// when `fuzzy' is true, the lines minmal or maximal index is returned
		// when x is left or right of the lines content
		//---------------------------------------------------------------------
		int GetIndex(double x, double y, boolean fuzzy) {
			TRectangle rect;
			if (x<elementAt(0).x)
				return fuzzy ? min_idx : -1;
			rect = elementAt(_size-1);
			if (x>rect.x+rect.w) {
				return fuzzy ? min_idx+_size-1 : -1;
			}
			
			// binary search for rectangle at position `x'
			int l=0, r=size()-1, m;
			while(true) {
				m=((r-l)>>1)+l;
				rect = elementAt(m);
				if (rect.x<=x && x<rect.x+rect.w) {
					return m+min_idx;
				}
				if (l>=r) {
					break;
				}
				if (x<rect.x) {
					r=m-1;	
				} else {
					l=m+1;
				}
			}
			
			return -1;
		}
	}

	class TLineBuffer {
		Vector _data;
		TLineBuffer() { _data = new Vector(); }
		void addElement(TLine line) { _data.addElement(line); }
		TLine elementAt(int n) { return (TLine)_data.elementAt(n); }
		int size() { return _data.size(); }
	}
}
