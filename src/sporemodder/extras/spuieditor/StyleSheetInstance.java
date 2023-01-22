package sporemodder.extras.spuieditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import sporemodder.extras.spuieditor.components.SPUIDefaultComponent;
import sporemodder.utilities.Hasher;

public class StyleSheetInstance {
	
	public static final float FONT_SIZE_SCALE = 1.0f;

	private final HashMap<String, String> objects = new HashMap<String, String>();
	private String name;
	private int styleID;
	
	private StyleSheetInstance parent;
	
	public StyleSheetInstance(String name, StyleSheetInstance parent) {
		this.name = name;
		this.styleID = Hasher.stringToFNVHash(name);
		this.parent = parent;
	}
	
	public StyleSheetInstance(String name, int styleID, StyleSheetInstance parent) {
		this.name = name;
		this.styleID = styleID;
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return name + " (0x" + Hasher.fillZeroInHexString(styleID) + ")";
	}
	
	public void read(BufferedReader reader) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null) {
			
			if (line.isEmpty() || line.startsWith("//")) {
				continue;
			}
			
			if (line.trim().startsWith("}")) {
				break;
			}
			
			// some lines don't use this
			int indexOf = line.indexOf(";");
			String validLine = indexOf != -1 ? line.substring(0, line.indexOf(";")).trim() : line.trim();
			String[] splits = validLine.split(":", 2);
			
			objects.put(splits[0], splits[1]);
		}
	}
	
	public String getValue(String key, String defaultValue) {
		String value = objects.get(key);
		if (value == null && parent != null) {
			return parent.getValue(key, defaultValue);
		}
		return value == null ? defaultValue : value;
	}
	
	public String getValue(String key) {
		return getValue(key, null);
	}
	
	public String getStringValue(String key) {
		return getStringValue(key, null);
	}

	public String getStringValue(String key, String defaultValue) {
		String str = getValue(key);
		if (str != null) {
			// strings have ""
			return str.substring(1, str.length() - 1);
		}
		else {
			return defaultValue;
		}
	}
	
	public float getFloatValue(String key) {
		return getFloatValue(key, 0);
	}
	
	public float getFloatValue(String key, float defaultValue) {
		String str = getValue(key);
		if (str != null) {
			if (str.endsWith("px") || str.endsWith("pt")) {
				return Float.parseFloat(str.substring(0, str.length() - 2));
			}
			else {
				return Float.parseFloat(str);
			}
		}
		else {
			return defaultValue;
		}
	}
	
	public Color getColorValue(String key) {
		return getColorValue(key, null);
	}
	
	public Color getColorValue(String key, Color defaultValue) {
		String str = getValue(key);
		if (str != null) {
			// colors start with #
			int value = Integer.parseUnsignedInt(str.substring(1), 16);
			
			if (str.length() == 9) {
				return new Color(
						(value & 0xFF000000) >> 24, 
						(value & 0x00FF0000) >> 8, 
						(value & 0x0000FF00) >> 8, 
						value & 0x000000FF);
			}
			else {
				return new Color(
						(value & 0x00FF0000) >> 16, 
						(value & 0x0000FF00) >> 8, 
						(value & 0x000000FF) >> 0, 
						0xFF);
			}
		}
		else {
			return defaultValue;
		}
	}

	public String getName() {
		return name;
	}

	public int getStyleID() {
		return styleID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStyleID(int styleID) {
		this.styleID = styleID;
	}
	
	public Font getFont() {
		String weight = getValue(StyleSheet.FONT_WEIGHT, "normal").toLowerCase();
		String style = getValue(StyleSheet.FONT_STYLE, "normal");
		float fontSize = getFloatValue(StyleSheet.FONT_SIZE, 10) * FONT_SIZE_SCALE;
		
		int styleFlags = Font.PLAIN;
		if (weight.equals("bold") || weight.equals("bolder")) {
			styleFlags |= Font.BOLD;
		}
		if (style.equals("italic") || style.equals("oblique")) {
			styleFlags |= Font.ITALIC;
		}
		
		return StyleSheet.getFont(getStringValue(StyleSheet.FONT_FAMILY, "Arial"), styleFlags, (int) Math.round(pointToPixel(fontSize)));
	}
	
	public void paintText(Graphics g, String text, Color fontColor, Rectangle bounds, float[] margins) {
		if (text == null) {
			return;
		}
		
		if (margins != null) {
			Rectangle marginBounds = new Rectangle();
			marginBounds.x = (int) Math.round(bounds.x + margins[SPUIDefaultComponent.LEFT]);
			marginBounds.y = (int) Math.round(bounds.y + margins[SPUIDefaultComponent.TOP]);
			marginBounds.width = (int) Math.round(bounds.x + bounds.width - margins[SPUIDefaultComponent.RIGHT] - margins[SPUIDefaultComponent.LEFT]) - bounds.x;
			marginBounds.height = (int) Math.round(bounds.y + bounds.height - margins[SPUIDefaultComponent.BOTTOM] - margins[SPUIDefaultComponent.TOP]) - bounds.y;
			
			bounds = marginBounds;
		}
		
		String align = getValue(StyleSheet.TEXT_ALIGN, "left");
		String valign = getValue(StyleSheet.TEXT_VALIGN, "middle").toLowerCase();
		
		String fontSmooth = getValue(StyleSheet.FONT_SMOOTH, "always");
		if (fontSmooth.equals("always") || fontSmooth.equals("auto")) {
			((Graphics2D) g).setRenderingHint(
			        RenderingHints.KEY_TEXT_ANTIALIASING,
			        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		else if (fontSmooth.equals("never")) {
			((Graphics2D) g).setRenderingHint(
			        RenderingHints.KEY_TEXT_ANTIALIASING,
			        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		
		g.setFont(getFont());
		
		String[] lines = text.split("\n");
		
		FontMetrics metrics = g.getFontMetrics();
		
		Rectangle2D.Double stringBounds = new Rectangle2D.Double();
		
		for (String line : lines) {
			Rectangle2D strRect = metrics.getStringBounds(line, g);
			stringBounds.setRect(0, 0, Math.max(strRect.getWidth(), stringBounds.getWidth()), strRect.getHeight() + stringBounds.getHeight());
		}
		
		Rectangle textBounds = new Rectangle();
		
		textBounds.width = (int) Math.round(stringBounds.getWidth());
		textBounds.height = (int) Math.round(stringBounds.getHeight());
		
		if (align.equals("left") || align.equals("justify")) {
			textBounds.x = bounds.x;
		}
		else if (align.equals("center")) {
			int hSpace = (int) Math.round((bounds.width - stringBounds.getWidth()) / 2);
			textBounds.x = bounds.x + hSpace;
		}
		else if (align.equals("right")) {
			textBounds.x = (int) Math.round(bounds.x + bounds.width - stringBounds.getWidth());
		}
		else {
			textBounds.x = bounds.x;
		}

		if (valign.equals("top")) {
			textBounds.y = (int) Math.round(bounds.y + stringBounds.getHeight());
		}
		else if (valign.equals("middle")) {
			int vSpace = (int) Math.round((bounds.height - stringBounds.getHeight()) / 2);
			
			textBounds.y = (int) Math.round(bounds.y + vSpace);
			
			if (lines.length == 1) {
				// it works? wat's this
				textBounds.y += metrics.getAscent() - stringBounds.getHeight();
			}
		}
		else if (valign.equals("bottom")) {
			textBounds.y = (int) Math.round(bounds.y + bounds.height - stringBounds.getHeight());
		}
		else {
			textBounds.y = (int) Math.round(bounds.y + stringBounds.getHeight());
		}
		
		Color backgroundColor = getColorValue(StyleSheet.BACKGROUND_COLOR);
		if (backgroundColor != null) {
			g.setColor(backgroundColor);
			g.fillRect(textBounds.x, textBounds.y, textBounds.width, textBounds.height);
		}
		
		if (fontColor == null) {
			fontColor = getColorValue(StyleSheet.COLOR, Color.black);
		}
		
		g.setColor(fontColor);
		// g.drawString(text, textBounds.x, textBounds.y);
		
		for (String line : lines) {
	        g.drawString(line, textBounds.x, textBounds.y += metrics.getHeight());
		}
	}
	
	public static float pointToPixel(float pt){
	   int ppi = Toolkit.getDefaultToolkit().getScreenResolution();
	   return pt / (72.0f / ppi);
	}
	
	public static void paintText(Graphics2D graphics, StyleSheetInstance style, String text, Color fontColor, Rectangle realBounds, float[] margins) {
		if (style != null) {
			style.paintText(graphics, text, fontColor, realBounds, margins);
		}
		else {
			FontMetrics fontMetrics = graphics.getFontMetrics();
			graphics.setColor(fontColor == null ? Color.BLACK : fontColor);
			
			float x = realBounds.x + margins[SPUIDefaultComponent.LEFT];
			float y = realBounds.y + margins[SPUIDefaultComponent.TOP];
			
			for (String line : text.split("\n")) {
		        graphics.drawString(line, x, y += fontMetrics.getHeight());
			}
		}
	}
	
	public static Rectangle2D getStringBounds(Graphics2D graphics, StyleSheetInstance style, String text) {
		if (text == null) {
			return new Rectangle();
		}
		if (style != null) {
			return graphics.getFontMetrics(style.getFont()).getStringBounds(text, graphics);
		}
		else {
			return graphics.getFontMetrics().getStringBounds(text, graphics);
		}
	}
}
