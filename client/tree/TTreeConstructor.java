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

import java.util.*;

public class TTreeConstructor
{
	Stack _stack;
	TTreeNode _first;
	
	public TTreeConstructor()
	{
		_stack = new Stack();
		_first = new TTreeNode();
		_first.SetClosed(false);
	}

	public TTreeNode Top()
	{
		return _first;
	}

	public void BgnSub(TTreeNode tn)
	{
		if (_stack.empty()) {
			_first.Add(tn);
		} else {
			((TTreeNode)_stack.peek()).Add(tn);
		}
		_stack.push(tn);
	}
	
	public void EndSub()
	{
		if (_stack.empty()) {
			System.out.println("TTreeConstructor.EndSub(): no sub, command ignored");
			return;
		}
		_stack.pop();
	}
	
	public void Add(TTreeNode tn)
	{
		if (_stack.empty()) {
			System.out.println("TTreeConstructor.Add(): no sub, command ignored");
			return;
		}
		((TTreeNode)_stack.peek()).Add(tn);
	}
}
