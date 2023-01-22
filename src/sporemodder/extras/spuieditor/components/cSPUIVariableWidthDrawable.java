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
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class cSPUIVariableWidthDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final int TYPE = 0x0109C69D;
	
	private Image image;
	private Color color;

	public cSPUIVariableWidthDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short index = SectionShort.getValues(block.getSection(0xEEC1D000, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			SPUIObject object = block.getParent().get(index);
			Image.loadImagePath(block, object);
			image = (Image) ResourceLoader.getComponent(object);
		}
		
		color = PanelUtils.decodeColor(SectionInt.getValues(block, 0xEEC1D001, new int[] {0}, 1)[0]);
	}
	
	public cSPUIVariableWidthDrawable(SPUIViewer viewer) {
		super(viewer);
		
		color = Color.WHITE;
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder)	{
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addReference(block, 0xEEC1D000, new SPUIObject[] { image != null ? builder.addComponent(image) : null });
		builder.addInt(block, 0xEEC1D001, new int[] { PanelUtils.encodeColor(color) });
		
		if (image != null && !image.isAtlasImage()) {
			image.addImagePath(builder, block);
		}
		
		return block;
	}

	private cSPUIVariableWidthDrawable() {
		super();
	}


	@Override
	public cSPUIVariableWidthDrawable copyComponent(boolean propagateIndependent) {
		cSPUIVariableWidthDrawable other = new cSPUIVariableWidthDrawable();
		copyComponent(other, propagateIndependent);

		other.image = image == null ? null : (propagateIndependent ? image.copyComponent(propagateIndependent) : image);
		other.color = color; 
		
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

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		
		if (bounds.width == 0 || bounds.height == 0) {
			return;
		}
		
		Color tintColor = component.getTintColor();
		Color color = new Color(
				Math.round(this.color.getRed() * tintColor.getRed() / 255.0f),
				Math.round(this.color.getGreen() * tintColor.getGreen() / 255.0f),
				Math.round(this.color.getBlue() * tintColor.getBlue() / 255.0f),
				Math.round(this.color.getAlpha() * tintColor.getAlpha() / 255.0f));

		if (!Image.isValid(image)) {
			graphics.setColor(color);
			graphics.draw(bounds);
		}
		else {
			Dimension imageDim = image.getDimensions();
			int[] imageUVCoords = image.getImageUVCoords();
			int uvWidth = imageUVCoords[2] - imageUVCoords[0];
			
			if (imageDim.width > bounds.width) {
				// draw left side
				graphics.drawImage(
						Image.drawTintedImage(image.getBufferedImage(), new Dimension(
								Math.round(bounds.width * 0.5f), 
								bounds.height), 
								new int[] {
										imageUVCoords[0], 
										imageUVCoords[1], 
										Math.round(imageUVCoords[0] + uvWidth * (((imageDim.width*0.5f - (imageDim.width - bounds.width) * 0.5f)) / imageDim.width)),
										imageUVCoords[3]
								}, color), 
						
						bounds.x, bounds.y, null);
				
				// draw right side
				graphics.drawImage(
						Image.drawTintedImage(image.getBufferedImage(), new Dimension(
								bounds.width - Math.round(bounds.width * 0.5f), 
								bounds.height), 
								new int[] {
										Math.round(imageUVCoords[0] + uvWidth * (((imageDim.width*0.5f + (imageDim.width - bounds.width) * 0.5f)) / imageDim.width)), 
										imageUVCoords[1], 
										imageUVCoords[2],
										imageUVCoords[3]
								}, color), 
						
						bounds.x + Math.round(bounds.width * 0.5f), bounds.y, null);
				
			}
			else if (imageDim.width < bounds.width) {
				// draw left side
				
				graphics.drawImage(
						Image.drawTintedImage(image.getBufferedImage(), new Dimension(
								Math.round(imageDim.width * 0.5f), 
								bounds.height), 
								new int[] {
										imageUVCoords[0], 
										imageUVCoords[1],
										Math.round(imageUVCoords[0] + uvWidth * 0.5f),
										imageUVCoords[3],
								}, color), 
						
						bounds.x, bounds.y, null);
				
				// draw right side
				graphics.drawImage(
						Image.drawTintedImage(image.getBufferedImage(), new Dimension(
								Math.round(imageDim.width * 0.5f), 
								bounds.height), 
								new int[] {
										Math.round(imageUVCoords[0] + uvWidth * 0.5f), 
										imageUVCoords[1],
										imageUVCoords[2],
										imageUVCoords[3],
								}, color), 
						
						bounds.x + bounds.width - Math.round(imageDim.width * 0.5f), bounds.y, null);
				
				// draw center
				
				int[] uvCoords = new int[4];
				uvCoords[0] = Math.round(imageUVCoords[0] + uvWidth * 0.325f);
				uvCoords[1] = imageUVCoords[1];
				uvCoords[2] = Math.round(imageUVCoords[0] + uvWidth * 0.625f);
				uvCoords[3] = imageUVCoords[3];
				
				Rectangle drawBounds = new Rectangle();
				drawBounds.y = bounds.y;
				drawBounds.height = bounds.height;
				drawBounds.x = Math.round(bounds.x + imageDim.width * 0.5f);
				drawBounds.width = bounds.width - imageDim.width;
				
				BufferedImage tintedImage = Image.drawTintedImage(image.getBufferedImage(), new Dimension(bounds.width - imageDim.width, bounds.height), uvCoords, color);
				
				Image.drawTiled((Graphics2D) graphics.create(drawBounds.x, drawBounds.y, drawBounds.width, drawBounds.height), 
						tintedImage, new Dimension(Math.round(imageDim.width * 0.25f), tintedImage.getHeight()), drawBounds, 
						new Point(0, 0), new Point(Math.round(imageDim.width * 0.25f), tintedImage.getHeight()), 
						new int[] {0, 0, tintedImage.getWidth(), tintedImage.getHeight()});
			}
			else {
				graphics.drawImage(Image.drawTintedImage(image.getBufferedImage(), new Dimension(bounds.width, bounds.height), imageUVCoords, color), bounds.x, bounds.y, null);
			}
		}
	}

	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xEEC1D001: return color;
				case 0xEEC1D000: return image;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC1D001: color = (Color) value; break;
					
				case 0xEEC1D000: 
					ComponentChooser.showChooserAction(cSPUIVariableWidthDrawable.this, "image", 
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
		return image == null ? null : image.getDimensions();
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
