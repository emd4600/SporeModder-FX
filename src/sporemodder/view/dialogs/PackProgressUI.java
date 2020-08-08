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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import sporemodder.EditorManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.dbpf.DBPFPackingTask;
import sporemodder.util.Project;
import sporemodder.util.ProjectItem;
import sporemodder.view.Controller;

public class PackProgressUI implements Controller {
	
	@FXML
	private DialogPane mainNode;
	
	@FXML
	private ProgressBar progressBar;
	
	private Dialog<Void> dialog;
	
	private Thread taskThread;
	private boolean forceClose;

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public Dialog<Void> getDialog() {
		return dialog;
	}

	private void showInternal(File inputFolder, String projectName, DBPFPackingTask task) {
		dialog = new Dialog<Void>();
		dialog.setTitle("Packing '" + projectName + "'");
		dialog.setDialogPane(mainNode);
		dialog.setResizable(false);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		
		dialog.setOnShowing(event -> {
			taskThread = new Thread(task);
			taskThread.start();
		});
		
		dialog.setOnCloseRequest(event -> {
			
			if (forceClose) return;
			
			Alert questionAlert = new Alert(AlertType.WARNING, "Confirm cancellation", ButtonType.NO, ButtonType.YES);
			questionAlert.setHeaderText("Are you sure you want to cancel?");
			questionAlert.setContentText("All the progress will be lost!");
			
			task.pause();
			
			UIManager.get().showDialog(questionAlert, false).ifPresent(questionResult -> {
				if (questionResult == ButtonType.YES) {
					// Cancel the task and close the dialog. No need to remove the already unpacked content
					task.cancel();
					forceClose = true;
				}
				else {
					task.resume();
				}
			});
			
			if (forceClose) {
				return;
			}
			else {
				// When we arrive here, either the progress has been closed programmatically or there's no need to close it
				// So consume the event
				event.consume();
			}
		});
		
		
		// Don't request close, as that triggers the "Are you sure?" dialog
		task.setOnSucceeded(event -> {
			forceClose = true;
			dialog.close();
			
			if (task.getFailException() != null) {
				showErrorDialog(inputFolder, task);
				
				String relativePath = task.getCurrentFile().getAbsolutePath().substring(inputFolder.getAbsolutePath().length() + 1);
				ProjectItem item = ProjectManager.get().getItem(relativePath);
				EditorManager mgr = EditorManager.get();
				try {
					if (mgr.hasItem(item)) {
						mgr.loadFile(item);
					}
					else {
						mgr.loadFile(item);
						mgr.moveFileToNewTab(item);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		task.setOnCancelled(event -> {
			forceClose = true;
			dialog.close();
		});
		
		// Show progress
		progressBar.progressProperty().bind(task.progressProperty());
		
		UIManager.get().setOverlay(true);
		UIManager.get().showDialog(dialog, false);
		
		// Ensure the overlay is not showing
		UIManager.get().setOverlay(false);
	}
	
	private void showErrorDialog(File folder, DBPFPackingTask task) {
		Alert alert = new Alert(AlertType.ERROR, "The project was not packed", ButtonType.OK);
		
		String errorText;
		if (task.getCurrentFile() != null) {
			String relativePath = task.getCurrentFile().getAbsolutePath().substring(folder.getAbsolutePath().length());
			errorText = "The project was not packed due to an error in file " + relativePath;
		}
		else {
			errorText = "The project was not packed";
		}
		
		alert.setContentText(errorText + ":\n" + task.getFailException().getLocalizedMessage());
		alert.getDialogPane().setExpandableContent(UIManager.get().createExceptionArea(task.getFailException()));
		
		UIManager.get().showDialog(alert, false);
	}
	
	public static boolean show(Project project, boolean storeDebugInformation) {
		PackProgressUI controller = UIManager.get().loadUI("dialogs/PackProgressUI");
		DBPFPackingTask task = new DBPFPackingTask(project, storeDebugInformation);
		
		controller.showInternal(project.getFolder(), project.getName(), task);
		
		// Update the UI
		UIManager.get().notifyUIUpdate(false);
		
		return task.getFailException() == null && !task.isCancelled();
	}
	
	public static boolean show(File folder, File outputFile, String projectName, boolean storeDebugInformation) {
		PackProgressUI controller = UIManager.get().loadUI("dialogs/PackProgressUI");
		DBPFPackingTask task = new DBPFPackingTask(folder, outputFile);
		
		controller.showInternal(folder, projectName, task);
		
		// Update the UI
		UIManager.get().notifyUIUpdate(false);
		
		return task.getFailException() == null && !task.isCancelled();
	}
}
