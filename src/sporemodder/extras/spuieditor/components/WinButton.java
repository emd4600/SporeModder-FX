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
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec2;
import sporemodder.utilities.Hasher;

public class WinButton extends Window implements ActionableComponent {
	
	public static final int FLAG_SHOWTEXT = 0x8;
	
	public final static int TEXT_IDLE = 0;
	public final static int TEXT_DISABLED = 1;
	public final static int TEXT_HOVER = 3;
	public final static int TEXT_CLICKED = 2;
	public final static int TEXT_SELECTED = 4;
	public final static int TEXT_SELECTED_DISABLED = 7;
	public final static int TEXT_SELECTED_HOVER = 5;
	public final static int TEXT_SELECTED_CLICK = 6;
	
	public final static int BUTTONTYPE_SELECTABLE = 0x2;
	
	public static final int TYPE = 0xEEEFE2C3;
	
	private final Color[] captionColors = new Color[8];
	private final float[] captionBorder = new float[4]; 
	private final float[] captionOffset = new float[2];
	private final OutlineFormat textOutline = new OutlineFormat();
	private String buttonGroup;
	protected int buttonType;
	
	protected int state;

	public WinButton(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		addUnassignedInt(block, 0xEEC1D001, 0);
		addUnassignedInt(block, 0xEEC1D002, 0);
		buttonType = getIntProperty(block, 0xEEC1D003, 1);
		addUnassignedInt(block, 0xEEC1D004, 1);
		buttonGroup = Hasher.getFileName(getIntProperty(block, 0xEEC1D005, 0));
		
		//buttonGroup = getIntProperty(block, 0xEEC1D005, 0);
		
		float[] captionOffset = SectionVec2.getValues(block.getSection(0xEEC1D00A, SectionVec2.class), new float[][] {null}, 1)[0];
		if (captionOffset != null) {
			this.captionOffset[0] = captionOffset[0];
			this.captionOffset[1] = captionOffset[1];
		}
		
		int[] textColorCodes = SectionInt.getValues(block, 0xEEC1D00B, null, 8);
		if (textColorCodes != null) {
			for (int i = 0; i < textColorCodes.length; i++) {
				captionColors[i] = PanelUtils.decodeColor(textColorCodes[i]);
			}
		}
		
		parseMarginsSections(block, 0xEEC1D006, captionBorder);
		
		ListSectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		
		if (property_039A69E6 != null) {
			textOutline.parse(property_039A69E6[0]);
		}
	}
	
	public WinButton(SPUIViewer viewer) {
		super(viewer);
		//buttonGroup = "";
		for (int i = 0; i < captionColors.length; i++) {
			captionColors[i] = Color.BLACK;
		}
		buttonType = 1;
		flags |= FLAG_SHOWTEXT;
		
		unassignedProperties.put(0xEEC1D004, 1);
		unassignedProperties.put(0xEEC1D001, 0);
		unassignedProperties.put(0xEEC1D002, 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		saveInt(builder, block, 0xEEC1D001);
		saveInt(builder, block, 0xEEC1D002);
		builder.addInt(block, 0xEEC1D003, new int[] {buttonType});
		saveInt(builder, block, 0xEEC1D004);
		builder.addInt(block, 0xEEC1D005, new int[] {Hasher.getFileHash(buttonGroup)});
		builder.addVec2(block, 0xEEC1D00A, new float[][] {captionOffset});
		
		builder.addSectionList(block, 0xEEC1D006, new ListSectionContainer[] {saveMarginsSections(captionBorder)}, 50);
		
		int[] colorValues = new int[captionColors.length];
		for (int i = 0; i < colorValues.length; i++) {
			colorValues[i] = PanelUtils.encodeColor(captionColors[i]);
		}
		builder.addInt(block, 0xEEC1D00B, colorValues);
		
		builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {textOutline.saveComponent(builder)}, 122);
		
		return block;
	}
	
	protected WinButton() {
		super();
	}
	
