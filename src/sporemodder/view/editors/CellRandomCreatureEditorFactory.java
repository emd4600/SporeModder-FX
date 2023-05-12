package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellRandomCreatureEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellRandomCreatureEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".random_creature_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}