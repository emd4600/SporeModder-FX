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
package sporemodder.file.bitmaps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import emord.filestructures.FileStream;
import emord.filestructures.FixedMemoryStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import sporemodder.FileManager;
import sporemodder.HashManager;

public class BitmapImage {
	
	public static final int TYPE_1BIT = 0x03E421E9;		// channelCount = 0
	public static final int TYPE_8BIT = 0x03E421EC;		// channelCount = 1
	public static final int TYPE_32BIT = 0x03E421ED;	// channelCount = 2
	public static final int TYPE_48BIT = 0x03E421EF;	// channelCount = 3
	
	public int type;
	public int width;
	public int height;
	public int channelCount;
	public Image image;
	public int unknown;  // Not all of them have it
	
	public BitmapImage() {
	}
	
	public BitmapImage(int type, Image image) {
		if (type != TYPE_1BIT && type != TYPE_8BIT && type != TYPE_32BIT && type != TYPE_48BIT) {
			throw new IllegalArgumentException("Unsupported type: only 1bit, 8bit, 32bit and 48bit is accepted");
		}
		this.type = type;
		this.image = image;
		this.width = (int) Math.round(image.getWidth());
		this.height = (int) Math.round(image.getHeight());
		
		if (type == TYPE_1BIT && (width % 8 != 0 || height % 8 != 0)) {
			throw new IllegalArgumentException("Unsupported dimensions: 1bit maps must be multiples of 8");
		}
		
		switch(type) {
		case TYPE_1BIT:	
			channelCount = 0; 
			unknown = width / 8;
			break;
		case TYPE_8BIT: channelCount = 1; break;
		case TYPE_32BIT: channelCount = 2; break;
		case TYPE_48BIT: channelCount = 3; break;
		}
	}
	
	public int getType() {
		return type;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public Image getImage() {
		return image;
	}

	private void processImage(StreamReader stream, PixelWriter dst) throws IOException {
		if (type == TYPE_1BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; i += 8) {
					int data = stream.readUByte();
					
					for (int k = 0; k < 8; ++k) {
						dst.setColor(i + k, j, Color.gray((data & 1) != 0 ? 1.0 : 0.0));
						data >>= 1;
					}
				}
			}
		}
		else if (type == TYPE_8BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					int data = stream.readUByte();
					dst.setColor(i, j, Color.gray(data / 255.0));
				}
			}
		}
		else if (type == TYPE_32BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					double b = stream.readUByte() / 255.0;
					double g = stream.readUByte() / 255.0;
					double r = stream.readUByte() / 255.0;
					double a = stream.readUByte() / 255.0;
					dst.setColor(i, j, Color.color(r, g, b, a));
				}
			}
		}
		else if (type == TYPE_48BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					Color color = Color.color(
							stream.readLEUShort() / 65535.0,
							stream.readLEUShort() / 65535.0,
							stream.readLEUShort() / 65535.0);
					dst.setColor(i, j, color);
				}
			}
		}
	}
	
	private int getDataSize() {
		switch (type)
		{
		case TYPE_1BIT:		return width * height / 8;
		case TYPE_8BIT:		return width * height;
		case TYPE_32BIT:	return width * height * 4;
		case TYPE_48BIT:	return width * height * 6;
		}
		return -1;
	}
	
	private void writeImageData(StreamWriter stream, PixelReader src) throws IOException {
		if (type == TYPE_1BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; i += 8) {
					int data = 0;
					for (int k = 7; k >= 0; --k) {
						boolean b = src.getColor(i + k, j).getRed() > 0.5;
						if (b) data |= 1;
						if (k != 0) data <<= 1;
					}
					stream.writeUByte(data);
				}
			}
		}
		else if (type == TYPE_8BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					stream.writeUByte((int) Math.round((src.getColor(i, j).getRed() * 255.0)));
				}
			}
		}
		else if (type == TYPE_32BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					Color c = src.getColor(i, j);
					stream.writeUByte((int) Math.round((c.getBlue() * 255.0)));
					stream.writeUByte((int) Math.round((c.getGreen() * 255.0)));
					stream.writeUByte((int) Math.round((c.getRed() * 255.0)));
					stream.writeUByte((int) Math.round((c.getOpacity() * 255.0)));
				}
			}
		}
		else if (type == TYPE_48BIT) {
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					Color c = src.getColor(i, j);
					stream.writeLEUShort((int) Math.round((c.getRed() * 65535.0)));
					stream.writeLEUShort((int) Math.round((c.getGreen() * 65535.0)));
					stream.writeLEUShort((int) Math.round((c.getBlue() * 65535.0)));
				}
			}
		}
	}
	
	public void read(StreamReader stream) throws IOException {
		// version, 0, the only Spore supports
		stream.readInt();
		width = stream.readInt();
		height = stream.readInt();
		channelCount = stream.readInt();
		
		int bufferSize = stream.readInt();
		byte[] data = new byte[bufferSize];
		stream.read(data);
		
		if (type == TYPE_1BIT) {
			unknown = stream.readInt();
		}
		
		try (FixedMemoryStream dataStream = new FixedMemoryStream(data)) {
			image = new WritableImage(width, height);
			processImage(dataStream, ((WritableImage)image).getPixelWriter());
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(0);
		stream.writeInt(width);
		stream.writeInt(height);
		stream.writeInt(channelCount);
		
		try (FixedMemoryStream dataStream = new FixedMemoryStream(getDataSize())) {
			dataStream.seek(0);
			writeImageData(dataStream, image.getPixelReader());
			
			stream.writeUInt(dataStream.length());
			stream.write(dataStream.toByteArray());
		}
		
		if (type == TYPE_1BIT) {
			stream.writeInt(unknown);
		}
	}
	
	public static BitmapImage readImage(File file) throws FileNotFoundException, IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			BitmapImage image = new BitmapImage();
			
			image.type = HashManager.get().getTypeHash(FileManager.getExtension(file.getName()));
			
			if (image.type != TYPE_1BIT && image.type != TYPE_8BIT && image.type != TYPE_32BIT && image.type != TYPE_48BIT) {
				throw new IOException("Unsupported type: only 1bit, 8bit, 32bit and 48bit is accepted");
			}
			
			image.read(stream);
			return image;
		}
	}

}
