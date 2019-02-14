package sporemodder.file.shaders;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;

public class ShaderFragment {
	public static class ShaderVariable {
		public String name;
		/* 10h */	public short dataIndex;
		/* 12h */	public short field_12;
		/* 14h */	public short registerSize;
		/* 16h */	public short field_16;  // always 0
		/* 18h */	public int flags;
		
		public void read(StreamReader in) throws IOException {
			name = in.readString(StringEncoding.ASCII, in.readInt());
			dataIndex = in.readShort();  // dataIndex again
			field_12 = in.readShort();
			registerSize = in.readShort();
			field_16 = in.readShort();
			flags = in.readInt();
		}
		
		public void writeHLSL(BufferedWriter out, int startRegister) throws IOException {
			out.write("extern uniform " + name + " : register(c" + startRegister + ");");
			/* Only for testing */ 
			out.write("  // 0x" + Integer.toHexString(dataIndex) + " 0x" + Integer.toHexString(field_12) + " 0x" + Integer.toHexString(flags));
		}
	}
	
	public static final int FLAG_DEFINED = 1;
	
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
	
	public String mainCode;
	public String declareCode;
	public final List<ShaderVariable> variables = new ArrayList<>();
	
	public int input;
	public int output;
	public byte numRegisters;
	public byte numOutputTexcoords;  // only for vertex shader
	public byte type;  // only for vertex shader, 4, sometimes 3?
	public int flags;  // -18h
	
	public String shaderName;
	
	public void read(StreamReader in) throws IOException {
		
		input = in.readInt();
		output = in.readInt();
		numOutputTexcoords = in.readByte();
		type = in.readByte();
		numRegisters = in.readByte();
		flags = in.readInt();
		
		mainCode = in.readString(StringEncoding.ASCII, in.readInt());
		declareCode = in.readString(StringEncoding.ASCII, in.readInt());
		
		int variableCount = in.readInt();
		
		for (int i = 0; i < variableCount; i++) {
			ShaderVariable variable = new ShaderVariable();
			variable.read(in);
			variables.add(variable);
		}
		
		if ((flags & 0x2) != 0) {
			shaderName = in.readString(StringEncoding.ASCII, in.readInt());
		}
	}
	
	public void writeHLSL(BufferedWriter out) throws IOException {
		
		if (type == 0) {
//			out.write("// input: 0x" + Integer.toHexString(input)); out.newLine();
//			out.write("// output: 0x" + Integer.toHexString(output)); out.newLine();
//			out.write("// numRegisters: 0x" + Integer.toHexString(numRegisters)); out.newLine();
//			out.write("// numOutputTexcoords: 0x" + Integer.toHexString(numOutputTexcoords)); out.newLine();
			//writePSInputStruct(out);  //TODO
			out.newLine();
			writePSOutputStruct(out, "cFragCurrent");
			out.newLine();
			writePSOutputStruct(out, "cFragOut");
			out.newLine();
		} else {
			writeVSInputStruct(out);
			out.newLine();
			writeVSOutputStruct(out);
			out.newLine();
		}
		
		int startRegister = 0;
		for (ShaderVariable var : variables) {
			var.writeHLSL(out, startRegister);
			out.newLine();
			
			startRegister += var.registerSize;
		}
		
		out.newLine();
		
		if (declareCode != null && declareCode.length() > 0) {
			out.write(declareCode);
			out.newLine();
			out.newLine();
		}
		
		if (type == 0) {
			out.write("cFragOut main( cFragIn In )");
			out.newLine();
			out.write("{");
			out.newLine();
			out.write("cFragCurrent Current;");
			out.newLine();
			out.write("cFragOut Out;");
			out.newLine();
			out.newLine();
			//writePSInput(out);
		} else {
			out.write("cVertOut main( cVertIn In )");
			out.newLine();
			out.write("{");
			out.newLine();
			out.write("cVertCurrent Current;");
			out.newLine();
			out.write("cVertOut Out;");
			out.newLine();
			out.newLine();
			writeVSInput(out);
		}
		
		out.newLine();
		
		out.write(mainCode);
		
		out.newLine();
		if (type == 0) {
			writePSOutput(out);
		} else {
			writeVSOutput(out);
		}
		out.write("return Out;");
		out.newLine();
		out.write("}");
		out.newLine();
	}
	
