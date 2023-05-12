package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellBackgroundMapEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellBackgroundMapEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".backgroundMap_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}