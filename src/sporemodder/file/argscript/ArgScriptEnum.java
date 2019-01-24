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

import java.util.Collection;
import java.util.HashMap;

import sporemodder.file.DocumentError;

public final class ArgScriptEnum {

	private final HashMap<String, Integer> textToValue = new HashMap<String, Integer>();
	private final HashMap<Integer, String> valueToText = new HashMap<Integer, String>();
	
	public final void add(int value, String text) {
		valueToText.put(value, text);
		textToValue.put(text, value);
	}
	
	public Collection<Integer> getValues() {
		return textToValue.values();
	}
	
	public Collection<String> getKeys() {
		return valueToText.values();
	}
	
	public final String get(int value) {
		return valueToText.get(value);
	}
	
	public final int get(String key) {
		Integer value = textToValue.get(key);
		if (value == null) return -1;
		else return value;
	}
	
	/**
	 * Tries to get the int value that is assigned to the text in the specified argument. If the text is not present
	 * in the enum, and error is added to the ArgScript stream and the method will return -1.
	 * @param args
	 * @param index
	 * @return
	 */
	public final int get(ArgScriptArguments args, int index) {
		if (textToValue.containsKey(args.get(index))) {
			return textToValue.get(args.get(index));
		}
		else {
			args.stream.addError(new DocumentError(String.format("The keyword '%s' is not part of the enum.", args.get(index)), args.getPosition(index), args.getEndPosition(index)));
			return -1;
		}
	}
}
