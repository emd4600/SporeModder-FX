package sporemodder.extras.spuieditor;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sporemodder.utilities.Hasher;

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

	private final List<StyleSheetInstance> instances = new ArrayList<StyleSheetInstance>();
	
	private static final HashMap<String, Font> loadedFonts = new HashMap<String, Font>(); 
	
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
				
				InputStream is = ResourceLoader.getResourceInputStream(fontFileName, 0x0248E873);
				if (is != null) {
					try {
						Font font = Font.createFont(Font.TRUETYPE_FONT, is);
						GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
						
						loadedFonts.put(font.getFontName(), font);
						
					} catch (FontFormatException e) {
						e.printStackTrace();
					}
					finally {
						if (is != null) {
							is.close();
						}
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
					StyleSheetInstance ins = new StyleSheetInstance(name, Hasher.decodeInt(splits[0].substring(indexOfID + 1, splits[0].indexOf(")"))), parent);
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
	
	public static Font getFont(String name, int style, int fontSize) {
		Font font = loadedFonts.get(name);
		if (font == null) {
			return new Font(name, style, fontSize);
		}
		else {
			return font.deriveFont(style, fontSize);
		}
	}

	public List<StyleSheetInstance> getInstances() {
		return instances;
	}
}
