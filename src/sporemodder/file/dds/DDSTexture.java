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
package sporemodder.file.dds;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import sporemodder.file.dds.DDSPixelFormat.Format;

public class DDSTexture {
	private static final int HEADER_SIZE = 128;

	private static final int DEFAULT_FLAGS = DDSHeader.LINEARSIZE | DDSHeader.MIPMAPCOUNT | DDSHeader.PIXELFORMAT
			| DDSHeader.WIDTH | DDSHeader.HEIGHT | DDSHeader.CAPS;
	private static final int DEFAULT_SIZE = 0x7C;
	private static final int DEFAULT_PIXELFORMAT_SIZE = 32;

	private byte[] data;

	private DDSHeader header;

	public DDSTexture(long width, long height, int mipmapCount, int textureFormat, byte[] data) {

		long pitchOrLinearSize;
		boolean isCompressed;
		long blockSize = 0;
		
		if (textureFormat == DDSPixelFormat.Format.DXT1.getFourCC()) {
			blockSize = 8;
		} 
		else if (textureFormat == DDSPixelFormat.Format.DXT2.getFourCC() ||  textureFormat == DDSPixelFormat.Format.DXT3.getFourCC()
				|| textureFormat == DDSPixelFormat.Format.DXT4.getFourCC() || textureFormat == DDSPixelFormat.Format.DXT5.getFourCC()) {
			blockSize = 16;
		}
		
		long depth = 0;
		long pfFlags = 0;
		long rgbBitCount = 32;
		long rMask = 0x00FF0000;
		long gMask = 0x0000FF00;
		long bMask = 0x000000FF;
		long aMask = 0xFF000000;
		long caps = 0;
		long caps2 = 0;
		long caps3 = 0;
		long caps4 = 0;
		
		if (textureFormat == DDSPixelFormat.D3DFMT_A8) {
			pfFlags |= DDSPixelFormat.ALPHAPIXELS;
			rgbBitCount = 8;
			aMask = 0x000000FF;
		}
		else if (textureFormat == DDSPixelFormat.D3DFMT_R8G8B8) {
			pfFlags |= DDSPixelFormat.RGB;
			rgbBitCount = 24;
		}
		else if (textureFormat == DDSPixelFormat.D3DFMT_A8R8G8B8) {
			pfFlags |= DDSPixelFormat.RGB | DDSPixelFormat.ALPHAPIXELS;
			rgbBitCount = 32;
		}
		
		if (blockSize != 0) {
			// Microsoft docs say it's like this:
			// pitchOrLinearSize = Math.max(1, (width+3) / 4) * blockSize;
			// But that doesn't work on Gimp. Gimp and Photoshop do this:
			pitchOrLinearSize = Math.max(1, (width+3) / 4) * Math.max(1, (height+3) / 4) * blockSize;
			isCompressed = true;
		} 
		else {
			// Microsoft documentation says we should use this
			// but it doesn't work on Gimp
			// pitchOrLinearSize = ( width * rgbBitCount + 7 ) / 8;
			pitchOrLinearSize = width * height * rgbBitCount / 8;
			isCompressed = false;
		}
		
		if (isCompressed) pfFlags |= DDSPixelFormat.FOURCC;
		long fourCC = isCompressed ? textureFormat : 0;

		DDSPixelFormat pixelFormat = new DDSPixelFormat(DEFAULT_PIXELFORMAT_SIZE, pfFlags, fourCC, rgbBitCount, rMask, gMask, bMask, aMask);
		header = new DDSHeader(DEFAULT_SIZE, DEFAULT_FLAGS, height, width, pitchOrLinearSize, depth, mipmapCount, pixelFormat, caps, caps2, caps3, caps4, null);

		this.data = data;
	}

	public DDSTexture() {};

	public long getWidth() {
		return header.getWidth(0);
	}
	public long getHeight() {
		return header.getHeight(0);
	}
	public long getMipmapCount() {
		return header.getMipMapCount();
	}
	public Format getFormat() {
		return header.getPixelFormat().getFormat();
	}
	public byte[] getData() {
		return data;
	}

	public void readHeader(StreamReader stream) throws IOException {
		stream.readLEInt();  // magic

		long size = stream.readLEUInt();
		long flags = stream.readLEUInt();
		long height = stream.readLEUInt();
		long width = stream.readLEUInt();
		long pitchOrLinearSize = stream.readLEUInt();
		long depth = stream.readLEUInt();
		long mipmapCount = stream.readLEUInt();
		stream.skip(44);

		int pixelFormatSize = stream.readLEInt();  // 32
		long pfFlags = stream.readLEUInt();
		long fourCC = stream.readLEUInt();
		long rgbBitCount = stream.readLEUInt();
		long rMask = stream.readLEUInt();
		long gMask = stream.readLEUInt();
		long bMask = stream.readLEUInt();
		long aMask = stream.readLEUInt();

		long caps = stream.readLEUInt();
		long caps2 = stream.readLEUInt();
		long caps3 = stream.readLEUInt();
		long caps4 = stream.readLEUInt();

		stream.readLEInt();

		// A special case for Spore
		if (fourCC == 0 && pfFlags == 0x41) {
			fourCC = 0x15;
		}

		DDSPixelFormat pixelFormat = new DDSPixelFormat(pixelFormatSize, pfFlags, fourCC, rgbBitCount, rMask, gMask, bMask, aMask);
		header = new DDSHeader(size, flags, height, width, pitchOrLinearSize, depth, mipmapCount, pixelFormat, caps, caps2, caps3, caps4, null);
	}

