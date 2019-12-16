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
package sporemodder.view.editors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.javafx.ribbon.Ribbon;
import emord.javafx.ribbon.RibbonButton;
import emord.javafx.ribbon.RibbonGallery;
import emord.javafx.ribbon.RibbonGallery.GalleryItemDisplay;
import emord.javafx.ribbon.RibbonGalleryItem;
import emord.javafx.ribbon.RibbonGroup;
import emord.javafx.ribbon.RibbonTab;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.spui.InspectableObject;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.SpuiViewer;
import sporemodder.file.spui.SpuiWriter;
import sporemodder.file.spui.components.DirectImage;
import sporemodder.file.spui.components.IDrawable;
import sporemodder.file.spui.components.ISporeImage;
import sporemodder.file.spui.components.IWindow;
import sporemodder.file.spui.components.WindowBase;
import sporemodder.file.spui.uidesigner.DesignerClass;
import sporemodder.file.spui.uidesigner.DesignerProperty;
import sporemodder.file.spui.uidesigner.InspectorRectangle;
import sporemodder.util.ProjectItem;
import sporemodder.view.FilterableTreeItem;
import sporemodder.view.FilterableTreeItem.TreeItemPredicate;
import sporemodder.view.UserInterface;
import sporemodder.view.editors.spui.SpuiDraggableType;
import sporemodder.view.editors.spui.SpuiEditorSkin;
import sporemodder.view.editors.spui.SpuiImageChooser;
import sporemodder.view.editors.spui.SpuiImageFileChooser;
import sporemodder.view.editors.spui.SpuiObjectCreatedAction;
import sporemodder.view.editors.spui.SpuiPropertyAction;
import sporemodder.view.editors.spui.SpuiUndoableAction;
import sporemodder.view.editors.spui.SpuiWindowItemCell;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.InspectorValueList;

public class SpuiEditor extends AbstractEditableEditor implements EditHistoryEditor {

	public static final String DEFAULT_STYLE_CLASS = "spui-editor";

	private static final Color ACTIVE_COLOR = Color.rgb(0x70, 0xE0, 0xAA);

	private static final int POINT_SIZE = 6;

	private static final double TAB_PANE_HEIGHT = 300;

	/** The minimum time, in ms, that a component should wait before registering a new undoable edit action. */ 
	public static final long MINIMUM_ACTION_TIME = 800;

	/** The maximum amount of remembered edit history actions. */
	private static final int MAX_EDIT_HISTORY = 25;

