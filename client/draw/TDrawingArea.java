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

/**
* TDrawingArea is a child of draw.TScrolledArea (not common.TScrolledArea)
* and is using a Canvas child to display the figures.
*/
public class TDrawingArea
	extends draw.TScrolledArea
	implements MouseListener, MouseMotionListener, KeyListener
{
	TTool _tool;
	public TDrawingData _data;
	public Canvas _paper;

	final static int CONTENT_SPACE = 10;

	// this is a buffer of `TSelectionNode's used by TToolSelect but it's
	// needed by the paint algorithm here too
	Vector _selected;

	public TDrawingArea(TDrawingData data)
	{
		_data = data;
		_tool = null;
		_selected = new Vector();
		_paper = new Canvas() {
			public void update(Graphics pen)
			{
				paint(pen);
			}
		
			public void paint(Graphics jpen)
			{
				int w,h;
				w = getSize().width;
				h = getSize().height;
				if (w<=0 || h<=0)
					return;
				Image buffer = createImage(w, h);
				Graphics pen = buffer.getGraphics();
				
				pen.translate(-area_x, -area_y);

				TFigCorrection fc = _data.FigCorrection();
				if (!fc.Foreground()) {
					pen.setColor(Color.red);
					fc.paint(pen);
				} else {
					pen.setColor(Color.lightGray);
				}
			
				int n=_data.Size();
				for(int i=0; i<n; i++) {
					if (_data.Figure(i)!=fc) {
						_data.Figure(i).paint(pen);
					}
				}
				
				if (fc.Foreground()) {
					pen.setColor(Color.red);
					fc.paint(pen);
				}
				
				if (_tool!=null) {
					_tool.paint(pen);
				}
				
				jpen.drawImage(buffer,0,0, null);
				pen.dispose();
			}
		};
		_paper.setBackground(Color.white);

		AddView(_paper);
		_paper.addMouseListener(this);
		_paper.addMouseMotionListener(this);
		_paper.addKeyListener(this);

		Invalidate();
	}

	public void scrolled()
	{
		_paper.repaint();
		Invalidate();
	}

	// should be a method in `common.Draw':
	final static void _order(Point b1, Point b2)
	{
		int a;
		if (b1.x > b2.x) {
			a=b1.x; b1.x=b2.x; b2.x=a;
		}
		if (b1.y > b2.y) {
			a=b1.y; b1.y=b2.y; b2.y=a;
		}
	}

	/**
	* Moves the correction form into the center of the drawing area.
	*/
	public void Center()
	{
		int n=_data.Size();
		for(int i=0; i<n; i++) {
			TFigure fig = _data.Figure(i);
			if (fig instanceof TFigCorrection) {
				TFigCorrection fc = (TFigCorrection)fig;
				Dimension d = _paper.getSize();

				area_x = -( (d.width -fc.form_w)/2 - fc.real_x1 );
				area_y = -( (d.height-fc.form_h)/2 - fc.real_y1 );

				// keep the forms head inside the window
				if (area_y>fc.real_y1)
					area_y = fc.real_y1;
				
				Invalidate();
				return;
			}
		}
	}	

	public void Invalidate()
	{
		int n = _data.Size();
		if (n>0) {
			Point p1=new Point(), p2=new Point(), b1=new Point(), b2=new Point();
			int a;
			Dimension d = _paper.getSize();
			b1.x = area_x;
			b1.y = area_y;
			b2.x = area_x+d.width;
			b2.y = area_y+d.height;
			_order(b1,b2);
			for(int i=0; i<n; i++) {
				_data.Figure(i).getBounding(p1,p2);
				_order(p1,p2);
				b1.x = Math.min(b1.x, p1.x - CONTENT_SPACE);
				b2.x = Math.max(b2.x, p2.x + CONTENT_SPACE);
				b1.y = Math.min(b1.y, p1.y - CONTENT_SPACE);
				b2.y = Math.max(b2.y, p2.y + CONTENT_SPACE);
//System.out.println("Fig : x-range="+(p1.x-CONTENT_SPACE)+","+(p2.x+CONTENT_SPACE)+" y-range="+(p1.y - CONTENT_SPACE)+","+(p2.y+CONTENT_SPACE));
			}
			area_x1 = b1.x; area_x2 = b2.x;
			area_y1 = b1.y; area_y2 = b2.y;
			AdjustSB();
//System.out.println("Area: x-range="+b1.x+","+b2.x+" y-range="+b1.y+","+b2.y);
		}
		_paper.repaint();
	}

	public TDrawingData Data()
	{
		return _data;
	}

	public void SetTool(TTool tool)
	{
		if (_tool!=null) {
			TTool t = _tool; _tool = null;		// avoid recursion
			t.stop();
		}
		_tool = tool;
		if (_tool!=null)
			_tool.start();
	}
	
	public void mouseClicked(MouseEvent e)
	{
		_ctool = _tool;
		if (_tool!=null) {
			e.translatePoint(area_x, area_y);
			_tool.mouseClicked(e);
		}
	}
	
	public void mousePressed(MouseEvent e)
	{
		_ctool = _tool;
		_paper.requestFocus();
		if (_tool!=null) {
			e.translatePoint(area_x, area_y);
			_tool.mousePressed(e);
		}
	}
	
	public void mouseReleased(MouseEvent e)
	{
		_ctool = _tool;
		if (_tool!=null) {
			e.translatePoint(area_x, area_y);
			_tool.mouseReleased(e);
			SetCursor(Cursor.DEFAULT_CURSOR);
		}
	}
	
	public void mouseEntered(MouseEvent e)
	{
		_ctool = _tool;
		_paper.requestFocus();
	}
	
	public void mouseExited(MouseEvent e)
	{
		_ctool = _tool;
	}
	
	public void mouseMoved(MouseEvent e)
	{
		_ctool = _tool;
		if (_tool!=null) {
			e.translatePoint(area_x, area_y);
			_tool.mouseMoved(e);
		}
	}
	
	public void mouseDragged(MouseEvent e)
	{
		_ctool = _tool;
		if (_tool!=null) {
			e.translatePoint(area_x, area_y);
			_tool.mouseDragged(e);
		}
	}
	
	public void keyPressed(KeyEvent e)
	{
		_ctool = _tool;
		if (_tool!=null)
			_tool.keyPressed(e);
	}
	
	public void keyReleased(KeyEvent e)
	{
		_ctool = _tool;
		if (_tool!=null) {
			_tool.keyReleased(e);
		}
	}
	
	public void keyTyped(KeyEvent e)
	{
		_ctool = _tool;
		if (_tool!=null)
			_tool.keyTyped(e);
	}
	
	static TTool _ctool = null;
	
	public static void SetCursor(int c) {
		if (_ctool!=null) {
			_ctool.SetCursor(c);
		}
	}
	
	public static void StaticInvalidate() {
		if (_ctool!=null)
			_ctool.Invalidate();
	}
}
