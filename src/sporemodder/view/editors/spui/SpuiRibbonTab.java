package sporemodder.view.editors.spui;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonButton;
import io.github.emd4600.javafxribbon.RibbonGallery;
import io.github.emd4600.javafxribbon.RibbonGalleryItem;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import io.github.emd4600.javafxribbon.RibbonGallery.GalleryItemDisplay;
import sporemodder.UIManager;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.uidesigner.DesignerClass;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.ribbons.RibbonTabController;

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

		tab.getGroups().addAll(windowsGroup, winprocsGroup, editorGroup);
	}

	public RibbonGallery getWindowsGallery() {
		return windowsGallery;
	}

	public RibbonGallery getProceduresGallery() {
		return proceduresGallery;
	}
}
