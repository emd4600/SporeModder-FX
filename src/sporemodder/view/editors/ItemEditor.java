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

import java.io.File;
import java.io.IOException;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

/**
 * This interface defines all the methods that an editor (a UI that displays certain types of files) must implement.
 */
public interface ItemEditor {

	/**
	 * Called when the file of the editor must be loaded. This must update the UI to show the file.
	 * It's only called for the types supported by the editor.
	 * <p>
	 * When the editor is closed, this method is called with parameter null. The object won't be used again, so you should 
	 * dispose the editor and its resources then.
	 * @param file
	 */
	public void loadFile(ProjectItem item) throws IOException;
	
	/**
	 * Changes the destination file where the editor content should be saved. This must not reload the editor contents.
	 * @param file
	 */
	public void setDestinationFile(File file);
	
	/**
	 * This method is used to notify the editor when the user switches to/from the main view. 
	 * The argument is true when this editor becomes the active view, and false when the editor stops being the active view.
	 * @param active
	 */
	public void setActive(boolean isActive);
	
	/**
	 * Returns the user interface of this editor.
	 * @return
	 */
	public Node getUI();
	
	/**
	 * Called when the Save button is pressed or the file is closed, this method must save the item.
	 * This is only called if the {@link #isEditable()} method returns true.
	 */
	public void save();
	
	/**
	 * Tells whether this editor supports editing (and therefore saving) the item (true), or is only for viewing (false).
	 * @return
	 */
	public boolean isEditable();
	
	/**
	 * Whether the editor can be casted to a {@link SearchableEditor}, and therefore the "Find" panel can be used to find words inside it.
	 * @return
	 */
	public boolean supportsSearching();
	
	/**
	 * Whether the editor can be casted to a {@link EditHistoryEditor}, and therefore supports undo/redo actions.
	 * @return
	 */
	public boolean supportsEditHistory();
}
