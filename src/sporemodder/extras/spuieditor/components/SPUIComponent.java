package sporemodder.extras.spuieditor.components;

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.extras.spuieditor.ComponentContainer;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public interface SPUIComponent extends ComponentContainer {
	public interface SPUIComponentFilter {
		public boolean accept(SPUIComponent component);
	}
	
	public SPUIComponent copyComponent(boolean propagateIndependent);
	
	public SPUIObject saveComponent(SPUIBuilder builder);
	
	public int getType();
	
	/**
	 * Returns the <code>SPUIBlock</code> that this component represents. It will be the original block, with no changes applied.
	 * @return
	 */
	public SPUIObject getObject();
	
	/**
	 * Returns a <code>PropertiesPanel</code> where the user can edit any property of the component.
	 * @return
	 */
	public PropertiesPanel getPropertiesPanel();
	
	public TreePath getHierarchyTreePath();
	
	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index);
	
	public MutableTreeNode getNode();
	
	/**
	 * Returns the <code>SPUIViewer</code> that is holding this component.
	 * @return
	 */
	public SPUIViewer getSPUIViewer();
	
	/**
	 * Sets the <code>SPUIViewer</code> that is holding this component. Each implementation must propagate this method to all
	 * the components used by it.
	 * 
	 * Components need to know which viewer they are in so they can repaint it when any changes are made.
	 * 
	 * @param viewer The <code>SPUIViewer</code> that is holding this component.
	 */
	public void setSPUIViewer(SPUIViewer viewer);
	
	// Can this component only be added once?
	public boolean isUnique();
	
	public boolean usesComponent(SPUIComponent component);
	
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter);
	
	public void restoreRemovedComponent(RemoveComponentAction removeAction);
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate);

}
