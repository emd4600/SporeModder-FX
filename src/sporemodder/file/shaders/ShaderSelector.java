package sporemodder.file.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class ShaderSelector extends MaterialShader {
	
	public static class ShaderSelectorEntry {
		public final List<ShaderFragmentSelector> vertexShaderSelectors = new ArrayList<>();
		public final List<ShaderFragmentSelector> pixelShaderSelectors = new ArrayList<>();
	}
	
	public final Map<Integer, ShaderSelectorEntry> entries = new TreeMap<>();

	@Override public void read(StreamReader in, int version) throws IOException {
		super.read(in, version);
		
		int index = 0;
		
		while ((index = in.readUByte()) != 0xFF) {
			
			ShaderSelectorEntry entry = new ShaderSelectorEntry();
			entries.put(index, entry);
			
			int count = in.readInt();
			for (int i = 0; i < count; i++) {
				ShaderFragmentSelector filter = new ShaderFragmentSelector();
				filter.read(in, version);
				entry.vertexShaderSelectors.add(filter);
			}
			
			count = in.readInt();
			for (int i = 0; i < count; i++) {
				ShaderFragmentSelector filter = new ShaderFragmentSelector();
				filter.read(in, version);
				entry.pixelShaderSelectors.add(filter);
			}
		}
	}
	
	@Override public void write(StreamWriter stream, int version) throws IOException {
		super.write(stream, version);
		
		for (Map.Entry<Integer, ShaderSelectorEntry> entry : entries.entrySet()) {
			stream.writeUByte(entry.getKey());
			
			stream.writeInt(entry.getValue().vertexShaderSelectors.size());
			for (ShaderFragmentSelector object : entry.getValue().vertexShaderSelectors) {
				object.write(stream);
			}
			
			stream.writeInt(entry.getValue().pixelShaderSelectors.size());
			for (ShaderFragmentSelector object : entry.getValue().pixelShaderSelectors) {
				object.write(stream);
			}
		}
		
		stream.writeByte(-1);
	}
}
