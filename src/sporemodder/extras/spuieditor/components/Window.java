package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.ComponentChooser.ComponentChooserCallback;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PanelUtils.PropertyInfo;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.RemoveComponentAction;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.StyleSheet;
import sporemodder.extras.spuieditor.StyleSheetInstance;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass.DesignerClassDelegate;
import sporemodder.extras.spuieditor.uidesigner.DesignerElement;
import sporemodder.extras.spuieditor.uidesigner.DesignerProperty;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionIntName;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec4;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.Hasher;

public class Window extends SPUIDefaultComponent implements WinComponent {
	
	public static final int TYPE = 0x4EC1B8D8;
	
	protected String name;  // originally an int, we'll convert back when setting the block
	protected int flags;
	protected Color tintColor = Color.white;
	protected Color backgroundColor = new Color(0, 0, 0, 0);
	protected final Rectangle bounds = new Rectangle();
	protected final Rectangle realBounds = new Rectangle();
	protected LocalizedText caption;
	
	protected StyleSheetInstance style;
	protected String styleName;
	
	protected WinComponent parent;
	protected final List<WinComponent> children = new ArrayList<WinComponent>();
	protected final List<SPUIWinProc> modifiers = new ArrayList<SPUIWinProc>();
	protected SPUIDrawable drawable;
	
	protected PropertyInfo[] areaProperties;
	
	private boolean isLayoutWindow;
	
	public Window(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);
		
		if (block == null) {
			return;
		}
		
		// TODO Auto-generated constructor stub
		
		flags = SectionInt.getValues(block, 0xEEC1B000, new int[] { 0 }, 1)[0];
		name = SectionIntName.getValues(block, 0xEEC1B001, new String[] { null }, 1)[0];
		if (name != null) {
			if (name.equals("0") || name.length() == 0) {
				// no text looks better
				name = null;
			}
			else if (name.startsWith("$")) {
				name = name.substring(1);
			}
		}
		tintColor = PanelUtils.decodeColor(SectionInt.getValues(block, 0xEEC1B004, new int[] { 0 }, 1)[0]);
		backgroundColor = decodeColor(SectionInt.getValues(block, 0xEEC1B006, new int[] { 0 }, 1)[0]);
		
		caption = SectionText.getValues(block.getSection(0xEEC1B00A, SectionText.class), new LocalizedText[] { null }, 1)[0];
		
		int styleID = SectionInt.getValues(block, 0xEEC1B00E, new int[] { 0 }, 1)[0];
		style = StyleSheet.getActiveStyleSheet().getInstance(styleID);
		styleName = style == null ? Hasher.getFileName(styleID) : style.getName();
		
		float[] boundCoords = SectionVec4.getValues(block.getSection(0xEEC1B005, SectionVec4.class), new float[][] { new float[] {0, 0, 0, 0} }, 1)[0];
		bounds.x = (int) boundCoords[0];
		bounds.y = (int) boundCoords[1];
		bounds.width = (int) (boundCoords[2] - boundCoords[0]);
		bounds.height = (int) (boundCoords[3] - boundCoords[1]);
		
		short[] childrenIndices = SectionShort.getValues(block.getSection(0xEEC1B00B, SectionShort.class), new short[] {}, -1);
		
		for (short index : childrenIndices) {
			WinComponent win = (WinComponent) ResourceLoader.getComponent(block.getParent().get(index));
			if (win != null) {
				children.add(win);
				win.setParent(this);
				win.setSPUIViewer(viewer);
			}
		}
		
		short[] modifierIndices = SectionShort.getValues(block.getSection(0xEEC1B00C, SectionShort.class), new short[] {}, -1);
		
		for (short index : modifierIndices) {
			SPUIWinProc mod = (SPUIWinProc) ResourceLoader.getComponent(block.getParent().get(index));
			if (mod != null) {
				modifiers.add(mod);
				mod.setParent(this);
				mod.setSPUIViewer(viewer);
			}
		}
		
		short drawableIndex = SectionShort.getValues(block.getSection(0xEEC1B007, SectionShort.class), new short[] {-1}, 1)[0];
		if (drawableIndex != -1) {
			drawable = (SPUIDrawable) ResourceLoader.getComponent(block.getParent().get(drawableIndex)); 
			if (drawable != null) {
				drawable.setSPUIViewer(viewer);
			}
		}
		
