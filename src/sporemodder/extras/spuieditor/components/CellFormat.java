package sporemodder.extras.spuieditor.components;

import java.awt.Color;

import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.StyleSheet;
import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.extras.spuieditor.uidesigner.UIDesigner;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUISectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.utilities.Hasher;

public class CellFormat extends PropertyObject {

	private final Color[] colors = new Color[8];
	private StyleSheetInstance style;
	private String styleName;
	
	public CellFormat() {
		unassignedProperties.put(0x00000001, 0);
		
		styleName = "DefaultStyle";
		style = StyleSheet.getActiveStyleSheet().getInstance(styleName);
		
		getColors()[0] = new Color(0xff000000);
		getColors()[1] = new Color(0xffffffff);
		getColors()[2] = new Color(0xffc0c0c0);
		getColors()[3] = new Color(0xffffffff);
		getColors()[4] = new Color(0xffffffff);
		getColors()[5] = new Color(0x7f000000);
		getColors()[6] = new Color(0xffffffff);
		getColors()[7] = new Color(0xbf000000);
		
		unassignedProperties.put(0x00000003, (byte) 1);
		unassignedProperties.put(0x00000004, (byte) 2);
		unassignedProperties.put(0x00000005, (byte) 6);
		unassignedProperties.put(0x00000006, 0);
		unassignedProperties.put(0x00000007, 0xFF000000);
	}
	
	public void parse(SPUISectionContainer sections) throws InvalidBlockException {
		
		int styleID = getIntProperty(sections, 0x00000001, 0);
		style = StyleSheet.getActiveStyleSheet().getInstance(styleID);
		styleName = style == null ? Hasher.getFileName(styleID) : style.getName();
		
		int[] colorValues = SectionInt.getValues(sections, 0x00000002, new int[getColors().length], getColors().length);
		for (int i = 0; i < colorValues.length; i++) {
			getColors()[i] = PanelUtils.decodeColor(colorValues[i]);
		}
		
		addUnassignedByte2(sections, 0x00000003, (byte) 1);
		addUnassignedByte2(sections, 0x00000004, (byte) 2);
		addUnassignedByte2(sections, 0x00000005, (byte) 6);
		addUnassignedInt(sections, 0x00000006, 0);
		addUnassignedInt(sections, 0x00000007, 0xFF000000);
	}
	
	public CellFormat copyComponent(CellFormat other) {
		other.unassignedProperties.putAll(unassignedProperties);
		
		other.style = style;
		other.styleName = styleName;
		System.arraycopy(getColors(), 0, other.getColors(), 0, getColors().length);
		
		return other;
	}
	
	public ListSectionContainer saveComponent(SPUIBuilder builder) {
		ListSectionContainer container = new ListSectionContainer();
		
		saveInt(builder, container, 0x00000006);
		builder.addInt(container, 0x00000001, new int[] {style == null ? Hasher.getFileHash(styleName) : style.getStyleID()});
		
		int[] colorValues = new int[getColors().length];
		for (int i = 0; i < getColors().length; i++) {
			colorValues[i] = getColors()[i] == null ? 0 : PanelUtils.encodeColor(getColors()[i]);
		}
		builder.addInt(container, 0x00000002, colorValues);
		
		saveByte2(builder, container, 0x00000003);
		saveByte2(builder, container, 0x00000004);
		saveByte2(builder, container, 0x00000005);
		saveInt(builder, container, 0x00000007);
		
		return container;
	}
	
	public PropertiesPanel getPropertiesPanel(final SPUIViewer viewer, String title) {
		PropertiesPanel panel = new PropertiesPanel(title);
		
		UIDesigner.Designer.getClass("CellFormat").fillPropertiesPanel(panel, new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x00000001: return styleName;
				case 0x00000002: return getColors();
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x00000001: 
					styleName = (String) value;
					if (styleName == null || styleName.isEmpty()) {
						styleName = "DefaultStyle";
					}
					
					style = StyleSheet.getActiveStyleSheet().getStyleInstance(styleName);
					
					break;
					
				case 0x00000002:
					getColors()[index] = (Color) value;
					break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		}, viewer.getEditor());
		
		return panel;
	}

	public Color[] getColors() {
		return colors;
	}
}
