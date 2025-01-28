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
package sporemodder.file.argscript;

import java.util.HashMap;

import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;
import sporemodder.file.SimplePattern;

public class ArgScriptLexer {
	
	public static final ArgScriptFunction FUNCTION_HASH = new ArgScriptFunction() {
		@Override
		public double getFloat(ArgScriptLexer lexer) throws DocumentException {
			return getInt(lexer);
		}

		@Override
		public long getInt(ArgScriptLexer lexer) throws DocumentException {
			lexer.expect('(');
			WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
			ws.skipWhitespaces();
			String string = ws.nextReadableWord();
			lexer.setIndex(ws.index);
			
			lexer.skipWhitespaces();
			lexer.expect(')');
			
			return Integer.toUnsignedLong(HashManager.get().getFileHash(string));
		}

		@Override
		public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
			throw new DocumentException(new DocumentError("'hash' is an integer/float function", lexer.getKeywordStartIndex(), lexer.getIndex()));
		}
	};

	private char[] array;
	private int index;
	private int startIndex;
	
	private boolean isHexadecimal;
	
	private final HashMap<String, ArgScriptFunction> functions = new HashMap<String, ArgScriptFunction>();
	
	public void removeFunctions() {
		functions.clear();
	}
	
	public void addDefaultFunctions(ArgScriptStream<?> stream) {
		
		if (stream != null) {
			this.addFunction("varExists", new ArgScriptFunction() {
				@Override
				public double getFloat(ArgScriptLexer lexer) throws DocumentException {
					throw new DocumentException(new DocumentError("'varExists' is a boolean function", lexer.getKeywordStartIndex(), lexer.getIndex()));
				}
	
				@Override
				public long getInt(ArgScriptLexer lexer) throws DocumentException {
					return getBoolean(lexer) ? 1 : 0;
				}
	
				@Override
				public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
					lexer.expect('(');
					
					WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
					ws.skipWhitespaces();
					String word = ws.nextParameter();
					
					lexer.setIndex(ws.index);
					
					lexer.expect(')');
					
					return stream.getVariable(word) != null;
				}
			});
			
			this.addFunction("defExists", new ArgScriptFunction() {
				@Override
				public double getFloat(ArgScriptLexer lexer) throws DocumentException {
					throw new DocumentException(new DocumentError("'defExists' is a boolean function", lexer.getKeywordStartIndex(), lexer.getIndex()));
				}
	
				@Override
				public long getInt(ArgScriptLexer lexer) throws DocumentException {
					return getBoolean(lexer) ? 1 : 0;
				}
	
				@Override
				public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
					lexer.expect('(');
					
					WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
					ws.skipWhitespaces();
					String word = ws.nextParameter();
					
					lexer.setIndex(ws.index);
					
					lexer.expect(')');
					
					return stream.getDefinition(word) != null;
				}
			});
			
			this.addFunction("commandExists", new ArgScriptFunction() {
				@Override
				public double getFloat(ArgScriptLexer lexer) throws DocumentException {
					throw new DocumentException(new DocumentError("'defExists' is a boolean function", lexer.getKeywordStartIndex(), lexer.getIndex()));
				}
	
				@Override
				public long getInt(ArgScriptLexer lexer) throws DocumentException {
					return getBoolean(lexer) ? 1 : 0;
				}
	
				@Override
				public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
					lexer.expect('(');
					
					WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
					ws.skipWhitespaces();
					String word = ws.nextParameter();
					
					lexer.setIndex(ws.index);
					
					lexer.expect(')');
					
					return stream.getParser(word) != null;
				}
			});
		}
		
		this.addFunction("eq", new ArgScriptFunction() {
			@Override
			public double getFloat(ArgScriptLexer lexer) throws DocumentException {
				throw new DocumentException(new DocumentError("'eq' is a boolean function", lexer.getKeywordStartIndex(), lexer.getIndex()));
			}

			@Override
			public long getInt(ArgScriptLexer lexer) throws DocumentException {
				return getBoolean(lexer) ? 1 : 0;
			}

			@Override
			public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
				lexer.expect('(');
				
				WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
				ws.skipWhitespaces();
				String word1 = ws.nextParameter();
				
				lexer.setIndex(ws.index);
				lexer.skipWhitespaces();
				lexer.expect(',', "The function 'eq' requires two parameters.");
				
				ws.index = lexer.getIndex();
				ws.skipWhitespaces();
				String word2 = ws.nextParameter();
				
				lexer.expect(')');
				
				return word1.equals(word2);
			}
		});
		
		this.addFunction("match", new ArgScriptFunction() {
			@Override
			public double getFloat(ArgScriptLexer lexer) throws DocumentException {
				throw new DocumentException(new DocumentError("'match' is a boolean function", lexer.getKeywordStartIndex(), lexer.getIndex()));
			}

			@Override
			public long getInt(ArgScriptLexer lexer) throws DocumentException {
				return getBoolean(lexer) ? 1 : 0;
			}

			@Override
			public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
				lexer.expect('(');
				
				WordSplitLexer ws = new WordSplitLexer(lexer.getChars(), lexer.getIndex());
				ws.skipWhitespaces();
				String string = ws.nextParameter();
				
				lexer.setIndex(ws.index);
				lexer.skipWhitespaces();
				lexer.expect(',', "The function 'match' requires two parameters.");
				
				ws.index = lexer.getIndex();
				ws.skipWhitespaces();
				String pattern = ws.nextParameter();
				
				lexer.expect(')');
				
				return SimplePattern.match(string, pattern);
			}
		});
		
		if (stream != null) {
			this.addFunction("minVersion", new ArgScriptFunction() {
				@Override
				public double getFloat(ArgScriptLexer lexer) throws DocumentException {
					return getInt(lexer);
				}
	
				@Override
				public long getInt(ArgScriptLexer lexer) throws DocumentException {
					lexer.expect('(');
					lexer.skipWhitespaces();
					lexer.expect(')');
					
					return stream.getMinVersion();
				}
	
				@Override
				public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
					throw new DocumentException(new DocumentError("'minVersion' is an integer/float function", lexer.getKeywordStartIndex(), lexer.getIndex()));
				}
			});
			
			this.addFunction("maxVersion", new ArgScriptFunction() {
				@Override
				public double getFloat(ArgScriptLexer lexer) throws DocumentException {
					return getInt(lexer);
				}
	
				@Override
				public long getInt(ArgScriptLexer lexer) throws DocumentException {
					lexer.expect('(');
					lexer.skipWhitespaces();
					lexer.expect(')');
					
					return stream.getMaxVersion();
				}
	
				@Override
				public boolean getBoolean(ArgScriptLexer lexer) throws DocumentException {
					throw new DocumentException(new DocumentError("'maxVersion' is an integer/float function", lexer.getKeywordStartIndex(), lexer.getIndex()));
				}
			});
		}
		
		this.addFunction("hash", FUNCTION_HASH);
	}
	
	public void addFunction(String keyword, ArgScriptFunction function) {
		functions.put(keyword, function);
	}
	
	/**
	 * Returns the characters this lexer is analyzing.
	 */
	public char[] getChars() {
		return array;
	}
	
	/**
	 * Returns the current index of the lexer.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Sets the current index of the lexer, from which the next character will be taken.
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * Returns the last starting index of a keyword parsed by the lexer.
	 */
	public int getKeywordStartIndex() {
		return startIndex;
	}
	
	public void setText(String text) {
		array = text.toCharArray();
		index = 0;
	}
	

	/**
	 * Moves the index to skip all characters that are whitespaces.
	 */
	public void skipWhitespaces() {
		while (index < array.length && Character.isWhitespace(array[index])) {
			index++;
		}
	}
	
	/**
	 * Whether there are still more characters to read.
	 */
	public boolean available() {
		return index < array.length;
	}
	
	/**
	 * Returns the next character and increases the index in one unit.
	 */
	public char nextCharater() {
		return array[index++];
	}
	
	/**
	 * Returns the next character without changing the index;
	 */
	public char peekNextCharater() {
		return array[index];
	}
	
	/**
	 * Checks if the next non-whitespace character is the one required, and raises an error if it isn't.
	 * @param c The character that is required.
	 * @throws DocumentException If the end of the stream is reached or the next non-whitespace character is not the one required.
	 */
	public void expect(char c) throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length || array[index] != c) {
			throw new DocumentException(new DocumentError(String.format("Expected '%c'.", c), index, index+1));
		}
		index++;
	}
	
	/**
	 * Checks if the next non-whitespace character is the one required, and raises an error if it isn't.
	 * The error will use the specified message.
	 * @param c The character that is required.
	 * @param errorMessage The message that is shown in the error if the character is not present.
	 * @throws DocumentException If the end of the stream is reached or the next non-whitespace character is not the one required.
	 */
	public void expect(char c, String errorMessage) throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length || array[index] != c) {
			throw new DocumentException(new DocumentError(errorMessage, index, index+1));
		}
		index++;
	}
	
	/**
	 * Checks if there are more characters to read, and if so, whether the next non-whitespace character is the one required, raising an error if it isn't.
	 * @param c The character that is optionally required.
	 * @throws DocumentException If the next non-whitespace character is not the one required.
	 * @return True if the character is there, false if the end of the stream has been reached.
	 */
	public boolean optionalExpect(char c) throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length) {
			return false;
		}
		if (array[index] != c) {
			throw new DocumentException(new DocumentError(String.format("Expected '%c'.", c), index, index+1));
		}
		index++;
		
		return true;
	}
	
	/**
	 * Checks if there are more characters to read, and if so, whether the next non-whitespace character is the one required, raising an error if it isn't.
	 * The error will use the specified message.
	 * @param c The character that is optionally required.
	 * @param errorMessage The message that is shown in the error if the character is not present.
	 * @throws DocumentException If the next non-whitespace character is not the one required.
	 * @return True if the character is there, false if the end of the stream has been reached.
	 */
	public boolean optionalExpect(char c, String errorMessage) throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length) {
			return false;
		}
		if (array[index] != c) {
			throw new DocumentException(new DocumentError(errorMessage, index, index+1));
		}
		index++;
		
		return true;
	}
	
	/**
	 * Parses a real number expression and returns its value. The expression can use:
	 * <li>The float operators <code>+ - * / % ^</code>.
	 * <li>Any real number: '98.4', '1.34e-9'.
	 * <li>The numbers <code>e, pi</code>.
	 * <li>A real function: <code>sqrt, exp, log, abs, floor, ceil, sqr, pow</code> or any custom function added in the lexer.
	 * <li>A trigonometric function, in radians: <code>sin, cos, tan, asin, acos, atan, atan2</code>.
	 * <li>A trigonometric function, in degrees: <code>sind, cosd, tand, dasin, dacos, datan, datan2</code>.
	 * @return The result of evaluating the real expression
	 * @throws DocumentException If there is an error while parsing the expression.
	 */
	public double parseFloat() throws DocumentException {
		isHexadecimal = false;
		
		skipWhitespaces();
		
		double number = parseFloatMultiplication();
		
		while (true) {
			
			skipWhitespaces();
			
			if (index == array.length) {
				break;
			}
			
			if (array[index] == '+') {
				index++;
				number = number + parseFloatMultiplication();
			}
			else if (array[index] == '-') {
				index++;
				number = number - parseFloatMultiplication();
			}
			else {
				break;
			}
		}
		
		return number;
	}
	
	/**
	 * Parses a float expression inside a parenthesis, and returns the result of evaluating that expression.
	 * @throws DocumentException If any of the parenthesis are missing, or there is an error while parsing the expression.
	 */
	private double parseFloatParenthesis() throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length || array[index] != '(') {
			throw new DocumentException(new DocumentError("Expected '(' in real expression.", index, index+1));
		}
		index++;
		
		double result = parseFloat();
		skipWhitespaces();
		
		if (index >= array.length || array[index] != ')') {
			throw new DocumentException(new DocumentError("Expected ')' in real expression.", index, index+1));
		}
		index++;
		
		return result;
	}
	
	/**
	 * Parses an integer expression and returns its value. The expression can use:
	 * <li>Decimal or hexadecimal numbers, such as <code>8000, 0x6A</code>.
	 * <li>The integer operators <code>+ - * / % ^</code>.
	 * <li>Integer functions: <code>abs, floor, ceil, round, sqr</code> or any custom integer function.
	 * <li>The boolean keywords <code>true, on</code> (1), <code>false, off</code> (0).
	 * @return The result of evaluating the integer expression
	 * @throws DocumentException If there is an error while parsing the expression.
	 */
	public long parseInteger() throws DocumentException {
		isHexadecimal = false;
		
		skipWhitespaces();
		
		long number = parseIntMultiplication();
		
		while (true) {
			
			skipWhitespaces();
			
			if (index == array.length) {
				break;
			}
			
			if (array[index] == '+') {
				index++;
				number = number + parseIntMultiplication();
			}
			else if (array[index] == '-') {
				index++;
				number = number - parseIntMultiplication();
			}
			else {
				break;
			}
		}
		
		return number;
	}
	
	/**
	 * Parses an integer expression inside a parenthesis, and returns the result of evaluating that expression.
	 * @throws DocumentException If any of the parenthesis are missing, or there is an error while parsing the expression.
	 */
	private long parseIntParenthesis() throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length || array[index] != '(') {
			throw new DocumentException(new DocumentError("Expected '(' in integer expression.", index, index+1));
		}
		index++;
		
		long result = parseInteger();
		skipWhitespaces();
		
		if (index >= array.length || array[index] != ')') {
			throw new DocumentException(new DocumentError("Expected ')' in integer expression.", index, index+1));
		}
		index++;
		
		return result;
	}
	
	/**
	 * Parses a boolean expression and returns its value. The expression can use:
	 * <li>The keywords <code>true, on, false, off</code> to express the value.
	 * <li>The logical operators <code>or, and, not</code>
	 * <li>The comparison operations <code>==, !=</code>. 
	 * <li>Integer expressions where 0 is false and 1 is true.
	 * @return The result of evaluating the boolean expression
	 * @throws DocumentException If there is an error while parsing the expression.
	 */
	public boolean parseBoolean() throws DocumentException {
		return parseBooleanInternal() != 0;
	}

	private long parseBooleanInternal() throws DocumentException {
		isHexadecimal = false;

		skipWhitespaces();

		long left = parseBoolAnd();

		while (true) {
			skipWhitespaces();

			startIndex = index;

			StringBuilder sb = new StringBuilder();
			while (index < array.length && Character.isAlphabetic(array[index])) {
				sb.append(array[index]);
				index++;
			}
			String keyword = sb.toString();

			switch(keyword) {
				case "or":
					// We must do the function first, because otherwise it won't run if left is true
					left = parseBoolAnd() != 0 || left != 0 ? 1 : 0;
					break;
				default:
					// The keyword does not belong to this method, restore the original position
					index = startIndex;
					return left;
			}
		}
	}
	
	/**
	 * Parses a boolean expression inside a parenthesis, and returns the result of evaluating that expression.
	 * @throws DocumentException If any of the parenthesis are missing, or there is an error while parsing the expression.
	 */
	private long parseBoolParenthesis() throws DocumentException {
		skipWhitespaces();
		
		if (index >= array.length || array[index] != '(') {
			throw new DocumentException(new DocumentError("Expected '(' in boolean expression.", index, index+1));
		}
		index++;

		long result = parseBooleanInternal();
		skipWhitespaces();
		
		if (index >= array.length || array[index] != ')') {
			throw new DocumentException(new DocumentError("Expected ')' in boolean expression.", index, index+1));
		}
		index++;
		
		return result;
	}
	
	/**
	 * Parses a boolean keyword and returns the result of evaluating it.
	 * It supports: <code>true, false, on, off</code>, boolean expressions inside parenthesis, and integer expressions.
	 * @throws DocumentException If there is an error while parsing the expression.
	 */
	private long parseBoolKeyword() throws DocumentException {
		
		skipWhitespaces();

		if (array[index] == '(') {
			return parseBoolParenthesis();
		}
		
		if (!Character.isAlphabetic(array[index])) {
			return parseInteger();
		}
		
		startIndex = index;
		
		StringBuilder sb = new StringBuilder();
		while (index < array.length && (Character.isAlphabetic(array[index]) || Character.isDigit(array[index]) || array[index] == '_')) {
			sb.append(array[index]);
			index++;
		}
		String keyword = sb.toString();
		
		switch (keyword) {
		case "true":
		case "on":
			return 1;
		case "false":
		case "off":
			return 0;
		default:
			ArgScriptFunction function = functions.getOrDefault(keyword, null);
			if (function != null) {
				return function.getInt(this);
			} else {
				// The keyword does not belong to this method, restore the original position
				index = startIndex;
				return parseInteger();
			}
		}
	}
	
	/**
	 * Parses a boolean comparison expression (true != false, things like that) and returns the result of evaluating it. There might be no
	 * comparison at all.
	 * @throws DocumentException If there is an error while parsing the expression.
	 */
	private long parseBoolComparison() throws DocumentException {
		
		long left = parseBoolKeyword();

		while (true) {
			skipWhitespaces();
			
			if (index >= array.length) {
				return left;
			}

			switch (array[index]) {
			case '>':
				index++;
				return left > parseBoolComparison() ? 1 : 0;
			case '<':
				index++;
				return left < parseBoolComparison() ? 1 : 0;
			case '=':
				index++;
				if (array[index] == '=') {
					index++;
				}
				left = left == parseBoolComparison() ? 1 : 0;
				break;
				
			case '!':
				index++;
				if (array[index] != '=') {
					throw new DocumentException(new DocumentError("Invalid operator !" + array[index] + "'. Did you mean != or 'not'?", index-1, index+1));
				}
				index++;
				left = left != parseBoolComparison() ? 1 : 0;
				break;
				
			default:
				return left;
			}
		}
	}
	
	/**
	 * Same as {@link #parseBoolComparison()}, but this one supports the keyword 'not'.
	 * @throws DocumentException 
	 */
	private long parseBoolExtendedComparison() throws DocumentException {
		
		skipWhitespaces();
		
		startIndex = index;
		
		StringBuilder sb = new StringBuilder();
		while (index < array.length && Character.isAlphabetic(array[index])) {
			sb.append(array[index]);
			index++;
		}
		String keyword = sb.toString();

        if (keyword.equals("not")) {
            return parseBoolExtendedComparison() == 0 ? 1 : 0;
        } else {
			// The keyword does not belong to this method, restore the original position
			index = startIndex;
			return parseBoolComparison();
		}
    }
	
	/**
	 * Parses a boolean expression and checks if it is followed by 'and' and another expression.
	 * @throws DocumentException 
	 */
	private long parseBoolAnd() throws DocumentException {
		
		long left = parseBoolExtendedComparison();
		
		while (true) {
			skipWhitespaces();
			
			startIndex = index;
			
			StringBuilder sb = new StringBuilder();
			while (index < array.length && Character.isAlphabetic(array[index])) {
				sb.append(array[index]);
				index++;
			}
			String keyword = sb.toString();

            if (keyword.equals("and")) {
				// Order is important, if we do it the other way it won't finish parsing
                left = parseBoolExtendedComparison() != 0 && left != 0 ? 1 : 0;
            } else {
				// The keyword does not belong to this method, restore the original position
                index = startIndex;
                return left;
            }
		}
	}
	
	
	/* -- INTEGERS -- */
	
	/**
	 * Parses an integer number, which can be:
	 * <li>An hexadecimal number prefixed with '0x': '0x4a8'
	 * <li>A positive integer, such as 503.
	 * <li>Boolean keywords: <code>true, on</code> (1), <code>false, off</code> (0).
	 * <li>An integer function: <code>abs, floor, ceil, round, sqr</code> or any custom function added in the lexer.
	 * @return
	 * @throws DocumentException
	 */
	private long parseIntNumber() throws DocumentException {
		
		skipWhitespaces();
		
		if (index == array.length) {
			throw new DocumentException(new DocumentError("Missing number after operation.", index-1, index));
		}
		
		if (Character.isDigit(array[index])) {
			
			startIndex = index;
			
			try {
				// Check if it's a hexadecimal number
				if (array[index] == '0') {
					if (index+1 < array.length && array[index+1] == 'x') {
						
						// If there is '0x' but no number following it, throw an error
						if (index+2 == array.length) {
							throw new DocumentException(new DocumentError("Bad number format: expecting a hexadecimal number after '0x'.", index, index + 2));
						}
						
						isHexadecimal = true;
						
						index += 2;
						
						StringBuilder sb = new StringBuilder();
						while (index < array.length && (Character.isDigit(array[index]) || 
								array[index] == 'A' || array[index] == 'a' || array[index] == 'B' || array[index] == 'b' || array[index] == 'C' ||
								array[index] == 'c' || array[index] == 'D' || array[index] == 'd' || array[index] == 'E' || array[index] == 'e' ||
								array[index] == 'F' || array[index] == 'f')) {
							sb.append(array[index++]);
						}
						
						return Long.parseUnsignedLong(sb.toString(), 16);
					}
				}
				
				StringBuilder sb = new StringBuilder();
				while (index < array.length && Character.isDigit(array[index])) {
					sb.append(array[index++]);
				}
				
				return Integer.parseUnsignedInt(sb.toString());
			} 
			catch (NumberFormatException e) {
				throw new DocumentException(new DocumentError("Invalid number format.", startIndex, index));
			}
		}
		else {
			if (array[index] == '(') {
				return parseIntParenthesis();
			}

			if (!Character.isAlphabetic(array[index])) {
				throw new DocumentException(new DocumentError("Bad integer number: expecting an integer number, an expression in parenthesis, or a function.", index, index+1));
			}
			
			startIndex = index;
			
			StringBuilder sb = new StringBuilder();
			while (index < array.length && (Character.isAlphabetic(array[index]) || Character.isDigit(array[index]) 
					|| array[index] == '_')) {
				sb.append(array[index++]);
			}
			
			switch (sb.toString()) {
			case "abs":
				return Math.abs(parseIntParenthesis());
			case "floor":
				return (long) Math.floor(parseFloatParenthesis());
			case "ceil":
				return (long) Math.ceil(parseFloatParenthesis());
			case "round":
				return Math.round(parseFloatParenthesis());
			case "sqr":
				long number = parseIntParenthesis();
				return number*number;
			case "true":
			case "on":
				return 1;
			case "false":
			case "off":
				return 0;
			default:
				ArgScriptFunction function = functions.getOrDefault(sb.toString(), null);
				if (function == null) {
					throw new DocumentException(new DocumentError(String.format("Unknown integer function '%s'.", sb.toString()), startIndex, index));
				}
				
				if (sb.toString().equals("hash")) {
					isHexadecimal = true;
				}
				
				return function.getInt(this);
			}
		}
	}
	
	/**
	 * Parses an integer number but calculating its sign. There are two possibilities:
	 * <li>No sign at all, in which case the number returned by {@link #parseIntNumber()} is returned.
	 * <li>An indefinite number of '+' signs, in which case the number returned by {@link #parseIntNumber()} is returned.
	 * <li>The sign '-', in which case the opposite of the number returned by {@link #parseIntNumber()} is returned.
	 * @return
	 * @throws DocumentException
	 */
	private long parseIntSign() throws DocumentException {
		while (true) {
			skipWhitespaces();
			
			if (array[index] != '+') break;
			
			index++;
		}
		
		if (array[index] == '-') {
			index++;
			return -parseIntSign();
		}
		else {
			return parseIntNumber();
		}
	}
	
	/**
	 * Parses a power expression which uses the sign '^', or just returns a number calling {@link #parseIntSign()}
	 * if there is no power expression.
	 * @return
	 * @throws DocumentException
	 */
	private long parseIntPower() throws DocumentException {
		skipWhitespaces();
		
		long number = parseIntSign();
		
		if (index < array.length && array[index] == '^') {
			number = (long) Math.pow(number, parseIntPower());
		}
		
		return number;
	}
	
	/**
	 * Parses the following integer operations: <code>*, /, %</code>, or just returns a number parsed with {@link #parseIntPower()}.
	 * @return
	 * @throws DocumentException
	 */
	private long parseIntMultiplication() throws DocumentException {
		skipWhitespaces();
		
		long number = parseIntPower();
		
		while (true) {
			skipWhitespaces();
			
			if (index == array.length) {
				break;
			}
		
			if (array[index] == '%') {
				index++;
				number = number % parseIntPower();
			}
			else if (array[index] == '*') {
				index++;
				number = number * parseIntPower();
			}
			else if (array[index] == '/') {
				index++;
				number = number / parseIntPower();
			}
			else {
				break;
			}
		}
		
		return number;
	}
	
	
	/* -- FLOATS -- */
	
	private double[] parseFloatBinomial(String functionName) throws DocumentException {
		skipWhitespaces();
		
		double[] result = new double[2];
		
		if (index >= array.length || array[index] != '(') {
			throw new DocumentException(new DocumentError(String.format("Expected '(' in parameters of function '%s'.", functionName), index, index+1));
		}
		index++;
		
		result[0] = parseFloat();
		skipWhitespaces();
		
		if (index >= array.length || array[index] != ',') {
			throw new DocumentException(new DocumentError(String.format("Expected ',' in parameters of function '%s' (2 parameters are required).", functionName), index, index+1));
		}
		index++;
		
		result[1] = parseFloat();
		skipWhitespaces();
		
		if (index >= array.length || array[index] != ')') {
			throw new DocumentException(new DocumentError(String.format("Expected ')' in parameters of function '%s'.", functionName), index, index+1));
		}
		index++;
		
		return result;
	}
	
	/**
	 * Parses a real number, which can be:
	 * <li>Any real number: '98.4', '1.34e-9'.
	 * <li>The numbers <code>e, pi</code>.
	 * <li>A real function: <code>sqrt, exp, log, abs, floor, ceil, sqr, pow</code> or any custom function added in the lexer.
	 * <li>A trigonometric function, in radians: <code>sin, cos, tan, asin, acos, atan, atan2</code>.
	 * <li>A trigonometric function, in degrees: <code>sind, cosd, tand, dasin, dacos, datan, datan2</code>.
	 * @return
	 * @throws DocumentException
	 */
	private double parseFloatNumber() throws DocumentException {
		
		skipWhitespaces();

		if (index == array.length) {
			throw new DocumentException(new DocumentError("Missing number after operation.", index-1, index));
		}

		if (Character.isDigit(array[index]) || array[index] == '.') {
			startIndex = index;
			try {
				StringBuilder sb = new StringBuilder();
				while (index < array.length && (Character.isDigit(array[index]) || array[index] == 'e' || array[index] == 'E' ||
						array[index] == '.' || 
						// Since we don't want e-6 or E+5 to be mistaken as an operation, we check this:
						((array[index] == '-' || array[index] == '+') && index > 0 && (array[index-1] == 'e' || array[index-1] == 'E')))) {
					sb.append(array[index++]);
				}

				return Float.parseFloat(sb.toString());
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
				throw new DocumentException(new DocumentError("Invalid number format.", startIndex, index));
			}
		}
		else {
			if (array[index] == '(') {
				return parseFloatParenthesis();
			}

			if (!Character.isAlphabetic(array[index])) {
				throw new DocumentException(new DocumentError("Bad real number: expecting a real number, an expression in parenthesis, or a function.", index, index+1));
			}
			
			startIndex = index;
			
			StringBuilder sb = new StringBuilder();
			while (index < array.length && (Character.isAlphabetic(array[index]) || Character.isDigit(array[index]) 
					|| array[index] == '_')) {
				sb.append(array[index++]);
			}
			
			double[] numbers;
			
			switch (sb.toString()) {
			case "pi":
				return Math.PI;
			case "e":
				return Math.E;
			case "sqrt":
				return Math.sqrt(parseFloatParenthesis());
			case "exp":
				return Math.exp(parseFloatParenthesis());
			case "log":
				return Math.log(parseFloatParenthesis());
			case "abs":
				return Math.abs(parseFloatParenthesis());
				
			case "sin":
				return Math.sin(parseFloatParenthesis());
			case "cos":
				return Math.cos(parseFloatParenthesis());
			case "tan":
				return Math.tan(parseFloatParenthesis());
			case "asin":
				return Math.asin(parseFloatParenthesis());
			case "acos":
				return Math.acos(parseFloatParenthesis());
			case "atan":
				return Math.atan(parseFloatParenthesis());
				
			case "sind":
				return Math.sin(Math.toRadians(parseFloatParenthesis()));
			case "cosd":
				return Math.cos(Math.toRadians(parseFloatParenthesis()));
			case "tand":
				return Math.tan(Math.toRadians(parseFloatParenthesis()));
			case "dasin":
				return Math.toDegrees(Math.asin(parseFloatParenthesis()));
			case "dacos":
				return Math.toDegrees(Math.acos(parseFloatParenthesis()));
			case "datan":
				return Math.toDegrees(Math.atan(parseFloatParenthesis()));
			
			case "floor":
				return Math.floor(parseFloatParenthesis());
			case "round":
				return Math.round(parseFloatParenthesis());
			case "ceil":
				return Math.ceil(parseFloatParenthesis());
			case "sqr":
				double number = parseFloatParenthesis();
				return number*number;
			case "pow":
				numbers = parseFloatBinomial("pow");
				return Math.pow(numbers[0], numbers[1]);
			case "atan2":
				numbers = parseFloatBinomial("pow");
				return Math.atan2(numbers[0], numbers[1]);
			case "datan2":
				numbers = parseFloatBinomial("pow");
				return Math.toDegrees(Math.atan2(numbers[0], numbers[1]));
				
			case "NaN":
				return Float.NaN;
				
			default:
				ArgScriptFunction function = functions.getOrDefault(sb.toString(), null);
				if (function == null) {
					throw new DocumentException(new DocumentError(String.format("Unknown float function '%s'.", sb.toString()), startIndex, index));
				}
				
				return function.getFloat(this);
			}
		}
	}
	
	/**
	 * Parses a real number number but calculating its sign. There are two possibilities:
	 * <li>No sign at all, in which case the number returned by {@link #parseFloatNumber()} is returned.
	 * <li>An indefinite number of '+' signs, in which case the number returned by {@link #parseFloatNumber()} is returned.
	 * <li>The sign '-', in which case the opposite of the number returned by {@link #parseFloatNumber()} is returned.
	 * @return
	 * @throws DocumentException
	 */
	private double parseFloatSign() throws DocumentException {
		while (true) {
			skipWhitespaces();
			
			if (index == array.length) {
				throw new DocumentException(new DocumentError("Expected a number.", index-1, index));
			}
			
			if (array[index] != '+') break;
			
			index++;
		}
		
		if (array[index] == '-') {
			index++;
			return -parseFloatSign();
		}
		else {
			return parseFloatNumber();
		}
	}
	
	/**
	 * Parses a power expression which uses the sign '^', or just returns a number calling {@link #parseIntSign()}
	 * if there is no power expression.
	 * @return
	 * @throws DocumentException
	 */
	private double parseFloatPower() throws DocumentException {
		skipWhitespaces();
		
		double number = parseFloatSign();
		
		if (index < array.length && array[index] == '^') {
			number = Math.pow(number, parseFloatPower());
		}
		
		return number;
	}
	
	/**
	 * Parses the following float operations: <code>*, /, %</code>, or just returns a number parsed with {@link #parseFloatPower()}.
	 * @return
	 * @throws DocumentException
	 */
	private double parseFloatMultiplication() throws DocumentException {
		skipWhitespaces();
		
		double number = parseFloatPower();
		
		while (true) {
			skipWhitespaces();
			
			if (index == array.length) {
				break;
			}
		
			if (array[index] == '%') {
				index++;
				number = number % parseFloatPower();
			}
			else if (array[index] == '*') {
				index++;
				number = number * parseFloatPower();
			}
			else if (array[index] == '/') {
				index++;
				number = number / parseFloatPower();
			}
			else {
				break;
			}
		}
		
		return number;
	}

	/**
	 * Parses a simple keyword which is made of alphanumerical characters, and '_'. 
	 */
	public String parseKeyword() {
		StringBuilder sb = new StringBuilder();
		
		skipWhitespaces();
		while (index < array.length && (Character.isAlphabetic(array[index]) || Character.isDigit(array[index]) || array[index] == '_')) {
			sb.append(array[index++]);
		}
		
		return sb.toString();
	}
	
	public boolean lastNumberWasHexadecimal() {
		return isHexadecimal;
	}
}
