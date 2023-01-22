package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt2;

public class WinScrollbar extends Window {
	
	protected class ScrollbarButton implements ActionableComponent {
		private static final int BUTTON_INCREMENT = 0;
		private static final int BUTTON_DECREMENT = 1;
		
		private int state;
		private int type;
		
		private ScrollbarButton(int type) {
			this.type = type;
		}
		
		@Override
		public void setState(int state) {
			if (this.state != state) {
				this.state = state;
				viewer.repaint();
			}
		}

		@Override
		public int getState() {
			return state;
		}

		@Override
		public Rectangle getRealBounds() {
			Rectangle rect = new Rectangle();
			
			if (type == BUTTON_INCREMENT) {
				Dimension incrementDim = getDimensions(ScrollbarDrawable.IMAGE_INCREMENT);
				
				if (scrollbarType == HORIZONTAL) {
					if (incrementDim == null) {
						incrementDim = new Dimension(Math.min(realBounds.width / 3, realBounds.height), realBounds.height);
					}
					
					rect.setSize(incrementDim);
					rect.setLocation(realBounds.x, Math.round(realBounds.y + ((realBounds.height - incrementDim.height) / 2f)));
				}
				else {
					if (incrementDim == null) {
						incrementDim = new Dimension(realBounds.width, Math.min(realBounds.height / 3, realBounds.width));
					}
					
					rect.setSize(incrementDim);
					rect.setLocation(Math.round(realBounds.x + ((realBounds.width - incrementDim.width) / 2f)), realBounds.y);
				}
			}
			else {
				Dimension decrementDim = getDimensions(ScrollbarDrawable.IMAGE_DECREMENT);
				
				if (scrollbarType == HORIZONTAL) {
					if (decrementDim == null) {
						decrementDim = new Dimension(Math.min(realBounds.width / 3, realBounds.height), realBounds.height);
					}
					
					rect.setSize(decrementDim);
					rect.setLocation(realBounds.x + realBounds.width - decrementDim.width, Math.round(realBounds.y + ((realBounds.height - decrementDim.height) / 2f)));
				}
				else {
					if (decrementDim == null) {
						decrementDim = new Dimension(realBounds.width, Math.min(realBounds.height / 3, realBounds.width));
					}
					
					rect.setSize(decrementDim);
					rect.setLocation(Math.round(realBounds.x + ((realBounds.width - decrementDim.width) / 2f)), realBounds.y + realBounds.height - decrementDim.height);
				}	
			}
			
			return rect;
		}
	}
	
	protected class ThumbWinButton extends WinButton {
		private int realX;
		private int realY;
		private int value;
		
		public ThumbWinButton() {
			super();
		}

		@Override
		public int getFlags() {
			return WinScrollbar.this.getFlags();
		}
		
		@Override
		public boolean isMoveable() {
			// nothing else, the thumb can still be moved even when the WinSlider is not enabled
			return WinScrollbar.this.viewer.isPreview();
		}
		
		@Override
		public void setBounds(Rectangle rect) {
			realX = rect.x;
			realY = rect.y;
			super.setBounds(rect);
		}
		
		@Override
		public void setState(int state) {
			int oldState = this.state;
			this.state = state;

			if ((state & STATE_CLICK) != STATE_CLICK && (oldState & STATE_CLICK) != STATE_CLICK) {
				realX = bounds.x;
				realY = bounds.y;
			}
			
			if (state != oldState && WinScrollbar.this.viewer != null && WinScrollbar.this.viewer.isPreview()) {
				WinScrollbar.this.viewer.repaint();
			}
		}
		
		private void verticalTranslate(int dy, Rectangle incrementBounds, Rectangle decrementBounds) {
			bounds.translate(0, dy);
			if (bounds.y < incrementBounds.height) {
				bounds.y = incrementBounds.height;
				value = minValue;
			}
			else if (bounds.y > (WinScrollbar.this.realBounds.height - decrementBounds.height - bounds.height)) {
				bounds.y = WinScrollbar.this.realBounds.height - decrementBounds.height - bounds.height;
				value = maxValue;
			}
			else {
				setValue(Math.round(
						(bounds.y - incrementBounds.height) / (float) (WinScrollbar.this.realBounds.height - decrementBounds.height - incrementBounds.height - bounds.height) 
						* (maxValue - minValue) + minValue));
			}
			
			WinScrollbar.this.viewer.repaint();
		}
		
