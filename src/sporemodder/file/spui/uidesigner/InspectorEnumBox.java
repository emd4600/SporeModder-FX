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
package sporemodder.file.spui.uidesigner;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import sporemodder.view.inspector.InspectorValue;

public class InspectorEnumBox implements InspectorValue<Integer> {
	
	private final ComboBox<String> node = new ComboBox<String>();
	private final DesignerEnum designerEnum;
	
	private final List<ChangeListener<Integer>> listeners = new ArrayList<>();
	
	// Use this to avoid sending multiple events with just one change
	private boolean settingValue;
	
	public InspectorEnumBox(DesignerEnum designerEnum) {
		super();
		this.designerEnum = designerEnum;
		
		node.getItems().addAll(designerEnum.getStringValues());
		
		node.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (!settingValue) updateListeners(designerEnum.get(oldValue), designerEnum.get(newValue));
		});
	}

	@Override
	public Integer getValue() {
		return designerEnum.get(node.getSelectionModel().getSelectedItem());
	}

	@Override
	public void setValue(Integer value) {
		settingValue = true;
		node.getSelectionModel().select(designerEnum.get(value));
		settingValue = false;
	}

	@Override
	public ComboBox<String> getNode() {
		return node;
	}

	public SingleSelectionModel<String> getSelectionModel() {
		return node.getSelectionModel();
	}
	
	private void updateListeners(int oldValue, int newValue) {
		for (ChangeListener<Integer> listener : listeners) {
			listener.changed(null, oldValue, newValue);
		}
	}
	
	@Override public void addValueListener(ChangeListener<Integer> listener) {
		listeners.add(listener);
	}
	
	@Override public void removeValueListener(ChangeListener<Integer> listener) {
		listeners.remove(listener);
	}
}
