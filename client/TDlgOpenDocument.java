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
import tree.*;

class TDlgOpenDocument
	extends Dialog
	implements ActionListener
{
	server.TDirectory _directory;
	TTreeView _tv;
	TPushButton btn_open, btn_new;
	
	public TDlgOpenDocument()
	{
		super(Main.wnd, "Open Document");

		setModal(true);
		setSize(360,300);
		
		TFormLayout form = new TFormLayout(this);
		form.nBorderOverlap=-10;


		// create buttons
		//------------------------------------
		TPushButton btn1, btn2, btn3, btn4;
		btn_open = btn1 = new TPushButton("Open");
			btn1.setActionCommand("o");
			btn1.addActionListener(this);
			btn1.setSize(80,25);
		btn_new = btn2 = new TPushButton("New");
			btn2.setActionCommand("n");
			btn2.addActionListener(this);
			btn2.setSize(80,25);
			btn2.SetEnabled(false);
		btn3 = new TPushButton("Cancel");
			btn3.setActionCommand("c");
			btn3.addActionListener(this);
			btn3.setSize(80,25);
		btn4 = new TPushButton("Help");
			btn4.setActionCommand("h");
			btn4.addActionListener(this);
			btn4.setSize(80,25);
			
		// setup tree
		//------------------------------------
		TTreeConstructor tc = new TTreeConstructor();

		int n = Main.NumServers();
		for(int i=0; i<n; i++) {
			tc.BgnSub(new TTNServer(Main.Server(i)));
			tc.EndSub();
		}
		
		TTreeView tv = new TTreeView(tc.Top()) {
			public void doubleClick() {
				Open();
			}
		};
		_tv = tv;
		tv.setSize(200,300);

		// arrange elements
		//------------------------------------
  	form.Attach(tv, TFormLayout.LEFT | TFormLayout.TOP | TFormLayout.BOTTOM );
  	form.Attach(tv, TFormLayout.RIGHT, btn1);
  	
  	form.Attach(btn1, TFormLayout.TOP | TFormLayout.RIGHT);
  	form.Attach(btn2, TFormLayout.TOP, btn1);
  	form.Attach(btn2, TFormLayout.RIGHT);
  	form.Attach(btn3, TFormLayout.TOP, btn2);
  	form.Attach(btn3, TFormLayout.RIGHT);
  	form.Attach(btn4, TFormLayout.TOP, btn3);
  	form.Attach(btn4, TFormLayout.RIGHT);
  }
  
  protected void Open()
  {
 		TTreeNode tn = _tv.Selection();
 		if (tn!=null && tn instanceof TTNCorrection) {
 			TTNCorrection tnc = (TTNCorrection)tn;
			TDDocument doc = new TDDocument(tnc.doc_title,
																			tnc.doc_remote,
																			tnc.cor_title);
			Main.documents.AddDocument(doc);
	  	setVisible(false);
	  }
  }
  
  protected void New()
  {
  	TMessageBox msg = new TMessageBox(
  		"STOP: Function Not Implemented",
  		"Creation of new files isn't possible yet.",
  		TMessageBox.OK,
  		Main.image[Main.IMG_ICON_HAND]);
  	msg.show();
  }
  
  public void actionPerformed(ActionEvent e)
  {
  	String cmd = e.getActionCommand();
  	if (cmd=="o") {
  		Open();
  	} else if (cmd=="n") {
  		New();
 		} else if (cmd=="c") {
	  	setVisible(false);
  	} else if (cmd=="h") {
  		Main.OpenHTML("filedialog.html");
  	}
  }

class TTNServer
	extends TTNImageString
{
	server.TServer _server;
	server.TDirectory _root_dir;
	
	public TTNServer(server.TServer server)
	{
		super(
			server.Name(), 
			server instanceof server.local.TServer ? 
				Main.image[Main.IMG_LOCAL_SERVER]
			: Main.image[Main.IMG_REMOTE_SERVER]
		);
		SetClosed(true);
		_server = server;
	}
	
	public boolean HasChildren()
	{
		return true;
	}

	public void SetClosed(boolean b)
	{
		boolean fetched = super.HasChildren();
		super.SetClosed(b);
		if (!IsClosed() && !fetched) {
//			System.out.println("need directory");
	  	_root_dir = _server.RootDirectory();
//	  	System.out.println("got root directory, reading entries");
	  	String[] lst;
	  	lst = _root_dir.DirEntries();
//	  	System.out.println("read entries");
	  	if (lst!=null) {
		   	for(int i=0; i<lst.length; i++) {
		   		Add(new TTNDirectory(_root_dir, lst[i],i ));
		  	}
		  }
	  	lst = _root_dir.DocEntries();
	  	if (lst!=null) {
		   	for(int i=0; i<lst.length; i++) {
		   		Add(new TTNDocument(_root_dir, lst[i], i));
		   	}
	  	}
		}
	}
	
	public void selected() {
		btn_open.SetEnabled(false);
		btn_new.SetEnabled(false);
	}

	public boolean IsSelectable() {
		return true;
	}
}

class TTNDocument
	extends TTNImageString
{
	long _doc_id;
	server.TDirectory _dir;
	server.TDocument _doc;
	boolean _haveChildren;
	
	public TTNDocument(server.TDirectory dir, String name, long doc_id)
	{
		super(name, Main.image[Main.IMG_DOCUMENT]);
		super.SetClosed(true);
		_haveChildren = true;
		_dir = dir;
		_doc_id = doc_id;
		_doc = null;
	}
	
	public boolean HasChildren()
	{
		return _haveChildren;
	}
	
	public void SetClosed(boolean b)
	{
		super.SetClosed(b);
		if (!IsClosed() && _doc==null) {
			// System.out.println("need corrections");
			_doc = _dir.Document((int)_doc_id);
			if (_doc==null)
				return;
			String[] lst;
			lst = _doc.Corrections();
			if (lst==null || lst.length==0) {
				_haveChildren=false;
				return;
			}
			for(int i=0; i<lst.length; i++) {
				Add(new TTNCorrection(lst[i], _s, _doc));
			}
		}
	}

	public void selected() {
		btn_open.SetEnabled(false);
		btn_new.SetEnabled(true);
	}

	public boolean IsSelectable() {
		return true;
	}
}

class TTNDirectory
	extends TTNImageString
{
	server.TDirectory _directory;
	server.TDirectory _parent;
	long _dir_id;
	boolean _haveChildren;

	public TTNDirectory(server.TDirectory parent, String name, long dir_id)
	{
		super(name, Main.image[Main.IMG_DIRECTORY]);
		_parent = parent;
		_directory = null;
		_dir_id = dir_id;
		_haveChildren=true;
	}
	
	public void SetClosed(boolean b)
	{
		super.SetClosed(b);
		if (!IsClosed() && _directory==null) {
			_directory = _parent.Directory((int)_dir_id);
			if (_directory==null) 
				return;
				
			String[] lst;
			lst = _directory.DirEntries();
			if (lst!=null) {
				for(int i=0; i<lst.length; i++) {
					Add(new TTNDirectory(_directory, lst[i],i ));
				}
			}
			lst = _directory.DocEntries();
			if (lst!=null) {
				for(int i=0; i<lst.length; i++) {
					Add(new TTNDocument(_directory, lst[i], i));
				}
			}
		}
	}

	public void selected() {
		btn_open.SetEnabled(false);
		btn_new.SetEnabled(false);
	}
	
	public boolean HasChildren()
	{
		return _haveChildren;
	}

	public boolean IsSelectable() {
		return true;
	}

}

class TTNCorrection
	extends TTNImageString
{
	String doc_title;
	server.TDocument doc_remote;
	String cor_title;

	public TTNCorrection(String cor, String doc, server.TDocument remote)
	{
		super(cor, Main.image[Main.IMG_CORRECTION]);
		cor_title = cor;
		doc_title = doc;
		doc_remote = remote;
	}
	
	public void selected() {
		btn_open.SetEnabled(true);
		btn_new.SetEnabled(false);
	}
}

}