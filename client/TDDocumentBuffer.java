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

import common.TState;
import common.TMessageBox;

/*
 * This is a singleton storing all loaded documents.
 */
class TDDocumentBuffer
	extends TState
{
	public final static int DOCUMENT_ADDED = 1;
	public final static int DOCUMENT_REMOVED = 2;

  Vector _documents;
  TDDocumentBuffer()
  {
    _documents = new Vector();
  }

  void AddDocument(TDDocument document)
  {
  	// current implementation can only handle one doc at a time
  	if (_documents.size()>0 && !RemoveAll())
  		return;

  	if (Main.debug_mvc)
	    System.out.println("DocumentBuffer.AddDocument: notifying");
    _documents.addElement(document);
    Notify(DOCUMENT_ADDED);
  }

	boolean RemoveAll() {
		int i, n;
		n = _documents.size();
		for(i=0; i<n; i++) {
			TDDocument d = (TDDocument)_documents.elementAt(i);
			if (d._modified) {
				TMessageBox mbox = new TMessageBox(
					"STOP",
					"Concerns document \"" + d.Filename() +"\"\n\n" +
					"Your modifications haven't been stored and when you continue "+
					"all your effort gets lost forever.",
					TMessageBox.OK | TMessageBox.CANCEL,
					Main.image[Main.IMG_ICON_HAND]);
				mbox.show();
				if (mbox.Result()!=TMessageBox.OK)
					return false;
			}
		}
		// here's a security check missing before we remove unsafed corrections
		_documents.removeAllElements();
		Notify(DOCUMENT_REMOVED);
		return true;
	}

	/**
	 * Number of Documents
	 */
  int Size()
  {
    return _documents.size();
  }

	/**
	 * Get document <VAR>i</VAR> where 0 &lt; <VAR>i</VAR> &lt; Size().
	 */
  TDDocument Document(int i)
  {
    return (TDDocument)_documents.elementAt(i);
  }
}