		private void horizontalTranslate(int dx, Rectangle incrementBounds, Rectangle decrementBounds) {
			bounds.translate(dx, 0);
			if (bounds.x < incrementBounds.width) {
				bounds.x = incrementBounds.width;
				value = minValue;
			}
			else if (bounds.x > (WinScrollbar.this.realBounds.width - decrementBounds.width - bounds.width)) {
				bounds.x = WinScrollbar.this.realBounds.width - decrementBounds.width - bounds.width;
				value = maxValue;
			}
			else {
				setValue(Math.round(
						(bounds.x - incrementBounds.width) / (float) (WinScrollbar.this.realBounds.width - decrementBounds.width - incrementBounds.width - bounds.width) 
						* (maxValue - minValue) + minValue));
			}
			
			WinScrollbar.this.viewer.repaint();
		}
		
		@Override
		public void translate(int dx, int dy) {
			Rectangle incrementBounds = btnIncrement.getRealBounds();
			Rectangle decrementBounds = btnDecrement.getRealBounds();
			
			if (scrollbarType == HORIZONTAL) {
				if (dx < 0 && realX < (WinScrollbar.this.realBounds.width - bounds.width)) {
					horizontalTranslate(dx, incrementBounds, decrementBounds);
				}
				else if (dx > 0 && realX > 0) {
					horizontalTranslate(dx, incrementBounds, decrementBounds);
				}
				realX += dx;
			}
			else {
				if (dy < 0 && realY < (WinScrollbar.this.realBounds.height - bounds.height)) {
					verticalTranslate(dy, incrementBounds, decrementBounds);
				}
				else if (dy > 0 && realY > 0) {
					verticalTranslate(dy, incrementBounds, decrementBounds);
				}
				realY += dy;
			}
		}
		
		public void setValue(int value) {
			this.value = value;
			
			Dimension thumbDim = getDimensions(ScrollbarDrawable.IMAGE_THUMB);
			if (thumbDim == null) {
				thumbDim = WinScrollbar.this.realBounds.getSize();
			}
			
			float pos = (value - minValue) / (float) (maxValue - minValue);
			float proportion = (thumbValueLength - minValue) / (float) (maxValue - minValue);
			
			Rectangle incrementBounds = btnIncrement.getRealBounds();
			Rectangle decrementBounds = btnDecrement.getRealBounds();
			
			if (WinScrollbar.this.scrollbarType == HORIZONTAL) {
				float yOffset = (WinScrollbar.this.realBounds.height - thumbDim.height) / 2.0f;
				
				bounds.width = Math.round(proportion * (WinScrollbar.this.bounds.width - incrementBounds.width - decrementBounds.width));
				
				bounds.setBounds(new Rectangle(
						(int) Math.round(incrementBounds.width + pos * (WinScrollbar.this.realBounds.width - thumbDim.width - incrementBounds.width - decrementBounds.width)),
						(int) Math.round(yOffset),
						bounds.width,
						thumbDim.height
						));
			}
			else {
				float xOffset = (WinScrollbar.this.realBounds.width - thumbDim.width) / 2.0f;
				
				bounds.height = Math.round(proportion * (WinScrollbar.this.bounds.height - incrementBounds.height - decrementBounds.height));
				
				bounds.setBounds(new Rectangle(
						(int) Math.round(xOffset),
						(int) Math.round(incrementBounds.height + pos * (WinScrollbar.this.bounds.height - bounds.height - incrementBounds.height - decrementBounds.height)),
						thumbDim.width,
						bounds.height
						));
			}
			
		}
		
		@Override
		public Rectangle getRealBounds() {
			setValue(value);
			
			Rectangle rect = new Rectangle(bounds);
			rect.x += WinScrollbar.this.realBounds.x;
			rect.y += WinScrollbar.this.realBounds.y;
			return rect;
		}
	}
	
	
	public static final int TYPE = 0x8EF37D6D;
	
	public static final int FLAG_ISEDITABLE = 0x8;
	
	private final ThumbWinButton btnThumb = new ThumbWinButton();
	private final ScrollbarButton btnIncrement = new ScrollbarButton(ScrollbarButton.BUTTON_INCREMENT);
	private final ScrollbarButton btnDecrement = new ScrollbarButton(ScrollbarButton.BUTTON_DECREMENT);
	
	private int scrollbarType;
	private int initialValue;
	private int minValue;
	private int maxValue;
	private int thumbValueLength;
	
	public WinScrollbar(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		initialValue = SectionInt2.getValues(block.getSection(0xEEC1C001, SectionInt2.class), new int[] {0}, 1)[0];
		minValue = SectionInt2.getValues(block.getSection(0xEEC1C002, SectionInt2.class), new int[] {0}, 1)[0];
		maxValue = SectionInt2.getValues(block.getSection(0xEEC1C003, SectionInt2.class), new int[] {256}, 1)[0];
		thumbValueLength = SectionInt2.getValues(block.getSection(0xEEC1C004, SectionInt2.class), new int[] {64}, 1)[0];
		
		scrollbarType = SectionInt.getValues(block, 0xEEC1C007, new int[] {VERTICAL}, 1)[0];
		
		addUnassignedInt2(block, 0xEEC1C005, 1);
		addUnassignedInt2(block, 0xEEC1C006, 8);
		addUnassignedInt2(block, 0xEEC1C008, 8);
		
		if (!(drawable instanceof ScrollbarDrawable)) {
			throw new InvalidBlockException("WinScrollbar drawable must be of the type ScrollbarDrawable");
		}
		
		btnThumb.setValue(initialValue);
	}
	
