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

package sporemodder.file.argscript;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;
import sporemodder.file.DocumentFragment;
import sporemodder.file.DocumentStructure;
import sporemodder.file.DocumentStructure.StructureNameFactory;
import sporemodder.file.argscript.ArgScriptLine.LineHighlighter;
import sporemodder.util.ColorRGB;
import sporemodder.util.ColorRGBA;
import sporemodder.view.syntax.SyntaxHighlighter;

public class ArgScriptStream<T> {
	
	@FunctionalInterface
	public static interface OnStartAction<T> {
		public void onStart(ArgScriptStream<T> stream, T data);
	}
	
	public static class HyperlinkData {
		public final int line;
		public final int start;
		public final int end;
		public final String type;
		public final Object object;
		public HyperlinkData(String type, Object object, int line, int start, int end) {
			this.type = type;
			this.object = object;
			this.line = line;
			this.start = start;
			this.end = end;
		}
	}
	
	public static final String HYPERLINK_COLOR = "COLOR";
	
	public static final String SYNTAX_COMMENT = "argscript-comment";
	public static final String SYNTAX_BLOCK = "argscript-block";
	public static final String SYNTAX_COMMAND = "argscript-command";
	public static final String SYNTAX_OPTION = "argscript-option";
	public static final String SYNTAX_ENUM = "argscript-enum";
	public static final String SYNTAX_VARIABLE = "argscript-variable";
	
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\R");
	
	/** The folder where this stream is being processed. */
	private File folder;
	
	private T data;
	
	/** An optional action executed every time a file is processed. */ 
	private OnStartAction<T> onStartAction;
	
	/** The minimum version the script must have. */
	private int minVersion;
	
	/** The maximum version the script can have. */
	private int maxVersion;
	
	/** The current version of the script, which can be specified with the `version` command. */
	private int version;
	
	
	/** The lexer object used to process and decode mathematical/boolean expressions. */
	private final ArgScriptLexer lexer = new ArgScriptLexer();
	
	
	/** A map that assigns every definition to its name. */
	private final HashMap<String, ArgScriptDefinition> definitions = new HashMap<String, ArgScriptDefinition>();
	
	/** A map that assigns one keyword to the object capable of parsing it. */
	private final HashMap<String, ArgScriptParser<T>> parsers = new HashMap<String, ArgScriptParser<T>>();
	
	/** A map that assigns one end keyword to the special block that reacts to it. */
	private final List<ArgScriptSpecialBlock.Pair<T>> specialBlocks = new ArrayList<ArgScriptSpecialBlock.Pair<T>>();
	
	/** This list stores all the nested blocks that we are inside of. The last item is the most recent block. */
	private final List<ArgScriptBlock<T>> nestedBlocks = new ArrayList<ArgScriptBlock<T>>();
	
	
	/** A list of the current scopes that are contained, in order of appearance. A scope is the space contained in a namespace. */
	private final List<String> scopes = new ArrayList<String>();

	/** A map that assigns one value to each variable name. */
	private final HashMap<String, String> variables = new HashMap<String, String>();
	
	/** A map that assigns one value to each variable name; this is for global variables, which are set from code. */
	private final HashMap<String, String> globalVariables = new HashMap<String, String>();
	
	
	/** All the errors that have been found. The errors in this list make the document impossible to compile. */
	private final List<DocumentError> errors = new ArrayList<DocumentError>();
	
	/** All the warnings (non-fatal errors) that have been found. These are errors that can be ignored, and therefore the document can still be compiled. */
	private final List<DocumentError> warnings = new ArrayList<DocumentError>();
	
	/** The current line number that is being parsed. */
	private int currentLineNumber;
	
	/** Whether the current text processor is inside a block comment. */
	private boolean insideBlockComment;
	/** Used for syntax highlighting, the absolute start position of the block comment. */
	private int blockCommentStart;
	
	// Not final because we create a different one when including file
	private ArgScriptLine line = new ArgScriptLine(this);
	
	/** A method used for generating syntax highlighting for a line. */
	private LineHighlighter lineHighlighter = (syntax, line, lineNumber, isBlock) -> {
		line.addSyntaxForKeyword(syntax, lineNumber, isBlock);
		line.addOptionsSyntax(syntax, lineNumber);
	};
	
	/** The object that contains information for syntax highlighting. */
	private final SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();
	
	/** The object that contains information for syntax highlighting, but only for comments since they must be added after. */
	private final SyntaxHighlighter commentsSyntax = new SyntaxHighlighter();
	
	/** The object that contains information for syntax highlighting, but only for variables since they must be added after. */
	private final SyntaxHighlighter variablesSyntax = new SyntaxHighlighter();
	
	/** An object generated every time the stream is processed, which keeps information for the structure of the document. 
	 * In ArgScript streams, all commands and blocks are added in the structure; first-level elements keep the keyword and arguments as the description,
	 * whereas the rest only keeps the keywords. */
	private DocumentStructure documentStructure;
	
	private final List<HyperlinkData> hyperlinkData = new ArrayList<HyperlinkData>();
	
	private final List<DocumentFragment> documentBlockFragments = new ArrayList<DocumentFragment>();
	
	private final List<Integer> linePositions = new ArrayList<Integer>();
	private final List<Integer> lineEnds = new ArrayList<Integer>();
	
	private boolean isIncluding; 
	
	private boolean isFastParsing;
	
	// Used internally
	private TextPositionMap commentTracker;
	
	/** A method used to generate descriptions for structure fragments. By default it shows the full line on 
	 * first-level commands/blocks and only the keyword on everything else, but the user can override this functionality. */
	private StructureNameFactory<ArgScriptLine> nameFactory = (fragment, text, line) -> {
		if (line == null) {
			// A special parsed line, just return it
			return text;
		}
		
		if (fragment.getLevel() == 1) {
			// First level, show the full line
			StringBuilder sb = new StringBuilder();
			
			sb.append(line.getKeyword());
			List<String> splits = line.getSplits();
			for (int i = 0; i < line.getArgumentCount(); i++) {
				sb.append(' ');
				sb.append(splits.get(1 + i));
			}
			
			return sb.toString();
		}
		else {
			return line.getKeyword();
		}
	};
	
	/**
	 * Sets the method used to generate descriptions for structure fragments. It's called on every command (including those that
	 * start a block) that is found. The method receives the fragment, the text of that line and the ArgScriptLine object (except
	 * if it's a special parsed line).
	 * @param nameFactory
	 */
	public void setStructureNameFactory(StructureNameFactory<ArgScriptLine> nameFactory) {
		this.nameFactory = nameFactory;
	}
	
	/**
	 * Sets the method used to generate syntax highlighting for an ArgScriptLine.
	 * The method receives the syntax object, the line, the line number and whether the line is a block command.
	 * @param lineHighlighter
	 */
	public void setLineHighlighter(LineHighlighter lineHighlighter) {
		this.lineHighlighter = lineHighlighter;
	}
	
	public boolean isFastParsing() {
		return isFastParsing;
	}
	
	public void setFastParsing(boolean isFastParsing) {
		this.isFastParsing = isFastParsing;
	}
	
	//TODO check if this works: the position of a word changes when we replace variables!
	
	
	public ArgScriptLine toLine(String text) {
		commentTracker = new TextPositionMap();
		text = removeComments(text, commentTracker);
		
		if (text.isEmpty()) {
			return null;
		}
		
		TextPositionMap positionTracker = new TextPositionMap();
		positionTracker.addAll(commentTracker);
		StringBuilder dst = new StringBuilder();
		if (!replaceVariables(text.toCharArray(), dst, commentTracker, positionTracker)) {
			return null;
		}
		
		text = dst.toString();
		
		// If there are no errors
		if (line.fromLine(text, positionTracker)) {
			return line;
		}
		else {
			return null;
		}
	}
	
