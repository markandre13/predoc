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

import common.TState;
import common.TStateListener;

/*
 * Class to coordinate the behaviour of all classes being part of
 * a single document viewer.
 */

class TDDocViewState
	extends TState
	implements TStateListener
{
  public static final int PAGE_CHANGED		= 1;
  public static final int PAGE_LOADED			= 2;
  public static final int PAGE_VIEWABLE 	= 4;
  public static final int PAGE_SCANNED		= 8;
  public static final int PAGE_SCROLLED		= 16;
  public static final int PAGE_NO_CHANGED = 32;
  public static final int PAGE_CORRECTION_ADDED = 64;
  public static final int PAGE_CORRECTION_REMOVED = 128;
  public static final int PAGE_CORRECTION_CHANGED = 256;

	public static final int SELECT_RECTANGLE = 1;
	public static final int SELECT_POLYGON   = 2;
	protected int _select_mode;
	public void SetSelectMode(int v) { _select_mode = v; }
	public int SelectMode() { return _select_mode; }

  TDDocumentBuffer _documents;
  TDDocument _doc;
  TDPage _page;
  int _current_page;
  int _scale;
  

  // Attention: don´t confuse the visible area with the image!
  int _x, _y; 				// upper, left corner of the visible area
  int _w, _h; 				// size of the visible area

  TDDocViewState()
  {
    _documents = Main.documents;
    _documents.Register(this);

    _current_page = 0;
    _scale = 100;

     if (_documents.Size()==0) {
       _doc = null;
       _page = null;
     } else {
       _doc = _documents.Document(0);
       _page = _doc.Page(_current_page);
     }
     
     _x = _y = 0;
//     _jx = _jy = true;
  }

	/**
	 * If available go to the next page and notify all observers.
	 */
  void NextPage()
  {
    if (_doc!=null && _current_page < _doc.PageCount()-1 ) {
	    _page.FreeImage(_scale, this);
	    _page = null;
      _current_page++;
      Notify(PAGE_NO_CHANGED);
      _page = _doc.Page(_current_page);
      Notify(PAGE_CHANGED);
    }
  }

	/**
	 * If available go to the previous page and notify all observers.
	 */
  void PrevPage()
  {
    if (_doc!=null && _current_page > 0 ) {
	    _page.FreeImage(_scale, this);
	    _page = null;
      _current_page--;
      Notify(PAGE_NO_CHANGED);
      _page = _doc.Page(_current_page);
      Notify(PAGE_CHANGED);
    }
  }

	/**
	 * Go to page <VAR>n</VAR>.
	 */
	void GoPage(int n) {
		if (n == _current_page || n<0 || n >= _doc.PageCount() )
			return;
		_page.FreeImage(_scale, this);
		_page = null;
		_current_page = n;
		Notify(PAGE_NO_CHANGED);
		_page = _doc.Page(_current_page);
		Notify(PAGE_CHANGED);
	}

	/**
	 * Change zoom and notify all observers.
	 */
	void SetZoom(int zoom)
	{
		_scale = zoom;
		Notify(PAGE_CHANGED);
	}
	
	int Zoom() {
		return _scale;
	}

	/**
	 * Called from TDDocumentBuffer when documents have been added
	 * or are removed.
	 */
  public void stateChanged(TState state)
  {
  	if (state instanceof TDDocumentBuffer) {
  		switch(state.Reason()) {
  			case TDDocumentBuffer.DOCUMENT_ADDED:
		    	_doc = ((TDDocumentBuffer)state).Document(_documents.Size()-1);
 		     	_page = _doc.Page(0);
 		     	Notify(PAGE_CHANGED);
 		     	return;
				case TDDocumentBuffer.DOCUMENT_REMOVED:
					_doc = null;
					_page = null;
					Notify(PAGE_CHANGED);
					return;
 	    }
    }
    if (state instanceof TDScaledImage) {
    	switch(state.Reason()) {
	    	case TDScaledImage.SCALED_IMAGE_AVAILABLE:
	    		if (Main.debug_mvc)
	    			System.out.println("TDDocViewState.stateChanged: SCALED_IMAGE_AVAILABLE from TDScaledImage");
	    		Notify(PAGE_VIEWABLE);
	    		return;
				case TDScaledImage.SCAN_AVAILABLE:
					if (Main.debug_mvc)
	    			System.out.println("TDDocViewState.stateChanged: MAIN_IMAGE_AVAILABLE from TDScaledImage");
	    		Notify(PAGE_SCANNED);
	    		return;
	    	case TDScaledImage.MAIN_IMAGE_AVAILABLE:
	    		if (Main.debug_mvc)
	    			System.out.println("TDDocViewState.stateChanged: MAIN_IMAGE_AVAILABLE from TDScaledImage");
	    		Notify(PAGE_LOADED);
	    		return;
	    }
    }
    if (Main.debug_mvc)
    	System.out.println("TDDocViewState.stateChanged: unknown");
  }

  /*
   * Called when one of scrollbars was moved.
   */

  public void SetPosition(int x, int y)
  {
		_x = x;
		_y = y;
    Notify(PAGE_SCROLLED);
  }

  int    X()    { return _x; }
  int    Y()    { return _y; }

  TDPage Page() { return _page; }
  int    PageNo() { return _current_page; }
  int		 PageCount() { return _doc.PageCount(); }

	// fuzzy range for horizontal during selection justification	
	double FuzzyX() { return 5.0; }
	
	TDDocument Document() { return _doc; }
	String Filename() { return _doc.Filename(); }

  Image Image()
  {
  	if (_page==null)
  		return null;
  	return _page.GetImage(_scale, this);
  }
  
  /**
   * See TPage.MainImage()
   */
  Image UnscaledImage()
  {
  	if (_page==null)
  		return null;
  	return _page.MainImage();
  }
  
  // each document should only have one overview window so when you want
  // to create a new one use this methods:
  //--------------------------------------------------------------------
  TOverview _overview;
	public void SetOverview(TOverview ov) { _overview = ov; }
	public TOverview Overview() { return _overview; }
}
