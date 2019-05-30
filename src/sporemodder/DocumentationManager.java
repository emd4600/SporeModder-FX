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
package sporemodder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class DocumentationManager extends AbstractManager {
	
	private final HashMap<String, Properties> loadedFiles = new HashMap<String, Properties>();

	public static DocumentationManager get() {
		return MainApp.get().getDocumentationManager();
	}
	
	/**
	 * Returns the documentation string associated with the given code. If the code is, for example, "particles.alpha.vary", that same code will be searched
	 * in the "particles" file. 
	 * If nothing was assigned to the given documentation code, or the text assigned was blank, the documentation code itself is returned.
	 * @param fileName
	 * @param code
	 * @return
	 */
	public String getDocumentation(String code) {
		return getDocumentation(code.split("\\.")[0], code);
	}
	
	/**
	 * Returns the documentation string found in the given file, which is given without extension (for example, just "properties"). 
	 * If nothing was assigned to the given documentation code, or the text assigned was blank, the documentation code itself is returned.
	 * @param fileName
	 * @param code
	 * @return
	 */
	public String getDocumentation(String fileName, String code) {
		Properties properties = loadFile(fileName);
		
		if (properties == null) {
			return code;
		} else {
			
			String result = properties.getProperty(code);
			
			if (result == null && code.endsWith(".")) {
				// Special case: particles.rate and particles.rate. might be the same
				result = properties.getProperty(code.substring(0, code.length() - 1));
			}
			
			if (result == null && !code.endsWith(".")) {
				// Special case: particles.rate and particles.rate. might be the same
				result = properties.getProperty(code + ".");
			}
			
			// Return the result or the code
			if (result == null || result.trim().isEmpty()) {
				return code;
			} else {
				return result;
			}
		}
	}
	
	private Properties loadFile(String fileName) {
		Properties result = loadedFiles.get(fileName);
		
		if (result == null) {
			File file = new File(PathManager.get().getProgramFile("Documentation"), fileName + ".txt");
			
			result = new Properties();
			
			try (FileReader reader = new FileReader(file)) {
				result.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
				
				return null;
			}
			
			loadedFiles.put(fileName, result);
		}
		
		return result;
	}
}
