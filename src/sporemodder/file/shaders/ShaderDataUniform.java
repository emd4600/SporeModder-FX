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

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class ShaderDataUniform {
	public String name;
	public int dataIndex;  // short;
	public int field_2;  // short;
	public int registerSize;  // short; register size?
	public int register;  // short;
	public int flags;
	
	public void read(StreamReader in, boolean withName) throws IOException {
		if (withName) name = in.readString(StringEncoding.ASCII, in.readInt());
		dataIndex = in.readShort();
		field_2 = in.readShort();
		registerSize = in.readShort();
		register = in.readShort();
		flags = in.readInt();
	}

	public void write(StreamWriter out, boolean withName) throws IOException {
		if (withName) {
			if (name == null) {
				out.writeInt(0);
			} else {
				out.writeInt(name.length());
				out.writeString(name, StringEncoding.ASCII);
			}
		}
		out.writeShort(dataIndex);
		out.writeShort(field_2);
		out.writeShort(registerSize);
		out.writeShort(register);
		out.writeInt(flags);
	}
	
	public static int calculateRegisterSize(String type) {
		switch (type) {
		case "float":
		case "int":
		case "bool":
		case "half":
			return 4;
		default:
			if (type.startsWith("float")) type = type.substring(5);
			else if (type.startsWith("int")) type = type.substring(3);
			else if (type.startsWith("bool") || type.startsWith("half")) type = type.substring(4);
			
			// we don't care whether it's a float3 or a float4
			int n = 1;
			if (type.length() > 1) {
				// it's a matrix, e.g. float4x3
				n *= Character.getNumericValue(type.charAt(2));
			}
			
			return n;
		}
	}

	@Override
	public String toString() {
		return "ShaderDataUniform [dataIndex=0x" + Integer.toHexString(dataIndex) + ", field_2=0x" + Integer.toHexString(field_2)
				+ ", registerSize=" + registerSize + ", register=" + register + ", flags=0x" + Integer.toHexString(flags) + ", name=" + name + "]";
	}
	
	
}
