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
package sporemodder.view.ribbons.edit;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import sporemodder.EditorManager;
import sporemodder.UIManager;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.editors.EditHistoryAction;
import sporemodder.view.editors.EditHistoryEditor;
import sporemodder.view.editors.ItemEditor;

public class EditActionsUI implements Controller, UIUpdateListener {
	
	@FXML private Node mainNode;
	
	@FXML private Button btnSave;
	@FXML private MenuButton btnUndoHistory;
	@FXML private Button btnUndo;
	@FXML private Button btnRedo;

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	@FXML private void initialize() {
		UIManager ui = UIManager.get();
		
//		btnSave.setGraphic(ui.loadIcon("save.png", 0, 48, true));
//		btnUndo.setGraphic(ui.loadIcon("undo.png", 0, 48, true));
//		btnRedo.setGraphic(ui.loadIcon("redo.png", 0, 48, true));
		
		btnSave.setGraphic(ui.loadIcon("save.png", 0, 38, true));
		btnUndo.setGraphic(ui.loadIcon("undo.png", 0, 38, true));
		btnRedo.setGraphic(ui.loadIcon("redo.png", 0, 38, true));
		
		BorderPane historyIcon = new BorderPane();
		historyIcon.setCenter(ui.loadIcon("arrow-down.png", 0, 16, true));
		historyIcon.setPrefHeight(48);
		BorderPane.setAlignment(historyIcon, Pos.CENTER);
		btnUndoHistory.setGraphic(historyIcon);
		
		btnSave.setTooltip(new Tooltip("Save the changes on the file (Ctrl+S)"));
		btnUndo.setTooltip(new Tooltip("Undo last edit action (Ctrl+Z)"));
		btnRedo.setTooltip(new Tooltip("Redo edit action (Ctrl+Y)"));
		
		btnSave.setOnAction(event -> {
			ItemEditor activeEditor = EditorManager.get().getActiveEditor();
			if (activeEditor != null && activeEditor.isEditable()) {
				activeEditor.save();
			}
			UIManager.get().notifyUIUpdate(false);
		});
		
		btnUndo.setOnAction(event -> {
			EditHistoryEditor editor = EditorManager.get().getEditHistoryEditor();
			if (editor != null) {
				editor.undo();
			}
			UIManager.get().notifyUIUpdate(false);
		});
		
		btnRedo.setOnAction(event -> {
			EditHistoryEditor editor = EditorManager.get().getEditHistoryEditor();
			if (editor != null) {
				editor.redo();
			}
			UIManager.get().notifyUIUpdate(false);
		});
		
		UIManager.get().addListener(this);
	}

	@Override
	public void onUIUpdate(boolean isFirstUpdate) {
		if (isFirstUpdate) {
			
			// Accelerators
			Platform.runLater(() -> {
				
				Scene scene = UIManager.get().getScene();
				
				scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> {
					btnSave.fire();
		        });
				
				// RichTextFX already includes the shortcuts for undo/redo, so we won't do them twice
		    });
		}
		
		EditHistoryEditor editor = EditorManager.get().getEditHistoryEditor();
		if (editor != null) {
			btnUndo.setDisable(!editor.canUndo());
			btnRedo.setDisable(!editor.canRedo());
			
			List<? extends EditHistoryAction> actions = editor.getActions();
			if (actions != null && !actions.isEmpty()) {
				
				btnUndoHistory.setDisable(false);
				btnUndoHistory.getItems().clear();
				final ToggleGroup undoHistoryGroup = new ToggleGroup();
				
				for (int i = actions.size()-1; i >= 0; --i) {
					EditHistoryAction action = actions.get(i);
					RadioMenuItem item = new RadioMenuItem(action.getText());
					item.setToggleGroup(undoHistoryGroup);
					
					if (i == editor.getUndoRedoIndex()) item.setSelected(true);
					
					btnUndoHistory.getItems().add(item);
				}
				
				undoHistoryGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
					if (btnUndoHistory.getItems().isEmpty() || newValue == null) return;
					
					int oldIndex = editor.getUndoRedoIndex();
					@SuppressWarnings("unlikely-arg-type")
					int newIndex = actions.size() - btnUndoHistory.getItems().indexOf(newValue) - 1;
					
					if (oldIndex < newIndex) {
						// redo from oldIndex+1 to newIndex (included)
						// We don't redo the previously selected cause it was not undone
						for (int i = oldIndex+1; i <= newIndex; ++i) {
							editor.redo();
						}
					} else if (newIndex < oldIndex) {
						// undo from oldIndex to newIndex (excluded)
						for (int i = oldIndex; i > newIndex; --i) {
							// Don't undo the action, but the whole editor itself (we are like "popping" the actions from the stack)
							// This way the redo index will be modified accordingly
							editor.undo();
						}
					}
				});
				
			} else {
				btnUndoHistory.setDisable(true);
			}
		} else {
			btnUndo.setDisable(true);
			btnRedo.setDisable(true);
			btnUndoHistory.setDisable(true);
		}
		
		if (EditorManager.get().canSave()) {
			btnSave.setDisable(false);
		} else {
			btnSave.setDisable(true);
		}
	}
}
