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
// import algorithm.*;

/*
 * should i merge TSelector with TDocViewPort?
 */

// the sort algorithm for the icons on the correction strip is still
// crap so the strip is disabled

class TADocViewZoom
	implements ActionListener
{
	TDDocViewState _state;
	int _zoom;
	
	public TADocViewZoom(TDDocViewState state, int zoom)
	{
		_state = state;
		_zoom = zoom;
	}

	public void actionPerformed(ActionEvent e)
	{
		_state.SetZoom(_zoom);
	}	
}

class TDocViewPort
	extends common.TScrolledArea
	implements TStateListener
{
  TDDocViewState _state;
  TSelector _selector;
  TPopup _popup;
  
  // attributes for the correction strip
  boolean _use_correction_strip;
  int _correction_strip_width = 50;
  Vector _correction_strip;
	final int _csew = 10;
	final int _cseh = 10;
	int _crnt_idx;      // correction containing the mouse pointer or `-1'

  public TDocViewPort(TDDocViewState state)
  {
  	_crnt_idx = -1;
  	_use_correction_strip = false;
    _state = state;
    _state.Register(this);
    Area().setBackground(new Color(128,0,0));
    _correction_strip = new Vector();

		// setup TScrolleaArea super class
		//---------------------------------
		item_h = item_w = 1;
		area_x1 = area_y1 = 0;
		area_x2 = 1;
		area_y2 = 1;
		SetPaintTo(ALL_AT_ONCE);
		SetDoubleBuffer(true);

		// setup the popup window
		//---------------------------------
		_popup = new TPopup(this);

		_popup.AddItem("Next Page", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_state.NextPage();
			}
		});
		_popup.AddItem("Previous Page", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_state.PrevPage();
			}
		});
/*		
		_popup.BgnPulldown("File");
		_popup.AddItem("Save");
		_popup.AddItem("Close");
		_popup.EndPulldown();

		_popup.BgnPulldown("View");
*/
		_popup.AddItem("Overview",	new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TOverview ov = _state.Overview();
				if (ov==null) {
					ov = new TOverview(_state);
					_state.SetOverview(ov);
					ov.show();
				} else {
					ov.toFront();
				}
			}
		});
/*
		_popup.AddItem("Duplicate");
		_popup.AddItem("Close");
		_popup.EndPulldown();
*/
		_popup.BgnPulldown("Zoom");
		_popup.AddItem("10%",  new TADocViewZoom(_state, 10));
		_popup.AddItem("25%",  new TADocViewZoom(_state, 25));
		_popup.AddItem("50%",  new TADocViewZoom(_state, 50));
		_popup.AddItem("75%",  new TADocViewZoom(_state, 75));
		_popup.AddItem("100% (1:1)", new TADocViewZoom(_state, 100));
		_popup.AddItem("125%", new TADocViewZoom(_state, 125));
		_popup.AddItem("150%", new TADocViewZoom(_state, 150));
		_popup.AddItem("175%", new TADocViewZoom(_state, 175));
		_popup.AddItem("200%", new TADocViewZoom(_state, 200));
		_popup.EndPulldown();
