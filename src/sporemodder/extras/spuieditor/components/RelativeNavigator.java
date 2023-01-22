package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class RelativeNavigator extends ExplicitNavigator {
	
	public static final int TYPE = 0xAF8772AF;

	public RelativeNavigator(SPUIBlock block) throws InvalidBlockException {
		super(block);

		addUnassignedInt(block, 0x2F870000, 1);
	}
	
	public RelativeNavigator(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x2F870000, (int) 1);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = super.saveComponent(builder);
		
		saveInt(builder, block, 0x2F870000);
		
		return block;
	}

	private RelativeNavigator() {
		super();
	}

	@Override
	public RelativeNavigator copyComponent(boolean propagateIndependent) {
		RelativeNavigator other = new RelativeNavigator();
		copyComponent(other, propagateIndependent);
		return other;
	}

}
