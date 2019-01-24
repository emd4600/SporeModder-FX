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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;
import sporemodder.view.syntax.SyntaxHighlighter;

public final class ArgScriptLine {
	
	@FunctionalInterface
	public static interface LineHighlighter {
		public void syntax(SyntaxHighlighter syntax, ArgScriptLine line, int lineNumber, boolean isBlock);
	}
	
	private class Option {
		/** The start index of the option in the 'splits' list.
		 * All the values that follow it, until the next option index, belong to an option and its arguments. */ 
		int splitIndex;
		
		/** The number of arguments used by this option. */
		int numArguments;
		
		/** Whether this option has already been used by the user. */
		boolean isUsed;
		
		/** The name used to access this option. */
		String name;
	}
	
	private boolean hasKeyword = true;
	
	/** The underlying stream that is being processed. */
	private ArgScriptStream<?> stream;

	/** The original text of the line. */
	private String text;
	
	/** All the words in the text, separated by spaces. */
	private final List<String> splits = new ArrayList<String>();
	
	/** The amount of Strings in the 'splits' list that are arguments; does not include the keyword. */
	private int numArguments;
	
	/** A list with all the options information. */
	private final List<Option> options = new ArrayList<Option>();
	
	/** This class allows us to correctly store text positions even if there are variable replacements or comment removals. */
	// Not private, default parsers might need it
	final OriginalPositionTracker positionTracker = new OriginalPositionTracker();
	
	/** A list that keeps track of the position in the text of all the words in the 'splits' list. */
	private final List<Integer> splitPositions = new ArrayList<Integer>();
	
	/** Sometimes we need to operate on the text positions and not the original ones. */
	private final TreeMap<Integer, Integer> originalToText = new TreeMap<Integer, Integer>();
	
	/** Similar to splitPositions, but this is for the end of the splits. */
	private final List<Integer> endPositions = new ArrayList<Integer>();
	
	private final LineLexer lexer = new LineLexer(this);
	
	
	public ArgScriptLine(ArgScriptStream<?> stream) {
		this.stream = stream;
	}
	
	/** Returns the number of arguments in the line.
	 * This is the amount of Strings in the 'splits' list that are arguments; does not include the keyword. */
	public int getArgumentCount() {
		return numArguments;
	}
	
	/**
	 * Adds syntax highlighting for the given option, using the ArgScriptStream.SYNTAX_OPTION style.
	 * @param syntax
	 * @param lineNumber
	 * @param option
	 */
	private void addOptionSyntax(SyntaxHighlighter syntax, int lineNumber, Option option) {
		int start = splitPositions.get(option.splitIndex);
		int end = endPositions.get(option.splitIndex);
		
		syntax.add(lineNumber, start, end - start, Collections.singleton(ArgScriptStream.SYNTAX_OPTION));
	}
	
	/**
	 * Adds syntax highlighting for all the options, using the ArgScriptStream.SYNTAX_OPTION style.
	 * @param syntax
	 * @param lineNumber
	 */
	public void addOptionsSyntax(SyntaxHighlighter syntax, int lineNumber) {
		for (Option option : options) {
			addOptionSyntax(syntax, lineNumber, option);
		}
	}
	
	/**
	 * Adds syntax highlighting for the specified word in the line. Words are separated by spaces, the first word is
	 * the keyword.
	 * @param syntax
	 * @param lineNumber
	 * @param splitIndex
	 * @param style
	 */
	public void addSyntaxForWord(SyntaxHighlighter syntax, int lineNumber, int wordIndex, String style) {
		syntax.add(lineNumber, splitPositions.get(wordIndex), endPositions.get(wordIndex) - splitPositions.get(wordIndex), Collections.singleton(style));
	}
	
