/****************************************************************************
* Copyright (C) 2018 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/

package sporemodder.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import sporemodder.EditorManager;
import sporemodder.PathManager;
import sporemodder.ProjectManager;
import sporemodder.view.ProjectTreeItem;
import sporemodder.view.editors.EditorFactory;
import sporemodder.view.editors.ItemEditor;

/**
 * A project item, which represents a file in the project in a hierarchical way. Project items are always wrapped in
 * a JavaFX TreeItem. Methods in the program never rely on the file, but on the project item instead; this means that subclasses
 * of ProjectItem can be used to give special behavior to certain files (check {@link ProjectItemFactory}). For example, a subclass
 * could override the {@link #isFolder()} method to make a folder appear as a leaf item on the tree. Similarly, you can use
 * the {@link #createEditor()} method to make the item use a different editor.
 * <p>
 * Calling the super implementation is not necessary when overriding methods. Doing so will call the default implementation used for most files.
 */
public class ProjectItem {

	/** The name that is shown in the project tree. */
	protected String name;
	/** The file that this item represents. This is null for the root item. */
	protected File file;
	/** The project that contains the file of this item. This is null for the root item. */
	protected Project project;
	/** The TreeItem that wraps this project item; this can be used to get the parent or children items. */
	protected ProjectTreeItem treeItem;
	/** Whether this item is a folder or not. */
	protected boolean isFolder;
	/** Whether this item is the root item of the project tree, which represents the project itself. */
	protected boolean isRoot;
	/** Whether this item belongs to the files of the active project itself. */
	protected boolean isMod;
	/** Whether this item belongs to one of the source projects of the active project. */
	protected boolean isSource;
	
	private ProjectItem() {}
	
	public ProjectItem(File file, Project project) {
		this.file = file;
		this.name = file.getName();
		this.isFolder = file.isDirectory();
		this.isRoot = false;
		this.project = project;
		this.isMod = false;
		this.isSource = false;
	}
	
	public ProjectItem(String name, Project project) {
		this.file = null;
		this.name = name;
		this.isFolder = false;
		this.isRoot = false;
		this.project = project;
		this.isMod = false;
		this.isSource = false;
	}
	
	public static ProjectItem createRoot(Project project, String name) {
		ProjectItem item = new ProjectItem();
		item.project = project;
		item.name = name;
		item.isFolder = true;
		item.isRoot = true;
		item.isMod = false;
		item.isSource = false;
		return item;
	}
	
	/** Tells whether this item belongs to the mod files. */
	public boolean isMod() {
		return isMod;
	}
	
	/** Tells whether this item belongs to the source projects. */
	public boolean isSource() {
		return isSource;
	}
	
	/** Sets whether this item belongs to the mod files. */
	public void setIsMod(boolean value) {
		this.isMod = value;
	}
	
	/** Sets whether this item belongs to the source projects. */
	public void setIsSource(boolean value) {
		this.isSource = value;
	}
	
	/** Gets the TreeItem that wraps this project item; this can be used to get the parent or children items. */
	public ProjectTreeItem getTreeItem() {
		return treeItem;
	}
	
	/** Sets the TreeItem that wraps this project item; this can be used to get the parent or children items. */
	public void setTreeItem(ProjectTreeItem treeItem) {
		this.treeItem = treeItem;
	}
	
	/** Gets the name that is displayed in this item. */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name that is displayed in this item. Special items might not reflect this changes.
	 * @param text
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the extension of the item, which is everything in the name after the first dot. For example,
	 * "particles.prop.xml" returns "prop.xml".
	 * @return The extension, or null if there is no extension.
	 */
	public String getExtension() {
		String[] splits = name.split("\\.", 2);
		if (splits.length == 2) {
			return splits[1];
		} else {
			return null;
		}
	}
	
	/**
	 * Similar to {@link #getExtension()}, but this method only returns the specific extension, that is, only the part of
	 * the string that comes after the last dot. For example, "particles.prop.xml" only returns "xml".
	 * @return
	 */
	public String getSpecificExtension() {
		int index = name.lastIndexOf(".");
		if (index == -1) {
			return null;
		}
		else {
			return name.substring(index + 1);
		}
	}
	
	private boolean getRelativePath_tree(StringBuilder sb) {
		TreeItem<ProjectItem> parent = treeItem.getParent();
		
		if (parent != null) {
			if (parent.getValue().getRelativePath_tree(sb)) {
				sb.append(File.separatorChar);
			}
			sb.append(name);
			return true;
		} else {
			// No parent -> root project item, we don't want it included in the relative path
			return false;
		}
	}
	
	public String getRelativePath() {
		if (isRoot) return "";
		
		if (treeItem == null) {
			return null;
		}
		
		if (file != null) {
			// The project is not always the same as in folder
			// A workaround: relativize with Projects folder
			Path filePath = file.toPath();
			Path path = null;
			if (project != null) {
				Path projectParentPath = project.getFolder().getParentFile().toPath();
				if (filePath.startsWith(projectParentPath)) {
					path = projectParentPath.relativize(filePath);
				}
				else {
					for (Project p : project.getReferences()) {
						projectParentPath = p.getFolder().getParentFile().toPath();
						if (filePath.startsWith(projectParentPath)) {
							path = projectParentPath.relativize(filePath);
							break;
						}
					}
				}
			}
			else {
				path = PathManager.get().getProjectsFolder().toPath().relativize(filePath);
			}
			return path.subpath(1, path.getNameCount()).toString();
		}
		else {
			System.err.println(name + (project == null ? " does not have project" : " does not have file"));
			// This won't work if the file does not match search, but hopefully it never happens anyways??
			StringBuilder sb = new StringBuilder();
			getRelativePath_tree(sb);
			return sb.toString();
		}
	}
	
