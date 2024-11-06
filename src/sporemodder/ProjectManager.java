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

import java.awt.Desktop;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamWriter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Worker.State;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFUnpackingTask;
import sporemodder.file.prop.PropertyList;
import sporemodder.file.prop.XmlPropParser;
import sporemodder.util.*;
import sporemodder.view.ProjectTreeItem;
import sporemodder.view.ProjectTreeUI;
import sporemodder.view.dialogs.*;
import sporemodder.view.editors.AbstractEditableEditor;
import sporemodder.view.editors.AnimEditorItem;
import sporemodder.view.editors.EffectEditorItem;
import sporemodder.view.editors.ItemEditor;

/**
 * The class that manages all the projects in the program and controls which one is the active.
 */
public class ProjectManager extends AbstractManager {
	
	/**
	 * Returns the current instance of the ProjectManager class.
	 */
	public static ProjectManager get() {
		return MainApp.get().getProjectManager();
	}
	
	private static final String PROPERTY_dontAskAgain_removeItem = "dontAskAgain_removeItem";
	private static final String PROPERTY_dontAskAgain_saveAsMod = "dontAskAgain_saveAsMod";
	private static final String PROPERTY_closeEditedFileDecision = "closeEditedFileDecision";

	private static enum ItemEditType { NEW_FILE, NEW_FOLDER, RENAME, NONE };
	
	public static final String NAMES_REGISTRY_PATH = "sporemaster/names.txt";
	
	/** Project presets: lists of commonly used projects that can be used as sources. 
	 * We save a String array instead of Projects because they don't necessarily exist. */
	private final List<ProjectPreset> presets = new ArrayList<>();

	/** All the factories that can be used to parse files and create project items. */
	private final List<ProjectItemFactory> itemFactories = new ArrayList<>();

	private Project activeProject;

	private final ProjectsList projects = new ProjectsList();
	private final ModBundlesList modBundles = new ModBundlesList();
	
	/** Special items that are always displayed in the bottom part of the project tree. */
	private final List<ProjectItem> specialItems = new ArrayList<>(); 
	
	private ProjectTreeItem rootItem;
	
	private ProjectTreeUI treeUI;
	
	private ItemEditType itemEditType = ItemEditType.NONE;
	/** For cell edit events, the item we are duplicating (if any). */
	private ProjectItem toDuplicateItem;
	
	private final List<String> searchedWords = new ArrayList<String>();
	private final ProjectSearcher projectSearcher = new ProjectSearcher();
	private final ReadOnlyBooleanWrapper isShowingSearch = new ReadOnlyBooleanWrapper();
	
	/** If true, events in the tree view will be ignored. */
	private boolean disableTreeEvents;
	
	private boolean dontAskAgain_removeItem;
	private boolean dontAskAgain_saveAsMod;
	private enum CloseEditedFileDecision {
		SAVE,
		IGNORE,
		ASK
	}
	private CloseEditedFileDecision closeEditedFileDecision = CloseEditedFileDecision.ASK;

	private ProjectTreeItem lastSelectedItem;
	
	private ContextMenu contextMenu;
	
	@Override
	public void initialize(Properties properties) {
		
		dontAskAgain_removeItem = Boolean.parseBoolean(properties.getProperty(PROPERTY_dontAskAgain_removeItem, "false"));
		dontAskAgain_saveAsMod = Boolean.parseBoolean(properties.getProperty(PROPERTY_dontAskAgain_saveAsMod, "false"));
		closeEditedFileDecision = CloseEditedFileDecision.valueOf(properties.getProperty(PROPERTY_closeEditedFileDecision, "ASK"));

		itemFactories.add(new DefaultProjectItemFactory());
		itemFactories.add(new OmitProjectItemFactory());
		itemFactories.add(new ProjectNamesItemFactory());
		
		
		specialItems.add(new EffectEditorItem());
		specialItems.add(new AnimEditorItem());

		// First load mod bundles, as it is necessary to exclude those when loading projects
		modBundles.loadList();
		// Load all standalone projects
		projects.loadStandaloneProjects();
		// Add projects inside mods to the projects list
		modBundles.getAll().forEach(mod -> mod.getProjects().forEach(projects::add));
		
		// Load the project settings after all projects are loaded,
		// because settings can use references to other projects
		projects.loadLastActiveTimes();
		for (Project project : projects.getAll()) {
			project.loadSettings();
		}
		// Load mod infos after project settings, as they need to know package names
		modBundles.loadModInfos();
		
		presets.add(new ProjectPreset("Spore (Game & Graphics)", "The Spore and Galactic Adventures packages containing everything needed for most mods.", true,
				item -> {
					int id = item.name.getGroupID();
					// Exclude audio files
					return !(id == 0x40C0C100 || id == 0x061AD433 || id == 0x021407EE);
				},
				"EP1_PatchData", "Spore_EP1_Data", "PatchData", "Spore_Game", "Spore_Graphics") {
			
			@Override public void getFiles(Map<String, File> files) {
				if (GameManager.get().hasGalacticAdventures()) {
					File folder = GameManager.get().getGalacticAdventures().getDataFolder();
					files.put("EP1_PatchData", new File(folder, "EP1_PatchData.package"));
					files.put("Spore_EP1_Data", new File(folder, "Spore_EP1_Data.package"));
				}
				if (GameManager.get().hasSpore()) {
					File folder = GameManager.get().getSpore().getDataFolder();
					files.put("PatchData", new File(folder, "PatchData.package"));
					files.put("Spore_Game", new File(folder, "Spore_Game.package"));
					files.put("Spore_Graphics", new File(folder, "Spore_Graphics.package"));
				}
			}
		});
		
		presets.add(new ProjectPreset("Spore Audio", "The packages that contain all Spore and Galactic Adventures audio.", false,
				item -> {
					int id = item.name.getGroupID();
					return id == 0x40C0C100 || id == 0x061AD433 || id == 0x021407EE;
				},
				"EP1_PatchData", "Spore_Audio1", "Spore_Audio2") {
			
			@Override public void getFiles(Map<String, File> files) {
				if (GameManager.get().hasGalacticAdventures()) {
					File folder = GameManager.get().getGalacticAdventures().getDataFolder();
					files.put("EP1_PatchData", new File(folder, "EP1_PatchData.package"));
				}
				if (GameManager.get().hasSpore()) {
					File folder = GameManager.get().getSpore().getDataFolder();
					files.put("Spore_Audio1", new File(folder, "Spore_Audio1.package"));
					files.put("Spore_Audio2", new File(folder, "Spore_Audio2.package"));
				}
			}
		});
		
		presets.add(new ProjectPreset("Player Creations", "The packages that contain data about player creations.", false, null,
				"GraphicsCache", "EditorSaves", "Pollination") {
			
			@Override public void getFiles(Map<String, File> files) {
				File folder = GameManager.get().getAppDataFolder();
				if (GameManager.get().hasGalacticAdventures()) {
					files.put("GraphicsCache", new File(folder, "GraphicsCache.package"));
					files.put("EditorSaves", new File(folder, "EditorSaves.package"));
					files.put("Pollination", new File(folder, "Pollination.package"));
				}
			}
		});
	}
	
	@Override
	public void dispose() {
		// This is to save the opened tabs
		if (activeProject != null) {
			activeProject.saveSettings();
		}
	}
	
	@Override public void saveSettings(Properties properties) {
		properties.put(PROPERTY_dontAskAgain_removeItem, dontAskAgain_removeItem ? "true" : "false");
		properties.put(PROPERTY_dontAskAgain_saveAsMod, dontAskAgain_saveAsMod ? "true" : "false");
		properties.put(PROPERTY_closeEditedFileDecision, closeEditedFileDecision.toString());
	}
	
	public TreeView<ProjectItem> getTreeView() {
		return treeUI.getTreeView();
	}
	
	public List<ProjectItemFactory> getItemFactories() {
		return itemFactories;
	}
	
	public boolean isShowModdedOnly() {
		return treeUI.getShowModdedBox().isSelected();
	}
	
	public void setShowModdedOnly(boolean value) {
		treeUI.getShowModdedBox().setSelected(value);
	}
	
	public BooleanProperty showModdedOnlyProperty() {
		return treeUI.getShowModdedBox().selectedProperty();
	}
	
	public boolean isShowingSearch() {
		return isShowingSearch.get();
	}
	
	public ReadOnlyBooleanProperty isShowingSearchProperty() {
		return isShowingSearch.getReadOnlyProperty();
	}
	
	private void getSearchStrings(String text) {
		String[] splits = text.toLowerCase().split(" ");
		searchedWords.clear();
		
		for (int i = 0; i < splits.length;) {
			if (splits[i].isEmpty()) {
				i++;
				continue;
			}
			if (splits[i].startsWith("\""))
			{
				StringBuilder sb = new StringBuilder();
				while (i < splits.length) {
					if (splits[i].endsWith("\"") && (!splits[i].equals("\"") || sb.length() > 0)) {
						sb.append(splits[i].substring(splits[i].startsWith("\"") ? 1 : 0));
						i++;
						break;
					}
					else {
						sb.append(splits[i].startsWith("\"") ? splits[i].substring(1) : splits[i]);
						if (i + 1 < splits.length) sb.append(" ");
						i++;
					}
				}
				searchedWords.add(sb.toString());
			}
			else {
				searchedWords.add(splits[i++]);
			}
		}
	}
	
	/**
	 * Starts a search with the given text; words are separated by spaces. 
	 * If the text is empty or null, this will call {@link #clearSearch()} removing the current search.
	 * @param text
	 */
	public void startSearch(String text) {
		clearSearch();
		
		if (text != null && !text.trim().isEmpty()) {
			clearSearch();
			
			getSearchStrings(text);
			
			projectSearcher.setSearchedWords(searchedWords);
			projectSearcher.setOnlyModFiles(isShowModdedOnly());
			projectSearcher.setExtensiveSearch(true);
			
			projectSearcher.startSearch(rootItem);
			
			UIManager.get().notifyUIUpdate(false);
			
		}
	}
	
