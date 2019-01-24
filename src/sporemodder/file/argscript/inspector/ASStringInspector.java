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

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ASStringInspector extends ASValueInspector {
	
	private String defaultValue = null;
	private TextField tf;
	
	public ASStringInspector(String name, String descriptionCode, String defaultValue) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		String initialValue = getArguments() == null ? defaultValue : getArgument(0);
		
		tf = new TextField();
		tf.setPrefWidth(Double.MAX_VALUE);
		tf.setText(initialValue);
		
		tf.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				finishEdit(tf.getText());
			}
		});
		
		tf.setOnAction((event) -> {
			finishEdit(tf.getText());
		});
		
		panel.getChildren().add(tf);
	}
	
	private void finishEdit(String text) {
		int splitIndex = argIndex + createIfNecessary();
		
		if (text.trim().isEmpty()) {
			if (isOptional || defaultValue == null) {
				text = "__remove__";
			}
			else {
				text = defaultValue;
				tf.setText(defaultValue);
			}
			
		}
		
		lineInspector.getSplits().set(splitIndex, text);
		
		submitChanges();
	}

	@Override
	void addDefaultValue(List<String> splits) {
		splits.add(defaultValue);
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
			return getArgument(0).trim().isEmpty() || getArgument(0).equals("__remove__");
		}
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		tf.setDisable(!isEnabled);
	}
}
