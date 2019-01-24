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
import sporemodder.util.ColorRGB;

public class ASColorRGBInspector extends ASValueInspector {
	
	private ColorRGB defaultValue = ColorRGB.white();
	private boolean is255;
	private ColorPicker colorPicker;
	
	public ASColorRGBInspector(String name, String descriptionCode, ColorRGB defaultValue) {
		this(name, descriptionCode, defaultValue, false);
	}
	
	public ASColorRGBInspector(String name, String descriptionCode, ColorRGB defaultValue, boolean is255) {
		super(name, descriptionCode);
		this.defaultValue = defaultValue;
		this.is255 = is255;
	}
	
	@Override
	public int getArgumentCount() {
		return 1;
	}
	
	private String colorToString(ColorRGB color) {
		String str = color.toString();
		return str.substring(1, str.length() - 1);
	}
	
	private String colorToString255(ColorRGB color) {
		String str = color.toString255();
		return str.substring(1, str.length() - 1);
	}
	
	@Override
	public void generateUI(VBox panel) {
		super.generateUI(panel);
		
		ColorRGB initialValue = new ColorRGB(defaultValue);
		
		// Only do this if the option exists
		if (getArguments() != null) {
			if (is255) lineInspector.getStream().parseColorRGB255(getArguments(), argIndex, initialValue);
			else lineInspector.getStream().parseColorRGB(getArguments(), argIndex, initialValue);
		}
		
		colorPicker = new ColorPicker();
		colorPicker.setValue(initialValue.toColor());
		colorPicker.setPrefWidth(Double.MAX_VALUE);
		
		colorPicker.setOnAction((event) -> {
			int splitIndex = argIndex + createIfNecessary();
			
			if (is255) lineInspector.getSplits().set(splitIndex, colorToString255(new ColorRGB(colorPicker.getValue())));
			else lineInspector.getSplits().set(splitIndex, colorToString(new ColorRGB(colorPicker.getValue())));
			
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
			ColorRGB test = new ColorRGB();
			
			if (is255) lineInspector.getStream().parseColorRGB255(getArguments(), argIndex, test);
			else lineInspector.getStream().parseColorRGB(getArguments(), argIndex, test);

			return test.equals(defaultValue);
		}
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		colorPicker.setDisable(!isEnabled);
	}
}