	/**
	 * Disables the current searching; this will make the project tree show all items.
	 */
	public void clearSearch() {
		searchedWords.clear();
		if (rootItem != null) {
			rootItem.propagateMatchesSearch(true);
		}
		isShowingSearch.set(false);
		// Set progress bar to 0 again
		projectSearcher.reset();
	}
	
	public void cancelSearch() {
		projectSearcher.cancel();
		clearSearch();
	}
	
	private void startSearchFast() {
		String text = treeUI.getSearchText();
		if (!text.trim().isEmpty()) {
			//TODO cancel existing search task
			
			clearSearch();
			
			getSearchStrings(text);
			
			projectSearcher.setSearchedWords(searchedWords);
			projectSearcher.setOnlyModFiles(isShowModdedOnly());
			projectSearcher.setExtensiveSearch(false);
			
//			rootItem.propagateMatchesSearch(true);
			projectSearcher.startSearch(rootItem);
			
			UIManager.get().notifyUIUpdate(false);
			
		} else {
			clearSearch();
		}
	}
	
	public List<String> getSearchedWords() {
		return searchedWords;
	}
	
	public ReadOnlyBooleanProperty isSearchingProperty() {
		return projectSearcher.isSearchingProperty();
	}
	
	public boolean isSearching() {
		return projectSearcher.isSearching();
	}
	
