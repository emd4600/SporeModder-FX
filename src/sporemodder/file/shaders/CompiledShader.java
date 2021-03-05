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
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CompiledShader {

	public final int[] fragmentIndices = new int[32];
	public byte[] data;
	public final List<ShaderDataUniform> dataUniforms = new ArrayList<>();
	public final List<Integer> startRegisters = new ArrayList<>();
	// The combined flags of shader data
	public int dataFlags;  // field_12C
	
	public String getSignatureString() {
		return getSignatureString(fragmentIndices);
	}
	
	public static String getSignatureString(int[] fragmentIndices) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fragmentIndices.length; ++i) {
			if (fragmentIndices[i] == 0) break;
			String text = Integer.toHexString(fragmentIndices[i]);
			if (text.length() == 1) sb.append('0');
			sb.append(text);
		}
		return sb.toString();
	}
	
	public void read(StreamReader in) throws IOException {
		in.readUBytes(fragmentIndices);
		//in.readUShorts(fragmentIndices);
		
		data = new byte[in.readInt()];
		in.read(data);
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			ShaderDataUniform uniform = new ShaderDataUniform();
			uniform.read(in, false);
			dataUniforms.add(uniform);
		}
		
		for (int i = 0; i < count; ++i) {
			startRegisters.add(in.readInt());
		}
		
		dataFlags = in.readInt();
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeUBytes(fragmentIndices);
		if (data != null) {
			out.writeInt(data.length);
			out.write(data);
		} else {
			out.writeInt(0);
		}
		
		out.writeInt(dataUniforms.size());
		for (ShaderDataUniform uniform : dataUniforms) {
			uniform.write(out, false);
		}
		for (int i : startRegisters) out.writeInt(i);
		
		out.writeInt(dataFlags);
	}
}
