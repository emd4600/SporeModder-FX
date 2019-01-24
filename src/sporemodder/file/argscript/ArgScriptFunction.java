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

import sporemodder.file.DocumentException;

public abstract class ArgScriptFunction {

	/** 
	 * Calls the function with the text in the specified lexer, expecting the function
	 * to return a float value.
	 * <p>
	 * This method is responsible of parsing the parenthesis and parameters of the function.
	 * @param lexer The lexer that contains the text.
	 * @return The float value result of evaluating the function.
	 */
	public abstract double getFloat(ArgScriptLexer lexer) throws DocumentException;
	
	/** 
	 * Calls the function with the text in the specified lexer, expecting the function
	 * to return an integer value. Boolean functions should use this method too, returning 1 (true) or 0 (false).
	 * <p>
	 * This method is responsible of parsing the parenthesis and parameters of the function.
	 * @param lexer The lexer that contains the text.
	 * @return The integer value result of evaluating the function.
	 */
	public abstract long getInt(ArgScriptLexer lexer) throws DocumentException;
	
	/** 
	 * Calls the function with the text in the specified lexer, expecting the function
	 * to return a boolean value.
	 * <p>
	 * This method is responsible of parsing the parenthesis and parameters of the function.
	 * @param lexer The lexer that contains the text.
	 * @return The boolean value result of evaluating the function.
	 */
	public abstract boolean getBoolean(ArgScriptLexer lexer) throws DocumentException;
}
