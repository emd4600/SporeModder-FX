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
import sporemodder.file.spui.SpuiViewer.PaintEvent;
import sporemodder.file.spui.StyleSheetInstance;
import sporemodder.util.Vector2;

public class WinButton extends WindowBase {
	
	public final Borders captionBorder = new Borders();
	public final Vector2 captionOffset = new Vector2();
	public final Color[] captionColors = new Color[8];
	
	public WinButton() {
		super();
		setFlag(FLAG_SHOW_TEXT, true);
	}
	
	@Override protected void paintComponent(PaintEvent event) {
		super.paintComponent(event);
		
		if ((getFlags() & FLAG_SHOW_TEXT) != 0) {
			int index = SporeUserInterface.getImageIndex(this);
			
			SPUIRectangle textArea = new SPUIRectangle(getRealArea());
			textArea.translate(captionOffset.getX(), captionOffset.getY());
			
			StyleSheetInstance.paintText(event.getViewer(), getTextFont(), getCaption().toString(), 
					captionColors[index], textArea, captionBorder);
		}
	}
}
