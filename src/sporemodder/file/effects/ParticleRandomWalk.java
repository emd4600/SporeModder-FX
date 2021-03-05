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
package sporemodder.file.effects;

import java.util.ArrayList;
import java.util.List;

import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public class ParticleRandomWalk {
	
	/** The structure metadata used for reading/writing this class. */
	public static final StructureMetadata<ParticleRandomWalk> STRUCTURE_METADATA = StructureMetadata.generate(ParticleRandomWalk.class);

	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] time = {5, 5};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] strength = new float[2];
	public float turnRange = 0.25f;
	public float turnOffset;
	public float mix;
	@StructureLength.Value(32) public final List<Float> turnOffsetCurve = new ArrayList<Float>();
	public byte loopType = 2;
	
	public ParticleRandomWalk() {}
	public ParticleRandomWalk(ParticleRandomWalk other) {
		copy(other);
	}
	
	public void copy(ParticleRandomWalk other) {
		time[0] = other.time[0];
		time[1] = other.time[1];
		strength[0] = other.strength[0];
		strength[1] = other.strength[1];
		turnRange = other.turnRange;
		turnOffset = other.turnOffset;
		mix = other.mix;
		turnOffsetCurve.clear();
		turnOffsetCurve.addAll(other.turnOffsetCurve);
		loopType = other.loopType;
	}
}
