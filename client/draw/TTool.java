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

public class TTool
{
	protected TDrawingArea _drawingarea;
	
	public TTool(TDrawingArea c)
	{
		_drawingarea = c;
	}
	
	public void start()	{}
	public void start_modify() {}
	public void stop() {}

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e)	{}
	public void keyTyped(KeyEvent e) {}
	public void paint(Graphics pen)	{}

	// utility methods for all tools
	//-------------------------------
	public void Invalidate()
	{
		if (_drawingarea!=null)
			_drawingarea.Invalidate();
	}
	
	public void Add(TFigure fig)
	{
		_drawingarea.Data().Add(fig);
		Invalidate();
	}

	public void SetCursor(int cursor)
	{
		_drawingarea._paper.setCursor(Cursor.getPredefinedCursor(cursor));
	}
}
