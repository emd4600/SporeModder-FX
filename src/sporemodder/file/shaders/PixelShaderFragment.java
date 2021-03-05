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

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.rw4.Direct3DEnums;

public class PixelShaderFragment extends ShaderFragment {
	
public static final String KEYWORD = "pixelFragment";
	
	public static final ArgScriptEnum InputEnum = new ArgScriptEnum();
	static {
		InputEnum.add(1, "position");
		InputEnum.add(2, "normal");
		InputEnum.add(3, "tangent");
		InputEnum.add(4, "binormal");
		InputEnum.add(5, "color");
		InputEnum.add(6, "color1");
		InputEnum.add(7, "indices");
	}
	
	public static final ArgScriptEnum OutputEnum = new ArgScriptEnum();
	static {
		OutputEnum.add(1, "color");
		OutputEnum.add(2, "color1");
		OutputEnum.add(3, "color2");
		OutputEnum.add(4, "color3");
		OutputEnum.add(5, "depth");
	}
	
	public static final int TEXCOORD_SHL = 16;

	public byte numSamplers;  // 28h
	public final ResourceID texture = new ResourceID();
	public int minFilter;  // 30h
	public int magFilter;  // 34h
	public int mipFilter;  // 38h
	public int numTextureAddresses;  // 3Ch
	public final int[] textureAddresses = new int[3];  // 40h
	
	public void read(StreamReader in) throws IOException {
		input = in.readInt();
		output = in.readInt();
		numSamplers = in.readByte();
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
		
		texture.setInstanceID(in.readInt());
		texture.setGroupID(in.readInt());
		
		minFilter = in.readInt();
		magFilter = in.readInt();
		mipFilter = in.readInt();
		numTextureAddresses = in.readInt();
		
		in.readInts(textureAddresses);
		
		if ((flags & FLAG_NAME) != 0) {
			shaderName = in.readString(StringEncoding.ASCII, in.readInt());
		}
	}
	
