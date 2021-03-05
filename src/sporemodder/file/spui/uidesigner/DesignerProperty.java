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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import sporemodder.HashManager;
import sporemodder.UIManager;
import sporemodder.file.LocalizedText;
import sporemodder.file.spui.InspectableObject;
import sporemodder.file.spui.RLEHitMask;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.SpuiPropertyType;
import sporemodder.file.spui.SpuiWriter;
import sporemodder.file.spui.StyleSheet;
import sporemodder.file.spui.StyleSheetInstance;
import sporemodder.file.spui.components.AtlasImage;
import sporemodder.file.spui.components.Borders;
import sporemodder.file.spui.components.DirectImage;
import sporemodder.file.spui.components.IDrawable;
import sporemodder.file.spui.components.ISporeImage;
import sporemodder.util.Vector2;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.editors.spui.SpuiObjectCreatedAction;
import sporemodder.view.editors.spui.SpuiPropertyAction;
import sporemodder.view.editors.spui.SpuiStyleChooser;
import sporemodder.view.editors.spui.SpuiUndoableAction;
import sporemodder.view.inspector.InspectorBoolean;
import sporemodder.view.inspector.InspectorColorPicker;
import sporemodder.view.inspector.InspectorFloatSpinner;
import sporemodder.view.inspector.InspectorIntSpinner;
import sporemodder.view.inspector.InspectorLocalizedText;
import sporemodder.view.inspector.InspectorReferenceLink;
import sporemodder.view.inspector.InspectorString;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.InspectorValueList;
import sporemodder.view.inspector.InspectorVector2;
import sporemodder.view.inspector.PropertyPane;

public class DesignerProperty extends DesignerElement {
	
	/** The keyword used in the XML to describe a property. */
	public static final String KEYWORD = "Property";
	
	/** The java field that corresponds to this property, might be null. */
	Field javaField;
	
	/** The name used to define this property. */
	String name;
	SpuiPropertyType type;
	/** The descriptions that is shown on tooltips. */
	String description;
	/** The ID used to identify this property in the SPUI file. */
	int proxyID = -1;
	/** The default value of the property when its value is not explicitly defined. */
	String defaultValue;

	/** For individual flag properties, the bit mask used to set/unset their values. */
	int mask;
	/** For individual flag properties, the real flag property that they modify. */
	String flagset;
	String format;
	/** Whether this property is deprecated and, therefore, shouldn't be used. */
	boolean isDeprecated;
	/** Whether this property is browsable. */
	boolean isBrowsable = true;
	/** Whether the viewer should be repainted when this property value changes. */
	boolean mustRepaint;
	/** Whether the hierarchy tree should be updated when this property value changes. */
	boolean mustUpdateTree;
	
	/** Used in Spinners, how much is incremented/decremented with the buttons. */
	float stepSize = 1.0f;
	
	/** For integer properties that accept an enumeration as a value, the possible values of the enumeration. */
	DesignerEnum valuesEnum;
	/** For array properties: a map that assigns a name to every array index. */ 
	Map<Integer, String> arrayIndices = new HashMap<Integer, String>();
	
	/** The current UI component (might be a Node or something else) that is displaying this property. */
	//InspectorValue<?>[] inspectorComponents;
	final List<InspectorValue<?>> inspectorComponents = new ArrayList<>();
	
	InspectorValueList<Object> inspectorListComponent;
	
	/** For properties that can change very quickly (like spinners), the last time (in ms) that the property value changed. */
	private long lastModifyTime;
	/** For properties that can change very quickly (like spinners), the last edit action generated by this property. */
	private SpuiUndoableAction lastEditAction;
	
	public DesignerProperty(DesignerClass parentClass) {
		super(parentClass);
	}
	
	public DesignerProperty(DesignerClass parentClass, int proxyID, String name, String type) {
		super(parentClass);
		this.proxyID = proxyID;
		this.name = name;
		this.type = SpuiPropertyType.parse(parentClass.getDesigner(), type);
		findJavaField(null);
		if (javaField == null) {
			findJavaField(null, SpuiElement.class);
		}
	}
	
	public List<InspectorValue<?>> getInspectorComponents() {
		return inspectorComponents;
	}
	
	public InspectorValueList<Object> getInspectorListComponent() {
		return inspectorListComponent;
	}
	
	@Override
	public String toString() {
		return "DesignerProperty [name=" + name + ", type=" + type + ", description=" + description + ", proxyID="
				+ proxyID + "]";
	}
	