	private void writeVSInputStruct(BufferedWriter out) throws IOException {
		out.write("struct cVertCurrent");
		out.newLine();
		out.write("{");
		out.newLine();
		out.write("float4 position;");
		out.newLine();
		if ((input & VS_INPUT_NORMAL) != 0) {
			out.write("float4 normal;");
			out.newLine();
		}
		if ((input & (10|VS_INPUT_COLOR)) != 0) {
			out.write("float4 color;");
			out.newLine();
		}
		for (int i = 0; i < 8; ++i) {
			if ((input & (1 << (VS_INPUT_SHL+i))) != 0) {
				out.write("float2 texcoord" + i + ";");
				out.newLine();
			}
		}
		if ((input & VS_INPUT_INDICES) != 0) {
			out.write("int4 indices;");
			out.newLine();
		}
		if ((input & VS_INPUT_WEIGHTS) != 0) {
			out.write("float4 weights;");
			out.newLine();
		}
		if ((input & VS_INPUT_POSITION2) != 0) {
			out.write("float4 position2;");
			out.newLine();
		}
		if ((input & VS_INPUT_NORMAL2) != 0) {
			out.write("float4 normal2;");
			out.newLine();
		}
		if ((input & VS_INPUT_INDICES2) != 0) {
			out.write("int4 indices2;");
			out.newLine();
		}
		if ((input & VS_INPUT_WEIGHTS2) != 0) {
			out.write("float4 weights2;");
			out.newLine();
		}
		if ((input & VS_INPUT_TANGENT) != 0) {
			out.write("float4 tangent;");
			out.newLine();
		}
		if ((input & VS_INPUT_BINORMAL) != 0) {
			out.write("float4 binormal;");
			out.newLine();
		}
		if ((input & VS_INPUT_COLOR1) != 0) {
			out.write("float4 color1;");
			out.newLine();
		}
		if ((input & VS_INPUT_FOG) != 0) {
			out.write("float fog;");
			out.newLine();
		}
		if ((input & VS_INPUT_POINTSIZE) != 0) {
			out.write("float pointSize;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private void writeVSInput(BufferedWriter out) throws IOException {
		out.write("Current.position = In.position;");
		out.newLine();
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
	}
	
	private void writeVSOutputStruct(BufferedWriter out) throws IOException {
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
			out.write("float2 texcoord" + i + " : TEXCOORD" + i + ";");
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
	
	private void writeVSOutput(BufferedWriter out) throws IOException {
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
	
	private void writePSOutputStruct(BufferedWriter out, String name) throws IOException {
		out.write("struct " + name);
		out.newLine();
		out.write("{");
		out.newLine();
		if ((output & PS_OUTPUT_COLOR) != 0) {
			out.write("float4 color : COLOR0;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_COLOR1) != 0) {
			out.write("float4 color1 : COLOR1;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_COLOR2) != 0) {
			out.write("float4 color2 : COLOR2;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_COLOR3) != 0) {
			out.write("float4 color3 : COLOR3;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_DEPTH) != 0) {
			out.write("float depth : DEPTH;");
			out.newLine();
		}
		out.write("};");
		out.newLine();
	}
	
	private void writePSOutput(BufferedWriter out) throws IOException {
		if ((output & PS_OUTPUT_COLOR) != 0) {
			out.write("Out.color = Current.color;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_COLOR1) != 0) {
			out.write("Out.color1 = Current.color1;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_COLOR2) != 0) {
			out.write("Out.color2 = Current.color2;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_COLOR3) != 0) {
			out.write("Out.color3 = Current.color3;");
			out.newLine();
		}
		if ((output & PS_OUTPUT_DEPTH) != 0) {
			out.write("Out.depth = Current.depth;");
			out.newLine();
		}
	}
	
	public String getName() {
		return shaderName;
	}
}
