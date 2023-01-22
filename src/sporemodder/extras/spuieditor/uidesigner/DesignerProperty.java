package sporemodder.extras.spuieditor.uidesigner;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.extras.spuieditor.ComponentValueAction;
import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PanelUtils.BooleanValueAction;
import sporemodder.extras.spuieditor.PanelUtils.ColorValueAction;
import sporemodder.extras.spuieditor.PanelUtils.EnumValueAction;
import sporemodder.extras.spuieditor.PanelUtils.FloatValueAction;
import sporemodder.extras.spuieditor.PanelUtils.IntValueAction;
import sporemodder.extras.spuieditor.PanelUtils.PropertyInfo;
import sporemodder.extras.spuieditor.PanelUtils.ShortValueAction;
import sporemodder.extras.spuieditor.PanelUtils.TextFieldValueAction;
import sporemodder.extras.spuieditor.PanelUtils.TextValueAction;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.extras.spuieditor.StyleChooser;
import sporemodder.extras.spuieditor.StyleChooser.StyleValueAction;
import sporemodder.extras.spuieditor.components.CellFormat;
import sporemodder.extras.spuieditor.components.OutlineFormat;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.SPUIDefaultComponent;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.files.formats.LocalizedText;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.Hasher;

public class DesignerProperty implements DesignerElement {
	
	private String name;
	private String type;
	private String description;
	private int proxyID = -1;

	private String mask;
	private String flagset;
	private String format;
	private boolean deprecated;
	
	private float stepSize = 1.0f;
	
	private DesignerEnum valuesEnum;
	
	private UIDesigner designer;
	
	private HashMap<Integer, String> arrayIndices;

	public DesignerProperty(UIDesigner designer) {
		this.designer = designer;
	}

	@Override
	public String toString() {
		return "DesignerProperty [name=" + name + ", type=" + type + ", description=" + description + ", proxyID="
				+ proxyID + "]";
	}



