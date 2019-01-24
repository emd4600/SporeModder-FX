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

public class RWSectionManifest extends RWObject {
	
	public static final int TYPE_CODE = 0x10004;
	
	public int field_0;
	public int field_4;
	public final RWSectionTypes types;
	public final RWSectionExternalArenas externalArenas;
	public final RWSectionSubReferences subReferences;
	public final RWSectionAtoms atoms;

	public RWSectionManifest(RenderWare renderWare) {
		super(renderWare);
		
		types = new RWSectionTypes(renderWare);
		externalArenas = new RWSectionExternalArenas(renderWare);
		subReferences = new RWSectionSubReferences(renderWare);
		atoms = new RWSectionAtoms(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		
		stream.readLEInt();  // type code
		
		field_0 = stream.readLEInt();
		field_4 = stream.readLEInt();
		
		int offset1 = stream.readLEInt();
		int offset2 = stream.readLEInt();
		int offset3 = stream.readLEInt();
		int offset4 = stream.readLEInt();
		
		stream.seek(baseOffset + offset1);
		stream.readLEInt();  // type code
		types.read(stream);
		
		stream.seek(baseOffset + offset2);
		stream.readLEInt();  // type code
		externalArenas.read(stream);
		
		stream.seek(baseOffset + offset3);
		stream.readLEInt();  // type code
		subReferences.read(stream);
		
		stream.seek(baseOffset + offset4);
		stream.readLEInt();  // type code
		atoms.read(stream);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		
		// We will have to rewrite it anyways, so just write padding
		stream.writePadding(28);
		
		long offset1 = stream.getFilePointer() - baseOffset;
		stream.writeLEInt(types.getTypeCode());
		types.write(stream);
		
		long offset2 = stream.getFilePointer() - baseOffset;
		stream.writeLEInt(externalArenas.getTypeCode());
		externalArenas.write(stream);
		
		long offset3 = stream.getFilePointer() - baseOffset;
		stream.writeLEInt(subReferences.getTypeCode());
		subReferences.write(stream);
		
		long offset4 = stream.getFilePointer() - baseOffset;
		stream.writeLEInt(atoms.getTypeCode());
		atoms.write(stream);
		
		long endOffset = stream.getFilePointer();
		
		stream.seek(baseOffset);
		stream.writeLEInt(TYPE_CODE);
		stream.writeLEInt(field_0);
		stream.writeLEInt(field_4);
		stream.writeLEUInt(offset1);
		stream.writeLEUInt(offset2);
		stream.writeLEUInt(offset3);
		stream.writeLEUInt(offset4);
		
		stream.seek(endOffset);
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
}
