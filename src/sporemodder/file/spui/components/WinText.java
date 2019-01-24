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
import sporemodder.file.spui.SpuiViewer.PaintEvent;
import sporemodder.file.spui.StyleSheetInstance;

public class WinText extends WindowBase {

	private final Borders textBorder = new Borders();
	private Color textColor = Color.BLACK;
	
	@Override protected void paintComponent(PaintEvent event) {
		super.paintComponent(event);
		
		String str = getCaption().toString();
		if (!str.isEmpty()) {
			StyleSheetInstance.paintText(event.getViewer(), getTextFont(), str, textColor, getRealArea(), textBorder);
		}
	}
}
