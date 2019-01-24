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
import sporemodder.DocumentationManager;
import sporemodder.file.DocumentFragment;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.prop.PropertyDouble;
import sporemodder.file.prop.PropertyFloat;
import sporemodder.file.prop.PropertyInt16;
import sporemodder.file.prop.PropertyInt32;
import sporemodder.file.prop.PropertyInt64;
import sporemodder.file.prop.PropertyInt8;
import sporemodder.file.prop.PropertyList;
import sporemodder.file.prop.PropertyUInt16;
import sporemodder.file.prop.PropertyUInt32;
import sporemodder.file.prop.PropertyUInt64;
import sporemodder.file.prop.PropertyUInt8;
import sporemodder.util.ProjectItem;
import sporemodder.util.inspector.InspectorUnit;
import sporemodder.view.inspector2.InspectorDouble;
import sporemodder.view.inspector2.InspectorList;
import sporemodder.view.inspector2.InspectorLong;
import sporemodder.view.inspector2.InspectorValue;

public class PropEditorOld extends ArgScriptEditorOld<PropertyList> {

	public static class Factory implements EditorFactory {

		@Override
		public ItemEditor createInstance() {
			return new PropEditorOld("editors/TextEditorUI");
		}

		@Override
		public boolean isSupportedFile(ProjectItem item) {
			return !item.isFolder() && item.getName().endsWith(".prop_t");
		}
		
		@Override
		public Node getIcon(ProjectItem item) {
			return null;
		}
	}
	
	public PropEditorOld(String uiPath) {
		super(uiPath, "Property Editor Inspector");
		
		tooltipFactories.add((text, event) -> {
			int charIndex = event.getCharacterIndex();
			if (charIndex != -1) {

				String word = ArgScriptStream.scanWord(text, charIndex);
				
				return DocumentationManager.get().getDocumentation("properties", word);
			}
			else {
				return null;
			}
		});
	}
	
	@Override
	protected void generateStream() {
		PropertyList list = new PropertyList();
		stream = list.generateStream();
		stream.setLineHighlighter((syntax, line, lineNumber, isBlock) -> {
			line.addSyntaxForKeyword(syntax, lineNumber, isBlock);
			// Avoid doing it for the 'end' command
			if (line.hasKeyword() && line.getArgumentCount() >= 1) {
				line.addSyntaxForWord(syntax, lineNumber, 1, "propeditor-property");
			}
			line.addOptionsSyntax(syntax, lineNumber);
		});
		stream.setStructureNameFactory((fragment, text, line) -> {
			if (line == null) return text;
			
			return line.getSplits().get(1);
		});
		
		// Inspectors only use the stream to parse values; we generate a new one
		// so it does not mess the real one.
		inspector = new InspectorUnit<ArgScriptStream<PropertyList>>(new ArgScriptStream<PropertyList>(), this);
	}
	
	@Override
	protected void inspectorSelectionChanged(TreeItem<DocumentFragment> oldValue, TreeItem<DocumentFragment> newValue) {
		
		inspector.clear();
		
		if (newValue != null) {
			Node node = inspectFragment(newValue.getValue());
			
			if (node != null) {
				inspector.add(node);
			}
		}
	}
	
	private boolean insideTextEvent;
	
	// Replaces the value of a single (i.e. non-array) property
	private void replaceSingleValue(DocumentFragment fragment, String originalText, ArgScriptLine line, String newValue, int splitIndex) {
		// We won't replace the text if the text was already changed by the user!
		if (!insideTextEvent) {
			
			inspector.setWriting(true);
			inspector.replaceText(fragment, line.replaceSplit(originalText, newValue, splitIndex));
			inspector.setWriting(false);
			
			applySyntaxHighlighting();
		}
	}
	
	private void replaceSingleNumberValue(DocumentFragment fragment, String originalText, ArgScriptLine line, String newValue, int splitIndex) {
		if (newValue.contains(" ") || newValue.contains("\t")) {
			newValue = "(" + newValue + ")";
		}
		replaceSingleValue(fragment, originalText, line, newValue, splitIndex);
	}
	
	private InspectorValue<?> inspectFragmentSingle(String keyword, DocumentFragment fragment, String text, ArgScriptLine line, int splitIndex) {
		if (line.getSplits().size() <= splitIndex) {
			return null;
		}
		
		switch (keyword) {
		case PropertyFloat.KEYWORD:
		case PropertyDouble.KEYWORD:
			{
				InspectorDouble node = new InspectorDouble();
				node.setValue(line.getSplits().get(splitIndex));
				
				node.valueProperty().addListener((obs, oldValue, newValue) -> {
					replaceSingleNumberValue(fragment, text, line, node.getValueFactory().getConverter().toString(newValue), splitIndex);
				});
				
				return node;
			}
		case PropertyInt32.KEYWORD:
			{
				InspectorLong node = new InspectorLong();
				node.setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
				node.setValue(line.getSplits().get(splitIndex));
				
				node.valueProperty().addListener((obs, oldValue, newValue) -> {
					replaceSingleNumberValue(fragment, text, line, node.getValueFactory().getConverter().toString(newValue), splitIndex);
				});
				
				return node;
			}
		default:
			return null;
		}
	}
	
	private void insertValue(DocumentFragment parent, int index, String keyword) {
		String text = null;
		
		switch (keyword) {
		case PropertyInt8.KEYWORD:
		case PropertyInt16.KEYWORD:
		case PropertyInt32.KEYWORD:
		case PropertyInt64.KEYWORD:
		case PropertyUInt8.KEYWORD:
		case PropertyUInt16.KEYWORD:
		case PropertyUInt32.KEYWORD:
		case PropertyUInt64.KEYWORD:
		case PropertyFloat.KEYWORD:
		case PropertyDouble.KEYWORD:
			text = "0";
			break;
		}
		
		int position = 0;
		if (parent.getChildren().isEmpty()) {
			position = parent.getEditPosition();
		}
		else {
			position = parent.getChildren().get(index).getStart();
		}
		
		inspector.setWriting(true);
		getCodeArea().insertText(position, "\t" + text + "\n");
		inspector.setWriting(false);
		
		applySyntaxHighlighting();
	}
	
	private Node inspectFragment(final DocumentFragment fragment) {
		// First try to get the edit text, in case it's a block
		String text = fragment.getBeforeEditText();
		if (text == null) {
			text = fragment.getText();
		}
		ArgScriptLine line = stream.generateLine(text);
		
		if (stream.getParser(line.getKeyword()) == null) {
			// There is no parser available, so it's probably the value of an array
			DocumentFragment parent = fragment.getParent();
			if (parent != null) {
				return inspectFragment(parent);
			} else {
				return null;
			}
		}
		
		String keyword = line.getKeyword();
		if (keyword.endsWith("s")) {
			// An array
			
			// Remove the "s" from the keyword
			final String singleKeyword = keyword.substring(0, keyword.length()-1);
			
			InspectorList list = new InspectorList();
			
			list.setValueFactory(createIndex -> {
				insertValue(fragment, createIndex, singleKeyword);
				
				return null;
			});
			
			for (DocumentFragment child : fragment.getChildren()) {
				String childText = child.getText();
				InspectorValue<?> node = inspectFragmentSingle(singleKeyword, child, childText, stream.generateLine(childText), 0);
				if (node == null) {
					return null;
				}
				else {
					list.getValues().add(node);
				}
			}
			
			return list;
		}
		else {
			// A single value
			// The split index is 2 because its 'type propertyName value'
			InspectorValue<?> node = inspectFragmentSingle(keyword, fragment, text, line, 2);
			return node == null ? null : node.getNode();
		}
	}
}
