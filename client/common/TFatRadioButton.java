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
import java.awt.event.*;
import common.*;

public class TFatRadioButton
	extends TButtonBase
{
	TRadioState _state;
	int _value;

	public TFatRadioButton(TRadioState state, String label)
	{
		_Init();
		_state = state;
		_label = label;
	}

	public TFatRadioButton(TRadioState state, Image image)
	{
		_Init();
		_state = state;
		_image = image;		
	}

	public TFatRadioButton(TRadioState state, Image image, int value)
	{
		_Init();
		_state = state;
		_image = image;
		_value = value;
	}

	private void _Init()
	{
		_state = null;
		_label = null;
		_image = null;
		_value = -1;
		addMouseListener(
			new MouseAdapter() {
				public void mousePressed(MouseEvent e)
				{
					if (!IsEnabled()) return;
					requestFocus();
					activated();
				}
			}
		);
	}

	void activated() {
		_state.SetCurrent(this);
	}

	public int Value() { return _value; }
	
	public boolean IsDown()
	{
		return _state.Current()==this;
	}
	
	public void changed()
	{
	}
}
