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

public class RWSectionAtoms extends RWObject {

	public static final int TYPE_CODE = 0x10008;
	
	public int field_0;
	public int field_4;
	
	public RWSectionAtoms(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		field_0 = stream.readLEInt();
		field_4 = stream.readLEInt();
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(field_0);
		stream.writeLEInt(field_4);
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

}
