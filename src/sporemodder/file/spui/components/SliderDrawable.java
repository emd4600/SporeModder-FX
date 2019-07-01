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
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class SliderDrawable extends IDrawable {
	public static final int IMAGE_BACKGROUND = 0;
	public static final int IMAGE_THUMB = 1;
	public static final int IMAGE_THUMB_CONTAINER = 2;
	
	private final ISporeImage[] images = new ISporeImage[3];
	
	public void paint(IWindow window, SpuiViewer viewer) {
		WinSlider slider = (WinSlider) window;
		SPUIRectangle area = window.getRealArea();
		
		if (images[IMAGE_BACKGROUND] != null) {
			ISporeImage.drawImageTiled(viewer.getGraphicsContext2D(), images[IMAGE_BACKGROUND], area, window.getShadeColor());
		}
		
		if (images[IMAGE_THUMB_CONTAINER] != null) {
			Vector2 imageDim = getDimensions(IMAGE_THUMB_CONTAINER);
			Borders scaleArea = new Borders();
			SPUIRectangle sliderArea = new SPUIRectangle();
			
			if (slider.orientation == SporeUserInterface.HORIZONTAL) {
				scaleArea.left = scaleArea.right = 0.5f - (0.5f - imageDim.getX());
				
				sliderArea.x1 = area.x1;
				sliderArea.y1 = area.y1 + (area.getHeight() - imageDim.getY()) / 2.0f;
				sliderArea.setWidth(area.getWidth());
				sliderArea.setHeight(imageDim.getY());
			} else {
				scaleArea.top = scaleArea.bottom = 0.5f - (0.5f - imageDim.getY());
				
				sliderArea.x1 = area.x1 + (area.getWidth() - imageDim.getX()) / 2.0f;
				sliderArea.y1 = area.y1;
				sliderArea.setWidth(imageDim.getY());
				sliderArea.setHeight(area.getWidth());
			}
			
			ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), images[IMAGE_THUMB_CONTAINER], false, sliderArea, scaleArea, window.getShadeColor());
		}
		
		if (images[IMAGE_THUMB] != null) {
			SPUIRectangle thumbArea = slider.getThumb().getRealArea();
			ISporeImage.drawImageTiled(viewer.getGraphicsContext2D(), images[IMAGE_THUMB], ISporeImage.getTileArea(slider.getThumb(), images[IMAGE_THUMB], 4), thumbArea, window.getShadeColor());
		} else {
			new ButtonDrawableStandard().paint(slider.getThumb(), viewer);
		}
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		if (images[imageIndex] == null) return super.getDimensions(imageIndex);
		else return new Vector2(images[imageIndex].getWidth(), images[imageIndex].getHeight());
	}
}
