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
package sporemodder.file.argscript.inspector;

import java.util.ArrayList;
import java.util.List;

import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import sporemodder.file.DocumentStructure.DocumentFragment;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.util.inspector.InspectorUnit;

/**
 * A class used to display properties in the inspector that correspond to an ArgScript line.
 */
public class ASLineInspector<T> {
	
	private final List<ASValueInspector> values = new ArrayList<ASValueInspector>();
	private final List<ASAbstractOptionInspector> options = new ArrayList<ASAbstractOptionInspector>();

	/** The multiple fragments that make the line. */
	private final List<String> splits = new ArrayList<String>();
	
	private int optionalIndex = -1;
	
	private ArgScriptLine line;
	private final ArgScriptArguments args = new ArgScriptArguments();
	
	private InspectorUnit<ArgScriptStream<T>> inspector;
	private DocumentFragment fragment;
	
	public ASLineInspector(InspectorUnit<ArgScriptStream<T>> inspector, DocumentFragment fragment) {
		this.inspector = inspector;
		this.fragment = fragment;
		this.line = new ArgScriptLine(inspector.getData());
		
		fromLine(inspector.getText(fragment));
		updateArguments();
	}
	
	public ASLineInspector(InspectorUnit<ArgScriptStream<T>> inspector, DocumentFragment fragment, String text) {
		this.inspector = inspector;
		this.fragment = fragment;
		this.line = new ArgScriptLine(inspector.getData());
		
		fromLine(text);
		updateArguments();
	}
	
	public ASLineInspector<T> setOptionalIndex(int optionalIndex) {
		this.optionalIndex = optionalIndex;
		for (int i = optionalIndex; i < values.size(); i++) {
			values.get(i).setOptional(true);
		}
		return this;
	}
	
	public InspectorUnit<ArgScriptStream<T>> getInspector() {
		return inspector;
	}
	
	public void add(ASValueInspector valueInspector) {
		// Calculate the arg index
		int argIndex = 0;
		for (ASValueInspector value : values) {
			
			int argCount = value.getArgumentCount();
			if (argCount == -1) {
				throw new UnsupportedOperationException("Cannot add a value inspector after an indefinite nubmer of arguments.");
			}
			argIndex += argCount;
		}
		
		if (optionalIndex != -1 && values.size() >= optionalIndex) {
			valueInspector.setOptional(true);
		}
		
		valueInspector.setLineInspector(this, null);
		valueInspector.setArgIndex(argIndex);
		values.add(valueInspector);
		
		valueInspector.generateUI(inspector.getVBox());
	}
	
	public void add(ASAbstractOptionInspector option) {
		option.setLineInspector(this, true);
		options.add(option);
		
		option.generateUI(inspector.getVBox());
	}
	
	public ArgScriptStream<?> getStream() {
		return inspector.getData();
	}
	
	private void fromLine(String text) {
		line.fromLine(text, null);
		
		splits.clear();
		splits.addAll(line.getSplits());
	}
	
	public void updateArguments() {
		line.getArguments(args, 0, Integer.MAX_VALUE);
		
		// Update sub-options as well, otherwise arguments will be using sublists that do not exist anymore
		for (ASAbstractOptionInspector optionInspector : options) {
			optionInspector.updateArguments();
		}
		
		if (optionalIndex != -1) {
			boolean isDefault = true;
			int removeCount = 0;
			for (int i = optionalIndex; i < values.size(); i++) {
				removeCount += values.get(i).getRemoveableCount();
				if (!values.get(i).isDefault()) {
					isDefault = false;
					break;
				}
			}
			
			if (isDefault) {
				int removeIndex = 0;
				// Add the count of the non-optional values
				for (int i = 0; i < optionalIndex; i++) {
					removeIndex += values.get(i).getArgumentCount();
				}
				
				// Some values might have returned default, but just because they don't exist
				removeCount = Math.min(removeIndex + removeCount, args.get().size()) - removeIndex;
				
				removeIndex += args.getSplitIndex();
				
				if (removeCount > 0) {
					for (int i = 0; i < removeCount; i++) {
						splits.remove(removeIndex);
					}
					
					submitText();
				}
			}
		}
	}
	
	public ArgScriptLine getLine() {
		return line;
	}
	
	public ArgScriptArguments getArguments() {
		return args;
	}
	
	public void submitText() {
		submitTextWithoutUpdating();
		updateArguments();
	}
	
	public int createIfNecessary(ASValueInspector caller) {
		if (caller.isOptional) {
			createOptionalIfNecessary(caller.argIndex);
		}
		
		// It always starts at position 1 (after the keyword)
		return 1;
	}
	
	private void createOptionalIfNecessary(int argIndex) {
		if (args.getSize() <= argIndex) {
			for (int i = args.getSize(); i <= argIndex; i++) {
				List<String> newSplits = new ArrayList<String>();
				values.get(i).addDefaultValue(newSplits);
				
				for (int j = 0; j < newSplits.size(); j++) {
					splits.add(args.getSplitIndex() + i + j, newSplits.get(j));
				}
			}
		}
	}
	
	public void submitTextWithoutUpdating() {
		
		StringBuilder sb = new StringBuilder();
		
		// Add as many tabulations as necessary
		// Even non-tabulated lines have a parent, so start with one
		DocumentFragment parent = fragment.parent;
		while ((parent = parent.parent) != null) {
			sb.append('\t');
		}

		for (int i = 0; i < splits.size(); i++) {
			
			// A bit of a wild guess to include () or ""; should work most of the times
			if (splits.get(i).contains(",")) {
				sb.append('(');
				sb.append(splits.get(i));
				sb.append(')');
			}
			else if (splits.get(i).contains(" ")) {
				sb.append('"');
				sb.append(splits.get(i));
				sb.append('"');
			}
			else {
				sb.append(splits.get(i));
			}
			
			if (i + 1 < splits.size()) {
				sb.append(' ');
			}
		}
		
		inspector.setWriting(true);
		
		inspector.replaceText(fragment, sb.toString());
		
		// If it has no other parent, use the entire line
		if (parent == fragment.parent) {
			fragment.description = sb.toString();
		}
		else {
			fragment.description = splits.get(0);
		}
		inspector.updateStructureItem(fragment);
		
		// Now parse the line again
		fromLine(inspector.getText(fragment));
		
		inspector.setWriting(false);
		
		// Replace syntax highlighting
		inspector.getTextEditor().applySyntaxHighlighting();
	}
	
	public List<String> getSplits() {
		return splits;
	}
	
}
