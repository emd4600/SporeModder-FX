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

import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;

public class WordSplitLexer {
	char[] chars;
	int index;
	
	public WordSplitLexer(String text) {
		chars = text.toCharArray();
		index = 0;
	}
	
	public WordSplitLexer(char[] chars, int index) {
		this.chars = chars;
		this.index = index;
	}
	
	public void skipWhitespaces() {
		while (index < chars.length && Character.isWhitespace(chars[index])) {
			index++;
		}
	}
	
	public String nextParameter() throws DocumentException {
		skipWhitespaces();
		
		if (index >= chars.length) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		// Read the keyword
		// We want to keep parenthesis in functions, which start with a letter
		boolean keepParenthesis = Character.isAlphabetic(chars[index]);
		
		while (index < chars.length && !Character.isWhitespace(chars[index])) {
			// We want to keep parenthesis in functions, which start with a letter
			if (!parseBasic(sb, keepParenthesis, 0, true)) break;
		}
		
		return sb.toString();
	}
	
	public String nextWord() throws DocumentException {
		skipWhitespaces();
		
		if (index >= chars.length) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		// Read the keyword
		// We want to keep parenthesis in functions, which start with a letter
		boolean keepParenthesis = Character.isAlphabetic(chars[index]);
		
		while (index < chars.length && !Character.isWhitespace(chars[index])) {
			// We want to keep parenthesis in functions, which start with a letter
			if (!parseBasic(sb, keepParenthesis, 0, false)) break;
		}
		
		return sb.toString();
	}
	
	public String nextReadableWord() throws DocumentException {
		skipWhitespaces();
		
		if (index >= chars.length) {
			return null;
		}
		
		
		StringBuilder sb = new StringBuilder();
		
		while (index < chars.length && !Character.isWhitespace(chars[index]) && 
				(Character.isAlphabetic(chars[index]) || Character.isDigit(chars[index]) || chars[index] == '_' || chars[index] == '-' || chars[index] == '~')) {
			sb.append(chars[index]);
			index++;
		}
		
		return sb.toString();
	}
	
	private boolean parseBasic(StringBuilder sb, boolean keepParenthesis, int parenthesisLevel, boolean isParameter) throws DocumentException {
		
		if (chars[index] == '"') {
			// Eat the opening "
			index++;
			
			parseQuotes(sb);
		}
		else if (chars[index] == '(') {
			// Eat the opening (
			index++;
			
			if (keepParenthesis) {
				sb.append('(');
			}
			
			++parenthesisLevel;
			parseParenthesis(sb, parenthesisLevel);
			--parenthesisLevel;
			
			if (keepParenthesis) {
				sb.append(')');
			}
			
			if (parenthesisLevel == 0) {
				return false;
			}
		}
		else if (isParameter && (chars[index] == ')' || chars[index] == ',')) {
			// don't eat the character
			return false;
		}
		else {
			sb.append(chars[index]);
			index++;
		}
		
		return true;
	}
	
	private void parseParenthesis(StringBuilder sb, int parenthesisLevel) throws DocumentException {
		int startIndex = index;
		boolean parenthesisClosed = false;
		
		// We have already parsed the ( in a previous method
		
		while (index < chars.length) {
			if (chars[index] == ')') {
				parenthesisClosed = true;
				index++;
				break;
			}
			
			parseBasic(sb, true, parenthesisLevel, false);
		}
		
		// If we have reached the end of the stream and no closing ) has been found
		if (!parenthesisClosed) {
			throw new DocumentException(new DocumentError("Missing end ) parenthesis.", startIndex, index));
		}
	}

	private void parseQuotes(StringBuilder sb) throws DocumentException {
		int startIndex = index;
		boolean quoteClosed = false;
		
		// We have already parsed the " in a previous method
		
		while (index < chars.length) {
			if (chars[index] == '"') {
				quoteClosed = true;
				index++;
				break;
			}
			
			sb.append(chars[index]);
			index++;
		}
		
		// If we have reached the end of the stream and no closing " has been found
		if (!quoteClosed) {
			throw new DocumentException(new DocumentError("Missing end \" quote.", startIndex, index));
		}
	}

	public int getPosition() {
		return index;
	}
}
