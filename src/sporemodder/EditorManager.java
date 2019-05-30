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

package sporemodder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sporemodder.util.ProjectItem;
import sporemodder.view.EditorPaneUI;
import sporemodder.view.EditorPaneUI.EditorTab;
import sporemodder.view.StatusBar.Status;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.UserInterface;
import sporemodder.view.editors.AnimTextEditorFactory;
import sporemodder.view.editors.CompiledShaderViewerFactory;
import sporemodder.view.editors.EditHistoryEditor;
import sporemodder.view.editors.EditorFactory;
import sporemodder.view.editors.ImageViewer;
import sporemodder.view.editors.ItemEditor;
import sporemodder.view.editors.PctpEditorFactory;
import sporemodder.view.editors.PfxEditorFactory;
import sporemodder.view.editors.PropEditorFactory;
import sporemodder.view.editors.RWModelViewer;
import sporemodder.view.editors.SearchableEditor;
import sporemodder.view.editors.ShaderBuilderEditorFactory;
import sporemodder.view.editors.ShaderFragmentEditorFactory;
import sporemodder.view.editors.SmtTextEditorFactory;
import sporemodder.view.editors.SpuiEditorFactory;
import sporemodder.view.editors.TextEditorFactory;
import sporemodder.view.editors.TlsaEditorFactory;
import sporemodder.view.editors.XmlEditorFactory;
import sporemodder.view.syntax.HlslSyntax;
import sporemodder.view.syntax.SyntaxFormatFactory;
import sporemodder.view.syntax.XmlSyntax;

/**
 * This class keeps track of all the supported file editors (such as text editor, texture viewer,...),
 * as well as all the supported syntax highlighting formats.
 * <p>
 * This class also controls the files that are currently being edited. There's always at least one editor open: the main editor.
 * The main editor is reused when a file is clicked, whereas double-clicking a file opens it in an additional tab/editor.
 * <p>
 * The files represented in the project tree rely on the type of editor that would open them; for that reason, this manager also controls
 * the icons displayed in the project tree.
 */
public class EditorManager extends AbstractManager implements UIUpdateListener {

	/**
	 * Returns the current instance of the EditorManager class.
	 */
	public static EditorManager get() {
		return MainApp.get().getEditorManager();
	}
	
	
	/** All the supported editor factories. */
	private final List<EditorFactory> editorFactories = new ArrayList<EditorFactory>();
	/** The default editor factory that is used for non-specialized file types. */
	private EditorFactory defaultEditorFactory;
	
	/** The active editor tab, the one that is currently selected and being used. */
	private EditorTab activeEditorTab;
	
	/** The main editor tab. When the user clicks on a file it opens in the main editor. */
	private EditorTab mainEditorTab;
	
	private final List<SyntaxFormatFactory> syntaxHighlighters = new ArrayList<SyntaxFormatFactory>();
	
	/** The Image used to display the icon for folder items in the project tree. */
	private Image folderIcon;
	
	private EditorPaneUI paneUI;
	
	private boolean disableTabEvents = false;
	private EditorTab previousMainEditorTab;
	
	// We could do this by just getting from mainEditorTab, but sometimes we might want to allow text search on special editors (like the RW4 viewer)
	private SearchableEditor searchableEditor;
	private EditHistoryEditor editHistoryEditor;
	
	/**
	 * Loads all the supported editors and syntax highlighting formats (both the default ones and the plugin ones).
	 * The plugin ones are loaded first and therefore have priority over the default ones; this means that plugins can override
	 * default editors.
	 */
	@Override
	public void initialize(Properties properties) {
		defaultEditorFactory = new TextEditorFactory();

		
		// Default editors
		// The default one goes first, as it will be the last one to be checked
		editorFactories.add(defaultEditorFactory);
		editorFactories.add(new CompiledShaderViewerFactory());
		editorFactories.add(new ShaderBuilderEditorFactory());
		editorFactories.add(new ShaderFragmentEditorFactory());
		editorFactories.add(new SmtTextEditorFactory());
		editorFactories.add(new XmlEditorFactory());
		editorFactories.add(new AnimTextEditorFactory());
		editorFactories.add(new PctpEditorFactory());
		editorFactories.add(new TlsaEditorFactory());
		editorFactories.add(new SpuiEditorFactory());
		editorFactories.add(new ImageViewer.Factory());
		editorFactories.add(new RWModelViewer.Factory());
		editorFactories.add(new PfxEditorFactory());
		editorFactories.add(new PropEditorFactory());
		
		// Default syntax highlighting
		//TODO add more here!
		syntaxHighlighters.add(new XmlSyntax());
		syntaxHighlighters.add(new HlslSyntax());
		
		
		// Load the icons for the project tree
		folderIcon = UIManager.get().loadImage("item-icon-folder.png");
		
		UIManager.get().addListener(this);
	}
	