	public void writeHeader(StreamWriter stream) throws IOException {
		stream.writeLEUInt(0x20534444L);
		stream.writeLEUInt(header.getSize());
		stream.writeLEUInt(header.getFlags());
		stream.writeLEUInt(header.getHeight(0));
		stream.writeLEUInt(header.getWidth(0));
		stream.writeLEUInt(header.getPitchOrLinearSize());
		stream.writeLEUInt(header.getDepth());
		stream.writeLEUInt(header.getMipMapCount());
		stream.writePadding(44);

		DDSPixelFormat pixelFormat = header.getPixelFormat();
		stream.writeLEUInt(pixelFormat.getSize());
		stream.writeLEUInt(pixelFormat.getFlags());
		//stream.writeLEUInt(pixelFormat.getFormat() == Format.UNCOMPRESSED ? 0x15 : pixelFormat.getFourCC());
		stream.writeLEUInt(pixelFormat.getFourCC());
		stream.writeLEUInt(pixelFormat.getRgbBitCount());
		stream.writeLEUInt(pixelFormat.getMaskRed());
		stream.writeLEUInt(pixelFormat.getMaskGreen());
		stream.writeLEUInt(pixelFormat.getMaskBlue());
		stream.writeLEUInt(pixelFormat.getMaskAlpha());

		stream.writeLEUInt(header.getCaps());
		stream.writeLEUInt(header.getCaps2());
		stream.writeLEUInt(header.getCaps3());
		stream.writeLEUInt(header.getCaps4());
		stream.writePadding(4);
	}
	
	public DDSHeader getHeader() {
		return header;
	}

	public void read(StreamReader stream) throws IOException {
		readHeader(stream);
		data = new byte[(int) (stream.length() - HEADER_SIZE)];
		stream.read(data);
	}
	
	public void read(File file) throws IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			read(stream);
		}
	}

	public void write(StreamWriter stream) throws IOException {
		writeHeader(stream);
		stream.write(data);
	}
	
	public void write(File file) throws IOException {
		try (FileStream stream = new FileStream(file, "rw")) {
			write(stream);
		}
	}

	public static BufferedImage toBufferedImage(File file) throws IOException {
		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
			DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
			reader.setInput(input);
			return reader.read(0);
		}
	}

	public BufferedImage toBufferedImage() throws IOException {
		MemoryStream stream = new MemoryStream(HEADER_SIZE + data.length);
		write(stream);
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(stream.toByteArray()))) {
			DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
			reader.setInput(input);
			return reader.read(0);
		}
	}

	public byte[] getMipmapData(int imageIndex) throws IOException {
		if (imageIndex >= header.getMipMapCount()) {
			throw new IllegalArgumentException("MipMap index not found.");
		}

		int skipsBytes = 0;
		for (int i = 0; i < imageIndex; i++){
			skipsBytes = (int) (skipsBytes + skipImage( i));
		}

		int width = (int) header.getWidth(imageIndex);
		int height = (int) header.getHeight(imageIndex);

		// is it 8 ?
		return readAll(skipsBytes, header.getFormat().fourCC, 8, width, height);
	}

	private long skipImage(int index) {
		long skipsBytes;
		if (header.getFormat().fourCC != Format.UNCOMPRESSED.fourCC) {
			int fixedHeight = fixSize((int) header.getHeight(index));
			int fixedWidth = fixSize((int) header.getWidth(index));
			
			if (header.getFormat().fourCC == Format.DXT1.fourCC 
					|| header.getFormat().fourCC == Format.ATI1.fourCC) { // DXT1 & ATI (8 bytes)
				long bytes = (8 * (Math.max(1, (fixedHeight / 4) * Math.max(1, fixedWidth / 4))));
				bytes = Math.max(bytes, 8);
				skipsBytes = bytes;
			} else { // DXT3 & DXT5 & ATI2 (16 bytes)
				long bytes = (16 * (Math.max(1, (fixedHeight / 4) * Math.max(1, fixedWidth / 4))));
				bytes = Math.max(bytes, 16);
				skipsBytes = bytes;
			}
		} else { // Uncompressed
			// long bytes = (ddsHeader.getPixelFormat().getRgbBitCount() / 8) *
			// ddsHeader.getHeight(index) * ddsHeader.getWidth(index);
			// skipsBytes = bytes;
			skipsBytes = header.getHeight(index) * header.getWidth(index);
		}
		return skipsBytes;
	}

	/**
	 * Fix size to be compatible with "bad size" ex.: 25x25 (instead of 24x24)
	 * @param size width or height
	 * @return fixed width or height 
	 */
	public int fixSize(int size) {
		while(size % 4 != 0) {
			size++;
		}
		return size;
	}

	private byte[] readAll(int offset, int format, int bitCount, int width, int height) throws IOException {
		byte[] bytes;
		if (format == Format.UNCOMPRESSED.fourCC) {
			int byteCount = (int)(bitCount/8);
			bytes = new byte[(height*width*byteCount)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else if (format == Format.RGBG.fourCC || format == Format.GRGB.fourCC ||
				format == Format.UYVY.fourCC || format == Format.UYVY.fourCC) {
			bytes = new byte[(height*width*2)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else if (format == Format.DXT1.fourCC || format == Format.ATI1.fourCC) {
			width = fixSize(width);
			height = fixSize(height);
			bytes = new byte[8*Math.max(1, width/4)*Math.max(1, height/4)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else if (format == Format.DXT3.fourCC || format == Format.DXT5.fourCC || format == Format.ATI2.fourCC) {
			width = fixSize(width);
			height = fixSize(height);
			bytes = new byte[16*(int)Math.max(1, width/4)*(int)Math.max(1, height/4)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else {
			throw new IOException("0x" + Integer.toHexString(format) + " is not a supported format!");
		}
		return bytes;
	}
	
	public Image toJavaFX() throws IOException {
		return SwingFXUtils.toFXImage(toBufferedImage(), null);
	}
}
