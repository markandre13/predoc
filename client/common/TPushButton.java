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

public class TPushButton
	extends TButtonBase
	implements MouseListener
{
	protected boolean _down, _inside;
	ActionListener _listener;
	String _cmd;
	int _id;

	public TPushButton()
	{
		_Init();
	}

	public TPushButton(Image image)
	{
		_Init();
		_image = image;
	}

	public TPushButton(String label)
	{
		_Init();
		_label = label;
	}

	public TPushButton(String label, int id)
	{
		_Init();
		_label = label;
		_id = id;
	}

	public TPushButton(Container parent, String label)
	{
		_Init();
		parent.add(this);
		_label = label;
	}


	private void _Init()
	{
		_down = false;
		_inside = false;
		_cmd = "";
		_id = -1;
		_listener = null;
		addMouseListener(this);
	}

	public void addActionListener(ActionListener al)
	{
		_listener = al;
	}

	public void setActionCommand(String cmd)
	{
		_cmd = cmd;
	}
	
	public void mouseClicked(MouseEvent e)
	{
	}
	
	public void mousePressed(MouseEvent e)
	{
		if (!IsEnabled()) return;
		_down = true;
		requestFocus();
		repaint();
	}

	void activated()
	{
		if (_listener!=null)
			_listener.actionPerformed(new ActionEvent(this,0,_cmd));
	}
	
	public void mouseReleased(MouseEvent e)
	{
		if (!IsEnabled()) return;
		if (_inside && _down) {
			activated();
		}
		_down = false;
		repaint();
	}
	
	public void mouseEntered(MouseEvent e)
	{
		if (!IsEnabled()) return;
		_inside = true;
		repaint();
	}
	
	public void mouseExited(MouseEvent e)
	{
		if (!IsEnabled()) return;
		_inside = false;
		repaint();
	}
	
	public boolean IsDown() {
		return (_down && _inside );
	}

	public int GetID() {
		return _id;
	}
};
