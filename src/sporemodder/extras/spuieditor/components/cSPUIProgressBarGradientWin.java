package sporemodder.extras.spuieditor.components;

import java.io.IOException;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIProgressBarGradientWin extends Window {
	
	public static final int TYPE = 0x015CBBD6;

	public cSPUIProgressBarGradientWin(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		addUnassignedInt(block, 0x015CBE07, 0);
		addUnassignedInt(block, 0x015CBE08, 0);
		addUnassignedInt(block, 0x015CBE09, 0);
		addUnassignedInt(block, 0x015CBE0A, 0);
		addUnassignedShort(block, 0x015CBE06, null);  // an Image  
	}
	
	public cSPUIProgressBarGradientWin(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x015CBE07, (int) 0);
		unassignedProperties.put(0x015CBE08, (int) 0);
		unassignedProperties.put(0x015CBE09, (int) 0);
		unassignedProperties.put(0x015CBE0A, (int) 0);
		unassignedProperties.put(0x015CBE06, null);
	}

	private cSPUIProgressBarGradientWin() {
		super();
	}
	
	@Override
	public cSPUIProgressBarGradientWin copyComponent(boolean propagateIndependent) {
		cSPUIProgressBarGradientWin other = new cSPUIProgressBarGradientWin();
		copyComponent(other, propagateIndependent);
		
		return other;
	}

	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = super.saveComponent(builder);
		
		saveInt(builder, block, 0x015CBE07);
		saveInt(builder, block, 0x015CBE08);
		saveInt(builder, block, 0x015CBE09);
		saveInt(builder, block, 0x015CBE0A);
		saveReference(builder, block, 0x015CBE06);
		
		return block;
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
