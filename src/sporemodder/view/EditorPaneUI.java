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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import sporemodder.EditorManager;
import sporemodder.util.ProjectItem;
import sporemodder.view.editors.ItemEditor;

public class EditorPaneUI implements Controller {
	
	public static final String MAIN_TAB_STYLE = "main-editor-tab";
	
	public static class EditorTab extends Tab {
		public ItemEditor editor;
		public String relativePath;
		public ProjectItem item;
		
		public EditorTab(ItemEditor editor, ProjectItem item, String relativePath) {
			super();
			this.editor = editor;
			this.item = item;
			this.relativePath = relativePath;
		}
	}
	
	@FXML
	private Node mainNode;
	
	@FXML
	/** The tab pane that holds the currently opened files. */
	private TabPane tabPane;
	
	@FXML
	/** A label that shows the name of the current file, as well as to what project it belongs. */
	private Label nameLabel;

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	@FXML
	private void initialize() {
		EditorManager.get().setUI(this);
	}
	
	public TabPane getTabPane() {
		return tabPane;
	}
	
	public EditorTab getTab(int index) {
		return (EditorTab) tabPane.getTabs().get(index);	
	}
	
	public EditorTab getTab(String relativePath) {
		for (Tab tab : tabPane.getTabs()) {
			EditorTab t = (EditorTab) tab;
			if (t.relativePath.equals(relativePath)) {
				return t;
			}
		}
		return null;
	}
	
	public EditorTab getTab(ItemEditor editor) {
		for (Tab tab : tabPane.getTabs()) {
			EditorTab t = (EditorTab) tab;
			if (t.editor == editor) {
				return t;
			}
		}
		return null;
	}
	
	public EditorTab createMainTab(ItemEditor editor, ProjectItem item, String relativePath, boolean addToTabs) {
		EditorTab tab = new EditorTab(editor, item, relativePath);
		tab.getStyleClass().add(MAIN_TAB_STYLE);
		if (addToTabs) tabPane.getTabs().add(0, tab);
		return tab;
	}

	/**
	 * Changes the label that shows the current file and the project it belongs to.
	 * @param projectName The name of the project.
	 * @param fileName The relative path of the file inside the project.
	 */
	public void setFileLabel(String projectName, String fileName) {
		if (projectName != null) {
			fileName = projectName + " - " + fileName;
		}
		nameLabel.setText(fileName);
	}
}
