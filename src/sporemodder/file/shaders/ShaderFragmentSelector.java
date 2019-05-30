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
package sporemodder.file.shaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptWriter;

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
	public int[] data = new int[3];
	public int vertexUsageFlags;
	// If any of these flags are missing, does not apply fragment
	public int requiredFlags;  // 0Ch
	// If the current flags aren't exactly these, does not apply fragment
	public int excludedFlags;  // 10h  // excludedFlags!
	public int flags;  // 14h
	public int field_18  = -1;  // compared with shader data 23A, always if it's not -1
	
	public void toArgScript(ArgScriptWriter writer, List<? extends ShaderFragment> fragments) {
		if (fragmentIndex != 0) writer.arguments(fragments.get(fragmentIndex - 1).shaderName);
		
		if (checkType == CHECK_DATA) {
			writer.option("hasData").arguments(ShaderData.getName(data[0]));
		}
		else if (checkType == CHECK_OBJECT_TYPE_COLOR) {
			writer.option("objectTypeColor").ints(data[0]);
		}
		else if (checkType == CHECK_ADD_DATA) {
			writer.option("array").arguments(ShaderData.getName(data[0]));
		}
		else if (checkType == CHECK_DATA_2) {
			writer.option("hasData").arguments(ShaderData.getName(data[0]), ShaderData.getName(data[1]));
		}
		else if (checkType == CHECK_DATA_3) {
			writer.option("hasData").arguments(ShaderData.getName(data[0]), ShaderData.getName(data[1]), ShaderData.getName(data[2]));
		}
		else if (checkType == CHECK_DATA_EQUAL) {
			writer.option("compareData").arguments(ShaderData.getName(data[0]), data[1]);
		}
		else if (checkType == CHECK_SAMPLER) {
			writer.option("hasSampler").ints(data[0]);
		}
		else if (checkType == 12) {
			writer.option("unk12").ints(data[0], data[1]);
		}
		else if (checkType != 0) {
			throw new UnsupportedOperationException("Illegal operator " + checkType);
		}
		
		if (vertexUsageFlags != 0) {
			writer.option("elements");
			for (int usage : VertexShaderFragment.InputEnum.getValues()) {
				if ((vertexUsageFlags & (1 << usage)) != 0) {
					writer.arguments(VertexShaderFragment.InputEnum.get(usage));
				}
			}
		}
	}
	
	private int tryShaderDataName(ArgScriptLine line, ArgScriptArguments args, String optionName, int i) {
		Integer index = ShaderData.getIndex(args.get(i), false);
		if (index == null) {
			args.getStream().addError(line.createErrorForOptionArgument(optionName, 
					"'" + args.get(i) + "' is not a recognized shader data name", i + 1));
			return -1;
		} else {
			return index;
		}
	}
	
	public void parse(ArgScriptLine line) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		if (line.getOptionArguments(args, "hasData", 1, 3)) {
			for (int i = 0; i < args.size(); ++i) {
				data[i] = tryShaderDataName(line, args, "hasData", i);
				if (data[i] == -1) return;
			}
			
			if (args.size() == 1) checkType = CHECK_DATA;
			else if (args.size() == 2) checkType = CHECK_DATA_2;
			else checkType = CHECK_DATA_3;
		}
		else if (line.getOptionArguments(args, "objectTypeColor", 1)) {
			checkType = CHECK_OBJECT_TYPE_COLOR;
			Integer value;
			if ((value = args.getStream().parseInt(args, 0)) != null) {
				data[0] = value.shortValue();
			}
		}
		else if (line.getOptionArguments(args, "array", 1)) {
			checkType = CHECK_ADD_DATA;
			
			data[0] = tryShaderDataName(line, args, "array", 0);
			if (data[0] == -1) return;
		}
		else if (line.getOptionArguments(args, "compareData", 2)) {
			checkType = CHECK_DATA_EQUAL;
			
			data[0] = tryShaderDataName(line, args, "compareData", 0);
			if (data[0] == -1) return;
			
			Integer value;
			if ((value = args.getStream().parseInt(args, 1)) != null) {
				data[1] = value.shortValue();
			}
		}
		else if (line.getOptionArguments(args, "hasSampler", 1)) {
			checkType = CHECK_SAMPLER;
			Integer value;
			if ((value = args.getStream().parseInt(args, 0, 0, Short.MAX_VALUE)) != null) {
				data[0] = value.shortValue();
			}
		}
		else if (line.getOptionArguments(args, "unk12", 2)) {
			checkType = 12;
			Integer value;
			if ((value = args.getStream().parseInt(args, 0)) != null) {
				data[0] = value.shortValue();
			}
			if ((value = args.getStream().parseInt(args, 1)) != null) {
				data[1] = value.shortValue();
			}
		}
		
		if (line.getOptionArguments(args, "elements", 1, 32)) {
			vertexUsageFlags = 0;
			for (int i = 0; i < args.size(); ++i) {
				int x = VertexShaderFragment.InputEnum.get(args, i);
				if (x != -1) vertexUsageFlags |= 1 << x;
			}
		}
	}
	
	public void read(StreamReader in, int version) throws IOException {
		checkType = in.readByte();
		
		if (version <= 6) {
			in.readByte();
		}
		
		data[0] = in.readShort();
		data[1] = in.readShort();
		data[2] = in.readShort();
		
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
		//fragmentIndex = in.readUShort();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeByte(checkType);
		stream.writeShort(data[0]);
		stream.writeShort(data[1]);
		stream.writeShort(data[2]);
		stream.writeInt(vertexUsageFlags);
		stream.writeInt(requiredFlags);
		stream.writeInt(excludedFlags);
		stream.writeInt(flags);
		stream.writeByte(field_18);
		stream.writeByte(fragmentIndex);
	}

	@Override
	public String toString() {
		return "ShaderFragmentSelector [fragmentIndex=" + fragmentIndex + ", checkType=" + checkType + ", data="
				+ Arrays.toString(data) + ", vertexUsageFlags=0x" + Integer.toHexString(vertexUsageFlags) + ", requiredFlags=" + requiredFlags
				+ ", excludedFlags=" + excludedFlags + ", flags=" + flags + ", field_18=" + field_18 + "]";
	}
}
