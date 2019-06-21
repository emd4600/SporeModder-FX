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

package sporemodder.view.ribbons.project;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.dialogs.ProjectSettingsUI;

public class OtherProjectRibbonUI implements Controller, UIUpdateListener {
	
	@FXML
	private Node mainNode;
	
	@FXML
	private Button btnSettings;
	
	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML
	protected void initialize() {
		
		UIManager ui = UIManager.get();
		
		btnSettings.setGraphic(ui.loadIcon("config.png", 0, 32, true));
		
		btnSettings.setOnAction((event) -> {
			ProjectSettingsUI.show(ProjectManager.get().getActive());
		});
		
		UIManager.get().addListener(this);
	}

	@Override
	public void onUIUpdate(boolean isFirstUpdate) {
		
		btnSettings.setDisable(ProjectManager.get().getActive() == null);
	}
}
