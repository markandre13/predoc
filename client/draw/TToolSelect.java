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

package draw;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import common.*;
import draw.*;

public class TToolSelect
	extends TTool
{
	static class TSelectionNode	{
		public TFigure fig;		// the figure it self
		public Point ref;			// reference point for translations
	}

	Vector _selected;
	TFigure _clicked;
	int _handle;								// handle number or `-1'
	Point _handle_translation;	// handle translation or `null'
	TFigure _handle_figure;
	static TToolSelect _this = null;
	boolean no_move = false;
	
	public TToolSelect(TDrawingArea c)
	{
		super(c);
		_selected = c._selected;
/*
		if (_this!=null) {
			System.out.println("ERROR!!! TToolSelect MUST BE A SINGLETON!!!");
*/
		_this = this;
		_clicked = null;
	}

	public void start()
	{
		_selected.removeAllElements();
		if (_clicked!=null) {
//System.out.println("TToolSelect: ADDING CLICKED ELEMENT");
			TSelectionNode node = new TSelectionNode();
			node.fig = _clicked;
			_selected.addElement(node);
			_clicked = null;
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		for(int i=_drawingarea.Data().Size()-1; i>=0; i--) {
			if (_drawingarea.Data().Figure(i).mouseReleased(e))
				return;
		}
	}

	public void mousePressed(MouseEvent e)
	{
//System.out.println("TToolSelect: MOUSE PRESSED");

		no_move = false;

		_handle = -1;
		int i,n;
		
		// event handling
		//--------------------------------------------------------------
		for(i=_drawingarea.Data().Size()-1; i>=0; i--) {
			if (_drawingarea.Data().Figure(i).mousePressed(e))
				return;
		}

		// lookup handles of selected figures
		//--------------------------------------------------------------
		n=_selected.size();
		for(i=0; i<n; i++) {
			TSelectionNode node = (TSelectionNode)_selected.elementAt(i);
			int h = node.fig.getHandle(e.getX(), e.getY());
			if (h>=0) {
				_handle_translation = node.fig.getHandlePosition(h);
				if (_handle_translation!=null) {
					_handle_translation.x -= e.getX();
					_handle_translation.y -= e.getY();
				}
				_handle = h;
				_handle_figure = node.fig;
				return;
			}
		}

		// found no handle, search for figure within pointer range (with
		// special treatment for the position of the TFigCorrection form
		//--------------------------------------------------------------
		TFigure fig = null;
		double min_distance = TFigure.OUT_OF_RANGE;
		TFigCorrection fc = _drawingarea.Data().FigCorrection();
		if (fc.Type()!=0) {	// correction in the foreground, check first
			double d = fc.Distance(e.getX(), e.getY());
			if ( d <= TFigure.RANGE && d<min_distance) {
				min_distance = d;
				fig = fc;
			}
		}
		if (fig==null) {
			for(i=_drawingarea.Data().Size()-1; i>=0; i--) {
				TFigure f = _drawingarea.Data().Figure(i);
				if (f==fc)
					continue;
				double d = f.Distance(e.getX(), e.getY());
				if ( d <= TFigure.RANGE && d<min_distance) {
					min_distance = d;
					fig = f;
				}
			}
			if (fc.Type()==0) {	// correction in the background, check last
				double d = fc.Distance(e.getX(), e.getY());
				if ( d <= TFigure.RANGE && d<min_distance) {
					min_distance = d;
					fig = fc;
				}
			}
		}
		if (min_distance<0.0) {
			no_move = true;
		}

		// when a figure was found, add it to the list of selected figures
		//----------------------------------------------------------------
		if (fig!=null) {	// found 'fig'
			// is 'fig' already stored in '_selected' ?
			n=_selected.size();
			for(i=0; i<n; i++) {
				TSelectionNode node = (TSelectionNode)_selected.elementAt(i);
				if (node.fig == fig)
					break;
			}
			if (i>=n) {	// 'fig' is not in '_selected', add it
				if (!e.isControlDown())
					_selected.removeAllElements();
				TSelectionNode node = new TSelectionNode();
				node.fig = fig;
				_selected.addElement(node);
			}
		} else {
			_selected.removeAllElements();
		}

		n=_selected.size();
		for(i=0; i<n; i++) {
			TSelectionNode node = (TSelectionNode)_selected.elementAt(i);
			node.ref = node.fig.getRefPoint(e.getX(), e.getY());
		}
		
		Invalidate();
	}

	public void mouseDragged(MouseEvent e)
	{
		int n;

		// event handling
		//--------------------------------------------------------------
		for(int i=_drawingarea.Data().Size()-1; i>=0; i--) {
			if (_drawingarea.Data().Figure(i).mouseDragged(e))
				return;
		}

		n = _selected.size();
//System.out.println("TToolSelect: MOUSE DRAGGED");
		if (_handle>=0) {
			if (_handle_translation!=null) {
				_handle_figure.translateHandle(
					_handle, 
					e.getX()+_handle_translation.x, 
					e.getY()+_handle_translation.y
				);
			} else {
				_handle_figure.translateHandle(_handle, e.getX(), e.getY());
			}
		} else {
			n=_selected.size();
			if (n>0 && !no_move) {
				for(int i=0; i<n; i++) {
					TSelectionNode node = (TSelectionNode)_selected.elementAt(i);
					node.fig.translate(node.ref, e.getX(), e.getY());
				}
			}
		}
		Invalidate();
		return;
	}

	/**
	* this method delegates key events to TFigCorrection when it's
	* selected
	*/
	public void keyPressed(KeyEvent e)
	{
		TSelectionNode node;
		int n=_selected.size();
		int i;
		// special treatment for TFigCorrection
		//--------------------------------------
		for(i=0; i<n; i++) {
			node = (TSelectionNode)_selected.elementAt(i);
			if (node.fig instanceof TFigCorrection) {
				if (n>1) {
					// remove all other selection to avoid confusions
					_selected.removeAllElements();
					_selected.addElement(node);
				}
				((TFigCorrection)node.fig).keyPressed(e);
				Invalidate();
				return;
			}
		}

		switch(e.getKeyCode()) {
			case KeyEvent.VK_DELETE:
				for(i=0; i<n; i++) {
					node = (TSelectionNode)_selected.elementAt(i);
					_drawingarea.Data().Remove(node.fig);
				}
				_selected.setSize(0);
				Invalidate();
				break;
		}
	}
	
	public void mouseClicked(MouseEvent e)
	{
//System.out.println("TToolSelect: MOUSE CLICKED");
		if (e.getClickCount()==2) {
			TFigure fig = null;
			double min_distance = TFigure.OUT_OF_RANGE;
			for(int i=_drawingarea.Data().Size()-1; i>=0; i--) {
				TFigure f = _drawingarea.Data().Figure(i);
				double d = f.Distance(e.getX(), e.getY());
				if ( d <= TFigure.RANGE && d<=min_distance) {
					min_distance = d;
					fig = f;
				}
			}
			if (fig!=null) {
				System.out.println("CLICKED");
				// this is bad style:
				if (fig instanceof TFigText) {
					_drawingarea.SetTool(new TToolDrawText(_drawingarea, (TFigText)fig));
					_clicked = fig;
				}
			}
		}
	}
	
	public void paint(Graphics pen)
	{
		int n=_selected.size();
//System.out.println("TToolSelect: HAVE "+n+" SELECTED FIGURES");
		for(int i=0; i<n; i++) {
			TSelectionNode node = (TSelectionNode)_selected.elementAt(i);
			node.fig.paintHandles(pen);
		}
	}
}
