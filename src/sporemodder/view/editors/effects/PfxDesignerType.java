package sporemodder.view.editors.effects;

import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public interface PfxDesignerType<T> {
	public InspectorValue<T> createNode();	
	public void generateUI(PropertyPane pane, String name, String description);
}
