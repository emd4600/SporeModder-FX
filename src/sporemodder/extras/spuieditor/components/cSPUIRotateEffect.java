package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIRotateEffect extends RotateEffect {
	
	public static final int TYPE = 0x02086743;

	public cSPUIRotateEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedFloat(block, 0x02086A1D, 0);
	}
	
	public cSPUIRotateEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x02086A1D, (float) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x02086A1D);
		
		return block;
	}

	private cSPUIRotateEffect() {
		super();
	}
	
	@Override
	public cSPUIRotateEffect copyComponent(boolean propagateIndependent) {
		cSPUIRotateEffect eff = new cSPUIRotateEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}

}
