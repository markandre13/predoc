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

package tree;

import java.awt.*;
import java.util.*;

public class TTreeNode
{
	public TTreeNode()
	{
		_sub=null; 
		_closed=true;
	}

	public void Flatten(Vector v)
	{
		Flatten(v, 0);
	}	

	protected void Flatten(Vector v, int d)
	{
		if (d!=0)
			v.addElement(this);
		_depth = d;
		if (_closed)
			return;
		if (_sub!=null) {
			for(int i=0; i<_sub.size(); i++) {
				((TTreeNode)_sub.elementAt(i)).Flatten(v,d+1);
			}
		}
	}

	public void Add(TTreeNode node)
	{
		if (_sub==null)
			_sub = new Vector();
		_sub.addElement(node);
	}

	public boolean IsClosed() {
		return _closed;
	}
	
	public void SetClosed(boolean b) {
		_closed = b;
	}
	
	public boolean HasChildren()
	{
		return _sub!=null;
	}
	
	public int NumChildren()
	{
		if (_sub==null)
			return 0;
		return _sub.size();
	}
	
	public boolean IsSelectable() {
		return !HasChildren();
	}

	public void selected()
	{
	}
	
	public void paint(Graphics pen)
	{
	}
	

	private Vector _sub;
	public int _depth;
	private boolean _closed;
};
