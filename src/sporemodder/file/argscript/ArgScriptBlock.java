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

public abstract class ArgScriptBlock<T> extends ArgScriptParser<T> {

	private final HashMap<String, ArgScriptParser<T>> parsers = new HashMap<String, ArgScriptParser<T>>();
	
//	/**
//	 * If this method returns false, commands that are not supported by this block will be processed using parsers of parent
//	 * blocks or parsers of the stream. If it returns true, any command not supported by this block will raise an error.
//	 */
//	public boolean onlySupported() {
//		return false;
//	}
	
	/**
	 * Method called when the 'end' keyword is found, and therefore the block is finished. 
	 */
	public void onBlockEnd() {
	}
	
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
		
		parser.setData(stream, data);
	}
	
	public void addParser(ArgScriptParser<T> parser, String ... keywords) {
		if (data == null) {
			throw new NullPointerException("Cannot add parsers before setting the ArgScript data.");
		}
		
		for (String keyword : keywords) parsers.put(keyword.toLowerCase(), parser);
		
		parser.setData(stream, data);
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
	
	@Override
	protected boolean isBlock() {
		return true;
	}
	
	
	@FunctionalInterface
	public static interface LineParser<T> {
		public void parse(ArgScriptBlock<T> block, ArgScriptLine line);
	}
	
	@FunctionalInterface
	public static interface OnBlockEndFunction<T> {
		public void onBlockEnd(ArgScriptBlock<T> block);
	}
	
	public static <T> ArgScriptBlock<T> create(LineParser<T> parser, OnBlockEndFunction<T> onBlockEnd) {
		return new ArgScriptBlock<T>() {
			@Override
			public void parse(ArgScriptLine line) {
				parser.parse(this, line);
			}
			
			@Override
			public void onBlockEnd() {
				onBlockEnd.onBlockEnd(this);
			}
		};
	}
	
	public static <T> ArgScriptBlock<T> create(LineParser<T> parser, SetDataFunction<T> setData, OnBlockEndFunction<T> onBlockEnd) {
		return new ArgScriptBlock<T>() {
			@Override
			public void parse(ArgScriptLine line) {
				parser.parse(this, line);
			}
			
			@Override
			public void setData(ArgScriptStream<T> stream, T data) {
				super.setData(stream, data);
				setData.setData(stream, data);
			}
			
			@Override
			public void onBlockEnd() {
				onBlockEnd.onBlockEnd(this);
			}
		};
	}
	
	public static <T> ArgScriptBlock<T> create(LineParser<T> parser, SetDataFunction<T> setData) {
		return new ArgScriptBlock<T>() {
			@Override
			public void parse(ArgScriptLine line) {
				parser.parse(this, line);
			}
			
			@Override
			public void setData(ArgScriptStream<T> stream, T data) {
				super.setData(stream, data);
				setData.setData(stream, data);
			}
		};
	}
}