	public List<EditorFactory> getEditorFactories() {
		return editorFactories;
	}
	
	public List<SyntaxFormatFactory> getSyntaxHighlighters() {
		return syntaxHighlighters;
	}
	
	@Override
	public void dispose() {
		try {
			for (Tab tab : paneUI.getTabPane().getTabs()) {
				EditorTab t = (EditorTab) tab;
				if (t.editor != null) t.editor.loadFile(null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the ProjectItem that is being currently edited.
	 * @return
	 */
	public ProjectItem getActiveItem() {
		return activeEditorTab == null ? null : activeEditorTab.item;
	}
	
	public void loadFile(ProjectItem item) throws IOException {
		UIManager ui = UIManager.get();
		
		previousMainEditorTab = mainEditorTab;
		
		String relativePath = item.getRelativePath();
		
		EditorTab tab = paneUI.getTab(relativePath);
		if (tab == null) {
			ItemEditor editor = item.createEditor();
			
			// autosave for old tab
			if (activeEditorTab != null && activeEditorTab.editor != null) {
				activeEditorTab.editor.setActive(false);
			}
			
			// Always create a new main tab, because we don't want to modify the previous one
			// Avoid changing selected tab when creating tab
			disableTabEvents = true;
			
			if (mainEditorTab == null) {
				activeEditorTab = tab = mainEditorTab = paneUI.createMainTab(editor, item, relativePath, true);
			} else {
				activeEditorTab = tab = mainEditorTab = paneUI.createMainTab(editor, item, relativePath, false);
				paneUI.getTabPane().getTabs().set(0, tab);
			}
			
			loadFile(mainEditorTab, item);
			
			// If there is searched text and the editor is searchable, show it highlighted
			if (editor != null && editor.supportsSearching() && ProjectManager.get().isShowingSearch()) {
				List<String> searchedWords = ProjectManager.get().getSearchedWords();
				SearchableEditor searchable = (SearchableEditor) editor;
				for (String word : searchedWords) {
					if (searchable.find(word)) break;
				}
			}
			
			disableTabEvents = false;
		}
			
		paneUI.getTabPane().getSelectionModel().select(tab);
		setActiveEditor_impl(paneUI.getTabPane().getTabs().indexOf(tab));
		
		updateFileLabel(relativePath);
		
		ui.notifyUIUpdate(false);
	}
	
	private void loadFile(EditorTab tab, ProjectItem item) throws IOException {
		if (tab.editor != null) {
			tab.editor.loadFile(item);
		}
		
		tab.setText(item.getName());
		
		if (tab.editor != null) {
			tab.setContent(tab.editor.getUI());
		} else {
			tab.setContent(null);
		}
	}
	
	/**
	 * Moves the last loaded file into a new tab, and restores the previous main editor.
	 * This will only work if the passed item is the same as the one loaded.
	 */
	public boolean moveFileToNewTab(ProjectItem item) {
		
		if (mainEditorTab == null || !item.getRelativePath().equals(mainEditorTab.relativePath)) {
			return false;
		}
		
		disableTabEvents = true;
		
		EditorTab tab = new EditorTab(mainEditorTab.editor, mainEditorTab.item, mainEditorTab.relativePath);
		tab.setText(mainEditorTab.getText());
		tab.setContent(mainEditorTab.getContent());
		paneUI.getTabPane().getTabs().add(tab);
		
		paneUI.getTabPane().getTabs().remove(0);
		if (previousMainEditorTab != null) {
			paneUI.getTabPane().getTabs().add(0, previousMainEditorTab);
		}
		
		mainEditorTab = previousMainEditorTab;
		previousMainEditorTab = null;
		
		disableTabEvents = false;
		
		paneUI.getTabPane().getSelectionModel().select(tab);
		
		UIManager.get().notifyUIUpdate(false);
			
		return true;
	}
	
	public ItemEditor getEditor(String relativePath) {
		for (Tab tab : paneUI.getTabPane().getTabs()) {
			EditorTab t = (EditorTab) tab;
			if (relativePath.equals(t.relativePath)) {
				return t.editor;
			}
		}
		return null;
	}
	
	public boolean removeEditor(String relativePath) {
		for (Tab tab : paneUI.getTabPane().getTabs()) {
			if (relativePath.equals(((EditorTab) tab).relativePath)) {
				paneUI.getTabPane().getTabs().remove(tab);
				return true;
			}
		}
		return false;
	}
	
	public boolean reloadEditor(String relativePath) throws IOException {
		List<Tab> tabsToRemove = new ArrayList<>();
		for (Tab tab : paneUI.getTabPane().getTabs()) {
			EditorTab t = (EditorTab) tab;
			if (relativePath.equals(t.relativePath)) {
				reloadEditor(t, tabsToRemove);
				return true;
			}
		}
		paneUI.getTabPane().getTabs().removeAll(tabsToRemove);
		return false;
	}
	
	private void reloadEditor(EditorTab t, List<Tab> tabsToRemove) throws IOException {
		ProjectItem item = ProjectManager.get().getItem(t.relativePath);
		if (item == null) {
			// Removing the tab here will throw ConcurrentModificationException as we are iterating the tabs
			tabsToRemove.add(t);
		} else if (t.item != item) {
			t.item = item;
			if (t.editor != null) {
				if (t.item.isMod()) {
					t.editor.save();
				}
				t.editor.loadFile(t.item);
			}
		}
	}
	
	/**
	 * Reloads all the editors whose ProjectItems changed.
	 */
	public void reloadEditors() throws IOException {
		List<Tab> tabsToRemove = new ArrayList<>();
		for (Tab tab : paneUI.getTabPane().getTabs()) {
			reloadEditor((EditorTab) tab, tabsToRemove);
		}
		paneUI.getTabPane().getTabs().removeAll(tabsToRemove);
	}
	
	
	public ItemEditor getActiveEditor() {
		return activeEditorTab == null ? null : activeEditorTab.editor;
	}
	
	public void setActiveEditor(int index) {
		paneUI.getTabPane().getSelectionModel().select(index);
	}
	
	private void updateSearchableEditor() {
		if (activeEditorTab != null && activeEditorTab.editor != null && activeEditorTab.editor.supportsSearching()) {
			setSearchableEditor((SearchableEditor) activeEditorTab.editor);
		} else {
			setSearchableEditor(null);
		}
		
		if (activeEditorTab != null && activeEditorTab.editor != null && activeEditorTab.editor.supportsEditHistory()) {
			setEditHistoryEditor((EditHistoryEditor) activeEditorTab.editor);
		} else {
			setEditHistoryEditor(null);
		}
	}
	
	/**
	 * Called when another tab is selected, updates the activeEditor variable and calls the setActive method on the previous and new editor tabs.
	 * @param index
	 */
	private void setActiveEditor_impl(int index) {
		if (activeEditorTab != null) {
			int currentIndex = paneUI.getTabPane().getTabs().indexOf(activeEditorTab);
			
			if (currentIndex == index) {
				// We still want to update the searchable editor and status label
				updateSearchableEditor();
				updateStatusLabel();
				if (activeEditorTab.editor != null) {
					activeEditorTab.editor.setActive(true);
				}
				return;
			}
			else if (activeEditorTab.editor != null) {
				activeEditorTab.editor.setActive(false);
			}
		}
		
		if (index != -1) {
			activeEditorTab = (EditorTab) paneUI.getTabPane().getTabs().get(index);
			if (activeEditorTab != null) {
				if (activeEditorTab.relativePath != null) {
					updateFileLabel(activeEditorTab.relativePath);
				}
			}
			
			updateStatusLabel();
		}

		if (activeEditorTab != null && activeEditorTab.editor != null) {
			activeEditorTab.editor.setActive(true);
		}
		
		updateSearchableEditor();

		UIManager.get().notifyUIUpdate(false);
	}
	
	private void updateStatusLabel() {
		UserInterface ui = UIManager.get().getUserInterface();
		ui.setStatusFile(activeEditorTab == null ? null : activeEditorTab.item);
		ui.setStatusInfo(null);
		ui.getStatusBar().setStatus(Status.DEFAULT);
	}
	
	private void updateFileLabel(String relativePath) {
		if (relativePath != null) {
			if (relativePath.startsWith("..")) {
				paneUI.setFileLabel(null, relativePath);
			}
			else {
				paneUI.setFileLabel(ProjectManager.get().getProjectByFile(relativePath).getName(), relativePath);
			}
		}
	}
	
	
	public void loadFixedTabs(List<String> tabPaths) {
		disableTabEvents = true;
		
		for (String path : tabPaths) {
			ProjectItem item = ProjectManager.get().getItem(path);
			if (item != null) {
				EditorTab tab = new EditorTab(item.createEditor(), item, path);
				try {
					loadFile(tab, item);
					paneUI.getTabPane().getTabs().add(tab);
				} 
				catch (IOException e) {
					// Just ignore it
					e.printStackTrace();
				}
			}
		}
		
		disableTabEvents = false;
		
		if (!paneUI.getTabPane().getTabs().isEmpty()) {
			setActiveEditor(0);
			// The tabPane might not trigger the event, so ensure it is done manually
			setActiveEditor_impl(0);
		}
	}
	
	public void clearTabs() {
		paneUI.getTabPane().getTabs().clear();
		mainEditorTab = null;
		activeEditorTab = null;
		previousMainEditorTab = null;
	}
	
	public void setUI(EditorPaneUI ui) {
		paneUI = ui;
		
		ui.getTabPane().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (!disableTabEvents && newValue != oldValue) {
				setActiveEditor_impl(ui.getTabPane().getTabs().indexOf(newValue));
			}
		});
		
		
		ui.getTabPane().getTabs().addListener((ListChangeListener.Change<? extends Tab> l) -> {
			while (l.next()) {
				for (Tab tab : l.getRemoved()) {
					ItemEditor editor = ((EditorTab) tab).editor;
					if (editor != null) {
						//This will save the file or ask for confirmation
						editor.setActive(false);
					}
					
					if (tab == mainEditorTab) {
						mainEditorTab = null;
					}
				}
			}

			if (!disableTabEvents) {
				List<String> paths = ProjectManager.get().getActive().getFixedTabPaths();
				paths.clear();
				for (Tab tab : l.getList()) {
					
					if (tab != mainEditorTab) {
						paths.add(((EditorTab) tab).relativePath);
					}
				}
			}
			
		});
	}

	
	/**
	 * Returns the first syntax highlighter that supports the given file. Plugin highlighters are given more priority than default ones.
	 * If no syntax highlighter is found, it returns null.
	 * @param file
	 * @return
	 */
	public SyntaxFormatFactory getSyntaxHighlighting(File file) {
		
		ListIterator<SyntaxFormatFactory> it = syntaxHighlighters.listIterator(syntaxHighlighters.size());
		// Default one is first, so iterate them backwards
		while (it.hasPrevious()) {
			SyntaxFormatFactory syntax = it.previous();
			if (syntax.isSupportedFile(file)) {
				return syntax;
			}
		}
		// This never happens, as the default one always returns true on isSupported
		return null;
	}
	
	/**
	 * Returns the first editor factory that supports the given file. Plugin editors are given more priority than default ones.
	 * Generally, the default text editor is returned in case no other editor supports the file; if the text editor does not support it
	 * neither (for example, the item is a folder), it returns null.
	 * @param file
	 * @return
	 */
	public EditorFactory getEditorFactory(ProjectItem item) {
		
		ListIterator<EditorFactory> it = editorFactories.listIterator(editorFactories.size());
		// Default one is first, so iterate them backwards
		while (it.hasPrevious()) {
			EditorFactory factory = it.previous();
			if (factory.isSupportedFile(item)) {
				return factory;
			}
		}
		// This never happens, as the default one always returns true on isSupported
		return null;
	}

	
	public void setTitle(ItemEditor editor, String title) {
		// This method might be called when an editor is closed (and therefore saved), so the editor might not exist anymore
		Tab tab = paneUI.getTab(editor);
		if (tab != null) tab.setText(title);
	}
	
	/**
	 * Returns the icon used to represent the specified project item.
	 * <li>If the item is the root folder, no icon is returned.
	 * <li>If the item is a folder, the folder icon will be returned.
	 * <li>For the rest of items, all supported editors are checked until one returns an icon; otherwise no icon will be used.
	 * @param item
	 * @return
	 */
	public Node getIcon(ProjectItem item) {
		if (item.isFolder() && !item.isRoot()) {
			ImageView imageView = new ImageView(folderIcon);
			imageView.setFitWidth(24);
			imageView.setFitHeight(18);
			return imageView;
		}
		else {
			for (EditorFactory editor : editorFactories) {
				Node icon = editor.getIcon(item);
				if (icon != null) {
					return icon;
				}
			}
			return null;
		}
	}
	
	public boolean canSave() {
		return activeEditorTab != null && activeEditorTab.editor != null && activeEditorTab.editor.isEditable();
	}
	
	public void saveActive() {
		if (canSave()) {
			activeEditorTab.editor.save();
			
			// Update the UI
			UIManager.get().notifyUIUpdate(false);
		}
	}

	@Override
	public void onUIUpdate(boolean isFirstUpdate) {
		// We don't do this anymore, as source files can be edited and included into the mod
//		// Ensure the "editability" of all editors is correct
//		for (Tab tab : paneUI.getTabPane().getTabs()) {
//			EditorTab t = (EditorTab) tab;
//			if (t.editor != null && t.item != null) t.editor.setEditable(t.item.isMod());
//		}
	}
	
	public boolean isAutosaveEnabled() {
		// I think there's no point in letting the user change this
		return false;
	}

	public SearchableEditor getSearchableEditor() {
		return searchableEditor;
	}

	public void setSearchableEditor(SearchableEditor searchableEditor) {
		this.searchableEditor = searchableEditor;
	}

	public EditHistoryEditor getEditHistoryEditor() {
		return editHistoryEditor;
	}

	public void setEditHistoryEditor(EditHistoryEditor editHistoryEditor) {
		this.editHistoryEditor = editHistoryEditor;
	}

}
