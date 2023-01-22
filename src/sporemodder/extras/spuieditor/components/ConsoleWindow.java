package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;

public class ConsoleWindow extends Window {
	
	public static final int TYPE = 0xEFBC56D8;

	public ConsoleWindow(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
	}

	private ConsoleWindow() {
		super();
	}

	@Override
	public ConsoleWindow copyComponent(boolean propagateIndependent) {
		ConsoleWindow other = new ConsoleWindow();
		copyComponent(other, propagateIndependent);
		return other;
	}
	
	public ConsoleWindow(SPUIViewer viewer) {
		super(viewer);
	}
}
