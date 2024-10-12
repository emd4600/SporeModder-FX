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
import sporemodder.EditorManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;

public class ModdingActionsUI implements Controller, UIUpdateListener {
	
	@FXML
	private Node mainNode;
	
	@FXML
	private Button btnCompare;
	@FXML
	private Button btnExploreReference;
	@FXML
	private Button btnExploreMod;
	
	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML
	protected void initialize() {
		
		UIManager ui = UIManager.get();
		
//		btnCompare.setGraphic(ui.loadIcon("compare.png", 0, 48, true));
//		btnExploreSource.setGraphic(ui.loadIcon("explore-source.png", 0, 48, true));
//		btnExploreMod.setGraphic(ui.loadIcon("explore-mod.png", 0, 48, true));
		
		btnCompare.setGraphic(ui.loadIcon("compare.png", 0, 38, true));
		btnExploreReference.setGraphic(ui.loadIcon("explore-source.png", 0, 38, true));
		btnExploreMod.setGraphic(ui.loadIcon("explore-mod.png", 0, 38, true));
		
		btnCompare.setDisable(true);
		btnExploreReference.setDisable(true);
		btnExploreMod.setDisable(true);
		
		btnCompare.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().compareItem(), "Cannot compare item.");
		});

		btnExploreReference.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().openSourceFolder(), "Cannot open reference folder.");
		});
		
		btnExploreMod.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().openModFolder(), "Cannot open mod folder.");
		});
		
		ui.addListener(this);
	}

	@Override
	public void onUIUpdate(boolean isFirstUpdate) {
		ProjectItem item = EditorManager.get().getActiveItem();
		
		if (item != null) {
			
			btnCompare.setDisable(!item.canCompareItem());
			btnExploreReference.setDisable(!item.canOpenSourceFolder());
			btnExploreMod.setDisable(!item.canOpenModFolder());
		}
		else {
			btnCompare.setDisable(true);
			btnExploreReference.setDisable(true);
			btnExploreMod.setDisable(true);
		}
	}
}
