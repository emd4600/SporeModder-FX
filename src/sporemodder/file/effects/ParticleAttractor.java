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

import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureLength;
import emord.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public class ParticleAttractor {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ParticleAttractor> STRUCTURE_METADATA = StructureMetadata.generate(ParticleAttractor.class);

	@StructureLength.Value(32) public final List<Float> attractorStrength = new ArrayList<Float>();
	public float range = 1.0f;
	public float killRange;
	
	public ParticleAttractor() {}
	public ParticleAttractor(ParticleAttractor other) {
		copy(other);
	}
	
	public void copy(ParticleAttractor other) {
		attractorStrength.clear();
		attractorStrength.addAll(other.attractorStrength);
		range = other.range;
		killRange = other.killRange;
}
}
