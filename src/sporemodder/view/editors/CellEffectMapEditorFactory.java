package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellEffectMapEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellEffectMapEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".effectMap_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}