	public void setUI(ProjectTreeUI treeUI) {
		this.treeUI = treeUI;
		
		// We add the special items to the UI
		List<TreeItem<ProjectItem>> items = treeUI.getSpecialItems().getRoot().getChildren();
		for (ProjectItem item : specialItems) {
			items.add(new ProjectTreeItem(item));
		}
		
		treeUI.getSpecialItems().setPrefHeight(treeUI.getSpecialItems().getFixedCellSize() * specialItems.size() + 5);
		
		
		treeUI.getSearchProgressBar().progressProperty().bind(projectSearcher.progressProperty());
		
		projectSearcher.isSearchingProperty().addListener((obs, oldValue, isSearching) -> {
			
			this.treeUI.getTreeView().setDisable(isSearching);
			this.treeUI.getTreeView().setCursor(isSearching ? Cursor.WAIT : null);
			this.treeUI.getSearchField().setDisable(isSearching);
			this.treeUI.changeSearchGraphic(isSearching);
			
			ProgressBar progressBar = this.treeUI.getSearchProgressBar();
			progressBar.setDisable(!isSearching);
			
			if (isSearching) isShowingSearch.set(true);
		});
		
		treeUI.getSearchField().setOnAction(event -> {
			if (!isSearching()) startSearch(this.treeUI.getSearchField().getText());
			else cancelSearch();
		});
		
		treeUI.getSearchButton().setOnAction(event -> {
			if (!isSearching()) startSearch(this.treeUI.getSearchField().getText());
			else cancelSearch();
		});
		
		treeUI.getSearchFastButton().setOnAction(event -> {
			startSearchFast();
		});
		
		treeUI.getTreeView().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (!disableTreeEvents && newValue != null) {
				lastSelectedItem = (ProjectTreeItem) newValue;
				
				treeUI.getSpecialItems().getSelectionModel().select(null);
				
				UIManager.get().tryAction(() -> EditorManager.get().loadFile(newValue.getValue()), "Cannot load file \"" + newValue.getValue().getName() + "\".");
			}
		});
		
		treeUI.getSpecialItems().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (!disableTreeEvents && newValue != null) {
				lastSelectedItem = (ProjectTreeItem) newValue;
				
				// If the new selected item is null, it means he user selected something in the tree view
				treeUI.getTreeView().getSelectionModel().select(null);
				
				UIManager.get().tryAction(() -> EditorManager.get().loadFile(newValue.getValue()), "Cannot load file \"" + newValue.getValue().getName() + "\".");
			}
		});
		
		treeUI.getTreeView().setOnEditCommit((event) -> {
			if (!disableTreeEvents) {
				
				ProjectTreeItem treeItem = (ProjectTreeItem) event.getTreeItem();
				
				if (itemEditType == ItemEditType.NEW_FILE || itemEditType == ItemEditType.NEW_FOLDER) {
					// If we were setting the name of a new file, committing means creating that file

					if (!UIManager.get().tryAction(() -> {
						ProjectItem item = treeItem.getValue();
						ProjectItem parentItem = treeItem.getParent().getValue();
						
						File parentFile = ensureModFolder(parentItem);
						
						String name = item.getName();
						File file = new File(parentFile, name);
						
						if (toDuplicateItem == null) {
							if (itemEditType == ItemEditType.NEW_FILE) {
								file.createNewFile();
							} else {
								file.mkdir();
							}
						}
						
						item.setIsMod(true);
						item.setFile(file);
						
						// ProjectTreeCell already checks that there are no other mod items with the same name
						// so we can ignore the result of addModItem
						addModItem(item);
						reorderChildren(parentItem.getTreeItem());
						
						if (toDuplicateItem != null) {
							copy(getFile(toDuplicateItem.getRelativePath()), file);
						}
						
						// So it updates the isFolder variable
						item.setFile(file);
						
						treeUI.getTreeView().getSelectionModel().select(treeItem);
						
						UIManager.get().notifyUIUpdate(false);
						
					}, "Cannot create new " + (itemEditType == ItemEditType.NEW_FILE ? "file." : "folder."))) {
						
						// If there was an error creating the file, at least delete the item
						((ProjectTreeItem) treeItem.getParent()).getInternalChildren().remove(treeItem);
					}
				} else if (itemEditType == ItemEditType.RENAME) {
					
					UIManager.get().tryAction(() -> {
						ProjectItem item = treeItem.getValue();
						ProjectItem parentItem = treeItem.getParent().getValue();
						
						File parentFile = ensureModFolder(parentItem);
						ProjectItem sourceItem = null;
						
						// If the item was source, we must restore that source item again
						if (item.isSource()) {
							// We can't use item.getRelativePath() because it would point to the renamed file!
							sourceItem = new ProjectItem(getSourceFile(parentItem.getRelativePath() + File.separatorChar + item.getFile().getName()), item.getProject());
							sourceItem.setIsSource(true);
							sourceItem.setTreeItem(new ProjectTreeItem(sourceItem));
							
							disableTreeEvents = true;
							parentItem.getTreeItem().getInternalChildren().add(sourceItem.getTreeItem());
							disableTreeEvents = false;
						}
						
						File file = new File(parentFile, item.getName());
						
						// 2 possibilities: either the file is mod and already exists in the mod project, or we have to copy it over 
						if (item.isMod()) {
							item.getFile().renameTo(file);
							
							// In source folders we must ensure that the original keeps its children,
							// and copy all its files into the renamed folder
							if (sourceItem != null && file.isDirectory()) {
								sourceItem.getTreeItem().getInternalChildren().setAll(item.getTreeItem().getInternalChildren());
								copyChildren(file, sourceItem.getTreeItem());
								// Also notify the new item that it must reload its children
								((ProjectTreeItem) sourceItem.getTreeItem()).requestReload();
							}
						} else {
							// Copy the source file to the new, renamed location in the mod project
							copy(item.getFile(), file);
							// Tell the old item that it must reload its children (as they are now children of the new item)
							if (sourceItem != null) {
								((ProjectTreeItem) sourceItem.getTreeItem()).requestReload();
							}
						}
						
						
						item.setIsMod(true);
						item.setFile(file);
						
						// If it collides with a source file, addModItem will change it
						item.setIsSource(false);
						addModItem(item);
						
						// The easiest way to rearrange the mod/source status is just reloading the nodes
						treeItem.requestReload();
						
						reorderChildren(parentItem.getTreeItem());
						
						EditorManager.get().reloadEditors();
						
						treeUI.getTreeView().getSelectionModel().select(treeItem);
						// Select might not be enough to open it in the main editor
						EditorManager.get().loadFile(item);
						
						UIManager.get().notifyUIUpdate(false);
						
					}, "Cannot rename item.");
				}

				itemEditType = ItemEditType.NONE;
			}
		});
		
		treeUI.getTreeView().setOnEditCancel((event) -> {
			if (!disableTreeEvents) {
				
				if (itemEditType == ItemEditType.NEW_FILE || itemEditType == ItemEditType.NEW_FOLDER) {
					// If we were setting the name of a new file/folder, canceling means deleting that item
					((ProjectTreeItem) event.getTreeItem().getParent()).getInternalChildren().remove(event.getTreeItem());
				}
				
				itemEditType = ItemEditType.NONE;
			}
		});
		
		contextMenu = new ContextMenu();
		contextMenu.getStyleClass().add("project-tree-context-menu");
		// Don't add the context menu to the tree view because we open it manually
		
		treeUI.getTreeView().addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			contextMenu.hide();
		});
		
		showModdedOnlyProperty().addListener((obs, oldValue, newValue) -> {
			projectSearcher.setOnlyModFiles(newValue);
			if (activeProject != null) activeProject.setShowOnlyModded(newValue);
			treeUI.getTreeView().scrollTo(treeUI.getTreeView().getRow(treeUI.getTreeView().getSelectionModel().getSelectedItem()));
		});
	}
	
	public void selectItem(ProjectItem item) {
		selectItem(treeUI.getTreeView(), item);
	}
	
	public void selectItem(TreeView<ProjectItem> treeView, ProjectItem item) {
		if (item == null) return;
		
		this.getItem(item.getRelativePath());
		treeView.getSelectionModel().select(item.getTreeItem());
		
		expandToItem(treeView, item);
	}
	
	/**
	 * Expands the required items in the Project view so that the given project item is visible.
	 * @param item
	 */
	public void expandToItem(ProjectItem item) {
		expandToItem(treeUI.getTreeView(), item);
	}
	
	/**
	 * Expands the required items in the given project tree view so that the given project item is visible.
	 * @param treeView
	 * @param item
	 */
	public void expandToItem(TreeView<ProjectItem> treeView, ProjectItem item) {
		TreeItem<ProjectItem> parent = item.getTreeItem();
		while ((parent = parent.getParent()) != null) parent.setExpanded(true);
		
		treeView.scrollTo(treeView.getRow(item.getTreeItem()));
	}
	
	/**
	 * Returns the context menu used when right clicking items in the Project view.
	 * @return
	 */
	public ContextMenu getContextMenu() {
		return contextMenu;
	}
	
	/**
	 * Generates a context menu for the given item. The generated menu can be accessed with {@link #getContextMenu()}.
	 * The menu will have items for general actions ("Copy name", "Remove", etc) and some specific items generated by the
	 * supported converters in the {@link FormatManager}.
	 * <p>
	 * Special project items can also add buttons to this menu, since at the end of the method it calls {@link ProjectItem.generateContextMenu(ContextMenu)}
	 * @param item
	 */
	public void generateContextMenu(ProjectItem item) {
		MenuItem itemName = new MenuItem(item.getName());
		itemName.setMnemonicParsing(false);
		
		MenuItem itemCopyName = new MenuItem("Copy name");
		MenuItem itemCopyPath = new MenuItem("Copy file path");
		MenuItem itemCopyKey = new MenuItem("Copy resource key");
		MenuItem itemCopyID = new MenuItem("Copy ID");
		
		MenuItem itemNewFile = new MenuItem(item.isFolder() ? "Create new file" : "Create new file in same directory");
		MenuItem itemNewFolder = new MenuItem(item.isFolder() ? "Create new folder" : "Create new folder in same directory");
		MenuItem itemRename = new MenuItem("Rename");
		MenuItem itemRemove = new MenuItem("Remove");
		MenuItem itemModify = new MenuItem("Modify");
		MenuItem itemImportFiles = new MenuItem("Import files...");
		MenuItem itemRefresh = new MenuItem("Refresh");
		
		MenuItem itemCompare = new MenuItem("Compare");
		MenuItem itemExploreSource = new MenuItem("Explore source folder");
		MenuItem itemExploreMod = new MenuItem("Explore mod folder");
		
		itemCopyName.setOnAction(event -> {
			ClipboardContent content = new ClipboardContent();
			content.putString(item.getName());
			
			Clipboard.getSystemClipboard().setContent(content);
		});
		
		itemCopyPath.setOnAction(event -> {
			ClipboardContent content = new ClipboardContent();
			content.putString(item.getFile().getAbsolutePath());
			
			Clipboard.getSystemClipboard().setContent(content);
		});
		
		itemCopyKey.setOnAction(event -> {
			String key = item.getName();
			String[] splits = key.split("\\.");
			// Remove extra extension such as prop_t
			if (splits.length >= 3) {
				key = splits[0] + "." + splits[1];
			}
			
			TreeItem<ProjectItem> parentItem = item.getTreeItem().getParent();
			// The root node does not count
			if (parentItem != null && !parentItem.getValue().isRoot()) {
				String parentName = parentItem.getValue().getName();
				// The animations~ folder is usually not included
				if (HashManager.get().getFileHash(parentName) != 0) {
					key = parentItem.getValue().getName() + "!" + key;
				}
			}
			
			ClipboardContent content = new ClipboardContent();
			content.putString(key);
			
			Clipboard.getSystemClipboard().setContent(content);
		});
		
		itemCopyID.setOnAction(event -> {
			int hash = HashManager.get().getFileHash(FileManager.removeExtension(item.getName()));
			
			ClipboardContent content = new ClipboardContent();
			content.putString(HashManager.get().hexToString(hash));
			
			Clipboard.getSystemClipboard().setContent(content);
		});
		
		itemName.setOnAction(event -> UIManager.get().tryAction(() -> selectItem(item), "Cannot select file"));
		itemNewFile.setOnAction(event -> UIManager.get().tryAction(item::createNewFile, "Cannot create new file."));
		itemNewFolder.setOnAction(event -> UIManager.get().tryAction(item::createNewFolder, "Cannot create new folder."));
		itemRename.setOnAction(event -> UIManager.get().tryAction(item::renameItem, "Cannot rename item."));
		itemRemove.setOnAction(event -> UIManager.get().tryAction(item::removeItem, "Cannot remove item."));
		itemModify.setOnAction(event -> UIManager.get().tryAction(item::modifyItem, "Cannot modify item."));
		itemImportFiles.setOnAction(event -> UIManager.get().tryAction(item::importFiles, "Cannot import files."));
		itemRefresh.setOnAction(event -> UIManager.get().tryAction(item::refreshItem, "Cannot refresh item."));
		
		itemCompare.setOnAction(event -> UIManager.get().tryAction(item::compareItem, "Cannot compare item."));
		itemExploreSource.setOnAction(event -> UIManager.get().tryAction(item::openSourceFolder, "Cannot open source folder."));
		itemExploreMod.setOnAction(event -> UIManager.get().tryAction(item::openModFolder, "Cannot open mod folder."));
		
		contextMenu.getItems().clear();
		contextMenu.getItems().addAll(itemName, new SeparatorMenuItem());
		contextMenu.getItems().addAll(itemCopyName, itemCopyPath, itemCopyKey, itemCopyID, new SeparatorMenuItem());
		contextMenu.getItems().addAll(itemNewFile, itemNewFolder, itemRename, itemRemove, itemModify, itemImportFiles, itemRefresh, new SeparatorMenuItem());
		
		for (Converter converter : FormatManager.get().getConverters()) {
			try {
				converter.generateContextMenu(contextMenu, item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		item.generateContextMenu(contextMenu);
		
		
		itemCopyPath.setDisable(item.getFile() == null);
		
		itemNewFile.setDisable(!item.canCreateNewFile());
		itemNewFolder.setDisable(!item.canCreateNewFolder());
		itemRename.setDisable(!item.canRenameItem());
		itemRemove.setDisable(!item.canRemoveItem());
		itemModify.setDisable(!item.canModifyItem());
		itemImportFiles.setDisable(!item.canImportFiles());
		itemRefresh.setDisable(!item.canRefreshItem());
		
		itemCompare.setDisable(!item.canCompareItem());
		itemExploreSource.setDisable(!item.canOpenSourceFolder());
		itemExploreMod.setDisable(!item.canOpenModFolder());
		
		int parentCount = 0;
		TreeItem<ProjectItem> parent = item.getTreeItem();
		while ((parent = parent.getParent()) != null) ++parentCount;
		
		// Root folder or something that is more than root/folder/file not allowed
		itemCopyKey.setDisable(parentCount == 0 || parentCount > 2);
	}

	public List<ProjectPreset> getPresets() {
		return presets;
	}

	/** Returns a collection of mod bundles, ordered alphabetically */
	public Collection<ModBundle> getModBundles() {
		return modBundles.getAll();
	}
	
	/**
	 * Returns a certain number of the most recent projects; that is, the projects that were last set as active.
	 * @param count How many projects must be returned.
	 * @return
	 */
	public List<Project> getRecentProjects(int count) {
		return projects.getRecentProjects(count);
	}
	
	/**
	 * Returns the project item factory that can be used to generate a project item that represents the given file. 
	 * This method never returns null, since the default item factory supports all files. Although this takes the parent tree item as a parameter,
	 * it does not modify it.
	 * @param file The file that must be put into a project item.
	 * @param project The project that this file belongs to.
	 * @param parent The parent TreeItem where the project item would be inserted. This will not be modified, but it can be used to check the parent folder, etc
	 * @return The factory that can create a project item for this file; never null.
	 */
	public ProjectItemFactory getItemFactory(File file, Project project, TreeItem<ProjectItem> parent) {
		ListIterator<ProjectItemFactory> it = itemFactories.listIterator(itemFactories.size());
		// Default one is first, so iterate them backwards
		while (it.hasPrevious()) {
			ProjectItemFactory factory = it.previous();
			if (factory.isSupported(file, project, parent)) {
				return factory;
			}
		}
		// This never happens, as the default one always returns true on isSupported
		return null;
	}
	
	/**
	 * Returns the project that has this name, or null if none of the loaded projects uses this name.
	 * @param name
	 * @return
	 */
	public Project getProject(String name) {
		return projects.get(name);
	}

	public ModBundle getModBundle(String name) {
		return modBundles.get(name.toLowerCase());
	}
	
	public Project getOrCreateProject(String name) {
		Project p = getProject(name);
		return p == null ? new Project(name) : p;
	}
	
	public void rename(Project project, String name) {
		Project existing = getProject(name);
		if (existing == null || existing == project) {
			projects.remove(project);
			project.setName(name);
			projects.add(project);
			projects.saveLastActiveTimesNoException();

			if (project == activeProject) {
				rootItem.getValue().setName(name);
				getTreeView().refresh();
			}
		}
	}
	
	public Project getProjectByPackageName(String packageName) {
		for (Project project : projects.getAll()) {
			if (project.getPackageName().equals(packageName)) return project;
		}
		return null;
	}

	public void saveProjectsLastActiveTimes() {
		projects.saveLastActiveTimesNoException();
	}

	/**
	 * Changes the active project. This will update the user interface.
	 * @param project
	 */
	public void setActive(Project project) {
		//TODO do something if project is null?
		// if (project == null)
		
		UIManager.get().showMainUI();
		
		UIManager.get().setTitleInfo(project.getName());
		
		// Disable searching things
		clearSearch();
		
		if (activeProject != null) {
			activeProject.saveSettings();
		}
		activeProject = project;
		
		projectSearcher.setProject(activeProject);
		
		// Update time and save it
		activeProject.updateLastTimeUsed();
		saveProjectsLastActiveTimes();

		// If it doesn't have a mod info, generate it
		ModBundle modBundle = activeProject.getParentModBundle();
		if (modBundle != null) {
			File modInfoFile = modBundle.getModInfoFile();
			if (!modInfoFile.exists()) {
				try {
					modBundle.saveModInfo();
				} catch (ParserConfigurationException | TransformerException e) {
					System.err.println("Failed to generate ModInfo.xml for mod: " + modBundle.getName());
					e.printStackTrace();
				}
            }
		}

		
		// Project names registry
		loadNamesRegistry();

		// Update UI
		ItemEditor activeEditor = EditorManager.get().getActiveEditor();
		if (activeEditor != null) activeEditor.setActive(false);
		EditorManager.get().clearTabs();
		
		UIManager.get().getUserInterface().setStatusFile(null);
		UIManager.get().getUserInterface().setStatusInfo(null);
		
		treeUI.getSearchField().setText("");
		treeUI.getShowModdedBox().setSelected(activeProject.isShowOnlyModded());
		
		refreshProjectTree();
		
		EditorManager.get().loadFixedTabs(project.getFixedTabPaths());
	}
	
	public boolean loadNamesRegistry() {
		HashManager.get().getProjectRegistry().clear();
		File namesFile = getModFile(NAMES_REGISTRY_PATH);
		if (namesFile != null) {
			try {
				HashManager.get().getProjectRegistry().read(namesFile);
				return true;
			} 
			catch (IOException e) {
				// It is not that important anyways, ignore it
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean saveNamesRegistry() {
		return saveNamesRegistry(activeProject);
	}
	
	public boolean saveNamesRegistry(Project project) {
		if (project == null) {
			throw new NullPointerException("Must provide a project to save the name registry.");
		}
		NameRegistry reg = HashManager.get().getProjectRegistry();
		if (!reg.isEmpty()) {
			File namesFile = new File(project.getFolder(), NAMES_REGISTRY_PATH);
			namesFile.mkdirs();
			try (StreamWriter stream = new FileStream(namesFile, "rw")) {
				reg.write(stream);
				return true;
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void refreshProjectTree() {
		
		ProjectTreeUI projectTree = UIManager.get().getUserInterface().getProjectTree();
		TreeView<ProjectItem> treeView = projectTree.getTreeView();
		
		rootItem = new ProjectTreeItem(ProjectItem.createRoot(activeProject, activeProject.getName()));
		rootItem.setPredicate((parent, value) -> {
			if (isShowModdedOnly() && !value.isMod()) {
				return false;
			} else if (isShowingSearch.get() && !value.getTreeItem().getMatchesSearch()) {
				return false;
			} else {
				return true;
			}
		}, showModdedOnlyProperty(), isShowingSearchProperty(), projectSearcher.isSearchingProperty());
		
		rootItem.predicateProperty().addListener((obs, oldValue, newValue) -> {
			if (lastSelectedItem != null) {
				treeUI.getTreeView().getSelectionModel().select(lastSelectedItem);
			}
		});
		
		loadItemFolder(activeProject, activeProject.getReferences(), rootItem);
		
		treeView.setRoot(rootItem);
		rootItem.setExpanded(true);
		
		// Update the UI
		UIManager.get().notifyUIUpdate(false);
	}
	
	public void loadItemFolder(ProjectTreeItem parentItem) {
		loadItemFolder(parentItem.getValue().getProject(), parentItem.getValue().getProject().getReferences(), parentItem);
	}
	
	public void loadItemFolder(Project project, Collection<Project> sources, ProjectTreeItem parentItem) {
					
		/** The nodes that have already been loaded in this level. */
		Map<String, ProjectTreeItem> loadedItems = createChildrenMap(null);
		
		/** The relative path to the folder we are loading. */
		String relativePath = parentItem.getValue().getRelativePath();
		
		// Iterate the sources in reverse orders, as the last ones (the least important) have to be loaded first
		ListIterator<Project> iterable = new LinkedList<>(sources).listIterator(sources.size());
		while (iterable.hasPrevious())
		{
			Project source = iterable.previous();
			File folder = new File(source.getFolder(), relativePath);
			
			if (folder.exists()) {
				String[] fileNames = folder.list();
				if (fileNames != null) {
					for (String fileName : fileNames) {
						File file = new File(folder, fileName);
						
						ProjectItem item = createItem(file, project, parentItem);
						if (item == null) continue;
						
						// We don't add the item to the tree yet; we will wait until all are loaded so we can order them
						ProjectTreeItem treeItem = new ProjectTreeItem(item);
						item.setIsSource(true);
						item.setTreeItem(treeItem);
						
						// We use the real file name here because when loading the mod ones we still don't know the name
						loadedItems.put(relativePath + File.separatorChar + fileName, treeItem);
					}
				}
			}
		}
		
		File folder = new File(project.getFolder(), relativePath);
		if (folder.exists()) {
			String[] fileNames = folder.list();
			if (fileNames != null) {
				for (String fileName : fileNames) {
					ProjectTreeItem treeItem = loadedItems.get(relativePath + File.separatorChar + fileName);
					File file = new File(folder, fileName);
					
					if (treeItem == null) {
						
						ProjectItem item = createItem(file, project, parentItem);
						if (item == null) continue;
						
						// We don't add the item to the tree yet; we will wait until all are loaded so we can order them
						treeItem = new ProjectTreeItem(item);
						item.setTreeItem(treeItem);
						
						// We use the real file name here because when loading the mod ones we still don't know the name
						loadedItems.put(relativePath + File.separatorChar + fileName, treeItem);
					}
					
					treeItem.getValue().setFile(file);
					treeItem.getValue().setIsMod(true);
				}
			}
		}
		
		parentItem.setLoadedChildren(loadedItems.values());
		
		// Change the item of certian editors if it was loaded now
		try {
			EditorManager.get().reloadEditors();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!searchedWords.isEmpty()) {
			// Search the new nodes
			projectSearcher.startSearch(parentItem);
		}
	}
	
	private Map<String, ProjectTreeItem> createChildrenMap(ProjectTreeItem parent) {
		Map<String, ProjectTreeItem> loadedItems = new TreeMap<String, ProjectTreeItem>((o1, o2) -> o1.compareToIgnoreCase(o2));
		
		if (parent != null) {
			String relativePath = parent.getValue().getRelativePath();
			
			for (ProjectTreeItem child : parent.getInternalChildren()) {
				loadedItems.put(relativePath + File.separatorChar + child.getValue().getFile().getName(), child);
			}
		}
		
		return loadedItems;
	}
	
	private ProjectItem createItem(File file, Project project, ProjectTreeItem parentItem) {
		return getItemFactory(file, project, parentItem).create(file, project, parentItem);
	}
	
	/**
	 * Returns the project that is currently active.
	 */
	public Project getActive() {
		return activeProject;
	}

	public ModBundle getActiveModBundle() {
		return activeProject == null ? null : activeProject.getParentModBundle();
	}
	
	/**
	 * Creates a dialog for packing the active project (if any), an operation which will start immediately. 
	 * The dialog has a progress bar to show the progress of the operation; the dialog blocks the rest of the application until
	 * the operation is finished.
	 */
	public boolean packActive(boolean storeDebugInformation) {
		if (activeProject != null) {
			return pack(activeProject, storeDebugInformation);
		} else {
			return false;
		}
	}
	
	/**
	 * Creates a dialog for packing the specified project, an operation which will start immediately. 
	 * The dialog has a progress bar to show the progress of the operation; the dialog blocks the rest of the application until
	 * the operation is finished.
	 */
	public boolean pack(Project project, boolean storeDebugInformation) {
		if (project == activeProject) {
			ItemEditor editor = EditorManager.get().getActiveEditor();
			if (editor != null && editor.isEditable()) {
				AbstractEditableEditor editable = (AbstractEditableEditor)editor;
				if (!editable.isSaved()) {
					editable.setActive(false);
					editable.setActive(true);
				}
			}
		}
		
		File outputPackage = project.getOutputPackage();
		if (outputPackage == null) {
			UIManager.get().showDialog(new Alert(AlertType.ERROR, "The specified output folder does not exist."));
			return false;
		}
		if (FileManager.get().isProtectedPackage(project.getOutputPackage())) {
			Alert alert = new Alert(AlertType.WARNING, "The package name \"" + project.getPackageName() + "\" is protected and should not be packed. "
					+ "Packing protected packages might cause irreversible changes to he game. Are you sure you want to proceed?", ButtonType.YES, ButtonType.CANCEL);
			
			if (UIManager.get().showDialog(alert).orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
				return false;
			}
		}
		
		return PackProgressUI.show(project, storeDebugInformation);
	}

	/**
	 * Returns a collection with all the projects, without any specific order.
	 * @return
	 */
	public Collection<Project> getProjects() {
		return projects.getAll();
	}
	
	/**
	 * Tells whether a project with this name exists.
	 * @param name
	 * @return
	 */
	public boolean hasProject(String name) {
		return projects.exists(name);
	}

	public boolean hasModBundle(String name) {
		return modBundles.exists(name);
	}
	
	/**
	 * Deletes this project, removing it from the list and deleting all its content.
	 * Be aware, this operation cannot be reverted.
	 * @param project
	 */
	public void deleteProject(Project project) throws IOException {
		FileManager.get().deleteDirectory(project.getFolder());
		
		projects.remove(project);
		projects.saveLastActiveTimesNoException();
		
		// Update the UI
		UIManager.get().notifyUIUpdate(false);
	}
	
	/**
	 * Does the necessary actions to include the given project into the program, including creating a blank folder
	 * for it. It will delete any previously existing contents in the folder.
	 * @param project
	 */
	public void initializeProject(Project project) throws IOException {
		if (project.getFolder().exists()) {
			// Ensure there isn't such folder
			FileManager.get().deleteDirectory(project.getFolder());
		}
		
		project.getFolder().mkdir();
		
		project.saveSettings();
		
		projects.add(project);
		
		// Update the UI
		UIManager.get().notifyUIUpdate(false);
	}

	/**
	 * Creates the root folders of a mod bundle and adds it to the list.
	 * Does not initialize the git repository.
	 * @param modBundle
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void initializeModBundle(ModBundle modBundle) throws IOException {
		// Ensure the folder does not exist yet
		if (modBundle.getFolder().exists()) {
			FileManager.get().deleteDirectory(modBundle.getFolder());
		}

		// Create root folder and 'src', 'data' folders
		if (!modBundle.getFolder().mkdir()) {
			throw new RuntimeException("Failed to create base folder for mod: " + modBundle.getFolder().getAbsolutePath());
		}
		if (!modBundle.getDataFolder().mkdir() || !modBundle.getSrcFolder().mkdir()) {
			modBundle.getFolder().delete();
			throw new RuntimeException("Failed to create 'data' and 'src' folders for mod: " + modBundle.getFolder().getAbsolutePath());
		}

		// Add .gitignore in the C++ 'src' folder
		File srcGitignore = new File(modBundle.getSrcFolder(), ".gitignore");
		try (InputStream inputStream = ProjectManager.class.getResourceAsStream("/sporemodder/resources/srcGitignore.txt")) {
            assert inputStream != null;
            Files.copy(inputStream, srcGitignore.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// Write a default readme file
		File readmeFile = new File(modBundle.getFolder(), "README.md");
		Files.writeString(readmeFile.toPath(), "# " + modBundle.getName() + "\nYou can download the mod in the Releases page.");

		// Add GitHub action to compile the mod
		File githubActionFolder = new File(modBundle.getFolder(), ".github/workflows");
		if (!githubActionFolder.mkdirs()) {
			throw new RuntimeException("Failed to create GitHub action folders for mod: " + modBundle.getFolder().getAbsolutePath());
		}
		try (InputStream inputStream = ProjectManager.class.getResourceAsStream("/sporemodder/resources/githubAction.yml")) {
			assert inputStream != null;
			Files.copy(inputStream, new File(githubActionFolder, "build.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// Add to mods list
		modBundles.add(modBundle);
	}

	/**
	 * Initialize git repository and commit initial files for a mod bundle
	 * @param modBundle
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void initializeModBundleGit(ModBundle modBundle) throws IOException, InterruptedException {
		GitCommands.gitInit(modBundle.getGitRepository());
		GitCommands.gitAddAll(modBundle.getGitRepository());
		GitCommands.gitCommit(modBundle.getGitRepository(), "Initial commit");
		GitCommands.gitSetMainBranch(modBundle.getGitRepository(), "main");
	}
	
	private ProjectTreeItem getItemRecursive(ProjectTreeItem node, String completeRelativePath, String relativePath, boolean forceLoad) {
		int indexOf = relativePath.indexOf(File.separatorChar);
		
		String name = indexOf == -1 ? relativePath : relativePath.substring(0, indexOf);
		ProjectTreeItem result = null;

		if (!node.isLoaded()) {
			if (forceLoad) {
				loadItemFolder(node);
				for (ProjectTreeItem child : node.getInternalChildren()) {
					if (name.equals(child.getValue().getName())) {
						result = child;
						break;
					}
				}
			}
			else {
				File file = getFile(node.getValue(), name);
				if (file == null) return null;
				ProjectItem item = createItem(file, node.getValue().getProject(), node);
				item.setIsMod(hasMod(completeRelativePath));
				item.setIsSource(hasSource(completeRelativePath));
				
				result = new ProjectTreeItem(item);
				// We don't care about the order, nodes are not loaded so this will get replaced when the user looks
				// We must add it so it has a parent though
				node.getInternalChildren().add(result);
			}
			
		} else {
			for (ProjectTreeItem child : node.getInternalChildren()) {
				if (name.equals(child.getValue().getName())) {
					result = child;
					break;
				}
			}
		}
		
		if (result != null && indexOf != -1) {
			// Still has children to look
			return getItemRecursive(result, completeRelativePath, relativePath.substring(indexOf + 1), forceLoad);
		}
		
		return result;
	}
	
	public ProjectItem getItem(String relativePath) {
		TreeItem<ProjectItem> item = getItemRecursive(rootItem, relativePath, relativePath, false);
		return item == null ? null : item.getValue();
	}
	
	public ProjectItem getLoadedItem(String relativePath) {
		TreeItem<ProjectItem> item = getItemRecursive(rootItem, relativePath, relativePath, true);
		return item == null ? null : item.getValue();
	}
	
	public String keyToRelativePath(ResourceKey key) {
		return key.toString().replace('!', File.separatorChar);
	}
	
	/**
	 * Returns the item with the specified name that has the same parent as the given item.
	 * This refreshes the parent to ensure the sibling item has been loaded.
	 * @param item
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public ProjectItem getSiblingItem(ProjectItem item, String name) throws Exception {
		TreeItem<ProjectItem> parent = item.getTreeItem().getParent();
		parent.getValue().refreshItem();
		return ProjectManager.get().getItem(parent.getValue().getRelativePath() + File.separatorChar + name);
	}
	
	public ResourceKey getResourceKey(ProjectItem item) {
		String[] splits = item.getName().split("\\.", 2);
		
		ResourceKey key = new ResourceKey();
		key.setInstanceID(splits[0]);
		key.setTypeID(splits.length > 1 ? splits[1] : "");
		
		TreeItem<ProjectItem> parentItem = item.getTreeItem().getParent();
		if (parentItem != null && !parentItem.getValue().isRoot()) {
			key.setGroupID(parentItem.getValue().getName());
		}
		
		return key;
	}
	
	/**
	 * Returns whether this relative path exists within the modified files of the active project. This is only true if the project folder
	 * contains the file.
	 * @param relativePath
	 * @return
	 */
	public boolean hasMod(String relativePath) {
		// This method is faster than getting the item and then checking
		return new File(activeProject.getFolder(), relativePath).exists();
	}
	
	/**
	 * Returns whether this relative path exists within the source files of the active project. This is only true if any of the source projects
	 * contain the file.
	 * @param relativePath
	 * @return
	 */
	public boolean hasSource(String relativePath) {
		// This method is faster than getting the item and then checking
		for (Project source : activeProject.getReferences()) {
			if (new File(source.getFolder(), relativePath).exists()) return true;
		}
		return false;
	}
	
	public File getModFile(ProjectItem item) {
		return item.isMod() ? item.getFile() : null;
	}
	
 	public File getModFile(String relativePath) {
		File file = new File(activeProject.getFolder(), relativePath);
		if (file.exists()) return file;
		else return null;
	}
 	
 	// does not require the file to exist
 	public File getDestinationModFile(String relativePath) {
		return new File(activeProject.getFolder(), relativePath);
	}
 	
 	public File getSourceFile(String relativePath) {
 		for (Project source : activeProject.getReferences()) {
 			File file = new File(source.getFolder(), relativePath);
 			if (file.exists()) return file;
 		}
 		return null;
	}
 	
 	public Project getProjectByFile(String relativePath) {
 		if (getModFile(relativePath) != null) return activeProject;
 		for (Project source : activeProject.getReferences()) {
 			if (new File(source.getFolder(), relativePath).exists()) return source;
 		}
 		return null;
	}
 	
 	/**
 	 * Returns the file that is in the given relative path to the project or any of its sources.
 	 * If no file exists in that path returns null.
 	 * @param relativePath
 	 * @return
 	 */
 	public File getFile(String relativePath) {
 		File file = new File(activeProject.getFolder(), relativePath);
		if (file.exists()) return file;

 		for (Project source : activeProject.getReferences()) {
 			file = new File(source.getFolder(), relativePath);
 			if (file.exists()) return file;
 		}
 		return null;
	}
 	
 	public File getFile(ProjectItem directory, String name) {
 		String relativePath = directory.getRelativePath() + File.separatorChar + name;
 		
 		return getFile(relativePath);
	}
	
	public void unpackPresets(List<ProjectPreset> presets, List<Converter> converters) {
		
		if (converters == null) {
			converters = new ArrayList<Converter>();
			for (Converter c : FormatManager.get().getConverters()) {
				if (c.isEnabledByDefault()) converters.add(c);
			}
		}
		
		// We will use this to tell the user which files could not be unpacked (because they didn't exist)
		List<String> failedPackages = new ArrayList<String>();
		
		for (int progressIndex = 0; progressIndex < presets.size(); progressIndex++) {
			ProjectPreset preset = presets.get(progressIndex);
			
			Map<String, File> files = new LinkedHashMap<String, File>();
			preset.getFiles(files);
			
			Map<File, String> fileToName = new HashMap<File, String>();
			for (Map.Entry<String, File> entry : files.entrySet()) {
				fileToName.put(entry.getValue(), entry.getKey());
			}
			
			// Create the project or override the existing one
			final Project project = getOrCreateProject(preset.getName());
			project.setReadOnly(true);
			// We don't need to save the settings here, as the unpacking task will call initializeProject()
			// project.saveSettings();
			
			// The project is passed to set the 'packageSignature' setting, but we don't want that in presets
			final DBPFUnpackingTask task = new DBPFUnpackingTask(files.values(), project.getFolder(), project, converters);
			
			task.setItemFilter(preset.getItemFilter());
			
			ProgressDialogUI progressUI = UIManager.get().loadUI("dialogs/ProgressDialogUI");
			Dialog<ButtonType> progressDialog = progressUI.createDialog(task);
			progressDialog.setTitle("Unpacking preset \"" + preset.getName() + "\" (" + (progressIndex+1) + " of " + presets.size() + ")");
			
			// Show progress
			progressUI.getProgressBar().progressProperty().bind(task.progressProperty());
			progressUI.getLabel().textProperty().bind(task.messageProperty());
			
			progressUI.setOnFailed(() -> {
				UIManager.get().showErrorDialog(task.getException(), "Fatal error, file could not be unpacked", true);
				
				for (String str : fileToName.values()) {
					failedPackages.add(str);
				}
			});
			
			UIManager.get().showDialog(progressDialog);
			
			if (task.isCancelled() || task.getState() == State.FAILED) {
				// The task was cancelled, don't continue unpacking presets
				return;
			} else {
				List<File> fails = task.getFailedDBPFs();
				for (File fail : fails) {
					failedPackages.add(fileToName.get(fail));
				}
			}
		}
		
		VBox resultPane = new VBox(5);
		resultPane.setPrefWidth(600);
		resultPane.getStyleClass().add("dialog-content");
		
		Label infoLabel = new Label();
		infoLabel.setWrapText(true);
		resultPane.getChildren().add(infoLabel);
		
		if (failedPackages.isEmpty()) {
			infoLabel.setText("Finished! All presets were unpacked successfully. Below you can check a list of all the packages they include:");
		} else {
			infoLabel.setText("Finished! Some packages could not be unpacked; below there is a list with the packages the presets contains, the ones in red were missing:");
		}
		
		boolean openedByDefault = true;
		for (ProjectPreset preset : presets) {
			VBox pane = new VBox(10);
			
			for (String name : preset.getProjectNames()) {
				Label lbl = new Label(name);
				pane.getChildren().add(lbl);
				
				if (failedPackages.contains(name)) {
					lbl.setStyle("-fx-background-color: red;");
				}
			}
			
			TitledPane presetPane = new TitledPane(preset.getName(), pane);
			resultPane.getChildren().add(presetPane);
			
			if (openedByDefault) {
				presetPane.setExpanded(true);
				openedByDefault = false;
			}
		}
		
		Dialog<ButtonType> resultDialog = new Dialog<ButtonType>();
		resultDialog.getDialogPane().setContent(resultPane);
		resultDialog.getDialogPane().getButtonTypes().setAll(ButtonType.FINISH);
		resultDialog.setTitle("Unpacking task finished");
		
		UIManager.get().showDialog(resultDialog);
	}
	
	/**
	 * Executes WinMerge to compare the source and mod version of a given item. The method returns true if the program was executed successfully,
	 * and false if item does not have a source or a mod version (so there's nothing to compare). Throws an exception if the program does not exist.
	 * @param item
	 * @return
	 * @throws IOException
	 */
	public boolean compareItem(ProjectItem item) throws IOException {
		if (!item.canCompareItem()) return false;
		
		String relativePath = item.getRelativePath();
		File sourceFile = getSourceFile(relativePath);
		File modFile = getModFile(relativePath);
		
		Runtime.getRuntime().exec(new String[] {
				PathManager.get().getProgramFile("WinMerge" + File.separatorChar + "WinMergeU.exe").getAbsolutePath(),
				sourceFile.getAbsolutePath(),
				modFile.getAbsolutePath()});
		
		return true;
	}
	
	public boolean openSourceFolder(ProjectItem item) throws IOException {
		if (item.canOpenSourceFolder() && Desktop.isDesktopSupported()) {
			String filePath = item.getRelativePath();
			File file = getSourceFile(filePath);
			
			if (file.isDirectory()) {
				Desktop.getDesktop().open(file);
			}
			else {
				Runtime.getRuntime().exec("explorer.exe /select, \"" + file.getAbsolutePath() + "\"");
			}
			return true;
		}
		return false;
	}
	
	public boolean openModFolder(ProjectItem item) throws IOException {
		if (item.canOpenModFolder() && Desktop.isDesktopSupported()) {
			String filePath = item.getRelativePath();
			File file = getModFile(filePath);
			
			if (file.isDirectory()) {
				Desktop.getDesktop().open(file);
			}
			else {
				Runtime.getRuntime().exec("explorer.exe /select, \"" + file.getAbsolutePath() + "\"");
			}
			return true;
		}
		return false;
	}
	
	private void copy(File sourceLocation, File targetLocation) throws IOException {
	    if (sourceLocation.isDirectory()) {
	        copyDirectory(sourceLocation, targetLocation);
	    } else {
	        Files.copy(sourceLocation.toPath(), targetLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    }
	}

	private void copyDirectory(File source, File target) throws IOException {
	    if (!target.exists()) {
	        target.mkdir();
	    }

	    for (String f : source.list()) {
	        copy(new File(source, f), new File(target, f));
	    }
	}
	
	/**
	 * Updates all the parents of this item, including the item itself, to be marked as (not) part of the mod.
	 * If isMod=false, each parent will check all its children to ensure none of them is mod. If that is the case, it's file (folder)
	 * in the mod project will be deleted.
	 * @param treeItem
	 * @param file
	 */
	private void setParentsAsMod(ProjectTreeItem treeItem, File file, boolean isMod) {
		while (treeItem != null && !treeItem.getValue().isRoot()) {
			
			ProjectItem item = treeItem.getValue();
			
			// If this parent is a mod (and so are their parents) and that's what we want, stop
			if (isMod && item.isMod()) return;
			
			if (!isMod) {
				boolean found = false;
				for (ProjectTreeItem child : treeItem.getInternalChildren()) {
					if (child.getValue().isMod()) {
						found = true;
						break;
					}
				}
				if (found) {
					// This parent will continue being mod, and so will its parents as well, so stop here
					return;
				} else {
					// Remove the file
					item.getFile().delete();
					// Now make it use the source file; do not change 'file' because that is for deleting empty mod files
					item.setFile(getSourceFile(item.getRelativePath()));
				}
			}
			else {
				item.setFile(file);
			}
			
			item.setIsMod(isMod);
			
			treeItem = (ProjectTreeItem) treeItem.getParent();
			file = file.getParentFile();
		}
	}
	
	/**
	 * If the children of this item are loaded, it updates them so they are marked as (not) part of the mod.
	 * This method is called on all the loaded children hierarchy. If the items are marked as not mod, they will get
	 * removed if they are not sources neither.
	 * @param treeItem
	 * @param file
	 */
	private void setChildrenAsMod(ProjectTreeItem treeItem, File file, boolean isMod) {
		disableTreeEvents = true;
		
		if (treeItem.isLoaded()) {
			
			List<ProjectTreeItem> newChildren = new ArrayList<ProjectTreeItem>();
			for (ProjectTreeItem child : treeItem.getInternalChildren()) {
				ProjectItem item = child.getValue();
				
				if (item.isMod() != isMod) {
					item.setIsMod(isMod);
					
					if (!isMod) {
						if (item.isSource()) {
							// Still a source, just update the file
							item.setFile(this.getFile(treeItem.getValue(), item.getFile().getName()));
							newChildren.add(child);
						} else {
							// Do not add the item; we can stop the recursivity here because we will just remove all its children as well
							EditorManager.get().removeEditor(item.getRelativePath());
							continue; 
						}
					} else {
						newChildren.add(child);
					}
					
					setChildrenAsMod(child, item.getFile(), isMod);
				} else {
					newChildren.add(child);
				}
			}
			
			treeItem.getInternalChildren().setAll(newChildren);
		}
		
		disableTreeEvents = false;
	}
	
	public boolean modifyItem(ProjectItem item) throws IOException {
		if (!item.canModifyItem()) return false;
		
		String filePath = item.getRelativePath();
		File sourceFile = getSourceFile(filePath);
		// getModFile would return null because the file does not exist, so we do it manually
		File modFile = new File(item.getProject().getFolder(), filePath);
		
		// Create any parent directories as needed
		if (!modFile.getParentFile().exists()) {
			modFile.getParentFile().mkdirs();
		}
		
		copy(sourceFile, modFile);
		
		
		// Update this node, its parents and its children so they are mods now
		setParentsAsMod(item.getTreeItem(), modFile, true);
		setChildrenAsMod(item.getTreeItem(), modFile, true);
		
		// Ensure the editor is pointing to the right file, and save any changes
		ItemEditor editor = EditorManager.get().getEditor(filePath);
		if (editor != null) {
			editor.setDestinationFile(modFile);
			editor.save();
		}
		
		// Repaint tree
		UIManager.get().getUserInterface().getProjectTree().getTreeView().refresh();
		
		UIManager.get().notifyUIUpdate(false);
		
		return true;
	}
	
	private void removeEmptyNonSourceParents(ProjectTreeItem parent) {
		if (parent != null && parent.getParent() != null && !parent.getValue().isSource() && parent.getInternalChildren().isEmpty()) {
			
			// Get it before removing the item
			ProjectTreeItem nextParent = (ProjectTreeItem) parent.getParent();
			
			nextParent.getInternalChildren().remove(parent);
			removeEmptyNonSourceParents(nextParent);
		}
	}
	
	private boolean confirmationDialogForRemoveItem() {
		if (!dontAskAgain_removeItem) {
			Dialog<ButtonType> dialog = new Dialog<ButtonType>();
			dialog.setTitle("Confirm action");
			
			CheckBox cb = new CheckBox("Don't ask me again.");
			
			dialog.getDialogPane().setHeaderText("Are you sure you want to remove this file?");
			dialog.getDialogPane().setContent(cb);
			
			dialog.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
			
			ButtonType result = UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL);
			if (result == ButtonType.YES) {
				dontAskAgain_removeItem = cb.isSelected();
				MainApp.get().saveSettings();
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	
	public boolean removeItem(ProjectItem item) throws IOException {
		if (!item.canRemoveItem()) return false;
		
		String filePath = item.getRelativePath();
		File file = getModFile(filePath);
		
		if (!confirmationDialogForRemoveItem()) return false;
		
		if (file.isFile()) file.delete();
		else FileManager.get().deleteDirectory(file);
		
		
		// If the item is not a source, it will get deleted, delete all the non-source empty parents as well
		if (!item.isSource()) {
			ProjectTreeItem parentItem = (ProjectTreeItem) item.getTreeItem().getParent();
			parentItem.getInternalChildren().remove(item.getTreeItem());
			
			removeEmptyNonSourceParents(parentItem);
			setParentsAsMod(parentItem, file.getParentFile(), false);
			
		} else {
			// The item is still a source, so now just update the file
			file = getSourceFile(filePath);
			// We will set this file manually and call the recursive method for the parent
			// Because it will check for mod children and the item is still a mod
			item.setFile(file);
			item.setIsMod(false);
			setParentsAsMod((ProjectTreeItem) item.getTreeItem().getParent(), file.getParentFile(), false);
			setChildrenAsMod(item.getTreeItem(), file, false);
			
			EditorManager.get().reloadEditor(filePath);
		}
		
		ProjectTreeItem parentItem = (ProjectTreeItem) item.getTreeItem().getParent();
		if (parentItem != null) parentItem.invalidatePredicate();
		
		// Repaint tree
		UIManager.get().getUserInterface().getProjectTree().getTreeView().refresh();
		
		UIManager.get().notifyUIUpdate(false);
		
		return true;
	}
	
	public boolean createNewFile(ProjectItem requestedItem) {
		if (!requestedItem.canCreateNewFile()) return false;
		
		// If the user clicked on a file, create the new file in the folder that contains it
		ProjectTreeItem directory = requestedItem.isFolder() ? requestedItem.getTreeItem() : (ProjectTreeItem) requestedItem.getTreeItem().getParent();
		
		// We don't use a factory here, because by default the user can only create standard items
		ProjectItem newItem = new ProjectItem("item_name.prop.prop_t", activeProject);
		newItem.setIsMod(true);
		newItem.setTreeItem(new ProjectTreeItem(newItem));
		
		// + 1 because shifting the focused item causes cancelEdit
		int index = requestedItem.isFolder() ? 0 : ((ProjectTreeItem) requestedItem.getTreeItem().getParent()).getInternalChildren().indexOf(requestedItem.getTreeItem()) + 1;
		
		// Before creating the item, ensure its children are visible
		directory.setExpanded(true);
		
		directory.getInternalChildren().add(index, newItem.getTreeItem());
		
		// Layout tree, otherwise we won't be able to edit the tree cell, a bug apparently https://bugs.openjdk.java.net/browse/JDK-8089497
		treeUI.getTreeView().layout();
		
		// Tell we are editing a new file
		toDuplicateItem = null;
		itemEditType =  ItemEditType.NEW_FILE;
		treeUI.getTreeView().edit(newItem.getTreeItem());
		
		return true;
	}
	
	public boolean createNewFolder(ProjectItem requestedItem) {
		if (!requestedItem.canCreateNewFolder()) return false;
		
		// If the user clicked on a file, create the new file in the folder that contains it
		ProjectTreeItem directory = requestedItem.isFolder() ? requestedItem.getTreeItem() : (ProjectTreeItem) requestedItem.getTreeItem().getParent();
		
		// We don't use a factory here, because by default the user can only create standard items
		ProjectItem newItem = new ProjectItem("folder_name", activeProject);
		newItem.setIsMod(true);
		newItem.setTreeItem(new ProjectTreeItem(newItem));
		
		// + 1 because shifting the focused item causes cancelEdit
		int index = requestedItem.isFolder() ? 0 : ((ProjectTreeItem) requestedItem.getTreeItem().getParent()).getInternalChildren().indexOf(requestedItem.getTreeItem()) + 1;
		
		// Before creating the item, ensure its children are visible
		directory.setExpanded(true);
		
		directory.getInternalChildren().add(index, newItem.getTreeItem());
		
		// Layout tree, otherwise we won't be able to edit the tree cell, a bug apparently https://bugs.openjdk.java.net/browse/JDK-8089497
		treeUI.getTreeView().layout();
		
		// Tell we are editing a new file
		toDuplicateItem = null;
		itemEditType =  ItemEditType.NEW_FOLDER;
		treeUI.getTreeView().edit(newItem.getTreeItem());
		
		return true;
	}
	
	public boolean renameItem(ProjectItem requestedItem) {
		if (!requestedItem.canRenameItem()) return false;
		
		// Tell we are editing a new file
		toDuplicateItem = null;
		itemEditType =  ItemEditType.RENAME;
		treeUI.getTreeView().edit(requestedItem.getTreeItem());
		
		return true;
	}
	
	private String getDuplicatedItemName(String name) {
		int indexOf = name.indexOf(".");
		if (indexOf == -1) return name + "_COPY";
		else return name.substring(0, indexOf) +  "_COPY" + name.substring(indexOf);
	}
	
	public boolean duplicateItem(ProjectItem item) {
		if (!item.canDuplicateItem()) return false;
		
		// Create the new file in the folder that contains it
		ProjectTreeItem directory = (ProjectTreeItem) item.getTreeItem().getParent();
		
		// We don't use a factory here, because by default the user can only create standard items
		ProjectItem newItem = new ProjectItem(getDuplicatedItemName(item.getName()), activeProject);
		
		newItem.setIsMod(true);
		newItem.setTreeItem(new ProjectTreeItem(newItem));
		
		// + 1 because shifting the focused item causes cancelEdit
		int index = item.isFolder() ? 0 : ((ProjectTreeItem) item.getTreeItem().getParent()).getInternalChildren().indexOf(item.getTreeItem()) + 1;
		
		// Before creating the item, ensure its children are visible
		directory.setExpanded(true);
		
		directory.getInternalChildren().add(index, newItem.getTreeItem());
		
		// Layout tree, otherwise we won't be able to edit the tree cell, a bug apparently https://bugs.openjdk.java.net/browse/JDK-8089497
		treeUI.getTreeView().layout();
		
		//TODO
		// Tell we are editing a new file
		toDuplicateItem = item;
		itemEditType =  ItemEditType.NEW_FILE;
		treeUI.getTreeView().edit(newItem.getTreeItem());
		
		return true;
	}
	
	/**
	 * Ensures that the given directory item is included in the mod, so new files can be added to it.
	 * If the item is not in the mod, it will be included without including any children nodes, and any necessary folders
	 * will be created in the mod project.
	 * Returns the File location of the folder.
	 * @param parentItem
	 * @return
	 */
	private File ensureModFolder(ProjectItem parentItem) {
		if (parentItem.isRoot()) {
			return parentItem.getProject().getFolder();
		}
		
		File parentFile;
		// Ensure the directory is included in the mod
		if (!parentItem.isMod()) {
			parentFile = new File(parentItem.getProject().getFolder(), parentItem.getRelativePath());
			parentFile.mkdirs();
			setParentsAsMod(parentItem.getTreeItem(), parentFile, true);
		} else {
			parentFile = parentItem.getFile();
		}
		return parentFile;
	}
	
	/**
	 * Correctly adds this item as a mod item. The temporary item must already be a children of the desired parent.
	 * This method searches for other children with the same name as the temporary item; if an item with the same name is found,
	 * the method returns false if the item is already part of the mod, or replaces it with the new item if it is not
	 * This does not update any editors.
	 * @param temporaryItem
	 * @return True if the item can be used afterwards, false if another mod item with the same nam was found.
	 */
	private boolean addModItem(ProjectItem temporaryItem) {
		ProjectTreeItem treeItem = temporaryItem.getTreeItem();
		ProjectTreeItem parent = ((ProjectTreeItem) treeItem.getParent());
		String name = temporaryItem.getName();
		
		ProjectTreeItem oldSource = null;
		for (ProjectTreeItem child : parent.getInternalChildren()) {
			if (child != treeItem && name.equals(child.getValue().getName())) {
				if (child.getValue().isMod()) return false;
				else if (child.getValue().isSource()) {
					oldSource = child;
					break;
				}
			}
		}
		if (oldSource != null) {
			temporaryItem.setIsSource(true);
			
			disableTreeEvents = true;
			
			// Use the old children, remove the old source item
			treeItem.getInternalChildren().setAll(oldSource.getInternalChildren());
			parent.getInternalChildren().remove(oldSource);
			
			disableTreeEvents = false;
			// We don't need to adapt any editors because we will select the item now anyways
		}
		
		return true;
	}
	
	/**
	 * Rearranges the children items of the given TreeItem so they are alphabetically ordered,
	 * ignoring all generated events.
	 * @param item
	 */
	private void reorderChildren(ProjectTreeItem item) {
		// Now ensure the children are still ordered. Disable tree events so the active editor does not change
		disableTreeEvents = true;
		item.getInternalChildren().setAll(createChildrenMap(item).values());
		disableTreeEvents = false;
	}
	
	private void copyChildren(File destFolder, ProjectTreeItem sourceItem) throws IOException {
		for (ProjectTreeItem child : sourceItem.getInternalChildren()) {
			// Mod files have already been copied with renameTo
			if (!child.getValue().isMod()) {
				File sourceFile = child.getValue().getFile();
				copy(sourceFile, new File(destFolder, sourceFile.getName()));
			}
		}
	}
	
	private void importFile(File sourceFile, File destFolder) throws IOException {
		String fileName = sourceFile.getName();
		
		if (fileName.endsWith(".prop.xml")) {
			String name = fileName.substring(0, fileName.length() - ".prop.xml".length()) + ".prop.prop_t";
			File dest = new File(destFolder, name);
			try (InputStream in = new FileInputStream(sourceFile);
					StreamWriter out = new FileStream(dest, "rw")) {
				
				MemoryStream stream = XmlPropParser.xmlToProp(in);
				stream.seek(0);
				//stream.writeToFile(new File(destFolder, name + ".prop"));
				
				PropertyList list = new PropertyList();
				list.read(stream);
				list.toArgScript().write(dest);
				
				stream.close();
			} catch (ParserConfigurationException | SAXException e) {
				throw new IOException(e);
			}
		}
		else {
			Files.copy(sourceFile.toPath(), new File(destFolder, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	public boolean importFiles(ProjectItem item) throws IOException {
		if (!item.canImportFiles()) return false;
		
		UIManager.get().setOverlay(true);
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
		List<File> result = chooser.showOpenMultipleDialog(UIManager.get().getScene().getWindow());
		UIManager.get().setOverlay(false);
		
		HashManager.get().setUpdateProjectRegistry(true);
		
		if (result != null && !result.isEmpty()) {
			// If the user clicked on a file, import the files in the folder that contains it
			ProjectTreeItem treeItem = item.isFolder() ? item.getTreeItem() : (ProjectTreeItem)item.getTreeItem().getParent();
			item = treeItem.getValue();
			
			if (!item.isMod()) {
				ensureModFolder(item);
				// We have selected a folder, which have no editor, no point on doing this
				// EditorManager.get().reloadEditor(filePath);
			}
			
			File destFolder = item.getFile();
			for (File file : result) {
				importFile(file, destFolder);
			}
			
			// The easiest way to rearrange the mod/source status is just reloading the nodes
			treeItem.requestReload();
			// If it is expanded we need to ensure its children are reloaded
			if (treeItem.isExpanded()) {
				treeItem.setExpanded(false);
				treeItem.setExpanded(true);
			}
			
			// Repaint tree
			UIManager.get().getUserInterface().getProjectTree().getTreeView().refresh();
			
			UIManager.get().notifyUIUpdate(false);
		}
		
		HashManager.get().setUpdateProjectRegistry(false);
		
		return true;
	}
	
	public boolean refreshItem(ProjectItem item) throws IOException {
		if (!item.canRefreshItem()) return false;
		
		ProjectTreeItem treeItem = item.getTreeItem();
		
		// Reload the file and the editor that has it opened
		String relativePath = item.getRelativePath();
		
		File file = getSourceFile(relativePath);
		item.setIsSource(file != null);
		
		file = getModFile(relativePath);
		item.setIsMod(file != null);
			
		file = getFile(relativePath);
		
		if (file == null) {
			// The file does not exist anymore, so delete the item and remove its editor
			disableTreeEvents = true;
			((ProjectTreeItem) treeItem.getParent()).getInternalChildren().remove(treeItem);
			disableTreeEvents = false;
			EditorManager.get().removeEditor(relativePath);
			return true;
		}
		
		item.setFile(file);
		
		// Reloading the nodes
		((ProjectTreeItem) treeItem).requestReload();
		
		if (!searchedWords.isEmpty()) {
			// Search the new nodes
			projectSearcher.startSearch(treeItem);
		}
		
		// If it is expanded we need to ensure its children are reloaded
		if (treeItem.isExpanded()) {
			treeItem.setExpanded(false);
			treeItem.setExpanded(true);
		}
		
		// Repaint tree
		UIManager.get().getUserInterface().getProjectTree().getTreeView().refresh();
		
		UIManager.get().notifyUIUpdate(false);
		
		return true;
	}
	
	public boolean importOldProject() {
		UIManager.get().setOverlay(true);
		
		DirectoryChooser chooser = new DirectoryChooser();
		File sourceFolder = chooser.showDialog(UIManager.get().getScene().getWindow());
		
		if (sourceFolder == null) {
			UIManager.get().setOverlay(false);
			return false;
		}
		
		if (!new File(sourceFolder, "config.properties").exists()) {
			Alert alert = new Alert(AlertType.WARNING, "The selected folder \"" + sourceFolder + "\" does not look like a project. Are you sure you want to continue?", ButtonType.YES, ButtonType.CANCEL);
			if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
				UIManager.get().setOverlay(false);
				return false;
			}
		}
		
		String projectName = sourceFolder.getName();
		
		if (hasProject(projectName)) {
			Alert alert = new Alert(AlertType.WARNING, "A project with this name already exists. Do you want to replace all its contents?", ButtonType.YES, ButtonType.CANCEL);
			if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
				UIManager.get().setOverlay(false);
				return false;
			}
		}
		
		Project project = getOrCreateProject(projectName);
		if (!UIManager.get().tryAction(() -> initializeProject(project),
				"Cannot initialize project. Try manually deleting the project folder in SporeModder FX\\Projects\\")) {
			UIManager.get().setOverlay(false);
			return false;
		}
		
		HashManager hasher = HashManager.get();
		hasher.setUpdateProjectRegistry(true);
		hasher.getProjectRegistry().clear();
		hasher.setExtraRegistry(new NameRegistry(hasher, null, null));
		
		// Use the old registries to load files, if possible
		NameRegistry fileRegistry = null;
		NameRegistry propRegistry = null;
		NameRegistry typeRegistry = null;
		
		try {
			File registryFile = new File(sourceFolder.getParentFile(), "reg_file.txt");
			if (registryFile.exists()) {
				fileRegistry = new NameRegistry(hasher, "Old SporeModder File Names", registryFile.getName());
				fileRegistry.read(registryFile);
			}
			registryFile = new File(sourceFolder.getParentFile(), "reg_properties.txt");
			if (registryFile.exists()) {
				propRegistry = new NameRegistry(hasher, "Old SporeModder Properties", registryFile.getName());
				propRegistry.read(registryFile);
			}
			registryFile = new File(sourceFolder.getParentFile(), "reg_type.txt");
			if (registryFile.exists()) {
				typeRegistry = new NameRegistry(hasher, "Old SporeModder Types", registryFile.getName());
				typeRegistry.read(registryFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ImportProjectTask task = new ImportProjectTask(project, sourceFolder, fileRegistry, propRegistry, typeRegistry);
		task.showProgressDialog();
		
		hasher.replaceRegistries(null, null, null);
		hasher.setUpdateProjectRegistry(false);
		saveNamesRegistry(project);
		hasher.getProjectRegistry().clear();
		hasher.setExtraRegistry(null);
		
		UIManager.get().setOverlay(false);
		
		setActive(project);
		
		return true;
	}
	
	public boolean addExternalProject() {
		UIManager.get().setOverlay(true);
		
		DirectoryChooser chooser = new DirectoryChooser();
		File sourceFolder = chooser.showDialog(UIManager.get().getScene().getWindow());
		
		if (sourceFolder == null) {
			UIManager.get().setOverlay(false);
			return false;
		}
		
		boolean loadedCorrectly = true;

		final Project project = new Project(sourceFolder.getName(), sourceFolder, null);
		project.loadSettings();
		if (ProjectSettingsUI.show(project, false)) {
			File linkFile = new File(PathManager.get().getProjectsFolder(), project.getName());
			
			if (!UIManager.get().tryAction(() -> {
				Files.write(linkFile.toPath(), Collections.singletonList(sourceFolder.getAbsolutePath()), StandardOpenOption.CREATE_NEW);
				project.setExternalLinkFile(linkFile);
				project.saveSettings();
				
				projects.add(project);
			}, "Error adding external project")) {
				loadedCorrectly = false;
			}
		}
		else {
			loadedCorrectly = false;
		}
		
		UIManager.get().setOverlay(false);
		
		if (loadedCorrectly) {
			setActive(project);
			
			// Update the UI
			UIManager.get().notifyUIUpdate(false);
		}
		
		return true;
	}
	
	public void showFirstTimeDialog() {
		UnpackPresetsUI.showAsOptional("In order to start modding Spore, you'll need to unpack the game files. You can choose from the presets below. We have already checked the "
				+ "'Spore (Game & Graphics)' because it is what you will need for most of the things you want to modify.", false);
	}
	
	public boolean showSaveAsModDialog() {
		if (!dontAskAgain_saveAsMod) {
			Dialog<ButtonType> dialog = new Dialog<ButtonType>();
			dialog.setTitle("Confirm action");
			
			CheckBox cb = new CheckBox("Don't ask me again.");
			
			dialog.getDialogPane().setHeaderText("This file has been edited. Do you want to include it into your mod? If you press no, all changes will be discarded.");
			dialog.getDialogPane().setContent(cb);
			
			dialog.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
			
			ButtonType result = UIManager.get().showDialog(dialog).orElse(ButtonType.NO);
			if (result == ButtonType.YES) {
				dontAskAgain_saveAsMod = cb.isSelected();
				MainApp.get().saveSettings();
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public boolean showSaveDialog() {
		if (closeEditedFileDecision == CloseEditedFileDecision.ASK) {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Confirm action");

			CheckBox cb = new CheckBox("Remember my decision.");

			dialog.getDialogPane().setHeaderText("This file has unsaved changes. Do you want to save it? If you press no, all changes will be discarded.");
			dialog.getDialogPane().setContent(cb);

			dialog.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			ButtonType result = UIManager.get().showDialog(dialog).orElse(ButtonType.NO);
			if (result == ButtonType.YES) {
				closeEditedFileDecision = CloseEditedFileDecision.SAVE;
				MainApp.get().saveSettings();
				return true;
			} else {
				closeEditedFileDecision = CloseEditedFileDecision.IGNORE;
				MainApp.get().saveSettings();
				return false;
			}
		} else {
			return closeEditedFileDecision == CloseEditedFileDecision.SAVE;
		}
	}

	private void createNewProjectCommon(ModBundle modBundle, String projectName, List<ProjectPreset> presets) throws ParserConfigurationException, TransformerException, IOException {
		// Set default presets if not specified
		if (presets == null) {
			presets = this.presets.stream().filter(ProjectPreset::isRecommendable).collect(Collectors.toList());
		}

		// Create package project
		Project project = new Project(projectName, modBundle);
		modBundle.addProject(project);

		// Add project references
		project.getReferences().addAll(presets.stream().map(preset -> getProject(preset.getName())).collect(Collectors.toList()));

		// Initialize package project folder
		initializeProject(project);

		// Save ModInfo
		if (!modBundle.hasCustomModInfo()) {
			modBundle.saveModInfo();
		}
	}

	public void createNewMod(String modName, String uniqueTag, String description, String projectName, List<ProjectPreset> presets) throws IOException, ParserConfigurationException, TransformerException, InterruptedException {
		ModBundle modBundle = new ModBundle(modName);
		modBundle.setDescription(description);
		modBundle.setUniqueTag(uniqueTag);
		initializeModBundle(modBundle);
		createNewProjectCommon(modBundle, projectName, presets);

		// Initialize git repository, but don't try if git is not installed
		if (GitHubManager.get().hasGitInstalled()) {
			initializeModBundleGit(modBundle);
		}
	}

	public void createNewProjectInMod(ModBundle modBundle, String projectName, List<ProjectPreset> presets) throws ParserConfigurationException, IOException, TransformerException {
		assert modBundle != null;
		createNewProjectCommon(modBundle, projectName, presets);
	}

	public void removeInexistantMods() {
		modBundles.removeInexistantMods();
	}
}
