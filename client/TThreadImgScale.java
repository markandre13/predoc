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

class TThreadImgScale
	extends Thread
{
	TDScaledImage _scaled_image;
	int _scale;
	Image _image;
	
	public static final int NOSCALE = 100;
	
	TThreadImgScale(TDScaledImage si, int scale, Image image)
	{
		_scaled_image = si;
		_scale = scale;
		_image = image;
	}

	public void Flush()
	{
		_scaled_image = null;
		_image = null;
	}

	public void run()
	{
    int w, h;
    w = _image.getWidth(null);
    h = _image.getHeight(null);
    
    if (w==-1 || h==-1) {
			System.out.println("TThreadImgScale: fatal: main image not availabel");
			return;
    }
		
		Image image;
		
		if (_scale==NOSCALE) {
			image = _image;
		} else {
			int w2 = (int)((double)w*_factor(_scale));
			int h2 = (int)((double)h*_factor(_scale));
			image = _image.getScaledInstance(w2,h2,Image.SCALE_FAST);
	    MediaTracker _tracker = new MediaTracker(Main.wnd);
			_tracker.addImage(image, 0);
			try { _tracker.waitForAll(); }
			catch (Exception e) {
				System.out.println("fatal: TThreadImgScale couldn´t wait for image");
				return;
			}
		}
		_scaled_image._image_scaled(image);
		
		_image = null;
		image = null;
	}

	

  static double _factor(int scale)
  {
    return (double)scale / (double)NOSCALE;
  }
}
