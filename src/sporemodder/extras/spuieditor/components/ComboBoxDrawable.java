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
import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.extras.spuieditor.components.WinComboBox.ItemButton;
import sporemodder.extras.spuieditor.components.WinComboBox.ListButton;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class ComboBoxDrawable extends SPUIDefaultComponent implements SPUIDrawable {
	
	public static final String INTERFACE_NAME = "IComboBoxDrawable";
	
	public static final int TYPE = 0x2F552CCE;
	
	public static final int IMAGE_UNKNOWN = 0;
	public static final int IMAGE_MAIN = 1;
	public static final int IMAGE_BUTTON = 2;
	
	public static final int COLOR_IDLE_TEXT = 0;
	public static final int COLOR_IDLE_BACKGROUND = 1;
	public static final int COLOR_HOVER_TEXT = 2;
	public static final int COLOR_HOVER_BACKGROUND = 3;
	public static final int COLOR_SELECTED_TEXT = 4;
	public static final int COLOR_SELECTED_BACKGROUND = 5;
	
	public static final int ITEM_HEIGHT = 24;
	
	public static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(16, 16);
	public static final int DEFAULT_SCROLLBAR_WIDTH = 16;
	
	private final Image[] images = new Image[3];

	public ComboBoxDrawable(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short[] indices = SectionShort.getValues(block.getSection(0x2F591E33, SectionShort.class), null, 3);
		if (indices != null) {
			Image.loadImages(block, indices, images);
		}
	}
	
	public ComboBoxDrawable(SPUIViewer viewer) {
		super(viewer);
	}
	
	private ComboBoxDrawable() {
		super();
	}
	
	@Override
	public boolean isValidPoint(Point p, Rectangle bounds) {
		return true;
	}

	@Override
	public ComboBoxDrawable copyComponent(boolean propagateIndependent) {
		ComboBoxDrawable other = new ComboBoxDrawable();
		copyComponent(other, propagateIndependent);
		
		for (int i = 0; i < images.length; i++) {
			other.images[i] = images[i] == null ? null : (propagateIndependent ? images[i].copyComponent(propagateIndependent) : images[i]);
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
				
		builder.addReference(block, 0x2F591E33, objects);
		
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
		if (!(component instanceof WinComboBox)) {
			return;
		}
		
		WinComboBox combobox = (WinComboBox) component;
		
		ListButton btnShowList = combobox.getShowListButton();
		
		if (Image.isValid(images[IMAGE_MAIN])) {
			Image.drawImage(graphics, images[IMAGE_MAIN], bounds.x, bounds.y);
		}
		
		int state = component.isActionableComponent() ? ((ActionableComponent) component).getState() : 0;
		
		if ((combobox.getComboBoxFlags() & WinComboBox.COMBOBOX_DRAWOUTLINE) == WinComboBox.COMBOBOX_DRAWOUTLINE && viewer.isPreview() && 
				((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER
				|| (state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK 
				|| (btnShowList.getState() & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER
			|| (btnShowList.getState() & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK)) 
		{
			graphics.setColor(Color.BLACK);
			graphics.draw(bounds);
		}
		
		if ((combobox.getFlags() & WinComboBox.FLAG_ISEDITABLE) == WinComboBox.FLAG_ISEDITABLE) {
			if (Image.isValid(images[IMAGE_BUTTON])) {
				Image.drawComponentTiledStates(graphics, btnShowList.getRealBounds(), combobox.getFlags(), btnShowList.getState(), images[IMAGE_BUTTON]);
			}
		}
		
		Color[] colors = combobox.getColors();
		int valueCount = combobox.getListValueCount();
		
		StyleSheetInstance style = combobox.getStyle();
		LocalizedText selectedValue = combobox.getSelectedValue();
		if (selectedValue != null) {
			
			StyleSheetInstance.paintText(graphics, style, viewer.getString(selectedValue), colors[COLOR_IDLE_TEXT], bounds, null);
		}
		
		if (combobox.isShowingList()) {
			List<ItemButton> buttons = combobox.getItemButtons();
			for (int i = 0; i < buttons.size() && i < valueCount; i++) {
				
				ItemButton button = buttons.get(i);
				if (button != null) {
					Rectangle itemBounds = button.getRealBounds();
					Color backgroundColor = Color.WHITE;
					Color textColor = Color.BLACK;
					
					LocalizedText itemValue = button.getValue();
					
					if (itemValue == selectedValue) {
						backgroundColor = colors[COLOR_SELECTED_BACKGROUND];
						textColor = colors[COLOR_SELECTED_TEXT];
					}
					else if ((button.getState() & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
						backgroundColor = colors[COLOR_HOVER_BACKGROUND];
						textColor = colors[COLOR_HOVER_TEXT];
					}
					else {
						backgroundColor = colors[COLOR_IDLE_BACKGROUND];
						textColor = colors[COLOR_IDLE_TEXT];
					}
					
					graphics.setColor(backgroundColor);
					graphics.fill(itemBounds);
					
					StyleSheetInstance.paintText(graphics, style, viewer.getString(itemValue), textColor, itemBounds, null);
					
				}
			}
			
			if (buttons.size() > valueCount) {
				ScrollbarDrawable scrollbarDrawable = combobox.getScrollbarDrawable();
				if (scrollbarDrawable == null) {
					scrollbarDrawable = new ScrollbarDrawable();
				}
				
				WinScrollbar scrollbar = combobox.getScrollbar();
				
				Rectangle scrollbarBounds = new Rectangle(bounds.x + bounds.width, bounds.y + bounds.height, 
						scrollbar.bounds.width, valueCount * ITEM_HEIGHT);
				
				scrollbarDrawable.draw(graphics, scrollbarBounds, scrollbar);
			}
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x2F591E33: return images;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x2F591E33: 
					ComponentChooser.showChooserAction(ComboBoxDrawable.this, "images", index,
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
		if (imageIndex == SPUIDrawable.IMAGE_MAIN) {
			imageIndex = IMAGE_MAIN;
		}
		if (images[imageIndex] == null) {
			return null;
		}
		
		Dimension dim = new Dimension(images[imageIndex].getDimensions());
		if (imageIndex == IMAGE_BUTTON) {
			dim.width = dim.width / 4;
		}
		return dim;
	}
}
