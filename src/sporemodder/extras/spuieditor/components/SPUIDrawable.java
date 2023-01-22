package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public interface SPUIDrawable extends SPUIComponent {
	
	public static final String INTERFACE_NAME = "IDrawable";
	public static final String BUTTON_INTERFACE_NAME = "IButtonDrawable";
	
	public static final int IMAGE_MAIN = -1;
	
	public static final int[] DrawableTypes = new int[] {
			ButtonDrawableStandard.TYPE,
			ButtonDrawableRadio.TYPE,
			ComboBoxDrawable.TYPE,
			DialogDrawable.TYPE,
			FrameDrawable.TYPE,
			ImageDrawable.TYPE,
			ScrollbarDrawable.TYPE,
			SliderDrawable.TYPE,
			SpinnerDrawable.TYPE,
			StdDrawable.TYPE,
			/* TreeExpanderDrawable */		0xF02C7C44,
			cSPUIStdDrawable.TYPE,
			cSPUIVariableWidthDrawable.TYPE
	};
	
	@Override
	public SPUIDrawable copyComponent(boolean propagate);

	public void draw(Graphics2D graphics, Rectangle bounds, WinComponent component);
	
	public Dimension getDimensions(int imageIndex);
	
	public boolean isValidPoint(Point p, Rectangle bounds);
	
}
