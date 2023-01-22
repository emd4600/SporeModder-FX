package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;

public class WinComboBox extends Window {
	
	public static final int COMBOBOX_DRAWOUTLINE = 1;
	public static final int ALIGNMENT_LEFT = 0;
	public static final int ALIGNMENT_RIGHT = 1;
	
	protected class ListButton implements ActionableComponent {
		private int state;
		
		@Override
		public void setState(int state) {
			int oldState = this.state;
			this.state = state;

			if ((state & STATE_CLICK) == STATE_CLICK && (oldState & STATE_CLICK) != STATE_CLICK) {
				isShowingList = !isShowingList;
			}
			
			if (state != oldState && viewer != null && viewer.isPreview()) {
				viewer.repaint();
			}
		}

		@Override
		public int getState() {
			return state;
		}

		@Override
		public Rectangle getRealBounds() {
			Dimension dim = drawable == null ? null : ((ComboBoxDrawable) drawable).getDimensions(ComboBoxDrawable.IMAGE_BUTTON);
			if (dim == null) {
				dim = new Dimension(ComboBoxDrawable.DEFAULT_BUTTON_SIZE);
			}
			
			Rectangle rect = new Rectangle(dim.width, dim.height);
			rect.x = realBounds.x - rect.width;
			rect.y = realBounds.y + (realBounds.height - rect.height) / 2;
			
			if (alignment == ALIGNMENT_RIGHT) {
				rect.x += realBounds.width;
			}
			
			return rect;
		}
	}
	
	protected class ItemButton implements ActionableComponent {
		private int state;
		private int index;
		
		public ItemButton(int index) {
			this.index = index;
		}
		
		@Override
		public void setState(int state) {
			int oldState = this.state;
			this.state = state;

			if ((state & STATE_CLICK) != STATE_CLICK && (oldState & STATE_CLICK) == STATE_CLICK) {
				WinComboBox.this.selectedValue = getValue();
				WinComboBox.this.isShowingList = false;
				WinComboBox.this.btnShowList.state &= ~STATE_SELECTED;
			}
			
			if (state != oldState && viewer != null && viewer.isPreview()) {
				viewer.repaint();
			}
		}

		@Override
		public int getState() {
			return state;
		}

		@Override
		public Rectangle getRealBounds() {
			Rectangle rect = new Rectangle(realBounds.width, ComboBoxDrawable.ITEM_HEIGHT);
			rect.x = realBounds.x;
			rect.y = realBounds.y + realBounds.height + ComboBoxDrawable.ITEM_HEIGHT * index;
			
			return rect;
		}
		
		public LocalizedText getValue() {
			return values.get(index);
		}
	}
	
	public static final int TYPE = 0xAF552C4B;
	
	public static final int FLAG_ISEDITABLE = 0x8;
	
	private final Color[] colors = new Color[6];
	private final OutlineFormat textOutline = new OutlineFormat();
	private ScrollbarDrawable scrollbarDrawable;
	private final List<LocalizedText> values = new ArrayList<LocalizedText>();
	private int listValueCount;
	private int comboBoxFlags;
	private int alignment;
	
	private ListButton btnShowList;
	private WinScrollbar scrollbar;
	private LocalizedText selectedValue;
	
	// used in preview
	private final List<ItemButton> itemButtons = new ArrayList<ItemButton>();
	private boolean isShowingList;
	
	public WinComboBox(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		int[] colorValues = SectionInt.getValues(block, 0xAF58F7D4, new int[] {0, 0, 0, 0, 0, 0}, 6);
		for (int i = 0; i < colorValues.length; i++) {
			colors[i] = PanelUtils.decodeColor(colorValues[i]);
		}
		
		LocalizedText[] textValues = SectionText.getValues(block.getSection(0xaf58f7d1, SectionText.class), new LocalizedText[0], -1);
		for (LocalizedText value : textValues) {
			values.add(value);
		}
		
		short drawableIndex = SectionShort.getValues(block.getSection(0xAF58F7D7, SectionShort.class), new short[] {-1}, 1)[0];
		if (drawableIndex != -1) {
			scrollbarDrawable = (ScrollbarDrawable) ResourceLoader.getComponent(block.getParent().get(drawableIndex));
		} else {
			scrollbarDrawable = new ScrollbarDrawable();
		}
		
		listValueCount = this.getIntProperty(block, 0xAF58F7D5, 0);
		alignment = this.getIntProperty(block, 0xaf58f7d3, 0);
		comboBoxFlags = this.getIntProperty(block, 0xaf58f7d6, 0);
		
		ListSectionContainer[] property_039A69E6 = SectionSectionList.getValues(block.getSection(0x039A69E6, SectionSectionList.class), null, 1, -1);
		if (property_039A69E6 != null) {
			textOutline.parse(property_039A69E6[0]);;
		}
		
		addUnassignedInt(block, 0xAF58F7D3, 1);
		addUnassignedInt(block, 0xAF58F7D6, 1);
		
		if (!(drawable instanceof ComboBoxDrawable)) {
			throw new InvalidBlockException("WinComboBox drawable must be of the type ComboBoxDrawable");
		}
		
		initScrollbar();
		
		btnShowList = new ListButton();
		
		if (values.size() > 0) {
			selectedValue = values.get(0);
		}
		
		createItemButtons();
	}
	
