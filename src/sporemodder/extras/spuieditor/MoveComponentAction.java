package sporemodder.extras.spuieditor;

import javax.swing.tree.DefaultMutableTreeNode;

import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.WinComponent;

public class MoveComponentAction implements CommandAction {
	
	private DefaultMutableTreeNode node;
	private DefaultMutableTreeNode oldParent;
	private DefaultMutableTreeNode newParent;
	private int childIndex;
	private int oldChildIndex;
	private SPUIEditor editor;

	public MoveComponentAction(SPUIEditor editor, DefaultMutableTreeNode node, DefaultMutableTreeNode oldParent, DefaultMutableTreeNode newParent, int childIndex) {
		this.editor = editor;
		this.node = node;
		this.oldParent = oldParent;
		this.newParent = newParent;
		this.childIndex = childIndex;
		this.oldChildIndex = newParent.getIndex(node);
	}

	@Override
	public void undo() {
		execute(node, oldParent, newParent, oldChildIndex, false);
	}
	
	private void insertComponent(SPUIComponent component, DefaultMutableTreeNode newParent, int index) {
		((ComponentContainer) newParent.getUserObject()).insertComponent(component, index);
	}
	
	private void removeComponent(SPUIComponent component, DefaultMutableTreeNode oldParent) {
		((ComponentContainer) oldParent.getUserObject()).removeComponent(component);
	}

	@Override
	public void redo() {
		execute(node, newParent, oldParent, childIndex, true);
	}
	
	private void execute(DefaultMutableTreeNode node, DefaultMutableTreeNode newParent, DefaultMutableTreeNode oldParent, int index, boolean fixIndex) {
		SPUIComponent component = (SPUIComponent) node.getUserObject();
		boolean removeFirst = false;
		
		// the parent is the same; therefore, the index might be different when we remove the component
		// but we must remove it first, because the first component found gets removed (which might be the new component!)
		if (newParent == oldParent) {
			removeFirst = true;
		}
		
		if (removeFirst) {
			if (fixIndex && index != -1) {
				int currentIndex;
				// we must fix the index if the component is now after where it previously was
//				if (newParent == editor.getRootNode()) {
//					currentIndex = editor.getSPUIViewer().getMainWindows().indexOf(component);
//				} else {
//					currentIndex = ((WinComponent) newParent.getUserObject()).getIndexOfChild(component);
//				}
				currentIndex = ((WinComponent) newParent.getUserObject()).getIndexOfChild(component);
				if (index > currentIndex) {
					index--;
				}
			}
			
			removeComponent(component, oldParent);
			insertComponent(component, newParent, index);
		}
		else {
			insertComponent(component, newParent, index);
			removeComponent(component, oldParent);
		}
		
		
		editor.fillHierarchyTree();
		editor.setSelectedComponent(component);
		editor.getSPUIViewer().repaint();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

}
