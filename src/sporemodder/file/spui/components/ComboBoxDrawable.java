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
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class ComboBoxDrawable extends IDrawable {

	public static final int IMAGE_BACKGROUND = 0;
	public static final int IMAGE_SELECTION_BACKGROUND = 1;
	public static final int IMAGE_BUTTON = 2;
	
	public static final float DEFAULT_BUTTON_SIZE = 16.0f;
	
	private final ISporeImage[] images = new ISporeImage[3];
	
	public void paint(IWindow window, SpuiViewer viewer) {
		WinComboBox comboBox = (WinComboBox) window;
		SPUIRectangle area = window.getRealArea();
		
		if ((comboBox.comboBoxFlags & WinComboBox.COMBOBOX_OUTLINE) != 0) {
			int state = comboBox.getState() | comboBox.getPulldownButton().getState();
			
			if ((state & IWindow.STATE_FLAG_HOVER) != 0 ||
					(state & IWindow.STATE_FLAG_CLICKED) != 0) {
				
				viewer.getGraphicsContext2D().setStroke(Color.BLACK);
				viewer.getGraphicsContext2D().strokeRect(area.x1, area.y1, area.getWidth(), area.getHeight());
			}
		}
		
		if (images[IMAGE_SELECTION_BACKGROUND] != null) {
			ISporeImage.drawImage(viewer.getGraphicsContext2D(), images[IMAGE_SELECTION_BACKGROUND], area);
		}
		
		if (images[IMAGE_BUTTON] != null) {
			ISporeImage.drawImage(viewer.getGraphicsContext2D(), images[IMAGE_BUTTON], false, 
					ISporeImage.getTileArea(comboBox.getPulldownButton(), images[IMAGE_BUTTON], 4), 
					comboBox.getPulldownButton().getRealArea());
		}
		
		int drawText;
		//TODO
	}
	
	@Override public Vector2 getDimensions(int imageIndex) {
		if (images[imageIndex] == null) return super.getDimensions(imageIndex);
		else return new Vector2(images[imageIndex].getWidth(), images[imageIndex].getHeight());
	}
}
