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

import java.awt.*;
import java.awt.event.*;
import common.*;

public class TScrolledArea
	extends Container
	implements AdjustmentListener
{
	TScrolledArea _this;

	Scrollbar _vscroll, _hscroll;

	Canvas _area;
	public Canvas Area() { return _area; }

	public final static int FIXED_SIZE = 13;
	
	public int area_x, area_y;											// (item)
	public int area_x1, area_y1, area_x2, area_y2;	// area size in 2D mode (item)
	public int area_min, area_max;									// area size in 1D mode (item)
	public int item_w, item_h;											// (pixel)
	
	public int _visi_x, _visi_y;									// upper left corner (pixel)
	public int _visi_w, _visi_h;														// visible items (item)

	public boolean draw_frame;
	
	boolean _double_buffer;													// use double buffering
	public void SetDoubleBuffer(boolean b) { _double_buffer = b; }

	public final static int ALL_AT_ONCE=0;	
	public final static int PER_ITEM=1;
	public final static int PER_ROW=2;
	public final static int PER_COLUMN=3;
	int _paint_method;
	public void SetPaintTo(int n) { _paint_method = n; }
	
	public final static int ALMOST_NONE = 0;
	public final static int EACH_ITEM = 1;
	int _clip_method;
	public void SetClippingTo(int n) { _clip_method = n; }

	public final static int DIM1 = 1;
	public final static int DIM2 = 2;
	int _dim_method;
	public void SetDimensionTo(int n) { _dim_method = n; }
	
	public final static int STATIC = 1;
	public final static int FIT_WIDTH = 2;
	public final static int FIT_HEIGHT = 3;
	int _dim1_method;
	public void Set1DAdjustTo(int n) { _dim1_method = n; }

	public final static int CENTER_NONE = 0;
	public final static int CENTER_X    = 1;
	public final static int CENTER_Y    = 2;
	public final static int CENTER_XY   = 3;
	int _center_method;
	public void SetCenterTo(int n) { _center_method = n; }
	
	public TScrolledArea()
	{
		_this = this;
		_vscroll = _hscroll = null;
		
		draw_frame = false;

		area_x1 = area_x2 = area_y1 = area_y2 = 0;
		item_w = item_h = 1;
		area_x = area_x1; area_y = area_y1;

		_paint_method = PER_ITEM;
		_clip_method  = EACH_ITEM;
		_dim_method		= DIM2;
		_dim1_method  = FIT_WIDTH;
		_center_method= CENTER_XY;
		_double_buffer= false;

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				_this.resize();
				Update(); 
			}
		});
		
		_area = _NewCanvas();
		add(_area);
	}

	// create the canvas which is used for the actual drawing operations
	//   currently there's only support for the operations implemented
	//-------------------------------------------------------------------
	protected Canvas _NewCanvas() 
	{
		return new Canvas()
		{
			public void update(Graphics pen)
			{ 
				paint(pen);
			}

			public void paint(Graphics jpen)
			{
//System.out.println("common.TScrolledArea.paint: ENTRY");
				int x,y,xp,yp, n;

				// create a double buffer pen when necessary
				//------------------------------------------
				Graphics pen;
				Image buffer = null;
				if (_double_buffer) {
					int w,h;
					w = getSize().width;
					h = getSize().height;
					if (w<=0 || h<=0)
						return;
					buffer = createImage(w, h);
					pen = buffer.getGraphics();
				} else {
					pen = jpen;
				}
				
				if (_paint_method==ALL_AT_ONCE) {
					pen.setClip(_visi_x, _visi_y, _visi_w*item_w+1, _visi_h*item_h+1);
					pen.translate(_visi_x-area_x, _visi_y-area_y);
					paintItem(pen, 0,0);
				}	else {
					switch(_dim_method) {
						case DIM1:
							switch(_paint_method) {
								case PER_ITEM:
									if (_clip_method==ALMOST_NONE) {
										pen.setClip(_visi_x, _visi_y, _visi_w*item_w, _visi_h*item_h);
									}
									for(y=0,yp=_visi_y; y<_visi_h; y++,yp+=item_h) {
										n = (area_x-area_x1)+(y+area_y-area_y1)*(area_x2-area_x1+1)+area_min;
										for(x=0,xp=_visi_x; x<_visi_w && n<=area_max; x++,xp+=item_w) {
											if (_clip_method==EACH_ITEM) {
												pen.setClip(xp,yp,item_w,item_h);
											}
											pen.translate(xp,yp);
											paintItem(pen,n++,0);
											pen.translate(-xp,-yp);
										}
									}
									break; // end of _dim_method==DIM1, _paint_method==PER_ITEM
								default:
									System.out.println("TScrolledArea: not implemented yet");
							}
							break;	// end of _dim_method==DIM1

						case DIM2:
							switch(_paint_method) {
								case PER_ITEM:
//System.out.println("PER_ITEM");
									if (_clip_method==ALMOST_NONE) {
										pen.setClip(_visi_x, _visi_y, _visi_w*item_w, _visi_h*item_h);
									}
									for(y=0,yp=_visi_y; y<_visi_h; y++,yp+=item_h) {
										for(x=0,xp=_visi_x; x<_visi_w; x++,xp+=item_w) {
											if (_clip_method==EACH_ITEM) {
												pen.setClip(xp,yp,item_w,item_h);
											}
											pen.translate(xp,yp);
											paintItem(pen,x+area_x,y+area_y);
											pen.translate(-xp,-yp);
										}
									}
									break; // end of _dim_method==DIM2, _paint_method==PER_ITEM

								case PER_ROW:
//System.out.println("PER_ROW");
									if (_clip_method==ALMOST_NONE) {
										pen.setClip(_visi_x, _visi_y, _visi_w*item_w, _visi_h*item_h);
									}
									
									n = _visi_h;
/*
System.out.println(  "area_y:"+area_y+
									 " area_y1:"+area_y1+
									 " area_y2:"+area_y2+
									 " _visi_h:"+_visi_h);
*/
									if ((_center_method&CENTER_Y)==0 && area_y+n <= area_y2)
										n++;
									for(y=0,yp=_visi_y; y<n; y++,yp+=item_h) {
										if (_clip_method==EACH_ITEM) {
											pen.setClip(_visi_x,yp,item_w*_visi_w,item_h);
										}
										pen.translate(_visi_x-area_x,yp);
										paintItem(pen,0,y+area_y);
										pen.translate(-(_visi_x-area_x),-yp);
									}
									break; // end of _dim_method==DIM2, _paint_method==PER_ROW

								default:
									System.out.println("TScrolledArea: not implemented yet");
							}
							break;	// end of _dim_method==DIM2
					}
				}

				if (draw_frame) {
					pen.setColor(new Color(192,192,192));
					pen.setClip(0,0,getSize().width, getSize().height);
					pen.draw3DRect(0,0,getSize().width-1, getSize().height-1, false);
//					pen.draw3DRect(1,1,getSize().width-3, getSize().height-3, false);
				}

				if (_double_buffer) {
//					System.out.println("drawing double buffer");
					jpen.drawImage(buffer,0,0, null);
					pen.dispose();
				}
//System.out.println("common.TScrolledArea.paint: EXIT");
			}	// end of `Canvas.paint(Graphics)'
		}; // end of `return class Canvas {'
	} // end of `NewCanvas()'
		
	public void paintItem(Graphics pen, int x, int y)
	{
		pen.fillRect(0,0,item_w-1,item_h-1);
		pen.drawString(x+","+y,2,16);
	}
	
	public void adjustScrollbars(Rectangle h, Rectangle v)
	{
	}

	public void resize()
	{
	}

	public void scrolled()
	{
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if (_hscroll!=null)
			area_x = _hscroll.getValue();
		if (_vscroll!=null)
			area_y = _vscroll.getValue();
		scrolled();
		Update();
	}
	
	public void Update()
	{
//System.out.println("common.TScrolledArea.Update: ENTRY");
		// calculate visible area size
		//-------------------------------------------------------------
		Dimension ws = getSize();
		Insets wi = getInsets();
		int x,y,w,h;
		x = wi.left;
		y = wi.top;
		w = ws.width  - wi.left - wi.right  - 1 ;
		h = ws.height - wi.top  - wi.bottom - 1 ;

		// 
		//-------------------------------------------------------------
		int aw_i, ah_i;

		if (_dim_method==DIM1) {
			switch(_dim1_method) {
				case FIT_WIDTH:
					// uh, thats complicated 'cause there must be a loop
					// calculate visible area in pixel (aw_p, ah_p)
					//----------------------------------------------
					aw_i = w / item_w;
					if ( aw_i * (h/item_h) < area_max-area_min+1 ) {
						aw_i = (w-FIXED_SIZE) / item_w;
					}
					if (aw_i==0) {
						aw_i=1;
					}
//System.out.println("width " +n+ " can hold " +aw_i+ " items of width " +item_w);
					area_x2 = aw_i + area_x1 - 1;
					area_y2 = ( (area_max-area_min+1) / aw_i ) + area_y1;
					
					break;
			}
		}

		// calculate visible area in pixel (aw_p, ah_p)
		//-------------------------------------------------------------
		aw_i = area_x2-area_x1+1;
		ah_i = area_y2-area_y1+1;

		int aw_p = aw_i * item_w;
		int ah_p = ah_i * item_h;
		
		boolean hs = false, vs = false;

		if (aw_p>w) {
			hs = true;
			h-=FIXED_SIZE;
		}
		if (ah_p>h) {
			vs = true;
			w -= FIXED_SIZE;
		}
		if (!hs && aw_p>w) {
			hs = true;
			h-=FIXED_SIZE;
		}
		
		// calculate the visible area in items (_visi_w, _visi_h)
		//-------------------------------------------------------------
		_visi_w = w / item_w;
		_visi_h = h / item_h;
		
		if (_visi_w > aw_i) {
			_visi_w = aw_i;
		}
		if (_visi_h > ah_i) {
			_visi_h = ah_i;
		}

		// adjust position when window is growing
		//-------------------------------------------------------------
		if (area_x+_visi_w > area_x2) {
			area_x = area_x2 - _visi_w + 1;
		}

		if (area_y+_visi_h > area_y2) {
			area_y = area_y2 - _visi_h + 1;
		}
		
		// optimize me:
		_visi_x = 0;
		if ((_center_method & CENTER_X)!=0)
			_visi_x = (w - (_visi_w * item_w)) >> 1;
		_visi_y = 0;
		if ((_center_method & CENTER_Y)!=0)
			_visi_y = (h - (_visi_h * item_h)) >> 1;

		// setup scrollbars
		//-------------------------------------------------------------
		
		Rectangle hr=null, vr=null;
		
		if (hs) {
			if (_hscroll==null) {
				_hscroll = new Scrollbar(Scrollbar.HORIZONTAL);
				_hscroll.addAdjustmentListener(this);
				add(_hscroll);
			}
			hr = new Rectangle(x, y+h+1, w+1, FIXED_SIZE);
			_hscroll.setValues(area_x, _visi_w, area_x1, area_x2+1);
		}
		if (_hscroll!=null)
			_hscroll.setVisible(hs);
			
		if (vs) {
			if (_vscroll==null) {
				_vscroll = new Scrollbar(Scrollbar.VERTICAL);
				_vscroll.addAdjustmentListener(this);
				add(_vscroll);
			}
			vr =new Rectangle(x+w+1, y,	FIXED_SIZE, h+1);
			_vscroll.setValues(area_y, _visi_h, area_y1, area_y2+1);
		}
		if (_vscroll!=null)
			_vscroll.setVisible(vs);

		adjustScrollbars(hr, vr);
		if (hs)
			_hscroll.setBounds(hr);
		if (vs)
			_vscroll.setBounds(vr);
		_area.setBounds(
			0,0,
			ws.width - (vs ? FIXED_SIZE : 0), 
			ws.height- (hs ? FIXED_SIZE : 0));
		scrolled();
		_area.repaint();
//System.out.println("common.TScrolledArea.Update: EXIT");
	}
	
	public int GetY(Point p)
	{
		int n = _visi_h;
		if ((_center_method&CENTER_Y)==0 && area_y+n <= area_y2)
			n++;
//System.out.println("GetY: p.y="+p.y+" _visi_y="+_visi_y+" _visi_h="+_visi_h+" n="+n);

		if (p.x<_visi_x || p.y<_visi_y)
			return area_y1-1;
		int y;
		y=(p.y-_visi_y)/item_h;
		if (y>=n)
			 return area_y1-1;
		y+=area_y-area_y1;
		return y;
	}

	// should change `area_max+1' to `area_max-1' cause in most cases
	// this will be `-1'
	public int GetItem(Point p)
	{
		int x,y;
		
		if (p.x<_visi_x || p.y<_visi_y)
			return area_max+1;
		
		x=(p.x-_visi_x)/item_w;
		if (x>=_visi_w)
			return area_max+1;
		x+=area_x-area_x1;
		
		y=(p.y-_visi_y)/item_h;
		if (y>=_visi_h)
			 return area_max+1;
		y+=area_y-area_y1;


		int n = x+y*(area_x2-area_x1+1)+area_min;
		
		return n;
	}
}