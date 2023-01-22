package sporemodder.extras.spuieditor;

import sporemodder.extras.spuieditor.components.SPUIComponent;

public class SelectionAction implements CommandAction {
	
	private SPUIViewer viewer;
	private SPUIComponent previousActiveComponent;
	private SPUIComponent activeComponent;
	
	public SelectionAction(SPUIViewer viewer, SPUIComponent previousActiveComponent, SPUIComponent activeComponent) {
		this.viewer = viewer;
		this.previousActiveComponent = previousActiveComponent;
		this.activeComponent = activeComponent;
	}

	@Override
	public void undo() {
		viewer.setActiveComponent(previousActiveComponent, false);
	}

	@Override
	public void redo() {
		viewer.setActiveComponent(activeComponent, false);
	}

	@Override
	public boolean isSignificant() {
		return false;
	}
}
