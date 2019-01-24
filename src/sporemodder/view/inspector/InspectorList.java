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

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * An inspector control capable of displaying a list of nodes. Each node is shown on a special panel; the panels can be reordered, or removed by
 * dragging them outside the control.
 * <p> 
 * The developer can track edition changes made by the user in real time by adding a listener to the items list. However, if you only want to do an action
 * once the user has applied the changes (i.e. released the mouse button), you can use {@link #setOnEditFinish(Consumer)}.
 *
 * @param <T> The type of JavaFX node that is displayed for each value on the list.
 */
public class InspectorList<T extends Node> extends Control {
	
	public static final String DEFAULT_STYLE_CLASS = "inspector-list";
	
	private final ObservableList<T> items = FXCollections.observableArrayList();
	private Consumer<List<T>> onEditFinish;
	
	public InspectorList() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
	
	public final ObservableList<T> getItems() {
		return items;
	}
	
	public void setOnEditFinish(Consumer<List<T>> action) {
		onEditFinish = action;
	}
	
	public Consumer<List<T>> getOnEditFinish() {
		return onEditFinish;
	}
	
	/** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new InspectorListSkin<T>(this);
    }
}
