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

package sporemodder.file;

/**
 * An expression exception that wraps a document error. We can use exceptions here because there is no
 * reason to continue parsing an expression if there has been an error.
 */
public class DocumentException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2253171588526293460L;
	
	/** The error that caused the exception. */
	private DocumentError error;
	
	public DocumentException(DocumentError error) {
		this.error = error;
	}
	
	/** Returns the error that caused the exception. */
	public DocumentError getError() {
		return error;
	}
	
	@Override public String getMessage() {
		return "Line " + error.getLine() + ": " + error.getMessage();
	}
}
