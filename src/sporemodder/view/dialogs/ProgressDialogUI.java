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

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import sporemodder.UIManager;
import sporemodder.util.ResumableTask;
import sporemodder.view.Controller;

public class ProgressDialogUI implements Controller {

	@FXML
	private Region mainNode;

	@FXML
	private Label label;
	
	@FXML
	private ProgressBar progressBar;
	
	private Runnable onCancelled;
	private Runnable onSucceeded;
	
	@Override
	public Region getMainNode() {
		return mainNode;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public void setText(String text) {
		label.setText(text);
	}
	
	public void setOnCancelled(Runnable onCancelled) {
		this.onCancelled = onCancelled;
	}
	
	public void setOnSucceeded(Runnable onSucceeded) {
		this.onSucceeded = onSucceeded;
	}
	
	public Label getLabel() {
		return label;
	}
	
	public Dialog<ButtonType> createDialog(ResumableTask<?> task) {
		
		Dialog<ButtonType> progressDialog = new Dialog<ButtonType>();
		progressDialog.getDialogPane().setContent(getMainNode());
		progressDialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL);
		
		// If this is false, it will ask for confirmation of the user
		final AtomicBoolean forceClose = new AtomicBoolean(false);
		
		progressDialog.setOnShowing(event -> {
			new Thread(task).start();
		});
		
		progressDialog.setOnCloseRequest(event -> {
			
			if (forceClose.get()) {
				return;
			}
			
			Alert questionAlert = new Alert(AlertType.WARNING, "Confirm cancellation", ButtonType.NO, ButtonType.YES);
			questionAlert.setHeaderText("Are you sure you want to cancel?");
			questionAlert.setContentText("All the progress will be lost!");
			
			task.pause();
			
			UIManager.get().showDialog(questionAlert, false).ifPresent(questionResult -> {
				if (questionResult == ButtonType.YES) {
					// Cancel the task and close the dialog. No need to remove the already unpacked content
					task.cancel();
					forceClose.set(true);
				}
				else {
					task.resume();
				}
			});
			
			if (forceClose.get()) {
				return;
			}
			else {
				// When we arrive here, either the progress has been closed programmatically or there's no need to close it
				// So consume the event
				event.consume();
			}
		});
		
		
		// Don't request close, as that triggers the "Are you sure?" dialog
		task.setOnSucceeded(ev2 -> {
			forceClose.set(true);
			progressDialog.close();
			
			if (onSucceeded != null) onSucceeded.run();
		});
		task.setOnCancelled(ev2 -> {
			forceClose.set(true);
			progressDialog.close();
			
			if (onCancelled != null) onCancelled.run();
		});
		
		return progressDialog;
	}
}
