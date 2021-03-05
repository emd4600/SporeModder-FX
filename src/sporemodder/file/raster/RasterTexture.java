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
package sporemodder.file.raster;

import java.io.File;
import java.io.IOException;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.dds.DDSTexture;

public class RasterTexture {

	public int width;
	public int height;
	public int pixelWidth = 8;
	public int mipmapCount;
	public int textureFormat;
	private byte[][] data;
	
	public boolean read(StreamReader stream) throws IOException {
		if (stream.readLEInt() != 1) {
			return false;
		}
		
		width = stream.readLEInt();
		height = stream.readLEInt();
		mipmapCount = stream.readLEInt();
		pixelWidth = stream.readLEInt();
		textureFormat = stream.readLEInt();
		
		//TODO adapt this for when pixelWidth != 8 ?
		data = new byte[mipmapCount][];
		for (int i = 0; i < mipmapCount; i++) {
			data[i] = new byte[stream.readLEInt()];
			stream.read(data[i]);
		}
		
		return true;
	}
	
	public void write(StreamWriter out) throws IOException {
		
		out.writeLEInt(1);
		out.writeLEInt(width);
		out.writeLEInt(height);
		out.writeLEInt(mipmapCount);
		out.writeLEInt(pixelWidth);
		out.writeLEInt(textureFormat);
		
		for (byte[] d : data)
		{
			out.writeLEInt(d.length);
			out.write(d);
		}
	}
	
	/**
	 * Returns a DDSTexture with all the information of this Raster, ready to be used.
	 * @return
	 */
	public DDSTexture toDDSTexture() {
		int size = 0;
		for (byte[] d : data) size += d.length;
		
		byte[] newData = new byte[size];
		
		int pos = 0;
		for (byte[] d : data) {
			System.arraycopy(d, 0, newData, pos, d.length);
			pos += d.length;
		}
		
		return new DDSTexture(width, height, mipmapCount, textureFormat, newData);
	}
	
	/**
	 * Copies the necessary information from the given DDS texture into this raster.
	 * @param texture
	 * @throws IOException 
	 */
	public void fromDDSTexture(DDSTexture texture) throws IOException {
		textureFormat = texture.getFormat().getFourCC();
		width = (int) texture.getWidth();
		height = (int) texture.getHeight();
		mipmapCount = (int) texture.getMipmapCount();
		pixelWidth = 8;
		
		data = new byte[mipmapCount][];
		for (int i = 0; i < mipmapCount; ++i) {
			data[i] = texture.getMipmapData(i);
		}
	}
	
	public static DDSTexture textureFromFile(File input) throws IOException {
		try (FileStream stream = new FileStream(input, "r")) {
			RasterTexture texture = new RasterTexture();
			texture.read(stream);
			return texture.toDDSTexture();
		}
	}
	
	public static void textureFromFile(File input, File output) throws IOException {
		try (FileStream stream = new FileStream(input, "r");
				FileStream outputStream = new FileStream(output, "rw")) {
			RasterTexture texture = new RasterTexture();
			texture.read(stream);
			texture.toDDSTexture().write(outputStream);
		}
	}
}
