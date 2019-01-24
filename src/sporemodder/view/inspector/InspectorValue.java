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
package sporemodder.view.inspector;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

/**
 * A visual component that can be used to edit and visualize a certain type of variable.
 * It has support for value listeners that are called every time the user edits the value. 
 * It also has a method {@link #setValue(Object)} to silently change the value without calling the listeners.
 * @param <T> The type of value shown in this component.
 */
public interface InspectorValue<T> {

	/** 
	 * Returns the value that is currently being displayed in this component.
	 * @return
	 */
	public T getValue();
	
	/**
	 * Sets the value WITHOUT calling the value listeners. This updates the visual component to display the new value.
	 * @param value
	 */
	public void setValue(T value);
	
	/**
	 * Returns the visual JavaFX node of this component.
	 * @return
	 */
	public Node getNode();
	
	public void addValueListener(ChangeListener<T> listener);
	public void removeValueListener(ChangeListener<T> listener);
}
