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

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import sporemodder.file.spui.components.ISporeImage;

public class SpuiImageChooser extends Dialog<ButtonType> {
	private final FilteredList<ISporeImage> list;
	private final ListView<ISporeImage> listView;
	private final TextField tfSearch = new TextField();
	
	private final Canvas imageCanvas = new Canvas();
	
	public SpuiImageChooser(ObservableList<ISporeImage> images) {
		super();
		
		list = new FilteredList<ISporeImage>(images, item -> true);
		
		tfSearch.setPromptText("Search");
		tfSearch.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null || newValue.isEmpty()) list.setPredicate(item -> true);
			else list.setPredicate(item -> item.toString().toLowerCase().contains(newValue.toLowerCase()));
		});
		
		listView = new ListView<>(list);
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.setPrefHeight(500);
		listView.setPrefWidth(320);
		listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				drawImage(newValue);
			}
		});
		
		imageCanvas.setWidth(512);
		imageCanvas.setHeight(512);
		
		BorderPane pane = new BorderPane();
		pane.setCenter(listView);
		pane.setTop(tfSearch);
		pane.setRight(new ScrollPane(imageCanvas));
		
		setTitle("Choose an image");
		getDialogPane().setContent(pane);
		getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
	}

	public ISporeImage getSelectedImage() {
		return listView.getSelectionModel().getSelectedItem();
	}
	
	public void setSelectedImage(ISporeImage image) {
		listView.getSelectionModel().select(image);
	}
	
	private void drawImage(ISporeImage image) {
		double totalWidth = imageCanvas.getWidth();
		double totalHeight = imageCanvas.getHeight();
		double width = image.getWidth();
		double height = image.getHeight();
		
	
		imageCanvas.getGraphicsContext2D().clearRect(0, 0, totalWidth, totalHeight);
		
		image.drawImage(imageCanvas.getGraphicsContext2D(), 
				0, 0, width, height, 
				(totalWidth - width) / 2.0, (totalHeight - height) / 2.0, width, height);
	}
}
