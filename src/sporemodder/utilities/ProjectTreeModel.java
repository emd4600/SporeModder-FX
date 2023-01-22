package sporemodder.utilities;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class ProjectTreeModel extends FilteredTreeModel {

	//TODO correctly implement FilteredTreeModel
	
	public class ProjectTreeFilter implements TreeFilter {
		@Override
		public boolean accept(DefaultMutableTreeNode node) {
			ProjectTreeNode n = (ProjectTreeNode) node;
			if (filterMod) {
				if (!n.isMod) return false;
			}
			if (filterSearch) {
				if (!n.isMatch) return false;
			}
			return true;
		}
	}
	
	private boolean filterMod;
	private boolean filterSearch;

	public ProjectTreeModel(TreeNode root) {
		super(root);
		
		setFilter(new ProjectTreeFilter());
		setFilterEnabled(true);
	}

	@Override
	public Object getChild(Object parent, int index) {
		return ((ProjectTreeNode) parent).getChildAt(index, filterMod, filterSearch);
	}
	
	@Override
	public int getChildCount(Object parent) {
		return ((ProjectTreeNode) parent).getChildCount(filterMod, filterSearch);
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return ((ProjectTreeNode) parent).getIndex((TreeNode)child, filterMod, filterSearch);
	}
	
	@Override
	public void removeNodeFromParent(MutableTreeNode node) {
        ProjectTreeNode parent = (ProjectTreeNode)node.getParent();

        if(parent == null)
            throw new IllegalArgumentException("node does not have a parent.");

        int[] childIndex = new int[1];
        Object[] removedArray = new Object[1];

        childIndex[0] = parent.getIndex(node, filterMod, filterSearch);
        parent.remove(childIndex[0], filterMod, filterSearch);
        removedArray[0] = node;
        nodesWereRemoved(parent, childIndex, removedArray);
    }
	
	public void insertNode(MutableTreeNode newChild, MutableTreeNode parent) {
		ProjectTreeNode p = (ProjectTreeNode) parent;
		ProjectTreeNode child = (ProjectTreeNode) newChild;
		int index = p.getNextChildIndex(child.name);
		super.insertNodeInto(newChild, parent, index);
	}

	public boolean filterMod() {
		return filterMod;
	}

	public boolean filterSearch() {
		return filterSearch;
	}

	public void setFilterMod(boolean filterMod) {
		this.filterMod = filterMod;
	}

	public void setFilterSearch(boolean filterSearch) {
		this.filterSearch = filterSearch;
	}
	
	
}
