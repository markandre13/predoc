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

class TScrolledArea
	extends Container
	implements AdjustmentListener
{
	Scrollbar _vscroll, _hscroll;
	TFormLayout _form;
	Component _view;

	final static int DEFAULT_SIZE = 13;

	public int area_x, area_y;												// visible upper left corner in area
	public int area_x1, area_x2, area_y1, area_y2;		// size of area
	public int item_w, item_h;

	public TScrolledArea()
	{
		_form = new TFormLayout(this);

		area_x = 0; area_y = 0;
		area_x1 = area_x2 = area_y1 = area_y2 = 0;
		item_w = item_h = 1;
	
		_hscroll = new Scrollbar(Scrollbar.HORIZONTAL);
			_hscroll.setSize(DEFAULT_SIZE,DEFAULT_SIZE);
			_hscroll.addAdjustmentListener(this);
		_vscroll = new Scrollbar(Scrollbar.VERTICAL);
			_vscroll.setSize(DEFAULT_SIZE,DEFAULT_SIZE);
			_vscroll.addAdjustmentListener(this);

		_form.Attach(_vscroll, TFormLayout.TOP | TFormLayout.RIGHT);
		_form.Attach(_vscroll, TFormLayout.BOTTOM, _hscroll);
		_form.Attach(_hscroll, TFormLayout.LEFT | TFormLayout.BOTTOM);
		_form.Attach(_hscroll, TFormLayout.RIGHT, _vscroll);
	}
	
	public void AddView(Component view)
	{
		_view = view;
		AdjustSB();
		_form.Attach(view    , TFormLayout.TOP | TFormLayout.LEFT);
		_form.Attach(view		 , TFormLayout.BOTTOM, _hscroll);
		_form.Attach(view		 , TFormLayout.RIGHT, _vscroll);
		_view.addComponentListener(
			new ComponentAdapter() {
				public void componentResized(ComponentEvent e)
				{
					AdjustSB();
				}
			}
		);
	}
	
	public void AdjustSB()
	{
		Dimension d = _view.getSize();
		if (area_x+d.width>area_x2)
			area_x = area_x2-d.width;
		if (area_x < area_x1)
			area_x = area_x1;
		if (area_y+d.height>area_y2)
			area_y = area_y2-d.height;
		if (area_y < area_y1)
			area_y = area_y1;
// System.out.println("hscroll: pos="+area_x+" vis="+d.width+" rgn="+area_x1+","+area_x2);
		_hscroll.setValues(area_x, d.width,  area_x1,area_x2);
		_vscroll.setValues(area_y, d.height, area_y1,area_y2);
	}

	public void scrolled()
	{
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		area_x = _hscroll.getValue();
		area_y = _vscroll.getValue();
//System.out.println("Area: position = "+area_x+","+area_y);
		scrolled();
	}
}
