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

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import sporemodder.HashManager;
import sporemodder.file.spui.components.Borders;

public class StyleSheetInstance {
	
	public static final float FONT_SIZE_SCALE = 1.0f;

	private final Map<String, String> objects = new HashMap<>();
	private String name;
	private int styleID;
	
	private StyleSheetInstance parent;
	
	public StyleSheetInstance(String name, StyleSheetInstance parent) {
		this.name = name;
		this.styleID = HashManager.get().fnvHash(name);
		this.parent = parent;
	}
	
	public StyleSheetInstance(String name, int styleID, StyleSheetInstance parent) {
		this.name = name;
		this.styleID = styleID;
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return name + " (" + HashManager.get().hexToString(styleID) + ")";
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
				return Color.rgb(
						(value & 0xFF000000) >> 24, 
						(value & 0x00FF0000) >> 8, 
						(value & 0x0000FF00) >> 8, 
						(value & 0x000000FF) / 255.0f);
			}
			else {
				return Color.rgb(
						(value & 0x00FF0000) >> 16, 
						(value & 0x0000FF00) >> 8, 
						(value & 0x000000FF) >> 0);
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
		
		FontWeight fontWeight = FontWeight.NORMAL;
		if (weight.equals("bold") || weight.equals("bolder")) {
			fontWeight = FontWeight.BOLD;
		}
		FontPosture fontPosture = FontPosture.REGULAR;
		if (style.equals("italic") || style.equals("oblique")) {
			fontPosture = FontPosture.ITALIC;
		}
		
		return StyleSheet.getFont(getStringValue(StyleSheet.FONT_FAMILY, "Arial"), fontWeight, fontPosture, pointToPixel(fontSize));
	}
	
	public void paintText(GraphicsContext g, String text, Color fontColor, SPUIRectangle area, Borders margins) {
		if (text == null) {
			return;
		}
		
		// Avoid modifying original
		area = new SPUIRectangle(area);
		
		if (margins != null) {
			area.x1 += margins.left;
			area.y1 += margins.top;
			area.x2 -= margins.right;
			area.y2 -= margins.bottom;
		}
		
		String align = getValue(StyleSheet.TEXT_ALIGN, "left");
		String valign = getValue(StyleSheet.TEXT_VALIGN, "middle").toLowerCase();
		
		// Used to use fontSmooth, but we can't toggle antialiasing here - who cares anyways
		
		g.setFont(getFont());
		
		String[] lines = text.split("\n");
		Bounds[] bounds = new Bounds[lines.length];
		
		SPUIRectangle stringBounds = new SPUIRectangle();
		
		for (int i = 0; i < lines.length; ++i) {
			bounds[i] = reportSize(lines[i], g.getFont());
			stringBounds.x2 = (float) Math.max(bounds[i].getWidth(), stringBounds.x2);
			stringBounds.y2 += bounds[i].getHeight();
		}
		
		SPUIRectangle textArea = new SPUIRectangle();
		
		if (align.equals("left") || align.equals("justify")) {
			textArea.x1 = area.x1;
		}
		else if (align.equals("center")) {
			float hSpace = (area.getWidth() - stringBounds.getWidth()) / 2;
			textArea.x1 = area.x1 + hSpace;
		}
		else if (align.equals("right")) {
			textArea.x1 = area.x2 - stringBounds.getWidth();
		}
		else {
			textArea.x1 = area.x1;
		}

		if (valign.equals("top")) {
			textArea.y1 = area.y1 + stringBounds.getHeight();
		}
		else if (valign.equals("middle")) {
			float vSpace = (area.getHeight() - stringBounds.getHeight()) / 2;
			
			textArea.y1 = area.y1 + vSpace;
			// Text is painted from bottom
			textArea.y1 += stringBounds.getHeight();
			
			if (lines.length == 1) {
				textArea.y1 -= stringBounds.getHeight() / 4.0;  // ?
			}
			
//			if (lines.length == 1) {
//				// it works? wat's this
//				textArea.y1 += metrics.getAscent() - stringBounds.getHeight();
//			}
		}
		else if (valign.equals("bottom")) {
			textArea.y1 = area.y2 - stringBounds.getHeight();
		}
		else {
			textArea.y1 = area.y1 + stringBounds.getHeight();
		}
		
		textArea.setWidth(stringBounds.getWidth());
		textArea.setHeight(stringBounds.getHeight());
		
		Color backgroundColor = getColorValue(StyleSheet.BACKGROUND_COLOR);
		if (backgroundColor != null) {
			g.setFill(backgroundColor);
			g.fillRect(textArea.x1, textArea.y1, textArea.getWidth(), textArea.getHeight());
		}
		
		if (fontColor == null) {
			fontColor = getColorValue(StyleSheet.COLOR, Color.BLACK);
		}
		
		g.setFill(fontColor);
		//g.setTextBaseline(VPos.TOP);
		for (int i = 0; i < lines.length; ++i) {
	        g.fillText(lines[i], textArea.x1, textArea.y1);
	        textArea.y1 += bounds[i].getHeight();
		}
	}
	
	public static double pointToPixel(float pt){
	   int ppi = Toolkit.getDefaultToolkit().getScreenResolution();
	   return pt / (72.0f / ppi);
	}
	
	public static void paintText(SpuiViewer viewer, StyleSheetInstance style, String text, Color fontColor, SPUIRectangle area, Borders margins) {
		if (style != null) {
			style.paintText(viewer.getGraphicsContext2D(), text, fontColor, area, margins);
		}
		else {
			
			GraphicsContext graphics = viewer.getGraphicsContext2D();
			graphics.setFill(fontColor == null ? Color.BLACK : fontColor);
			
			float x = area.x1 + margins.left;
			float y = area.y1 + margins.top;
			
			for (String line : text.split("\n")) {
		        graphics.fillText(line, x, y);
		        y += reportSize(line, graphics.getFont()).getHeight();
			}
		}
	}
	
//	private static Bounds reportSize(String s, Font myFont) {
//		Text text = new Text(s);
//		text.setFont(myFont);
//		Bounds tb = text.getBoundsInLocal();
//		Rectangle stencil = new Rectangle(
//		        tb.getMinX(), tb.getMinY(), tb.getWidth(), tb.getHeight()
//		);
//		
//		Shape intersection = Shape.intersect(text, stencil);
//		
//		return intersection.getBoundsInLocal();
//	}
	
	private static Bounds reportSize(String s, Font myFont) {
		Text helper = new Text();
	    helper.setFont(myFont);
	    helper.setText(s);
		
		return helper.getLayoutBounds();
	}
}