	private String removeComments(String line, TextPositionMap tracker) {
		
		/** The index of the # character. */
		int index = -1;
		
		/** The last starting position of non-comment text. */
		int startIndex = 0;
		
		/** Whether we still have text to write or not. */
		boolean writeEnd = !insideBlockComment;
		
		/** Are we inside a line comment? */
		boolean insideComment = false;
		
		StringBuilder sb = new StringBuilder();
		
		while ((index = line.indexOf("#", index+1)) != -1) {
			
			// Does it have another character after it (so it could be a block comment) ?
			if (index+1 < line.length()) {
				
				// Is it beginning a block comment?
				char c = line.charAt(index+1);
				
				if (c == '<') {
					// #<, the beginning of a block comment
					
					// If we are already inside a block comment, there's no need to do anything
					if (!insideBlockComment) {
						// Copy the text until now
						sb.append(line.substring(startIndex, index));
						insideBlockComment = true;
						blockCommentStart = commentsSyntax.getLinePosition(currentLineNumber) + index;
						
						// Tell that we don't need to write the rest of the line
						writeEnd = false;
					}
					
					continue;
				}
				else if (c == '>') {
					// #>, the end of a block comment
					
					// If we are not inside a block comment, raise an error
					if (!insideBlockComment) {
						addError(new DocumentError("Missing start of block comment (#<).", index, index+2));
						return null;
					}
					
					// End the comment, we can write from this position
					insideBlockComment = false;
					startIndex = index + 2;
					
					// End the syntax highlighting block
					if (hasSyntaxHighlighting()) {
						commentsSyntax.add(blockCommentStart, commentsSyntax.getLinePosition(currentLineNumber) + startIndex - blockCommentStart, Collections.singleton(SYNTAX_COMMENT));
					}
					
					// Tell that we still have text to write
					writeEnd = true;
					
					// Keep track that when the position in the text is required (the current length of sb), the real position was the startIndex
					if (tracker != null) tracker.addEntry(sb.length(), startIndex);
					
					continue;
				}
			}
			
			// If we have reached here, this is a normal comment #
			// Only ignore the rest of the line if we are not already inside a comment
			if (!insideBlockComment && !insideComment) {
				// The rest of the line gets ignored
				writeEnd = false;
				// Copy until the comment
				sb.append(line.substring(startIndex, index));
				
				// Add the syntax highlighting
				if (hasSyntaxHighlighting()) commentsSyntax.add(currentLineNumber, index, line.length() - index, Collections.singleton(SYNTAX_COMMENT));
				
				insideComment = true;
			}
		}
		
		if (writeEnd) {
			sb.append(line.substring(startIndex));
		}
		
		return sb.toString();
	}
	
