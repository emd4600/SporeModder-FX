package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class PerspectiveEffect extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0xCF2B2AD6;
	
	public PerspectiveEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		addUnassignedFloat(block, 0x4F2C0110, 0);
	}
	
	public PerspectiveEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x4F2C0110, (float) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveFloat(builder, block, 0x4F2C0110);
		
		builder.addBlock(block);
		
		return block;
	}

	private PerspectiveEffect() {
		super();
	}
	
	@Override
	public PerspectiveEffect copyComponent(boolean propagateIndependent) {
		PerspectiveEffect eff = new PerspectiveEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}

}