	private void initScrollbar() {
		scrollbar = new WinScrollbar(scrollbarDrawable, 0, values.size(), 0, listValueCount);
		scrollbar.flags = WinScrollbar.FLAG_ISEDITABLE | FLAG_VISIBLE | FLAG_ENABLED;
		scrollbar.modifiers.clear();
		scrollbar.modifiers.add(new SimpleLayout(viewer, SimpleLayout.FLAG_RIGHT | SimpleLayout.FLAG_BOTTOM));
		setScrollbarSize();
	}
	
	public WinComboBox(SPUIViewer viewer) {
		super(viewer);
		
		comboBoxFlags |= COMBOBOX_DRAWOUTLINE;
		alignment = ALIGNMENT_RIGHT;

		values.add(new LocalizedText("Foo"));
		values.add(new LocalizedText("Bar"));
		values.add(new LocalizedText("Foobar"));
		
		listValueCount = 3;
		
		flags |= FLAG_ISEDITABLE;
		
		btnShowList = new ListButton();

		initScrollbar();
		createItemButtons();
	}
	
	private void setScrollbarSize() {
		if (scrollbarDrawable != null) {
			int width = 0;
			Dimension dim = scrollbarDrawable.getDimensions(ScrollbarDrawable.IMAGE_INCREMENT);
			if (dim != null && dim.width > width) {
				width = dim.width;
			}
			
			dim = scrollbarDrawable.getDimensions(ScrollbarDrawable.IMAGE_DECREMENT);
			if (dim != null && dim.width > width) {
				width = dim.width;
			}
			
			dim = scrollbarDrawable.getDimensions(ScrollbarDrawable.IMAGE_THUMB);
			if (dim != null && dim.width > width) {
				width = dim.width;
			}
			
			if (width == 0) {
				width = ComboBoxDrawable.DEFAULT_SCROLLBAR_WIDTH;
			}
			
			scrollbar.bounds.setSize(width, listValueCount * ComboBoxDrawable.ITEM_HEIGHT);
			scrollbar.setParent(this);
		}
	}
	
	private void createItemButtons() {
		itemButtons.clear();
		for (int i = 0; i < values.size(); i++) {
			itemButtons.add(new ItemButton(i));
		}
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		builder.addInt(block, 0xAF58F7D6, new int[] {comboBoxFlags});
		builder.addSectionList(block, 0x039A69E6, new ListSectionContainer[] {textOutline.saveComponent(builder)}, 122);
		builder.addInt(block, 0xAF58F7D3, new int[] {alignment});
		
		int[] colorValues = new int[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colorValues[i] = PanelUtils.encodeColor(colors[i]);
		}
		builder.addInt(block, 0xAF58F7D4, colorValues);
		
		builder.addReference(block, 0xAF58F7D7, new SPUIObject[] {builder.addComponent(scrollbarDrawable)});
		builder.addInt(block, 0xAF58F7D5, new int[] {listValueCount});
		builder.addText(block, 0xAF58F7D1, (LocalizedText[]) values.toArray(new LocalizedText[values.size()]));
		
		return block;
	}

	private WinComboBox() {
		super();
	}

	@Override
	public WinComboBox copyComponent(boolean propagateIndependent) {
		WinComboBox other = new WinComboBox();
		copyComponent(other, propagateIndependent);
		
		for (int i = 0; i < colors.length; i++) {
			other.colors[i] = colors[i];
		}
		
		other.listValueCount = listValueCount;
		other.comboBoxFlags = comboBoxFlags;
		other.alignment = alignment;
		if (scrollbarDrawable != null) {
			other.scrollbarDrawable = propagateIndependent ? scrollbarDrawable.copyComponent(propagateIndependent) : scrollbarDrawable;
		}
		other.values.addAll(values);
		other.selectedValue = selectedValue;
		textOutline.copyComponent(other.textOutline);
		
		other.scrollbar = scrollbar.copyComponent(propagateIndependent);
		other.scrollbar.setParent(other);

		other.btnShowList = other.new ListButton();
		
		other.isShowingList = isShowingList;
		
		other.createItemButtons();
		
		return other;
	}
	
