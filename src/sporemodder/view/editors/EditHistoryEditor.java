/****************************************************************************
* Copyright (C) 2019 Eric Mor
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

import java.util.List;

/**
 * Defines a type of editor that supports search history, that is, undo/redo actions.
 */
public interface EditHistoryEditor {

	public boolean canUndo();
	public boolean canRedo();
	public void undo();
	public void redo();
	
	public List<? extends EditHistoryAction> getActions();
	// Only necessary if it supports undo history
	public int getUndoRedoIndex();
}
