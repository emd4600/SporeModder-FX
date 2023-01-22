package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class GlideEffect extends UIEffect {
	
	public static final int TYPE = 0xCF2B2AD4;

	public GlideEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedVec2(block, 0x4F2C0000, new float[2]);
	}
	
	public GlideEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x4F2C0000, new float[2]);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveVec2(builder, block, 0x4F2C0000);
		
		return block;
	}

	private GlideEffect() {
		super();
	}
	
	@Override
	public GlideEffect copyComponent(boolean propagateIndependent) {
		GlideEffect eff = new GlideEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}
	
}
