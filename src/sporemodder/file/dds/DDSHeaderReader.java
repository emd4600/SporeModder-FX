/*
 * DDSHeaderReader.java - This file is part of Java DDS ImageIO Plugin
 *
 * Copyright (C) 2011 Niklas Kyster Rasmussen
 * 
 * COPYRIGHT NOTICE:
 * Java DDS ImageIO Plugin is based on code from the DDS GIMP plugin.
 * Copyright (C) 2004-2010 Shawn Kirst <skirst@insightbb.com>,
 * Copyright (C) 2003 Arne Reuter <homepage@arnereuter.de>
 *
 * Java DDS ImageIO Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Java DDS ImageIO Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java DDS ImageIO Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * FILE DESCRIPTION:
 * TODO Write File Description for DDSHeaderReader.java
 */

package sporemodder.file.dds;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.IIOException;
import javax.imageio.stream.ImageInputStream;


public class DDSHeaderReader {

	private static final int MAGIC = 0x20534444;

	public DDSHeader readHeader(ImageInputStream stream) throws IIOException {
		if (stream == null) {
			throw new IIOException("Failed to load header: Stream is null");
		}
		try {
			stream.reset();
			stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

			int magic = stream.readInt();
			if (magic != MAGIC) {
				throw new IIOException("Failed To Load Header: magic ("+magic+") is not MAGIC ("+MAGIC+") at " + stream.getStreamPosition());
			}
			int size = stream.readInt();
			if (size != 124) {
				throw new IIOException("Failed To Load Header: size ("+size+") value is not 124");
			}
			long flags = stream.readInt() & 0xFFFFFFFFL;
			long height = stream.readInt() & 0xFFFFFFFFL;
			long width = stream.readInt() & 0xFFFFFFFFL;
			long linearSize = stream.readInt() & 0xFFFFFFFFL;
			long depth = stream.readInt() & 0xFFFFFFFFL;
			long mipMapCount = stream.readInt() & 0xFFFFFFFFL;

			stream.skipBytes(11 * 4);

			DDSPixelFormat ddsPixelFormat = readPixelFormat(stream);
			long caps = stream.readInt() & 0xFFFFFFFFL;
			long caps2 = stream.readInt() & 0xFFFFFFFFL;
			long caps3 = stream.readInt() & 0xFFFFFFFFL;
			long caps4 = stream.readInt() & 0xFFFFFFFFL;
			DDSHeaderDX10 ddsHeaderDX10 = null;
			if (ddsPixelFormat.getFormat() == DDSPixelFormat.Format.DX10) {
				ddsHeaderDX10 = readHeaderDX10(stream);
			}
			// Added here by me, according to Microsoft specification: https://docs.microsoft.com/en-us/windows/desktop/direct3ddds/dds-header
			stream.readInt();
			
			DDSHeader ddsHeader = new DDSHeader(size, flags, height, width, linearSize, depth, mipMapCount, ddsPixelFormat, caps, caps2, caps3, caps4, ddsHeaderDX10);
			return ddsHeader;
		} catch (IOException ex) {
			throw new IIOException("Failed To Load Header: " + ex.getMessage());
		}
	}

	private DDSPixelFormat readPixelFormat(ImageInputStream stream) throws IOException {
		int size = stream.readInt();
		if (size != 32) {
			throw new IOException("Failed load PixelFormat: File ill formed");  //should throw something...
		}
		long flags = stream.readInt() & 0xFFFFFFFFL;
		long fourCC = stream.readInt() & 0xFFFFFFFFL;
		long rgbBitCount = stream.readInt() & 0xFFFFFFFFL;
		long rBitMask = stream.readInt() & 0xFFFFFFFFL;
		long gBitMask = stream.readInt() & 0xFFFFFFFFL;
		long bBitMask = stream.readInt() & 0xFFFFFFFFL;
		long aBitMask = stream.readInt() & 0xFFFFFFFFL;
		DDSPixelFormat ddsPixelFormat = new DDSPixelFormat(size, flags, fourCC, rgbBitCount, rBitMask, gBitMask, bBitMask, aBitMask);
		//stream.readInt();
		return ddsPixelFormat;
	}

	private DDSHeaderDX10 readHeaderDX10(ImageInputStream stream) throws IIOException {
		try {
			long dxgiFormat = stream.readInt() & 0xFFFFFFFFL;
			long resourceDimension = stream.readInt() & 0xFFFFFFFFL;
			long miscFlag = stream.readInt() & 0xFFFFFFFFL;
			long arraySize = stream.readInt() & 0xFFFFFFFFL;
			long miscFlags2 = stream.readInt() & 0xFFFFFFFFL;
			return new DDSHeaderDX10(dxgiFormat, resourceDimension, miscFlag, arraySize, miscFlags2);
		} catch (IOException ex) {
			throw new IIOException("Failed to load DX10 header: " + ex.getMessage());
		}
	}
}
