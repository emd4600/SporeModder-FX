package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUISection;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec4;

public class WinTextEdit extends Window {
	
	public static final int TYPE = 0x6F42B4F0;
	
	public static final int FLAG_SHOWCARET = 0x8;
	
	private static final int COLOR_IDLE_TEXT = 0;
	private static final int COLOR_IDLE_BACKGROUND = 1;
	private static final int COLOR_DISABLED_TEXT = 2;
	private static final int COLOR_DISABLED_BACKGROUND = 3;
	private static final int COLOR_SELECTED_TEXT = 4;
	private static final int COLOR_SELECTED_BACKGROUND = 5;
	private static final int COLOR_UNKNOWN_TEXT = 6;
	private static final int COLOR_UNKNOWN_BACKGROUND = 7;
	
	
	private final Color[] colors = new Color[8];
	private final OutlineFormat textOutline = new OutlineFormat();
	private final float[] margins = new float[4];
	
	private float blinkFrequency = 0.25f;
	
	public WinTextEdit(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);

		int[] colorValues = SectionInt.getValues(block, 0x0FF11731, new int[colors.length], colors.length);
		for (int i = 0; i < colorValues.length; i++) {
			colors[i] = PanelUtils.decodeColor(colorValues[i]);
		}
		
		SPUISection textSection = block.getSection(0x0FF11732);
		if (textSection != null) {
			if (textSection.getType() == SectionSectionList.TYPE) {
				parseMarginsSections(block, 0x0FF11732, margins);
			}
			else {
				// some old SPUIs use vec4s
				float[] data = ((SectionVec4) textSection).data[0];
				System.arraycopy(data, 0, margins, 0, margins.length);
			}
		}
		
		ListSectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		
		if (property_039A69E6 != null) {
			textOutline.parse(property_039A69E6[0]);;
		}
		
		blinkFrequency = SectionFloat.getValues(block.getSection(0x0FF11733, SectionFloat.class), new float[] {0.25f}, 1)[0];
		
		addUnassignedInt(block, 0x0FF11739, 0);
		addUnassignedInt(block, 0x0FF1173A, 0);
		// type 0xEEF3AF8C
		addUnassignedShort(block, 0x0FF1173B, null);  // ScrollbarDrawable ?
		addUnassignedShort(block, 0x0FF1173C, null);  // ScrollbarDrawable ?
		
		addUnassignedInt(block, 0x0FF11730, 84);
		addUnassignedInt(block, 0x0FF11734, 0);
		addUnassignedInt2(block, 0x0FF11735, -1);
		addUnassignedInt(block, 0x0FF11736, 0);
		addUnassignedInt(block, 0x0FF11737, 0);
		addUnassignedInt(block, 0x0FF11738, 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {textOutline.saveComponent(builder)}, 122);
		
		int[] colorValues = new int[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colorValues[i] = PanelUtils.encodeColor(colors[i]);
		}
		builder.addInt(block, 0x0FF11731, colorValues);
		
		builder.addSectionList(block, 0x0FF11732, new ListSectionContainer[] {saveMarginsSections(margins)}, 50);
		builder.addFloat(block, 0x0FF11733, new float[] {blinkFrequency});
		saveInt(builder, block, 0x0FF11739);
		saveInt(builder, block, 0x0FF1173A);
		saveReference(builder, block, 0x0FF1173B);
		saveReference(builder, block, 0x0FF1173C);
		saveInt(builder, block, 0x0FF11730);
		saveInt(builder, block, 0x0FF11734);
		saveInt2(builder, block, 0x0FF11735);
		saveInt(builder, block, 0x0FF11736);
		saveInt(builder, block, 0x0FF11737);
		saveInt(builder, block, 0x0FF11738);
		
		return block;
	}

	public WinTextEdit(SPUIViewer viewer) {
		super(viewer);
		
		colors[COLOR_IDLE_TEXT] = new Color(0xff000000);
		colors[COLOR_IDLE_BACKGROUND] = new Color(0xffffffff);
		colors[COLOR_DISABLED_TEXT] = new Color(0xff808080);
		colors[COLOR_DISABLED_BACKGROUND] = new Color(0xffffffff);
		colors[COLOR_SELECTED_TEXT] = new Color(0xff0080ff);
		colors[COLOR_SELECTED_BACKGROUND] = new Color(0xffc0c0ff);
		colors[COLOR_UNKNOWN_TEXT] = new Color(0xff00ff00);
		colors[COLOR_UNKNOWN_BACKGROUND] = new Color(0xff000000);
		
		margins[LEFT] = margins[RIGHT] = 4.0f;
		blinkFrequency = 0.25f;
		
		flags |= FLAG_SHOWCARET;
		
		unassignedProperties.put(0x0FF11739, (int) 0);
		unassignedProperties.put(0x0FF1173A, (int) 0);
		unassignedProperties.put(0x0FF1173B, null);
		unassignedProperties.put(0x0FF1173C, null);
		unassignedProperties.put(0x0FF11730, (int) 84);
		unassignedProperties.put(0x0FF11734, (int) 0);
		unassignedProperties.put(0x0FF11735, (int) -1);
		unassignedProperties.put(0x0FF11736, (int) 0);
		unassignedProperties.put(0x0FF11737, (int) 0);
		unassignedProperties.put(0x0FF11738, (int) 0);
	}

	private WinTextEdit() {
		super();
	}
	
	@Override
	public WinTextEdit copyComponent(boolean propagateIndependent) {
		WinTextEdit other = new WinTextEdit();
		copyComponent(other, propagateIndependent);
		
		System.arraycopy(colors, 0, other.colors, 0, colors.length);
		System.arraycopy(margins, 0, other.margins, 0, margins.length);
		
		other.blinkFrequency = blinkFrequency;
		textOutline.copyComponent(other.textOutline);
		
		return other;
	}

	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x0FF11731: return colors;
				case 0x0FF11732: return margins;
				case 0x039A69E6: return textOutline;
				case 0x0FF11733: return blinkFrequency;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				case 0x0FF11731: colors[index] = (Color) value; break;
				case 0x0FF11732: System.arraycopy((float[]) value, 0, margins, 0, margins.length); break;
				case 0x0FF11733: blinkFrequency = (float) value; break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
	
	@Override
	public void paintBasic(Graphics2D graphics, Rectangle drawBounds) {
		super.paintBasic(graphics, drawBounds);
		
		if (!shouldSkipPaint() && caption != null) {
			Color fontColor = colors[COLOR_IDLE_TEXT];
			Color backgroundColor = colors[COLOR_IDLE_BACKGROUND];
			
			graphics.setColor(backgroundColor);
			graphics.fill(drawBounds);
			
			StyleSheetInstance.paintText(graphics, style, viewer.getString(caption), fontColor, drawBounds, margins);
		}
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}

