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
package sporemodder.file.spui;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import sporemodder.view.editors.SpuiEditor;

public abstract class InspectableObject {
	protected TreeItem<?> treeItem;
	
	public abstract Node generateUI(SpuiEditor editor);

	public TreeItem<?> getTreeItem() {
		return treeItem;
	}

	public void setTreeItem(TreeItem<?> treeItem) {
		this.treeItem = treeItem;
	}

	public abstract void addComponents(SpuiWriter writer); 
}
