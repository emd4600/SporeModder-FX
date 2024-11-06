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
import java.nio.file.Paths;
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
import sporemodder.GitHubManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.GitCommands;
import sporemodder.util.ModBundle;
import sporemodder.util.Project;
import sporemodder.util.ProjectPreset;
import sporemodder.view.Controller;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class CreateProjectUI implements Controller {

	private static final String WARNING_UNIQUE_TAG_EMPTY = "Unique tag cannot be empty";
	private static final String WARNING_MOD_NAME_EMPTY = "Mod name cannot be empty";
	private static final String WARNING_PROJECT_NAME_EMPTY = "Package project name cannot be empty";
	private static final String WARNING_MOD_NAME_REPEATED = "A mod with this name already exists";
	private static final String WARNING_PROJECT_NAME_REPEATED = "A package project with this name already exists";
	private static final String WARNING_MOD_NAME_INVALID = "Mod name cannot contain any character from " + ModBundle.NAME_ILLEGAL_CHARACTERS;
	private static final String WARNING_PROJECT_NAME_INVALID = "Package project name cannot contain any character from " + ModBundle.NAME_ILLEGAL_CHARACTERS;

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
	private TextField uniqueTagTextField;
	@FXML
	private TextField descriptionTextField;
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

	@Override
	public Pane getMainNode() {
		return mainNode;
	}

	public void createMod() throws IOException, InterruptedException, ParserConfigurationException, TransformerException {
		List<ProjectPreset> presets = presetBoxes.stream()
				.filter(CheckBox::isSelected)
				.map(box -> ((ProjectPreset)box.getUserData()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (newModButton.isSelected()) {
			ProjectManager.get().createNewMod(
					modNameField.getText(),
					uniqueTagTextField.getText(),
					descriptionTextField.getText(),
					projectNameField.getText(),
					presets
			);
		} else {
			ProjectManager.get().createNewProjectInMod(
					ProjectManager.get().getModBundle(existingModChoiceBox.getValue()),
					projectNameField.getText(),
					presets
			);
		}
	}

	public static void show() {
		CreateProjectUI node = UIManager.get().loadUI("dialogs/CreateProjectUI");
		node.dialog = new Dialog<>();
		node.dialog.getDialogPane().setContent(node.getMainNode());
		
		node.dialog.setTitle("Create new project");
		node.dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

		node.validateModFields();
		
		UIManager.get().showDialog(node.dialog).ifPresent(result -> {
			if (result == ButtonType.OK && UIManager.get().tryAction(node::createMod,
						"Cannot initialize project. Try manually deleting the project folder in SporeModder FX\\Projects\\"))
			{
				ProjectManager.get().setActive(ProjectManager.get().getProject(node.projectNameField.getText()));
			}
		});
	}

	private static boolean hasIllegalChar(String text) {
		return ModBundle.NAME_ILLEGAL_CHARACTERS.chars().anyMatch(c -> text.indexOf(c) != -1);
	}

	private boolean isValid() {
		String projectName = projectNameField.getText();
		String modName = modNameField.getText();
		if (projectName.isEmpty() || hasIllegalChar(projectName) ||
				ProjectManager.get().hasProject(projectNameField.getText())) {
			return false;
		}
		if (newModButton.isSelected() &&
				(modName.isEmpty() || hasIllegalChar(modName) || ProjectManager.get().hasModBundle(modName)) ||
				uniqueTagTextField.getText().isBlank()) {
			return false;
		}
		return true;
	}

	private void validateModFields() {
		boolean showWarning = true;
		// Show warning if name collides
		String modName = modNameField.getText();
		if (ProjectManager.get().hasModBundle(modName)) {
			modWarningLabel.setText(WARNING_MOD_NAME_REPEATED);
		} else {
			if (modName.isEmpty()) {
				modWarningLabel.setText(WARNING_MOD_NAME_EMPTY);
			} else if (hasIllegalChar(modName)) {
				modWarningLabel.setText(WARNING_MOD_NAME_INVALID);
			} else if (uniqueTagTextField.getText().isBlank()) {
				modWarningLabel.setText(WARNING_UNIQUE_TAG_EMPTY);
			} else {
				showWarning = false;
			}
		}

		modWarningLabel.setVisible(showWarning);
		dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(showWarning || !isValid());
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
		uniqueTagTextField.setText(ModBundle.generateUniqueTagFromName(projectNameField.getText()));

		modWarningLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
		projectWarningLabel.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));

		// Ban illegal characters
		modNameField.setTextFormatter(new TextFormatter<>(change -> {
			String text = change.getControlNewText();
			if (ModBundle.NAME_ILLEGAL_CHARACTERS.chars().anyMatch(c -> text.indexOf(c) != -1)) {
				return null;
			} else {
				return change;
			}
		}));
		projectNameField.setTextFormatter(new TextFormatter<>(change -> {
			String text = change.getControlNewText();
			if (ModBundle.NAME_ILLEGAL_CHARACTERS.chars().anyMatch(c -> text.indexOf(c) != -1)) {
				return null;
			} else {
				return change;
			}
		}));
		uniqueTagTextField.setTextFormatter(new TextFormatter<>(change -> {
			String text = change.getControlNewText();
			if (text.matches(ModBundle.UNIQUETAG_ALLOWED_REGEX + "+")) {
				return change;
			} else {
				return null;  // reject the change
			}
		}));

		modNameField.textProperty().addListener((obs, oldValue, newValue) -> {
			// Replicate changes to project name
			if (projectNameField.getText().equals(oldValue)) {
				projectNameField.setText(newValue);
			}
			if (Objects.equals(uniqueTagTextField.getText(), ModBundle.generateUniqueTagFromName(oldValue))) {
				uniqueTagTextField.setText(ModBundle.generateUniqueTagFromName(newValue));
			}
			validateModFields();
		});
		projectNameField.textProperty().addListener((obs, oldValue, newValue) -> {
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
