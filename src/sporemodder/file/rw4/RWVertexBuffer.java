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

public class RWVertexBuffer extends RWObject {
	
	public static final int TYPE_CODE = 0x20005;
	public static final int ALIGNMENT = 4;
	
	public RWVertexDescription vertexDescription;
	public int field_4;
	public int baseVertexIndex;
	public int vertexCount; 
	public int field_10;
	public int vertexSize;
	public RWBaseResource vertexData;

	public RWVertexBuffer(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		vertexDescription = (RWVertexDescription) renderWare.get(stream.readLEInt());
		field_4 = stream.readLEInt();
		baseVertexIndex = stream.readLEInt();
		vertexCount = stream.readLEInt();
		field_10 = stream.readLEInt();
		vertexSize = stream.readLEInt();
		vertexData = (RWBaseResource) renderWare.get(stream.readLEInt());
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(renderWare.indexOf(vertexDescription));
		stream.writeLEInt(field_4);
		stream.writeLEInt(baseVertexIndex);
		stream.writeLEInt(vertexCount);
		stream.writeLEInt(field_10);
		stream.writeLEInt(vertexSize);
		stream.writeLEInt(renderWare.indexOf(vertexData));
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
