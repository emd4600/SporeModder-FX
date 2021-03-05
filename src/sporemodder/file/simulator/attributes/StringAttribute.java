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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Stream.StringEncoding;

public class StringAttribute implements SimulatorAttribute {
	public String value;
	
	@Override
	public void read(StreamReader stream, int size) throws Exception {
		value = stream.readString(StringEncoding.UTF16BE, stream.readInt());
	}

	@Override
	public void write(StreamWriter stream) throws Exception {
		stream.writeInt(value.length());
		stream.writeString(value, StringEncoding.UTF16BE);
	}

	@Override
	public int getSize() {
		return 4 + 2*value.length();
	}
	
	@Override
	public String toString(String tabulation) {
		return value.toString();
	}

	@Override
	public String toXmlString(String tabulation) {
		return toString(tabulation);
	}
}
