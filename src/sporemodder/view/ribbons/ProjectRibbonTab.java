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
package sporemodder.view.ribbons;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import sporemodder.UIManager;
import sporemodder.view.ribbons.project.BasicActionsUI;
import sporemodder.view.ribbons.modandgit.ModAndGitActionsUI;
import sporemodder.view.ribbons.project.ModdingActionsUI;
import sporemodder.view.ribbons.project.OtherProjectRibbonUI;

public class ProjectRibbonTab extends RibbonTabController {
	
	public static final String ID = "PROJECT";
	
	private BasicActionsUI basicActions;
	private ModdingActionsUI moddingActions;
	private OtherProjectRibbonUI otherProjectRibbon;
	
	public void addTab(Ribbon ribbon) {
		tab = new RibbonTab("Project");
		ribbon.getTabs().add(tab);
		
		basicActions = UIManager.get().loadUI("ribbons/project/BasicActionsUI");
		moddingActions = UIManager.get().loadUI("ribbons/project/ModdingActionsUI");
		otherProjectRibbon = UIManager.get().loadUI("ribbons/project/OtherProjectRibbonUI");

		tab.getGroups().add((RibbonGroup) basicActions.getMainNode());
		tab.getGroups().add((RibbonGroup) moddingActions.getMainNode());
		tab.getGroups().add((RibbonGroup) otherProjectRibbon.getMainNode());
	}

	public BasicActionsUI getBasicActionsUI() {
		return basicActions;
	}

	public ModdingActionsUI getModdingActionsUI() {
		return moddingActions;
	}

	public OtherProjectRibbonUI getOtherProjectRibbonUI() {
		return otherProjectRibbon;
	}
}
