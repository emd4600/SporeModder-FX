package sporemodder.view.editors.spui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import emord.javafx.ribbon.Ribbon;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import sporemodder.HashManager;
import sporemodder.UIManager;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.userinterface.SwingNodeEx;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;
import sporemodder.view.editors.EditHistoryAction;
import sporemodder.view.editors.EditHistoryEditor;

public class SmSpuiEditor extends AbstractSpuiEditor implements EditHistoryEditor {
	private SwingNode viewer = new SwingNodeEx();
	//private JComponent swingEditorPane = null;
	private SPUIEditor spuiEditor = null;
	private final SPUIMain spuiMain = new SPUIMain();
	private final SwingNode swingInspectorPane = new SwingNodeEx();
	private final SwingNode swingMenuBar = new SwingNode();
	private BorderPane inspectorPane = new BorderPane();
	
	@SuppressWarnings("unchecked")
	public SmSpuiEditor(ReadOnlyBooleanWrapper isSavedWrapper, ReadOnlyBooleanWrapper isActiveWrapper) {
		super(isSavedWrapper, isActiveWrapper);
		inspectorPane.setTop(swingMenuBar);
		inspectorPane.setCenter(swingInspectorPane);
	}
	
	public SmSpuiEditor() {
		this(null, null);
	}
	
	@Override
	protected void loadFileOverride(File file) throws IOException {
		String spuiPath = file.getAbsolutePath();
		ProjectItem smfxItem = getItem();
		Action whenSpuiSavedAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				//MainApp.getCurrentProject().saveNames();
			}
		};
		try {
			spuiMain.read(new FileStreamAccessor(file, "r"));
			spuiEditor = new SPUIEditor(
					spuiMain,
					"an ancient artifact of unimaginable power, recovered from the ruins of a forgotten era", //MainApp.getCurrentProject().getProjectName() + " - " + filePath,
					spuiPath,
					file,
					false,
					smfxItem.isMod(),
					whenSpuiSavedAction,
					smfxItem
			);
			//swingEditorPane = spuiEditor.getRootPane();
			//swingEditorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			viewer.setContent(spuiEditor.getSPUIViewer()); //swingEditorPane);
			swingInspectorPane.setContent(spuiEditor.getInspectorSplitPane());
			swingMenuBar.setContent(spuiEditor.getJMenuBar());
			//inspectorPane.setTop(swingMenuBar);
			//swingMenuBar
			//getSPUIViewer()
		} catch (InvalidBlockException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		showInspector(true);
	}
	
	@Override
	public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
	
	private void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("SPUI Editor", "spui", inspectorPane);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override
	public Node getUI() {
		return viewer;
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsSearching() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsEditHistory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void saveData() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void restoreContents() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Node getInspectorPane() {
		return inspectorPane; 
	}

	@Override
	public boolean canUndo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canRedo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends EditHistoryAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getUndoRedoIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
}