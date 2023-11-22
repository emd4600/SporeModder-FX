/*
 * DDSHeader.java - This file is part of Java DDS ImageIO Plugin
 *
 * Copyright (C) 2011 Niklas Kyster Rasmussen <niklaskr@gmail.com>
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
 * TODO Write File Description for DDSHeader.java
 */

package sporemodder.file.dds;

import sporemodder.file.dds.DDSPixelFormat.Format;

public class DDSHeader {
	public static final int CAPS = 0x1;
	public static final int HEIGHT = 0x2;
	public static final int WIDTH = 0x4;
	public static final int PITCH = 0x8;
	public static final int PIXELFORMAT = 0x1000;
	public static final int MIPMAPCOUNT = 0x20000;
	public static final int LINEARSIZE = 0x80000;
	public static final int DEPTH = 0x800000;
	
	public static final int CAPS_COMPLEX = 0x8;
	public static final int CAPS_MIPMAP = 0x400000;
	public static final int CAPS_TEXTURE = 0x1000;
	
	public static final int CAPS2_CUBEMAP = 0x200;
	public static final int CAPS2_CUBEMAP_POSITIVEX = 0x400;
	public static final int CAPS2_CUBEMAP_NEGATIVEX = 0x800;
	public static final int CAPS2_CUBEMAP_POSITIVEY = 0x1000;
	public static final int CAPS2_CUBEMAP_NEGATIVEY = 0x2000;
	public static final int CAPS2_CUBEMAP_POSITIVEZ = 0x4000;
	public static final int CAPS2_CUBEMAP_NEGATIVEZ = 0x8000;
	public static final int CAPS2_VOLUME = 0x200000;
	
	private final long size;
	private final long flags;
	private final long height;
	private final long width;
	private final long pitchOrLinearSize;
	private final long depth;
	private final long mipMapCount;
	private final DDSPixelFormat ddsPixelFormat;
	private final long caps;
    private final long caps2;
    private final long caps3;
    private final long caps4;
	private final DDSHeaderDX10 ddsHeaderDX10;
	//Calculated
	private final Format format;
	private final long maxImageIndex;

	public DDSHeader(long size, long flags, long height, long width, long pitchOrLinearSize, long depth, long mipMapCount, DDSPixelFormat ddsPixelFormat, long caps, long caps2, long caps3, long caps4, DDSHeaderDX10 ddsHeaderDX10) {
		this.size = size;
		this.flags = flags;
		this.height = height;
		this.width = width;
		this.pitchOrLinearSize = pitchOrLinearSize;
		this.depth = depth;
		if (mipMapCount <= 0) { //Minimum one image inside (Also when the mapmap flag is not set)
			this.mipMapCount = 1;
		} else {
			this.mipMapCount = mipMapCount;
		}
		this.ddsPixelFormat = ddsPixelFormat;
		this.caps = caps;
		this.caps2 = caps2;
		this.caps3 = caps3;
		this.caps4 = caps4;
		this.ddsHeaderDX10 = ddsHeaderDX10;
		this.format = calcFormat();
		this.maxImageIndex = calcMaxImageIndex(this.mipMapCount);
	}

	public Format getFormat() {
		return format;
	}

	public long getSize() {
		return size;
	}
	public long getFlags() {
		return flags;
	}
	public long getHeight(int mipMap) {
		int fixedMipMap = mipMap;
		if (fixedMipMap >= getMipMapCount()) {
			fixedMipMap = fixedMipMap - ((fixedMipMap / (int)getMipMapCount()) * (int)getMipMapCount());
		}
		return Math.max(height >> fixedMipMap, 1);
	}
	public long getWidth(int mipMap) {
		int fixedMipMap = mipMap;
		if (fixedMipMap >= getMipMapCount()) {
			fixedMipMap = fixedMipMap - ((fixedMipMap / (int)getMipMapCount()) * (int)getMipMapCount());
		}
		return Math.max(width >> fixedMipMap, 1);
	}
	public long getPitchOrLinearSize() {
		return pitchOrLinearSize;
	}
	public long getDepth() {
		return depth;
	}
	public long getMipMapCount() {
		return mipMapCount;
	}

	public long getMaxImageIndex() {
		return maxImageIndex;
	}

