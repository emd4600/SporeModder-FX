package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIBehaviorWinBoolStateEvent extends cSPUIBehaviorWinEventBase {
	
	public static final int TYPE = 0x025611ED;

	public cSPUIBehaviorWinBoolStateEvent(SPUIBlock block) throws InvalidBlockException {
		super(block);

		addUnassignedInt(block, 0x03339952, 0);
	}
	
	public cSPUIBehaviorWinBoolStateEvent(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x03339952, (int) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveInt(builder, block, 0x03339952);
		
		return block;
	}

	private cSPUIBehaviorWinBoolStateEvent() {
		super();
	}
	
	@Override
	public cSPUIBehaviorWinBoolStateEvent copyComponent(boolean propagateIndependent) {
		cSPUIBehaviorWinBoolStateEvent other = new cSPUIBehaviorWinBoolStateEvent();
		copyComponent(other, propagateIndependent);
		return other;
	}
}
