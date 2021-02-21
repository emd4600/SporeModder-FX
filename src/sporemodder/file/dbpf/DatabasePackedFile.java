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
package sporemodder.file.dbpf;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.RefPackCompression.CompressorOutput;

public class DatabasePackedFile {
	
	private static final int TYPE_DBPF = 0x46504244;
	private static final int TYPE_DBBF = 0x46424244;
	
	public int majorVersion = 3;
	public int minVersion = 0;
	public int indexMajorVersion = 0; 
	public int indexMinorVersion = 3;
	/** Whether this is a big DBPF (and therefore supports 64-bit offsets) or not. */
	public boolean isDBBF;
	/** The index that contains information about all the files in the package. */
	public final DBPFIndex index = new DBPFIndex();
	
	/** The number of items in the index. */
	public int indexCount;
	/** The position in the file where the index is stored. */ 
	public long indexOffset;
	/** The amount of bytes used by the DBPFIndex. */
	public long indexSize;
	
	private void readDBPF(StreamReader stream) throws IOException {
		majorVersion = stream.readLEInt();
		minVersion = stream.readLEInt();
		
		stream.skip(20);
		indexMajorVersion = stream.readLEInt();
		indexCount = stream.readLEInt();
		stream.skip(4);
		indexSize = stream.readLEUInt();
		stream.skip(12);
		indexMinorVersion = stream.readLEInt();
		indexOffset = stream.readLEUInt();
	}
	
	private void writeDBPF(StreamWriter stream) throws IOException {
		stream.writeLEInt(majorVersion);
		stream.writeLEInt(minVersion);
		stream.writePadding(20); // ?
		stream.writeLEInt(indexMajorVersion);
		stream.writeLEInt(indexCount);
		stream.writePadding(4);
		stream.writeLEUInt(indexSize);
		stream.writePadding(12);
		stream.writeLEInt(indexMinorVersion);
		stream.writeLEUInt(indexOffset);
		stream.writePadding(28);
	}
	
	private void readDBBF(StreamReader in) throws IOException {
		majorVersion = in.readLEInt();
		minVersion = in.readLEInt();
		
		in.skip(20);
		indexMajorVersion = in.readLEInt();
		indexCount = in.readLEInt();
		indexSize = in.readLEInt();
		in.skip(8);
		indexMinorVersion = in.readLEInt();
		indexOffset = in.readLEInt();
	}
	
	private void writeDBBF(StreamWriter stream) throws IOException {
		stream.writeLEInt(majorVersion);
		stream.writeLEInt(minVersion);
		stream.writePadding(20);
		stream.writeLEInt(indexMajorVersion);
		stream.writeLEInt(indexCount);
		stream.writeLELong(indexSize);
		stream.writePadding(4);
		stream.writeLEInt(indexMinorVersion);
		stream.writeLELong(indexOffset);
		stream.writePadding(56);
	}
	
	
	public void readHeader(StreamReader stream) throws IOException {
		int magic = stream.readLEInt();
		if (magic == TYPE_DBPF) {
			isDBBF = false;
			readDBPF(stream);
		} 
		else if (magic == TYPE_DBBF) {
			isDBBF = true;
			readDBBF(stream);
		} 
		else {
			throw new IOException("Unrecognised DBPF type magic: " + HashManager.get().hexToString(magic));
		}
	}
	
	public void writeHeader(StreamWriter stream) throws IOException {
		if (isDBBF) {
			stream.writeLEInt(TYPE_DBBF);
			writeDBBF(stream);
		} 
		else {
			stream.writeLEInt(TYPE_DBPF);
			writeDBPF(stream);
		}
	}
	
	public void writeFile(StreamWriter stream, DBPFItem item, byte[] data, boolean compress) throws IOException {
		
		item.chunkOffset = stream.getFilePointer();
		
		if (compress) {
			CompressorOutput compressOut = new CompressorOutput();
			RefPackCompression.compress(data, data.length, compressOut);

			stream.write(compressOut.data, 0, compressOut.lengthInBytes);
			item.isCompressed = true;
			item.memSize = data.length;
			item.compressedSize = compressOut.lengthInBytes;
		}
		else {
			stream.write(data, 0, data.length);
			item.isCompressed = false;
			item.memSize = data.length;
			item.compressedSize = item.memSize;
		}
		
		index.items.add(item);
		indexCount++;
	}
	
	public void readIndex(StreamReader stream) throws IOException {
		stream.seek(indexOffset);
		index.read(stream);
	}
	
	public void writeIndex(StreamWriter stream) throws IOException {
		indexOffset = stream.getFilePointer();
		
		long baseOffset = stream.getFilePointer();
		index.write(stream);
		index.writeItems(stream, isDBBF);
		
		indexSize = stream.getFilePointer() - baseOffset;
	}
	
	public void read(StreamReader stream) throws IOException {
		readHeader(stream);
		readIndex(stream);
		index.readItems(stream, indexCount, isDBBF);
	}
	
	public DBPFItem getItem(ResourceKey key) {
		for (DBPFItem item : index.items) {
			if (item.name.equals(key)) {
				return item;
			}
		}
		return null;
	}
	
	public void print() {
		System.out.println((isDBBF ? "DBBF" : "DBPF") + " v" + majorVersion + "." + minVersion);
		System.out.println("Num entries: " + indexCount);
		System.out.println("Index size: " + indexSize);
		System.out.println("Index version: " + indexMajorVersion + "." + indexMinorVersion);
		System.out.println("Index offset: " + indexOffset);
	}
	
}
