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

public class RWSkinsInK extends RWObject {

	public static final int TYPE_CODE = 0x7000c;
	public static final int ALIGNMENT = 4;
	
	public RWObject field_0;
	/** Not used in the file, in Spore code it's a pointer to a function. */
	public int field_4;
	public RWSkinMatrixBuffer skinMatrixBuffer;
	public RWSkeleton skeleton;
	public RWAnimationSkin animationSkin;

	public RWSkinsInK(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		field_0 = renderWare.get(stream.readLEInt());
		field_4 = stream.readLEInt();
		skinMatrixBuffer = (RWSkinMatrixBuffer) renderWare.get(stream.readLEInt());
		skeleton = (RWSkeleton) renderWare.get(stream.readLEInt());
		animationSkin = (RWAnimationSkin) renderWare.get(stream.readLEInt());
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(renderWare.indexOf(field_0));
		stream.writeLEInt(field_4);
		stream.writeLEInt(renderWare.indexOf(skinMatrixBuffer));
		stream.writeLEInt(renderWare.indexOf(skeleton));
		stream.writeLEInt(renderWare.indexOf(animationSkin));
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
