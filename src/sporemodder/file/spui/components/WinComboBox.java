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

import javafx.event.Event;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.util.Vector2;

public class WinComboBox extends WindowBase {
	
	public static final int COMBOBOX_OUTLINE = 0x00000001;
	
	public static final int ALIGNMENT_LEFT = 0;
	public static final int ALIGNMENT_RIGHT = 1;
	
	public int alignment = ALIGNMENT_LEFT;
	public int comboBoxFlags;

	private final SubWindow<WinComboBox> pulldownButton = new SubWindow<WinComboBox>(this) {
		public void layout(SPUIRectangle area, IWindow parent) {
			Vector2 dim = new Vector2(ComboBoxDrawable.DEFAULT_BUTTON_SIZE, ComboBoxDrawable.DEFAULT_BUTTON_SIZE);
			if (getFillDrawable() != null) {
				dim = getFillDrawable().getDimensions(ComboBoxDrawable.IMAGE_BUTTON);
			}
			
			area.x1 = 0;
			area.y1 = (parent.getRealArea().getHeight() - dim.getY()) / 2.0f;
			if (alignment == ALIGNMENT_RIGHT) {
				area.x1 = parent.getRealArea().getWidth() - dim.getX();
			}
			area.setWidth(dim.getX());
			area.setHeight(dim.getY());
		}
	};
	
	public WinComboBox() {
		super();
		setFlag(8, true);  // IS_EDITABLE
	}
	
	@Override public boolean handleEvent(SpuiViewer viewer, Event event) {
		boolean result = pulldownButton.handleEvent(viewer, event);
		if (result) return true;
		else return super.handleEvent(viewer, event);
	}
	
	public SubWindow<WinComboBox> getPulldownButton() {
		return pulldownButton;
	}
}
