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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import common.*;
import draw.*;

public class TToolButton
	extends TFatRadioButton
{
	TTool _tool;
	TDrawingArea _drawingarea;

	public TToolButton(TRadioState state, 
										 Image image, 
										 TTool tool, 
										 TDrawingArea area)
	{
		super(state, image);
		_tool = tool;
		_drawingarea = area;
	}
	
	public void changed()
	{
		if (IsDown()) {
			_drawingarea.SetTool(_tool);
		}
	}
}
