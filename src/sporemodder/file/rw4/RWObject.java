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

/**
 * The most basic class used by objects in RenderWare files. Its implementations must define a read and write method,
 * as well as a method that returns the type code used to uniquely identify the type of object. Optionally, an alignment method can also
 * be specified. All RWObjects keep track of the owner RenderWare class.
 */
public abstract class RWObject {
	
	/** The owner RenderWare class. */
	protected RenderWare renderWare;
	
	/** Object metadata used when generating the file. Not all objects require this. */
	protected RWSectionInfo sectionInfo;
	
	public RWObject(RenderWare renderWare) {
		this.renderWare = renderWare;
	}
	
	public RWSectionInfo getSectionInfo() {
		return sectionInfo;
	}

	/**
	 * Reads the data of the object from the given stream. The stream file pointer is at the start offset of the object.
	 * @param stream
	 * @throws IOException
	 */
	public abstract void read(StreamReader stream) throws IOException;
	
	/**
	 * Writes the data of the object to the given stream. The stream file pointer is at the start offset of the object.
	 * @param stream
	 * @throws IOException
	 */
	public abstract void write(StreamWriter stream) throws IOException;
	
	/**
	 * Returns the 32-bit code used to uniquely identify the type of object.
	 * @return
	 */
	public abstract int getTypeCode();
	
	/**
	 * Returns the alignment used by the section data, if any. Padding bytes will be added before the object data so that
	 * the start offset can be divided by the alignment.
	 * @return
	 */
	public int getAlignment() {
		return 0;
	}
	
	/**
	 * Returns the owner RenderWare object.
	 * @return
	 */
	public final RenderWare getRenderWare() {
		return renderWare;
	}
}
