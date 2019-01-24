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
package sporemodder.view.inspector2.skin;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sporemodder.view.inspector2.InspectorList;
import sporemodder.view.inspector2.InspectorValue;

public class InspectorListSkin extends SkinBase<InspectorList> {
	
	private final VBox layout = new VBox(5);
	private final VBox valuesPanel = new VBox();
	private final List<Pane> panes = new ArrayList<Pane>();
	private final Button addButton = new Button("Add value");
	
	private final Pane tempDragPane = new Pane();
	private InspectorValue<?> draggedValue;
	private int tempDragIndex = -1;
	
	// We can only add it when the scene is created
	private boolean sceneListenerAdded;

	public InspectorListSkin(InspectorList control) {
		super(control);
		construct();
	}

	private void construct() {
		
		getChildren().add(layout);
		
		layout.getStyleClass().add(InspectorList.DEFAULT_STYLE_CLASS);
		
		valuesPanel.getStyleClass().add("inspector-list-values");
		
		addValuePanes();
		
		getSkinnable().getValues().addListener((ListChangeListener<InspectorValue<?>>) c -> {
			addValuePanes();
		});
		
		// Add value button
		addButton.setPrefWidth(Double.MAX_VALUE);
		addButton.setOnAction((event) -> {
//			// Get the index that comes after the last value
//			int lastIndex = argIndex + getRemoveableCount() + createIfNecessary();
//			List<String> splits = lineInspector.getSplits();
//			
//			// Add a new value in the correct position, update the text and update the UI so another spinner is shown.
//			if (lastIndex == splits.size()) {
//				splits.add(stringConverter.toString((double) defaultValue));
//			} else {
//				splits.add(lastIndex, stringConverter.toString((double) defaultValue));
//			}
//			
//			submitChanges();
//			insertElements();
//			// contentPanel.updateUI();
//			
//			// Set the focus on the new spinner and select all text
//			Spinner<Double> spinner = spinners.get(spinners.size() - 1);
//			spinner.requestFocus();
//			spinner.getEditor().selectAll();
//			spinner.getEditor().requestFocus();
		});
		
		// A panel used temporarily to show where the dragged value will go
		tempDragPane.setPrefWidth(Double.MAX_VALUE);
		tempDragPane.getStyleClass().add("inspector-list-temp-drag");
		
		layout.getChildren().add(valuesPanel);
		layout.getChildren().add(addButton);
		
		// Functionality: double-clicking on empty space generates new values
		valuesPanel.setOnMouseClicked((event) -> {
			// Cannot create values if there is no factory for it
			if (getSkinnable().getValueFactory() == null) {
				return;
			}
			
			// Only accept double-click
			if (event.getClickCount() != 2) {
				return;
			}
			
			double y = event.getY();
			
			int index = 0;
			
			for (int i = 0; i < panes.size(); i++) {
				Bounds bounds = panes.get(i).getBoundsInParent();
				
				if (y > bounds.getMaxY()) {
					index = i;
				}
				else {
					break;
				}
			}
			
			// We don't want to insert it at that position, but next to it
			index += 1;
			
			InspectorValue<?> value = getSkinnable().getValueFactory().createValue(index);
			if (value != null) {
				getSkinnable().getValues().add(index, value);
				
				value.getNode().requestFocus();
			}
			
			
//			// Set the focus on the new spinner and select all text
//			Spinner<Double> spinner = spinners.get(index);
//			spinner.requestFocus();
//			spinner.getEditor().selectAll();
//			spinner.getEditor().requestFocus();
		});
	}
	