	public void addDefaultParsers() {
		DefaultParsers.addDefaultParsers(this);
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
	public T getData() {
		return data;
	}
	
	public void setOnStartAction(OnStartAction<T> onStartAction) {
		this.onStartAction = onStartAction;
	}
	
	public SyntaxHighlighter getSyntaxHighlighter() {
		return syntaxHighlighter;
	}
	
	public void clearErrors() {
		errors.clear();
		warnings.clear();
	}
	
	private void resetStatus() {
		currentLineNumber = 0;
		errors.clear();
		warnings.clear();
		
		insideBlockComment = false;
		
		linePositions.clear();
		lineEnds.clear();
		
		hyperlinkData.clear();
		
		documentBlockFragments.clear();
		
		specialBlocks.clear();
		nestedBlocks.clear();
		
		if (onStartAction != null) {
			onStartAction.onStart(this, data);
		}
	}
	
	public void process(String text) {
		Matcher matcher = NEWLINE_PATTERN.matcher(text);
		List<String> lines = new ArrayList<String>();
		
		List<Integer> linePositions = new ArrayList<Integer>();
		List<Integer> lineEnds = new ArrayList<Integer>();
		linePositions.add(0);
		
		while (matcher.find()) {
			linePositions.add(matcher.end());
			lineEnds.add(matcher.start());
		}
		
		lineEnds.add(text.length());
		
		for (int i = 0; i < linePositions.size(); i++) {
			lines.add(text.substring(linePositions.get(i), lineEnds.get(i)));
		}
		
		if (!isIncluding) {
			resetStatus();
			
			this.linePositions.addAll(linePositions);
			this.lineEnds.addAll(lineEnds);
			
			syntaxHighlighter.setText(text, linePositions);
			commentsSyntax.setText(text, linePositions);
			variablesSyntax.setText(text, linePositions);
			
			if (!isFastParsing) documentStructure = new DocumentStructure(text);
		}
		
		for (String line : lines) {
			try {
				processLine(line);
			} catch (Exception e) {
				e.printStackTrace();
				addError(new DocumentError(e.getMessage(), 0, line.length()));
			}
			
			if (!isIncluding) currentLineNumber++;
		}
		
		if (!isIncluding && insideBlockComment) {
			int lineNumber = 0;
			while (lineNumber < lines.size() && linePositions.get(lineNumber) <= blockCommentStart) 
				++lineNumber;
			
			lineNumber--;
			this.addError(new DocumentError("Block comment not closed. Close the comment with #>", 0, lines.get(lineNumber).length(), lineNumber));
		}
		
		if (hasSyntaxHighlighting()) {
			// First add variables, then comments
			syntaxHighlighter.addExtras(variablesSyntax, true);
			// Add the comments syntax highlighting, removing any previous style if necessary
			syntaxHighlighter.addExtras(commentsSyntax, true);
		}
	}
	
	// Similar to process, but this restores the line number, errors, etc after it
	public void includeFile(File file) throws Exception {
		
		List<DocumentError> errors = protectedParsing(() -> {
			process(file);
		});
		if (!errors.isEmpty()) {
			addError(line.createError("Cannot include file: " + errors.get(0).getMessage()));
		}
	}
	
	@FunctionalInterface
	public interface ParsingRunnable {
	    void run() throws Exception;
	}
	
	public List<DocumentError> protectedParsing(ParsingRunnable runnable) throws Exception {
		boolean oldIncluding = isIncluding;
		isIncluding = true;
		TextPositionMap oldCommentTracker = commentTracker;
		int oldErrorsSize = errors.size();
		int oldWarningsSize = warnings.size();
		ArgScriptLine oldLine = line;
		line = new ArgScriptLine(this);
		
		runnable.run();
		
		line = oldLine;
		isIncluding = oldIncluding;
		commentTracker = oldCommentTracker;
		
		List<DocumentError> foundErrors = new ArrayList<DocumentError>();
		for (int i = oldErrorsSize; i < errors.size(); i++) {
			foundErrors.add(errors.remove(i));
		}
		for (int i = oldWarningsSize; i < warnings.size(); i++) {
			warnings.remove(i);
		}
		return foundErrors;
	}
	
	public void process(File file) throws IOException {
		process(new String(Files.readAllBytes(file.toPath())));
	}
	
	// Used internally to get the positions of comments
	TextPositionMap getCommentTracker() {
		return commentTracker;
	}
	
	public boolean processLine(String text) {
		
		commentTracker = new TextPositionMap();
		text = removeComments(text, commentTracker);
		
		if (text == null) return false;
		
		// We don't save the trimmed text for syntax highlighting reasons
		if (text.trim().isEmpty()) {
			return true;
		}
		
		//TODO document structure
		// Handle special blocks
		if (!specialBlocks.isEmpty()) {
			// Build keyword
			StringBuilder sb = new StringBuilder();
			int index = 0;
			int length = text.length();
			
			while (index < length && Character.isWhitespace(text.charAt(index))) {
				index++;
			}
			
			int startIndex = index;
			
			while (index < length && !Character.isWhitespace(text.charAt(index))) {
				sb.append(text.charAt(index));
				index++;
			}
			
			ArgScriptSpecialBlock.Pair<T> last = specialBlocks.get(specialBlocks.size() - 1);
			
			if (sb.toString().equals(last.value)) {
				last.key.onBlockEnd();
				
				addStructureEND();
				
				if (hasSyntaxHighlighting()) {
					syntaxHighlighter.add(currentLineNumber, startIndex, last.value.length(), Collections.singleton(ArgScriptStream.SYNTAX_BLOCK));
				}
				
				return true;
			}
			
			if (last.key.processLine(text)) {
				addStructure(null, text, false);
				return true;
			}
		}
		
		TextPositionMap positionTracker = new TextPositionMap();
		positionTracker.addAll(commentTracker);
		StringBuilder dst = new StringBuilder();
		if (!replaceVariables(text.toCharArray(), dst, commentTracker, positionTracker)) {
			return false;
		}
		
		text = dst.toString();
		
		// Only parse it if there are no errors
		if (line.fromLine(text, positionTracker)) {
			
			if (!line.isEmpty()) {
				
				String keyword = line.getKeyword().toLowerCase();
				ArgScriptParser<T> parser = null;
				
				ListIterator<ArgScriptBlock<T>> it = nestedBlocks.listIterator(nestedBlocks.size());
				
				while (it.hasPrevious()) {
					ArgScriptBlock<T> block = it.previous();
					
					parser = block.getParser(keyword);
					if (parser != null) {
						break;
					}
				}
				
				if (parser == null/* && checkStreamParsers*/) {
					parser = parsers.getOrDefault(keyword, null);
				}
				
				if (parser == null) {
					addError(line.createErrorForKeyword(String.format("Unrecognised command '%s'.", keyword)));
					return false;
				}
				
				// We want to do special syntax highlighting on end keywords,
				// so we will store the number of blocks before and after parsing the line;
				// if they are different, then it was a block
				int blocksCount = nestedBlocks.size();
				
				parser.parse(line);
				
				boolean isBlock = parser.isBlock();
				if (!isBlock && blocksCount != nestedBlocks.size()) {
					// It's part of a block but not the parser: it's the end keyword
					isBlock = true;
					addStructureEND();
				}
				else {
					addStructure(line, text, isBlock);
				}
				
				addSyntax(line, isBlock);
			}
		}
		
		return true;
	}
	
	/**
	 * Converts a piece of text into a usable ArgScript line object. This does not change the state of the ArgScript stream
	 * nor it uses it for anything: this means it can be used in any place without altering the stream.
	 * More specifically, this method:
	 * <li>Removes the both block and line comments from the text.
	 * <li>Replaces the variables, if any.
	 * <li>Converts the text into an {@link ArgScriptLine}.
	 * The line object is only returned if there are no errors and the line is not empty.
	 * @param text
	 * @return
	 */
	public ArgScriptLine generateLine(String text) {
		
		TextPositionMap commentTracker = new TextPositionMap();
		text = removeComments(text, commentTracker);
		
		if (text.trim().isEmpty()) {
			return null;
		}
		
		
		TextPositionMap positionTracker = new TextPositionMap();
		positionTracker.addAll(commentTracker);
		StringBuilder dst = new StringBuilder();
		if (!replaceVariables(text.toCharArray(), dst, commentTracker, positionTracker)) {
			return null;
		}
		
		text = dst.toString();
		
		// Don't keep track of the stream as that might add errors and such
		ArgScriptLine line = new ArgScriptLine(null);
		
		if (line.fromLine(text, positionTracker) && !line.isEmpty()) {
			return line;
		}
		else {
			return null;
		}
	}
	
	public void addSyntax(ArgScriptLine line, boolean isBlock) {
		if (hasSyntaxHighlighting()) {
			lineHighlighter.syntax(syntaxHighlighter, line, currentLineNumber, isBlock);
			line.addOptionWarnings();
		}
	}
	
	private void addStructureEND() {
		// Modify the last block fragment to change the end position
		if (hasSyntaxHighlighting() && !documentBlockFragments.isEmpty()) {
			DocumentFragment fragment = documentBlockFragments.get(documentBlockFragments.size() - 1);
			fragment.setEnd(lineEnds.get(currentLineNumber));
			
			documentBlockFragments.remove(documentBlockFragments.size() - 1);
		}
	}
	
	private void addStructure(ArgScriptLine line, String text, boolean isBlock) {
		if (hasSyntaxHighlighting()) {
			DocumentFragment fragment = new DocumentFragment(documentStructure);
			
			if (!documentBlockFragments.isEmpty()) {
				documentBlockFragments.get(documentBlockFragments.size() - 1).addRaw(fragment);
			}
			else {
				documentStructure.add(fragment);
			}
			
			if (isBlock) {
				fragment.setStart(linePositions.get(currentLineNumber));
				fragment.setEditPosition(lineEnds.get(currentLineNumber));
				// The end position will be filled when the 'end' keyword is found
				
				documentBlockFragments.add(fragment);
			}
			else {
				fragment.setStart(linePositions.get(currentLineNumber));
				fragment.setEnd(lineEnds.get(currentLineNumber));
			}
			
			fragment.setDescription(nameFactory.createName(fragment, text, line));
		}
	}
	
	public void setFolder(File folder) {
		this.folder = folder;
	}
	
	public File getFolder() {
		return folder;
	}
	
	
	/* -- DEFINITIONS -- */
	
	/**
	 * Adds the given definition to the stream, so that it can be used with its name.
	 */
	public void addDefinition(ArgScriptDefinition definition) {
		definitions.put(definition.getName(), definition);
	}
	
	/**
	 * Removes the definition that is assigned to the given name, if any.
	 * @param name
	 * @return True if the definition was removed, false if it didn't exist.
	 */
	public boolean removeDefinition(String name) {
		return definitions.remove(name) != null;
	}
	
	/**
	 * Gets the definition with the given name, or null if no definition with that name exists.
	 * @param name
	 * @return
	 */
	public ArgScriptDefinition getDefinition(String name) {
		return definitions.getOrDefault(name, null);
	}
	
	
	/* -- ----------- -- */

	
	/* -- PARSERS -- */

	/**
	 * Assigns a parser to the given keyword, so that when a new line is found that starts with that keyword,
	 * the specified object will be used to process it. The method 'setData' will be called on the parser with
	 * the current data. The keywords are case-insensitive.
	 * @param keyword The keyword that is used to detect a line that must use this parser.
	 * @param parser The parser used to process lines that use the keyword.
	 * @throws NullPointerException If no data has been set in this ArgScript stream.
	 */
	public void addParser(String keyword, ArgScriptParser<T> parser) {
		if (data == null) {
			throw new NullPointerException("Cannot add parsers before setting the ArgScript data.");
		}
		
		parsers.put(keyword.toLowerCase(), parser);
		
		parser.setData(this, data);
	}
	
	public void addParser(ArgScriptParser<T> parser, String ... keywords) {
		if (data == null) {
			throw new NullPointerException("Cannot add parsers before setting the ArgScript data.");
		}
		
		for (String keyword : keywords) parsers.put(keyword.toLowerCase(), parser);
		
		parser.setData(this, data);
	}
	
	/**
	 * Returns the parser that is assigned to the specified keyword, if any. It returns the parser
	 * that will be used to process a line that starts with that keyword. If no parser is assigned to that keyword,
	 * it returns null. The keywords are case-insensitive.
	 * @param keyword
	 */
	public ArgScriptParser<T> getParser(String keyword) {
		return parsers.getOrDefault(keyword.toLowerCase(), null);
	}
	
	/**
	 * Removes the parser assigned to the given keyword, if any. The keywords are case-insensitive.
	 * @param keyword The keyword that is used to detect a line that must use the parser that will be removed.
	 */
	public void removeParser(String keyword) {
		parsers.remove(keyword.toLowerCase());
	}
	
	/**
	 * Removes the specified parser from all the keywords it's assigned to, if any.
	 * @param parser The parser object that will be removed.
	 */
	public void removeParser(ArgScriptParser<T> parser) {
		for (HashMap.Entry<String, ArgScriptParser<T>> entry : parsers.entrySet()) {
			
			if (entry.getValue() == parser) {
				parsers.remove(entry.getKey());
			}
		}
	}
	
	/**
	 * Notifies the stream that a block command has been found, and starts parsing it.
	 * This will allow the block to be notified when the 'end' keyword is found. The commands within the block
	 * will use the parsers that are added in the block.
	 * @param block
	 */
	public void startBlock(ArgScriptBlock<T> block) {
		nestedBlocks.add(block);	
	}
	
	/**
	 * Notifies the stream that the current block has ended and must not be used anymore.
	 */
	public void endBlock() {
		ArgScriptBlock<T> block = nestedBlocks.get(nestedBlocks.size() - 1);
		block.onBlockEnd();
		nestedBlocks.remove(nestedBlocks.size() - 1);
	}
	
	/**
	 * Notifies the stream that a special block command has been found, and starts parsing it.
	 * This will allow the block to be notified when the specified end keyword is found. The lines within the
	 * block can be parsed by the special block.
	 * @param block
	 */
	public void startSpecialBlock(ArgScriptSpecialBlock<T> block, String endKeyword) {
		specialBlocks.add(new ArgScriptSpecialBlock.Pair<T>(block, endKeyword));	
	}
	
	/**
	 * Notifies the stream that the current special block has ended and must not be used anymore.
	 */
	public void endSpecialBlock() {
		specialBlocks.remove(specialBlocks.size() - 1);
	}
	
	/**
	 * Tells whether the parser is currently inside a block.
	 */
	public boolean insideBlock() {
		return !nestedBlocks.isEmpty();
	}
	
	/* -- ------- -- */

	
	/* -- VERSION -- */
	
	/**
	 * Sets the minimum and versions this script can have. This is only used when the command <code>version</code> is used,
	 * raising an error if the version is not supported.
	 */
	public void setVersionRange(int minVersion, int maxVersion) {
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
	}
	
	/**
	 * Returns the minimum version this script must have. This is only used when the command <code>version</code> is used,
	 * raising an error if the version is not supported.
	 */
	public int getMinVersion() {
		return minVersion;
	}
	
	/**
	 * Returns the maximum version this script can have. This is only used when the command <code>version</code> is used,
	 * raising an error if the version is not supported.
	 */
	public int getMaxVersion() {
		return maxVersion;
	}
	

	/**
	 * Returns the version of the stream, which must be in the range [minVersion, maxVersion].
	 * @return
	 */
	public int getVersion() {
		return version;
	}

	/** 
	 * Sets the version of the stream, which must be in the range [minVersion, maxVersion].
	 * @param intValue
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/* -- ------ -- */
	
	
	/* -- VARIABLES -- */
	
	/**
	 * Returns the value that is assigned to the given variable name, or null if the variable is not assigned.
	 * The following cases are accepted:
	 * <li>If the name begins with the symbol ':', then it is a global variable.
	 * <li>If the name contains a scope, the variable will only be searched within that scope. For example,
	 * <code>Intel:card1</code> only accepts the variable <code>card1</code> inside the <code>Intel</code> scope.
	 * <li>If the name is not scoped, it is considered inside the local namespace (if any).
	 * @param name The variable name.
	 * @return The value assigned to the variable, or null if it is not assigned.
	 */
	public String getVariable(String name) {
		if (name.startsWith(":")) {
			// It's a global variable
			return globalVariables.getOrDefault(name.substring(1), null);
		}
		else {
			// First check without scope (generally, people does not use namespaces)
			String value = variables.getOrDefault(name, null);
			if (value != null) return value;
			
			// To contain the namespaces
			StringBuilder sb = new StringBuilder();
			
			// We want to try the parent namespaces first
			for (String scope : scopes) {
				sb.append(scope);
				sb.append(":");
				
				value = variables.getOrDefault(sb.toString() + name, null);
				if (value != null) return value;
			}
			
			return null;
		}
	}
	
	/**
	 * Assigns a value to the specified variable. The value is a string which can contain references to other variables. 
	 * The variable will be added to the current scope. If there's an error while processing the value (i.e. replacing variables),
	 * the errors will be added to the stream and this method will do nothing.
	 * @param name The name of the variable.
	 * @param value The string value that will be assigned to the variable.
	 */
	public void setVariable(String name, String value) {
		StringBuilder sb_value = new StringBuilder();
		StringBuilder sb_name = new StringBuilder();
		
		for (String scope : scopes) {
			sb_name.append(scope);
			sb_name.append(':');
		}
		
		sb_name.append(name);
		
		if (this.replaceVariables(value.toCharArray(), sb_value, null, null)) {
			this.variables.put(name.toString(), value);
		}
	}
	
	/**
	 * Assigns a value to the specified global variable. The value is a string which can contain references to other variables. 
	 * If there's an error while processing the value (i.e. replacing variables),
	 * the errors will be added to the stream and this method will do nothing.
	 * @param name The name of the global variable.
	 * @param value The string value that will be assigned to that variable.
	 */
	public void setGlobalVariable(String name, String value) {
		StringBuilder sb_value = new StringBuilder();
		
		if (this.replaceVariables(value.toCharArray(), sb_value, null, null)) {
			this.variables.put(name, value);
		}
	}
	
	/* -- ------- -- */
	
	
	/* -- SCOPES -- */
	
	/** 
	 * Starts a scope (namespace) with the given name. Scopes keep variables that can only be used within that namespace.
	 * The scope will be created within the current scope.
	 * @param scopeName
	 */
	public void startScope(String scopeName) {
		scopes.add(scopeName);
	}
	
	/** 
	 * Ends the current scope, returning to the previous namespace.
	 */
	public void endScope() {
		scopes.remove(scopes.size() - 1);
	}
	
	/**
	 * Removes all the variables that are defined inside the specified scope.
	 * @param scope The string that represents the scope, such as <code>Intel:GraphicCards</code>.
	 */
	public void purgeScope(String scope) {
		for (String key : variables.keySet()) {
			
			if (key.startsWith(scope)) {
				variables.remove(key);
			}
		}
	}
	
	/* -- ------ -- */
	
	
	/* -- LEXER -- */
	
	/**
	 * Returns the lexer object used to process and decode mathematical/boolean expressions.
	 * @return
	 */
	public ArgScriptLexer getLexer() {
		return lexer;
	}
	
	/**
	 * Parses a boolean expression and returns its value. The expression can use:
	 * <li>The keywords <code>true, on, false, off</code> to express the value.
	 * <li>The logical operators <code>or, and, not</code>
	 * <li>The comparison operations <code>==, !=</code>. 
	 * <li>Integer expressions where 0 is false and 1 is true.
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the boolean expression.
	 */
	public Boolean parseBoolean(ArgScriptArguments args, int index) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a double at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		String text = args.get(index);
		text = text.trim();
		if (text.isEmpty()) {
			addError(new DocumentError("Empty expression.", args.getPosition(index), args.getPosition(index)+1));
			return null;
		}
		
		try {
			lexer.setText(text);
			boolean result = lexer.parseBoolean();
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return result;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return null;
		}
	}
	
