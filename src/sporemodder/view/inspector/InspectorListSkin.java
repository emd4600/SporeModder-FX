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

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

class InspectorListSkin<T extends Node> extends SkinBase<InspectorList<T>> {

	private final VBox valuesPanel = new VBox();
	private final List<Pane> panes = new ArrayList<Pane>();
	
	private final Pane tempDragPane = new Pane();
	private T draggedItem;
	private int tempDragIndex = -1;
	// We want to keep the temp pane when we only have 1 item, otherwise the list becomes to small to put it back again
	private boolean forceDragPane;
	
	InspectorListSkin(InspectorList<T> control) {
		super(control);
		
		getChildren().add(valuesPanel);
		
		valuesPanel.getStyleClass().add("inspector-list-values");
		
		addValuePanes();
		
		control.getItems().addListener((ListChangeListener<T>) c -> {
			// The items might change while dragging, but we don't generate the panels again
			if (draggedItem == null) addValuePanes();
		});
		
		Platform.runLater(() -> {
			control.getScene().addEventFilter(DragEvent.DRAG_OVER, this::onDragOver);
			control.getScene().addEventFilter(DragEvent.DRAG_DROPPED, this::onDragDropped);
		});
	}
	
	private void addValuePane(T node) {
		BorderPane pane = new BorderPane();
		pane.setPrefWidth(Double.MAX_VALUE);
		
		Separator separator = new Separator(Orientation.VERTICAL);
		separator.getStyleClass().add("inspector-list-item-handle");
		
		separator.setOnDragDetected(event -> {
			ClipboardContent content = new ClipboardContent();
			// We need to put some content, otherwise it won't work
			content.putString("");
			
			Dragboard dragboard = pane.startDragAndDrop(TransferMode.MOVE);
			dragboard.setContent(content);
			dragboard.setDragView(pane.snapshot(null, null));
			
			tempDragPane.setPrefHeight(pane.getHeight());
			
			draggedItem = node;
			tempDragIndex = valuesPanel.getChildren().indexOf(pane);
			valuesPanel.getChildren().set(tempDragIndex, tempDragPane);
			
			forceDragPane = getSkinnable().getItems().size() == 1;
			
			event.consume();
		});
		
		pane.setLeft(separator);
		pane.setCenter(node);
		
		pane.getStyleClass().setAll("inspector-list-value");
		
		panes.add(pane);
		valuesPanel.getChildren().add(pane);
	}
	
	private void removeTempPane() {
		if (tempDragIndex != -1) {
			getSkinnable().getItems().remove(draggedItem);
			if (!forceDragPane) {
				// If not removed (because it had already been removed) we must remove the temp pane manually
				valuesPanel.getChildren().remove(tempDragPane);
			}
		}
		tempDragIndex = -1;
	}
	
	private void onDragDropped(DragEvent event) {
		
		// If we weren't dragging any item, this isn't our event
		if (draggedItem == null) return;
		
		double screenX = event.getScreenX();
		double screenY = event.getScreenY();
		Point2D localPosition = valuesPanel.screenToLocal(screenX, screenY);
		if (valuesPanel.contains(localPosition)) {
			
			if (tempDragIndex != -1) {
				// Do it on a separated list so only one event is generated
				List<T> values = new ArrayList<>(getSkinnable().getItems());
				values.remove(draggedItem);
				values.add(tempDragIndex, draggedItem);
				
				// Tell the items listener that we've finished dragging
				draggedItem = null;
				getSkinnable().getItems().setAll(values);
				
				// Ensure onDragExited doesn't do anything
				tempDragIndex = -1;
			}
		}
		
		if (getSkinnable().getOnEditFinish() != null) {
			getSkinnable().getOnEditFinish().accept(getSkinnable().getItems());
		}
		
		draggedItem = null;
		event.consume();
	}
	
	private void onDragOver(DragEvent event) {
		// If we weren't dragging any item, this isn't our event
		if (draggedItem == null) return;
		
		// Accept it always, otherwise DRAG_DROPPED isn't called outside the panel
		event.acceptTransferModes(TransferMode.MOVE);
		
		double screenX = event.getScreenX();
		double screenY = event.getScreenY();
		Point2D localPosition = valuesPanel.screenToLocal(screenX, screenY);
		
		if (valuesPanel.contains(localPosition)) {
			
			double y = localPosition.getY();
			
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
		} else {
			// We are dragging outside the panel
			removeTempPane();
		}
		
		event.consume();
	}
	
	private void moveDragPane(int index) {
		if (tempDragIndex != -1) {
			valuesPanel.getChildren().remove(tempDragIndex);
		}
		if (!forceDragPane) {
			valuesPanel.getChildren().add(index, tempDragPane);
		}
		tempDragIndex = index;
	}
	
	private void addValuePanes() {
		panes.clear();
		valuesPanel.getChildren().clear();
		
		for (T node : getSkinnable().getItems()) {
			addValuePane(node);
		}
	}
}
