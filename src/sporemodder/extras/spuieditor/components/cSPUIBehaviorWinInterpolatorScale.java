package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorWinInterpolatorScale extends SPUIDefaultComponent {

	public static final int TYPE = 0x0269EFE5;
	
	public cSPUIBehaviorWinInterpolatorScale(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedBoolean(block, 0x0254AB8E, false);
		addUnassignedFloat(block, 0x0254B152, 0);
		addUnassignedFloat(block, 0x0254b153, 0);
	}
	
	public cSPUIBehaviorWinInterpolatorScale(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0254AB8E, (boolean) false);
		unassignedProperties.put(0x0254B152, (float) 0);
		unassignedProperties.put(0x0254B153, (float) 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveBoolean(builder, block, 0x0254AB8E);
		saveFloat(builder, block, 0x0254B152);
		saveFloat(builder, block, 0x0254b153);
		
		return block;
	}
	
	private cSPUIBehaviorWinInterpolatorScale() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagateIndependent) {
		cSPUIBehaviorWinInterpolatorScale other = new cSPUIBehaviorWinInterpolatorScale();
		copyComponent(other, propagateIndependent);
		return other;
	}

}
