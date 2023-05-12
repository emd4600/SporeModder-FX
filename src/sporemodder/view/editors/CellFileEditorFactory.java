package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellFileEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellFileEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".cell_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}