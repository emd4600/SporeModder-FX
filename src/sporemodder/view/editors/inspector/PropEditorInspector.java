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
package sporemodder.view.editors.inspector;

import sporemodder.file.DocumentFragment;
import sporemodder.file.DocumentStructure;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.prop.PropertyList;
import sporemodder.view.editors.TextEditor;

public class PropEditorInspector {

	private TextEditor textEditor;
	private ArgScriptStream<PropertyList> stream;
	private DocumentStructure structrue;
	
	public PropEditorInspector(TextEditor textEditor, ArgScriptStream<PropertyList> stream) {
		this.textEditor = textEditor;
		this.stream = stream;
	}

	public TextEditor getTextEditor() {
		return textEditor;
	}

	public ArgScriptStream<PropertyList> getStream() {
		return stream;
	}
	
	// Replaces the value of a single (i.e. non-array) property
	private void replaceSplit(DocumentFragment fragment, ArgScriptLine line, String newValue, int splitIndex) {
		textEditor.eventlessEdit(codeArea -> {
			textEditor.replaceText(fragment, line.replaceSplit(fragment.getText(), newValue, splitIndex));
		});
	}
}
