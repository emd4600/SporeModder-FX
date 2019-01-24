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
package sporemodder.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import sporemodder.view.skin.StatusBarSkin;

public class StatusBar extends Control {
	
	private static final int DEFAULT_HEIGHT = 35;
	
	private static final PseudoClass PSEUDO_CLASS_ERROR = PseudoClass.getPseudoClass("error");
	
	public static enum Status {
		DEFAULT,
		ERROR
	}
	
	public StatusBar() {
		setPrefWidth(Double.MAX_VALUE);
		setPrefHeight(DEFAULT_HEIGHT);
		
		getStyleClass().add("status-bar");
	}

	private final ObservableList<Node> leftNodes = FXCollections.observableArrayList();
	private final ObservableList<Node> rightNodes = FXCollections.observableArrayList();
	private final ObjectProperty<Status> status = new SimpleObjectProperty<Status>(this, "status", Status.DEFAULT) {
		@Override protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_ERROR, get() == Status.ERROR);
            super.invalidated();
        }
	};
    
    /**
     * The nodes that are displayed on the left side of the status bar, ordered from left to right.
     * @return
     */
    public final ObservableList<Node> getLeftNodes() {
    	return leftNodes;
    }
    
    /**
     * The nodes that are displayed on the right side of the status bar, ordered from left to right.
     * @return
     */
    public final ObservableList<Node> getRightNodes() {
    	return rightNodes;
    }
    
    public Status getStatus() {
    	return status.get();
    }
    
    public void setStatus(Status status) {
    	this.status.set(status);
    }
    
    public ObjectProperty<Status> statusProperty() {
    	return status;
    }
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new StatusBarSkin(this);
    }
}
