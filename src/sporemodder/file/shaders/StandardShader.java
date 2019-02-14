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
				uniform.readCompiled(in);
				entry.vertexShaderData.add(uniform);
			}
			
			count = in.readInt();
			for (int i = 0; i < count; i++) {
				ShaderDataUniform uniform = new ShaderDataUniform();
				uniform.readCompiled(in);
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
				uniform.write(out);
			}
			
			out.writeInt(shader.pixelShaderData.size());
			for (ShaderDataUniform uniform : shader.pixelShaderData) {
				uniform.write(out);
			}
		}
		out.writeUByte(0xFF);
	}
	
	public void compile(StandardShaderEntry entry, File sourceVertexFile, File sourcePixelFile, File includeFolder) throws IOException, InterruptedException {
		FXCompiler fxc = FXCompiler.get();
		File compiledVShader = fxc.compile("vs_3_0", sourceVertexFile, includeFolder);
		File compiledPShader = fxc.compile("ps_3_0", sourcePixelFile, includeFolder);
		
		entry.vertexShader = Files.readAllBytes(compiledVShader.toPath());
		entry.pixelShader = Files.readAllBytes(compiledPShader.toPath());
		
		fxc.getUniformData(entry.vertexShader, entry.vertexShaderData);
		fxc.getUniformData(entry.pixelShader, entry.pixelShaderData);
		
		compiledVShader.delete();
		compiledPShader.delete();
	}
}
