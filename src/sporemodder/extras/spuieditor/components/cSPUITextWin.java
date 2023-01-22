package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUITextWin extends WinText {
	
	public static final int TYPE = 0x039A721C;

	public cSPUITextWin(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}
	
	public cSPUITextWin(SPUIViewer viewer) {
		super(viewer);
	}

	protected cSPUITextWin() {
		super();
	}
	
	public cSPUITextWin copyComponent(boolean propagateIndependent) {
		cSPUITextWin other = new cSPUITextWin();
		super.copyComponent(other, propagateIndependent);
		return other;
	}
}
