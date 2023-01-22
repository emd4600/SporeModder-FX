package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class CascadeEffect extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0xF90A5AE;
	
	public CascadeEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedFloat(block, 0x4F2B0000, (float) 0);
		addUnassignedFloat(block, 0x4F2B0001, (float) 0);
	}

	private CascadeEffect() {
		super();
	}
	
	public CascadeEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x4F2B0000, (float) 0);
		unassignedProperties.put(0x4F2B0001, (float) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x4F2B0000);
		saveFloat(builder, block, 0x4F2B0001);
		
		return block;
	}
	
	@Override
	public CascadeEffect copyComponent(boolean propagate) {
		CascadeEffect eff = new CascadeEffect();
		copyComponent(eff, propagate);
		return eff;
	}

}
