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

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.components.AtlasImage;
import sporemodder.file.spui.uidesigner.InspectorRectangle;

public class SpuiUVEditor extends Dialog<ButtonType> {
	
	private static final int POINT_SIZE = 4;
	private static final Color UV_COLOR = Color.RED;

	private final AtlasImage atlasImage = new AtlasImage();
	private final InspectorRectangle inspectorRect = new InspectorRectangle(atlasImage.getUVCoordinates());
	private final Canvas canvas = new Canvas();
	private final ScrollPane scrollPane = new ScrollPane(canvas);
	
	private final Canvas previewCanvas = new Canvas();
	private final ScrollPane previewPane = new ScrollPane(previewCanvas);
	
	private SpuiDraggableType draggable;
	
	private double mouseX;
	private double mouseY;
	private double deltaMouseX;
	private double deltaMouseY;
	
	public SpuiUVEditor(AtlasImage atlasImage) {
		super();
		this.atlasImage.setAtlas(atlasImage.getAtlas());
		this.atlasImage.getUVCoordinates().copy(atlasImage.getUVCoordinates());
		recalculateDimensions();
		
		scrollPane.setPrefWidth(526);
		scrollPane.setPrefHeight(526);
		
		canvas.setWidth(atlasImage.getAtlas().getWidth());
		canvas.setHeight(atlasImage.getAtlas().getHeight());
		
		inspectorRect.setRange(0, 1.0);
		inspectorRect.setStep(0.05);
		inspectorRect.setValue(getUVCoordinates());
		inspectorRect.addValueListener((obs, oldValue, newValue) -> {
			
			recalculateDimensions();
			drawCanvas();
		});
		
		VBox inspectorPane = new VBox(5);
		inspectorPane.getChildren().addAll(inspectorRect.getNode(), previewPane);
		
		HBox pane = new HBox(5);
		pane.getChildren().addAll(scrollPane, inspectorPane);
		
		setTitle("UV Editor");
		getDialogPane().setContent(pane);
		getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		
		previewPane.setPrefWidth(200);
		previewPane.setPrefHeight(200);
		
		
		canvas.setOnMousePressed(event -> {
			SPUIRectangle rect = getScaledCoordinates();
			
			for (SpuiDraggableType type : SpuiDraggableType.values()) {
				SPUIRectangle point = type.getPointRect(rect, POINT_SIZE);
				if (point != null && point.contains(mouseX, mouseY)) {
					draggable = type;
					break;
				}
    		}
    		if (draggable == null && rect.contains(mouseX, mouseY)) {
    			draggable = SpuiDraggableType.COMPONENT;
    		}
		});
		
		canvas.setOnMouseMoved(event -> {
			deltaMouseX = event.getX() - mouseX;
    		deltaMouseY = event.getY() - mouseY;
    		mouseX = event.getX();
    		mouseY = event.getY();
    		
			Cursor cursor = null;
			SPUIRectangle rect = getScaledCoordinates();
    		
    		for (SpuiDraggableType type : SpuiDraggableType.values()) {
				SPUIRectangle point = type.getPointRect(rect, POINT_SIZE);
				if (point != null && point.contains(mouseX, mouseY)) {
					cursor = type.getCursor();
					break;
				}
    		}
    		
    		canvas.setCursor(cursor);
		});
		
		canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
    		deltaMouseX = event.getX() - mouseX;
    		deltaMouseY = event.getY() - mouseY;
    		mouseX = event.getX();
    		mouseY = event.getY();
    		
    		if (event.isSecondaryButtonDown() && draggable != null) {
    			SPUIRectangle rect = getScaledCoordinates();
    			draggable.process(rect, (float)deltaMouseX, (float)deltaMouseY);
    			applyScaledCoordinates(rect);
    			drawCanvas();
    			inspectorRect.setValue(getUVCoordinates());
    		}
    	});
		
		canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
			draggable = null;
		});
		
		drawCanvas();
	}
	
	private void recalculateDimensions() {
		atlasImage.getDimensions()[0] = (int) Math.round(atlasImage.getUVCoordinates().getWidth() * atlasImage.getAtlas().getWidth());
		atlasImage.getDimensions()[1] = (int) Math.round(atlasImage.getUVCoordinates().getHeight() * atlasImage.getAtlas().getHeight());
	}
	
	private void applyScaledCoordinates(SPUIRectangle rect) {
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		SPUIRectangle uv = atlasImage.getUVCoordinates();
		uv.x1 = (float) (rect.x1 / w);
		uv.y1 = (float) (rect.y1 / h);
		uv.x2 = (float) (rect.x2 / w);
		uv.y2 = (float) (rect.y2 / h);
	}
	
	private SPUIRectangle getScaledCoordinates() {
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		SPUIRectangle uv = atlasImage.getUVCoordinates();
		SPUIRectangle scaledUV = new SPUIRectangle();
		scaledUV.x1 = (float) (uv.x1 * w);
		scaledUV.y1 = (float) (uv.y1 * h);
		scaledUV.x2 = (float) (uv.x2 * w);
		scaledUV.y2 = (float) (uv.y2 * h);
		return scaledUV;
	}
	
	private void drawCanvas() {
		
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.drawImage(atlasImage.getAtlas().getImage(), 0, 0);
		g.setStroke(UV_COLOR);
		
		SPUIRectangle scaledUV = getScaledCoordinates();
		
		g.setLineWidth(1);
		g.strokeRect(scaledUV.x1, scaledUV.y1, scaledUV.getWidth(), scaledUV.getHeight());
		
		g.setFill(UV_COLOR);
		for (SpuiDraggableType type : SpuiDraggableType.values()) {
			SPUIRectangle point = type.getPointRect(scaledUV, POINT_SIZE);
			if (point != null) {
				g.fillRect(point.x1, point.y1, point.getWidth(), point.getHeight());
			}
		}
		
		
		w = atlasImage.getWidth();
		h = atlasImage.getHeight();
		previewCanvas.setWidth(w);
		previewCanvas.setHeight(h);
		
		previewCanvas.getGraphicsContext2D().clearRect(0, 0, w, h);
		atlasImage.drawImage(previewCanvas.getGraphicsContext2D(), 0, 0, w, h, 0, 0, w, h, Color.WHITE);
		
		previewCanvas.translateXProperty().bind(previewPane.widthProperty().subtract(w).divide(2.0));
		previewCanvas.translateYProperty().bind(previewPane.heightProperty().subtract(h).divide(2.0));
	}
	
	public SPUIRectangle getUVCoordinates() {
		return atlasImage.getUVCoordinates();
	}
	
	public int[] getDimensions() {
		return atlasImage.getDimensions();
	}
}
