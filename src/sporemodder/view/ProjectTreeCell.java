/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

import java.awt.Toolkit;
import java.util.regex.Pattern;

import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import sporemodder.EditorManager;
import sporemodder.ProjectManager;
import sporemodder.util.ProjectItem;

public class ProjectTreeCell extends TreeCell<ProjectItem> {
	
	private final TextField textField = new TextField();
	private Tooltip warningTooltip;
	
	public ProjectTreeCell(boolean projectBehaviour) {
		
		addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
			// Avoid the double click edit
			if (isSelected() && e.getButton() == MouseButton.PRIMARY && 
					// Ensure the user is not clicking the disclosure icon
					!getDisclosureNode().getBoundsInParent().contains(e.getX(), e.getY())) {
				e.consume();
			}
		});
		
		if (projectBehaviour) addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			ProjectManager mgr = ProjectManager.get();
			
			if (e.getButton() == MouseButton.SECONDARY && !isEmpty() && getItem() != null) {
				mgr.getContextMenu().hide();
				mgr.generateContextMenu(getItem());
				mgr.getContextMenu().show(this, e.getScreenX(), e.getScreenY());
				
				e.consume();
			}
			else {
				// Any other case, just hide the menu
				mgr.getContextMenu().hide();
			}
		});
		
		setOnMouseClicked((event) -> {
			if (!isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {

				ProjectItem item = getItem();
				if (item.isFolder()) {
					getTreeItem().setExpanded(!getTreeItem().isExpanded());
				}
				else if (projectBehaviour) {
					EditorManager.get().moveFileToNewTab(item);
				}
								
				event.consume();
			}
			
		});
		
		textField.setOnAction(event -> {
			// We have accepted text, no need to show the alert anymore
			if (warningTooltip != null && warningTooltip.isShowing()) {
				warningTooltip.hide();
			}
			
			commitEdit(getItem());
			event.consume();
		});
		
		textField.textProperty().addListener((obs, oldValue, newValue) -> {
			// We are changing the text, no need to show the alert anymore
			if (warningTooltip != null && warningTooltip.isShowing()) {
				warningTooltip.hide();
			}
		});
	}
	
	@Override 
	public void startEdit() {
        if (!isEditable() || !getTreeView().isEditable()) {
            return;
        }
        super.startEdit();
        
        if (isEditing()) {
        	textField.setText(getItem() == null ? "" : getItem().getName());
        	
        	setText(null);
			setGraphic(textField);
			
			// Don't select the file extension, it's better for the user
        	textField.requestFocus();
        	textField.selectRange(0, textField.getText().split("\\.", 2)[0].length());
        }
    }
	
	@Override 
	public void cancelEdit() {
        super.cancelEdit();
        
        restoreItem(getItem());
    }
	
	private void showWarning(String text) {
		Point2D point = localToScreen(new Point2D(getWidth(), getHeight()));
		warningTooltip = new Tooltip(text);
		warningTooltip.show(this, point.getX(), point.getY());
		
		Toolkit.getDefaultToolkit().beep();
		
		// make it disappear after some time
		PauseTransition pause = new PauseTransition(Duration.seconds(3.5));
		pause.setOnFinished(e -> warningTooltip.hide());
		pause.play();
	}
	
	private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\s@*%;,\\(\\)'{}<>\\[\\]|\"^]");
	
	@Override
	public void commitEdit(ProjectItem newValue) {
		String name = textField.getText();
		
		if (ILLEGAL_CHARACTERS.matcher(name).find()) {
			showWarning("The name can only contain numbers, letters, -, _, ~,. and #.");
			return;
		}
		
		// Ensure there is no other mod file with that name
		boolean found = false;
		for (TreeItem<ProjectItem> child : newValue.getTreeItem().getParent().getChildren()) {
			if (child != getTreeItem() && child.getValue().isMod() && name.equals(child.getValue().getName())) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			newValue.setName(textField.getText());
			
			super.commitEdit(newValue);
		} else {
			// Show an error
			showWarning("There's already a file with this name in the mod.");
		}
	}

	@Override
	public void updateItem(ProjectItem item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			getStyleClass().setAll("tree-cell", "project_item_not_mod", "project_item_not_source");
		}
		else {
			if (isEditing()) {
				textField.setText(item.getName());
				
				setText(null);
				setGraphic(textField);
			}
			else {
				setText(item.getName());
				setGraphic(item.getIcon());
				
				getStyleClass().setAll("tree-cell", 
						item.isMod() ? "project_item_mod" : "project_item_not_mod",
						item.isSource() ? "project_item_source" : "project_item_not_source"
						);
			}
		}
	}
	
	private void restoreItem(ProjectItem item) {
		if (item == null) {
			setText(null);
			setGraphic(null);
			getStyleClass().setAll("text-field-tree-cell", "project_item_not_mod", "project_item_not_source");
		}
		else {
			setText(item.getName());
			setGraphic(item.getIcon());
			
			getStyleClass().setAll("tree-cell", 
					item.isMod() ? "project_item_mod" : "project_item_not_mod",
					item.isSource() ? "project_item_source" : "project_item_not_source"
					);
		}
	}
}
