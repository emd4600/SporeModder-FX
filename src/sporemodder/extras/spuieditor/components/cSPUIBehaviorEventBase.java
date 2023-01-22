package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;

public class cSPUIBehaviorEventBase extends SPUIDefaultWinProc {

	public static final int TYPE = 0x024BA741;
	
	// private static final int[] PROPERTIES = new int[] {0x03335C12, 0x03335C13, 0x03335C14, 0x03335C15, 0x03335C41,0x03335C42, 0x03335C43, 0x03335C44};
	
	public cSPUIBehaviorEventBase(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		addUnassignedShort(block, 0x03335C12, null);
		addUnassignedShort(block, 0x03335C13, null);
		addUnassignedShort(block, 0x03335C14, null);
		addUnassignedShort(block, 0x03335C15, null);
		addUnassignedShort(block, 0x03335C41, null);
		addUnassignedShort(block, 0x03335C42, null);
		addUnassignedShort(block, 0x03335C43, null);
		addUnassignedShort(block, 0x03335C44, null);
	}
	
	public cSPUIBehaviorEventBase(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x03335C12, null);
		unassignedProperties.put(0x03335C13, null);
		unassignedProperties.put(0x03335C14, null);
		unassignedProperties.put(0x03335C15, null);
		unassignedProperties.put(0x03335C41, null);
		unassignedProperties.put(0x03335C42, null);
		unassignedProperties.put(0x03335C43, null);
		unassignedProperties.put(0x03335C44, null);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveReference(builder, block, 0x03335C12);
		saveReference(builder, block, 0x03335C13);
		saveReference(builder, block, 0x03335C14);
		saveReference(builder, block, 0x03335C15);
		saveReference(builder, block, 0x03335C41);
		saveReference(builder, block, 0x03335C42);
		saveReference(builder, block, 0x03335C43);
		saveReference(builder, block, 0x03335C44);
		
		return block;
	}
	
	protected cSPUIBehaviorEventBase() {
		super();
	}
	
	@Override
	public cSPUIBehaviorEventBase copyComponent(boolean propagate) {
		cSPUIBehaviorEventBase other = new cSPUIBehaviorEventBase();
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
