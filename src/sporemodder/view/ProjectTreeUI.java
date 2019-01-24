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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;

public class ProjectTreeUI implements Controller {
	
	@FXML
	private Node mainNode;

	@FXML
	private TextField tfSearch;
	
	@FXML
	private CheckBox cbShowModded;
	
	@FXML
	private TreeView<ProjectItem> tvProjectTree;
	
	@FXML
	private Button searchButton;
	
	private MultipleSelectionModel<TreeItem<ProjectItem>> selectionModel;
	
	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	@FXML
	protected void initialize() {
		
		selectionModel = tvProjectTree.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		
		tvProjectTree.setCellFactory(c -> new ProjectTreeCell(true));
		tvProjectTree.setEditable(true);
		
		searchButton.setGraphic(UIManager.get().loadIcon("search.png", 24, 24, true));
		
		ProjectManager.get().setUI(this);
	}
	
	public TreeView<ProjectItem> getTreeView() {
		return tvProjectTree;
	}

	public TreeItem<ProjectItem> getSelectedNode() {
		return selectionModel.getSelectedItem();
	}
	
	public CheckBox getShowModdedBox() {
		return cbShowModded;
	}
	
	public Button getSearchButton() {
		return searchButton;
	}

	public String getSearchText() {
		return tfSearch.getText();
	}

	public TextField getSearchField() {
		return tfSearch;
	}
}
