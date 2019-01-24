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

import org.fxmisc.richtext.CodeArea;

import sporemodder.file.DocumentStructure.DocumentFragment;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.util.inspector.InspectorToggleButton;
import sporemodder.util.inspector.InspectorUnit;
import sporemodder.view.editors.TextEditorOld;

public class ArgScriptInspector {

	/**
	 * Checks whether the given fragment has a parent and, if it does, whether it uses the given keyword.
	 * This can be used to check if a certain fragment belongs to a particle effect, etc
	 * @param fragment
	 * @param keyword
	 * @return
	 */
	public static boolean checkParent(DocumentFragment fragment, String keyword) {
		return fragment.parent != null && fragment.parent.description.split("\\s")[0].equalsIgnoreCase(keyword);
	}
	
	/**
	 * Checks whether the given fragment uses this keyword.
	 * @param fragment
	 * @param keyword
	 * @return
	 */
	public static boolean checkKeyword(DocumentFragment fragment, String keyword) {
		return fragment.description.split("\\s")[0].equalsIgnoreCase(keyword);
	}
	
	/**
	 * Checks whether any of the children fragments use the given keyword.
	 * @param blockFragment
	 * @param keyword
	 * @return
	 */
	public static boolean checkProperty(DocumentFragment blockFragment, String keyword) {
		for (DocumentFragment fragment : blockFragment.children) {
			if (fragment.description.split("\\s")[0].equalsIgnoreCase(keyword)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the keyword from the description of the given fragment, which is expected to follow ArgScript specifications.
	 * @param fragment
	 * @return
	 */
	public static String getKeyword(DocumentFragment fragment) {
		return fragment.description.split("\\s")[0];
	}
	
	/**
	 * Gets a property from the given fragment; that is, returns the first children fragment that uses the specified keyword.
	 * @param blockFragment
	 * @param keyword
	 * @return
	 */
	public static DocumentFragment getProperty(DocumentFragment blockFragment, String keyword) {
		for (DocumentFragment fragment : blockFragment.children) {
			if (fragment.description.split("\\s")[0].equalsIgnoreCase(keyword)) {
				return fragment;
			}
		}
		return null;
	}
	
	/**
	 * Inserts the given line into the specified fragment, editing the text in the inspector's text editor.
	 * The line will be added after the last children fragment, or at the editPosition of the fragment.
	 * @param inspector
	 * @param fragment
	 * @param line
	 */
	public static <T> DocumentFragment insertLine(InspectorUnit<T> inspector, DocumentFragment fragment, String line) {
		inspector.setWriting(true);
		
		TextEditorOld editor = inspector.getTextEditor();
		CodeArea codeArea = editor.getCodeArea();
		DocumentFragment insertedFragment = new DocumentFragment(fragment.structure, fragment);
		
		int position = fragment.editPosition;
		if (!fragment.children.isEmpty()) position = fragment.children.get(fragment.children.size() - 1).end;
		
		codeArea.insertText(position, "\n");
		
		codeArea.moveTo(position + 1);
		insertedFragment.start = position;
		insertedFragment.description = line.split("\\s")[0];
		
		StringBuilder sb = new StringBuilder();
		
		// Add as many tabulations as necessary
		// Even non-tabulated lines have a parent, so start with one
		DocumentFragment parent = insertedFragment.parent;
		while ((parent = parent.parent) != null) {
			sb.append('\t');
		}
		
		sb.append(line);
		
		line = sb.toString();
		
		insertedFragment.end = codeArea.getCaretPosition() + line.length();
		
		codeArea.insertText(codeArea.getCaretPosition(), line);
		
		
		inspector.insertFragment(insertedFragment, fragment);
		
		// We don't want the new line to be included
		insertedFragment.start = position + 1;
		
		inspector.setWriting(false);
		
		return insertedFragment;
	}
	
	/**
	 * Removes the contents of the fragment and the new-line sign (if any) that follows it.
	 * @param inspector
	 * @param fragment
	 */
	public static <T> void removeLine(InspectorUnit<T> inspector, DocumentFragment fragment) {
		inspector.setWriting(true);
		
		TextEditorOld editor = inspector.getTextEditor();
		CodeArea codeArea = editor.getCodeArea();
		
		if (fragment.end + 1 <= codeArea.getLength() && codeArea.getText().charAt(fragment.end) == '\n') {
			// Delete the new line sign as well
			// We fool the fragment to make it think it included the new line sign as well
			fragment.end++;
		}
		
		codeArea.deleteText(fragment.start, fragment.end);
		
		inspector.removeFragment(fragment);
		
		inspector.setWriting(false);
	}
	
	/**
	 * Creates a new toggle button in the inspector that is used to generate/remove a line of content (a.k.a property) inside the given block fragment.
	 * @param inspector
	 * @param blockFragment The document fragment of the ArgScript block that will contain the new line. 
	 * The line will be appended at the end of the block (before the 'end' keyword).
	 * @param buttonText The text shown in the button.
	 * @param text The text inserted when the button is enabled.
	 * @param keywords All the accepted keywords that document lines can have to represent this button.
	 * @return The toggle button created, which can be added to the inspector.
	 */
	public static <T> InspectorToggleButton createPropertyButton(InspectorUnit<T> inspector, DocumentFragment blockFragment, String buttonText, String text, String ... keywords) {
		
		boolean isEnabled = false;
		
		for (String keyword : keywords) {
			isEnabled = ArgScriptInspector.checkProperty(blockFragment, keyword);
			if (isEnabled) {
				break;
			}
		}
		
		return new InspectorToggleButton(buttonText, isEnabled, (obs, oldValue, newValue) -> {
			if (newValue) {
				ArgScriptInspector.insertLine(inspector, blockFragment, text);
				inspector.getTextEditor().applySyntaxHighlighting();
			}
			else {
				DocumentFragment f = null;
				for (String keyword : keywords) {
					f = ArgScriptInspector.getProperty(blockFragment, keyword);
					if (f != null) {
						break;
					}
				}
				if (f != null) {
					ArgScriptInspector.removeLine(inspector, f);
					inspector.getTextEditor().applySyntaxHighlighting();
				}
			}
		},
		(event) -> {
			DocumentFragment f = null;
			for (String keyword : keywords) {
				f = ArgScriptInspector.getProperty(blockFragment, keyword);
				if (f != null) {
					break;
				}
			}
			if (f != null) inspector.setSelectedFragment(f);
		});
	}
	
	
	public static <T> ArgScriptLine toLine(InspectorUnit<T> inspector, DocumentFragment fragment) {
		ArgScriptStream<Object> stream = new ArgScriptStream<Object>();
		stream.setData(new Object());
		
		return stream.toLine(inspector.getText(fragment));
	}
}
