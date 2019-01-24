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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import emord.javafx.ribbon.Ribbon;
import emord.javafx.ribbon.RibbonProgramButton;
import emord.javafx.ribbon.RibbonTab;
import emord.javafx.ribbon.RibbonWindow;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import sporemodder.DocumentationManager;
import sporemodder.PathManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;
import sporemodder.view.DragResizer.DragSide;
import sporemodder.view.HideablePane.HideSide;
import sporemodder.view.ribbons.EditRibbonTab;
import sporemodder.view.ribbons.ProgramMenu;
import sporemodder.view.ribbons.ProjectRibbonTab;
import sporemodder.view.ribbons.UtilRibbonTab;

public class UserInterface extends RibbonWindow {
	
	private IntroUI introUI;
	
	/** The pane that overlays the user interface when a dialog is shown. */
	private FlowPane dialogOverlay;
	
	/** The main panel in the user interface. It's just a simple BorderPane where all the elements will be inserted. */
	private BorderPane rootPane;
	
	/** The panel that contains the project tree and the search bar. */
	private ProjectTreeUI projectTree;
	
	/** A panel that contains the inspector, that provides extra tools for editing files. */
	private InspectorPaneUI inspectorPane;
	
	/** The main panel, the one that contains the text editor/texture viewer/etc. */
	private EditorPaneUI editorPane;
	
	/** The status bar shown at the bottom of the window. */
	private StatusBar statusBar;
	private Label statusNameLabel;
	private Label statusTypeLabel;
	
	/** The main menu that is shown in the first tab of the ribbon. */
	private ProgramMenu programMenu;
	
	@Override
	public String getUserAgentStylesheet() {
		return PathManager.get().getStyleFile("ribbonstyle.css").toURI().toString();
	}
	
