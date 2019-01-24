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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.file.spui.SpuiElement;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.inspector.PropertyPane;

public class DesignerCategory extends DesignerElement {
	/** The keyword used in the XML to describe a property. */
	public static final String KEYWORD = "Category";
	
	/** A list of all the properties contained inside this category, in declaration order. */
	private final List<DesignerProperty> properties = new ArrayList<DesignerProperty>();
	
	/** For parsing, the current property that is being processed. */
	private DesignerProperty currentProperty;
	
	public DesignerCategory(DesignerClass parentClass) {
		super(parentClass);
	}

	@Override public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (currentProperty != null) {
			currentProperty.startElement(uri, localName, qName, attributes);
		}
		else if (qName.equalsIgnoreCase(DesignerProperty.KEYWORD)) {
			currentProperty = new DesignerProperty(parentClass);
			currentProperty.startElement(uri, localName, qName, attributes);
			properties.add(currentProperty);
		}
		else if (qName.equalsIgnoreCase(DesignerCategory.KEYWORD)) {
			name = attributes.getValue("name");
		}
	}
	
	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase(DesignerProperty.KEYWORD)) {
			currentProperty = null;
		}
		else if (currentProperty != null) {
			currentProperty.endElement(uri, localName, qName);
		}
	}
	
	public List<DesignerProperty> getProperties() {
		return properties;
	}
	
	@Override public void generateUI(DesignerUIBuilder builder) {
		DesignerCategory category = builder.getCategory(name);
		if (category == null) {
			category = new DesignerCategory(parentClass);
			category.name = name;
			builder.add(category);
		}
		
		builder.startCategory(category);
		
		for (DesignerProperty property : properties) {
			property.generateUI(builder);
		}
		
		builder.endCategory();
	}

	@Override
	public void generateUI(SpuiEditor editor, PropertyPane parentPane, SpuiElement element) {
		PropertyPane pane = new PropertyPane();
		for (DesignerProperty property : properties) {
			property.generateUI(editor, pane, element);
		}
		parentPane.add(PropertyPane.createTitled(name, pane));
	}
}
