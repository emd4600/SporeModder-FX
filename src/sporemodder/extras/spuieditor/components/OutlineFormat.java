package sporemodder.extras.spuieditor.components;

import java.awt.Color;

import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerElement;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.extras.spuieditor.uidesigner.UIDesigner;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUISectionContainer;

public class OutlineFormat extends PropertyObject {
	
	public static final String DESIGNER_NAME = "Outline Format";
	
	private int size;
	private int strength;
	private int quality;
	private float xOffset;
	private float yOffset;
	private float xSize;
	private float ySize;
	private float smoothness;
	private float saturation;
	private Color color;
	
	public OutlineFormat() {
		size = 0;
		strength = 2;
		quality = 2;
		xOffset = 0;
		yOffset = 0;
		xSize = 0;
		ySize = 0;
		smoothness = 3.0E-4f;
		saturation = 0.5f;
		color = new Color(0, 0, 0, 0);
	}
	
	public void parse(SPUISectionContainer sections) throws InvalidBlockException {
		size = getIntProperty(sections, 0x00000001, 0);
		strength = getIntProperty(sections, 0x00000002, 2);
		quality = getIntProperty(sections, 0x00000003, 2);
		
		xOffset = getFloatProperty(sections, 0x00000004, 0);
		yOffset = getFloatProperty(sections, 0x00000005, 0);
		xSize = getFloatProperty(sections, 0x00000006, 0);
		ySize = getFloatProperty(sections, 0x00000007, 0);
		smoothness = getFloatProperty(sections, 0x00000008, 3.0E-4f);
		saturation = getFloatProperty(sections, 0x00000009, 0.5f);
		
		color = PanelUtils.decodeColor(getIntProperty(sections, 0x0000000A, 0));
		
		//TODO draw shadow
		int placeholder;
	}
	
	public OutlineFormat copyComponent(OutlineFormat other) {
		other.size = size;
		other.strength = strength;
		other.quality = quality;
		other.xOffset = xOffset;
		other.yOffset = yOffset;
		other.xSize = xSize;
		other.ySize = ySize;
		other.smoothness = smoothness;
		other.saturation = saturation;
		other.color = color;
		
		return other;
	}
	
	public ListSectionContainer saveComponent(SPUIBuilder builder) {
		ListSectionContainer sectionContainer = new ListSectionContainer();
		
		builder.addInt(sectionContainer, 0x00000001, new int[] {size});
		builder.addInt(sectionContainer, 0x00000002, new int[] {strength});
		builder.addInt(sectionContainer, 0x00000003, new int[] {quality});
		builder.addFloat(sectionContainer, 0x00000004, new float[] {xOffset});
		builder.addFloat(sectionContainer, 0x00000005, new float[] {yOffset});
		builder.addFloat(sectionContainer, 0x00000006, new float[] {xSize});
		builder.addFloat(sectionContainer, 0x00000007, new float[] {ySize});
		builder.addFloat(sectionContainer, 0x00000008, new float[] {smoothness});
		builder.addFloat(sectionContainer, 0x00000009, new float[] {saturation});
		builder.addInt(sectionContainer, 0x0000000A, new int[] {PanelUtils.encodeColor(color)});
		
		return sectionContainer;
	}
	
	public PropertiesPanel getPropertiesPanel(SPUIViewer viewer, String title) {
		PropertiesPanel panel = new PropertiesPanel(title);
		
		UIDesigner.Designer.getClass(DESIGNER_NAME).fillPropertiesPanel(panel, new DesignerClassDelegate() {

			@Override
			public boolean isValid(DesignerElement element) {
				return true;
			}

			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				case 0x00000001: size = (int) value; return;
				case 0x00000002: strength = (int) value; return;
				case 0x00000003: quality = (int) value; return;
				case 0x00000004: xOffset = (float) value; return;
				case 0x00000005: yOffset = (float) value; return;
				case 0x00000006: xSize = (float) value; return;
				case 0x00000007: ySize = (float) value; return;
				case 0x00000008: smoothness = (float) value; return;
				case 0x00000009: saturation = (float) value; return;
				case 0x0000000A: color = (Color) value; return;
				}
			}

			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				case 0x00000001: return size;
				case 0x00000002: return strength;
				case 0x00000003: return quality;
				case 0x00000004: return xOffset;
				case 0x00000005: return yOffset;
				case 0x00000006: return xSize;
				case 0x00000007: return ySize;
				case 0x00000008: return smoothness;
				case 0x00000009: return saturation;
				case 0x0000000A: return color;
				default: return null;
				}
			}

			@Override
			public void propertyComponentAdded(DesignerProperty property, Object component) {
			}
			
		}, viewer.getEditor());
		
		
		return panel;
	}
}
