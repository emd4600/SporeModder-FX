package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUIPopupMenuItemWin extends WinButton {
	
	public static final int TYPE = 0x04C04B4E;

	public cSPUIPopupMenuItemWin(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}
	
	public cSPUIPopupMenuItemWin(SPUIViewer viewer) {
		super(viewer);
	}

	private cSPUIPopupMenuItemWin() {
		super();
	}

	public cSPUIPopupMenuItemWin copyComponent(boolean propagateIndependent) {
		cSPUIPopupMenuItemWin other = new cSPUIPopupMenuItemWin();
		copyComponent(other, propagateIndependent);
		return other;
	}

}
