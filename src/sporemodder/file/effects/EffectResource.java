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

import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureIgnore;
import emord.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public abstract class EffectResource implements EffectFileElement {
	
	/** The structure metadata used for reading/writing this class. */
	public static final StructureMetadata<EffectResource> STRUCTURE_METADATA = StructureMetadata.generate(EffectResource.class);

	public static final int TYPE_MASK = 0x7F0000;
	
	public final ResourceID resourceID = new ResourceID();
	@StructureIgnore protected int version;
	@StructureIgnore protected EffectDirectory effectDirectory;
	
	public EffectResource(EffectDirectory effectDirectory, int version) {
		this.effectDirectory = effectDirectory;
		this.version = version;
	}
	
	@Override public String toString() {
		return getFactory().getKeyword() + ' '  + getName();
	}
	
	@Override public String getName() {
		return resourceID.toString();
	}

	public abstract EffectResourceFactory getFactory();
	
	@Override public boolean isEffectComponent() {
		return false;
	}
}
