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
import java.awt.image.*;
import java.util.*;
import java.io.*;
import java.applet.Applet;

import common.*;
import server.*;
import html.THTMLFrame;

public class Main
	extends Frame
{
	final static String _image_name[] = {
		"local_server.gif",
		"remote_server.gif",
		"directory.gif",
		"document.gif",
		"correction.gif",
		"tool_select.gif",
		"tool_line.gif",
		"tool_rect.gif",
		"tool_circ.gif",
		"tool_poly.gif",
		"tool_text.gif",
		"select_line.gif",
		"select_rect.gif",
		"select_poly.gif",
		"page_left.gif",
		"page_right.gif",
		"mark_rect.gif",
		"mark_poly.gif",
		"page_zoom_in.gif",
		"page_zoom_out.gif",
		"predoc.gif",
		"icon_hand.gif"
	};

	public static Image image[];
	
	public static final int IMG_LOCAL_SERVER	=  0;
	public static final int IMG_REMOTE_SERVER	=  1;
	public static final int IMG_DIRECTORY			=  2;
	public static final int IMG_DOCUMENT			=  3;
	public static final int IMG_CORRECTION		=  4;
	public static final int IMG_TOOL_SELECT		=  5;
	public static final int IMG_TOOL_LINE 		=  6;
	public static final int IMG_TOOL_RECT 		=  7;
	public static final int IMG_TOOL_CIRC 		=  8;
	public static final int IMG_TOOL_POLY 		=  9;
	public static final int IMG_TOOL_TEXT 		= 10;
	public static final int IMG_SELECT_LINE		= 11;
	public static final int IMG_SELECT_RECT		= 12;
	public static final int IMG_SELECT_POLY		= 13;
	public static final int IMG_PAGE_PREV			= 14;
	public static final int IMG_PAGE_NEXT			= 15;
	public static final int IMG_MARK_RECT			= 16;
	public static final int IMG_MARK_POLY			= 17;
	public static final int IMG_ZOOM_IN				= 18;
	public static final int IMG_ZOOM_OUT			= 19;
	public static final int IMG_LOGO					= 20;
	public static final int IMG_ICON_HAND			= 21;

	public static final Font fntSystem		= new Font("SansSerif", Font.PLAIN, 10);
	public static final Font fntSans 			= new Font("SansSerif", Font.PLAIN, 12);
	public static final Font fntSansBold	= new Font("SansSerif", Font.BOLD , 12);
	public static final Font fntType			= new Font("Monospaced", Font.PLAIN, 12);
	public static FontMetrics fmSystem;
  public static FontMetrics fmSans;
  public static FontMetrics fmSansBold;
  public static FontMetrics fmType;

	// the current file format version
	public static int io_version = 4;

  public static TDDocumentBuffer documents;

  // debug
  public static boolean debug_bounding_rectangle = false;
  public static boolean debug_bounding_letter = false;

	public static boolean debug_selector = false;  
  public static boolean debug_memory = false;
  public static boolean debug_mvc = false;
  public static boolean debug_loader = false;
  
  public static boolean debug_memory_force = false;
  
  public static boolean use_corba = false;
  public static boolean thread_safe_corba = false;
  
  /**
   * 0: PNG
   * 1: Gray Uncompressed Bitmap
   */
  public static int bitmap_format = 0;

  public static Frame wnd;

	public static String Memory()
	{
		System.runFinalization();
		System.gc();
		Runtime rt = Runtime.getRuntime();
		String str = "mem: " + (rt.totalMemory()-rt.freeMemory());
		return str;
	}

	private static Applet _applet;

  public static void main(String server[], Applet applet)
  {
  	_applet = applet;
  	init_servers();
  	
    Main.documents = new TDDocumentBuffer();

    System.out.println("Loading libraries...");
    wnd = new Main();
    wnd.show();
  }

  
  public Main()
  {
    super("Main");

    
    System.out.println("Loading fonts...");
    fmSystem 	 = Toolkit.getDefaultToolkit().getFontMetrics(fntSystem);
    fmSans 		 = Toolkit.getDefaultToolkit().getFontMetrics(fntSans);
    fmSansBold = Toolkit.getDefaultToolkit().getFontMetrics(fntSansBold);
    fmType 		 = Toolkit.getDefaultToolkit().getFontMetrics(fntType);
    
		System.out.println("Loading icons...");
		MediaTracker tracker = new MediaTracker(this);
		image = new Image[_image_name.length];
		for(int i=0; i<_image_name.length; i++) {
			try {
				image[i] = GetImage("images/"+_image_name[i]);
			} catch (Exception e) {
				System.out.println("couldn't load image \""+_image_name[i]+"\"");
			}
			tracker.addImage(image[i], 0);
		}
		try {
			tracker.waitForAll();
		}
		catch(Exception e) {
			System.out.println("Couldn't load icons: "+e.getMessage());
			System.exit(0);
		}

		System.out.println("okay");

wnd = this;
/*
mbox = new TMessageBox(
	"STOP",
	"Your modifications aren't stored. Do you want to ",
	TMessageBox.OK | TMessageBox.CANCEL,
	image[IMG_ICON_HAND]);
*/

    setTitle("PReDoc");
    setSize(640,480);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});

    TMenuBar mb = new TMenuBar(this);
    mb.BgnPulldown("File");

    mb.AddItem("Open..",    KeyEvent.VK_O, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		TDlgOpenDocument dlg = new TDlgOpenDocument();
    		dlg.show();
    	}
    });
    mb.AddItem("Save",      KeyEvent.VK_S, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		System.out.println("Save");
    		for(int i=0; i<Main.documents.Size(); i++)
	    		Main.documents.Document(i).Save();
    	}
    });
    mb.AddItem("Close",      KeyEvent.VK_C, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		System.out.println("Close");
/*
    		for(int i=0; i<Main.documents.Size(); i++)
	    		Main.documents.Document(i).Save();
*/
	    	Main.documents.RemoveAll();
    	}
    });
