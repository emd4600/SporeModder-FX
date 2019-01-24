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
package sporemodder.file.tlsa;

import java.io.IOException;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class TLSAAnimation {
	public int id;
	public String description;
	public float durationScale = 1.0f;
	public float duration = -1.0f;
	
	public void read(StreamReader in, int version) throws IOException {
		
		if (version > 8) {
			durationScale = in.readFloat();
			duration = in.readFloat();
		}
		
		id = in.readInt();
		description = in.readString(StringEncoding.UTF16LE, in.readInt());
		
	}
	
	public void write(StreamWriter out, int version) throws IOException {
		
		if (version > 8) {
			out.writeFloat(durationScale);
			out.writeFloat(duration);
		}
		
		out.writeInt(id);
		out.writeInt(description == null ? 0 : description.length());
		if (description != null) {
			out.writeString(description, StringEncoding.UTF16LE);
		}
	}
}
