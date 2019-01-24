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
import sporemodder.HashManager;

@Structure(StructureEndian.LITTLE_ENDIAN)
public class Vector4 {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<Vector4> STRUCTURE_METADATA = StructureMetadata.generate(Vector4.class);

	float x;
	float y;
	float z;
	float w;
	
	public Vector4() {}

	public Vector4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vector4(float[] array) {
		this.x = array[0];
		this.y = array[1];
		this.z = array[2];
		this.w = array[3];
	}
	
	public Vector4 set(Vector4 vector) {
		x = vector.x;
        y = vector.y;
        z = vector.z;
        w = vector.w;
        return this;
	}

	public Vector4 add(Vector4 vector) {
		x += vector.x;
        y += vector.y;
        z += vector.z;
        w += vector.w;
        return this; // method chaining would be very useful
	}
	
	public void readBE(StreamReader stream) throws IOException {
		x = stream.readFloat();
		y = stream.readFloat();
		z = stream.readFloat();
		w = stream.readFloat();
	}
	
	public void readLE(StreamReader stream) throws IOException {
		x = stream.readLEFloat();
		y = stream.readLEFloat();
		z = stream.readLEFloat();
		w = stream.readLEFloat();
	}
	
	public void writeBE(StreamWriter stream) throws IOException {
		stream.writeFloats(x, y, z, w);
	}
	
	public void writeLE(StreamWriter stream) throws IOException {
		stream.writeLEFloats(x, y, z, w);
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

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}
	
	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}
	
	public boolean allEqual() {
		return x == y && x == z && x == w;
	}
	
	@Override
	public String toString() {
		HashManager hasher = HashManager.get();
		StringBuilder sb = new StringBuilder();
		
		sb.append('(');
		sb.append(hasher.floatToString(x));
		sb.append(", ");
		sb.append(hasher.floatToString(y));
		sb.append(", ");
		sb.append(hasher.floatToString(z));
		sb.append(", ");
		sb.append(hasher.floatToString(w));
		sb.append(')');
		
		return sb.toString();
	}

	// Other operations...
}
