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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;

@Structure(StructureEndian.LITTLE_ENDIAN)
public class Vector2 {
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<Vector2> STRUCTURE_METADATA = StructureMetadata.generate(Vector2.class);

	float x;
	float y;
	
	public Vector2() {}
	public Vector2(Vector2 other) {
		set(other);
	}

	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2(float[] array) {
		this.x = array[0];
		this.y = array[1];
	}
	
	public void set(int i, float value) {
		if (i == 0) x = value;
		else y = value;
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float get(int i) {
		if (i == 0) return x;
		else return y;
	}
	
	public Vector2 set(Vector2 vector) {
		x = vector.x;
        y = vector.y;
        return this;
	}

	public Vector2 add(Vector2 vector) {
		x += vector.x;
        y += vector.y;
        return this; // method chaining would be very useful
	}
	
	public void readBE(StreamReader stream) throws IOException {
		x = stream.readFloat();
		y = stream.readFloat();
	}
	
	public void readLE(StreamReader stream) throws IOException {
		x = stream.readLEFloat();
		y = stream.readLEFloat();
	}
	
	public void writeBE(StreamWriter stream) throws IOException {
		stream.writeFloats(x, y);
	}
	
	public void writeLE(StreamWriter stream) throws IOException {
		stream.writeLEFloats(x, y);
	}
	

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		HashManager hasher = HashManager.get();
		StringBuilder sb = new StringBuilder();
		
		sb.append('(');
		sb.append(hasher.floatToString(x));
		sb.append(", ");
		sb.append(hasher.floatToString(y));
		sb.append(')');
		
		return sb.toString();
	}

	// Other operations...
}
