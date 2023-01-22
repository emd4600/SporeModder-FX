package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;

public class WinText extends Window {

	public static final int TYPE = 0x6F15F51B;
	
	private Color fontColor;
	private final float[] margins = new float[4]; 
	private final OutlineFormat textOutline = new OutlineFormat();
	
	public WinText(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		fontColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0x6F43FCCD, new int[] { 0 }, 1)[0]);
		
		parseMarginsSections(block, 0x6F43FCCE, margins);
		
		ListSectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		
		if (property_039A69E6 != null) {
			textOutline.parse(property_039A69E6[0]);;
		}
		
	}
	
	public WinText(SPUIViewer viewer) {
		super(viewer);
		
		fontColor = Color.WHITE;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addSectionList(block, 0x6F43FCCE, new ListSectionContainer[] {saveMarginsSections(margins)}, 50);
		
		builder.addInt(block, 0x6F43FCCD, new int[] {PanelUtils.encodeColor(fontColor == null ? Color.black : fontColor)});
		
		builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {textOutline.saveComponent(builder)}, 122);
		
		return block;
	}
	
	protected WinText() {
		super();
	}
	
	protected void copyComponent(WinText other, boolean propagateIndependent) {
		super.copyComponent(other, propagateIndependent);
		
		other.fontColor = fontColor;
		for (int i = 0; i < margins.length; i++) {
			other.margins[i] = margins[i];
		}
		textOutline.copyComponent(other.textOutline);
	}

	@Override
	public WinText copyComponent(boolean propagateIndependent) {
		WinText other = new WinText();
		copyComponent(other, propagateIndependent);
		return other;
	}
	
	@Override
	public void paintComponent(Graphics2D graphics) {
		super.paintComponent(graphics);
		
		if (!shouldSkipPaint() && caption != null) {
			StyleSheetInstance.paintText(graphics, style, viewer.getString(caption), fontColor, realBounds, margins);
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x6F43FCCD: return fontColor;
				case 0x6F43FCCE: return margins;
				case 0x039A69E6: return textOutline;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x6F43FCCD: fontColor = (Color) value; break;
				case 0x6F43FCCE: System.arraycopy((float[]) value, 0, margins, 0, margins.length); break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
}
