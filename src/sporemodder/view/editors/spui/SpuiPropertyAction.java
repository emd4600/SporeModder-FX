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

import java.util.function.Consumer;

public class SpuiPropertyAction<T> extends SpuiUndoableAction {
	
	private final Consumer<T> undoAction;
	private final Consumer<T> redoAction;
	private T oldValue;
	private T newValue;
	private final String text;
	
	public SpuiPropertyAction(T oldValue, T newValue, Consumer<T> action, String text) {
		this(oldValue, newValue, action, action, text);
	}

	public SpuiPropertyAction(T oldValue, T newValue, Consumer<T> undoAction, Consumer<T> redoAction, String text) {
		this.redoAction = redoAction;
		this.undoAction = undoAction;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.text = text;
	}
	
	public T getOldValue() {
		return oldValue;
	}

	public T getNewValue() {
		return newValue;
	}

	public void setOldValue(T oldValue) {
		this.oldValue = oldValue;
	}

	public void setNewValue(T newValue) {
		this.newValue = newValue;
	}

	@Override
	public void undo() {
		undoAction.accept(oldValue);
	}

	@Override
	public void redo() {
		redoAction.accept(newValue);
	}

	@Override
	public String getText() {
		return text;
	}

}
