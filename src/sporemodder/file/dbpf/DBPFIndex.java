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
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class DBPFIndex {
	
	/** Certain index types can use a single group ID (folder name) for all its files. If that is not used, this value is -1. */
	public int groupID = -1;
	/** Certain index types can use a single type ID (extension) for all its files. If that is not used, this value is -1. */
	public int typeID = -1;
	/** A list with all the items in the index. This only stores the items metadata such as the size, compression, etc, but not the data itself. */
	public final List<DBPFItem> items = new ArrayList<DBPFItem>();
	
	/** The position where the items metadata is stored. Only used for reading. */
	private long itemsOffset;

	/**
	 * Reads the parameters of this index; this does not include the DBPFItems that it contains.
	 * @param stream
	 * @throws IOException
	 */
	public void read(StreamReader stream) throws IOException {
		int typeFlags = stream.readLEInt();
		
		// type id
		if ((typeFlags & (1 << 0)) == 1 << 0)
		{
			typeID = stream.readLEInt();
		}

		// group id
		if ((typeFlags & (1 << 1)) == 1 << 1)
		{
			groupID = stream.readLEInt();
		}

		// unknown value
		if ((typeFlags & (1 << 2)) == 1 << 2)
		{
			stream.readLEInt();
		}
		
		itemsOffset = stream.getFilePointer();
	}
	
	/**
	 * Writes the parameters of this index; this does not include the DBPFItems that it contains.
	 * @param stream
	 * @throws IOException
	 */
	public void write(StreamWriter stream) throws IOException {
		int typeFlags = 0;
		
		// The unknown value, which is generally used
		typeFlags |= 1 << 2;
		
		if (typeID != -1) {
			typeFlags |= 1 << 0;
		}
		if (groupID != -1) {
			typeFlags |= 1 << 1;
		}
		
		stream.writeLEInt(typeFlags);
		
		if (typeID != -1) {
			stream.writeLEInt(typeID);
		}
		if (groupID != -1) {
			stream.writeLEInt(typeID);
		}
		
		// The unknown value
		stream.writeLEInt(0);
	}
	
	public void readItems(StreamReader stream, int numItems, boolean isDBBF) throws IOException {
		stream.seek(itemsOffset);
		
		boolean readGroup = groupID == -1;
		boolean readType = typeID == -1;
		
		for (int i = 0; i < numItems; i++) {
			
			DBPFItem item = new DBPFItem();
			
			if (!readGroup) {
				item.name.setGroupID(groupID);
			}
			if (!readType) {
				item.name.setTypeID(typeID);
			}
			item.read(stream, isDBBF, readType, readGroup);
			
			items.add(item);
		}
	}
	
	public void writeItems(StreamWriter stream, boolean isDBBF) throws IOException {
		boolean writeGroup = groupID == -1;
		boolean writeType = typeID == -1;
		
		for (DBPFItem item : items) {
			
			item.write(stream, isDBBF, writeType, writeGroup);
			
		}
	}
}
