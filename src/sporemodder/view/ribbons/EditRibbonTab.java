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

import emd4600.javafx.ribbon.Ribbon;
import emd4600.javafx.ribbon.RibbonGroup;
import emd4600.javafx.ribbon.RibbonTab;
import sporemodder.UIManager;
import sporemodder.view.ribbons.edit.EditActionsUI;
import sporemodder.view.ribbons.edit.TextActionsUI;

public class EditRibbonTab extends RibbonTabController {
	
	public static final String ID = "EDIT";
	
	private EditActionsUI editActions;
	private TextActionsUI textActions;
	
	@Override public void addTab(Ribbon ribbon) {
		tab = new RibbonTab("Edit");
		ribbon.getTabs().add(tab);
		
		editActions = UIManager.get().loadUI("ribbons/edit/EditActionsUI");
		textActions = UIManager.get().loadUI("ribbons/edit/TextActionsUI");
		
		tab.getGroups().add((RibbonGroup) editActions.getMainNode());
		tab.getGroups().add((RibbonGroup) textActions.getMainNode());
	}
	
	public EditActionsUI getEditActionsUI() {
		return editActions;
	}
	
	public TextActionsUI getTextActionsUI() {
		return textActions;
	}
}
