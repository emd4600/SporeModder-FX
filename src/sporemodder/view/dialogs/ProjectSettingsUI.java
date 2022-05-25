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
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
	
	@FXML private CheckBox cbPackageCompression;
	@FXML private Pane compressionThresholdBox;
	@FXML private Spinner compressionThresholdSpinner; //TextField compressionThresholdField;
	
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
		
		List<Project> oldSources = project.getSources();
		List<Project> newSources = sourcesList.getItems();
		boolean sourcesChanged = oldSources.size() != newSources.size();
		
		if (!sourcesChanged) {
			for (int i = 0; i < oldSources.size(); i++) {
				if (oldSources.get(i) != newSources.get(i)) {
					sourcesChanged = true;
					break;
				}
			}
		}
		
		project.setPackageName(packageNameField.getText());
		
		if (sourcesChanged) {
			project.getSources().clear();
			project.getSources().addAll(sourcesList.getItems());
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
		
		
		project.setUseCompression(cbPackageCompression.isSelected());
		project.setCompressionThreshold((int)(compressionThresholdSpinner.getValueFactory().getValue()));
		//cbPackageCompression
		//compressionThresholdBox
		//compressionThresholdField
		//compressionThresholdSpinner
		
		if (saveSettingsOnExit) {
			project.saveSettings();
			
			UIManager.get().notifyUIUpdate(false);
			
			if (sourcesChanged) {
				ProjectManager.get().refreshProjectTree();
			}
		}
	}
	
	
	private static class ByteCountSpinnerValueFactory extends SpinnerValueFactory<Integer> {
		
		private int min = 0;
		private int max = Integer.MAX_VALUE;
		private int step = 1024;
		private float stepF = step;
		private boolean roundOnIncrement = true;
		
		public ByteCountSpinnerValueFactory(int initialValue) {
			
			valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)
                if (newValue < min) {
                    setValue(min);
                } else if (newValue > max) {
                	setValue(max);
            	}
            });
			
			setValue(initialValue >= min && initialValue <= max ? initialValue : min);
		}

		
		public void setRoundOnIncrement(boolean newValue) {
			roundOnIncrement = newValue;
		}
		
		
		private int getRoundedValue() {
			int value = getValue();
			return value;
			//return roundOnIncrement ? (step * Math.round(value / stepF)) : value;
		}
		
		@Override
		public void decrement(int steps) {
            final int newIndex = getRoundedValue() - steps * step;
            setValue(newIndex >= min ? newIndex : min);
		}

		@Override
		public void increment(int steps) {
			final int newIndex = getRoundedValue() + steps * step;
            setValue(newIndex <= max ? newIndex : max);
		}
	};
	
	private boolean showInternal(boolean saveSettingsOnExit) {
		dialog.setTitle("Project Settings (" + project.getName() + ")");
		
		dialog.getDialogPane().setContent(mainNode);
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
		
		
		tfName.setText(project.getName());
		sourcesList.getItems().setAll(project.getSources());
		
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
		
		cbPackageCompression.selectedProperty().addListener((obs, oldValue, newValue) -> {
			compressionThresholdBox.setDisable(!newValue);
		});
		cbPackageCompression.setSelected(project.getUseCompression());
		
		ByteCountSpinnerValueFactory byteCountFactory = new ByteCountSpinnerValueFactory(project.getCompressionThreshold());
		compressionThresholdSpinner.setValueFactory(byteCountFactory);
		/*compressionThresholdSpinner.setOnKeyReleased((value) -> {
			ensureCompressionThresholdSpinnerValue(byteCountFactory);
		});*/
		//boolean refreshValue = true;
		TextField compressionThresholdSpinnerEditor = compressionThresholdSpinner.getEditor();
		compressionThresholdSpinnerEditor.textProperty().addListener((obs, oldValue, newValue) -> {	
			//if (refreshValue) {
				try {
					int value = 0;
					String newValL = newValue.replace(",", "").replace(".", "").replace(" ", "").toLowerCase();
					if (newValL.endsWith("b"))
					{
						int charIndex = -1;
						for (int i = 0; i < newValL.length(); i++) {
							if (!Character.isDigit(newValL.charAt(i))) {
								charIndex = i;
								break;
							}
						}
						if (charIndex <= -1)
							value = Integer.parseInt(newValue);
						else {
							String newValSuffix = newValL.substring(charIndex).toLowerCase();
							
							int mult = 1;
							int valueRaw = -1;
							
							if (newValSuffix.equalsIgnoreCase("kb") || newValSuffix.equalsIgnoreCase("kilobytes"))
								mult = 1024;
							else if (newValSuffix.equalsIgnoreCase("mb") || newValSuffix.equalsIgnoreCase("megabytes"))
								mult = 1024 * 1024;
							/*else if (newValSuffix.equalsIgnoreCase("gb") || newValSuffix.equalsIgnoreCase("gigabytes"))
								mult = 1024 * 1024 * 1024;*/
							
							valueRaw = Integer.parseInt(newValL.substring(0, newValL.length() - newValSuffix.length()));
							
							int oldVal = (int)(byteCountFactory.getValue());
							
							long newValLong = valueRaw * mult;
							int newVal = valueRaw * mult;
							
							if ((valueRaw != 0) && (newVal != 0))
								value = newVal;
							else if (valueRaw == 0)
								value = 0;
							else //if (newValLong != newVal) //((oldVal != 0) && ((newVal) == 0))
								value = oldVal;
						}
						
					}
					else
						value = Integer.parseInt(newValue);
					
					if ((value != byteCountFactory.getValue()) && (value >= 0))
						byteCountFactory.setValue(value);
					else
						ensureCompressionThresholdSpinnerValue(byteCountFactory);
				}
				catch (Exception ex) {
					//refreshValue = false;
					//compressionThresholdSpinnerEditor.setText(oldValue);
					//refreshValue = true;
					ensureCompressionThresholdSpinnerValue(byteCountFactory);
					//compressionThresholdSpinner.getValueFactory().setValue(compressionThresholdSpinner.getValue());
				}
			//}
		});
		/*compressionThresholdSpinner.focusedProperty().addListener((obs, oldValue, newValue) -> {
			if (!newValue)
				compressionThresholdSpinner.increment(0);
				//ensureCompressionThresholdSpinnerValue(byteCountFactory);
		});*/
		
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
	
	void ensureCompressionThresholdSpinnerValue(ByteCountSpinnerValueFactory byteCountFactory) {
		System.out.println("VALUE WAS: " + byteCountFactory.getValue());
		if (byteCountFactory.getValue() > 1024) {
			byteCountFactory.decrement(0);
			byteCountFactory.increment(0);
		}
		else {
			byteCountFactory.increment(0);
			byteCountFactory.decrement(0);
		}
		System.out.println("VALUE IS: " + byteCountFactory.getValue());
		/*byteCountFactory.setRoundOnIncrement(false);
		compressionThresholdSpinner.increment(0);
		byteCountFactory.setRoundOnIncrement(true);*/
	}

	public static boolean show(Project project, boolean saveSettingsOnExit) {
		ProjectSettingsUI node = UIManager.get().loadUI("dialogs/ProjectSettingsUI");
		node.dialog = new Dialog<ButtonType>();
		node.project = project;
		return node.showInternal(saveSettingsOnExit);
	}
}
