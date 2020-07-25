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
package sporemodder.file;

public class TextUtils {

	/**
	 * Returns the word that contains the given character index. Words are separated by whitespaces.
	 * If the index is on a whitespace, returns null.
	 * @return
	 */
	public static String scanWord(String text, int charIndex) {
		int startIndex = scanWordStart(text, charIndex);
		int endIndex = scanWordEnd(text, charIndex);
		
		return scanWord(text, startIndex, endIndex);
	}
	
	public static String scanWord(String text, int startIndex, int endIndex) {
		if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) return null;
		
		return text.substring(startIndex, endIndex);
	}
	
	/**
	 * Finds the start index of the word that contains the given character index. Words are separated by whitespaces.
	 * The returned value is <code>startIndex</code> such as <code>startIndex <= charIndex and startIndex >= 0</code> and
	 * is the closest to <code>charIndex</code> that is preceded by a whitespace.
	 * @param text The text to analyze.
	 * @param charIndex 
	 * @return
	 */
	public static int scanWordStart(String text, int charIndex) {
		if (charIndex <= 0) return 0;
		
		int startIndex = charIndex;
		
		while (startIndex >= 0 && !Character.isWhitespace(text.charAt(startIndex)) && !isNewLine(text, charIndex)) {
			startIndex--;
		}
		
		if (startIndex < 0) {
			startIndex = 0;
		}
		
		if (Character.isWhitespace(text.charAt(startIndex)) || isNewLine(text, charIndex)) {
			startIndex++;
		}
		
		if (startIndex >= text.length()) return -1;
		return startIndex;
	}
	
	/**
	 * Finds the end index of the word that contains the given character index. Words are separated by whitespaces.
	 * The returned value is <code>endIndex</code> such as <code>endIndex >= charIndex and endIndex <= length</code> and
	 * is the closest to <code>charIndex</code> that is followed by a whitespace or end of text.
	 * @param text The text to analyze.
	 * @param charIndex 
	 * @return
	 */
	public static int scanWordEnd(String text, int charIndex) {
		int endIndex = charIndex;
		
		while (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
			endIndex++;
		}
		
		// Not necessary, the end of the word might be the end of the stream
		//if (endIndex >= text.length()) return -1;
		return endIndex;
	}
	
	public static int scanLineStart(String text, int charIndex) {
		while (charIndex >= 0 && !isNewLine(text, charIndex)) {
			charIndex--;
		}
		
		if (charIndex < 0) {
			charIndex = 0;
		}
		
		if (isNewLine(text, charIndex)) {
			charIndex++;
		}
		
		return charIndex;
	}
	
	public static boolean isNewLine(String text, int charIndex) {
		return text.charAt(charIndex) == '\n' || text.charAt(charIndex) == '\r';
	}
	
	public static int scanLineEnd(String text, int charIndex) {
		while (charIndex < text.length() && text.charAt(charIndex) != '\n' && text.charAt(charIndex) != '\r') {
			charIndex++;
		}
		
		return charIndex;
	}
	
	public static int scanPreviousWordEnd(String text, int wordStart) {
		int charIndex = wordStart - 1;
		while (charIndex >= 0 && (Character.isWhitespace(text.charAt(charIndex)) || isNewLine(text, charIndex))) {
			--charIndex;
		}
		
		return charIndex;
	}
	
	public static int scanNextWordStart(String text, int wordEnd) {
		int charIndex = wordEnd;
		while (charIndex < text.length() && (Character.isWhitespace(text.charAt(charIndex)) || isNewLine(text, charIndex))) {
			++charIndex;
		}
		
		if (charIndex == text.length()) return -1;
		else return charIndex;
	}
	
	public static int getLineNumber(String text, int position) {
		int line = 0;
		int indexOf = 0;
		while ((indexOf = text.indexOf('\n', indexOf + 1)) != -1 && indexOf < position) ++line;
		return line;
	}
}
