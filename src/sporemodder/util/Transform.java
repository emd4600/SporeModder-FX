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
import sporemodder.file.filestructures.StructureUnsigned;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

/**
 * A class that represents a 3D transformation: it can store the same information as a 4x4
 * matrix, but it's easier to use. This object stores the location, scale and rotation 
 * (as a 3x3 matrix) separately, making it easy to modify.
 */
public class Transform {

	private static final int FLAG_SCALE = 1;
	private static final int FLAG_ROTATE = 2;
	private static final int FLAG_OFFSET = 4;
	
	@StructureUnsigned(16) private int flags;  // short
	@StructureUnsigned(16) private int transformCount = 1;  // short
	private final Vector3 offset = new Vector3();
	private float scale = 1.0f;
	private final Matrix rotation = Matrix.getIdentity();
	
	public int getFlags() {
		return flags;
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public float getScale() {
		flags |= FLAG_SCALE;
		transformCount++;
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public Vector3 getOffset() {
		return offset;
	}
	
	public void setOffset(Vector3 offset) {
		flags |= FLAG_OFFSET;
		transformCount++;
		this.offset.set(offset);
	}
	
	public Matrix getRotation() {
		return rotation;
	}
	
	public void setRotation(Matrix rotation) {
		flags |= FLAG_ROTATE;
		transformCount++;
		this.rotation.copy(rotation);
	}

	public void read(StreamReader in) throws IOException {
		flags = in.readUShort();
		scale = in.readFloat();
		rotation.readLE(in);
		offset.readLE(in);
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeUShort(flags);
		out.writeFloat(scale);
		rotation.writeLE(out);
		offset.writeLE(out);
	}
	
	public void readComplete(StreamReader in) throws IOException {
		flags = in.readLEUShort();
		transformCount = in.readLEShort();
		offset.readLE(in);
		scale = in.readLEFloat();
		rotation.readLE(in);
	}
	
	public void writeComplete(StreamWriter out) throws IOException {
		out.writeLEUShort(flags);
		out.writeLEShort(transformCount);
		offset.writeLE(out);
		out.writeLEFloat(scale);
		rotation.writeLE(out);
	}
	
	public void toArgScript(ArgScriptWriter writer, boolean ignoreFlags) {
		toArgScriptNoDefault(writer, ignoreFlags);
		
		if (flags == 0) {
			writer.option("default");
		}
	}
	
	public void toArgScriptNoDefault(ArgScriptWriter writer, boolean ignoreFlags) {
		if (ignoreFlags || (flags & FLAG_OFFSET) != 0) {
			writer.option("offset");
			writer.arguments(offset.toString());
		}
		
		if (ignoreFlags || (flags & FLAG_SCALE) != 0) {
			writer.option("scale");
			writer.floats(scale);
		}
		
		if (ignoreFlags || (flags & FLAG_ROTATE) != 0) {
			writer.option("rotateXYZ");
			//TODO use transposed matrix?
			writer.floats(rotation.toEulerDegrees());
		}
	}
	
	public <T> void parse(ArgScriptStream<T> stream, ArgScriptLine line) {
		ArgScriptArguments args = new ArgScriptArguments();
		Number value = null;
		
		// - Offset - //
		
		if (line.getOptionArguments(args, "offset", 1)) {
			float[] arr = new float[3];
			stream.parseVector3(args, 0, arr);
			offset.set(new Vector3(arr));
			
			flags |= FLAG_OFFSET;
		}
		
		
		// - Scale - //
		
		if (line.getOptionArguments(args, "scale", 1) && (value = stream.parseFloat(args, 0)) != null) {
			scale = value.floatValue();
			
			flags |= FLAG_SCALE;
		}
		
		
		// - Rotation - //
		
		boolean usesRotation = false;
		float[] eulerRotation = new float[3];
		
		if (line.getOptionArguments(args, "rotateZ", 1) && 
				(value = stream.parseFloat(args, 0)) != null) {
			usesRotation = true;
			eulerRotation[2] = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "rotateY", 1) && 
				(value = stream.parseFloat(args, 0)) != null) {
			usesRotation = true;
			eulerRotation[1] = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "rotateX", 1) && 
				(value = stream.parseFloat(args, 0)) != null) {
			usesRotation = true;
			eulerRotation[0] = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "rotateXYZ", 3) && stream.parseFloats(args, eulerRotation)) {
			usesRotation = true;
		}
		
		if (line.getOptionArguments(args, "rotateZXY", 3) && stream.parseFloats(args, eulerRotation)) {
			usesRotation = true;
			// We must swap them
			float[] temp = new float[3];
			System.arraycopy(eulerRotation, 0, temp, 0, 3);
			eulerRotation[0] = temp[1];
			eulerRotation[1] = temp[2];
			eulerRotation[2] = temp[0];
		}
		
		if (usesRotation) {
			int todo = 0;
			//TODO SG_ufo_drop_cargo_Muzzle has wrong rotation; either we are calculating it wrong when encoding or when decoding
			rotation.rotate(Math.toRadians(eulerRotation[0]), Math.toRadians(eulerRotation[1]), Math.toRadians(eulerRotation[2]));
			rotation.transposed();
			
			flags |= FLAG_ROTATE;
		}
	}

	public void copy(Transform other) {
		scale = other.scale;
		offset.copy(other.offset);
		rotation.copy(other.rotation);
	}
}
