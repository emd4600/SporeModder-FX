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
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import sporemodder.DocumentationManager;
import sporemodder.file.argscript.ArgScriptArguments;

public class ASFlagOption extends ASAbstractOptionInspector {
	
	private String title;
	private String descriptionCode;
	private String name;
	
	private CheckBox checkBox;
	
	private ASAbstractOptionInspector elseOption;
	private final List<String> elseSplits = new ArrayList<String>();
	
	public ASFlagOption(String name, String title, String descriptionCode) {
		super();
		this.title = title;
		this.descriptionCode = descriptionCode;
		this.name = name;
	}

	@Override
	public void updateArguments() {
		args = new ArgScriptArguments();
		if (!lineInspector.getLine().getOptionArguments(args, name, 0)) {
			args = null;
		}
		
		if (elseOption != null) {
			elseOption.updateArguments();
		}
	}
	
	public ASFlagOption orElse(ASAbstractOptionInspector elseOption) {
		this.elseOption = elseOption;
		return this;
	}

	@Override
	public void generateUI(VBox panel) {
		boolean initialValue = args != null;
		
		checkBox = new CheckBox();
		checkBox.setText(title);
		checkBox.setTooltip(new Tooltip(DocumentationManager.get().getDocumentation(descriptionCode)));
		checkBox.setSelected(initialValue);
		checkBox.setPrefWidth(Double.MAX_VALUE);
		
		checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
			
			if (newValue.booleanValue()) {
				lineInspector.getSplits().add("-" + name);
			}
			else {
				lineInspector.getSplits().remove(args.getSplitIndex() - 1);
			}
			
			if (elseOption != null) {
				elseOption.setEnabled(!newValue.booleanValue());
				
				if (newValue.booleanValue()) {
					deleteElseOption();
				} else {
					restoreElseOption();
				}
			}
			
			lineInspector.submitText();
		});
		
		panel.getChildren().add(checkBox);
		
		VBox.setMargin(checkBox, new Insets(7, 0, 7, 0));
		
		if (elseOption != null) {
			elseOption.setLineInspector(lineInspector, true);
			elseOption.generateUI(panel);
			elseOption.setEnabled(!initialValue);
		}
	}
	
	private void deleteElseOption() {
		ArgScriptArguments args = elseOption.getArguments();
		
		if (args != null) {
			elseSplits.clear();
			int splitIndex = args.getSplitIndex();
			
			elseSplits.add(lineInspector.getSplits().get(splitIndex - 1));
			
			for (int i = 0; i < args.size(); i++) {
				elseSplits.add(lineInspector.getSplits().get(splitIndex));
				lineInspector.getSplits().remove(splitIndex);
			}
			
			// Remove the keyword
			lineInspector.getSplits().remove(splitIndex - 1);
		}
	}
	
	private void restoreElseOption() {
		for (String split : elseSplits) {
			lineInspector.getSplits().add(split);
		}
	}

	@Override
	public int createIfNecessary(ASValueInspector value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		checkBox.setDisable(!isEnabled);
	}

}
