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

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import sporemodder.UIManager;

public class InspectorReferenceLink implements InspectorValue<Object> {
	
	private Object value;
	
	private final Tooltip tooltip = new Tooltip();
	private Button hyperlink;
	private Button menuButton;
	
	private BorderPane pane;

	private final List<ChangeListener<Object>> listeners = new ArrayList<>();
	
	public InspectorReferenceLink() {
		hyperlink = PropertyPane.createHyperlink("No value", null);
		hyperlink.setTooltip(tooltip);
		
		menuButton = new Button();
		menuButton.setGraphic(UIManager.get().loadIcon("arrow-down.png", 16, 16, true));
		menuButton.getStyleClass().add("button-no-background");
		
		pane = new BorderPane();
		pane.setCenter(hyperlink);
	}
	
	public void setOnAction(EventHandler<ActionEvent> onAction) {
		hyperlink.setOnAction(onAction);
	}
	
	public void setOnButtonAction(EventHandler<ActionEvent> onAction) {
		menuButton.setOnAction(onAction);
		pane.setRight(menuButton);
	}
	
	public Button getMenuButton() {
		return menuButton;
	}
	
	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
		loadValue();
	}
	
	private void loadValue() {
		if (value == null) {
			hyperlink.setText("No value");
			hyperlink.setTooltip(null);
		} else {
			String text = value.toString();
			tooltip.setText(text);
			hyperlink.setText(text);
			hyperlink.setTooltip(tooltip);
		}
	}

	@Override
	public Node getNode() {
		return pane;
	}

	@Override
	public void addValueListener(ChangeListener<Object> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeValueListener(ChangeListener<Object> listener) {
		listeners.remove(listener);
	}

	public void triggerListeners(Object oldValue) {
		for (ChangeListener<Object> listener : listeners) {
			listener.changed(null, oldValue, value);
		}
	}
}
