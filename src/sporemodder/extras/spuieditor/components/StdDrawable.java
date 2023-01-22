package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.ComponentChooser.ComponentChooserCallback;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUIHitMaskResource;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec2;
import sporemodder.userinterface.JLabelLink;

public class StdDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	private static final int MODE_STRETCH = 1;
	private static final int MODE_SLICE_STRETCH = 2;
	private static final int MODE_TILE = 3;
	private static final int MODE_SLICE_TILE = 4;
	
	public static final int TYPE = 0xB03C196F;
	
	protected final Image[] images = new Image[8];
	private final float[] sliceProportions = new float[4];
	
	private final float[] scales = new float[2];
	private int tilingMode;
	private final OutlineFormat imageOutline = new OutlineFormat();
	private HitMask hitMask;
	
	public StdDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short[] imageIndices = SectionShort.getValues(block.getSection(0x0F3AC75E, SectionShort.class), new short[] {-1, -1, -1, -1, -1, -1, -1, -1}, 8);
		
		Image.loadImages(block, imageIndices, images);
		
		tilingMode = SectionInt.getValues(block, 0xEF3C000A, new int[] { 1 }, 1)[0];
		
		float[] scales = SectionVec2.getValues(block.getSection(0xEF3C000B, SectionVec2.class), new float[][] {new float[] {1, 1}}, 1)[0];
		this.scales[X] = scales[X];
		this.scales[Y] = scales[Y];
		
		parseMarginsSections(block, 0xEF3C000C, sliceProportions);
		
		ListSectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		
		if (property_039A69E6 != null) {
			imageOutline.parse(property_039A69E6[0]);
		}
		
		short hitMaskIndex = SectionShort.getValues(block.getSection(0x0F3C000D, SectionShort.class), new short[] {-1}, 1)[0];
		if (hitMaskIndex != -1) {
			hitMask = new HitMask((SPUIHitMaskResource) block.getParent().get(hitMaskIndex));
		}
		
		addUnassignedFloat(block, 0x0F3C000E);
	}
	
	public StdDrawable(SPUIViewer viewer) {
		super(viewer);
		
		tilingMode = MODE_STRETCH;
		sliceProportions[0] = sliceProportions[1] = sliceProportions[2] = sliceProportions[3] = 0.333f;
		scales[0] = scales[1] = 1.0f;
	}
	
	protected StdDrawable() {
		super();
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		SPUIObject[] imageObjects = new SPUIObject[images.length];
		for (int i = 0; i < imageObjects.length; i++) {
			imageObjects[i] = images[i] == null ? null : builder.addComponent(images[i]);
		}
		builder.addReference(block, 0x0F3AC75E, imageObjects);
		builder.addReference(block, 0x0F3C000D, new SPUIObject[] {hitMask == null ? null : builder.addComponent(hitMask)});
		if (unassignedProperties.containsKey(0x0F3C000E)) {
			saveFloat(builder, block, 0x0F3C000E);
		}
		builder.addInt(block, 0xEF3C000A, new int[] {tilingMode});
		builder.addVec2(block, 0xEF3C000B, new float[][] {scales});
		
		builder.addSectionList(block, 0xEF3C000C, new ListSectionContainer[] {saveMarginsSections(sliceProportions)}, 50);
		
		builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {imageOutline.saveComponent(builder)}, 122);
		
		Image.addImagePath(builder, block, images);
		
		return block;
	}
	
	protected void copyComponent(StdDrawable other, boolean propagateIndependent) {
		super.copyComponent(other, propagateIndependent);
		
		for (int i = 0; i < images.length; i++) {
			other.images[i] = images[i] == null ? null : (propagateIndependent ? images[i].copyComponent(propagateIndependent) : images[i]);
		}
		
		System.arraycopy(sliceProportions, 0, other.sliceProportions, 0, sliceProportions.length);
		other.scales[0] = scales[0];
		other.scales[1] = scales[1];
		other.tilingMode = tilingMode;
		imageOutline.copyComponent(other.imageOutline);
		if (hitMask != null) {
			other.hitMask = hitMask;
		}
	}
	
	@Override
	public StdDrawable copyComponent(boolean propagateIndependent) {
		StdDrawable other = new StdDrawable();
		copyComponent(other, propagateIndependent);
		return other;
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			for (Image image : images) {
				if (image == component) {
					return true;
				}
			}
			if (hitMask == component) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		for (Image image : images) {
			if (image != null) {
				image.getComponents(resultList, filter);
			}
		}
		if (hitMask != null) {
			hitMask.getComponents(resultList, filter);
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.startsWith("IMAGE ")) {
				images[Integer.parseInt(value.split(" ", 2)[1])] = (Image) removeAction.getRemovedComponent();
			}
			else if (value.equals("HITMASK")) {
				hitMask = (HitMask) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		for (int i = 0; i < images.length; i++) {
			if (propagate && images[i] != null) {
				images[i].removeComponent(removeAction, propagate);
			}
			if (images[i] == removedComp) {
				modifiedValues.add("IMAGE " + i);
				images[i] = null;
			}
		}
		
		if (propagate && hitMask != null) {
			hitMask.removeComponent(removeAction, propagate);
		}
		if (hitMask == removedComp) {
			modifiedValues.add("HITMASK");
			hitMask = null;
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		if (hitMask != null) {
			hitMask.setSPUIViewer(viewer);
		}
	}
	
	@Override
	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index) {
		super.fillHierarchyTree(model, parent, index);
		
		if (hitMask != null) {
			hitMask.fillHierarchyTree(model, node, 0);
		}
		
		return node;
	}
	
	protected void paintImage(Graphics2D graphics, Image image, Rectangle bounds) {
		Dimension dim = image.getDimensions();
		int[] uvCoordinates = image.getImageUVCoords();
		
		if (tilingMode == MODE_STRETCH) {
			Image.drawImage(graphics, image.getBufferedImage(), bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, 
					uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3]);
		}
		else if (tilingMode == MODE_SLICE_STRETCH) {
			Image.drawSlicedImage(graphics, bounds, image, uvCoordinates, sliceProportions, scales, false);
			
		}
		else if (tilingMode == MODE_TILE) {
			Image.drawTiled((Graphics2D) graphics.create(bounds.x, bounds.y, bounds.width, bounds.height), 
					image.getBufferedImage(), dim, bounds, new Point(0, 0), new Point(dim.width, dim.height), uvCoordinates);
		}
		else if (tilingMode == MODE_SLICE_TILE) {
			Image.drawSlicedImage(graphics, bounds, image, uvCoordinates, sliceProportions, scales, true);
		}
	}
	
	protected void paintImage(Graphics2D graphics, Image image, Rectangle bounds, Color tint) {
		if (tint == null || tint.equals(Color.white)) {
			paintImage(graphics, image, bounds);
		}
		else {
			BufferedImage graphicsBuffer = new BufferedImage(Math.abs(bounds.width), Math.abs(bounds.height), BufferedImage.TYPE_INT_ARGB);
			
			paintImage(graphicsBuffer.createGraphics(), image, new Rectangle(0, 0, bounds.width, bounds.height));
			
			float[] scaleFactors = new float[4];
			float[] offsets = new float[4];
			
			scaleFactors[0] = tint.getRed() / 255f;
			scaleFactors[1] = tint.getGreen() / 255f;
			scaleFactors[2] = tint.getBlue() / 255f;
			scaleFactors[3] = tint.getAlpha() / 255f;
			
			// we must use another image, otherwise the tint isn't applied correctly (check the Sporepedia button transparency in the editor)
			BufferedImage img = new BufferedImage(Math.abs(bounds.width), Math.abs(bounds.height), BufferedImage.TYPE_INT_ARGB);
			img.createGraphics().drawImage(graphicsBuffer, new RescaleOp(scaleFactors, offsets, null), 0, 0);
			
			graphics.drawImage(img, bounds.x, bounds.y, null);
		}
	}
	

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		if (bounds.width <= 0 || bounds.height <= 0) {
			return;
		}
		
		Image image = null;
		
		if (viewer.isPreview()) {
			int state = component.isActionableComponent() ? ((ActionableComponent) component).getState() : 0;
			
			if ((component.getFlags() & WinComponent.FLAG_ENABLED) != WinComponent.FLAG_ENABLED && images[INDEX_DISABLED] != null) {
				image = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						images[INDEX_SELECTED_DISABLED] : 
							images[INDEX_DISABLED];
			}
			else if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK && images[INDEX_CLICKED] != null) {
				image = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						images[INDEX_SELECTED_CLICK] : 
							images[INDEX_CLICKED];
			}
			else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER && images[INDEX_HOVER] != null) {
				image = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						images[INDEX_SELECTED_HOVER] : 
							images[INDEX_HOVER];
			}
			else {
				image = (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED ? 
						images[INDEX_SELECTED] : 
							images[INDEX_IDLE];
			}
		} else {
			image = images[INDEX_IDLE];
		}
		
		if (!Image.isValid(image)) {
			return;
		}
		
		paintImage(graphics, image, bounds, component.getTintColor());
	}
	
	protected class StdDrawableDesignerDelegate extends DefaultDesignerDelegate {
		public StdDrawableDesignerDelegate(SPUIViewer viewer) {
			super(viewer);
		}

		@Override
		public Object getValue(DesignerProperty property) {
			switch (property.getProxyID()) {
			
			case 0xEF3C000A: return tilingMode;
			case 0xEF3C000B: return scales;
			case 0xEF3C000C: return sliceProportions;
			case 0x039A69E6: return imageOutline;
			case 0x0F3C000D: return hitMask;
			case 0x0F3AC75E: return images;
			}
			
			return super.getValue(property);
		}
		
		@Override
		public void setValue(DesignerProperty property, Object value, final int index) {
			switch (property.getProxyID()) {
			
			case 0xEF3C000A: tilingMode = (int) value; break;
			case 0xEF3C000B: System.arraycopy((float[]) value, 0, scales, 0, scales.length); break;
			case 0xEF3C000C: System.arraycopy((float[]) value, 0, sliceProportions, 0, sliceProportions.length); break;
			
			case 0x0F3AC75E: 
				ComponentChooser.showChooserAction(new ComponentChooserCallback<Image>() {

					@Override
					public Image getValue() {
						return images[index];
					}

					@Override
					public void valueChanged(Image value) {
						images[index] = value;
						viewer.repaint();
					}
					
				}, Image.getImageChooser(viewer), (JLabelLink) value, viewer);
				break;
				
			case 0x0F3C000D: 
//				ComponentChooser.showChooserAction(StdDrawable.this, "hitMask", 
//						HitMask.getComponentChooser(viewer), 
//						(JLabelLink) value, viewer, false);
				
				ComponentChooser.showChooserAction(new ComponentChooserCallback<HitMask>() {
					@Override
					public HitMask getValue() {
						return hitMask;
					}

					@Override
					public void valueChanged(HitMask value) {
						hitMask = value;
					}
				}, HitMask.getComponentChooser(viewer), (JLabelLink) value, viewer);
				break;
			}
			
			viewer.repaint();
			
			super.setValue(property, value, index);
		}
	}

	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new StdDrawableDesignerDelegate(viewer);
	}

	@Override
	public Dimension getDimensions(int imageIndex) {
		if (imageIndex == IMAGE_MAIN) {
			imageIndex = INDEX_IDLE;
		}
		return images[imageIndex] == null ? null : new Dimension(images[imageIndex].getDimensions());
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		if (hitMask != null && viewer.isPreview()) {
			return hitMask.isValidPoint(p, bounds);
		}
		else {
			return true;
		}
	}
}
