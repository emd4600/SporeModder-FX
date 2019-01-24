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

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sporemodder.UIManager;

public class InspectorPaneUI implements Controller {
	
	private static final double PREF_WIDTH = 340.0;
	
	@FXML
	private BorderPane mainNode;
	
	@FXML
	private Pane topPane;
	
	@FXML
	private Button expandButton;
	
	@FXML
	private Label titleLabel;
	
	/** Whether the window was maximized before expanding the inspector. */
	private boolean previouslyMaximized;
	/** The bounds (in screen) that the entire scene window had before expanding the inspector. */
	private Rectangle windowBounds = new Rectangle();
	
	private ChangeListener<? super Number> widthListener;
	private ChangeListener<? super Number> heightListener;
	private ChangeListener<? super Boolean> maximizedListener;
	
	private boolean maximizingProgramatically = false;
	
	@FXML
	private void initialize() {
		
		titleLabel.setText(null);
		
		widthListener = (obs, oldValue, newValue) -> {
			mainNode.setPrefWidth(newValue.doubleValue());
		};
		
		heightListener = (obs, oldValue, newValue) -> {
			mainNode.setPrefHeight(newValue.doubleValue());
		};
		
		maximizedListener = (obs, oldValue, newValue) -> {
			if (newValue.booleanValue() && !maximizingProgramatically) {
				contractInspector();
			}
		};
		
		expandButton.setOnAction((event) -> {
			expandInspector();
		});
		
		mainNode.setPrefWidth(PREF_WIDTH);
	}
	
	private void setMaximized(boolean maximize) {
		maximizingProgramatically = true;
		UIManager.get().getPrimaryStage().setMaximized(maximize);
		maximizingProgramatically = false;
	}
	
	private void contractInspector() {
		UIManager ui = UIManager.get();
		Stage stage = ui.getPrimaryStage();
		
		stage.setAlwaysOnTop(false);
		
		ui.getUserInterface().toggleInspectorOnly(false);
		ui.restoreTitle();
		
		ui.getScene().widthProperty().removeListener(widthListener);
		ui.getScene().heightProperty().removeListener(heightListener);
		stage.maximizedProperty().removeListener(maximizedListener);
		
		mainNode.setPrefWidth(PREF_WIDTH);
		
		mainNode.setTop(topPane);
		
		if (previouslyMaximized) {
			setMaximized(true);
		}
		else {
			stage.setWidth(windowBounds.getWidth());
			stage.setHeight(windowBounds.getHeight());
			stage.setX(windowBounds.getX());
			stage.setY(windowBounds.getY());
		}
	}
	
	private void expandInspector() {
		Bounds paneBounds = mainNode.getBoundsInParent();
		
		Stage stage = UIManager.get().getPrimaryStage();
		windowBounds.setWidth(stage.getWidth());
		windowBounds.setHeight(stage.getHeight());
		windowBounds.setX(stage.getX());
		windowBounds.setY(stage.getY());
		
		previouslyMaximized = stage.isMaximized();
		stage.maximizedProperty().addListener(maximizedListener);
		
		if (previouslyMaximized) {
			setMaximized(false);
		}
		
		mainNode.setTop(null);
		
		stage.setWidth(paneBounds.getWidth() + 10);
		stage.setHeight(paneBounds.getHeight());
		
		stage.setAlwaysOnTop(true);
		
		UIManager.get().getUserInterface().toggleInspectorOnly(true);
		stage.setTitle(titleLabel.getText());
		
		mainNode.resize(paneBounds.getWidth() + 10, paneBounds.getHeight());
		
		UIManager.get().getScene().widthProperty().addListener(widthListener);
		UIManager.get().getScene().heightProperty().addListener(heightListener);
	}

	@Override
	public Pane getMainNode() {
		return mainNode;
	}

	public void setContent(Node content) {
		mainNode.setCenter(content);
		
		if (content == null) {
			topPane.setVisible(false);
		} else {
			topPane.setVisible(true);
		}
	}
	
	public void setTitle(String title) {
		titleLabel.setText(title);
	}
}
