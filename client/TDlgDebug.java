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
import java.awt.event.*;
import java.util.*;
import java.io.*;

import common.*;

class TDlgDebug
extends Dialog
{
  boolean _init;

/*
  Scrollbar _contrast;
*/

  public TDlgDebug()
  {
    super(Main.wnd, "Debug Options");
    setModal( false );
    setResizable( false );
    TFormLayout form = new TFormLayout(this);
    setSize(280,350);

		Checkbox br = new Checkbox("Paper Content Bounding", 
															 Main.debug_bounding_rectangle);
		br.setSize(80,25);
		br.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_bounding_rectangle = e.getStateChange()==ItemEvent.SELECTED;
			}
		});

		Checkbox lr = new Checkbox("Letter Bounding", 
															 Main.debug_bounding_letter);
		lr.setSize(80,25);
		lr.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_bounding_letter = e.getStateChange()==ItemEvent.SELECTED;
			}
		});

		Checkbox c3,c4,c5,c6,c7,c8;
		
		c3 = new Checkbox("Selector", Main.debug_selector);
		c3.setSize(80,25);
		c3.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_selector = e.getStateChange()==ItemEvent.SELECTED;
			}
		});

		c4 = new Checkbox("Memory", Main.debug_memory);
		c4.setSize(80,25);
		c4.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_memory = e.getStateChange()==ItemEvent.SELECTED;
			}
		});
		
		c5 = new Checkbox("Data Flow", Main.debug_mvc);
		c5.setSize(80,25);
		c5.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_mvc = e.getStateChange()==ItemEvent.SELECTED;
			}
		});
		
		c6 = new Checkbox("Image Loader", Main.debug_loader);
		c6.setSize(80,25);
		c6.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_loader = e.getStateChange()==ItemEvent.SELECTED;
			}
		});

		c7 = new Checkbox("Extra Memory Dump", Main.debug_memory_force);
		c7.setSize(80,25);
		c7.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.debug_memory_force = e.getStateChange()==ItemEvent.SELECTED;
			}
		});

		Choice ch = new Choice();
		ch.addItem("Compressed PNG Bitmaps");
		ch.addItem("Uncompressed Gray Bitmap");
		ch.setSize(80,25);
		ch.select(Main.bitmap_format);
		ch.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Main.bitmap_format = ((Choice)e.getItemSelectable()).getSelectedIndex();
			}
		});
		
		form.Attach(br, TFormLayout.TOP | TFormLayout.LEFT | TFormLayout.RIGHT);

		form.Attach(lr, TFormLayout.TOP, br);
    form.Attach(lr, TFormLayout.LEFT | TFormLayout.RIGHT);

		form.Attach(c3, TFormLayout.TOP, lr);
    form.Attach(c3, TFormLayout.LEFT | TFormLayout.RIGHT);
		form.Attach(c4, TFormLayout.TOP, c3);
    form.Attach(c4, TFormLayout.LEFT | TFormLayout.RIGHT);
		form.Attach(c5, TFormLayout.TOP, c4);
    form.Attach(c5, TFormLayout.LEFT | TFormLayout.RIGHT);
		form.Attach(c6, TFormLayout.TOP, c5);
    form.Attach(c6, TFormLayout.LEFT | TFormLayout.RIGHT);
		form.Attach(c7, TFormLayout.TOP, c6);
    form.Attach(c7, TFormLayout.LEFT | TFormLayout.RIGHT);
		form.Attach(ch, TFormLayout.TOP, c7);
    form.Attach(ch, TFormLayout.LEFT | TFormLayout.RIGHT);

/*
    _contrast = new Scrollbar(Scrollbar.HORIZONTAL, 0,1,0,255);
    //    _contrast.setBounds(5+in.top,32+in.left,280-5-5,15);
    add(_contrast);
*/
    _init = false;
  }

  public void paint(Graphics pen)
  {
/*
    Insets in = getInsets();
    pen.translate(in.left,in.top);
    if (!_init) {
      _init = true;
      setSize(255+10+in.left+in.right, 350+in.top+in.bottom);
      _contrast.setBounds(5+in.left,32+in.top,256,15);
    }
    pen.drawString("contrast", 5,16);
    for(int i=0; i<=255; i++) {
      pen.setColor(new Color(i,i,i));
      pen.drawLine(i+5,32-8,i+5,32);
    }
*/
  }
}
