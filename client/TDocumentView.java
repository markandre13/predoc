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

import common.*;

class TDocumentView
{
  TDDocViewState _state;

  public TDocumentView(Container parent)
  {
    _state = new TDDocViewState();

    TFormLayout form = new TFormLayout(parent);

		// status bar
		//------------
		TStatusBar statusbar = new TStatusBar(_state);
	    statusbar.setSize(0,22);

		// tool bar
		//----------
		TRadioState select_state = new TRadioState() {
			public void changed() {
				if (Current()!=null) {
					_state.SetSelectMode(Current().Value());
				}
			}
		};

		int dist = 2;

		TPushButton btn;
		Component top = null;
		Component last, crnt = null;
		for (int i=0; i<40; i++) {
			last = crnt;
			crnt = null;
			switch(i) {
				case 0:
			    crnt = btn = new TPushButton(Main.image[Main.IMG_PAGE_PREV]);
					btn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							_state.PrevPage();
						}
					});
					form.Distance(crnt,dist, TFormLayout.LEFT);
					break;
				case 1:
					crnt = btn = new TPushButton(Main.image[Main.IMG_PAGE_NEXT]);
					btn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							_state.NextPage();
						}
					});
					break;
				case 2:
			    crnt = btn = new TPushButton(Main.image[Main.IMG_ZOOM_IN]);
					btn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int z = _state.Zoom()+25;
							if (z<=200)
								_state.SetZoom(z);
						}
					});
					form.Distance(crnt,dist, TFormLayout.LEFT);
					break;
				case 3:
			    crnt = btn = new TPushButton(Main.image[Main.IMG_ZOOM_OUT]);
					btn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int z = _state.Zoom()-25;
							if (z>=25)
								_state.SetZoom(z);
						}
					});
					break;
				case 4:
					crnt = new TFatRadioButton(select_state, 
																	 Main.image[Main.IMG_MARK_POLY], 
																	 TDDocViewState.SELECT_POLYGON);
					form.Distance(crnt,dist+3, TFormLayout.LEFT);
					select_state.SetCurrent((TFatRadioButton)crnt);
					break;
				case 5:
					crnt = new TFatRadioButton(select_state, 
																	 Main.image[Main.IMG_MARK_RECT],
																	 TDDocViewState.SELECT_RECTANGLE);
					break;
			}
			if (crnt!=null) {
				top = crnt;
				crnt.setSize(30,30);
				form.Attach(crnt, TFormLayout.TOP);
				if (last==null) {
					form.Attach(crnt, TFormLayout.LEFT);
				} else {
					form.Attach(crnt, TFormLayout.LEFT, last);
				}
				form.Distance(crnt,dist, TFormLayout.TOP|TFormLayout.BOTTOM);
			}
		}

		// document view
		//---------------
    TDocViewPort viewer = new TDocViewPort(_state);

		form.Attach(viewer, TFormLayout.TOP, top);
		form.Attach(viewer, TFormLayout.LEFT | TFormLayout.RIGHT);
		form.Attach(viewer, TFormLayout.BOTTOM, statusbar);
    form.Attach(statusbar,TFormLayout.BOTTOM | TFormLayout.LEFT | TFormLayout.RIGHT );
  }
}
