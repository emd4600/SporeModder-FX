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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import javafx.scene.image.Image;
import sporemodder.file.dds.DDSPixelFormat;
import sporemodder.file.dds.DDSTexture;

public class RWRaster extends RWObject {
	
	public static final int TYPE_CODE = 0x20003;
	public static final int ALIGNMENT = 4;

	public static final int FLAG_CUBE_TEXTURE = 0x1000;
	
	public int textureFormat;
	public int textureFlags = 8;
	public int volumeDepth;
	/** Unused in the file, in the code it's the pointer to the IDirect3DBaseTexture9 object. */
	public int dxBaseTexture;
	public int width;
	public int height;
	public int field_10 = 8;
	public int mipmapLevels;
	public int field_14;
	public int field_18;
	public RWBaseResource textureData;

	public RWRaster(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		textureFormat = stream.readLEInt();
		textureFlags = stream.readLEUShort();
		volumeDepth = stream.readLEUShort();
		dxBaseTexture = stream.readLEInt();
		width = stream.readLEUShort();
		height = stream.readLEUShort();
		field_10 = stream.readByte();
		mipmapLevels = stream.readByte();
		stream.skip(2);
		field_14 = stream.readLEInt();
		field_18 = stream.readLEInt();
		textureData = (RWBaseResource) renderWare.get(stream.readLEInt());
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(textureFormat);
		stream.writeLEUShort(textureFlags);
		stream.writeLEUShort(volumeDepth);
		stream.writeLEInt(dxBaseTexture);
		stream.writeLEUShort(width);
		stream.writeLEUShort(height);
		stream.writeByte(field_10);
		stream.writeByte(mipmapLevels);
		stream.writeShort(0);
		stream.writeLEInt(field_14);
		stream.writeLEInt(field_18);
		stream.writeLEInt(renderWare.indexOf(textureData));
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
	
	@Override
	public int getAlignment() {
		return ALIGNMENT;
	}

	/**
	 * Returns a DDSTexture with all the information of this RenderWare Raster, including the buffer data.
	 * @return
	 */
	public DDSTexture toDDSTexture() {
		boolean isCube = (textureFlags & FLAG_CUBE_TEXTURE) != 0;
		return new DDSTexture(width, height, mipmapLevels, textureFormat, textureData.data, isCube);
	}
	
	/**
	 * Copies information from the given DDS texture into this raster. This does not include the buffer data.
	 * @param texture
	 */
	public void fromDDSTexture(DDSTexture texture) {
		textureFormat = texture.getFormat().getFourCC();
		width = (int) texture.getWidth();
		height = (int) texture.getHeight();
		mipmapLevels = (int) texture.getMipmapCount();
		
		if (textureFormat == 0) {
			long flags = texture.getHeader().getPixelFormat().getFlags();
			if ((flags & DDSPixelFormat.RGB) != 0) {
				if ((flags & DDSPixelFormat.ALPHAPIXELS) != 0 || (flags & DDSPixelFormat.ALPHA) != 0) {
					textureFormat = DDSPixelFormat.D3DFMT_A8R8G8B8;
				}
				else {
					textureFormat = DDSPixelFormat.D3DFMT_R8G8B8;
				}
			}
			else if ((flags & DDSPixelFormat.ALPHAPIXELS) != 0 || (flags & DDSPixelFormat.ALPHA) != 0) {
				textureFormat = DDSPixelFormat.D3DFMT_A8;
			} 
		}

		if (texture.isCubeMap()) {
			textureFlags |= FLAG_CUBE_TEXTURE;
		}
	}
	
	public Image toJavaFX() throws IOException {
		return toDDSTexture().toJavaFX();
	}
}
