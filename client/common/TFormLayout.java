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
import java.util.Vector;

/*
 * The form layout manager i developed for my TOAD C++ GUI Toolkit.
 */
public class TFormLayout
  implements LayoutManager
{
	static class Node {
		Node() {
	 		how = new int[4];
	  	which = new Component[4];
	  	for(int i=0; i<4; i++) {
	    	how[i] = 0; // NONE;
	    	which[i] = null;
	  	}
	  	coord = new int[4];
	  	dist  = new int[4];
		}
		int how[];
		Component which[];
		int dist[];
		Component it;
		int coord[];
		int flag, nflag;
	}

  // attachment side
  public static final int TOP    = 1;
  public static final int BOTTOM = 2;
  public static final int LEFT   = 4;
  public static final int RIGHT  = 8;
  public static final int HORZ   = 12;
  public static final int VERT   = 3;
  public static final int ALL    = 15;

  // attachment method
  public static final int NONE = 0;
  public static final int FORM = 1;
  public static final int WINDOW = 2;
  public static final int OPPOSITE_WINDOW = 3;

  Vector _flist;
  Container _parent;

  public int nBorderOverlap;

  public TFormLayout(Container parent)
  {
    _flist = new Vector();
    _parent = parent;
    _parent.setLayout(this);
    
    nBorderOverlap = 0;
  }
  
  public void Attach(Component wnd, int where, int how, Component which)
  {
    Node node = _find(wnd);

    // System.out.println("where="+where+", how="+how);

    for(int i=0; i<4; i++) {
      if( (i==0 && (where&TOP)!=0   ) ||
				  (i==1 && (where&BOTTOM)!=0) ||
				  (i==2 && (where&LEFT)!=0  ) ||
				  (i==3 && (where&RIGHT)!=0 ) )
			{
			  // System.out.println("attach side "+i+" with "+how);
			  node.how[i]=how;
			  node.which[i]=which;
			}
		}
  }

  public void Attach(Component wnd, int where)
  {
    Attach(wnd, where, FORM, null);
  }

  public void Attach(Component wnd, int where, Component which)
  {
    Attach(wnd, where, WINDOW, which);
  }


  public void Distance(Component wnd, int distance, int where)
  {
    Node node = _find(wnd);

    // System.out.println("where="+where+", how="+how);

    for(int i=0; i<4; i++) {
      if( (i==0 && (where&TOP)!=0   ) ||
			  (i==1 && (where&BOTTOM)!=0) ||
			  (i==2 && (where&LEFT)!=0  ) ||
			  (i==3 && (where&RIGHT)!=0 ) )
			{
			  node.dist[i]=distance;
			}
		}
  }

	public void Distance(Component wnd, int distance)
	{
		Distance(wnd, distance, ALL);
	}

  Node _find(Component which)
  {
    // System.out.println("_find");
    Node node;
    for(int i=0; i<_flist.size(); i++) {
      // System.out.println("loop: " +i);
      node = (Node)_flist.elementAt(i);
      if (which==node.it) {
				return node;
      }
    }
//System.out.println("addding");
    _parent.add(which);
    node = new Node();
    _flist.addElement(node);
    node.it = which;
    return node;
  }

  static final int DTOP = 0;
  static final int DBOTTOM = 1;
  static final int DLEFT = 2;
  static final int DRIGHT = 3;
    
  static final int HAS_T = 1;
  static final int HAS_B = 2;
  static final int HAS_L = 4;
  static final int HAS_R = 8;
  static final int HAS_ALL = 15;
  
  public void layoutContainer(Container parent)
  {
  	if (_flist.size()==0)
  		return;
  
    Insets insets = parent.getInsets();
    int fx = insets.left - nBorderOverlap;
    int fy = insets.top  - nBorderOverlap;
    int fw = parent.getSize().width - insets.left - insets.right + 2*nBorderOverlap;
    int fh = parent.getSize().height- insets.top  - insets.bottom+ 2*nBorderOverlap;

    boolean bKeepOwnBorder = true;

    // Verbindungsflags löschen, etc.
    //--------------------------------
    Rectangle shape;
    int nChildren = 0;
    boolean bError = false;
    int nptr = 0;
    Node ptr;
    do {
      ptr = (Node)_flist.elementAt(nptr); nptr++;
      // System.out.println("Child #"+(nptr-1) );
      nChildren++;
      ptr.flag = 0;
      ptr.nflag= 0;
      // ptr.it.setSize(ptr.it.getPreferredSize());
      shape=ptr.it.getBounds();
      ptr.coord[DTOP]    = shape.y;
      ptr.coord[DBOTTOM] = shape.y+shape.height;
      ptr.coord[DLEFT]   = shape.x;
      ptr.coord[DRIGHT]  = shape.x+shape.width;
      int n=0;
      for(int i=0; i<4; i++) {
				// System.out.println("  side "+i+" with "+ptr.how[i]);
				if (ptr.how[i] == NONE ) {
				  ptr.nflag|=(1<<i);
				  n++;
				}
      }
      /*
      System.out.println("  ("+
			 ptr.coord[DLEFT]+","+
			 ptr.coord[DTOP]+")-("+
			 ptr.coord[DRIGHT]+","+
			 ptr.coord[DBOTTOM]+")");
      System.out.println("  already connected: "+ptr.nflag);
      */      
      if ((ptr.nflag&3)==3 || (ptr.nflag&12)==12) {
				// System.out.println("TFormlayout: Widget within TForm has undefined attachment\n");
				bError = true;
      }
    }while(nptr<_flist.size());
    if (bError)
      System.out.println("TFormLayout: can´t arrange children");

//    System.out.println("layoutContainer: (2)");

    // Objekte setzen, deren Verbindungen geklärt sind
    //-------------------------------------------------
    nptr=0;
    int form[] = new int[4];
    // form[0]=0; form[1]=nHeight; form[2]=0; form[3]=nWidth;
    form[DTOP]=fy;
    form[DBOTTOM]=fy+fh;
    form[DLEFT]=fx;
    form[DRIGHT]=fx+fw;
    Node ptr2;
    int count=0, done=0;
    while(true) {
      count++;
      if (ptr.flag != HAS_ALL) {
				for(int i=0; i<4; i++) {
				  if ( (ptr.flag & (1<<i)) ==0 ) {
				    switch(ptr.how[i]) {
					    case FORM:
					      ptr.flag |= (1<<i);
					      ptr.coord[i] = form[i];
					      if ((i&1)!=0) {
									if (!bKeepOwnBorder)
									  ptr.coord[i] += nBorderOverlap;
									ptr.coord[i] -= ptr.dist[i];
					      } else {
									if (!bKeepOwnBorder)
									  ptr.coord[i] -= nBorderOverlap;
									ptr.coord[i] += ptr.dist[i];
					      }
					      count = 0;
					      break;
					    case WINDOW:
					      ptr2=_find(ptr.which[i]);
					      if ( ((ptr2.flag) & (1<<(i^1))) != 0 ) {
									ptr.flag |=(1<<i);
									ptr.coord[i] = ptr2.coord[i^1];
									if ( (i&1)!=0 ) {
									  ptr.coord[i] += nBorderOverlap;
									  ptr.coord[i] -= Math.max(ptr.dist[i], ptr2.dist[i^1]);
									} else {
									  ptr.coord[i] -= nBorderOverlap;
									  ptr.coord[i] += Math.max(ptr.dist[i], ptr2.dist[i^1]);
									}
									count = 0;
					      }
					      break;
					    case OPPOSITE_WINDOW:
					      ptr2=_find(ptr.which[i]);
					      if ( ((ptr2.flag) & (1<<(i))) != 0 ) {
									ptr.flag |=(1<<i);
									ptr.coord[i] = ptr2.coord[i];
									count = 0;
					      }
					      // nBorderOverlap & Distance code is missing here
					      break;
				    }
				  }
				} // end for
				if ( (ptr.flag|ptr.nflag) == HAS_ALL) {

				  // no top and/or left attachment
				  shape = ptr.it.getBounds();
				  if ((ptr.nflag & TOP)!=0)
				    ptr.coord[DTOP] = ptr.coord[DBOTTOM] - shape.height;
				  if ((ptr.nflag & BOTTOM)!=0)
				    ptr.coord[DBOTTOM] = ptr.coord[DTOP] + shape.height;
				  if ((ptr.nflag & LEFT)!=0)
				    ptr.coord[DLEFT] = ptr.coord[DRIGHT] - shape.width;
				  if ((ptr.nflag & RIGHT)!=0)
				    ptr.coord[DRIGHT] = ptr.coord[DLEFT] + shape.width;
				  int w,h;

				  w = ptr.coord[DRIGHT] - ptr.coord[DLEFT];
				  h = ptr.coord[DBOTTOM] - ptr.coord[DTOP];

					shape.width = w; shape.height = h;
				  shape.x=ptr.coord[DLEFT]; shape.y=ptr.coord[DTOP];

/*
	  System.out.println("setBounds("+
			     shape.x+","+shape.y+","+
			     shape.width+","+shape.height+")");
Graphics pen = parent.getGraphics();
pen.setColor(Color.red);
pen.drawRect(shape.x, shape.y, shape.width, shape.height);
System.out.println("waiting");
try{System.in.read();}catch(Exception e){}
*/
				  ptr.it.setBounds(shape);
				  shape = ptr.it.getBounds();

				  ptr.coord[DTOP]    = shape.y;
				  ptr.coord[DBOTTOM] = shape.y+shape.height;
				  ptr.coord[DLEFT]   = shape.x;
				  ptr.coord[DRIGHT]  = shape.x+shape.width;
				  ptr.flag = HAS_ALL;

				  done++;
				}
      }
      if (done>=nChildren) {
	// System.out.println("arranged all children");
				return;
      }

      if (count>nChildren) {
				boolean bNoGuess = true;
				count=0;
				while(count<nChildren) {
				  if (ptr.flag != HAS_ALL) {
				    shape = ptr.it.getBounds();
				    if ( (ptr.nflag&LEFT)!=0 && (ptr.flag&LEFT)==0 && (ptr.flag&RIGHT)!=0 ) {
				      ptr.coord[DLEFT] = ptr.coord[DRIGHT] - shape.width;
				      ptr.flag|=HAS_L;
				      bNoGuess = false;
				    }
				    if ( (ptr.nflag&RIGHT)!=0 && (ptr.flag&RIGHT)==0 && (ptr.flag&LEFT)!=0 ) {
				      ptr.coord[DRIGHT] = ptr.coord[DLEFT] + shape.width;
				      ptr.flag|=HAS_R;
				      bNoGuess = false;
				    }
				    if ( (ptr.nflag&TOP)!=0 && (ptr.flag&TOP)==0 && (ptr.flag&BOTTOM)!=0 ) {
				      ptr.coord[DTOP] = ptr.coord[DBOTTOM] - shape.height;
				      ptr.flag|=HAS_T;
				      bNoGuess = false;
				    }
				    if ( (ptr.nflag&BOTTOM)!=0 && (ptr.flag&BOTTOM)==0 && (ptr.flag&TOP)!=0 ) {
				      ptr.coord[DBOTTOM] = ptr.coord[DTOP] + shape.height;
				      ptr.flag|=HAS_B;
				      bNoGuess = false;
				    }
				  }
				  count++;
				  ptr = (Node)_flist.elementAt(nptr); nptr++;
				  if (nptr==_flist.size()) 
				    nptr=0;
				}
				if(bNoGuess) {
				  System.out.println("TFormLayout: Can't handle recursive attachment. Stopped.");
				  return;
				}
				count = 0;
      }
      ptr = (Node)_flist.elementAt(nptr); nptr++;
      if (nptr==_flist.size()) 
				nptr=0;
    }
  }
  
  
  public void addLayoutComponent(String name,
				 Component comp)
  {
    // System.out.println("addLayoutComponent");
  }

  public void removeLayoutComponent(Component comp)
  {
    // System.out.println("removeLayoutComponent");
  }

  public Dimension preferredLayoutSize(Container parent)
  {
    // System.out.println("preferredLayoutSize");
    return parent.getSize();
  }
  
  public Dimension minimumLayoutSize(Container parent)
  {
    // System.out.println("minimumLayoutSize");
    return parent.getSize();
  }
}

