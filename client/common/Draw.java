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

import java.awt.*;
import java.util.Stack;

/**
	This `namespace' contains additional graphic functions missing in
	`java.awt.Graphics' and adds a PostScript like interface also.<BR>
	I hope this might be a good base to start with when someone is willing
	to add code for PostScript output.
	
	Some thing this interface tries to cope with:
	- `Graphics' has a `translate(dx,dy)' method but no `setOrigin(x,y)'
	  method to undo previous translations. Reseting the origin can be done
	  now with `GSave()' and `GRestore'.
	- `Graphics' is a 1:1 interface to the basic X11 drawing operations so
	  bezier methods were missing and PReDoc needs 'em to display some of
	  the correction symbols.
*/

public class Draw
{
	static class TGState {
		double tx, ty;
	}
	
	static public class T2Points {
		public int x1, y1;
		public int x2, y2;
	}

	public static void DrawRectangle(Graphics pen, Point a, Point b) {
		pen.drawRect(a.x, a.y, b.x-a.x, b.y-a.y);
	}

	public static T2Points GetPolygonBounds(Polygon p) {
		T2Points r = new T2Points();
		GetMinMax(p ,r);
		return r;
	}

	public static T2Points GetPolyBezierBounds(double[] x, double[] y)
	{
		Polygon poly = new Polygon();
		int n=x.length-3;
		int i=0;
		poly.addPoint( (int)(x[0]),(int)(y[0]) );
		while(i<=n) {
			bezier(
				poly,
				x[i],y[i], 
				x[i+1],y[i+1], 
				x[i+2],y[i+2], 
				x[i+3],y[i+3]);
			i+=3;
		}
		T2Points r = new T2Points();
		GetMinMax(poly ,r);
		return r;
	}

	public static void GetMinMax(Polygon p, T2Points r) {
		r.x1 = r.x2 = p.xpoints[0];
		r.y1 = r.y2 = p.ypoints[0];
		for(int i=1; i<p.npoints; i++) {
			if (r.x1 > p.xpoints[i])
				r.x1 = p.xpoints[i];
			if (r.x2 < p.xpoints[i])
				r.x2 = p.xpoints[i];
			if (r.y1 > p.ypoints[i])
				r.y1 = p.ypoints[i];
			if (r.y2 < p.ypoints[i])
				r.y2 = p.ypoints[i];
		}
	}

	final public static void OrderPoints(Point b1, Point b2)
	{
		int a;
		if (b1.x > b2.x) {
			a=b1.x; b1.x=b2.x; b2.x=a;
		}
		if (b1.y > b2.y) {
			a=b1.y; b1.y=b2.y; b2.y=a;
		}
	}
	
	// PostScript like interface
	//----------------------------------------------------------------------
	static Polygon _poly = new Polygon();
	static double _x, _y;			// last point
	static double _tx, _ty;		// current translation
	static Graphics _gc;
	static Stack _gstack = new Stack();
	
	public static void SetContext(Graphics gc) {
		_gc = gc;
	}

	public static void FillPolyBezier(double[] x, double[] y, double sw, double sh)
	{
		_poly = new Polygon();
		int n=x.length-3;
		int i=0;
		MoveTo((int)(sw*x[0]),(int)(sh*y[0]));
		while(i<=n) {
			bezier(
				_poly,
				sw*x[i],sh*y[i], 
				sw*x[i+1],sh*y[i+1], 
				sw*x[i+2],sh*y[i+2], 
				sw*x[i+3],sh*y[i+3]);
			i+=3;
		}
		_gc.fillPolygon(_poly);
		// _gc.drawPolygon(_poly);
		_poly = new Polygon();
	}

	public static void DrawBezier(double[] x, double[] y, double sw, double sh)
	{
		_poly = new Polygon();
		int n=x.length-3;	// 3
		int i=0;
//		MoveTo((int)(sw*x[0]),(int)(sh*y[0]));

		while(i<=n) {
			bezier(
				_poly,
				sw*x[i],sh*y[i], 
				sw*x[i+1],sh*y[i+1], 
				sw*x[i+2],sh*y[i+2], 
				sw*x[i+3],sh*y[i+3]);
			i+=3;
for(int j=0; j<_poly.xpoints.length; j++) {
	System.out.println(j+": "+_poly.xpoints[j]+","+_poly.ypoints[j]);
}
		}
System.out.println("-----------------------------");
		_gc.drawPolyline(_poly.xpoints, _poly.ypoints, _poly.xpoints.length-1);
		_poly = new Polygon();
	}

	public static void Translate(double x, double y)
	{
		_tx+=x; _ty+=y;
		_gc.translate((int)x, (int)y);
	}
	
	public static void GSave()
	{
		TGState state = new TGState();
		state.tx = _tx;
		state.ty = _ty;
		_gstack.push(state);
		_tx = _ty = 0;
	}
	
	public static void GRestore()
	{
		_gc.translate((int)-_tx, (int)-_ty);
		TGState state = (TGState)_gstack.pop();
		_tx = state.tx;
		_ty = state.ty;
	}
	
	public static void MoveTo(double x, double y)
	{
		_x = x; _y = y;
		_poly.addPoint((int)x,(int)y);
	}

	public static void LineTo(double x, double y)
	{
		_x = x; _y = y;
		_poly.addPoint((int)x,(int)y);
	}
	
	public static void CurveTo(
		double x1, double y1,
		double x2, double y2,
		double x3, double y3)
	{
		bezier(_poly, _x,_y, x1,y1, x2,y2, x3,y3);
		_x=x3; _y=y3;
	}
	
	public static void Fill()
	{
		_gc.fillPolygon(_poly);
		_poly = new Polygon();
	}

	public static void Stroke()
	{
		_gc.drawPolygon(_poly);
		_poly = new Polygon();
	}

	final static double mid(double a, double b)
	{
		return (a + b) / 2.0;
	}
	
	final static void bezier(
		Polygon p,
		double x0, double y0, 
		double x1, double y1,
		double x2, double y2,
		double x3, double y3)
	{
		double tx, ty, ax, ay, bx, by;
		ax = x1-x0; ay = y1-y0;
		tx = x2-x1; ty = y2-y1;
		bx = x3-x2; by = y3-y2;
		double f = Math.abs(ax*ty - ay*tx) + Math.abs(tx*by - ty*bx);
		if (f*f < 100.0 ) {
			p.addPoint((int)x3, (int)y3);
		} else {
			double xx  = mid(x1, x2);
			double yy  = mid(y1, y2);
			double x11 = mid(x0, x1);
			double y11 = mid(y0, y1);
			double x22 = mid(x2, x3);
			double y22 = mid(y2, y3);
			double x12 = mid(x11, xx);
			double y12 = mid(y11, yy);
			double x21 = mid(xx, x22);
			double y21 = mid(yy, y22);
			double cx  = mid(x12, x21);
			double cy  = mid(y12, y21);
			bezier(p, x0, y0, x11, y11, x12, y12, cx, cy);
	    bezier(p, cx, cy, x21, y21, x22, y22, x3, y3);
		}
	}
}
