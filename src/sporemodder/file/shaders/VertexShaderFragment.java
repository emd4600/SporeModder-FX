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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class VertexShaderFragment extends ShaderFragment {
	
	public static final String KEYWORD = "vertexFragment";
	
	public static final ArgScriptEnum InputEnum = new ArgScriptEnum();
	static {
		InputEnum.add(0, "position");
		
		InputEnum.add(2, "normal");
		InputEnum.add(3, "color");
		
		InputEnum.add(5, "color1");
		InputEnum.add(6, "texcoord0");
		InputEnum.add(7, "texcoord1");
		InputEnum.add(8, "texcoord2");
		InputEnum.add(9, "texcoord3");
		InputEnum.add(10, "texcoord4");
		InputEnum.add(11, "texcoord5");
		InputEnum.add(12, "texcoord6");
		InputEnum.add(13, "texcoord7");
		InputEnum.add(14, "blendIndices");
		InputEnum.add(15, "blendWeights");
		InputEnum.add(16, "pointSize");
		InputEnum.add(17, "position2");
		InputEnum.add(18, "normal2");
		InputEnum.add(19, "tangent");
		InputEnum.add(20, "binormal");
		InputEnum.add(21, "fog");
		InputEnum.add(22, "blendIndices2");
		InputEnum.add(23, "blendWeights2");
	}
	
	public static final ArgScriptEnum OutputEnum = new ArgScriptEnum();
	static {
		OutputEnum.add(0, "position");
		OutputEnum.add(2, "normal");
		OutputEnum.add(3, "color");
		OutputEnum.add(5, "color1");
		OutputEnum.add(21, "fog");
		OutputEnum.add(16, "pointSize");
	}
	
	// 1 << RWDECLUSAGE
	public static final int VS_INPUT_NORMAL = 0x4;
	public static final int VS_INPUT_COLOR = 0x8;
	public static final int VS_INPUT_COLOR1 = 0x20;
	public static final int VS_INPUT_INDICES = 0x4000;
	public static final int VS_INPUT_WEIGHTS = 0x8000;
	public static final int VS_INPUT_POINTSIZE = 0x10000;
	public static final int VS_INPUT_POSITION2 = 0x20000;
	public static final int VS_INPUT_NORMAL2 = 0x40000;
	public static final int VS_INPUT_TANGENT = 0x80000;
	public static final int VS_INPUT_BINORMAL = 0x100000;
	public static final int VS_INPUT_FOG = 0x200000;
	public static final int VS_INPUT_INDICES2 = 0x400000;
	public static final int VS_INPUT_WEIGHTS2 = 0x800000;
	public static final int VS_INPUT_SHL = 6;
	
	// Only these and position are really used for output, the rest for Current
	public static final int VS_OUTPUT_COLOR = 0x8;
	public static final int VS_OUTPUT_COLOR1 = 0x20;
	public static final int VS_OUTPUT_FOG = 0x200000;
	public static final int VS_OUTPUT_POINTSIZE = 0x10000;

	// ???
	public static final int PS_INPUT_POSITION = 0x2;
	public static final int PS_INPUT_NORMAL = 0x4;
	public static final int PS_INPUT_TANGENT = 0x8;
	public static final int PS_INPUT_BINORMAL = 0x10;
	public static final int PS_INPUT_COLOR = 0x20;
	public static final int PS_INPUT_COLOR1 = 0x140;
	public static final int PS_INPUT_INDICES = 0x80;
	
	public static final int PS_OUTPUT_COLOR = 0x2;
	public static final int PS_OUTPUT_COLOR1 = 0x4;
	public static final int PS_OUTPUT_COLOR2 = 0x8;
	public static final int PS_OUTPUT_COLOR3 = 0x10;
	public static final int PS_OUTPUT_DEPTH = 0x20;

	public byte numOutputTexcoords;  // 29h
	public byte texcoordSize;  // 2Ah
	
	public void read(StreamReader in) throws IOException {
		input = in.readInt();
		output = in.readInt();
		numOutputTexcoords = in.readByte();
		texcoordSize = in.readByte();
		numRegisters = in.readUByte();
		flags = in.readInt();
		
		mainCode = in.readString(StringEncoding.ASCII, in.readInt());
		declareCode = in.readString(StringEncoding.ASCII, in.readInt());
		
		int variableCount = in.readInt();
		
		for (int i = 0; i < variableCount; i++) {
			ShaderDataUniform variable = new ShaderDataUniform();
			variable.read(in, true);
			shaderData.add(variable);
		}
		
		if ((flags & 0x2) != 0) {
			shaderName = in.readString(StringEncoding.ASCII, in.readInt());
		}
	}
	
	public void write(StreamWriter out) throws IOException {
		
		out.writeInt(input);
		out.writeInt(output);
		out.writeByte(numOutputTexcoords);
		out.writeByte(texcoordSize);
		out.writeUByte(numRegisters);
		out.writeInt(flags);
		
		if (mainCode != null) {
			out.writeInt(mainCode.length());
			out.writeString(mainCode, StringEncoding.ASCII);
		} else {
			out.writeInt(0);
		}
		if (declareCode != null) {
			out.writeInt(declareCode.length());
			out.writeString(declareCode, StringEncoding.ASCII);
		} else {
			out.writeInt(0);
		}
		
		out.writeInt(shaderData.size());
		for (ShaderDataUniform variable : shaderData) {
			variable.write(out, true);
		}
		
		if ((flags & FLAG_NAME) != 0) {
			out.writeInt(shaderName.length());
			out.writeString(shaderName, StringEncoding.ASCII);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(shaderName).startBlock();
		
		boolean needsBlankLine = false;
		if (input != 0) {
			writer.command("input");
			for (int i : InputEnum.getValues()) {
				if (((1 << i) & input) != 0) writer.arguments(InputEnum.get(i));
			}
			needsBlankLine = true;
		}
		
		if (output != 0 || numOutputTexcoords != 0) {
			writer.command("output");
			for (int i : InputEnum.getValues()) {
				if (((1 << i) & output) != 0) writer.arguments(InputEnum.get(i));
			}
			if (numOutputTexcoords != 0 || texcoordSize != 0) writer.option("texcoords").arguments(numOutputTexcoords, "float" + texcoordSize);
			needsBlankLine = true;
		}
		
		if (needsBlankLine) writer.blankLine();
		
		codeToArgScript(writer);
		
		writer.endBlock().commandEND();
	}
	
	public static class Parser extends ArgScriptBlock<ShaderFragmentUnit> {
		
		private VertexShaderFragment fragment;
		private final CodeParser declareCodeParser = new CodeParser(true);
		private final CodeParser mainCodeParser = new CodeParser(false);

		@Override public void parse(ArgScriptLine line) {
			if (getData().isAlreadyParsed()) {
				stream.addError(line.createError("Only one fragment per file is allowed."));
			}
			else if (!KEYWORD.equals(getData().getRequiredType())) {
				stream.addError(line.createError("This file can only contain a single " + KEYWORD + " block."));
			}
			
			if (getData().getFragment() == null) {
				fragment = new VertexShaderFragment();
				getData().setFragment(fragment);
			} else {
				fragment = (VertexShaderFragment) getData().getFragment();
			}

			mainCodeParser.fragment = fragment;
			declareCodeParser.fragment = fragment;
			
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				fragment.shaderName = args.get(0);
			}
			
			stream.startBlock(this);
		}
		
		@Override public void setData(ArgScriptStream<ShaderFragmentUnit> stream, ShaderFragmentUnit data) {
			super.setData(stream, data);
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			addParser("input", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					fragment.input = 0;
					for (int i = 0; i < args.size(); ++i) {
						int a = InputEnum.get(args, i);
						fragment.input |= 1 << a;
					}
				}
			}));
			
			addParser("output", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 0, Integer.MAX_VALUE)) {
					fragment.output = 0;
					for (int i = 0; i < args.size(); ++i) {
						int a = InputEnum.get(args, i);
						fragment.output |= 1 << a;
					}
				}
				
				if (line.getOptionArguments(args, "texcoords", 2)) {
					Number value;
					if ((value = stream.parseInt(args, 0, 1, 8)) != null) fragment.numOutputTexcoords = value.byteValue();
					
					String format = args.get(1);
					if (!format.startsWith("float")) {
						stream.addError(line.createErrorForOptionArgument("texcoords", "Wrong format, expected float1, float2, float3 or float4", 1));
					} else {
						try {
							fragment.texcoordSize = (byte) Integer.parseInt(format.substring("float".length()));
						} catch (Exception e) {
							stream.addError(line.createErrorForOptionArgument("texcoords", "Wrong format, expected float1, float2, float3 or float4", 1));
						}
					}
				}
			}));
			
			addParser("declareCode", declareCodeParser);
			addParser("code", mainCodeParser);
		}
	}
	
	private static void generateVertIn(BufferedWriter out, int input) throws IOException {
		out.write("struct cVertIn");
		out.newLine();
		out.write("{");
		out.newLine();
		if ((input & 3) != 0) {
			out.write("float4 position : POSITION0;");
			out.newLine();
		}
		if ((input & VS_INPUT_NORMAL) != 0) {
			out.write("float4 normal : NORMAL0;");
			out.newLine();
		}
		if ((input & (10|VS_INPUT_COLOR)) != 0) {
			out.write("float4 color : COLOR0;");
			out.newLine();
		}
		for (int i = 0; i < 8; ++i) {
			if ((input & (1 << (VS_INPUT_SHL+i))) != 0) {
				out.write("float4 texcoord" + i + " : TEXCOORD" + i + ";");
				out.newLine();
			}
		}
		if ((input & VS_INPUT_INDICES) != 0) {
			out.write("int4 indices : BLENDINDICES0;");
			out.newLine();
		}
		if ((input & VS_INPUT_WEIGHTS) != 0) {
			out.write("float4 weights : BLENDWEIGHT0;");
			out.newLine();
		}
		if ((input & VS_INPUT_POSITION2) != 0) {
			out.write("float4 position2 : POSITION1;");
			out.newLine();
		}
		if ((input & VS_INPUT_NORMAL2) != 0) {
			out.write("float4 normal2 : NORMAL1;");
			out.newLine();
		}
		if ((input & VS_INPUT_INDICES2) != 0) {
			out.write("int4 indices2 : BLENDINDICES1;");
			out.newLine();
		}
		if ((input & VS_INPUT_WEIGHTS2) != 0) {
			out.write("float4 weights2 : BLENDWEIGHT1;");
			out.newLine();
		}
		if ((input & VS_INPUT_TANGENT) != 0) {
			out.write("float4 tangent : TANGENT0;");
			out.newLine();
		}
		if ((input & VS_INPUT_BINORMAL) != 0) {
			out.write("float4 binormal : BINORMAL0;");
			out.newLine();
		}
		if ((input & VS_INPUT_COLOR1) != 0) {
			out.write("float4 color1 : COLOR1;");
			out.newLine();
		}
		if ((input & VS_INPUT_FOG) != 0) {
			out.write("float fog : FOG;");
			out.newLine();
		}
		if ((input & VS_INPUT_POINTSIZE) != 0) {
			out.write("float pointSize : PSIZE;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private static void generateVertCurrent(BufferedWriter out, int inputOutput, int numTexcoords, int[] texcoordSizes) throws IOException {
		out.write("struct cVertCurrent");
		out.newLine();
		out.write("{");
		out.newLine();
		out.write("float4 position;");
		out.newLine();
		if ((inputOutput & VS_INPUT_NORMAL) != 0) {
			out.write("float4 normal;");
			out.newLine();
		}
		if ((inputOutput & (10|VS_INPUT_COLOR)) != 0) {
			out.write("float4 color;");
			out.newLine();
		}
		for (int i = 0; i < numTexcoords; ++i) {
			out.write("float" + texcoordSizes[i] + " texcoord" + i + ";");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_INDICES) != 0) {
			out.write("int4 indices;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_WEIGHTS) != 0) {
			out.write("float4 weights;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_POSITION2) != 0) {
			out.write("float4 position2;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_NORMAL2) != 0) {
			out.write("float4 normal2;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_INDICES2) != 0) {
			out.write("int4 indices2;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_WEIGHTS2) != 0) {
			out.write("float4 weights2;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_TANGENT) != 0) {
			out.write("float4 tangent;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_BINORMAL) != 0) {
			out.write("float4 binormal;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_COLOR1) != 0) {
			out.write("float4 color1;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_FOG) != 0) {
			out.write("float fog;");
			out.newLine();
		}
		if ((inputOutput & VS_INPUT_POINTSIZE) != 0) {
			out.write("float pointSize;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private static void generateVertOut(BufferedWriter out, int output, int numOutputTexcoords, int[] texcoordSizes) throws IOException {
		out.write("struct cVertOut");
		out.newLine();
		out.write("{");
		out.newLine();
		out.write("float4 position : POSITION;");
		out.newLine();
		if ((output & (10|VS_OUTPUT_COLOR)) != 0) {
			out.write("float4 diffuse : COLOR0;");
			out.newLine();
		}
		for (int i = 0; i < numOutputTexcoords; ++i) {
			out.write("float" + texcoordSizes[i] + " texcoord" + i + " : TEXCOORD" + i + ";");
			out.newLine();
		}
		if ((output & VS_OUTPUT_COLOR1) != 0) {
			out.write("float4 color1 : COLOR1;");
			out.newLine();
		}
		if ((output & VS_OUTPUT_FOG) != 0) {
			out.write("float fog : FOG;");
			out.newLine();
		}
		if ((output & VS_OUTPUT_POINTSIZE) != 0) {
			out.write("float pointSize : PSIZE;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private static void generateInputCopy(BufferedWriter out, int input) throws IOException {
		out.newLine();
		if ((input & 3) != 0) {
			out.write("Current.position = In.position;");
			out.newLine();
		}
		if ((input & VS_INPUT_NORMAL) != 0) {
			out.write("Current.normal = In.normal;");
			out.newLine();
		}
		if ((input & (10|VS_INPUT_COLOR)) != 0) {
			out.write("Current.color = In.color;");
			out.newLine();
		}
		for (int i = 0; i < 8; ++i) {
			if ((input & (1 << (VS_INPUT_SHL+i))) != 0) {
				out.write("Current.texcoord" + i + " = In.texcoord" + i + ";");
				out.newLine();
			}
		}
		if ((input & VS_INPUT_INDICES) != 0) {
			out.write("Current.indices = In.indices;");
			out.newLine();
		}
		if ((input & VS_INPUT_WEIGHTS) != 0) {
			out.write("Current.weights = In.weights;");
			out.newLine();
		}
		if ((input & VS_INPUT_POSITION2) != 0) {
			out.write("Current.position2 = In.position2;");
			out.newLine();
		}
		if ((input & VS_INPUT_NORMAL2) != 0) {
			out.write("Current.normal2 = In.normal2;");
			out.newLine();
		}
		if ((input & VS_INPUT_INDICES2) != 0) {
			out.write("Current.indices2 = In.indices2;");
			out.newLine();
		}
		if ((input & VS_INPUT_WEIGHTS2) != 0) {
			out.write("Current.weights2 = In.weights2;");
			out.newLine();
		}
		if ((input & VS_INPUT_TANGENT) != 0) {
			out.write("Current.tangent = In.tangent;");
			out.newLine();
		}
		if ((input & VS_INPUT_BINORMAL) != 0) {
			out.write("Current.binormal = In.binormal;");
			out.newLine();
		}
		if ((input & VS_INPUT_COLOR1) != 0) {
			out.write("Current.color1 = In.color1;");
			out.newLine();
		}
		if ((input & VS_INPUT_FOG) != 0) {
			out.write("Current.fog = In.fog;");
			out.newLine();
		}
		if ((input & VS_INPUT_POINTSIZE) != 0) {
			out.write("Current.pointSize = In.pointSize;");
			out.newLine();
		}
		out.newLine();
	}
	
	private static void generateOutputCopy(BufferedWriter out, int output, int numOutputTexcoords) throws IOException {
		out.newLine();
		out.write("Out.position = Current.position;");
		out.newLine();
		if ((output & (10|VS_OUTPUT_COLOR)) != 0) {
			out.write("Out.diffuse = Current.color;");
			out.newLine();
		}
		for (int i = 0; i < numOutputTexcoords; ++i) {
			out.write("Out.texcoord" + i + " = Current.texcoord" + i + ";");
			out.newLine();
		}
		if ((output & VS_OUTPUT_COLOR1) != 0) {
			out.write("Out.color1 = Current.color1;");
			out.newLine();
		}
		if ((output & VS_OUTPUT_FOG) != 0) {
			out.write("Out.fog = Current.fog;");
			out.newLine();
		}
		if ((output & VS_OUTPUT_POINTSIZE) != 0) {
			out.write("Out.pointSize = Current.pointSize;");
			out.newLine();
		}
	}
	
	public static List<ShaderDataUniform> generateHLSL(BufferedWriter out, List<VertexShaderFragment> fragments) throws IOException {
		int input = 0;
		int output = 0;
		int texcoordCount = 0;
		final int[] texcoordSizes = new int[8];
		
		int texcoordIndex = 0;
		for (VertexShaderFragment frag : fragments) {
			input |= frag.input;
			output |= frag.output;

			for (int i = 0; i < frag.numOutputTexcoords; ++i) {
				if (texcoordIndex + i >= 8) break;
				++texcoordCount;
				texcoordSizes[texcoordIndex + i] = frag.texcoordSize;
			}
		}
		
		List<ShaderDataUniform> uniforms = ShaderFragment.generateDeclareCode(out, fragments);
		
		out.newLine();
		generateVertIn(out, input);
		generateVertCurrent(out, input | output, texcoordCount, texcoordSizes);
		generateVertOut(out, output, texcoordCount, texcoordSizes);
		
		out.write("cVertOut main( cVertIn In )"); out.newLine();
		out.write('{'); out.newLine();
		out.write("cVertCurrent Current;"); out.newLine();
		out.write("cVertOut Out;"); out.newLine();
		
		generateInputCopy(out, input);
		
		if ((input & 0x20) != 0) {
			out.write("Current.color = In.color;"); out.newLine();
		}
		
		texcoordIndex = 0;
		for (VertexShaderFragment frag : fragments) {
			if (frag.mainCode != null && !frag.mainCode.isEmpty()) {
				out.newLine();
				ShaderFragment.replaceCodeIndices(out, frag.mainCode, texcoordIndex, 0);
			}
			texcoordIndex += frag.numOutputTexcoords;
		}
		
		generateOutputCopy(out, output, texcoordCount);
		
		out.write("return Out;"); out.newLine();
		out.write('}'); out.newLine();
		
		return uniforms;
	}
}
