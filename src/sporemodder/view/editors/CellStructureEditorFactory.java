package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellStructureEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellStructureEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".structure_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}