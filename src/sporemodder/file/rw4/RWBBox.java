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
import sporemodder.file.BoundingBox;

public class RWBBox extends RWObject {

	public static final int TYPE_CODE = 0x80005;
	public static final int ALIGNMENT = 16;
	
	public final BoundingBox boundingBox = new BoundingBox();
	public int field_0C;  // probably padding
	public int field_1C;  // probably padding
	
	public RWBBox(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		boundingBox.getMin().readLE(stream);
		field_0C = stream.readLEInt();
		boundingBox.getMax().readLE(stream);
		field_1C = stream.readLEInt();
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		boundingBox.getMin().writeLE(stream);
		stream.writeLEInt(field_0C);
		boundingBox.getMax().writeLE(stream);
		stream.writeLEInt(field_1C);
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
