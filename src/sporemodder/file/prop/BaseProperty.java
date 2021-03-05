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
package sporemodder.file.prop;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptWriter;

abstract class BaseProperty {
	protected boolean isArray;
	protected int arrayItemSize;
	protected int arrayItemCount;
	protected int flags;
	protected int type;
	
	protected BaseProperty(int type, int flags) {
		this.isArray = false;
		this.arrayItemSize = 0;
		this.arrayItemCount = 0;
		this.flags = flags;
		this.type = type;
	}
	
	protected BaseProperty(int type, int flags, int arrayItemSize, int arrayItemCount) {
		this.isArray = true;
		this.arrayItemSize = arrayItemSize;
		this.arrayItemCount = arrayItemCount;
		this.flags = flags | 0x30;  // Ensure it is an array
		this.type = type;
	}
	
	public abstract void read(StreamReader stream, int itemCount) throws IOException;
	public abstract void write(StreamWriter stream) throws IOException;
	
	public abstract void writeArgScript(String propertyName, ArgScriptWriter writer);
}
