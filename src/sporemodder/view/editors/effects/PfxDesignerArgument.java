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
package sporemodder.view.editors.effects;

import sporemodder.view.inspector.InspectorColorPicker;
import sporemodder.view.inspector.InspectorFloatSpinner;
import sporemodder.view.inspector.InspectorIntSpinner;
import sporemodder.view.inspector.InspectorValueList;
import sporemodder.view.inspector.PropertyPane;

public class PfxDesignerArgument {

	private String type;
	private String name;
	private String defaultValue;
	private boolean animatable;
	private boolean optional;
	
	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public boolean isAnimatable() {
		return animatable;
	}
	public boolean isOptional() {
		return optional;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public void setAnimatable(boolean animatable) {
		this.animatable = animatable;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	public void generateUI(PropertyPane pane, PfxDesignerArgumentable caller) {
		if (animatable) {
			//TODO
		} else {
			generateUI_impl(pane, caller.isSingleArgument() ? caller.getName() : name);
		}
	}
	
	private void createColorRGB() {
		
	}
	
	private void addNodeColorRGB(PropertyPane parent, String name) {
		InspectorColorPicker node = new InspectorColorPicker();
		
		parent.add(name, getDescription(), node);
	}
	
	private void addNodeFloat(PropertyPane parent, String name) {
		InspectorFloatSpinner node = new InspectorFloatSpinner();
		
		parent.add(name, getDescription(), node);
	}
	
	private void addNodeInteger(PropertyPane parent, String name) {
		InspectorIntSpinner node = new InspectorIntSpinner();
		
		parent.add(name, getDescription(), node);
	}
	
	private void generateUI_impl(PropertyPane pane, String name) {
		switch (type) {
		case "colorRGB": 
			addNodeColorRGB(pane, name);
			break;
			
		case "float": 
			addNodeFloat(pane, name);
			break;
			
		case "int": 
			addNodeInteger(pane, name);
			break;
		}
	}
	
	private InspectorValueList<?> createValueList() {
		switch (type) {
		case "colorRGB":
		}
	}
	
	private void generateUI_implAnimatable(PropertyPane pane, PfxDesignerArgumentable caller) {
		InspectorValueList<?> list = new InspectorValueList<?>();
	}
	
	public String getDescription() {
		//TODO
		return null;
	}
}
