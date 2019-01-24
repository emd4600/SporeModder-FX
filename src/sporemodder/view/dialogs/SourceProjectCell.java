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
package sporemodder.view.dialogs;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import sporemodder.ProjectManager;
import sporemodder.util.Project;

public class SourceProjectCell extends ListCell<Project> {

	public SourceProjectCell() {
		
		setOnDragDetected(event -> {
			if (getItem() == null) {
				return;
			}
			
			ClipboardContent content = new ClipboardContent();
			content.putString(getItem().getName());
			
			Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
			dragboard.setContent(content);
			dragboard.setDragView(snapshot(null, null));
			
			event.consume();
		});
		
		setOnDragOver(event -> {
			if (event.getGestureSource() != this && event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			
			event.consume();
		});
		
		setOnDragEntered(event -> {
			if (event.getGestureSource() != this && event.getDragboard().hasString()) {
				setOpacity(0.3);
			}
		});
		
		setOnDragExited(event -> {
			if (event.getGestureSource() != this && event.getDragboard().hasString()) {
				setOpacity(1);
			}
		});
		
		setOnDragDropped(event -> {
			if (getItem() == null) {
				return;
			}
			
			Dragboard db = event.getDragboard();
			boolean success = false;
			
			if (db.hasString()) {
				
				Project draggedProject = ProjectManager.get().getProject(db.getString());
				
				ObservableList<Project> items = getListView().getItems();
				int draggedIndex = items.indexOf(draggedProject);
				int thisIndex = items.indexOf(getItem());
				
				items.set(draggedIndex, getItem());
				items.set(thisIndex, draggedProject);
				
				List<Project> itemsCopy = new ArrayList<Project>(getListView().getItems());
				getListView().getItems().setAll(itemsCopy);
				
				success = true;
			}
			
			event.setDropCompleted(success);
			event.consume();
		});
		
		setOnDragDone(DragEvent::consume);
	}
	
	@Override
    protected void updateItem(Project item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
        	setText(item.getName());
        	setGraphic(null);
        }
	}
}
