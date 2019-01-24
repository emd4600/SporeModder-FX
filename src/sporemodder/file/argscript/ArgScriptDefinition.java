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

import java.util.ArrayList;
import java.util.List;

import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;

public class ArgScriptDefinition {

	/** The position of the first line within the whole stream. */
	private int startingLine;
	private String name;
	private final List<String> parameters = new ArrayList<String>();
	private final List<String> lines = new ArrayList<String>();
	
	public ArgScriptDefinition(String name, int startingLine) {
		this.name = name;
		this.startingLine = startingLine;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public void addParameter(String parameter) {
		parameters.add(parameter);
	}
	
	public void addLine(String line) {
		lines.add(line);
	}
	
	private void replaceParameters(List<String> arguments, char[] text, StringBuilder dst) throws DocumentException {
		int startIndex = 0;
		
		// Are we inside a brace?
		boolean insideBraces = false;
		
		// Keep reading until we find a $, then read the variable name and replace it with the value
		for (int i = 0; i < text.length; i++) {
			
			if (text[i] == '&') {
				// Copy the text as it is
				dst.append(text, startIndex, i - startIndex);
				// Eat the character
				i++;
				
				// Check to avoid throwing out of bounds exceptions
				if (i == text.length) {
					throw new DocumentException(new DocumentError("Missing parameter name after '$'.", i-1, i));
				}
				
				// Is the variable name inside braces?
				if (text[i] == '{') {
					insideBraces = true;
					// Eat the character
					i++;
				}
				
				// Check to avoid throwing out of bounds exceptions
				if (i == text.length) {
					throw new DocumentException(new DocumentError("Missing parameter name after '{'; the format should be '${parameterName}'.", i-2, i));
				}
				
				// Get the variable name, accumulating all the accepted characters for a name
				StringBuilder varName = new StringBuilder();
				// The position at which the variable starts, this is for errors
				int varStart = i;
				
				// Keep using the same 'i' variable
				for (; i < text.length; i++) {
					if (Character.isAlphabetic(text[i]) || Character.isDigit(text[i]) || text[i] == '_') {
						varName.append(text[i]);
					}
					else {
						break;
					}
				}
				
				String variableName = varName.toString();
				
				// After getting the variable name, do some checks to ensure it is valid
				if (Character.isDigit(variableName.charAt(0))) {
					throw new DocumentException(new DocumentError(
							String.format("Invalid variable name '%s': parameter names cannot start with a numeric digit.", variableName), varStart, i));
				}
				
				if (insideBraces) {
					// Ensure the brace is closed now
					if (i == text.length || text[i] != '}') {
						throw new DocumentException(new DocumentError(String.format("Missing closing '}' after parameter '%s'.", variableName), varStart, i));
					}
					// Eat the character
					i++;
					
					insideBraces = false;
				}
				
				// Replace the variable with its value
				int parameterIndex = parameters.indexOf(variableName);
				if (parameterIndex == -1) {
					throw new DocumentException(new DocumentError(String.format("Unknown parameter '%s'.", variableName), varStart, i));
				}
				
				dst.append(arguments.get(parameterIndex));
				
				// Prepare for reading plain text again
				startIndex = i;
			}
		}
		
		// Write the remaining text
		if (startIndex < text.length) {
			dst.append(text, startIndex, text.length - startIndex);
		}
	}
	
	public <T> boolean creteInstance(ArgScriptStream<T> stream, List<String> arguments) throws DocumentException {
		if (parameters.size() != arguments.size()) {
			throw new DocumentException(new DocumentError(String.format("Definition '%s' requires %d arguments, %d have been given.", name, parameters.size(), arguments.size()), 0, 0));
		}
		
		List<DocumentError> errors;
		try {
			errors = stream.protectedParsing(() -> {
				for (int i = 0; i < lines.size(); i++) {
					
					String lineWithReplacedParams = null;
					
					try {
						StringBuilder sb = new StringBuilder();
						replaceParameters(arguments, lines.get(i).toCharArray(), sb);
						lineWithReplacedParams = sb.toString();
						
						stream.processLine(lineWithReplacedParams);
					}
					catch (DocumentException e) {
						// The error has happened while parsing the definition parameter, so we 
						// can move it to the definition itself
						DocumentError error = e.getError();
						error.setLine(startingLine + i);
						stream.addError(error);
						
						// We won't throw an exception because that would add it to the command that is creating the definition,
						// when the problem is on the definition itself.
					}
				}
			});
			if (!errors.isEmpty()) {
				// The position does not matter, the command that is calling this method should fix it
				//throw new DocumentException(new DocumentError(String.format("Error on line %d: %s", startingLine + i, errors.get(0).getMessage()), 0, 0));
				throw new DocumentException(new DocumentError("Error on definition: " + errors.get(0).getMessage(), 0, 0));
			}
		} catch (DocumentException e) {
			throw e;
		} catch (Exception e) {
			throw new DocumentException(new DocumentError("Error on definition: " + e.getLocalizedMessage(), 0, 0));
		}
		
		return true;
	}
}