	public void write(StreamWriter out) throws IOException {
		if (shaderName != null) {
			flags |= FLAG_NAME;
		}
		
		out.writeInt(input);
		out.writeInt(output);
		out.writeByte(numSamplers);
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
		
		out.writeInt(texture.getInstanceID());
		out.writeInt(texture.getGroupID());
		
		out.writeInt(minFilter);
		out.writeInt(magFilter);
		out.writeInt(mipFilter);
		out.writeInt(numTextureAddresses);
		out.writeInts(textureAddresses);
		
		if ((flags & FLAG_NAME) != 0) {
			out.writeInt(shaderName.length());
			out.writeString(shaderName, StringEncoding.ASCII);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("pixelFragment").arguments(shaderName).startBlock();
		
		boolean needsBlankLine = false;
		if (input != 0) {
			writer.command("input");
			for (int i : InputEnum.getValues()) {
				if (((1 << i) & input) != 0) writer.arguments(InputEnum.get(i));
			}
			int numTexcoords = (0xFFFF0000 & input) >> 16;
			if (numTexcoords != 0) writer.option("texcoords").ints(numTexcoords);
			needsBlankLine = true;
		}
		
		if (output != 0) {
			writer.command("output");
			for (int i : OutputEnum.getValues()) {
				if (((1 << i) & output) != 0) writer.arguments(OutputEnum.get(i));
			}
			needsBlankLine = true;
		}
		
		if (numSamplers > 0) {
			writer.command("samplers").ints(numSamplers);
			needsBlankLine = true;
		}
		
		if (!texture.isDefault()) {
			writer.command("texture").arguments(texture);
			writer.option("minFilter").arguments(Direct3DEnums.D3DTEXTUREFILTERTYPE.getById(minFilter));
			writer.option("magFilter").arguments(Direct3DEnums.D3DTEXTUREFILTERTYPE.getById(magFilter));
			writer.option("mipFilter").arguments(Direct3DEnums.D3DTEXTUREFILTERTYPE.getById(mipFilter));
			if (numTextureAddresses > 0) {
				writer.option("addresses");
				for (int i = 0; i < numTextureAddresses; ++i) {
					writer.arguments(Direct3DEnums.D3DTEXTUREADDRESS.getById(textureAddresses[i]));
				}
			}
			needsBlankLine = true;
		}
		
		if (needsBlankLine) writer.blankLine();
		
		codeToArgScript(writer);
		
		writer.endBlock().commandEND();
	}
	
	public static class Parser extends ArgScriptBlock<ShaderFragmentUnit> {
		
		private PixelShaderFragment fragment;
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
				fragment = new PixelShaderFragment();
				getData().setFragment(fragment);
			} else {
				fragment = (PixelShaderFragment) getData().getFragment();
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
				if (line.getArguments(args, 0, Integer.MAX_VALUE)) {
					fragment.input = 0;
					for (int i = 0; i < args.size(); ++i) {
						int a = InputEnum.get(args, i);
						fragment.input |= 1 << a;
					}
				}
				
				if (line.getOptionArguments(args, "texcoords", 1)) {
					Number value;
					if ((value = stream.parseInt(args, 0, 1, 8)) != null) fragment.input |= value.byteValue() << TEXCOORD_SHL;
				}
			}));
			
			addParser("output", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					fragment.output = 0;
					for (int i = 0; i < args.size(); ++i) {
						int a = OutputEnum.get(args, i);
						fragment.output |= 1 << a;
					}
				}
			}));
			
			addParser("samplers", ArgScriptParser.create((parser, line) -> {
				Number value;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0, 1, 8)) != null) {
					fragment.numSamplers = value.byteValue();
				}
			}));
			
			addParser("texture", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					fragment.texture.parse(args, 0);
				}
				
				fragment.minFilter = Direct3DEnums.D3DTEXTUREFILTERTYPE.D3DTEXF_POINT.getId();
				fragment.magFilter = Direct3DEnums.D3DTEXTUREFILTERTYPE.D3DTEXF_POINT.getId();
				fragment.mipFilter = Direct3DEnums.D3DTEXTUREFILTERTYPE.D3DTEXF_POINT.getId();
				
				if (line.getOptionArguments(args, "minFilter", 1)) {
					try {
						fragment.minFilter = Direct3DEnums.D3DTEXTUREFILTERTYPE.valueOf(args.get(0)).getId();
					} catch (Exception e) {
						stream.addError(line.createErrorForOptionArgument("minFilter", args.get(0) + " is not a valid member of the D3DTEXTUREFILTERTYPE enum", 1));
					}
				}
				if (line.getOptionArguments(args, "magFilter", 1)) {
					try {
						fragment.magFilter = Direct3DEnums.D3DTEXTUREFILTERTYPE.valueOf(args.get(0)).getId();
					} catch (Exception e) {
						stream.addError(line.createErrorForOptionArgument("magFilter", args.get(0) + " is not a valid member of the D3DTEXTUREFILTERTYPE enum", 1));
					}
				}
				if (line.getOptionArguments(args, "mipFilter", 1)) {
					try {
						fragment.mipFilter = Direct3DEnums.D3DTEXTUREFILTERTYPE.valueOf(args.get(0)).getId();
					} catch (Exception e) {
						stream.addError(line.createErrorForOptionArgument("mipFilter", args.get(0) + " is not a valid member of the D3DTEXTUREFILTERTYPE enum", 1));
					}
				}
				
				if (line.getOptionArguments(args, "addresses", 1, 3)) {
					fragment.numTextureAddresses = args.size();
					fragment.textureAddresses[0] = -1;
					fragment.textureAddresses[1] = -1;
					fragment.textureAddresses[2] = -1;
					for (int i = 0; i < fragment.numTextureAddresses; ++i) {
						try {
							fragment.textureAddresses[i] = Direct3DEnums.D3DTEXTUREADDRESS.valueOf(args.get(0)).getId();
						} catch (Exception e) {
							stream.addError(line.createErrorForOptionArgument("addresses", args.get(0) + " is not a valid member of the D3DTEXTUREADDRESS enum", 1 + i));
						}
					}
				}
			}));
			
			addParser("declareCode", declareCodeParser);
			addParser("code", mainCodeParser);
		}
	}
	
	
	private static void generateFragIn(BufferedWriter out, int input) throws IOException {
		out.write("struct cFragIn");
		out.newLine();
		out.write("{");
		out.newLine();
		if ((input & (1 << 1)) != 0) {
			out.write("float4 position : POSITION0;");
			out.newLine();
		}
		if ((input & (1 << 2)) != 0) {
			out.write("float3 normal : NORMAL0;");
			out.newLine();
		}
		if ((input & (1 << 3)) != 0) {
			out.write("float3 tangent : TANGENT0;");
			out.newLine();
		}
		if ((input & (1 << 4)) != 0) {
			out.write("float3 binormal : BINORMAL0;");
			out.newLine();
		}
		if ((input & (1 << 5)) != 0) {
			out.write("float4 color : COLOR0;");
			out.newLine();
		}
		if ((input & (1 << 6)) != 0) {
			out.write("float4 color1 : COLOR1;");
			out.newLine();
		}
		
		int texcoordCount = input >> TEXCOORD_SHL;
		for (int i = 0; i < texcoordCount; ++i) {
			out.write("float4 texcoord" + i + " : TEXCOORD" + i + ";");
			out.newLine();
		}
		
		if ((input & (1 << 7)) != 0) {
			out.write("int4 indices : BLENDINDICES0;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private static void generateFragCurrent(BufferedWriter out, int output) throws IOException {
		out.write("struct cFragCurrent");
		out.newLine();
		out.write("{");
		out.newLine();
		if ((output & (1 << 1)) != 0) {
			out.write("float4 color;");
			out.newLine();
		}
		if ((output & (1 << 2)) != 0) {
			out.write("float4 color1;");
			out.newLine();
		}
		if ((output & (1 << 3)) != 0) {
			out.write("float4 color2;");
			out.newLine();
		}
		if ((output & (1 << 4)) != 0) {
			out.write("float4 color3;");
			out.newLine();
		}
		if ((output & (1 << 5)) != 0) {
			out.write("float depth;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private static void generateFragOut(BufferedWriter out, int output) throws IOException {
		out.write("struct cFragOut");
		out.newLine();
		out.write("{");
		out.newLine();
		if ((output & (1 << 1)) != 0) {
			out.write("float4 color : COLOR0;");
			out.newLine();
		}
		if ((output & (1 << 2)) != 0) {
			out.write("float4 color1 : COLOR1;");
			out.newLine();
		}
		if ((output & (1 << 3)) != 0) {
			out.write("float4 color2 : COLOR2;");
			out.newLine();
		}
		if ((output & (1 << 4)) != 0) {
			out.write("float4 color3 : COLOR3;");
			out.newLine();
		}
		if ((output & (1 << 5)) != 0) {
			out.write("float depth : DEPTH;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private static void generateOutputCopy(BufferedWriter out, int output) throws IOException {
		out.newLine();
		if ((output & (1 << 1)) != 0) {
			out.write("Out.color = Current.color;");
			out.newLine();
		}
		if ((output & (1 << 2)) != 0) {
			out.write("Out.color1 = Current.color1;");
			out.newLine();
		}
		if ((output & (1 << 3)) != 0) {
			out.write("Out.color2 = Current.color2;");
			out.newLine();
		}
		if ((output & (1 << 4)) != 0) {
			out.write("Out.color3 = Current.color3;");
			out.newLine();
		}
		if ((output & (1 << 5)) != 0) {
			out.write("Out.depth = Current.depth;");
			out.newLine();
		}
	}
	
	public static List<ShaderDataUniform> generateHLSL(BufferedWriter out, List<PixelShaderFragment> fragments) throws IOException {
		int input = 0;
		int output = 0;
		int samplers = 0;
		int numFragments = fragments.size();
		int[] texcoordCount = new int[numFragments];
		
		for (int i = 0; i < numFragments; ++i) {
			PixelShaderFragment frag = fragments.get(i);
			input |= frag.input;
			output |= frag.output;
			samplers += frag.numSamplers;
			texcoordCount[i] += frag.input >> TEXCOORD_SHL;
		}
		
		for (int i = 0; i < samplers; ++i) {
			out.write("extern uniform sampler Sampler" + i + " : register(s" + i + ");");
			out.newLine();
		}
		
		List<ShaderDataUniform> uniforms = ShaderFragment.generateDeclareCode(out, fragments);
		
		out.newLine();
		generateFragIn(out, input);
		generateFragCurrent(out, output);
		generateFragOut(out, output);
		
		out.write("cFragOut main( cFragIn In )"); out.newLine();
		out.write('{'); out.newLine();
		out.write("cFragCurrent Current;"); out.newLine();
		out.write("cFragOut Out;"); out.newLine();
		
		if ((input & 0x20) != 0) {
			out.write("Current.color = In.color;"); out.newLine();
		}
		
		int texcoordIndex = 0;
		int samplerIndex = 0;
		for (int i = 0; i < numFragments; ++i) {
			PixelShaderFragment frag = fragments.get(i);
			if (frag.mainCode != null && !frag.mainCode.isEmpty()) {
				out.newLine();
				ShaderFragment.replaceCodeIndices(out, frag.mainCode, texcoordIndex, samplerIndex);
			}
			texcoordIndex += texcoordCount[i];
			samplerIndex += frag.numSamplers;
		}
		
		generateOutputCopy(out, output);
		
		out.write("return Out;"); out.newLine();
		out.write('}'); out.newLine();
		
		return uniforms;
	}
}
