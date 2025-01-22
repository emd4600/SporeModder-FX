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

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import sporemodder.DocumentationManager;
import sporemodder.MainApp;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.Project;
import sporemodder.view.dialogs.*;

public class IntroUI implements Controller {
	
	/** How many projects are displayed on the "Recent projects" list. */
	private static final int RECENT_COUNT = 10;
	
	@FXML private Node mainNode;
	@FXML private VBox documentationPane;
	
	@FXML private VBox recentProjectsList;
	
	@FXML private Button btnNew;
	@FXML private Button btnOpen;
	@FXML private Button btnPresets;
	@FXML private Button btnUnpack;
	@FXML private Button btnImport;
	@FXML private Button btnAddExternal;

	@FXML private Hyperlink modBrowserLink;
	@FXML private Hyperlink moddingServerLink;
	@FXML private Hyperlink officialServerLink;

	@Override
	public Node getMainNode() {
		return mainNode;
	}
	
	private Button createButton(String text, Node graphic) {
		Button button = new Button(text, graphic);
		button.getStyleClass().add("artificial-menu-item");
		return button;
	}

	private void setHyperlinkURL(Hyperlink hyperlink, String url) {
		hyperlink.getStyleClass().add("inspector-docs-link");
		hyperlink.setTooltip(new Tooltip(url));
		hyperlink.setOnAction(event -> MainApp.get().getHostServices().showDocument(url));
	}

	@FXML private void initialize() {
		
		btnNew.setOnAction(event -> {
			CreateProjectSimpleUI.show();
		});
		btnNew.setTooltip(new Tooltip("Creates a new empty project to start modding."));
		
		btnOpen.setOnAction(event -> {
			OpenProjectUI.show();
		});
		btnOpen.setTooltip(new Tooltip("Opens an existing project."));
		
		btnPresets.setOnAction(event -> {
			UnpackPresetsUI.show(null, false);
			UIManager.get().setOverlay(false);
		});
		btnPresets.setTooltip(new Tooltip("Unpacks groups of packages that are useful for investigation and modding."));
		
		btnUnpack.setOnAction(event -> {
			UnpackPackageUI.showChooser();
		});
		btnUnpack.setTooltip(new Tooltip("Unpacks the contents of a .package file into a new project."));
		
		btnImport.setOnAction(event -> {
			ProjectManager.get().importOldProject();
		});
		btnImport.setTooltip(new Tooltip("Imports a project from older SporeModder versions, converting .prop.xml into the modern .prop.prop_t format."));
		
		btnAddExternal.setOnAction(event -> {
			ProjectManager.get().addExternalProject();
		});
		btnAddExternal.setTooltip(new Tooltip("Adds an external folder as a project, allowing to use SporeModder FX on it."));
		
		List<Project> recentProjects = ProjectManager.get().getRecentProjects(RECENT_COUNT);
		recentProjectsList.getChildren().clear();
		for (Project project : recentProjects) {
			Button button = createButton(project.getName(), null);
			button.setMnemonicParsing(false);  // will mess up with '_' symbols in the name otherwise
			button.setOnAction((event) -> {
				ProjectManager.get().setActive(project);
			});
			recentProjectsList.getChildren().add(button);
		}
		
		Pane mainDocumentationPane = DocumentationManager.get().createDocumentationPane("main");
		if (mainDocumentationPane != null) {
			documentationPane.getChildren().add(mainDocumentationPane);
		}

		setHyperlinkURL(modBrowserLink, "https://mods.sporecommunity.com/");
		setHyperlinkURL(moddingServerLink, "https://discord.gg/QR8CjQT");
		setHyperlinkURL(officialServerLink, "https://discord.gg/sporeofficial");
	}
}
