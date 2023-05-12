package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class CellLookAlgorithmEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new CellLookAlgorithmEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".look_algorithm_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}