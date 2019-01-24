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
import sporemodder.file.spui.SporeUserInterface;

public class ProportionalLayout extends ILayoutStyle {
	
	private final float[] proportions = new float[4]; 

	@Override
	public void applyLayout(SPUIRectangle destArea, SPUIRectangle parentArea) {
		double width = parentArea.getWidth();
		double height = parentArea.getHeight();
		
		destArea.x1 += Math.round(width * proportions[SporeUserInterface.LEFT]);
		destArea.y1 += Math.round(height * proportions[SporeUserInterface.TOP]);
		destArea.x2 += Math.round(width * proportions[SporeUserInterface.RIGHT]);
		destArea.y2 += Math.round(height * proportions[SporeUserInterface.BOTTOM]);
	}

}
