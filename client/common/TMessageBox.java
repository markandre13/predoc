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

import Main;
import java.awt.*;
import java.util.Vector;
import common.TPushButton;

public class TMessageBox
	extends Dialog
{
	public final static int ACCEPT  = 1;
	public final static int ABORT		= 2;
	public final static int OK			= 4;
	public final static int RETRY		= 8;
	public final static int YES			= 16;
	public final static int NO			= 32;
	public final static int CANCEL	= 64;
	public final static int IGNORE	= 128;

	String _message;
	int _tx, _ty;				// text position
	int _iy;						// icon position
	Image _bitmap;			// bitmap
	int _type;
	int _height;				// font height;
	int _result;
	TMessageBox _this;

	final static int btn_width  = 100;    // width for all buttons
	final static int btn_height = Main.fmSystem.getAscent() + Main.fmSystem.getDescent() + 8;
	final static int btn_hspace = 8;      // gap between buttons
	final static int gap        = 16;     // all other gaps

	final static String label[] = {
		"Accept","Abort","Ok","Retry","Yes","No","Cancel","Ignore"
	};

	public TMessageBox(String title, String message, int type, Image bitmap)
	{
		super(Main.wnd, title, true);
		_this = this;
		_message = message;
		_bitmap = bitmap;
		_type = type;
		_result = 0;
		
		int icon_width, icon_height;
		if (_bitmap!=null) {
			icon_width  = _bitmap.getWidth(null);
			icon_height = _bitmap.getHeight(null);
		} else {
			icon_width  = 32;
			icon_height = 32;
		}
		
		int y;
		int msg_height;
		int msg_width=360+icon_width;	// width of the messagebox

		// space for icon & position for text;
		if ((_type & 0xF000)!=0 || _bitmap!=null) {
			_tx=icon_width+gap*2;		// text x position
		} else {
			_tx = gap;
		}
		
		// text size
		int txt_width = msg_width - _tx - gap;
		int txt_height = GetHeightOfTextFromWidth(_message, txt_width);
			
		if (txt_height<icon_height) {
			_ty = gap+icon_height/2-txt_height/2;
			_iy = gap;
			msg_height=icon_height;
		} else {
			_ty = gap;
			_iy = gap+txt_height/2-icon_height/2;
			msg_height=txt_height;
		}
		_ty += Main.fmSystem.getAscent();
		
		y = gap*3 + msg_height;
		msg_height = y + btn_height + gap;

		setSize(msg_width, msg_height);
		
		// create buttons
		//----------------
		
		// count buttons
		int btn = _type & 0xFF;
		int n = 0;
		for(int i=0; i<8; i++) {
			if( (btn&1)!=0 )
				n++;
			btn>>=1;
		}
		if (n==0) {
			_type|=OK;
			n++;
		}
		
		// create buttons
		Insets insets = getInsets();
		int w = n * btn_width + (n-1) * btn_hspace;
		int x = ((msg_width - w)>>1) + insets.left;
		y+=insets.top;
		btn = _type & 0xFF;
		int count = 0;
		for(int i=0; i<8; i++) {
			if ( (btn&1)!=0 ) {
				// create button
				TPushButton pb = new TPushButton(label[i], 1<<i) {
					void activated() {
						_result = GetID();
						_this.setVisible(false);
					}
				};
				add(pb);
				pb.setBounds(x,y,btn_width,btn_height);
				x=x+btn_width+btn_hspace;
				count+=0x0100;
				// if (count == (_type & 0x0F00) && LastChild())
				//	LastChild()->SetFocus();
			}
			btn>>=1;
		}

		// This one is a little bit dirty and depends on a special AWT behaviour:
		// the layout used by `java.awt.Dialog' is the BorderLayout and the last
		// added element will be moved in background, so the buttons stay visible,
		// and the elements bounds will be set to the whole dialog size
		add(new Canvas() {
			public void paint(Graphics pen) {
				pen.setFont(Main.fntSystem);
				int m = _split.size();
				int yp = _ty;
				for(int i=0; i<m; i++) {
					pen.drawString((String)_split.elementAt(i), _tx, yp);
					yp+=_height;
				}
				if (_bitmap!=null) {
					pen.drawImage(_bitmap, 16, _iy, null);
					return;
				}
			}
		});
	}
	
	Vector _split;
	
	/*
		0		start of line
		1		inside line & inside word
	*/
	int GetHeightOfTextFromWidth(String text, int txt_width)
	{
		_split = new Vector();
		_height = Main.fmSystem.getAscent() + Main.fmSystem.getDescent();
		_message+="\n";
		int n = _message.length();
		int l=0;			// left index
		int r;				// right index
		int or=-1;		// old right index
		int state = 0;
		for(int i=0; i<n; i++) {
			char c = _message.charAt(i);
			switch(state) {
				case 0:
					if (c==' ')
						l = i+1;
					else
						state = 1;
					break;
				case 1:
					if (c==' ' || c=='\n') {
						r=i;
						if (l<=r) {
							String sub = _message.substring(l,r);
							if (Main.fmSystem.stringWidth(sub)>txt_width || c=='\n') {
								if (or!=-1 && c!='\n') {
									r=or;
									i=or;
								}
								if (_message.charAt(l)=='\n') {
									_split.addElement(" ");
									l++;
								}
								_split.addElement(_message.substring(l,r));
								l = i+1;
								state = 0;
								or = -1;
							} else {
								or = r;
							}
						} else {
							_split.addElement("");
							or=-1;
						}
					}
					break;
			}
		}
		
		return _height*_split.size();
	}
	
	public int Result() {
		return _result;
	}
}