	private void onDragOverScene(DragEvent event) {
		// Only do it if it's outside the values panel
		if (!valuesPanel.localToScene(valuesPanel.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
			removeTempPane();
		}
	}
	
	private void addValuePane(InspectorValue<?> value) {
		BorderPane pane = new BorderPane();
		pane.getStyleClass().add("inspector-list-value");
		pane.setPrefWidth(Double.MAX_VALUE);
		
		Separator separator = new Separator(Orientation.VERTICAL);
		separator.getStyleClass().add("inspector-list-item-handle");
		
		separator.setOnDragDetected(event -> {
			
			if (!sceneListenerAdded) {
				separator.getScene().setOnDragOver(this::onDragOverScene);
				sceneListenerAdded = true;
			}
			
			ClipboardContent content = new ClipboardContent();
			// We need to put some content, otherwise it won't work
			content.putString(value.getValue().toString());
			
			Dragboard dragboard = pane.startDragAndDrop(TransferMode.MOVE);
			dragboard.setContent(content);
			dragboard.setDragView(pane.snapshot(null, null));
			
			tempDragPane.setPrefHeight(pane.getHeight());
			
			draggedValue = value;
			tempDragIndex = valuesPanel.getChildren().indexOf(pane);
			valuesPanel.getChildren().set(tempDragIndex, tempDragPane);
			
			event.consume();
		});
		
		
		valuesPanel.setOnDragOver(this::onDragOver);
		
		valuesPanel.setOnDragDropped(event -> {
			if (tempDragIndex != -1) {
				List<InspectorValue<?>> values = new ArrayList<>(getSkinnable().getValues());
				values.remove(draggedValue);
				values.add(tempDragIndex, draggedValue);
				
				getSkinnable().getValues().setAll(values);
				
				// Ensure onDragExited doesn't do anything
				tempDragIndex = -1;
			}
			
			event.consume();
		});
		
		valuesPanel.setOnDragExited(event -> {
			removeTempPane();
		});
		
		pane.setLeft(separator);
		pane.setCenter(value.getNode());
		
		panes.add(pane);
		valuesPanel.getChildren().add(pane);
	}
	
	private void removeTempPane() {
		if (tempDragIndex != -1) {
			boolean removed = getSkinnable().getValues().remove(draggedValue);
			if (!removed) {
				// If not removed (because it had already been removed) we must remove the temp pane manually
				valuesPanel.getChildren().remove(tempDragPane);
			}
		}
		tempDragIndex = -1;
	}
	
	private void onDragOver(DragEvent event) {
		event.acceptTransferModes(TransferMode.MOVE);
		
		double y = event.getY();
		
		// We must find the child that comes before 
		int count = valuesPanel.getChildren().size();
		for (int i = 0; i < count; i++) {
			
			if (i == tempDragIndex) {
				continue;
			}
			
			Bounds childBounds = valuesPanel.getChildren().get(i).getBoundsInParent();
			
			// Dragging below half the node should put the item there
			double acceptedY = childBounds.getMinY() + childBounds.getHeight() / 2;
			
			// If it's the last node, dragging below it moves the panel to the last point
			if (i == count-1 && y > acceptedY) {
				// Relocate the white panel at the end of the list
				moveDragPane(i);
				break;
			}
			// If it's the first node, dragging ABOVE it moves the panel to the first point
			else if (i == 0 && y < acceptedY) {
				// Relocate the white panel at the beginning of the list
				moveDragPane(i);
				break;
			}
			
			// To do the check we must ensure there is another node after this
			if (i+1 < count) {
				
				Bounds nextBounds = valuesPanel.getChildren().get(i+1).getBoundsInParent();
				double nextAcceptedY = nextBounds.getMinY() + nextBounds.getHeight() / 2;
				
				if (acceptedY < y && y < nextAcceptedY) {
					moveDragPane(i+1);
					break;
				}
			}
		}
		
		event.consume();
	}
	
	private void moveDragPane(int index) {
		if (tempDragIndex != -1) {
			valuesPanel.getChildren().remove(tempDragIndex);
		}
		valuesPanel.getChildren().add(index, tempDragPane);
		tempDragIndex = index;
	}
	
	private void addValuePanes() {
		panes.clear();
		valuesPanel.getChildren().clear();
		
		for (InspectorValue<?> value : getSkinnable().getValues()) {
			addValuePane(value);
		}
	}
}
