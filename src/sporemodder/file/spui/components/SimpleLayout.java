/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.spui.components;

import sporemodder.file.spui.SPUIRectangle;

public class SimpleLayout extends ILayoutStyle {
	
	/**
	 * If true, the right side of the component will be aligned with its parent right side. 
	 */
	public static final int FLAG_RIGHT = 0x8;
	/**
	 * If true, the left side of the component will be aligned with its parent right side (therefore respecting the original width). 
	 */
	public static final int FLAG_LEFT = 0x4;
	/**
	 * If true, the bottom side of the component will be aligned with its parent bottom side. 
	 */
	public static final int FLAG_BOTTOM = 0x2;
	/**
	 * If true, the top side of the component will be aligned with its parent bottom side (therefore respecting the original height).  
	 */
	public static final int FLAG_TOP = 0x1;
	
	private int anchor;

	public SimpleLayout() {

	}

	public SimpleLayout(int anchor) {
		this.anchor = anchor;
	}

	@Override
	public void applyLayout(SPUIRectangle destArea, SPUIRectangle parentArea) {
		if ((anchor & FLAG_RIGHT) != 0) {
			destArea.x2 += parentArea.getWidth();
			
			if ((anchor & FLAG_LEFT) == 0) {
				destArea.x1 += parentArea.getWidth();
			}
		}
		
		if ((anchor & FLAG_BOTTOM) != 0) {
			destArea.y2 += parentArea.getHeight();
			
			if ((anchor & FLAG_TOP) == 0) {
				destArea.y1 += parentArea.getHeight();
			}
		}
	}

}
