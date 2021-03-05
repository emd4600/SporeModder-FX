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
package sporemodder.file.simulator.attributes;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class DefaultAttribute implements SimulatorAttribute {
	public byte[] data;
	public long position;
	
	@Override
	public void read(StreamReader stream, int size) throws IOException {
		data = new byte[size];
		position = stream.getFilePointer();
		stream.read(data);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		if (data != null) {
			stream.write(data);
		}
	}
	
	@Override
	public String toString(String tabulation) {
		return "DEFAULT IMPLEMENTATION (" + data.length + ", " + position + ")";
	}

	@Override
	public int getSize() {
		return data == null ? 0 : data.length;
	}

	@Override
	public String toXmlString(String tabulation) {
		return toString(tabulation);
	}
}
