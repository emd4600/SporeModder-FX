package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUIProgressBarWin extends Window {
	
	public static final int TYPE = 0x010EDE03;

	public cSPUIProgressBarWin(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}

	private cSPUIProgressBarWin() {
		super();
	}
	
	public cSPUIProgressBarWin(SPUIViewer viewer) {
		super(viewer);
	}
	
	@Override
	public cSPUIProgressBarWin copyComponent(boolean propagateIndependent) {
		cSPUIProgressBarWin other = new cSPUIProgressBarWin();
		copyComponent(other, propagateIndependent);
		
		return other;
	}

}
