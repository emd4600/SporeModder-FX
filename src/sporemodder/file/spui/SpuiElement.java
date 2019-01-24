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

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import sporemodder.file.LocalizedText;
import sporemodder.file.spui.uidesigner.DesignerClass;
import sporemodder.view.editors.SpuiEditor;

//We don't make it abstract so we can create it with reflection, as many components don't have defined classes
public class SpuiElement extends InspectableObject {
	
	public static final int EDITOR_TAG_PROXYID = 0x3C8A53CB;
	
	/** Maps data to the proxy ID of those properties that haven't been assigned to a field. */
	private final Map<Integer, Object> properties = new HashMap<>();
	private DesignerClass designerClass;
	private final LocalizedText editorTag = new LocalizedText();
	
	public SpuiElement() {
		designerClass = SporeUserInterface.getDesigner().getClass(getClass());
	}
	
	public void setProperty(int proxyID, Object value) {
		properties.put(proxyID, value);
	}
	
	public Object getProperty(int proxyID) {
		return properties.get(proxyID);
	}
	
	public void setDesignerClass(DesignerClass designerClass) {
		this.designerClass = designerClass;
	}
	
	public DesignerClass getDesignerClass() {
		return designerClass;
	}
	
	@Override public String toString() {
		String str = designerClass.getName();
		if (editorTag.getText() != null && !editorTag.getText().isEmpty()) {
			str += " [" + editorTag.getText() + ']';
		}
		return str;
	}
	
	public String getEditorTag() {
		return editorTag.getText();
	}
	
	public void setEditorTag(String editorTag) {
		this.editorTag.setText(editorTag);
	}

	@Override
	public Node generateUI(SpuiEditor editor) {
		return designerClass.generateUI(editor, this);
	}
	
	public boolean isRoot() {
		return false;
	}

	@Override
	public void addComponents(SpuiWriter writer) {
		designerClass.addComponents(writer, this);
	}
}
