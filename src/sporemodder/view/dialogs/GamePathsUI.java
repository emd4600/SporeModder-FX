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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import sporemodder.GameManager;
import sporemodder.MainApp;
import sporemodder.UIManager;
import sporemodder.view.Controller;

public class GamePathsUI implements Controller {
	@FXML private Node mainNode;
	
	@FXML private TextField sporeField;
	@FXML private Button findSporeButton;
	
	@FXML private TextField gaField;
	@FXML private Button findGAButton;
	
	private Dialog<ButtonType> dialog;

	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML private void initialize() {
		
		// -- Game Settings -- //
		
		final GameManager gameMgr = GameManager.get();
		
		findSporeButton.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			if (!sporeField.getText().isEmpty()) chooser.setInitialDirectory(new File(sporeField.getText()));
			
			UIManager.get().setOverlay(true);
			File result = chooser.showDialog(UIManager.get().getScene().getWindow());
			UIManager.get().setOverlay(false);
			
			if (result != null) {
				result = gameMgr.findInstallationFolder(GameManager.SPORE_SPOREBIN, result);
				if (result != null) {
					sporeField.setText(result.getAbsolutePath());
				} else {
					Alert alert = new Alert(AlertType.WARNING, "The selected folder is not a valid Spore installation folder.", ButtonType.OK);
					alert.setTitle("Invalid Folder");
					UIManager.get().showDialog(alert);
				}
			}
		});
		
		findGAButton.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			if (!gaField.getText().isEmpty()) chooser.setInitialDirectory(new File(gaField.getText()));
			
			UIManager.get().setOverlay(true);
			File result = chooser.showDialog(UIManager.get().getScene().getWindow());
			UIManager.get().setOverlay(false);
			
			if (result != null) {
				result = gameMgr.findInstallationFolder(GameManager.GA_SPOREBIN, result);
				if (result != null) {
					gaField.setText(result.getAbsolutePath());
				} else {
					Alert alert = new Alert(AlertType.WARNING, "The selected folder is not a valid Galactic Adventures installation folder.", ButtonType.OK);
					alert.setTitle("Invalid Folder");
					UIManager.get().showDialog(alert);
				}
			}
		});
		
		if (gameMgr.hasSpore()) {
			sporeField.setText(gameMgr.getSpore().getInstallFolder().getAbsolutePath());
		}
		
		if (gameMgr.hasGalacticAdventures()) {
			gaField.setText(gameMgr.getGalacticAdventures().getInstallFolder().getAbsolutePath());
		}
	}
	
	private void applyChanges() {
		
		// -- Game Settings -- //
		
		GameManager gameMgr = GameManager.get();
		
		// Only save changes if they are different from the existing ones (which might be automatic)
		
		String path = sporeField.getText();
		if (path.isEmpty()) {
			gameMgr.setSpore(null);
		}
		else if (!gameMgr.hasSpore() || !path.equals(gameMgr.getSpore().getInstallFolder().getAbsolutePath())) {
			gameMgr.setSpore(gameMgr.createSpore(path));
		} 
		
		path = gaField.getText();
		if (path.isEmpty()) {
			gameMgr.setGalacticAdventures(null);
		}
		else if (!gameMgr.hasGalacticAdventures() || !path.equals(gameMgr.getGalacticAdventures().getInstallFolder().getAbsolutePath())) {
			gameMgr.setGalacticAdventures(gameMgr.createGA(path));
		}
		
		MainApp.get().saveSettings();
	}
	
	private void showInternal(boolean disableOverlayOnClose) {
		dialog.setTitle("Find the paths to Spore");
		
		dialog.getDialogPane().setContent(mainNode);
		
		// We use OK isntead of Apply to respect the same order as the UnpackPresetsUI dialog
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(true);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Skip");
		
		UIManager.get().showDialog(dialog, disableOverlayOnClose).ifPresent(result -> {
			if (result == ButtonType.APPLY) {
				applyChanges();
			}
		});
	}
	
	public static void show(boolean disableOverlayOnClose) {
		GamePathsUI node = UIManager.get().loadUI("dialogs/GamePathsUI");
		node.dialog = new Dialog<ButtonType>();
		node.showInternal(disableOverlayOnClose);
	}
}
