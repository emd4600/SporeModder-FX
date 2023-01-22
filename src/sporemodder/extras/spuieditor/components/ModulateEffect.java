package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class ModulateEffect extends UIEffect {
	
	public static final int TYPE = 0x2F8BBB4D;

	public ModulateEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedInt(block, 0x4F2C0000, 0);
		addUnassignedInt(block, 0x4F2C0001, 0);
	}
	
	public ModulateEffect(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x4F2C0000, (int) 0);
		unassignedProperties.put(0x4F2C0001, (int) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveInt(builder, block, 0x4F2C0000);
		saveInt(builder, block, 0x4F2C0001);
		
		return block;
	}

	private ModulateEffect() {
		super();
	}
	
	@Override
	public ModulateEffect copyComponent(boolean propagateIndependent) {
		ModulateEffect eff = new ModulateEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}

}
