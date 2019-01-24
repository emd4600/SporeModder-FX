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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import sporemodder.EditorManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;

public class BasicActionsUI implements Controller, UIUpdateListener {
	
	@FXML
	private Node mainNode;
	
	@FXML
	private Button btnNewFile;
	@FXML
	private Button btnNewFolder;
	@FXML
	private Button btnRename;
	@FXML
	private Button btnRemove;
	@FXML
	private Button btnModify;
	@FXML
	private Button btnDuplicate;
	@FXML
	private Button btnImportExternal;
	@FXML
	private Button btnRefresh;
	
	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML
	protected void initialize() {
		
		UIManager ui = UIManager.get();
		
		btnNewFile.setGraphic(ui.loadIcon("new-file.png", 0, 48, true));
		btnNewFolder.setGraphic(ui.loadIcon("new-folder.png", 0, 48, true));
		btnRename.setGraphic(ui.loadIcon("rename-item.png", 0, 48, true));
		btnRefresh.setGraphic(ui.loadIcon("refresh.png", 0, 48, true));
		btnImportExternal.setGraphic(ui.loadIcon("import-external.png", 0, 48, true));
		btnRemove.setGraphic(ui.loadIcon("remove-item.png", 0, 48, true));
		btnModify.setGraphic(ui.loadIcon("modify-item.png", 0, 48, true));
		btnDuplicate.setGraphic(ui.loadIcon("duplicate-item.png", 0, 48, true));
		
		btnNewFile.setTooltip(new Tooltip("Create a new file in the selected folder (Ctrl+N)"));
		btnNewFolder.setTooltip(new Tooltip("Create a new folder inside the selected folder (Ctrl+Shift+N)"));
		btnRename.setTooltip(new Tooltip("Rename the selected file. (Ctrl+R)"));
		btnRefresh.setTooltip(new Tooltip("Reload the project tree for this item (F5)"));
		btnImportExternal.setTooltip(new Tooltip("Import files into the project (Ctrl+I)"));
		btnRemove.setTooltip(new Tooltip("Removes the file from the project"));
		btnModify.setTooltip(new Tooltip("Includes the file into your mod project so it can be modified (Ctrl+M)"));
		btnDuplicate.setTooltip(new Tooltip("Creates a copy inside the mod project of the selected file."));
		
		
		btnNewFile.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().createNewFile(), "Cannot create new file.");
		});
		
		btnNewFolder.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().createNewFolder(), "Cannot create new folder.");
		});
		
		btnRename.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().renameItem(), "Cannot rename item.");
		});
		
		btnRemove.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().removeItem(), "Cannot remove item.");
		});
		
		btnModify.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().modifyItem(), "Cannot modify item.");
		});
		
		btnDuplicate.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().duplicateItem(), "Cannot duplicate item.");
		});
		
		btnImportExternal.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().importFiles(), "Cannot import external files.");
		});
		
		btnRefresh.setOnAction((event) -> {
			UIManager.get().tryAction(() -> EditorManager.get().getActiveItem().refreshItem(), "Cannot refresh item.");
		});
		
		UIManager.get().addListener(this);
	}

	@Override
	public void onUIUpdate(boolean isFirstUpdate) {
		
		if (isFirstUpdate) {
			
			// Accelerators
			Platform.runLater(() -> {
				
				Scene scene = UIManager.get().getScene();
				
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), () -> {
					btnNewFile.fire();
		        });
				
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), () -> {
					btnNewFolder.fire();
		        });
				
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), () -> {
					btnRename.fire();
		        });
				
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), () -> {
					btnRefresh.fire();
		        });
		        
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN), () -> {
					btnImportExternal.fire();
		        });
				
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN), () -> {
					btnModify.fire();
		        });
		    });
		}
		
		
		ProjectItem item = EditorManager.get().getActiveItem();
		
		if (item != null) {
			
			btnNewFile.setDisable(!item.canCreateNewFile());
			btnNewFolder.setDisable(!item.canCreateNewFolder());
			btnRename.setDisable(!item.canRenameItem());
			btnRemove.setDisable(!item.canRemoveItem());
			btnModify.setDisable(!item.canModifyItem());
			btnDuplicate.setDisable(!item.canDuplicateItem());
			btnImportExternal.setDisable(!item.canImportFiles());
			btnRefresh.setDisable(!item.canRefreshItem());
		}
		else {
			btnNewFile.setDisable(true);
			btnNewFolder.setDisable(true);
			btnRename.setDisable(true);
			btnRefresh.setDisable(true);
			btnImportExternal.setDisable(true);
			btnRemove.setDisable(true);
			btnModify.setDisable(true);
			btnDuplicate.setDisable(true);
		}
		
		//btnModify.setText((selectedItem == null || !selectedItem.isFolder()) ? "Modify" : "Modify Contents");
	}
}
