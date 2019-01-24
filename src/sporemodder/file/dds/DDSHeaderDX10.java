/*
 * DDSHeaderDX10.java - This file is part of Java DDS ImageIO Plugin
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
 * TODO Write File Description for DDSHeaderDX10.java
 */

package sporemodder.file.dds;

import sporemodder.file.dds.DDSPixelFormat.Format;


public class DDSHeaderDX10 {
	enum DxgiFormat { 
		DXGI_FORMAT_UNKNOWN(0),
		DXGI_FORMAT_R32G32B32A32_TYPELESS(1),
		DXGI_FORMAT_R32G32B32A32_FLOAT(2),
		DXGI_FORMAT_R32G32B32A32_UINT(3),
		DXGI_FORMAT_R32G32B32A32_SINT(4),
		DXGI_FORMAT_R32G32B32_TYPELESS(5),
		DXGI_FORMAT_R32G32B32_FLOAT(6),
		DXGI_FORMAT_R32G32B32_UINT(7),
		DXGI_FORMAT_R32G32B32_SINT(8),
		DXGI_FORMAT_R16G16B16A16_TYPELESS(9),
		DXGI_FORMAT_R16G16B16A16_FLOAT(10),
		DXGI_FORMAT_R16G16B16A16_UNORM(11),
		DXGI_FORMAT_R16G16B16A16_UINT(12),
		DXGI_FORMAT_R16G16B16A16_SNORM(13),
		DXGI_FORMAT_R16G16B16A16_SINT(14),
		DXGI_FORMAT_R32G32_TYPELESS(15),
		DXGI_FORMAT_R32G32_FLOAT(16),
		DXGI_FORMAT_R32G32_UINT(17),
		DXGI_FORMAT_R32G32_SINT(18),
		DXGI_FORMAT_R32G8X24_TYPELESS(19),
		DXGI_FORMAT_D32_FLOAT_S8X24_UINT(20),
		DXGI_FORMAT_R32_FLOAT_X8X24_TYPELESS(21),
		DXGI_FORMAT_X32_TYPELESS_G8X24_UINT(22),
		DXGI_FORMAT_R10G10B10A2_TYPELESS(23, Format.UNCOMPRESSED),
		DXGI_FORMAT_R10G10B10A2_UNORM(24, Format.UNCOMPRESSED),
		DXGI_FORMAT_R10G10B10A2_UINT(25, Format.UNCOMPRESSED),
		DXGI_FORMAT_R11G11B10_FLOAT(26),
		DXGI_FORMAT_R8G8B8A8_TYPELESS(27, Format.UNCOMPRESSED),
		DXGI_FORMAT_R8G8B8A8_UNORM(28, Format.UNCOMPRESSED),
		DXGI_FORMAT_R8G8B8A8_UNORM_SRGB(29, Format.UNCOMPRESSED),
		DXGI_FORMAT_R8G8B8A8_UINT(30, Format.UNCOMPRESSED),
		DXGI_FORMAT_R8G8B8A8_SNORM(31, Format.UNCOMPRESSED),
		DXGI_FORMAT_R8G8B8A8_SINT(32, Format.UNCOMPRESSED),
		DXGI_FORMAT_R16G16_TYPELESS(33),
		DXGI_FORMAT_R16G16_FLOAT(34),
		DXGI_FORMAT_R16G16_UNORM(35),
		DXGI_FORMAT_R16G16_UINT(36),
		DXGI_FORMAT_R16G16_SNORM(37),
		DXGI_FORMAT_R16G16_SINT(38),
		DXGI_FORMAT_R32_TYPELESS(39),
		DXGI_FORMAT_D32_FLOAT(40),
		DXGI_FORMAT_R32_FLOAT(41),
		DXGI_FORMAT_R32_UINT(42),
		DXGI_FORMAT_R32_SINT(43),
		DXGI_FORMAT_R24G8_TYPELESS(44),
		DXGI_FORMAT_D24_UNORM_S8_UINT(45),
		DXGI_FORMAT_R24_UNORM_X8_TYPELESS(46),
		DXGI_FORMAT_X24_TYPELESS_G8_UINT(47),
		DXGI_FORMAT_R8G8_TYPELESS(48),
		DXGI_FORMAT_R8G8_UNORM(49),
		DXGI_FORMAT_R8G8_UINT(50),
		DXGI_FORMAT_R8G8_SNORM(51),
		DXGI_FORMAT_R8G8_SINT(52),
		DXGI_FORMAT_R16_TYPELESS(53),
		DXGI_FORMAT_R16_FLOAT(54),
		DXGI_FORMAT_D16_UNORM(55),
		DXGI_FORMAT_R16_UNORM(56),
		DXGI_FORMAT_R16_UINT(57),
		DXGI_FORMAT_R16_SNORM(58),
		DXGI_FORMAT_R16_SINT(59),
		DXGI_FORMAT_R8_TYPELESS(60),
		DXGI_FORMAT_R8_UNORM(61),
		DXGI_FORMAT_R8_UINT(62),
		DXGI_FORMAT_R8_SNORM(63),
		DXGI_FORMAT_R8_SINT(64),
		DXGI_FORMAT_A8_UNORM(65),
		DXGI_FORMAT_R1_UNORM(66),
		DXGI_FORMAT_R9G9B9E5_SHAREDEXP(67),
		DXGI_FORMAT_R8G8_B8G8_UNORM(68, Format.RGBG),
		DXGI_FORMAT_G8R8_G8B8_UNORM(69, Format.GRGB),
		DXGI_FORMAT_BC1_TYPELESS(70, Format.DXT1),
		DXGI_FORMAT_BC1_UNORM(71, Format.DXT1),
		DXGI_FORMAT_BC1_UNORM_SRGB(72, Format.DXT1),
		DXGI_FORMAT_BC2_TYPELESS(73, Format.DXT3),
		DXGI_FORMAT_BC2_UNORM(74, Format.DXT3),
		DXGI_FORMAT_BC2_UNORM_SRGB(75, Format.DXT3),
		DXGI_FORMAT_BC3_TYPELESS(7675, Format.DXT5),
		DXGI_FORMAT_BC3_UNORM(77, Format.DXT5),
		DXGI_FORMAT_BC3_UNORM_SRGB(78, Format.DXT5),
		DXGI_FORMAT_BC4_TYPELESS(79, Format.ATI1),
		DXGI_FORMAT_BC4_UNORM(80, Format.ATI1),
		DXGI_FORMAT_BC4_SNORM(81, Format.ATI1),
		DXGI_FORMAT_BC5_TYPELESS(82, Format.ATI2),
		DXGI_FORMAT_BC5_UNORM(83, Format.ATI2),
		DXGI_FORMAT_BC5_SNORM(84, Format.ATI2),
		DXGI_FORMAT_B5G6R5_UNORM(85, Format.UNCOMPRESSED),
		DXGI_FORMAT_B5G5R5A1_UNORM(86, Format.UNCOMPRESSED),
		DXGI_FORMAT_B8G8R8A8_UNORM(87, Format.UNCOMPRESSED),
		DXGI_FORMAT_B8G8R8X8_UNORM(88, Format.UNCOMPRESSED),
		DXGI_FORMAT_R10G10B10_XR_BIAS_A2_UNORM(89),
		DXGI_FORMAT_B8G8R8A8_TYPELESS(90, Format.UNCOMPRESSED),
		DXGI_FORMAT_B8G8R8A8_UNORM_SRGB(91, Format.UNCOMPRESSED),
		DXGI_FORMAT_B8G8R8X8_TYPELESS(92, Format.UNCOMPRESSED),
		DXGI_FORMAT_B8G8R8X8_UNORM_SRGB(93, Format.UNCOMPRESSED),
		DXGI_FORMAT_BC6H_TYPELESS(94, Format.NOT_SUPPORTED),
		DXGI_FORMAT_BC6H_UF16(95, Format.NOT_SUPPORTED),
		DXGI_FORMAT_BC6H_SF16(96, Format.NOT_SUPPORTED),
		DXGI_FORMAT_BC7_TYPELESS(97, Format.NOT_SUPPORTED),
		DXGI_FORMAT_BC7_UNORM(98, Format.NOT_SUPPORTED),
		DXGI_FORMAT_BC7_UNORM_SRGB(99, Format.NOT_SUPPORTED),
		DXGI_FORMAT_AYUV(100, Format.NOT_SUPPORTED),
		DXGI_FORMAT_Y410(101, Format.NOT_SUPPORTED),
		DXGI_FORMAT_Y416(102, Format.NOT_SUPPORTED),
		DXGI_FORMAT_NV12(103, Format.NOT_SUPPORTED),
		DXGI_FORMAT_P010(104, Format.NOT_SUPPORTED),
		DXGI_FORMAT_P016(105, Format.NOT_SUPPORTED),
		DXGI_FORMAT_420_OPAQUE(106, Format.NOT_SUPPORTED),
		DXGI_FORMAT_YUY2(107, Format.YUY2),
		DXGI_FORMAT_Y210(108, Format.NOT_SUPPORTED),
		DXGI_FORMAT_Y216(109, Format.NOT_SUPPORTED),
		DXGI_FORMAT_NV11(110, Format.NOT_SUPPORTED),
		DXGI_FORMAT_AI44(111, Format.NOT_SUPPORTED),
		DXGI_FORMAT_IA44(112, Format.NOT_SUPPORTED),
		DXGI_FORMAT_P8(113, Format.NOT_SUPPORTED),
		DXGI_FORMAT_A8P8(114, Format.NOT_SUPPORTED),
		DXGI_FORMAT_B4G4R4A4_UNORM(115);
		//DXGI_FORMAT_FORCE_UINT(0xffffffffUL);

