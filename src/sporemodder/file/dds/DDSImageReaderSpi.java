/*
 * DDSImageReaderSpi.java - This file is part of Java DDS ImageIO Plugin
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
 * TODO Write File Description for DDSImageReaderSpi.java
 */

package sporemodder.file.dds;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


public class DDSImageReaderSpi extends ImageReaderSpi {

	static final int MAGIC = 0x20534444;
	
	public DDSImageReaderSpi(){
		super (
			"Niklas K. Rasmussen",	// vendorName
			"0.0.1 ALPHA 1",		// version
			new String [] { "DDS" },// names
			new String [] { "dds" },// suffixes
			new String [] { "image/dds" },// MIMETypes
			"net.nikr.dds.DDSImageReader",	// readerClassName
			STANDARD_INPUT_TYPE,	// inputTypes
			null,	// writerSpiNames
			false,	// supportsStandardStreamMetadataFormat
			null,	// nativeStreamMetadataFormatName
			null,	// nativeStreamMetadataFormatClassName
			null,	// extraStreamMetadataFormatNames
			null,	// extraStreamMetadataFormatClassNames
			false,	// supportsStandardImageMetadataFormat
			null,	// nativeImageMetadataFormatName
			null,	// nativeImageMetadataFormatClassName
			null,	// extraImageMetadataFormatNames
			null);	// extraImageMetadataFormatClassNames
	}
	
	@Override
	public boolean canDecodeInput(Object source) throws IOException {
		if (!(source instanceof ImageInputStream)) return false;
		ImageInputStream stream = (ImageInputStream) source;
		stream.reset();
		stream.mark();
		final ByteOrder order = stream.getByteOrder();
		try {
			stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
			int magic = stream.readInt();
			if (magic != MAGIC) return false;
			int size = stream.readInt();
			if (size != 124) return false;
			stream.reset();
			return true;
		} finally {
			stream.setByteOrder(order);
		}
	}

	@Override
	public ImageReader createReaderInstance(Object extension) throws IOException {
		return new DDSImageReader(this);
	}

	@Override
	public String getDescription(Locale locale) {
		return "Java DDS ImageIO Plugin by Niklas K. Rasmussen";
	}

}
