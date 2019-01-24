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

import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class ImageDrawable extends IDrawable {
	
	public static final int TILING_NONE = 0;
	public static final int TILING_STANDARD = 1;
	public static final int TILING_EDGE = 2;
	
	private ISporeImage image;
	private final OutlineFormat imageOutline = new OutlineFormat();
	private int alignmentHorizontal = SporeUserInterface.ALIGN_CENTER;
	private int alignmentVertical = SporeUserInterface.ALIGN_MIDDLE;
	private int imageDrawableFlags;
	private int tiling;
	private float scale = 1.0f;
	
	@Override public void paint(IWindow window, SpuiViewer viewer) {
		//TODO use alignment for something
		if (image != null) {
			if (tiling == TILING_EDGE) {
				ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), image, false, window.getRealArea(), new Borders(0.333f, 0.333f, 0.333f, 0.333f));
			}
			else if (tiling == TILING_STANDARD) {
				ISporeImage.drawImageTiled(viewer.getGraphicsContext2D(), image, window.getRealArea());
			}
			else {
				ISporeImage.drawImage(viewer.getGraphicsContext2D(), image, window.getRealArea());
			}
		}
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		if (image == null) return super.getDimensions(imageIndex);
		return new Vector2(image.getWidth(), image.getHeight());
	}
}
