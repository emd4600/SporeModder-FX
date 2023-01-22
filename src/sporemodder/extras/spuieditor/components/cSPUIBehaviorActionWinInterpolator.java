package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorActionWinInterpolator extends SPUIDefaultComponent {

	public static final int TYPE = 0x0254CB67;
	
	// private static final int[] PROPERTIES = new int[] {0x0254CDFB, 0x0254CDFC, 0x03335C12, 0x03335C13, 0x03335C14, 0x03335C15};
	
	public cSPUIBehaviorActionWinInterpolator(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		// 243A853
		addUnassignedShort(block, 0x0254CDFB, null);
		addUnassignedShort(block, 0x0254CDFC, null);
		
		//cSPUIBehaviorWinInterpolator
		// types 24B5C98
		addUnassignedShort(block, 0x03335C12, null);
		addUnassignedShort(block, 0x03335C13, null);
		addUnassignedShort(block, 0x03335C14, null);
		addUnassignedShort(block, 0x03335C15, null);
	}
	
	private cSPUIBehaviorActionWinInterpolator() {
		super();
	}
	
	public cSPUIBehaviorActionWinInterpolator(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x0254CDFB, null);
		unassignedProperties.put(0x0254CDFC, null);
		unassignedProperties.put(0x03335C12, null);
		unassignedProperties.put(0x03335C13, null);
		unassignedProperties.put(0x03335C14, null);
		unassignedProperties.put(0x03335C15, null);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveReference(builder, block, 0x0254CDFB);
		saveReference(builder, block, 0x0254CDFC);
		saveReference(builder, block, 0x03335C12);
		saveReference(builder, block, 0x03335C13);
		saveReference(builder, block, 0x03335C14);
		saveReference(builder, block, 0x03335C15);
		
		return block;
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorActionWinInterpolator other = new cSPUIBehaviorActionWinInterpolator();
		copyComponent(other, propagate);
		return other;
	}
	
//	@Override
//	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index) {
//		super.fillHierarchyTree(model, parent, index);
//		
//		int ind = 0;
//		for (int i = 0; i < PROPERTIES.length; i++) {
//			Object obj = unassignedProperties.get(PROPERTIES[i]);
//			if (obj != null) {
//				((SPUIComponent) obj).fillHierarchyTree(model, node, ind++);
//			}
//		}
//		
//		return node;
//	}
	
}