	/**
	 * Calls {@link #addSyntaxForWord(SyntaxHighlighter, int, int, String)} for the first word, the keyword.
	 * @param syntax
	 * @param lineNumber
	 * @param isBlock
	 */
	public void addSyntaxForKeyword(SyntaxHighlighter syntax, int lineNumber, boolean isBlock) {
		if (hasKeyword) {
			// The keyword
			if (isBlock) {
				addSyntaxForWord(syntax, lineNumber, 0, ArgScriptStream.SYNTAX_BLOCK);
			} else {
				addSyntaxForWord(syntax, lineNumber, 0, ArgScriptStream.SYNTAX_COMMAND);
			}
		}
	}
	
	protected void addSyntaxHighlighting(SyntaxHighlighter syntax, int lineNumber, boolean isBlock) {
		if (splits.isEmpty()) return;
		
		// The keyword
		if (isBlock) {
			syntax.add(lineNumber, splitPositions.get(0), endPositions.get(0) - splitPositions.get(0), Collections.singleton(ArgScriptStream.SYNTAX_BLOCK));
		} else {
			syntax.add(lineNumber, splitPositions.get(0), endPositions.get(0) - splitPositions.get(0), Collections.singleton(ArgScriptStream.SYNTAX_COMMAND));
		}
		
		for (Option option : options) {
			int start = splitPositions.get(option.splitIndex);
			int end = endPositions.get(option.splitIndex);
			
			syntax.add(lineNumber, start, end - start, Collections.singleton(ArgScriptStream.SYNTAX_OPTION));
		}
	}
	
	/** Adds a warning for all those options that have not been used. */
	protected void addOptionWarnings() {
		if (stream == null) return;
		for (Option option : options) {
			if (!option.isUsed) {
				stream.addWarning(new DocumentError("Unused option.", splitPositions.get(option.splitIndex), endPositions.get(option.splitIndex)));
			}
		}
	}
	
