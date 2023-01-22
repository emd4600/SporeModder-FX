package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerElement;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;

public class WinTreeView extends Window {
	
	public static final int TYPE = 0xF02AFF4B;
	
	private final List<TreeNode> childrenNodes = new ArrayList<TreeNode>();
	
	// possibly colors?
	private final Color[] colors = new Color[9];
	private final float[] borders = new float[4];

	public WinTreeView(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		addUnassignedInt(block, 0x302A0000, 0);
		
		parseMarginsSections(block, 0x302A0001, borders);

		int[] p_302A0002 = SectionInt.getValues(block, 0x302A0002, new int[colors.length], colors.length);
		for (int i = 0; i < p_302A0002.length; i++) {
			colors[i] = PanelUtils.decodeColor(p_302A0002[i]);
		}
		
		short[] indices = SectionShort.getValues(block.getSection(0x302A0003, SectionShort.class), null, -1);
		if (indices != null) {
			for (int i = 0; i < indices.length; i++) {
				TreeNode node = (TreeNode) ResourceLoader.getComponent(block.getParent().get(indices[i]));
				childrenNodes.add(node);
			}
		}
		
		addUnassignedFloat(block, 0x302A0004, 0.0f);
		addUnassignedShort(block, 0x302A0005, null);  // DefaultIcon
		addUnassignedShort(block, 0x302A0006, null);  // TreeExpanderDrawable
		addUnassignedInt(block, 0x302A0007, 0);
		addUnassignedInt(block, 0x302A0008, 0);
		addUnassignedInt(block, 0x302A0009, 0);
		addUnassignedInt(block, 0x302A000A, 0);
		addUnassignedInt(block, 0x302A000B, 0);
		addUnassignedInt(block, 0x302A000C, 0);
		addUnassignedShort(block, 0x302A000D, null);
		addUnassignedShort(block, 0x302A000E, null);
		
	}
	
	public WinTreeView(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x302A0000, (int) 0);
		unassignedProperties.put(0x302A0004, (float) 0);
		unassignedProperties.put(0x302A0005, null);
		unassignedProperties.put(0x302A0006, null);
		unassignedProperties.put(0x302A0007, (int) 0);
		unassignedProperties.put(0x302A0008, (int) 0);
		unassignedProperties.put(0x302A0009, (int) 0);
		unassignedProperties.put(0x302A000A, (int) 0);
		unassignedProperties.put(0x302A000B, (int) 0);
		unassignedProperties.put(0x302A000C, (int) 0);
		unassignedProperties.put(0x302A000D, null);
		unassignedProperties.put(0x302A000E, null);
		
		TreeNode rootNode = new TreeNode(viewer);
		childrenNodes.add(rootNode);
		
		colors[0] = new Color(0xFF000000);
		colors[1] = new Color(0xFFFFFFFF);
		colors[2] = new Color(0xFF808080);
		colors[3] = new Color(0xFFFFFFFF);
		colors[4] = new Color(0xFF000000);
		colors[5] = new Color(0xFFC0C0FF);
		colors[6] = new Color(0xFF0000FF);
		colors[7] = new Color(0xFFFFFFFF);
		colors[8] = new Color(0x30000000);
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		saveInt(builder, block, 0x302A0000);
		
		builder.addSectionList(block, 0x302A0001, new ListSectionContainer[] {saveMarginsSections(borders)}, 50);
		
		int[] colorValues = new int[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colorValues[i] = PanelUtils.encodeColor(colors[i]);
		}
		builder.addInt(block, 0x302A0002, colorValues);
		
		if (!childrenNodes.isEmpty()) {
			SPUIObject[] objects = new SPUIObject[childrenNodes.size()];
			for (int i = 0; i < objects.length; i++) {
				if (childrenNodes.get(i) != null) {
					objects[i] = builder.addComponent(childrenNodes.get(i));
				}
			}
			builder.addReference(block, 0x302A0003, objects);
		}
		
		saveFloat(builder, block, 0x302A0004);
		saveReference(builder, block, 0x302A0005);
		saveReference(builder, block, 0x302A0006);
		saveInt(builder, block, 0x302A0007);
		saveInt(builder, block, 0x302A0008);
		saveInt(builder, block, 0x302A0009);
		saveInt(builder, block, 0x302A000A);
		saveInt(builder, block, 0x302A000B);
		saveInt(builder, block, 0x302A000C);
		saveReference(builder, block, 0x302A000D);
		saveReference(builder, block, 0x302A000E);
		
		return block;
	}

	private WinTreeView() {
		super();
	}
	
	@Override
	public WinTreeView copyComponent(boolean propagateIndependent) {
		WinTreeView other = new WinTreeView();
		copyComponent(other, propagateIndependent);
		
		if (childrenNodes != null) {
			for (int i = 0; i < childrenNodes.size(); i++) {
				TreeNode child = childrenNodes.get(i);
				other.childrenNodes.add(child == null ? null : child.copyComponent(propagateIndependent));
				other.childrenNodes.get(i).setTreeView(other);
			}
		}
		
		System.arraycopy(borders, 0, other.borders, 0, borders.length);
		System.arraycopy(colors, 0, other.colors, 0, colors.length);
		
		return other;
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		super.setSPUIViewer(viewer);
		
		for (TreeNode child : childrenNodes) {
			child.setTreeView(this);
			child.setSPUIViewer(viewer);
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
	public boolean nodeAcceptsComponent(SPUIComponent other) {
		return other instanceof TreeNode;
	}
	
	@Override
	public void insertComponent(SPUIComponent component, int index) {
		if (component instanceof TreeNode)
		{
			childrenNodes.add(index == -1 ? children.size() : index, (TreeNode) component);
		}
		else 
		{
			super.insertComponent(component, index);
		}
	}

	@Override
	public void removeComponent(SPUIComponent component) {
		childrenNodes.remove(component);
	}
	
	public List<TreeNode> getChildrenNodes() {
		return childrenNodes;
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
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer) {
			@Override
			public boolean isValid(DesignerElement element) {
				if (!super.isValid(element)) {
					return false;
				}
				if (element instanceof DesignerProperty && ((DesignerProperty) element).getProxyID() == 0x302A0003) {
					return false;
				}
				return true;
			}
			@Override
			public Object getValue(DesignerProperty property) {
				switch (property.getProxyID()) {
				
				case 0x302A0001: return borders;
				case 0x302A0002: return colors;
				}
				
				return super.getValue(property);
			}
			
			@Override
			public void setValue(DesignerProperty property, Object value, int index) {
				switch (property.getProxyID()) {
				
				case 0x302A0001: System.arraycopy((float[]) value, 0, borders, 0, borders.length); break;
				case 0x302A0002: colors[index] = (Color) value; break;
				}
				
				super.setValue(property, value, index);
			}
		};
	}
	
	@Override
	protected boolean shouldUseFillColor() {
		return false;
	}
}
