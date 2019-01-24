/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

package sporemodder.view.ribbons.util;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import sporemodder.HashManager;
import sporemodder.util.NameRegistry;
import sporemodder.view.Controller;

public class HashConverterUI implements Controller {
	
	private static final String FNV_HASH = "FNV Hash";
	
	@FXML
	private Node mainNode;
	
	@FXML
	private TextField tfName;
	@FXML
	private TextField tfHash;
	
	private TextField lastTextField;
	
	@FXML
	private ChoiceBox<String> cbRegister;
	
	private boolean settingValue;

	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML
	protected void initialize() {
		cbRegister.getItems().add(FNV_HASH);
		cbRegister.setValue(FNV_HASH);
		
		cbRegister.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (lastTextField != null && !settingValue && !lastTextField.getText().isEmpty()) {
				if (lastTextField == tfName) {	
					settingValue = true;
					
					int hash;
					NameRegistry registry = HashManager.get().getRegistryByDescription(newValue);
					if (registry == null) {
						hash = HashManager.get().fnvHash(tfName.getText());
					} else {
						hash = registry.getHash(tfName.getText());
					}
					
					tfHash.setText(HashManager.get().hexToStringUC(hash));
					
					settingValue = false;
				} else {
					settingValue = true;
					
					tfName.setText(HashManager.get().getRegistryByDescription(cbRegister.getValue()).getName(parseHash(tfName.getText())));
					
					settingValue = false;
				}
			}
		});
		
		tfName.textProperty().addListener((obs, oldValue, newValue) -> {
			if (!settingValue) {
				settingValue = true;
				
				processText(newValue);
				lastTextField = tfName;
				
				settingValue = false;
			}
		});
		
		tfHash.textProperty().addListener((obs, oldValue, newValue) -> {
			if (!settingValue) {
				settingValue = true;
				
				processHash(newValue);
				lastTextField = tfHash;
				
				settingValue = false;
			}
		});
	}
	
	private void processText(String name) {
		
		if (name == null || name.isEmpty()) {
			tfHash.setText("");
			cbRegister.getItems().setAll("Not found");
			cbRegister.getSelectionModel().select(0);
			return;
		}
		
		HashManager hasher = HashManager.get();
		
		if (name.endsWith("~")) {
			// Special case, only file registry available
			Integer value = hasher.getFileRegistry().getHash(name);
			if (value == null) {
				tfHash.setText("");
				cbRegister.getItems().setAll("Not found");
				cbRegister.getSelectionModel().select(0);
			} else {
				cbRegister.getItems().setAll(hasher.getFileRegistry().getFileName());
				cbRegister.getSelectionModel().select(0);
				
				tfHash.setText(HashManager.get().hexToStringUC(value));
			}
		}
		else {
			Integer value;
			cbRegister.getItems().clear();
			
			value = hasher.getTypeRegistry().getHash(name);
			if (value != null) {
				cbRegister.getItems().add(hasher.getTypeRegistry().getDescription());
			}
			
			value = hasher.getSimulatorRegistry().getHash(name);
			if (value != null) {
				cbRegister.getItems().add(hasher.getSimulatorRegistry().getDescription());
			}
			
			value = hasher.getPropRegistry().getHash(name);
			if (value != null) {
				cbRegister.getItems().add(hasher.getPropRegistry().getDescription());
			}
			
			value = hasher.getFileRegistry().getHash(name);
			if (value != null) {
				cbRegister.getItems().add(hasher.getFileRegistry().getDescription());
			}
			
			value = hasher.getProjectRegistry().getHash(name);
			if (value != null) {
				cbRegister.getItems().add(hasher.getFileRegistry().getDescription());
			}
			
			cbRegister.getItems().add(FNV_HASH);
			
			cbRegister.getSelectionModel().select(0);
			
			int hash;
			NameRegistry registry = hasher.getRegistryByDescription(cbRegister.getValue());
			if (registry == null) {
				hash = hasher.fnvHash(name);
			} else {
				hash = registry.getHash(name);
			}
			
			tfHash.setText(HashManager.get().hexToStringUC(hash));
		}
	}
	
	private int parseHash(String hashText) {
		if (hashText.startsWith("0x")) {
			return Integer.parseUnsignedInt(hashText.substring(2), 16);
		} else if (hashText.startsWith("#")) {
			return Integer.parseUnsignedInt(hashText.substring(1), 16);
		} else {
			return Integer.parseUnsignedInt(hashText, 16);
		}
	}
	
	private void processHash(String hashText) {
		
		if (hashText == null || hashText.isEmpty()) {
			tfName.setText("");
			cbRegister.getItems().setAll("Not found");
			cbRegister.getSelectionModel().select(0);
			tfHash.setStyle(null);
			return;
		}
		
		HashManager hasher = HashManager.get();
		
		int hash;
		
		try {
			hash = parseHash(hashText);
		} catch (Exception e) {
			tfHash.setStyle("-fx-background-color: red;");
			return;
		}
		
		tfHash.setStyle(null);
		
		String value;
		cbRegister.getItems().clear();
		
		value = hasher.getTypeRegistry().getName(hash);
		if (value != null) {
			cbRegister.getItems().add(hasher.getTypeRegistry().getDescription());
		}
		
		value = hasher.getSimulatorRegistry().getName(hash);
		if (value != null) {
			cbRegister.getItems().add(hasher.getSimulatorRegistry().getDescription());
		}
		
		value = hasher.getPropRegistry().getName(hash);
		if (value != null) {
			cbRegister.getItems().add(hasher.getPropRegistry().getDescription());
		}
		
		value = hasher.getFileRegistry().getName(hash);
		if (value != null) {
			cbRegister.getItems().add(hasher.getFileRegistry().getDescription());
		}
		
		value = hasher.getFileRegistry().getName(hash);
		if (value != null) {
			cbRegister.getItems().add(hasher.getProjectRegistry().getDescription());
		}
		
		if (cbRegister.getItems().isEmpty()) {
			tfName.setText("");
			cbRegister.getItems().setAll("Not found");
			cbRegister.getSelectionModel().select(0);
		}
		else {
			cbRegister.getSelectionModel().select(0);
			
			tfName.setText(hasher.getRegistryByDescription(cbRegister.getValue()).getName(hash));
		}
	}
}
