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
import java.util.*;
import java.awt.image.*;

import common.TState;
import common.TStateListener;
class TDScaledImage
	extends TState
{
	public static final int MAIN_IMAGE_AVAILABLE = 1;
	public static final int SCALED_IMAGE_AVAILABLE = 2;
	public static final int SCAN_AVAILABLE = 4;

  private int _scale;			// value to identify the scale factor
  private Image _image;		// the scaled image
  private TDPage _page;		// the page
  private TThreadImgScale _thread;	// image scaler
    
  TDScaledImage(TDPage page, int scale) {
  	_page = page;
  	_scale = scale;
  	_image = null;
  	_thread = null;
  }

	synchronized public void UnRegister(TStateListener l)
	{
		super.UnRegister(l);
		if (Listeners()==0) {
			if (_thread!=null) {
				_thread.stop();
				_thread.Flush();
				_thread = null;
			}
			if (Main.debug_memory_force && _image!=null && _scale!=TThreadImgScale.NOSCALE)
				_image.flush();
			_image=null;
		}
	}

	synchronized public Image Image()
	{
		if (Main.debug_mvc)
			System.out.println("TDScaledImage.Image()");
		if (_image == null && _thread==null && _page._main_image!=null ) {
			if (Main.debug_mvc)
				System.out.println("TDScaledImage.Image(): scaling start");
			_thread = new TThreadImgScale(this, _scale, _page._main_image);
			_thread.run();
		}
		return _image;
	}
	
	int Scale()
	{
		return _scale;
	}
	
	synchronized void _image_scaled(Image image)
	{
		if (Main.debug_memory)
			System.out.println("  scaled                            "+Main.Memory());
		_image = image;
		_thread.stop();
		_thread=null;
		Notify(SCALED_IMAGE_AVAILABLE);
	}
}
