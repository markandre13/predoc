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
import java.util.Vector;

import common.*;
import draw.*;
import tree.*;

class TDlgSelection
	extends Dialog
	implements ActionListener
{
	class TSelectionShapeButton
		extends TFatRadioButton
	{
		int _shape;
		public TSelectionShapeButton(TRadioState state, Image image, int shape)
		{
			super(state,image);
			_shape = shape;
		}
		
		public void changed()
		{
			if (IsDown()) {
				_data.FigCorrection().SetSelectionShape(_shape);
				Invalidate();	// invalidate TDrawingArea
			}
		}
	}

	public static final int OK = 1;
	public static final int DELETE = 2;
	public int result;

	// graphic editor
	TDrawingArea _area;

	// correction data
	TDrawingData _data;
	TPolygon _poly;
	
	TDDocViewState _state;
	
	int _pw, _ph;			// page size
	int _sx, _sy;			// selection position (in image)


	/**
	*	When `data' is null a new TDrawingData object is created
	*/
	public TDlgSelection(TPolygon poly, TDDocViewState state, TDrawingData data)
	{
		super(Main.wnd, "Edit Selection");
		setModal(true);
		_state = state;
		_poly = poly;
		_data = data;

		// create an image from the selection
		//--------------------------------------------------------
		Image clip;
		Polygon awt_poly;
		Image whole = state.UnscaledImage();
		_pw = whole.getWidth(null);
		_ph = whole.getHeight(null);
		Rectangle clipping = poly.GetAWTRectangle(_pw, _ph);
		awt_poly = poly.GetAWTPolygon(whole.getWidth(null), whole.getHeight(null));

		clipping.x=0;
		clipping.y-=10;
		clipping.width=_pw;
		clipping.height+=20;
		awt_poly.translate(-clipping.x, -clipping.y);

		_sx = clipping.x;
		_sy = clipping.y;

		CropImageFilter filter = new CropImageFilter(
			clipping.x, clipping.y, 
			clipping.width, clipping.height);
		clip = createImage(new FilteredImageSource(whole.getSource(), filter));
		MediaTracker tracker = new MediaTracker(Main.wnd);
		tracker.addImage(clip, 0);
		try { tracker.waitForAll(); }
		catch (Exception e) {
			System.out.println("fatal: TDlgSelection couldn´t wait for image");
		}

		// add image to the TDrawingData object
		//--------------------------------------------------------
		if (_data==null) {
			_data = new TDrawingData();
			_data.Add(new TFigCorrection(clip, awt_poly, 0, 0));
		} else {
			TFigCorrection fi = _data.FigCorrection();
			fi._poly = awt_poly;
			if (fi!=null)
				fi.SetImage(clip);
		}

		// create drawing area
		//--------------------------------------------------------
		_area = new TDrawingArea(_data);

		// create tree with correction types
		//--------------------------------------------------------
		TTreeConstructor tc = new TTreeConstructor();
		tc.BgnSub(new TTNDIN16551("Freihand", 0));
		tc.EndSub();

		tc.BgnSub(new TTNSection("DIN 16511 Korrekturen"));
			tc.BgnSub(new TTNSection("Sortiert nach Adjektiv"));

				{ int a[] = { 1,2,9,18,23,26,37 }; 
					_AddTypes(tc, "falsch", a); }
				{	int a[] = { 3,4,7,13,22,29,30,25,27 }; 
					_AddTypes(tc, "überflüssig", a); }
				{ int a[] = { 5,6,12,17,19,24,28,31,33 }; 
					_AddTypes(tc, "fehlend", a); }
				{ int a[] = { 14,15,16,32 };
					_AddTypes(tc, "verstellt", a); }
				{ int a[] = { 8,10,11,20,21,34,35,36 };
					_AddTypes(tc, "sonstiges", a); }
			tc.EndSub();
			tc.BgnSub(new TTNSection("Sortiert nach Substantiv"));
				{	int a[] = { 1,3,5,8,10,14,6,7,27 };
					_AddTypes(tc, "Buchstabe & Satzzeichen", a); }
				{	int a[] = { 2,4,15,17,16 };
					_AddTypes(tc, "Wort & Zahl", a); }
				{	int a[] = { 9,23,12,13 };
					_AddTypes(tc, "Schrift & Ligatur", a); }
				{	int a[] = { 19,20,21,22,24,25,28,29,30,31,33,34 };
					_AddTypes(tc, "Abstände", a); }
				{	int a[] = { 11,32,26 };
					_AddTypes(tc, "Zeilen & Linie", a); }
				{	int a[] = { 18,36,35,37 };
					_AddTypes(tc, "Sonstiges", a); }
			tc.EndSub();
			tc.BgnSub(new TTNSection("Sortiert nach Korrekturverfahren"));
			
				{	int a[] = { 1,2,5,6,14,16,18 };
					_AddTypes(tc, "Durchstreichen & Randtext", a); }
				{	int a[] = { 8,9 };
					_AddTypes(tc, "Durchstreichen & 1× unterstrichener Randtext", a); }
				{	int a[] = { 3,4,7 };
					_AddTypes(tc, "Durchstreichen, Delatur & Wortzwischenraumzeichen", a); }
				{	int a[] = { 36 };
					_AddTypes(tc, "Durchstreichen & Blockade Zeichen", a); }
				{	int a[] = { 10 };
					_AddTypes(tc, "Einkreisen", a); }
				{	int a[] = { 11,23,24,25 };
					_AddTypes(tc, "1× unterstreichen, waagerechter Strich am Rand & Randtext", a); }
				{	int a[] = { 15 };
					_AddTypes(tc, "1× unterstreichen, waagerechter Strich am Rand & Randtext", a); }
				{	int a[] = { 26 };
					_AddTypes(tc, "Oben und unten waagerecht unterstreichen", a); }
				{	int a[] = { 27 };
					_AddTypes(tc, "1× unterstreichen, Doppelkreuz am Rand", a); }
				{	int a[] = { 32 };
					_AddTypes(tc, "Zeilen nummerieren", a); }
				{	int a[] = { 17,35 };
					_AddTypes(tc, "Winkelzeichen (mit Randtext)", a); }
				{	int a[] = { 37 };
					_AddTypes(tc, "Unterpunktieren und Korrektur am Rand durchstreichen", a); }
				{	int a[] = { 12,13,19,20,21,22,28,29,30,31,33,34 };
					_AddTypes(tc, "Zeichen", a); }
			tc.EndSub();
		tc.EndSub();
		tc.BgnSub(new TTNSection("Inhaltliche Fehler"));
			tc.Add(new TTNContent("Sachlich falsch",""));
			tc.BgnSub(new TTNSection("Quellenangabe"));
				tc.Add(new TTNContent("falsch",""));
				tc.Add(new TTNContent("fehlt",""));
			tc.EndSub();
			tc.Add(new TTNContent("Wortwahl",""));
		tc.EndSub();

		// create treeview and preselect item
		//--------------------------------------------------------
		TTreeView tv = new TTreeView(tc.Top());
{
	int type = _data.FigCorrection().Type();
	TTreeNode tn = tc.Top();
	Vector v = new Vector();
	tc.Top().Flatten(v);
	for(int i=0; i<v.size(); i++) {
		Object o = v.elementAt(i);
		if (o instanceof TTNDIN16551) {
			if ( ((TTNDIN16551)o)._id == type ) {
//				System.out.println("PRESELECT "+((TTNDIN16551)o)._label);
				tv.Select((TTreeNode)o);
			}
		}
	}
}

		// create command buttons
		//--------------------------------------------------------
		TPushButton btn1, btn2, btn3, btn4;
		btn1 = new TPushButton("Ok");
			btn1.setActionCommand("o");
			btn1.addActionListener(this);
			btn1.setSize(80,25);
		btn2 = new TPushButton("Delete");
			btn2.setActionCommand("d");
			btn2.addActionListener(this);
			btn2.setSize(80,25);
		btn3 = new TPushButton("Help");
			btn3.setActionCommand("h");
			btn3.addActionListener(this);
			btn3.setSize(80,25);
		btn4 = new TPushButton("Center");
			btn4.setActionCommand("c");
			btn4.addActionListener(this);
			btn4.setSize(80,25);

		// create toolbar buttons
		//--------------------------------------------------------

		TRadioState state1 = new TRadioState();
		TRadioState state2 = new TRadioState();

		TFatRadioButton btn[] = {
			new TToolButton(state1, Main.image[Main.IMG_TOOL_SELECT], new TToolSelect(_area), _area),
			new TToolButton(state1, Main.image[Main.IMG_TOOL_LINE], new TToolDrawLine(_area), _area),
			new TToolButton(state1, Main.image[Main.IMG_TOOL_RECT], new TToolDrawRect(_area), _area),
			new TToolButton(state1, Main.image[Main.IMG_TOOL_CIRC], new TToolDrawCirc(_area), _area),
			new TToolButton(state1, Main.image[Main.IMG_TOOL_POLY], new TToolDrawPoly(_area), _area),
			new TToolButton(state1, Main.image[Main.IMG_TOOL_TEXT], new TToolDrawText(_area), _area),
			new TSelectionShapeButton(state2, Main.image[Main.IMG_SELECT_LINE], TFigCorrection.SELECT_LINE),
			new TSelectionShapeButton(state2, Main.image[Main.IMG_SELECT_RECT], TFigCorrection.SELECT_RECT),
			new TSelectionShapeButton(state2, Main.image[Main.IMG_SELECT_POLY], TFigCorrection.SELECT_POLY),
		};

		state1.SetCurrent(btn[0]);	// preselect the `Select' Tool
		
		// compute the number of edges in the selection polygon in `awt_poly' from 
		// above
{
//	System.out.println("COMPUTING NUMBER OF EDGES");	

	int n=0;
	int d=-1, dn=0;
	int end = awt_poly.npoints;
	for(int i=0; i<=end; i++) {
		int i1 = (i  ) % awt_poly.npoints;
		int i2 = (i+1) % awt_poly.npoints;

		int dx = awt_poly.xpoints[i2]-awt_poly.xpoints[i1];
		int dy = awt_poly.ypoints[i2]-awt_poly.ypoints[i1];

		dn = 0;
		if (dx<0) dn|=1; else if (dx>0) dn|=2;
		if (dy<0) dn|=4; else if (dy>0) dn|=8;
/*
		System.out.println((i1)+" to "+i2+": "+
				"("+awt_poly.xpoints[i1]+","+awt_poly.ypoints[i1]+")-"+
				"("+awt_poly.xpoints[i2]+","+awt_poly.ypoints[i2]+") "+
				"  delta: "+dx+","+dy+
				"  old d: "+d+
				"  new d: "+dn);
*/
		if (d>0 && dn>0 && d!=dn) {
			n++;
			d = dn;
//			System.out.println("edge setting new d = "+dn);
		}
		if (d<0 && dn>0) {
			d = dn;
			end+=i;
//			System.out.println("first line new d = "+dn);
		}
	}
//	System.out.println("FOUND " + n + " EDGES");
	if (n<=2)
		state2.SetCurrent(btn[6]);
	else if (n<=4)
		state2.SetCurrent(btn[7]);
	else
		state2.SetCurrent(btn[8]);
}

		// arrange children
		//--------------------------------------------------------
		TFormLayout form = new TFormLayout(this);
		setSize(790,480);

		int dist = 8;

		// toolbar to the upper left side
		//--------------------------------------------------------
		for(int i=0; i<btn.length; i++) {
			btn[i].setSize(30,30);
			form.Distance(btn[i], 3, TFormLayout.RIGHT);
			form.Distance(btn[i], dist, TFormLayout.LEFT);
			if (i==0) {
				form.Attach(btn[0], TFormLayout.LEFT | TFormLayout.TOP);
			} else {
				form.Attach(btn[i], TFormLayout.LEFT);
				form.Attach(btn[i], TFormLayout.TOP, btn[i-1]);
			}
		}
		form.Distance(btn[0], dist, TFormLayout.TOP);
		form.Distance(btn[6], 3, TFormLayout.TOP);

		// command buttons to the upper right side
		//--------------------------------------------------------
		form.Attach(btn1,	TFormLayout.TOP | TFormLayout.RIGHT);
		form.Distance(btn1, dist);
		form.Attach(btn2,	TFormLayout.TOP, btn1);
		form.Attach(btn2, TFormLayout.RIGHT);
		form.Distance(btn2, dist);
		form.Attach(btn3,	TFormLayout.TOP, btn2);
		form.Attach(btn3, TFormLayout.RIGHT);
		form.Distance(btn3, dist);
		form.Attach(btn4,	TFormLayout.TOP, btn3);
		form.Attach(btn4, TFormLayout.RIGHT);
		form.Distance(btn4, dist);

		// place drawing area between toolbar and command buttons
		//--------------------------------------------------------
		form.Attach(_area, TFormLayout.TOP,    TFormLayout.OPPOSITE_WINDOW, btn[0]);
		form.Attach(_area, TFormLayout.BOTTOM, TFormLayout.OPPOSITE_WINDOW, btn[btn.length-1]);
		form.Attach(_area, TFormLayout.LEFT, btn[0]);
		form.Attach(_area, TFormLayout.RIGHT, btn1);

		form.Attach(tv, TFormLayout.TOP, _area);
		form.Attach(tv,   TFormLayout.BOTTOM | TFormLayout.LEFT);
		form.Attach(tv,		TFormLayout.RIGHT, btn1);
		form.Distance(tv, dist);
	}

	public void actionPerformed(ActionEvent e)
	{
		TFigCorrection fig = _data.FigCorrection();
		String cmd = e.getActionCommand();
		if (cmd=="o") {
			result = OK;
			
			// set polygon from drawing data!!! (fig._poly -> _poly)
			for(int i=0; i<fig._poly.npoints; i++) {
				_poly.getPoint(i).Set(
					((double)fig._poly.xpoints[i]+_sx)/((double)_pw),
					((double)fig._poly.ypoints[i]+_sy)/((double)_ph)
				);
			}
			setVisible(false);
			if (fig!=null) {
				fig.SetImage(null);
			}
			_state.Notify(TDDocViewState.PAGE_CORRECTION_CHANGED);
		} else if (cmd=="d") {
			result = DELETE;
			setVisible(false);
			if (fig!=null) {
				fig.SetImage(null);
			}
		} else if (cmd=="c") {
			Center();
		} else if (cmd=="h") {
			Main.OpenHTML("edit_selection.html");
		}
	}

	class TTNDIN16551
		extends TTreeNode
	{
		String _label;
		int _id;
		public TTNDIN16551(String label, int id)
		{
			_label = label;
			_id = id;
		}
		
		public void paint(Graphics pen)
		{
			pen.setFont(Main.fntSans);
			pen.drawString(_label,2,12);
		}
		
		public boolean IsSelectable()
		{
			return true;
		}
		
		public void selected()
		{
			TFigCorrection fc = _data.FigCorrection();
			if (fc!=null) {
				fc.SetType(_id);
				Center();
			}
		}
	}

	public TDrawingData DrawingData()
	{
		return _area._data;
	}
	
	public void Invalidate()
	{
		_area._paper.repaint();
	}
	
	public void Center()
	{
		_area.Center();
	}

	protected final void _AddTypes(TTreeConstructor tc, String head, int[] a)
	{
		tc.BgnSub(new TTNSection(head));
		for(int i=0; i<a.length; i++)
		tc.Add(new TTNDIN16551(draw.TFigCorrection.Name(a[i]), a[i]));
		tc.EndSub();
	}

}
