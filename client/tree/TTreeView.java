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

package tree;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import Main;

import common.*;

public class TTreeView
	extends TScrolledArea
{
	TTreeNode _top;
	protected TTreeNode _selection;
	TTreeView _this;
	Vector _flat;

	public TTreeView(TTreeNode tn)
	{
		draw_frame = true;
		_top = tn;
		_this = this;
		SetDoubleBuffer(true);
		SetPaintTo(PER_ROW);
		SetCenterTo(CENTER_NONE);
		item_h = 17;
		item_w = 1;
		area_x1 = area_y1 = 0;
		area_x2 = 300;
		_flat = new Vector();
		_top.Flatten(_flat);
		_selection=null;
		area_y2 = _flat.size()-1;

		Area().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int y = _this.GetY(e.getPoint());
				if (y>=0)
					_this._ClickedOn(y, e.getClickCount());
			}
		});
	}

	public void Select(TTreeNode node)
	{
		if (node.IsSelectable()) {
			if (_selection!=node) {
				_selection = node;
			}
		}
	}

	private void _ClickedOn(int y, int clickCount)
	{
		TTreeNode node = ((TTreeNode)_flat.elementAt(y));

		if (node.IsSelectable()) {
			if (_selection!=node) {
				_selection = node;
				_selection.selected();
				Update();
				if (!node.IsClosed())
					return;
			}
			if (clickCount==2)
				doubleClick();
		}
		
		node.SetClosed(!node.IsClosed());
		_flat.removeAllElements();
		_top.Flatten(_flat);
		area_y2 = _flat.size()-1;
		
		Update();
	}
	
	public void paintItem(Graphics pen, int x, int y)
	{
		pen.setFont(Main.fntSystem);
		int w1 = 8;
		int w2 = w1<<1;
		int d1 = 4;
		int d2 = d1<<1;
		int s1 = d1+2;
		int s2 = s1<<1;
if (y>=_flat.size()) { System.out.println("out of range"); return; }

		TTreeNode node = (TTreeNode)_flat.elementAt(y);

		if (node==_selection) {
			pen.setColor(new Color(255,255,191));
			pen.fillRect(0,0, area_x2-area_x1+1, item_h);
		}

		int d;
		d=node._depth-1;

		// draw node symbol
		//-----------------------------------------------------------------
		pen.setColor(Color.white);
		pen.fillRect(d*w2+d1,d1,w2-d2,item_h-1-d2);
		pen.setColor(Color.black);
		pen.drawRect(d*w2+d1,d1,w2-d2,item_h-1-d2);
		
		// draw `+' or `-' when the node has children
		//-----------------------------------------------------------------
		if (node.HasChildren()) {
			pen.drawLine(d*w2+s1,item_h>>1,(d+1)*w2-s1,item_h>>1);
			if (node.IsClosed())
				pen.drawLine(d*w2+w1,s1, d*w2+w1, item_h-1-s1);
		}

		// draw lines
		//-----------------------------------------------------------------
		d=node._depth-2;

		pen.drawLine(d*w2+w1,0,d*w2+w1,item_h>>1);

		d=node._depth-1;
		for(int i=0; i<node._depth-1; i++) {
			for(int j=y+1; j<_flat.size(); j++) {
				TTreeNode tn = (TTreeNode)_flat.elementAt(j);
				if (tn._depth<=i+1)
					break;
				if (i+2==tn._depth) {
					if (i<node._depth-2) {
						pen.drawLine(i*w2+w1,0,i*w2+w1,item_h);
					}	else {
						pen.drawLine(i*w2+w1,0,i*w2+w1,item_h);
					}
					break;
				}
			}
		}

		if(y!=_flat.size()-1 && ((TTreeNode)_flat.elementAt(y+1))._depth>node._depth)
			pen.drawLine(d*w2+w1,item_h-1-d1, d*w2+w1, item_h);
		if(node._depth>1)
			pen.drawLine(d*w2-w1, item_h>>1, d*w2+d1, item_h>>1);

		// paint contents
		int tx = node._depth*w2;
		pen.translate(tx,0);
		node.paint(pen);
		pen.translate(-tx,0);
	}

	public void doubleClick()
	{
	}
	
	public TTreeNode Selection()
	{
		return _selection;
	}
	
	public void resize()
	{
		// 350 is a stupid random value, should compute the value instead
		// but currently there are other major things to solve...
		area_x2 = Math.max(350, getSize().width-16);
	}
}