	// EditActionsUI does not set the accelerators automatically, so we have to add/remove them
	private static final KeyCodeCombination CTRL_Z = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
	private static final KeyCodeCombination CTRL_Y = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);

	private static enum WindowDropType {ABOVE, INSIDE, BELOW};

	private static final SpuiUndoableAction ORIGINAL_ACTION = new SpuiUndoableAction() {

		@Override public void undo() {}

		@Override public void redo() {}

		@Override public String getText() {
			return "Original";
		}
	};


	private SporeUserInterface spui;
	private final SpuiViewer viewer = new SpuiViewer(this);

	private VBox inspectorPane;

	/** A canvas that is drawn on top of the viewer, to display selected items and things like that. */
	private Canvas overlayCanvas;

	private TextField tfSearch;
	private CheckBox cbShowAll;
	private TabPane tabPane;
	private ScrollPane propertiesContainer;

	private TreeView<IWindow> tvWindows;
	private FilterableTreeItem<IWindow> rootWindowsItem;

	// We could use a list, but that would require creating a FilterableListItem...
	private TreeView<ISporeImage> tvImages;
	private FilterableTreeItem<ISporeImage> rootImagesItem;

	private TreeView<IDrawable> tvDrawables;
	private FilterableTreeItem<IDrawable> rootDrawablesItem;

	/** The selected element whose properties are being displayed in the inspector. */
	private final ObjectProperty<InspectableObject> selectedElement = new SimpleObjectProperty<>();
	/** The selected window that is remarked in the viewer. Will be the same as selected element or null if element is not a window. */
	private final ObjectProperty<IWindow> selectedWindow = new SimpleObjectProperty<>();

	private SpuiDraggableType draggable;
	/** The original area of the window that is being modified by dragging the mouse. */
	private final SPUIRectangle originalArea = new SPUIRectangle();

	private double mouseX;
	private double mouseY;
	private double deltaMouseX;
	private double deltaMouseY;
	private double mouseClickX;
	private double mouseClickY;

	private RibbonTab ribbonTab;
	private RibbonGallery windowsGallery;
	private RibbonGallery proceduresGallery;

	/** True if the viewer is being manipulated, and so the inspector shouldn't generate events. */
	private boolean isEditingViewer;
	/** True if the editor is executing undo/redo, and therefore no new edit actions should be accepted. */
	private boolean isUndoingAction;

	// For undo redo:
	private final Stack<SpuiUndoableAction> editHistory = new Stack<>();
	private int undoRedoIndex = -1;

	// For dragging in the hierarchy tree
	private FilterableTreeItem<IWindow> draggedItem;
	private TreeCell<IWindow> dropZone;
	private WindowDropType dropType;

	/** An action run whenever the inspector requests an update. It's set to null every time the selected inspectable changes. */
	private Runnable onInspectorUpdateRequest;

	/** {@inheritDoc} */
	@Override protected Skin<?> createDefaultSkin() {
		return new SpuiEditorSkin(this);
	}

	@SuppressWarnings("unchecked")
	public SpuiEditor() {
		super();

		getStyleClass().add(DEFAULT_STYLE_CLASS);

		createRibbonTab();

		viewer.widthProperty().bind(widthProperty());
		viewer.heightProperty().bind(heightProperty());

		overlayCanvas = new Canvas();
		overlayCanvas.widthProperty().bind(widthProperty());
		overlayCanvas.heightProperty().bind(heightProperty());

		//overlayCanvas.translateXProperty().bind(viewer.contentTranslateXProperty());
		//overlayCanvas.translateYProperty().bind(viewer.contentTranslateYProperty());
		// We want the events go to the viewer and not this layer
		overlayCanvas.setMouseTransparent(true);

		rootWindowsItem = new FilterableTreeItem<IWindow>(null);
		tvWindows = new TreeView<IWindow>(rootWindowsItem);
		tvWindows.setShowRoot(true);
		tvWindows.setCellFactory(c -> createWindowCell());
		tvWindows.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if (event.getCode() == KeyCode.DELETE) {
				removeWindow(tvWindows.getSelectionModel().getSelectedItem());
			}
		});

		rootImagesItem = new FilterableTreeItem<ISporeImage>(null);
		tvImages = new TreeView<ISporeImage>(rootImagesItem);
		tvImages.setShowRoot(false);

		rootDrawablesItem = new FilterableTreeItem<IDrawable>(null);
		tvDrawables = new TreeView<IDrawable>(rootDrawablesItem);
		tvDrawables.setShowRoot(false);

		tabPane = new TabPane();
		propertiesContainer = new ScrollPane();
		propertiesContainer.setFitToWidth(true);
		tabPane.setMinHeight(260);
		tabPane.setMaxHeight(260);

		Tab windowsTab = new Tab("Layout", tvWindows);
		Tab imagesTab = new Tab("Images", tvImages);
		Tab drawablesTab = new Tab("Drawables", tvDrawables);
		windowsTab.setClosable(false);
		imagesTab.setClosable(false);
		drawablesTab.setClosable(false);

		tabPane.getTabs().addAll(windowsTab, imagesTab, drawablesTab);
		tabPane.getSelectionModel().select(0);
		tabPane.setPrefHeight(TAB_PANE_HEIGHT);

		tfSearch = new TextField();
		tfSearch.setPromptText("Search");

		cbShowAll = new CheckBox("Show all");
		viewer.showInvisibleProperty().bind(cbShowAll.selectedProperty());

		BorderPane topPane = new BorderPane();
		topPane.setCenter(tfSearch);
		topPane.setRight(cbShowAll);
		BorderPane.setMargin(tfSearch, new Insets(0, 5, 0, 0));
		BorderPane.setAlignment(cbShowAll, Pos.CENTER_LEFT);

		inspectorPane = new VBox(5);
		inspectorPane.getChildren().addAll(topPane, tabPane, propertiesContainer);

		VBox.setVgrow(propertiesContainer, Priority.ALWAYS);


		rootWindowsItem.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			if (tfSearch.getText().isEmpty()) return null;
			else return TreeItemPredicate.create(window -> {
				return window.toString().toLowerCase().contains(tfSearch.getText().toLowerCase());
			});
		}, tfSearch.textProperty()));

		tvWindows.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				selectedElement.set((InspectableObject) newValue.getValue());
			}
		});

		tvImages.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) selectedElement.set((InspectableObject) newValue.getValue());
		});

		tvDrawables.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) selectedElement.set(newValue.getValue());
		});

		selectedElement.addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				onInspectorUpdateRequest = null;
				propertiesContainer.setContent(newValue.generateUI(this));

				if (newValue instanceof IWindow) {
					selectedWindow.set((IWindow) newValue); 
				}

				//    			if (newValue instanceof WindowBase) {
				//    				tvImages.getSelectionModel().select(null);
				//    				tvDrawables.getSelectionModel().select(null);
				//    			} else if (newValue instanceof IDrawable) {
				//    				tvImages.getSelectionModel().select(null);
				//    				tvWindows.getSelectionModel().select(null);
				//    			} else if (newValue instanceof ISporeImage) {
				//    				tvDrawables.getSelectionModel().select(null);
				//    				tvWindows.getSelectionModel().select(null);
				//    			} else {
				//    				tvDrawables.getSelectionModel().select(null);
				//    				tvWindows.getSelectionModel().select(null);
				//    				tvImages.getSelectionModel().select(null);
				//    			}

				boolean disable = newValue == null || newValue != selectedWindow.get();

				// We can't add procedures to the layout window
				proceduresGallery.setDisable(disable || selectedWindow.get() == viewer.getLayoutWindow());
				windowsGallery.setDisable(disable);
			} else {
				propertiesContainer.setContent(null);
			}
		});

		selectedWindow.addListener((obs, oldValue, newValue) -> {
			// We might show new windows now
			if (!viewer.getShowInvisible()) viewer.repaint();

			paintSelection();
		});

		viewer.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getClickCount() >= 2 && event.getButton() == MouseButton.MIDDLE) {
				viewer.setContentTranslateX(0);
				viewer.setContentTranslateY(0);
			}

			if (event.getButton() == MouseButton.PRIMARY) {
				WindowBase newSelection = (WindowBase) viewer.getWindowInCoords(event.getX(), event.getY(), selectedWindow.get());
				// We want to keep the selection if the user clicks on empty space
				if (newSelection != null) {
					selectInspectable((InspectableObject) newSelection);
					//selectedElement.set((InspectableObject) newSelection);
				}
			}
		});

		viewer.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
			deltaMouseX = event.getX() - mouseX;
			deltaMouseY = event.getY() - mouseY;
			mouseX = event.getX();
			mouseY = event.getY();

			if (selectedWindow.get() != null) {
				Cursor cursor = null;
				SPUIRectangle rect = selectedWindow.get().getRealArea();

				for (SpuiDraggableType type : SpuiDraggableType.values()) {
					SPUIRectangle point = type.getPointRect(rect, POINT_SIZE);
					if (point != null && point.contains(mouseX, mouseY)) {
						cursor = type.getCursor();
						break;
					}
				}

				viewer.setCursor(cursor);
			}
		});

		viewer.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			isEditingViewer = true;
			mouseClickX = event.getX();
			mouseClickY = event.getY();

			if (event.getButton() == MouseButton.SECONDARY && selectedWindow.get() != null && selectedWindow.get() != viewer.getLayoutWindow()) {
				SPUIRectangle rect = selectedWindow.get().getRealArea();

				for (SpuiDraggableType type : SpuiDraggableType.values()) {
					SPUIRectangle point = type.getPointRect(rect, POINT_SIZE);
					if (point != null && point.contains(mouseX, mouseY)) {
						draggable = type;
						break;
					}
				}
				if (draggable == null && rect.contains(mouseX, mouseY)) {
					draggable = SpuiDraggableType.COMPONENT;
				}

				originalArea.copy(selectedWindow.get().getArea());
			}
		});

		viewer.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
			if (draggable != null) {
				SPUIRectangle newArea = selectedWindow.get().getArea();
				if (!originalArea.compare(newArea)) {
					DesignerClass clazz = ((SpuiElement) selectedWindow.get()).getDesignerClass();
					DesignerProperty property = clazz.getProperty(0xeec1b005);

					addEditAction(new SpuiPropertyAction<SPUIRectangle>(new SPUIRectangle(originalArea), new SPUIRectangle(newArea), (v) -> {
						((InspectorValue<SPUIRectangle>)property.getInspectorComponents().get(0)).setValue(v);
					}, clazz.getName() + ": " + property.getName()));
				}
			}

			draggable = null;
			isEditingViewer = false;
		});

		viewer.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
			deltaMouseX = event.getX() - mouseX;
			deltaMouseY = event.getY() - mouseY;
			mouseX = event.getX();
			mouseY = event.getY();

			if (event.isSecondaryButtonDown() && draggable != null && selectedWindow.get() != null) {
				draggable.process(selectedWindow.get(), (float)deltaMouseX, (float)deltaMouseY);
				repaint();

				// We need to update the inspector panel as well
				// We know it won't generate events cause MOUSE_PRESSED set isEditingViewer = true
				if (selectedElement.get() == selectedWindow.get()) {
					DesignerProperty property = ((SpuiElement) selectedWindow.get()).getDesignerClass().getProperty(0xeec1b005);
					((InspectorRectangle)property.getInspectorComponents().get(0)).setValue(selectedWindow.get().getArea());
				}
			}
			else if (event.isMiddleButtonDown()) {
				viewer.setContentTranslateX(viewer.getContentTranslateX() + deltaMouseX);
				viewer.setContentTranslateY(viewer.getContentTranslateY() + deltaMouseY);
				repaint();
			}
		});

		// Add an original action that does nothing:
		addEditAction(ORIGINAL_ACTION);
	}

	private void createRibbonTab() {
		ribbonTab = new RibbonTab("SPUI Editor");
		ribbonTab.getStyleClass().add("spui-editor-ribbon-header");

		Ribbon ribbon = UIManager.get().getUserInterface().getRibbon();

		windowsGallery = new RibbonGallery(ribbon);
		windowsGallery.setDisplayPriority(GalleryItemDisplay.TEXT_PRIORITY);
		windowsGallery.setColumnCount(3);
		windowsGallery.setOnItemAction(item -> addWindow(item.getText()));

		proceduresGallery = new RibbonGallery(ribbon);
		proceduresGallery.setDisplayPriority(GalleryItemDisplay.TEXT_PRIORITY);
		proceduresGallery.setColumnCount(3);
		proceduresGallery.setOnItemAction(item -> addWinProc(item.getText()));

		for (DesignerClass clazz : SporeUserInterface.getDesigner().getClasses().values()) {
			if (!clazz.isAbstract()) {
				if (clazz.implementsInterfaceComplete("IWindow")) {
					RibbonGalleryItem item = new RibbonGalleryItem();
					item.setText(clazz.getName());
					item.setUserData(clazz);
					item.setDescription(clazz.getDescription());
					windowsGallery.getItems().add(item);
				}
				else if (clazz.implementsInterfaceComplete("IWinProc")) {
					RibbonGalleryItem item = new RibbonGalleryItem();
					item.setText(clazz.getName());
					item.setUserData(clazz);
					item.setDescription(clazz.getDescription());
					proceduresGallery.getItems().add(item);
				}
			}
		}

		proceduresGallery.setDisable(true);
		windowsGallery.setDisable(true);

		RibbonGroup windowsGroup = new RibbonGroup("Add Windows");
		RibbonGroup winprocsGroup = new RibbonGroup("Add Window Procedures");
		RibbonGroup editorGroup = new RibbonGroup("Layout");

		windowsGroup.getNodes().add(windowsGallery);

		winprocsGroup.getNodes().add(proceduresGallery);

		RibbonButton previewButton = new RibbonButton("Preview", UIManager.get().loadIcon("spui-preview.png", 0, 48, true));
		previewButton.setOnAction(event -> showPreview());
		editorGroup.getNodes().add(previewButton);
		
		RibbonButton duplicateButton = new RibbonButton("Duplicate", UIManager.get().loadIcon("spui-duplicate.png", 0, 48, true));
		duplicateButton.setOnAction(event -> {
			try {
				duplicateSelectedBlock();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		editorGroup.getNodes().add(duplicateButton);

		RibbonButton exportButton = new RibbonButton("Export", UIManager.get().loadIcon("spui-export.png", 0, 48, true));
		exportButton.setOnAction(event -> {
			try {
				exportBlocks();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		editorGroup.getNodes().add(exportButton);
		
		RibbonButton importButton = new RibbonButton("Import", UIManager.get().loadIcon("spui-import.png", 0, 48, true));
		importButton.setOnAction(event -> {
			try {
				importSpui();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		editorGroup.getNodes().add(importButton);

		ribbonTab.getGroups().addAll(windowsGroup, winprocsGroup, editorGroup);

	}

	private void addWinProc(String className) {
		DesignerClass clazz = SporeUserInterface.getDesigner().getClass(className);
		SpuiElement element = clazz.createInstance();
		clazz.fillDefaults(this, element);

		WindowBase window = (WindowBase) selectedWindow.get();
		DesignerProperty property = window.getDesignerClass().getProperty(0xeec1b00c);
		InspectorValueList<Object> component = property.getInspectorListComponent();

		List<Object> list = component.getValue();
		List<Object> oldValue = new ArrayList<>(list);
		list.add(element);
		component.setValue(list);
		property.processUpdate(this);

		addEditAction(new SpuiObjectCreatedAction(this, element, "Created " + clazz.getName()) {
			@Override public void undo() {
				super.undo();
				property.getInspectorListComponent().setValue(oldValue);
				property.processUpdate(SpuiEditor.this);
			}

			@Override public void redo() {
				super.redo();
				property.getInspectorListComponent().setValue(list);
				property.processUpdate(SpuiEditor.this);
			}
		});

		selectInspectable(element);
	}

	private void addWindow(String className) {
		DesignerClass clazz = SporeUserInterface.getDesigner().getClass(className);
		SpuiElement childWindow = clazz.createInstance();
		clazz.fillDefaults(this, childWindow);

		IWindow window = selectedWindow.get();
		List<IWindow> children = window.getChildren();

		List<IWindow> oldValue = new ArrayList<>(children);
		children.add((IWindow) childWindow);
		generateWindowTree(getWindowItem(window), (IWindow) childWindow);

		repaint();

		List<IWindow> newValue = new ArrayList<>(children);

		addEditAction(new SpuiUndoableAction() {
			@Override public void undo() {
				removeElement(childWindow);
				children.clear();
				children.addAll(oldValue);
				repaint();
			}

			@Override public void redo() {
				children.clear();
				children.addAll(newValue);
				generateWindowTree(getWindowItem(window), (IWindow) childWindow);
				selectInspectable(childWindow);
				repaint();
			}

			@Override public String getText() {
				return "Created " + clazz.getName();
			}
		});

		selectInspectable(childWindow);
	}
	
	private void addWindow(SpuiElement childWindow) {
		IWindow window = selectedWindow.get();
		List<IWindow> children = window.getChildren();

		List<IWindow> oldValue = new ArrayList<>(children);
		children.add((IWindow) childWindow);
		generateWindowTree(getWindowItem(window), (IWindow) childWindow);

		repaint();

		List<IWindow> newValue = new ArrayList<>(children);

		selectInspectable(childWindow);
	}

	private void removeWindow(TreeItem<IWindow> treeItem) {
		if (treeItem == null || treeItem == rootWindowsItem) return;

		FilterableTreeItem<IWindow> parentItem = (FilterableTreeItem<IWindow>) treeItem.getParent();
		IWindow parentWindow = parentItem.getValue();
		IWindow childWindow = treeItem.getValue();

		DesignerClass clazz = ((SpuiElement) childWindow).getDesignerClass();

		List<IWindow> children = parentWindow.getChildren();

		List<IWindow> oldValue = new ArrayList<>(children);
		int index = children.indexOf(childWindow);
		children.remove(childWindow);

		parentItem.getInternalChildren().remove(treeItem);

		repaint();
		refreshTree();

		List<IWindow> newValue = new ArrayList<>(children);

		addEditAction(new SpuiUndoableAction() {
			@Override public void undo() {
				getWindowItem(parentWindow).getInternalChildren().add(index, getWindowItem(childWindow));
				children.clear();
				children.addAll(oldValue);
				selectInspectable((InspectableObject) childWindow);
				repaint();
				refreshTree();
			}

			@Override public void redo() {
				children.clear();
				children.addAll(newValue);
				getWindowItem(parentWindow).getInternalChildren().remove(getWindowItem(childWindow));
				selectInspectable((InspectableObject) parentWindow);
				repaint();
				refreshTree();
			}

			@Override public String getText() {
				return "Removed " + clazz.getName();
			}
		});

		selectInspectable((InspectableObject) parentWindow);

	}

	private void paintSelection() {
		GraphicsContext g = overlayCanvas.getGraphicsContext2D();
		g.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

		if (selectedWindow.get() != null && selectedWindow.get() != viewer.getLayoutWindow()) {
			SPUIRectangle rect = selectedWindow.get().getRealArea();

			g.setStroke(ACTIVE_COLOR);
			g.setLineWidth(1.0);
			g.strokeRect(rect.x1, rect.y1, rect.getWidth(), rect.getHeight());

			g.setFill(ACTIVE_COLOR);
			for (SpuiDraggableType type : SpuiDraggableType.values()) {
				SPUIRectangle point = type.getPointRect(rect, POINT_SIZE);
				if (point != null) {
					g.fillRect(point.x1, point.y1, point.getWidth(), point.getHeight());
				}
			}
		}
	}

	private int getImageIndex(ISporeImage value) {
		int index = 0;
		for (TreeItem<ISporeImage> item : tvImages.getRoot().getChildren()) {
			if (item.getValue() == value) return index;
			++index;
		}
		return -1;
	}

	private int getDrawableIndex(IDrawable value) {
		int index = 0;
		for (TreeItem<IDrawable> item : tvDrawables.getRoot().getChildren()) {
			if (item.getValue() == value) return index;
			++index;
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public void selectInspectable(InspectableObject value) {
		if (value instanceof ISporeImage) {
			int index = getImageIndex((ISporeImage) value);
			tvImages.getSelectionModel().select(index);
			tabPane.getSelectionModel().select(1);
			tvImages.scrollTo(index);
		}
		else if (value instanceof IDrawable) {
			int index = getDrawableIndex((IDrawable) value);
			tvDrawables.getSelectionModel().select(index);
			tabPane.getSelectionModel().select(2);
			tvDrawables.scrollTo(index);
		}
		else if (value instanceof IWindow) {
			TreeItem<IWindow> item = (TreeItem<IWindow>) ((InspectableObject) value).getTreeItem();
			tvWindows.getSelectionModel().select(item);
			tvWindows.scrollTo(tvWindows.getRow(item));
			tabPane.getSelectionModel().select(0);
		}
		else {
			selectedElement.set(value);
		}
	}

	public void addElement(InspectableObject object) {
		if (object instanceof WindowBase) {
			throw new UnsupportedOperationException("Cannot add a window element without a parent.");
		}
		else if (object instanceof ISporeImage) {
			addSporeImage((ISporeImage) object);
		}
		else if (object instanceof IDrawable) {
			addDrawable((IDrawable) object);
		}
	}

	public void removeElement(InspectableObject object) {
		TreeItem<?> item = object.getTreeItem();
		if (item != null) {
			((FilterableTreeItem<?>)item.getParent()).getInternalChildren().remove(item);
		}
	}

	public IWindow getSelectedWindow() {
		return selectedWindow.get();
	}

	public SpuiViewer getViewer() {
		return viewer;
	}

	public Canvas getOverlayCanvas() {
		return overlayCanvas;
	}

	@Override
	public void loadFile(ProjectItem item) throws IOException {
		this.item = item;
		if (item != null) {
			loadFile(item.getFile());
		}
	}

	private void generateWindowTree(FilterableTreeItem<IWindow> parentItem, IWindow window) {
		FilterableTreeItem<IWindow> item = new FilterableTreeItem<IWindow>(window);
		parentItem.getInternalChildren().add(item);
		((WindowBase)window).setTreeItem(item);

		for (IWindow child : window.getChildren()) {
			generateWindowTree(item, (WindowBase) child);
		}
	}

	public void loadFile(File file) throws IOException {
		this.file = file;

		rootWindowsItem.getChildren().clear();
		rootImagesItem.getChildren().clear();
		rootDrawablesItem.getChildren().clear();
		spui = new SporeUserInterface();

		try (FileStream stream = new FileStream(file, "r")) {
			if (stream.length() != 0) {
				spui.read(stream);
				viewer.getLayoutWindow().getChildren().addAll(spui.getRootWindows());
			}
		}

		if (!spui.getUnloadedFiles().isEmpty()) {
			Label label = new Label();
			label.setWrapText(true);

			StringBuilder sb = new StringBuilder();
			sb.append("The following images could not be loaded:");
			for (String path : spui.getUnloadedFiles()) {
				sb.append("\n - " + path);
			}
			label.setText(sb.toString());

			Alert alert = new Alert(AlertType.WARNING, null, ButtonType.OK);
			alert.getDialogPane().setContent(label);
			UIManager.get().showDialog(alert);
		}

		viewer.repaint();

		rootWindowsItem.setValue(viewer.getLayoutWindow());
		viewer.getLayoutWindow().setTreeItem(rootWindowsItem);

		for (IWindow window : viewer.getLayoutWindow().getChildren()) {
			generateWindowTree(rootWindowsItem, window);
		}

		rootWindowsItem.setExpanded(true);


		for (ISporeImage image : spui.getDirectImages()) {
			FilterableTreeItem<ISporeImage> item = new FilterableTreeItem<ISporeImage>(image);
			rootImagesItem.getInternalChildren().add(item);
		}
		for (SpuiElement element : spui.getElements()) {
			if (element instanceof ISporeImage) {
				addSporeImage((ISporeImage) element);
			}
			else if (element instanceof IDrawable) {
				addDrawable((IDrawable) element);
			}
		}

		showInspector(true);
	}

	private void addSporeImage(ISporeImage element) {
		FilterableTreeItem<ISporeImage> item = new FilterableTreeItem<ISporeImage>(element);
		((InspectableObject)element).setTreeItem(item);
		rootImagesItem.getInternalChildren().add(item);
	}

	private void addDrawable(IDrawable element) {
		FilterableTreeItem<IDrawable> item = new FilterableTreeItem<IDrawable>(element);
		element.setTreeItem(item);
		rootDrawablesItem.getInternalChildren().add(item);
	}

	private void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("SPUI Editor", "spui", inspectorPane);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}

	@Override
	public void setDestinationFile(File file) {
		this.file = file;
	}

	@Override
	public void setActive(boolean isActive) {
		super.setActive(isActive);

		HashManager.get().setUpdateProjectRegistry(isActive);

		if (isActive) {
			Ribbon ribbon = UIManager.get().getUserInterface().getRibbon();
			ribbon.getTabs().add(ribbonTab);

			// Accelerators
			Scene scene = UIManager.get().getScene();
			scene.getAccelerators().put(CTRL_Z, () -> undo());
			scene.getAccelerators().put(CTRL_Y, () -> redo());
		} else {
			Ribbon ribbon = UIManager.get().getUserInterface().getRibbon();
			ribbon.getTabs().remove(ribbonTab);

			// Accelerators
			Scene scene = UIManager.get().getScene();
			scene.getAccelerators().remove(CTRL_Z);
			scene.getAccelerators().remove(CTRL_Y);
		}

		showInspector(isActive);
	}

	@Override
	public Node getUI() {
		return this;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean supportsSearching() {
		return false;
	}

	@Override
	public boolean supportsEditHistory() {
		return true;
	}

	@Override
	protected void saveData() throws Exception {
		try (StreamWriter stream = new FileStream(getFile(), "rw")) {
			SpuiWriter writer = new SpuiWriter(viewer.getLayoutWindow().getChildren());
			writer.write(stream);

			setIsSaved(true);

			// Also save names registry
			ProjectManager.get().saveNamesRegistry();
		}
	}

	@Override
	protected void restoreContents() throws Exception {
		loadFile(getFile());
	}

	public void repaint() {
		viewer.repaint();
		paintSelection();
		if (onInspectorUpdateRequest != null) onInspectorUpdateRequest.run();
	}

	public void refreshTree() {
		tvWindows.refresh();
		tvImages.refresh();
		tvDrawables.refresh();
		if (onInspectorUpdateRequest != null) onInspectorUpdateRequest.run();
	}

	/**
	 * Returns true if the viewer is being manipulated, and so the inspector shouldn't generate events.
	 * @return
	 */
	public boolean isEditingViewer() {
		return isEditingViewer;
	}

	@Override
	public boolean canUndo() {
		// We can't undo the first action cause we couldn't go to the previous action
		return editHistory.size() > 1 && undoRedoIndex > 0;
	}

	@Override
	public boolean canRedo() {
		return undoRedoIndex != editHistory.size() - 1;
	}

	@Override
	public void undo() {
		isUndoingAction = true;

		SpuiUndoableAction action = editHistory.get(undoRedoIndex);
		if (action.getSelectedObject() != null) {
			selectInspectable(action.getSelectedObject());
			// The old inspectable might already be selected but not be in selectedElement
			selectedElement.set(action.getSelectedObject());
		}

		action.undo();
		--undoRedoIndex;

		isUndoingAction = false;

		if (undoRedoIndex == 1 && editHistory.get(0) == ORIGINAL_ACTION) {
			setIsSaved(true);
		} else {
			setIsSaved(false);
		}

		UIManager.get().notifyUIUpdate(false);
	}

	@Override
	public void redo() {
		isUndoingAction = true;
		// We redo the last action we did
		++undoRedoIndex;

		SpuiUndoableAction action = editHistory.get(undoRedoIndex);
		if (action.getSelectedObject() != null) {
			selectInspectable(action.getSelectedObject());
		}
		action.redo();

		isUndoingAction = false;

		setIsSaved(false);

		UIManager.get().notifyUIUpdate(false);
	}

	private void deleteActionsAfterIndex(int index) {
		for (int i = editHistory.size()-1; i > index; --i) {
			editHistory.remove(i);
		}
	}

	public void addEditAction(SpuiUndoableAction action) {
		if (!isUndoingAction) {
			// If the edit undoed certain actions we start a new edit branch now
			deleteActionsAfterIndex(undoRedoIndex);
			action.setSelectedObject(selectedElement.get());
			editHistory.push(action);
			++undoRedoIndex;

			if (editHistory.size() > MAX_EDIT_HISTORY) {
				editHistory.remove(0);
				--undoRedoIndex;
			}

			if (action != ORIGINAL_ACTION) setIsSaved(false);

			UIManager.get().notifyUIUpdate(false);
		}
	}

	public boolean isUndoingAction() {
		return isUndoingAction;
	}

	@Override
	public List<? extends EditHistoryAction> getActions() {
		return editHistory;
	}

	@Override
	public int getUndoRedoIndex() {
		return undoRedoIndex;
	}

	private static final String WINDOW_DRAG_KEY = "spui-editor-window-drag";
	private static final String DROP_ZONE_STYLE = "spui-editor-tree-dropzone";
	private static final String DROP_BELOW_STYLE = "spui-editor-tree-dropbelow";
	private static final String DROP_ABOVE_STYLE = "spui-editor-tree-dropabove";
	private static final double DROP_THRESHOLD = 0.25;

	private SpuiWindowItemCell createWindowCell() {
		SpuiWindowItemCell cell = new SpuiWindowItemCell();

		cell.setOnDragDetected(event -> {
			draggedItem = (FilterableTreeItem<IWindow>) cell.getTreeItem();

			// We can't drag the root
			if (draggedItem.getParent() == null) return;
			Dragboard db = startDragAndDrop(TransferMode.MOVE);

			ClipboardContent content = new ClipboardContent();
			// We need to put some data, but as we keep the item in 'draggedItem' we don't care
			content.put(DataFormat.PLAIN_TEXT, WINDOW_DRAG_KEY);
			db.setContent(content);
			db.setDragView(cell.snapshot(null, null));

			event.consume();
		});

		cell.setOnDragOver(event -> {
			// Not an event for us
			if (!WINDOW_DRAG_KEY.equals(event.getDragboard().getContent(DataFormat.PLAIN_TEXT))) return;

			TreeItem<IWindow> thisItem = cell.getTreeItem();

			// We can't drop the item on itself
			if (draggedItem == null || thisItem == null || thisItem == draggedItem) return;

			event.acceptTransferModes(TransferMode.MOVE);
			removeDropStyle();
			dropZone = cell;

			if (thisItem.getParent() == null) {
				// We only allow dropping inside the layout root
				dropType = WindowDropType.INSIDE;
				dropZone.getStyleClass().add(DROP_ZONE_STYLE);
			}
			else {
				double height = cell.getHeight();
				Point2D sceneCoords = cell.localToScene(0, 0);
				double y = event.getSceneY() - sceneCoords.getY();

				if (y <= (height*DROP_THRESHOLD)) {
					dropType = WindowDropType.ABOVE;
					dropZone.getStyleClass().add(DROP_ABOVE_STYLE);
				} 
				else if (y >= height*(1-DROP_THRESHOLD)) {
					dropType = WindowDropType.BELOW;
					dropZone.getStyleClass().add(DROP_BELOW_STYLE);
				} 
				else {
					dropType = WindowDropType.INSIDE;
					dropZone.getStyleClass().add(DROP_ZONE_STYLE);
				}
			}
		});

		cell.setOnDragExited(event -> {
			// Not an event for us
			if (!WINDOW_DRAG_KEY.equals(event.getDragboard().getContent(DataFormat.PLAIN_TEXT))) return;

			removeDropStyle();
		});

		cell.setOnDragDropped(event -> {
			IWindow window = draggedItem.getValue();
			IWindow oldParent = window.getParent();

			TreeItem<IWindow> droppedItem = cell.getTreeItem();
			FilterableTreeItem<IWindow> newParentItem;
			FilterableTreeItem<IWindow> oldParentItem = ((FilterableTreeItem<IWindow>) draggedItem.getParent());
			int index;

			if (dropType == WindowDropType.ABOVE) {
				newParentItem = (FilterableTreeItem<IWindow>) droppedItem.getParent();
				index = newParentItem.getInternalChildren().indexOf(droppedItem);
			} 
			else if (dropType == WindowDropType.BELOW) {
				newParentItem = (FilterableTreeItem<IWindow>) droppedItem.getParent();
				index = newParentItem.getInternalChildren().indexOf(droppedItem) + 1;
			} 
			else {
				newParentItem = (FilterableTreeItem<IWindow>) droppedItem;
				index = 0;
			}

			int oldIndex = oldParentItem.getInternalChildren().indexOf(draggedItem);

			// If we are putting it into the same parent the index might change!
			if (draggedItem.getParent() == newParentItem) {
				if (index > oldIndex) --index;
			}

			final int newIndex = index;

			// Remove it from its parent
			oldParentItem.getInternalChildren().remove(draggedItem);

			newParentItem.getInternalChildren().add(index, draggedItem);
			tvWindows.getSelectionModel().select(draggedItem);

			// Now we must update the IWindow children
			IWindow newParent = newParentItem.getValue();

			List<IWindow> oldParent_oldList = new ArrayList<>(oldParent.getChildren());
			List<IWindow> newParent_oldList = new ArrayList<>(newParent.getChildren());

			oldParent.getChildren().remove(window);
			newParent.getChildren().add(index, window);

			List<IWindow> oldParent_newList = new ArrayList<>(oldParent.getChildren());
			List<IWindow> newParent_newList = new ArrayList<>(newParent.getChildren());

			DesignerClass clazz = ((SpuiElement) window).getDesignerClass();
			DesignerProperty property = clazz.getProperty(0xeec1b00b);

			// Add the edit action
			addEditAction(new SpuiUndoableAction() {

				@Override public void undo() {
					oldParent.getChildren().clear();
					oldParent.getChildren().addAll(oldParent_oldList);

					getWindowItem(newParent).getInternalChildren().remove(getWindowItem(window));
					getWindowItem(oldParent).getInternalChildren().add(oldIndex, getWindowItem(window));

					if (newParent != oldParent) {
						newParent.getChildren().clear();
						newParent.getChildren().addAll(newParent_oldList);
					}

					property.processUpdate(SpuiEditor.this);
				}

				@Override public void redo() {
					oldParent.getChildren().clear();
					oldParent.getChildren().addAll(oldParent_newList);

					getWindowItem(oldParent).getInternalChildren().remove(getWindowItem(window));
					getWindowItem(newParent).getInternalChildren().add(newIndex, getWindowItem(window));

					if (newParent != oldParent) {
						newParent.getChildren().clear();
						newParent.getChildren().addAll(newParent_newList);
					}

					property.processUpdate(SpuiEditor.this);
				}

				@Override public String getText() {
					return property.getActionText((SpuiElement) window);
				}
			});

			property.processUpdate(this);

			removeDropStyle();
			draggedItem = null;
			dropZone = null;
			event.setDropCompleted(true);
			event.consume();
		});

		return cell;
	}

	@SuppressWarnings("unchecked")
	private FilterableTreeItem<IWindow> getWindowItem(IWindow window) {
		return (FilterableTreeItem<IWindow>) ((InspectableObject)window).getTreeItem();
	}

	private void removeDropStyle() {
		if (dropZone != null) {
			dropZone.getStyleClass().removeAll(DROP_ZONE_STYLE, DROP_BELOW_STYLE, DROP_ABOVE_STYLE);
		}
	}

	private void showPreview() {

		Stage stage = new Stage();

		SpuiViewer preview = new SpuiViewer(this, viewer.getLayoutWindow());

		Group group = new Group();
		group.getChildren().add(preview);

		double width = 10;
		double height = 10;

		for (IWindow window : preview.getLayoutWindow().getChildren()) {
			if (window.getArea().getWidth() > width)
				width = window.getArea().getWidth();

			if (window.getArea().getHeight() > height)
				height = window.getArea().getHeight();
		}
		
 		Scene scene = new Scene(group, width, height);
		
		preview.setIsPreview(true);
		preview.widthProperty().bind(scene.widthProperty());
		preview.heightProperty().bind(scene.heightProperty());

		stage.setScene(scene);
		stage.setResizable(true);
		stage.setTitle("SPUI Preview");
		stage.initModality(Modality.APPLICATION_MODAL);

		UIManager.get().setOverlay(true);
		stage.showAndWait();
		UIManager.get().setOverlay(false);

		preview.restoreOriginal();
	}
	
	private void duplicateSelectedBlock() throws Exception {
		IWindow iwin = getSelectedWindow();
		if (iwin != null) {
			//MemoryStream memstream = new MemoryStream();
			byte[] raw = new byte[0];
			try (StreamWriter stream = new MemoryStream()) {
				IWindow selWin = getSelectedWindow();
				IWindow selParent = selWin.getParent();
				int windowIndex = selParent.getChildren().indexOf(selWin);
				selParent.getChildren().remove(selWin);
				sporemodder.view.editors.spui.SpuiLayoutWindow layoutWindow = new sporemodder.view.editors.spui.SpuiLayoutWindow();
				layoutWindow.getChildren().add(selWin);
				SpuiWriter writer = new SpuiWriter(layoutWindow.getChildren());
				writer.write(stream);
				layoutWindow.getChildren().remove(selWin);
				selParent.getChildren().add(windowIndex, selWin);
				
				raw = ((MemoryStream)stream).getRawData();
			}
			try (StreamReader stream = new MemoryStream(raw)) {
				SporeUserInterface importSpui = new SporeUserInterface();
				if (stream.length() != 0) {
					importSpui.read(stream);
					
					List<IWindow> windows = new ArrayList<IWindow>();
					windows.addAll(importSpui.getRootWindows());
					for (IWindow window : windows) {						
						if (window.getParent() != null)
							window.getParent().getChildren().remove(window);
						importSpui.getRootWindows().remove(window);
						importSpui.getElements().remove(window);
						//getSelectedWindow().getChildren().add(window);
						//addWindow(window);
						/*selectInspectable((InspectableObject)window);
						repaint();*/
						if (getSelectedWindow().getParent() != null)
							selectedWindow.set(getSelectedWindow().getParent());
						
						addWindow((SpuiElement)window);
					}
					
					//getSelectedWindow().getChildren().addAll(windows);
				}
			}
		}
	}

	private void exportBlocks() throws Exception {
		if (getSelectedWindow() != null) {
			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(getFile().getParentFile());
			chooser.getExtensionFilters().add(new ExtensionFilter("Partial SPUI file (*.spui_part)", "*.spui_part"));
			File targetFile = chooser.showSaveDialog(null);
			if (targetFile != null) {
				try (StreamWriter stream = new FileStream(targetFile, "rw")) {
					IWindow selWin = getSelectedWindow();
					IWindow selParent = selWin.getParent();
					int windowIndex = selParent.getChildren().indexOf(selWin);
					selParent.getChildren().remove(selWin);
					sporemodder.view.editors.spui.SpuiLayoutWindow layoutWindow = new sporemodder.view.editors.spui.SpuiLayoutWindow();
					layoutWindow.getChildren().add(selWin);
					SpuiWriter writer = new SpuiWriter(layoutWindow.getChildren());
					writer.write(stream);
					layoutWindow.getChildren().remove(selWin);
					selParent.getChildren().add(windowIndex, selWin);
				}
			}
		}
	}
	
	private void importSpui() throws Exception {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(getFile().getParentFile());
		chooser.getExtensionFilters().add(new ExtensionFilter("All SPUI files (*.spui, *.spui_part)", "*.spui", "*.spui_part"));
		chooser.getExtensionFilters().add(new ExtensionFilter("Partial SPUI files (*.spui_part)", "*.spui_part"));
		chooser.getExtensionFilters().add(new ExtensionFilter("SPUI files (*.spui)", "*.spui"));
		File targetFile = chooser.showOpenDialog(null);
		if (targetFile != null) {
			SporeUserInterface importSpui = new SporeUserInterface();
			try (FileStream stream = new FileStream(targetFile, "r")) {
				if (stream.length() != 0) {
					importSpui.read(stream);
					
					List<IWindow> windows = new ArrayList<IWindow>();
					windows.addAll(importSpui.getRootWindows());
					for (IWindow window : windows) {						
						if (window.getParent() != null)
							window.getParent().getChildren().remove(window);
						importSpui.getRootWindows().remove(window);
						importSpui.getElements().remove(window);
						//getSelectedWindow().getChildren().add(window);
						//addWindow(window);
						/*selectInspectable((InspectableObject)window);
						repaint();*/
						addWindow((SpuiElement)window);
					}
					
					//getSelectedWindow().getChildren().addAll(windows);
				}
			}
		}
	}

	public IDrawable showDrawableChooser(IDrawable selected, String interfaceName) {
		ObservableList<IDrawable> sourceList = FXCollections.observableArrayList();
		for (TreeItem<IDrawable> item : rootDrawablesItem.getInternalChildren()) {
			if (interfaceName != null) {
				if (item.getValue().getDesignerClass().implementsInterfaceComplete(interfaceName)) {
					sourceList.add(item.getValue());
				}
			} else {
				sourceList.add(item.getValue());
			}
		}

		FilteredList<IDrawable> list = new FilteredList<IDrawable>(sourceList, item -> true);

		TextField tfSearch = new TextField();
		tfSearch.setPromptText("Search");
		tfSearch.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null || newValue.isEmpty()) list.setPredicate(item -> true);
			else list.setPredicate(item -> item.toString().toLowerCase().contains(newValue.toLowerCase()));
		});

		ListView<IDrawable> listView = new ListView<>(list);
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.getSelectionModel().select(selected);
		listView.setPrefHeight(500);
		listView.setPrefWidth(320);

		BorderPane pane = new BorderPane();
		pane.setCenter(listView);
		pane.setTop(tfSearch);

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Choose a drawable");
		dialog.getDialogPane().setContent(pane);
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

		if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
			return listView.getSelectionModel().getSelectedItem();
		}
		else {
			return null;
		}
	}

	public ISporeImage showImageChooser(ISporeImage selected) {
		ObservableList<ISporeImage> images = FXCollections.observableArrayList();
		for (TreeItem<ISporeImage> item : rootImagesItem.getInternalChildren()) {
			images.add(item.getValue());
		}

		SpuiImageChooser dialog = new SpuiImageChooser(images);
		dialog.setSelectedImage(selected);

		if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
			return dialog.getSelectedImage();
		}
		else {
			return null;
		}
	}

	/**
	 * Shows a dialog which allows the user to select an image file. The dialog shows the project file hierarchy and a preview of the selected image. 
	 * The user can only accept if the file is valid (that is, if it has a parent folder and .png extension)
	 * <p>
	 * The passed parameter is used to select the current used file. If the user presses OK, the new selected file will be returned. If the user cancels,
	 * the function returns null
	 * @param selected
	 * @returns Null if the user cancelled, the selected key otherwise
	 */
	public DirectImage showImageFileChooser(ResourceKey selected) {
		SpuiImageFileChooser dialog = new SpuiImageFileChooser();
		if (selected != null) dialog.setSelectedFile(selected);

		if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
			return new DirectImage(dialog.getSelectedImage(), dialog.getSelectedFile());
		} else {
			return null;
		}
	}

	public void setOnInspectorUpdateRequest(Runnable action) {
		onInspectorUpdateRequest = action;
	}
}
