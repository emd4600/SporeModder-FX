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

import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

// Not an interface or abstract because non-implemented drawables will use this class
public class IDrawable extends SpuiElement {
	public void paint(IWindow window, SpuiViewer viewer) {
	}
	
	public Vector2 getDimensions(int imageIndex) {
		return new Vector2();
	}
}
