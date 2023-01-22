package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIMaterialWinProc extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0x02BCFC0E;

	public cSPUIMaterialWinProc(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedBoolean(block, 0x02BCB912, false);
		addUnassignedText(block, 0x02BCB913, null);
	}
	
	public cSPUIMaterialWinProc(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x02BCB912, (boolean) false);
		unassignedProperties.put(0x02BCB913, null);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveBoolean(builder, block, 0x02BCB912);
		
		if (unassignedProperties.containsKey(0x02BCB913)) {
			saveText(builder, block, 0x02BCB913);
		}
		
		return block;
	}

	private cSPUIMaterialWinProc() {
		super();
	}

	@Override
	public cSPUIMaterialWinProc copyComponent(boolean propagate) {
		cSPUIMaterialWinProc other = new cSPUIMaterialWinProc();
		copyComponent(other, propagate);
		return other;
	}

}
