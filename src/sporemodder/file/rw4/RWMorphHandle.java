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

public class RWMorphHandle extends RWObject {
	
	public static final int TYPE_CODE = 0xff0000;
	public static final int ALIGNMENT = 4;
	
	public int handleID;
	public int field_4;
	public int field_8;
	public float field_C;
	public int field_10;
	public float field_14;
	public int field_18;
	public float field_1C;
	public int field_20;
	public float field_24;
	public int field_28;
	public float field_2C;
	public int field_30;
	public float field_34;
	/** The progress of the morph animation when no handles have been modified. */
	public float defaultTime;
	public RWKeyframeAnim animation;

	public RWMorphHandle(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		handleID = stream.readLEInt();
		field_4 = stream.readLEInt();
		field_8 = stream.readLEInt();
		field_C = stream.readLEFloat();
		field_10 = stream.readLEInt();
		field_14 = stream.readLEFloat();
		field_18 = stream.readLEInt();
		field_1C = stream.readLEFloat();
		field_20 = stream.readLEInt();
		field_24 = stream.readLEFloat();
		field_28 = stream.readLEInt();
		field_2C = stream.readLEFloat();
		field_30 = stream.readLEInt();
		field_34 = stream.readLEFloat();
		defaultTime = stream.readLEFloat();
		animation = (RWKeyframeAnim) renderWare.get(stream.readLEInt());
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(handleID);
		stream.writeLEInt(field_4);
		stream.writeLEInt(field_8);
		stream.writeLEFloat(field_C);
		stream.writeLEInt(field_10);
		stream.writeLEFloat(field_14);
		stream.writeLEInt(field_18);
		stream.writeLEFloat(field_1C);
		stream.writeLEInt(field_20);
		stream.writeLEFloat(field_24);
		stream.writeLEInt(field_28);
		stream.writeLEFloat(field_2C);
		stream.writeLEInt(field_30);
		stream.writeLEFloat(field_34);
		stream.writeLEFloat(defaultTime);
		stream.writeLEInt(renderWare.indexOf(animation));
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
