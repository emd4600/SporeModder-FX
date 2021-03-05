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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

import java.io.IOException;

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.HashManager;

public class MaterialShader {
	
	public static final int FLAG_NAME = 0x10;
	
	public static final int VERTEX_SHADER = 0xFFFE0000;
	public static final int PIXEL_SHADER = 0xFFFF0000;
	
	public int id;
	public int var_28;
	public int var_24;
	public int var_2C;
	public int vertexShaderVersion = VERTEX_SHADER | 0x00000300;
	public int pixelShaderVersion = PIXEL_SHADER | 0x00000300;
	public int flags = 4;
	
	public String name;
	
	public void read(StreamReader in, int version) throws IOException {
		id = in.readInt();
		
		var_28 = in.readInt();
		var_24 = in.readInt();
		var_2C = in.readInt();
		vertexShaderVersion = in.readInt();
		pixelShaderVersion = in.readInt();
		flags = in.readInt();
		
		if ((flags & FLAG_NAME) != 0) {
			name = in.readString(StringEncoding.ASCII, in.readInt());
		}
	}
	
	public void write(StreamWriter out, int version) throws IOException {
		out.writeInt(id);
		out.writeInt(var_28);
		out.writeInt(var_24);
		out.writeInt(var_2C);
		out.writeInt(vertexShaderVersion);
		out.writeInt(pixelShaderVersion);
		out.writeInt(flags);
		if ((flags & FLAG_NAME) != 0) {
			out.writeInt(name.length());
			out.writeString(name, StringEncoding.ASCII);
		}
	}
	
	/**
	 * Process the file name to get the shader ID and name. The given name must not have any extensions. The default format is
	 * ShaderID(ShaderName); if the name does not have parenthesis the shader ID is calculated from the shader name.
	 * @param name
	 */
	public void processName(String fileName) {
		int indexOf = fileName.indexOf("(");
		if (indexOf != -1) {
			id = HashManager.get().int32(fileName.substring(0, indexOf));
			name = fileName.substring(indexOf + 1, fileName.length() - 1);
			flags |= FLAG_NAME;
		}
		else if (fileName.startsWith("0x")) {
			id = HashManager.get().int32(name);
			name = fileName;
			flags |= FLAG_NAME;
		}
		else {
			name = fileName;
			id = HashManager.get().getFileHash(name);
			flags |= FLAG_NAME;
		}
	}
}
