package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public abstract class UIEffect extends SPUIDefaultWinProc {
	
	public UIEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedFloat(block, 0x4F2B0000, 0);
		addUnassignedInt(block, 0x4F2B0001, 0);
		addUnassignedInt(block, 0x4F2B0002, 0);
		addUnassignedFloat(block, 0x4F2B0003, 0);
		addUnassignedFloat(block, 0x4F2B0004, 0);
		addUnassignedFloat(block, 0x4F2B0005, 0);
	}
	
	public UIEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x4F2B0000, (float) 0);
		unassignedProperties.put(0x4F2B0001, (int) 0);
		unassignedProperties.put(0x4F2B0002, (int) 0);
		unassignedProperties.put(0x4F2B0003, (float) 0);
		unassignedProperties.put(0x4F2B0004, (float) 0);
		unassignedProperties.put(0x4F2B0005, (float) 0);
	}

	protected UIEffect() {
		super();
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x4F2B0000);
		saveInt(builder, block, 0x4F2B0001);
		saveInt(builder, block, 0x4F2B0002);
		saveFloat(builder, block, 0x4F2B0003);
		saveFloat(builder, block, 0x4F2B0004);
		saveFloat(builder, block, 0x4F2B0005);
		
		return block;
	}

}
