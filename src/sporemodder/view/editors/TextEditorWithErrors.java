package sporemodder.view.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import sporemodder.UIManager;
import sporemodder.file.DocumentError;
import sporemodder.view.StatusBar.Status;
import sporemodder.view.syntax.SyntaxHighlighter;

public abstract class TextEditorWithErrors extends TextEditor {

	// A class we need to store additional information
	protected static class ErrorInfo {
		DocumentError error;
		
		int position;
		int length;
	}
	
	/** The current errors, used in tooltips. */
	protected final List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
		
	public TextEditorWithErrors() {
		// Generate tooltips for the errors
		getTooltipFactories().add((text, event) -> {
			int index = event.getCharacterIndex();
			
			for (ErrorInfo error : errors) {
				if (error.position <= index && index < error.position + error.length) {
					return error.error.getMessage();
				}
			}
			
			return null;
		});
	}
	
	protected abstract List<DocumentError> getErrors();
	protected abstract List<DocumentError> getWarnings();
	
	protected void setErrorInfo(SyntaxHighlighter syntax) {
		errors.clear();
		
		Map<DocumentError, ErrorInfo> errorsMap = new HashMap<>();
		
		// Store some information about the errors for tooltips
		List<DocumentError> documentErrors = getErrors();
		for (DocumentError error : documentErrors) {
			int lineStart = syntax.getLinePosition(error.getLine());
			int startPosition = lineStart + error.getStartPosition();
			int endPosition = lineStart + error.getEndPosition();
			
			ErrorInfo errorInfo = new ErrorInfo();
			errorInfo.error = error;
			errorInfo.position = startPosition;
			errorInfo.length = endPosition - startPosition;
			
			errorsMap.put(error, errorInfo);
			errors.add(errorInfo);
		}
		
		List<DocumentError> documentWarnings = getWarnings();
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
		
		if (item != null) {
			if (documentErrors.isEmpty()) {
				UIManager.get().getUserInterface().setStatusInfo(null);
				UIManager.get().getUserInterface().getStatusBar().setStatus(Status.DEFAULT);
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("The file contains ");
				sb.append(documentErrors.size());
				sb.append(" error");
				if (documentErrors.size() > 1) sb.append('s');
				sb.append(", cannot be compiled. Line");
				if (documentErrors.size() > 1) sb.append('s');
				sb.append(": ");
				
				
				HBox hbox = new HBox();
				hbox.setSpacing(4.0);
				Label label = new Label(sb.toString());
				label.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
				hbox.getChildren().add(label);
				
				for (int i = 0; i < Math.min(documentErrors.size(), 5); ++i) {
					Hyperlink hyperlink = new Hyperlink(Integer.toString(documentErrors.get(i).getLine() + 1));
					hyperlink.setMaxHeight(12.0);
					hyperlink.setPrefHeight(12.0);
					hyperlink.setPadding(Insets.EMPTY);
					hbox.getChildren().add(hyperlink);
					
					final int index = i;
					hyperlink.setOnAction(ev -> {
						ErrorInfo error = errorsMap.get(documentErrors.get(index));
						Platform.runLater(() -> getCodeArea().requestFocus());
						getCodeArea().moveTo(error.position);
						getCodeArea().selectRange(error.position + error.length, error.position);
						getCodeArea().requestFollowCaret();
					});
				}
				
				UIManager.get().getUserInterface().setStatusInfo(hbox);
				UIManager.get().getUserInterface().getStatusBar().setStatus(Status.ERROR);
			}
		}
	}
}
