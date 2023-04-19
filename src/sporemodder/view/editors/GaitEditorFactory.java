package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class GaitEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new GaitEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".gait_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}