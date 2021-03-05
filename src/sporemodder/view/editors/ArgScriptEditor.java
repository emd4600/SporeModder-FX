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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sporemodder.EditorManager;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentFragment;
import sporemodder.file.TextUtils;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptStream.HyperlinkData;
import sporemodder.file.locale.LocaleUnit;
import sporemodder.util.ColorRGB;
import sporemodder.util.ColorRGBA;
import sporemodder.util.ProjectItem;
import sporemodder.view.StatusBar.Status;
import sporemodder.view.colorpicker.ColorSwatchUI;
import sporemodder.view.syntax.SyntaxHighlighter;

/**
 * An editor that is backed by an ArgScript stream. This means that the editor will use the ArgScript syntax highlighting,
 * show the errors of the stream in tooltips, etc
 */
public abstract class ArgScriptEditor<T> extends TextEditor {
	
	// A class we need to store additional information
	private static class ErrorInfo {
		DocumentError error;
		
		int position;
		int length;
	}
	
	/** The current errors, used in tooltips. */
	private final List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
	
	private final Map<List<String>, LocaleUnit> localeCache = new HashMap<>();
	private final Map<List<String>, Long> localeCacheTimes = new HashMap<>();
	
	protected HyperlinkData currentHyperlink;
	
	protected ArgScriptStream<T> stream;
	
	private double mouseX;
	private double mouseY;
	
	private ContextMenu colorPickerMenu;
	private ColorSwatchUI colorPicker;
	private HyperlinkData colorHyperlink;
	
