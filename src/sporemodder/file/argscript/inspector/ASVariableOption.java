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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import sporemodder.DocumentationManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.util.inspector.InspectorValue;

public class ASVariableOption extends ASAbstractOptionInspector {

	public static class Value {
		public String title;
		public String text;
		public String descriptionCode;
		
		public Value(String name, String text, String descriptionCode) {
			super();
			this.title = name;
			this.text = text;
			this.descriptionCode = descriptionCode;
		}

		@Override
		public String toString() {
			return title;
		}
	}
	
	private final List<Value> enumValues = new ArrayList<Value>();
	private final List<ASOptionGroup> optionGroups = new ArrayList<ASOptionGroup>();
	private Value currentValue;
	private ASOptionGroup currentGroup;
	private Value defaultValue;
	
	private String title;
	private String descriptionCode;
	
	private ComboBox<Value> comboBox;
	private VBox optionsPanel;
	
	public ASVariableOption(String title, String descriptionCode, String defaultValue, Value ... enumValues) {
		this.enumValues.addAll(Arrays.asList(enumValues));
		currentValue = this.defaultValue = getByText(defaultValue);
		
		this.title = title;
		this.descriptionCode = descriptionCode;
	}
	
	public ASVariableOption acceptNone() {
		return this;
	}
	
	public ASVariableOption addOptions(ASOptionGroup group) {
		optionGroups.add(group);
		return this;
	}
	
	@Override
	public void setLineInspector(ASLineInspector<?> lineInspector, boolean updateArguments) {
		super.setLineInspector(lineInspector, updateArguments);
		
		for (ASOptionGroup group : optionGroups) {
			for (ASOptionInspector option : group.getOptions()) {
				// We don't want the arguments to be updated, because only one of the options will exist
				option.setLineInspector(lineInspector, false);
			}
		}
		
		updateArguments();
	}

	@Override
	public void updateArguments() {
		args = new ArgScriptArguments();
		currentValue = null;
		
		for (Value value : enumValues) {
			if (value.text == null) {
				// The none option is expected to come last; if we have arrived here, no other option existed
				currentValue = value;
				// Just a way to know that no option existed
				args = null;
				break;
			}
			else if (lineInspector.getLine().getOptionArguments(args, value.text, 0, Integer.MAX_VALUE)) {
				currentValue = value;
				break;
			}
		}
		
		if (currentValue == null) {
			// Just a way to know that no option existed
			args = null;
			
			// Use the default value
			currentValue = defaultValue;
		}
		
		if (args != null) {
			ASOptionInspector option = null;
			for (ASOptionGroup group : optionGroups) {
				option = group.getOption(currentValue.text);
				if (option != null) {
					option.args = args;
					break;
				}
			}
		}
	}
	
	private Value getByText(String text) {
		for (Value value : enumValues) {
			if (value.text == null) {
				if (text == null) return value;
			}
			else if (value.text.equals(text)) {
				return value;
			}
		}
		return null;
	}
	
	public void setCurrentValue(Value value, boolean updateUI) {
		if (value == currentValue) return;
		
		this.currentValue = value;
		ASOptionGroup newGroup = null;
		ASOptionInspector option = null;
		
		if (value.text == null) {
			// Delete the option
			
			// Keep two lists: the splits that go before the option and the ones that go after
			List<String> beforeSplits = new ArrayList<String>();
			for (int i = 0; i < args.getSplitIndex() - 1; i++) {
				beforeSplits.add(lineInspector.getSplits().get(i));
			}
			
			List<String> afterSplits = new ArrayList<String>();
			for (int i = args.getSplitIndex() + args.get().size(); i < lineInspector.getSplits().size(); i++) {
				afterSplits.add(lineInspector.getSplits().get(i));
			}
			
			lineInspector.getSplits().clear();
			lineInspector.getSplits().addAll(beforeSplits);
			lineInspector.getSplits().addAll(afterSplits);
		}
		else {
			for (ASOptionGroup group : optionGroups) {
				option = group.getOption(value.text);
				if (option != null) {
					newGroup = group;
					break;
				}
			}
			
			List<String> optionSplits = new ArrayList<String>();
			optionSplits.add("-" + value.text);
			
			for (ASValueInspector optionValue : option.getValues()) {
				optionValue.addDefaultValue(optionSplits);
			}
			
			if (args == null) {
				lineInspector.getSplits().addAll(optionSplits);
			}
			else {
				// Keep two lists: the splits that go before the option and the ones that go after
				List<String> beforeSplits = new ArrayList<String>();
				for (int i = 0; i < args.getSplitIndex() - 1; i++) {
					beforeSplits.add(lineInspector.getSplits().get(i));
				}
				
				List<String> afterSplits = new ArrayList<String>();
				for (int i = args.getSplitIndex() + args.get().size(); i < lineInspector.getSplits().size(); i++) {
					afterSplits.add(lineInspector.getSplits().get(i));
				}
				
				// Only keep arguments if the group remains the same
				if (newGroup == currentGroup) {
					int numCommon = currentGroup.getNumberOfCommonArguments();
					int first = currentGroup.getFirstCommonArgument();
					
					for (int i = 0; i < numCommon; i++) {
						// Add one because of the option keyword
						optionSplits.set(1 + first + i, args.get(first + i));
					}
				}
				
				lineInspector.getSplits().clear();
				lineInspector.getSplits().addAll(beforeSplits);
				lineInspector.getSplits().addAll(optionSplits);
				lineInspector.getSplits().addAll(afterSplits);
			}
		}
			
		lineInspector.submitText();
		
		currentGroup = newGroup;
		
		optionsPanel.getChildren().clear();
		if (option != null) {
			option.generateUI(optionsPanel);
		}
		
		if (updateUI) {
			//optionsPanel.updateUI();
		}
	}
	
//	private void updateCurrentValue() {
//		for (Value value : enumValues) {
//			if (lineInspector.getLine().getO)
//		}
//	}

	@Override
	public void generateUI(VBox panel) {
		optionsPanel = new VBox(5);
		optionsPanel.setSpacing(5);
		
		new InspectorValue(title, DocumentationManager.get().getDocumentation(descriptionCode)).generateUI(panel);
		
		
//		updateCurrentValue();
		//setCurrentValue(currentValue, false);
		
		comboBox = new ComboBox<Value>();
		comboBox.getItems().addAll(enumValues);
		comboBox.setValue(currentValue);
		comboBox.setPrefWidth(Double.MAX_VALUE);
		
		comboBox.setOnAction((event) -> {
			setCurrentValue(comboBox.getValue(), true);
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
		            	tooltip.setText(DocumentationManager.get().getDocumentation(item.descriptionCode));
		            	
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
		
		panel.getChildren().add(optionsPanel);
	}

	@Override
	public int createIfNecessary(ASValueInspector caller) {
		return 0;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		comboBox.setDisable(!isEnabled);
		optionsPanel.setDisable(!isEnabled);
	}

}
