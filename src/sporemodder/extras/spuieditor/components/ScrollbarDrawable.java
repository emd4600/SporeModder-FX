package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.WinScrollbar.ScrollbarButton;
import sporemodder.extras.spuieditor.components.WinScrollbar.ThumbWinButton;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class ScrollbarDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final String INTERFACE_NAME = "IScrollbarDrawable";
	
	public static final int TYPE = 0x0F034637;
	
	public static final int IMAGE_BACKGROUND = 0;
	public static final int IMAGE_TRACK = 4;
	public static final int IMAGE_INCREMENT = 1;
	public static final int IMAGE_DECREMENT = 6;
	public static final int IMAGE_THUMB = 3;
	
	private final Image[] images = new Image[7];

	public ScrollbarDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short[] indices = SectionShort.getValues(block.getSection(0xAF42D91C, SectionShort.class), null, 7);
		if (indices != null) {
			Image.loadImages(block, indices, images);
		}
	}
	
	public ScrollbarDrawable(SPUIViewer viewer) {
		super(viewer);
	}

	ScrollbarDrawable() {
		super();
	}

	@Override
	public ScrollbarDrawable copyComponent(boolean propagateIndependent) {
		ScrollbarDrawable other = new ScrollbarDrawable();
		copyComponent(other, propagateIndependent);
		for (int i = 0; i < images.length; i++) {
			other.images[i] = images[i];
		}
		return other;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		SPUIObject[] objects = new SPUIObject[images.length];
		for (int i = 0; i < images.length; i++) {
			objects[i] = images[i] == null ? null : builder.addComponent(images[i]);
		}
				
		builder.addReference(block, 0xAF42D91C, objects);
		
		Image.addImagePath(builder, block, images);
		
		return block;
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
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.startsWith("IMAGES")) {
				images[Integer.parseInt(value.split(" ", 2)[1])] = (Image) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		if (images != null) {
			for (int i = 0; i < images.length; i++) {
				if (propagate && images[i] != null) {
					images[i].removeComponent(removeAction, propagate);
				}
				if (images[i] == removedComp) {
					modifiedValues.add("IMAGES " + i);
					images[i] = null;
				}
			}
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		if (!(component instanceof WinScrollbar)) {
			return;
		}
		
		WinScrollbar scrollbar = (WinScrollbar) component;
		
		if ((scrollbar.getFlags() & WinScrollbar.FLAG_ISEDITABLE) != WinScrollbar.FLAG_ISEDITABLE) {
			return;
		}
		
		ScrollbarButton incrementButton = scrollbar.getIncrementButton();
		ScrollbarButton decrementButton = scrollbar.getDecrementButton();
		Rectangle incrementBounds = incrementButton.getRealBounds();
		Rectangle decrementBounds = decrementButton.getRealBounds();
		
		if (Image.isValid(images[IMAGE_BACKGROUND])) {
			Dimension imageDim = images[IMAGE_BACKGROUND].getDimensions();
			
			float[] sliceProportions = null;
			
			if (scrollbar.getScrollbarType() == HORIZONTAL) {
				sliceProportions = new float[] {
						0.5f - (0.5f / imageDim.width),
						0.5f - (0.5f / imageDim.height),
						0.5f - (0.5f / imageDim.width),
						0.5f - (0.5f / imageDim.height),
				};
			}
			else {
				sliceProportions = new float[] {
						0.5f - (0.5f / imageDim.width),
						0.5f - (0.5f / imageDim.height),
						0.5f - (0.5f / imageDim.width),
						0.5f - (0.5f / imageDim.height),
				};
			}
			
			Image.drawSlicedImage(graphics, bounds, images[IMAGE_BACKGROUND], images[IMAGE_BACKGROUND].getImageUVCoords(), sliceProportions, 
					new float[] {1.0f, 1.0f}, false);
		}

		if (Image.isValid(images[IMAGE_TRACK])) {
			Dimension imageDim = images[IMAGE_TRACK].getDimensions();
			
			float[] sliceProportions = null;
			Rectangle trackBounds = new Rectangle();
			
			if (scrollbar.getScrollbarType() == HORIZONTAL) {
				sliceProportions = new float[] {
						0.5f - (0.5f / imageDim.width),
						0,
						0.5f - (0.5f / imageDim.width),
						0
				};
				
				trackBounds.x = bounds.x + incrementBounds.width;
				trackBounds.y = Math.round(bounds.y + (bounds.height - imageDim.height) / 2.0f);
				trackBounds.width = bounds.width - incrementBounds.width - decrementBounds.width;
				trackBounds.height = imageDim.height;
			}
			else {
				sliceProportions = new float[] {
						0,
						0.5f - (0.5f / imageDim.height),
						0,
						0.5f - (0.5f / imageDim.height),
				};
				trackBounds.x = Math.round(bounds.x + (bounds.width - imageDim.width) / 2.0f);
				trackBounds.y = bounds.y + incrementBounds.height;
				trackBounds.width = imageDim.width;
				trackBounds.height = bounds.height - incrementBounds.height - decrementBounds.height;
			}
			
			Image.drawSlicedImage(graphics, trackBounds, images[IMAGE_TRACK], images[IMAGE_TRACK].getImageUVCoords(), sliceProportions, 
					new float[] {1.0f, 1.0f}, false);
		} 
		else {
			graphics.setColor(new Color(0xFFECE9D8));
			graphics.fill(bounds);
		}
		
		if (Image.isValid(images[IMAGE_INCREMENT])) {
			Image.drawComponentTiledStates(graphics, incrementBounds, component.getFlags(), incrementButton.getState(),
					images[IMAGE_INCREMENT]);
		}
		else {
			ButtonDrawableStandard drawable = new ButtonDrawableStandard();
			drawable.draw(graphics, incrementBounds, null);
			//TODO
		}
		
		if (Image.isValid(images[IMAGE_DECREMENT])) {
			Image.drawComponentTiledStates(graphics, decrementBounds, component.getFlags(), decrementButton.getState(),
					images[IMAGE_DECREMENT]);
		}
		else {
			//TODO
			ButtonDrawableStandard drawable = new ButtonDrawableStandard();
			drawable.draw(graphics, decrementBounds, null);
		}
		
		// draw thumb
		{
			ThumbWinButton btnThumb = scrollbar.getThumbButton();
			Rectangle thumbBounds = btnThumb.getRealBounds();
			
			if (images[IMAGE_THUMB] != null) {
				Dimension imageDim = images[IMAGE_THUMB].getDimensions();
				
				float[] sliceProportions = null;
				
				if (scrollbar.getScrollbarType() == HORIZONTAL) {
					sliceProportions = new float[] {
							0.5f - (0.5f / imageDim.width),
							0,
							0.5f - (0.5f / imageDim.width),
							0
					};
				}
				else {
					sliceProportions = new float[] {
							0,
							0.5f - (0.5f / imageDim.height),
							0,
							0.5f - (0.5f / imageDim.height),
					};
				}
				
				Image.drawComponentTiledStates(graphics, thumbBounds, btnThumb, 
						images[IMAGE_THUMB], sliceProportions, viewer);
			}
			else {
				ButtonDrawableStandard drawable = new ButtonDrawableStandard();
				drawable.draw(graphics, thumbBounds, btnThumb);
			}
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				case 0xAF42D91C: return images;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xAF42D91C: 
					ComponentChooser.showChooserAction(ScrollbarDrawable.this, "images", index,
							Image.getImageChooser(viewer), 
							(JLabelLink) value, viewer, false);
					break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
	
	
	@Override
	public Dimension getDimensions(int imageIndex) {
		if (imageIndex == IMAGE_MAIN) {
			imageIndex = IMAGE_BACKGROUND;
			if (images[imageIndex] == null) {
				imageIndex = IMAGE_TRACK;
			}
			if (images[imageIndex] == null) {
				imageIndex = IMAGE_THUMB;
			}
		}
		
		if (images[imageIndex] == null) {
			return null;
		}
		
		Dimension dim = new Dimension(images[imageIndex].getDimensions());
		if (imageIndex == IMAGE_INCREMENT || imageIndex == IMAGE_DECREMENT || imageIndex == IMAGE_THUMB) {
			dim.width = dim.width / 4;
		}
		return dim;
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
