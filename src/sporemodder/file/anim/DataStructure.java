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
package sporemodder.file.anim;

import java.io.IOException;

import emord.filestructures.StreamReader;

/**
 * A convenience class used to read data from a file without any order. It is meant
 * to be equivalent to accessing certain fields of an structure.
 */
public class DataStructure {

	private final StreamReader stream;
	private long pointer;
	
	public DataStructure(StreamReader stream) {
		this.stream = stream;
	}
	
	public StreamReader getStream() {
		return stream;
	}
	
	public long getPointer() {
		return pointer;
	}
	
	public void setPointer(long pointer) {
		this.pointer = pointer;
	}
	
	public long getUInt(int offset) throws IOException {
		stream.seek(pointer + offset);
		return stream.readLEUInt();
	}
	
	public int getInt(int offset) throws IOException {
		stream.seek(pointer + offset);
		return stream.readLEInt();
	}
	
	public float getFloat(int offset) throws IOException {
		stream.seek(pointer + offset);
		return stream.readLEFloat();
	}
}
