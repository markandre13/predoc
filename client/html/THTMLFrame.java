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

package html;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Vector;
import java.util.Stack;
import common.*;

public class THTMLFrame
	extends Frame
{
/*
	public static void main(String args[])
	{
		html w = new html();
		w.setSize(418+10,400);
		w.show();
	}
*/
	THTMLView hv;

	public THTMLFrame()
	{
		hv = new THTMLView();
		TFormLayout form = new TFormLayout(this);
		form.Attach(hv, TFormLayout.ALL);

    addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
	}
	
	public void ReadFile(String f)
	{
		hv.ReadFile(f);
	}
}

	class TState
	{
		int x;				// current x-Position
		int y;				// current y baseline position
		int ascent;		// ascent of current line
		int descent;	// descent of current line
		int left;			// left border of current line
		int right;		// right border of current line

		// font stuff
		//------------------------------------------------------
		Font font;
		FontMetrics fm;
		
		Font font_normal;
		Font font_h1, font_h2, font_h3;
		
		boolean font_changed;
		
		TState()
		{
			font_normal = new Font("Serif", Font.PLAIN, 12);
			font_h1     = new Font("SansSerif", Font.BOLD, 16);
			font_h2     = new Font("SansSerif", Font.BOLD, 14);
			font_h3     = new Font("SansSerif", Font.BOLD, 12);
			SetFont(font_normal);
			SetColor(0,0,0);
		}
		
		void SetFont(Font f) {
			font = f;
			fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			font_changed = true;
		}

		int GetSpaceWidth()	{
			return fm.charWidth(' ');
		}

		// color stuff
		//------------------------------------------------------
		Color color;
		boolean color_changed;
		void SetColor(int r,int g, int b) {
			color = new Color(r,g,b);
			color_changed = true;
		}
		
		// parser stuff
		//------------------------------------------------------
		Vector parsed;
		int current_idx;
		int end_idx;
	}

	// this `TState' is only used during `Init()' and has some additional
	// attributes
	//-------------------------------------------------------------------
	class TStateInit 
		extends TState
	{
		int element_ascent;
		int element_descent;
		int par_top;
		int par_bottom;

		void Reset() {
			element_ascent = 0;
			element_descent = 0;
			par_top = 0;
			par_bottom = 0;
		}

		int GetWidth(String s) {
			return fm.stringWidth(s);
		}
		int Ascent() {
			return fm.getAscent();
		}
		int Descent() {
			return fm.getDescent();
		}
	}

	class TLine {
		int x, y;				// start position of the line (y is the baseline!)
		int eol,bol;		// index range in `parsed' vector
		int ascent, descent;
	}
	
	class TInit {					// Stage 1			Stage 2				Stage 3			paint
		int start_idx;			// in						in						-						-
		int end_idx;				// in						in						-						-
		int current_idx;		// -						-							-						-
		int min_width;			// out					-							-						-
		int weight;					// out					-							-						-
		int y;							// -						in (top)			-						in (translate)
												//              out (bottom)	-
		int left;						// in						in						-						-
		int right;					// in						in						-						-
		Vector parsed;			// in						in						in					in
		Vector lines;				// -						out						-						in
		Vector anchors;			// -						-							out					-
	}
	
	class TAnchor {
		Polygon polygon;
		String href;
	}


