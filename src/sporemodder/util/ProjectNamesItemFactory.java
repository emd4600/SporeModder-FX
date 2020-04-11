package sporemodder.util;

import java.io.File;

import javafx.scene.control.TreeItem;

public class ProjectNamesItemFactory implements ProjectItemFactory {

	@Override
	public boolean isSupported(File file, Project project, TreeItem<ProjectItem> parent) {
		return file.isDirectory() && file.getName().equals("sporemaster");
	}

	@Override
	public ProjectItem create(File file, Project project, TreeItem<ProjectItem> parent) {
		return new ProjectNamesItem(file, project);
	}

}
