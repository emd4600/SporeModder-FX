package sporemodder.view.editors.spui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import emord.javafx.ribbon.Ribbon;
import emord.javafx.ribbon.RibbonButton;
import emord.javafx.ribbon.RibbonGallery;
import emord.javafx.ribbon.RibbonGalleryItem;
import emord.javafx.ribbon.RibbonGroup;
import emord.javafx.ribbon.RibbonTab;
import emord.javafx.ribbon.RibbonGallery.GalleryItemDisplay;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.uidesigner.DesignerClass;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.ribbons.RibbonTabController;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.util.ProjectItem;

public class SpuiRibbonTab extends RibbonTabController {
	
	public static final String ID = "SPUI";
	
	private RibbonGallery windowsGallery;
	private RibbonGallery proceduresGallery;

	@Override public void addTab(Ribbon ribbon) {
		tab = new RibbonTab("SPUI Editor");
		tab.getStyleClass().add("spui-editor-ribbon-header");

		windowsGallery = new RibbonGallery(ribbon);
		windowsGallery.setDisplayPriority(GalleryItemDisplay.TEXT_PRIORITY);
		windowsGallery.setColumnCount(3);
		windowsGallery.setOnItemAction(item -> {
			if (SpuiEditor.getActiveSpuiEditor() != null)
				SpuiEditor.getActiveSpuiEditor().addWindow(item.getText());
		});

		proceduresGallery = new RibbonGallery(ribbon);
		proceduresGallery.setDisplayPriority(GalleryItemDisplay.TEXT_PRIORITY);
		proceduresGallery.setColumnCount(3);
		proceduresGallery.setOnItemAction(item -> {
			if (SpuiEditor.getActiveSpuiEditor() != null)
				SpuiEditor.getActiveSpuiEditor().addWinProc(item.getText());
		});

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

		RibbonGroup windowsGroup = new RibbonGroup("Add Windows");
		RibbonGroup winprocsGroup = new RibbonGroup("Add Window Procedures");
		RibbonGroup editorGroup = new RibbonGroup("Layout");

		windowsGroup.getNodes().add(windowsGallery);

		winprocsGroup.getNodes().add(proceduresGallery);

		RibbonButton previewButton = new RibbonButton("Preview", UIManager.get().loadIcon("spui-preview.png", 0, 48, true));
		previewButton.setOnAction(event -> {
			if (SpuiEditor.getActiveSpuiEditor() != null)
				SpuiEditor.getActiveSpuiEditor().showPreview();
		});
		editorGroup.getNodes().add(previewButton);
		
		RibbonButton duplicateButton = new RibbonButton("Duplicate", UIManager.get().loadIcon("spui-duplicate.png", 0, 48, true));
		duplicateButton.setOnAction(event -> {
			UIManager.get().tryAction(() -> {
				if (SpuiEditor.getActiveSpuiEditor() != null)
					SpuiEditor.getActiveSpuiEditor().duplicateSelectedBlock();	
			}, "Cannot duplicate SPUI block.");
		});
		editorGroup.getNodes().add(duplicateButton);

		RibbonButton exportButton = new RibbonButton("Export", UIManager.get().loadIcon("spui-export.png", 0, 48, true));
		exportButton.setOnAction(event -> {
			UIManager.get().tryAction(() -> {
				if (SpuiEditor.getActiveSpuiEditor() != null)
					SpuiEditor.getActiveSpuiEditor().exportBlocks();
			}, "Cannot export SPUI part.");
		});
		editorGroup.getNodes().add(exportButton);
		
		RibbonButton importButton = new RibbonButton("Import", UIManager.get().loadIcon("spui-import.png", 0, 48, true));
		importButton.setOnAction(event -> {
			UIManager.get().tryAction(() -> {
				if (SpuiEditor.getActiveSpuiEditor() != null)
					SpuiEditor.getActiveSpuiEditor().importSpui();
			}, "Cannot import partial SPUI.");
		});
		editorGroup.getNodes().add(importButton);
		
		/*RibbonButton yeOldSpuiEditorButton = new RibbonButton("Activate time machine", UIManager.get().loadIcon("spui-duplicate.png", 0, 48, true));
		yeOldSpuiEditorButton.setOnAction(event -> {
			SpuiEditor smfxEditor = SpuiEditor.getActiveSpuiEditor();
			if (smfxEditor != null) {
				smfxEditor.setUseLegacySpuiEditor(!smfxEditor.getUseLegacySpuiEditor()); //.getLegacySpuiEditorNode().setVisible(true);
			}
		});
		editorGroup.getNodes().add(yeOldSpuiEditorButton);*/

		tab.getGroups().addAll(windowsGroup, winprocsGroup, editorGroup);
	}

	public RibbonGallery getWindowsGallery() {
		return windowsGallery;
	}

	public RibbonGallery getProceduresGallery() {
		return proceduresGallery;
	}
}
