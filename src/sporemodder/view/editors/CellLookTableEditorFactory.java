package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellLookTableEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellLookTableEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".look_table_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}