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
import java.awt.event.*;
import Main;

public abstract class TButtonBase
	extends Canvas
{
	boolean _focus;
	String _label;
	Image _image;
	
	boolean _enabled;
	
	TButtonBase _this() { return this; }
	
	public TButtonBase()
	{
		_focus = false;
		_label = "(unnamed)";
		_image = null;
		_enabled = true;
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
//				if (e.isTemporary()) return;
				_focus = true;
				_this().repaint();
			}
			public void focusLost(FocusEvent e) {
//				if (e.isTemporary()) return;
				_focus = false;
				_this().repaint();
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_SPACE:
						activated();
				}
			}
		});
	}

	public boolean isFocusTraversable() { return _enabled; }

	public void SetEnabled(boolean e) {
		if (_enabled==e) return;
		_enabled = e;
		repaint();
	}

	public boolean IsEnabled() { return _enabled; }

	public void paint(Graphics pen)
	{
		pen.setFont(Main.fntSystem);
		Dimension d = getSize();
		pen.drawRect(0,0,d.width-1,d.height-1);
		int e=0;
		if (_focus) {
			e=1;
			pen.setColor(Color.black);
			pen.drawRect(1,1,d.width-3, d.height-3);
		}
		pen.setColor(Color.lightGray);
		pen.fill3DRect(1+e,1+e,d.width-2-(e<<1), d.height-2-(e<<1), !IsDown());
		pen.setColor(Color.black);
		paintLabel(pen);
	}

	abstract boolean IsDown();
	abstract void activated();

	public void paintLabel(Graphics pen)
	{
		if (_image!=null) {
			int w = _image.getWidth(null);
			int h = _image.getHeight(null);
			Dimension d = getSize();
			int x = (d.width-w)/2;
			int y = (d.height-h)/2;
			if (IsDown()) {
				x++; y++;
			}
			pen.drawImage(_image, x, y, null);
		} else {
			FontMetrics fm = pen.getFontMetrics();
			int w = fm.stringWidth(_label);
			int h = fm.getHeight();
			int a = fm.getAscent();
			int b = 6;
		
			Dimension d = getSize();
			int x = (d.width-w)/2;
			int y = (d.height-h)/2+a;
			if (IsDown()) {
				x++; y++;
			}
			if (_enabled) {
				pen.drawString(_label, x, y);
			} else {
				pen.setColor(Color.white);
				pen.drawString(_label, x, y);
				pen.setColor(Color.darkGray);
				pen.drawString(_label, x-1, y-1);
			}
		}
	}
}