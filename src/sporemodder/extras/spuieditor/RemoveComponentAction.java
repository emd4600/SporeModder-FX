package sporemodder.extras.spuieditor;

import java.util.HashMap;
import java.util.List;

import sporemodder.extras.spuieditor.components.SPUIComponent;

public class RemoveComponentAction implements CommandAction {
	
	private final HashMap<SPUIComponent, List<String>> componentMap = new HashMap<SPUIComponent, List<String>>();
	private final SPUIComponent removedComponent;
	
	public RemoveComponentAction(SPUIComponent removedComponent) {
		this.removedComponent = removedComponent;
	}

	@Override
	public void undo() {
		for (SPUIComponent comp : componentMap.keySet()) {
			comp.restoreRemovedComponent(this);
		}
		
		removedComponent.getSPUIViewer().getEditor().fillHierarchyTree();
		removedComponent.getSPUIViewer().setActiveComponent(removedComponent, false);
		removedComponent.getSPUIViewer().repaint();
	}

	@Override
	public void redo() {
		for (SPUIComponent comp : componentMap.keySet()) {
			comp.removeComponent(this, false);
		}
		
		removedComponent.getSPUIViewer().getEditor().removeComponentNode(removedComponent);
		removedComponent.getSPUIViewer().setActiveComponent(null, false);
		removedComponent.getSPUIViewer().repaint();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	public void putModifiedComponent(SPUIComponent component, List<String> modifiedValues) {
		componentMap.put(component, modifiedValues);
	}

	public SPUIComponent getRemovedComponent() {
		return removedComponent;
	}
	
	public List<String> getModifiedValues(SPUIComponent component) {
		return componentMap.get(component);
	}
}
