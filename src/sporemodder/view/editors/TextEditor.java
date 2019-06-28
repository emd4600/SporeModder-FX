/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.stage.Popup;
import sporemodder.EditorManager;
import sporemodder.FileManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.DocumentFragment;
import sporemodder.file.DocumentStructure;
import sporemodder.file.TextUtils;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;
import sporemodder.view.syntax.SyntaxFormat;
import sporemodder.view.syntax.SyntaxHighlighter;

public class TextEditor extends AbstractEditableEditor implements ItemEditor, SearchableEditor, EditHistoryEditor {

	public static final String DEFAULT_STYLE_CLASS = "sporemodder-text-editor";
	
	@FunctionalInterface
	public static interface EditAction {
		public void edit(CodeArea codeArea);
	}
	
	/** The code area where the text is shown and can be edited. It's the most important node in the text editor. */
	private final CodeArea codeArea = new CodeArea();
	
	/** If this value is true, no text events will be shown. This can be used when editing the text triggers events but you want to avoid it.*/
	private boolean textEventsDisabled;  
	
	/** The current syntax highlighting format, or null if there is none. */
	private SyntaxFormat syntaxHighlighting;
	
	/** The structure of the document, optional. */
	private DocumentStructure structure;
	
	/* -- TOOLTIPS -- */
	private final List<TooltipFactory> tooltipFactories = new ArrayList<TooltipFactory>();
	
	/** The popup used to show text tooltips. */
	private final Popup popup = new Popup();
	/** The label shown in text tooltips. */
	private final Label popupMsg = new Label();
	private boolean isValidText = true;
	
	private String originalContents;
	
	
	private String searchedText;
	private final TreeSet<Integer> searchPositions = new TreeSet<Integer>();
	
