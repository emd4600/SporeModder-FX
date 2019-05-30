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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class StandardShader extends MaterialShader {
	public static class StandardShaderEntry {
		public byte[] vertexShader;
		public byte[] pixelShader;
		
		public final List<ShaderDataUniform> vertexShaderData = new ArrayList<>();
		public final List<ShaderDataUniform> pixelShaderData = new ArrayList<>();
	}
	
	// Selected depending on the value of shader data 0x201
	public final Map<Integer, StandardShaderEntry> entries = new TreeMap<>();
	
	public StandardShader() {
		super();
		flags |= 4;  // ?
	}
	
	@Override public void read(StreamReader in, int version) throws IOException {
		
		super.read(in, version);
		
		int renderType = in.readUByte();
		
		while (renderType != 0xFF) {
			
			StandardShaderEntry entry = new StandardShaderEntry();
			entries.put(renderType, entry);
			
			entry.vertexShader = new byte[in.readInt()];
			in.read(entry.vertexShader);
			
			entry.pixelShader = new byte[in.readInt()];
			in.read(entry.pixelShader);
			
			int count = in.readInt();
			for (int i = 0; i < count; i++) {
				ShaderDataUniform uniform = new ShaderDataUniform();
				uniform.read(in, false);
				entry.vertexShaderData.add(uniform);
			}
			
			count = in.readInt();
			for (int i = 0; i < count; i++) {
				ShaderDataUniform uniform = new ShaderDataUniform();
				uniform.read(in, false);
				entry.pixelShaderData.add(uniform);
			}
			
			renderType = in.readUByte();
		}
	}
	
	@Override public void write(StreamWriter out, int version) throws IOException {
		super.write(out, version);
		
		for (Map.Entry<Integer, StandardShaderEntry> entry : entries.entrySet()) {
			out.writeUByte(entry.getKey());
			
			StandardShaderEntry shader = entry.getValue();
			
			out.writeInt(shader.vertexShader.length);
			out.write(shader.vertexShader);
			
			out.writeInt(shader.pixelShader.length);
			out.write(shader.pixelShader);
			
			out.writeInt(shader.vertexShaderData.size());
			for (ShaderDataUniform uniform : shader.vertexShaderData) {
				uniform.write(out, false);
			}
			
			out.writeInt(shader.pixelShaderData.size());
			for (ShaderDataUniform uniform : shader.pixelShaderData) {
				uniform.write(out, false);
			}
		}
		out.writeUByte(0xFF);
	}
	
	public void compile(StandardShaderEntry entry, File sourceVertexFile, File sourcePixelFile, File includeFolder) throws IOException, InterruptedException {
		FXCompiler fxc = FXCompiler.get();
		File compiledVShader = fxc.compile(FXCompiler.VS_PROFILE, sourceVertexFile, includeFolder);
		File compiledPShader = fxc.compile(FXCompiler.PS_PROFILE, sourcePixelFile, includeFolder);
		
		entry.vertexShader = Files.readAllBytes(compiledVShader.toPath());
		entry.pixelShader = Files.readAllBytes(compiledPShader.toPath());
		
		fxc.getUniformData(entry.vertexShader, entry.vertexShaderData);
		fxc.getUniformData(entry.pixelShader, entry.pixelShaderData);
		
		compiledVShader.delete();
		compiledPShader.delete();
	}
}
