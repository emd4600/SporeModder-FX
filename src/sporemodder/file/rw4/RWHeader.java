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
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class RWHeader {
	
	private static final byte[] MAGIC = {(byte)0x89, 0x52, 0x57, 0x34, 0x77, 0x33, 0x32, 0x00, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x20, 0x04
			, 0x00, 0x34, 0x35, 0x34, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00};
	
	public static enum RenderWareType {
		MODEL(1),
		TEXTURE(0x04000000),
		SPECIAL(0xCAFED00D);
		
		private int code;
		
		private RenderWareType(int code) { this.code = code; };
		
		public int getCode() {
			return code;
		}
		
		public static RenderWareType get(int code) {
			for (RenderWareType type : RenderWareType.values()) {
				if (type.code == code) {
					return type;
				}
			}
			return null;
		}
	}
	
	public final static int TYPE_MODEL = 1;
	public final static int TYPE_TEXTURE = 0x04000000;
	public final static int TYPE_SPECIAL = 0xCAFED00D;
	
	public final RenderWare renderWare;
	public final RWSectionManifest sectionManifest;
	public RenderWareType type;
	public int unknownBits = 0x00C00758;

	public RWHeader(RenderWare renderWare) {
		this.renderWare = renderWare;
		sectionManifest = new RWSectionManifest(renderWare);
	}
	
	/**
	 * Reads the header information and the section headers, which are returned in a list.
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public List<RWSectionInfo> read(StreamReader stream, List<RWSectionInfo> sectionInfos) throws IOException {
		// magic
		stream.skip(28);
		type = RenderWareType.get(stream.readLEInt());
		
		// This one is sectionCount too, but apparently Spore uses the second one
		stream.skip(1 * 4);
		int sectionCount = stream.readLEInt();
		// 0x10 if it's a model, 4 if it's a texture
		// Always 0?
		stream.skip(2 * 4);
		long pSectionInfo = stream.readLEUInt();
		// Always 0x98, 0, 0, 0 ?
		stream.skip(4 * 4);
		long pBufferData = stream.readLEUInt();
		
		// Nothing important here
		stream.skip(7 * 4);
		unknownBits = stream.readLEInt();
		stream.skip(12 * 4);
		
		sectionManifest.read(stream);
		
		stream.seek(pSectionInfo);
		for (int i = 0; i < sectionCount; i++) {
			RWSectionInfo sectionInfo = new RWSectionInfo();
			sectionInfo.read(stream);
			sectionInfos.add(sectionInfo);
			
			if (sectionInfo.typeCode == RWBaseResource.TYPE_CODE) {
				sectionInfo.pData += pBufferData;
			}
		}
		
		return sectionInfos;
	}

	/**
	 * Writes the header information but NOT the section headers.
	 * @param stream
	 * @throws IOException
	 */
	public void write(StreamWriter stream, long pSectionInfo, long pBufferData, long buffersSize) throws IOException {
		stream.write(MAGIC);
		stream.writeLEInt(type.getCode());
		stream.writeLEInt(renderWare.getObjects().size());
		stream.writeLEInt(renderWare.getObjects().size());
		
		if (type == RenderWareType.TEXTURE) {
			stream.writeLEInt(4);
		}
		else {
			// SPECIAL and MODEL use this
			stream.writeLEInt(16);
		}
		
		stream.writeLEInt(0);
		stream.writeLEUInt(pSectionInfo);
		stream.writeLEInt(0x98);
		stream.writeLEInt(0);
		stream.writeLEInt(0);
		stream.writeLEInt(0);
		stream.writeLEUInt(pBufferData);
		stream.writeLEInt(16);
		stream.writeLEUInt(buffersSize);
		stream.writeLEInts(4, 0, 1, 0, 1, unknownBits, 4, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0);
		
		sectionManifest.write(stream);
	}
}
