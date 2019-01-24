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

import javafx.scene.control.TreeItem;

/**
 * An interface that acts similar to a factory, it is capable of detecting certain files in a project and using them in a 
 * special way. This can be used to make certain files invisible to the user, or use an special editor, etc.
 * All the behavior is done in a subclass of ProjectItem, that must be returned in a method in this class.
 */
public interface ProjectItemFactory {

	/**
	 * Checks whether a certain file is supported by this factory. If this method returns true, the {@link #create(File, Project, TreeItem)} method
	 * will be called immediately to create the adequate project item.
	 * @param file The file that must be put into a project item.
	 * @param project The project that this file belongs to.
	 * @param parent The parent TreeItem where the project item would be inserted. This must not be modified, but it can be used to check the parent folder, etc
	 * @return True if the file is supported by this factory, false otherwise.
	 */
	public boolean isSupported(File file, Project project, TreeItem<ProjectItem> parent);
	
	/**
	 * Gives the special file to the class so that it creates the corresponding ProjectItem. Usually a subclass of
	 * {@link ProjectItem} is returned so that it has a different behavior. If the method returns null, nothing will
	 * be added to the file tree, therefore ignoring the file.
	 * @param file The file that must be put into a project item.
	 * @param project The project that this file belongs to.
	 * @param parent The parent TreeItem where the project item would be inserted. This must not be modified, but it can be used to check the parent folder, etc
	 * @return A new ProjectItem to represent the file, or null if the file must be ignored.
	 */
	public ProjectItem create(File file, Project project, TreeItem<ProjectItem> parent);
}