	protected void copyComponent(WinButton other, boolean propagateIndependent) {
		super.copyComponent(other, propagateIndependent);
		
		other.buttonType = buttonType;
		other.buttonGroup = buttonGroup;
		
		for (int i = 0; i < captionColors.length; i++) {
			other.captionColors[i] = captionColors[i];
		}
		for (int i = 0; i < captionBorder.length; i++) {
			other.captionBorder[i] = captionBorder[i];
		}
		for (int i = 0; i < captionOffset.length; i++) {
			other.captionOffset[i] = captionOffset[i];
		}
		textOutline.copyComponent(other.textOutline);
	}

	@Override
	public WinButton copyComponent(boolean propagateIndependent) {
		WinButton other = new WinButton();
		
		copyComponent(other, propagateIndependent);
		
		return other;
	}
	
	public boolean isSelectable() {
		return (buttonType & BUTTONTYPE_SELECTABLE) == BUTTONTYPE_SELECTABLE;
	}
	
	public int getButtonGroup() {
		return Hasher.getFileHash(buttonGroup);
	}
	
	@Override
	public int getState() {
		return state;
	}

	@Override
	public void setState(int state) {
		int oldState = this.state;
		this.state = state;
		if (state != oldState && viewer != null && viewer.isPreview()) {
			viewer.repaint();
		}
	}
	
	protected class ButtonDesignerDelegate extends WindowDesignerDelegate {
		public ButtonDesignerDelegate(SPUIViewer viewer) {
			super(viewer);
		}

		@Override
		public Object getValue(DesignerProperty property) {
			switch (property.getProxyID()) {
			
			case 0xEEC1D003: return buttonType;
			case 0xEEC1D005: return buttonGroup;
			case 0xEEC1D00B: return captionColors;
			case 0xEEC1D00A: return captionOffset;
			case 0xEEC1D006: return captionBorder;
			case 0x039A69E6: return textOutline;
			}
			
			return super.getValue(property);
		}
		
		@Override
		public void setValue(DesignerProperty property, Object value, int index) {
			switch (property.getProxyID()) {
			
			case 0xEEC1D003: buttonType = (int) value; break;
			case 0xEEC1D005: buttonGroup = (String) value; break;
			case 0xEEC1D00B: 
				captionColors[index] = (Color) value;
				viewer.repaint();
				break;
			case 0xEEC1D006: 
				System.arraycopy((float[]) value, 0, captionBorder, 0, captionBorder.length);
				viewer.repaint();
				break;
			case 0xEEC1D00A: 
				System.arraycopy((float[]) value, 0, captionOffset, 0, captionOffset.length);
				viewer.repaint();
				break;
			}
			
			super.setValue(property, value, index);
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new ButtonDesignerDelegate(viewer);
	}
	
	@Override
	public void paintBasic(Graphics2D graphics, Rectangle drawBounds) {
		super.paintBasic(graphics, drawBounds);
		
		if (!shouldSkipPaint() && (flags & FLAG_SHOWTEXT) == FLAG_SHOWTEXT && caption != null) {
			Color fontColor = null;
			Rectangle textBounds = new Rectangle(drawBounds);
			
			textBounds.x += captionOffset[X];
			textBounds.y += captionOffset[Y];
			
//			if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED || 
//					(state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK || 
//					(state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
//				textBounds.x += captionOffset[X];
//				textBounds.y += captionOffset[Y];
//			}
			
			if (viewer.isPreview()) {
				if ((flags & WinComponent.FLAG_ENABLED) != WinComponent.FLAG_ENABLED && captionColors[TEXT_DISABLED] != null) {
					fontColor = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
							captionColors[TEXT_SELECTED_DISABLED] : 
								captionColors[TEXT_DISABLED];
				}
				else if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK && captionColors[TEXT_CLICKED] != null) {
					fontColor = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
							captionColors[TEXT_SELECTED_CLICK] : 
								captionColors[TEXT_CLICKED];
				}
				else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER && captionColors[TEXT_HOVER] != null) {
					fontColor = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
							captionColors[TEXT_SELECTED_HOVER] : 
								captionColors[TEXT_HOVER];
				}
				else {
					fontColor = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
							captionColors[TEXT_SELECTED] : 
								captionColors[TEXT_IDLE];
				}
			} else {
				fontColor = captionColors[TEXT_IDLE];
			}
			
			StyleSheetInstance.paintText(graphics, style, viewer.getString(caption), fontColor, textBounds, captionBorder);
		}
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
