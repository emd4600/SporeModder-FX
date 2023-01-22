package sporemodder.extras.spuieditor;

import sporemodder.extras.spuieditor.components.SPUIComponent;

public class InsertComponentAction implements CommandAction {
	
	private ComponentContainer parentContainer;
	private SPUIComponent component;
	private SPUIComponent previousSelectedComponent;
	private int index;
	private SPUIEditor editor;

	public InsertComponentAction(ComponentContainer parentContainer, SPUIComponent component, SPUIComponent previousSelectedComponent, SPUIEditor editor, int index) {
		this.parentContainer = parentContainer;
		this.component = component;
		this.previousSelectedComponent = previousSelectedComponent;
		this.editor = editor;
		this.index = index;
	}

	@Override
	public void undo() {
		parentContainer.removeComponent(component);
		
		editor.fillHierarchyTree();
		editor.getSPUIViewer().setActiveComponent(previousSelectedComponent, false);
		editor.getSPUIViewer().repaint();
	}

	@Override
	public void redo() {
		parentContainer.insertComponent(component, index);
		
		editor.fillHierarchyTree();
		editor.getSPUIViewer().setActiveComponent(component, false);
		editor.getSPUIViewer().repaint();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

}
