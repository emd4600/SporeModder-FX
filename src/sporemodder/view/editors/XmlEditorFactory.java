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
import sporemodder.UIManager;
import sporemodder.util.ProjectItem;

public class XmlEditorFactory implements EditorFactory {
		
	private Image xmlIcon;

	@Override
	public ItemEditor createInstance() {
		return new TextEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && (
				item.getName().endsWith(".xml") |
				item.getName().endsWith(".crt") |
				item.getName().endsWith(".cll") |
				item.getName().endsWith(".flr") |
				item.getName().endsWith(".bld") |
				item.getName().endsWith(".vcl") |
				item.getName().endsWith(".ufo") |
				item.getName().endsWith(".eapdPixie"));
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		if (xmlIcon == null) {
			xmlIcon = UIManager.get().loadImage("item-icon-xml.png");
		}
		
		if (item.getName().endsWith(".prop.xml")) {
			ImageView imageView = new ImageView(xmlIcon);
			imageView.setFitWidth(24);
			imageView.setFitHeight(18);
			return imageView;
		}
		else {
			return null;
		}
	}
}
