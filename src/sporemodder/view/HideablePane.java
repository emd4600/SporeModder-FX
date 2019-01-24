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
package sporemodder.view;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import sporemodder.UIManager;

public class HideablePane {

	public static enum HideSide {LEFT, RIGHT};
	
	private static final int ICON_SIZE = 10;
	
	private final HideSide side;
	private final Region region;
	private final BorderPane pane = new BorderPane();
	
	private final Button hideButton = new Button();
	private final ImageView leftArrowImage;
	private final ImageView rightArrowImage;
	
	private boolean isHidden = false;
	
	private double mouseX;
	private double mouseY;
	
	public HideablePane(Region region, HideSide side) {
		this.side = side;
		this.region = region;
	
		leftArrowImage = UIManager.get().loadIcon("arrow-left.png", ICON_SIZE, ICON_SIZE, true);
		rightArrowImage = UIManager.get().loadIcon("arrow-right.png", ICON_SIZE, ICON_SIZE, true);
		
		hideButton.getStyleClass().add("button-no-background");
		
		pane.setCenter(region);
		
		Pane sidePane = new Pane();
		sidePane.getChildren().add(hideButton);
		
		if (side == HideSide.LEFT) pane.setLeft(sidePane);
		else pane.setRight(sidePane);
		updateGraphic();
		
		// -- FUNCTIONALITY -- //
		hideButton.setOnAction(event -> {
			if (isHidden) {
				pane.setCenter(region);
			}
			else {
				pane.setCenter(null);
			}
			
			isHidden = !isHidden;
			updateGraphic();
		});
		
		sidePane.setCursor(Cursor.E_RESIZE);
		hideButton.setCursor(Cursor.DEFAULT);
		
		sidePane.setOnMousePressed(event -> {
			mouseX = event.getSceneX();
			mouseY = event.getSceneY();
		});
		
		sidePane.setOnMouseDragged(event -> {
			if (!isHidden) {
				if (side == HideSide.RIGHT) {
					double x = event.getScreenX();
		            region.setPrefWidth(region.getWidth() + (x - mouseX));
		            mouseX = x;
				} else {
					double x = event.getScreenX();
		            region.setPrefWidth(region.getWidth() - (x - mouseX));
		            mouseX = x;
				}
			}
		});
	}
	
	private void updateGraphic() {
		if (side == HideSide.LEFT) {
			hideButton.setGraphic(isHidden ? leftArrowImage : rightArrowImage);
		} else {
			hideButton.setGraphic(isHidden ? rightArrowImage : leftArrowImage);
		}
	}
	
	public Pane getNode() {
		return pane;
	}
}
