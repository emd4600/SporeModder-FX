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
package sporemodder.file.lvl;

import sporemodder.util.Vector3;
import sporemodder.util.Vector4;

public class GameplayMarker {

	private final Vector3 offset = new Vector3();
	private final Vector4 orientation = new Vector4();  // quaternion
	// Such as CreatureArchetype, 0x1BE418E, avatar
	// Not sure if it might be a ResourceKey
	private final long[] ids = new long[3];
	
	public Vector3 getOffset() {
		return offset;
	}
	
	public Vector4 getOrientation() {
		return orientation;
	}

	public long[] getIds() {
		return ids;
	}
	
}
