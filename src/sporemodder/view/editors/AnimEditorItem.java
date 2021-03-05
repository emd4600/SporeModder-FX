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

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sporemodder.PathManager;
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;

public class AnimEditorItem extends ProjectItem {
	
	public static final String FOLDER_NAME = "Animation Editor";
	public static final String RELATIVE_PATH = "../" + FOLDER_NAME;
	
	private static Image icon;
	
	public AnimEditorItem() {
		super(FOLDER_NAME, null);
		
		setFile(PathManager.get().getProgramFile(FOLDER_NAME));
	}
	
	@Override public Node getIcon() {
		//return null;
		if (icon == null) {
			icon = UIManager.get().loadImage("anim-icon.png");
		}
		
		ImageView imageView = new ImageView(icon);
		imageView.setFitWidth(24);
		imageView.setFitHeight(18);
		return imageView;
	}

	@Override
	public ItemEditor createEditor() {
		return new AnimEditor();
	}
	
	// Never show the item as a folder
	@Override
	public boolean isFolder() {
		return false;
	}
	
	@Override public String getRelativePath() {
		return RELATIVE_PATH;
	}
	
	// So it reacts properly to AbstractEditableEditor save
	public boolean isMod() {
		return true;
	}
	
	public boolean isRoot() {
		// This avoids exceptions when right-clicking the item
		return true;
	}
	
	public boolean canRemoveItem() {
		return false;
	}
	public boolean canModifyItem() {
		return false;
	}
	public boolean canDuplicateItem() {
		return false;
	}
	public boolean canOpenModFolder() {
		return false;
	}
	public boolean canOpenSourceFolder() {
		return false;
	}
	public boolean canCompareItem() {
		return false;
	}
	public boolean canCreateNewFile() {
		return false;
	}
	public boolean canCreateNewFolder() {
		return false;
	}
	public boolean canRenameItem() {
		return false;
	}
	public boolean canImportFiles() {
		return false;
	}
	public boolean canRefreshItem() {
		return false;
	}
}
