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
import sporemodder.view.editors.spui.SpuiLayoutWindow;

public abstract class ILayoutStyle extends IWinProc {

	public abstract void applyLayout(SPUIRectangle destArea, SPUIRectangle parentArea);
	
	@Override public boolean handleEvent(SpuiViewer viewer, IWindow window, Event event) {
		if (event.getEventType() == SpuiViewer.LAYOUT_EVENT && window.getParent() != null && !(window.getParent() instanceof SpuiLayoutWindow)) {
			applyLayout(window.getRealArea(), window.getParent().getRealArea());
		}
		// If we return true, the event will stop propagating
		return false;
	}
}
