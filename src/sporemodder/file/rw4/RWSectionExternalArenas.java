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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class RWSectionExternalArenas extends RWObject {
	
	public static final int TYPE_CODE = 0x10006;
	
	public int field_0 = 3;
	public int field_4 = 0x18;
	public int field_8 = 1;
	public int field_C = 0xffb00000;
	public int field_10 = 1;
	public int field_14;
	public int field_18;
	public int field_1C;

	public RWSectionExternalArenas(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		field_0 = stream.readLEInt();
		field_4 = stream.readLEInt();
		field_8 = stream.readLEInt();
		field_C = stream.readLEInt();
		field_10 = stream.readLEInt();
		field_14 = stream.readLEInt();
		field_18 = stream.readLEInt();
		field_1C = stream.readLEInt();
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(field_0);
		stream.writeLEInt(field_4);
		stream.writeLEInt(field_8);
		stream.writeLEInt(field_C);
		stream.writeLEInt(field_10);
		stream.writeLEInt(field_14);
		stream.writeLEInt(field_18);
		stream.writeLEInt(field_1C);
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

}
