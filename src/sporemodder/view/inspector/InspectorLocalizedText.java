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
package sporemodder.view.inspector;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import sporemodder.HashManager;
import sporemodder.file.LocalizedText;

public class InspectorLocalizedText implements InspectorValue<LocalizedText> {

	private RadioButton btnText = new RadioButton();
	private RadioButton btnLocale = new RadioButton();
	private TextField tfText = new TextField();
	private TextField tfTable = new TextField();
	private TextField tfInstance = new TextField();
	
	private final PropertyPane pane = new PropertyPane();
	
	private final LocalizedText oldValue = new LocalizedText();
	private final LocalizedText text;
	
	private final List<ChangeListener<LocalizedText>> listeners = new ArrayList<>();
	
	// Use this to avoid sending multiple events with just one change
	private boolean settingValue;
	
	public InspectorLocalizedText(LocalizedText text) {
		if (text == null) {
			throw new NullPointerException("Must specify a LocalizedText for the inspector to modify.");
		}
		this.text = text;
		oldValue.copy(text);
		
		HBox hbox = new HBox(5);
		hbox.getChildren().addAll(tfTable, tfInstance);
		
		tfText.setPromptText("Text");
		tfTable.setPromptText("tableID");
		tfInstance.setPromptText("instanceID");
		
		tfText.setPrefColumnCount(20);
		tfTable.setPrefColumnCount(10);
		tfInstance.setPrefColumnCount(10);
		
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(btnText, btnLocale);
		
		group.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == btnText) {
				tfText.setDisable(false);
				tfTable.setDisable(true);
				tfInstance.setDisable(true);
			} else {
				tfText.setDisable(true);
				tfTable.setDisable(false);
				tfInstance.setDisable(false);
			}
		});
	
		loadValue();
		
		tfText.textProperty().addListener((obs, old, newValue) -> {
			oldValue.copy(text);
			text.setText(newValue);
			
			if (!settingValue) updateListeners();
		});
		tfTable.textProperty().addListener((obs, old, newValue) -> {
			oldValue.copy(text);
			text.setTableID(HashManager.get().getFileHash(newValue));
			
			if (!settingValue) updateListeners();
		});
		tfInstance.textProperty().addListener((obs, old, newValue) -> {
			oldValue.copy(text);
			text.setInstanceID(HashManager.get().getFileHash(newValue));
			
			if (!settingValue) updateListeners();
		});
		
		pane.add(btnText, tfText);
		pane.add(btnLocale, hbox);
	}
	
	private void updateListeners() {
		LocalizedText oldValue = new LocalizedText(this.oldValue);
		LocalizedText newValue = new LocalizedText(this.text);
		for (ChangeListener<LocalizedText> listener : listeners) {
			listener.changed(null, oldValue, newValue);
		}
	}
	
	@Override public void addValueListener(ChangeListener<LocalizedText> listener) {
		listeners.add(listener);
	}
	
	@Override public void removeValueListener(ChangeListener<LocalizedText> listener) {
		listeners.remove(listener);
	}
	
	private void loadValue() {
		if (text.getTableID() != 0) {
			tfTable.setText(HashManager.get().getFileName(text.getTableID()));
		}
		if (text.getInstanceID() != 0) {
			tfInstance.setText(HashManager.get().getFileName(text.getInstanceID()));
		}
		if (text.getText() != null) {
			tfText.setText(text.getText());
		}
		
		if (text.getInstanceID() == 0 || text.getTableID() == 0) {
			btnText.setSelected(true);
		} else {
			btnLocale.setSelected(true);
		}
	}
	
	@Override
	public LocalizedText getValue() {
		return text;
	}

	@Override
	public void setValue(LocalizedText value) {
		oldValue.copy(text);
		text.copy(value);
		
		settingValue = true;
		loadValue();
		settingValue = false;
	}

	@Override
	public Node getNode() {
		return pane.getNode();
	}
}
