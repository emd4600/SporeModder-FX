package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class FadeEffect extends UIEffect {
	
	public static final int TYPE = 0x6F2B6D2C;

	public FadeEffect(SPUIBlock block) throws InvalidBlockException {
		super(block);
	}
	
	public FadeEffect(SPUIViewer viewer) {
		super(viewer);
	}

	private FadeEffect() {
		super();
	}
	
	@Override
	public FadeEffect copyComponent(boolean propagateIndependent) {
		FadeEffect eff = new FadeEffect();
		copyComponent(eff, propagateIndependent);
		return eff;
	}

}
