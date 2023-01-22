package sporemodder.extras.spuieditor;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import sporemodder.extras.spuieditor.PanelUtils.BooleanValueAction;
import sporemodder.extras.spuieditor.PanelUtils.ColorValueAction;
import sporemodder.extras.spuieditor.PanelUtils.EnumValueAction;
import sporemodder.extras.spuieditor.PanelUtils.FloatValueAction;
import sporemodder.extras.spuieditor.PanelUtils.IntValueAction;
import sporemodder.extras.spuieditor.PanelUtils.PropertyInfo;
import sporemodder.extras.spuieditor.PanelUtils.ShortValueAction;
import sporemodder.extras.spuieditor.PanelUtils.TextFieldValueAction;
import sporemodder.extras.spuieditor.PanelUtils.TextValueAction;
import sporemodder.extras.spuieditor.components.PropertyObject;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.files.formats.LocalizedText;
import sporemodder.utilities.Hasher;

public class PropertiesPanel extends JPanel {

	private int currentRow = 0;
	
	public PropertiesPanel() {
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
	}
	
	public PropertiesPanel(String title) {
		this();
		if (title != null) {
			setBorder(BorderFactory.createTitledBorder(title));
		}
	}
	
	public int getNextRow() {
		return currentRow++;
	}
	
	public PropertyInfo addColorValue(String text, String chooserText, Color value, ColorValueAction action, UndoableEditor editor) {
		return PanelUtils.addColorValue(this, currentRow++, text, chooserText, value, action, editor);
	}
	
	public PropertyInfo addFloatValue(String text, float value, Float min, Float max, Float stepSize, FloatValueAction action, UndoableEditor editor) {
		return PanelUtils.addFloatValue(this, currentRow++, text, value, min, max, stepSize, action, editor);
	}
	
	public PropertyInfo addFloatFieldValue(String text, float value, FloatValueAction action, UndoableEditor editor) {
		return PanelUtils.addFloatFieldValue(this, currentRow++, text, value, action, editor);
	}
	
	public PropertyInfo addIntValue(String text, String textValue, IntValueAction action, UndoableEditor editor) {
		return PanelUtils.addIntValue(this, currentRow++, text, textValue, action, editor);
	}
	
	public PropertyInfo addIntValue(String text, int value, IntValueAction action, UndoableEditor editor) {
		return PanelUtils.addIntValue(this, currentRow++, text, Integer.toString(value), action, editor);
	}
	
	public PropertyInfo addIntSpinnerValue(String text, int value, Integer min, Integer max, Integer stepSize, IntValueAction action, UndoableEditor editor) {
		return PanelUtils.addIntSpinnerValue(this, currentRow++, text, value, min, max, stepSize, action, editor);
	}
	
	public PropertyInfo addTextFieldValue(String text, String value, TextFieldValueAction action, UndoableEditor editor) {
		return PanelUtils.addTextFieldValue(this, currentRow++, text, value, action, editor);
	}
	
	public PropertyInfo addEnumValue(String text, int valueIndex, String[] values, EnumValueAction action, UndoableEditor editor) {
		return PanelUtils.addEnumValue(this, currentRow++, text, valueIndex, values, action, editor);
	}
	
	public PropertyInfo addEnumValue(String text, String selectedValue, String[] values, EnumValueAction action, UndoableEditor editor) {
		return PanelUtils.addEnumValue(this, currentRow++, text, selectedValue, values, action, editor);
	}
	
	public JCheckBox addBooleanValue(String text, boolean value, BooleanValueAction action, UndoableEditor editor) {
		return PanelUtils.addBooleanValue(this, currentRow++, text, value, action, editor);
	}
	
	public PropertyInfo addShortValue(String text, Object value, ShortValueAction action) {
		return PanelUtils.addShortValue(this, currentRow++, text, value, action);
	}
	
	public PropertyInfo addLinkValue(String text, Object value, ShortValueAction action) {
		return PanelUtils.addLinkValue(this, currentRow++, text, value, action);
	}
	
	public void addPanel(JPanel panel) {
		PanelUtils.addPanel(this, panel, currentRow++);
	}
	
	public void addComponent(JComponent component) {
		PanelUtils.addComponent(this, component, currentRow++);
	}
	
	// This only works when removing the last panel!
	public void removePanel(JPanel panel) {
		PanelUtils.removePanel(this, panel);
	}
	
	
	public JPanel addTextValue(String title, final LocalizedText value, final TextValueAction action, UndoableEditor editor) {
		return PanelUtils.addTextValue(this, currentRow++, title, value, action, editor);
	}
	
	
	
	public JCheckBox addBooleanProperty(PropertyObject component, int property, UndoableEditor editor) {
		return PanelUtils.addBooleanProperty(this, currentRow++, component, property, 
				component.getUnassignedProperties().containsKey(property) ? (boolean) component.getUnassignedProperties().get(property) : false, editor);
	}
	
	public PropertyInfo addIntProperty(PropertyObject component, int property, UndoableEditor editor) {
		return PanelUtils.addIntProperty(this, currentRow++, component, property, 
				component.getUnassignedProperties().containsKey(property) ? (int) component.getUnassignedProperties().get(property) : 0, editor);
	}
	
	public PropertyInfo addByteProperty(PropertyObject component, int property, UndoableEditor editor) {
		return PanelUtils.addIntProperty(this, currentRow++, component, property, 
				component.getUnassignedProperties().containsKey(property) ? (byte) component.getUnassignedProperties().get(property) : 0, editor);
	}
	
	public PropertyInfo addFloatProperty(PropertyObject component, int property, UndoableEditor editor) {
		return PanelUtils.addFloatProperty(this, currentRow++, component, property, 
				component.getUnassignedProperties().containsKey(property) ? (float) component.getUnassignedProperties().get(property) : 0, editor);
	}
	
	public PropertyInfo addColorProperty(PropertyObject component, int property, UndoableEditor editor) {
		return PanelUtils.addColorProperty(this, currentRow++, component, property, 
				component.getUnassignedProperties().containsKey(property) ? (int) component.getUnassignedProperties().get(property) : 0, editor);
	}
	
	public <T extends SPUIComponent> PropertyInfo addShortProperty(PropertyObject component, int property, SPUIViewer viewer, ComponentChooser<T> chooser, boolean updateHierarchyTree) {
		return PanelUtils.addShortProperty(this, currentRow++, component, viewer, property, 
				component.getUnassignedProperties().containsKey(property) ? component.getUnassignedProperties().get(property) : null, chooser, updateHierarchyTree);
	}
	
	public JPanel addTextProperty(PropertyObject component, int property, UndoableEditor editor) {
		LocalizedText text = null;
		
		Object textProp = component.getUnassignedProperties().get(property);
		if (textProp != null) {
			text = (LocalizedText) textProp;
		}
		
		return PanelUtils.addTextProperty(this, currentRow++, Hasher.getSPUIName(property), text, editor);
	}
}