class THTMLView
	extends TScrolledArea
{
	Vector parsed;
	Vector lines;
	Vector anchors;

	TAnchor inside;
	static THTMLView _this;
	
	String currentpath;

	public THTMLView()
	{
		_this = this;
		SetDoubleBuffer(true);
		SetPaintTo(ALL_AT_ONCE);
		SetCenterTo(CENTER_NONE);
		currentpath = "";
		item_h = 1;
		item_w = 1;
		area_x1 = area_y1 = 0;
		area_x2 = 800;
		area_y2 = 800;
		lines = new Vector();
		anchors = new Vector();
		parsed = new Vector();
		inside = null;

		Area().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (inside!=null) {
					System.out.println("go to: "+inside.href);
					ReadFileRelative(inside.href);
				}
			}
		});
		
		Area().setBackground(new Color(255,208,128));

		Area().addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				p.translate(-_visi_x+area_x, -_visi_y+area_y);
				int n = anchors.size();
				for(int i=0; i<n; i++) {
					TAnchor a = (TAnchor)anchors.elementAt(i);
					if (a.polygon.contains(p)) {
						if (inside!=a) {
							Area().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							inside = a;
						}
						return;
					}
				}
				if (inside != null) {
					Area().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					inside = null;
				}
			}
		});
	}

	public void ReadFileRelative(String filename)
	{
		ReadFile(currentpath+filename);
	}
	
	public void ReadFile(String filename)
	{
		currentpath = filename.substring(0,filename.lastIndexOf('/')+1);
System.out.println("Loading "+filename);
		String content = new String();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line;
			while(true) {
				line= in.readLine();
				if (line==null)
					break;
				content+=line;
			}
		}
		catch(Exception e) { }
		SetValue(content);
	}
	
	public void SetValue(String value)
	{
		parsed.removeAllElements();
		lines.removeAllElements();
		anchors.removeAllElements();
		THTMLParser hp = new THTMLParser(value, parsed);
		Init();
		Area().repaint();
	}


	public void resize()
	{
		Update();
		Init();
	}

	public void Init()
	{
		TInit init = new TInit();
		init.start_idx 	= 0;
		init.end_idx		= parsed.size();
		init.lines			= lines;
		init.parsed			= parsed;
		init.anchors		= anchors;
		init.y					= 8;
		init.left				= 8;
		init.right			= Area().getSize().width - 8;
		Init(init,1);
System.out.println("min_width:"+init.min_width);
System.out.println("weight   :"+init.weight);
		Init(init,2);

		// should put the anchors stuff here or in a own method instead of
		// doing it in Init(...)
	}
	
	static public void Init(TInit init, int stage)
	{
		TStateInit state = new TStateInit();
		state.y 			= init.y;
		state.ascent 	= 8;
		state.descent	= 8;
		state.left		= init.left;
		state.right		= init.right;
		state.x				= state.left;
		state.parsed	= init.parsed;
		state.end_idx = init.end_idx;

		// stage 1
		int width_till_newline = 0;

		// stage 2
		int max_ascent = 0, max_descent = 0;	// line content
		int max_top    = 0, max_bottom  = 0;  // extra for paragraph
		
		TLine line = new TLine();
		line.x   = state.x;
		line.bol = line.eol = init.start_idx;
		if (stage==1) {
			init.min_width=0;
			init.weight=0;
		}
		if (stage==2) {
			init.lines.removeAllElements();
			init.lines.addElement(line);
		}
//String txtline = "";
		for(int i=init.start_idx; i<init.end_idx; i++) {
			TElement e = (TElement)init.parsed.elementAt(i);
			state.Reset();
			if (stage==1 || stage==2) {
				state.current_idx = i;
				e.init(state);	// width needed in stage 1&2, height in stage 2
			}
			state.font_changed = false;
			state.color_changed = false;
			for(int k=0; k<2; k++) {
				boolean newline = false;
				switch(k) {
					case 0:
						newline = e.needNewLine(state);
						if (stage==1) {
							if (newline) { 
								if (init.weight<width_till_newline)
									init.weight = width_till_newline;
								width_till_newline = 0;
							}
						}
						if (state.x+e.width > state.right)
							newline=true;
						break;
					case 1:
//if (e instanceof TEWord) txtline+=((TEWord)e).word+" ";
						if (stage==2) {
							if (max_ascent<state.element_ascent)
								max_ascent = state.element_ascent;
							if (max_descent<state.element_descent)
								max_descent = state.element_descent;
							if (max_top<state.par_top)
								max_top = state.par_top;
							if (max_bottom<state.par_bottom)
								max_bottom = state.par_bottom;
						}
						
						state.current_idx = i;
						e.afterPaint(state);
						state.x += e.width;
						i = state.current_idx;
						line.eol=i+1;

						if (stage==1) {
							if (init.min_width < e.width)
								init.min_width=e.width;
							width_till_newline += e.width;
							if (e.space)
								width_till_newline += state.GetSpaceWidth();
						}
						if (state.x!=state.left && e.space)
							state.x += state.GetSpaceWidth();
						if (state.x >= state.right)
							newline = true;
						break;
				}
				if (newline) {
//System.out.println("line "+init.lines.size()+":"+txtline); txtline="";
					// store values of current line
					//-----------------------------
					if (stage==2) {
						state.y += max_ascent + max_top;
						line.y = state.y;
						line.ascent = max_ascent;
						line.descent = max_descent;
					}
					int end_of_last = line.eol;
					// move to the next line
					//----------------------
					state.x = state.left;
					if (stage==2) {
//System.out.println("line from "+line.bol+" to "+line.eol);
						state.y += max_descent + max_bottom;
						line = new TLine();
						init.lines.addElement(line);
					}
					// prepare values for next line
					//-----------------------------
					if (stage==2) {
						max_ascent = 0;
						max_descent = 0;
						max_top = 0;
						max_bottom = 0;
					}
					line.x = state.x;
					line.bol = line.eol = end_of_last;
				}
			}
		}
		if (init.weight<width_till_newline)
			init.weight = width_till_newline;

		if (stage!=2) {
			return;
		}

		state.y += max_ascent /*+ max_descent */;
		line.y = state.y;
		line.eol = init.end_idx;
		//System.out.println("currently there are "+init.lines.size()+" lines");

		int top_y = init.y;
		init.y = state.y;			// return value for stage 2

if (init.anchors==null)
	return;

		// find hyperlink areas
		//-------------------------------------------------------------------------
		// currently i'm using a Polygon but this will become a Region in the
		// future when problems appear with tables and the like

		init.anchors.removeAllElements();

		state.y 			= top_y;
		state.ascent 	= 8;
		state.descent	= 8;
		state.left		= init.left;
		state.right		= init.right;
		state.x				= state.left;
		state.parsed	= init.parsed;
		state.end_idx = init.end_idx;
	
		int n = init.lines.size();
		
		TAnchor current_anchor = null;

		int x1,x2,x3,x4, y1,y2,y3,y4;

		/*
			The meaning of x1..x4, y1..y4:
		
			y1		        +---------------------------------+
										|                                 |
			y2			+-----+                                 |
							|                                       |
			y3			|                      +----------------+
							|                      |
			y4			+----------------------+
				      x1    x2               x3               x4
		*/

		x1=x2=x3=x4=y1=y2=y3=y4=0;	// calm down compiler

		for(int i=0; i<n; i++) {
			line = (TLine)init.lines.elementAt(i);
			state.x = line.x;
			state.y = line.y;
			for(int j=line.bol; j<line.eol; j++) {
				TElement e = (TElement)init.parsed.elementAt(j);

				if (e instanceof TEAnchorBgn) {
					TEAnchorBgn a = (TEAnchorBgn)e;
					if (a.href!=null) {
						current_anchor = new TAnchor();
						current_anchor.href = a.href;
						x1 = x2 = x3 = x4 = state.x;
						y1 = state.y-line.ascent;
						y2 = y3 = y4 = state.y+line.descent;
					}
				}

				if (current_anchor!=null) {
					if (x1>state.x)
						x1=state.x;
				}

				e.afterPaint(state);
				state.x += e.width;
				
				if (current_anchor!=null) {
					if (x4<state.x)
						x4=state.x;
				}

				if (e instanceof TEAnchorEnd && current_anchor!=null) {
						x3 = state.x;
						y3 = state.y-line.ascent;
						y4 = state.y+line.descent;
						Polygon p = new Polygon();
						p.addPoint(x2,y1);
						p.addPoint(x4,y1);
						p.addPoint(x4,y3);
						p.addPoint(x3,y3);
						p.addPoint(x3,y4);
						p.addPoint(x1,y4);
						p.addPoint(x1,y2);
						p.addPoint(x2,y2);
						current_anchor.polygon = p;
						init.anchors.addElement(current_anchor);
						current_anchor = null;
				}

				// special stuff for space to be removed...
				if (state.x!=state.left && e.space)
					state.x += state.GetSpaceWidth();
			}
		}
	}

	public void paintItem(Graphics pen,int dummy1, int dummy2)
	{
		TInit init = new TInit();
		init.start_idx 	= 0;
		init.end_idx		= parsed.size();
		init.lines			= lines;
		init.parsed			= parsed;
		init.anchors		= anchors;
		init.y					= 0;			// translation!!!
		init.left				= 8;
		init.right			= Area().getSize().width - 8;
		paint(pen, init);
	}
	
	static public void paint(Graphics pen, TInit init)
	{
		TState state = new TState();
//		TStateInit state = new TStateInit();
		state.y 			= init.y;			// needed ?
		state.ascent 	= 8;					
		state.descent	= 8;
		state.left		= init.left;
		state.right		= init.right;
		state.x				= state.left;
		state.parsed	= init.parsed;
		state.end_idx = init.end_idx;
		
		pen.setFont(state.font);

//System.out.println("static paint called");

		int n = init.lines.size();	
		for(int i=0; i<n; i++) {
			TLine line = (TLine)init.lines.elementAt(i);
			state.x = line.x;
			state.y = line.y + init.y;
//			System.out.println("drawing line "+i+" from "+line.bol+" to "+line.eol);
			for(int j=line.bol; j<line.eol; j++) {
				TElement e = (TElement)init.parsed.elementAt(j);
				
				state.font_changed = false;
				state.color_changed = false;
				pen.translate(state.x,state.y);
				e.paint(pen, state);
				pen.translate(-state.x,-state.y);
				state.current_idx = j;
				e.afterPaint(state);
				state.x += e.width;
				j = state.current_idx;
				if (state.font_changed)
					pen.setFont(state.font);
				if (state.color_changed)
					pen.setColor(state.color);

				// special stuff for space to be removed...
				if (state.x!=state.left && e.space)
					state.x += state.GetSpaceWidth();
			}
		}
/*		
		n = anchors.size();
		pen.setColor(new Color(255,0,0));
		for(int i=0; i<n; i++) {
			TAnchor a = (TAnchor)init.anchors.elementAt(i);
			pen.drawPolygon(a.polygon);
		}
*/
	}

	class TElement
	{
		TElement() {
			width = 0;
			space = false;
		}
		int width;				// must be set to final value after init stage 1
		boolean space;		// must be set to final value after init stage 1
											// in some situations this flag will also be set
											// by the parser
		public void addParam(String s) { }
		public void init(TStateInit state) { }
		public boolean needNewLine(TState state) { return false; }
		public void paint(Graphics pen, TState state) { }
		public void afterPaint(TState state) { }
	}

	// static utility methods
	public static String GetRightSide(String s)
	{
		int p = s.indexOf('=');
		if (p<0)
			return "";
		return s.substring(p+1).trim();
	}
	
	public static String GetString(String s) {
		s = GetRightSide(s);
		int p = s.indexOf('\"');
		if (p<0)
			return "";
		s = s.substring(p).trim();
		int n = s.length();
		if (s.charAt(0)=='\"' && s.charAt(n-1)=='\"')
			s = s.substring(1,n-1);
			// System.out.println("\""+s+"\"");
		return s;
	}
	
	public static int GetPercent(String s) {
		s = GetRightSide(s);
		if (s.endsWith("%")) {
			return Integer.parseInt(s.substring(0,s.length()-1));
		}
		return -1;
	}
	
	public static int GetInteger(String s) {
		return Integer.parseInt(GetRightSide(s));
	}
	
	class TEWord
		extends TElement
	{
		String word;
	
		public TEWord(String s)
		{
			word = s;
			space = true;
		}

		public void init(TStateInit state)
		{
			width = state.GetWidth(word);
			state.element_ascent = state.Ascent();
			state.element_descent= state.Descent();
		}

		public void paint(Graphics pen, TState state)
		{
			pen.drawString(word,0,0);
		}
	}
	
	class TEBreak
		extends TElement
	{
		public boolean needNewLine(TState state)
		{
			return true;
		}
	}

	class TEParagraph
		extends TElement
	{
		public void init(TStateInit state)
		{
			state.par_top = 4;
			state.par_bottom = 8;
		}
	
		public boolean needNewLine(TState state)
		{
			return true;
		}
	}
	
	class TEHeading1Bgn
		extends TElement
	{
		public void init(TStateInit state)
		{
			state.par_top = 4;
		}
	
		public boolean needNewLine(TState state)
		{
			return true;
		}
		
		public void afterPaint(TState state)
		{
			state.SetFont(state.font_h1);
		}
	}

	class TEHeading2Bgn
		extends TEHeading1Bgn
	{
		public void afterPaint(TState state)
		{
			state.SetFont(state.font_h2);
		}
	}

	class TEHeading3Bgn
		extends TEHeading1Bgn
	{
		public void afterPaint(TState state)
		{
			state.SetFont(state.font_h3);
		}
	}

	class TEHeadingEnd
		extends TElement
	{
		public void init(TStateInit state)
		{
			state.par_bottom = 8;
		}

		public void afterPaint(TState state)
		{
			state.SetFont(state.font_normal);
		}
	}
	
	class TEUnorderedListBgn
		extends TElement
	{
		public void afterPaint(TState state)
		{
			state.left += 16;
		}
	
		public boolean needNewLine(TState state)
		{
			return true;
		}
	}

	class TEUnorderedListEnd
		extends TElement
	{
		public void afterPaint(TState state)
		{
			state.left -= 16;
		}
	}

	class TEListItem
		extends TElement
	{
		public void paint(Graphics pen, TState state)
		{
			pen.fillOval(-12,-8,7,7);
		}
	
		public boolean needNewLine(TState state)
		{
			return true;
		}
	}

	class TEAnchorBgn
		extends TElement
	{
		String href;
		String name;
	
		TEAnchorBgn() {
			href = name = null;
		}
	
		public void addParam(String p)
		{
			String u = p.toUpperCase();
			if (u.startsWith("HREF")) {
				href=GetString(p);
			} else if (u.startsWith("NAME")) {
				name=GetString(p);
			}
		}

		public void afterPaint(TState state)
		{
			if (href!=null)
				state.SetColor(0,0,255);
		}
	}

	class TEAnchorEnd
		extends TElement
	{
		public void afterPaint(TState state)
		{
			state.SetColor(0,0,0);
		}
	}
	
	class TEImage
		extends TElement
	{
		String filename;
		Image image;
		int height;
		int border;
		int img_width;

		TEImage() {
			border=0;
		}

		public void addParam(String p) {
			if (p.startsWith("SRC")) {
				try {
					filename=GetString(p);
					image = Toolkit.getDefaultToolkit().getImage(filename);
					MediaTracker tracker = new MediaTracker(THTMLView._this);
					tracker.addImage(image, 0);
					tracker.waitForAll();
					img_width = width = image.getWidth(null);
					height = image.getHeight(null);
				} catch (Exception e) {
					System.out.println("failed to load image " + e.getMessage());
				}
			} else if (p.startsWith("BORDER")) {
				 border = GetInteger(p);
			}
		}
		
		public void init(TStateInit state)
		{
			width = img_width + border*2;
			state.element_ascent = height+border*2;
		}
		
		public void paint(Graphics pen, TState state)
		{
			pen.fillRect(0,-height-border,width,height+border*2);
			pen.drawImage(image,0+border,-height,null);
		}
	}

	class TEHorizontalRule
		extends TElement
	{
		public void init(TStateInit state)
		{
			state.element_ascent = 2;
			state.element_descent = 2;
			width = state.right-state.left;
		}
		
		public boolean needNewLine(TState state)
		{
			return true;
		}

		public void paint(Graphics pen, TState state)
		{
			pen.drawLine(0,0,width,0);
		}
	}
	
	class TETableBgn
		extends TElement
	{
		int start_idx, end_idx;
		int height, width;				// table size in pixels
		int rows, cols;						// table size in fields
		int table_min_width;
		int table_max_width;
		
		int border;								// controls frame width around table
		int cellspacing;					// spacing between cells
		int cellpadding;					// spacing within cells to cell content
		int pwidth;								// width in percent (-1=none)

		class TField
		{
			int start_idx, end_idx;
			Vector lines;
		}
		TField field[][];
	
		class TRow
		{
			int max_height;			// of all fields in a row
		}
		TRow row[];

		class TCol
		{
			int min_width, max_width;			// of all fields in a column
			int width;
		}
		TCol col[];

		public TETableBgn()
		{
			cellspacing = 2;
			cellpadding = 5;
			pwidth = 0;
			border = 1;
		}

		public void addParam(String p)
		{
			String u = p.toUpperCase();
			if (u.startsWith("WIDTH")) {
				pwidth=GetPercent(p);
			} else if (u.startsWith("CELLPADDING")) {
				cellpadding=GetInteger(p);
			} else if (u.startsWith("CELLSPACING")) {
				cellspacing = GetInteger(p);
			} else if (u.startsWith("BORDER")) {
				border = GetInteger(p);
			}
		}
		
		public void init(TStateInit state)
		{
			width = 10;
			height = 100;
		
			state.element_ascent = height;

			// calculate number of rows and columns
			//--------------------------------------
			rows = 0;
			cols = 0;
			int cols_in_row = 0;

			start_idx = state.current_idx;
			for(int i=state.current_idx; i<state.end_idx; i++) {
				TElement e = (TElement)parsed.elementAt(i);
				if (e instanceof TETableEnd) {
					state.current_idx = i;
					end_idx = i;
					break;
				}
				if (e instanceof TETableRow) {
					rows++;
					cols_in_row = 0;
				}
				if (e instanceof TETableHead || e instanceof TETableData) {
					if (rows==0)
						rows++;
					cols_in_row++;
					if (cols_in_row>cols)
						cols = cols_in_row;
				}
			}

			// setup field array: create fields and set start_idx & end_idx
			//--------------------------------------------------------------
			field = new TField[cols][rows];
			int x,y;
			for(y=0; y<rows; y++) {
				for(x=0; x<cols; x++) {
					field[x][y]=null;
				}
			}
			
			x = y = -1;
			for(int i=start_idx; i<end_idx; i++) {
				TElement e = (TElement)parsed.elementAt(i);
				if (e instanceof TETableRow) {
					if (x>=0 && y>=0)
						field[x][y].end_idx = i;
					y++;
					x=-1;
				}
				if (e instanceof TETableHead || e instanceof TETableData) {
					if (x>=0 && y>=0)
						field[x][y].end_idx = i;
					if (y<0)
						y++;
					x++;
					field[x][y]=new TField();
					field[x][y].start_idx = i;
					field[x][y].lines = new Vector();
				}
			}
			if (x>=0 && y>=0)
				field[x][y].end_idx = end_idx;

			// setup field, row and column array: calculat min & max width
			//-------------------------------------------------------------
			col = new TCol[cols];
			for(x=0; x<cols; x++) {
				col[x] = new TCol();
				col[x].min_width = 0;
				col[x].max_width = 0;
			}

			TInit init = new TInit();
			for(y=0; y<rows; y++) {
				for(x=0; x<cols; x++) {
					TField f = field[x][y];
//					System.out.print("["+x+","+y+"]:");
					if (f==null) {
//						System.out.println("empty");
					} else {
						init.parsed			= state.parsed;
						init.start_idx	= f.start_idx;
						init.end_idx		= f.end_idx;
						init.left = 0;
						init.right = 10000;
						init.lines = null; init.anchors = null;
						THTMLView.Init(init,1);
						init.min_width+=cellpadding<<1;
						init.weight+=cellpadding<<1;
						if (col[x].min_width<init.min_width)
							col[x].min_width = init.min_width;
						if (col[x].max_width<init.weight)
							col[x].max_width = init.weight;
/*
						System.out.println(
							 " idx:"+f.start_idx+","+f.end_idx
							+" min:"+init.min_width
							+" weight:"+init.weight
						);
*/
					}
				}
			}
			
			// calculate table minimal and maximal width
			//-------------------------------------------
			table_min_width = table_max_width = 0;
			for(x=0; x<cols; x++) {
				table_min_width+=col[x].min_width;
				table_max_width+=col[x].max_width;
			}

			// if (state.stage=2) {

			// calculate table width
			//-------------------------------------------
			width = state.right - state.left;
//System.out.println("table: min:"+table_min_width+" max:"+table_max_width+" available:"+width);
			if (width < table_min_width)
				width = table_min_width;
			else if (width > table_max_width)
				width = table_max_width;
			if (pwidth>0) {
					int w2 = (state.right-state.left)*pwidth/100;
				if (width<w2)
					width = w2;
			}

			// calculate column width
			//-------------------------------------------
			if (width==table_max_width) {
				for(x=0; x<cols; x++) {
					col[x].width=col[x].max_width;
				}
			} else if (width==table_min_width) {
				for(x=0; x<cols; x++) {
					col[x].width=col[x].min_width;
				}
			} else {
				for(x=0; x<cols; x++) {
					col[x].width=col[x].min_width + (int)
					(
						((double)width-(double)table_min_width) *
						((double)col[x].max_width/(double)table_max_width)
					);
				}
			}

			// calculate field height & line layout
			//-------------------------------------------
			row = new TRow[rows];

			int yp = cellpadding;
			for(y=0; y<rows; y++) {
				int xp = 0; // state.left;
				row[y] = new TRow();
				row[y].max_height = 0;
				for(x=0; x<cols; x++) {
					TField f = field[x][y];
					if (f==null) {
//						System.out.println("empty");
					} else {
						init.parsed			= state.parsed;
						init.anchors		= null;
						init.start_idx	= f.start_idx;
						init.end_idx		= f.end_idx;
						init.y					= yp;
						init.left 			= xp+cellpadding;
						init.right			= xp+col[x].width-cellpadding;
						init.lines 			= f.lines;
						THTMLView.Init(init,2);
						xp+=col[x].width;
						int height = init.y-yp;
//System.out.println("["+x+","+y+"]: height:"+height);
						if (row[y].max_height < height)
							row[y].max_height = height;
					}
				}
//System.out.println("row "+y+" height:" + row[y].max_height); 
				row[y].max_height += (cellpadding<<1);
				yp+=row[y].max_height;
			}
			height = yp - cellpadding;
			state.element_ascent = height;
//System.out.println("table height:"+height);
			// }
		}

		public void paint(Graphics pen, TState state)
		{
			int xp, yp=0;
			
			if (border>0) {
				pen.drawRect(0,-height,width,height);
				xp = 0;
				for(int x=0; x<cols-1; x++) {
					xp+=col[x].width;
					pen.drawLine(xp,-height,xp,0);
				}

				yp = -height;
				for(int y=0; y<rows-1; y++) {
					yp+=row[y].max_height;
					pen.drawLine(0,yp,width,yp);
				}
			}
			
//			pen.setColor(new Color(0,0,0));

			TInit init = new TInit();
			for(int y=0; y<rows; y++) {
				xp=0;
				for(int x=0; x<cols; x++) {
					TField f = field[x][y];
					if (f!=null) {
//						System.out.println("painting ["+x+","+y+"] from "+f.start_idx+" to "+f.end_idx);
						init.start_idx 	= f.start_idx;
						init.end_idx		= f.end_idx;
						init.lines			= f.lines;
						init.parsed			= parsed;
						init.anchors		= anchors;
						init.y					= -height;
						init.left				= xp;
						init.right			= xp+col[x].width;
						THTMLView.paint(pen, init);
					}
					xp+=col[x].width;
				}
				yp+=row[y].max_height;
			}
/*			
			pen.setColor(new Color(255,0,0));
			pen.drawLine(0,-2,table_max_width,-2);
			pen.setColor(new Color(0,255,0));
			pen.drawLine(0,-1,table_min_width,-1);
			pen.setColor(Color.black);
*/
		}

		public void afterPaint(TState state)
		{
			state.current_idx = end_idx;
			state.x += width;
		}
	}
	
	class TETableEnd
		extends TElement
	{
	}

	class TETableRow
		extends TElement
	{
	}

	class TETableHead
		extends TElement
	{
	}

	class TETableData
		extends TElement
	{
	}

	/**************************************************************************
	 *                                                                        *
	 * class THTMLParser                                                      *
	 *                                                                        *
	 **************************************************************************/
	class THTMLParser
	{
		String input;			// input string
		int eof;					// length of input string
		int pos;					// current position in input string
		boolean _debug_scan_ws = false;
		
		Vector parsed;		// parsed elements
		TElement last;		// last added element
		
		TEBreak element_break;	

		public THTMLParser(String value, Vector p)
		{
			element_break = new TEBreak();
		
			parsed = p;
			input = value+" ";
			eof = value.length();
			pos = 0;
			while(GetToken());
			System.out.println("end of parse");
		}
		
		boolean GetToken()
		{
			SkipWS();
			if (pos>=eof)
				return false;
			String s;
			if (input.charAt(pos)=='<') {
				pos++;
				if (pos<=eof && !IsWS()) {
					TknBgnCommand();
					return true;
				}
				pos--;
			} else if (input.charAt(pos)=='>' && inside_command) {
				pos++;
				TknEndCommand();
				return true;
			}
			int l1 = pos;
			while(!IsWS() && 
						input.charAt(pos)!='<' && 
						input.charAt(pos)!='>' &&
						pos<eof) {
				pos++;
			}
			s = input.substring(l1,pos);
			if (inside_command) {
				DistCommandParam(s);
			} else {
				DistTextParam(s);
			}
			return true;
		}

		boolean inside_command = false;
		boolean inside_html    = false;
		boolean inside_head    = false;
		boolean inside_body    = false;
		boolean first_of_cmd   = false;
		boolean start_cmd      = false;
		
		void TknBgnCommand()
		{
			inside_command = true;
			first_of_cmd   = true;
			start_cmd      = true;
		}
		
		void TknEndCommand()
		{
			inside_command = false;
		}
		
		void DistCommandParam(String s)
		{
			if (first_of_cmd) {
				if (s.charAt(0)=='/') {
					start_cmd = false;
					s = s.substring(1);
				}
				s=s.toUpperCase();
//System.out.println(":"+s+":");
				if (s.compareTo("HTML")==0) {
					inside_html = start_cmd;
				} else if (s.compareTo("BODY")==0) {
					inside_body = start_cmd && inside_html;
				} else if (s.compareTo("BR")==0 && start_cmd) {
					Add(element_break);
				} else if (s.compareTo("P")==0) {
					Add(new TEParagraph());
					Add(element_break);
				} else if (s.compareTo("HR")==0 && start_cmd) {
					Add(new TEHorizontalRule());
					Add(element_break);
				} else if (s.compareTo("H1")==0) {
					if (start_cmd) {
						Add(new TEHeading1Bgn());
					} else {
						Add(new TEHeadingEnd());
						Add(element_break);
					}
				} else if (s.compareTo("H2")==0) {
					if (start_cmd) {
						Add(new TEHeading2Bgn());
					} else {
						Add(new TEHeadingEnd());
						Add(element_break);
					}
				} else if (s.compareTo("H3")==0) {
					if (start_cmd) {
						Add(new TEHeading3Bgn());
					} else {
						Add(new TEHeadingEnd());
						Add(element_break);
					}
				} else if (s.compareTo("UL")==0) {
					if (start_cmd) {
						Add(new TEUnorderedListBgn());
					} else {
						Add(new TEUnorderedListEnd());
						Add(element_break);
					}
				} else if (s.compareTo("LI")==0) {
					if (start_cmd) {
						Add(new TEListItem());
					}
				} else if (s.compareTo("A")==0) {
					if (start_cmd) {
						Add(new TEAnchorBgn());
					} else {
						Add(new TEAnchorEnd());
					}
				} else if (s.compareTo("IMG")==0) {
					if (start_cmd) {
						Add(new TEImage());
					}
				} else if (s.compareTo("TABLE")==0) {
					if (start_cmd) {
						Add(element_break);
						Add(new TETableBgn());
					} else {
						Add(new TETableEnd());
						Add(element_break);
					}
				} else if (s.compareTo("TR")==0) {
					if (start_cmd) {
						Add(new TETableRow());
					}
				} else if (s.compareTo("TH")==0) {
					if (start_cmd) {
						Add(new TETableHead());
					}
				} else if (s.compareTo("TD")==0) {
					if (start_cmd) {
						Add(new TETableData());
					}
				}
				first_of_cmd = false;
			} else {
				// System.out.println("extra param: \""+s+"\"");
				if (last!=null) {
					last.addParam(s);
				}
			}
		}

		void DistTextParam(String s)
		{
			if (inside_body) {
//				System.out.println(":"+s+":");
				Add(new TEWord(s));
			}
		}

		void Add(TElement e)
		{
			last = e;
			parsed.addElement(e);
		}
		
		boolean IsWS()
		{
			char c = input.charAt(pos);
			switch(c) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					return true;
			}
			return false;
		}
		
		void NextChar()
		{
			pos++;
			if (pos>=eof) {
				System.out.println("stop");
			}
		}
		
		void SkipWS()
		{
			if (_debug_scan_ws) {
				int start = pos;
				while(IsWS() && pos<eof) {
					pos++;
				}
				System.out.println("SkipWs:+\""+input.substring(start,pos)+"\"");
			} else {
				while (IsWS() && pos<eof) {
					pos++;
				}
			}
		}
	}	// end of class THTMLParser

}
