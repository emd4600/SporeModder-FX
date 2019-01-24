/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

package sporemodder.file.dbpf;

import java.io.IOException;

import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.ResourceKey;

public class DBPFItem {

	/** Whether the data represented by this item is compressed or not. */
	public boolean isCompressed;
	/** The position where the data of this item is stored. */
	public long chunkOffset;
	/** The real amount of bytes used by the data on this item when used on memory; that is, once uncompressed. */
	public int memSize;
	/** The amount of bytes used by the data in the .package file; that is, while compressed. */
	public int compressedSize;
	/** The resource key that can be used in the game to access this item. */
	public final ResourceKey name = new ResourceKey();
	/** This is used in memory by the game, but it is irrelevant in .package files; always true. */
	public boolean isSaved = true;
	
	
	public void read(StreamReader stream, boolean isDBBF, boolean readType, boolean readGroup) throws IOException {
		
		if (readType) {
			name.setTypeID(stream.readLEInt());
		}
		if (readGroup) {
			name.setGroupID(stream.readLEInt());
		}
		name.setInstanceID(stream.readLEInt());
		
		chunkOffset = isDBBF ? stream.readLELong() : stream.readLEUInt();
		
		compressedSize = stream.readLEInt() & 0x7FFFFFFF;
		memSize = stream.readLEInt();
		
		switch(stream.readLEShort()) {
			case 0: isCompressed = false;
					break;
			case -1: isCompressed = true;
					break;
			default: throw new IOException("Unknown compression label on position " + stream.getFilePointer());
		}
		
		isSaved = stream.readBoolean();
		// Padding
		stream.skip(1);
	}
	
	public void write(StreamWriter stream, boolean isDBBF, boolean writeType, boolean writeGroup) throws IOException {
		if (writeType) {
			stream.writeLEInt(name.getTypeID());
		}
		if (writeGroup) {
			stream.writeLEInt(name.getGroupID());
		}
		stream.writeLEInt(name.getInstanceID());
		
		if (isDBBF) {
			stream.writeLELong(chunkOffset);
		}
		else {
			stream.writeLEUInt(chunkOffset);
		}
		
		stream.writeLEInt(compressedSize | 0x80000000);
		stream.writeLEInt(memSize);
		stream.writeLEShort(isCompressed ? 0xFFFF : 0);
		stream.writeBoolean(isSaved);
		stream.writePadding(1);
	}
	
	public MemoryStream processFile(StreamReader in) throws IOException {
		in.seek(chunkOffset);
		
		if (isCompressed) {
			byte[] arr = new byte[compressedSize];
			in.read(arr);
			
			byte[] out = new byte[memSize];
			RefPackCompression.decompressFast(arr, out);
			
			return new MemoryStream(out);
		}
		else {
			byte[] arr = new byte[memSize];
			in.read(arr);
			return new MemoryStream(arr);
		}
	}
}
