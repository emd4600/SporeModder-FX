package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorTimeFunctionSmoothRamp extends cSPUIBehaviorTimeFunction {

	public static final int TYPE = 0x0253ADD9;
	
	public cSPUIBehaviorTimeFunctionSmoothRamp(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedFloat(block, 0x0253AD8C, 0);
		addUnassignedFloat(block, 0x0253AD8D, 0);
		
	}
	
	public cSPUIBehaviorTimeFunctionSmoothRamp(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0253AD8C, (float) 0);
		unassignedProperties.put(0x0253AD8D, (float) 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x0253AD8C);
		saveFloat(builder, block, 0x0253AD8D);
		
		return block;
	}
	
	private cSPUIBehaviorTimeFunctionSmoothRamp() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorTimeFunctionSmoothRamp other = new cSPUIBehaviorTimeFunctionSmoothRamp();
		copyComponent(other, propagate);
		return other;
	}

}