	/**
	 * Parses an integer expression and returns its value. The expression can use:
	 * <li>Decimal or hexadecimal numbers, such as <code>8000, 0x6A</code>.
	 * <li>The integer operators <code>+ - * / % ^</code>.
	 * <li>Integer functions: <code>abs, floor, ceil, round, sqr</code> or any custom integer function.
	 * <li>The boolean keywords <code>true, on</code> (1), <code>false, off</code> (0).
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * If the parsed number is out of the range (-2147483648, 2147483647), an error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the integer expression.
	 */
	public Integer parseInt(ArgScriptArguments args, int index) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected an int at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		String text = args.get(index);
		
		if (text.isEmpty()) {
			addError(new DocumentError("Empty expression.", args.getPosition(index), args.getPosition(index)+1));
			return null;
		}
		
		try {
			lexer.setText(text);
			long value = lexer.parseInteger();
			
			// We have to check the range differently. Some int32 properties use hashes, which might return positive longs
			if ((value & 0xFFFFFFFFL) != value) {
				// The value cannot be represented in 32 bits
				if (value > Integer.MAX_VALUE) {
					addError(new DocumentError("Maximum integer value is 2147483647.", args.getPosition(index), args.getEndPosition(index)));
					return null;
				}
				
				if (value < Integer.MIN_VALUE) {
					addError(new DocumentError("Minimum integer value is -2147483648.", args.getPosition(index), args.getEndPosition(index)));
					return null;
				}
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return (int) value;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return null;
		}
	}
	
