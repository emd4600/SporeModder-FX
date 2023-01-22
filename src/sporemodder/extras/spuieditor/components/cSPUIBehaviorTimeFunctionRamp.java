package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class cSPUIBehaviorTimeFunctionRamp extends cSPUIBehaviorTimeFunction {

	public static final int TYPE = 0x0253ACD2;
	
	public cSPUIBehaviorTimeFunctionRamp(SPUIBlock block) throws InvalidBlockException {
		super(block);
	}
	
	public cSPUIBehaviorTimeFunctionRamp(SPUIViewer viewer) {
		super(viewer);
	}
	
	private cSPUIBehaviorTimeFunctionRamp() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorTimeFunctionRamp other = new cSPUIBehaviorTimeFunctionRamp();
		copyComponent(other, propagate);
		return other;
	}
}
