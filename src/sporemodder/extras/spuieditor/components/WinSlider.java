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

public class WinSlider extends Window {
	
	public static final int SLIDER_HORIZONTAL = 1;
	public static final int SLIDER_VERTICAL = 2;  // it also works for 0 (and probably any other value)
	
	private class ThumbWinButton extends WinButton {
		private int realX;
		private int realY;
		private int value;
		
		public ThumbWinButton() {
			super();
		}

		@Override
		public int getFlags() {
			return WinSlider.this.getFlags();
		}
		
		@Override
		public boolean isMoveable() {
			// nothing else, the thumb can still be moved even when the WinSlider is not enabled
			return WinSlider.this.viewer.isPreview();
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
			
			if (state != oldState && WinSlider.this.viewer != null && WinSlider.this.viewer.isPreview()) {
				WinSlider.this.viewer.repaint();
			}
		}
		
		private void verticalTranslate(int dy) {
			bounds.translate(0, dy);
			if (bounds.y < 0) {
				bounds.y = 0;
				value = maxValue;
			}
			else if (bounds.y > (WinSlider.this.realBounds.height - bounds.height)) {
				bounds.y = WinSlider.this.realBounds.height - bounds.height;
				value = minValue;
			}
			else {
				setValue(Math.round((1 - bounds.y / (float) (WinSlider.this.realBounds.height - bounds.height)) * (maxValue - minValue) + minValue));
			}
			
			WinSlider.this.viewer.repaint();
		}
		
		private void horizontalTranslate(int dx) {
			bounds.translate(dx, 0);
			if (bounds.x < 0) {
				bounds.x = 0;
				value = minValue;
			}
			else if (bounds.x > (WinSlider.this.realBounds.width - bounds.width)) {
				bounds.x = WinSlider.this.realBounds.width - bounds.width;
				value = maxValue;
			}
			else {
				setValue(Math.round(bounds.x / (float) (WinSlider.this.realBounds.width - bounds.width) * (maxValue - minValue) + minValue));
			}
			
			WinSlider.this.viewer.repaint();
		}
		
		@Override
		public void translate(int dx, int dy) {
			if (sliderType == SLIDER_HORIZONTAL) {
				if (dx < 0 && realX < (WinSlider.this.realBounds.width - bounds.width)) {
					horizontalTranslate(dx);
				}
				else if (dx > 0 && realX > 0) {
					horizontalTranslate(dx);
				}
				realX += dx;
			}
			else {
				if (dy < 0 && realY < (WinSlider.this.realBounds.height - bounds.height)) {
					verticalTranslate(dy);
				}
				else if (dy > 0 && realY > 0) {
					verticalTranslate(dy);
				}
				realY += dy;
			}
		}
		
		public void setValue(int value) {
			this.value = value;
			
			Dimension thumbDim = null; 
			if (WinSlider.this.drawable != null) {
				thumbDim = ((SliderDrawable) WinSlider.this.drawable).getThumbDimensions();
			}
			
			float pos = (value - minValue) / (float) (maxValue - minValue);
			
			if (WinSlider.this.sliderType == SLIDER_HORIZONTAL) {
				if (thumbDim == null) {
					thumbDim = new Dimension(24, WinSlider.this.realBounds.height);
				}
				
				float yOffset = (WinSlider.this.realBounds.height - thumbDim.height) / 2.0f;
				
				bounds.setBounds(new Rectangle(
						(int) Math.round(pos * (WinSlider.this.realBounds.width - thumbDim.width)),
						(int) Math.round(yOffset),
						thumbDim.width,
						thumbDim.height
						));
			}
			else {
				if (thumbDim == null) {
					thumbDim = new Dimension(WinSlider.this.realBounds.width, 24);
				}
				
				float xOffset = (WinSlider.this.realBounds.width - thumbDim.width) / 2.0f;
				
				bounds.setBounds(new Rectangle(
						(int) Math.round(xOffset),
						(int) Math.round((1 - pos) * (WinSlider.this.realBounds.height - thumbDim.height)),
						thumbDim.width,
						thumbDim.height
						));
			}
			
		}
		
		@Override
		public Rectangle getRealBounds() {
			setValue(value);
			
			Rectangle rect = new Rectangle(bounds);
			rect.x += WinSlider.this.realBounds.x;
			rect.y += WinSlider.this.realBounds.y;
			return rect;
		}
	}
	
	public static final int TYPE = 0x2F00BDB3;
	
	public static final int FLAG_ISEDITABLE = 0x8;
	
	private final ThumbWinButton btnThumb = new ThumbWinButton();
	private int minValue;
	private int maxValue;
	private int initialValue;
	private int sliderType;

	public WinSlider(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		initialValue = SectionInt2.getValues(block.getSection(0xEEC1E001, SectionInt2.class), new int[] {0}, 1)[0];
		minValue = SectionInt2.getValues(block.getSection(0xEEC1E002, SectionInt2.class), new int[] {0}, 1)[0];
		maxValue = SectionInt2.getValues(block.getSection(0xEEC1E003, SectionInt2.class), new int[] {1000}, 1)[0];
		
		sliderType = SectionInt.getValues(block, 0xEEC1E004, new int[] {SLIDER_VERTICAL}, 1)[0];
		
		if (!(drawable instanceof SliderDrawable)) {
			throw new InvalidBlockException("WinSlider drawable must be of the type SliderDrawable");
		}
		
		btnThumb.setValue(initialValue);
	}
	
	public WinSlider(SPUIViewer viewer) {
		super(viewer);
		
		sliderType = SLIDER_VERTICAL;
		initialValue = 0;
		minValue = 0;
		maxValue = 1000;
		
		btnThumb.setValue(initialValue);
		
		flags |= FLAG_ISEDITABLE;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addInt2(block, 0xEEC1E001, new int[] {initialValue});
		builder.addInt2(block, 0xEEC1E002, new int[] {minValue});
		builder.addInt2(block, 0xEEC1E003, new int[] {maxValue});
		builder.addInt(block, 0xEEC1E004, new int[] {sliderType});
		
		return block;
	}

	private WinSlider() {
		super();
	}
	
	@Override
	public WinSlider copyComponent(boolean propagateIndependent) {
		WinSlider other = new WinSlider();
		super.copyComponent(other, propagateIndependent);

		other.minValue = minValue;
		other.maxValue = maxValue;
		other.initialValue = initialValue;
		other.sliderType = sliderType;
		other.btnThumb.setParent(other);
		other.btnThumb.setValue(initialValue);
		
		return other;
	}
	
	public WinButton getThumbButton() {
		return btnThumb;
	}
	
	public int getSliderType() {
		return sliderType;
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
					else {
						return null;
					}
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
				if (type.isInstance(btnThumb) && btnThumb.getRealBounds().contains(p)) {
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
				
				case 0xEEC1E001: return initialValue;
				case 0xEEC1E002: return minValue;
				case 0xEEC1E003: return maxValue;
				case 0xEEC1E004: return sliderType;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC1E001: initialValue = (int) value; break;
				case 0xEEC1E002: minValue = (int) value; break;
				case 0xEEC1E003: maxValue = (int) value; break;
				case 0xEEC1E004: sliderType = (int) value; break;
				}
				
				viewer.repaint();
				
				super.setValue(property, value, index);
			}
		};
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
