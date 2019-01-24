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
package sporemodder.util.inspector;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class InspectorRadioButton extends InspectorElement {
	
	private String text;
	private ChangeListener<? super Boolean> selectionListener;
	private EventHandler<ActionEvent> actionHandler;
	private boolean isSelected;  // only used in initialization
	
	private RadioButton rb;
	private ToggleGroup toggleGroup;
	
	public InspectorRadioButton(String text, boolean isSelected, ToggleGroup toggleGroup, ChangeListener<? super Boolean> selectionListener, EventHandler<ActionEvent> actionHandler) {
		this.text = text;
		this.selectionListener = selectionListener;
		this.actionHandler = actionHandler;
		this.isSelected = isSelected;
		this.toggleGroup = toggleGroup;
	}
	
	
	
	public ChangeListener<? super Boolean> getSelectionListener() {
		return selectionListener;
	}

	public void setSelectionListener(ChangeListener<? super Boolean> selectionListener) {
		this.selectionListener = selectionListener;
	}

	public EventHandler<ActionEvent> getActionHandler() {
		return actionHandler;
	}

	public void setActionHandler(EventHandler<ActionEvent> actionHandler) {
		this.actionHandler = actionHandler;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public ToggleGroup getToggleGroup() {
		return toggleGroup;
	}

	public void setToggleGroup(ToggleGroup toggleGroup) {
		this.toggleGroup = toggleGroup;
	}

	@Override
	public void generateUI(Pane pane) {
		BorderPane bp = new BorderPane();
		
		final Button button = new Button(text);
		button.setDisable(!isSelected);
		button.setOnAction(actionHandler);
		button.setPrefWidth(Double.MAX_VALUE);
		
		rb = new RadioButton();
		rb.setSelected(isSelected);
		rb.setToggleGroup(toggleGroup);
		rb.selectedProperty().addListener((obs, oldValue, newValue) -> {
			button.setDisable(!newValue);
		});
		rb.selectedProperty().addListener(selectionListener);
		
		bp.setLeft(rb);
		bp.setCenter(button);
		
		BorderPane.setAlignment(rb, Pos.CENTER_LEFT);
		
		pane.getChildren().add(bp);
	}

	public RadioButton getRadioButton() {
		return rb;
	}
}
