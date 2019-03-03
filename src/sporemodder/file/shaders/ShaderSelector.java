package sporemodder.file.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptWriter;

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
	
	private static void toArgScript(List<ShaderFragmentSelector> selectors, ArgScriptWriter writer, List<String> fragmentNames) {
		int currentFlags = 0;
		boolean inBlock = false;
		
		for (int i = 0; i < selectors.size(); ++i) {
			ShaderFragmentSelector s = selectors.get(i);
			
			if (s.flags == 0) {
				// End the current block
				if (inBlock) writer.endBlock().commandEND();
				inBlock = false;

				s.toArgScript(writer, fragmentNames);
			}
			else if (s.flags == currentFlags) {
				s.toArgScript(writer, fragmentNames);
			}
			else {
				// End the current block
				if (inBlock) writer.endBlock().commandEND();
				inBlock = true;
				currentFlags = s.flags;
				writer.command("select").option("flags").arguments("0x" + Integer.toHexString(s.flags)).startBlock();
				
				s.toArgScript(writer, fragmentNames);
			}
		}
		
		if (inBlock) writer.endBlock().commandEND();
	}
	
	public void toArgScript(ArgScriptWriter writer, List<String> fragmentNames) {
		for (Map.Entry<Integer, ShaderSelectorEntry> entry : entries.entrySet()) {
			writer.command("renderType").ints(entry.getKey()).startBlock();
			
			ShaderSelectorEntry e = entry.getValue();
			toArgScript(entry.getValue().vertexShaderSelectors, writer, fragmentNames);
			
			writer.endBlock().commandEND();
		}
	}
}