	/**
	 * Resets this ArgScriptLine and parses a new one from the given text. Optionally, a position tracker can be given
	 * in case the text is not exactly like the original.
	 * @param line
	 * @param positions
	 * @return
	 */
	public boolean fromLine(String line, OriginalPositionTracker positions) {
		
		// Reset everything
		text = line;
		splits.clear();
		options.clear();
		splitPositions.clear();
		endPositions.clear();
		originalToText.clear();
		positionTracker.clear();
		numArguments = 0;
		
		if (positions != null) {
			positionTracker.addAll(positions);
		}
		
		try {
			lexer.parse(line);
		}
		catch (DocumentException e) {
			if (stream != null) {
				stream.addError(e.getError());
			} else {
				e.printStackTrace();
			}
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns whether this line has a keyword (i.e. the first split word that is not an option) or not.
	 * @return
	 */
	public boolean hasKeyword() {
		return hasKeyword;
	}
	
	/**
	 * Returns all the split words that make up this line.
	 * @return
	 */
	public List<String> getSplits() {
		return splits;
	}
	
	/**
	 * Puts all the split words in an argument list so that they can be used in ArgScriptStream parse methods. 
	 * @param args
	 */
	public void getSplitsAsArguments(ArgScriptArguments args) {
		args.arguments = Collections.unmodifiableList(splits);
		args.numArguments = args.arguments.size();
		args.positions = splitPositions.subList(0, args.numArguments);
		args.endPositions = endPositions.subList(0, args.numArguments);
		args.stream = stream;
		args.tracker = positionTracker;
		args.originalToText = originalToText;
		args.splitIndex = 0;
	}
	
	/**
	 * Tells whether this line is empty or not.
	 */
	public boolean isEmpty() {
		return splits.size() == 0;
	}
	
	/**
	 * Returns the keyword used to identify this command; this is the first word in the line.
	 */
	public String getKeyword() {
		return splits.get(0);
	}
	
	/**
	 * Gets the specified number of arguments from this line. This does not include the keyword. 
	 * If the number of arguments is not equal to the one required, an error will be raised and the method will return false. 
	 * @param args An unmodifiable list with the arguments, or null if the number of arguments does not match.
	 * @param count The required number of arguments.
	 * @return True if the method succeeded, false if there was an error.
	 */
	public boolean getArguments(ArgScriptArguments args, int count) {
		return getArguments(args, count, count);
	}
	
	/**
	 * Gets a variable number of arguments from this line. If the line does not have at least <code>min</code> arguments,
	 * or if it has more than <code>max</code> arguments, an error will be raised and the method will return false.
	 * @param args An unmodifiable list with the arguments, or null if the number of arguments does not match.
	 * @param min The minimum number of arguments required.
	 * @param max The maximum number of arguments required.
	 * @return True if the method succeeded, false if there was an error.
	 */
	public boolean getArguments(ArgScriptArguments args, int min, int max) {
		
		if (numArguments < min) {
			if (stream != null) stream.addError(createErrorUntilOptions(String.format("Expecting at least %d arguments for command %s", min, splits.get(0))));
			return false;
		}
		else if (numArguments > max) {
			if (stream != null) stream.addError(createErrorUntilOptions(String.format("Expecting at most %d arguments for command %s", max, splits.get(0))));
			return false;
		}
		else {
			args.arguments = Collections.unmodifiableList(splits.subList(1, numArguments + 1));
			args.numArguments = args.arguments.size();
			args.positions = splitPositions.subList(1, 1 + args.numArguments);
			args.endPositions = endPositions.subList(1, 1 + args.numArguments);
			args.stream = stream;
			args.tracker = positionTracker;
			args.originalToText = originalToText;
			args.splitIndex = 1;
			return true;
		}
	}
	
	public boolean hasOption(String name) {
		for (Option option : options) {
			if (option.name.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasFlag(String name) {
		for (Option option : options) {
			if (!option.isUsed && option.name.equals(name)) {
				if (option.numArguments != 0) {
					if (stream != null) stream.addError(new DocumentError(String.format("Not expecting any arguments for flag option %s", name),
							splitPositions.get(option.splitIndex), endPositions.get(option.splitIndex)));
					return false;
				}
				
				option.isUsed = true;
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the specified number of arguments from an option in this line. This does not include the option keyword. 
	 * If the number of arguments is not equal to the one required, an error will be raised and the method will return false. 
	 * The method also returns null if the options does not exist.
	 * @param args An unmodifiable list with the arguments, or null if the number of arguments does not match.
	 * @param name The name of the option.
	 * @param count The required number of arguments.
	 * @return True if the method succeeded, false if there was an error.
	 */
	public boolean getOptionArguments(ArgScriptArguments args, String name, int count) {
		return getOptionArguments(args, name, count, count);
	}
	
	/**
	 * Gets a variable number of arguments from an option in this line. If the option does not have at least <code>min</code> arguments,
	 * or if it has more than <code>max</code> arguments, an error will be raised and the method will return false.
	 * The method also returns false if the options does not exist.
	 * @param args An unmodifiable list with the arguments, or null if the number of arguments does not match.
	 * @param min The minimum number of arguments required.
	 * @param max The maximum number of arguments required.
	 * @return True if the method succeeded, false if there was an error.
	 */
	public boolean getOptionArguments(ArgScriptArguments args, String name, int min, int max) {
		
		for (Option option : options) {
			if (!option.isUsed && option.name.equals(name)) {
				// Don't let the user use this option again
				option.isUsed = true;
				
				if (option.numArguments < min) {
					if (stream != null) stream.addError(new DocumentError(String.format("Expecting at least %d arguments for option %s", min, name),
							splitPositions.get(option.splitIndex), endPositions.get(option.splitIndex)));
					return false;
				}
				else if (option.numArguments > max) {
					if (stream != null) stream.addError(new DocumentError(String.format("Expecting at most %d arguments for option %s", max, name),
							splitPositions.get(option.splitIndex), endPositions.get(option.splitIndex)));
					return false;
				}
				else {
					args.arguments = Collections.unmodifiableList(splits.subList(option.splitIndex + 1, option.splitIndex + option.numArguments + 1));
					args.numArguments = args.arguments.size();
					args.positions = splitPositions.subList(option.splitIndex + 1, option.splitIndex + 1 + args.numArguments);
					args.endPositions = endPositions.subList(option.splitIndex + 1, option.splitIndex + 1 + args.numArguments);
					args.stream = stream;
					args.tracker = positionTracker;
					args.originalToText = originalToText;
					args.splitIndex = option.splitIndex + 1;
					
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Creates a new document error/warning with the given message that happens on all the line content.
	 * This does not define the line number.
	 * @param message The message that describes the error.
	 */
	public DocumentError createError(String message) {
		if (splitPositions.isEmpty()) {
			return new DocumentError(message, 0, text.length());
		}
		else {
			return new DocumentError(message, splitPositions.get(0), endPositions.get(endPositions.size() - 1));
		}
	}
	
	/**
	 * Creates a new document error/warning with the given message that happens on the line keyword.
	 * This does not define the line number.
	 * @param message The message that describes the error.
	 */
	public DocumentError createErrorForKeyword(String message) {
		return new DocumentError(message, splitPositions.get(0), endPositions.get(0));
	}
	
	/**
	 * Creates a new document error/warning with the given message that happens on the specified argument.
	 * This does not define the line number.
	 * @param message The message that describes the error.
	 */
	public DocumentError createErrorForArgument(String message, int index) {
		return new DocumentError(message, splitPositions.get(index + 1), endPositions.get(index + 1));
	}
	
	public void addHyperlinkForArgument(String type, int index) {
		stream.addHyperlink(type, splits.get(index + 1), splitPositions.get(index + 1), endPositions.get(index + 1));
	}
	
	public void addHyperlinkForArgument(String type, Object object, int index) {
		stream.addHyperlink(type, object, splitPositions.get(index + 1), endPositions.get(index + 1));
	}
	
	public void addHyperlinkForOptionArgument(String type, Object object, String optionName, int index) {
		Option option = null;
		
		for (Option o : options) {
			if (o.name.equals(optionName)) {
				option = o;
			}
		}
		
		if (option != null) stream.addHyperlink(type, object, splitPositions.get(option.splitIndex + index + 1), endPositions.get(option.splitIndex + index + 1));
	}
	
	/**
	 * Creates a new document error/warning with the given message that happens on the keyword and its arguments, but not
	 * the options.
	 * This does not define the line number.
	 * @param message The message that describes the error.
	 */
	public DocumentError createErrorUntilOptions(String message) {
		// Is there any option?
		if (options.isEmpty()) {
			return createError(message);
		}
		else {
			// Get the start position of the first option, which will be the end position of the error.
			// Subtract one position for the whitespace.
			int endPosition = splitPositions.get(numArguments + 1) - 1; 
			
			return new DocumentError(message, splitPositions.get(0), endPosition);
		}
	}
	
	/**
	 * Creates a new document error/warning with the given message that happens on an option and its arguments.
	 * This does not define the line number.
	 * @param message The message that describes the error.
	 */
	public DocumentError createErrorForOption(String optionName, String message) {
		Option option = null;
		
		for (Option o : options) {
			if (o.name.equals(optionName)) {
				option = o;
			}
		}
		
		if (option != null) {
			return new DocumentError(message, splitPositions.get(option.splitIndex), endPositions.get(option.splitIndex + option.numArguments));
		}
		else {
			return null;
		}
	}
	
	/**
	 * Creates a new document error/warning with the given message that happens on the argument of a certain option.
	 * This does not define the line number.
	 * @param message The message that describes the error.
	 */
	public DocumentError createErrorForOptionArgument(String optionName, String message, int index) {
		Option option = null;
		
		for (Option o : options) {
			if (o.name.equals(optionName)) {
				option = o;
			}
		}
		
		if (option != null) {
			return new DocumentError(message, splitPositions.get(option.splitIndex + index), endPositions.get(option.splitIndex + index));
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Returns the position in the line of the keyword. This can be used for document errors.
	 * @return
	 */
	public int getKeywordPosition() {
		return splitPositions.get(0);
	}

	private int getRealPosition(int position) {
		return positionTracker.getRealPosition(position);
	}
	
	private static class LineLexer {
		
		private ArgScriptLine line;
		private WordSplitLexer ws;
		
		private LineLexer(ArgScriptLine line) {
			this.line = line;
		}
		
		
		private void parse(String text) throws DocumentException {
			
			ws = new WordSplitLexer(text);
			
			Option option = null;
			String optionKeyword = null;
			
			//TODO accept (0,1,2)(0,3,2)
			
			while (ws.index < ws.chars.length) {
				ws.skipWhitespaces();
				if (ws.index == ws.chars.length) {
					break;
				}
				
				int splitPosition = line.getRealPosition(ws.index);
				line.splitPositions.add(splitPosition);
				line.originalToText.put(splitPosition, ws.index);
				
				if (ws.chars[ws.index] == '-') {
					// If we have reached the end of the line, throw an error
					if (ws.index+1 == ws.chars.length) {
						throw new DocumentException(new DocumentError("Expected a number or a name after - sign.", ws.index, ws.index+1));
					}
					// If the next character is whitespace (so no number and no option)
					if (Character.isWhitespace(ws.chars[ws.index+1])) {
						throw new DocumentException(new DocumentError("Expected a number or a name after - sign.", ws.index, ws.index+1));
					}
					
					// If this is really an option, we need at least one alphabetic character
					if (ws.index+1 < ws.chars.length && Character.isAlphabetic(ws.chars[ws.index+1])) {
						
						// Is this the first option we find?
						if (option == null) {
							// Subtract one for the keyword
							line.numArguments = line.splits.size() - 1;
							
							line.hasKeyword = line.numArguments != -1;
						}
						else {
							// Add the previous option before creating a new one
							// Subtract one for the keyword
							option.numArguments = line.splits.size() - option.splitIndex - 1;
							
							option.name = optionKeyword;
							line.options.add(option);
						}
						
						// Eat the - sign
						ws.index++;
						
						option = line.new Option();
						option.splitIndex = line.splits.size();
						
						StringBuilder sb = new StringBuilder();
						// Read the keyword
						while (ws.index < ws.chars.length && (ws.chars[ws.index] == '_' || Character.isAlphabetic(ws.chars[ws.index]) || Character.isDigit(ws.chars[ws.index]))) {
							sb.append(ws.chars[ws.index]);
							ws.index++;
						}
						optionKeyword = sb.toString();
						
						// We don't do the normal parsing, since we just parsed the keyword.
						splitPosition = line.getRealPosition(ws.index);
						line.splits.add("-" + sb.toString());
						line.endPositions.add(splitPosition);
						line.originalToText.put(splitPosition, ws.index);
						continue;
					}
				}
				
				line.splits.add(ws.nextWord());
				
				splitPosition = line.getRealPosition(ws.index);
				line.endPositions.add(splitPosition);
				line.originalToText.put(splitPosition, ws.index);
			}
			
			// Add the last option, if any
			if (option != null) {
				// Subtract one for the keyword
				option.numArguments = line.splits.size() - option.splitIndex - 1;
				
				option.name = optionKeyword;
				line.options.add(option);
			}
			else {
				// If option was null, it means there are no options, so we must do this her
				line.numArguments = line.splitPositions.size() - 1;
			}
		}
	}
	
	public String replaceSplit(String originalText, String newSplit, int index) {
		int start = positionTracker.getRealPosition(splitPositions.get(index));
		int end = positionTracker.getRealPosition(endPositions.get(index));
		
		return originalText.substring(0, start) + newSplit + originalText.substring(end);
	}
}
