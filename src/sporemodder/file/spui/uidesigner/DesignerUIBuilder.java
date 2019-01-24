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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sporemodder.file.spui.SpuiElement;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.inspector.PropertyPane;

public class DesignerUIBuilder {

	private final List<DesignerElement> elements = new ArrayList<>();
	private final Map<String, DesignerCategory> categories = new HashMap<>();
	private final Map<Integer, DesignerProperty> properties = new HashMap<>();
	private DesignerCategory currentCategory;
	
	public DesignerCategory getCategory(String name) {
		return categories.get(name);
	}
	
	public DesignerProperty getProperty(int proxyID) {
		return properties.get(proxyID);
	}
	
	public void remove(DesignerElement element) {
		if (!elements.remove(element)) {
			// It's in a category
			for (DesignerCategory category : categories.values()) {
				if (category.getProperties().remove(element)) break;
			}
		}
	}
	
	public void startCategory(DesignerCategory category) {
		currentCategory = category;
	}
	public void endCategory() {
		currentCategory = null;
	}
	
	public void add(DesignerCategory category) {
		elements.add(category);
		categories.put(category.getName(), category);
	}
	
	public void add(DesignerProperty property) {
		if (currentCategory != null) {
			currentCategory.getProperties().add(property);
		}
		else {
			elements.add(property);
		}
		
		properties.put(property.proxyID, property);
	}
	
	public void replaceProperty(DesignerProperty existing, DesignerProperty newProperty) {
		properties.put(existing.proxyID, newProperty);
		
		for (DesignerCategory category : categories.values()) {
			int indexOf = category.getProperties().indexOf(existing);
			if (indexOf != -1) {
				category.getProperties().set(indexOf, newProperty);
				return;
			}
		}
		
		int indexOf = elements.indexOf(existing);
		if (indexOf != -1) {
			elements.set(indexOf, newProperty);
			return;
		}
	}

	private void removeEmptyCategories() {
		for (DesignerCategory category : categories.values()) {
			if (category.getProperties().isEmpty()) {
				elements.remove(category);
			}
		}
	}
	
	public PropertyPane generateUI(SpuiEditor editor, SpuiElement element) {
		PropertyPane parentPane = new PropertyPane();
		generateUI(editor, parentPane, element);
		return parentPane;
	}
	
	public void generateUI(SpuiEditor editor, PropertyPane parentPane, SpuiElement element) {
		removeEmptyCategories();
		
		for (DesignerElement e : elements) {
			e.generateUI(editor, parentPane, element);
		}
	}
}
