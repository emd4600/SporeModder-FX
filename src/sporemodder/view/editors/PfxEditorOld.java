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
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sporemodder.UIManager;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.effects.EffectDirectory;
import sporemodder.file.effects.EffectUnit;
import sporemodder.util.ProjectItem;
import sporemodder.util.inspector.InspectorUnit;

public class PfxEditorOld extends ArgScriptEditorOld<EffectUnit> {

	public static class Factory implements EditorFactory {
		
		private Image icon;

		@Override
		public ItemEditor createInstance() {
			return new PfxEditorOld("editors/TextEditorUI");
		}

		@Override
		public boolean isSupportedFile(ProjectItem item) {
			return !item.isFolder() && item.getName().endsWith(".pfx");
		}
		
		@Override
		public Node getIcon(ProjectItem item) {
			if (isSupportedFile(item)) {
				if (icon == null) {
					icon = UIManager.get().loadImage("item-icon-effects.png");
				}
				
				ImageView iv = new ImageView();
				iv.setFitWidth(24);
				iv.setPreserveRatio(true);
				
				return iv;
			}
			return null;
		}
	}
	
	public PfxEditorOld(String uiPath) {
		super(uiPath, "Effect Editor Inspector");
	}
	
	@Override
	protected void generateStream() {
		EffectUnit unit = new EffectUnit(null);
		stream = unit.generateStream();
		
		// Inspectors only use the stream to parse values; we generate a new one
		// so it does not mess the real one.
		inspector = new InspectorUnit<ArgScriptStream<EffectUnit>>(new ArgScriptStream<EffectUnit>(), this);
	}
	
	@Override
	protected void inspectorSelectionChanged(TreeItem<DocumentFragment> oldValue, TreeItem<DocumentFragment> newValue) {
		
		inspector.clear();
		
		if (newValue != null) {
			EffectDirectory.buildInspector(inspector, newValue.getValue());
		}
	}
}
