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

import common.*;

/*
 *  Aufgabe dieser Klasse ist es in der Bitmap nach Boundingrectangles für
 * alle Buchstaben und Grafiken im Text zu suchen.
 *  Folgende Daten werden ermittelt:
 *  - Das kleinste den bedruckten Bereich umgebende Rechteck, normiert
 *    auf die Größe der Bitmap. Im folgenden "Bounding Rectangle (BR)" 
 *    genannt.
 *  - Die die Buchstaben, etc. umgebenden Rechtecke relativ zum BR und auf
 *    die Größe des BR normiert. Im folgenden "Letter Rectangle (LR)" 
 *    genannt.
 *
 */

class TThreadImgScan
extends Thread
{
	TDPage _page;
	Image _image;

	public TThreadImgScan(TDPage page, Image image)
	{
		_page = page;
		_image = image;
	}

	public void run()
	{
		int contrast = 200;
	
		TDScanData data = new TDScanData();
		
		int width = _image.getWidth(null);
		int height = _image.getHeight(null);
		
		while (width<0 || height<0) {
			yield();
			MediaTracker tracker = new MediaTracker(Main.wnd);
			tracker.addImage(_image, 0);
			try { tracker.waitForAll(); }
			catch (Exception e) {
				System.out.println("fatal: TThreadImgScan couldn´t wait for image");
				return;
			}
		}

		int[] pixels = new int[width*(height+1)];		// +1 is to hide a bug in the
																								// loop below

		// grab pixels
		//-------------
		PixelGrabber pg = new PixelGrabber(_image,0,0,width,height,pixels,0,width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("TThreadImgScan: pixel grabber interrupted");
			return;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			System.err.println("TThreadImgScan: image fetch aborted or errored");
			return;
		}

		// reduce to black and white
		//---------------------------
		for (int py = 0; py < height; py++) {
			for (int px = 0; px < width; px++) {
				int pixel = pixels[py * width + px];
				int red   = (pixel >> 16) & 0xff;
				int green = (pixel >>  8) & 0xff;
				int blue  = (pixel      ) & 0xff;
				double gray = red*0.30 + green*0.59 + blue*0.11;
				pixels[py * width + px] = (int) gray;
			}
			yield();
		}

		// get bounding rectangle
		//------------------------
		int brx1=width, bry1=height, brx2=0, bry2=height;
		boolean ll = false;
		for (int py = 0; py < height; py++) {
			int px;
			for (px = 0; px < width; px++) {
				if (pixels[py * width + px]<contrast) {
					if (brx1>px)
						brx1=px;
					break;
				}
			}
			if (px<width) {
				ll = true;
				if (bry1==height) {
					bry1 = py;
				}
				for (px = width; px >= 0; --px) {
					if (py * width + px >= height*width) {
						System.out.println("index out of range:" + (py * width + px) +">=" +(height*width));
						System.out.println("pos:"+px+","+py);
					}
					if (pixels[py * width + px]<contrast) {
						if (brx2<px)
							brx2=px;
						break;
					}
				}
			} else {
				if (ll)
					bry2 = py-1;
				ll = false;
			}
			yield();
		}
		
		if (brx1>brx2 || bry1==height)
			System.out.println("PReDoc: NO BOUNDING RECTANGLE FOR PAGE");

		brx2=brx2-brx1+1;
		bry2=bry2-bry1+1;
		data.SetBR(
			new TRectangle(
				(double)brx1/(double)width, (double)bry1/(double)height,
				(double)brx2/(double)width, (double)bry2/(double)height
			)
		);
		
		// vertical loop (detects rows)
		//------------------------------
		boolean flag;
		boolean[] vertical = new boolean[height];
     
		for (int py = 0; py < height; py++) {
			flag = false;
			for (int px = 0; px < width; px++) {
				if (pixels[py * width + px]<contrast) {
					flag = true;
					break;
				}
			}
			vertical[py] = flag;
			yield();
		}

		// vertical loop (detects chars)
		//-------------------------------
		int top=0, bottom, left=0, right;
		flag = false;
		for (int py = 0; py < height; py++) {
			if (!flag && vertical[py]) {
				top=py;
				flag = true;
			}
			if (flag && !vertical[py]) {
				bottom = py-1;
				// horizontal character loop
				boolean inside = false;
				data.NewLine();
				for (int px2=0; px2<width; px2++) {
					flag = false;
					for(int py2=top; py2<=bottom; py2++) {
						if ( pixels[py2 * width + px2]<contrast ) {
							flag = true;
							break;
						}
					}
					if (!inside && flag) {
						inside = true;
						left = px2;
					}
					if (inside && !flag) {
						inside = false;
						right = px2-1;
						
						data.AddLetterRect(
							new TRectangle(
								(double)(left)  / (double)width,
								(double)(top )  / (double)height,
								(double)(right-left) / (double)width,
								(double)(bottom-top) / (double)height
							)
						);
					}
				}
				flag = false;
			}
			yield();
		}
		_page._imageScaned(data);
	}
}
