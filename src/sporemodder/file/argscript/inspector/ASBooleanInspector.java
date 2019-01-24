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

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import sporemodder.DocumentationManager;

public class ASBooleanInspector extends ASValueInspector {
	
	private boolean defaultValue;
	
	private CheckBox checkBox;
	
	private ASValueInspector elseValue;
	
	public ASBooleanInspector(String name, String descriptionCode, boolean defaultValue) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	public ASBooleanInspector orElse(ASValueInspector elseValue) {
		this.elseValue = elseValue;
		return this;
	}
	
	@Override
	public void generateUI(VBox panel) {
		// We don't call super, it's important: we don't want the title and description to be added the common way
		//super.generateUI(panel);
		
		boolean initialValue = defaultValue;
		
		if (getArguments() != null) {
			Boolean value = lineInspector.getStream().parseBoolean(getArguments(), argIndex);
			if (value != null) {
				initialValue = value.booleanValue();
			}
		}
		
		checkBox = new CheckBox();
		checkBox.setText(title);
		checkBox.setTooltip(new Tooltip(DocumentationManager.get().getDocumentation(descriptionCode)));
		checkBox.setSelected(initialValue);
		checkBox.setPrefWidth(Double.MAX_VALUE);
		
		checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
			// If it's not an option, it will come after the keyword
			int splitIndex = argIndex + createIfNecessary();
			
			lineInspector.getSplits().set(splitIndex, Boolean.toString(newValue).toLowerCase());
			
			submitChanges();
			
			if (elseValue != null) {
				elseValue.setEnabled(!newValue);
			}
		});
		
		panel.getChildren().add(checkBox);
		
		VBox.setMargin(checkBox, new Insets(5, 0, 5, 0));
		
		if (elseValue != null) {
			elseValue.setLineInspector(lineInspector, option);
			elseValue.generateUI(panel);
			elseValue.setEnabled(!initialValue);
		}
	}

	@Override
	void addDefaultValue(List<String> splits) {
		splits.add(Boolean.toString(defaultValue).toLowerCase());
	}
	
	@Override
	int getRemoveableCount() {
		return 1;
	}
	
	@Override
	boolean isDefault() {
		Boolean test = lineInspector.getStream().parseBoolean(getArguments(), argIndex);
		if (test == null) return false;
		if (test.booleanValue() != defaultValue) return false;
		
		return true;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		checkBox.setDisable(!isEnabled);
	}
}