	public ArgScriptEditor() {
		super();
		
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
		
		// Generate tooltips for colors
		getTooltipFactories().add((text, event) -> {
			if (stream == null) return null;
			
			int index = event.getCharacterIndex();
			
			for (HyperlinkData hyperlink : stream.getHyperlinkData()) {
				int lineStart = stream.getLinePositions().get(hyperlink.line);
				if (index >= lineStart + hyperlink.start && index <= lineStart + hyperlink.end) {
					if (ArgScriptStream.HYPERLINK_COLOR.equals(hyperlink.type)) {
						ColorRGB value = (ColorRGB) hyperlink.object;
						
						Rectangle rect = new Rectangle(0, 0, 80, 25);
						rect.setFill(value.toColor());
						rect.setStroke(Color.BLACK);
						
						return rect;
					}
					else if (LocaleUnit.HYPERLINK_LOCALE.equals(hyperlink.type)) {
						return getLocaleTooltip((String[]) hyperlink.object);
					}
					else {
						Object result = generateHyperlinkTooltip(hyperlink, index);
						if (result != null) return result;
					}
				}
			}
			
			return null;
		});
		
		setSyntaxHighlighting((text, syntax) -> {
			if (stream != null) {
				onStreamParse();
				
				stream.process(getText());
				SyntaxHighlighter streamSyntax = stream.getSyntaxHighlighter();
				stream.addErrorsSyntax();
				syntax.addExtras(streamSyntax, false);
				
				setErrorInfo(streamSyntax);
				
				afterStreamParse();
			}
		});
		
		getCodeArea().addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
			mouseX = event.getScreenX();
			mouseY = event.getScreenY();
			
			updateHyperlink(event.isControlDown());
		});
		getCodeArea().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			updateHyperlink(event.isControlDown());
		});
		getCodeArea().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
			updateHyperlink(event.isControlDown());
		});
		
		getCodeArea().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (currentHyperlink != null) {
				int linePos = stream.getSyntaxHighlighter().getLinePosition(currentHyperlink.line);
				getCodeArea().getCharacterBoundsOnScreen(linePos + currentHyperlink.start, linePos + currentHyperlink.end).ifPresent(bounds -> {
					if (bounds.contains(mouseX, mouseY)) {
						onHyperlinkAction(currentHyperlink);
					}
				});
			}
		});

        UIManager.get().getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleColorPickerClick);
	}

	@Override public void loadFile(ProjectItem item) throws IOException {
		if (item == null) {
			UIManager.get().getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, this::handleColorPickerClick);
		}
		super.loadFile(item);
	}
	
	protected Object generateHyperlinkTooltip(HyperlinkData hyperlink, int index) {
		return null;
	}
	
	protected String getLocaleTooltip(String[] splits) {
		if (splits.length == 2 && splits[0] != null && splits[1] != null) 
		{
			String folderName = HashManager.get().getFileName(0x02FABF01);
			String extension = '.' + HashManager.get().getTypeName(0x02FAC0B6);
			File file = ProjectManager.get().getFile(folderName + File.separatorChar + splits[0] + extension);
			if (file != null) {
				List<String> key = Arrays.asList(splits);
				LocaleUnit locale = localeCache.get(key);
				long time = 0;
				if (locale != null) {
					try {
						time = Files.getLastModifiedTime(file.toPath()).toMillis();
					} catch (IOException e) {
						e.printStackTrace();
						time = Long.MAX_VALUE;
					}
					if (time > localeCacheTimes.get(key)) locale = null;
				}
				if (locale == null) {
					locale = LocaleUnit.fromFile(file);
					localeCache.put(key, locale);
					localeCacheTimes.put(key, time);
				}
				if (locale != null) {
					return locale.getText(HashManager.get().getFileHash(splits[1]));
				}
			}
		}
		return null;
	}
	
	private void handleColorPickerClick(MouseEvent event) {
		if (colorPicker != null && colorPickerMenu.isShowing() && !colorPicker.contains(colorPicker.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
			colorPickerMenu.hide();
    	}
	}
	
	protected void onColorHyperlink(HyperlinkData hyperlink) {
		colorHyperlink = hyperlink;
		double r, g, b;
		
		if (hyperlink.object instanceof ColorRGB) {
			ColorRGB color = (ColorRGB) hyperlink.object;
			r = color.getR();
			g = color.getG();
			b = color.getB();
		} else {
			ColorRGBA color = (ColorRGBA) hyperlink.object;
			r = color.getR();
			g = color.getG();
			b = color.getB();
		}
		
		if (colorPicker == null) {
			colorPicker = new ColorSwatchUI();
	        
			CustomMenuItem menuItem = new CustomMenuItem();
			menuItem.getStyleClass().add("color-swatch-menu-item");
			menuItem.setContent(colorPicker);
			menuItem.setHideOnClick(false);
			
			colorPickerMenu = new ContextMenu();
			colorPickerMenu.setHideOnEscape(true);
			colorPickerMenu.getItems().add(menuItem);
			
			colorPickerMenu.setOnHidden(event -> {
				int linePos = stream.getLinePositions().get(colorHyperlink.line);
				Color customColor = colorPicker.getCustomColor();
				Object object;
				if (colorHyperlink.object instanceof ColorRGB) {
					object = new ColorRGB((float) customColor.getRed(), (float) customColor.getGreen(), (float) customColor.getBlue());
				} else {
					object = new ColorRGBA((float) customColor.getRed(), (float) customColor.getGreen(), (float) customColor.getBlue(), ((ColorRGBA) colorHyperlink.object).getA());
				}
				getCodeArea().replaceText(linePos + colorHyperlink.start, linePos + colorHyperlink.end, object.toString());
				
				updateSyntaxHighlighting();
			});
		}
		else if (colorPickerMenu.isShowing()) {
			colorPickerMenu.hide();
		}
		
		colorPicker.setCurrentColor(new Color(r, g, b, 1.0));
		
		int linePos = stream.getLinePositions().get(hyperlink.line);
		int pos = hyperlink.start + linePos;
		
		Bounds bounds = getCodeArea().getCharacterBoundsOnScreen(pos, pos+1).orElse(new BoundingBox(0, 0, 0, 0));
		
		colorPickerMenu.show(getCodeArea(), bounds.getMinX(), bounds.getMaxY());
	}
	
	protected void onLocaleHyperlink(HyperlinkData hyperlink) {
		String[] splits = (String[]) hyperlink.object;
		hyperlinkOpenFile(new String[] { HashManager.get().getFileName(LocaleUnit.GROUP_ID), splits[0], HashManager.get().getTypeName(LocaleUnit.TYPE_ID) });
	}
	
	protected void onHyperlinkAction(HyperlinkData hyperlink) {
		if (ArgScriptStream.HYPERLINK_COLOR.equals(hyperlink.type)) {
			onColorHyperlink(hyperlink);
		}
		else if (LocaleUnit.HYPERLINK_LOCALE.equals(hyperlink.type)) {
			onLocaleHyperlink(hyperlink);
		}
	}
	
	protected boolean hyperlinkOpenFile(String path) {
		try {
			ProjectItem item = ProjectManager.get().getItem(path);
			ProjectManager.get().expandToItem(item);
			EditorManager.get().loadFile(item);
			EditorManager.get().moveFileToNewTab(item);	
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	protected void hyperlinkOpenFile(String[] names) {
		// group, instance, type
		
		String path = null;
		
		if (names[0] != null) {
			path = names[0] + File.separatorChar + names[1];
		} else {
			path = HashManager.get().getFileName(0) + File.separatorChar + names[1];
		}
		
		// By default, try .prop/.soundProp files
		String prop = HashManager.get().getTypeName(0x00B1B104);
		String soundProp = HashManager.get().getTypeName(0x02B9F662);
		
		if (names[2] != null) {
			path = path + '.' + names[2];
			
			if (names[2].equals(prop) || names[2].equals(soundProp)) {
				path = path + ".prop_t";
			}
		} else {
			File file = getFile();
			
			if (file.getName().contains(soundProp)) {
				path = path + '.' + soundProp;
			} else {
				path = path + '.' + prop;
			}
			
			path = path + ".prop_t";
		}
		
		try {
			ProjectItem item = ProjectManager.get().getItem(path);
			ProjectManager.get().expandToItem(item);
			EditorManager.get().loadFile(item);
			EditorManager.get().moveFileToNewTab(item);
		} 
		catch (Exception e) {
			// Maybe it's a converted .rw4.dds
			if (path.endsWith('.' + HashManager.get().getTypeName(0x2F4E681B))) {
				try {
					path = path + ".dds";
					ProjectItem item = ProjectManager.get().getItem(path);
					ProjectManager.get().expandToItem(item);
					EditorManager.get().loadFile(item);
					EditorManager.get().moveFileToNewTab(item);
				}
				catch (IOException e1) {
					// Just ignore it
				}
			}
		}
	}
	
	private void updateHyperlink(boolean mustFindHyperlink) {
		HyperlinkData oldHyperlink = currentHyperlink;
		currentHyperlink = null;
		
		if (mustFindHyperlink) {
			findHyperlink();
		}
		
		if (oldHyperlink != null && oldHyperlink != currentHyperlink) {
			int linePos = stream.getSyntaxHighlighter().getLinePosition(oldHyperlink.line);
			getCodeArea().clearStyle(linePos+oldHyperlink.start, linePos+oldHyperlink.end);
		}
		if (currentHyperlink != null) {
			int linePos = stream.getSyntaxHighlighter().getLinePosition(currentHyperlink.line);
			getCodeArea().setStyle(linePos+currentHyperlink.start, linePos+currentHyperlink.end, Collections.singleton("hyperlink"));
		}
	}
	
	private void findHyperlink() {
		List<HyperlinkData> hyperlinks = stream.getHyperlinkData();
		
		for (HyperlinkData data : hyperlinks) {
			if (currentHyperlink != null) break;
			
			int linePos = stream.getSyntaxHighlighter().getLinePosition(data.line);
			getCodeArea().getCharacterBoundsOnScreen(linePos + data.start, linePos + data.end).ifPresent(bounds -> {
				if (bounds.contains(mouseX, mouseY)) {
					currentHyperlink = data;
				}
			});
		}
	}
	
	public ArgScriptStream<T> getStream() {
		return stream;
	}
	
	private void setErrorInfo(SyntaxHighlighter syntax) {
		errors.clear();
		
		Map<DocumentError, ErrorInfo> errorsMap = new HashMap<>();
		
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
			
			errorsMap.put(error, errorInfo);
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
	
	protected void onStreamParse() {
		
	}
	
	protected void afterStreamParse() {
		
	}
	
	/**
	 * Replaces a split word of an ArgScriptLine. The line is fully contained in the specified DocumentFragment. 
	 * The document structure will be adapted accordingly.
	 * <p>
	 * The replacement is done in an no-event edit, meaning that this won't trigger any text events but it will apply
	 * syntax highlighting once done.
	 * @param fragment
	 * @param line
	 * @param newValue
	 * @param splitIndex
	 */
	public void replaceSplit(DocumentFragment fragment, ArgScriptLine line, String newValue, int splitIndex) {
		eventlessEdit(codeArea -> {
			replaceText(fragment, line.replaceSplit(fragment.getText(), newValue, splitIndex));
		});
	}
	
	@Override public void setDestinationFile(File file) {
		super.setDestinationFile(file);
		stream.setFolder(file.getParentFile());
	}
	
	private void removeBlockComment(String text, int textStart, int textEnd) {
		// We use replaceText instead of multiple deleteText so that it goes into a single undoable action
		text = text.substring(0, textStart) + text.substring(textStart + 2, textEnd - 1) + text.substring(textEnd + 1);
		getCodeArea().replaceText(text);
		getCodeArea().selectRange(textStart, textEnd - 3);
	}
	
	@Override protected void toggleBlockComment(int start, int end) {
		if (end - start <= 0) return;
		String text = getText();
		int textStart = TextUtils.scanNextWordStart(text, start);
		if (textStart != -1 && text.charAt(textStart) == '#') 
		{
			if (textStart + 1 < text.length() && text.charAt(textStart + 1) == '<')
			{
				// If it's a block comment, uncomment
				int textEnd = TextUtils.scanPreviousWordEnd(text, end);
				if (textEnd > 1 && text.charAt(textEnd) == '>' && text.charAt(textEnd - 1) == '#')  {
					removeBlockComment(text, textStart, textEnd);
					return;
				}
			}
			else {
				// Special case: if there are multiple lines with '#', uncomment them
				List<Integer> lineComments = new ArrayList<>();
				lineComments.add(textStart);
				boolean multipleLineComment = true;
				int pos = textStart;
				while ((pos = TextUtils.scanNextWordStart(text, TextUtils.scanLineEnd(text, pos))) != -1 && pos < end) {
					if (text.charAt(pos) != '#' || (pos + 1 < text.length() && text.charAt(pos + 1) == '<')) {
						multipleLineComment = false;
						break;
					}
					lineComments.add(pos);
				}
				
				if (multipleLineComment) {
					StringBuilder sb = new StringBuilder();
					int lastPos = 0;
					for (int p : lineComments) {
						sb.append(text.substring(lastPos, p));
						lastPos = p + 1;
					}
					sb.append(text.substring(lastPos));
					getCodeArea().replaceText(sb.toString());
					getCodeArea().selectRange(textStart, end - lineComments.size());
					return;
				}
				else {
					// If it's a block comment, uncomment
					int textEnd = TextUtils.scanPreviousWordEnd(text, end);
					if (textEnd > 1 && text.charAt(textEnd) == '>' && text.charAt(textEnd - 1) == '#')  {
						removeBlockComment(text, textStart, textEnd);
						return;
					}
				}
			}
		}
		
		text = text.substring(0, start) + "#<" + text.substring(start, end) + "#>" + text.substring(end);
		getCodeArea().replaceText(text);
		getCodeArea().selectRange(start, end + 4);
	}
	
	
	@Override protected void toggleLineComment(int position) {
		int originalPosition = position;
		String text = getText();
		
		if (position >= text.length() || TextUtils.isNewLine(text, position)) position--;
		position = TextUtils.scanLineStart(text, position);
		
		boolean removeComment;

		int wordStart = TextUtils.scanNextWordStart(text, position);
		if (wordStart == -1) {
			// end of stream, no text in the line
			removeComment = false;
		}
		else if (wordStart < TextUtils.scanLineEnd(text, position) && wordStart < text.length()) {
			position = wordStart;
			removeComment = text.charAt(wordStart) == '#';
			// if it's the beginning of a block comment, comment after that
			if (removeComment && wordStart + 1 < text.length() && text.charAt(wordStart + 1) == '<') {
				removeComment = false;
				position += 2;
			}
		}
		else {
			// We are on an empty line
			removeComment = false;
		}
		
		int moveTo = originalPosition;
		if (removeComment) {
			getCodeArea().deleteText(position, position + 1);
			
			if (originalPosition > wordStart) moveTo--;
		}
		else {
			getCodeArea().insertText(position, "#");

			if (originalPosition > wordStart) moveTo++;
		}
		
		getCodeArea().moveTo(moveTo);
	}
}
