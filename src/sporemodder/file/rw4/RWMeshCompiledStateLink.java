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

public class RWMeshCompiledStateLink extends RWObject {
	
	public static final int TYPE_CODE = 0x2001a;
	public static final int ALIGNMENT = 4;
	
	public RWMesh mesh;
	public final List<RWCompiledState> compiledStates = new ArrayList<RWCompiledState>();

	public RWMeshCompiledStateLink(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		mesh = (RWMesh) renderWare.get(stream.readLEInt());
		int count = stream.readLEInt();
		for (int i = 0; i < count; i++) {
			compiledStates.add((RWCompiledState) renderWare.get(stream.readLEInt()));
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(renderWare.indexOf(mesh));
		stream.writeLEInt(compiledStates.size());
		
		for (RWCompiledState state : compiledStates) {
			stream.writeLEInt(renderWare.indexOf(state));
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
