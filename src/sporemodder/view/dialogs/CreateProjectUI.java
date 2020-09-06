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

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.Project;
import sporemodder.util.ProjectPreset;
import sporemodder.view.Controller;

public class CreateProjectUI implements Controller {
	
	private Dialog<ButtonType> dialog;
	
	@FXML
	private Pane mainNode;
	
	@FXML
	private Label alreadyExistsLabel;
	
	@FXML
	private TextField nameField;
	
	private final List<CheckBox> presetBoxes = new ArrayList<CheckBox>();

	@Override
	public Pane getMainNode() {
		return mainNode;
	}
	
	public static void show() {
		CreateProjectUI node = UIManager.get().loadUI("dialogs/CreateProjectUI");
		node.dialog = new Dialog<ButtonType>();
		node.dialog.getDialogPane().setContent(node.getMainNode());
		
		node.dialog.setTitle("Create new project");
		node.dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		
		UIManager.get().showDialog(node.dialog).ifPresent(result -> {
			if (result == ButtonType.OK) {
				ProjectManager projectManager = ProjectManager.get();
				
				Project project = new Project(node.nameField.getText());
				
				List<ProjectPreset> presets = ProjectManager.get().getPresets();
				for (int i = 0; i < node.presetBoxes.size(); i++) {
					if (node.presetBoxes.get(i).isSelected()) {
						Project source = projectManager.getProject(presets.get(i).getName());
						if (source != null) {
							project.getSources().add(source);
						}
					}
				}
				
				if (UIManager.get().tryAction(() -> projectManager.initializeProject(project), 
						"Cannot initialize project. Try manually deleting the project folder in SporeModder FX\\Projects\\"))
				{
					projectManager.setActive(project);
				}
			}
		});
	}

	@FXML
	private void initialize() {
		// Set a default text
		nameField.setText("Project " + (ProjectManager.get().getProjects().size() + 1));
		
		alreadyExistsLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
		
		nameField.textProperty().addListener((obs, oldValue, newValue) -> {
			if (ProjectManager.get().hasProject(newValue)) {
				alreadyExistsLabel.setVisible(true);
				
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
			else {
				alreadyExistsLabel.setVisible(false);
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
			}
		});
		
		nameField.requestFocus();
		nameField.selectAll();
		nameField.requestFocus();
		
		List<ProjectPreset> presets = ProjectManager.get().getPresets();
		for (ProjectPreset preset : presets) {
			CheckBox checkBox = new CheckBox(preset.getName());
			checkBox.setUserData(preset);
			checkBox.setTooltip(new Tooltip(preset.getDescription()));
			checkBox.setSelected(preset.isRecommendable());
			
			presetBoxes.add(checkBox);
			mainNode.getChildren().add(checkBox);
		}
	}
}
