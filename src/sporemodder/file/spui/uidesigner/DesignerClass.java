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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Stream.StringEncoding;
import javafx.scene.Node;
import sporemodder.HashManager;
import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.SpuiWriter;
import sporemodder.file.LocalizedText;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.inspector.PropertyPane;
import sporemodder.file.spui.SporeUserInterface;

public class DesignerClass implements DesignerNode {
	
	/** The keyword used in the XML to describe a class. */
	public static final String KEYWORD = "Class";
	/** Another possible keyword used in the XML to describe a class. */
	public static final String KEYWORD_STRUCT = "Struct";
	
	private SpuiDesigner designer;
	
	private Class<? extends SpuiElement> javaClass;

	/** The name that was used to declare this class. */
	private String name;
	private String description;
	/** The ID used to identify this class in the SPUI file. */
	private int proxyID = -1;
	
	/** The designer class that this class extends, if any. */
	private DesignerClass baseClass;
	
	private final List<String> implementedInterfaces = new ArrayList<String>();
	/** The Java name of this class. */
	private String className;
	
	/** Whether this class is saved as a structure (true) or as a class reference (false). */
	private boolean isStruct;
	
	private boolean isAbstract;
	
	/** A list of all elements that will generate an UI to edit them in this class. */
	private final List<DesignerElement> elements = new ArrayList<DesignerElement>();
	
	/** A map of all enums declared inside this class, mapped to their name. */
	private final Map<String, DesignerEnum> enums = new HashMap<String, DesignerEnum>();
	
	/** A map that assigns a property to its proxy ID. */
	private final Map<Integer, DesignerProperty> properties = new LinkedHashMap<>();
	
	
	/** For parsing, the current enumeration that is being processed. */
	private DesignerEnum currentEnum;
	/** For parsing, the current element that is being processed. */
	private DesignerElement currentElement;
	
	public DesignerClass(SpuiDesigner designer, boolean isStruct) {
		this.designer = designer;
		this.isStruct = isStruct;
	}
	
	/**
	 * Returns whether this class is saved as a structure (true) or as a class reference (false).
	 * @return
	 */
	public boolean isStruct() {
		return isStruct;
	}
	
	/**
	 * Returns the name that was used to declare this class.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public void addEnum(DesignerEnum enumeration) {
		enums.put(enumeration.getName(), enumeration);
	}
	
	public void addElement(DesignerElement element) {
		elements.add(element);
	}
	
	/**
	 * Returns the designer enumeration that has the given name, or null if it does not exist.
	 * If it was not declared inside this class scope, it will search them in the designer scope.
	 * @param name
	 * @return
	 */
	public DesignerEnum getEnum(String name) {
		DesignerEnum obj = enums.get(name);
		return obj == null ? designer.getEnum(name) : obj;
	}
	
	public SpuiDesigner getDesigner() {
		return designer;
	}
	
	@Override
	public String toString() {
		return "DesignerClass [name=" + name + ", proxyID=" + HashManager.get().hexToString(proxyID) + "]";
	}

