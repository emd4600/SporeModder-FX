package sporemodder.utilities;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.utilities.FilteredTreeModel.TreeFilter;

public class FilteredTree extends JTree {

	public static interface FilteredTreeDragAndDrop {
		public DataFlavor getDataFlavor();

		public boolean canBeDragged(DefaultMutableTreeNode node);
		public boolean canBeDropped(DefaultMutableTreeNode node, DefaultMutableTreeNode newParent);
		public void move(DefaultMutableTreeNode selectedNode, DefaultMutableTreeNode newParent, int childIndex);
	}

	private FilteredTreeDragAndDrop dragAndDrop;

	public FilteredTree(FilteredTreeModel treeModel) {
		super(treeModel);
	}

	public FilteredTree(FilteredTreeModel treeModel, FilteredTreeDragAndDrop dragAndDrop) {
		super(treeModel);

		this.dragAndDrop = dragAndDrop;
		
		this.setDragEnabled(true);
		this.setTransferHandler(new FilteredTreeTransferHandler());
		this.setDropMode(DropMode.ON_OR_INSERT);
	}

	@Override
	public FilteredTreeModel getModel() {
		return (FilteredTreeModel) treeModel;
	}

	public TreeFilter getFilter() {
		return getModel().getFilter();
	}

	public void setFilter(TreeFilter filter) {
		getModel().setFilter(filter);
	}

	public boolean isFilterEnabled() {
		return getModel().isFilterEnabled();
	}

	public void setFilterEnabled(boolean enabled) {
		getModel().setFilterEnabled(enabled);
	}
	
	@Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // filter property change of "dropLocation" with newValue==null, 
        // since this will result in a NPE in BasicTreeUI.getDropLineRect(...)
        if(newValue!=null || !"dropLocation".equals(propertyName)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

	private DefaultMutableTreeNode getSelectedNode() {
		if (FilteredTree.this.getSelectionCount() == 0) {
			return null;
		}
		return (DefaultMutableTreeNode) FilteredTree.this.getSelectionPath().getLastPathComponent();
	}

	private class FilteredTreeTransferHandler extends TransferHandler {
		private DataFlavor dataFlavor;
		private DataFlavor[] flavors;

		private FilteredTreeTransferHandler() {
			dataFlavor = dragAndDrop.getDataFlavor();
			flavors = new DataFlavor[] {dataFlavor};
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDrop()) {
				return false;
			}

			DefaultMutableTreeNode node = getSelectedNode();
			
			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			
			TreePath dest = dl.getPath();
			DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest.getLastPathComponent();
			
			if (!dragAndDrop.canBeDropped(node, target)) {
				return false;
			}

			support.setShowDropLocation(true);
			if (!support.isDataFlavorSupported(dataFlavor)) {
				return false;
			}
			
			int action = support.getDropAction();
			if(action == MOVE) {
				return true;
			}

			return false;
		}

		@Override
		protected FilteredTreeTransferable createTransferable(JComponent c) {
			DefaultMutableTreeNode node = getSelectedNode();
			
			if (node != null) {
				return new FilteredTreeTransferable(this, node);
			}
			
			return null;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
		}

		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			
			try {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) support.getTransferable().getTransferData(dataFlavor);
				
				JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
				
				TreePath dest = dl.getPath();
				DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest.getLastPathComponent();
				
				dragAndDrop.move(node, target, dl.getChildIndex());
				
				return true;
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	private class FilteredTreeTransferable implements Transferable {

		private FilteredTreeTransferHandler handler;
		private DefaultMutableTreeNode node;

		public FilteredTreeTransferable(FilteredTreeTransferHandler handler, DefaultMutableTreeNode node) {
			this.handler = handler;
			this.node = node;
		}

		@Override
		public DefaultMutableTreeNode getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!flavor.equals(handler.dataFlavor)) {
				throw new UnsupportedFlavorException(flavor); 
			}
			return node;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return handler.flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(handler.dataFlavor);
		}

	}
}
