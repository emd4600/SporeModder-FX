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

import javafx.beans.binding.Bindings;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.StyleSheet;
import sporemodder.file.spui.StyleSheetInstance;
import sporemodder.file.spui.components.Borders;
import sporemodder.view.FilterableTreeItem;
import sporemodder.view.FilterableTreeItem.TreeItemPredicate;

public class SpuiStyleChooser extends Dialog<ButtonType> {
	private static final String PREVIEW_TEXT = "AaBbCcDdEeFfGg\r\n1234567890\r\n!@#%^&*()";
	
	private class StyleTreeCell extends TreeCell<StyleSheetInstance> {
		public StyleTreeCell() {
			super();
			
			this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
					SpuiStyleChooser.this.setResult(ButtonType.OK);
					SpuiStyleChooser.this.close();
				}
			});
		}
		
		@Override protected void updateItem(StyleSheetInstance item, boolean empty) {
			super.updateItem(item, empty);
			
			setGraphic(null);
			if (item == null || empty) {
				setText(null);
			} else {
				setText(item.toString());
			}
		}
	}
	
	private final TextField tfSearch = new TextField();
	// It's not really a tree, but we use it for searching
	private final FilterableTreeItem<StyleSheetInstance> rootItem = new FilterableTreeItem<>(null);
	private final TreeView<StyleSheetInstance> treeView = new TreeView<>(rootItem);
	
	private final Canvas previewCanvas = new Canvas();
	
	public SpuiStyleChooser() {
		
		previewCanvas.setWidth(400);
		previewCanvas.setHeight(300);
		
		tfSearch.setPromptText("Search");
		
		treeView.setShowRoot(false);
		rootItem.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			if (tfSearch.getText().isEmpty()) return null;
			else return TreeItemPredicate.create(value -> value.toString().toLowerCase().contains(tfSearch.getText().toLowerCase()));
		}, tfSearch.textProperty()));
		
		treeView.setCellFactory(tree -> new StyleTreeCell());
		
		// Add all the style sheets
		for (StyleSheetInstance ins : StyleSheet.getActiveStyleSheet().getInstances()) {
			rootItem.getInternalChildren().add(new FilterableTreeItem<>(ins));
		}
		
		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) paintPreview(newValue.getValue());
		});
		
		BorderPane leftPane = new BorderPane();
		leftPane.setTop(tfSearch);
		leftPane.setCenter(treeView);
		leftPane.setPrefWidth(300);
		
		HBox contentPane = new HBox(5);
		contentPane.setFillHeight(true);
		
		contentPane.getChildren().addAll(leftPane, previewCanvas);
		
		setTitle("Choose a style");
		getDialogPane().setContent(contentPane);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	}
	
	public StyleSheetInstance getSelectedStyle() {
		return treeView.getSelectionModel().getSelectedItem().getValue();
	}
	
	public void setSelectedStyle(StyleSheetInstance style) {
		for (TreeItem<StyleSheetInstance> item : rootItem.getInternalChildren()) {
			if (item.getValue() == style) {
				treeView.getSelectionModel().select(item);
				break;
			}
		}
	}
	
	private void paintPreview(StyleSheetInstance instance) {
		previewCanvas.getGraphicsContext2D().clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
		
		if (instance != null) instance.paintText(previewCanvas.getGraphicsContext2D(), PREVIEW_TEXT, Color.BLACK, 
				new SPUIRectangle(0.0f, 0.0f, (float)previewCanvas.getWidth(), (float)previewCanvas.getHeight()), new Borders());
	}
}
