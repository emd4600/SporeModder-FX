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
	public double[] startPos = new double[3];
	public double[] endPos = new double[3];
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
		stream.readLEDoubles(startPos);
		stream.readLEDoubles(endPos);
		defaultTime = stream.readLEFloat();
		animation = (RWKeyframeAnim) renderWare.get(stream.readLEInt());
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(handleID);
		stream.writeLEInt(field_4);
		stream.writeLEDoubles(startPos);
		stream.writeLEDoubles(endPos);
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
