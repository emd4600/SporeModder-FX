package sporemodder.file.shaders;

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class ShaderFragmentSelector {
	// Writes the byte if shaderData[field_2] != nullptr
	public static final int CHECK_DATA = 1;
	
	// Writes the byte if objectTypeColor != field_2
	public static final int CHECK_OBJECT_TYPE_COLOR = 2;
	
	// Writes the byte + shaderData[field_2] if shaderData[field_2] != nullptr
	public static final int CHECK_ADD_DATA = 4;
	
	// Writes the byte if shaderData[field_2] != nullptr and shaderData[field_4] != nullptr
	public static final int CHECK_DATA_2 = 5;
	
	// Writes the byte if shaderData[field_2] != nullptr and shaderData[field_4] != nullptr and shaderData[field_6] != nullptr
	public static final int CHECK_DATA_3 = 6;
	
	// Writes the byte if shaderData[field_2] != nullptr and *(byte*)shaderData[field_2] == field_2
	public static final int CHECK_DATA_EQUAL = 7;
	
	// Writes the byte + shaderData[field_2] if shaderData[field_2] != nullptr and shaderData[field_4] != nullptr
	public static final int CHECK_ADD_DATA_2 = 9;
	
	// Writes the byte if samplers[field_2] != nullptr
	public static final int CHECK_SAMPLER = 10;
	
	public int fragmentIndex;
	public int checkType;
	public int field_2;
	public int field_4;
	public int field_6;
	public int vertexUsageFlags;
	// If any of these flags are missing, does not apply fragment
	public int requiredFlags;
	// If the current flags aren't exactly these, does not apply fragment
	public int exactFlags;
	public int flags;
	public int field_18  = -1;  // compared with shader data 23A, always if it's not -1
	
	public void read(StreamReader in, int version) throws IOException {
		checkType = in.readByte();
		
		if (version <= 6) {
			in.readByte();
		}
		
		field_2 = in.readShort();
		field_4 = in.readShort();
		field_6 = in.readShort();
		
		if (version <= 6) {
			in.readShort();
			in.readShort();
			in.readShort();
		}
		
		vertexUsageFlags = in.readInt();
		requiredFlags = in.readInt();
		exactFlags = in.readInt();
		flags = in.readInt();
		field_18 = in.readByte();
		
		fragmentIndex = in.readUByte();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeByte(checkType);
		stream.writeShort(field_2);
		stream.writeShort(field_4);
		stream.writeShort(field_6);
		stream.writeInt(vertexUsageFlags);
		stream.writeInt(requiredFlags);
		stream.writeInt(exactFlags);
		stream.writeInt(flags);
		stream.writeByte(field_18);
		stream.writeByte(fragmentIndex);
	}
}
