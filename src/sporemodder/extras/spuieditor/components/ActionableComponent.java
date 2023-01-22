package sporemodder.extras.spuieditor.components;

import java.awt.Rectangle;

public interface ActionableComponent {
	
	public static final int STATE_SELECTED = 0x10;  // just a placeholder, don't know what Spore uses
	public static final int STATE_HOVER = 0x8;
	public static final int STATE_CLICK = 0x2;

	public void setState(int state);
	public int getState();
	public Rectangle getRealBounds();
}
