package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec2;
import sporemodder.userinterface.JLabelLink;

public class cSPUIStdDrawableImageInfo extends SPUIDefaultComponent {
	
	public static final int TYPE = 0x0540037E;
	
	protected Image backgroundImage;
	protected Image glyphImage;
	protected Color backgroundColor;
	protected Color glyphColor;
	protected final float[] glyphScale = new float[] {1.0f, 1.0f};
	protected final float[] glyphOffset = new float[] {0, 0};
	protected final float[] paintScale = new float[] {1.0f, 1.0f};
	protected final float[] paintOffset = new float[] {0, 0};
	protected final OutlineFormat glyphShadow = new OutlineFormat();
	protected final OutlineFormat backgroundShadow = new OutlineFormat();

	public cSPUIStdDrawableImageInfo(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);

		short bgImageIndex = SectionShort.getValues(block.getSection(0x00000001, SectionShort.class), new short[] {-1}, 1)[0];
//		if (bgImageIndex != -1) {
//			SPUIObject object = block.getParent().get(bgImageIndex);
//			Image.loadImagePath(block, object);
//			backgroundImage = (Image) ResourceLoader.getComponent(object);
//		}
		
		short gImageIndex = SectionShort.getValues(block.getSection(0x00000002, SectionShort.class), new short[] {-1}, 1)[0];
//		if (gImageIndex != -1) {
//			SPUIObject object = block.getParent().get(gImageIndex);
//			Image.loadImagePath(block, object);
//			glyphImage = (Image) ResourceLoader.getComponent(object);
//		}
		
		Image[] images = new Image[2];
		Image.loadImages(block, new short[] {bgImageIndex, gImageIndex}, images);
		
		backgroundImage = images[0];
		glyphImage = images[1];
		
		backgroundColor = Window.decodeColor(SectionInt.getValues(block, 0x00000003, new int[] { 0 }, 1)[0]);
		glyphColor = Window.decodeColor(SectionInt.getValues(block, 0x00000004, new int[] { 0 }, 1)[0]);
		
		float[] values = SectionVec2.getValues(block.getSection(0x00000006, SectionVec2.class), null, 1)[0];
		if (values != null) {
			glyphScale[0] = values[0];
			glyphScale[1] = values[1];
		}
		values = SectionVec2.getValues(block.getSection(0x00000007, SectionVec2.class), null, 1)[0];
		if (values != null) {
			glyphOffset[0] = values[0];
			glyphOffset[1] = values[1];
		}
		values = SectionVec2.getValues(block.getSection(0x0000000C, SectionVec2.class), null, 1)[0];
		if (values != null) {
			paintScale[0] = values[0];
			paintScale[1] = values[1];
		}
		values = SectionVec2.getValues(block.getSection(0x0000000D, SectionVec2.class), null, 1)[0];
		if (values != null) {
			paintOffset[0] = values[0];
			paintOffset[1] = values[1];
		}
		
		ListSectionContainer[] property_shadow = SectionSectionList.getValues(block.getSection(0x0000000A, SectionSectionList.class), null, 1, -1);
		
		if (property_shadow != null) {
			glyphShadow.parse(property_shadow[0]);
		}
		
		property_shadow = SectionSectionList.getValues(block.getSection(0x0000000B, SectionSectionList.class), null, 1, -1);
		
		if (property_shadow != null) {
			backgroundShadow.parse(property_shadow[0]);
		}
		
