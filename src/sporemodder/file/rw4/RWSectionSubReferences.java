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

public class RWSectionSubReferences extends RWObject {
	
	public static final int TYPE_CODE = 0x10007;
	
	/**
	 * A pair used to declare sub-objects.
	 */
	public static class SubReference {
		/** The parent object which contains the subreference. */
		public RWObject object;
		/** The offset within the object where the subreferenced object starts. */
		public int offset;
		
		public SubReference(RWObject object, int offset) {
			super();
			this.object = object;
			this.offset = offset;
		}
	}
	
	public final List<SubReference> references = new ArrayList<SubReference>();
	public int field_4;
	public int field_8;
	/** The subreference data is written in a special position, so it must be specified beforehand. */
	public long offset;
	
	// Only used when reading
	private int count;

	public RWSectionSubReferences(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		count = stream.readLEInt();
		field_4 = stream.readLEInt();
		field_8 = stream.readLEInt();
		
		// The end offset of the objects
		stream.readLEInt();
		
		offset = stream.readLEUInt();
		
		// The count again
		stream.readLEInt();

	}
	
	public void readReferences(StreamReader stream) throws IOException {
		// So that we can continue reading from the same position
		long oldOffset = stream.getFilePointer();
		
		stream.seek(offset);
		for (int i = 0; i < count; i++) {
			references.add(new SubReference(renderWare.get(stream.readLEInt()), stream.readLEInt()));
		}
		
		stream.seek(oldOffset);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(references.size());
		stream.writeLEInt(field_4);
		stream.writeLEInt(field_8);
		stream.writeLEUInt(offset + references.size()*8);
		stream.writeLEUInt(offset);
		stream.writeLEInt(references.size());
		
		if (offset != 0) {
			// So that we can continue writing from the same position
			long oldOffset = stream.getFilePointer();
			
			stream.seek(offset);
			
			for (SubReference r : references) {
				stream.writeLEInt(renderWare.indexOf(r.object));
				stream.writeLEInt(r.offset);
			}
			
			stream.seek(oldOffset);
		}
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

}
