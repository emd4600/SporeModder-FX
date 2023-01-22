package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class cSPUIStdDrawable extends StdDrawable {
	
	public static final int TYPE = 0x053EB32A;
	
	private final cSPUIStdDrawableImageInfo[] imageInfos = new cSPUIStdDrawableImageInfo[8];

	public cSPUIStdDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		for (int i = 0; i < imageInfos.length; i++) {
			short index = SectionShort.getValues(block.getSection(0x054010A0 + i, SectionShort.class), new short[] { -1 }, 1)[0];
			if (index != -1) {
				imageInfos[i] = (cSPUIStdDrawableImageInfo) ResourceLoader.getComponent(block.getParent().get(index));
			}
		}
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = super.saveComponent(builder);
		
		for (int i = 0; i < imageInfos.length; i++) {
			builder.addReference(block, 0x054010A0 + i, new SPUIObject[] {imageInfos[i] == null ? null : builder.addComponent(imageInfos[i])});
		}
		
		return block;
	}
	
	public cSPUIStdDrawable(SPUIViewer viewer) {
		super(viewer);
	}
	
	private cSPUIStdDrawable() {
		super();
	}
	
	@Override
	public cSPUIStdDrawable copyComponent(boolean propagateIndependent) {
		cSPUIStdDrawable other = new cSPUIStdDrawable();
		super.copyComponent(other, propagateIndependent);
		
		for (int i = 0; i < 8; i++) {
			// image infos are not independent; they must always be copied
			other.imageInfos[i] = imageInfos[i] == null ? null : imageInfos[i].copyComponent(propagateIndependent);
		}
		
		return other;
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			for (cSPUIStdDrawableImageInfo imageInfo : imageInfos) {
				if (imageInfo == component) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		for (cSPUIStdDrawableImageInfo imageInfo : imageInfos) {
			if (imageInfo != null) {
				imageInfo.getComponents(resultList, filter);
			}
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.startsWith("IMAGEINFO ")) {
				imageInfos[Integer.parseInt(value.split(" ", 2)[1])] = (cSPUIStdDrawableImageInfo) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		for (int i = 0; i < imageInfos.length; i++) {
			if (propagate && imageInfos[i] != null) {
				imageInfos[i].removeComponent(removeAction, propagate);
			}
			if (imageInfos[i] == removedComp) {
				modifiedValues.add("IMAGEINFO " + i);
				imageInfos[i] = null;
			}
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index) {
		super.fillHierarchyTree(model, parent, index);
		
		int ind = 0;
		for (int i = 0; i < imageInfos.length; i++) {
			if (imageInfos[i] != null) {
				imageInfos[i].fillHierarchyTree(model, node, ind++);
			}
		}
		
		return node;
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		for (cSPUIStdDrawableImageInfo info : imageInfos) {
			if (info != null) {
				info.setSPUIViewer(viewer);
			}
		}
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		super.draw(graphics, bounds, component);
		
		cSPUIStdDrawableImageInfo imageInfo = null;
		
		if (viewer.isPreview()) {
			int state = component.isActionableComponent() ? ((ActionableComponent) component).getState() : 0;
			
			if ((component.getFlags() & WinComponent.FLAG_ENABLED) != WinComponent.FLAG_ENABLED) {
				if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					
					if (imageInfos[INDEX_SELECTED_DISABLED] != null) imageInfo = imageInfos[INDEX_SELECTED_DISABLED];
					else if (imageInfos[INDEX_DISABLED] != null) imageInfo = imageInfos[INDEX_DISABLED];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
				else {
					
					if (imageInfos[INDEX_DISABLED] != null) imageInfo = imageInfos[INDEX_DISABLED];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
			}
			else if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK) {
				
				if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					
					if (imageInfos[INDEX_SELECTED_CLICK] != null) imageInfo = imageInfos[INDEX_SELECTED_CLICK];
					else if (imageInfos[INDEX_CLICKED] != null) imageInfo = imageInfos[INDEX_CLICKED];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
				else {
					
					if (imageInfos[INDEX_CLICKED] != null) imageInfo = imageInfos[INDEX_CLICKED];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
			}
			else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
				if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					if (imageInfos[INDEX_SELECTED_HOVER] != null) imageInfo = imageInfos[INDEX_SELECTED_HOVER];
					else if (imageInfos[INDEX_HOVER] != null) imageInfo = imageInfos[INDEX_HOVER];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
				else {
					
					if (imageInfos[INDEX_HOVER] != null) imageInfo = imageInfos[INDEX_HOVER];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
			}
			else {
				if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					
					if (imageInfos[INDEX_SELECTED] != null) imageInfo = imageInfos[INDEX_SELECTED];
					else imageInfo = imageInfos[INDEX_IDLE];
				}
				else {
					imageInfo = imageInfos[INDEX_IDLE];
				}
			}
		} else {
			imageInfo = imageInfos[INDEX_IDLE];
		}
		
		if (imageInfo != null) {
			
			Image glyphImage = imageInfo.glyphImage;
			
			if (glyphImage == null) {
				if (imageInfos[INDEX_IDLE] != null) {
					glyphImage = imageInfos[INDEX_IDLE].glyphImage;
				}
			}
			
			bounds = new Rectangle(
					Math.round(bounds.x + imageInfo.paintOffset[0]),
					Math.round(bounds.y + imageInfo.paintOffset[1]),
					Math.round(bounds.width * imageInfo.paintScale[0]),
					Math.round(bounds.height * imageInfo.paintScale[1]));
			
			if (Image.isValid(imageInfo.backgroundImage)) {
				super.paintImage(graphics, imageInfo.backgroundImage, bounds, imageInfo.backgroundColor);
			}
			if (Image.isValid(glyphImage)) {
				Dimension dim = glyphImage.getDimensions();
				int[] uvCoordinates = glyphImage.getImageUVCoords();
				
				BufferedImage temp = Image.drawTintedImage(glyphImage.getBufferedImage(), dim, uvCoordinates, imageInfo.glyphColor);
				
				
				Dimension realDim = new Dimension(
						(int) Math.round(dim.width * imageInfo.glyphScale[0]),
						(int) Math.round(dim.height * imageInfo.glyphScale[1]));
				
				float h = (bounds.width - realDim.width) * 0.5f;
				float v = (bounds.height - realDim.height) * 0.5f;
				int x = (int) Math.round(bounds.x + h + imageInfo.glyphOffset[0]);
				int y = (int) Math.round(bounds.y + v + imageInfo.glyphOffset[1]);
				
				graphics.drawImage(temp, 
						x, 
						y,
						realDim.width,
						realDim.height,
						null);
				
			}
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new StdDrawableDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				for (int i = 0; i < imageInfos.length; i++) {
					if (property.getProxyID() == 0x054010A0 + i) {
						return imageInfos[i];
					}
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				for (int i = 0; i < imageInfos.length; i++) {
					if (property.getProxyID() == 0x054010A0 + i) {
						ComponentChooser.showChooserAction(cSPUIStdDrawable.this, "imageInfos", i, 
								ComponentFactory.getComponentChooser(property.getType(), viewer), 
								(JLabelLink) value, viewer, false);
					}
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
}
