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
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class RWVertexDescription extends RWObject {
	
	public static final int TYPE_CODE = 0x20004;
	public static final int ALIGNMENT = 4;
	
	public int field_0;
	public int field_4;
	/** Not used in file, pointer to IDirect3DVertexDeclaration9 object. */
	public int dxVertexDeclaration;
	public final List<RWVertexElement> elements = new ArrayList<RWVertexElement>();
	public byte field_0E;
	public byte vertexSize;
	public int field_10;
	public int field_14;

	public RWVertexDescription(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		field_0 = stream.readLEInt();
		field_4 = stream.readLEInt();
		dxVertexDeclaration = stream.readLEInt();
		
		int count = stream.readLEShort();
		field_0E = stream.readByte();
		vertexSize = stream.readByte();
		field_10 = stream.readLEInt();
		field_14 = stream.readLEInt();
		
		for (int i = 0; i < count; i++) {
			RWVertexElement element = new RWVertexElement();
			element.read(stream);
			elements.add(element);
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(field_0);
		stream.writeLEInt(field_4);
		stream.writeLEInt(dxVertexDeclaration);
		stream.writeLEShort(elements.size());
		stream.writeByte(field_0E);
		stream.writeByte(vertexSize);
		stream.writeLEInt(field_10);
		stream.writeLEInt(field_14);
		
		for (RWVertexElement element : elements) {
			element.write(stream);
		}
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
	
	@Override
	public int getAlignment() {
		return ALIGNMENT;
	}

}
