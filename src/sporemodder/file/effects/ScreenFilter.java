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

import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;

public class ScreenFilter {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ScreenFilter> STRUCTURE_METADATA = StructureMetadata.generate(ScreenFilter.class);
	
	
	public byte type;
	public byte destination;
	public final ResourceID source = new ResourceID();
	@StructureLength.Value(32) public final List<Byte> parameters = new ArrayList<Byte>();

	
}
