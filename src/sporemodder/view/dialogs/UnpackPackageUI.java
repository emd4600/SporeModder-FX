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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sporemodder.FileManager;
import sporemodder.FormatManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.Converter;
import sporemodder.file.dbpf.DBPFItem;
import sporemodder.file.dbpf.DBPFUnpackingTask;
import sporemodder.util.Project;
import sporemodder.view.Controller;

public class UnpackPackageUI implements Controller {
	
	private Dialog<ButtonType> dialog;
	
	@FXML private Region mainNode;
	
	@FXML private TextField nameField;
	
	@FXML private Label infoLabel;
	
	@FXML private CheckBox cbReadOnly;
	
	@FXML private VBox settingsPane;
	
	private final List<CheckBox> converterBoxes = new ArrayList<CheckBox>();
	private final List<Converter> converters = new ArrayList<>();

	@Override
	public Region getMainNode() {
		return mainNode;
	}

	@FXML
	private void initialize() {
		
		cbReadOnly.setTooltip(new Tooltip("Read-only projects cannot be packed and its contents cannot be directly edited. Use this when you want to ensure that you don't override the .package accidentally."));
		
		List<Converter> converters = FormatManager.get().getConverters();
		ListIterator<Converter> it = converters.listIterator(converters.size());
		while (it.hasPrevious()) {
			Converter converter = it.previous();
			CheckBox cb = new CheckBox(converter.getName());
			cb.setSelected(converter.isEnabledByDefault());
			
			converterBoxes.add(cb);
			settingsPane.getChildren().add(cb);
			this.converters.add(converter);
			
			//TODO add converter settings?
		}
		
		nameField.textProperty().addListener((obs, oldValue, newValue) -> {
			if (ProjectManager.get().hasProject(newValue)) {
				ImageView image = UIManager.get().loadIcon("dialog-warning.png");
				image.setFitWidth(18);
				image.setFitHeight(18);
				
				infoLabel.setGraphic(image);
				infoLabel.setText("The project already exists, its contents will be replaced.");
			}
			else {
				infoLabel.setGraphic(null);
				infoLabel.setText("A new project will be created");
			}
		});
	}
	
	private void showInternal(File packageFile) {
		dialog = new Dialog<ButtonType>();
		dialog.getDialogPane().setContent(mainNode);
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		
		dialog.setTitle("Unpacking " + packageFile.getName());
		
		// Set the new project name, removing the extension
		String packageName = packageFile.getName();
		nameField.setText(packageName.substring(0, packageName.lastIndexOf(".")));
		
		if (FileManager.get().isProtectedPackage(packageFile)) {
			cbReadOnly.setSelected(true);
		}
		
		UIManager.get().showDialog(dialog, false).ifPresent(result -> {
			if (result == ButtonType.OK) {
				Project p = ProjectManager.get().getProject(nameField.getText());
				if (p == null) {
					p = new Project(nameField.getText());
				}
				
				final Project project = p;
				
				project.setReadOnly(cbReadOnly.isSelected());
				
				List<Converter> selectedConverters = new ArrayList<Converter>();
				
				for (int i = 0; i < converters.size(); i++) {
					if (converterBoxes.get(i).isSelected()) {
						selectedConverters.add(converters.get(i));
					}
				}
				
				final DBPFUnpackingTask task = new DBPFUnpackingTask(packageFile, project.getFolder(), project, selectedConverters);
				
				ProgressDialogUI progressUI = UIManager.get().loadUI("dialogs/ProgressDialogUI");
				Dialog<ButtonType> progressDialog = progressUI.createDialog(task);
				progressDialog.setTitle("Unpacking " + packageFile.getName());
				
				progressUI.setOnSucceeded(() -> {
					if (task.getValue() != null) {
						UIManager.get().showErrorDialog(task.getValue(), "Could not unpack file.", false);
					}
					else if (task.getExceptions().isEmpty()) {
						Alert alert = new Alert(AlertType.INFORMATION, "Unpack finished", ButtonType.OK);
						alert.setContentText("Successfully unpacked in " + (task.getEllapsedTime() / 1000.0f) + " seconds.");
						UIManager.get().showDialog(alert);
					}
					else {
						showErrorDialog(task);
					}
				});
				
				progressUI.setOnFailed(() -> {
					UIManager.get().showErrorDialog(task.getException(), "Fatal error, file could not be unpacked", true);
				});
				
				// Show progress
				progressUI.getProgressBar().progressProperty().bind(task.progressProperty());
				progressUI.getLabel().textProperty().bind(task.messageProperty());
				
				UIManager.get().showDialog(progressDialog);
				
				// Ensure the overlay is not showing
				UIManager.get().setOverlay(false);
				
				if (!task.isCancelled() && task.getException() == null) {
					ProjectManager.get().setActive(project);
				}
			}
		});
		
		// Ensure the overlay is not showing
		UIManager.get().setOverlay(false);
	}
	
	public static void showErrorDialog(DBPFUnpackingTask task) {
		Alert alert = new Alert(AlertType.ERROR, "Unpack finished", ButtonType.OK);
		
		alert.setContentText("There were errors on " + task.getExceptions().size() + " files, which were not unpacked.");
		
		VBox pane = new VBox();
		ScrollPane scrollPane = new ScrollPane(pane);
		scrollPane.setMaxHeight(400);
		// If we don't put this nothing appears on the scrollpane for some reason
		scrollPane.setPrefHeight(400);
		
		for (Map.Entry<DBPFItem, Exception> entry : task.getExceptions().entrySet()) {
			Button button = new Button(entry.getKey().name.toString());
			button.setMnemonicParsing(false);
			button.getStyleClass().setAll("button-no-background", "hyperlink");
			pane.getChildren().add(button);
			
			button.setOnAction(event -> {
				UIManager.get().showErrorDialog(entry.getValue(), null, false);
			});
		}
		
		alert.getDialogPane().setExpandableContent(scrollPane);
		
		scrollPane.requestFocus();
		
		UIManager.get().showDialog(alert);
	}
	
	public static void show(File packageFile) {
		
		UnpackPackageUI node = UIManager.get().loadUI("dialogs/UnpackPackageUI");
		node.showInternal(packageFile);
	}
	
	public static void showChooser() {
		UIManager.get().setOverlay(true);
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_DBPF);
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
		File result = chooser.showOpenDialog(UIManager.get().getScene().getWindow());
		
		if (result != null) {
			UnpackPackageUI.show(result);
		}
		else {
			// We don't disable the overlay in the other case as we are showing another dialog
			UIManager.get().setOverlay(false);
		}
	}
}