	@Override public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (currentEnum != null) {
			currentEnum.startElement(uri, localName, qName, attributes);
		}
		else if (currentElement != null) {
			currentElement.startElement(uri, localName, qName, attributes);
		}
		else if (qName.equalsIgnoreCase(DesignerEnum.KEYWORD)) {
			currentEnum = new DesignerEnum();
			currentEnum.startElement(uri, localName, qName, attributes);
			enums.put(currentEnum.getName(), currentEnum);
		}
		else if (qName.equalsIgnoreCase(DesignerProperty.KEYWORD)) {
			currentElement = new DesignerProperty(this);
			currentElement.startElement(uri, localName, qName, attributes);
			elements.add(currentElement);
		}
		else if (qName.equalsIgnoreCase(DesignerCategory.KEYWORD)) {
			currentElement = new DesignerCategory(this);
			currentElement.startElement(uri, localName, qName, attributes);
			elements.add(currentElement);
		}
		else if (qName.equalsIgnoreCase(KEYWORD) || qName.equalsIgnoreCase(KEYWORD_STRUCT)) {
			
			name = attributes.getValue("name");
			description = attributes.getValue("description");
			
			if ("FrameStyle".equals(name)) {
				System.out.print("");
			}
			
			String proxyStr = attributes.getValue("proxy");
			
			if (proxyStr != null) {
				if (proxyStr.startsWith("utfwin:")) {
					proxyStr = proxyStr.substring("utfwin:".length());
				}
				
				proxyID = HashManager.get().getFileHash(proxyStr);
			}
			
			String baseStr = attributes.getValue("base");
			if (baseStr != null) {
				baseClass = designer.getClass(baseStr);
			}
			
			baseStr = attributes.getValue("abstract");
			if (baseStr != null) isAbstract = Boolean.parseBoolean(baseStr);
			
			className = attributes.getValue("classname");
			if (className != null) {
				javaClass = getClass(className);
			} else if (name != null) {
				javaClass = getClass(name);
			}
			
			if (javaClass == null) {
				// First try with parent
				if (baseClass != null && baseClass.javaClass != null) {
					javaClass = baseClass.javaClass;
				}
			}
			if (javaClass == null) {
				javaClass = SpuiElement.class;
			}
			
			if (!isStruct) {
				// Add a special property used by the SPUI editor
				DesignerProperty property = new DesignerProperty(this, SpuiElement.EDITOR_TAG_PROXYID, "EditorTag", "string_resource");
				property.mustUpdateTree = true;
				property.format = "textonly";
				properties.put(SpuiElement.EDITOR_TAG_PROXYID, property);
				properties.put(0x1722b221, property);
				elements.add(property);
			}
		}
		else if (qName.equalsIgnoreCase("Implements")) {
			String str = attributes.getValue("name");
			implementedInterfaces.add(str);
			if (javaClass == SpuiElement.class) {
				javaClass = getClass(str);
				if (javaClass == null) javaClass = SpuiElement.class;
			}
		}
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	@SuppressWarnings("unchecked")
	private static Class<? extends SpuiElement> getClass(String className) {
		try {
			return (Class<? extends SpuiElement>) Class.forName("sporemodder.file.spui.components." + className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentElement instanceof DesignerCategory && qName.equalsIgnoreCase(DesignerCategory.KEYWORD)) {
			DesignerCategory category = (DesignerCategory) currentElement;
			for (DesignerProperty property : category.getProperties()) {
				properties.put(property.getProxyID(), property);
			}
			currentElement = null;
		}
		else if (currentElement instanceof DesignerProperty && qName.equalsIgnoreCase(DesignerProperty.KEYWORD)) {
			DesignerProperty property = (DesignerProperty) currentElement;
			properties.put(property.getProxyID(), property);
			currentElement = null;
		}
		else if (qName.equalsIgnoreCase(DesignerEnum.KEYWORD)) {
			currentEnum = null;
		}
		else if (currentEnum != null) {
			currentEnum.endElement(uri, localName, qName);
		}
		else if (currentElement != null) {
			currentElement.endElement(uri, localName, qName);
		}
	}
	
	public boolean implementsInterface(String name) {
		return implementedInterfaces.contains(name);
	}
	
	public boolean implementsInterfaceComplete(String name) {
		boolean result = implementedInterfaces.contains(name);
		if (!result && baseClass != null) return baseClass.implementsInterfaceComplete(name);
		else return result;
	}
	
	public SpuiElement createInstance() {
		if (javaClass == null) {
			throw new UnsupportedOperationException("Java class for " + name + " not found.");
		}
		try {
			SpuiElement element = javaClass.getConstructor().newInstance();
			element.setDesignerClass(this);
			return element;
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public SpuiElement createInstanceWithDefaults() {
		SpuiElement element = createInstance();
		fillDefaults(null, element);
		return element;
	}
	
	/**
	 * Returns the property with the given proxy ID, first looking in this class declaration and, if not
	 * found, looking through all its base classes.
	 * @param proxyID
	 * @return
	 */
	public DesignerProperty getProperty(int proxyID) {
		DesignerProperty property = properties.get(proxyID);
		if (property != null) return property;
		else if (baseClass != null) return baseClass.getProperty(proxyID);
		else return null;
	}
	
	public DesignerProperty getProperty(String name) {
		for (DesignerProperty property : properties.values()) {
			if (name.equals(property.getName())) return property;
		}
		if (baseClass != null) return baseClass.getProperty(name);
		else return null;
	}

	public void read(SporeUserInterface spui, StreamReader stream, SpuiElement element, int propertiesCount) throws IOException {
		for (int i = 0; i < propertiesCount; ++i) {
			int id = stream.readLEInt();
			
			DesignerProperty property = getProperty(id);
			if (property != null) {
				property.read(spui, stream, element);
			} else {
				throw new IOException("Unsupported property " + HashManager.get().hexToString(id) + " for class " + name + '.');
			}
		}
	}
	
	private void writeProperties(SpuiWriter writer, StreamWriter stream, SpuiElement element, Set<Integer> writtenProperties) throws IOException {
		if (baseClass != null) {
			baseClass.writeProperties(writer, stream, element, writtenProperties);
		}
		
		for (DesignerProperty property : properties.values()) {
			if (property.proxyID != -1 && !property.isDeprecated && !writtenProperties.contains(property.proxyID)) {
				boolean proceed = false;
				if (property.type.getType() == sporemodder.file.spui.SpuiPropertyType.TYPE_TEXT) {
					/*LocalizedText[] text = (LocalizedText[])property.getValue(element);
					if (text.length != 0)
						for (LocalizedText a : text) {
							if ((a.getTableID() != 0) || (a.getInstanceID() != 0) || (a.getText() != null)) {
								proceed = true;
								break;
							}
						}*/
					LocalizedText text = (LocalizedText)property.getValue(element);
					proceed = (text.getTableID() != 0) || (text.getInstanceID() != 0) || (text.getText() != null);
				}
				else {
					proceed = true;
				}
				
				if (proceed) {
					stream.writeLEInt(property.proxyID);
					property.write(writer, stream, element);
					
					writtenProperties.add(property.proxyID);
				}
			}
		}
	}
	
	public void write(SpuiWriter writer, StreamWriter stream, SpuiElement element) throws IOException {
		
		long countOffset = stream.getFilePointer();
		stream.writeLEUShort(0);
		
		Set<Integer> writtenProperties = new HashSet<>();
		writeProperties(writer, stream, element, writtenProperties);
		
		long endOffset = stream.getFilePointer();
		
		int propertyCount = writtenProperties.size();
		if (element.isRoot()) propertyCount |= SporeUserInterface.ROOT_FLAG;
		stream.seek(countOffset);
		stream.writeLEUShort(propertyCount);
		
		
		stream.seek(endOffset);
	}
	
	private void addComponents(SpuiWriter writer, SpuiElement element, Set<Integer> skipProperties) {
		if (baseClass != null) {
			baseClass.addComponents(writer, element, skipProperties);
		}
		
		for (DesignerProperty property : properties.values()) {
			if (property.proxyID != -1 && !property.isDeprecated && !skipProperties.contains(property.proxyID)) {
				property.addComponents(writer, element);
				
				skipProperties.add(property.proxyID);
			}
		}
	}
	
	public void addComponents(SpuiWriter writer, SpuiElement element) {
		Set<Integer> skipProperties = new HashSet<>();
		addComponents(writer, element, skipProperties);
		writer.addElement(element);
	}

	public int getProxyID() {
		return proxyID;
	}

	public Class<? extends SpuiElement> getJavaClass() {
		return javaClass;
	}
	
	public void generateUI(SpuiEditor editor, PropertyPane parentPane, SpuiElement element) {
		DesignerUIBuilder builder = new DesignerUIBuilder();
		
		if (baseClass != null) {
			for (DesignerElement e : baseClass.elements) {
				e.generateUI(builder);
			}
		}
		for (DesignerElement e : elements) {
			e.generateUI(builder);
		}
		
		builder.generateUI(editor, parentPane, element);
	}

	public Node generateUI(SpuiEditor editor, SpuiElement element) {
		PropertyPane parentPane = new PropertyPane();
		generateUI(editor, parentPane, element);
		
		return parentPane.getNode();
	}
	
	/**
	 * Sets a value to all the properties recognized by this designer class, assigning them the default value if specified.
	 * If new elements are created and the 'editor' variable is not null, those new elements will be added into the editor hierarchy trees.
	 * @param editor
	 * @param element
	 */
	public void fillDefaults(SpuiEditor editor, SpuiElement element) {
		Set<Integer> skipProperties = new HashSet<>();
		fillDefaults(editor, element, skipProperties);
	}
	
	private void fillDefaults(SpuiEditor editor, SpuiElement element, Set<Integer> skipProperties) {
		for (DesignerProperty property : properties.values()) {
			if (!skipProperties.contains(property.proxyID)) {
				property.fillDefault(editor, element);
				skipProperties.add(property.proxyID);
			}
		}
		if (baseClass != null) {
			baseClass.fillDefaults(editor, element, skipProperties);
		}
	}

	public String getDescription() {
		return description;
	}
}
