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

package tree;

import java.awt.*;

public class TTNImageString
	extends TTreeNode
{
	public String _s;
	Image _i;
	public TTNImageString(String s, Image i)
	{
		_s = s; _i = i;
	}
	public void paint(Graphics pen)
	{
		pen.drawImage(_i, 2, 1, null);
		pen.setColor(new Color(0,0,0));
		pen.drawString(_s,16+2,12);
	}
}
