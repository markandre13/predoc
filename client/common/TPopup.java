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

import common.TMenuHelper;
import java.awt.Component;
import java.awt.PopupMenu;

public class TPopup
	extends TMenuHelper
{
	Component _parent;

  public TPopup(Component p)
  {
  	PopupMenu pm = new PopupMenu();
  	p.add(pm);
  	_pm = pm;
  	_parent = p;
  }
  
  public void SetLabel(String l)
  {
  	_pm.setLabel(l);
  }
  
  public void Show(int x, int y)
  {
  	_pm.show(_parent,x,y);
  }
}
