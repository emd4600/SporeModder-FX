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

import sporemodder.file.spui.RLEHitMask;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class StdDrawable extends IDrawable {
	public static final int STRETCH_IMAGE = 1;
	public static final int STRETCH_CENTER = 2;
	public static final int TILE_IMAGE = 3;
	public static final int TILE_CENTER = 4;
	
	private final ISporeImage[] image = new ISporeImage[8];
	private RLEHitMask hitMask;  //TODO use this to detect hits
	protected int scaleType;
	private final Vector2 scaleFactor = new Vector2(1.0f, 1.0f);
	protected final Borders scaleArea = new Borders();
	// outline format as well, but we don't use it
	
	@Override public void paint(IWindow window, SpuiViewer viewer) {
		ISporeImage img = image[SporeUserInterface.getImageIndex(window)];
		if (img == null) img = image[0];
		if (img != null) {
			if (scaleType == STRETCH_IMAGE) {
				ISporeImage.drawImage(viewer.getGraphicsContext2D(), img, window.getRealArea(), window.getShadeColor());
			}
			else if (scaleType == STRETCH_CENTER) {
				ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), img, false, window.getRealArea(), scaleArea, window.getShadeColor());
			}
			else if (scaleType == TILE_IMAGE) {
				ISporeImage.drawImageTiled(viewer.getGraphicsContext2D(), img, window.getRealArea(), window.getShadeColor());
			}
			else if (scaleType == TILE_CENTER) {
				ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), img, true, window.getRealArea(), scaleArea, window.getShadeColor());
			}
		}
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		if (image[imageIndex] == null) return super.getDimensions(imageIndex);
		return new Vector2(image[imageIndex].getWidth(), image[imageIndex].getHeight());
	}

	public int getScaleType() {
		return scaleType;
	}

	public void setScaleType(int scaleType) {
		this.scaleType = scaleType;
	}

	public Vector2 getScaleFactor() {
		return scaleFactor;
	}

	public Borders getScaleArea() {
		return scaleArea;
	}
}
