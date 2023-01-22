package sporemodder.view.editors.spui;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.Node;
import sporemodder.util.ProjectItem;
import sporemodder.view.editors.AbstractEditableEditor;
import sporemodder.view.editors.EditHistoryEditor;

public abstract class AbstractSpuiEditor extends AbstractEditableEditor implements EditHistoryEditor {
	private final ReadOnlyBooleanWrapper forwardedIsSavedWrapper;
	private final ReadOnlyBooleanWrapper forwardedIsActiveWrapper;
	
	public AbstractSpuiEditor(ReadOnlyBooleanWrapper isSavedWrapper, ReadOnlyBooleanWrapper isActiveWrapper) {
		forwardedIsSavedWrapper = isSavedWrapper;
		forwardedIsActiveWrapper = isActiveWrapper;
	}
	
	@Override
	protected ReadOnlyBooleanWrapper createIsSavedWrapper() {
		if (forwardedIsSavedWrapper != null) {
			return forwardedIsSavedWrapper;
		} else {
			return super.createIsSavedWrapper();
		}
	}
	
	@Override
	protected ReadOnlyBooleanWrapper createIsActiveWrapper() {
		if (forwardedIsActiveWrapper != null) {
			return forwardedIsActiveWrapper;
		} else {
			return super.createIsActiveWrapper();
		}
	}
	
	public void spuiSaveData() throws Exception {
		this.saveData();
	}
	
	public void spuiRestoreContents() throws Exception {
		this.restoreContents();
	}
	
	
	@Override
	public void loadFile(ProjectItem item) throws IOException {
		this.item = item;
		if (item != null) {
			loadFile(item.getFile());
		}
	}

	protected abstract void loadFileOverride(File file) throws IOException;
	public void loadFile(File file) throws IOException {
		this.file = file;
		loadFileOverride(file);
	}
	
	protected abstract Node getInspectorPane();
	
	public ReadOnlyBooleanWrapper getIsSavedWrapper() {
		return isSaved;
	}
	
	public ReadOnlyBooleanWrapper getIsActiveWrapper() {
		return isActive;
	}
}
