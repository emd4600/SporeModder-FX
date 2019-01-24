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

public class RWCompiledState extends RWObject {
	
	public static final int TYPE_CODE = 0x2000b;
	public static final int ALIGNMENT = 16;
	
	public final MaterialStateCompiler data;

	public RWCompiledState(RenderWare renderWare) {
		super(renderWare);
		data = new MaterialStateCompiler(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		// The first value is the size, but it's not always reliable
		byte[] data = new byte[sectionInfo.size];
		stream.read(data);
		this.data.data = data;
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(data.data.length);
		stream.write(data.data);
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
	
	@Override
	public int getAlignment() {
		return ALIGNMENT;
	}

}
