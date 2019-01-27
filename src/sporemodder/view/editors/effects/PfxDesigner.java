package sporemodder.view.editors.effects;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import sporemodder.view.editors.effects.PfxDesignerType;
import sporemodder.view.inspector.InspectorColorPicker;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public class PfxDesigner {

	private final Map<String, PfxDesignerType<?>> types = new HashMap<>();
	
	public PfxDesigner() {
		types.put("colorRGB", new PfxDesignerType<Color>() {
			@Override public InspectorValue<Color> createNode() {
				InspectorColorPicker control = new InspectorColorPicker();
				
				return control;
			}

			@Override public void generateUI(PropertyPane pane, String name, String description) {
				pane.add(name, description, createNode().getNode());
			}
		});
	}
}
