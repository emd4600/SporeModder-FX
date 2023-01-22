package sporemodder.extras.spuieditor.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.ComponentChooser.ComponentChooserCallback;
import sporemodder.extras.spuieditor.PanelUtils.PropertyInfo;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerElement;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionBoolean;
import sporemodder.files.formats.spui.SPUINumberSections.SectionByte;
import sporemodder.files.formats.spui.SPUINumberSections.SectionByte2;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt2;
import sporemodder.files.formats.spui.SPUINumberSections.SectionIntName;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUISectionContainer;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec2;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.Hasher;

public abstract class PropertyObject {
	protected final HashMap<Integer, Object> unassignedProperties = new HashMap<Integer, Object>();
	
	public HashMap<Integer, Object> getUnassignedProperties() {
		return unassignedProperties;
	}
	
	public void addUnassignedBoolean(SPUISectionContainer block, int property, boolean defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionBoolean.getValues(block.getSection(property, SectionBoolean.class), new boolean[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedShort(SPUIBlock block, int property, Object defaultValue) throws InvalidBlockException {
		short index = SectionShort.getValues(block.getSection(property, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			SPUIComponent obj = null;
			try {
				obj = ResourceLoader.getComponent(block.getParent().get(index));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			unassignedProperties.put(property, obj == null ? block.getParent().get(index) : obj);
		} else {
			unassignedProperties.put(property, defaultValue);
		}
	}
	
	public void addUnassignedInt(SPUISectionContainer block, int property) throws InvalidBlockException {
		int[] values = SectionInt.getValues(block, property, new int[0], 1);
		if (values.length > 0) {
			unassignedProperties.put(property, values[0]);
		}
	}
	
	public void addUnassignedFloat(SPUISectionContainer block, int property) throws InvalidBlockException {
		float[] values = SectionFloat.getValues(block.getSection(property, SectionFloat.class), new float[0], 1);
		if (values.length > 0) {
			unassignedProperties.put(property, values[0]);
		}
	}
	
	public int getIntProperty(SPUISectionContainer block, int property, int defaultValue) throws InvalidBlockException {
		return SectionInt.getValues(block, property, new int[] {defaultValue}, 1)[0];
	}
	
	public float getFloatProperty(SPUISectionContainer block, int property, float defaultValue) throws InvalidBlockException {
		return SectionFloat.getValues(block.getSection(property, SectionFloat.class), new float[] {defaultValue}, 1)[0];
	}
	
	
	public void addUnassignedInt(SPUISectionContainer block, int property, int defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionInt.getValues(block, property, new int[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedIntName(SPUISectionContainer block, int property, String defaultValue) throws InvalidBlockException {
		String value = SectionIntName.getValues(block, property, new String[] {defaultValue}, 1)[0];
		if (value != null) {
			if (value.equals("0") || value.length() == 0) {
				// no text looks better
				value = null;
			}
			else if (value.startsWith("$")) {
				value = value.substring(1);
			}
		}
		unassignedProperties.put(property, value);
	}
	
	public void addUnassignedFloat(SPUISectionContainer block, int property, float defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionFloat.getValues(block.getSection(property, SectionFloat.class), new float[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedByte(SPUISectionContainer block, int property, byte defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionByte.getValues(block.getSection(property, SectionByte.class), new byte[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedByte2(SPUISectionContainer block, int property, byte defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionByte2.getValues(block.getSection(property, SectionByte2.class), new byte[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedVec2(SPUISectionContainer block, int property, float[] defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionVec2.getValues(block.getSection(property, SectionVec2.class), new float[][] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedInt2(SPUISectionContainer block, int property, int defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionInt2.getValues(block.getSection(property, SectionInt2.class), new int[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassignedText(SPUISectionContainer block, int property, LocalizedText defaultValue) throws InvalidBlockException {
		unassignedProperties.put(property, SectionText.getValues(block.getSection(property, SectionText.class), new LocalizedText[] {defaultValue}, 1)[0]);
	}
	
	public void addUnassigned(SPUISectionContainer block, int property, Object defaultValue) throws InvalidBlockException {
		Object value = block.getSection(property);
		unassignedProperties.put(property, value == null ? defaultValue : value);
	}
	
	
	public void saveText(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		//builder.addText(block, property, value != null ? new LocalizedText[] {(LocalizedText) value} : null);
		if (value != null) {
			builder.addText(block, property, new LocalizedText[] {(LocalizedText) value});
		}
	}
	
	public void saveVec2(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addVec2(block, property, new float[][] {(float[]) (value == null ? new float[] {0, 0} : value)});
	}
	
	public void saveInt(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addInt(block, property, new int[] {value != null ? (int) value : 0});
	}
	
	public void saveIntName(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addIntName(block, property, new String[] {value != null ? (String) value : null});
	}
	
	public void saveInt2(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addInt2(block, property, new int[] {value != null ? (int) value : 0});
	}
	
	public void saveFloat(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addFloat(block, property, new float[] {value != null ? (float) value : 0});
	}
	
	public void saveReference(SPUIBuilder builder, SPUIBlock block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addReference(block, property, new SPUIObject[] {value != null ? builder.addComponent((SPUIComponent) value) : null});
	}
	
	public void saveByte(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addByte(block, property, new byte[] {value != null ? (byte) value : 0});
	}
	
	public void saveByte2(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addByte2(block, property, new byte[] {value != null ? (byte) value : 0});
	}
	
	public void saveBoolean(SPUIBuilder builder, SPUISectionContainer block, int property) {
		Object value = unassignedProperties.get(property);
		
		builder.addBoolean(block, property, new boolean[] {value != null ? (boolean) value  : false});
	}
	
	protected class DefaultDesignerDelegate implements DesignerClassDelegate {
		protected SPUIViewer viewer;
		protected final HashMap<DesignerProperty, JTextField> flagsetFields = new HashMap<DesignerProperty, JTextField>();
		protected final HashMap<DesignerProperty, JCheckBox> flagCheckboxes = new HashMap<DesignerProperty, JCheckBox>();
		
		public DefaultDesignerDelegate(SPUIViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public boolean isValid(DesignerElement element) {
			if (element instanceof DesignerProperty) {
				DesignerProperty property = (DesignerProperty) element;
				// we don't want properties that can't be saved to be displayed, except flags
				if (property.getProxyID() == -1 && !property.getType().equals("flag")) {
					return false;
				}
			}
			return true;
		}
		
		protected void setFlagValue(DesignerProperty property, Object value) {
			String flagset = property.getFlagset();
			DesignerProperty flagsetProp = null;
			for (DesignerProperty p : flagsetFields.keySet()) {
				if (p.getName().equals(flagset)) {
					flagsetProp = p;
					break;
				}
			}
			if (flagsetProp != null) {
				
				int mask = Hasher.decodeInt((String) property.getMask());
				int oldValue = (int) getValue(flagsetProp);
				
				oldValue &= ~mask;
				if ((boolean) value) {
					oldValue |= mask;
				}
				
				if (Hasher.decodeInt(flagsetFields.get(flagsetProp).getText()) != oldValue) {
					flagsetFields.get(flagsetProp).setText(Integer.toBinaryString(oldValue) + "b");
				}
				
			}
		}
		
		protected boolean getFlagValue(DesignerProperty property) {
			String flagset = property.getFlagset();
			DesignerProperty flagsetProp = null;
			for (DesignerProperty p : flagsetFields.keySet()) {
				if (p.getName().equals(flagset)) {
					flagsetProp = p;
				}
			}
			if (flagsetProp != null) {
				
				int mask = Hasher.decodeInt((String) property.getMask());
				return ((int) getValue(flagsetProp) & mask) == mask;
			}
			return false;
		}
		
		protected void setFlagsetValue(DesignerProperty property, Object value) {
			String flagset = property.getName();
			int flags = (int) value;
			for (Map.Entry<DesignerProperty, JCheckBox> entry : flagCheckboxes.entrySet()) {
				if (flagset.equals(entry.getKey().getFlagset())) {
					
					int mask = Hasher.decodeInt((String) entry.getKey().getMask());
					JCheckBox checkBox = entry.getValue();
					boolean newValue = (flags & mask) == mask;
					if (checkBox.isSelected() != newValue) {
						checkBox.setSelected(newValue);
					}
				}
			}
		}
		
		@Override
		public void setValue(DesignerProperty property, Object value, final int index) {
			final int proxyID = property.getProxyID();
			final String propertyType = property.getType();
			
			switch (propertyType) {
			case "flag":
				setFlagValue(property, value);
				break;
				
			case "flagset":
				if (unassignedProperties.containsKey(proxyID)) {
					if (index == -1) {
						unassignedProperties.put(proxyID, value);
					}
					else {
						((Object[]) unassignedProperties.get(proxyID))[index] = value;
					}
				}
				setFlagsetValue(property, value);
				break;
				
			default:
				if (unassignedProperties.containsKey(proxyID)) {
					Object object = unassignedProperties.get(proxyID);
					
					//TODO not really a good way!
					if (propertyType.startsWith("I")) {
						ComponentChooser.showChooserAction(new ComponentChooserCallback<SPUIComponent>() {
							@Override
							public SPUIComponent getValue() {
								if (index == -1) {
									return (SPUIComponent) unassignedProperties.get(proxyID);
								}
								else {
									return ((SPUIComponent[]) unassignedProperties.get(proxyID))[index];
								}
							}

							@Override
							public void valueChanged(SPUIComponent value) {
								if (index == -1) {
									unassignedProperties.put(proxyID, value);
								}
								else {
									((SPUIComponent[]) unassignedProperties.get(proxyID))[index] = value;
								}
								viewer.getEditor().fillHierarchyTree();
								viewer.getEditor().setSelectedComponent(viewer.getActiveComponent());
							}
						}, ComponentFactory.getComponentChooser(propertyType, viewer), (JLabelLink) value, viewer);
					}
					else {
						if (index == -1) {
							unassignedProperties.put(property.getProxyID(), value);
						}
						else {
							((Object[]) object)[index] = value;
						}
					}
				}
				break;
			}
		}

		@Override
		public void propertyComponentAdded(DesignerProperty property, Object component) {
			switch (property.getType()) {
			case "flag":
				flagCheckboxes.put(property, (JCheckBox) component);
				break;
				
			case "flagset":
				JTextField tf = (JTextField) ((PropertyInfo) component).components[0];
				flagsetFields.put(property, tf);
				setFlagsetValue(property, Hasher.decodeInt(tf.getText()));
				break;
			}
		}

		@Override
		public Object getValue(DesignerProperty property) {
			Object value;
			switch (property.getType()) {
			case "flag":
				return getFlagValue(property);
				
			default:
				value = unassignedProperties.get(property.getProxyID());
				
				if (value != null && property.getType().equals("uint32") && property.getValuesEnum() == null && "hex".equals(property.getFormat())) {
					// return "0x" + Integer.toHexString((int) value);
					return (String) value;
				}
				return value;
			}
			
		}
		
	}
}
