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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import sporemodder.FileManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ModBundle;
import sporemodder.util.Project;
import sporemodder.util.ProjectPreset;
import sporemodder.view.Controller;

public class CreateProjectUI implements Controller {

	private static final String ILLEGAL_CHARACTERS = "/\\";

	private static final String WARNING_MOD_NAME_EMPTY = "Mod name cannot be empty";
	private static final String WARNING_PROJECT_NAME_EMPTY = "Package project name cannot be empty";
	private static final String WARNING_MOD_NAME_REPEATED = "A mod with this name already exists";
	private static final String WARNING_PROJECT_NAME_REPEATED = "A package project with this name already exists";
	private static final String WARNING_MOD_NAME_INVALID = "Mod name cannot contain any character from " + ILLEGAL_CHARACTERS;
	private static final String WARNING_PROJECT_NAME_INVALID = "Package project name cannot contain any character from " + ILLEGAL_CHARACTERS;

	private Dialog<ButtonType> dialog;
	
	@FXML
	private Pane mainNode;
	
	@FXML
	private Label modWarningLabel;
	@FXML
	private Label projectWarningLabel;
	
	@FXML
	private TextField modNameField;
	@FXML
	private TextField projectNameField;

	@FXML
	private ChoiceBox<String> existingModChoiceBox;

	@FXML
	private Pane newModPane;
	@FXML
	private Pane existingModPane;

	@FXML
	private RadioButton newModButton;
	@FXML
	private RadioButton existingModButton;

	@FXML
	private ImageView referencesInfoImage;
	
	private final List<CheckBox> presetBoxes = new ArrayList<>();

	private boolean projectNameEqualsModName = true;

	@Override
	public Pane getMainNode() {
		return mainNode;
	}

	public void createMod() throws IOException, InterruptedException {
		ProjectManager projectManager = ProjectManager.get();

		// Create base mod
		ModBundle modBundle;
		if (newModButton.isSelected()) {
			modBundle = new ModBundle(modNameField.getText());
			projectManager.initializeModBundle(modBundle);
		} else {
			modBundle = projectManager.getModBundle(existingModChoiceBox.getValue());
			assert modBundle != null;
		}

		// Create package project
		File projectFolder = new File(modBundle.getDataFolder(), projectNameField.getText());
		Project project = new Project(projectNameField.getText(), projectFolder, null);
		modBundle.addProject(project);

		// Add project references
		project.getReferences().addAll(presetBoxes.stream()
				.filter(CheckBox::isSelected)
				.map(box -> projectManager.getProject(((ProjectPreset)box.getUserData()).getName()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		// Initialize package project folder
		projectManager.initializeProject(project);

		// Initialize git repository
		projectManager.initializeModBundleGit(modBundle);
	}

	public static void show() {
		CreateProjectUI node = UIManager.get().loadUI("dialogs/CreateProjectUI");
		node.dialog = new Dialog<>();
		node.dialog.getDialogPane().setContent(node.getMainNode());
		
		node.dialog.setTitle("Create new project");
		node.dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		
		UIManager.get().showDialog(node.dialog).ifPresent(result -> {
			if (result == ButtonType.OK && UIManager.get().tryAction(node::createMod,
						"Cannot initialize project. Try manually deleting the project folder in SporeModder FX\\Projects\\"))
			{
				ProjectManager.get().setActive(ProjectManager.get().getProject(node.projectNameField.getText()));
			}
		});
	}

	private static boolean hasIllegalChar(String text) {
		return ILLEGAL_CHARACTERS.chars().anyMatch(c -> text.indexOf(c) != -1);
	}

	private boolean isValid() {
		String projectName = projectNameField.getText();
		String modName = modNameField.getText();
		if (projectName.isEmpty() || hasIllegalChar(projectName) ||
				ProjectManager.get().hasProject(projectNameField.getText())) {
			return false;
		}
		if (newModButton.isSelected() &&
				(modName.isEmpty() || hasIllegalChar(modName) || ProjectManager.get().hasModBundle(modName))) {
			return false;
		}
		return true;
	}

	private void setWarningLabel(Label label, String text) {
		if (text == null) {
			label.setVisible(false);
		} else {
			label.setText(text);
			label.setVisible(true);
		}
	}

	@FXML
	private void initialize() {
		// Set a default text
		projectNameField.setText("Project " + (ProjectManager.get().getProjects().size() + 1));
		modNameField.setText(projectNameField.getText());

		modWarningLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
		projectWarningLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));

		modNameField.textProperty().addListener((obs, oldValue, newValue) -> {
			// Replicate changes to project name
			if (projectNameEqualsModName) {
				projectNameField.setText(newValue);
			}
			// Show alert if name collides
			if (ProjectManager.get().hasModBundle(newValue)) {
				setWarningLabel(modWarningLabel, WARNING_MOD_NAME_REPEATED);
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			} else {
				if (newValue.isEmpty()) {
					setWarningLabel(modWarningLabel, WARNING_MOD_NAME_EMPTY);
				} else if (hasIllegalChar(newValue)) {
					setWarningLabel(modWarningLabel, WARNING_MOD_NAME_INVALID);
				} else {
					setWarningLabel(modWarningLabel, null);
				}
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValid());
			}
		});
		projectNameField.textProperty().addListener((obs, oldValue, newValue) -> {
			// Check if mod name and project name have to be coordinated
			// If the project name has changed to be different, don't coordinate anymore
			projectNameEqualsModName = newValue.equals(modNameField.getText());
			// Show alert if name collides
			if (ProjectManager.get().hasProject(newValue)) {
				setWarningLabel(projectWarningLabel, WARNING_PROJECT_NAME_REPEATED);
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			} else {
				if (newValue.isEmpty()) {
					setWarningLabel(projectWarningLabel, WARNING_PROJECT_NAME_EMPTY);
				} else if (hasIllegalChar(newValue)) {
					setWarningLabel(projectWarningLabel, WARNING_PROJECT_NAME_INVALID);
				} else {
					setWarningLabel(projectWarningLabel, null);
				}
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValid());
			}
		});

