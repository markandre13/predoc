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

import java.util.Vector;

import java.io.*;

class TDDocument
{
  Vector _pages;			// vector of `TDPage' objects
  String _filename;		// document title
  String _corname;		// name of the correction file
  boolean _modified;	// `true' when document was modified
  server.TDocument _remote;
  
  public TDDocument(String filename, server.TDocument remote, String cor)
  {
    _filename = filename;
    _pages = new Vector();
    _remote = remote;
    _corname = cor;
    _modified = false;
    
    byte a[] = _remote.LoadCorrection(cor);
    
    try {
    	DataInputStream in = null;
    	int version = 0;
    	if (a==null) {
    		System.out.println("empty or no correction");
    	} else {
		    ByteArrayInputStream ba = new ByteArrayInputStream(a);
		    in = new DataInputStream(ba);
		    version = in.readInt();
		    int pages   = in.readInt();
		    System.out.println("version: " + version);
		    System.out.println("pages  : " + pages);
		  }

			int n = remote.NumPages();
			for(int i=0; i<n; i++) {
				TDPage page = new TDPage(remote.Page(i));
				_pages.addElement(page);
				if (in!=null) {
					try {
						TDPageCorrection correction = new TDPageCorrection();
						correction.Restore(in, version);
						page.SetCorrection(correction);
					} catch (Exception e) {
						System.out.println("failed to load correction for page "+i);
				  	System.out.println("exception: "+e.getMessage());
				  	e.printStackTrace();
				  }
				}
		  }
	  } catch (Exception e) {
	  	System.out.println("failed to load all pages");
	  	System.out.println("exception: "+e.getMessage());
	  	e.printStackTrace();
	  }
  }

	public void Save()
	{
		try {
			int n = PageCount();
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(ba);
			int version = Main.io_version;
			out.writeInt(version);		// version
			out.writeInt(n);					// number of pages
			for(int i=0; i<n; i++) {
				Page(i).Correction().Store(out, version);
			}
			_remote.SaveCorrection(_corname, ba.toByteArray());
		}
		catch(Exception e) {
			System.out.println("storage failed");
			System.out.println("exception: " + e.getMessage());
      e.printStackTrace();
      return;
		}
		_modified = false;
	}  

  public TDPage Page(int page)
  {
    if (page >= _pages.size())
      return null;
    return (TDPage)_pages.elementAt(page);
  }
  
  public int PageCount()
  {
    return _pages.size();
  }

  public String Filename()
  {
    return _filename;
  }
}
