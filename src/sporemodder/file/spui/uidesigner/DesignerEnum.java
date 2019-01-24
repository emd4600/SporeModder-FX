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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DesignerEnum implements DesignerNode {
	
	/** The keyword used in the XML to describe an enumeration. */
	public static final String KEYWORD = "Enum";
	
	private String name;
	private final Map<Integer, String> intValues = new HashMap<Integer, String>();
	private final Map<String, Integer> stringValues = new HashMap<String, Integer>();
	private final List<String> values = new ArrayList<String>();	

	@Override public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase(KEYWORD)) {
			name = attributes.getValue("name");
		}
		else if (qName.equalsIgnoreCase("EnumVal")) {
			String name = attributes.getValue("name");
			int index = Integer.decode(attributes.getValue("value"));
			
			intValues.put(index, name);
			stringValues.put(name, index);
			values.add(name);
		}
	}
	
	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		
	}

	/**
	 * Returns the name that was used to declare this enumeration.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the name assigned to the given integer value.
	 * @param value
	 * @return
	 */
	public String get(int value) {
		return intValues.get(value);
	}
	
	/**
	 * Returns the integer value assigned to the given name.
	 * @param value
	 * @return
	 */
	public int get(String name) {
		return stringValues.get(name);
	}
	
	/** 
	 * Returns a list of the name of all the possible values in this enumeration, in declaration order.
	 * @return
	 */
	public List<String> getStringValues() {
		return values;
	}
}
