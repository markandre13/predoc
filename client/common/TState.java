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

import java.util.Vector;

public class TState
{
	private Vector _list;
	private int _reason;

	public TState()
	{
		_list = new Vector();
	}
	
	/**
	* Register a new state listener when it's not already registered.
	*/
	synchronized public void Register(TStateListener l)
	{
		if (!_list.contains(l))
			_list.addElement(l);
	}
	
	/**
	* Remove TStateListener from the list and stop telling him what's
	* going on.
	*/
	synchronized public void UnRegister(TStateListener l)
	{
		if (_list.contains(l))
			_list.removeElement(l);
	}

	/**
	* Return the number of TStateListeners currently listening.
	*/
	synchronized public int Listeners()
	{
		return _list.size();
	}

	/**
	* Return true whether the state is talking to this TStateListener
	* or not.
	*/
	synchronized public boolean Contains(TStateListener l)
	{
		return _list.contains(l);
	}

	/**
	* Notify all listeners and call their `stateChanged' method.
	* @param reason
	*		
	*/ 
	synchronized public void Notify(int reason)
	{
		 Notify(reason, null);
	}
	
	synchronized public void Notify(int reason, TStateListener who)
	{
		_reason = reason;
		int n = _list.size();
		for (int i=0; i<n; i++) {
			TStateListener l = (TStateListener)_list.elementAt(i);
			if (l!=who) {
				l.stateChanged(this);
			}
		}
	}
	
	public boolean Reason(int mask)
	{
		return (_reason&mask) != 0;
	}	
	
	public int Reason()
	{
		return _reason;
	}
}
