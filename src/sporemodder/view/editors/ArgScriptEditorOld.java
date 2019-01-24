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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sporemodder.UIManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentFragment;
import sporemodder.file.DocumentStructure;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.util.ProjectItem;
import sporemodder.util.inspector.InspectorUnit;
import sporemodder.view.syntax.SyntaxHighlighter;

/**
 * An editor that is backed by an ArgScript stream. This means that the editor will use the ArgScript syntax highlighting,
 * show the errors of the stream in tooltips, etc
 */
public abstract class ArgScriptEditorOld<T> extends TextEditorOld implements ItemEditor, TooltipFactory {
	
	// A class we need to store additional information
	private static class ErrorInfo {
		DocumentError error;
		
		int position;
		int length;
	}
	
	protected String inspectorTitle;
	protected ArgScriptStream<T> stream;
	
	protected InspectorUnit<ArgScriptStream<T>> inspector;
	
	// The current errors, used in tooltips
	private final List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
	
	// To avoid cycling events
	private boolean callingFromEvent = false;

	public ArgScriptEditorOld(String uiPath, String title) {
		super(uiPath);
		this.inspectorTitle = title;
	}
	
	//TODO re enable the inspector when the editor is set active again
	
	/**
	 * This method must be used by subclasses to generate the ArgScriptStream that processes the document in the editor.
	 * If an inspector is generated as well, an inspector panel will be generated and set in the UI every time the editor is active.
	 */
	protected abstract void generateStream();
	
	private void generateInspector() {
		UIManager.get().getUserInterface().setInspectorContent(inspector.generateUI(true));
		UIManager.get().getUserInterface().getInspectorPane().setTitle(inspectorTitle);
		
		TreeView<DocumentFragment> treeView = inspector.getStructureTree();
		
		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (!callingFromEvent) {
				callingFromEvent = true;
				
				if (newValue != null) {
					DocumentFragment fragment = newValue.getValue();
					
					CodeArea codeArea = editorUI.getCodeArea();
					codeArea.moveTo(fragment.getStart());
					codeArea.selectRange(fragment.getStart(), fragment.getEnd());
					
					//TODO make it scroll to the content
				}
			}
			
			// This happens regardless it has been called from the tree or from the text
			inspectorSelectionChanged(oldValue, newValue);
			//inspector.updateUI();
			
			callingFromEvent = false;
		});
		
		editorUI.getCodeArea().caretPositionProperty().addListener((obs, oldValue, newValue) -> {
			if (!callingFromEvent && !inspector.isWriting()) {
				callingFromEvent = true;
				
				DocumentStructure structure = inspector.getDocumentStructure();
				List<DocumentFragment> list = structure.getFragment(newValue);
				if (list != null) {
					inspector.setSelectedFragment(list);
				}
			}
			
			callingFromEvent = false;
		});
	}
	
	protected void inspectorSelectionChanged(TreeItem<DocumentFragment> oldValue, TreeItem<DocumentFragment> newValue) {
		
	}
	
	@Override
	public void applySyntaxHighlighting(String text) {
		stream.process(text);
		SyntaxHighlighter syntax = stream.getSyntaxHighlighter();
		stream.addErrorsSyntax();
		
		editorUI.getCodeArea().setStyleSpans(0, syntax.generateStyleSpans());
		
		setErrorInfo(syntax);
		
	}

	@Override
	protected void onTextChange(ObservableValue<? extends String> obs, String oldText, String newText) {
		if (inspector != null && inspector.isWriting()) {
			// If this event was called in an inspector internal write, do nothing
			return;
		}
		
		long startTime = System.currentTimeMillis();
		
		if (stream == null) {
			generateStream();
			
			if (inspector != null) {
				generateInspector();
			}
		}
		
		applySyntaxHighlighting(newText);
		
		if (inspector != null) {
			DocumentStructure structure = stream.getDocumentStructure();
			structure.setText(newText);
			inspector.setDocumentStructure(structure);
		}
		
		long processTime = System.currentTimeMillis() - startTime;
		
		System.out.println(processTime + "ms"); 
	}
	
	private void setErrorInfo(SyntaxHighlighter syntax) {
		errors.clear();
		
		// Store some information about the errors for tooltips
		List<DocumentError> documentErrors = stream.getErrors();
		for (DocumentError error : documentErrors) {
			int lineStart = syntax.getLinePosition(error.getLine());
			int startPosition = lineStart + error.getStartPosition();
			int endPosition = lineStart + error.getEndPosition();
			
			ErrorInfo errorInfo = new ErrorInfo();
			errorInfo.error = error;
			errorInfo.position = startPosition;
			errorInfo.length = endPosition - startPosition;
			
			errors.add(errorInfo);
		}
		
		List<DocumentError> documentWarnings = stream.getWarnings();
		for (DocumentError error : documentWarnings) {
			int lineStart = syntax.getLinePosition(error.getLine());
			int startPosition = lineStart + error.getStartPosition();
			int endPosition = lineStart + error.getEndPosition();
			
			ErrorInfo errorInfo = new ErrorInfo();
			errorInfo.error = error;
			errorInfo.position = startPosition;
			errorInfo.length = endPosition - startPosition;
			
			errors.add(errorInfo);
		}
	
		// Errors are ordered by lines, but not by position; fix that here
		Collections.sort(errors, new Comparator<ErrorInfo>() {
			@Override
			public int compare(ErrorInfo obj1, ErrorInfo obj2) {
				return obj1.position - obj2.position;
			}
		});
	}
	
	protected void initialize(ProjectItem item) throws IOException {
		super.initialize(item);
		
		tooltipFactories.add(this);
	}
	
	@Override
	public String getTooltip(String text, MouseOverTextEvent event) {
		int index = event.getCharacterIndex();
		
		for (ErrorInfo error : errors) {
			if (error.position <= index && index < error.position + error.length) {
				return error.error.getMessage();
			}
		}
		
		return null;
	}
}
