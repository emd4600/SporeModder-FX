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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.Control;
import sporemodder.EditorManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;

/**
 * The base class of all editors that can be saved. This manages user interaction when saving in this way:
 * <li>If the file is part of the mod, it saves it directly.
 * <li>If the file does not belong to the mod, it asks the user if it wants to include it, and then saves it.
 */
public abstract class AbstractEditableEditor extends Control implements ItemEditor {
	
	protected File file;
	protected ProjectItem item;
	
	/** If any unsaved changes have been done, this value is false. */
	protected final ReadOnlyBooleanWrapper isSaved = createIsSavedWrapper();
	protected ReadOnlyBooleanWrapper createIsSavedWrapper() {
		return new ReadOnlyBooleanWrapper(this, "isSaved", true);
	}
	
	/** For threads that depend on this text editor, whether it is still being used by the user or not. */
	protected final ReadOnlyBooleanWrapper isActive = createIsActiveWrapper();
	protected ReadOnlyBooleanWrapper createIsActiveWrapper() {
		return new ReadOnlyBooleanWrapper(this, "isActive", true);
	}
	
	/** If true, the file will be saved when setActive(null) is called. */
	private boolean isAutosaveEnabled = true;
	
	/** If true, the contents of the file must be reloaded when the editor is set active again. */
	private boolean mustRestoreContents;
	
	public AbstractEditableEditor() {
		initIsSavedListener();
		initIsActiveListener();
	}
	protected void initIsSavedListener() {
		isSavedProperty().addListener((obs, oldValue, isSaved) -> {
			if (item != null) {
				if (!isSaved) {
					EditorManager.get().setTitle(this, "*" + item.getName());
				}
				else {
					EditorManager.get().setTitle(this, item.getName());
				}
			}
		});
	}
	protected void initIsActiveListener() {
		isActiveProperty().addListener((obs, oldValue, isActive) -> {
			if (item != null) {
				if (!isActive && !isSaved()) {
					// Ask the user if he wants to include the file into the mod;
					// in that case, save the text to the mod file, not the source one we are editing!
					boolean saveAsMod = item.isMod() ? ProjectManager.get().showSaveDialog() : ProjectManager.get().showSaveAsModDialog();
					
					if (saveAsMod) {
						// This will include it into the mod if necessary
						save();
					}
					else {
						// The user didn't want to save the changes, so restore the original contents
						mustRestoreContents = true;
						setIsSaved(true);
					}
				}
			}
			else if (!isActive && isAutosaveEnabled() && EditorManager.get().isAutosaveEnabled()) {
				// Using save() will include the file, which we don't want
				saveInternal();
			}
			
			if (isActive && mustRestoreContents) {
				UIManager.get().tryAction(() -> {
					restoreContents();
				}, "Could not restore contents.");
				mustRestoreContents = false;
			}
		});
	}
	
	
	
	public boolean isAutosaveEnabled() {
		return isAutosaveEnabled;
	}

	public void setAutosaveEnabled(boolean isAutosaveEnabled) {
		this.isAutosaveEnabled = isAutosaveEnabled;
	}

	/**
	 * The property that controls whether the editor is still being used by the user or not.
	 * @return
	 */
	public final ReadOnlyBooleanProperty isActiveProperty() {
		return isActive.getReadOnlyProperty();
	}
	
	public final boolean isActive() {
		return isActive.get();
	}
	
	@Override
	public void setActive(boolean value) {
		isActive.set(value);
	}
	
	/** 
	 * Returns the file that is being edited.
	 * @return
	 */
	public File getFile() {
		return file;
	}
	
	/** 
	 * Returns the project item that is being edited, might be null if it's editing file directly.
	 * @return
	 */
	public ProjectItem getItem() {
		return item;
	}
	
	/**
	 * The property that controls whether the file is saved. If any changes have been done to the text since the last save, this value is false.
	 * @return
	 */
	public final ReadOnlyBooleanProperty isSavedProperty() {
		return isSaved.getReadOnlyProperty();
	}
	
	public final boolean isSaved() {
		return isSaved.get();
	}
	
	protected void setIsSaved(boolean isSaved) {
		this.isSaved.set(isSaved);
	}
	
	private void saveInternal() {
		if (!isSaved.get() && file != null && isEditable()) {
			UIManager.get().tryAction(() -> {
				saveData();
			}, "Cannot save file.");
		}
	}
	
	@Override
	public void save() {
		if (!isSaved.get() && item != null && !item.isMod()) {
			UIManager.get().tryAction(() -> {
				if (item.modifyItem()) {
					file = item.getFile();
					saveInternal();
				}
			}, "Cannot include file into mod project.");
		} else {
			saveInternal();
		}
	}
	
	@Override public void setDestinationFile(File file) {
		this.file = file;
	}
	
	/**
	 * Called when the file is saved, only when it has passed all user checks and if it needs to be saved.
	 * This method must write the data into the file.
	 * @param file
	 * @throws IOException 
	 */
	protected abstract void saveData() throws Exception;
	
	/**
	 * Called when the user switches tab and decides not to save the contents.
	 */
	protected abstract void restoreContents() throws Exception;
}