	@Override
	public void parseElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("Property")) {
			name = attributes.getValue("name");
			
			String proxyStr = attributes.getValue("proxy");
			
			if (proxyStr != null) {
				if (proxyStr.startsWith("utfwin:")) {
					proxyStr = proxyStr.substring("utfwin:".length());
				}
				
				try {
					proxyID = Hasher.decodeInt(proxyStr);
				} catch (NumberFormatException e) {
					proxyID = Hasher.stringToFNVHash(proxyStr);
				}
			}
			
			type = attributes.getValue("type");
			description = attributes.getValue("description");
			
			format = attributes.getValue("format");
			mask = attributes.getValue("mask");
			flagset = attributes.getValue("flagset");
			
			String deprecatedStr = attributes.getValue("deprecated");
			if (deprecatedStr != null) {
				deprecated = Boolean.parseBoolean(deprecatedStr);
			}
			
			String enumStr = attributes.getValue("enum");
			if (enumStr != null) {
				valuesEnum = designer.getEnum(enumStr);
			}
			
			String stepStr = attributes.getValue("stepsize");
			if (stepStr != null) {
				stepSize = Float.parseFloat(stepStr);
			}
		}
		else if (qName.equalsIgnoreCase("Index")) {
			if (arrayIndices == null) {
				arrayIndices = new HashMap<Integer, String>();
			}
			
			arrayIndices.put(Integer.parseInt(attributes.getValue("value")), attributes.getValue("name"));
		}
	}
	
	@Override
	public void getDesignerElements(List<DesignerElement> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getName().equals(name)) {
				list.set(i, this);
				return;
			}
		}
		list.add(this);
	}
	
	@Override
	public void fillPropertiesPanel(PropertiesPanel panel, final DesignerClassDelegate delegate, SPUIEditor editor) {
		if (deprecated) {
			return;
		}
		
		fillPropertiesPanel(panel, delegate, editor, type, name, delegate.getValue(this), -1);
	}
	
	@SuppressWarnings("unchecked")
	private void addArrayValue(PropertiesPanel panel, DesignerClassDelegate delegate, SPUIEditor editor, String type, String name, Object propertyValue) {
		
		String countStr = type.substring(type.indexOf("[") + 1, type.indexOf("]"));
		
		if (countStr.length() > 0) {
			int count = Integer.parseInt(countStr);
			
			PropertiesPanel arrayPanel = new PropertiesPanel(name);
			arrayPanel.setToolTipText(description);
			panel.addPanel(arrayPanel);
			
			Object[] values = (Object[]) propertyValue;
			
			for (int i = 0; i < count; i++) {
				fillPropertiesPanel(arrayPanel, delegate, editor, type.substring(0, type.indexOf("[")), 
						arrayIndices == null ? null : arrayIndices.get(i), values[i], i);
			}
		}
		else {
			PropertiesPanel p = addListValuesPanel(delegate, editor, type.substring(0, type.indexOf("[")), name, (List<Object>) propertyValue);
			p.setToolTipText(description);
			panel.addPanel(p);
		}
	}

	private Object fillPropertiesPanel(PropertiesPanel panel, final DesignerClassDelegate delegate, final SPUIEditor editor, String type, String name, Object propertyValue, final int index) {
		
		Object pi = null;
		
		switch(type) {
		case "uint32":
			pi = addIntValue(panel, delegate, editor, name, propertyValue, index, false, false);
			break;
			
		case "int32":
			pi = addIntValue(panel, delegate, editor, name, propertyValue, index, true, false);
			break;
			
		case "uint8":
			pi = addIntValue(panel, delegate, editor, name, propertyValue, index, false, true);
			break;
			
		case "int8":
			pi = addIntValue(panel, delegate, editor, name, propertyValue, index, true, true);
			break;
			
		case "float":
//			pi = panel.addFloatFieldValue(name, (float) propertyValue, new FloatValueAction() {
//				@Override
//				public void valueChanged(float value) {
//					delegate.setValue(DesignerProperty.this, value, index);
//				}
//			}, editor);
//			break;
			
			pi = panel.addFloatValue(name, (float) propertyValue, null, null, stepSize, new FloatValueAction() {
				@Override
				public void valueChanged(float value) {
					delegate.setValue(DesignerProperty.this, value, index);
				}
			}, editor);
			break;
			
		case "color":
			Color color;
			if (propertyValue instanceof Color) {
				color = (Color) propertyValue;
			} else {
				color = PanelUtils.decodeColor((int) propertyValue);
			}
			pi = panel.addColorValue(name, "Choose color", color, new ColorValueAction() {
				@Override
				public void valueChanged(Color color) {
					delegate.setValue(DesignerProperty.this, color, index);
				}
			}, editor);
			break;
			
		case "string_resource":
		case "string":
			pi = addTextPanel(panel, delegate, editor, name, (LocalizedText) propertyValue, index);
			break;
			
		case "bool":
		case "flag":
			pi = panel.addBooleanValue(name, (boolean) propertyValue, new BooleanValueAction() {
				@Override
				public void valueChanged(boolean isSelected) {
					delegate.setValue(DesignerProperty.this, isSelected, index);
				}
			}, editor);
			break;
			
		case "flagset":
			pi = panel.addIntValue(name, Integer.toBinaryString((int) propertyValue) + "b", new IntValueAction() {

				@Override
				public void valueChanged(int value) {
					delegate.setValue(DesignerProperty.this, value, index);
				}
				
			}, editor);
			break;
			
		case "rectf":
			pi = createRectfPanel(panel, delegate, editor, name, (Rectangle) propertyValue, index);
			break;
			
		case "Borders":
			pi = addBordersValue(panel, delegate, editor, name, (float[]) propertyValue, index);
			break;
			
		case "pointf":
			pi = addPointfValue(panel, delegate, editor, name, (float[]) propertyValue, index);
			break;
			
		case "Outline Format":
			PropertiesPanel p = ((OutlineFormat) propertyValue).getPropertiesPanel(editor.getSPUIViewer(), name);
			panel.addPanel(p);
			pi = p;
			break;
			
		case "CellFormat":
			PropertiesPanel p2 = ((CellFormat) propertyValue).getPropertiesPanel(editor.getSPUIViewer(), name);
			panel.addPanel(p2);
			pi = p2;
			break;
			
		case "textstyle":
			pi = addTextStyleValue(panel, delegate, editor, name, (String) propertyValue, index);
			break;
			
		case "anchor":
			// special case, we don't do anything here because everything will be done in SimpleLayout class
			break;
			
		case "class":
			// just do nothing
			break;
			
		default:
			if (type.contains("[")) {
				addArrayValue(panel, delegate, editor, type, name, propertyValue);
			}
			else {
				// a chooser type
				addReferenceValue(panel, delegate, editor, name, propertyValue, index);
			}
			break;
		}
		
		if (pi != null) { 
			delegate.propertyComponentAdded(this, pi);
			
			setTooltip(pi);
		}
		
		return pi;
	}
	
	private PropertyInfo addReferenceValue(PropertiesPanel panel, final DesignerClassDelegate delegate, final SPUIEditor editor, String name, Object propertyValue, final int arrayIndex) {
		PropertiesPanel shortPanel = new PropertiesPanel();
		shortPanel.setBorder(null);
		
		//panel.addPanel(shortPanel);  // this adds 5 extra empty pixels in the bottom
		PanelUtils.addGBC(panel, shortPanel, 0, panel.getNextRow(), GridBagConstraints.WEST, new Insets(0, 0, 0, 0), 2, 1, GridBagConstraints.HORIZONTAL, 1.0f, 0f);
		
		return shortPanel.addShortValue(name, propertyValue, new ShortValueAction() {

			@Override
			public void linkAction(JLabelLink labelLink) {
				Object currentValue = delegate.getValue(DesignerProperty.this);
				if (arrayIndex != -1) {
					currentValue = ((SPUIComponent[]) currentValue)[arrayIndex];
				}
				editor.getSPUIViewer().setActiveComponent((SPUIComponent) currentValue);
			}

			@Override
			public void changeAction(JLabelLink labelLink) {
				delegate.setValue(DesignerProperty.this, labelLink, arrayIndex);
				
				Object currentValue = delegate.getValue(DesignerProperty.this);
				if (arrayIndex != -1) {
					currentValue = ((SPUIComponent[]) currentValue)[arrayIndex];
				}
				
				labelLink.setText(currentValue == null ? "None" : currentValue.toString());
				labelLink.setActionActive(currentValue != null);
			}
			
		});
	}

	private PropertyInfo[] addBordersValue(PropertiesPanel panel, final DesignerClassDelegate delegate, SPUIEditor editor, String name, final float[] propertyValue, final int arrayIndex) {
		PropertiesPanel bordersPanel = new PropertiesPanel(name);
		panel.addPanel(bordersPanel);
		
		PropertyInfo[] pi = new PropertyInfo[propertyValue.length];
		
		for (int i = 0; i < propertyValue.length; i++) {
			final int index = i;
			pi[i] = bordersPanel.addFloatValue(SPUIDefaultComponent.getConstantString(i), propertyValue[i], null, null, stepSize, new FloatValueAction() {
				@Override
				public void valueChanged(float value) {
					propertyValue[index] = value;
					delegate.setValue(DesignerProperty.this, propertyValue, arrayIndex);
				}
			}, editor);
		}
		
		return pi;
	}
	
	private PropertyInfo[] addPointfValue(PropertiesPanel panel, final DesignerClassDelegate delegate, SPUIEditor editor, String name, final float[] propertyValue, final int arrayIndex) {
		PropertiesPanel bordersPanel = new PropertiesPanel(name);
		panel.addPanel(bordersPanel);
		
		PropertyInfo[] pi = new PropertyInfo[propertyValue.length];
		
		for (int i = 0; i < propertyValue.length; i++) {
			final int index = i;
			pi[i] = bordersPanel.addFloatValue(SPUIDefaultComponent.getConstantPointString(i), propertyValue[i], null, null, stepSize, new FloatValueAction() {
				@Override
				public void valueChanged(float value) {
					propertyValue[index] = value;
					delegate.setValue(DesignerProperty.this, propertyValue, arrayIndex);
				}
			}, editor);
		}
		
		return pi;
	}
	
	private Object addIntValue(PropertiesPanel panel, final DesignerClassDelegate delegate, SPUIEditor editor, String name, Object propertyValue, final int arrayIndex,
			boolean isSigned, boolean isByte) {
		
		if (valuesEnum != null) {
			List<String> values = valuesEnum.getStringValues();
			String selectedValue;
			if (isByte) {
				selectedValue = valuesEnum.getValue((byte) propertyValue);
			} else {
				selectedValue = valuesEnum.getValue((int) propertyValue);
			}
			
			return panel.addEnumValue(name, selectedValue, (String[]) values.toArray(new String[values.size()]), new EnumValueAction() {
				@Override
				public void valueChanged(int selectedIndex, Object selectedValue) {
					delegate.setValue(DesignerProperty.this, valuesEnum.getValue((String) selectedValue), arrayIndex);
				}
				
			}, editor);
		}
		else {
			if ("hex".equals(format)) {
				return panel.addTextFieldValue(name, (String) propertyValue, new TextFieldValueAction() {
					@Override
					public void documentModified(DocumentEvent event, String textFieldText) {
						delegate.setValue(DesignerProperty.this, textFieldText, arrayIndex);
					}
				}, editor);
			}
			else {
				if (isSigned) {
					if (isByte) {
						return panel.addIntSpinnerValue(name, (byte) propertyValue, (int) Byte.MIN_VALUE, (int) Byte.MAX_VALUE, 1, new IntValueAction() {
							@Override
							public void valueChanged(int value) {
								delegate.setValue(DesignerProperty.this, value, arrayIndex);
							}
						}, editor);
					} 
					else {
						return panel.addIntSpinnerValue(name, (int) propertyValue, null, null, 1, new IntValueAction() {
							@Override
							public void valueChanged(int value) {
								delegate.setValue(DesignerProperty.this, value, arrayIndex);
							}
						}, editor);
					}
				}
				else {
					if (isByte) {
						return panel.addIntSpinnerValue(name, Byte.toUnsignedInt((byte) propertyValue), 0, 255, 1, new IntValueAction() {
							@Override
							public void valueChanged(int value) {
								delegate.setValue(DesignerProperty.this, value, arrayIndex);
							}
						}, editor);
					}
					else {
						String text;
						int intValue = (int) propertyValue;
						if (intValue < 0 || intValue > 10000000) {
							text = "0x" + Integer.toHexString(intValue);
						} else {
							text = Integer.toString(intValue);
						}
						return panel.addIntValue(name, text, new IntValueAction() {

							@Override
							public void valueChanged(int value) {
								delegate.setValue(DesignerProperty.this, value, arrayIndex);
							}
							
						}, editor);
					}
				}
			}
		}
	}
	
	private Object createRectfPanel(PropertiesPanel panel, final DesignerClassDelegate delegate, SPUIEditor editor, String name, final Rectangle bounds, final int arrayIndex) {
		PropertiesPanel boundsPanel = new PropertiesPanel(name);
		panel.addPanel(boundsPanel);
		if (description != null) {
			boundsPanel.setToolTipText(description);
		}
		
		PropertyInfo[] pi = new PropertyInfo[4];
		
		pi[0] = boundsPanel.addFloatValue("Top-left X:", bounds.x, null, null, 1f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				bounds.width = bounds.width + (bounds.x - (int) value);
				bounds.x = (int) value;
				delegate.setValue(DesignerProperty.this, bounds, arrayIndex);
			}
		}, editor);
		
		pi[1] = boundsPanel.addFloatValue("Top-left Y:", bounds.y, null, null, 1f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				bounds.height = bounds.height + (bounds.y - (int) value);
				bounds.y = (int) value;
				delegate.setValue(DesignerProperty.this, bounds, arrayIndex);
			}
		}, editor);
		
		pi[2] = boundsPanel.addFloatValue("Bottom-right X:", bounds.x + bounds.width, null, null, 1f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				bounds.width = (int) value - bounds.x;
				delegate.setValue(DesignerProperty.this, bounds, arrayIndex);
			}
		}, editor);
		
		pi[3] = boundsPanel.addFloatValue("Bottom-right Y:", bounds.y + bounds.height, null, null, 1f, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				bounds.height = (int) value - bounds.y;
				delegate.setValue(DesignerProperty.this, bounds, arrayIndex);
			}
		}, editor);
		
		return pi;
	}
	
	private PropertyInfo addTextStyleValue(PropertiesPanel panel, final DesignerClassDelegate delegate, final SPUIEditor editor, String name, String propertyValue, final int arrayIndex) {
		
		return panel.addLinkValue(name, propertyValue, new ShortValueAction() {

			@Override
			public void linkAction(final JLabelLink labelLink) {
				Object rawValue = delegate.getValue(DesignerProperty.this);
				String oldValue;
				
				if (arrayIndex == -1) {
					oldValue = (String) rawValue;
				} else {
					oldValue = ((String[]) rawValue)[arrayIndex];
				}
				
				final StyleChooser chooser = new StyleChooser(editor, "Choose a style");
				chooser.setStyleValueAction(new StyleValueAction() {

					@Override
					public void styleChanged(String styleName) {
						delegate.setValue(DesignerProperty.this, chooser.getSelectedStyleName(), arrayIndex);
						
						labelLink.setText(styleName);
					}
					
				});
				chooser.setSelectedStyle(oldValue);
				chooser.setDefaultCloseOperation(StyleChooser.DISPOSE_ON_CLOSE);
				chooser.setVisible(true);
				
				if (!chooser.wasCancelled()) {
					String selectedName = chooser.getSelectedStyleName();
					labelLink.setText(selectedName);
					delegate.setValue(DesignerProperty.this, selectedName, arrayIndex);
					
					editor.addCommandAction(new ComponentValueAction<String>(oldValue, selectedName, new ComponentValueListener<String>() {
						@Override
						public void valueChanged(String value) {
							labelLink.setText(value);
							delegate.setValue(DesignerProperty.this, value, arrayIndex);
						}
					}));
				} 
				else {
					delegate.setValue(DesignerProperty.this, oldValue, arrayIndex);
					labelLink.setText(oldValue);
				}
			}

			@Override
			public void changeAction(JLabelLink labelLink) {
			}
		});
	}

	private void setTooltip(Object pi) {
		if (description != null) {
			if (pi instanceof PropertyInfo) {
				((PropertyInfo) pi).setTooltip(description);
			}
			else if (pi instanceof PropertyInfo[]) {
				PropertyInfo[] array = (PropertyInfo[]) pi;
				for (PropertyInfo p : array) {
					p.setTooltip(description);
				}
			}
			else if (pi instanceof JComponent) {
				((JComponent) pi).setToolTipText(description);
			}
		}
	}
	
	private void removeFromPanel(Object pi, JPanel parentPanel) {
		if (pi instanceof PropertyInfo) {
			PropertyInfo p = ((PropertyInfo) pi);
			if (p.label != null) {
				parentPanel.remove(p.label);
			}
			if (p.components != null) {
				for (JComponent comp : p.components) {
					parentPanel.remove(comp);
				}
			}
		}
		else if (pi instanceof PropertyInfo[]) {
			PropertyInfo[] array = (PropertyInfo[]) pi;
			for (PropertyInfo p : array) {
				if (p.label != null) {
					parentPanel.remove(p.label);
				}
				if (p.components != null) {
					for (JComponent comp : p.components) {
						parentPanel.remove(comp);
					}
				}
			}
		}
		else if (pi instanceof JComponent) {
			parentPanel.remove((JComponent) pi);
		}
	}

	private PropertiesPanel addListValuesPanel(final DesignerClassDelegate delegate, final SPUIEditor editor, final String type, final String name, final List<Object> values) {
		
		final PropertiesPanel valuesPanel = new PropertiesPanel();
		
		final List<Object> valuePanels = new ArrayList<Object>();
		
		int index = 0;
		for (final Object value : values) {
			Object obj = fillPropertiesPanel(valuesPanel, delegate, editor, type, name, value, index++);
			setTooltip(obj);
			valuePanels.add(obj);
		}
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalizedText value = new LocalizedText("Value " + values.size());
				values.add(value);
				
				Object obj = fillPropertiesPanel(valuesPanel, delegate, editor, type, name, value, values.size());
				setTooltip(obj);
				valuePanels.add(obj);
				valuesPanel.revalidate();
			}
		});
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeFromPanel(valuePanels.get(values.size() - 1), valuesPanel);
				valuePanels.remove(values.size() - 1);
				
				values.remove(values.size() - 1);
				
				valuesPanel.revalidate();
			}
		});
		
		buttonsPanel.add(btnAdd);
		buttonsPanel.add(btnRemove);
		
		PropertiesPanel finalValuePanel = new PropertiesPanel(name);
		finalValuePanel.addPanel(valuesPanel);
		finalValuePanel.addPanel(buttonsPanel);
		
		return finalValuePanel;
	}
	
	private JPanel addTextPanel(PropertiesPanel valuesPanel, final DesignerClassDelegate delegate, SPUIEditor editor, String name, final LocalizedText value, final int index) {
		return valuesPanel.addTextValue(name, value, new TextValueAction() {
			@Override
			public void textChanged(LocalizedText text) {
				delegate.setValue(DesignerProperty.this, text, index);
			}
		}, editor);
	}
	

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public int getProxyID() {
		return proxyID;
	}

	public String getMask() {
		return mask;
	}

	public String getFlagset() {
		return flagset;
	}

	public String getFormat() {
		return format;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public DesignerEnum getValuesEnum() {
		return valuesEnum;
	}

	public UIDesigner getDesigner() {
		return designer;
	}

	public HashMap<Integer, String> getArrayIndices() {
		return arrayIndices;
	}
	
	
}
