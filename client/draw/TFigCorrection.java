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
	 Für das Greifen der Nummer sind ein paar elementare Änderungen nötig,
	die ich aber erst einmal provisorisch abhandle. Zumindest liegen mit
	dieser Funktionalität zwei Gründe vor, die gesamte Objektmanipulation
	von TToolSelektion nach TFigure zu verschieben.
	 (Oder machen wir's gleich richtig? Was dauert länger?)
*/

// this class is an horror to the eye, will fix this when it's complete

package draw;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import common.*;
import draw.*;
import Main;

/**
* This class is treated different than other figures:
* - it can't be removed from TDrawingData
* - it will receive keyboard events from TToolSelect
* - when Type() equals 0 TFigCorrection will be drawn in the background
*   otherwise in the foreground
*/

// The current form consits out of 4 parts (from top to bottom)
// - heading
// - number field (optional)
// - image
// - text field (optional)

public class TFigCorrection
	extends TFigure
{
	Image _img;						// image during editing
	private int _type;		// correction type

	public final static int SELECT_LINE = 1;
	public final static int SELECT_RECT = 2;
	public final static int SELECT_POLY = 3;
	int _selection_shape;	
	
	public Polygon _poly;	// polygon during editing

	public Point p1, p2;	// bounding rectangle of the image

	int real_x1, real_y1;	// the upper, left corner of the whole correction form
												// (doesn't equal p1 since i had to enlarge the form)

	// constant parameters for the layout computation
	//-----------------------------------------------------------------------
	final	int distance = 3;		// distance between the lines of the boxes
	final	int image_border=2;	// border size of the image
	final	int form_border=2;	// border size of the form
	final int enum_border=2;	// border size of enumeration field
	final int num_border=2;		// border size of number field in enum. field
	final int text_border=2;	// border size of the text field
	final int text_rows=10;

	// lots of data needed by `paint(Graphics)' which is calculated once when
	// `_types' is modified by `_ComputeLayout()'
	//-----------------------------------------------------------------------
	int form_w, form_h;
	int titl_x, titl_y, titl_w, titl_h, titl_a, titl_d;
	int enum_x, enum_y, enum_w, enum_h;
	int num_x, num_y, num_w, num_h, num_a, num_d;
	int img_x, img_y, img_w, img_h, img_cx;
	boolean has_text;
	boolean has_enum;
	int text_x, text_y, text_w, text_h, text_a, text_d;

	TFigText _fig_text;				// we use a text figure to display the text
	TToolDrawText _text_tool;

	int mflag; 								// for `translateHandle'

	// Constructor
	//-------------------------------------------------------------------------
	public TFigCorrection(Image img, Polygon poly, int x, int y)
	{
		p1=new Point(x,y);
		p2=new Point(p1.x + img.getWidth(null), p1.y + img.getHeight(null));
		_img		= img;
		_type		= 0;
		_poly		= poly;
		_fig_text = new TFigText("",0,0);
		_text_tool = new TToolDrawText(null, _fig_text);
		_ComputeLayout();
		enum_positions = new Vector();
	}

	public TFigCorrection()
	{
		p1=new Point();
		p2=new Point();
		_img = null;
		_type = 0;
		_poly = null;
		_fig_text = new TFigText("", 0,0);
		_text_tool = new TToolDrawText(null, _fig_text);
		_ComputeLayout();
		enum_positions = new Vector();
	}

	public void SetType(int type)
	{
		_type		= type;
		_ComputeLayout();
	}

	// correction type
	public int Type() {
		return _type;
	}

	public void SetImage(Image img)
	{
		_img = img;
		if (_img!=null) {
			p2.x = p1.x + img.getWidth(null);
			p2.y = p1.y + img.getHeight(null);
			_ComputeLayout();
		}
	}
	
	// called when the correction type was changed and parts of the layout
	// need to calculated
	//--------------------------------------------------------------------
	protected void _ComputeLayout()
	{
		if (_img==null)
			return;

		has_text = _ccf[_type].textfield;
		has_enum = _ccf[_type].enumeration;

		// heading
		//---------
		// loop to get maximum width of text in form heading
		int titl_mw = 0;
		for(int i=0; i<_ccf.length; i++) {
			int w = Main.fmSansBold.stringWidth(_ccf[i].name);
			if (w>titl_mw)
				titl_mw = w;
		}
		titl_mw+=distance*2;

		titl_x = distance + form_border;
		titl_y = distance + form_border;
		titl_a = Main.fmSansBold.getAscent();
		titl_d = Main.fmSansBold.getDescent();
		titl_h = titl_a + titl_d + 2*distance;

		// enumeration
		//-------------
		if (has_enum) {
			enum_x = distance + form_border;
			enum_y = titl_y + titl_h + distance;
			num_a = Main.fmSansBold.getAscent();
			num_d = Main.fmSansBold.getDescent();
			num_x = enum_x + enum_border + distance;
			num_y = enum_y + enum_border + distance;
			num_h = num_a + num_d + 2*num_border + 2*distance;
			num_w = Main.fmSansBold.stringWidth("88") + 2*num_border + 2*distance;
			enum_h = num_h + 2*enum_border + 2*distance;
		} else {
			enum_x = distance + form_border;
			enum_y = titl_y + titl_h;
			enum_h = 0;
		}

		// image
		//-------
		img_x  = distance + form_border;
		img_y  = enum_y + enum_h + distance;
		img_w  = _img.getWidth(null)+image_border*2;
		img_h  = _img.getHeight(null)+image_border*2;

		// width
		//-------
		if (img_w < titl_mw) {
			img_cx = (titl_mw-img_w)/2;
			titl_w = img_w = titl_mw;
		} else {
			img_cx = 0;
			titl_w = img_w;
		}
		enum_w = titl_w;

		form_w = titl_x + titl_w + distance + form_border;
		form_h = img_y+img_h+distance+form_border;

		if (has_text) {
			text_x = distance + form_border;
			text_y = img_y+img_h+distance;
			text_w = img_w;
			text_h = text_rows * (Main.fmType.getAscent()+Main.fmType.getDescent()) +
							 distance*2 + text_border*2;
			
			form_h += text_h + distance;
			
			_fig_text.p1.x = text_x+text_border+distance;
			_fig_text.p1.y = text_y+text_border+distance;

			_text_tool._x = text_x+text_border+distance;
			_text_tool._y = text_y+text_border+distance + Main.fmType.getAscent();
		}
	}	

	//----------
	public void paint(Graphics pen)
	{
		// paint might be called because p1 was modified
		real_x1 = p1.x-img_x-image_border-img_cx;
		real_y1 = p1.y-img_y-image_border;

		pen.translate(real_x1, real_y1);

		// draw form
		//------------------------------------------------------------------------
		// draw form frame
		pen.setColor(Color.black);
		pen.fillRect(0,0,form_w, form_h);
		pen.setColor(Color.lightGray);
		pen.fillRect(form_border,form_border,form_w-form_border*2, form_h-form_border*2);

		// draw title
		pen.setFont(Main.fntSansBold);
		pen.setColor(Color.black);
		int h = Main.fmSansBold.getDescent()+Main.fmSansBold.getAscent();
		pen.fillRect(titl_x, titl_y, titl_w, titl_h);
		pen.setColor(Color.white);
		pen.drawString(_ccf[_type].name, titl_x+distance, titl_y+titl_a+distance);
		pen.setColor(Color.black);

		// draw enumeration field
		if (has_enum) {
			pen.fillRect(enum_x, enum_y, enum_w, enum_h);
			pen.setColor(Color.lightGray);
			pen.fillRect(enum_x+enum_border, enum_y+enum_border, enum_w-(enum_border<<1), enum_h-(enum_border<<1));
			pen.setColor(Color.black);
			pen.fillRect(num_x, num_y, num_w, num_h);
			pen.setColor(Color.white);
			pen.fillRect(num_x+num_border, num_y+num_border, num_w-(num_border<<1), num_h-(num_border<<1));
			pen.setColor(Color.black);
			String a;
			a=""+(enum_positions.size()+1);
			if (enum_positions.size()<9)
				a="0"+a;
			pen.drawString(a, num_x+num_border+distance, num_y+num_border+distance+num_a);
			pen.drawString("drag a number", num_x+num_w+distance, num_y+num_border+distance+num_a);
		}

		// draw image		
		pen.fillRect(img_x, img_y, img_w, img_h);

		if (img_cx>0) {
			pen.setColor(Color.lightGray);
			pen.fillRect(img_x+image_border, img_y+image_border, img_w-image_border*2, img_h-image_border*2);
			pen.setColor(Color.black);
		}
		
		if (has_text) {
			pen.fillRect(text_x, text_y, text_w, text_h);
			pen.setColor(Color.white);
			pen.fillRect(text_x+text_border, text_y+text_border, text_w-text_border*2, text_h-text_border*2);
			pen.setColor(Color.red);
//			_fig_text.paint(pen);
			Graphics pen2 = pen.create();
			pen2.clipRect(text_x+text_border, text_y+text_border, text_w-text_border*2, text_h-text_border*2);
			_text_tool.paintFigure(pen2);
			pen2.dispose();
			pen.setColor(Color.black);
		}

		pen.drawImage(_img, img_x+image_border+img_cx, img_y+image_border, null);

		// draw selection
		//------------------------------------------------------------------------
		pen.translate(img_x+image_border+img_cx, img_y+image_border);
		
		pen.setColor(Color.red);

		if (_ccf[_type].strike) {
			_Strike(pen);
		}
		
		if (has_enum) {
			pen.setFont(Main.fntSansBold);
			for(int i=0; i<enum_positions.size(); i++) {
				Point p = (Point)enum_positions.elementAt(i);
				String a = ""+(i+1);
				pen.drawString(a,p.x-Main.fmSansBold.stringWidth(a),p.y-Main.fmSansBold.getDescent());
			}
		}

		switch(_ccf[_type].symbol) {
			case SYM_DELP:	// Zeichen: überflüssiger Absatz (aka Absatz zusammenziehen)
				{							//----------------------------------------------------------
					int h2;
					int x1 = _poly.xpoints[2];
					int y1 = (_poly.ypoints[0]-_poly.ypoints[1])/2+_poly.ypoints[1];
					int x2 = x1;
					int y2 = (_poly.ypoints[4]-_poly.ypoints[0])/2+_poly.ypoints[0];
					int x3 = _poly.xpoints[7];
					int y3 = y2;
					int x4 = x3;
					int y4 = (_poly.ypoints[5]-_poly.ypoints[4])/2+_poly.ypoints[4];
					
					h=y2-y1; h2=h/2;
					pen.drawArc(x1-h2,y1,h,h, 270, 180);
					pen.drawLine(x3,y3, x2,y2);
					h=y4-y3; h2=h/2;
					pen.drawArc(x3-h2,y3, h,h, 90, 180);
				}
				break;
			case SYM_BLCK: // Blockade
				{
					pen.drawPolygon(_poly);
					int x2 = img_w-image_border*2-distance;
					int x1 = x2-10;
					int y1 = _poly.ypoints[1];
					int y2 = _poly.ypoints[5];
					pen.drawLine(x1,y1,x2,y1);
					pen.drawLine(x1,y1,x2,y2);
					pen.drawLine(x1,y1,x1,y2);
					pen.drawLine(x1,y2,x2,y1);
					pen.drawLine(x1,y2,x2,y2);
					pen.drawLine(x2,y1,x2,y2);
				}
				break;
			case SYM_DELA: // Delatur
				{
					pen.drawPolygon(_poly);
					double[] x={0,25,67,78,84,174,247,336,134,133,129,164,113,90,42,9};
					double[] y={188,298,178,151,217,246,166,51,-139,165,257,455,492,505,513,419};
					Draw.T2Points sb = Draw.GetPolyBezierBounds(x,y);
					int x2 = img_w-image_border*2-distance;
					int x1 = x2-10;
					int y1 = _poly.ypoints[1];
					int y2 = _poly.ypoints[5];
					double sw = sb.x2-sb.x1;
					double sh = sb.y2-sb.y1;
					double mw = x2-x1;
					double mh = y2-y1;
					Draw.SetContext(pen);
					Draw.GSave();
					Draw.Translate(x1,y1);
//Draw.MoveTo(x[0]/mw*sw,y[0]/mw*sw);
					Draw.DrawBezier(x,y, mw/sw, mh/sh);
					Draw.GRestore();
				}
			default:
				pen.drawPolygon(_poly);
		}

		pen.translate(-p1.x, -p1.y);
		pen.setColor(Color.red);
	}

	// correction configuration
	//-------------------------------------------------------------------------
	static final int SYM_NONE = 1;
	static final int SYM_DELA = 2;		// Delatur
	static final int SYM_BLCK = 3;		// Blockade
	
	static final int SYM_ADDF = 4;		// Freiraum einfügen
	static final int SYM_INCF	= 5;		// Freiraum vergrößern
	static final int SYM_DECF = 6;		// Freiraum verkleinern
	static final int SYM_DELF = 7;		// Freiraum entfernen

	static final int SYM_ADDS = 8;		// fehlende Sperrung
	static final int SYM_DELS = 9;		// entferne Sperrung

	static final int SYM_ADDL = 10;		// fehlende Ligatur
	static final int SYM_DELL = 11;		// entferne Ligatur

	static final int SYM_DELP = 12;		// überflüssiger Absatz
	static final int SYM_ADDP = 13;   // fehlender Absatz

	static final int SYM_CIRC = 14;		// einkreisen
	static final int SYM_2PAL = 15;		// Waagerecht über- und unterstreichen
	static final int SYM_1LIN = 16;		// 1x unterstreichen
	
	static class TCCf {
		TCCf(String n) {
			symbol = SYM_NONE;
			enumeration = false;
			strike = false;
			textfield = false;
			foreground = true;
			name = n;
		}
		TCCf(int sym, boolean t, boolean e, boolean s, boolean f, String n) {
			symbol = sym;
			enumeration = e;
			strike = s;
			textfield = t;
			foreground = f;
			name = n;
		}
		int symbol;
		boolean textfield;
		boolean enumeration;
		boolean strike;
		boolean foreground;
		String name;
	};

	public static String Name(int i) { return _ccf[i].name; }
	public boolean Foreground() { return _ccf[_type].foreground; }

	static final TCCf[] _ccf = {
		new TCCf(SYM_NONE, false, false, false, false, "Freihand"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Falsche Buchstaben"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Falsche Wörter"),
		new TCCf(SYM_DELA, false, false, true,  true,  "Überflüssige Buchstaben"),
		new TCCf(SYM_DELA, false, false, true,  true,  "Überflüssige Wörter"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Fehlende Buchstaben"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Fehlende Satzzeichen"),
		new TCCf(SYM_DELA, false, false, true,  true,  "Überflüssige Satzzeichen"),
		new TCCf(SYM_1LIN, true,  false, true,  true,  "Beschädigte Buchstaben"),
		new TCCf(SYM_1LIN, true,  false, true,  true,  "Falsche Schrift (umgebende Schrift verwenden)"),
		new TCCf(SYM_CIRC, true,  false, true,  true,  "Verschmutzte und zu stark gedruckte Buchstaben"),
		new TCCf(SYM_NONE, true,  false, false, true,  "Neu zu setzende Zeilen"),
		new TCCf(SYM_ADDL, false, false, false, true,  "Fehlende Ligatur"),
		new TCCf(SYM_DELL, false, false, false, true,  "Überflüssige Ligatur"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Verstellte Buchstaben"),
		new TCCf(SYM_NONE, false, true,  false, true,  "Verstellte Wörter"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Verstellte Zahlen"),
		new TCCf(SYM_NONE, true,  false, false, true,  "Fehlende Wörter"),
		new TCCf(SYM_NONE, true,  false, true,  true,  "Falsche Trennung"),
		new TCCf(SYM_ADDF, false, false, false, true,  "Fehlender Freiraum"),
		new TCCf(SYM_INCF, false, false, false, true,  "Zu enger Freiraum"),
		new TCCf(SYM_DECF, false, false, false, true,  "Zu weiter Freiraum"),
		new TCCf(SYM_DELF, false, false, false, true,  "Überflüssiger Freiraum"),
		new TCCf(SYM_NONE, true,  false, false, true,  "Falsche Schrift (gewünschte Schrift angeben)"),
		
		// Randtext fest vorgegeben
		new TCCf(SYM_ADDS, false, false, false, true,  "Fehlende Sperrung"),
		new TCCf(SYM_DELS, false, false, false, true,  "Überflüssige Sperrung"),
		
		new TCCf(SYM_2PAL, false, false, false, true,  "Falsch Linie haltende Stelle"),
		new TCCf("Überflüssig mitgedruckte Stelle"),
		new TCCf("Fehlender Absatz"),
		new TCCf(SYM_DELP, false, false, false, true,  "Überflüssiger Absatz"),
		new TCCf("Überflüssiger oder zu großer Einzug"),
		new TCCf("Fehlender oder zu kleiner Einzug"),
		new TCCf(SYM_NONE, false, true,  false, true,  "Verstellte Zeilen"),
		new TCCf("Fehlender Durchschuß"),
		new TCCf("Zu großer Durchschuß"),
		
		// Erklärung muß zweimal rund eingeklammert werden
		new TCCf(SYM_NONE, true , false, false, true,  "Erklärung zur Korrektur"),
		new TCCf(SYM_BLCK, false, false, false, true,  "Unleserliche oder zweifelhafte Stelle"),
		new TCCf("Falsche Korrektur")
	};

	// code for enumeration
	//----------------------
	boolean enum_dragging;
	Vector  enum_positions;
	int enum_number;

	public boolean mousePressed(MouseEvent e)
	{
		enum_dragging = false;
		enum_number   = -1;
		if (!has_enum)
			return false;
	
		int x,y;

		// check drag field
		//------------------
		x = e.getX() - (p1.x-img_x-image_border-img_cx+num_x);
		y = e.getY() - (p1.y-img_y-image_border +      num_y);
		
		if (x>0 && x<num_w && y>0 && y<num_h) {
			System.out.println("got number");
			enum_dragging = true;
x = e.getX() - (p1.x-image_border-img_cx);
y = e.getY() - (p1.y-image_border);
enum_positions.addElement(new Point(x,y));
enum_number = enum_positions.size();
TDrawingArea.StaticInvalidate();
			TDrawingArea.SetCursor(Cursor.HAND_CURSOR);
			return true;
		}
		
		// check numbers
		//---------------
		x = e.getX() - (p1.x-image_border-img_cx);
		y = e.getY() - (p1.y-image_border);
		if (x>=0 && x<img_w && y>=0 && y<img_h) {
			int h,w;
			h = Main.fmSansBold.getAscent() + Main.fmSansBold.getDescent();
			w = Main.fmSansBold.stringWidth("88");
			for(int i=0; i<enum_positions.size(); i++) {
				Point p = (Point)enum_positions.elementAt(i);
				if (x>=p.x-w && x<=p.x && y>=p.y-h && y<=p.y) {
					System.out.println("found number "+(i+1));
					TDrawingArea.SetCursor(Cursor.HAND_CURSOR);
					enum_number = i+1;
					enum_dragging = true;
					return true;
				}
			}
		}	
		return false;
	}

	public boolean mouseDragged(MouseEvent e)
	{
		if (!enum_dragging)
			return false;
		if (enum_number!=-1) {
			Point p = (Point)enum_positions.elementAt(enum_number-1);
		int x = e.getX() - (p1.x-image_border-img_cx);
		int y = e.getY() - (p1.y-image_border);
			p.x = x;
			p.y = y;
			TDrawingArea.StaticInvalidate();
		}
		return true;
	}

	public boolean mouseReleased(MouseEvent e)
	{
		if (!enum_dragging)
			return false;
		TDrawingArea.SetCursor(Cursor.DEFAULT_CURSOR);
		enum_dragging = false;

		int x = e.getX() - (p1.x-image_border-img_cx);
		int y = e.getY() - (p1.y-image_border);

		if (enum_number==-1) {
			if (x>=0 && x<img_w && y>=0 && y<img_h) {
				System.out.println("drop at "+x+","+y);
				enum_positions.addElement(new Point(x,y));
				TDrawingArea.StaticInvalidate();
			}
		} else {
			if (x>=0 && x<img_w && y>=0 && y<img_h) {
				Point p = (Point)enum_positions.elementAt(enum_number-1);
				p.x = x;
				p.y = y;
			} else {
				enum_positions.removeElementAt(enum_number-1);
			}
			TDrawingArea.StaticInvalidate();
		}
		return true;
	}

	// This bunch of code simulates a fillPolygon with a pattern
	// but the AWT doesn't support this; clipping is limited to 
	// rectangular clipping regions also, without these limitations 
	// only three lines of code would appear here, oh and another
	// bug i spend wasting my time to remove the clipping region
	// how to do it:
	// (1) you must trace every translate statement in two additional
	//     integer variables
	// (2) translate back to the origin
	// (3) set a really big clipping region from (0,0) to (32000,32000)
	// (4) go back
	// why? A negative start point is invalid and a translate beyond
	// the original origin at (0,0) is invalid too and breaks all attempts
	// to remove the clipping region
	// the only way seems to duplicate the GC and to use the copy for
	// the clipping ops'
	//-------------------------------------------------------------------------
	void _Strike(Graphics pen)
	{
		Graphics pen2 = pen.create();
		Shape oclip = pen2.getClip();	// this is always a fuckin' null
		Draw.T2Points b = Draw.GetPolygonBounds(_poly);
		int d = Math.max(b.x2-b.x1, b.y2-b.y1);
		int s = 5;
		d-=(d%s);
		d+=(b.x1%s);
		d+=(b.y2%s);
		int y1, y2;
		pen2.drawPolygon(_poly);
		for(int i=0; i<3; i++) {
			switch(i) {
				case 0:
					y1 = Math.min(_poly.ypoints[0],_poly.ypoints[4]);
					pen2.clipRect(
						_poly.xpoints[1], 
						_poly.ypoints[1],
						_poly.xpoints[2]-_poly.xpoints[1], 
						y1-_poly.ypoints[2]);
					break;
				case 1:
					if (_poly.ypoints[0]<_poly.ypoints[4]) {
						pen2.clipRect(
							_poly.xpoints[7],
							_poly.ypoints[7],
							_poly.xpoints[3] - _poly.xpoints[7],
							_poly.ypoints[3] - _poly.ypoints[7]);
					} else {
						pen2.clipRect(
							_poly.xpoints[0],
							_poly.ypoints[4],
							_poly.xpoints[4] - _poly.xpoints[0],
							_poly.ypoints[0] - _poly.ypoints[4]);
					}
					break;
				case 2:
					y2 = Math.max(_poly.ypoints[7],_poly.ypoints[3]);
					pen2.clipRect(
						_poly.xpoints[7],
						y2,
						_poly.xpoints[5]-_poly.xpoints[6], 
						_poly.ypoints[6]-y2);
					break;
			};
			for(int j=b.x1-d; j<b.x2; j+=s) {
				pen2.drawLine(j,b.y2,j+d,b.y2-d);
			}
			pen2.setClip(oclip);
		}
	}

	// TFigure and marker modification stuff
	//-------------------------------------------------------------------------
	public void SetSelectionShape(int type)
	{
		_selection_shape = type;

		// there's some strange behaviour in Polygon.getBounds so we have to
		// calculate the bounding on our own: (Uuh, i currently don't know
		// what it was... so someone might check this piece of code again.)
		int min_x, max_x, min_y, max_y;
		min_x = max_x = _poly.xpoints[0];
		min_y = max_y = _poly.ypoints[0];
		for(int i=1; i<_poly.npoints; i++) {
			if (min_x > _poly.xpoints[i])
				min_x = _poly.xpoints[i];
			if (max_x < _poly.xpoints[i])
				max_x = _poly.xpoints[i];
			if (min_y > _poly.ypoints[i])
				min_y = _poly.ypoints[i];
			if (max_y < _poly.ypoints[i])
				max_y = _poly.ypoints[i];
		}

		switch(_selection_shape) {
			case SELECT_LINE:
				for(int i=0; i<_poly.npoints; i++) {
					_poly.xpoints[i] = min_x;
					if (i==7 || i<=2) {
						_poly.ypoints[i] = min_y;
					} else {
						_poly.ypoints[i] = max_y;
					}
				}
				break;
			case SELECT_RECT:
				_poly.xpoints[7] = _poly.xpoints[0] = _poly.xpoints[1] = min_x;
				_poly.ypoints[7] = _poly.ypoints[0] = _poly.ypoints[1] = min_y;
				_poly.xpoints[2] = max_x;
				_poly.ypoints[2] = min_y;
				_poly.xpoints[3] = _poly.xpoints[4] = _poly.xpoints[5] = max_x;
				_poly.ypoints[3] = _poly.ypoints[4] = _poly.ypoints[5] = max_y;
				_poly.xpoints[6] = min_x;
				_poly.ypoints[6] = max_y;
				break;
			case SELECT_POLY:
				break;
		}
	}
	
	public void paintHandles(Graphics pen)
	{
		pen.drawRect(real_x1-1, real_y1-1, form_w+1, form_h+1);
		pen.translate(p1.x, p1.y);
		for(int i=0; i<_poly.npoints; i++) {
			drawHandle(pen, _poly.xpoints[i], _poly.ypoints[i]);
		}
		if (has_text) {
			Graphics pen2 = pen.create();
			pen2.translate(-img_x-image_border, -img_y-image_border);
			pen2.clipRect(text_x+text_border, text_y+text_border, text_w-text_border*2, text_h-text_border*2);
			_text_tool.paintSelection(pen2);
			// pen.translate(img_x+image_border, img_y+image_border);
		}
		pen.translate(-p1.x, -p1.y);
	}
	
	public int getHandle(double x, double y)
	{
		x-=p1.x;
		y-=p1.y;
		for(int i=0; i<_poly.npoints; i++) {
			if (isInHandle(_poly.xpoints[i], _poly.ypoints[i], x,y)) {
				mflag = 0;
				return i;
			}
		}
		return -1;
	}

	public Point getHandlePosition(int h)
	{
		return new Point(_poly.xpoints[h]+p1.x, _poly.ypoints[h]+p1.y);
	}

	public void translateHandle(int handle, int nx, int ny)
	{
		if (nx<p1.x) nx = p1.x; else if (nx>p2.x) nx = p2.x;
		if (ny<p1.y) ny = p1.y; else if (ny>p2.y) ny = p2.y;
		nx-=p1.x; ny-=p1.y;

		switch(mflag) {
			case 0:
				int _nx = _poly.xpoints[handle];
				int _ny = _poly.ypoints[handle];
				if (_nx!=nx) {
					switch(handle) {
						case 0: case 1: case 6: case 7:
							TDrawingArea.SetCursor(Cursor.E_RESIZE_CURSOR);
							break;
						default:
							TDrawingArea.SetCursor(Cursor.W_RESIZE_CURSOR);
					}
					mflag = 2;	// resizing horizontal
				} else {
					switch(handle) {
						case 0: case 1: case 2: case 7:
							TDrawingArea.SetCursor(Cursor.S_RESIZE_CURSOR);
							break;
						default:
							TDrawingArea.SetCursor(Cursor.N_RESIZE_CURSOR);
					}
					mflag = 3;	// resizing vertical
				}
				break;
		}

		switch(mflag) {
			case 2:
				ny = _poly.ypoints[handle];
				break;
			case 3:
				nx = _poly.xpoints[handle];
				break;
		}

		switch(_selection_shape) {
			case SELECT_LINE:
				for(int i=0; i<_poly.npoints; i++) {
					if (mflag==2) {
						_poly.xpoints[i] = nx;
					} else {
						if (handle==7 || handle<=2) {
							if (i==7 || i<=2)
								_poly.ypoints[i] = ny;
						} else {
							if (i>=3 && i<=6)
								_poly.ypoints[i] = ny;
						}
					}
				}
				break;
			case SELECT_RECT:
				switch(handle) {
					case 7: case 0: case 1:
						_poly.xpoints[7] = nx;
						_poly.ypoints[7] = ny;
						_poly.xpoints[0] = nx;
						_poly.ypoints[0] = ny;
						_poly.xpoints[1] = nx;
						_poly.ypoints[1] = ny;
						if (mflag==2) {
							_poly.xpoints[6] = nx;
						} else {
							_poly.ypoints[2] = ny;
						}
						break;
					case 3: case 4: case 5:
						_poly.xpoints[3] = nx;
						_poly.ypoints[3] = ny;
						_poly.xpoints[4] = nx;
						_poly.ypoints[4] = ny;
						_poly.xpoints[5] = nx;
						_poly.ypoints[5] = ny;
						if (mflag==2) {
							_poly.xpoints[2] = nx;
						} else {
							_poly.ypoints[6] = ny;
						}
						break;
					case 2:
						_poly.xpoints[handle] = nx;
						_poly.ypoints[handle] = ny;
						if (mflag==2) {
							_poly.xpoints[3] = _poly.xpoints[4] = _poly.xpoints[5] = nx;
						} else {
							_poly.ypoints[7] = _poly.ypoints[0] = _poly.ypoints[1] = ny;
						}
						break;
					case 6:
						_poly.xpoints[handle] = nx;
						_poly.ypoints[handle] = ny;
						if (mflag==2) {
							_poly.xpoints[7] = _poly.xpoints[0] = _poly.xpoints[1] = nx;
						} else {
							_poly.ypoints[3] = _poly.ypoints[4] = _poly.ypoints[5] = ny;
						}
						break;
				}
				if (_poly.xpoints[6]>_poly.xpoints[2]) {
					int l = _poly.xpoints[2];
					int r = _poly.xpoints[6];
					_poly.xpoints[6] = _poly.xpoints[7] = _poly.xpoints[0] = _poly.xpoints[1] = l;
					_poly.xpoints[2] = _poly.xpoints[3] = _poly.xpoints[4] = _poly.xpoints[5] = r;
				}
				if (_poly.ypoints[7]>_poly.ypoints[3]) {
					int t = _poly.ypoints[3];
					int b = _poly.ypoints[7];
					_poly.ypoints[7] = _poly.ypoints[0] = _poly.ypoints[1] = _poly.ypoints[2] = t;
					_poly.ypoints[3] = _poly.ypoints[4] = _poly.ypoints[5] = _poly.ypoints[6] = b;
				}
				break;

			case SELECT_POLY:
				// i know one can make it with fewer code but i don't have the time
				// to do it; just imagine i'm an old IBM programmer getting paid
				// per ten lines of code ;)
				_poly.xpoints[handle] = nx;
				_poly.ypoints[handle] = ny;

				switch(handle) {
					case 0:
						_poly.xpoints[1] = nx;
						_poly.ypoints[7] = ny;
						if (nx>_poly.xpoints[2]) {
							_poly.xpoints[2]=nx;
							_poly.xpoints[3]=nx;
						}
						if (ny>_poly.ypoints[5]) {
							_poly.ypoints[5]=ny;
							_poly.ypoints[6]=ny;
						}
						if (ny<_poly.ypoints[1]) {
							_poly.ypoints[1]=ny;
							_poly.ypoints[2]=ny;
						}
						if (nx>_poly.xpoints[4] && ny>_poly.ypoints[4]) {
							switch(mflag) {
								case 3:
									_poly.ypoints[3]=ny;
									_poly.ypoints[4]=ny;
									break;
								case 2:
									_poly.xpoints[4]=nx;
									_poly.xpoints[5]=nx;
									break;
							}
						}
						break;
					case 1:
						_poly.xpoints[0] = nx;
						_poly.ypoints[2] = ny;
						if (nx>_poly.xpoints[2]) {
							_poly.xpoints[2] = nx;
							_poly.xpoints[3] = nx;
						}
						if (ny>_poly.ypoints[7]) {
							_poly.ypoints[7] = ny;
							_poly.ypoints[0] = ny;
						}
						switch(mflag) {
							case 2:
								if (_poly.ypoints[0]>_poly.ypoints[4] && nx>_poly.xpoints[4]) {
									_poly.xpoints[4]=nx;
									_poly.xpoints[5]=nx;
								}
								break;
							case 3:
								if (ny>_poly.ypoints[3]) {
									_poly.ypoints[3] = ny;
									_poly.ypoints[4] = ny;
								}
								if (ny>_poly.ypoints[6]) {
									 _poly.ypoints[6] = ny;
									 _poly.ypoints[7] = ny;
								}
								break;
						}
						break;
					case 2:
						_poly.xpoints[3] = nx;
						_poly.ypoints[1] = ny;
						if (ny>_poly.ypoints[7]) {
							_poly.ypoints[7] = ny;
							_poly.ypoints[0] = ny;
						}
						if (ny>_poly.ypoints[3]) {
							_poly.ypoints[3] = ny;
							_poly.ypoints[4] = ny;
						}
						if (ny>_poly.ypoints[5]) {
							_poly.ypoints[5] = ny;
							_poly.ypoints[6] = ny;
						}
						if (nx<_poly.xpoints[0]) {
							_poly.xpoints[0] = nx;
							_poly.xpoints[1] = nx;
						}
						if (nx<_poly.xpoints[6] && _poly.ypoints[3]>_poly.ypoints[7]) {
							_poly.xpoints[6] = nx;
							_poly.xpoints[7] = nx;
						}
						break;
					case 3:
						_poly.xpoints[2] = nx;
						_poly.ypoints[4] = ny;
						if (ny>_poly.ypoints[5]) {
							_poly.ypoints[5] = ny;
							_poly.ypoints[6] = ny;
						}
						if (nx<_poly.xpoints[0]) {
							_poly.xpoints[0] = nx;
							_poly.xpoints[1] = nx;
						}
						if (ny<_poly.ypoints[1]) {
							_poly.ypoints[1] = ny;
							_poly.ypoints[2] = ny;
						}
						if ( _poly.xpoints[3]<_poly.xpoints[6] && 
								 _poly.ypoints[3]>_poly.ypoints[7])
						{
							if (mflag==2) {
								_poly.xpoints[6] = nx;
								_poly.xpoints[7] = nx;
							} else {
								_poly.ypoints[7] = ny;
								_poly.ypoints[0] = ny;
							}
						}
						break;
					case 4:
						_poly.xpoints[5] = nx;
						_poly.ypoints[3] = ny;
						if (ny<_poly.ypoints[1]) {
							_poly.ypoints[1] = ny;
							_poly.ypoints[2] = ny;
						}
						if (ny>_poly.ypoints[5]) {
							_poly.ypoints[5] = ny;
							_poly.ypoints[6] = ny;
						}
						if (nx<_poly.xpoints[6]) {
							_poly.xpoints[6] = nx;
							_poly.xpoints[7] = nx;
						}
						if (_poly.xpoints[4]<_poly.xpoints[0] && 
								_poly.ypoints[4]<_poly.ypoints[0])
						{
							if (mflag==2) {
								_poly.xpoints[0] = nx;
								_poly.xpoints[1] = nx;
							} else {
								_poly.ypoints[7] = ny;
								_poly.ypoints[0] = ny;
							}
						}
						break;
					case 5:
						_poly.xpoints[4] = nx;
						_poly.ypoints[6] = ny;
						if (ny<_poly.ypoints[1]) {
							_poly.ypoints[1] = ny;
							_poly.ypoints[2] = ny;
						}
						if (ny<_poly.ypoints[3]) {
							_poly.ypoints[3] = ny;
							_poly.ypoints[4] = ny;
						}
						if (ny<_poly.ypoints[7]) {
							_poly.ypoints[7] = ny;
							_poly.ypoints[0] = ny;
						}
						if (nx<_poly.xpoints[6]) {
							_poly.xpoints[6] = nx;
							_poly.xpoints[7] = nx;
						}
						if (_poly.xpoints[5]<_poly.xpoints[0] &&
								_poly.ypoints[4]<_poly.ypoints[0])
						{
							 _poly.xpoints[0] = nx;
							 _poly.xpoints[1] = nx;
						}
						break;
					case 6:
						_poly.xpoints[7] = nx;
						_poly.ypoints[5] = ny;
						if (nx>_poly.xpoints[4]) {
							_poly.xpoints[4]=nx;
							_poly.xpoints[5]=nx;
						}
						if (ny<_poly.ypoints[7]) {
							_poly.ypoints[7] = ny;
							_poly.ypoints[0] = ny;
						}
						if (ny<_poly.ypoints[3]) {
							_poly.ypoints[3] = ny;
							_poly.ypoints[4] = ny;
						}
						if (ny<_poly.ypoints[1]) {
							_poly.ypoints[1] = ny;
							_poly.ypoints[2] = ny;
						}
						if (_poly.xpoints[6]>_poly.xpoints[2] && 
								_poly.ypoints[7]<_poly.ypoints[3]) 
						{
							_poly.xpoints[2]=nx;
							_poly.xpoints[3]=nx;
						}
						break;
					case 7:
						_poly.xpoints[6] = nx;
						_poly.ypoints[0] = ny;
						if (ny<_poly.ypoints[1]) {
							_poly.ypoints[1] = ny;
							_poly.ypoints[2] = ny;
						}
						if (ny>_poly.ypoints[5]) {
							_poly.ypoints[5] = ny;
							_poly.ypoints[6] = ny;
						}
						if (nx>_poly.xpoints[4]) {
							_poly.xpoints[4]=nx;
							_poly.xpoints[5]=nx;
						}
						if (_poly.xpoints[7]>_poly.xpoints[3] &&
								_poly.ypoints[7]<_poly.ypoints[3] )
						{
							if (mflag==2) {
								_poly.xpoints[2]=nx;
								_poly.xpoints[3]=nx;
							} else {
								_poly.ypoints[3] = ny;
								_poly.ypoints[4] = ny;
							}
						}
					break;
			}
		}
	}
	
	public double Distance(int x, int y)
	{
    if (real_x1+titl_x<=x && x<=real_x1+titl_x+titl_w && 
        real_y1+titl_y<=y && y<=real_y1+titl_y+titl_h)
			return RANGE; 
		if (real_x1<=x && x<=real_x1+form_w && 
				real_y1<=y && y<=real_y1+form_h) 
			return RANGE_DONT_MOVE;
		return OUT_OF_RANGE;
	}
	
	public Point getRefPoint(int x, int y)
	{
		 return new Point(x-p1.x, y-p1.y);
	}
	
	public void getBounding(Point q1, Point q2)
	{
		q1.x = real_x1; 					q1.y = real_y1;
		q2.x = real_x1+form_w; ;	q2.y = real_y1+form_h;
	}
	
	public void translate(Point ref, int x, int y)
	{
		int dx,dy;
		dx = p2.x-p1.x;
		dy = p2.y-p1.y;
		p1.x = x-ref.x;
		p1.y = y-ref.y;
		p2.x = p1.x+dx;
		p2.y = p1.y+dy;
	}

	public void keyPressed(KeyEvent e)
	{
		_text_tool.keyPressed(e);
	}

	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		if (version>=2) {
			out.writeInt(p1.x);
			out.writeInt(p1.y);
			out.writeInt(p2.x);
			out.writeInt(p2.y);
			out.writeInt(_type);
		}
		if (version>=3) {
			out.writeInt(_text_tool._text.length());
			out.writeBytes(_text_tool._text);
		}
		if (version>=4) {
			out.writeInt(enum_positions.size());
			for(int i=0; i<enum_positions.size(); i++) {
				Point p = (Point)enum_positions.elementAt(i);
				out.writeInt(p.x);
				out.writeInt(p.y);
			}
		}
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		if (version>=2) {
			p1 = new Point(in.readInt(), in.readInt());
			p2 = new Point(in.readInt(), in.readInt());
			_type = in.readInt();
			_ComputeLayout();
		}
		if (version>=3) {
			int n = in.readInt();
			byte buffer[] = new byte[n];
			in.readFully(buffer);
			_text_tool._text = new String(buffer);
			_text_tool._cx   = 0;
		}
		if (version>=4) {
			int n = in.readInt();
			enum_positions = new Vector();
			for(int i=0; i<n; i++) {
				enum_positions.addElement(new Point(in.readInt(), in.readInt()));
			}
		}
	}
}
