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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import sporemodder.EditorManager;
import sporemodder.UIManager;
import sporemodder.view.Controller;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.editors.SearchableEditor;

public class TextActionsUI implements Controller, UIUpdateListener {

	@FXML private Node mainNode;
	
	@FXML private TextField tfFind;
	@FXML private Button btnFindUp;
	@FXML private Button btnFindDown;
	
	private static TextActionsUI instance;
	
	public static TextActionsUI getInstance() {
		return instance;
	}
	
	public TextField getFindField() {
		return tfFind;
	}
	
	public void focusFindField() {
		Platform.runLater(() -> tfFind.requestFocus());
	}

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	@FXML private void initialize() {
		instance = this;
		
		UIManager ui = UIManager.get();
		
		btnFindUp.setGraphic(ui.loadIcon("find-up.png", 20, 20, true));
		btnFindDown.setGraphic(ui.loadIcon("find-down.png", 20, 20, true));
		
		btnFindUp.setOnAction(event -> {
			SearchableEditor editor = EditorManager.get().getSearchableEditor();
			if (editor != null) {
				editor.findUp();
				updateFindButtons(editor);
			}
		});
		
		btnFindDown.setOnAction(event -> {
			SearchableEditor editor = EditorManager.get().getSearchableEditor();
			if (editor != null) {
				editor.findDown();
				updateFindButtons(editor);
			}
		});
		
		tfFind.textProperty().addListener((obs, oldValue, newValue) -> {
			SearchableEditor editor = EditorManager.get().getSearchableEditor();
			if (editor != null && newValue != null && !newValue.equals(oldValue)) {
				editor.find(newValue);
				updateFindButtons(editor);
			}
		});
		
		UIManager.get().addListener(this);
	}
	
	private void updateFindButtons(SearchableEditor editor) {
		if (editor != null) {
			btnFindUp.setDisable(!editor.canFindUp());
			btnFindDown.setDisable(!editor.canFindDown());
		} else {
			btnFindUp.setDisable(true);
			btnFindDown.setDisable(true);
		}
	}

	@Override
	public void onUIUpdate(boolean isFirstUpdate) {
		
		SearchableEditor editor = EditorManager.get().getSearchableEditor();
		if (editor != null) {
			tfFind.setText(editor.getSearchedText());
			
			tfFind.setDisable(false);
		} else {
			tfFind.setDisable(true);
		}
		
		updateFindButtons(editor);
	}

}
