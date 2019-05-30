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
package sporemodder.file.rw4;

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLMETHOD;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLTYPE;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLUSAGE;

public class RWVertexElement {
	
	public static final int VERTEX_POSITION = 0;
	public static final int VERTEX_NORMAL = 2;
	public static final int VERTEX_COLOR = 3;
	public static final int VERTEX_COLOR1 = 5;
	public static final int VERTEX_TEXCOORD0 = 6;
	public static final int VERTEX_TEXCOORD1 = 7;
	public static final int VERTEX_TEXCOORD2 = 8;
	public static final int VERTEX_TEXCOORD3 = 9;
	public static final int VERTEX_TEXCOORD4 = 10;
	public static final int VERTEX_TEXCOORD5 = 11;
	public static final int VERTEX_TEXCOORD6 = 12;
	public static final int VERTEX_TEXCOORD7 = 13;
	public static final int VERTEX_BLENDINDICES = 14;
	public static final int VERTEX_BLENDWEIGHTS = 15;
	public static final int VERTEX_POINTSIZE = 16;
	public static final int VERTEX_POSITION2 = 17;
	public static final int VERTEX_NORMAL2 = 18;
	public static final int VERTEX_TANGENT = 19;
	public static final int VERTEX_BINORMAL = 20;
	public static final int VERTEX_FOG = 21;
	public static final int VERTEX_BLENDINDICES2 = 22;
	public static final int VERTEX_BLENDWEIGHTS2 = 23;
	
	public static final int VERTEX2_POSITION = 0x1;
	public static final int VERTEX2_POSITION2 = 0x2;  // invented
	public static final int VERTEX2_TANGENT = 0x100;
	public static final int VERTEX2_COLOR = 0x1000;
	public static final int VERTEX2_COLOR1 = 0x2000;
	public static final int VERTEX2_TEXCOORD0 = 0x10000;
	public static final int VERTEX2_TEXCOORD1 = 0x20000;
	public static final int VERTEX2_TEXCOORD2 = 0x40000;
	public static final int VERTEX2_TEXCOORD3 = 0x80000;
	public static final int VERTEX2_TEXCOORD4 = 0x100000;
	public static final int VERTEX2_TEXCOORD5 = 0x200000;
	public static final int VERTEX2_TEXCOORD6 = 0x400000;
	public static final int VERTEX2_TEXCOORD7 = 0x800000;
	public static final int VERTEX2_NORMAL = 0x1000000;
	public static final int VERTEX2_NORMAL2 = 0x2000000;  // invented
	public static final int VERTEX2_BLENDINDICES = 0x10000000;
	public static final int VERTEX2_BLENDINDICES2 = 0x20000000;  // invented
	public static final int VERTEX2_BLENDWEIGHTS = 0x40000000;
	public static final int VERTEX2_BLENDWEIGHTS2 = 0x80000000;  // invented
	
	public static final ArgScriptEnum VertexInputEnum = new ArgScriptEnum();
	static {
		VertexInputEnum.add(0, "position");
		
		VertexInputEnum.add(2, "normal");
		VertexInputEnum.add(3, "color");
		
		VertexInputEnum.add(5, "color1");
		VertexInputEnum.add(6, "texcoord0");
		VertexInputEnum.add(7, "texcoord1");
		VertexInputEnum.add(8, "texcoord2");
		VertexInputEnum.add(9, "texcoord3");
		VertexInputEnum.add(10, "texcoord4");
		VertexInputEnum.add(11, "texcoord5");
		VertexInputEnum.add(12, "texcoord6");
		VertexInputEnum.add(13, "texcoord7");
		VertexInputEnum.add(14, "blendIndices");
		VertexInputEnum.add(15, "blendWeights");
		VertexInputEnum.add(16, "pointSize");
		VertexInputEnum.add(17, "position2");
		VertexInputEnum.add(18, "normal2");
		VertexInputEnum.add(19, "tangent");
		VertexInputEnum.add(20, "binormal");
		VertexInputEnum.add(21, "fog");
		VertexInputEnum.add(22, "blendIndices2");
		VertexInputEnum.add(23, "blendWeights2");
	}

	public static final String KEYWORD = "element";
	
	public int stream;
	/** The offset position of this element inside the vertex data. */
	public int offset;
	public D3DDECLTYPE type;
	public D3DDECLMETHOD method = D3DDECLMETHOD.D3DDECLMETHOD_DEFAULT;
	public D3DDECLUSAGE usage;
	public byte usageIndex;
	public int typeCode;
	