		addUnassignedIntName(block, 0xEEC1B002, null);
		addUnassignedInt(block, 0xEEC1B003);
		addUnassignedInt(block, 0xEEC1B009);
	}
	
	public Window(SPUIViewer viewer) {
		super(viewer);
		flags = FLAG_VISIBLE | FLAG_ENABLED;
		bounds.width = 100;
		bounds.height = 100;
		realBounds.setBounds(bounds);
		tintColor = Color.white;
		backgroundColor = new Color(0, 0, 0, 0);
		styleName = "DefaultStyle";
		style = StyleSheet.getActiveStyleSheet().getInstance(styleName);
		
		unassignedProperties.put(0xEEC1B002, null);
		unassignedProperties.put(0xEEC1B003, 0);
		unassignedProperties.put(0xEEC1B009, 0);
	}
	
	public Window(SPUIViewer viewer, boolean isLayoutWindow) {
		super(viewer);
		this.isLayoutWindow = isLayoutWindow;
	}
	
	@Override
	public boolean isLayoutWindow() {
		return isLayoutWindow;
	}

	public void setLayoutWindow(boolean isLayoutWindow) {
		this.isLayoutWindow = isLayoutWindow;
	}

	@Override
	public List<WinComponent> getChildren() {
		return children;
	}

	@Override
	public List<SPUIWinProc> getModifiers() {
		return modifiers;
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
		
		if (parent == null || (parent instanceof Window && ((Window) parent).isLayoutWindow)) {
			block.setIsRoot(true);
		}
		
		builder.addInt(block, 0xEEC1B000, new int[] {flags});
		//builder.addInt(block, 0xEEC1B001, new int[] {name == null ? 0 : Hasher.getFileHash(name)});
		builder.addIntName(block, 0xEEC1B001, new String[] {name});
		
		saveIntName(builder, block, 0xEEC1B002);
		if (unassignedProperties.containsKey(0xEEC1B003)) {
			saveInt(builder, block, 0xEEC1B003);
		}
		
		builder.addVec4(block, 0xEEC1B005, new float[][] {new float[] {bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height}});
		if (caption != null) {
			builder.addText(block, 0xEEC1B00A, new LocalizedText[] {caption});
		}
		builder.addInt(block, 0xEEC1B006, new int[] {PanelUtils.encodeColor(backgroundColor)});
		builder.addInt(block, 0xEEC1B004, new int[] {PanelUtils.encodeColor(tintColor)});
		if (drawable != null) {
			builder.addReference(block, 0xEEC1B007, new SPUIObject[] {builder.addComponent(drawable)});
		}
		
		if (unassignedProperties.containsKey(0xEEC1B009)) {
			saveInt(builder, block, 0xEEC1B009);
		}
		
		builder.addInt(block, 0xEEC1B00E, new int[] {style == null ? Hasher.getFileHash(styleName) : style.getStyleID()});
		
		if (!modifiers.isEmpty()) {
			SPUIObject[] savedModifiers = new SPUIObject[modifiers.size()];
			for (int i = 0; i < savedModifiers.length; i++) {
				if (modifiers.get(i) != null) {
					savedModifiers[i] = builder.addComponent(modifiers.get(i));
				}
			}
			builder.addReference(block, 0xEEC1B00C, savedModifiers);
		}
		if (!children.isEmpty()) {
			SPUIObject[] savedChildren = new SPUIObject[children.size()];
			for (int i = 0; i < savedChildren.length; i++) {
				if (children.get(i) != null) {
					savedChildren[i] = builder.addComponent(children.get(i));;
				}
			}
			builder.addReference(block, 0xEEC1B00B, savedChildren);
		}
		
		return block;
	}
	
	protected Window() {
		super();
	}
	
	protected void copyComponent(Window other, boolean propagateIndependent) {
		super.copyComponent(other, propagateIndependent);
		other.flags = flags;
		other.name = name;
		other.viewer = viewer;
		if (caption != null) {
			other.caption = new LocalizedText(caption);
		}
		other.bounds.setRect(bounds);
		other.realBounds.setRect(realBounds);
		other.tintColor = new Color(tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue(), tintColor.getAlpha());
		other.backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), backgroundColor.getAlpha());
		other.style = style;
		other.styleName = styleName;
		
		for (WinComponent child : children) {
			WinComponent copy = child.copyComponent(propagateIndependent);
			copy.setParent(other);
			other.children.add(copy);
		}
		for (SPUIWinProc mod : modifiers) {
			SPUIWinProc copy = mod.copyComponent(propagateIndependent);
			copy.setParent(other);
			other.modifiers.add(copy);
		}
		if (drawable != null) {
			other.drawable = propagateIndependent ? drawable.copyComponent(propagateIndependent) : drawable;
		}
	}
	
	@Override
	public Window copyComponent(boolean propagateIndependent) {
		Window other = new Window();
		
		copyComponent(other, propagateIndependent);
		
		return other;
	}
	
	@Override
	public boolean isMoveable() {
		return !viewer.isPreview();
	}
	
	protected boolean shouldSkipPaint() {
		// previews don't show invisible components
		return !isLayoutWindow && (viewer.isPreview() || !viewer.getShowInvisibleComponents()) && (flags & FLAG_VISIBLE) != FLAG_VISIBLE;
	}
	
	protected boolean shouldUseFillColor() {
		return true;
	}
	
	protected void paintBasic(Graphics2D graphics, Rectangle drawBounds) {
		if (shouldUseFillColor()) {
			graphics.setColor(new Color(
					(backgroundColor.getRed() * tintColor.getRed()) / 255, 
					(backgroundColor.getGreen() * tintColor.getGreen()) / 255,
					(backgroundColor.getBlue() * tintColor.getBlue()) / 255,
					(backgroundColor.getAlpha() * tintColor.getAlpha()) / 255));
			
			graphics.fill(drawBounds);
		}
		
		if (drawable != null) {
			drawable.draw(graphics, drawBounds, this);
		}
	}
	
	protected void paintChildren(Graphics2D graphics) {
		ListIterator<WinComponent> it = children.listIterator(children.size());
		while (it.hasPrevious()) {
			it.previous().paintComponent(graphics);
		}
	}
	
	@Override
	public void paintComponent(Graphics2D graphics) {
		if (shouldSkipPaint()) {
			return;
		}
		
		Rectangle drawBounds = new Rectangle(realBounds);
		Graphics2D drawGraphics = graphics;
		
		for (SPUIWinProc modifier : modifiers) {
			drawGraphics = modifier.modifyPreRender(this, drawGraphics, drawBounds);
		}
		
		paintBasic(drawGraphics, drawBounds);
		paintChildren(drawGraphics);
		
		for (SPUIWinProc modifier : modifiers) {
			modifier.modifyPostRender(this, graphics, drawBounds);
		}
	}
	
	@Override
	public void revalidate() {
		//TODO use layout ?
		realBounds.setBounds(bounds);
		if (parent != null) {
			if (parent.getRealBounds() != null) {
				realBounds.translate(parent.getRealBounds().x, parent.getRealBounds().y);
			}
			for (SPUIWinProc modifier : modifiers) {
				modifier.modify(this);
			}
		} else {
			// root windows always start at 0 0
			realBounds.setLocation(0, 0);
		}
		
		for (WinComponent child : children) {
			child.revalidate();
		}
	}
	
	protected static Color decodeColor(int value) {
		return new Color(
				(int) (value & 0xFF0000) >> 16, 
				(int) (value & 0xFF00) >> 8, 
				(int) value & 0xFF, 
				(int) (((long) value & 0xFF000000L) >> 24));
	}
	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		if (viewer != null) {
			if (viewer.getEditor() != null) {
				viewer.getEditor().updateHierarchyTree();
				// since this changes the toString() result, we have to change the selected path
				JTree tree = viewer.getEditor().getHierarchyTree();
				tree.setSelectionPath(getHierarchyTreePath());
			}
		}
	}
	
	@Override
	public Rectangle getRealBounds() {
		return realBounds;
	}
	
	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(Rectangle bounds) {
		this.bounds.setRect(bounds);
		revalidate();
		updatePropertiesPanelBounds();
		if (viewer != null) {
			viewer.repaint();
		}
	}
	
	@Override
	public void translate(int dx, int dy) {
		bounds.translate(dx, dy);
		revalidate();
		updatePropertiesPanelBounds();
		if (viewer != null) {
			viewer.repaint();
		}
	}
	
	private void updatePropertiesPanelBounds() {
		if (areaProperties != null) {
			((JSpinner) areaProperties[0].components[0]).setValue((float) bounds.x);
			((JSpinner) areaProperties[1].components[0]).setValue((float) bounds.y);
			((JSpinner) areaProperties[2].components[0]).setValue((float) (bounds.x + bounds.width));
			((JSpinner) areaProperties[3].components[0]).setValue((float) (bounds.y + bounds.height));
		}
	}

	@Override
	public Color getTintColor() {
		return tintColor;
	}

	@Override
	public void setTintColor(Color color) {
		tintColor = color;
		if (viewer != null) {
			viewer.repaint();
		}
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public void setBackgroundColor(Color color) {
		backgroundColor = color;
		if (viewer != null) {
			viewer.repaint();
		}
	}
	
	@Override
	public StyleSheetInstance getStyle() {
		return style;
	}

	@Override
	public void setStyle(StyleSheetInstance style) {
		StyleSheetInstance oldStyle = this.style;
		this.style = style;
		
		if (oldStyle != style) {
//			if (propertiesPanel != null) {
//				propertiesPanel.tfStyle.setText(style == null ? Hasher.style.getName());
//			}
			if (viewer != null) {
				viewer.repaint();
			}
		}
		
	}

	@Override
	public int getFlags() {
		return flags;
	}
	
	@Override
	public void setFlag(int flag, boolean value) {
		int oldFlags = flags;
		
		flags &= ~flag;
		if (value) {
			flags |= flag;
		}
		
		// only need to repaint if the value was actually changed
		if (oldFlags != flags && viewer != null) {
			viewer.repaint();
		}
	}

	@Override
	public LocalizedText getText() {
		return caption;
	}

	@Override
	public void setText(LocalizedText text) {
		this.caption = text;
		if (viewer != null) {
			viewer.repaint();
			if (viewer.getEditor() != null) {
				viewer.getEditor().updateHierarchyTree();
			}
		}
	}
	
	@Override
	public WinComponent getParent() {
		return parent;
	}
	
	@Override
	public void setParent(WinComponent parent) {
		this.parent = parent;
	}
	
	@Override
	public void setSPUIViewer(SPUIViewer viewer) {
		this.viewer = viewer;
		for (WinComponent comp : children) {
			comp.setSPUIViewer(viewer);
		}
		for (SPUIWinProc mod : modifiers) {
			mod.setSPUIViewer(viewer);
		}
		if (drawable != null) {
			drawable.setSPUIViewer(viewer);
		}
	}
	
	@Override
	public boolean usesComponent(SPUIComponent component) {
		if (super.usesComponent(component) == true) {
			return true;
		}
		else {
			for (WinComponent comp : children) {
				if (comp == component) {
					return true;
				}
			}
			for (SPUIWinProc mod : modifiers) {
				if (mod == component) {
					return true;
				}
			}
			if (drawable == component) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void getComponents(List<SPUIComponent> resultList, SPUIComponentFilter filter) {
		super.getComponents(resultList, filter);
		
		for (WinComponent comp : children) {
			comp.getComponents(resultList, filter);
		}
		for (SPUIWinProc mod : modifiers) {
			mod.getComponents(resultList, filter);
		}
		if (drawable != null) {
			drawable.getComponents(resultList, filter);
		}
	}
	
	@Override
	public void restoreRemovedComponent(RemoveComponentAction removeAction) {
		super.restoreRemovedComponent(removeAction);
		
		List<String> modifiedValues = removeAction.getModifiedValues(this);
		
		for (String value : modifiedValues) {
			if (value.startsWith("CHILDREN")) {
				String[] splits = value.split(" ", 2);
				children.add(Integer.parseInt(splits[1]), (WinComponent) removeAction.getRemovedComponent());
			}
			else if (value.startsWith("MODIFIER")) {
				String[] splits = value.split(" ", 2);
				modifiers.add(Integer.parseInt(splits[1]), (SPUIWinProc) removeAction.getRemovedComponent());
			}
			else if (value.equals("DRAWABLE")) {
				drawable = (SPUIDrawable) removeAction.getRemovedComponent();
			}
		}
	}
	
	@Override
	public List<String> removeComponent(RemoveComponentAction removeAction, boolean propagate) {
		List<String> modifiedValues = super.removeComponent(removeAction, propagate);
		SPUIComponent removedComp = removeAction.getRemovedComponent();
		
		for (int i = 0; i < children.size(); i++) {
			if (propagate && children.get(i) != null) {
				children.get(i).removeComponent(removeAction, propagate);
			}
			if (children.get(i) == removedComp) {
				modifiedValues.add("CHILDREN " + i /*+ " " + node.getIndex(children.get(i).getNode())*/);
				children.remove(i);
			}
		}
		for (int i = 0; i < modifiers.size(); i++) {
			if (propagate && modifiers.get(i) != null) {
				modifiers.get(i).removeComponent(removeAction, propagate);
			}
			if (modifiers.get(i) == removedComp) {
				modifiedValues.add("MODIFIER " + i /*+ " " + node.getIndex(modifiers.get(i).getNode())*/);
				modifiers.remove(i);
			}
		}
		if (drawable != null && propagate) {
			drawable.removeComponent(removeAction, propagate);
		}
		if (drawable == removedComp) {
			modifiedValues.add("DRAWABLE");
			drawable = null;
		}
		
		removeAction.putModifiedComponent(this, modifiedValues);
		
		return modifiedValues;
	}
	
	@Override
	public WinComponent getComponentInCoords(Point p) {
		
		// we don't want to select invisible components,
		// but we do want to be able to select the root component
		if (viewer != null && !viewer.getShowInvisibleComponents() /*&& parent != null*/ &&
				(flags & FLAG_VISIBLE) != FLAG_VISIBLE) {
			return null;
		}
		
		WinComponent result = null;
		Rectangle resultBounds = null;
		
		ListIterator<WinComponent> iterator = children.listIterator(children.size());
		
		while (iterator.hasPrevious()) {
			WinComponent child = iterator.previous();
			WinComponent comp = child.getComponentInCoords(p);
			if (comp == null) {
				continue;
			}
			
			Rectangle compBounds = comp.getRealBounds();
			if (result == null || (
					compBounds.width <= resultBounds.width &&
							compBounds.height <= resultBounds.height)) {
				result = comp;
				resultBounds = compBounds;
			}
		}
		
		if (result != null) {
			return result;
		}
		
		if (realBounds.contains(p) && viewer.getActiveComponent() != this) {
			return this;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ActionableComponent> T getComponentInCoords(Point p, Class<T> type) {
		// we don't want to select invisible components,
		// but we do want to be able to select the root component
		if (viewer != null && !viewer.getShowInvisibleComponents() && parent != null &&
				(flags & FLAG_VISIBLE) != FLAG_VISIBLE) {
			return null;
		}
		
		ActionableComponent result = null;
		Rectangle resultBounds = null;
		
		for (WinComponent child : children) {
			ActionableComponent comp = child.getComponentInCoords(p, type);
			if (comp == null) {
				continue;
			}
			
			Rectangle compBounds = comp.getRealBounds();
			if (result == null || (
					compBounds.width <= resultBounds.width &&
							compBounds.height <= resultBounds.height)) {
				result = comp;
				resultBounds = compBounds;
			}
		}
		
		if (result != null) {
			return (T) result;
		}
		
		if (realBounds.contains(p) && type.isInstance(this)) {
			if (drawable == null || drawable.isValidPoint(p, realBounds)) {
				return (T) this;
			}
		}
		return null;
	}
	
	@Override
	public void removeChildComponent(SPUIComponent component) {
		if (component instanceof SPUIWinProc) {
			modifiers.remove((SPUIWinProc) component);
		}
		else if (component instanceof WinComponent) {
			children.remove((WinComponent) component);
		}
		
		revalidate();
	}
	
	@Override
	public void insertChildComponent(SPUIComponent component, int childIndex) {
		if (component instanceof SPUIWinProc) {
			((SPUIWinProc) component).setParent(this);
			modifiers.add(Math.min(childIndex, modifiers.size()), (SPUIWinProc) component);
		}
		else if (component instanceof WinComponent) {
			// children windows are set after modifiers, therefore we must fix the index
			int index = childIndex;
			if (index <= modifiers.size()) {
				index = 0;
			}
			else if (index >= modifiers.size() + children.size()) {
				index = children.size();
			}
			else {
				index -= modifiers.size();
				
				// the 'index' received doesn't care about invisible components; we fix that here
				int realIndex = index;
				if (!viewer.getShowInvisibleComponents()) {
					int visibleIndex = 0;
					for (int i = 0; i < children.size(); i++) {
						WinComponent child = children.get(i);
						if ((child.getFlags() & FLAG_VISIBLE) != 0) {
							visibleIndex++;
						}
						else {
							realIndex++;
						}
						
						// stop if we have examined all the visible indexes until the one specified
						if (visibleIndex == index) {
							break;
						}
					}
					
					index = realIndex;
				}
			}
			
			((WinComponent) component).setParent(this);
			children.add(index, (WinComponent) component);
		}
		
		revalidate();
	}
	
	@Override
	public int getIndexOfChild(SPUIComponent component) {
		if (component == null) {
			return -1;
		}
		if (component instanceof SPUIWinProc) {
			return modifiers.indexOf(component);
		}
		else if (component instanceof WinComponent) {
			return modifiers.size() + children.indexOf(component);
		}
		return -1;
	}

	@Override
	public MutableTreeNode fillHierarchyTree(DefaultTreeModel model, MutableTreeNode parent, int index) {
		if (isLayoutWindow) {
			node = new DefaultMutableTreeNode(this);
			model.setRoot(node);
		}
		else {
			super.fillHierarchyTree(model, parent, index);
		}
		
		int ind = 0;
		
		for (int i = 0; i < modifiers.size(); i++) {
			if (modifiers.get(i) != null) {
				modifiers.get(i).fillHierarchyTree(model, node, ind++);
			}
		}
		
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) != null) {
				children.get(i).fillHierarchyTree(model, node, ind++);
			}
		}
		
		return node;
	}
	
	@Override
	public boolean nodeIsMovable() {
		if (isLayoutWindow) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean nodeAcceptsComponent(SPUIComponent other) {
		return other instanceof WinComponent || other instanceof SPUIWinProc;
	}
	
	@Override
	public boolean nodeCanBeMovedAbove() {
		if (isLayoutWindow) {
			return false;
		}
		
		int indexOf = parent.getIndexOfChild(this);
		return indexOf > parent.getModifiers().size();
	}
	
	@Override
	public boolean nodeCanBeMovedBelow() {
		if (isLayoutWindow) {
			return false;
		}
		
		int indexOf = parent.getIndexOfChild(this);
		return indexOf < parent.getModifiers().size() + parent.getChildren().size() - 1;
	}
	
	@Override
	public void insertComponent(SPUIComponent component, int index) {
		insertChildComponent(component, index == -1 ? node.getChildCount() : index);
	}

	@Override
	public void removeComponent(SPUIComponent component) {
		removeChildComponent(component);
	}
	
	
	@Override
	public TreePath getHierarchyTreePath() {
		
		if (node != null) {
			return new TreePath(node.getPath());
		}
		
		List<Object> path = new ArrayList<Object>();
		path.add(this);
		WinComponent comp = this;
		while ((comp = comp.getParent()) != null) {
			path.add(comp);
		}
		
		Collections.reverse(path);
		
		return new TreePath((Object[]) path.toArray(new Object[path.size()]));
	}

	@Override
	public String toString() {
		if (isLayoutWindow) {
			return "Layout";
		}
		
		StringBuilder sb = getBasicNameBuilder();

		if (name != null && !name.isEmpty()) {
			sb.append(":");
			sb.append(name);
		}
		if (caption != null) {
			sb.append(" - ");
			sb.append(caption);
		}	
		
		addComponentTagName(sb);
		
		return sb.toString();
	}
	
	@Override
	public PropertiesPanel getPropertiesPanel() {
		if (isLayoutWindow) {
			return null;
		}
		else {
			return super.getPropertiesPanel();
		}
	}
	
	protected class WindowDesignerDelegate extends DefaultDesignerDelegate {
		public WindowDesignerDelegate(SPUIViewer viewer) {
			super(viewer);
		}

		@Override
		public boolean isValid(DesignerElement element) {
			if (!super.isValid(element)) {
				return false;
			}
			String name = element.getName();
			return !name.equalsIgnoreCase("Design") && !name.equalsIgnoreCase("WinProcs") && !name.equalsIgnoreCase("Children")&& !name.equalsIgnoreCase("Class");
		}
		
		@Override
		public void propertyComponentAdded(DesignerProperty property, Object component) {
			super.propertyComponentAdded(property, component);
			
			switch (property.getProxyID()) {
			case 0xEEC1B005: 
				areaProperties = (PropertyInfo[]) component;
				break;
			}
		}
		
		@Override
		public Object getValue(DesignerProperty property) {
			switch (property.getProxyID()) {
			
			case 0xeec1b000: return flags;
			case 0xeec1b001: return name;
			
			case 0xEEC1B004: return tintColor;
			case 0xEEC1B005: return bounds;
			case 0xEEC1B006: return backgroundColor;
			case 0xEEC1B007: return drawable;
			case 0xEEC1B00A: return caption;
			case 0xEEC1B00E: return styleName;
			}
			
			return super.getValue(property);
		}
		
		@Override
		public void setValue(DesignerProperty property, Object value, int index) {
			switch (property.getProxyID()) {
			
			case 0xeec1b000: 
				flags = (int) value;
				viewer.repaint();
				break;
				
			case 0xeec1b001: setName((String) value); break;
			
			case 0xEEC1B004: setTintColor((Color) value); break;
			case 0xEEC1B005: setBounds((Rectangle) value); break;
			case 0xEEC1B006: setBackgroundColor((Color) value); break;
			
			case 0xEEC1B00A:
				LocalizedText text = (LocalizedText) value;
				if (text.tableID == -1 && text.instanceID == -1 && (text.text == null || text.text.length() == 0)) {
					Window.this.setText(null);
				}
				else {
					Window.this.setText(text);
				}
				viewer.repaint();
				break;
				
			case 0xeec1b007:
				showDrawableChooserAction(property, (JLabelLink) value);
				break;
				
			case 0xEEC1B00E:
				
				styleName = (String) value;
				if (styleName == null || styleName.isEmpty()) {
					styleName = "DefaultStyle";
				}
				
				style = StyleSheet.getActiveStyleSheet().getStyleInstance(styleName);
				
				viewer.repaint();
				break;
			}
			
			super.setValue(property, value, index);
		}
	}
	
	@Override
	protected DesignerClassDelegate getDesignerClassDelegate() {
		return new WindowDesignerDelegate(viewer);
	}
	
	
	@SuppressWarnings("unchecked")
	private ComponentChooser<? extends SPUIDrawable> getDrawableChooser(DesignerProperty property) {
		return (ComponentChooser<? extends SPUIDrawable>) ComponentFactory.getComponentChooser(property.getType(), viewer);
	}
	
	private void showDrawableChooserAction(DesignerProperty property, JLabelLink labelLink) {
		ComponentChooser.showChooserAction(new ComponentChooserCallback<SPUIDrawable>() {
			@Override
			public SPUIDrawable getValue() {
				return drawable;
			}

			@Override
			public void valueChanged(SPUIDrawable value) {
				if (drawable != value) {
					drawable = value;
					viewer.repaint();
				}
			}
		}, getDrawableChooser(property), labelLink, viewer);
	}
	
	@Override
	public SPUIDrawable getDrawable() {
		return drawable;
	}
	
	@Override
	public void setDrawable(SPUIDrawable drawable) {
		SPUIDrawable oldDrawable = this.drawable;
		this.drawable = drawable;
		
		if (oldDrawable != drawable && viewer != null) {
			viewer.repaint();
		}
	}
	

	@Override
	public boolean isActionableComponent() {
		return this instanceof ActionableComponent;
	}


}
