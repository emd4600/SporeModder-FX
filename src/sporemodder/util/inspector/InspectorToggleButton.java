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
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class InspectorToggleButton extends InspectorElement {
	
	private String text;
	private ChangeListener<? super Boolean> selectionListener;
	private EventHandler<ActionEvent> actionHandler;
	private boolean isSelected;  // only used in initialization
	
	public InspectorToggleButton(String text, boolean isSelected, ChangeListener<? super Boolean> selectionListener, EventHandler<ActionEvent> actionHandler) {
		this.text = text;
		this.selectionListener = selectionListener;
		this.actionHandler = actionHandler;
		this.isSelected = isSelected;
	}

	@Override
	public void generateUI(Pane pane) {
		BorderPane bp = new BorderPane();
		
		final Button button = new Button(text);
		button.setDisable(!isSelected);
		button.setOnAction(actionHandler);
		//button.setPrefWidth(200);
		button.setPrefWidth(Double.MAX_VALUE);
		
		CheckBox cb = new CheckBox();
		cb.setSelected(isSelected);
		cb.selectedProperty().addListener((obs, oldValue, newValue) -> {
			button.setDisable(!newValue);
		});
		cb.selectedProperty().addListener(selectionListener);
		
		bp.setLeft(cb);
		bp.setCenter(button);
		
		BorderPane.setAlignment(cb, Pos.CENTER_LEFT);
		
		pane.getChildren().add(bp);
	}

}
