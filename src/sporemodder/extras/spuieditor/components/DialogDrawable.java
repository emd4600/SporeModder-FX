package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class DialogDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final String INTERFACE_NAME = "IDialogDrawable";
	
	public static final int TYPE = 0x6F0C7AEA;
	
	private static final Color BORDER_COLOR = new Color(0xFFACA899); 
	private static final Color TITLEBAR_COLOR = new Color(0xFFEFEBD6);
	
	private static final int IMAGE_BACKGROUND = 0;
	private static final int IMAGE_TITLEBAR_BACKGROUND = 1;  // draw 0.25 of image, stretch 0.5 of image, draw 0.25 of image
	
	private static final int MODE_STRETCH = 0;
	private static final int MODE_TILE = 1;
	private static final int MODE_SLICE_STRETCH = 2;
	private static final int MODE_SLICE_TILE = 3;
	
	private final Image[] images = new Image[10];
	private Color backgroundColor;
	private int backgroundDrawMode;

	public DialogDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		backgroundColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0x2F42E1A2, new int[] {0}, 1)[0]);
		backgroundDrawMode = SectionInt.getValues(block, 0x2F42E1A3, new int[] {0}, 1)[0];
		
		short[] indices = SectionShort.getValues(block.getSection(0x2F42E1A1, SectionShort.class), null, 10);
		if (indices != null) {
			Image.loadImages(block, indices, images);
		}
	}
	
	public DialogDrawable(SPUIViewer viewer) {
		super(viewer);
		
		backgroundColor = new Color(0, 0, 0, 0);
		backgroundDrawMode = MODE_STRETCH;
	}
	
	private DialogDrawable() {
		super();
	}

	@Override
	public DialogDrawable copyComponent(boolean propagateIndependent) {
		DialogDrawable other = new DialogDrawable();
		copyComponent(propagateIndependent);
		
		for (int i = 0; i < images.length; i++) {
			if (images[i] != null) {
				other.images[i] = propagateIndependent ? images[i].copyComponent(propagateIndependent) : images[i];
			}
		}
		
		other.backgroundColor = backgroundColor;
		other.backgroundDrawMode = backgroundDrawMode;
		
		return other;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		builder.addInt(block, 0x2F42E1A2, new int[] {PanelUtils.encodeColor(backgroundColor)});
		builder.addInt(block, 0x2F42E1A3, new int[] {backgroundDrawMode});
		
		SPUIObject[] objects = new SPUIObject[images.length];
		for (int i = 0; i < images.length; i++) {
			objects[i] = images[i] == null ? null : builder.addComponent(images[i]);
		}
		builder.addReference(block, 0x2F42E1A1, objects);
		
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
		
		if (component instanceof WinDialog) {
			
			WinDialog dialog = (WinDialog) component;
			int dialogFlags = dialog.getDialogFlags();
			
			Rectangle backgroundBounds = new Rectangle(bounds);
			
			if ((dialogFlags & WinDialog.DIALOG_FLAG_SHOWBORDERS) == WinDialog.DIALOG_FLAG_SHOWBORDERS) {
				graphics.setColor(BORDER_COLOR);
				
				/* LEFT */		graphics.fillRect(bounds.x, bounds.y, WinDialog.DIALOG_BORDER_SIZE, bounds.height);
				/* TOP */		graphics.fillRect(bounds.x, bounds.y, bounds.width, WinDialog.DIALOG_BORDER_SIZE);
				/* RIGHT */		graphics.fillRect(bounds.x + bounds.width - WinDialog.DIALOG_BORDER_SIZE, bounds.y, WinDialog.DIALOG_BORDER_SIZE, bounds.height);
				/* BOTTOM */	graphics.fillRect(bounds.x, bounds.y + bounds.height - WinDialog.DIALOG_BORDER_SIZE, bounds.width, WinDialog.DIALOG_BORDER_SIZE);
				
				backgroundBounds.x += WinDialog.DIALOG_BORDER_SIZE;
				backgroundBounds.y += WinDialog.DIALOG_BORDER_SIZE;
				backgroundBounds.width -= 2*WinDialog.DIALOG_BORDER_SIZE;
				backgroundBounds.height -= 2*WinDialog.DIALOG_BORDER_SIZE;
			}
			
			
			if ((dialogFlags & WinDialog.DIALOG_FLAG_SHOWTITLEBAR) == WinDialog.DIALOG_FLAG_SHOWTITLEBAR) {
				Rectangle titleBarBounds = dialog.getTitleBarBounds(graphics, bounds);
				
				backgroundBounds.y += titleBarBounds.height;
				backgroundBounds.height -= titleBarBounds.height;
				
				if (Image.isValid(images[IMAGE_TITLEBAR_BACKGROUND])) {
					Image.drawSlicedImage(graphics, titleBarBounds, images[IMAGE_TITLEBAR_BACKGROUND], images[IMAGE_TITLEBAR_BACKGROUND].getImageUVCoords(), 
							new float[] {0.25f, 0, 0.25f, 0}, new float[] {1f, 1f}, false);
				}
				else {
					graphics.setColor(TITLEBAR_COLOR);
					graphics.fill(titleBarBounds);
				}
				
				if ((dialogFlags & WinDialog.DIALOG_FLAG_SHOWTITLE) == WinDialog.DIALOG_FLAG_SHOWTITLE) {
					StyleSheetInstance.paintText(graphics, dialog.getTitleStyle(), 
							viewer.getString(dialog.getTitleText()), dialog.getTitleColor(), titleBarBounds, dialog.getTitleMargins());
				}
				
				if (dialog.getCloseButtonDrawable() != null &&
						(dialogFlags & WinDialog.DIALOG_FLAG_SHOWCLOSEBUTTON) == WinDialog.DIALOG_FLAG_SHOWCLOSEBUTTON) {
					WinButton closeButton = dialog.getCloseButton();
					
					dialog.getCloseButtonDrawable().draw(graphics, closeButton.getRealBounds(), closeButton);
				}
			}
			
			if (Image.isValid(images[IMAGE_BACKGROUND])) {
				switch (backgroundDrawMode) {
				case MODE_STRETCH:
					Image.drawImage(graphics, images[IMAGE_BACKGROUND], backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height);
					break;
					
				case MODE_TILE:
					Image.drawTiled(graphics, images[IMAGE_BACKGROUND], backgroundBounds);
					break;
					
				case MODE_SLICE_STRETCH:
					Image.drawSlicedImage(graphics, backgroundBounds, images[IMAGE_BACKGROUND], images[IMAGE_BACKGROUND].getImageUVCoords(), 
							new float[] {0.333f, 0.333f, 0.333f, 0.333f}, new float[] {1f, 1f}, false);
					break;
					
				case MODE_SLICE_TILE:
					Image.drawSlicedImage(graphics, backgroundBounds, images[IMAGE_BACKGROUND], images[IMAGE_BACKGROUND].getImageUVCoords(), 
							new float[] {0.333f, 0.333f, 0.333f, 0.333f}, new float[] {1f, 1f}, true);
					break;
					
				default:
					graphics.setColor(backgroundColor);
					graphics.fill(backgroundBounds);
					break;
				}
			}
			else {
				graphics.setColor(backgroundColor);
				graphics.fill(backgroundBounds);
			}
		}
	}

	@Override
	public Dimension getDimensions(int imageIndex) {
		if (imageIndex == IMAGE_MAIN) {
			imageIndex = IMAGE_BACKGROUND;
		}
		if (images[imageIndex] != null) {
			return images[imageIndex].getDimensions();
		}
		return null;
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x2F42E1A2: return backgroundColor;
				case 0x2F42E1A3: return backgroundDrawMode;
				case 0x2F42E1A1: return images;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x2F42E1A2: backgroundColor = (Color) value; break;
				case 0x2F42E1A3: backgroundDrawMode = (int) value; break;
					
				case 0x2F42E1A1: 
					ComponentChooser.showChooserAction(DialogDrawable.this, "images", index ,
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
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}
}