		addUnassignedInt(block, 0x00000005, 0);
		addUnassignedInt(block, 0x00000008, 3);
		addUnassignedInt(block, 0x00000009, 0);
		
	}
	
	public cSPUIStdDrawableImageInfo(SPUIViewer viewer) {
		super(viewer);
		
		backgroundColor = Color.WHITE;
		glyphColor = Color.WHITE;
		
		unassignedProperties.put(0x00000005, (int) 0);
		unassignedProperties.put(0x00000008, (int) 3);
		unassignedProperties.put(0x00000009, (int) 0);
	}
	
	private cSPUIStdDrawableImageInfo() {
		super();
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			if (backgroundImage == component || glyphImage == component) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		if (backgroundImage != null) {
			backgroundImage.getComponents(resultList, filter);
		}
		if (glyphImage != null) {
			glyphImage.getComponents(resultList, filter);
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.equals("backgroundImage")) {
				backgroundImage = (Image) removeAction.getRemovedComponent();
			}
			else if (value.equals("glyphImage")) {
				glyphImage = (Image) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		if (propagate && backgroundImage != null) {
			backgroundImage.removeComponent(removeAction, propagate);
		}
		if (backgroundImage == removedComp) {
			modifiedValues.add("backgroundImage");
			backgroundImage = null;
		}
		
		if (propagate && glyphImage != null) {
			glyphImage.removeComponent(removeAction, propagate);
		}
		if (glyphImage == removedComp) {
			modifiedValues.add("glyphImage");
			glyphImage = null;
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public cSPUIStdDrawableImageInfo copyComponent(boolean propagateIndependent) {
		cSPUIStdDrawableImageInfo other = new cSPUIStdDrawableImageInfo();
		
		copyComponent(other, propagateIndependent);
		
		other.backgroundImage = backgroundImage == null ? null : (propagateIndependent ? backgroundImage.copyComponent(propagateIndependent) : backgroundImage);
		other.glyphImage = glyphImage == null ? null : (propagateIndependent ? glyphImage.copyComponent(propagateIndependent) : glyphImage);
		other.glyphColor = glyphColor;
		other.backgroundColor = backgroundColor;
		other.glyphScale[0] = glyphScale[0];
		other.glyphScale[1] = glyphScale[1];
		other.glyphOffset[0] = glyphOffset[0];
		other.glyphOffset[1] = glyphOffset[1];
		other.paintScale[0] = paintScale[0];
		other.paintScale[1] = paintScale[1];
		other.paintOffset[0] = paintOffset[0];
		other.paintOffset[1] = paintOffset[1];
		glyphShadow.copyComponent(other.glyphShadow);
		backgroundShadow.copyComponent(other.backgroundShadow);
		
		return other;
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder)	{
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addReference(block, 0x00000001, new SPUIObject[] {
				backgroundImage == null ? null : builder.addComponent(backgroundImage)});
		
		builder.addReference(block, 0x00000002, new SPUIObject[] {
				glyphImage == null ? null : builder.addComponent(glyphImage)});
		
		builder.addInt(block, 0x00000003, new int[] {
				backgroundColor == null ? -1 : PanelUtils.encodeColor(backgroundColor)});
		
		builder.addInt(block, 0x00000004, new int[] {
				glyphColor == null ? -1 : PanelUtils.encodeColor(glyphColor)});
		
		saveInt(builder, block, 0x00000005);
		
		builder.addVec2(block, 0x00000006, new float[][] {glyphScale});
		builder.addVec2(block, 0x00000007, new float[][] {glyphOffset});
		builder.addVec2(block, 0x0000000C, new float[][] {paintScale});
		builder.addVec2(block, 0x0000000D, new float[][] {paintOffset});
		
		builder.addSectionList(block, 0x0000000A, new ListSectionContainer[] {glyphShadow.saveComponent(builder)}, 122);
		
		saveInt(builder, block, 0x00000008);
		
		builder.addSectionList(block, 0x0000000B, new ListSectionContainer[] {backgroundShadow.saveComponent(builder)}, 122);
		
		saveInt(builder, block, 0x00000009);
		
		Image.addImagePath(builder, block, new Image[] {backgroundImage, glyphImage});
		
		return block;
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x00000001: return backgroundImage;
				case 0x00000002: return glyphImage;
				case 0x00000003: return backgroundColor;
				case 0x00000004: return glyphColor;
				case 0x00000006: return glyphScale;
				case 0x00000007: return glyphOffset;
				case 0x0000000C: return paintScale;
				case 0x0000000D: return paintOffset;
				case 0x0000000A: return glyphShadow;
				case 0x0000000B: return backgroundShadow;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x00000001: 
					ComponentChooser.showChooserAction(cSPUIStdDrawableImageInfo.this, "backgroundImage", 
							Image.getImageChooser(viewer), 
							(JLabelLink) value, viewer, false);
					break;
					
				case 0x00000002: 
					ComponentChooser.showChooserAction(cSPUIStdDrawableImageInfo.this, "glyphImage", 
							Image.getImageChooser(viewer), 
							(JLabelLink) value, viewer, false);
					break;
					
				case 0x00000003: backgroundColor = (Color) value; break;
				case 0x00000004: glyphColor = (Color) value; break;
				case 0x00000006: System.arraycopy((float[]) value, 0, glyphScale, 0, glyphScale.length); break;
				case 0x00000007: System.arraycopy((float[]) value, 0, glyphOffset, 0, glyphOffset.length); break;
				case 0x0000000C: System.arraycopy((float[]) value, 0, paintScale, 0, paintScale.length); break;
				case 0x0000000D: System.arraycopy((float[]) value, 0, paintOffset, 0, paintOffset.length); break;
					
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}

}