	public Byte parseByte(ArgScriptArguments args, int index) {
		Integer result = parseInt(args, index, Byte.MIN_VALUE, Byte.MAX_VALUE);
		if (result == null) return null;
		
		return result.byteValue();
	}
	
	public Integer parseUByte(ArgScriptArguments args, int index) {
		Integer result = parseInt(args, index, 0, 255);
		if (result == null) return null;
		
		return result;
	}
	
	public Long parseLong(ArgScriptArguments args, int index) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a 64-bit int at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		String text = args.get(index);
		
		if (text.isEmpty()) {
			addError(new DocumentError("Empty expression.", args.getPosition(index), args.getPosition(index)+1));
			return null;
		}
		
		try {
			long value = Long.parseLong(text.trim());
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return value;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return null;
		}
	}
	
	/**
	 * Parses an unsigned integer expression and returns its value. The expression can use:
	 * <li>Decimal or hexadecimal numbers, such as <code>8000, 0x6A</code>.
	 * <li>The integer operators <code>+ - * / % ^</code>.
	 * <li>Integer functions: <code>abs, floor, ceil, round, sqr</code> or any custom integer function.
	 * <li>The boolean keywords <code>true, on</code> (1), <code>false, off</code> (0).
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * If the parsed number is out of the range (0, 4294967295), an error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the unsigned integer expression.
	 */
	public Long parseUInt(ArgScriptArguments args, int index) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a uint at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		String text = args.get(index);
		
		if (text.isEmpty()) {
			addError(new DocumentError("Empty expression.", args.getPosition(index), args.getPosition(index)+1));
			return null;
		}
		
		try {
			lexer.setText(text);
			long value = lexer.parseInteger();
			
			if (value > 4294967295l) {
				addError(new DocumentError("Maximum unsigned integer value is 4294967295.", args.getPosition(index), args.getEndPosition(index)));
				return null;
			}
			
			if (value < 0) {
				addError(new DocumentError("Minimum integer value is 0.", args.getPosition(index), args.getEndPosition(index)));
				return null;
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return value;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return null;
		}
	}
	
	/**
	 * Parses an integer expression and returns its value, raising an error if the number is out of range.. The expression can use:
	 * <li>Decimal or hexadecimal numbers, such as <code>8000, 0x6A</code>.
	 * <li>The integer operators <code>+ - * / % ^</code>.
	 * <li>Integer functions: <code>abs, floor, ceil, round, sqr</code> or any custom integer function.
	 * <li>The boolean keywords <code>true, on</code> (1), <code>false, off</code> (0).
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * If the parsed number is out of the range (minValue, maxValue), an error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param minValue The minimum value the parsed integer must have.
	 * @param maxValue The maximum value the parsed integer can have.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the integer expression.
	 */
	public Integer parseInt(ArgScriptArguments args, int index, long minValue, long maxValue) {
		Integer value = parseInt(args, index);
		if (value == null) return null;
		
		if (value > maxValue || value < minValue) {
			addError(new DocumentError(String.format("Integer out of the range (%d, %d).", minValue, maxValue), args.getPosition(index), args.getEndPosition(index)));
			return null;
		}
		
		return value;
	}
	
	/**
	 * Parses an integer expression and returns its value, raising an error if the number is out of range.. The expression can use:
	 * <li>Decimal or hexadecimal numbers, such as <code>8000, 0x6A</code>.
	 * <li>The integer operators <code>+ - * / % ^</code>.
	 * <li>Integer functions: <code>abs, floor, ceil, round, sqr</code> or any custom integer function.
	 * <li>The boolean keywords <code>true, on</code> (1), <code>false, off</code> (0).
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * If the parsed number is out of the range (minValue, maxValue), an error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param minValue The minimum value the parsed integer must have.
	 * @param maxValue The maximum value the parsed integer can have.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the integer expression.
	 */
	public Long parseUInt(ArgScriptArguments args, int index, long minValue, long maxValue) {
		Long value = parseUInt(args, index);
		if (value == null) return null;
		
		if (value > maxValue || value < minValue) {
			addError(new DocumentError(String.format("Unsigned integer out of the range (%d, %d).", minValue, maxValue), args.getPosition(index), args.getEndPosition(index)));
			return null;
		}
		
		return value;
	}
	
	/**
	 * Parses a real number (float) expression and returns its value. The expression can use:
	 * <li>The float operators <code>+ - * / % ^</code>.
	 * <li>Any real number: '98.4', '1.34e-9'.
	 * <li>The numbers <code>e, pi</code>.
	 * <li>A real function: <code>sqrt, exp, log, abs, floor, ceil, sqr, pow</code> or any custom function added in the lexer.
	 * <li>A trigonometric function, in radians: <code>sin, cos, tan, asin, acos, atan, atan2</code>.
	 * <li>A trigonometric function, in degrees: <code>sind, cosd, tand, dasin, dacos, datan, datan2</code>.
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the real number (float) expression.
	 */
	public Float parseFloat(ArgScriptArguments args, int index) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a float at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		String text = args.get(index);
		
		if (text.isEmpty()) {
			addError(new DocumentError("Empty expression.", args.getPosition(index), args.getPosition(index)+1));
			return null;
		}
		
		try {
			lexer.setText(text);
			float result = (float) lexer.parseFloat();
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return result;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return null;
		}
	}
	
	/**
	 * Parses a real number (float) expression and returns its value. The expression can use:
	 * <li>The float operators <code>+ - * / % ^</code>.
	 * <li>Any real number: '98.4', '1.34e-9'.
	 * <li>The numbers <code>e, pi</code>.
	 * <li>A real function: <code>sqrt, exp, log, abs, floor, ceil, sqr, pow</code> or any custom function added in the lexer.
	 * <li>A trigonometric function, in radians: <code>sin, cos, tan, asin, acos, atan, atan2</code>.
	 * <li>A trigonometric function, in degrees: <code>sind, cosd, tand, dasin, dacos, datan, datan2</code>.
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the real number (float) expression.
	 */
	public Double parseDouble(ArgScriptArguments args, int index) {
		
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a double at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		String text = args.get(index);
		
		if (text.isEmpty()) {
			addError(new DocumentError("Empty expression.", args.getPosition(index), args.getPosition(index)+1));
			return null;
		}
		
		try {
			lexer.setText(text);
			double result = lexer.parseFloat();
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return result;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return null;
		}
	}
	
	/**
	 * Parses a real number (float) expression and returns its value, raising an error if the number is out of range.. The expression can use:
	 * <li>The float operators <code>+ - * / % ^</code>.
	 * <li>Any real number: '98.4', '1.34e-9'.
	 * <li>The numbers <code>e, pi</code>.
	 * <li>A real function: <code>sqrt, exp, log, abs, floor, ceil, sqr, pow</code> or any custom function added in the lexer.
	 * <li>A trigonometric function, in radians: <code>sin, cos, tan, asin, acos, atan, atan2</code>.
	 * <li>A trigonometric function, in degrees: <code>sind, cosd, tand, dasin, dacos, datan, datan2</code>.
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * If the parsed number is out of the range (minValue, maxValue), an error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param minValue The minimum value the parsed integer must have.
	 * @param maxValue The maximum value the parsed integer can have.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the real number (float) expression.
	 */
	public Float parseFloat(ArgScriptArguments args, int index, float minValue, float maxValue) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a float at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		Float value = parseFloat(args, index);
		if (value == null) return null;
		
		if (value > maxValue || value < minValue) {
			addError(new DocumentError(String.format("Real number out of the range (%f, %f).", minValue, maxValue), args.getPosition(index), args.getEndPosition(index)));
			return null;
		}
		
		return value;
	}
	
	/**
	 * Parses a real number (float) expression and returns its value, raising an error if the number is out of range.. The expression can use:
	 * <li>The float operators <code>+ - * / % ^</code>.
	 * <li>Any real number: '98.4', '1.34e-9'.
	 * <li>The numbers <code>e, pi</code>.
	 * <li>A real function: <code>sqrt, exp, log, abs, floor, ceil, sqr, pow</code> or any custom function added in the lexer.
	 * <li>A trigonometric function, in radians: <code>sin, cos, tan, asin, acos, atan, atan2</code>.
	 * <li>A trigonometric function, in degrees: <code>sind, cosd, tand, dasin, dacos, datan, datan2</code>.
	 * <p>
	 * If there is an error while parsing the expression, the error will be added and the method will return null.
	 * If the parsed number is out of the range (minValue, maxValue), an error will be added and the method will return null.
	 * <p>
	 * This method takes the position of the text in the current line, although it is optional. You can specify it using the
	 * methods in in {@link ArgScriptLine.Arguments}.
	 * @param text The text to be parsed.
	 * @param minValue The minimum value the parsed integer must have.
	 * @param maxValue The maximum value the parsed integer can have.
	 * @param startPosition The starting position of the text within its line.
	 * @return The result of evaluating the real number (float) expression.
	 */
	public Double parseDouble(ArgScriptArguments args, int index, float minValue, float maxValue) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a double at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return null;
		}
		
		Double value = parseDouble(args, index);
		if (value == null) return null;
		
		if (value > maxValue || value < minValue) {
			addError(new DocumentError(String.format("Real number out of the range (%d, %d).", minValue, maxValue), args.getPosition(index), args.getEndPosition(index)));
			return null;
		}
		
		return value;
	}
	
	/**
	 * Parses a vector2 variable; that is, two real numbers separated by a comma and parsed with {@link #parseFloat(String, int)}.
	 * The second value is optional; if it is not present, the first value will be used for all the values in the vector.
	 * @param dst A float array of at least two values where the result will be written.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return True if the vector was parsed successfully, false if there was an error.
	 */
	public boolean parseVector2(ArgScriptArguments args, int index, float[] dst) {
		try {
			if (index >= args.size()) {
				DocumentError error = new DocumentError("Expected a Vector2 at argument position " + index + ".", 
						args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
				addError(error);
				return false;
			}
			
			lexer.setText(args.get(index));
			
			float value0 = (float) lexer.parseFloat();
			dst[0] = value0;
			
			if (lexer.optionalExpect(',')) {
				dst[1] = (float) lexer.parseFloat();
			}
			else {
				dst[1] = dst[0];
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return true;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return false;
		}
	}
	
	/**
	 * Parses a vector3 variable; that is, three real numbers separated by commas and parsed with {@link #parseFloat(String, int)}.
	 * Optionally, only one value can be specified: it will be broadcasted to all the values in the vector.
	 * @param dst A float array of at least three values where the result will be written.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return True if the vector was parsed successfully, false if there was an error.
	 */
	public boolean parseVector3(ArgScriptArguments args, int index, float[] dst) {
		try {
			if (index >= args.size()) {
				DocumentError error = new DocumentError("Expected a Vector3 at argument position " + index + ".", 
						args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
				addError(error);
				return false;
			}
			
			String text = args.get(index);
			
			lexer.setText(text);
			
			float value0 = (float) lexer.parseFloat();
			dst[0] = value0;
			
			if (lexer.optionalExpect(',')) {
				dst[1] = (float) lexer.parseFloat();
				
				lexer.expect(',', "Expected ',' (three values are required).");
				
				dst[2] = (float) lexer.parseFloat();
			}
			else {
				dst[1] = dst[0];
				dst[2] = dst[0];
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return true;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return false;
		}
	}
	
	/**
	 * Parses a vector3 variable; that is, four real numbers separated by commas and parsed with {@link #parseFloat(String, int)}.
	 * Optionally, only one value can be specified: it will be broadcasted to all the values in the vector.
	 * @param dst A float array of at least four values where the result will be written.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return True if the vector was parsed successfully, false if there was an error.
	 */
	public boolean parseVector4(ArgScriptArguments args, int index, float[] dst) {
		String text = args.get(index);
		
		try {
			lexer.setText(text);
			
			float value0 = (float) lexer.parseFloat();
			dst[0] = value0;
			
			if (lexer.optionalExpect(',')) {
				dst[1] = (float) lexer.parseFloat();
				
				lexer.expect(',', "Expected ',' (four values are required).");
				
				dst[2] = (float) lexer.parseFloat();
				
				lexer.expect(',', "Expected ',' (four values are required).");
				
				dst[3] = (float) lexer.parseFloat();
			}
			else {
				dst[1] = dst[0];
				dst[2] = dst[0];
				dst[3] = dst[0];
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			return true;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return false;
		}
	}
	
	/**
	 * Parses a colorRGB variable; that is, three real numbers separated by commas and parsed with {@link #parseFloat(String, int)}.
	 * Optionally, only one value can be specified: it will be broadcasted to all the values in the vector, resulting in a gray color.
	 * @param dst There ColorRGB variable where the result will be written.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return True if the color was parsed successfully, false if there was an error.
	 */
	public boolean parseColorRGB(ArgScriptArguments args, int index, ColorRGB dst) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a ColorRGB at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return false;
		}
		
		String text = args.get(index);
		
		try {
			lexer.setText(text);
			
			float value0 = (float) lexer.parseFloat();
			dst.setR(value0);
			
			if (lexer.optionalExpect(',')) {
				dst.setG((float) lexer.parseFloat());
				
				lexer.expect(',', "Expected ',' (three values are required).");
				
				dst.setB((float) lexer.parseFloat());
			}
			else {
				dst.setG(dst.getR());
				dst.setB(dst.getR());
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			this.addHyperlink(HYPERLINK_COLOR, dst, args.getRealPosition(args.getPosition(index)), args.getRealPosition(args.getEndPosition(index)));
			
			return true;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return false;
		}
	}
	
	/**
	 * Same as {@link #parseColorRGB(ArgScriptArguments, int, ColorRGB)}, but this method expects numbers to be in
	 * the [0, 255] range.
	 * @param args
	 * @param index
	 * @param dst
	 * @return
	 */
	public boolean parseColorRGB255(ArgScriptArguments args, int index, ColorRGB dst) {
		if (parseColorRGB(args, index, dst)) {
			dst.setR(dst.getR() / 255.0f);
			dst.setG(dst.getG() / 255.0f);
			dst.setB(dst.getB() / 255.0f);
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Parses a colorRGBA variable; that is, four real numbers separated by commas and parsed with {@link #parseFloat(String, int)}.
	 * Optionally, the last value (alpha) can be omitted: it is 1 (no transparency) by default.
	 * @param dst The ColorRGBA variable where the result will be written.
	 * @param text The text to be parsed.
	 * @param startPosition The starting position of the text within its line.
	 * @return True if the vector was parsed successfully, false if there was an error.
	 */
	public boolean parseColorRGBA(ArgScriptArguments args, int index, ColorRGBA dst) {
		if (index >= args.size()) {
			DocumentError error = new DocumentError("Expected a ColorRGBA at argument position " + index + ".", 
					args.getRealPosition(args.getStartPosition()), args.getRealPosition(args.getEndPosition()));
			addError(error);
			return false;
		}
		
		String text = args.get(index);
		
		try {
			lexer.setText(text);
			
			dst.setR((float) lexer.parseFloat());
			
			lexer.expect(',', "Expected ',' (three or four values are required).");
			
			dst.setG((float) lexer.parseFloat());
			
			lexer.expect(',', "Expected ',' (three or four values are required).");
			
			dst.setB((float) lexer.parseFloat());
			
			if (lexer.optionalExpect(',')) {
				dst.setA((float) lexer.parseFloat());
			}
			else {
				dst.setA(1.0f);
			}
			
			int endIndex = lexer.getIndex();
			lexer.skipWhitespaces();
			if (lexer.available()) {
				throw new DocumentException(new DocumentError("Garbage at end of expression", endIndex, lexer.getChars().length));
			}
			
			this.addHyperlink(HYPERLINK_COLOR, dst, args.getRealPosition(args.getPosition(index)), args.getRealPosition(args.getEndPosition(index)));
			
			return true;
		} 
		catch (DocumentException e) {
			int startPosition = args.getPosition(index);
			DocumentError error = e.getError();
			error.setStartPosition(args.getRealPosition(error.getStartPosition() + startPosition));
			error.setEndPosition(args.getRealPosition(error.getEndPosition() + startPosition));
			addError(error);
			return false;
		}
	}
	
	/**
	 * Same as {@link #parseColorRGBA(ArgScriptArguments, int, ColorRGBA)}, but this method expects numbers to be in
	 * the [0, 255] range.
	 * @param args
	 * @param index
	 * @param dst
	 * @return
	 */
	public boolean parseColorRGBA255(ArgScriptArguments args, int index, ColorRGBA dst) {
		if (parseColorRGBA(args, index, dst)) {
			dst.setR(dst.getR() / 255.0f);
			dst.setG(dst.getG() / 255.0f);
			dst.setB(dst.getB() / 255.0f);
			dst.setA(dst.getA() / 255.0f);
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean parseInts(ArgScriptArguments args, List<Integer> dst) {
		Integer value = null;
		for (int i = 0; i < args.size(); i++) {
			value = parseInt(args, i);
			if (value == null) return false;
			
			dst.add(value);
		}
		return true;
	}
	
	public boolean parseInts(ArgScriptArguments args, int[] dst) {
		Integer value = null;
		for (int i = 0; i < dst.length; i++) {
			value = parseInt(args, i);
			if (value == null) return false;
			
			dst[i] = value;
		}
		return true;
	}
	
	public boolean parseFloats(ArgScriptArguments args, List<Float> dst) {
		Float value = null;
		for (int i = 0; i < args.size(); i++) {
			value = parseFloat(args, i);
			if (value == null) return false;
			
			dst.add(value);
		}
		return true;
	}
	
	public boolean parseFloat255s(ArgScriptArguments args, List<Float> dst) {
		Float value = null;
		for (int i = 0; i < args.size(); i++) {
			value = parseFloat(args, i);
			if (value == null) return false;
			
			dst.add(value / 255.0f);
		}
		return true;
	}
	
	public boolean parseFloats(ArgScriptArguments args, float[] dst) {
		Float value = null;
		for (int i = 0; i < dst.length; i++) {
			value = parseFloat(args, i);
			if (value == null) return false;
			
			dst[i] = value;
		}
		return true;
	}
	
	public boolean parseColorRGBs(ArgScriptArguments args, List<ColorRGB> dst) {
		for (int i = 0; i < args.size(); i++) {
			ColorRGB color = new ColorRGB();
			if (!parseColorRGB(args, i, color)) return false; 
			
			dst.add(color);
		}
		return true;
	}
	
	/**
	 * Same as {@link #parseColorRGBs(ArgScriptArguments, List)}, but this method expects numbers to be in
	 * the [0, 255] range.
	 * @param args
	 * @param number
	 * @param dst
	 * @return
	 */
	public boolean parseColorRGB255s(ArgScriptArguments args, List<ColorRGB> dst) {
		for (int i = 0; i < args.size(); i++) {
			ColorRGB color = new ColorRGB();
			if (!parseColorRGB255(args, i, color)) return false; 
			
			dst.add(color);
		}
		return true;
	}
	
	public Integer parseFileID(ArgScriptArguments args, int index) {
		try {
			return HashManager.get().getFileHash(args.get(index));
		} catch (Exception e) {
			args.getStream().addError(new DocumentError(e.getLocalizedMessage(), args.getPosition(index), args.getEndPosition(index)));
			return null;
		}
	}
	
	public Integer parsePropertyID(ArgScriptArguments args, int index) {
		try {
			return HashManager.get().getFileHash(args.get(index));
		} catch (Exception e) {
			args.getStream().addError(new DocumentError(e.getLocalizedMessage(), args.getPosition(index), args.getEndPosition(index)));
			return null;
		}
	}
	
	public boolean parseFileIDs(ArgScriptArguments args, List<Integer> dst) {
		Integer value = null;
		for (int i = 0; i < args.size(); i++) {
			value = parseFileID(args, i);
			if (value == null) return false; 
			
			dst.add(value);
		}
		return true;
	}

	public boolean hasSyntaxHighlighting() {
		return !isFastParsing && !isIncluding;
	}
	
	/* -- ----- -- */
	
	
	public void addHyperlink(String type, Object object, int start, int end) {
		if (hasSyntaxHighlighting()) {
			hyperlinkData.add(new HyperlinkData(type, object, currentLineNumber, start, end));
		}
	}
	
	public List<HyperlinkData> getHyperlinkData() {
		return hyperlinkData;
	}
	
	/**
	 * Adds the given error to the compile errors list: that is, all those errors that make the document impossible to compile.
	 * If no line number has been specified in the error, the number of the line that is currently being processed will be used.
	 * @param error
	 */
	public void addError(DocumentError error) {
		if (error.getLine() == -1) {
			error.setLine(currentLineNumber);
		}
		errors.add(error);
	}
	
	/**
	 * Adds the given error to the warnings list: that is, all those errors that can be ignored so that the document can still be compiled.
	 * If no line number has been specified in the error, the number of the line that is currently being processed will be used.
	 * @param error
	 */
	public void addWarning(DocumentError warning) {
		if (warning.getLine() == -1) {
			warning.setLine(currentLineNumber);
		}
		warnings.add(warning);
	}
	
	/**
	 * Returns an unmodifiable list with all the compile errors.
	 */
	public List<DocumentError> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	/**
	 * Returns an unmodifiable list with all the warnings (errors that don't affect compilation).
	 */
	public List<DocumentError> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}
	
	/**
	 * Returns the last added error, if any. 
	 */
	public DocumentError getLastError() {
		if (errors.isEmpty()) {
			return null;
		}
		else {
			return errors.get(errors.size() - 1);
		}
	}
	
//	/** 
//	 * Removes all the errors generated during the last line processing, and returns them in a list.
//	 * Returns an empty list if no errors were generated.
//	 * @return
//	 */
//	public List<DocumentError> removeLastLineErrors() {
//		List<DocumentError> result = new ArrayList<DocumentError>();
//		
//		if (lastLineErrorIndex < errors.size()) {
//			result.addAll(errors.subList(lastLineErrorIndex, errors.size()));
//			
//			errors.removeAll(result);
//		}
//		
//		return result;
//	}
	
	/**
	 * Returns the line number of the line that is currently being processed.
	 * @return
	 */
	public int getCurrentLine() {
		return currentLineNumber;
	}
	
	public DocumentStructure getDocumentStructure() {
		return documentStructure;
	}
	
	
	/**
	 * Replaces all the variables in a given text, writing the text with the variables replaced in the given string builder.
	 * @param text
	 * @param dst
	 * @param sourceTracker Used to keep track of any changed positions in the original text (such as comments).
	 * @param dstTracker The position changes will be added here.
	 * @return Whether the operation succeeded (true) or there were any errors (false).
	 */
	protected boolean replaceVariables(char[] text, StringBuilder dst, TextPositionMap sourceTracker, TextPositionMap dstTracker) {
		int startIndex = 0;
		
		// Are we inside a brace?
		boolean insideBraces = false;
		
		boolean trackPosition = sourceTracker != null && dstTracker != null;
		
		// Keep reading until we find a $, then read the variable name and replace it with the value
		for (int i = 0; i < text.length; i++) {
			
			if (text[i] == '$') {
				
				// Copy the text as it is
				dst.append(text, startIndex, i - startIndex);
				
				if (trackPosition) {
					// Add the position to the tracker so errors can be correctly displayed
					dstTracker.addEntry(dst.length(), sourceTracker.getRealPosition(i));
				}
				
				int syntaxStart = i;
				
				// Eat the character
				i++;
				
				// Check to avoid throwing out of bounds exceptions
				if (i == text.length) {
					addError(new DocumentError("Missing variable name after '$'.", i-1, i));
					return false;
				}
				
				// Is the variable name inside braces?
				if (text[i] == '{') {
					insideBraces = true;
					// Eat the character
					i++;
				}
				
				// Check to avoid throwing out of bounds exceptions
				if (i == text.length) {
					int errorStart = i-2;
					int errorEnd = i;
					
					if (trackPosition) {
						errorStart = dstTracker.getRealPosition(errorStart);
						errorEnd = dstTracker.getRealPosition(errorEnd);
					}
					
					addError(new DocumentError("Missing variable name after '{'; the format should be '${variableName}'.", errorStart, errorEnd));
					return false;
				}
				
				// Get the variable name, accumulating all the accepted characters for a name
				StringBuilder varName = new StringBuilder();
				// The position at which the variable starts, this is for errors
				int varStart = i;
				
				// Keep using the same 'i' variable
				for (; i < text.length; i++) {
					if (Character.isAlphabetic(text[i]) || Character.isDigit(text[i]) || text[i] == '_' || text[i] == ':') {
						varName.append(text[i]);
					}
					else {
						break;
					}
				}
				
				String variableName = varName.toString();
				
				// After getting the variable name, do some checks to ensure it is valid
				if (Character.isDigit(variableName.charAt(0))) {
					int errorStart = varStart;
					int errorEnd = i;
					
					if (trackPosition) {
						errorStart = dstTracker.getRealPosition(errorStart);
						errorEnd = dstTracker.getRealPosition(errorEnd);
					}
					
					addError(new DocumentError(String.format("Invalid variable name '%s': variable names cannot start with a numeric digit.", variableName), errorStart, errorEnd));
					return false;
				}
				
				if (insideBraces) {
					// Ensure the brace is closed now
					if (i == text.length || text[i] != '}') {
						int errorStart = i-1;
						int errorEnd = i;
						
						if (trackPosition) {
							errorStart = dstTracker.getRealPosition(errorStart);
							errorEnd = dstTracker.getRealPosition(errorEnd);
						}
						
						
						addError(new DocumentError(String.format("Missing closing '}' after variable '%s'.", variableName), errorStart, errorEnd));
						return false;
					}
					// Eat the character
					i++;
					
					insideBraces = false;
				}
				
				variablesSyntax.add(currentLineNumber, syntaxStart, i - syntaxStart, Collections.singleton(SYNTAX_VARIABLE));
				
				// Replace the variable with its value
				String value = this.getVariable(variableName);
				if (value == null) {
					int errorStart = varStart;
					int errorEnd = i;
					
					if (trackPosition) {
						errorStart = dstTracker.getRealPosition(errorStart);
						errorEnd = dstTracker.getRealPosition(errorEnd);
					}
					
					addError(new DocumentError(String.format("Unknown variable '%s'.", variableName), errorStart, errorEnd));
					return false;
				}
				
				dst.append(value);
				
				if (trackPosition) {
					// Add the end position to the tracker so errors can be correctly displayed
					dstTracker.addEntry(dst.length(), sourceTracker.getRealPosition(i));
				}
				
				// Prepare for reading plain text again
				startIndex = i;
			}
		}
		
		// Write the remaining text
		if (startIndex < text.length) {
			dst.append(text, startIndex, text.length - startIndex);
		}
		
		return true;
	}

	public void addErrorsSyntax() {
		for (DocumentError error : errors) {
			int start = error.getStartPosition() + syntaxHighlighter.getLinePosition(error.getLine());
			int end = error.getEndPosition() + syntaxHighlighter.getLinePosition(error.getLine());
			syntaxHighlighter.addExtra(start, end - start, DocumentError.STYLE_ERROR, false);
		}
		
		for (DocumentError error : warnings) {
			int start = error.getStartPosition() + syntaxHighlighter.getLinePosition(error.getLine());
			int end = error.getEndPosition() + syntaxHighlighter.getLinePosition(error.getLine());
			syntaxHighlighter.addExtra(start, end - start, DocumentError.STYLE_WARNING, false);
		}
	}

	public List<Integer> getLinePositions() {
		return linePositions;
	}

}
