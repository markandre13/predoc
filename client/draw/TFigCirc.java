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

class TFigCirc
	extends TFigRect
{
	public TFigCirc()
	{
	}
	
	public TFigCirc(TFigCirc o)
	{
		super(o);
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

		pen.drawArc(x1, y1, x2-x1+1, y2-y1+1, 0,360);
	}

	public double Distance(int x, int y)
	{
		/*
		if (_fill_color!=null) {
			...
		} else {
		*/
			double x1,y1,x2,y2;
			x1=p1.x; y1=p1.y; x2=p2.x; y2=p2.y;
			
			double rx = (x2-x1)/2.0;
			double ry = (y2-y1)/2.0;
			double cx = x1+rx;
			double cy = y1+ry;
			double dx = x - cx;
			double dy = y - cy;
			
			double phi = Math.atan( (dy*rx) / (dx*ry) );
			if (dx<0.0)
				phi=phi+Math.PI;
			double ex = rx*Math.cos(phi);
			double ey = ry*Math.sin(phi);
			dx -= ex;
			dy -= ey;
			double d = Math.sqrt(dx*dx+dy*dy);
			return d;
		/*
		}	
		*/
	}

}
