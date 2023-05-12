package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellGlobalsEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellGlobalsEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".globals_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}