	public WinScrollbar(SPUIViewer viewer) {
		super(viewer);
		
		scrollbarType = VERTICAL;
		initialValue = 0;
		minValue = 0;
		maxValue = 256;
		thumbValueLength = 64;
		
		unassignedProperties.put(0xEEC1C005, (int) 1);
		unassignedProperties.put(0xEEC1C006, (int) 8);
		unassignedProperties.put(0xEEC1C008, (int) 8);
		
		flags |= FLAG_ISEDITABLE;
		
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		builder.addInt(block, 0xEEC1C007, new int[] {scrollbarType});
		builder.addInt2(block, 0xEEC1C001, new int[] {initialValue});
		builder.addInt2(block, 0xEEC1C002, new int[] {minValue});
		builder.addInt2(block, 0xEEC1C003, new int[] {maxValue});
		builder.addInt2(block, 0xEEC1C004, new int[] {thumbValueLength});
		
		saveInt2(builder, block, 0xEEC1C005);
		saveInt2(builder, block, 0xEEC1C006);
		saveInt2(builder, block, 0xEEC1C008);
		
		return block;
	}
	
	private Dimension getDimensions(int index) {
		if (drawable == null) {
			return null;
		}
		else {
			return drawable.getDimensions(index);
		}
	}
	
	public WinScrollbar(ScrollbarDrawable scrollbarDrawable, int minValue, int maxValue, int initialValue, int thumbValueLength) {
		super();
		
		this.drawable = scrollbarDrawable;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initialValue = initialValue;
		this.thumbValueLength = thumbValueLength;
	}

	private WinScrollbar() {
		super();
	}
	
	@Override
	public WinScrollbar copyComponent(boolean propagateIndependent) {
		WinScrollbar other = new WinScrollbar();
		super.copyComponent(other, propagateIndependent);

		other.minValue = minValue;
		other.maxValue = maxValue;
		other.initialValue = initialValue;
		other.scrollbarType = scrollbarType;
		other.thumbValueLength = thumbValueLength;
		
		other.btnThumb.setParent(other);
		other.btnThumb.setValue(initialValue);
		
		return other;
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}

	public ThumbWinButton getThumbButton() {
		return btnThumb;
	}
	
	public ScrollbarButton getIncrementButton() {
		return btnIncrement;
	}
	
	public ScrollbarButton getDecrementButton() {
		return btnDecrement;
	}
	
	public int getScrollbarType() {
		return scrollbarType;
	}
	
	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
		btnThumb.setValue(initialValue);
	}
	
	public void setMinValue(int minValue) {
		this.minValue = minValue;
		btnThumb.setValue(initialValue);
	}
	
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		btnThumb.setValue(initialValue);
	}
	
	public void setThumbValueLength(int thumbValueLength) {
		this.thumbValueLength = thumbValueLength;
		btnThumb.setValue(initialValue);
	}
	
	@Override
	public WinComponent getComponentInCoords(Point p) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) {
				if (realBounds.contains(p)) {
					return this;
				}
				else {
					if (btnThumb.getRealBounds().contains(p)) {
						return btnThumb;
					}
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return super.getComponentInCoords(p);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) {
				if (type.isInstance(btnIncrement) && btnIncrement.getRealBounds().contains(p)) {
					return (T) btnIncrement;
				}
				else if (type.isInstance(btnDecrement) && btnDecrement.getRealBounds().contains(p)) {
					return (T) btnDecrement;
				}
				else if (type.isInstance(btnThumb) && btnThumb.getRealBounds().contains(p)) {
					return (T) btnThumb;
				}
				else if (realBounds.contains(p) && type.isInstance(this)) {
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
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xEEC1C001: return initialValue;
				case 0xEEC1C002: return minValue;
				case 0xEEC1C003: return maxValue;
				case 0xEEC1C004: return thumbValueLength;
				case 0xEEC1C007: return scrollbarType;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC1C001: initialValue = (int) value; break;
				case 0xEEC1C002: minValue = (int) value; break;
				case 0xEEC1C003: maxValue = (int) value; break;
				case 0xEEC1C004: thumbValueLength = (int) value; break;
				case 0xEEC1C007: scrollbarType = (int) value; break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
}
