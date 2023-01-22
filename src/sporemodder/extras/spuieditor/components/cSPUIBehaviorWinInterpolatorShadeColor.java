package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorWinInterpolatorShadeColor extends SPUIDefaultComponent {

	public static final int TYPE = 0x0254AA82;
	
	public cSPUIBehaviorWinInterpolatorShadeColor(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedBoolean(block, 0x0254AB8E, false);
		addUnassignedInt(block, 0x0254B152, 0xFFFFFFFF);
		addUnassignedInt(block, 0x0254B153, 0xFF000000);
	}
	
	public cSPUIBehaviorWinInterpolatorShadeColor(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0254AB8E, (boolean) false);
		unassignedProperties.put(0x0254B152, (int) 0xFFFFFFFF);
		unassignedProperties.put(0x0254B153, (int) 0xFF000000);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveBoolean(builder, block, 0x0254AB8E);
		saveInt(builder, block, 0x0254B152);
		saveInt(builder, block, 0x0254B153);
		
		return block;
	}
	
	private cSPUIBehaviorWinInterpolatorShadeColor() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagateIndependent) {
		cSPUIBehaviorWinInterpolatorShadeColor other = new cSPUIBehaviorWinInterpolatorShadeColor();
		copyComponent(other, propagateIndependent);
		return other;
	}

}
