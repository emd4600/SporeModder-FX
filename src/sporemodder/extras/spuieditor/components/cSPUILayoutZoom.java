package sporemodder.extras.spuieditor.components;

import java.awt.Rectangle;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUILayoutZoom extends SPUIDefaultWinProc {

	public static final int TYPE = 0x04EA92A3;
	
	public cSPUILayoutZoom(SPUIBlock block) throws InvalidBlockException {
		super(block);
	}
	
	protected cSPUILayoutZoom() {
		super();
	}
	
	public cSPUILayoutZoom(SPUIViewer viewer) {
		super(viewer);
	}
	
	@Override
	public cSPUILayoutZoom copyComponent(boolean propagate) {
		cSPUILayoutZoom other = new cSPUILayoutZoom();
		copyComponent(other, propagate);
		return other;
	}

	@Override
	public void modify(WinComponent component) {
		if (component.getParent() == null) {
			return;
		}
		
		// Is this component only used from the code??
		float[] values = new float[] {0.0f, 0.0f, 1.0f, 1.0f};
		Rectangle result = component.getRealBounds();
		Rectangle parentBounds = component.getParent().getRealBounds();
		
		result.x = (int) (parentBounds.width * values[0]);
		result.y = (int) (parentBounds.height * values[1]);
		result.width = (int) (parentBounds.width * values[2] - parentBounds.width * values[0]);
		result.height = (int) (parentBounds.height * values[3] - parentBounds.height * values[1]);
	}
}
