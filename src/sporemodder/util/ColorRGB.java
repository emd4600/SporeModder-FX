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
package sporemodder.util;

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.metadata.StructureMetadata;
import javafx.scene.paint.Color;
import sporemodder.HashManager;

@Structure(StructureEndian.LITTLE_ENDIAN)
public class ColorRGB {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ColorRGB> STRUCTURE_METADATA = StructureMetadata.generate(ColorRGB.class);
	
	private float r;
	private float g;
	private float b;
	
	public ColorRGB(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public ColorRGB(int r, int g, int b) {
		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
	}
	
	public ColorRGB(Color color) {
		this.r = (float) color.getRed();
		this.g = (float) color.getGreen();
		this.b = (float) color.getBlue();
	}
	
	public ColorRGB(ColorRGB color) {
		copy(color);
	}
	
	public void copy(ColorRGB color) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
	}
	
	public ColorRGB() {};
	
	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", 
				HashManager.get().floatToString(r),
				HashManager.get().floatToString(g),
				HashManager.get().floatToString(b));
	}
	
	public String toString255() {
		return String.format("(%d, %d, %d)", 
				Math.round(r * 255),
				Math.round(g * 255),
				Math.round(b * 255));
	}
	
	public float getR() {
		return r;
	}

	public float getG() {
		return g;
	}

	public float getB() {
		return b;
	}

	public void setR(float r) {
		this.r = r;
	}

	public void setG(float g) {
		this.g = g;
	}

	public void setB(float b) {
		this.b = b;
	}
	
	public boolean isWhite() {
		return r == 1.0f && g == 1.0f && b == 1.0f;
	}
	public boolean isBlack() {
		return r == 0.0f && g == 0.0f && b == 0.0f;
	}

	public void readLE(StreamReader in) throws IOException {
		r = in.readLEFloat();
		g = in.readLEFloat();
		b = in.readLEFloat();
	}
	
	public void readBE(StreamReader in) throws IOException {
		r = in.readFloat();
		g = in.readFloat();
		b = in.readFloat();
	}
	
	public void writeLE(StreamWriter out) throws IOException {
		out.writeLEFloat(r);
		out.writeLEFloat(g);
		out.writeLEFloat(b);
	}
	
	public void writeBE(StreamWriter out) throws IOException {
		out.writeFloat(r);
		out.writeFloat(g);
		out.writeFloat(b);
	}
	
	
	public static ColorRGB white() {
		return new ColorRGB(1.0f, 1.0f, 1.0f);
	}
	
	public static ColorRGB black() {
		return new ColorRGB(0.0f, 0.0f, 0.0f);
	}

	public Color toColor() {
		return Color.color(r, g, b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(b);
		result = prime * result + Float.floatToIntBits(g);
		result = prime * result + Float.floatToIntBits(r);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorRGB other = (ColorRGB) obj;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
			return false;
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
			return false;
		if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r))
			return false;
		return true;
	}
}
