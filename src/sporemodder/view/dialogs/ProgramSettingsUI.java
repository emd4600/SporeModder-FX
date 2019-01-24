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
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import sporemodder.FileManager;
import sporemodder.GameManager;
import sporemodder.GameManager.GameType;
import sporemodder.MainApp;
import sporemodder.UIManager;
import sporemodder.util.GamePathConfiguration.GamePathType;
import sporemodder.util.SporeGame;
import sporemodder.view.Controller;

public class ProgramSettingsUI implements Controller {
	
	@FXML private Node mainNode;
	@FXML private ChoiceBox<String> stylesBox;
	
	@FXML private ChoiceBox<String> gamePathBox;
	@FXML private Button autoDetectButton;
	
	@FXML private TextField sporeField;
	@FXML private Button findSporeButton;
	
	@FXML private TextField gaField;
	@FXML private Button findGAButton;
	
	@FXML private TextField customPathField;
	@FXML private Button findCustomPathButton;
	
	@FXML private TextField commandLineField;
	
	// To set tooltips
	@FXML private Node gameSettingsPanel;
	@FXML private Control commandLineLabel;
	@FXML private Control gameTypeLabel;
	@FXML private Control styleLabel;
	
	private Dialog<ButtonType> dialog;

	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML private void initialize() {
		
		// -- Game Settings -- //
		
		final GameManager gameMgr = GameManager.get();
		
		Tooltip.install(gameSettingsPanel, new Tooltip("The game settings used by default"));
		
		
		autoDetectButton.setOnAction(event -> {
			autoDetect();
		});
		
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
		
		findCustomPathButton.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			if (!customPathField.getText().isEmpty()) {
				File file = new File(customPathField.getText());
				chooser.setInitialDirectory(file.getParentFile());
				chooser.setInitialFileName(file.getName());
			}
			chooser.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
			chooser.getExtensionFilters().add(FileManager.FILEFILTER_EXE);
			
			UIManager.get().setOverlay(true);
			File result = chooser.showOpenDialog(UIManager.get().getScene().getWindow());
			UIManager.get().setOverlay(false);
			
			if (result != null) {
				customPathField.setText(result.getAbsolutePath());
			}
		});
		
		if (gameMgr.hasSpore()) {
			sporeField.setText(gameMgr.getSpore().getInstallFolder().getAbsolutePath());
		}
		
		if (gameMgr.hasGalacticAdventures()) {
			gaField.setText(gameMgr.getGalacticAdventures().getInstallFolder().getAbsolutePath());
		}
		
		if (gameMgr.getAlternativeExecutable() != null) {
			customPathField.setText(gameMgr.getAlternativeExecutable().getAbsolutePath());
		}
		
		
		commandLineField.setText(GameManager.get().getCommandLineArguments());
		commandLineLabel.setTooltip(new Tooltip("The command line arguments that are used when executing the game."));
		
		
		gameTypeLabel.setTooltip(new Tooltip("Which game should SporeModder execute: Spore, Galactic Adventures, or a custom executable."));
		
		gamePathBox.getItems().setAll("Spore", "Galactic Adventures", "Custom");
		
		GamePathType gameType = gameMgr.getGameToExecute();
		int gameIndex = 1;
		if (gameType == GamePathType.SPORE) gameIndex = 0;
		else if (gameType == GamePathType.GALACTIC_ADVENTURES) gameIndex = 1;
		else if (gameType == GamePathType.CUSTOM) gameIndex = 2;
		
		gamePathBox.getSelectionModel().select(gameIndex);
		
		// -- User Interface -- //
		
		styleLabel.setTooltip(new Tooltip("The visual styling used in the program. Changes will be applied on restart."));
		
		stylesBox.getItems().setAll(UIManager.get().getAvailableStyles());
		stylesBox.getSelectionModel().select(UIManager.get().getSelectedStyle());
	}
	
	private void applyChanges() {
		
		// -- Game Settings -- //
		
		GameManager gameMgr = GameManager.get();
		gameMgr.setCommandLineArguments(commandLineField.getText());

		switch (gamePathBox.getSelectionModel().getSelectedItem()) {
		case "Spore":
			gameMgr.setGameToExecute(GamePathType.SPORE);
			break;
		case "Galactic Adventures":
			gameMgr.setGameToExecute(GamePathType.GALACTIC_ADVENTURES);
			break;
		default:
			gameMgr.setGameToExecute(GamePathType.CUSTOM);
			break;
		}
		
		
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
		
		path = customPathField.getText();
		if (path.isEmpty()) {
			gameMgr.setAlternativeExecutable(null);
		} else {
			gameMgr.setAlternativeExecutable(new File(path));
		}
		
		// -- User Interface -- //
		
		UIManager.get().setSelectedStyle(stylesBox.getSelectionModel().getSelectedItem());
		
		MainApp.get().saveSettings();
	}
	
	private void autoDetect() {
		Map<GameType, SporeGame> map = GameManager.get().autoDetectPaths();
		
		if (map.containsKey(GameType.SPORE)) {
			sporeField.setText(map.get(GameType.SPORE).getInstallFolder().getAbsolutePath());
		}
		
		if (map.containsKey(GameType.GA)) {
			gaField.setText(map.get(GameType.GA).getInstallFolder().getAbsolutePath());
		}
	}
	
	private void showInternal() {
		dialog.setTitle("SporeModder FX Settings");
		
		dialog.getDialogPane().setContent(mainNode);
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
		
		UIManager.get().showDialog(dialog).ifPresent(result -> {
			if (result == ButtonType.APPLY) {
				applyChanges();
			}
		});
	}
	
	public static void show() {
		ProgramSettingsUI node = UIManager.get().loadUI("dialogs/ProgramSettingsUI");
		node.dialog = new Dialog<ButtonType>();
		node.showInternal();
	}
}
