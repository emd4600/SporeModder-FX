package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionBoolean;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class ButtonDrawableStandard extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final int TYPE = 0xAF3A9E26;
	
	protected Image image;
	protected boolean sliceImage;
	
	public ButtonDrawableStandard(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);

		short index = SectionShort.getValues(block.getSection(0x0F3AC75E, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			SPUIObject object = block.getParent().get(index);
			Image.loadImagePath(block, object);
			image = (Image) ResourceLoader.getComponent(object);
		}
		
		sliceImage = SectionBoolean.getValues(block.getSection(0xEF3C000A, SectionBoolean.class), new boolean[] {false}, 1)[0];
	}
	
	public ButtonDrawableStandard(SPUIViewer viewer) {
		super(viewer);
	}
	
	protected ButtonDrawableStandard() {
		super();
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addReference(block, 0x0F3AC75E, new SPUIObject[] {builder.addComponent(image)});
		
		builder.addBoolean(block, 0xEF3C000A, new boolean[] {sliceImage});
		
		if (image != null && !image.isAtlasImage()) {
			image.addImagePath(builder, block);
		}
		
		return block;
	}
	
	protected void copyComponent(ButtonDrawableStandard other, boolean propagate) {
		super.copyComponent(other, propagate);
		
		other.sliceImage = sliceImage;
		if (image != null) {
			other.image = propagate ? image.copyComponent(propagate) : image;
		}
	}
	
	@Override
	public ButtonDrawableStandard copyComponent(boolean propagate) {
		ButtonDrawableStandard other = new ButtonDrawableStandard();
		copyComponent(other, propagate);
		return other;
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			if (image == component) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		if (image != null) {
			image.getComponents(resultList, filter);
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.equals("image")) {
				image = (Image) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		if (propagate && image != null) {
			image.removeComponent(removeAction, propagate);
		}
		if (image == removedComp) {
			modifiedValues.add("image");
			image = null;
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	protected int[] getTransformedUVs(WinComponent component) {
		
		int index = Image.STATE_INDEX_IDLE;
		if ((component.getFlags() & WinComponent.FLAG_ENABLED) == WinComponent.FLAG_ENABLED) {
			if (viewer.isPreview()) {
				int state = component.isActionableComponent() ? ((ActionableComponent) component).getState() : 0;
				
				if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK) {
					index = Image.STATE_INDEX_ONCLICK;
				}
				else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
					index = Image.STATE_INDEX_HOVER;
				}
				else {
					index = Image.STATE_INDEX_IDLE;
				}
			}
			else {
				index = Image.STATE_INDEX_IDLE;
			}
		}
		
		float[] uvCoordinates = image.getUVCoords();
		BufferedImage bufferedImage = image.getBufferedImage();
		
		float tileSize = (uvCoordinates[2] - uvCoordinates[0]) / 4.0f;
		
		int[] imageUVCoords = new int[4];
		imageUVCoords[0] = (int) Math.round((uvCoordinates[0] + tileSize * index) * bufferedImage.getWidth());
		imageUVCoords[1] = (int) Math.round(uvCoordinates[1] * bufferedImage.getHeight()); 
		imageUVCoords[2] = (int) Math.round((uvCoordinates[0] + tileSize * index + tileSize) * bufferedImage.getWidth());
		imageUVCoords[3] = (int) Math.round(uvCoordinates[3] * bufferedImage.getHeight());
		
		return imageUVCoords;
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		if (Image.isValid(image)) {
			
			int[] imageUVCoords = getTransformedUVs(component);
			
			if (sliceImage) {
				Image.drawSlicedImage(graphics, bounds, image, imageUVCoords, new float[] {0.333f, 0.333f, 0.333f, 0.333f}, new float[] {1.0f, 1.0f}, false);
			}
			else {
				Image.drawImage(graphics, image.getBufferedImage(), 
						bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, 
						imageUVCoords[0], imageUVCoords[1], imageUVCoords[2], imageUVCoords[3]);
			}
		}
		else {
			graphics.setColor(new Color(0xECE9D8));
			graphics.fill(new Rectangle(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4));
			
			graphics.setColor(new Color(0xF1EFE2));
			graphics.draw(new Rectangle(bounds.x+1, bounds.y+1, bounds.width-3, 1));
			graphics.draw(new Rectangle(bounds.x+1, bounds.y+2, 1, bounds.height-4));
			
			graphics.setColor(new Color(0xACA899));
			graphics.draw(new Rectangle(bounds.x+bounds.width-2, bounds.y+1, 1, bounds.height - 2));
			graphics.draw(new Rectangle(bounds.x+1, bounds.y+bounds.height-2, bounds.width-3, 1));
			
			graphics.setColor(new Color(0xFFFFFF));
			graphics.draw(new Rectangle(bounds.x, bounds.y, bounds.width - 1, 1));
			graphics.draw(new Rectangle(bounds.x, bounds.y+1, 1, bounds.height - 2));
			
			graphics.setColor(new Color(0x716F64));
			graphics.draw(new Rectangle(bounds.x+bounds.width-1, bounds.y, 1, bounds.height));
			graphics.draw(new Rectangle(bounds.x, bounds.y+bounds.height-1, bounds.width-1, 1));
		}
		
	}

	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x0f3ac75e: return image;
				case 0xef3c000a: return sliceImage;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xef3c000a: sliceImage = (boolean) value; break;
				case 0x0f3ac75e: 
					ComponentChooser.showChooserAction(ButtonDrawableStandard.this, "image", 
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
		Dimension dim = image == null ? null : new Dimension(image.getDimensions());
		if (dim != null) {
			dim.width /= 4; 
		}
		return dim;
	}
}
