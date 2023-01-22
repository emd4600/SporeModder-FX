package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class WinXHTML extends Window {
	
	public static final int TYPE = 0x04D04553;

	public WinXHTML(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		addUnassignedInt(block, 0x0FF11739, 0);
		addUnassignedInt(block, 0x0FF1173A, 0);
		// type 0xEEF3AF8C
		addUnassignedShort(block, 0x0FF1173B, null);  // ScrollbarDrawable ?
		addUnassignedShort(block, 0x0FF1173C, null);  // ScrollbarDrawable ?
	}
	
	public WinXHTML(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0FF11739, (int) 0);
		unassignedProperties.put(0x0FF1173A, (int) 0);
		unassignedProperties.put(0x0FF1173B, null);
		unassignedProperties.put(0x0FF1173C, null);
	}

	private WinXHTML() {
		super();
	}
	
	@Override
	public WinXHTML copyComponent(boolean propagateIndependent) {
		WinXHTML other = new WinXHTML();
		copyComponent(other, propagateIndependent);
		
		return other;
	}

	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = super.saveComponent(builder);
		
		saveInt(builder, block, 0x0FF11739);
		saveInt(builder, block, 0x0FF1173A);
		saveReference(builder, block, 0x0FF1173B);
		saveReference(builder, block, 0x0FF1173C);
		
		return block;
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
