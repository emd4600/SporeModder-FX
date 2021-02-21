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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.rw4.Direct3DEnums.D3DPRIMITIVETYPE;

public class RWMesh extends RWObject {
	
	public static final int TYPE_CODE = 0x20009;
	public static final int ALIGNMENT = 4;
	
	public int field_0;
	/** The type of primitive polygons created with the indices, usually D3DPT_TRIANGLELIST. */
	public D3DPRIMITIVETYPE primitiveType =  D3DPRIMITIVETYPE.D3DPT_TRIANGLELIST;
	public RWIndexBuffer indexBuffer;
	public int triangleCount;
	public int firstIndex;
	public int primitiveCount;
	public int firstVertex;
	public int vertexCount;
	public final List<RWVertexBuffer> vertexBuffers = new ArrayList<RWVertexBuffer>();

	public RWMesh(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		field_0 = stream.readLEInt();
		primitiveType = D3DPRIMITIVETYPE.getById(stream.readLEInt());
		indexBuffer = (RWIndexBuffer) renderWare.get(stream.readLEInt());
		triangleCount = stream.readLEInt();
		int buffersCount = stream.readLEInt();
		firstIndex = stream.readLEInt();
		primitiveCount = stream.readLEInt();
		firstVertex = stream.readLEInt();
		vertexCount = stream.readLEInt();
		
		for (int i = 0; i < buffersCount; i++) {
			//TODO sometimes it refers to shape mesh things?
			vertexBuffers.add((RWVertexBuffer) renderWare.get(stream.readLEInt()));
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(field_0);
		stream.writeLEInt(primitiveType.getId());
		stream.writeLEInt(renderWare.indexOf(indexBuffer));
		stream.writeLEInt(triangleCount);
		stream.writeLEInt(vertexBuffers.size());
		stream.writeLEInt(firstIndex);
		stream.writeLEInt(primitiveCount);
		stream.writeLEInt(firstVertex);
		stream.writeLEInt(vertexCount);
		
		for (RWVertexBuffer buffer : vertexBuffers) {
			stream.writeLEInt(renderWare.indexOf(buffer));
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
