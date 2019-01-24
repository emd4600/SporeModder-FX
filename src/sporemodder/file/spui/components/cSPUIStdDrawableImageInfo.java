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
import sporemodder.util.Vector2;

public class cSPUIStdDrawableImageInfo extends SpuiElement {

	public static final int ICON_IMAGE_SIZE = 0;
	public static final int ICON_WINDOW_SIZE = 1;
	
	public static final int SHADOW_NONE = 0;
	public static final int SHADOW_FULL = 1;
	public static final int SHADOW_BACKGROUND = 2;
	public static final int SHADOW_ICON = 3;
	
	public ISporeImage backgroundImage;
	public Color backgroundColor = Color.WHITE;
	public ISporeImage iconImage;
	public Color iconColor = Color.WHITE;
	public int iconDrawMode = ICON_WINDOW_SIZE;
	public final Vector2 iconScale = new Vector2(1.0f, 1.0f);
	public final Vector2 iconOffset = new Vector2();
	public final Vector2 backgroundScale = new Vector2(1.0f, 1.0f);
	public final Vector2 backgroundOffset = new Vector2();
	// 4 properties related with shadows/outline formats, won't use them
	
}
