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

package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

/**
 * An editor factory is a class that is created only once, and that is used to create instances of a particular file editor.
 * It also contains a method that decides whether a file is supported or not by this editor.
 */
public interface EditorFactory {

	/**
	 * Creates one instance of the editor represented by this factory.
	 */
	public ItemEditor createInstance();
	
	/**
	 * This method must tell whether the given type of file can be accepted in this editor.
	 * If the return value is true, the setFile() method will be called so the editor shows the file;
	 * otherwise, other editors will be checked.
	 * @param file
	 * @return Whether the file can be viewed in this editor.
	 */
	public boolean isSupportedFile(ProjectItem item);
	
	/**
	 * Returns the icon that represents an item supported by this editor, or null if the default icon must be used.
	 * If the editor does not support the item, this method should return null so other editors have the opportunity of using their icons.
	 */
	public Node getIcon(ProjectItem item);
}
