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
package sporemodder.view.editors.spui;

import sporemodder.file.spui.InspectableObject;
import sporemodder.view.editors.SpuiEditor;

public class SpuiObjectCreatedAction extends SpuiUndoableAction {
	
	private String text;
	private SpuiEditor editor;
	private InspectableObject createdObject;
	
	
	public SpuiObjectCreatedAction(SpuiEditor editor, InspectableObject createdObject, String text) {
		super();
		this.text = text;
		this.editor = editor;
		this.createdObject = createdObject;
	}

	@Override
	public void undo() {
		// Remove the created object
		editor.removeElement(createdObject);
	}

	@Override
	public void redo() {
		// Add the created object
		editor.addElement(createdObject);
	}

	@Override
	public String getText() {
		return text;
	}

}
