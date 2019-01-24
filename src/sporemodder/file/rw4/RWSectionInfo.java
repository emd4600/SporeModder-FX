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

public class RWSectionInfo {
	
	/** A "pointer" to the data, that is, the offset within the file to the data of the section. */
	public long pData;
	public int field_04;
	/** The size, of the section data, in bytes. */
	public int size;
	/** The required alignment for the section data. Padding bytes will be added before the data so that the offset can be divided by this number .*/
	public int alignment;
	/** The index to the type code in the header list. */
	public int typeCodeIndex;
	/** A 32-bit code used to identify the type of section. */
	public int typeCode;
	
	public void read(StreamReader stream) throws IOException {
		pData = stream.readLEUInt();
		field_04 = stream.readLEInt();
		size = stream.readLEInt();
		alignment = stream.readLEInt();
		typeCodeIndex = stream.readLEInt();
		typeCode = stream.readLEInt();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEUInt(pData);
		stream.writeLEInt(field_04);
		stream.writeLEInt(size);
		stream.writeLEInt(alignment);
		stream.writeLEInt(typeCodeIndex);
		stream.writeLEInt(typeCode);
	}
}