/*
    mb.AddItem("Save As..", KeyEvent.VK_A, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		System.out.println("SaveAs");
    	}
    });
*/
    mb.AddItem("Exit",      KeyEvent.VK_X, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		if (Main.documents.RemoveAll()) {
	    		System.exit(0);
	    	}
    	}
    });
    mb.EndPulldown();
/*    
    mb.BgnPulldown("Options");
    mb.AddCheck("Debug", new ItemListener() {
		  TDlgDebug _dlg = null;

		  public void itemStateChanged(ItemEvent e)
		  {
		    switch(e.getStateChange()) {
		      case ItemEvent.SELECTED:
						if (_dlg==null)
							_dlg = new TDlgDebug();
						_dlg.setVisible(true);
						break;
      		case ItemEvent.DESELECTED:
						if (_dlg!=null)
						  _dlg.setVisible(false);
					  _dlg = null;
						break;
					}
			}
		});

    mb.EndPulldown();
*/
    mb.BgnPulldown("Help");
    mb.AddItem("Help",       KeyEvent.VK_N, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		Main.OpenHTML("index.html");
    	}
    });
    mb.AddItem("Copyright",    KeyEvent.VK_O, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		TMessageBox mbox = new TMessageBox("PReDoc: Copyright",
					"PReDoc - an editor for proof-reading digital documents\n"+
					"Copyright © 1998 by Mark-André Hopf\n"+
					"eMail: hopf@informatik.uni-rostock.de\n"+
					"www: http://toad.home.pages.de/\n"+
					"\n"+
					"This program is free software; you can redistribute it and/or modify "+
					"it under the terms of the GNU General Public License as published by "+
					"the Free Software Foundation; either version 2 of the License, or "+
					"(at your option) any later version.\n"+
					"\n"+
					"This program is distributed in the hope that it will be useful, "+
					"but WITHOUT ANY WARRANTY; without even the implied warranty of "+
					"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "+
					"GNU General Public License for more details.\n"+
					"\n"+
					"You should have received a copy of the GNU General Public License "+
					"along with this program; if not, write to the Free Software "+
					"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA",
				  TMessageBox.OK,
				  image[IMG_LOGO]);
					mbox.show();
    	}
    });
    mb.AddItem("License",      KeyEvent.VK_S, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		Main.OpenHTML("gpl.html");
    	}
    });
    mb.EndPulldown();
   
    TDocumentView dview = new TDocumentView(this);
  }

	// initialize servers
	//-------------------------------------------------------------------------

  static Vector _server_list = null;
  
  static void AddServer(server.TServer server)
  {
  	_server_list.addElement(server);
  }
  
  public static int NumServers()
  {
  	return _server_list.size();
  }

	public static server.TServer Server(int i)
	{
		return (server.TServer) _server_list.elementAt(i);
	}

	private static void init_servers()
	{
		_server_list = new Vector();
		if (_applet==null) {
			AddServer(new server.local.TServer());
			try {
				AddServer(new server.socket.TServer("localhost", 8081));
			} catch (Exception e) { }
		} else {
			try {
				AddServer(new server.socket.TServer(
					_applet.getParameter("server"),
					Integer.parseInt(_applet.getParameter("port"))
				));
			} catch (Exception e) {
				System.out.println("failed to connect to server: "+e.getMessage());
			}
		}
	}

	public static Image GetImage(String filename) {
		if (_applet==null) {
			return Toolkit.getDefaultToolkit().getImage(filename);
		} else {
			return _applet.getImage(_applet.getCodeBase(), filename);
		}
	}

	
	// Display HTML Text
	//-------------------------------------------------------------------------
	static private THTMLFrame _html_frame = null;

	static public void OpenHTML(String file)
	{
		if (_applet==null) {
			String f = "help/"+file;
			if (_html_frame==null) {
	   		_html_frame = new THTMLFrame();
	   		_html_frame.setTitle("PReDoc: Manual");
		 		_html_frame.ReadFile(f);
	   		_html_frame.setSize(640,400);
	   		_html_frame.show();
	   	} else {
		 		_html_frame.ReadFile(f);
	   		_html_frame.show();
		 	}
		} else {
      try {
        _applet.getAppletContext().showDocument(
          new java.net.URL(_applet.getCodeBase()+"help/"+file)
        );
      } catch (Exception e) {
        System.out.println("failed to create URL: "+e.getMessage());
      }
		}
	}
	
	// create rectangle from coordinates
	static Rectangle CreateRectangleCoord(int x1, int y1, int x2, int y2)
	{
		if (x1>x2) { int a = x1; x1 = x2; x2 = a; }
		if (y1>y2) { int a = y1; y1 = y2; y2 = a; }
		return new Rectangle(x1, y1, x2-x1+1, y2-y1+1);
	}

	// move me to common/Draw:	
	static void DrawRectangle(Graphics pen, Rectangle r) {
		pen.drawRect(r.x, r.y, r.width, r.height);
	}
}
