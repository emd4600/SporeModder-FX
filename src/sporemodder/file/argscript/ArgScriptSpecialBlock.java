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

public abstract class ArgScriptSpecialBlock<T> extends ArgScriptParser<T> {
	
	// Used by the stream to store it
	static class Pair<T> {
		Pair(ArgScriptSpecialBlock<T> key, String value) {
			this.key = key;
			this.value = value;
		}
		
		ArgScriptSpecialBlock<T> key;
		String value;
	}

	/**
	 * Method executed every time a new line is found while this block is being read.
	 * The raw line is given without comments, but with no variables replaced neither.
	 * If the method returns true, the line will be considered processed and it won't be parsed like it normally would. 
	 * @param line
	 */
	public boolean processLine(String line) {
		return false;
	}
	
	/**
	 * Method called when the end keyword is found, and therefore the block is finished. 
	 * It's recommended to call ArgScriptStream.endSpecialBlock() here.
	 */
	public abstract void onBlockEnd();
	
	@Override
	protected boolean isBlock() {
		return true;
	}
	
	/**
	 * Replaces the variables of the text and converts it into an ArgScriptLine.
	 * @param text
	 */
	public ArgScriptLine preprocess(String text) {
		StringBuilder dst = new StringBuilder();
		OriginalPositionTracker tracker = new OriginalPositionTracker();
		tracker.addAll(stream.getCommentTracker());
		
		if (!stream.replaceVariables(text.toCharArray(), dst, stream.getCommentTracker(), tracker)) {
			return null;
		}
		
		ArgScriptLine line = new ArgScriptLine(stream);
		line.fromLine(text, tracker);
		return line;
	}
}