	/** Gets the file that this project item represents. */
	public File getFile() {
		return file;
	}
	
	/** Sets the file that this item represents. The file should be contained in the project this item belongs to. */
	public void setFile(File file) {
		this.file = file;
		this.isFolder = file != null && file.isDirectory();
	}
	
	/** Gets whether this item represents a folder, and therefore can have children on it. */
	public boolean isFolder() {
		return isFolder;
	}
	
	/** Gets whether this item represents the root folder, which is the project directory. */
	public boolean isRoot() {
		return isRoot;
	}
	
	/** Gets the project this file belongs to. This is used because some items can belong to source projects instead of the main project. */
	public Project getProject() {
		return project;
	}
	
	/** Sets the project this file belongs to. */
	public void setProject(Project project) {
		this.project = project;
	}
	
	/** Returns a new instance of the editor that is used to modify or visualize this item. */
	public ItemEditor createEditor() {
		EditorFactory factory = EditorManager.get().getEditorFactory(this);
		
		if (factory != null) {
			return factory.createInstance();
		} else {
			// folders don't use editors
			return null;
		}
	}
	
	/** Returns the icon displayed in the item. By default, this just redirects to {@link EditorManager.getIcon(ProjectItem)}.*/
	public Node getIcon() {
		return EditorManager.get().getIcon(this);
	}
	

	/** 
	 * Returns whether the {@link #removeItem()} action can be used on this item.
	 * @return
	 */
	public boolean canRemoveItem() {
		return isMod;
	}
	
	/**
	 * Removes this item from the project tree. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean removeItem() throws Exception {
		return ProjectManager.get().removeItem(this);
	}
	
	/** 
	 * Returns whether the {@link #modifyItem()} action can be used on this item.
	 * @return
	 */
	public boolean canModifyItem() {
		return isSource && !isMod;
	}
	
	/**
	 * Modifies this item from the project tree, including the file into the mod. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean modifyItem() throws Exception {
		return ProjectManager.get().modifyItem(this);
	}
	
	/** 
	 * Returns whether the {@link #duplicateItem()} action can be used on this item.
	 * @return
	 */
	public boolean canDuplicateItem() {
		return true;
	}
	
	/**
	 * Creates a copy of this item, saving it into the mod project. The user is required to edit the new file name. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean duplicateItem() throws Exception {
		return ProjectManager.get().duplicateItem(this);
	}
	
	/** 
	 * Returns whether the {@link #openModFolder()} action can be used on this item.
	 * @return
	 */
	public boolean canOpenModFolder() {
		return isMod || isRoot;
	}
	
	/**
	 * Opens in file explorer the folder that contains the mod version of this item. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean openModFolder() throws Exception {
		return ProjectManager.get().openModFolder(this);
	}
	
	/** 
	 * Returns whether the {@link #openSourceFolder()} action can be used on this item.
	 * @return
	 */
	public boolean canOpenSourceFolder() {
		return isSource || (isRoot && !project.getReferences().isEmpty());
	}
	
	/**
	 * Opens in file explorer the folder that contains the source version of this item. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean openSourceFolder() throws Exception {
		return ProjectManager.get().openSourceFolder(this);
	}
	
	/** 
	 * Returns whether the {@link #compareItem()} action can be used on this item.
	 * @return
	 */
	public boolean canCompareItem() {
		return isMod && isSource;
	}
	
	/**
	 * Executes WinMerge to compare the source and mod version of a given item. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean compareItem() throws Exception {
		return ProjectManager.get().compareItem(this);
	}
	
	/** 
	 * Returns whether the {@link #createNewFile()} action can be used on this item.
	 * @return
	 */
	public boolean canCreateNewFile() {
		return true;
	}
	
	/**
	 * Creates a new file inside this item or next to it. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean createNewFile() throws Exception {
		return ProjectManager.get().createNewFile(this);
	}
	
	/** 
	 * Returns whether the {@link #createNewFolder()} action can be used on this item.
	 * @return
	 */
	public boolean canCreateNewFolder() {
		return true;
	}
	
	/**
	 * Creates a new folder inside this item or next to it. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean createNewFolder() throws Exception {
		return ProjectManager.get().createNewFolder(this);
	}
	
	/** 
	 * Returns whether the {@link #renameItem()} action can be used on this item.
	 * @return
	 */
	public boolean canRenameItem() {
		return true;
	}
	
	/**
	 * Renames the item, including it into the mod if necessary. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean renameItem() throws Exception {
		return ProjectManager.get().renameItem(this);
	}
	
	/** 
	 * Returns whether the {@link #importFiles()} action can be used on this item.
	 * @return
	 */
	public boolean canImportFiles() {
		return true;
	}
	
	/**
	 * Opens a file browser where the user can select all the files he wants to import into this item, which will be included
	 * into the mod if necessary. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean importFiles() throws Exception {
		return ProjectManager.get().importFiles(this);
	}
	
	/** 
	 * Returns whether the {@link #refreshItem()} action can be used on this item.
	 * @return
	 */
	public boolean canRefreshItem() {
		return true;
	}
	
	/**
	 * Reloads this item to show the updated information. By default, it calls the implementation on {@link ProjectManager}.
	 * @return Whether the action was carried out successfully.
	 * @throws IOException
	 */
	public boolean refreshItem() throws Exception {
		return ProjectManager.get().refreshItem(this);
	}
	
	/**
	 * This method is called when the user right-clicks on the item, so a context menu must be generated.
	 * This is called after all default buttons have been added to the menu, and by default it does nothing.
	 * @param menu
	 */
	public void generateContextMenu(ContextMenu menu) {
		
	}
	
	/** Only for debugging purposes. */
	@Override public String toString() {
		return name;
	}
}
