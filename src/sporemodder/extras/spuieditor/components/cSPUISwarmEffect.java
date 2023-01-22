package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUISwarmEffect extends UIEffect {
	
	public static final int TYPE = 0x0238EA36;

	public cSPUISwarmEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedText(block, 0x0238EC88, null);
	}
	
	public cSPUISwarmEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0238EC88, null);
	}

	private cSPUISwarmEffect() {
		super();
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveText(builder, block, 0x0238EC88);
		
		return block;
	}
	
	@Override
	public cSPUISwarmEffect copyComponent(boolean propagateIndependent) {
		cSPUISwarmEffect eff = new cSPUISwarmEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}

}
