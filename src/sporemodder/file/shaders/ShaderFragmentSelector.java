package sporemodder.file.shaders;

import java.io.IOException;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.rw4.Direct3DEnums.RWDECLUSAGE;

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
	
	// Writes the byte if shaderData[field_2] != nullptr and *(byte*)shaderData[field_2] == field_4
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
	public int requiredFlags;  // 0Ch
	// If the current flags aren't exactly these, does not apply fragment
	public int excludedFlags;  // 10h  // excludedFlags!
	public int flags;  // 14h
	public int field_18  = -1;  // compared with shader data 23A, always if it's not -1
	
	public void toArgScript(ArgScriptWriter writer, List<String> fragmentNames) {
		if (fragmentIndex == 0) {
			writer.command("stop");
		}
		else {
			writer.command("add").arguments(fragmentNames.get(fragmentIndex));
		}
		
		if (checkType == CHECK_DATA) {
			writer.option("hasData").arguments(ShaderData.getName(field_2));
		}
		else if (checkType == CHECK_OBJECT_TYPE_COLOR) {
			writer.option("objectTypeColor").ints(field_2);
		}
		else if (checkType == CHECK_ADD_DATA) {
			writer.option("array").arguments(ShaderData.getName(field_2));
		}
		else if (checkType == CHECK_DATA_2) {
			writer.option("hasData").arguments(ShaderData.getName(field_2), ShaderData.getName(field_4));
		}
		else if (checkType == CHECK_DATA_3) {
			writer.option("hasData").arguments(ShaderData.getName(field_2), ShaderData.getName(field_4), ShaderData.getName(field_6));
		}
		else if (checkType == CHECK_DATA_EQUAL) {
			writer.option("compareData").arguments(ShaderData.getName(field_2), field_4);
		}
		else if (checkType == CHECK_SAMPLER) {
			writer.option("hasSampler").ints(field_2);
		}
		else if (checkType == 12) {
			writer.option("unk12").ints(field_2, field_4);
		}
		else if (checkType != 0) {
			throw new UnsupportedOperationException("Illegal operator " + checkType);
		}
		
		if (vertexUsageFlags != 0) {
			writer.option("elements");
			for (RWDECLUSAGE usage : RWDECLUSAGE.values()) {
				if ((vertexUsageFlags & (1 << usage.getId())) != 0) {
					writer.arguments(usage.toString());
				}
			}
		}
		
		if (requiredFlags != 0) {
			writer.option("require").arguments("0x" + Integer.toHexString(requiredFlags));
		}
		if (excludedFlags != 0) {
			writer.option("exclude").arguments("0x" + Integer.toHexString(excludedFlags));
		}
	}
	
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
		excludedFlags = in.readInt();
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
		stream.writeInt(excludedFlags);
		stream.writeInt(flags);
		stream.writeByte(field_18);
		stream.writeByte(fragmentIndex);
	}
}
