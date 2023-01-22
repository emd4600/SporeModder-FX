package sporemodder.extras.spuieditor.components;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class ButtonDrawableRadio extends ButtonDrawableStandard {
	
	private static final int STATE_INDEX_IDLE = 0;
	private static final int STATE_INDEX_HOVER = 1;
	private static final int STATE_INDEX_ONCLICK = 2;
	
	private static final int STATE_SELECTED = 3;
	
	public static final int TYPE = 0x2F3ADC5D;

	public ButtonDrawableRadio(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}
	
	public ButtonDrawableRadio(SPUIViewer viewer) {
		super(viewer);
	}

	private ButtonDrawableRadio() {
		super();
	}

	@Override
	public ButtonDrawableRadio copyComponent(boolean propagateIndependent) {
		ButtonDrawableRadio other = new ButtonDrawableRadio();
		copyComponent(other, propagateIndependent);
		return other;
	}
	
	@Override
	protected int[] getTransformedUVs(WinComponent component) {
		
		int index = STATE_INDEX_IDLE;
		if ((component.getFlags() & WinComponent.FLAG_ENABLED) == WinComponent.FLAG_ENABLED) {
			if (viewer.isPreview()) {
				int state = component.isActionableComponent() ? ((ActionableComponent) component).getState() : 0;
				
				if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK) {
					index = STATE_INDEX_ONCLICK;
				}
				else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
					index = STATE_INDEX_HOVER;
				}
				else {
					index = STATE_INDEX_IDLE;
				}
				
				if ((state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					index += STATE_SELECTED;
				}
			}
			else {
				index = STATE_INDEX_IDLE;
			}
		}
		
		float[] uvCoordinates = image.getUVCoords();
		BufferedImage bufferedImage = image.getBufferedImage();
		
		float tileSize = (uvCoordinates[2] - uvCoordinates[0]) / 6.0f;
		
		int[] imageUVCoords = new int[4];
		imageUVCoords[0] = (int) Math.round((uvCoordinates[0] + tileSize * index) * bufferedImage.getWidth());
		imageUVCoords[1] = (int) Math.round(uvCoordinates[1] * bufferedImage.getHeight()); 
		imageUVCoords[2] = (int) Math.round((uvCoordinates[0] + tileSize * index + tileSize) * bufferedImage.getWidth());
		imageUVCoords[3] = (int) Math.round(uvCoordinates[3] * bufferedImage.getHeight());
		
		return imageUVCoords;
	}
	
	@Override
	public Dimension getDimensions(int imageIndex) {
		Dimension dim = image == null ? null : new Dimension(image.getDimensions());
		if (dim != null) {
			dim.width /= 6; 
		}
		return dim;
	}
}
