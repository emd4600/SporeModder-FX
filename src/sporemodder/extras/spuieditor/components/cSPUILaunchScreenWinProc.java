package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUILaunchScreenWinProc extends SPUIDefaultWinProc {

	public static final int TYPE = 0x0279BF9E;
	
	public cSPUILaunchScreenWinProc(SPUIBlock block) throws InvalidBlockException {
		super(block);
	}
	
	public cSPUILaunchScreenWinProc(SPUIViewer viewer) {
		super(viewer);
	}
	
	protected cSPUILaunchScreenWinProc() {
		super();
	}
	
	@Override
	public cSPUILaunchScreenWinProc copyComponent(boolean propagate) {
		cSPUILaunchScreenWinProc other = new cSPUILaunchScreenWinProc();
		copyComponent(other, propagate);
		return other;
	}
}
