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
/*
 * Since most AWT and Swing applications introduce about 3 methods to
 * ease the creation of menubars, i delegate this job to a helper class.
 */

import java.awt.MenuBar;
import java.awt.PopupMenu;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.CheckboxMenuItem;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.util.Stack;

public class TMenuHelper
{
	private Stack _stack;

  protected MenuBar _mb;
	protected PopupMenu _pm;  
	
	public TMenuHelper()
	{
		_stack = new Stack();
		_mb = null;
		_pm = null;
	}

  public void BgnPulldown(String label)
  {
  	Menu pd = new Menu(label);
  	if (_stack.empty()) {
  		if (_mb!=null) _mb.add(pd); else _pm.add(pd);
  	} else {
  		((Menu)_stack.peek()).add(pd);
  	}
  	_stack.push(pd);
  }

  public void EndPulldown()
  {
  	_stack.pop();
  }

	public void AddItem(String label)
	{
		AddItem(label, -1, null);
	}

	public void AddItem(String label, ActionListener action)
	{
		AddItem(label, -1, action);
	}

  public void AddItem(String label, int key, ActionListener action)
  {
  	MenuItem mi;
  	
  	if (key!=-1)
	    mi = new MenuItem(label, new MenuShortcut(key));
		else
	   	mi = new MenuItem(label);
	
	  if (action!=null)
	    mi.addActionListener(action);
	   else
	   	mi.setEnabled(false);

  	if (_stack.empty()) {
  		if (_mb==null) _pm.add(mi);
  	} else {
  		((Menu)_stack.peek()).add(mi);
  	}
  }

  public void AddCheck(String label)
  {
  	AddCheck(label, null);
  }

  public void AddCheck(String label, ItemListener action)
  {
    CheckboxMenuItem mi = new CheckboxMenuItem(label);
    if (action!=null)
	    mi.addItemListener(action);
		else
			mi.setEnabled(false);

  	if (_stack.empty()) {
  		if (_mb==null) _pm.add(mi);
  	} else {
  		((Menu)_stack.peek()).add(mi);
  	}
  }
}