	public void read(StreamReader stream) throws IOException {
		this.stream = stream.readLEShort();
		offset = stream.readLEShort();
		type = D3DDECLTYPE.getById(stream.readByte());
		method = D3DDECLMETHOD.getById(stream.readByte());
		usage = D3DDECLUSAGE.getById(stream.readByte());
		usageIndex = stream.readByte();
		typeCode = stream.readLEInt();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEShort(this.stream);
		stream.writeLEShort(offset);
		stream.writeByte(type.id);
		stream.writeByte(method.id);
		stream.writeByte(usage.id);
		stream.writeByte(usageIndex);
		stream.writeLEInt(typeCode);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
//		writer.command(KEYWORD).arguments(type, usage, RWDECLUSAGE.getById(typeCode) == null ? typeCode : RWDECLUSAGE.getById(typeCode));
//		if (stream != 0) writer.option("stream").ints(stream);
//		if (usageIndex != 0) writer.option("usageIndex").ints(usageIndex);
//		if (method != D3DDECLMETHOD.D3DDECLMETHOD_DEFAULT) writer.option("method").arguments(method);
		
		writer.command(KEYWORD).arguments(VertexInputEnum.get(typeCode), type);
		if (stream != 0) writer.option("stream").ints(stream);
		if (method != D3DDECLMETHOD.D3DDECLMETHOD_DEFAULT) writer.option("method").arguments(method);
	}
	
	public int getFlags2() {
		switch (typeCode) {
		case VERTEX_POSITION:
			return VERTEX2_POSITION;
		case VERTEX_NORMAL:
			return VERTEX2_NORMAL;
		case VERTEX_COLOR:
			return VERTEX2_COLOR;
		case VERTEX_COLOR1:
			return VERTEX2_COLOR1;
		case VERTEX_TEXCOORD0:
			return VERTEX2_TEXCOORD0;
		case VERTEX_TEXCOORD1:
			return VERTEX2_TEXCOORD1;
		case VERTEX_TEXCOORD2:
			return VERTEX2_TEXCOORD2;
		case VERTEX_TEXCOORD3:
			return VERTEX2_TEXCOORD3;
		case VERTEX_TEXCOORD4:
			return VERTEX2_TEXCOORD4;
		case VERTEX_TEXCOORD5:
			return VERTEX2_TEXCOORD5;
		case VERTEX_TEXCOORD6:
			return VERTEX2_TEXCOORD6;
		case VERTEX_TEXCOORD7:
			return VERTEX2_TEXCOORD7;
		case VERTEX_BLENDINDICES:
			return VERTEX2_BLENDINDICES;
		case VERTEX_BLENDWEIGHTS:
			return VERTEX2_BLENDWEIGHTS;
		case VERTEX_POINTSIZE:
			return 0;
		case VERTEX_POSITION2:
			return VERTEX2_POSITION2;
		case VERTEX_NORMAL2:
			return VERTEX2_NORMAL2;
		case VERTEX_TANGENT:
			return VERTEX2_TANGENT;
		case VERTEX_BINORMAL:
		case VERTEX_FOG:
			return 0;
		case VERTEX_BLENDINDICES2:
			return VERTEX2_BLENDINDICES2;
		case VERTEX_BLENDWEIGHTS2:
			return VERTEX2_BLENDWEIGHTS2;
		default:
			return 0;
		}
	}
	
	public void setUsage(int elementUsage) {
		typeCode = elementUsage;
		switch (elementUsage) {
		case VERTEX_POSITION:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_POSITION;
			usageIndex = 0;
			break;
		case VERTEX_NORMAL:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_NORMAL;
			usageIndex = 0;
			break;
		case VERTEX_COLOR:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_COLOR;
			usageIndex = 0;
			break;
		case VERTEX_COLOR1:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_COLOR;
			usageIndex = 1;
			break;
		case VERTEX_TEXCOORD0:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 0;
			break;
		case VERTEX_TEXCOORD1:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 1;
			break;
		case VERTEX_TEXCOORD2:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 2;
			break;
		case VERTEX_TEXCOORD3:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 3;
			break;
		case VERTEX_TEXCOORD4:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 4;
			break;
		case VERTEX_TEXCOORD5:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 5;
			break;
		case VERTEX_TEXCOORD6:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 6;
			break;
		case VERTEX_TEXCOORD7:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TEXCOORD;
			usageIndex = 7;
			break;
		case VERTEX_BLENDINDICES:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_BLENDINDICES;
			usageIndex = 0;
			break;
		case VERTEX_BLENDWEIGHTS:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_BLENDWEIGHT;
			usageIndex = 0;
			break;
		case VERTEX_POINTSIZE:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_PSIZE;
			usageIndex = 0;
			break;
		case VERTEX_POSITION2:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_POSITION;
			usageIndex = 1;
			break;
		case VERTEX_NORMAL2:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_NORMAL;
			usageIndex = 1;
			break;
		case VERTEX_TANGENT:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_TANGENT;
			usageIndex = 0;
			break;
		case VERTEX_BINORMAL:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_BINORMAL;
			usageIndex = 0;
			break;
		case VERTEX_FOG:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_FOG;
			usageIndex = 0;
			break;
		case VERTEX_BLENDINDICES2:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_BLENDINDICES;
			usageIndex = 1;
			break;
		case VERTEX_BLENDWEIGHTS2:
			usage = D3DDECLUSAGE.D3DDECLUSAGE_BLENDWEIGHT;
			usageIndex = 1;
			break;
		}
	}
}