	public ListButton getShowListButton() {
		return btnShowList;
	}
	
	public WinScrollbar getScrollbar() {
		return scrollbar;
	}
	
	public int getListValueCount() {
		return listValueCount;
	}
	
	public int getComboBoxFlags() {
		return comboBoxFlags;
	}

	public int getAlignment() {
		return alignment;
	}

	public void setComboBoxFlags(int comboBoxFlags) {
		this.comboBoxFlags = comboBoxFlags;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public Color[] getColors() {
		return colors;
	}
	
	public List<LocalizedText> getValues() {
		return values;
	}
	
	public LocalizedText getSelectedValue() {
		return selectedValue != null ? selectedValue : (values.isEmpty() ? null : values.get(0));
	}
	
	public ScrollbarDrawable getScrollbarDrawable() {
		return scrollbarDrawable;
	}
	
	public List<ItemButton> getItemButtons() {
		return itemButtons;
	}
	
	public boolean isShowingList() {
		return isShowingList;
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			if (scrollbarDrawable == component) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		if (scrollbarDrawable != null) {
			scrollbarDrawable.getComponents(resultList, filter);
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.equals("scrollbarDrawable")) {
				scrollbarDrawable = (ScrollbarDrawable) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		if (scrollbarDrawable != null) {
			if (propagate) {
				scrollbarDrawable.removeComponent(removeAction, propagate);
			}
			if (scrollbarDrawable == removedComp) {
				modifiedValues.add("scrollbarDrawable");
				scrollbarDrawable = null;
			}
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		
		if (scrollbar != null) {
			scrollbar.setSPUIViewer(viewer);
		}
	}
	
	@Override
	public void revalidate() {
		super.revalidate();
		
		if (scrollbar != null) {
			scrollbar.revalidate();
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) {
				if (isShowingList) {
					T comp = scrollbar.getComponentInCoords(p, type);
					if (comp != null) {
						return comp;
					}
				}
				if (type.isInstance(scrollbar) && scrollbar.realBounds.contains(p)) {
					return (T) scrollbar;
				}
				else if (type.isInstance(btnShowList) && btnShowList.getRealBounds().contains(p)) {
					return (T) btnShowList;
				}
				else if (type.isAssignableFrom(ItemButton.class) && isShowingList) {
					for (int i = 0; i < itemButtons.size() && i < listValueCount; i++) {
						if (itemButtons.get(i).getRealBounds().contains(p)) {
							return (T) itemButtons.get(i);
						}
					}
				}
				
				if (realBounds.contains(p) && type.isInstance(this)) {
					return (T) this;
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return super.getComponentInCoords(p, type);
		}
	}
	
	public void addValue(LocalizedText value) {
		values.add(value);
		itemButtons.add(new ItemButton(values.size() - 1));
	}
	
	public void removeLastValue() {
		values.remove(values.size() - 1);
		itemButtons.remove(itemButtons.size() - 1);
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xAF58F7D5: return listValueCount;
				case 0xaf58f7d3: return alignment;
				case 0xaf58f7d6: return comboBoxFlags;
				case 0xAF58F7D4: return colors;
				case 0xaf58f7d1: return values;
				case 0xAF58F7D7: return scrollbarDrawable;
				case 0x039A69E6: return textOutline;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xAF58F7D5: listValueCount = (int) value; break;
				case 0xAF58F7D3: 
					alignment = (int) value; 
					viewer.repaint();
					break;
				case 0xAF58F7D6: comboBoxFlags = (int) value; break;
				case 0xAF58F7D4: 
					colors[index] = (Color) value;
					viewer.repaint();
					break;
				case 0xaf58f7d1: 
					values.set(index, new LocalizedText((LocalizedText) value));
					viewer.repaint();
					break;
					
				case 0xAF58F7D7: 
					ComponentChooser.showChooserAction(WinComboBox.this, "scrollbarDrawable", 
							ComponentFactory.getComponentChooser(property.getType(), viewer),
							(JLabelLink) value, viewer, false);
					break;
				}
				
				super.setValue(property, value, index);
			}
		};
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
