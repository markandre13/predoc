import java.awt.*;
import java.awt.event.*;
import common.*;

class TOverview
	extends Frame
	implements TStateListener
{
	TDDocViewState _state;
	Dimension _page_size;
	TViewPort _viewport;
	TOverview _this;

	TOverview(TDDocViewState state)
	{
		_this = this;
		_state = state;
		_state.Register(this);
		_state.SetOverview(this);
		setTitle("PReDoc Overview: "+_state.Filename());
		setSize(640,480);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				_state.SetOverview(null);
				_state.UnRegister(_this);
			}
		});
		
		_page_size = new Dimension(100,150);
		
		common.TFormLayout form = new common.TFormLayout(this);
		_viewport = new TViewPort();
		
		form.Attach(_viewport, TFormLayout.ALL);
	}

	public void stateChanged(TState state)
	{
		if (state instanceof TDDocViewState) {
			switch(state.Reason()) {
				case TDDocViewState.PAGE_SCANNED:
				case TDDocViewState.PAGE_CORRECTION_ADDED:
				case TDDocViewState.PAGE_CORRECTION_REMOVED:
				case TDDocViewState.PAGE_CORRECTION_CHANGED:
					_viewport.Area().repaint();
					break;
			}
		}
	}
	
	class TViewPort
		extends TScrolledArea
	{
		int dx,dy;
		
		TViewPort()
		{
			int bx = 10, by = 10;	// minimal space to border
			dx = 10; dy = 10; 		// space between the pages

			dy+=16;	// font height

			item_w = _page_size.width  + dx;
			item_h = _page_size.height + dy;

			SetDoubleBuffer(true);

			SetDimensionTo(DIM1);
			area_min = 1;
			area_max = _state.PageCount();
			
			dx>>=1;
			dy>>=2;
			
			Area().addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					int n = GetItem(e.getPoint());
					_state.GoPage(n-1);
				}
			});
		}
	
		public void paintItem(Graphics pen, int pn, int dummy)
		{
			FontMetrics fm = pen.getFontMetrics();
			// dy+=fm.getHeight()+4;
			int fa = fm.getAscent();

			TDDocument doc = _state.Document();
			TDPage page = doc.Page(pn-1);
			TDPageCorrection correction = page.Correction();
			TDScanData scan = page.ScanData();

//			pen.drawRect(0,0,item_w-1,item_h-1);
					
			// paper
			int px = dx; // bx+in.left+x*(_page_size.width +dx);
			int py = dy; // by+in.top +y*(_page_size.height+dy);
			if (correction.Size()!=0) {
				pen.setColor(Color.red);
				pen.drawRect(px-1,py-1,_page_size.width+1,_page_size.height+1);
			}
			pen.setColor(Color.white);
			pen.fillRect(
				px,
				py,
				_page_size.width,
				_page_size.height
			);
					
			// contents
			if (scan!=null) {
				pen.setColor(Color.black);
				TRectangle r;
				int i=0;
				while(true) {
					r = scan.LetterRect(i);
					if (r==null)
						break;
					i++;
					pen.fillRect(
						(int)(r.x*_page_size.width)+px,
						(int)(r.y*_page_size.height)+py,
						(int)(r.w*_page_size.width),
						(int)(r.h*_page_size.height)
					);
				}
			}
					
			// corrections
			pen.setColor(Color.red);
			for(int i=0; i<correction.Size(); i++) {
				Polygon poly = correction.Polygon(i,_page_size.width,_page_size.height);
				poly.translate(px,py);
				pen.drawPolygon(poly);
			}
			pen.setColor(Color.black);
			
			String pns = ""+pn;
			pn++;
					
			pen.drawString(pns,
				( item_w - fm.stringWidth(pns) ) >> 1,
				dy + _page_size.height + 2 + fa);
		}
	}	// end of class TViewPort
}
