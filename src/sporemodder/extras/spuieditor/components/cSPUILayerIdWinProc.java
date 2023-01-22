package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUILayerIdWinProc extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0x02BCFC18;
	
	public cSPUILayerIdWinProc(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedInt2(block, 0x02BCFD66, 1);
		addUnassignedBoolean(block, 0x02BCFD64, true);
		addUnassignedBoolean(block, 0x02BCFD65, true);
	}
	
	public cSPUILayerIdWinProc(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x02BCFD66, (int) 1);
		unassignedProperties.put(0x02BCFD64, (boolean) true);
		unassignedProperties.put(0x02BCFD65, (boolean) true);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		saveInt2(builder, block, 0x02BCFD66);
		saveBoolean(builder, block, 0x02BCFD64);
		saveBoolean(builder, block, 0x02BCFD65);
		
		return block;
	}

	protected cSPUILayerIdWinProc() {
	}
	
	@Override
	public cSPUILayerIdWinProc copyComponent(boolean propagate) {
		cSPUILayerIdWinProc other = new cSPUILayerIdWinProc();
		copyComponent(other, propagate);
		return other;
	}

}
