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
package sporemodder.view.dialogs;

import java.util.Collection;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.Project;
import sporemodder.view.Controller;

public class OpenProjectUI implements Controller {
	
	private Dialog<ButtonType> dialog;
	
	@FXML
	private Region mainNode;
	
	@FXML
	private ListView<Project> list;

	@FXML
	private TextField searchField;

	@Override
	public Region getMainNode() {
		return mainNode;
	}
	
	@FXML
	private void initialize() {
		Collection<Project> projects = ProjectManager.get().getProjects();
		ObservableList<Project> data = FXCollections.observableArrayList();
		data.addAll(projects);
		
		FilteredList<Project> filteredData = new FilteredList<Project>(data, p -> true);
		
		list.setItems(filteredData);
		
		list.setCellFactory((list) -> {
			return new ListCell<Project>() {
		        @Override
		        protected void updateItem(Project item, boolean empty) {
		            super.updateItem(item, empty);

		            if(empty) {
		                setText(null);
		                setOnMouseClicked(null);
		            }
		            else {
		                setText(item.toString());
		                setOnMouseClicked((event) -> {
		        			if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY
		        					&& !isEmpty()) {
		        				dialog.setResult(ButtonType.OK);
		        				dialog.close();
		        			}
		        		});
		            }
		        }
		    };
		});
		
		list.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
//			alert.getAlertPane().getButton(ButtonType.OK).setDisable(newValue == null);
			dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(newValue == null);
		});
		
		searchField.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null || newValue.isEmpty()) {
				filteredData.setPredicate(p -> true);
			}
			else {
				filteredData.setPredicate(p -> p.getName().toLowerCase().contains(newValue.toLowerCase()));
			}
		});
		
		Platform.runLater(()->searchField.requestFocus());
	}
	
	public ListView<Project> getList() {
		return list;
	}
	
	public void show(String title, Consumer<? super ButtonType> consumer, boolean multiSelection) {
		dialog = new Dialog<ButtonType>();
		dialog.setTitle("Open project");
		dialog.getDialogPane().setContent(mainNode);
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		
		// Disable the OK button as nothing is selected
		dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
		
		if (multiSelection) {
			list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		}
		
		UIManager.get().showDialog(dialog).ifPresent(consumer);
	}
	
	public static void show() {
		OpenProjectUI content = UIManager.get().loadUI("dialogs/OpenProjectUI");
		
		content.show("Open project", result -> {
			if (result == ButtonType.OK) {
				Project project = content.list.getSelectionModel().getSelectedItem();
				if (project != null) {
					ProjectManager.get().setActive(project);
				}
			}
		}, false);
	}
}
