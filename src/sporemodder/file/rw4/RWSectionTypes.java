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

public class RWSectionTypes extends RWObject {
	
	public static final int TYPE_CODE = 0x10005;
	
	public final List<Integer> typeCodes = new ArrayList<Integer>();
	public int field_04 = 12;

	public RWSectionTypes(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		int count = stream.readLEInt();
		field_04 = stream.readLEInt();
		
		for (int i = 0; i < count; i++) {
			typeCodes.add(stream.readLEInt());
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(typeCodes.size());
		stream.writeLEInt(field_04);
		for (Integer code : typeCodes) {
			stream.writeLEInt(code);
		}
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

}
