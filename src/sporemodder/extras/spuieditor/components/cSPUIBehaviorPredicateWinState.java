package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorPredicateWinState extends SPUIDefaultComponent {

	public static final int TYPE = 0x033C9C5B;
	
	public cSPUIBehaviorPredicateWinState(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedBoolean(block, 0x02521AFB, false);
		addUnassignedInt(block, 0x02521AFA, 0);
		addUnassignedInt(block, 0x033CDC1B, 0);
		addUnassignedIntName(block, 0x033CDCF4, null);
		addUnassignedIntName(block, 0x033CDC22, null);
		addUnassignedInt(block, 0x0341EF60, 0);
	}
	
	public cSPUIBehaviorPredicateWinState(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x02521AFB, (boolean) false);
		unassignedProperties.put(0x02521AFA, (int) 0);
		unassignedProperties.put(0x033CDC1B, (int) 0);
		unassignedProperties.put(0x033CDCF4, null);
		unassignedProperties.put(0x033CDC22, null);
		unassignedProperties.put(0x0341EF60, (int) 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveBoolean(builder, block, 0x02521AFB);
		saveInt(builder, block, 0x02521AFA);
		saveInt(builder, block, 0x033CDC1B);
		saveIntName(builder, block, 0x033CDCF4);
		saveIntName(builder, block, 0x033CDC22);
		saveInt(builder, block, 0x0341EF60);
		
		return block;
	}
	
	private cSPUIBehaviorPredicateWinState() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorPredicateWinState other = new cSPUIBehaviorPredicateWinState();
		copyComponent(other, propagate);
		return other;
	}

}
