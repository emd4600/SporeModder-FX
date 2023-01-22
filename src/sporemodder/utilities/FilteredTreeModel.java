package sporemodder.utilities;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class FilteredTreeModel extends DefaultTreeModel {
	
	public static interface TreeFilter {
		public boolean accept(DefaultMutableTreeNode node);
	}
	
	private TreeFilter filter;
	private boolean filterEnabled;
	
	public FilteredTreeModel(TreeNode root) {
		super(root);
	}

	@Override
	public Object getChild(Object parent, int index) {
		return getChildAt((TreeNode) parent, index);
	}
	
	@Override
	public int getChildCount(Object parent) {
		return getChildCount((TreeNode) parent);
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return getIndex((TreeNode) parent, (TreeNode) child);
	}
	
	@Override
	public void removeNodeFromParent(MutableTreeNode node) {
		MutableTreeNode parent = (MutableTreeNode) node.getParent();

        if(parent == null)
            throw new IllegalArgumentException("node does not have a parent.");

        int[] childIndex = new int[1];
        Object[] removedArray = new Object[1];

        childIndex[0] = getIndex(parent, node);
        parent.remove(childIndex[0]);
        removedArray[0] = node;
        nodesWereRemoved(parent, childIndex, removedArray);
    }
	
	private int getIndex(TreeNode parent, TreeNode aChild) {
 		if (filter == null || !filterEnabled) {
 			return parent.getIndex(aChild);
 		}
 		
 		int visibleIndex = 0;
 		@SuppressWarnings("rawtypes")
		Enumeration e = parent.children();
 		
 		if (e == null) {
 			return -1;
 		}
 		
 		while (e.hasMoreElements()) {
 			DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
 			
 			if (filter.accept(n)) {
 				if (n == aChild) {
 	 				return visibleIndex;
 	 			}
 	 			visibleIndex++;
 			}
 		}
 		
 		return -1;
 	}
	
	public int getVisibleIndex(TreeNode node, int realIndex) {
		if (filter == null || !filterEnabled) {
			return realIndex;
		}
		
		@SuppressWarnings("rawtypes")
		Enumeration e = node.children();
		int visibleIndex = -1;
		int index = 0;
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
			
			if (filter.accept(n)) visibleIndex++;
			
			if (realIndex == index++) {
				return visibleIndex;
			}
		}
		
		return -1;
	}
	
	private TreeNode getChildAt(TreeNode node, int index) {
		
		if (filter == null || !filterEnabled) {
			return node.getChildAt(index);
		}
		
		@SuppressWarnings("rawtypes")
		Enumeration e = node.children();
		
		if (e == null) {
			throw new ArrayIndexOutOfBoundsException("Node has no children");
		}
		
		int visibleIndex = -1;
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
			
			if (filter.accept(n)) visibleIndex++;
			
			if (visibleIndex == index) {
				return (TreeNode) n;
			}
		}
		
		throw new ArrayIndexOutOfBoundsException("Index unmatched");
	}
	
	private int getChildCount(TreeNode node) {
		
		if (filter == null || !filterEnabled) {
			return node.getChildCount();
		}
		
		@SuppressWarnings("rawtypes")
		Enumeration e = node.children();
		
		if (node.children() == null) {
			throw new ArrayIndexOutOfBoundsException("Node has no children");
		}
		
		int count = 0;
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
			
			if (filter.accept(n)) count++;
		}
		
		return count;
	}
	
	public TreeFilter getFilter() {
		return filter;
	}
	
	public void setFilter(TreeFilter filter) {
		this.filter = filter;
	}
	
	public boolean isFilterEnabled() {
		return filterEnabled;
	}
	
	public void setFilterEnabled(boolean enabled) {
		this.filterEnabled = enabled;
	}
	
}
