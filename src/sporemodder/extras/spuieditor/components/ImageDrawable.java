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

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUISectionContainer;
import sporemodder.userinterface.JLabelLink;

public class ImageDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final int TYPE = 0xCF3C4EAE;
	
	private static final int SLICE = 2;
	private static final int TILE = 1;
	private static final int STRETCH = 0;
	
	private static final int FLAG_SCALE_X = 0x1;
	private static final int FLAG_SCALE_Y = 0x2;
	
	private Image image;
	private float scalingFactor;
	private int alignX;
	private int alignY;
	private int scalingFlags;
	private int tilingMode;
	private final OutlineFormat imageOutline = new OutlineFormat();

	public ImageDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short index = SectionShort.getValues(block.getSection(0x4F3C4A26, SectionShort.class), new short[] {-1}, 1)[0];
		if (index != -1) {
			SPUIObject object = block.getParent().get(index);
			Image.loadImagePath(block, object);
			image = (Image) ResourceLoader.getComponent(object);
		}
		
		scalingFlags = SectionInt.getValues(block, 0x4F3C4A27, new int[] {0}, 1)[0];
		alignX = SectionInt.getValues(block, 0x4F3C4A29, new int[] {3}, 1)[0];
		alignY = SectionInt.getValues(block, 0x4F3C4A28, new int[] {3}, 1)[0];
		scalingFactor = SectionFloat.getValues(block.getSection(0x4F3C4A2B, SectionFloat.class), new float[] {1.0f}, 1)[0];
		
		tilingMode = SectionInt.getValues(block, 0x4F3C4A2A, new int[] {0}, 1)[0];
		
		SPUISectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		
		if (property_039A69E6 != null) {
			this.imageOutline.parse(property_039A69E6[0]);
		}
	}
	
	public ImageDrawable(SPUIViewer viewer) {
		super(viewer);
		
		alignX = 3;
		alignY = 3;
		tilingMode = STRETCH;
		scalingFlags = 0;
		scalingFactor = 1.0f;
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder)	{
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addReference(block, 0x4F3C4A26, new SPUIObject[] { image != null ? builder.addComponent(image) : null });
		builder.addInt(block, 0x4F3C4A29, new int[] { alignX });
		builder.addInt(block, 0x4F3C4A28, new int[] { alignY });
		builder.addInt(block, 0x4F3C4A27, new int[] { scalingFlags });
		builder.addInt(block, 0x4F3C4A2A, new int[] { tilingMode });
		builder.addFloat(block, 0x4F3C4A2B, new float[] { scalingFactor });
		
		if (imageOutline != null) {
			builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {imageOutline.saveComponent(builder)}, 122);
		}
		
		if (image != null && !image.isAtlasImage()) {
			image.addImagePath(builder, block);
		}
		
		
		return block;
	}
	
	private ImageDrawable() {
		super();
	}
	
	public ImageDrawable copyComponent(boolean propagate) {
		ImageDrawable other = new ImageDrawable();
		super.copyComponent(other, propagate);
		other.image = image;
		other.scalingFactor = scalingFactor;
		other.scalingFlags = scalingFlags;
		other.tilingMode = tilingMode;
		other.alignX = alignX;
		other.alignY = alignY;
		imageOutline.copyComponent(other.imageOutline);
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
	
	private void paint(Graphics2D graphics, Rectangle bounds, Point p1, Point p2, int[] uvCoordinates, int[] cutUvCoordinates, Dimension realDim) {
		if (tilingMode == SLICE) {
			
			float[] sliceProportions = new float[] {0.333f, 0.333f, 0.333f, 0.333f};
			
			Image.drawSlicedImage(graphics, bounds, image, uvCoordinates, sliceProportions, 
					new float[] {(p2.x - p1.x) / realDim.width, (p2.y - p1.y) / realDim.height}, false);
			
		}
		else if (tilingMode == TILE) {
			Image.drawTiled((Graphics2D) graphics.create(bounds.x, bounds.y, bounds.width, bounds.height), image.getBufferedImage(), realDim, bounds, p1, p2, uvCoordinates);
		}
		else if (tilingMode == STRETCH) {
			p1.x = Math.max(p1.x, 0);
			p1.y = Math.max(p1.y, 0);
			p2.x = bounds.width;
			p2.y = bounds.height;
			
			Image.drawImage(graphics, image.getBufferedImage(), 
					bounds.x + p1.x, 
					bounds.y + p1.y, 
					bounds.x + p2.x, 
					bounds.y + p2.y, 
					cutUvCoordinates[0], cutUvCoordinates[1], cutUvCoordinates[2], cutUvCoordinates[3]);
		}
	}

	@Override
	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component) {
		if (Image.isValid(image)) {
			Dimension dim = image.getDimensions();
			int[] uvCoordinates = image.getImageUVCoords();
			
			int[] cutUvCoordinates = new int[] {uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3] };
			
			Point p1 = new Point(0, 0);
			Point p2 = new Point(dim.width, dim.height);
			
			Dimension realDim = new Dimension(dim.width, dim.height);
			
			if ((scalingFlags & FLAG_SCALE_X) == FLAG_SCALE_X) {
				float value = dim.width * scalingFactor;
				realDim.width = (int) value;
				if (alignX == 1) {
					p2.x += value;
				} else if (alignX == 2) {
					p1.x = (int) (p2.x - value);
				} else if (alignX == 3) {
					p1.x = (int) ((p2.x - p1.x - value) * 0.5 + p1.x);
					p2.x = (int) (p1.x + value);
				}
				
				if (p2.x > bounds.width) {
					// remove part of the UV too
					cutUvCoordinates[2] -= ((float) dim.width / (p2.x - p1.x)) * (p2.x - bounds.width);
				}
				if (p1.x > 0) {
					// remove part of the UV too
					cutUvCoordinates[0] -= ((float) dim.width / (p2.x - p1.x)) * p2.x;
				}
			}
			
			if ((scalingFlags & FLAG_SCALE_Y) == FLAG_SCALE_Y) {
				float value = dim.height * scalingFactor;
				realDim.height = (int) value;
				if (alignY == 1) {
					p2.y += value;
				} else if (alignY == 2) {
					p1.y = (int) (p2.y - value);
				} else if (alignY == 3) {
					p1.y = (int) ((p2.y - p1.y - value) * 0.5 + p1.y);
					p2.y = (int) (p1.y + value);
				}
				
				if (p2.y > bounds.height) {
					// remove part of the UV too
					cutUvCoordinates[3] -= ((float) dim.height / (p2.y - p1.y)) * (p2.y - bounds.height);
				}
				if (p1.y > 0) {
					// remove part of the UV too
					cutUvCoordinates[1] -= ((float) dim.height / (p2.y - p1.y)) * p2.y;
				}
			}
			
			Color tint = component.getTintColor();
			if (tint == null || tint.equals(Color.white)) {
				paint(graphics, bounds, p1, p2, uvCoordinates, cutUvCoordinates, realDim);
			}
			else {
				BufferedImage graphicsBuffer = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
				
				paint(graphicsBuffer.createGraphics(),  new Rectangle(0, 0, bounds.width, bounds.height), p1, p2, uvCoordinates, cutUvCoordinates, realDim);
				
				float[] scaleFactors = new float[4];
				float[] offsets = new float[4];
				
				scaleFactors[0] = tint.getRed() / 255f;
				scaleFactors[1] = tint.getGreen() / 255f;
				scaleFactors[2] = tint.getBlue() / 255f;
				scaleFactors[3] = tint.getAlpha() / 255f;
				
				graphics.drawImage(graphicsBuffer, new RescaleOp(scaleFactors, offsets, null), bounds.x, bounds.y);				
			}
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x4F3C4A27: return scalingFlags;
				case 0x4F3C4A29: return alignX;
				case 0x4F3C4A28: return alignY;
				case 0x4F3C4A2B: return scalingFactor;
				case 0x4F3C4A2A: return tilingMode;
				case 0x039A69E6: return imageOutline;
				case 0x4F3C4A26: return image;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x4F3C4A27: scalingFlags = (int) value; break;
				case 0x4F3C4A29: alignX = (int) value; break;
				case 0x4F3C4A28: alignY = (int) value; break;
				case 0x4F3C4A2B: scalingFactor = (float) value; break;
				case 0x4F3C4A2A: tilingMode = (int) value; break;
					
				case 0x4F3C4A26: 
					ComponentChooser.showChooserAction(ImageDrawable.this, "image",
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
		return image == null ? null : new Dimension(image.getDimensions());
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
