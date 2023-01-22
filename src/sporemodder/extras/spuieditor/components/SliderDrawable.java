package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class SliderDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final String INTERFACE_NAME = "ISliderDrawable";
	
	public static final int TYPE = 0xEF034604;
	
	public static final int IMAGE_UNKNOWN = 0;
	public static final int IMAGE_THUMB = 1;
	public static final int IMAGE_BACKGROUND = 2;
	
	private final Image[] images = new Image[3];
	
	public SliderDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short[] indices = SectionShort.getValues(block.getSection(0x6F42D5CD, SectionShort.class), null, 3);
		if (indices != null) {
			Image.loadImages(block, indices, images);
		}
	}
	
	public SliderDrawable(SPUIViewer viewer) {
		super(viewer);
	}

	private SliderDrawable() {
		super();
	}

	@Override
	public SliderDrawable copyComponent(boolean propagateIndependent) {
		SliderDrawable other = new SliderDrawable();
		copyComponent(other, propagateIndependent);
		for (int i = 0; i < images.length; i++) {
			other.images[i] = images[i];
		}
		return other;
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		SPUIObject[] objects = new SPUIObject[images.length];
		for (int i = 0; i < images.length; i++) {
			objects[i] = images[i] == null ? null : builder.addComponent(images[i]);
		}
				
		builder.addReference(block, 0x6F42D5CD, objects);
		
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
	
	public Dimension getThumbDimensions() {
		if (images[IMAGE_THUMB] == null) {
			return null;
		}
		else {
			Dimension dim = images[IMAGE_THUMB].getDimensions();
			return new Dimension(dim.width / 4, dim.height);
		}
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		if (component instanceof WinSlider) {
			WinSlider slider = (WinSlider) component;
			
			if (Image.isValid(images[IMAGE_BACKGROUND])) {
				Dimension imageDim = images[IMAGE_BACKGROUND].getDimensions();
				
				float[] sliceProportions = null;
				Rectangle sliderBounds = new Rectangle();
				
				if (slider.getSliderType() == WinSlider.SLIDER_HORIZONTAL) {
					sliceProportions = new float[] {
							0.5f - (0.5f / imageDim.width),
							0,
							0.5f - (0.5f / imageDim.width),
							0
					};
					
					sliderBounds.x = bounds.x;
					sliderBounds.y = Math.round(bounds.y + (bounds.height - imageDim.height) / 2.0f);
					sliderBounds.width = bounds.width;
					sliderBounds.height = imageDim.height;
				}
				else {
					sliceProportions = new float[] {
							0,
							0.5f - (0.5f / imageDim.height),
							0,
							0.5f - (0.5f / imageDim.height),
					};
					sliderBounds.x = Math.round(bounds.x + (bounds.width - imageDim.width) / 2.0f);
					sliderBounds.y = bounds.y;
					sliderBounds.width = imageDim.width;
					sliderBounds.height = bounds.height;
				}
				
				Image.drawSlicedImage(graphics, sliderBounds, images[IMAGE_BACKGROUND], images[IMAGE_BACKGROUND].getImageUVCoords(), sliceProportions, 
						new float[] {1.0f, 1.0f}, false);
			}
			
			if ((slider.getFlags() & WinSlider.FLAG_ISEDITABLE) == WinSlider.FLAG_ISEDITABLE) {
				WinButton thumbButton = slider.getThumbButton();
				
				if (Image.isValid(images[IMAGE_THUMB])) {
					Image.drawComponentTiledStates(graphics, thumbButton.getRealBounds(), thumbButton, images[IMAGE_THUMB], viewer);
				}
				else {
					new ButtonDrawableStandard().draw(graphics, thumbButton.getRealBounds(), thumbButton);
				}
			}
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				case 0x6F42D5CD: return images;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x6F42D5CD: 
					ComponentChooser.showChooserAction(SliderDrawable.this, "images", index,
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
		}
		if (images[imageIndex] == null) {
			return null;
		}
		Dimension dim = images[imageIndex].getDimensions();
		if (imageIndex == IMAGE_THUMB) {
			dim.width /= 4;
		}
		return dim;
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
