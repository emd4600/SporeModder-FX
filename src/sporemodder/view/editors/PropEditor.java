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

import org.fxmisc.richtext.CodeArea;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import sporemodder.DocumentationManager;
import sporemodder.HashManager;
import sporemodder.file.TextUtils;
import sporemodder.file.argscript.ArgScriptStream.HyperlinkData;
import sporemodder.file.prop.PropertyList;
import sporemodder.view.UserInterface;

public class PropEditor extends ArgScriptEditor<PropertyList> {
	
	private static final int AUTOCOMPLETE_WIDTH = 400;
	private static final int AUTOCOMPLETE_HEIGHT = 24 * 6;
	
	private final Popup autocompletePopup = new Popup();
	private final ListView<Object> autocompleteList = new ListView<Object>(); 
	
	private int currentWordStart = -1;
	private int currentWordEnd = -1;
	private boolean caretMouseEvent = false;
	
	public PropEditor() {
		super();
		
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
			if (line == null || line.getSplits().size() < 2) return text;
			
			return line.getSplits().get(1);
		});
		
		getTooltipFactories().add((text, event) -> {
			int charIndex = event.getCharacterIndex();
			if (charIndex != -1) {

				String word = TextUtils.scanWord(text, charIndex);
				
				if (word != null) return DocumentationManager.get().getDocumentation("properties", word);
			}
			return null;
		});
		
		
		autocompletePopup.getContent().add(autocompleteList);
		autocompletePopup.setHideOnEscape(true);
		autocompletePopup.setAutoFix(false);  // We do it manually so it doesn't stay over the text
		autocompletePopup.setAutoHide(true);
		
		autocompleteList.setPrefHeight(AUTOCOMPLETE_HEIGHT);
		autocompleteList.setPrefWidth(AUTOCOMPLETE_WIDTH);
		autocompleteList.setCellFactory(listView -> new ListCell<Object>() {
			@Override protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setTooltip(null);
				} else {
					setText((String) item);
					
					String documentation = DocumentationManager.get().getDocumentation("properties", (String) item);
					setTooltip(documentation == null ? null : new Tooltip(documentation));
				}
			}
		});
		autocompleteList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				applyAutocomplete((String) newValue);
				hideAutocomplete();
			}
		});
		
		final CodeArea codeArea = getCodeArea();
		
		codeArea.setOnMousePressed(event -> {
			caretMouseEvent = true;
		});
		
		codeArea.caretPositionProperty().addListener((obs, oldValue, newValue) -> {
			// The caret also moves when typing, but we don't want to close the popup there
			if (caretMouseEvent && autocompletePopup.isShowing()) {
				if (newValue > currentWordEnd || newValue < currentWordStart) {
					hideAutocomplete();
				}
			}
			caretMouseEvent = false;
		});
		
		getCodeArea().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
			if (event.getCode() == KeyCode.ESCAPE && autocompletePopup.isShowing()) {
				hideAutocomplete();
			}
		});
		
		getCodeArea().addEventHandler(KeyEvent.KEY_TYPED, event -> {
			
			boolean hide = true;
			char c = event.getCharacter().charAt(0);
			
			// We don't want to show it when user is using Ctrl+C, Ctrl+V, etc
			if (event.isControlDown()) return;
			
			if (!Character.isWhitespace(c) && !TextUtils.isNewLine(event.getCharacter(), 0)) {
				
				String text = codeArea.getText();
				int charIndex = codeArea.getCaretPosition();
				
				currentWordStart = TextUtils.scanWordStart(text, charIndex-1);
				currentWordEnd = TextUtils.scanWordEnd(text, charIndex);
				String word = TextUtils.scanWord(text, currentWordStart, currentWordEnd);
				
				if (word != null && !word.isEmpty()) {
					final String lowercase = word.toLowerCase();
					Object[] matches = HashManager.get().getPropRegistry().getNames().stream().filter(name -> name.toLowerCase().startsWith(lowercase)).toArray();
					
					if (matches.length > 0) {
						autocompleteList.getItems().setAll(matches);
						
						if (!autocompletePopup.isShowing()) {
							// Always show the popup at the beginning so it doesn't move
							Bounds bounds = codeArea.getCharacterBoundsOnScreen(currentWordStart, currentWordStart+1).orElse(new BoundingBox(0, 0, 0, 0));
							Bounds editorBounds = codeArea.localToScreen(codeArea.getBoundsInLocal());
							
							double x = bounds.getMinX();
							double y = bounds.getMaxY();
							
							// Ensure list fits horizontally
							if (x + AUTOCOMPLETE_WIDTH > editorBounds.getMaxX()) {
								x = editorBounds.getMaxX() - AUTOCOMPLETE_WIDTH;
							}
							
							// Display above the text if the list may be outside the editor
							if (y + AUTOCOMPLETE_HEIGHT > editorBounds.getMaxY()) {
								y = bounds.getMinY() - AUTOCOMPLETE_HEIGHT;
							}
							
							autocompletePopup.show(codeArea, x, y);
						}
						
						hide = false;
					}
				}
			}
			
			if (hide) {
				hideAutocomplete();
			}
		});
	}
	
	@Override protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Property List (.prop)", "prop", null);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
	
	private void hideAutocomplete() {
		autocompletePopup.hide();
		currentWordStart = -1;
		currentWordEnd = -1;
	}
	
	private static String getDocumentationType(String documentation) {
		if (documentation.charAt(0) == '[') {
			int indexOf = documentation.indexOf("]");
			if (indexOf != -1) {
				return documentation.substring(1, indexOf);
			}
		}
		return "";  // never null, documentation should always include type, so this only happens if documentation is wrong
	}
	
	private static String getDocumentationDefault(String documentation, String type) {
		int index = 2 + type.length();
		if (index < documentation.length() && documentation.charAt(index) == '[') {
			int indexOf = documentation.indexOf("]", index);
			if (indexOf != -1) {
				return documentation.substring(index + 1, indexOf);
			}
		}
		return null;
	}
	
	private void applyAutocomplete(String selection) {
		String documentation = DocumentationManager.get().getDocumentation("properties", selection);
		
		int replaceStart = currentWordStart;
		int replaceEnd = currentWordEnd;
		String replaceString = selection;
		
		String type = null;
		
		if (documentation != null && !documentation.isEmpty()) {
			
			// We have to check whether this is the first word in the line or not
			String text = getCodeArea().getText();
			int lineStart = TextUtils.scanLineStart(text, currentWordStart);
			int lineEnd = TextUtils.scanLineEnd(text, currentWordStart);
			int previousEnd = TextUtils.scanPreviousWordEnd(text, currentWordStart);
			int nextStart = TextUtils.scanNextWordStart(text, currentWordEnd);
			
			// Find the type and default value from the documentation
			type = getDocumentationType(documentation);
			String defaultValue = getDocumentationDefault(documentation, type);
			
			// Should we add the type? Only if there are no previous words
			if (previousEnd == -1 || previousEnd <= lineStart) {
				replaceStart = lineStart;
				replaceString = type + ' ' + selection;
				currentWordStart += type.length() + 1;
			}
			else {
				// We still want to know the type, in this case the existing one
				type = TextUtils.scanWord(text, previousEnd-1);
				if (type == null) type = "";
			}
			
			
			if (type.endsWith("s")) {
				// Array properties
				replaceString = replaceString + "\n\t\nend";
				// Do not change replaceEnd
			}
			else {
				// Single-value properties: we add the value in the same line
				// Should we add the default value? Only if there are no following words
				if (defaultValue != null && (nextStart == -1 || nextStart >= lineEnd)) {
					replaceEnd = lineEnd;
					// Already contains the type (maybe) and the property name
					replaceString = replaceString + ' ' + defaultValue;
				}
			}
		}
		
		// We will need this later to select the value or find the type
		currentWordEnd = currentWordStart + selection.length();
		
		getCodeArea().replaceText(replaceStart, replaceEnd, replaceString);

		if (type == null) {
			// Try to get existing type
			String text = getCodeArea().getText();
			int lineStart = TextUtils.scanLineStart(text, currentWordStart);
			int previousEnd = TextUtils.scanPreviousWordEnd(text, currentWordStart);
			
			if (previousEnd != -1 && previousEnd >= lineStart) {
				type = TextUtils.scanWord(text, previousEnd-1);
			}
		}
		
		if (type != null) {
			if (type.endsWith("s")) {
				// Array properties: place the caret in the empty line
				getCodeArea().moveTo(currentWordEnd + 2);
			}
			else {
				// Single-value properties: select the value, which is the next word
				String text = getCodeArea().getText();
				int lineEnd = TextUtils.scanLineEnd(text, currentWordStart);
				int nextStart = TextUtils.scanNextWordStart(text, currentWordEnd);
				if (nextStart != -1 && nextStart < lineEnd) {
					getCodeArea().selectRange(nextStart, TextUtils.scanWordEnd(text, nextStart));
				}
			}
		}
	}
	
	@Override protected void onHyperlinkAction(HyperlinkData hyperlink) {
		if (hyperlink.type.equals("key")) {
			hyperlinkOpenFile((String[]) hyperlink.object);
		}
		
		super.onHyperlinkAction(currentHyperlink);
	}
}