	public void printValues(){
		System.out.println("DDSHeader:");
		System.out.println("	size: "+size);
		System.out.print("	flags: "+flags);
		if ((flags & CAPS) != 0) System.out.print(" (CAPS)");
		if ((flags & HEIGHT) != 0) System.out.print(" (HEIGHT)");
		if ((flags & WIDTH) != 0) System.out.print(" (WIDTH)");
		if ((flags & PITCH) != 0) System.out.print(" (PITCH)");
		if ((flags & PIXELFORMAT) != 0) System.out.print(" (PIXELFORMAT)");
		if ((flags & MIPMAPCOUNT) != 0) System.out.print(" (MIPMAPCOUNT)");
		if ((flags & LINEARSIZE) != 0) System.out.print(" (LINEARSIZE)");
		if ((flags & DEPTH) != 0) System.out.print(" (DEPTH)");
		System.out.print("\n");
		System.out.println("	height: "+height);
		System.out.println("	width: "+width);
		System.out.println("	linearSize: "+pitchOrLinearSize);
		System.out.println("	depth: "+depth);
		System.out.println("	mipMapCount: "+mipMapCount);
		ddsPixelFormat.printValues(1);
		System.out.print("	caps: "+caps);
		if ((caps & CAPS_COMPLEX) != 0) System.out.print(" (CAPS_COMPLEX)");
		if ((caps & CAPS_MIPMAP) != 0) System.out.print(" (CAPS_MIPMAP)");
		if ((caps & CAPS_TEXTURE) != 0) System.out.print(" (CAPS_TEXTURE)");
		System.out.print("\n");
		System.out.print("	caps2: "+caps2);
		if ((caps2 & CAPS2_CUBEMAP) != 0) System.out.print(" (CAPS2_CUBEMAP)");
		if ((caps2 & CAPS2_CUBEMAP_POSITIVEX) != 0) System.out.print(" (CAPS2_CUBEMAP_POSITIVEX)");
		if ((caps2 & CAPS2_CUBEMAP_NEGATIVEX) != 0) System.out.print(" (CAPS2_CUBEMAP_NEGATIVEX)");
		if ((caps2 & CAPS2_CUBEMAP_POSITIVEY) != 0) System.out.print(" (CAPS2_CUBEMAP_POSITIVEY)");
		if ((caps2 & CAPS2_CUBEMAP_NEGATIVEY) != 0) System.out.print(" (CAPS2_CUBEMAP_NEGATIVEY)");
		if ((caps2 & CAPS2_CUBEMAP_POSITIVEZ) != 0) System.out.print(" (CAPS2_CUBEMAP_POSITIVEZ)");
		if ((caps2 & CAPS2_CUBEMAP_NEGATIVEZ) != 0) System.out.print(" (CAPS2_CUBEMAP_NEGATIVEZ)");
		if ((caps2 & CAPS2_VOLUME) != 0) System.out.print(" (CAPS2_VOLUME)");
		System.out.print("\n");
		System.out.println("	caps3: "+caps3);
		System.out.println("	caps4: "+caps4);
		if (ddsHeaderDX10 != null) {
			ddsHeaderDX10.printValues(1);
		}
	}
	public DDSPixelFormat getPixelFormat() {
		return ddsPixelFormat;
	}

	public DDSHeaderDX10 getHeaderDX10() {
		return ddsHeaderDX10;
	}

	private Format calcFormat() {
		Format calcFormat;
		if (ddsHeaderDX10 != null) {
			calcFormat = ddsHeaderDX10.getFormat();
		} else {
			calcFormat = ddsPixelFormat.getFormat();
		}
		if (calcFormat == Format.UNCOMPRESSED) {
			calcFormat.setName(ddsPixelFormat.getUncompressedName());
		}
		return calcFormat;
	}

	private long calcMaxImageIndex(long mipMapCount) {
		//DX10
		if (ddsHeaderDX10 != null) {
			mipMapCount = mipMapCount * ddsHeaderDX10.getArraySize();
		}
		return mipMapCount;
	}

	public long getCaps() {
		return caps;
	}

	public long getCaps2() {
		return caps2;
	}

	public long getCaps3() {
		return caps3;
	}

	public long getCaps4() {
		return caps4;
	}
	
}
