package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorTimeFunctionDampedPeriodic extends cSPUIBehaviorTimeFunction {

	public static final int TYPE = 0x0269A661;
	
	public cSPUIBehaviorTimeFunctionDampedPeriodic(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedInt(block, 0x0269B186, 0);
		addUnassignedFloat(block, 0x0269A916, 0);
		addUnassignedFloat(block, 0x0269A919, 0);
		addUnassignedFloat(block, 0x0269A91B, 0);
		addUnassignedBoolean(block, 0x026A02AB, false);
	}
	
	public cSPUIBehaviorTimeFunctionDampedPeriodic(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0269B186, (int) 0);
		unassignedProperties.put(0x0269A916, (float) 0);
		unassignedProperties.put(0x0269A919, (float) 0);
		unassignedProperties.put(0x0269A91B, (float) 0);
		unassignedProperties.put(0x026A02AB, (boolean) false);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveInt(builder, block, 0x0269B186);
		saveFloat(builder, block, 0x0269A916);
		saveFloat(builder, block, 0x0269A919);
		saveFloat(builder, block, 0x0269A91B);
		saveBoolean(builder, block, 0x026A02AB);
		
		return block;
	}
	
	private cSPUIBehaviorTimeFunctionDampedPeriodic() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorTimeFunctionDampedPeriodic other = new cSPUIBehaviorTimeFunctionDampedPeriodic();
		copyComponent(other, propagate);
		return other;
	}

}
