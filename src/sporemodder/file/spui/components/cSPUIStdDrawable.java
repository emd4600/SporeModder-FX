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
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class cSPUIStdDrawable extends StdDrawable {
	private cSPUIStdDrawableImageInfo image0;
	private cSPUIStdDrawableImageInfo image1;
	private cSPUIStdDrawableImageInfo image2;
	private cSPUIStdDrawableImageInfo image3;
	private cSPUIStdDrawableImageInfo image4;
	private cSPUIStdDrawableImageInfo image5;
	private cSPUIStdDrawableImageInfo image6;
	private cSPUIStdDrawableImageInfo image7;

	public cSPUIStdDrawableImageInfo getImage(int index) {
		switch (index) {
		case 0: return image0;
		case 1: return image1;
		case 2: return image2;
		case 3: return image3;
		case 4: return image4;
		case 5: return image5;
		case 6: return image6;
		case 7: return image7;
		default: return null;
		}
	}

	public void setImage(int index, cSPUIStdDrawableImageInfo info) {
		switch (index) {
			case 0: image0 = info; break;
			case 1: image1 = info; break;
			case 2: image2 = info; break;
			case 3: image3 = info; break;
			case 4: image4 = info; break;
			case 5: image5 = info; break;
			case 6: image6 = info; break;
			case 7: image7 = info; break;
			default: break;
		}
	}
	
	@Override public void paint(IWindow window, SpuiViewer viewer) {
		int index = SporeUserInterface.getImageIndex(window);
		cSPUIStdDrawableImageInfo image = getImage(index);
		if (image == null) image = image0;
		
		if (image != null) {
			SPUIRectangle windowArea = window.getRealArea();
			
			SPUIRectangle area = new SPUIRectangle();
			area.x1 = image.backgroundOffset.getX() + windowArea.x1;
			area.y1 = image.backgroundOffset.getY() + windowArea.y1;
			// Are we sure it works like this? Maybe it tiles it
			area.x2 = area.x1 + image.backgroundScale.getX() * windowArea.getWidth();
			area.y2 = area.y1 + image.backgroundScale.getY() * windowArea.getHeight();
			
			if (image.backgroundImage != null) {
				//ISporeImage.drawImage(viewer.getGraphicsContext2D(), image.backgroundImage, area, image.backgroundColor);
				Color newShadeColor = window.getShadeColor();
				if ((image != null) && (image.backgroundColor != null))
					newShadeColor = ISporeImage.multiplyColor(image.backgroundColor, window.getShadeColor());
					/*newShadeColor = new Color(window.getShadeColor().getRed() / image.backgroundColor.getRed(),
							window.getShadeColor().getGreen() / image.backgroundColor.getGreen(),
							window.getShadeColor().getBlue() / image.backgroundColor.getBlue(),
							window.getShadeColor().getOpacity() / image.backgroundColor.getOpacity());*/
				
				if (scaleType == STRETCH_IMAGE) {
					ISporeImage.drawImage(viewer.getGraphicsContext2D(), image.backgroundImage, area, newShadeColor);
				}
				else if (scaleType == STRETCH_CENTER) {
					ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), image.backgroundImage, false, area, scaleArea, newShadeColor);
				}
				else if (scaleType == TILE_IMAGE) {
					ISporeImage.drawImageTiled(viewer.getGraphicsContext2D(), image.backgroundImage, area, newShadeColor);
				}
				else if (scaleType == TILE_CENTER) {
					ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), image.backgroundImage, true, area, scaleArea, newShadeColor);
				}
			}
			
			ISporeImage iconImage = image.iconImage;
			
			if (iconImage == null) {
				// Sometimes only the first one is specified
				if (image0 != null) iconImage = image0.iconImage;
			}
			
			if (iconImage != null) {
				SPUIRectangle iconArea = new SPUIRectangle();
				iconArea.setWidth(iconImage.getWidth() * image.iconScale.getX());
				iconArea.setHeight(iconImage.getHeight() * image.iconScale.getY());
				
				float h = (area.getWidth() - iconArea.getWidth()) / 2;
				float v = (area.getHeight() - iconArea.getHeight()) / 2;
				iconArea.translateX(area.x1 + h + image.iconOffset.getX());
				iconArea.translateY(area.y1 + v + image.iconOffset.getY());
				
				ISporeImage.drawImage(viewer.getGraphicsContext2D(), iconImage, iconArea, image.iconColor);
			}
		}
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		cSPUIStdDrawableImageInfo info = getImage(imageIndex);
		if (info == null || info.backgroundImage == null) return super.getDimensions(imageIndex);
		return new Vector2(info.backgroundImage.getWidth(), info.backgroundImage.getHeight());
	}
}
