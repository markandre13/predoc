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
import Main;

public class TToolDrawText
	extends TTool
{
	int _x, _y;								// position of text in edit window
														// `_y' is the baseline
	int _cx, _cy;							// cursor position in text
	String _text;							// the current text
	boolean _create;					// `true' when not editing an old TFigText figure
	TFigText _fig;						// the figure when editing an old TFigText figure
	int _keep_x;							// to avoid drift when cursor is moved verticaly
	
	// constructors
	//-------------------------------------------------------------------------

	// create a new TFigText
	public TToolDrawText(TDrawingArea c)
	{
		super(c);
		_create = true;
		_cx = 0;
	}

	// modify an existing TFigText
	public TToolDrawText(TDrawingArea c, TFigText fig)
	{
		super(c);
		_create = false;

		_x = fig.p1.x;
		_y = fig.p1.y + Main.fmType.getAscent();;
		_keep_x = -1;
		
		_text = fig._text; 
		
		// `String' pointers don't behave like other pointers in JAVA so we have
		// to handle two separated strings:
		fig._text = "";		// clear Figure to disable printing of the old string
		_fig = fig;				// Store pointer to figure to write-back the new string
		_cx   = 0;
		Invalidate();
	}

	// start/stop edit process
	//-------------------------------------------------------------------------
	public void start()
	{
		if (_create) {
			_cx = 0;
			_text = new String();
		}
	}

	public void stop()
	{
		if (_create) {
			if (_text.length()>0)
				Add(new TFigText(_text, _x, _y));
		} else {
			_fig.SetText(_text);
			_drawingarea.SetTool(TToolSelect._this);
		}
	}
	
	public void mousePressed(MouseEvent e)
	{
		stop();
		_x = e.getX();
		_y = e.getY();
		start();
		Invalidate();
	}

	public void keyPressed(KeyEvent e)
	{
		boolean keep_x = false;
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
				if (_cx>0)
					_cx--;
				break;
			case KeyEvent.VK_RIGHT:
				if (_cx<_text.length())
					_cx++;
				break;
			case KeyEvent.VK_UP: {
					keep_x = true;
					int n1;																			// start of current line
					if (_cx==0) {
						n1 = 0;
					} else {
						n1 = _text.lastIndexOf('\n', _cx-1)+1;
					}
					if (n1==0)
						break;
					int n2;																			// start of previous line
					if (n1==1) {
						n2=0;
					} else {
						n2 = _text.lastIndexOf('\n', n1-2)+1;
					}
// begin of duplicated code
					int x1;
					if (_keep_x>=0) {
						x1 = _keep_x;
					} else {
						x1 = _keep_x = Main.fmType.stringWidth(_text.substring(n1,_cx));
					}
					int x2 = 0;
					_cx = n2;
					while(true) {
						if (_cx>=_text.length())
							break;
						String c = _text.substring(_cx,_cx+1);
						byte[] bubble = c.getBytes();
						if (bubble[0]==10)
							break;
						int dx = Main.fmType.stringWidth(c);
						if ( x2+(dx>>1) > x1 )
							break;
						x2+=dx;
						_cx++;
					}
// end of duplicated code
				}
				break;
			case KeyEvent.VK_DOWN: {
					keep_x = true;
					int n2 = _text.indexOf('\n', _cx)+1;				// start of next line
					if (n2>0) {
						int n1;																		// start of current line
						if (_cx==0) {
							n1 = 0;
						} else {
							n1 = _text.lastIndexOf('\n', _cx-1)+1;
						}
						int x1;
						if (_keep_x>=0) {
							x1 = _keep_x;
						} else {
							x1 = _keep_x = Main.fmType.stringWidth(_text.substring(n1,_cx));
						}
						int x2 = 0;
						_cx = n2;
						while(true) {
							if (_cx>=_text.length())
								break;
							String c = _text.substring(_cx,_cx+1);
							byte[] bubble = c.getBytes();
							if (bubble[0]==10)
								break;
							int dx = Main.fmType.stringWidth(c);
							if ( x2+(dx>>1) > x1 )
								break;
							x2+=dx;
							_cx++;
						}
					}
				} break;
			case KeyEvent.VK_HOME:
				if (_cx>0)
					_cx = _text.lastIndexOf('\n', _cx-1)+1;
				break;
			case KeyEvent.VK_END: {
					int n = _text.indexOf('\n', _cx);
					if (n<0)
						_cx = _text.length();
					else
						_cx = n;
				} break;
			case KeyEvent.VK_BACK_SPACE:
				if (_cx>0)
					_cx--;
			case KeyEvent.VK_DELETE:
				if (_cx<_text.length())
					_text = _text.substring(0,_cx) + _text.substring(_cx+1);
				break;
			case KeyEvent.VK_ENTER:
				_text=_text.substring(0,_cx)+"\n"+_text.substring(_cx);
				_cx++;
				break;
			default: {
				char c = e.getKeyChar();
				if (c!=KeyEvent.CHAR_UNDEFINED) {
					_text=_text.substring(0,_cx)+c+_text.substring(_cx);
					_cx++;
				}}
		}
		Invalidate();
		if (!keep_x)
			_keep_x = -1;
	}
	
	// the paint method is going to be splitted in to methods, the idea
	// is that the figures work hand in hand with their tool not only for
	// creation and modification but for their screen output also;
	// the `paintFigure' definition is not done yet
	
	// a call to `paintSelection' must always follow a call to
  // `paintFigure' of the same figure (so TDrawingArea has to
  // cooperate with TToolSelect, or when other way around when
  // i move the list of selected figure to TDrawingArea...)
	
	public void paint(Graphics pen)
	{
		paintFigure(pen);
		paintSelection(pen);
	}

	int _sx, _sy;
	
	public void paintFigure(Graphics pen /*, figure paramater*/)
	{
		if (_text != null) {
			_sx=_x; _sy = _y;	// screen position of the cursor
			int h = Main.fmType.getAscent()+Main.fmType.getDescent();
			pen.setFont(Main.fntType);

			// draw all lines			
			int idx = 0;
			int y = _y;
			while(true) {
				int n = _text.indexOf('\n', idx);
				String line;
				if (n==-1) {
					line = _text.substring(idx);
					if (_cx>=idx) {
						_sx = _x + Main.fmType.stringWidth(_text.substring(idx,_cx));
						_sy = y;
					}
				} else {
					line = _text.substring(idx, n);
					if (idx<=_cx && _cx<=n) {
						_sx = _x + Main.fmType.stringWidth(_text.substring(idx,_cx));
						_sy = y;
					}
				}
				pen.drawString(line,_x,y);
				y+=h;
				if (n==-1)
					break;
				idx=n+1;
			}
			_sy-=Main.fmType.getAscent();
		}
	}
	
	public void paintSelection(Graphics pen)
	{
		// draw text cursor
		//------------------
		pen.drawLine(_sx,_sy-1,
								 _sx,_sy+Main.fmType.getAscent()+Main.fmType.getDescent()+1);
	}
}
