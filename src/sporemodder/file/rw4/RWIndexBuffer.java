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
import sporemodder.file.rw4.Direct3DEnums.D3DFORMAT;
import sporemodder.file.rw4.Direct3DEnums.D3DPRIMITIVETYPE;

public class RWIndexBuffer extends RWObject {

	public static final int TYPE_CODE = 0x20007;
	public static final int ALIGNMENT = 4;
	
	/** Not used in the file, the pointer to the IDirect3DIndexBuffer9 object. */
	public int dxIndexBuffer;
	/** This quantity is added to every index of the stream */
	public int startIndex;
	public int primitiveCount;
	/** A combination of the D3DUSAGE_ flags; usually D3DUSAGE_WRITEONLY. */
	public int usage = Direct3DEnums.D3DUSAGE_WRITEONLY;
	/** The format used in the index data, either D3DFMT_INDEX16 or D3DFMT_INDEX32; apparently 
	 * Spore only supports the first one. */
	public D3DFORMAT format = D3DFORMAT.D3DFMT_INDEX16;
	/** The type of primitive polygons created with the indices, usually D3DPT_TRIANGLELIST. */
	public D3DPRIMITIVETYPE primitiveType =  D3DPRIMITIVETYPE.D3DPT_TRIANGLELIST;
	public RWBaseResource indexData;
	
	public RWIndexBuffer(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		dxIndexBuffer = stream.readLEInt();
		startIndex = stream.readLEInt();	
		primitiveCount = stream.readLEInt();
		usage = stream.readLEInt();
		format = D3DFORMAT.getById(stream.readLEInt());
		primitiveType = D3DPRIMITIVETYPE.getById(stream.readLEInt());
		indexData = (RWBaseResource) renderWare.get(stream.readLEInt());
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(dxIndexBuffer);
		stream.writeLEInt(startIndex);
		stream.writeLEInt(primitiveCount);
		stream.writeLEInt(usage);
		stream.writeLEInt(format.getId());
		stream.writeLEInt(primitiveType.getId());
		stream.writeLEInt(renderWare.indexOf(indexData));
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
