/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

package sporemodder.view.ribbons;

import emord.javafx.ribbon.Ribbon;
import emord.javafx.ribbon.RibbonGroup;
import emord.javafx.ribbon.RibbonTab;
import sporemodder.UIManager;

public class UtilRibbonTab {
	
	public static void addTab(Ribbon ribbon) {
		RibbonTab tab = new RibbonTab("Utilities");
		ribbon.getTabs().add(tab);
		
		tab.getGroups().add((RibbonGroup) UIManager.get().loadUI("ribbons/util/NumberConverterUI").getMainNode());
		tab.getGroups().add((RibbonGroup) UIManager.get().loadUI("ribbons/util/HashConverterUI").getMainNode());
	}

}
