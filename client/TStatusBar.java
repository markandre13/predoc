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

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import common.*;

/**
 * The statusbar displays informations from the TDocViewState like
 * document name, page number, fetching or scanning page, ...
 */
class TStatusBar
	extends Canvas
	implements TStateListener
{
	TDDocViewState _state;

	TStatusBar(TDDocViewState state)
	{
		_state = state;
		_state.Register(this);
	}

	public void paint(Graphics pen)
	{
		String str = new String();

		if (_state.Document()!=null) {
			str += "Document:" + _state.Filename()+"  ";
		}
		
		if (_state.Page()!=null) {
			str += "Page: " + (_state.PageNo()+1) + "/" + _state.PageCount() + "  ";
		}

		if (_state.Page()!=null) {
			if (_state.Image()==null) {
				str+="   (fetching)";
			} else if (_state.Page().ScanData()==null) {
				str+="   (scanning)";
			}
		}

		pen.drawString(str, 5,15);
	}
	
  public void stateChanged(TState state)
  {
  	if (state instanceof TDDocViewState) {
  		switch(state.Reason()) {
  			case TDDocViewState.PAGE_CHANGED:
  			case TDDocViewState.PAGE_LOADED:
  			case TDDocViewState.PAGE_VIEWABLE:
  			case TDDocViewState.PAGE_SCANNED:
  			case TDDocViewState.PAGE_NO_CHANGED:
			  	if (Main.debug_mvc)
				  	System.out.println("TStatusBar.stateChanged: repaint");
			  	repaint();
	  			return;
	  		case TDDocViewState.PAGE_SCROLLED:
	  			return;
	  	}
	  }
	  if (Main.debug_mvc)
	  	System.out.println("TStatusBar.stateChanged: unknown reason");
  }
}
