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
package sporemodder.file.lvl;

import java.io.IOException;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Vector3;
import sporemodder.util.Vector4;

public class GameplayMarker {
	
	public static final int DATA_SIZE = 0x88;

	public final Vector3 offset = new Vector3();
	public final Vector4 orientation = new Vector4();  // quaternion
	// Such as CreatureArchetype, 0x1BE418E (herd), avatar
	public int type;
	public int id;
	public int definitionID;
	public GameplayMarkerData data;
	public long pos;
	
	public void read(StreamReader stream, int version, int dataSize) throws IOException {
		offset.readBE(stream);
		orientation.readBE(stream);
		type = stream.readInt();
		id = stream.readInt();
		definitionID = stream.readInt();
		
		if (version == 2) {
			stream.readBoolean();
			int count = stream.readInt();
			stream.skip(4 * count);
		}
		
		pos = stream.getFilePointer();
		byte[] rawData = new byte[dataSize];
		stream.read(rawData);
		
		data = createData();
		if (data != null) {
			try (MemoryStream dataStream = new MemoryStream(rawData)) {
				data.group = dataStream.readLEInt();
				data.propertyCount = dataStream.readLEInt();
				data.read(dataStream);
			}
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		offset.writeBE(stream);
		orientation.writeBE(stream);
		stream.writeInt(type);
		stream.writeInt(id);
		stream.writeInt(definitionID);
		
		try (MemoryStream dataStream = new MemoryStream(DATA_SIZE)) {
			if (data != null) {
				dataStream.writeLEInt(data.group);
				dataStream.writeLEInt(data.propertyCount);
				data.write(dataStream);
			}
			
			dataStream.writePadding((int)(DATA_SIZE - dataStream.getFilePointer()));
			
			stream.write(dataStream.getRawData());
		}
	}
	
	public GameplayMarkerData createData() {
		switch (type) {
		case MigrationPointData.TYPE: return new MigrationPointData(this);
		case CreatureArchetypeData.TYPE: return new CreatureArchetypeData(this);
		default: return null;
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("marker").arguments(HashManager.get().getFileName(type));
		if (id != 0 || definitionID != 0) writer.option("id").arguments(HashManager.get().getFileName(id), HashManager.get().getFileName(definitionID));
		writer.startBlock();
		
		writer.command("position").vector3(offset);
		writer.command("orientation").vector4(orientation);
		
		if (data != null) {
			writer.blankLine();
			
			if (data.group != 0) writer.command("group").arguments(HashManager.get().formatInt32(data.group));
			if (data.propertyCount != 0) writer.command("propertyCount").ints(data.propertyCount);
			data.toArgScript(writer);
		}
		
		writer.endBlock().commandEND();
	}
}
