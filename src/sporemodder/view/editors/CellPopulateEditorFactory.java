package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellPopulateEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellPopulateEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".populate_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}