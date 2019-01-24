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

import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import sporemodder.util.ColorRGBA;

public class ASColorRGBAInspector extends ASValueInspector {
	
	private ColorRGBA defaultValue = ColorRGBA.white();
	private boolean is255;
	private ColorPicker colorPicker;
	
	public ASColorRGBAInspector(String name, String descriptionCode, ColorRGBA defaultValue) {
		this(name, descriptionCode, defaultValue, false);
	}
	
	public ASColorRGBAInspector(String name, String descriptionCode, ColorRGBA defaultValue, boolean is255) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
		this.is255 = is255;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	private String colorToString(ColorRGBA color) {
		String str = color.toString();
		return str.substring(1, str.length() - 1);
	}
	
	private String colorToString255(ColorRGBA color) {
		String str = color.toString255();
		return str.substring(1, str.length() - 1);
	}
	
	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		ColorRGBA initialValue = new ColorRGBA(defaultValue);
		
		// Only do this if the option exists
		if (getArguments() != null) {
			if (is255) lineInspector.getStream().parseColorRGBA255(getArguments(), argIndex, initialValue);
			else lineInspector.getStream().parseColorRGBA(getArguments(), argIndex, initialValue);
		}
		
		colorPicker = new ColorPicker();
		colorPicker.setValue(initialValue.toColor());
		colorPicker.setPrefWidth(Double.MAX_VALUE);
		
		colorPicker.setOnAction((event) -> {
			// If it's not an option, it will come after the keyword
			int splitIndex = argIndex + createIfNecessary();
			
			if (is255) lineInspector.getSplits().set(splitIndex, colorToString255(new ColorRGBA(colorPicker.getValue())));
			else lineInspector.getSplits().set(splitIndex, colorToString(new ColorRGBA(colorPicker.getValue())));
			
			submitChanges();
		});
		
		panel.getChildren().add(colorPicker);
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
			ColorRGBA test = new ColorRGBA();
			
			if (is255) lineInspector.getStream().parseColorRGBA255(getArguments(), argIndex, test);
			else lineInspector.getStream().parseColorRGBA(getArguments(), argIndex, test);

			return test.equals(defaultValue);
		}
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		colorPicker.setDisable(!isEnabled);
	}
}
