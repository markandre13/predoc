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

package draw;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import common.*;
import draw.*;

public class TDrawingData
{
	public Vector _data;
	
	public TDrawingData()
	{
		_data = new Vector();
	}

	public void Store(DataOutputStream out, int version)
		throws java.io.IOException
	{
		int n = _data.size();
		out.writeInt(n);
		for(int i=0; i<n; i++) {
			TFigure.SStore(Figure(i), out, version);
		}
	}
	
	public void Restore(DataInputStream in, int version)
		throws java.io.IOException
	{
		int n = in.readInt();
		for(int i=0; i<n; i++) {
			Add(TFigure.SRestore(in, version));
		}
	}
	
	public void Add(TFigure fig)
	{
		_data.addElement(fig);
	}
	
	public int Size()
	{
		return _data.size();
	}

	public TFigCorrection FigCorrection()
	{
		for(int i=0; i<_data.size(); i++) {
			if (_data.elementAt(i) instanceof TFigCorrection)
				return (TFigCorrection)_data.elementAt(i);
		}
		return null;
	}

	/**
	*	Remove a figure unless it's TFigCorrection which must always be there.
	*/
	public void Remove(TFigure fig)
	{
		if(!(fig instanceof TFigCorrection))
			_data.removeElement(fig);
	}
	
	public TFigure Figure(int n)
	{
		return (TFigure)_data.elementAt(n);
	}
}
