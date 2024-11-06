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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.Project;
import sporemodder.util.Project.PackageSignature;
import sporemodder.util.GamePathConfiguration.GamePathType;
import sporemodder.view.Controller;

/**
 * The UI for the dialog that contains the project settings. If a developer wants to modify this,
 * it is possible to do so by listening the message <code>OnUILoad</code> message (check {@link MessageManager}).
 * The UI name is <code>dialogs/ProjectSettingsUI</code> and the controller is an object of this class.
 */
public class ProjectSettingsUI implements Controller {
	
	@FXML private TabPane mainNode;
	
	@FXML private TextField tfName;
	@FXML private Label nameErrorLabel;
	
	@FXML private ListView<Project> sourcesList;
	@FXML private Button btnAdd;
	@FXML private Button btnRemove;
	
	@FXML private TextField packageNameField;
	@FXML private Label packageNameErrorLabel;
	private boolean isDefaultPackageName = true;
	
	@FXML private ChoiceBox<String> packPathBox;
	@FXML private TextField packPathField;
	@FXML private Button findPackPathButton;
	
	@FXML private ChoiceBox<PackageSignature> signatureBox;
	
	private Dialog<ButtonType> dialog;
	
	private Project project;
	
	@FXML
	private void initialize() {
		
		// Add a warning icon to the warning label
		nameErrorLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
		packageNameErrorLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
		
		// Show a warning if the name collides
		tfName.textProperty().addListener((obs, oldValue, newValue) -> {
			if (isDefaultPackageName) {
				packageNameField.setText(Project.getDefaultPackageName(newValue));
			}
			
			Project existing = ProjectManager.get().getProject(newValue);
			
			if (existing != null && existing != project) {
				nameErrorLabel.setVisible(true);
			}
			else {
				nameErrorLabel.setVisible(false);
			}
			
			updateError();
		});
		
		// Use draggable items in the sources list
		sourcesList.setCellFactory(value -> new SourceProjectCell());
		
		// Add source button
		btnAdd.setOnAction(event -> {
			OpenProjectUI openProjectUI = UIManager.get().loadUI("dialogs/OpenProjectUI");
			openProjectUI.show("Add source project", result -> {
				if (result == ButtonType.OK) {
					sourcesList.getItems().addAll(openProjectUI.getList().getSelectionModel().getSelectedItems());
				}
			}, true);
		});
		
		// Remove source button
		btnRemove.setOnAction(event -> {
			Project item = sourcesList.getSelectionModel().getSelectedItem();
			if (item != null) {
				sourcesList.getItems().remove(item);
			}
		});
		sourcesList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			btnRemove.setDisable(newValue == null);
		});
		
		
		packageNameField.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue.trim().isEmpty()) {
				packageNameField.setText(Project.getDefaultPackageName(tfName.getText()));
				isDefaultPackageName = true;
			}
			else {
				isDefaultPackageName = Project.getDefaultPackageName(tfName.getText()).equals(newValue);
				
				Project p = ProjectManager.get().getProjectByPackageName(newValue);
				if (p != null && p != project) {
					packageNameErrorLabel.setVisible(true);
				} else {
					packageNameErrorLabel.setVisible(false);
				}
				updateError();
			}
		});
		
		packPathBox.getItems().setAll("Spore", "Galactic Adventures", "Custom");
		packPathBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			packPathField.setDisable(!newValue.equals("Custom"));
			findPackPathButton.setDisable(!newValue.equals("Custom"));
		});
		packPathField.setDisable(true);
		
		findPackPathButton.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			if (!packPathField.getText().isEmpty()) chooser.setInitialDirectory(new File(packPathField.getText()));
			
			UIManager.get().setOverlay(true);
			File result = chooser.showDialog(UIManager.get().getScene().getWindow());
			UIManager.get().setOverlay(false);
			
			if (result != null) {
				packPathField.setText(result.getAbsolutePath());
			}
		});
		
		signatureBox.getItems().setAll(PackageSignature.values());
	}
	
	private void updateError() {
		if (nameErrorLabel.isVisible() || packageNameErrorLabel.isVisible()) {
			dialog.getDialogPane().lookupButton(ButtonType.APPLY).setDisable(true);
		} else {
			dialog.getDialogPane().lookupButton(ButtonType.APPLY).setDisable(false);
		}
	}

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	/**
	 * Returns the tab pane node. By default this just contains the main tab.
	 * @return
	 */
	public TabPane getTabPane() {
		return mainNode;
	}
	
	private void saveSettings(boolean saveSettingsOnExit) {
		boolean nameChanged = !tfName.getText().equals(project.getName());
		
		if (saveSettingsOnExit && nameChanged) {
			ProjectManager.get().rename(project, tfName.getText());
		}

		project.setPackageName(packageNameField.getText());

		Set<Project> oldSources = project.getReferences();
		Set<Project> newSources = new LinkedHashSet<>(sourcesList.getItems());
		boolean sourcesChanged = !oldSources.equals(newSources);
		if (sourcesChanged) {
			project.getReferences().clear();
			project.getReferences().addAll(sourcesList.getItems());
		}
		
		switch (packPathBox.getSelectionModel().getSelectedItem()) {
		case "Spore":
			project.getPackPath().setType(GamePathType.SPORE);
			break;
		case "Galactic Adventures":
			project.getPackPath().setType(GamePathType.GALACTIC_ADVENTURES);
			break;
		default:
			project.getPackPath().setCustomPath(packPathField.getText());
			break;
		}
		
		project.setPackageSignature(signatureBox.getSelectionModel().getSelectedItem());
		
		if (saveSettingsOnExit) {
			project.saveSettings();
			
			UIManager.get().notifyUIUpdate(false);
			
			if (sourcesChanged) {
				ProjectManager.get().refreshProjectTree();
			}
		}
	}
	
	private boolean showInternal(boolean saveSettingsOnExit) {
		dialog.setTitle("Project Settings (" + project.getName() + ")");
		
		dialog.getDialogPane().setContent(mainNode);
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
		
		
		tfName.setText(project.getName());
		sourcesList.getItems().setAll(project.getReferences());
		
		String packPath = project.getPackPath().getCustomPath();
		if (packPath != null) {
			packPathField.setText(packPath);
		}
		GamePathType packType = project.getPackPath().getType();
		if (packType == GamePathType.SPORE) packPathBox.getSelectionModel().select("Spore");
		else if (packType == GamePathType.GALACTIC_ADVENTURES) packPathBox.getSelectionModel().select("Galactic Adventures");
		else packPathBox.getSelectionModel().select("Custom");
		
		packageNameField.setText(project.getPackageName());
		
		signatureBox.getSelectionModel().select(project.getPackageSignature());
		
		dialog.setOnShown(event -> {
			// Make the user start editing the project name
			tfName.requestFocus();
			tfName.selectAll();
			tfName.requestFocus();
		});

		
		Optional<ButtonType> result = UIManager.get().showDialog(dialog);
		if (result.isPresent() && result.get() == ButtonType.APPLY) {
			UIManager.get().tryAction(() -> saveSettings(saveSettingsOnExit), "Couldn't save project settings");
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean show(Project project, boolean saveSettingsOnExit) {
		ProjectSettingsUI node = UIManager.get().loadUI("dialogs/ProjectSettingsUI");
		node.dialog = new Dialog<ButtonType>();
		node.project = project;
		return node.showInternal(saveSettingsOnExit);
	}
}
