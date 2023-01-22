package sporemodder.extras.spuieditor.components;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.extras.spuieditor.PanelUtils.TextFieldValueAction;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.UIDesigner;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.ListSectionContainer;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionSectionList;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionFloat;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUISection;
import sporemodder.utilities.Hasher;

public abstract class SPUIDefaultComponent extends PropertyObject implements SPUIComponent {
	
	public static final int X = 0;
	public static final int Y = 1;
	
	public static final int HORIZONTAL = 1;
	public static final int VERTICAL = 2;  // it also works for 0 (and probably any other value)
	
	public static final int LEFT = 0;
	public static final int TOP = 1;
	public static final int RIGHT = 2;
	public static final int BOTTOM = 3;
	
	public final static int INDEX_IDLE = 0;
	public final static int INDEX_DISABLED = 1;
	public final static int INDEX_HOVER = 2;
	public final static int INDEX_CLICKED = 3;
	public final static int INDEX_SELECTED = 4;
	public final static int INDEX_SELECTED_DISABLED = 5;
	public final static int INDEX_SELECTED_HOVER = 6;
	public final static int INDEX_SELECTED_CLICK = 7;
	
	private static final String SPUI_EDITOR_TAG = "SPUI_EDITOR_TAG";
	private static final int SPUI_EDITOR_TAG_PROPERTY = Hasher.stringToFNVHash(SPUI_EDITOR_TAG);

	protected SPUIObject object;
	protected SPUIViewer viewer;
	protected DefaultMutableTreeNode node;
	
	protected String componentTag;
	
	public SPUIDefaultComponent(SPUIObject object) throws InvalidBlockException {
		this.object = object;
		
		if (object instanceof SPUIBlock) {
			LocalizedText[] text = SectionText.getValues(((SPUIBlock) object).getSection(SPUI_EDITOR_TAG_PROPERTY, SectionText.class), null, 1);
			if (text != null) {
				componentTag = text[0].text;
			}
		}
	}
	
	protected SPUIDefaultComponent() {
		
	}

