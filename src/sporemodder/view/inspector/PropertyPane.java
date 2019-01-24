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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * A pane capable of displaying properties in a grid-like manner. The pane is divided in two columns: property names
 * are displayed at the left and their controls are displayed at the right column.
 */
public class PropertyPane {
	public static final String DEFAULT_STYLE_CLASS = "inspector-property-pane";
	
	private GridPane pane;
	private int currentRow;
	
	public PropertyPane() {
		pane = new GridPane();
		
		pane.getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
	
	public Pane getNode() {
		return pane;
	}
	
	public void add(String name, String description, Node node) {
		add(-1, name, description, node);
	}
	
	public void add(int index, String name, String description, Node node) {
		Label label = new Label(name);
		label.getStyleClass().add("inspector-value-label");
		if (description != null) label.setTooltip(new Tooltip(description));
		
		if (index == -1) {
			pane.add(label, 0, currentRow);
			pane.add(node, 1, currentRow);
			++currentRow;
		} else {
			pane.getChildren().removeIf(n -> GridPane.getRowIndex(n) == index);
			pane.add(label, 0, index);
			pane.add(node, 1, index);
		}
		
		GridPane.setHalignment(node, HPos.RIGHT);
		GridPane.setHgrow(label, Priority.ALWAYS);
		GridPane.setHalignment(label, HPos.LEFT);
		GridPane.setValignment(label, VPos.CENTER);
	}
	
	public void add(Node leftNode, Node rightNode) {
		add(-1, leftNode, rightNode);
	}
	
	public void add(int index, Node leftNode, Node rightNode) {
		if (index == -1) {
			pane.add(leftNode, 0, currentRow);
			pane.add(rightNode, 1, currentRow);
			++currentRow;
		} else {
			pane.getChildren().removeIf(n -> GridPane.getRowIndex(n) == index);
			pane.add(leftNode, 0, index);
			pane.add(rightNode, 1, index);
		}
		
		GridPane.setHgrow(rightNode, Priority.SOMETIMES);
		GridPane.setHalignment(leftNode, HPos.LEFT);
		GridPane.setValignment(leftNode, VPos.CENTER);
	}
	
	public void add(Node node) {
		add(-1, node);
	}
	
	public void add(int index, Node node) {
		if (index == -1) {
			pane.add(node, 0, currentRow, 2, 1);
			++currentRow;
		} else {
			pane.getChildren().removeIf(n -> GridPane.getRowIndex(n) == index);
		}
		
		GridPane.setHgrow(node, Priority.ALWAYS);
	}
	
	public int size() {
		return currentRow;
	}
	
	public static TitledPane createTitled(String title, PropertyPane pane) {
		return createTitled(title, null, pane);
	}
	
	public static TitledPane createTitled(String title, String description, PropertyPane pane) {
		return createTitled(title, description, pane.getNode());
	}
	
	public static TitledPane createTitled(String title, String description, Node node) {
		TitledPane titledPane = new TitledPane();
		
		titledPane.setText(title);
		titledPane.setContent(node);
		
		titledPane.getStyleClass().add("inspector-titled-pane");
		
		if (description != null) {
			Tooltip.install(titledPane, new Tooltip(description));
		}
		
		return titledPane;
	}
	
	public static Button createHyperlink(String text, EventHandler<ActionEvent> eventHandler) {
		Button button = new Button(text);
		button.getStyleClass().addAll("hyperlink", "button-no-background");
		if (eventHandler != null) button.setOnAction(eventHandler);
		return button;
	}
}
