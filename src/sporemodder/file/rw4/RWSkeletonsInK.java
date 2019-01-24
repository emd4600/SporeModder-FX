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

public class RWSkeletonsInK extends RWObject {
	
	public static final int TYPE_CODE = 0x7000b;
	public static final int ALIGNMENT = 4;
	
	public RWObject object1;
	/** Not used in file, pointer to a function. */
	public int pFunction;
	public RWObject object2;
	public RWSkeleton skeleton;
	public final List<RWCompiledState> compiledStates = new ArrayList<RWCompiledState>();

	public RWSkeletonsInK(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		object1 = renderWare.get(stream.readLEInt());
		pFunction = stream.readLEInt();
		long arrayOffset = stream.readLEUInt();
		object2 = renderWare.get(stream.readLEInt());
		skeleton = (RWSkeleton) renderWare.get(stream.readLEInt());
		int arrayCount = stream.readLEInt();
		
		stream.seek(arrayOffset);
		for (int i = 0; i < arrayCount; i++) {
			compiledStates.add((RWCompiledState) renderWare.get(stream.readLEInt()));
		}
		
		// After this goes a 0?
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(renderWare.indexOf(object1));
		stream.writeLEInt(pFunction);
		stream.writeLEUInt(stream.getFilePointer() + 16);
		stream.writeLEInt(renderWare.indexOf(object2));
		stream.writeLEInt(renderWare.indexOf(skeleton));
		stream.writeLEInt(compiledStates.size());
		
		for (RWCompiledState compiledState : compiledStates) {
			stream.writeLEInt(renderWare.indexOf(compiledState));
		}
		
		// After this goes a 0?
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
