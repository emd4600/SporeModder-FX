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

public class SimplePattern {
	
	private static boolean match(char[] text, char[] pattern, int textIndex, int patternIndex) {
		
		while (true) {
			if (textIndex == text.length) {
				return patternIndex == text.length;
			}
			// Since we have checked before, if this happens it means we haven't finished the text ??
			if (patternIndex == text.length) {
				return false;
			}
			
			char c = pattern[patternIndex];
			if (c == '*' && patternIndex == pattern.length) {
				return true;
			}
			
			if (c == '*') {
				if (match(text, pattern, textIndex, patternIndex + 1)) {
					return true;
				}
				else {
					textIndex++;
				}
			}
			else if (c == '?') {
				// Just ignore this character
				textIndex++;
				patternIndex++;
			}
			else {
				// A required character
				if (c != text[textIndex]) {
					return false;
				} else {
					textIndex++;
					patternIndex++;
				}
			}
		}
	}
	
	public static boolean match(String text, String pattern) {
		return match(text.toCharArray(), pattern.toCharArray(), 0, 0);
	}

	public static boolean match(String text, String pattern, boolean caseSensitive) {
		if (caseSensitive) {
			text = text.toLowerCase();
			pattern = pattern.toLowerCase();
		}
		
		return match(text, pattern);
	}
}
