package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIMaterialEffect extends UIEffect {
	
	public static final int TYPE = 0x02C200CE;

	public cSPUIMaterialEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedText(block, 0x02C1F2C7, null);
		addUnassignedText(block, 0x02C1F2CA, null);
		addUnassignedBoolean(block, 0x02C1F2D4, false);
	}
	
	public cSPUIMaterialEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x02C1F2C7, null);
		unassignedProperties.put(0x02C1F2CA, null);
		unassignedProperties.put(0x02C1F2D4, (boolean) false);
	}

	private cSPUIMaterialEffect() {
		super();
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveText(builder, block, 0x02C1F2C7);
		saveText(builder, block, 0x02C1F2CA);
		saveBoolean(builder, block, 0x02C1F2D4);
		
		return block;
	}
	
	@Override
	public cSPUIMaterialEffect copyComponent(boolean propagate) {
		cSPUIMaterialEffect eff = new cSPUIMaterialEffect();
		copyComponent(eff, propagate);
		return eff;
	}

}
