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
import java.util.HashMap;
import java.util.Map;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonProgramButton;
import io.github.emd4600.javafxribbon.RibbonTab;
import io.github.emd4600.javafxribbon.RibbonWindow;
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
import sporemodder.view.HideablePane.HideSide;
import sporemodder.view.ribbons.*;

/**
 * The class that contains the main user interface. The SporeModderFX UI is basically a window with a ribbon, and the following objects:
 * <li>{@link ProjectTreeUI}: On the left side, this panel contains the Project tree view and file search.</li>
 * <li>{@link EditorPaneUI}: On the center, this panel contains the tab panes that show the editors.</li>
 * <li>{@link InspectorPaneUI}: On the right side, this panel contains links to documentation and useful tools for certain editors. </li>
 * <li>{@link StatusBar}: On the bottom, a small panel that contains information about the current file, error, etc</li>
 * <p>
 * This class also allows access to the {@link Ribbon}, to the {@link ProgramMenu} and to the {@link IntroUI}
 * <p>
 * Use {@code UserInterface.get()} to get the active (and only) object of this class.
 */
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
	
	/** A map to store the controllers of the ribbon tabs. Their identifiers are stored in uppercase. */
	private final Map<String, RibbonTabController> ribbonTabs = new HashMap<>();
	private String activeRibbonTab;
	
	@Override
	public String getUserAgentStylesheet() {
		return PathManager.get().getStyleFile("ribbonstyle.css").toURI().toString();
	}
	
	@Override public InputStream getResource(String fileName) {
		try {
			return new FileInputStream(PathManager.get().getStyleFile(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the only object of this class.
	 * @return
	 */
	public static UserInterface get() {
		return UIManager.get().getUserInterface();
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
		projectTree = UIManager.get().loadUI("ProjectTreeUI");
		rootPane.setLeft(new HideablePane((Region) projectTree.getMainNode(), HideSide.RIGHT).getNode());
		
		
		/* --- Inspector Pane --- */
		// We load the inspector pane and insert it to the RIGHT region.
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

		inspectorPane.reset();

		/* --- Ribbon --- */
		loadRibbon();

	}
	
	/**
	 * Shows the main user interface, the one that contains the project view, editors, etc
	 */
	public void showMainUI() {
		setContent(rootPane);
	}
	
	/**
	 * Shows the introduction user interface, the one that is shown by default when the user opens the program.
	 */
	public void showIntroUI() {
		setContent(introUI.getMainNode());
	}
	
	/** 
	 * Inserts all the ribbon tabs into the ribbon. This includes both the default tabs that 
	 * come with the program as well as any tabs plugins might add.
	 */
	private void loadRibbon() {
		
		Ribbon ribbon = getRibbon();
		
		//ribbon.setContentHeight(115);
		ribbon.setContentHeight(UIManager.get().scaleByDpi(92));
		
		ProjectRibbonTab projectRibbonTab = new ProjectRibbonTab();
		projectRibbonTab.addTab(ribbon);
		ribbonTabs.put(ProjectRibbonTab.ID.toUpperCase(), projectRibbonTab);
		
		EditRibbonTab editRibbonTab = new EditRibbonTab();
		editRibbonTab.addTab(ribbon);
		ribbonTabs.put(EditRibbonTab.ID.toUpperCase(), editRibbonTab);
		
		UtilRibbonTab utilRibbonTab = new UtilRibbonTab();
		utilRibbonTab.addTab(ribbon);
		ribbonTabs.put(UtilRibbonTab.ID.toUpperCase(), utilRibbonTab);

		ModAndGitRibbonTab modAndGitRibbonTab = new ModAndGitRibbonTab();
		modAndGitRibbonTab.addTab(ribbon);
		ribbonTabs.put(ModAndGitRibbonTab.ID.toUpperCase(), modAndGitRibbonTab);
		
		programMenu = new ProgramMenu();
		programMenu.initialize(ribbon);
		
		RibbonProgramButton programButton = new RibbonProgramButton("File");
		programButton.getStyleClass().add(RibbonTab.DEFAULT_STYLE_CLASS);
		programButton.setRibbonMenu(programMenu.getMenu());
		ribbon.setProgramButton(programButton);
	}
	
	public void setRibbonTabController(String id, RibbonTabController controller) {
		ribbonTabs.put(id, controller);
	}
	
	/** Returns the controller of the ribbon tab identified with the given string. */
	public RibbonTabController getRibbonTabController(String id) {
		return ribbonTabs.get(id.toUpperCase());
	}
	
	public void setActiveRibbonTab(String id) {
		if (id == null || !ribbonTabs.containsKey(id)) 
			throw new IllegalArgumentException();
		
		activeRibbonTab = id;
		getRibbon().getSelectionModel().select(ribbonTabs.get(id).getTab());
	}
	
	/** Returns the id of the active ribbon tab controller. */
	public String getActiveRibbonTab() {
		return activeRibbonTab;
	}

	
	/**
	 * This method can be used to isolate the inspector, making everything invisible except the inspector panel.
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
	 * Sets the current project item, so that the status bar text is updated properly.
	 * This will change both the file name (shown on the left) and type information (shown on the right). The type information
	 * is taken from the "type_names.txt" documentation file.
	 * @param item The current item, or null.
	 */
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

	/**
	 * Returns the main menu that is shown in the first tab of the ribbon.
	 * @return
	 */
	public ProgramMenu getProgramMenu() {
		return programMenu;
	}
	
	/**
	 * Returns the editor panel, the one that contains the tabs with the text editor/texture viewer/etc.
	 * * @return
	 */
	public EditorPaneUI getEditorPane() {
		return editorPane;
	}
	
	/**
	 * Returns the panel that contains the project tree view and the search bar.
	 * @returns
	 */
	public ProjectTreeUI getProjectTree() {
		return projectTree;
	}
	
	/**
	 * Returns the panel that contains the inspector, which provides extra tools for editing files.
	 * @return
	 */
	public InspectorPaneUI getInspectorPane() {
		return inspectorPane;
	}
	
	/**
	 * Returns the status bar that is shown on the bottom part of the program, and that contains information about the current file, error, etc..
	 * @return
	 */
	public StatusBar getStatusBar() {
		return statusBar;
	}

	/**
	 * Returns the pane that overlays the user interface when a dialog is shown.
	 * @return
	 */
	public Pane getDialogOverlay() {
		return dialogOverlay;
	}
	
	/**
	 * Returns the panel that is shown when the program is opened.
	 * @return
	 */
	public IntroUI getIntroUI() {
		return introUI;
	}
}
