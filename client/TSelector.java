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
import draw.TDrawingData;

class TSelector
	extends MouseAdapter
	implements MouseMotionListener
{
	TDocViewPort _viewport;	// the window were i get the mouse events from
	TPopup _popup;					// the popup to open
	TDDocViewState _state;
	TDPage _lastpage;				// to check if the page has changed between some ops
	TDScanData _data;

	boolean _empty;			// rectangle: `true'=>draw selection
	
	boolean _lock;			// prevent some problems with the AWT thread behaviour
	

	int _select_mode;		// current selection mode (rectangle, polygon)

	// rectangle selection mode
	int rx1, ry1, rx2, ry2;

	// polygon selection mode
	int 	 l1, l2;			// first and last index of a selected area
	double x1, x2;			// x position of the mouse pointer for each index

	public TSelector(TDocViewPort viewport, TPopup popup, TDDocViewState state)
	{
		_popup = popup;
		_viewport = viewport;
		_state = state;
		
		viewport.Area().addMouseListener(this);
		viewport.Area().addMouseMotionListener(this);

		_lastpage = null;
		_empty = true;
		_lock = false;
		l1 = -1;
	}

	double _x(MouseEvent e)
	{
		return (double)(e.getX()+_state.X()) / (double)_state.Image().getWidth(null);
	}

	double _y(MouseEvent e)
	{
		return (double)(e.getY()+_state.Y()) / (double)_state.Image().getHeight(null);
	}

	public void mousePressed(MouseEvent e)
	{
		if (_lock) {
			if (Main.debug_selector)
				System.out.println("REJECTED BY LOCK");
			return;
		}
		_lock = true;
		if (Main.debug_selector)
			System.out.println("LOCKED");
		_mousePressed(e);
		_lock = false;
		if (Main.debug_selector)
			System.out.println("UNLOCKED");
	}
	
	void _mousePressed(MouseEvent e)
	{
		if (Main.debug_selector)
			System.out.println("TSelector MOUSE PRESSED");
		if (_state==null || _state.Page()==null)
			return;

		_data = _state.Page().ScanData();

		if (TryPopup(e))
			return;

		if (TryCorrection(e))
			return;		

		// abort when there's no scan data available
		//-------------------------------------------
		if (_data==null) {
			Main.wnd.getToolkit().beep();
			l1 = -1;
			return;
		}

		// start new selection
		//-------------------------------------------
		_select_mode = _state.SelectMode();
		switch(_select_mode) {
			case TDDocViewState.SELECT_RECTANGLE:
				rx1 = rx2 = e.getX();
				ry1 = ry2 = e.getY();
				_empty = false;
				break;
			case TDDocViewState.SELECT_POLYGON:
				_lastpage = _state.Page();
				_empty = false;
				x1 = x2 = _x(e);
				l1 = l2 = _data.GetIndex1(x1,_y(e));
				if (l1!=-1)
					_viewport.Area().repaint();
				break;
		}
	}
	
	public void mouseReleased(MouseEvent e)
	{
		if (_lock) 
			return;
			
		TPolygon poly = null;
		
		switch(_select_mode) {
			case TDDocViewState.SELECT_RECTANGLE: {
					rx2 = e.getX();
					ry2 = e.getY();
					if (rx1>rx2) { int a = rx1; rx1 = rx2; rx2 = a; }
					if (ry1>ry2) { int a = ry1; ry1 = ry2; ry2 = a; }
					double x1,y1,x2,y2, w,h;
					w = (double)_state.Image().getWidth(null);
					h = (double)_state.Image().getHeight(null);
					x1 = (rx1+_state.X())/w;
					x2 = (rx2+_state.X()+1)/w;
					y1 = (ry1+_state.Y())/h;
					y2 = (ry2+_state.Y()+1)/h;
					poly = new TPolygon();
					poly.addPoint(x1,y1);
					poly.addPoint(x1,y1);
					poly.addPoint(x2,y1);
					poly.addPoint(x2,y2);
					poly.addPoint(x2,y2);
					poly.addPoint(x2,y2);
					poly.addPoint(x1,y2);
					poly.addPoint(x1,y1);
				}
				break;

			case TDDocViewState.SELECT_POLYGON:
				if (l1==-1)
					return;
				x2 = _x(e);
				l2 = _data.GetIndex2(x2, _y(e), l1);
				Image img = _state.Image();
				double fuzzy = (_state.FuzzyX()/(double)img.getWidth(null));
		 		if (_state.Page()!=null && _state.Page().ScanData()!=null) {
					poly = _data.GetPoly(l1,l2,x1,x2, fuzzy);
				}
		   	break;
		}

		// when the user has selected an area (poly!=null), edit the selection
		//---------------------------------------------------------------------
		if (poly!=null) {
			TDlgSelection dlg = new TDlgSelection(poly, _state, null);
			dlg.setVisible(true);
			if (dlg.result == TDlgSelection.OK) {
				_state.Page().Correction().Add(poly, dlg.DrawingData());
				_state.Notify(TDDocViewState.PAGE_CORRECTION_ADDED);
				_state.Document()._modified=true;
			}
			_viewport.Area().repaint();
  	  _empty = true;
 		}
	}
	
	public void mouseDragged(MouseEvent e)
	{
		if (_lock || _data==null)
			return;
		switch(_select_mode) {
			case TDDocViewState.SELECT_RECTANGLE:
				rx2 = e.getX();
				ry2 = e.getY();
				_viewport.Area().repaint();
				break;
			case TDDocViewState.SELECT_POLYGON:
				if (l1==-1)
					return;
				x2 = _x(e);
				int l = _data.GetIndex2(x2, _y(e), l1);
				if (l!=-1) {
					l2=l;
					_viewport.Area().repaint();
				}
				break;
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		if (_state==null || _state.Page()==null || _state.Image()==null)
			return;

		int idx = FindCorrection(e);
		if (idx!=_viewport._crnt_idx) {
			_viewport._crnt_idx = idx;
			_viewport.Area().repaint();
		}
		SetCursor();
	}

	public void SetCursor()
	{
		if (_viewport._crnt_idx==-1) {
			switch(_state.SelectMode()) {
				case TDDocViewState.SELECT_RECTANGLE:
					_viewport.Area().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case TDDocViewState.SELECT_POLYGON:
					_viewport.Area().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
					break;
			}
		} else {
			_viewport.Area().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	void paint(Graphics pen)
	{
		Image img = _state.Image();
		if (img==null) 
			return;
		int w = img.getWidth(null);
		int h = img.getHeight(null);
	
		if (_viewport._crnt_idx!=-1) {
			pen.setColor(Color.blue);
			pen.drawPolygon(_state.Page().Correction().Polygon(_viewport._crnt_idx, w, h));
		}

		switch(_select_mode) {
			case TDDocViewState.SELECT_RECTANGLE:
				if (!_empty) {
					pen.setColor(Color.red);
					Rectangle r = Main.CreateRectangleCoord(rx1,ry1, rx2,ry2);
					r.translate(_state.X(), _state.Y());
					Main.DrawRectangle(pen, r); 
				}
				break;

			case TDDocViewState.SELECT_POLYGON:
				if (l1==-1)
					return;
				_checkPage();
		 		if (!_empty && _state.Page()!=null && _state.Page().ScanData()!=null) {
		 			pen.setColor(Color.red);

		 			double fuzzy = (_state.FuzzyX()/(double)img.getWidth(null));
		 			TDScanData data = _state.Page().ScanData();
	 			
		 			TPolygon poly = data.GetPoly(l1,l2,x1,x2, fuzzy );
		 			if (poly!=null) {
						pen.setColor(Color.red);
						pen.drawPolygon( poly.GetAWTPolygon(w, h)	);
					}
				}
		    break;
		}
	}

	void _checkPage()
	{
		if (_lastpage!=null && _lastpage!=_state.Page()) {
			_empty = true;
		}
	}

	boolean TryPopup(MouseEvent e)
	{
		if (e.isPopupTrigger()) {
			_popup.SetLabel(_state.Filename());
			_popup.Show(e.getX(), e.getY());
			return true;
		}
		return false;
	}

	int FindCorrection(MouseEvent e)
	{
		Image image = _state.Image();
		if (image==null)
			return -1;

		TDPageCorrection pc = _state.Page().Correction();
		int n = pc.Size();
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		int x = e.getX() + _state.X();
		int y = e.getY() + _state.Y();
		int idx = -1;
		double d = 2.0;	// find in a range of 2 pixels
		for(int i=0; i<n; i++) {
			Polygon p = pc.Polygon(i,w,h);
			if (p.contains(x,y)) {
				idx = i;
				break;
			}
			for(int j=0; j<p.npoints-1; j++) {
				double d2 = draw.TFigure.getDistanceLine(x, y, 
																								 p.xpoints[j], p.ypoints[j],
																								 p.xpoints[j+1], p.ypoints[j+1]);
				if (d2<=d)	{
					d = d2;
					idx = i;
					break;
				}
			}
		}
		return idx;
	}
	
	/**
	* Try to edit a correction at mouse position. Returns `false' when there
	* was no marked area or `true' when editing is done.
	*/
	boolean TryCorrection(MouseEvent e)
	{
		int idx = FindCorrection(e);
	
		if (idx==-1) return false;	// no marked area here => report failure
			
		TDPageCorrection pc = _state.Page().Correction();
		TDlgSelection dlg = new TDlgSelection(pc.Polygon(idx), 
																					_state, 
																					pc.DrawingData(idx));
		dlg.setVisible(true);
		_state.Document()._modified=true;
		if (dlg.result==TDlgSelection.DELETE) {
			_viewport._crnt_idx = -1;	// don't try to draw the polygon anymore!
			pc.Remove(idx);
			_state.Notify(TDDocViewState.PAGE_CORRECTION_REMOVED);
		}
		return true;
	}		
}
