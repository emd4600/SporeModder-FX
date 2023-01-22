package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public abstract class cSPUIBehaviorTimeFunction extends SPUIDefaultComponent {

	public cSPUIBehaviorTimeFunction(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedBoolean(block, 0x0253AD19, false);
		addUnassignedFloat(block, 0x0253ACED, 0);
		addUnassignedFloat(block, 0x0269A849, 0);
		addUnassignedFloat(block, 0x0269A84B, 0);
	}
	
	public cSPUIBehaviorTimeFunction(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0253AD19, (boolean) false);
		unassignedProperties.put(0x0253ACED, (float) 0);
		unassignedProperties.put(0x0269A849, (float) 0);
		unassignedProperties.put(0x0269A84B, (float) 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveBoolean(builder, block, 0x0253AD19);
		saveFloat(builder, block, 0x0253ACED);
		saveFloat(builder, block, 0x0269A849);
		saveFloat(builder, block, 0x0269A84B);
		
		return block;
	}
	
	protected cSPUIBehaviorTimeFunction() {
		super();
	}
	
}
