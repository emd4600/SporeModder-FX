package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class WatchWindow extends Window {
	
	public static final int TYPE = 0x6FB339C9;

	public WatchWindow(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}

	private WatchWindow() {
		super();
	}
	
	public WatchWindow(SPUIViewer viewer) {
		super(viewer);
	}

	@Override
	public WatchWindow copyComponent(boolean propagateIndependent) {
		WatchWindow other = new WatchWindow();
		copyComponent(other, propagateIndependent);
		return other;
	}
}
