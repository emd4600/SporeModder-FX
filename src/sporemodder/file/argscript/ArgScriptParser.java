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

/**
 * This class represents a command or block that can be processed in an ArgScript stream.
 * It takes a generic parameter which defines the type of data that is processed in the parser.
 */
public abstract class ArgScriptParser<T> {
	
	/** The object that is currently being processed. */
	protected T data;
	/** The ArgScript stream that is using this parser. */
	protected ArgScriptStream<T> stream;

	/** 
	 * Called when a line accepted by this parser is found, this must process it.
	 * ArgScript blocks should call {@link ArgScriptStream.startBlock} on the stream to
	 * be notified when the block ends.
	 * @param line The line of ArgScript data to be processed.
	 */
	public abstract void parse(ArgScriptLine line);
	
	public void setData(ArgScriptStream<T> stream, T data) {
		this.data = data;
		this.stream = stream;
	}
	
	/** Returns the object that is currently being processed. */
	public final T getData() {
		return data;
	}
	
	/** Returns the ArgScript stream that is using this parser. */
	public final ArgScriptStream<T> getStream() {
		return stream;
	}
	
	/** For syntax highlighting (the parser will work regardless of this method): whether
	 * this parser should be considered a block. */
	protected boolean isBlock() {
		return false;
	}
	
	@FunctionalInterface
	public static interface LineParser<T> {
		public void parse(ArgScriptParser<T> parser, ArgScriptLine line);
	}
	
	@FunctionalInterface
	public static interface SetDataFunction<T> {
		public void setData(ArgScriptStream<T> stream, T data);
	}
	
	public static <T> ArgScriptParser<T> create(LineParser<T> parser) {
		return new ArgScriptParser<T>() {
			@Override
			public void parse(ArgScriptLine line) {
				parser.parse(this, line);
			}
		};
	}
	
	
}
