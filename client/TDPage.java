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

/*
 * Representation of a single page.
 * (and a cache for differently scaled images of the page)
 *
 * This one stores:
 * - the image
 * - scaled versions of the image
 * - the data from the scanner
 * - the selected parts of the page
 */

import java.awt.*;
import java.util.*;
import java.awt.image.*;

import common.TState;
import common.TStateListener;

class TDPage
{
  Image _main_image;		// main image
  Vector _scaled;				// scaled versions of the images
	server.TPage _page;		// remote page

  TDScanData _scan_data;
  TDPageCorrection _correction;
  
	Thread _thread;				// thread that loads the image

  Vector _listener;			// TDDocViewStates using this page (needed to remove main image)

	// constructor
	//-------------------------------------------------------------------------
	public TDPage(server.TPage page)
	{
    _main_image = null;
    _scaled			= new Vector();
		_listener		= new Vector();
		_page = page;

    _scan_data	= null;
    _correction = new TDPageCorrection();

    _thread = null;
	}

	public TDScanData ScanData() {	return _scan_data; }
	public TDPageCorrection Correction() { return _correction; }
	public void SetCorrection(TDPageCorrection c) { _correction = c; }
	
	/**
	 * Delivers the unscaled original image or null. No further
	 * actions will be started to load the image.<BR>
	 */
	public Image MainImage()
	{
		return _main_image;
	}

	/**
	 * Delivers a scaled version of the image. When called for another
	 * scaling factor, the old Image might be removed from the memory,
	 * depending on the other TDDocViewStates.<BR>
	 * When the image is not available, threads will load, scale and
	 * scan the image and inform TDDocViewState.
	 */
  synchronized public Image GetImage(int scale, TDDocViewState listener)
  {
  	if (Main.debug_mvc)
  		System.out.println("TDPage.GetImage");
  
    // 1. register listener
    //------------------------------------
    TDScaledImage scaled = FindScaled(listener);
    if (scaled == null)
    	scaled = CreateScaled(listener, scale);
 
 		// 2. check scaling factor
 		//------------------------------------
 		if (scaled.Scale()!=scale) {
			if (Main.debug_memory)
				System.out.println("  will remove scaled image          "+Main.Memory());
			scaled.UnRegister(listener);
			if (scaled.Listeners()==0)
				_scaled.removeElement(scaled);
			scaled = CreateScaled(listener, scale);
			if (Main.debug_memory)
				System.out.println("  removed scaled image (or not)     "+Main.Memory());
		}

  	// 3. load main image if neccesary
  	//------------------------------------
    if (_main_image==null) {
      if (_thread==null) {
      	if (Main.debug_memory)
					System.out.println("  will load image                   "+Main.Memory());
      	_start_load();		// method call so derived classes can change this
      }
      return null;				// return now, thread will call _imageLoaded
    }

		// 4. load scaled image if neccesary
		//-----------------------------------
		
		return scaled.Image();
  }

	/**
	 * Start thread to load the image. It will call _imageLoaded below
	 * when it's done.
	 */
	void _start_load()
	{
		if (_page==null) {
			System.out.println("TThreadImgDownload: no remote page");
		} else {
			_thread = new TThreadImgDownload(this, _page);
			_thread.start();
		}
	}

  synchronized public void _imageLoaded(Image image)
  {
  	if (_scaled.size()==0) {
  		System.out.println("TDPage._imageLoaded: ...and removed at once");
  		System.out.println("                     Main.thread_safe_corba==false?");
  		_thread=null;
  		return;
  	}
  
  	if (Main.debug_mvc)
  		System.out.println("TDPage._imageLoaded");
  	
		if (Main.debug_memory)
			System.out.println("  loaded image                      "+Main.Memory());

		int n=_scaled.size();
		for(int i=0; i<n; i++)
			((TDScaledImage)_scaled.elementAt(i)).Notify(TDScaledImage.MAIN_IMAGE_AVAILABLE, null);

    _main_image = image;

		// force TDScaleImages to create their scaled version by calling
		// Image()
		if (n==0) {
			System.out.println("TDPage._imageLoaded: no need to view it");
		}
		for(int i=0; i<n; i++)
			((TDScaledImage)_scaled.elementAt(i)).Image();

		_thread = null;

		// start the scanner
		if (_scan_data==null) {
			_thread = new TThreadImgScan(this, _main_image);
			_thread.start();
		}
  }

	synchronized public void _imageScaned(TDScanData data)
	{
  	if (Main.debug_mvc)
  		System.out.println("TDPage._imageScanned");
		int n=_scaled.size();
		_scan_data = data;
		_thread = null;
		for(int i=0; i<n; i++)
			((TDScaledImage)_scaled.elementAt(i)).Notify(TDScaledImage.SCAN_AVAILABLE, null);
	}

  synchronized public void FreeImage(int scale, TDDocViewState listener)
  {
  	if (_thread!=null)
  		_thread.suspend();
  	_free_image(scale, listener);
  	if (_thread!=null)
  		_thread.resume();
  }

	synchronized private void _free_image(int scale, TDDocViewState listener)
	{
  	if (Main.debug_mvc)
	  	System.out.println("TDPage.FreeImage: free image for zoom " + scale);

		TDScaledImage scaled = FindScaled(listener);
		if (scaled==null) {
			System.out.println("TDPage.FreeImage: image isn't stored");
			return;
		}

		if (Main.debug_memory)
			System.out.println("  will remove scaled image          "+Main.Memory());
		scaled.UnRegister(listener);
		if (scaled.Listeners()==0)
			_scaled.removeElement(scaled);
		scaled=null;
		if (Main.debug_memory)
			System.out.println("  removed scaled image (or not)     "+Main.Memory());

		if (_scaled.size()==0) {
			if (Main.debug_memory)
				System.out.println("  will remove main image            "+Main.Memory());
			if (Main.thread_safe_corba || _main_image!=null) {
				if (_thread!=null)
					_thread.stop();
				_thread=null;
			}
			if (Main.debug_memory_force && _main_image!=null)
				_main_image.flush();
			_main_image=null;
			if (Main.debug_memory)
				System.out.println("  removed main image                "+Main.Memory());
		}
	}

  /*
   * Inform all TDocObservers that the image is available now.
   */

	/**
	 * Find image with scaling factor <VAR>scale</VAR>. Will return null
	 * when there´s none.
	 */
  TDScaledImage FindScaled(TDDocViewState state) 
  {
  	int n = _scaled.size();
    for (int i=0; i<n; i++) {
      TDScaledImage scaled = (TDScaledImage)_scaled.elementAt(i);
      if (scaled.Contains(state)) {
      	return scaled;
      }
    }
    return null;
  }

	/**
	 * Register TDDocViewState for scaling factor <VAR>scale</VAR>.
	 */
  TDScaledImage CreateScaled(TDDocViewState state, int scale)
  {
  	// lookup list of scaled images if we can satisfy the request
  	// without creating a new image
  	//-----------------------------------------------------------
  	TDScaledImage scaled;
  	int n = _scaled.size();
    for (int i=0; i<n; i++) {
      scaled = (TDScaledImage)_scaled.elementAt(i);
      if (scaled.Scale()==scale) {
      	scaled.Register(state);
      	return scaled;
      }
    }
    
    scaled = new TDScaledImage(this, scale);
    scaled.Register(state);
    _scaled.addElement(scaled);
    return scaled;
	}
}
