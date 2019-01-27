package sporemodder.view.editors.effects;

import sporemodder.view.inspector.PropertyPane;

public class PfxDesignerOption extends PfxDesignerArgumentable {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void generateUI(PropertyPane pane) {
		if (isSingleArgument()) {
			arguments.get(0).generateUI(pane);
		} else {
			//TODO
		}
	}
}
