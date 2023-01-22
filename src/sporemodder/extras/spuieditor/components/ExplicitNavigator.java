package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class ExplicitNavigator extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0x4F8772AA;
	
	public ExplicitNavigator(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedInt(block, 0x2F860000, 9);
		addUnassignedInt(block, 0x2F860001, 1);
	}
	
	public ExplicitNavigator(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x2F860000, (int) 9);
		unassignedProperties.put(0x2F860001, (int) 1);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		saveInt(builder, block, 0x2F860000);
		saveInt(builder, block, 0x2F860001);
		
		return block;
	}

	protected ExplicitNavigator() {
	}
	
	@Override
	public ExplicitNavigator copyComponent(boolean propagateIndependent) {
		ExplicitNavigator other = new ExplicitNavigator();
		copyComponent(other, propagateIndependent);
		return other;
	}

}
