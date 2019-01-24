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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectPreset;
import sporemodder.view.Controller;

public class UnpackPresetsUI implements Controller {

private Dialog<ButtonType> dialog;
	
	@FXML private Pane mainNode;
	@FXML private Pane presetsPane;
	@FXML private Label label;
	
	private final List<CheckBox> presetBoxes = new ArrayList<CheckBox>();
	private boolean requiresPresets = true;
	
	@Override
	public Pane getMainNode() {
		return mainNode;
	}
	
	private void show(boolean disableOverlayOnClose) {
		UIManager.get().showDialog(dialog, disableOverlayOnClose).ifPresent(result -> {
			if (result == ButtonType.OK) {
				
				List<ProjectPreset> presets = ProjectManager.get().getPresets();
				List<ProjectPreset> selectedPresets = new ArrayList<ProjectPreset>();
				for (int i = 0; i < presetBoxes.size(); i++) {
					if (presetBoxes.get(i).isSelected()) {
						selectedPresets.add(presets.get(i));
					}
				}
				
				if (!selectedPresets.isEmpty()) {
					ProjectManager.get().unpackPresets(selectedPresets);
				}
			}
		});
	}
	
	public static void show(String overrideText, boolean disableOverlayOnClose) {
		UnpackPresetsUI node = UIManager.get().loadUI("dialogs/UnpackPresetsUI");
		node.dialog = new Dialog<ButtonType>();
		node.dialog.getDialogPane().setContent(node.getMainNode());
		
		node.dialog.setTitle("Unpack Presets");
		node.dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		
		if (overrideText != null) {
			node.label.setText(overrideText);
		}
		
		node.show(disableOverlayOnClose);
	}
	
	public static void showAsOptional(String overrideText, boolean disableOverlayOnClose) {
		UnpackPresetsUI node = UIManager.get().loadUI("dialogs/UnpackPresetsUI");
		node.dialog = new Dialog<ButtonType>();
		node.dialog.getDialogPane().setContent(node.getMainNode());
		
		node.dialog.setTitle("Unpack Presets");
		node.dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
		((Button) node.dialog.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(true);
		((Button) node.dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Skip");
		
		if (overrideText != null) {
			node.label.setText(overrideText);
		}
		
		node.show(disableOverlayOnClose);
	}

	@FXML
	private void initialize() {
		
		List<ProjectPreset> presets = ProjectManager.get().getPresets();
		for (ProjectPreset preset : presets) {
			CheckBox checkBox = new CheckBox(preset.getName());
			presetBoxes.add(checkBox);
			
			checkBox.setUserData(preset);
			checkBox.setTooltip(preset.buildTooltip());
			checkBox.setSelected(preset.isRecommendable());
			
			checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
				boolean selected = false;
				
				for (CheckBox cb : presetBoxes) {
					if (cb.isSelected()) {
						selected = true;
						break;
					}
				}
				
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!selected && requiresPresets);
			});
			
			mainNode.getChildren().add(checkBox);
		}
	}
}
