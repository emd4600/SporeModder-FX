package sporemodder.view.editors.spui;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.Node;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;
import sporemodder.view.editors.AbstractEditableEditor;
import sporemodder.view.editors.EditHistoryAction;
import sporemodder.view.editors.EditHistoryEditor;
import sporemodder.view.editors.SpuiEditor;

public class OldIsNewSpuiEditor extends AbstractEditableEditor implements EditHistoryEditor {
	private SpuiEditor smfxSpuiEditor = null;
	private SmSpuiEditor smSpuiEditor = null;
	private boolean useSmSpuiEditor = false;
	
	private AbstractSpuiEditor getCurrent() {
		if (useSmSpuiEditor) {
			if (smSpuiEditor == null) {
				smSpuiEditor = new SmSpuiEditor(this.isSaved, this.isActive);
			}
			return smSpuiEditor;
		} else {
			if (smfxSpuiEditor == null) {
				smfxSpuiEditor = new SpuiEditor(this.isSaved, this.isActive);
			}
			return smfxSpuiEditor;
		}
	}
	
	@Override
	protected void initIsSavedListener() {
		
	}
	@Override
	protected void initIsActiveListener() {
		
	}
	
	/*@Override
	protected ReadOnlyBooleanWrapper createReadOnlyBooleanWrapper(String name, boolean initialValue) {
		AbstractSpuiEditor current = getCurrent();
		ReadOnlyBooleanWrapper isActiveWrapper = current.getIsActiveWrapper();
		if (name.equals(isActiveWrapper.getName())) {
			return isActiveWrapper;
		} else {
			ReadOnlyBooleanWrapper isSavedWrapper = current.getIsSavedWrapper();
			if (name.equals(isSavedWrapper.getName())) {
				return isSavedWrapper;
			}
		}
		
		return new ReadOnlyBooleanWrapper(current, name, initialValue);
	}*/
	
	@Override
	public void loadFile(ProjectItem item) throws IOException {
		getCurrent().loadFile(item);
		showInspector(true);
	}

	@Override
	public Node getUI() {
		return getCurrent().getUI();
	}

	@Override
	public boolean isEditable() {
		return getCurrent().isEditable();
	}

	@Override
	public boolean supportsSearching() {
		return getCurrent().supportsSearching();
	}

	@Override
	public boolean supportsEditHistory() {
		return getCurrent().supportsEditHistory();
	}

	@Override
	protected void saveData() throws Exception {
		getCurrent().spuiSaveData();
	}

	@Override
	protected void restoreContents() throws Exception {
		getCurrent().spuiRestoreContents();
	}
	
	
	@Override
	public void setActive(boolean isActive) {
		getCurrent().setActive(isActive);
		showInspector(isActive);
	}
	
	private void showInspector(boolean show) {
		Node inspectorContent = getCurrent().getInspectorPane();
		if (show && (inspectorContent != null)) {
			UserInterface.get().getInspectorPane().configureDefault("SPUI Editor", "spui", inspectorContent);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override
	public boolean isAutosaveEnabled() {
		return getCurrent().isAutosaveEnabled();
	}
	
	@Override
	public void setAutosaveEnabled(boolean isAutosaveEnabled) {
		getCurrent().setAutosaveEnabled(isAutosaveEnabled);
	}
	
	/*public final boolean isActive() {
	public void setActive(boolean value) {
	public File getFile() {
	public ProjectItem getItem() {
	public final boolean isSaved() {
	protected void setIsSaved(boolean isSaved) {
	private void saveInternal() {
	public void save() {
	@Override public void setDestinationFile(File file) {
	protected abstract void saveData() throws Exception;
	protected abstract void restoreContents() throws Exception;*/

	@Override
	public boolean canUndo() {
		return getCurrent().canUndo();
	}

	@Override
	public boolean canRedo() {
		return getCurrent().canRedo();
	}

	@Override
	public void undo() {
		getCurrent().undo();
	}

	@Override
	public void redo() {
		getCurrent().redo();
	}

	@Override
	public List<? extends EditHistoryAction> getActions() {
		return getCurrent().getActions();
	}

	@Override
	public int getUndoRedoIndex() {
		return getCurrent().getUndoRedoIndex();
	}
}
