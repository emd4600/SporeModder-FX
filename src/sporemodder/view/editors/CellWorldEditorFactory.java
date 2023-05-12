package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellWorldEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellWorldEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".world_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}