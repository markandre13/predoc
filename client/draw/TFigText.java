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
import Main;

class TFigText
	extends TFigRect
{
	String _text;

	public TFigText()
	{
		_text = new String();
		p1 = new Point();
		p2 = new Point();
	}

	/**
	* y is the baseline of the first line
	*/
	public TFigText(String text, int x, int y)
	{
		p1 = new Point(x,y-Main.fmType.getAscent());
		SetText(new String(text));
	}

	public TFigText(TFigText o)
	{
		super(o);
		_text = new String(o._text);
	}

	public void SetText(String text)
	{
		_text = text;
		int h = 1;
		int w = 0;
		int idx = 0;
		while(true) {
			int n = _text.indexOf('\n', idx);
			String line;
			if (n==-1) {
				line = _text.substring(idx);
			} else {
				line = _text.substring(idx, n);
			}
			w = Math.max(w, Main.fmType.stringWidth(line));
			if (n==-1)
				break;
			idx=n+1;
			h++;
		}
		p2 = new Point(p1.x+w, 
									 p1.y+(Main.fmType.getAscent()+Main.fmType.getDescent())*h);
	}
	
	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		super.Store(out, version);
		out.writeInt(_text.length());
		out.writeBytes(_text);
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		super.Restore(in, version);
		int n = in.readInt();
		byte buffer[] = new byte[n];
		in.readFully(buffer);
		SetText(new String(buffer));
	}
	
	public void paint(Graphics pen)
	{
		pen.setFont(Main.fntType);

		// draw all lines			
		int idx = 0;
		int h = Main.fmType.getAscent()+Main.fmType.getDescent();
		int y = p1.y + Main.fmType.getAscent();
		while(true) {
			int n = _text.indexOf('\n', idx);
			String line;
			if (n==-1) {
				line = _text.substring(idx);
			} else {
				line = _text.substring(idx, n);
			}
			pen.drawString(line,p1.x,y);
			y+=h;
			if (n==-1)
				break;
			idx=n+1;
		}
	}

	public int getHandle(double x, double y)
	{
		return -1;	
	}

	public double Distance(int x, int y)
	{
		return (p1.x<=x && x<=p2.x && p1.y<=y && y<=p2.y) ? RANGE : OUT_OF_RANGE;
/*
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

		if (x1<=x && x<=x2 && y1<=y && y<=y2) {
			return RANGE;
		}
		
		return OUT_OF_RANGE;
*/
	}
}
