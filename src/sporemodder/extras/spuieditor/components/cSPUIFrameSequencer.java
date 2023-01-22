package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUIFrameSequencer extends SPUIDefaultComponent {

	public static final int TYPE = 0x0106F61E;
	
	public cSPUIFrameSequencer(SPUIBlock block) throws InvalidBlockException {
		super(block);
	}
	
	public cSPUIFrameSequencer(SPUIViewer viewer) {
		super(viewer);
	}
	
	protected cSPUIFrameSequencer() {
		super();
	}
	
	@Override
	public cSPUIFrameSequencer copyComponent(boolean propagate) {
		cSPUIFrameSequencer other = new cSPUIFrameSequencer();
		copyComponent(other, propagate);
		return other;
	}

}
