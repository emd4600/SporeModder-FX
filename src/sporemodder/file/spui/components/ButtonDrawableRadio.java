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
import sporemodder.util.Vector2;

public class ButtonDrawableRadio extends ButtonDrawableStandard {
	
	@Override protected SPUIRectangle getSourceArea(IWindow window) {
		//TODO it has 6 tiles, but we aren't using the last 2
		return ISporeImage.getTileArea(window, image, 6);
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		if (image == null) return super.getDimensions(imageIndex);
		return new Vector2(image.getWidth() / 6, image.getHeight());
	}
}