	@Override
	public InputStream getResource(String fileName) {
		try {
			return new FileInputStream(PathManager.get().getStyleFile(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads all the user interface and prepares it to be shown. This does not set the user interface visible.
	 */
	public void initialize() {
		
		dialogOverlay = new FlowPane();
		dialogOverlay.getStyleClass().add("dialog-overlay-pane");
		dialogOverlay.setMouseTransparent(true);
		dialogOverlay.setOpacity(0);
		getChildren().add(dialogOverlay);
		
		introUI = UIManager.get().loadUI("IntroUI");
		setContent(introUI.getMainNode());
		
		rootPane = new BorderPane();
		
		/* --- Project Tree --- */
		// We load the project tree and insert it to the LEFT region.
//		projectTree = UIManager.get().loadUI("ProjectTreeUI");
//		DragResizer.makeResizable((Region) projectTree.getMainNode(), DragSide.RIGHT);
//		rootPane.setLeft(projectTree.getMainNode());
		
		projectTree = UIManager.get().loadUI("ProjectTreeUI");
		rootPane.setLeft(new HideablePane((Region) projectTree.getMainNode(), HideSide.RIGHT).getNode());
		
		
		/* --- Inspector Pane --- */
		// We load the inspector pane and insert it to the RIGHT region.
//		inspectorPane = UIManager.get().loadUI("InspectorPaneUI");
//		DragResizer.makeResizable((Pane) inspectorPane.getMainNode(), DragSide.LEFT);
//		rootPane.setRight(inspectorPane.getMainNode());
		
		inspectorPane = UIManager.get().loadUI("InspectorPaneUI");
		rootPane.setRight(new HideablePane(inspectorPane.getMainNode(), HideSide.LEFT).getNode());
		
		/* --- Editor Pane --- */
		// We load the editor pane and insert it to the CENTER region.
		editorPane = UIManager.get().loadUI("EditorPaneUI");
		rootPane.setCenter(editorPane.getMainNode());
		
		/* --- Status Bar --- */
		statusBar = new StatusBar();
		rootPane.setBottom(statusBar);
		
		statusNameLabel = new Label();
		statusTypeLabel = new Label();
		
		statusBar.getLeftNodes().add(statusNameLabel);
		statusBar.getRightNodes().add(statusTypeLabel);

		setInspectorContent(null);

		/* --- Ribbon --- */
		loadRibbon();

	}
	
	public void showMainUI() {

		setContent(rootPane);
	}
	
	/** 
	 * Inserts all the ribbon tabs into the ribbon. This includes both the default tabs that 
	 * come with the program as well as any tabs plugins might add.
	 */
	private void loadRibbon() {
		
		Ribbon ribbon = getRibbon();
		ribbon.setContentHeight(130);
		
		ribbon.setContentHeight(115);
		
		ProjectRibbonTab.addTab(ribbon);
		EditRibbonTab.addTab(ribbon);
		UtilRibbonTab.addTab(ribbon);
		
		programMenu = new ProgramMenu();
		programMenu.initialize(ribbon);
		
		RibbonProgramButton programButton = new RibbonProgramButton("File");
		programButton.getStyleClass().add(RibbonTab.DEFAULT_STYLE_CLASS);
		programButton.setRibbonMenu(programMenu.getMenu());
		ribbon.setProgramButton(programButton);
	}

	
	/**
	 * Returns the panel that contains the project tree and the search bar.
	 */
	public ProjectTreeUI getProjectTree() {
		return projectTree;
	}

	/**
	 * Sets the inspector pane content, which provides extra tools for editing files.
	 */
	public void setInspectorContent(Pane pane) {
		inspectorPane.setContent(pane);
	}
	
	/**
	 * Returns the panel that contains the inspector, which provides extra tools for editing files.
	 * @return
	 */
	public InspectorPaneUI getInspectorPane() {
		return inspectorPane;
	}
	
	public void removeInspector() {
		inspectorPane.setContent(null);
		inspectorPane.setTitle(null);
	}
	
	/**
	 * This method can be used to isolate the inspector, making everything not visible except the inspector panel.
	 * It can also be used to restore the visibility of the rest of elements.
	 * @param inspectorOnly
	 */
	public void toggleInspectorOnly(boolean inspectorOnly) {
		if (inspectorOnly) {
			rootPane.setCenter(null);
			rootPane.setLeft(null);
			rootPane.setRight(null);
			
			rootPane.setCenter(inspectorPane.getMainNode());
		}
		else {
			rootPane.setCenter(editorPane.getMainNode());
			rootPane.setLeft(projectTree.getMainNode());
			
			rootPane.setRight(inspectorPane.getMainNode());
		}
	}
	
	/**
	 * Returns the main panel, the one that contains the text editor/texture viewer/etc.
	 * * @return
	 */
	public EditorPaneUI getEditorPane() {
		return editorPane;
	}

	/**
	 * Returns the pane that overlays the user interface when a dialog is shown.
	 * @return
	 */
	public Pane getDialogOverlay() {
		return dialogOverlay;
	}
	
	public StatusBar getStatusBar() {
		return statusBar;
	}
	
	public void setStatusFile(ProjectItem item) {
		if (item == null) {
			statusNameLabel.setText("");
			statusTypeLabel.setText("");
		}
		else {
			String name = item.getName();
			
			statusNameLabel.setText(name);
			
			int indexOf = name.indexOf(".");
			if (indexOf != -1) {
				String extension = name.substring(indexOf);
				String doc = DocumentationManager.get().getDocumentation("type_names", extension);
				
				if (doc.equals(extension)) {
					// Try with only the last part of the extension
					extension = name.substring(name.lastIndexOf("."));
					doc = DocumentationManager.get().getDocumentation("type_names", extension);
				}
				
				// If no documentation has been found, only show the extension
				if (doc.equals(extension)) {
					statusTypeLabel.setText(doc);
				} else {
					statusTypeLabel.setText(doc + " (" + extension + ")");
				}
			}
			else {
				statusTypeLabel.setText("");
			}
		}
	}
	
	public void setStatusInfo(Node node) {
		if (node == null) {
			if (statusBar.getLeftNodes().size() > 1) {
				statusBar.getLeftNodes().remove(1);
			}
		} else {
			if (statusBar.getLeftNodes().size() == 1) {
				statusBar.getLeftNodes().add(node);
			} else {
				statusBar.getLeftNodes().set(1, node);
			}
		}
	}
}
