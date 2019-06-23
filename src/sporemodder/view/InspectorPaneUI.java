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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sporemodder.DocumentationManager;
import sporemodder.UIManager;

public class InspectorPaneUI implements Controller {
	
	public static final double PREF_WIDTH = 340.0;
	
	/** The standard name used for the tab that shows the documentation for the current editor. */
	public static final String DOCS_TAB_NAME = "Documentation";
	/** The standard name used for the tab that has buttons to edit the current file. */
	public static final String INSPECTOR_TAB_NAME = "Inspector";
	
	@FXML private BorderPane mainNode;
	@FXML private Pane topPane;
	@FXML private Button expandButton;
	@FXML private Label titleLabel;
	@FXML private TabPane tabPane;
	
	/** Whether the window was maximized before expanding the inspector. */
	private boolean previouslyMaximized;
	/** The bounds (in screen) that the entire scene window had before expanding the inspector. */
	private Rectangle windowBounds = new Rectangle();
	
	private ChangeListener<? super Number> widthListener;
	private ChangeListener<? super Number> heightListener;
	private ChangeListener<? super Boolean> maximizedListener;
	
	private boolean maximizingProgramatically = false;
	
	@FXML private void initialize() {
		
		titleLabel.setText(null);
		titleLabel.getStyleClass().add("inspector-pane-title");
		
		// It doesn't work correctly and never needs to be used anyways
		// We will disable it temporarily
		expandButton.setVisible(false);
		
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

	@Override public Pane getMainNode() {
		return mainNode;
	}
	
	/**
	 * Configures the inspector pane to show the default appearance used in most editors: a title, one tab for documentation and
	 * another for the inspector. All these 3 elements are optional, if their value is null they will be ignored.
	 * @param title The title of the inspector pane.
	 * @param docsEntry The documentation entry ID; if not null, the generated pane will be added to the "Documentation" tab.
	 * @param inspectorContent If not null, it will be added to the "Inspector" tab.
	 */
	public void configureDefault(String title, String docsEntry, Node inspectorContent) {
		reset();
		if (title != null) {
			setTitle(title);
		}
		if (docsEntry != null) {
			Pane pane = DocumentationManager.get().createDocumentationPane(docsEntry);
			if (pane != null) setContent(DOCS_TAB_NAME, pane);
		}
		if (inspectorContent != null) {
			setContent(INSPECTOR_TAB_NAME, inspectorContent);
		}
	}

	/**
	 * Changes the content of a certain tab. After this method is called, the title and tab pane will be visible.
	 * @param tab The title of the new tab.
	 * @param content The content to add to the tab.
	 */
	public void setContent(String tab, Node content) {
		tabPane.setVisible(true);
		topPane.setVisible(true);
		
		Tab t = new Tab();
		t.setText(tab);
		t.setContent(content);
		tabPane.getTabs().add(t);
	}
	
	/**
	 * Resets the inspector pane so no title and tabs are shown. The title and tab pane won't be shown
	 * again until content is added with the {@link #setContent(String, Node)} method.
	 */
	public void reset() {
		tabPane.getTabs().clear();
		tabPane.setVisible(false);
		titleLabel.setText(null);
		topPane.setVisible(false);
	}
	
	/**
	 * Sets the title text shown in the top part of the pane.
	 * @param title
	 */
	public void setTitle(String title) {
		titleLabel.setText(title);
	}
}
