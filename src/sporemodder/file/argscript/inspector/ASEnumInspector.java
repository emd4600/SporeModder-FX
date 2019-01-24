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
package sporemodder.file.argscript.inspector;

import java.util.List;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class ASEnumInspector extends ASValueInspector {
	
	public static class Value {
		public String title;
		public String text;
		public String descriptionCode;
		
		public Value(String title, String text, String descriptionCode) {
			super();
			this.title = title;
			this.text = text;
			this.descriptionCode = descriptionCode;
		}

		@Override
		public String toString() {
			return title;
		}
	}
	
	private String defaultValue;
	private final Value[] values;
	private ComboBox<Value> comboBox;
	
	public ASEnumInspector(String name, String descriptionCode, String defaultValue, Value ... values) {
		super(name, descriptionCode);
		this.values = values;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	private Value getByText(String text) {
		for (Value value : values) {
			if (value.text.equals(text)) {
				return value;
			}
		}
		return null;
	}
	
	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		String initialValue = defaultValue;
		
		// Only do this if the option exists
		if (getArguments() != null) {
			initialValue = getArgument(0);
		}
		
		Value value = getByText(initialValue);
		if (value == null) getByText(defaultValue);
		
		comboBox = new ComboBox<Value>();
		comboBox.getItems().addAll(values);
		comboBox.setPrefWidth(Double.MAX_VALUE);
		comboBox.setValue(value);
		
		comboBox.setOnAction((event) -> {
			// If it's not an option, it will come after the keyword
			int splitIndex = argIndex + createIfNecessary();
			
			lineInspector.getSplits().set(splitIndex, comboBox.getValue().text);
			
			submitChanges();
		});
		
		// Tooltips not supported by default; add them here:
		comboBox.setCellFactory((param) -> {
			return new ListCell<Value>() {
				@Override
		        public void updateItem(Value item, boolean empty) {
		            super.updateItem(item, empty);
		            
		            if (item != null) {
		            	setText(item.toString());
		            	
		            	Tooltip tooltip = new Tooltip();
		            	tooltip.setText(item.descriptionCode);
		            	
		            	setTooltip(tooltip);
		            }
		            else {
		            	setText(null);
		            	setTooltip(null);
		            }
				}
			};
		});
		
		panel.getChildren().add(comboBox);
	}

	@Override
	void addDefaultValue(List<String> splits) {
		splits.add(defaultValue.toString());
	}
	
	@Override
	int getRemoveableCount() {
		return 1;
	}
	
	@Override
	boolean isDefault() {
		if (getArguments() == null) {
			return true;
		}
		else {
			return getArgument(0).equals(defaultValue);
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		comboBox.setDisable(!isEnabled);
	}
}