/*		
		_popup.BgnPulldown("Justify");
		_popup.AddCheck("Vertical");
		_popup.AddCheck("Horizontal");
		_popup.EndPulldown();
*/
    _selector = new TSelector(this, _popup, state);
  }

	// called from the TScrolledArea base class
	//-------------------------------------------------------------------------
	public void scrolled()
	{
		_state.SetPosition(area_x-_visi_x, area_y-_visi_y);
	}
	
	// paint document
	//-------------------------------------------------------------------------
	public void paintItem(Graphics pen, int x, int y)
	{
    Image image = _state.Image();
    if (image==null)
    	return;

   	pen.drawImage(image,0,0,null);

    int w = image.getWidth(null);
    int h = image.getHeight(null);

		TDPage page = _state.Page();
		if (page==null)
			return;

		TDPageCorrection correction = page.Correction();

		// draw correction strip
		//-------------------------------------------------------
		if (_use_correction_strip) {
			pen.setColor(Color.lightGray);
			pen.fillRect(area_x2-_correction_strip_width,0,
									 _correction_strip_width, area_y2);
			pen.setColor(Color.black);
			pen.drawLine(area_x2-_correction_strip_width, 0,
									 area_x2-_correction_strip_width, area_y2);
			Color cb = new Color(255, 192, 128);
			
			_ComputeCorrectionStrip();			

			for(int i=0; i<correction.Size(); i++) {
				Point r = (Point)_correction_strip.elementAt(i);
				pen.setColor(Color.gray);
				pen.fillOval(r.x+w+3+1,r.y+1, _csew,_cseh);
				pen.drawOval(r.x+w+3+1,r.y+1, _csew,_cseh);
				pen.setColor(cb);
				pen.fillOval(r.x+w+3,r.y, _csew,_cseh);
				pen.setColor(Color.black);
				pen.drawOval(r.x+w+3,r.y, _csew,_cseh);
			}
		}

		// draw correction marks
		//-------------------------------------------------------
		pen.setColor(Color.red);
		for(int i=0; i<correction.Size(); i++) {
			if (i!=_crnt_idx)
				pen.drawPolygon(correction.Polygon(i,w,h));
		}

		_selector.paint(pen);
	}


	// stateChanged
	//-------------------------------------------------------------------------
  public void stateChanged(TState state)
  {
  	if (state instanceof TDDocViewState) {
  		switch(state.Reason()) {
  			case TDDocViewState.PAGE_CHANGED:
  			case TDDocViewState.PAGE_LOADED:
  			case TDDocViewState.PAGE_VIEWABLE:
					Image img = _state.Image();
					if (img!=null) {
						area_x = area_y = 0;
						area_x2 = img.getWidth(null) + 
							(_use_correction_strip ? _correction_strip_width : 0);
						area_y2 = img.getHeight(null);
						Update();
					}
		  		if (Main.debug_mvc)
 						System.out.println("TDocViewPort.stateChanged: repaint");
		 			Area().repaint();
 					break;
 				case TDDocViewState.PAGE_CORRECTION_ADDED:
 				case TDDocViewState.PAGE_CORRECTION_REMOVED:
 				case TDDocViewState.PAGE_CORRECTION_CHANGED:
 					Area().repaint();
 					break;
 			}
/*
  		switch(state.Reason()) {
  			case TDDocViewState.PAGE_CHANGED:
 				case TDDocViewState.PAGE_CORRECTION_ADDED:
 				case TDDocViewState.PAGE_CORRECTION_REMOVED:
 				case TDDocViewState.PAGE_CORRECTION_CHANGED:
 					_crnt_idx = -1;
 					_ComputeCorrectionStrip();
 					Area().repaint();
 					break;
 			}
*/
  	}

  	// copied from TStatusBar.java:
  	//-----------------------------
  	if (_state.Page()!=null) {
  		if (_state.Image()==null) {
  			// fetching
  			Area().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  		} else { 
  			if (_state.Page().ScanData()==null) {
  			// scanning
  			Area().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	  		} else {
  			// okay
  			_selector.SetCursor();
  			}
  		}
  	}
  }


	// correction strip helpers
	//-------------------------------------------------------------------------
  class TCCS
  	extends Point
  {
  	int pos;
  };
  
  void _ComputeCorrectionStrip()
  {
  	if (!_use_correction_strip)
  		return;

    Image image = _state.Image();
		if (image==null)
    	return;
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		int max_x = 50;

  	_correction_strip.removeAllElements();
		TDPage page = _state.Page();
		TDPageCorrection correction = page.Correction();

		TCCS v[] = new TCCS[correction.Size()];
		for(int i=0; i<correction.Size(); i++) {
			v[i] = new TCCS();
			Rectangle p = correction.Polygon(i,w,h).getBounds();
			v[i].x = p.x;
			v[i].y = p.y;
			v[i].pos = i;
		}
		
		algorithm.sort.BubbleSort(v, new algorithm.TCompare() {
			public boolean less(Object a, Object b) {
				return (((TCCS)a).x < ((TCCS)b).x)
							&& (((TCCS)a).y < ((TCCS)b).y);
			}
		});

		for(int i=0; i<correction.Size(); i++) {
			TCCS p = v[i];
			p.x = 0;
			for(int j=0; j<i; j++) {
				Point pt = (Point)_correction_strip.elementAt(j);
				if (p.y>=pt.y-_cseh && p.y<=pt.y+(_cseh<<1)) {
					p.x = pt.x + _csew + 3;
				}
			}
System.out.println("POS:"+p.x+","+p.y);
			_correction_strip.addElement(new Point(p.x, p.y));
			max_x = Math.max(max_x, p.x+_csew+6);
		}
		if (_correction_strip_width!=max_x) {
			_correction_strip_width = max_x;
			area_x2 = max_x + w;
			Update();
		}
  }
}
