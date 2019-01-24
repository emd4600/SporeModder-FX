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
import java.util.HashMap;
import java.util.Map;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class RWAnimations extends RWObject {
	
	public static final int TYPE_CODE = 0xff0001;
	public static final int ALIGNMENT = 4;
	
	public final HashMap<Integer, RWKeyframeAnim> animations = new HashMap<Integer, RWKeyframeAnim>();

	public RWAnimations(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		stream.readLEInt();  // index to subreference to this object
		int count = stream.readLEInt();
		
		for (int i = 0; i < count; i++) {
			animations.put(stream.readLEInt(), (RWKeyframeAnim) renderWare.get(stream.readLEInt()));
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		renderWare.addReference(this, 8);
		
		stream.writeLEInt(renderWare.indexOf(this, RenderWare.INDEX_SUB_REFERENCE));
		stream.writeLEInt(animations.size());
		
		for (Map.Entry<Integer, RWKeyframeAnim> entry : animations.entrySet()) {
			stream.writeLEInt(entry.getKey());
			stream.writeLEInt(renderWare.indexOf(entry.getValue()));
		}
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
