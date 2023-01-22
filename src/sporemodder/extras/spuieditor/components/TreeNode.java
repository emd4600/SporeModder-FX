package sporemodder.extras.spuieditor.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerElement;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;

public class TreeNode extends SPUIDefaultComponent {
	
	public static final String INTERFACE_NAME = "ITreeNode";
	
	public static final int TYPE = 0x702C5143;
	
	private WinTreeView treeView;
	private TreeNode parent;
	private final List<TreeNode> childrenNodes = new ArrayList<TreeNode>();

	public TreeNode(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		short[] indices = SectionShort.getValues(block.getSection(0xF0310001, SectionShort.class), null, -1);
		if (indices != null) {
			for (int i = 0; i < indices.length; i++) {
				TreeNode node = (TreeNode) ResourceLoader.getComponent(block.getParent().get(indices[i]));
				node.parent = this;
				childrenNodes.add(node);
			}
		}
		
		addUnassignedInt(block, 0xF0310002, 0);
		addUnassignedInt(block, 0xF0310003, 0);
		addUnassignedShort(block, 0xF0310004, null);  // TreeExpanderDrawable ?
		addUnassignedText(block, 0xF0310005, null);
		addUnassignedInt(block, 0xF0310006, 0);
	}
	
	public TreeNode(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0xF0310002, (int) 0);
		unassignedProperties.put(0xF0310003, (int) 0);
		unassignedProperties.put(0xF0310004, null);
		unassignedProperties.put(0xF0310005, null);
		unassignedProperties.put(0xF0310006, (int) 0);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		if (!childrenNodes.isEmpty()) {
			SPUIObject[] nodes = new SPUIObject[childrenNodes.size()];
			for (int i = 0; i < nodes.length; i++) {
				if (childrenNodes.get(i) != null) {
					nodes[i] = builder.addComponent(childrenNodes.get(i));
				}
			}
			builder.addReference(block, 0xF0310001, nodes);
		}
		
		saveInt(builder, block, 0xF0310002);
		saveInt(builder, block, 0xF0310003);
		saveReference(builder, block, 0xF0310004);
		saveText(builder, block, 0xF0310005);
		saveInt(builder, block, 0xF0310006);
		
		return block;
	}

	private TreeNode() {
		super();
	}

	@Override
	public TreeNode copyComponent(boolean propagateIndependent) {
		TreeNode other = new TreeNode();
		copyComponent(other, propagateIndependent);
		
		if (childrenNodes != null) {
			for (int i = 0; i < childrenNodes.size(); i++) {
				TreeNode child = childrenNodes.get(i);
				other.childrenNodes.add(child == null ? null : child.copyComponent(propagateIndependent));
				other.childrenNodes.get(i).parent = other;
			}
		}
		
		return other;
	}
	
	public void setTreeView(WinTreeView treeView) {
		this.treeView = treeView;
		if (childrenNodes != null) {
			for (TreeNode child : childrenNodes) {
				child.treeView = treeView;
			}
		}
	}
	
	@Override
	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index) {
		super.fillHierarchyTree(model, parent, index);
		
		if (!childrenNodes.isEmpty()) {
			int ind = 0;
			
			for (int i = 0; i < childrenNodes.size(); i++) {
				if (childrenNodes.get(i) != null) {
					childrenNodes.get(i).fillHierarchyTree(model, node, ind++);
				}
			}
		}
		
		return node;
	}
	
	@Override
	public boolean nodeIsMovable() {
		return true;
	}
	
	@Override
	public boolean nodeAcceptsComponent(SPUIComponent other) {
		return other instanceof TreeNode;
	}
	
	@Override
	public boolean nodeCanBeMovedAbove() {
		List<TreeNode> childrenNodes = null;
		
		if (parent == null) {
			if (treeView == null) return false;
			childrenNodes = treeView.getChildrenNodes();
		}
		else {
			childrenNodes = parent.childrenNodes;
		}
		
		if (childrenNodes == null) return false; 
		
		for (int i = 0; i < childrenNodes.size(); i++) {
			if (childrenNodes.get(i) == this) {
				return i > 0;
			}
		}
		return false;
	}
	
	@Override
	public boolean nodeCanBeMovedBelow() {
		List<TreeNode> childrenNodes = null;
		
		if (parent == null) {
			if (treeView == null) return false;
			childrenNodes = treeView.getChildrenNodes();
		}
		else {
			childrenNodes = parent.childrenNodes;
		}
		
		if (childrenNodes == null) return false; 
		
		for (int i = 0; i < childrenNodes.size(); i++) {
			if (childrenNodes.get(i) == this) {
				return i < childrenNodes.size() - 1;
			}
		}
		return false;
	}
	
	@Override
	public void insertComponent(SPUIComponent component, int index) {
		childrenNodes.add(index == -1 ? childrenNodes.size() : index, (TreeNode) component);
	}

	@Override
	public void removeComponent(SPUIComponent component) {
		childrenNodes.remove(component);
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			for (TreeNode childrenNode : childrenNodes) {
				if (childrenNode == component) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		for (TreeNode childrenNode : childrenNodes) {
			if (childrenNode != null) {
				childrenNode.getComponents(resultList, filter);
			}
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.startsWith("childrenNodes ")) {
				childrenNodes.add(Integer.parseInt(value.split(" ", 2)[1]), (TreeNode) removeAction.getRemovedComponent());
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		for (int i = 0; i < childrenNodes.size(); i++) {
			if (propagate && childrenNodes.get(i) != null) {
				childrenNodes.get(i).removeComponent(removeAction, propagate);
			}
			if (childrenNodes.get(i) == removedComp) {
				modifiedValues.add("childrenNodes " + i);
				childrenNodes.remove(i);
			}
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		this.viewer = viewer;
		for (TreeNode node : childrenNodes) {
			node.setSPUIViewer(viewer);
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer) {
			@Override
			public boolean isValid(DesignerElement element) {
				if (!super.isValid(element)) {
					return false;
				}
				if (element instanceof DesignerProperty && ((DesignerProperty) element).getProxyID() == 0xF0310001) {
					return false;
				}
				return true;
			}
		};
	}
	
}
