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

import javafx.scene.paint.Color;
import sporemodder.file.spui.SpuiElement;

public class OutlineFormat extends SpuiElement {
	public int size;
	public int strength;
	public int quality = 2;
	public float offsetX;
	public float offsetY;
	public float sizeX = 1.0f;
	public float sizeY = 1.0f;
	public float smoothness = 0.01f;
	public float saturation = 0.5f;
	public Color color = Color.rgb(0, 0, 0, 0);
}
