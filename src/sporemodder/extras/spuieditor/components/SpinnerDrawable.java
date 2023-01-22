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

public class SpinnerDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final String INTERFACE_NAME = "ISpinnerDrawable";

	public static final int TYPE = 0xEF063C4A;
	
	public static final int IMAGE_BACKGROUND = 0;
	public static final int IMAGE_ARROWUP = 1;
	public static final int IMAGE_ARROWDOWN = 2;
	
	private final Image[] images = new Image[3];
	
	public SpinnerDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short[] indices = SectionShort.getValues(block.getSection(0x4F4134A8, SectionShort.class), null, 3);
		if (indices != null) {
			Image.loadImages(block, indices, images);
		}
	}
	
	public SpinnerDrawable(SPUIViewer viewer) {
		super(viewer);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addReference(block, 0x4F4134A8, new SPUIObject[] {
			images[IMAGE_BACKGROUND] == null ? null : builder.addComponent(images[IMAGE_BACKGROUND]),
			images[IMAGE_ARROWUP] == null ? null : builder.addComponent(images[IMAGE_ARROWUP]),
			images[IMAGE_ARROWDOWN] == null ? null : builder.addComponent(images[IMAGE_ARROWDOWN])
		});
		
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
	
	private SpinnerDrawable() {
		super();
	}
	
	@Override
	public SpinnerDrawable copyComponent(boolean propagate) {
		SpinnerDrawable other = new SpinnerDrawable();
		copyComponent(other, propagate);
		
		for (int i = 0; i < images.length; i++) {
			other.images[i] = images[i] == null ? null : (propagate ? images[i].copyComponent(propagate) : images[i]);
		}
		
		return other;
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		if (component instanceof WinSpinner) {
			WinSpinner spinner = (WinSpinner) component;
			
			// apparently the background isn't used
			
			if (Image.isValid(images[IMAGE_ARROWUP])) {
				Image.drawComponentTiledStates(graphics, spinner.getArrowUpButton().getRealBounds(), component.getFlags(), spinner.getArrowUpButton().getState(), 
						images[IMAGE_ARROWUP]);
			}
			
			if (Image.isValid(images[IMAGE_ARROWDOWN])) {
				Image.drawComponentTiledStates(graphics, spinner.getArrowDownButton().getRealBounds(), component.getFlags(), spinner.getArrowDownButton().getState(), 
						images[IMAGE_ARROWDOWN]);
			}
		}
	}

	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x4F4134A8: return images;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x4F4134A8: 
					ComponentChooser.showChooserAction(SpinnerDrawable.this, "images", index,
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
		Dimension dim = new Dimension(images[imageIndex].getDimensions());
		if (imageIndex == IMAGE_ARROWUP || imageIndex == IMAGE_ARROWDOWN) {
			dim.width /= 4;
		}
		return dim;
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