	@Override public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase(KEYWORD)) {
			name = attributes.getValue("name");
			
			String proxyStr = attributes.getValue("proxy");
			
			if (proxyStr != null) {
				if (proxyStr.startsWith("utfwin:")) {
					proxyStr = proxyStr.substring("utfwin:".length());
				}
				
				try {
					proxyID = HashManager.get().int32(proxyStr);
				} catch (NumberFormatException e) {
					proxyID = HashManager.get().fnvHash(proxyStr);
				}
			}
			
			String typeStr = attributes.getValue("type");
			if (proxyID != -1 || typeStr.equals("flag")) {
				type = SpuiPropertyType.parse(parentClass.getDesigner(), typeStr);
			}
			description = attributes.getValue("description");
			
			format = attributes.getValue("format");
			mask = HashManager.get().int32(attributes.getValue("mask"));
			flagset = attributes.getValue("flagset");
			
			String deprecatedStr = attributes.getValue("deprecated");
			if (deprecatedStr != null) {
				isDeprecated = Boolean.parseBoolean(deprecatedStr);
			}
			deprecatedStr = attributes.getValue("browsable");
			if (deprecatedStr != null) {
				isBrowsable = Boolean.parseBoolean(deprecatedStr);
			}
			deprecatedStr = attributes.getValue("repaint");
			if (deprecatedStr != null) {
				mustRepaint = Boolean.parseBoolean(deprecatedStr);
			}
			deprecatedStr = attributes.getValue("updateTree");
			if (deprecatedStr != null) {
				mustUpdateTree = Boolean.parseBoolean(deprecatedStr);
			}
			
			String enumStr = attributes.getValue("enum");
			if (enumStr != null) {
				valuesEnum = parentClass.getEnum(enumStr);
			}
			
			String stepStr = attributes.getValue("stepsize");
			if (stepStr != null) {
				stepSize = Float.parseFloat(stepStr);
			}
			
			defaultValue = attributes.getValue("default");
			
			
			findJavaField(attributes.getValue("fieldname"));
			if (proxyID != -1 && parentClass.getJavaClass() != null && parentClass.getJavaClass() != SpuiElement.class) {
				findJavaField(attributes.getValue("fieldname"));
				
				// Some subclasses use different names for the same proxyID, and therefore same field
				if (javaField == null) {
					DesignerProperty p = parentClass.getProperty(proxyID);
					if (p != null) javaField  = p.javaField;
				}
			}
		}
		else if (qName.equalsIgnoreCase("Index")) {
			if (arrayIndices == null) {
				arrayIndices = new HashMap<Integer, String>();
			}
			
			arrayIndices.put(Integer.parseInt(attributes.getValue("value")), attributes.getValue("name"));
		}
	}
	
	private void findJavaField(String fieldName, Class<?> clazz) {
		try {
			if (fieldName == null) fieldName = getFieldName(name);
			javaField = clazz.getDeclaredField(fieldName);
			javaField.setAccessible(true);
		} 
		catch (NoSuchFieldException | SecurityException e) {
		}
	}
	
	private void findJavaField(String fieldName) {
		findJavaField(fieldName, parentClass.getJavaClass());
	}
	
	private static String getFieldName(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(name.charAt(0)));
		for (int i = 1; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (!Character.isWhitespace(c)) sb.append(c);
		}
		return sb.toString();
	}
	
	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	public int getProxyID() {
		return proxyID;
	}

	public void read(SporeUserInterface spui, StreamReader stream, SpuiElement element) throws IOException {
		int typeCode = stream.readLEShort();
		int count = stream.readLEUShort();
		
		if (typeCode != type.getType()) {
			throw new IOException("Error on property " + HashManager.get().hexToString(proxyID) + ": expected " + type.toString() + " but got type code " + typeCode + '.');
		}
		if (type.isArray()) {
			if (type.getArrayCount() != 0 && type.getArrayCount() != count) {
				throw new IOException("Error on property " + HashManager.get().hexToString(proxyID) + ": expected " + type.getArrayCount() + " values but got " + count + '.');
			}
		}/* else if (count != 1) {
			throw new IOException("Error on property " + HashManager.get().hexToString(proxyID) + ": expected 1 value but got " + count + '.');
		}*/
		
		if (typeCode == SpuiPropertyType.TYPE_STRUCT) {
			readStruct(spui, stream, element, count);
		} 
		else {
			Object object = SpuiPropertyType.read(stream, typeCode, count);
			// Some types need to be converted
			object = type.processValue(spui, element, object, format);
			
			if (javaField == null) {
				element.setProperty(proxyID, object);
			} else {
				try {
					type.set(javaField, element, object);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void write(SpuiWriter writer, StreamWriter stream, SpuiElement element) throws IOException {
		Object data = getValue(element);
		int count = isArray() ? type.getArrayCount() : 1;
		
		data = type.processValueForWriting(writer, element, data, format);
		
		if (isList()) {
			count = Array.getLength(data);
		}
		
		stream.writeLEShort(type.getType());
		stream.writeLEUShort(count);
		
		if (type.getType() == SpuiPropertyType.TYPE_STRUCT) {
			stream.writeLEInt(0);  // unknown
			for (int i = 0; i < count; ++i) {
				SpuiElement struct = (SpuiElement) Array.get(data, i);
				struct.getDesignerClass().write(writer, stream, struct);
			}
		} else {
			SpuiPropertyType.write(stream, data, type.getType());
		}
	}
	
	public void addComponents(SpuiWriter writer, SpuiElement element) {
		Object data = getValue(element);
		type.addComponents(writer, element, data);
	}
	
	public void writeStruct(SpuiWriter writer, StreamWriter stream, SpuiElement data) throws IOException {
		stream.writeLEInt(0);  // unknown
		data.getDesignerClass().write(writer, stream, data);
	}
	
	private void readStruct(DesignerClass structureClass, SporeUserInterface spui, StreamReader stream,  SpuiElement structElement) throws IOException {
		int structCount = stream.readLEShort();
		structureClass.read(spui, stream, structElement, structCount);
	}
	
	private void readStruct(SporeUserInterface spui, StreamReader stream, SpuiElement parentElement, int count) throws IOException {
		DesignerClass structureClass = parentClass.getDesigner().getClass(type.getTypeName());
		
		stream.readLEInt();  // unknown
		
		if (type.isArray()) {
			if (isList()) {
				@SuppressWarnings("unchecked")
				List<SpuiElement> list = (List<SpuiElement>) getValue(parentElement);
				if (list == null) {
					list = new ArrayList<SpuiElement>();
					parentElement.setProperty(proxyID, list);
				}
				for (int i = 0; i < count; ++i) {
					SpuiElement structElement = structureClass.createInstance();
					readStruct(structureClass, spui, stream, structElement);
					list.add(structElement);
				}
			} 
			else {
				Object array = getValue(parentElement);
				if (array == null) {
					array = new SpuiElement[type.getArrayCount()];
					parentElement.setProperty(proxyID, array);
				}
				for (int i = 0; i < type.getArrayCount(); ++i) {
					SpuiElement structElement = structureClass.createInstance();
					readStruct(structureClass, spui, stream, structElement);
					Array.set(array, i, structElement);
				}
			}
		}
		else {
			SpuiElement structElement;
			if (javaField != null) {
				// Structures must be declared as final, so they already exist
				try {
					structElement = (SpuiElement) javaField.get(parentElement);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					structElement = new SpuiElement();
				}
			}
			else {
				structElement = structureClass.createInstance();
				parentElement.setProperty(proxyID, structElement);
			}
			readStruct(structureClass, spui, stream, structElement);
		}
	}
	
	private void fillInspectorList(SpuiEditor editor, SpuiElement element, List<Object> values) {
		if (!isList()) return;
		
		int count = values.size();
		
		// So createNode methods can use .set(arrayIndex, ...)
		inspectorComponents.clear();
		for (int i = 0; i < count; ++i) inspectorComponents.add(null);
		
		for (int i = 0; i < count; ++i) {
			createNode(editor, element, i);
		}
	}
	
	@Override public void generateUI(DesignerUIBuilder builder) {
		if (type == null || "flagset".equals(type.getTypeName())) return;
		
		// 3 possibilities: new property, overridden property and overridden property that must be removed
		if (proxyID == -1) {
			// Just add the property, it cannot be overridden
			if (isBrowsable && !isDeprecated) builder.add(this);
		} else {
			DesignerProperty existing = builder.getProperty(proxyID);
			if (existing != null) {
				if (!isBrowsable || isDeprecated) {
					// Remove the existing property
					builder.remove(existing);
				} else {
					builder.replaceProperty(existing, this);
				}
			} 
			else {
				if (isBrowsable && !isDeprecated) builder.add(this);
			}
		}
	}
	
	
	@Override public void generateUI(SpuiEditor editor, PropertyPane parentPane, SpuiElement element) {
		
		inspectorComponents.clear();
		
		if (type.isArray()) {
			if (type.getArrayCount() == 0) {
				@SuppressWarnings("unchecked")
				List<Object> values = (List<Object>)getValue(element);
				
				fillInspectorList(editor, element, values);
				
				inspectorListComponent = new InspectorValueList<Object>(values) {
					@Override protected void fillValuesList(List<InspectorValue<?>> dest, List<Object> backupList) {
						fillInspectorList(editor, element, backupList);
						dest.clear();
						dest.addAll(inspectorComponents);
					}
				};
				inspectorListComponent.addValueListener((obs, oldValue, newValue) -> {
					if (!editor.isEditingViewer()) {
						processUpdate(editor);
						addFastListEditAction(editor, element, oldValue, newValue);
					}
				});
				
				parentPane.add(-1, PropertyPane.createTitled(name, description, inspectorListComponent));
			} 
			else {
				for (int i = 0; i < type.getArrayCount(); ++i) inspectorComponents.add(null);
				
				PropertyPane pane = new PropertyPane();
				for (int i = 0; i < type.getArrayCount(); ++i) {
					createArrayNode(editor, pane, element, i);
				}
				parentPane.add(-1, PropertyPane.createTitled(name, description, pane));
				
			}
		} 
		else {
			inspectorComponents.add(null);
			createNode(editor, parentPane, element, -1);
		}
	}
	
	public boolean isArray() {
		return type.isArray();
	}
	
	public boolean isList() {
		return type.isArray() && type.getArrayCount() == 0;
	}
	
	public Object getValue(SpuiElement element, int arrayIndex) {
		Object value = getValue(element);
		if (isArray()) {
			if (isList()) return ((List<?>) value).get(arrayIndex);
			else return Array.get(value, arrayIndex);
		}
		return value;
	}
	
	public Object getValue(SpuiElement element) {
		if (javaField != null) {
			try {
				return javaField.get(element);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return element.getProperty(proxyID);
		}
	}
	
	public void processUpdate(SpuiEditor editor) {
		if (mustRepaint) {
			editor.repaint();
		}
		if (mustUpdateTree) {
			editor.refreshTree();
		}
	}

	public void setValue(SpuiEditor editor, SpuiElement element, Object value, int arrayIndex) {
		if (isArray()) {
			if (isList()) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) getValue(element);
				list.set(arrayIndex, value);
			} else {
				Object existingValue = getValue(element);
				Array.set(existingValue, arrayIndex, value);
			}
		} else if (javaField != null) {
			try {
				type.set(javaField, element, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			element.setProperty(proxyID, value);
		}
		if (editor != null) processUpdate(editor);
	}
	
	public String getActionText(SpuiElement element) {
		return element.getDesignerClass().getName() + ": " + name;
	}
	
	private InspectorIntSpinner createIntSpinner(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorIntSpinner intSpinner = new InspectorIntSpinner();
		if (defaultValue != null) intSpinner.setDefaultValue(HashManager.get().int64(defaultValue));
		if (stepSize != 0) intSpinner.setStep((long) stepSize);
		
		Object value = getValue(element, arrayIndex);
		intSpinner.setValue(value == null ? 0 : ((Number) value).longValue());
		
		switch (type.getType()) {
		case SpuiPropertyType.TYPE_BYTE: intSpinner.setRange(-128, 127); break;
		case SpuiPropertyType.TYPE_UBYTE: intSpinner.setRange(0, 255); break;
		case SpuiPropertyType.TYPE_SHORT: intSpinner.setRange(-32768, 32767); break;
		case SpuiPropertyType.TYPE_USHORT: intSpinner.setRange(0, 65535); break;
		case SpuiPropertyType.TYPE_INT: intSpinner.setRange(-2147483648, 2147483647); break;
		case SpuiPropertyType.TYPE_UINT: intSpinner.setRange(0, 4294967295L); break;
		}
		
		if (!isList()) intSpinner.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, intSpinner);
		
		return intSpinner;
	}
	
	private InspectorFloatSpinner createFloatSpinner(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorFloatSpinner floatSpinner = new InspectorFloatSpinner();
		if (stepSize != 0) floatSpinner.setStep(stepSize);
		// We can't do this in the general method as rectf/pointf use multiple values
		if (defaultValue != null) floatSpinner.setDefaultValue(Double.parseDouble(defaultValue));
		
		Object value = getValue(element, arrayIndex);
		floatSpinner.setValue(value == null ? 0 : ((Number) value).doubleValue());
		
		if (!isList()) floatSpinner.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue.floatValue(), arrayIndex);
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, floatSpinner);
		
		return floatSpinner;
	}
	
	private int getFlagsetValue(SpuiElement element) {
		DesignerProperty flagset = parentClass.getProperty(this.flagset);
		return (int) flagset.getValue(element);
	}
	
	private void setFlagsetValue(SpuiEditor editor, SpuiElement element, int value) {
		DesignerProperty flagset = parentClass.getProperty(this.flagset);
		flagset.setValue(editor, element, value, -1);
	}
	
	private boolean getFlagValue(SpuiElement element) {
		DesignerProperty flagset = parentClass.getProperty(this.flagset);
		int flags = (int) flagset.getValue(element);
		return (flags & mask) != 0;
	}
	
	private CheckBox createFlagCheckBox(SpuiEditor editor, SpuiElement element) {
		InspectorFlag cb = new InspectorFlag();
		cb.setText(name);
		cb.setSelected((boolean) getFlagValue(element)); 
		
		if (description != null) cb.setTooltip(new Tooltip(description));
		
		if (!isList()) cb.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				int flagset = getFlagsetValue(element) & ~mask;
				if (newValue) flagset |= mask;
				setFlagsetValue(editor, element, flagset);
				addEditAction(editor, element, 0, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(0, cb);
		
		return cb;
	}
	
	private ComboBox<String> createEnumComboBox(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		Object value = getValue(element, arrayIndex);
		
		InspectorEnumBox enumBox = new InspectorEnumBox(valuesEnum);
		enumBox.setValue(value == null ? 0 : ((Number)value).intValue());
		if (!isList()) enumBox.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				addEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, enumBox);
		
		return enumBox.getNode();
	}
	
	private Node createVector4Node(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorRectangle node = new InspectorRectangle((SPUIRectangle) getValue(element, arrayIndex));
		node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				processUpdate(editor);
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		if (stepSize != 0) node.setStep(stepSize);
		
		inspectorComponents.set(arrayIndex, node);
		
		return PropertyPane.createTitled(name, description, node.getNode());
	}
	
	private Node createVector2Node(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorVector2 node = new InspectorVector2((Vector2) getValue(element, arrayIndex));
		if (!isList()) node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				processUpdate(editor);
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		if (stepSize != 0) node.setStep(stepSize);
		
		inspectorComponents.set(arrayIndex, node);
		
		return PropertyPane.createTitled(name, description, node.getNode());
	}
	
	private Node createDimensionsNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorDimensions node = new InspectorDimensions((int[]) getValue(element, arrayIndex));
		if (!isList()) node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				processUpdate(editor);
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		if (stepSize != 0) node.setStep((long) stepSize);
		
		inspectorComponents.set(arrayIndex, node);
		
		return PropertyPane.createTitled(name, description, node.getNode());
	}
	
	private void linkNewItemAction(SpuiEditor editor, SpuiElement element, InspectorReferenceLink link, int arrayIndex, DesignerClass clazz) {
		if (!editor.isEditingViewer()) {
			SpuiElement instance = clazz.createInstance();
			clazz.fillDefaults(editor, instance);
			
			Object oldValue = link.getValue();
			link.setValue(instance);
			link.triggerListeners(oldValue);
			
			if (!isList()) {
				setValue(editor, element, instance, arrayIndex);
				addCreatedObjectAction(editor, element, oldValue, instance, arrayIndex);
			}
			
			editor.addElement(instance);
			editor.selectInspectable(instance);
			
			processUpdate(editor);
		}
	}
	
	private void addCreatedObjectAction(SpuiEditor editor, SpuiElement element, Object oldValue, InspectableObject newValue, int arrayIndex) {
		editor.addEditAction(new SpuiObjectCreatedAction(editor, newValue, getActionText(element)) {
			@Override public void undo() {
				super.undo();
				setValue(editor, element, oldValue, arrayIndex);
				((InspectorReferenceLink)inspectorComponents.get(arrayIndex)).setValue(oldValue);
			}
			
			@Override public void redo() {
				super.redo();
				setValue(editor, element, newValue, arrayIndex);
				((InspectorReferenceLink)inspectorComponents.get(arrayIndex)).setValue(newValue);
			}
		});
	}
	
	private Node createReferenceLink(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		
		boolean isImage = type.getTypeName().equals("image");
		boolean isHitMask = type.getTypeName().equals("hitmask");
		
		InspectorReferenceLink link = new InspectorReferenceLink();
		link.setOnAction(event ->  {
			if (link.getValue() != null) {
				editor.selectInspectable((InspectableObject) link.getValue());
			}
		});
		
		link.setValue(getValue(element, arrayIndex));
		
		final ContextMenu menu = new ContextMenu();

		if (isImage) {
			MenuItem miNewImage = new MenuItem("New Image");
			miNewImage.setOnAction(event -> {
				DirectImage newImage = editor.showImageFileChooser(null);
				if (newImage != null) {
					editor.addElement(newImage);
					Object oldValue = link.getValue();
					link.setValue(newImage);
					link.triggerListeners(oldValue);
					
					if (!isList()) {
						setValue(editor, element, newImage, arrayIndex);
						addCreatedObjectAction(editor, element, oldValue, newImage, arrayIndex);
						// No need to select the image, there's nothing of interest for the user
					}
				}
			});
			
			MenuItem miNewAtlas = new MenuItem("New AtlasImage");
			miNewAtlas.setOnAction(event -> {
				DirectImage image = editor.showImageFileChooser(null);
				if (image != null) {
					AtlasImage newImage = new AtlasImage();
					newImage.setAtlas(image);
					editor.addElement(newImage);
					Object oldValue = link.getValue();
					link.setValue(newImage);
					link.triggerListeners(oldValue);
					
					if (!isList()) {
						setValue(editor, element, newImage, arrayIndex);
						addCreatedObjectAction(editor, element, oldValue, newImage, arrayIndex);
						editor.selectInspectable(newImage);
					}
				}
			});
			
			menu.getItems().addAll(miNewImage, miNewAtlas);
		} 
		else if (isHitMask) {
			MenuItem miNew = new MenuItem("New HitMask");
			miNew.setOnAction(event -> {
				RLEHitMask hitMask = new RLEHitMask();
				Object oldValue = link.getValue();
				link.setValue(hitMask);
				link.triggerListeners(oldValue);
				
				if (!isList()) {
					setValue(editor, element, hitMask, arrayIndex);
					addCreatedObjectAction(editor, element, oldValue, hitMask, arrayIndex);
					editor.selectInspectable(hitMask);
				}
			});
			menu.getItems().add(miNew);
		}
		else {
			for (DesignerClass clazz : parentClass.getDesigner().getImplementingClasses(type.getTypeName())) {
				MenuItem item = new MenuItem("New " + clazz.getName());
				item.setOnAction(event -> linkNewItemAction(editor, element, link, arrayIndex, clazz));
				menu.getItems().add(item);
			}
		}
		
		if (isImage || type.getTypeName().contains("Drawable")) {
			if (!menu.getItems().isEmpty()) menu.getItems().add(new SeparatorMenuItem());
			
			MenuItem miChange = new MenuItem("Change");
			miChange.setOnAction(event -> {
				if (!editor.isEditingViewer()) {
					Object oldValue = link.getValue();
					Object newValue;
					
					if (type.getTypeName().equals("image")) {
						newValue = editor.showImageChooser((ISporeImage) oldValue);
					} else {
						newValue = editor.showDrawableChooser((IDrawable) oldValue, type.getTypeName());
					}
					
					if (newValue != null) {
						link.setValue(newValue);
						link.triggerListeners(oldValue);
						
						if (oldValue != newValue && !isList()) {
							setValue(editor, element, newValue, arrayIndex);
							addEditAction(editor, element, arrayIndex, oldValue, newValue);
						}
					}
				}
			});
			menu.getItems().add(miChange);
		}
		if (!isImage) {

			MenuItem miNone = new MenuItem("Set to none");
			miNone.setOnAction(event -> {
				if (!editor.isEditingViewer()) {
					Object oldValue = link.getValue();
					link.setValue(null);
					link.triggerListeners(oldValue);
					
					if (oldValue != null && !isList()) {
						setValue(editor, element, null, arrayIndex);
						addEditAction(editor, element, arrayIndex, oldValue, null);
					}
					
					processUpdate(editor);
				}
			});
			
			menu.getItems().add(miNone);
		}
		
		link.setOnButtonAction(event -> {
			if (menu.isShowing()) menu.hide();
			else menu.show(link.getMenuButton(), Side.BOTTOM, 0, 0);
		});
		
		inspectorComponents.set(arrayIndex, link);
		
		return link.getNode();
	}
	
	private ColorPicker createColorPicker(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorColorPicker picker = new InspectorColorPicker();
		picker.setValue((Color) getValue(element, arrayIndex));
		
		if (!isList()) picker.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				addEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, picker);
		
		return picker;
	}
	
	private TextField createIDField(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorString tf = new InspectorString();
		tf.setValue((String) getValue(element, arrayIndex));
		
		if (!isList()) tf.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, tf);
		
		return tf;
	}
	
	@SuppressWarnings("unchecked")
	private <T> void addEditAction(SpuiEditor editor, SpuiElement element, int arrayIndex, T oldValue, T newValue) {
		editor.addEditAction(new SpuiPropertyAction<T>(oldValue, newValue, 
					v -> {
						setValue(editor, element, v, arrayIndex);
						((InspectorValue<T>)inspectorComponents.get(arrayIndex)).setValue(v);
					},
					getActionText(element)));
	}
	
	@SuppressWarnings("unchecked")
	private <T> void addFastEditAction(SpuiEditor editor, SpuiElement element, int arrayIndex, T oldValue, T newValue) {
		addFastEditAction(editor, element, arrayIndex, oldValue, newValue, v -> {
			setValue(editor, element, v, arrayIndex);
			((InspectorValue<T>)inspectorComponents.get(arrayIndex)).setValue(v);
		});
	}
	
	@SuppressWarnings("unchecked")
	private <T> void addFastEditAction(SpuiEditor editor, SpuiElement element, int arrayIndex, T oldValue, T newValue, Consumer<T> consumer) {
		long time = System.currentTimeMillis();
		if (time - lastModifyTime > SpuiEditor.MINIMUM_ACTION_TIME) {
			// Enough time has passed, register a new undo action
			lastModifyTime = time;
			lastEditAction = new SpuiPropertyAction<T>(oldValue, newValue, 
					consumer,
					getActionText(element));
			editor.addEditAction(lastEditAction);
		}
		else {
			// The change was too quick, so we shouldn't register a new action;
			// instead, modify the last one we added
			lastModifyTime = time;
			((SpuiPropertyAction<T>)lastEditAction).setNewValue(newValue);
		}
	}
	
	private class SpuiListAction extends SpuiUndoableAction {
		
		private final SpuiElement element;
		private final List<Object> oldValue;
		private List<Object> newValue;

		public SpuiListAction(SpuiElement element, List<Object> oldValue, List<Object> newValue) {
			super();
			this.element = element;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override public void undo() {
			inspectorListComponent.setValue(oldValue);
		}

		@Override public void redo() {
			inspectorListComponent.setValue(newValue);
		}

		@Override public String getText() {
			return getActionText(element);
		}
	}
	
	private void addFastListEditAction(SpuiEditor editor, SpuiElement element, List<Object> oldValue, List<Object> newValue) {
		long time = System.currentTimeMillis();
		if (time - lastModifyTime > SpuiEditor.MINIMUM_ACTION_TIME) {
			// Enough time has passed, register a new undo action
			lastModifyTime = time;
			lastEditAction = new SpuiListAction(element, oldValue, newValue);
			editor.addEditAction(lastEditAction);
		}
		else {
			// The change was too quick, so we shouldn't register a new action;
			// instead, modify the last one we added
			lastModifyTime = time;
			((SpuiListAction)lastEditAction).newValue = newValue;
		}
	}
	
	private Node createStyleNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		StyleSheetInstance instance = (StyleSheetInstance) getValue(element, arrayIndex);
		
		InspectorReferenceLink hyperlink = new InspectorReferenceLink();
		hyperlink.setOnAction(event -> {
			StyleSheetInstance oldValue = (StyleSheetInstance) getValue(element, arrayIndex);
			
			SpuiStyleChooser chooser = new SpuiStyleChooser();
			chooser.setSelectedStyle(oldValue);
			
			if (UIManager.get().showDialog(chooser).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
				StyleSheetInstance newValue = chooser.getSelectedStyle();
				hyperlink.setValue(newValue);
				hyperlink.triggerListeners(oldValue);
			}
		});
		hyperlink.setValue(instance);
		
		hyperlink.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				addEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, hyperlink);
		
		return hyperlink.getNode();
	}
	
	private Node createUIntNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		if (type.getTypeName().equals("color")) {
			return createColorPicker(editor, element, arrayIndex);
		} 
		else if (type.getTypeName().equals("textstyle")) {
			return createStyleNode(editor, element, arrayIndex);
		} 
		else if (format != null && format.equals("id")) {
			return createIDField(editor, element, arrayIndex);
		}
		else {
			return createIntSpinner(editor, element, arrayIndex);
		}
	}
	
	private Node createStructNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		DesignerClass struct = getParentClass().getDesigner().getClass(type.getTypeName());
		
		PropertyPane pane = new PropertyPane();
		struct.generateUI(editor, pane, (SpuiElement) getValue(element, arrayIndex));
		
		return PropertyPane.createTitled(name, description, pane);
	}
	
	private Node createTextNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorLocalizedText node = new InspectorLocalizedText((LocalizedText) getValue(element, arrayIndex));
		
		if (!isList()) node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				processUpdate(editor);
				addFastEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, node);
		
		return PropertyPane.createTitled(name, description, node.getNode());
	}
	
	@SuppressWarnings("unchecked")
	private Node createOnlyTextNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		InspectorString node = new InspectorString();
		LocalizedText text = (LocalizedText) getValue(element, arrayIndex);
		node.setValue(text.getText());
		
		if (!isList()) node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				text.setText(newValue);
				processUpdate(editor);
				addFastEditAction(editor, element, arrayIndex, new LocalizedText(oldValue), new LocalizedText(newValue), v -> {
					setValue(editor, element, v, arrayIndex);
					((InspectorValue<String>)inspectorComponents.get(arrayIndex)).setValue(v.getText());
				});
			}
		});
		
		inspectorComponents.set(arrayIndex, node);
		
		return node.getNode();
	}
	
	private Node createAnchorNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		Object value = getValue(element, arrayIndex);
		InspectorAnchor node = new InspectorAnchor(value == null ? 0 : ((Number)value).intValue());
		
		if (!isList()) node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				addEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, node);
		
		TitledPane pane = PropertyPane.createTitled(name, description, node.getNode());
		pane.setPrefWidth(Double.MAX_VALUE);
		return pane;
	}
	
	private Node createBoolNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		Object value = getValue(element, arrayIndex);
		InspectorBoolean node = new InspectorBoolean();
		node.setText(name);
		if (description != null) node.setTooltip(new Tooltip(description));
		node.setValue(value == null ? false : ((Boolean)value).booleanValue());
		
		if (!isList()) node.addValueListener((obs, oldValue, newValue) -> {
			if (!editor.isEditingViewer()) {
				setValue(editor, element, newValue, arrayIndex);
				addEditAction(editor, element, arrayIndex, oldValue, newValue);
			}
		});
		
		inspectorComponents.set(arrayIndex, node);
		
		return node;
	}
	
	private void createNode(SpuiEditor editor, PropertyPane parentPane, SpuiElement element, int index) {
		if (type.getTypeName().equals("flag")) {
			parentPane.add(index, createFlagCheckBox(editor, element));
		}
		else if (valuesEnum != null) {
			parentPane.add(index, name, description, createEnumComboBox(editor, element, 0));
		} 
		else if (type.getTypeName().equals("anchor")) {
			parentPane.add(index, createAnchorNode(editor, element, 0));
		}
		else {
			switch (type.getType()) {
			case SpuiPropertyType.TYPE_BOOLEAN:
				parentPane.add(index, createBoolNode(editor, element, 0));
				break;
			case SpuiPropertyType.TYPE_BYTE:
			case SpuiPropertyType.TYPE_UBYTE:
			case SpuiPropertyType.TYPE_SHORT:
			case SpuiPropertyType.TYPE_USHORT:
			case SpuiPropertyType.TYPE_INT:
			case SpuiPropertyType.TYPE_LONG:
			case SpuiPropertyType.TYPE_ULONG:
				parentPane.add(index, name, description, createIntSpinner(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_UINT:
				parentPane.add(index, name, description, createUIntNode(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_FLOAT:
			case SpuiPropertyType.TYPE_DOUBLE:
				parentPane.add(index, name, description, createFloatSpinner(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_VECTOR4:
				parentPane.add(index, createVector4Node(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_VECTOR2:
				parentPane.add(index, createVector2Node(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_DIMENSION:
				parentPane.add(index, createDimensionsNode(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_REFERENCE:
				parentPane.add(index, name, description, createReferenceLink(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_STRUCT:
				parentPane.add(index, createStructNode(editor, element, 0));
				break;
				
			case SpuiPropertyType.TYPE_TEXT:
				if (format != null && format.equals("textonly")) {
					parentPane.add(index, name, description, createOnlyTextNode(editor, element, 0));
				} else {
					parentPane.add(index, createTextNode(editor, element, 0));
				}
				break;
			}
		}
	}

	private Node createNode(SpuiEditor editor, SpuiElement element, int arrayIndex) {
		// Flags not supported in arrays
		if (valuesEnum != null) return createEnumComboBox(editor, element, arrayIndex);
		else if (type.getTypeName().equals("anchor")) return createAnchorNode(editor, element, arrayIndex);
		else {
			switch (type.getType()) {
			case SpuiPropertyType.TYPE_BOOLEAN: return createBoolNode(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_BYTE:
			case SpuiPropertyType.TYPE_UBYTE:
			case SpuiPropertyType.TYPE_SHORT:
			case SpuiPropertyType.TYPE_USHORT:
			case SpuiPropertyType.TYPE_INT:
			case SpuiPropertyType.TYPE_LONG:
			case SpuiPropertyType.TYPE_ULONG: return createIntSpinner(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_UINT: return createUIntNode(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_FLOAT:
			case SpuiPropertyType.TYPE_DOUBLE: return createFloatSpinner(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_VECTOR4: return createVector4Node(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_VECTOR2: return createVector2Node(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_DIMENSION: return createDimensionsNode(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_REFERENCE: return createReferenceLink(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_STRUCT: return createStructNode(editor, element, arrayIndex);
			case SpuiPropertyType.TYPE_TEXT: 
				if (format != null && format.equals("textonly")) {
					return createOnlyTextNode(editor, element, arrayIndex);
				} else {
					return createTextNode(editor, element, arrayIndex);
				}
			default: return null;
			}
		}
	}
	
	private void createArrayNode(SpuiEditor editor, PropertyPane pane, SpuiElement element, int arrayIndex) {
		String name = arrayIndices.get(arrayIndex);
		if (name == null) name = Integer.toString(arrayIndex);
		
		pane.add(name, null, createNode(editor, element, arrayIndex));
	}
	
	public String getName() {
		return name;
	}
	
	private static Color parseColor(String color) {
		// Color.web uses RGBA, but Spore designer uses ARGB
		if (color.length() == 9) {
			double opacity = Integer.parseInt(color.substring(1, 3), 16) / 255.0;
			return Color.web('#' + color.substring(3), opacity);
		} else {
			return Color.web(color);
		}
	}
	
	private static int[] parseDimension(String text) {
		String[] splits = text.split(",");
		return new int[] {Integer.parseInt(splits[0]), Integer.parseInt(splits[1])};
	}
	
	private static Vector2 parseVector2(String text) {
		String[] splits = text.split(",");
		return new Vector2(Float.parseFloat(splits[0]), Float.parseFloat(splits[1]));
	}
	
	private static SPUIRectangle parseRectangle(String text) {
		String[] splits = text.split(",");
		return new SPUIRectangle(Float.parseFloat(splits[0]), Float.parseFloat(splits[1]),
				Float.parseFloat(splits[2]), Float.parseFloat(splits[3]));
	}
	
	private Object getDefaultNoArray(SpuiEditor editor, SpuiElement element) {
		if (type.getTypeName().equals("color")) {
			if (defaultValue != null) return parseColor(defaultValue);
			else return Color.WHITE;
		}
		else if (type.getTypeName().equals("textstyle")) {
			if (defaultValue != null) return StyleSheet.getActiveStyleSheet().getInstance(defaultValue);
			else return null;
		}
		else if (format != null && format.equals("id")) {
			return defaultValue;
		}
		else {
			switch (type.getType()) {
			case SpuiPropertyType.TYPE_BOOLEAN: return false;
			case SpuiPropertyType.TYPE_BYTE:
			case SpuiPropertyType.TYPE_UBYTE:
			case SpuiPropertyType.TYPE_SHORT:
			case SpuiPropertyType.TYPE_USHORT:
			case SpuiPropertyType.TYPE_INT:
			case SpuiPropertyType.TYPE_UINT:
				if (defaultValue != null) {
					if (valuesEnum != null && !Character.isDigit(defaultValue.charAt(0))) {
						return valuesEnum.get(defaultValue);
					} else {
						return HashManager.get().int32(defaultValue);
					}
				}
				else return 0;
			case SpuiPropertyType.TYPE_LONG: 
			case SpuiPropertyType.TYPE_ULONG: 
				if (defaultValue != null) return HashManager.get().int64(defaultValue);
				else return 0;
			case SpuiPropertyType.TYPE_FLOAT:
				if (defaultValue != null) return Float.parseFloat(defaultValue);
				else return 0.0f;
				
			case SpuiPropertyType.TYPE_DOUBLE: 
				if (defaultValue != null) return Double.parseDouble(defaultValue);
				else return 0.0;
			
			case SpuiPropertyType.TYPE_DIMENSION: 
				if (defaultValue != null) return parseDimension(defaultValue);
				return new int[2];
				
			case SpuiPropertyType.TYPE_VECTOR4: 
				if (defaultValue != null) return parseRectangle(defaultValue);
				return new SPUIRectangle();
				
			case SpuiPropertyType.TYPE_VECTOR2: 
				if (defaultValue != null) return parseVector2(defaultValue);
				return new Vector2();
				
			case SpuiPropertyType.TYPE_TEXT: return new LocalizedText();
				
			case SpuiPropertyType.TYPE_STRUCT: 
				if (defaultValue != null && type.getTypeName().equals("Borders")) {
					// special case
					SPUIRectangle rect = parseRectangle(defaultValue);
					Borders borders = new Borders();
					borders.left = rect.x1;
					borders.top = rect.y1;
					borders.right = rect.x2;
					borders.bottom = rect.y2;
					return borders;
				} else {
					DesignerClass structureClass = parentClass.getDesigner().getClass(type.getTypeName());
					SpuiElement struct = structureClass.createInstance();
					structureClass.fillDefaults(editor, struct);
					return struct;
				}
				
			case SpuiPropertyType.TYPE_REFERENCE:
				if (defaultValue != null && !"null".equals(defaultValue)) {
					DesignerClass clazz = parentClass.getDesigner().getClass(defaultValue);
					SpuiElement instance = clazz.createInstance();
					clazz.fillDefaults(editor, instance);
					if (editor != null) editor.addElement(instance);
					return instance;
				} else {
					return null;
				}
				
			default: return null;
			}
		}
	}
	
	private Object creteEmptyArray() {
		int count = type.getArrayCount();
		
		if (type.getTypeName().equals("color")) return new Color[count];
		else if (type.getTypeName().equals("textstyle")) return new StyleSheetInstance[count];
		else {
			switch (type.getType()) {
			case SpuiPropertyType.TYPE_BOOLEAN: return new boolean[count];
			case SpuiPropertyType.TYPE_BYTE: return new byte[count];
			case SpuiPropertyType.TYPE_UBYTE:
			case SpuiPropertyType.TYPE_SHORT:
			case SpuiPropertyType.TYPE_USHORT:
			case SpuiPropertyType.TYPE_INT:
			case SpuiPropertyType.TYPE_UINT: return new int[count];
			case SpuiPropertyType.TYPE_LONG:
			case SpuiPropertyType.TYPE_ULONG: return new long[count];
			case SpuiPropertyType.TYPE_FLOAT: return new float[count];
			case SpuiPropertyType.TYPE_DOUBLE: return new double[count];
			case SpuiPropertyType.TYPE_DIMENSION: return new int[count][2];
			case SpuiPropertyType.TYPE_VECTOR4: return new SPUIRectangle[count];
			case SpuiPropertyType.TYPE_VECTOR2: return new Vector2[count];
			case SpuiPropertyType.TYPE_TEXT: return new LocalizedText[count];
			case SpuiPropertyType.TYPE_STRUCT: return new Object[count];
			case SpuiPropertyType.TYPE_REFERENCE: return new Object[count];
			default: return null;
			}
		}
	}

	public void fillDefault(SpuiEditor editor, SpuiElement element) {
		// Special case: we set the 'flagset' and ignore the individual 'flag's
		if (proxyID == -1 || type == null || type.getTypeName().equals("flag")) return;
		
		Object value = getDefaultNoArray(editor, element);
		
		if (isArray()) {
			// If the Java field exists for an array/list, it's final and therefore already set
			if (javaField == null) {
				element.setProperty(proxyID, creteEmptyArray());
			}
			
			// It's possible to assign a default value to all its elements though
			if (value != null && !isList()) {
				Object array = getValue(element);
				int count = Array.getLength(array);
				for (int i = 0; i < count; ++i) {
					Array.set(array, i, value);
				}
			}
		} else {
			setValue(null, element, value, 0);
		}
	}

}
