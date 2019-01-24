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
import sporemodder.util.Vector3;

public class RWTriangleKDTreeProcedural extends RWObject {
	
	public static final int TYPE_CODE = 0x80003;
	public static final int ALIGNMENT = 16;
	
	public static class UnknownData {
		public int[] integers = new int[6];
		public float[] floats = new float[2];
	}
	
	public final RWBBox boundingBox;
	public int[][] triangles;
	public Vector3[] vertices;
	public int field_20 = 0x00D59208;
	public int field_24 = 8;
	public int field_2C = 0;
	public int[] triangleUnknowns;
	public final RWBBox boundingBox2;
	public final List<UnknownData> unknownData = new ArrayList<UnknownData>();

	public RWTriangleKDTreeProcedural(RenderWare renderWare) {
		super(renderWare);
		boundingBox = new RWBBox(renderWare);
		boundingBox2 = new RWBBox(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		boundingBox.read(stream);
		field_20 = stream.readLEInt();
		field_24 = stream.readLEInt();
		
		int triangleCount = stream.readLEInt();
		field_2C = stream.readLEInt();
		int vertexCount = stream.readLEInt();
		
		long pTriangles = stream.readLEUInt();
		long pVertices = stream.readLEUInt();
		long p4 = stream.readLEUInt();
		long p3 = stream.readLEUInt();
		
		stream.seek(pVertices);
		vertices = new Vector3[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			Vector3 vector = new Vector3();
			vector.readLE(stream);
			stream.skip(4);
			vertices[i] = vector;
		}
		
		stream.seek(pTriangles);
		// 4 because some triangles contain extra information?
		triangles = new int[triangleCount][4];
		for (int i = 0; i < triangleCount; i++) {
			int[] dst = new int[4];
			stream.readLEInts(dst);
			triangles[i] = dst;
		}
		
		stream.seek(p3);
		triangleUnknowns = new int[triangleCount];
		int x = 0;
		for (int i = 0; i < triangleCount; i++) {
			if ((i & 7) == 0) {
				x = stream.readLEInt();
			}
			
			triangleUnknowns[i] = (x >> ((i & 7) * 4)) & 0xf;
		}
		
		stream.seek(p4);
		// vertexPos - 8 * 4
		stream.skip(4);
		int unknownCount = stream.readLEInt();
		// triangleCount, 0
		stream.skip(8);
		boundingBox2.read(stream);
		
		for (int i = 0; i < unknownCount; i++) {
			UnknownData data = new UnknownData();
			stream.readLEInts(data.integers);
			stream.readLEFloats(data.floats);
			unknownData.add(data);
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		boundingBox.write(stream);
		stream.writeLEInt(field_20);
		stream.writeLEInt(field_24);
		stream.writeLEInt(triangles.length);
		stream.writeLEInt(field_2C);
		stream.writeLEInt(vertices.length);
		
		// We will write the offsets later
		long pointersOffset = stream.getFilePointer();
		long pTriangles = 0;
		long pVertices = 0;
		long p4 = 0;
		long p3 = 0;
		stream.writePadding(4*4);
		
		long offset = stream.getFilePointer();
		pVertices = (offset + 15) & ~15;
		stream.writePadding((int) (pVertices - offset));
		
		for (Vector3 vertex : vertices) {
			vertex.writeLE(stream);
			stream.writePadding(4);
		}
		
		// Each vertex is 16 bytes long, so we are still 16-byte aligned
		pTriangles = stream.getFilePointer();
		
		for (int[] triangle : triangles) {
			stream.writeLEInts(triangle);
		}
		
		// Each triangle is 16 bytes long, so we are still 16-byte aligned
		p3 = stream.getFilePointer();
		int count = triangleUnknowns.length / 8;
		for (int i = 0; i < count; i++) {
			stream.writeUByte(triangleUnknowns[i*8+0] | (triangleUnknowns[i*8+1] << 4));
			stream.writeUByte(triangleUnknowns[i*8+2] | (triangleUnknowns[i*8+3] << 4));
			stream.writeUByte(triangleUnknowns[i*8+4] | (triangleUnknowns[i*8+5] << 4));
			stream.writeUByte(triangleUnknowns[i*8+6] | (triangleUnknowns[i*8+7] << 4));
		}
		
		int triPack = triangleUnknowns.length % 8;
		if (triPack > 0) {
			int pack = 0;
			for (int i = 0; i < triPack; i++) {
				pack |= triangleUnknowns[count*8 + i] << (i*4);
			}
			for (int i = 0; i < 8-triPack; i++) {
				pack |= 15 << ((i+triPack)*4);
			}
			stream.writeLEInt(pack);
		}
		
		
		offset = stream.getFilePointer();
		p4 = (offset + 15) & ~15;
		stream.writePadding((int) (p4 - offset));
		
		stream.writeLEInt((int) (pVertices - 8*4));
		stream.writeLEInt(unknownData.size());
		stream.writeLEInt(triangles.length);
		stream.writePadding(4);
		
		boundingBox2.write(stream);
		
		for (UnknownData data : unknownData) {
			stream.writeLEInts(data.integers);
			stream.writeLEFloats(data.floats);
		}
		
		
		long endOffset = stream.getFilePointer();
		
		stream.seek(pointersOffset);
		stream.writeLEUInt(pTriangles);
		stream.writeLEUInt(pVertices);
		stream.writeLEUInt(p4);
		stream.writeLEUInt(p3);
		
		stream.seek(endOffset);
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