		modNameField.requestFocus();
		modNameField.selectAll();
		modNameField.requestFocus();
		
		List<ProjectPreset> presets = ProjectManager.get().getPresets();
		for (ProjectPreset preset : presets) {
			CheckBox checkBox = new CheckBox(preset.getName());
			checkBox.setUserData(preset);
			checkBox.setTooltip(new Tooltip(preset.getDescription()));
			checkBox.setSelected(preset.isRecommendable());
			
			presetBoxes.add(checkBox);
			mainNode.getChildren().add(checkBox);
		}

		newModPane.visibleProperty().bind(newModButton.selectedProperty());
		existingModPane.visibleProperty().bind(existingModButton.selectedProperty());

		newModButton.selectedProperty().addListener((obs, oldValue, newValue) -> {
			dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValid());
		});

		newModButton.setSelected(true);

		Collection<ModBundle> modBundles = ProjectManager.get().getModBundles();
		ObservableList<String> modBundleItems = FXCollections.observableArrayList(modBundles.stream().map(ModBundle::getName).collect(Collectors.toList()));
		existingModButton.setDisable(modBundles.isEmpty());
		existingModChoiceBox.setItems(modBundleItems);
		if (!modBundles.isEmpty()) {
			existingModChoiceBox.getSelectionModel().select(0);  //TODO find most recent mod bundle
		}

		Image image = UIManager.get().loadImage("dialog-information.png");
		referencesInfoImage.setImage(image);
		Tooltip tooltip = new Tooltip();
		tooltip.setShowDelay(Duration.ZERO);
		tooltip.setText("References allow you to see files from other projects, and edit and copy them to your mod. You can change them later in the project settings.");
		Tooltip.install(referencesInfoImage, tooltip);
	}
}
