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

/**
 * A class used to mark errors and warnings in a text document. The class keeps the line number and the start (inclusive) and end (exclusive) positions
 * of the error, as well as a description message to show.
 */
public class DocumentError {
	
	public static final String STYLE_ERROR = "text-errors";
	public static final String STYLE_WARNING = "text-warnings";
	
	private String message;
	private int line;
	private int startPosition;
	private int endPosition;
	//TODO solution suggestions?
	
	public DocumentError(String message, int startPosition, int endPosition, int line) {
		this.message = message;
		this.line = line;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public DocumentError(String message, int startPosition, int endPosition) {
		this.message = message;
		this.line = -1;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public String getMessage() {
		return message;
	}

	public int getLine() {
		return line;
	}
	
	public int getStartPosition() {
		return startPosition;
	}
	
	public int getEndPosition() {
		return endPosition;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

}