		private final int value;
		private final Format format;

		private DxgiFormat(int value) {
			this.value = value;
			this.format = Format.UNCOMPRESSED;
		}

		private DxgiFormat(int value, Format format) {
			this.value = value;
			this.format = format;
		}

		public Format getFormat() {
			return format;
		}
	};

	enum D3d10ResourceDimension { 
		D3D10_RESOURCE_DIMENSION_UNKNOWN(0),
		D3D10_RESOURCE_DIMENSION_BUFFER(1),
		D3D10_RESOURCE_DIMENSION_TEXTURE1D(2),
		D3D10_RESOURCE_DIMENSION_TEXTURE2D(3),
		D3D10_RESOURCE_DIMENSION_TEXTURE3D(4);

		private final int value;

		private D3d10ResourceDimension(int value) {
			this.value = value;
		}
  }

	private final long dxgiFormat;
	private final long resourceDimension;
	private final long miscFlag;
	private final long arraySize;
	private final long miscFlags2;
	private final Format format;

	public DDSHeaderDX10(long dxgiFormat, long resourceDimension, long miscFlag, long arraySize, long miscFlags2) {
		this.dxgiFormat = dxgiFormat;
		this.resourceDimension = resourceDimension;
		this.miscFlag = miscFlag;
		this.arraySize = arraySize;
		this.miscFlags2 = miscFlags2;
		this.format = calcFormat();
	}

	public Format getFormat() {
		return format;
	}

	public void printValues(int nSpace) {
		String sSpace = "";
		for (int i = 0; i < nSpace; i++){
			sSpace = sSpace + "	";
		}
		System.out.println(sSpace + "DX10 Header: ");
		System.out.println(sSpace + "	dxgiFormat: " + dxgiFormat + " (" + DxgiFormat.values()[(int)dxgiFormat].name() + ")");
		System.out.println(sSpace + "	resourceDimension: " + resourceDimension + " (" + D3d10ResourceDimension.values()[(int)resourceDimension].name() + ")");
		System.out.println(sSpace + "	miscFlag: " + miscFlag);
		System.out.println(sSpace + "	arraySize: " + arraySize);
		System.out.println(sSpace + "	reserved: " + miscFlags2);
	}

	public long getArraySize() {
		return arraySize;
	}

	private Format calcFormat() {
		if (dxgiFormat < DxgiFormat.values().length) {
			return DxgiFormat.values()[(int)dxgiFormat].getFormat();
		} else {
			return Format.NOT_SUPPORTED;
		}
	}
}