	public SPUIDefaultComponent(SPUIViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public SPUIObject getObject() {
		return object;
	}
	
	protected StringBuilder getBasicNameBuilder() {
		StringBuilder sb = new StringBuilder();
		
//		if (object != null) {
//			int index = object.getBlockIndex();
//			if (index != -1) {
//				sb.append(object.getBlockIndex());
//				sb.append(":");
//			}
//			sb.append(object.getTypeString());
//		}
//		else {
//			sb.append(getClass().getSimpleName());
//		}
		
		if (object != null) {
			int index = object.getBlockIndex();
			if (index != -1) {
				sb.append(object.getBlockIndex());
				sb.append(":");
			}
		}
		sb.append(getClass().getSimpleName());
		
		return sb;
	}
	
	protected void addComponentTagName(StringBuilder sb) {
		if (componentTag != null && !componentTag.isEmpty()) {
			sb.append(" [");
			sb.append(componentTag);
			sb.append("]");
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = getBasicNameBuilder();
		
		addComponentTagName(sb);
			
		return sb.toString();
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder)	{
		object = builder.createBlock(getType(), false);
		((SPUIBlock) object).getSections().clear();
		if (componentTag != null && !componentTag.isEmpty()) {
			builder.addText((SPUIBlock) object, SPUI_EDITOR_TAG_PROPERTY, new LocalizedText[] {new LocalizedText(componentTag)});
		}
		
		return object;
	}
	
	protected void setDefaultPropertiesPanel(PropertiesPanel panel) {
		JTextField tfTag = (JTextField) panel.addTextFieldValue("Tag", componentTag, new TextFieldValueAction() {
			@Override
			public void documentModified(DocumentEvent event, String textFieldText) {
				componentTag = textFieldText;
				viewer.getEditor().updateHierarchyTree();
			}
		}, viewer.getEditor()).components[0];
		
		tfTag.setColumns(20);
		
		// we shouldn't be using the SPUI reg anymore
		// Condition.setObject(getType());
	}
	
//	@Override
//	public PropertiesPanel getSimplePropertiesPanel() {
//		PropertiesPanel panel = new PropertiesPanel();
//		
//		setDefaultPropertiesPanel(panel);
//		
//		return panel;
//	}
	
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new DefaultDesignerDelegate(viewer);
	}
	
	protected PropertiesPanel createBasicPanel() {
		PropertiesPanel panel = new PropertiesPanel(); 
		setDefaultPropertiesPanel(panel);
		return panel;
	}
	
	@Override
	public PropertiesPanel getPropertiesPanel() {
		PropertiesPanel panel = createBasicPanel();
		
		DesignerClass clazz = UIDesigner.Designer.getClass(getType());
		if (clazz != null) {
			clazz.fillPropertiesPanel(panel, getDesignerClassDelegate(), viewer.getEditor());
		}
		
		return panel;
	}

	@Override
	public SPUIViewer getSPUIViewer() {
		return viewer;
	}

	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		this.viewer = viewer;
		for (Map.Entry<Integer, Object> entry : unassignedProperties.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof SPUIComponent) {
				((SPUIComponent) value).setSPUIViewer(viewer);
			}
		}
	}
	
	@Override
	public TreePath getHierarchyTreePath() {
		if (node != null) {
			return new TreePath(node.getPath());
		} else {
			return null;
		}
	}
	
	public MutableTreeNode getNode() {
		return node;
	}
	
	@Override
	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index)
	{
		node = new DefaultMutableTreeNode(this);
		
		model.insertNodeInto(node, parent, index);
		
		return node;
	}
	
	@Override
	public int getType() {
//		if (object != null) {
//			return object.getObjectType();
//		}
		
		try {
			Field field = this.getClass().getDeclaredField("TYPE");
			if (field != null) {
				return field.getInt(null);
			}
			
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	
	public String getComponentTag() {
		return componentTag;
	}

	public void setComponentTag(String componentTag) {
		this.componentTag = componentTag;
	}

	protected void copyComponent(SPUIDefaultComponent other, boolean propagateIndependent) {
		other.unassignedProperties.clear();
		other.unassignedProperties.putAll(unassignedProperties);
		
		// children are always propagated, and all properties are children (you must not use them if not!)
		for (Map.Entry<Integer, Object> entry : unassignedProperties.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof SPUIComponent) {
				other.unassignedProperties.put(entry.getKey(), ((SPUIComponent) value).copyComponent(propagateIndependent));
			}
		}
		
		other.componentTag = componentTag;
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		for (Map.Entry<Integer, Object> entry : unassignedProperties.entrySet()) {
			if (entry.getValue() == component) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		if (filter.accept(this)) {
			resultList.add(this);
		}
		for (Map.Entry<Integer, Object> entry : unassignedProperties.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof SPUIComponent) {
				((SPUIComponent) value).getComponents(resultList, filter);
			}
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.startsWith("unassigned ")) {
				unassignedProperties.put(Integer.parseInt(value.split(" ")[1]), removeAction.getRemovedComponent());
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		List<String> modifiedValues = new ArrayList<String>();
		
		for (Map.Entry<Integer, Object> entry : unassignedProperties.entrySet()) {
			if (propagate && entry.getValue() instanceof SPUIComponent) {
				((SPUIComponent) entry.getValue()).removeComponent(removeAction, propagate);
			}
			if (entry.getValue() == removedComp) {
				entry.setValue(null);
				modifiedValues.add("unassigned " + Integer.toString(entry.getKey()));
			}
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	public static void parseMarginsSections(SPUIBlock block, int property, float[] result) throws InvalidBlockException {
		//ListSectionContainer[] marginsSections = SectionSectionList.getValues(block.getSection(property, SectionSectionList.class), null, 1, 50);
		ListSectionContainer[] marginsSections = SectionSectionList.getValues(block.getSection(property, SectionSectionList.class), null, 1, -1);
		if (marginsSections != null && marginsSections.length > 0 && !marginsSections[0].getSections().isEmpty()) {
			for (int i = 0; i < 4; i++) {
				result[i] = ((SectionFloat) marginsSections[0].getSection(i + 1)).data[0];
			}
		}
	}
	
	public static ListSectionContainer saveMarginsSections(float[] margins) {
		ListSectionContainer marginSections = new ListSectionContainer();
		List<SPUISection> sections = marginSections.getSections();
		
		for (int i = 0; i < margins.length; i++) {
			SectionFloat sec = new SectionFloat();
			sec.setChannel(i + 1);
			sec.setCount(1);
			sec.data = new float[] {margins[i]};
			sections.add(sec);
		}
		
		return marginSections;
	}
	
	public static String getConstantString(int constant) {
		switch(constant) {
		case LEFT: return "Left";
		case TOP: return "Top";
		case RIGHT: return "Right";
		case BOTTOM: return "Bottom";
		default: return null;
		}
	}
	public static String getConstantPointString(int constant) {
		switch(constant) {
		case X: return "X";
		case Y: return "Y";
		default: return null;
		}
	}
	
	@Override
	public boolean nodeIsMovable() {
		return false;
	}
	
	@Override
	public boolean nodeAcceptsComponent(SPUIComponent other) {
		return false;
	}
	
	@Override
	public boolean nodeCanBeMovedAbove() {
		return false;
	}
	
	@Override
	public boolean nodeCanBeMovedBelow() {
		return false;
	}
	
	@Override
	public void insertComponent(SPUIComponent component, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeComponent(SPUIComponent component) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSPUIComponent() {
		return true;
	}
	
}
