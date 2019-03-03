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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.util.ColorRGB;
import sporemodder.util.ColorRGBA;
import sporemodder.util.Vector2;
import sporemodder.util.Vector3;
import sporemodder.util.Vector4;

public class ArgScriptWriter {

	private final StringBuilder sb = new StringBuilder();
	private int indentationLevel = 0;
	
	private boolean firstArgument = true;
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	public void write(File outputFile) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(outputFile)) {
		    out.println(sb.toString());
		}
	}
	
	public ArgScriptWriter startBlock() {
		indentationLevel++;
		return this;
	}
	
	public ArgScriptWriter endBlock() {
		indentationLevel--;
		return this;
	}
	
	public ArgScriptWriter blankLine() {
		sb.append('\n');
		firstArgument = true;
		return this;
	}
	
	public ArgScriptWriter tabulatedText(String text, boolean newLine) {
		String[] lines = text.split("\n");
		for (String line : lines) {
			if (sb.length() != 0 && newLine) {
				sb.append('\n');
			}
			if (newLine) for (int i = 0; i < indentationLevel; i++) {
				sb.append('\t');
			}
			newLine = true;
			sb.append(line);
		}
		
		return this;
	}
	
	public ArgScriptWriter command(String name) {
		if (sb.length() != 0) {
			sb.append('\n');
		}
		for (int i = 0; i < indentationLevel; i++) {
			sb.append('\t');
		}
		sb.append(name);
		firstArgument = false;
		return this;
	}
	
	public ArgScriptWriter commandEND() {
		return command("end");
	}
	
	public ArgScriptWriter newline() {
		sb.append('\n');
		firstArgument = true;
		return this;
	}
	
	public ArgScriptWriter indentNewline() {
		sb.append('\n');
		for (int i = 0; i < indentationLevel; i++) {
			sb.append('\t');
		}
		firstArgument = true;
		return this;
	}
	
	public ArgScriptWriter option(String name) {
		if (!firstArgument) {
			sb.append(' ');
		}
		sb.append('-');
		sb.append(name);
		firstArgument = false;
		return this;
	}
	
	public ArgScriptWriter flag(String name, boolean value) {
		if (value) option(name);
		return this;
	}
	
//	public ArgScriptWriter arguments(String ... values) {
//		for (String v : values) {
//			if (!firstArgument) sb.append(' ');
//			sb.append(v);
//			firstArgument = false;
//		}
//		return this;
//	}
	
	public ArgScriptWriter arguments(Object ... values) {
		for (Object v : values) {
			if (!firstArgument) sb.append(' ');
			sb.append(v);
			firstArgument = false;
		}
		return this;
	}
	
	public <T> ArgScriptWriter arguments(List<T> list) {
		for (T v : list) {
			if (!firstArgument) sb.append(' ');
			sb.append(v);
			firstArgument = false;
		}
		return this;
	}
	
	public ArgScriptWriter literal(String text) {
		if (!firstArgument) {
			sb.append(' ');
		}
		sb.append('"');
		sb.append(text);
		sb.append('"');
		firstArgument = false;
		return this;
	}
	
	public ArgScriptWriter parenthesis(String text) {
		if (!firstArgument) {
			sb.append(' ');
		}
		sb.append('(');
		sb.append(text);
		sb.append(')');
		firstArgument = false;
		return this;
	}
	
	public ArgScriptWriter floats(float ... values) {
		HashManager hasher = HashManager.get();
		for (float v : values) {
			if (!firstArgument) sb.append(' ');
			sb.append(hasher.floatToString(v));
			firstArgument = false;
		}
		return this;
	}
	
	public ArgScriptWriter floats(List<Float> values) {
		HashManager hasher = HashManager.get();
		for (float v : values) {
			if (!firstArgument) sb.append(' ');
			sb.append(hasher.floatToString(v));
			firstArgument = false;
		}
		return this;
	}
	
	public ArgScriptWriter ints(int ... values) {
		for (int v : values) {
			if (!firstArgument) sb.append(' ');
			sb.append(Integer.toString(v));
			firstArgument = false;
		}
		return this;
	}

	public ArgScriptWriter vector(float ... values) {
		HashManager hasher = HashManager.get();
		if (!firstArgument) sb.append(' ');
		firstArgument = false;
		
		boolean firstValue = true;
		sb.append('(');
		for (float v : values) {
			if (!firstValue) sb.append(", ");
			sb.append(hasher.floatToString(v));
			firstValue = false;
		}
		sb.append(')');
		return this;
	}
	
	public ArgScriptWriter vector2(Vector2 value) {
		return vector(value.getX(), value.getY());
	}
	
	public ArgScriptWriter vector3(Vector3 value) {
		return vector(value.getX(), value.getY(), value.getZ());
	}
	
	public ArgScriptWriter vector4(Vector4 value) {
		return vector(value.getX(), value.getY(), value.getZ(), value.getW());
	}
	
	public ArgScriptWriter color(ColorRGB color) {
		return vector(color.getR(), color.getG(), color.getB());
	}
	
	public ArgScriptWriter colorRGBA(ColorRGBA color) {
		return vector(color.getR(), color.getG(), color.getB(), color.getA());
	}
	
	public ArgScriptWriter vectors(float[] ... values) {
		for (float[] value : values) vector(value);
		return this;
	}
	
	public ArgScriptWriter colors(ColorRGB ... values) {
		for (ColorRGB value : values) color(value);
		return this;
	}
	
	public ArgScriptWriter colorsRGBA(ColorRGBA ... values) {
		for (ColorRGBA value : values) colorRGBA(value);
		return this;
	}
	
	public ArgScriptWriter vectors(List<float[]> values) {
		for (float[] value : values) vector(value);
		return this;
	}
	
	public ArgScriptWriter vector3s(List<Vector3> values) {
		for (Vector3 value : values) vector3(value);
		return this;
	}
	
	public ArgScriptWriter colors(List<ColorRGB> values) {
		for (ColorRGB value : values) color(value);
		return this;
	}
	
	public ArgScriptWriter colorsRGBA(List<ColorRGBA> values) {
		for (ColorRGBA value : values) colorRGBA(value);
		return this;
	}
	
	/**
	 * Returns true if the given list is empty or only contains a 1.0 value.
	 * @return
	 */
	public boolean isDefault(List<Float> list) {
		return list.isEmpty() || (list.size() == 1 && list.get(0) == 1.0f);
	}
	
	/**
	 * Returns true if the given list is empty or only contains one `defaultValue` value.
	 * @return
	 */
	public boolean isDefault(List<Float> list, float defaultValue) {
		return list.isEmpty() || (list.size() == 1 && list.get(0) == defaultValue);
	}
	
	/**
	 * Returns true if the given list is empty or only contains a white color.
	 * @return
	 */
	public boolean isDefaultColor(List<ColorRGB> list) {
		return list.isEmpty() || (list.size() == 1 && list.get(0).isWhite());
	}
}
