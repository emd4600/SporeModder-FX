package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class InflateEffect extends UIEffect {
	
	public static final int TYPE = 0x4F3DAF02;

	public InflateEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedFloat(block, 0x4F2C0000, 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x4F2C0000);
		
		return block;
	}
	
	public InflateEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x4F2C0000, (float) 0);
	}

	private InflateEffect() {
		super();
	}
	
	@Override
	public InflateEffect copyComponent(boolean propagateIndependent) {
		InflateEffect eff = new InflateEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}

}
