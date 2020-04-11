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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class ShaderBuilder extends MaterialShader {
	
	public static class ShaderBuilderEntry {
		public final List<ShaderFragmentSelector> vertexShaderSelectors = new ArrayList<>();
		public final List<ShaderFragmentSelector> pixelShaderSelectors = new ArrayList<>();
	}
	
	public final Map<Integer, ShaderBuilderEntry> entries = new TreeMap<>();

	@Override public void read(StreamReader in, int version) throws IOException {
		super.read(in, version);
		
		int index = 0;
		
		while ((index = in.readUByte()) != 0xFF) {
			
			ShaderBuilderEntry entry = new ShaderBuilderEntry();
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
		
		for (Map.Entry<Integer, ShaderBuilderEntry> entry : entries.entrySet()) {
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
	
	private static void toArgScript(ArgScriptWriter writer, List<? extends ShaderFragment> fragments, ShaderFragmentSelector s, String keyword) {
		writer.command(keyword);
		
		s.toArgScript(writer, fragments);
		
		// The group is always considered excluded
		int excludedFlags = s.excludedFlags & ~s.flags;
		if (excludedFlags != 0) {
			writer.option("exclude");
			for (int j = 0; j < 32; ++j) {
				if (((1 << j) & excludedFlags) != 0) {
					writer.arguments("0x" + Integer.toHexString(1 << j));
				}
			}
		}
		
		if (s.requiredFlags != 0) {
			writer.option("require");
			for (int j = 0; j < 32; ++j) {
				if (((1 << j) & s.requiredFlags) != 0) {
					writer.arguments("0x" + Integer.toHexString(1 << j));
				}
			}
		}
	}
	
	private static void toArgScript(List<ShaderFragmentSelector> selectors, ArgScriptWriter writer, List<? extends ShaderFragment> fragments) {
		if (selectors.isEmpty()) return;
		
		// Contains all the elements with the same flags, only if flags != 0
		List<ShaderFragmentSelector> list = new ArrayList<>();
		int lastFlags = 0;
		boolean needsBlankLine = false;
		
		for (ShaderFragmentSelector s : selectors) {
			if (s.flags != 0 && s.flags == lastFlags) {
				list.add(s);
			} 
			else {
				// We have to write the accumulated ones in a new group
				if (!list.isEmpty()) {
					writer.blankLine();
					writer.command("group").arguments("0x" + Integer.toHexString(lastFlags)).startBlock();
					for (ShaderFragmentSelector listS : list) {
						toArgScript(writer, fragments, listS, "select");
					}
					writer.endBlock().commandEND();
					list.clear();
					needsBlankLine = true;
				}
				
				if (s.flags == 0) {
					if (needsBlankLine) writer.blankLine();
					needsBlankLine = false;
					toArgScript(writer, fragments, s, "add");
				}
				else {
					// This is a new group
					list.add(s);
				}
			}
			
			lastFlags = s.flags;
		}
	}
	
	public void toArgScript(ArgScriptWriter writer, ShaderBuilderEntry entry, ShaderFragments fragments) {
		writer.command("vertexShader").startBlock();
		toArgScript(entry.vertexShaderSelectors, writer, fragments.vertexFragments);
		writer.endBlock().commandEND();
		writer.blankLine();
		writer.command("pixelShader").startBlock();
		toArgScript(entry.pixelShaderSelectors, writer, fragments.pixelFragments);
		writer.endBlock().commandEND();
	}
	
	public static class Parser extends ArgScriptBlock<ShaderBuilderEntry> {
		
		private Map<String, Integer> fragmentsMap;
		private boolean isVertexShader;
		
		// Maps group name to flags
		private final Map<String, Integer> groups = new HashMap<>();
		
		private int lastFlags;
		
		public Parser(boolean isVertexShader, Map<String, Integer> fragmentsMap) {
			this.fragmentsMap = fragmentsMap;
			this.isVertexShader = isVertexShader;
		}

		@Override public void parse(ArgScriptLine line) {
			if (isVertexShader && !getData().vertexShaderSelectors.isEmpty()) {
				stream.addError(line.createError("Vertex shader already specified."));
			}
			else if (!isVertexShader && !getData().pixelShaderSelectors.isEmpty()) {
				stream.addError(line.createError("Pixel shader already specified."));
			}
		
			stream.startBlock(this);
		}
		
		private void parseSelector(ShaderFragmentSelector s, ArgScriptLine line) {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 0, 1)) {
				if (args.size() == 1) {
					if (fragmentsMap != null) {
						Integer value = fragmentsMap.get(args.get(0));
						
						if (value == null) {
							stream.addError(line.createErrorForArgument(args.get(0) + " is not a defined "+ (isVertexShader ? "vertex" : "pixel") + "shader fragment.", 0));
							return;
						}
						
						s.fragmentIndex = value;
						if (isVertexShader) {
							s.fragmentIndex++;
						}
					}
				} else {
					s.fragmentIndex = 0;
				}
			}
			
			s.parse(line);
			
			if (line.getOptionArguments(args, "exclude", 1, Integer.MAX_VALUE)) {
				for (int i = 0; i < args.size(); ++i) {
					if (!groups.containsKey(args.get(i))) {
						stream.addWarning(line.createErrorForOptionArgument("exclude", args.get(i) + " is not the name of a defined group", 1 + i));
					}
					else {
						s.excludedFlags |= groups.get(args.get(i));
					}
				}
			}
			
			if (line.getOptionArguments(args, "require", 1, Integer.MAX_VALUE)) {
				for (int i = 0; i < args.size(); ++i) {
					if (!groups.containsKey(args.get(i))) {
						stream.addWarning(line.createErrorForOptionArgument("require", args.get(i) + " is not the name of a defined group", 1 + i));
					}
					else {
						s.requiredFlags |= groups.get(args.get(i));
					}
				}
			}
		}
		
		@Override public void setData(ArgScriptStream<ShaderBuilderEntry> stream, ShaderBuilderEntry data) {
			super.setData(stream, data);
			
			this.addParser("add", ArgScriptParser.create((parser, line) -> {
				ShaderFragmentSelector s = new ShaderFragmentSelector();
				parseSelector(s, line);
				
				if (isVertexShader) {
					getData().vertexShaderSelectors.add(s);
				} else {
					getData().pixelShaderSelectors.add(s);
				}
			}));
			
			this.addParser("group", new ArgScriptBlock<ShaderBuilderEntry>() {
				// Contains all the selectors parsed in the last group. When the group ends, we apply flags to all of them
				private final List<ShaderFragmentSelector> lastGroup = new ArrayList<>();

				@Override public void parse(ArgScriptLine line) {
					lastGroup.clear();
					
					final ArgScriptArguments args = new ArgScriptArguments();
					if (line.getArguments(args, 1)) {
						lastFlags = lastFlags == 0 ? 1 : (lastFlags << 1);
						groups.put(args.get(0), lastFlags);
					}
					
					stream.startBlock(this);
				}
				
				@Override public void setData(ArgScriptStream<ShaderBuilderEntry> stream, ShaderBuilderEntry data) {
					super.setData(stream, data);
					
					this.addParser("select", ArgScriptParser.create((parser, line) -> {
						ShaderFragmentSelector s = new ShaderFragmentSelector();
						s.flags = lastFlags;
						parseSelector(s, line);
						
						if (isVertexShader) {
							getData().vertexShaderSelectors.add(s);
						} else {
							getData().pixelShaderSelectors.add(s);
						}
						lastGroup.add(s);
					}));
				}
				
				@Override public void onBlockEnd() {
					// The ones with only one are not included in exclude flags
					if (lastGroup.size() != 1) {
						for (ShaderFragmentSelector s : lastGroup) {
							s.excludedFlags |= lastFlags;
						}
					}
				}
			});
		}
	}
	
	public static ArgScriptStream<ShaderBuilderEntry> generateStream(ShaderBuilderEntry entry, Map<String, Integer> vsMap, Map<String, Integer> psMap) {
		ArgScriptStream<ShaderBuilderEntry> stream = new ArgScriptStream<>();
		stream.setData(entry);
		stream.addDefaultParsers();
		
		stream.addParser("vertexShader", new Parser(true, vsMap));
		stream.addParser("pixelShader", new Parser(false, psMap));
		
		return stream;
	}
	
	/**
	 * Uses a backtracking algorithm to generate and compile all possible valid combinations of shader fragments.
	 * @param output An array of 32 fragment indices
	 * @param builder
	 * @param outputIndex
	 * @param builderIndex
	 * @param flags
	 */
	private static void buildShaders(List<int[]> result, int[] output, List<ShaderFragmentSelector> builder, int outputIndex, int builderIndex, int flags) {
		if (builderIndex == builder.size()) {
			// No more selectors to process, time to compile
			if (outputIndex != output.length) {
				output[outputIndex] = 0;
			}
			int[] copy = new int[output.length];
			System.arraycopy(output, 0, copy, 0, output.length);
			result.add(copy);
			return;
		}
		
		ShaderFragmentSelector s = builder.get(builderIndex);
		// Ensure it fits the requirements
		if ((s.excludedFlags & flags) != 0 || (s.requiredFlags != 0 && (s.requiredFlags & flags) == 0)) {
			// If the selector isn't accepted, the outputIndex does not advance and flags do not change
			buildShaders(result, output, builder, outputIndex, builderIndex + 1, flags);
		}
		else if (s.vertexUsageFlags == 0 && s.checkType == 0) {
			// This is added always (at least for the current flags)
			if (s.fragmentIndex != 0) {
				output[outputIndex++] = s.fragmentIndex;
			}
			buildShaders(result, output, builder, outputIndex, builderIndex + 1, flags | s.flags);
		}
		else {
			// Two possibilities: either this selector is accepted or not
			
			// If the selector isn't accepted, the outputIndex does not advance and flags do not change
			buildShaders(result, output, builder, outputIndex, builderIndex + 1, flags);
						
			if (s.fragmentIndex != 0) {
				output[outputIndex++] = s.fragmentIndex;
			}
			buildShaders(result, output, builder, outputIndex, builderIndex + 1, flags | s.flags);
		}
	}
	
	public static List<int[]> buildShaders(List<ShaderFragmentSelector> builder) {
		List<int[]> list = new ArrayList<>();
		buildShaders(list, new int[32], builder, 0, 0, 0);
		return list;
	}
	
}
