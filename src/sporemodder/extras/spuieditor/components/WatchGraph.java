package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class WatchGraph extends Window {
	
	public static final int TYPE = 0x0295571A;

	public WatchGraph(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}

	private WatchGraph() {
		super();
	}
	
	public WatchGraph(SPUIViewer viewer) {
		super(viewer);
	}

	@Override
	public WatchGraph copyComponent(boolean propagateIndependent) {
		WatchGraph other = new WatchGraph();
		copyComponent(other, propagateIndependent);
		return other;
	}
}
