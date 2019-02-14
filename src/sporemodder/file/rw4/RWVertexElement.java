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
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLMETHOD;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLTYPE;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLUSAGE;
import sporemodder.file.rw4.Direct3DEnums.RWDECLUSAGE;

public class RWVertexElement {

	public static final String KEYWORD = "element";
	
	public int stream;
	/** The offset position of this element inside the vertex data. */
	public int offset;
	public D3DDECLTYPE type;
	public D3DDECLMETHOD method;
	public D3DDECLUSAGE usage;
	public byte usageIndex;
	public int typeCode;
	
	public void read(StreamReader stream) throws IOException {
		this.stream = stream.readLEShort();
		offset = stream.readLEShort();
		type = D3DDECLTYPE.getById(stream.readByte());
		method = D3DDECLMETHOD.getById(stream.readByte());
		usage = D3DDECLUSAGE.getById(stream.readByte());
		usageIndex = stream.readByte();
		typeCode = stream.readLEInt();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEShort(this.stream);
		stream.writeLEShort(offset);
		stream.writeByte(type.id);
		stream.writeByte(method.id);
		stream.writeByte(usage.id);
		stream.writeByte(usageIndex);
		stream.writeLEInt(typeCode);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(type, usage, RWDECLUSAGE.getById(typeCode) == null ? typeCode : RWDECLUSAGE.getById(typeCode));
		if (stream != 0) writer.option("stream").ints(stream);
		if (usageIndex != 0) writer.option("usageIndex").ints(usageIndex);
		if (method != D3DDECLMETHOD.D3DDECLMETHOD_DEFAULT) writer.option("method").arguments(method);
	}
}