	public TextEditor() {
		super();
		getStyleClass().add(DEFAULT_STYLE_CLASS);
		
		codeArea.setPrefWidth(Double.MAX_VALUE);
		codeArea.setPrefHeight(Double.MAX_VALUE);
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		
		//TODO use this wisely?
		codeArea.requestFollowCaret();
		
		
		// -- Special functionality -- //
		
		// When the user presses enter, add tabulate to keep the previous indentation
		codeArea.setOnKeyTyped(event -> {
			if (event.getCharacter().getBytes()[0] == '\n' || event.getCharacter().getBytes()[0] == '\r') {
				tabulate();
				codeArea.requestFollowCaret();
			}
		});
		
		Nodes.addInputMap(codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.TAB), event -> {
			if (codeArea.getSelection().getLength() != 0) {
				tabulateSelection();
			} else {
				codeArea.replaceSelection("\t");
			}
		}));
		
		Nodes.addInputMap(codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.TAB, KeyCombination.SHIFT_DOWN), event -> {
			if (codeArea.getSelection().getLength() != 0) {
				untabulateSelection();
			} else {
				codeArea.replaceSelection("\t");
			}
		}));
		
		// -- Tooltips -- //
		popupMsg.getStyleClass().add("tooltip");
		popup.getContent().add(popupMsg);
			
		// The time that takes to show a tooltip
		codeArea.setMouseOverTextDelay(Duration.ofMillis(500));
		
		codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, event -> {
			String text = getTooltipText(event);
			if (text != null) {
				popupMsg.setText(text);
				
				Point2D pos = event.getScreenPosition();
				popup.show(codeArea, pos.getX(), pos.getY() + 10);
			}
		});
		codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, event -> {
			popup.hide();
		});
		
		
		codeArea.textProperty().addListener((obs, oldText, newText) -> {
			setIsSaved(false);
			
			if (!textEventsDisabled) {
				doSearch();
				
				updateSyntaxHighlighting();
			}
			
			UIManager.get().notifyUIUpdate(false);
		});
		
		codeArea.caretPositionProperty().addListener((obs, oldText, newText) -> {
			UIManager.get().notifyUIUpdate(false);
		});
	}
	
	private void tabulateSelection() {
		int selectionStart = codeArea.getSelection().getStart();
		int selectionEnd = codeArea.getSelection().getEnd();
		String text = codeArea.getText();
		
		List<Integer> indices = new ArrayList<>();
		int lineStart = TextUtils.scanLineStart(text, selectionStart);
		while (lineStart < selectionEnd) {
			indices.add(lineStart);
			lineStart = TextUtils.scanLineStart(text, TextUtils.scanLineEnd(text, lineStart) + 1);
		}
		
		for (int i = 0; i < indices.size(); ++i) {
			// Add i because there are i \t characters now
			codeArea.insertText(indices.get(i) + i, "\t");
		}
	}
	
	private void untabulateSelection() {
		int selectionStart = codeArea.getSelection().getStart();
		int selectionEnd = codeArea.getSelection().getEnd();
		String text = codeArea.getText();
		
		List<Integer> indices = new ArrayList<>();
		int lineStart = TextUtils.scanLineStart(text, selectionStart);
		while (lineStart < selectionEnd) {
			indices.add(lineStart);
			lineStart = TextUtils.scanLineStart(text, TextUtils.scanLineEnd(text, lineStart) + 1);
		}
		
		int subtract = 0;
		for (int i = 0; i < indices.size(); ++i) {
			int index = indices.get(i) - subtract;
			if (text.charAt(index) == '\t') {
				codeArea.replaceText(index, index + 1, "");
				text = codeArea.getText();
				++subtract;
			}
		}
	}
	
	public void loadFile(ProjectItem item) throws IOException {
		if (item != null) {
			this.item = item;
			load(getFile(item));
		}
		else {
			this.item = null;
			this.file = null;
		}
	}
	
	public void loadFile(File file) throws IOException {
		if (file != null) {
			load(file);
		}
		else {
			this.file = null;
		}
	}
	
	/**
	 * Returns the file that is shown in the text editor. By default this just returns the item file,
	 * but certain editors might want to show other files related to that item instead.
	 * @param item
	 * @return
	 */
	protected File getFile(ProjectItem item) {
		return item.getFile();
	}
	
	public void load(File file) throws IOException {
		setDestinationFile(file);
		
		/* -- Syntax Highlighting -- */
		SyntaxFormat syntaxHighlighting = EditorManager.get().getSyntaxHighlighting(file);
		if (syntaxHighlighting != null) {
			// Only replace the existing one if there is a supported format
			this.syntaxHighlighting = syntaxHighlighting;
		}
		
		// Set the text
		byte[] bytes = Files.readAllBytes(file.toPath());
		
		// Always returns null, does not work fine
//		String type = Files.probeContentType(file.toPath());
//		isValidText = "text/plain".equals(type);
		try {  
			Charset.availableCharsets().get("UTF-8").newDecoder()
				.decode(ByteBuffer.wrap(bytes));  
			isValidText = true;
		} catch (CharacterCodingException e) {  
			isValidText = false;
		}  
		
		loadContents(new String(bytes));
	}
	
	public void loadContents(String contents) throws IOException {
		originalContents = contents;
		codeArea.replaceText(originalContents);
		
		codeArea.moveTo(0);
		codeArea.scrollToPixel(0, 0);
		
		if (item != null && item.isMod() && ProjectManager.get().getActive().isReadOnly()) {
			codeArea.setEditable(false);
		} else {
			codeArea.setEditable(isValidText);
		}
		
		setIsSaved(true);
		
		updateSyntaxHighlighting();
		
		codeArea.getUndoManager().forgetHistory();
	}
	
	
	/** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new TextEditorSkin(this);
    }
	
	
	public CodeArea getCodeArea() {
		return codeArea;
	}
	
	public ProjectItem getItem() {
		return item;
	}

	public boolean isTextEventsDisabled() {
		return textEventsDisabled;
	}

	public void setTextEventsDisabled(boolean textEventsDisabled) {
		this.textEventsDisabled = textEventsDisabled;
	}
	
	/**
	 * Executes the given action to edit text without triggering any text events. After the editing is finished,
	 * this will update the syntax highlighting.
	 * @param action
	 */
	public void eventlessEdit(EditAction action) {
		textEventsDisabled = true;
		action.edit(codeArea);
		textEventsDisabled = false;
		updateSyntaxHighlighting();
	}
	
	
	/**
	 * Returns the SyntaxFormat object that is used to display syntax highlighting. If it is null, no syntax 
	 * highlighting is shown.
	 * @return
	 */
	public SyntaxFormat getSyntaxHighlighting() {
		return syntaxHighlighting;
	}

	/**
	 * Sets the SyntaxFormat object that is used to display syntax highlighting. If set to null, no syntax 
	 * highlighting will be shown. This method will automatically apply it to the text
	 * @return
	 */
	public void setSyntaxHighlighting(SyntaxFormat syntaxHighlighting) {
		this.syntaxHighlighting = syntaxHighlighting;
		updateSyntaxHighlighting();
	}

	/**
	 * Updates the syntax highlighting applying it to the current text.
	 */
	public void updateSyntaxHighlighting() {
		SyntaxHighlighter syntax = new SyntaxHighlighter();
		syntax.setText(codeArea.getText(), null);
		
		if (syntaxHighlighting != null) {
			syntaxHighlighting.generateStyle(codeArea.getText(), syntax);
		}
		
		if (!searchPositions.isEmpty()) {
			int length = searchedText.length();
			for (Integer start : searchPositions) {
				syntax.addExtra(start, length, "text-searched-word", false);
			}
		}
		
		// Update always, cause the syntax might have been removed
		try {
			codeArea.setStyleSpans(0, syntax.generateStyleSpans());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Replaces the text of a document fragment with the new text. This will update all the document structure parameters 
	 * and the positions of its fragments. This method can only be used if a DocumentStructure has been specified for this editor.
	 * @param fragment
	 * @param text
	 */
	public void replaceText(DocumentFragment fragment, String text) {
		codeArea.replaceText(fragment.getStart(), fragment.getEnd(), text);
		structure.setLength(fragment, text.length());
		structure.setText(codeArea.getText());
	}
	
	public DocumentStructure getStructure() {
		return structure;
	}

	public void setStructure(DocumentStructure structure) {
		this.structure = structure;
	}
	
	
	/**
	 * Adds as many tabulation (\t) spaces as necessary in the current caret position so that the line
	 * is aligned with the previous line. The current line is expected to be empty.
	 */
	public void tabulate() {

		// Try to find where the current line starts
		// Subtract one because we just typed the '\n' character
		String text = codeArea.getText().substring(0, codeArea.getCaretPosition() - 1);
		int lineStartIndex = text.lastIndexOf("\n");
		
		if (lineStartIndex == -1) {
			// There are no previous new lines, so the line is the first one
			lineStartIndex = 0;
		}
		
		int tabulation = 0;
		while (lineStartIndex < text.length()) {
			char c = text.charAt(lineStartIndex++);
			
			if (c == '\r' || c == '\n') {
				// Accept this character, but do nothing
			}
			else if (c == '\t') {
				tabulation++;
			}
			else {
				break;
			}
		}
		
		if (tabulation != 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tabulation; i++) {
				sb.append('\t');
			}
			
			codeArea.insertText(codeArea.getCaretPosition(), sb.toString());
		}
	}
	
	
	@Override
	public void setActive(boolean isActive) {
		super.setActive(isActive);
		
		if (isActive && !isValidText && item != null) {
			Label label = new Label("This format is not supported by SporeModder and it cannot be read as text, therefore it cannot be edited.");
			label.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
			
			UIManager.get().getUserInterface().setStatusInfo(label);
		}
		
		if (isActive) {
			// This will allow the editor to set error into the status bar if necessary
			updateSyntaxHighlighting();
		}
		
		showInspector(isActive);
	}
	
	/**
	 * Returns the current text.
	 * @return
	 */
	public final String getText() {
		return codeArea.getText();
	}
	
	protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault(null, FileManager.getExtension(getFile().getName()), null);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override protected void saveData() throws Exception {
		if (!file.exists()) file.createNewFile();
		
		try (PrintWriter out = new PrintWriter(file)) {
			out.write(codeArea.getText());
			
			setIsSaved(true);
		}
	}

	@Override
	public boolean isEditable() {
		return isValidText;
	}
	
	public List<TooltipFactory> getTooltipFactories() {
		return tooltipFactories;
	}
	
	private String getTooltipText(MouseOverTextEvent e) {
		String tooltip = null;
		String text = codeArea.getText();
		
		for (TooltipFactory factory : tooltipFactories) {
			tooltip = factory.getTooltip(text, e);
			if (tooltip != null) {
				return tooltip;
			}
		}
		
		return null;
	}

	@Override
	public Node getUI() {
		return this;
	}

	@Override
	public boolean supportsSearching() {
		return true;
	}

	@Override
	public boolean supportsEditHistory() {
		return true;
	}

	@Override
	public boolean canFindUp() {
		int caret = codeArea.getCaretPosition();
		// floor returns x such that x <=; do it for -1 so it does not return the same position;
		return searchPositions.floor(caret - 1) != null;
	}

	@Override
	public boolean canFindDown() {
		int caret = codeArea.getCaretPosition();
		// ceiling returns x such that x >=; do it for +1 so it does not return the same position;
		return searchPositions.ceiling(caret + 1) != null;
	}

	@Override
	public void findUp() {
		int caret = codeArea.getCaretPosition();
		// floor returns x such that x <=; do it for -1 so it does not return the same position;
		Integer pos = searchPositions.floor(caret - 1);
		if (pos != null) {
			codeArea.moveTo(pos);
			codeArea.selectRange(pos + searchedText.length(), pos);
			codeArea.requestFollowCaret();
		}
	}

	@Override
	public void findDown() {
		int caret = codeArea.getCaretPosition();
		// ceiling returns x such that x >=; do it for +1 so it does not return the same position;
		Integer pos = searchPositions.ceiling(caret + 1);
		if (pos != null) {
			codeArea.moveTo(pos);
			codeArea.selectRange(pos + searchedText.length(), pos);
			codeArea.requestFollowCaret();
		}
	}
	
	private void doSearch() {
		searchPositions.clear();
		
		if (searchedText != null && !searchedText.isEmpty()) {
			String searchableText = codeArea.getText().toLowerCase();
			String textToSearch = searchedText.toLowerCase();
			
			int indexOf = -1;
			while ((indexOf = searchableText.indexOf(textToSearch, indexOf+1)) != -1) {
				searchPositions.add(indexOf);
			}
		}
	}

	@Override
	public boolean find(String text) {
		if ((searchedText == null && text == null) || (searchedText != null && searchedText.equals(text))) {
			return true;
		}
		
		searchedText = text;
		
		doSearch();
		
		updateSyntaxHighlighting();
		
		return !searchPositions.isEmpty();
	}

	@Override
	public String getSearchedText() {
		return searchedText;
	}

	@Override
	public boolean canUndo() {
		return codeArea.isUndoAvailable();
	}

	@Override
	public boolean canRedo() {
		return codeArea.isRedoAvailable();
	}

	@Override
	public void undo() {
		codeArea.undo();
	}

	@Override
	public void redo() {
		codeArea.redo();
	}

	@Override
	protected void restoreContents() {
		codeArea.replaceText(originalContents);
	}

	@Override
	public List<? extends EditHistoryAction> getActions() {
		return null;
	}

	@Override
	public int getUndoRedoIndex() {
		return -1;
	}
}
