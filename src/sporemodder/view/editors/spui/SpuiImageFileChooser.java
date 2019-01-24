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
package sporemodder.view.editors.spui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import sporemodder.ProjectManager;
import sporemodder.file.ResourceKey;
import sporemodder.util.ProjectItem;
import sporemodder.view.ProjectTreeCell;

public class SpuiImageFileChooser extends Dialog<ButtonType> {

	private final TreeView<ProjectItem> treeView;
	private final ImageView imageView = new ImageView();
	
	public SpuiImageFileChooser() {
		super();
		
		treeView = new TreeView<>(ProjectManager.get().getTreeView().getRoot());
		treeView.setCellFactory(c -> new ProjectTreeCell(false));
		treeView.setPrefHeight(500);
		treeView.setPrefWidth(320);
		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			updateButtons();
			if (isValidImage(newValue)) {
				Image image = new Image(ProjectManager.get().getFile(newValue.getValue().getRelativePath()).toURI().toString());
				imageView.setImage(image);
			} else {
				imageView.setImage(null);
			}
		});
		
		ScrollPane imageContainer = new ScrollPane(imageView);
		imageContainer.setPrefWidth(512);
		imageContainer.setPrefHeight(512);
		
		HBox pane = new HBox(5);
		pane.getChildren().addAll(treeView, imageContainer);
		
		setTitle("Choose an image");
		getDialogPane().setContent(pane);
		getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		updateButtons();
	}
	
	public void setSelectedFile(ResourceKey key) {
		ProjectManager p = ProjectManager.get();
		p.selectItem(treeView, p.getLoadedItem(p.keyToRelativePath(key)));
	}
	
	public ResourceKey getSelectedFile() {
		TreeItem<ProjectItem> item = treeView.getSelectionModel().getSelectedItem();
		return ProjectManager.get().getResourceKey(item.getValue());
	}
	
	public Image getSelectedImage() {
		return imageView.getImage();
	}
	
	private void updateButtons() {
		TreeItem<ProjectItem> item = treeView.getSelectionModel().getSelectedItem();
		boolean disable = !isValidImage(item);
		
		getDialogPane().lookupButton(ButtonType.OK).setDisable(disable);
	}
	
	private static int getParentCount(TreeItem<ProjectItem> item) {
		int count = 0;
		while ((item = item.getParent()) != null) ++count;
		return count;
	}
	
	private static boolean isValidImage(TreeItem<ProjectItem> item) {
		// It must have folder and project root parents
		return item != null && item.getValue().getName().endsWith(".png") && getParentCount(item) == 2;
	}
}
