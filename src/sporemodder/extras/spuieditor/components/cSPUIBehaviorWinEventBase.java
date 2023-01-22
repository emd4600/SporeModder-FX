package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIBehaviorWinEventBase extends cSPUIBehaviorEventBase {
	
	public static final int TYPE = 0x0255E38E;

	public cSPUIBehaviorWinEventBase(SPUIBlock block) throws InvalidBlockException {
		super(block);

		addUnassignedInt(block, 0x0255EAF8, 0);
		addUnassignedBoolean(block, 0x0255EAF9, false);
		addUnassignedIntName(block, 0x0255EAFA, null);
	}
	
	public cSPUIBehaviorWinEventBase(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0255EAF8, (int) 0);
		unassignedProperties.put(0x0255EAF9, (boolean) false);
		unassignedProperties.put(0x0255EAFA, null);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder)	{
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveInt(builder, block, 0x0255EAF8);
		saveBoolean(builder, block, 0x0255EAF9);
		saveIntName(builder, block, 0x0255EAFA);
		
		return block;
	}

	protected cSPUIBehaviorWinEventBase() {
		super();
	}
	
	@Override
	public cSPUIBehaviorWinEventBase copyComponent(boolean propagateIndependent) {
		cSPUIBehaviorWinEventBase other = new cSPUIBehaviorWinEventBase();
		copyComponent(other, propagateIndependent);
		return other;
	}
	
}
