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
package sporemodder.file.spui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import sporemodder.HashManager;
import sporemodder.PathManager;
import sporemodder.file.spui.uidesigner.SpuiDesigner;

public class StyleSheet {
	public static final String FONT_FAMILY = "font-family";
	public static final String FONT_SIZE = "font-size";
	public static final String FONT_STYLE = "font-style";
	public static final String FONT_WEIGHT = "font-weight";
	public static final String FONT_VARIANT = "font-variant";
	public static final String FONT_PITCH = "font-pitch";
	public static final String FONT_SMOOTH = "font-smooth";
	public static final String FONT_STRETCH = "font-stretch";
	public static final String FONT_EMPHASIZE_STYLE = "font-emphasize-style";
	public static final String FONT_EMPHASIZE_POSITION = "font-emphasize-position";
	public static final String TEXT_DECORATION = "text-decoration";
	public static final String LINE_SPACING = "line-spacing";
	public static final String LETTER_SPACING = "letter-spacing";
	public static final String WORD_SPACING = "word-spacing";
	public static final String TEXT_ALIGN = "text-align";
	public static final String TEXT_VALIGN = "text-valign";
	public static final String TEXT_JUSTIFY = "text-justify";
	public static final String TEXT_OVERFLOW_MODE = "text-overflow-mode";
	public static final String COLOR = "color";
	public static final String BACKGROUND_COLOR = "background-color";
	public static final String WRAP_OPTION = "word-spacing";
	public static final String DIGIT_SUBSTITUTION = "digit-substitution";
	public static final String PASSWORD_MODE = "password-mode";
	
	private static StyleSheet activeStyleSheet; 

	private final List<StyleSheetInstance> instances = new ArrayList<>();
	
	private static final Map<String, Font> loadedFonts = new HashMap<>(); 
	
	public StyleSheetInstance getInstance(int styleID) {
		for (StyleSheetInstance ins : instances) {
			if (ins.getStyleID() == styleID) {
				return ins;
			}
		}
		return null;
	}
	
	public StyleSheetInstance getInstance(String name) {
		for (StyleSheetInstance ins : instances) {
			if (ins.getName().equals(name)) {
				return ins;
			}
		}
		return null;
	}
	
	public StyleSheetInstance getStyleInstance(String string) {
		if (string.startsWith("0x") && string.length() > 2) {
			return getInstance(Integer.parseInt(string.substring(2), 16));
		}
		else if (string.startsWith("#") && string.length() > 1) {
			return getInstance(Integer.parseInt(string.substring(1), 16));
		}
		else {
			return getInstance(string);
		}
	}
	
	public static StyleSheet readStyleSheet(BufferedReader reader) throws IOException {
		
		StyleSheet styleSheet = new StyleSheet();
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			if (line.isEmpty() || line.startsWith("//")) {
				continue;
			}
			
			if (line.startsWith("@font")) {
				String fontFileName = line.substring(7, line.indexOf(";") - 2);
				
				File file = PathManager.get().getProgramFile(SpuiDesigner.FOLDER_NAME + File.separatorChar + fontFileName);
				if (file != null && file.exists()) {
					try (InputStream is = new FileInputStream(file)) {
						Font font = Font.loadFont(is, 12);
						loadedFonts.put(font.getFamily(), font);
						
					}
				}
			}
			else {
				String validLine = line.substring(0, line.indexOf("{"));
				
				String[] splits = validLine.split("\\:", 2);
				int indexOfID = splits[0].indexOf("(");
				String name = indexOfID == -1 ? splits[0] : splits[0].substring(0, indexOfID);
				name.trim();
				
				StyleSheetInstance parent = null;
				if (splits.length > 1) {
					parent = styleSheet.getInstance(splits[1].trim());
				}
				
				if (indexOfID != -1) {
					StyleSheetInstance ins = new StyleSheetInstance(name, HashManager.get().int32(splits[0].substring(indexOfID + 1, splits[0].indexOf(")"))), parent);
					ins.read(reader);
					styleSheet.instances.add(ins);
				}
				else {
					StyleSheetInstance ins = new StyleSheetInstance(name, parent);
					ins.read(reader);
					styleSheet.instances.add(ins);
				}
			}
		}
		
		return styleSheet;
	}
	
	public static StyleSheet getActiveStyleSheet() {
		return activeStyleSheet;
	}
	
	public static void setActiveStyleSheet(StyleSheet styleSheet) {
		activeStyleSheet = styleSheet;
	}
	
	public static Font getFont(String family, FontWeight weight, FontPosture posture, double size) {
		Font font = loadedFonts.get(family);
		if (font == null) {
			return Font.font(family, weight, posture, size);
		}
		else {
			return Font.font(font.getFamily(), weight, posture, size);
		}
	}

	public List<StyleSheetInstance> getInstances() {
		return instances;
	}
}
