package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorActionWinState extends SPUIDefaultComponent {

	public static final int TYPE = 0x033F7AD6;
	
	// private static final int[] PROPERTIES = new int[] {0x03335C12, 0x03335C13, 0x03335C14, 0x03335C15};
	
	public cSPUIBehaviorActionWinState(SPUIBlock block) throws InvalidBlockException {
		super(block);
		//24B5C98
		addUnassignedShort(block, 0x03335C12, null);
		addUnassignedShort(block, 0x03335C13, null);
		addUnassignedShort(block, 0x03335C14, null);
		addUnassignedShort(block, 0x03335C15, null);
		addUnassignedInt(block, 0x033F7F81, 0);
		addUnassignedIntName(block, 0x033F7F8C, null);
		addUnassignedIntName(block, 0x033F7F89, null);
		addUnassignedInt(block, 0x0341F297, 0);
	}
	
	public cSPUIBehaviorActionWinState(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x03335C12, null);
		unassignedProperties.put(0x03335C13, null);
		unassignedProperties.put(0x03335C14, null);
		unassignedProperties.put(0x03335C15, null);
		unassignedProperties.put(0x033F7F81, (int) 0);
		unassignedProperties.put(0x033F7F8C, null);
		unassignedProperties.put(0x033F7F89, null);
		unassignedProperties.put(0x0341F297, (int) 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveReference(builder, block, 0x03335C12);
		saveReference(builder, block, 0x03335C13);
		saveReference(builder, block, 0x03335C14);
		saveReference(builder, block, 0x03335C15);
		saveInt(builder, block, 0x033F7F81);
		saveIntName(builder, block, 0x033F7F8C);
		saveIntName(builder, block, 0x033F7F89);
		saveInt(builder, block, 0x0341F297);
		
		return block;
	}
	
	private cSPUIBehaviorActionWinState() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorActionWinState other = new cSPUIBehaviorActionWinState();
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
