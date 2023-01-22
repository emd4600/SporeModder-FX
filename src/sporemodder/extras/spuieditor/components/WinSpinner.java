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
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt2;

public class WinSpinner extends Window {
	
	public static final int TYPE = 0x4F063BB3;
	
	public static final int FLAG_ISEDITABLE = 0x8;
	
	protected class SpinnerButton implements ActionableComponent {
		private static final int TYPE_ARROWUP = 0;
		private static final int TYPE_ARROWDOWN = 1;
		
		private int state;
		private int type;
		
		private SpinnerButton(int type) {
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
			Dimension dim = null;
			if (drawable instanceof SpinnerDrawable) {
				dim = ((SpinnerDrawable) drawable).getDimensions(
						type == TYPE_ARROWUP ? SpinnerDrawable.IMAGE_ARROWUP : SpinnerDrawable.IMAGE_ARROWDOWN);
			}
			
			if (dim == null) {
				dim = new Dimension(16, 16);
			}
			
			Rectangle rect = new Rectangle(dim.width, dim.height);
			
			if (orientation == HORIZONTAL) {
				rect.y = realBounds.y + (realBounds.height - rect.height) / 2;
				
				if (type == TYPE_ARROWUP) {
					rect.x = realBounds.x;
				}
				else {
					rect.x = realBounds.x + realBounds.width - rect.width;
				}
			}
			else {
				rect.x = realBounds.x + (realBounds.width - rect.width) / 2;
				
				if (type == TYPE_ARROWUP) {
					rect.y = realBounds.y;
				}
				else {
					rect.y = realBounds.y + realBounds.height - rect.height;
				}
			}
			
			return rect;
		}
		
	}
	
	private final SpinnerButton btnArrowUp = new SpinnerButton(SpinnerButton.TYPE_ARROWUP);
	private final SpinnerButton btnArrowDown = new SpinnerButton(SpinnerButton.TYPE_ARROWDOWN);
	private int minValue;
	private int maxValue;
	private int initialValue;
	private int valueDelta;
	private int orientation;
	private int valuePeriod;
	
	public WinSpinner(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		addUnassignedInt(block, 0xEEC1F003, 0);
		addUnassignedInt(block, 0xEEC1F006, 0);
		
		orientation = getIntProperty(block, 0xEEC1F006, VERTICAL);
		
		initialValue = SectionInt2.getValues(block.getSection(0xEEC1F001, SectionInt2.class), new int[] {0}, 1)[0];
		valueDelta = SectionInt2.getValues(block.getSection(0xEEC1F002, SectionInt2.class), new int[] {1}, 1)[0];
		minValue = SectionInt2.getValues(block.getSection(0xEEC1F004, SectionInt2.class), new int[] {0}, 1)[0];
		maxValue = SectionInt2.getValues(block.getSection(0xEEC1F005, SectionInt2.class), new int[] {10}, 1)[0];
		
		valuePeriod = getIntProperty(block, 0xEEC1F003, 100);
	}
	
	public WinSpinner(SPUIViewer viewer) {
		super(viewer);
		
		minValue = 0;
		maxValue = 10;
		valueDelta = 1;
		valuePeriod = 100;
		initialValue = 0;
		orientation = VERTICAL;
		
		flags |= FLAG_ISEDITABLE;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addInt(block, 0xEEC1F006, new int[] {orientation});
		builder.addInt2(block, 0xEEC1F001, new int[] {initialValue});
		builder.addInt2(block, 0xEEC1F004, new int[] {minValue});
		builder.addInt2(block, 0xEEC1F005, new int[] {maxValue});
		builder.addInt2(block, 0xEEC1F002, new int[] {valueDelta});
		builder.addInt(block, 0xEEC1F003, new int[] {valuePeriod});
		
		return block;
	}
	
	private WinSpinner() {
		super();
	}
	
	@Override
	public WinSpinner copyComponent(boolean propagateIndependent) {
		WinSpinner other = new WinSpinner();
		super.copyComponent(other, propagateIndependent);

		other.minValue = minValue;
		other.maxValue = maxValue;
		other.initialValue = initialValue;
		other.valueDelta = valueDelta;
		other.valuePeriod = valuePeriod;
		other.orientation = orientation;
		
		return other;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type) {
		if (viewer != null && viewer.isPreview()) {
			if ((flags & FLAG_VISIBLE) == FLAG_VISIBLE) 
			{
				if (type.isInstance(btnArrowUp) && btnArrowUp.getRealBounds().contains(p)) {
					return (T) btnArrowUp;
				}
				else if (type.isInstance(btnArrowDown) && btnArrowDown.getRealBounds().contains(p)) {
					return (T) btnArrowDown;
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
	
	public SpinnerButton getArrowUpButton() {
		return btnArrowUp;
	}
	
	public SpinnerButton getArrowDownButton() {
		return btnArrowDown;
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0xEEC1F001: return initialValue;
				case 0xEEC1F002: return valueDelta;
				case 0xEEC1F003: return valuePeriod;
				case 0xEEC1F004: return minValue;
				case 0xEEC1F005: return maxValue;
				case 0xEEC1F006: return orientation;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0xEEC1F001: initialValue = (int) value; break;
				case 0xEEC1F002: valueDelta = (int) value; break;
				case 0xEEC1F003: valuePeriod = (int) value; break;
				case 0xEEC1F004: minValue = (int) value; break;
				case 0xEEC1F005: maxValue = (int) value; break;
				case 0xEEC1F006: orientation = (int) value; break;
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
