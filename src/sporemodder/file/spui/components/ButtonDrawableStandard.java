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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class ButtonDrawableStandard extends IDrawable {
	
	protected ISporeImage image;
	protected boolean tileable;
	
	protected SPUIRectangle getSourceArea(IWindow window) {
		return ISporeImage.getTileArea(window, image, 4);
	}

	public void paint(IWindow window, SpuiViewer viewer) {
		if (image != null) {
			SPUIRectangle sourceBounds = ISporeImage.getTileArea(window, image, 4);

			if (tileable) {
				ISporeImage.drawImageSliced(viewer.getGraphicsContext2D(), image, false, sourceBounds, window.getRealArea(),
						new Borders(0.333f, 0.333f, 0.333f, 0.333f));
			} else {
				ISporeImage.drawImage(viewer.getGraphicsContext2D(), image, false, sourceBounds, window.getRealArea());
			}
		}
		else {
			SPUIRectangle bounds = window.getRealArea();
			double width = bounds.getWidth();
			double height = bounds.getHeight();
			double x = bounds.x1;
			double y = bounds.y1;
			
			GraphicsContext graphics = viewer.getGraphicsContext2D();
			
			graphics.setFill(Color.rgb(0xEC, 0xE9, 0xD8));
			graphics.fillRect(x + 2, y + 2, width - 4, height - 4);
			
			graphics.setLineWidth(1.0);
			
			graphics.setStroke(Color.rgb(0xF1, 0xEF, 0xE2));
			graphics.strokeRect(x+1, y+1, width-3, 1);
			graphics.strokeRect(x+1, y+2, 1, height-4);
			
			graphics.setStroke(Color.rgb(0xAC, 0xA8, 0x99));
			graphics.strokeRect(x+width-2, y+1, 1, height - 2);
			graphics.strokeRect(x+1, y+height-2, width-3, 1);
			
			graphics.setStroke(Color.rgb(0xFF, 0xFF, 0xFF));
			graphics.strokeRect(x, y, width - 1, 1);
			graphics.strokeRect(x, y+1, 1, height - 2);
			
			graphics.setStroke(Color.rgb(0x71, 0x6F, 0x64));
			graphics.strokeRect(x+width-1, y, 1, height);
			graphics.strokeRect(x, y+height-1, width-1, 1);
		}
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		if (image == null) return super.getDimensions(imageIndex);
		return new Vector2(image.getWidth() / 4, image.getHeight());
	}
}
