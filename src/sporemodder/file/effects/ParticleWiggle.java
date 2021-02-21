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

import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public class ParticleWiggle {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ParticleWiggle> STRUCTURE_METADATA = StructureMetadata.generate(ParticleWiggle.class);
	
	public float timeRate;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] rateDirection = new float[3];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] wiggleDirection = new float[3];
	
	public ParticleWiggle() {}
	public ParticleWiggle(ParticleWiggle other) {
		timeRate = other.timeRate;
		rateDirection[0] = other.rateDirection[0];
		rateDirection[1] = other.rateDirection[1];
		rateDirection[2] = other.rateDirection[2];
		wiggleDirection[0] = other.wiggleDirection[0];
		wiggleDirection[1] = other.wiggleDirection[1];
		wiggleDirection[2] = other.wiggleDirection[2];
	}
}
