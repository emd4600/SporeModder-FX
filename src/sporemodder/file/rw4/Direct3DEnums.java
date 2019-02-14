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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sporemodder.HashManager;
import sporemodder.util.ColorRGBA;

public class Direct3DEnums {
	public static interface D3DStateEnum {
		public int getId();
	}
	
	public static D3DStateEnum getStateEnumById(int id, Class<?> stateEnum) {
		try {
			if (stateEnum == null || !stateEnum.isEnum()) return null;
			Method valuesMethod = stateEnum.getDeclaredMethod("values");
			D3DStateEnum[] states = (D3DStateEnum[]) valuesMethod.invoke(null);
			for (D3DStateEnum state : states) {
				if (state.getId() == id) {
					return state;
				}
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public static Integer getStateValue(String text, Class<?> stateEnum) {
		try {
			if (stateEnum == null || !stateEnum.isEnum()) return null;
			Method valuesMethod = stateEnum.getDeclaredMethod("values");
			D3DStateEnum[] states = (D3DStateEnum[]) valuesMethod.invoke(null);
			for (D3DStateEnum state : states) {
				if (state.toString().equals(text)) {
					return state.getId();
				}
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static String getValueToString(Class<?> typeClass, int value) {
		if (typeClass == int.class) return Integer.toString(value);
		else if (typeClass == float.class) return HashManager.get().floatToString(Float.intBitsToFloat(value));
		else if (typeClass == ColorRGBA.class) return new ColorRGBA(value).toString();
		else {
			D3DStateEnum stateEnum = getStateEnumById(value, typeClass);
			if (stateEnum == null) {
				return Integer.toString(value);
			} else {
				return stateEnum.toString();
			}
		}
	}
	
	public static final int D3DUSAGE_RENDERTARGET = 0x00000001;
	public static final int D3DUSAGE_DEPTHSTENCIL = 0x00000002;
	public static final int D3DUSAGE_DYNAMIC = 0x00000200;
	public static final int D3DUSAGE_NONSECURE = 0x00800000;
	public static final int D3DUSAGE_AUTOGENMIPMAP = 0x00000400;
	public static final int D3DUSAGE_DMAP = 0x00004000;
	public static final int D3DUSAGE_QUERY_LEGACYBUMPMAP = 0x00008000;
	public static final int D3DUSAGE_QUERY_SRGBREAD = 0x00010000;
	public static final int D3DUSAGE_QUERY_FILTER = 0x00020000;
	public static final int D3DUSAGE_QUERY_SRGBWRITE = 0x00040000;
	public static final int D3DUSAGE_QUERY_POSTPIXELSHADER_BLENDING = 0x00080000;
	public static final int D3DUSAGE_QUERY_VERTEXTEXTURE = 0x00100000;
	public static final int D3DUSAGE_QUERY_WRAPANDMIP = 0x00200000;
	
	public static final int D3DUSAGE_WRITEONLY = 0x00000008;
	public static final int D3DUSAGE_SOFTWAREPROCESSING = 0x00000010;
	public static final int D3DUSAGE_DONOTCLIP = 0x00000020;
	public static final int D3DUSAGE_POINTS = 0x00000040;
	public static final int D3DUSAGE_RTPATCHES = 0x00000080;
	public static final int D3DUSAGE_NPATCHES = 0x00000100;
	
	public static final int D3DUSAGE_TEXTAPI = 0x10000000;
	public static final int D3DUSAGE_RESTRICTED_CONTENT = 0x00000800;
	public static final int D3DUSAGE_RESTRICT_SHARED_RESOURCE = 0x00002000;
	public static final int D3DUSAGE_RESTRICT_SHARED_RESOURCE_DRIVER = 0x00001000;

	// This enum is much bigger, but we are only interested in these values
	public static enum D3DFORMAT implements D3DStateEnum {
		D3DFMT_INDEX16(101),
		D3DFMT_INDEX32(102);
		int id;
		private D3DFORMAT(int id) { this.id = id; }
		public static D3DFORMAT getById(int id) { for (D3DFORMAT state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DDECLTYPE implements D3DStateEnum {
		D3DDECLTYPE_FLOAT1(0, 4*1),
		D3DDECLTYPE_FLOAT2(1, 4*2),
		D3DDECLTYPE_FLOAT3(2, 4*3),
		D3DDECLTYPE_FLOAT4(3, 4*4),
		D3DDECLTYPE_D3DCOLOR(4, 4),
		D3DDECLTYPE_UBYTE4(5, 4),
		D3DDECLTYPE_SHORT2(6, 4),
		D3DDECLTYPE_SHORT4(7, 8),
		D3DDECLTYPE_UBYTE4N(8, 4),
		D3DDECLTYPE_SHORT2N(9, 4),
		D3DDECLTYPE_SHORT4N(10, 8),
		D3DDECLTYPE_USHORT2N(11, 4),
		D3DDECLTYPE_USHORT4N(12, 8),
		D3DDECLTYPE_UDEC3(13, 4),
		D3DDECLTYPE_DEC3N(14, 4),
		D3DDECLTYPE_FLOAT16_2(15, 4),
		D3DDECLTYPE_FLOAT16_4(16, 8),
		D3DDECLTYPE_UNUSED(17, 0);
		int id;
		public int size;
		private D3DDECLTYPE(int id, int size) { 
			this.id = id; 
			this.size = size;
		}
		public static D3DDECLTYPE getById(int id) { for (D3DDECLTYPE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DDECLUSAGE implements D3DStateEnum {
		D3DDECLUSAGE_POSITION(0),
		D3DDECLUSAGE_BLENDWEIGHT(1),
		D3DDECLUSAGE_BLENDINDICES(2),
		D3DDECLUSAGE_NORMAL(3),
		D3DDECLUSAGE_PSIZE(4),
		D3DDECLUSAGE_TEXCOORD(5),
		D3DDECLUSAGE_TANGENT(6),
		D3DDECLUSAGE_BINORMAL(7),
		D3DDECLUSAGE_TESSFACTOR(8),
		D3DDECLUSAGE_POSITIONT(9),
		D3DDECLUSAGE_COLOR(10),
		D3DDECLUSAGE_FOG(11),
		D3DDECLUSAGE_DEPTH(12),
		D3DDECLUSAGE_SAMPLE(13);
		int id;
		private D3DDECLUSAGE(int id) { this.id = id; }
		public static D3DDECLUSAGE getById(int id) { for (D3DDECLUSAGE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum RWDECLUSAGE implements D3DStateEnum {
		POSITION(0),
		NORMAL(2),
		COLOR0(3),
		COLOR1(5),
		TEXCOORD0(6),
		TEXCOORD1(7),
		TEXCOORD2(8),
		TEXCOORD3(9),
		BLENDINDICES(14),
		BLENDWEIGHTS(15),
		POINTSIZE(16),
		POSITION2(17),
		NORMAL2(18),
		TANGENT(19),
		BINORMAL(20),
		FOG(21),
		BLENDINDICES2(22),
		BLENDWEIGHTS2(23);
		int id;
		private RWDECLUSAGE(int id) { this.id = id; }
		public static RWDECLUSAGE getById(int id) { for (RWDECLUSAGE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DDECLMETHOD implements D3DStateEnum {
		D3DDECLMETHOD_DEFAULT(0),
		D3DDECLMETHOD_PARTIALU(1),
		D3DDECLMETHOD_PARTIALV(2),
		D3DDECLMETHOD_CROSSUV(3),
		D3DDECLMETHOD_UV(4),
		D3DDECLMETHOD_LOOKUP(5),
		D3DDECLMETHOD_LOOKUPPRESAMPLED(6);
		int id;
		private D3DDECLMETHOD(int id) { this.id = id; }
		public static D3DDECLMETHOD getById(int id) { for (D3DDECLMETHOD state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DPRIMITIVETYPE implements D3DStateEnum {
		D3DPT_POINTLIST(1),
		D3DPT_LINELIST(2),
		D3DPT_LINESTRIP(3),
		D3DPT_TRIANGLELIST(4),
		D3DPT_TRIANGLESTRIP(5),
		D3DPT_TRIANGLEFAN(6);
		int id;
		private D3DPRIMITIVETYPE(int id) { this.id = id; }
		public static D3DPRIMITIVETYPE getById(int id) { for (D3DPRIMITIVETYPE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DBOOLEAN implements D3DStateEnum {
		FALSE(0),
		TRUE(1);
		int id;
		private D3DBOOLEAN(int id) { this.id = id; }
		public static D3DBOOLEAN getById(int id) { for (D3DBOOLEAN state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DZBUFFERTYPE implements D3DStateEnum {
		D3DZB_FALSE(0),
		D3DZB_TRUE(1),
		D3DZB_USEW(2),
		D3DZB_FORCE_DWORD(0x7fffffff);
		int id;
		private D3DZBUFFERTYPE(int id) { this.id = id; }
		public static D3DZBUFFERTYPE getById(int id) { for (D3DZBUFFERTYPE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DFILLMODE implements D3DStateEnum {
		D3DFILL_POINT(1),
		D3DFILL_WIREFRAME(2),
		D3DFILL_SOLID(3),
		D3DFILL_FORCE_DWORD(0x7fffffff);
		int id;
		private D3DFILLMODE(int id) { this.id = id; }
		public static D3DFILLMODE getById(int id) { for (D3DFILLMODE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DSHADEMODE implements D3DStateEnum {
		D3DSHADE_FLAT(1),
		D3DSHADE_GOURAUD(2),
		D3DSHADE_PHONG(3),
		D3DSHADE_FORCE_DWORD(0x7fffffff);
		int id;
		private D3DSHADEMODE(int id) { this.id = id; }
		public static D3DSHADEMODE getById(int id) { for (D3DSHADEMODE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DBLEND implements D3DStateEnum {
		D3DBLEND_ZERO(1),
		D3DBLEND_ONE(2),
		D3DBLEND_SRCCOLOR(3),
		D3DBLEND_INVSRCCOLOR(4),
		D3DBLEND_SRCALPHA(5),
		D3DBLEND_INVSRCALPHA(6),
		D3DBLEND_DESTALPHA(7),
		D3DBLEND_INVDESTALPHA(8),
		D3DBLEND_DESTCOLOR(9),
		D3DBLEND_INVDESTCOLOR(10),
		D3DBLEND_SRCALPHASAT(11),
		D3DBLEND_BOTHSRCALPHA(12),
		D3DBLEND_BOTHINVSRCALPHA(13),
		D3DBLEND_BLENDFACTOR(14),
		D3DBLEND_INVBLENDFACTOR(15),
		D3DBLEND_SRCCOLOR2(16),
		D3DBLEND_INVSRCCOLOR2(17),
		D3DBLEND_FORCE_DWORD(0x7fffffff);
		int id;
		private D3DBLEND(int id) { this.id = id; }
		public static D3DBLEND getById(int id) { for (D3DBLEND state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DCULL implements D3DStateEnum {
		D3DCULL_NONE         (1),
		D3DCULL_CW           (2),
		D3DCULL_CCW          (3),
		D3DCULL_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DCULL(int id) { this.id = id; }
		public static D3DCULL getById(int id) { for (D3DCULL state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DCMPFUNC implements D3DStateEnum {
		D3DCMP_NEVER(1),
		D3DCMP_LESS(2),
		D3DCMP_EQUAL(3),
		D3DCMP_LESSEQUAL(4),
		D3DCMP_GREATER(5),
		D3DCMP_NOTEQUAL(6),
		D3DCMP_GREATEREQUAL(7),
		D3DCMP_ALWAYS(8),
		D3DCMP_FORCE_DWORD(0x7fffffff);
		int id;
		private D3DCMPFUNC(int id) { this.id = id; }
		public static D3DCMPFUNC getById(int id) { for (D3DCMPFUNC state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DFOGMODE implements D3DStateEnum {
		D3DFOG_NONE         (0),
		D3DFOG_EXP          (1),
		D3DFOG_EXP2         (2),
		D3DFOG_LINEAR       (3),
		D3DFOG_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DFOGMODE(int id) { this.id = id; }
		public static D3DFOGMODE getById(int id) { for (D3DFOGMODE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DSTENCILOP implements D3DStateEnum {
		D3DSTENCILOP_KEEP         (1),
		D3DSTENCILOP_ZERO         (2),
		D3DSTENCILOP_REPLACE      (3),
		D3DSTENCILOP_INCRSAT      (4),
		D3DSTENCILOP_DECRSAT      (5),
		D3DSTENCILOP_INVERT       (6),
		D3DSTENCILOP_INCR         (7),
		D3DSTENCILOP_DECR         (8),
		D3DSTENCILOP_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DSTENCILOP(int id) { this.id = id; }
		public static D3DSTENCILOP getById(int id) { for (D3DSTENCILOP state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DWRAPCOORD implements D3DStateEnum {
		NONE(0),
		D3DWRAPCOORD_0(1),
		D3DWRAPCOORD_1(2),
		D3DWRAPCOORD_2(4),
		D3DWRAPCOORD_3(8);
		int id;
		private D3DWRAPCOORD(int id) { this.id = id; }
		public static D3DWRAPCOORD getById(int id) { for (D3DWRAPCOORD state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DMATERIALCOLORSOURCE implements D3DStateEnum {
		D3DMCS_MATERIAL     (0),
		D3DMCS_COLOR1       (1),
		D3DMCS_COLOR2       (2),
		D3DMCS_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DMATERIALCOLORSOURCE(int id) { this.id = id; }
		public static D3DMATERIALCOLORSOURCE getById(int id) { for (D3DMATERIALCOLORSOURCE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DVERTEXBLENDFLAGS implements D3DStateEnum {
		D3DVBF_DISABLE   (0),
		D3DVBF_1WEIGHTS  (1),
		D3DVBF_2WEIGHTS  (2),
		D3DVBF_3WEIGHTS  (3),
		D3DVBF_TWEENING  (255),
		D3DVBF_0WEIGHTS  (256);
		int id;
		private D3DVERTEXBLENDFLAGS(int id) { this.id = id; }
		public static D3DVERTEXBLENDFLAGS getById(int id) { for (D3DVERTEXBLENDFLAGS state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DPATCHEDGESTYLE implements D3DStateEnum {
		D3DPATCHEDGE_DISCRETE     (0),
		D3DPATCHEDGE_CONTINUOUS   (1),
		D3DPATCHEDGE_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DPATCHEDGESTYLE(int id) { this.id = id; }
		public static D3DPATCHEDGESTYLE getById(int id) { for (D3DPATCHEDGESTYLE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DDEBUGMONITORTOKENS implements D3DStateEnum {
		D3DDMT_ENABLE       (0),
		D3DDMT_DISABLE      (1),
		D3DDMT_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DDEBUGMONITORTOKENS(int id) { this.id = id; }
		public static D3DDEBUGMONITORTOKENS getById(int id) { for (D3DDEBUGMONITORTOKENS state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DBLENDOP implements D3DStateEnum {
		D3DBLENDOP_ADD          (1),
		D3DBLENDOP_SUBTRACT     (2),
		D3DBLENDOP_REVSUBTRACT  (3),
		D3DBLENDOP_MIN          (4),
		D3DBLENDOP_MAX          (5),
		D3DBLENDOP_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DBLENDOP(int id) { this.id = id; }
		public static D3DBLENDOP getById(int id) { for (D3DBLENDOP state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DDEGREETYPE implements D3DStateEnum {
		D3DDEGREE_LINEAR     (1),
		D3DDEGREE_QUADRATIC  (2),
		D3DDEGREE_CUBIC      (3),
		D3DDEGREE_QUINTIC    (5),
		D3DCULL_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DDEGREETYPE(int id) { this.id = id; }
		public static D3DDEGREETYPE getById(int id) { for (D3DDEGREETYPE state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DRenderStateType {
		D3DRS_ZENABLE                     (7, D3DZBUFFERTYPE.class),
		D3DRS_FILLMODE                    (8, D3DFILLMODE.class),
		D3DRS_SHADEMODE                   (9, D3DSHADEMODE.class),
		D3DRS_ZWRITEENABLE                (14, D3DBOOLEAN.class),
		D3DRS_ALPHATESTENABLE             (15, D3DBOOLEAN.class),
		D3DRS_LASTPIXEL                   (16, D3DBOOLEAN.class),
		D3DRS_SRCBLEND                    (19, D3DBLEND.class),
		D3DRS_DESTBLEND                   (20, D3DBLEND.class),
		D3DRS_CULLMODE                    (22, D3DCULL.class),
		D3DRS_ZFUNC                       (23, D3DCMPFUNC.class),
		D3DRS_ALPHAREF                    (24, int.class),  // from 0 to 0xff
		D3DRS_ALPHAFUNC                   (25, D3DCMPFUNC.class),
		D3DRS_DITHERENABLE                (26, D3DBOOLEAN.class),
		D3DRS_ALPHABLENDENABLE            (27, D3DBOOLEAN.class),
		D3DRS_FOGENABLE                   (28, D3DBOOLEAN.class),
		D3DRS_SPECULARENABLE              (29, D3DBOOLEAN.class),
		D3DRS_FOGCOLOR                    (34, ColorRGBA.class),
		D3DRS_FOGTABLEMODE                (35, D3DFOGMODE.class),
		D3DRS_FOGSTART                    (36, float.class),  // from 0 to 1
		D3DRS_FOGEND                      (37, float.class),  // from 0 to 1
		D3DRS_FOGDENSITY                  (38, float.class),  // from 0 to 1
		D3DRS_RANGEFOGENABLE              (48, D3DBOOLEAN.class),
		D3DRS_STENCILENABLE               (52, D3DBOOLEAN.class),
		D3DRS_STENCILFAIL                 (53, D3DSTENCILOP.class),
		D3DRS_STENCILZFAIL                (54, D3DSTENCILOP.class),
		D3DRS_STENCILPASS                 (55, D3DSTENCILOP.class),
		D3DRS_STENCILFUNC                 (56, D3DCMPFUNC.class),
		D3DRS_STENCILREF                  (57, int.class),
		D3DRS_STENCILMASK                 (58, int.class),
		D3DRS_STENCILWRITEMASK            (59, int.class),
		D3DRS_TEXTUREFACTOR               (60, ColorRGBA.class),
		D3DRS_WRAP0                       (128, D3DWRAPCOORD.class),
		D3DRS_WRAP1                       (129, D3DWRAPCOORD.class),
		D3DRS_WRAP2                       (130, D3DWRAPCOORD.class),
		D3DRS_WRAP3                       (131, D3DWRAPCOORD.class),
		D3DRS_WRAP4                       (132, D3DWRAPCOORD.class),
		D3DRS_WRAP5                       (133, D3DWRAPCOORD.class),
		D3DRS_WRAP6                       (134, D3DWRAPCOORD.class),
		D3DRS_WRAP7                       (135, D3DWRAPCOORD.class),
		D3DRS_CLIPPING                    (136, D3DBOOLEAN.class),
		D3DRS_LIGHTING                    (137, D3DBOOLEAN.class),
		D3DRS_AMBIENT                     (139, ColorRGBA.class),
		D3DRS_FOGVERTEXMODE               (140, D3DFOGMODE.class),
		D3DRS_COLORVERTEX                 (141, D3DBOOLEAN.class),
		D3DRS_LOCALVIEWER                 (142, D3DBOOLEAN.class),
		D3DRS_NORMALIZENORMALS            (143, D3DBOOLEAN.class),
		D3DRS_DIFFUSEMATERIALSOURCE       (145, D3DMATERIALCOLORSOURCE.class),
		D3DRS_SPECULARMATERIALSOURCE      (146, D3DMATERIALCOLORSOURCE.class),
		D3DRS_AMBIENTMATERIALSOURCE       (147, D3DMATERIALCOLORSOURCE.class),
		D3DRS_EMISSIVEMATERIALSOURCE      (148, D3DMATERIALCOLORSOURCE.class),
		D3DRS_VERTEXBLEND                 (151, D3DVERTEXBLENDFLAGS.class),
		D3DRS_CLIPPLANEENABLE             (152, int.class),  // bit mask
		D3DRS_POINTSIZE                   (154, float.class),
		D3DRS_POINTSIZE_MIN               (155, float.class),
		D3DRS_POINTSPRITEENABLE           (156, D3DBOOLEAN.class),
		D3DRS_POINTSCALEENABLE            (157, D3DBOOLEAN.class),
		D3DRS_POINTSCALE_A                (158, float.class),
		D3DRS_POINTSCALE_B                (159, float.class),
		D3DRS_POINTSCALE_C                (160, float.class),
		D3DRS_MULTISAMPLEANTIALIAS        (161, D3DBOOLEAN.class),
		D3DRS_MULTISAMPLEMASK             (162, int.class),
		D3DRS_PATCHEDGESTYLE              (163, D3DPATCHEDGESTYLE.class),
		D3DRS_DEBUGMONITORTOKEN           (165, D3DDEBUGMONITORTOKENS.class),
		D3DRS_POINTSIZE_MAX               (166, float.class),
		D3DRS_INDEXEDVERTEXBLENDENABLE    (167, D3DBOOLEAN.class),
		D3DRS_COLORWRITEENABLE            (168, int.class),
		D3DRS_TWEENFACTOR                 (170, float.class),
		D3DRS_BLENDOP                     (171, D3DBLENDOP.class),
		D3DRS_POSITIONDEGREE              (172, D3DDEGREETYPE.class),
		D3DRS_NORMALDEGREE                (173, D3DDEGREETYPE.class),
		D3DRS_SCISSORTESTENABLE           (174, D3DBOOLEAN.class),
		D3DRS_SLOPESCALEDEPTHBIAS         (175, float.class),
		D3DRS_ANTIALIASEDLINEENABLE       (176, D3DBOOLEAN.class),
		D3DRS_MINTESSELLATIONLEVEL        (178, float.class),
		D3DRS_MAXTESSELLATIONLEVEL        (179, float.class),
		D3DRS_ADAPTIVETESS_X              (180, float.class),
		D3DRS_ADAPTIVETESS_Y              (181, float.class),
		D3DRS_ADAPTIVETESS_Z              (182, float.class),
		D3DRS_ADAPTIVETESS_W              (183, float.class),
		D3DRS_ENABLEADAPTIVETESSELLATION  (184, D3DBOOLEAN.class),
		D3DRS_TWOSIDEDSTENCILMODE         (185, D3DBOOLEAN.class),
		D3DRS_CCW_STENCILFAIL             (186, D3DSTENCILOP.class),
		D3DRS_CCW_STENCILZFAIL            (187, D3DSTENCILOP.class),
		D3DRS_CCW_STENCILPASS             (188, D3DSTENCILOP.class),
		D3DRS_CCW_STENCILFUNC             (189, D3DCMPFUNC.class),
		D3DRS_COLORWRITEENABLE1           (190, int.class),
		D3DRS_COLORWRITEENABLE2           (191, int.class),
		D3DRS_COLORWRITEENABLE3           (192, int.class),
		D3DRS_BLENDFACTOR                 (193, ColorRGBA.class),
		D3DRS_SRGBWRITEENABLE             (194, int.class),
		D3DRS_DEPTHBIAS                   (195, float.class),
		D3DRS_WRAP8                       (198, D3DWRAPCOORD.class),
		D3DRS_WRAP9                       (199, D3DWRAPCOORD.class),
		D3DRS_WRAP10                      (200, D3DWRAPCOORD.class),
		D3DRS_WRAP11                      (201, D3DWRAPCOORD.class),
		D3DRS_WRAP12                      (202, D3DWRAPCOORD.class),
		D3DRS_WRAP13                      (203, D3DWRAPCOORD.class),
		D3DRS_WRAP14                      (204, D3DWRAPCOORD.class),
		D3DRS_WRAP15                      (205, D3DWRAPCOORD.class),
		D3DRS_SEPARATEALPHABLENDENABLE    (206, D3DBOOLEAN.class),
		D3DRS_SRCBLENDALPHA               (207, D3DBLEND.class),
		D3DRS_DESTBLENDALPHA              (208, D3DBLEND.class),
		D3DRS_BLENDOPALPHA                (209, D3DBLENDOP.class),
		D3DRS_FORCE_DWORD                 (0x7fffffff, int.class);

		public int id;
		public Class<?> typeClass;
		
		private D3DRenderStateType(int id, Class<?> typeClass) {
			this.id = id;
			this.typeClass = typeClass;
		}
		public static D3DRenderStateType getById(int id) {
			for (D3DRenderStateType state : values()) {
				if (state.id == id) {
					return state;
				}
			}
			return null;
		}
	}

	//TextureStage
	
	public static enum D3DTEXTUREOP implements D3DStateEnum {
		D3DTOP_DISABLE                    (1),
		D3DTOP_SELECTARG1                 (2),
		D3DTOP_SELECTARG2                 (3),
		D3DTOP_MODULATE                   (4),
		D3DTOP_MODULATE2X                 (5),
		D3DTOP_MODULATE4X                 (6),
		D3DTOP_ADD                        (7),
		D3DTOP_ADDSIGNED                  (8),
		D3DTOP_ADDSIGNED2X                (9),
		D3DTOP_SUBTRACT                   (10),
		D3DTOP_ADDSMOOTH                  (11),
		D3DTOP_BLENDDIFFUSEALPHA          (12),
		D3DTOP_BLENDTEXTUREALPHA          (13),
		D3DTOP_BLENDFACTORALPHA           (14),
		D3DTOP_BLENDTEXTUREALPHAPM        (15),
		D3DTOP_BLENDCURRENTALPHA          (16),
		D3DTOP_PREMODULATE                (17),
		D3DTOP_MODULATEALPHA_ADDCOLOR     (18),
		D3DTOP_MODULATECOLOR_ADDALPHA     (19),
		D3DTOP_MODULATEINVALPHA_ADDCOLOR  (20),
		D3DTOP_MODULATEINVCOLOR_ADDALPHA  (21),
		D3DTOP_BUMPENVMAP                 (22),
		D3DTOP_BUMPENVMAPLUMINANCE        (23),
		D3DTOP_DOTPRODUCT3                (24),
		D3DTOP_MULTIPLYADD                (25),
		D3DTOP_LERP                       (26),
		D3DTOP_FORCE_DWORD                (0x7fffffff);
		int id;
		private D3DTEXTUREOP(int id) { this.id = id; }
		public static D3DTEXTUREOP getById(int id) { for (D3DTEXTUREOP state : values())  if (state.id == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DTA implements D3DStateEnum {
		D3DTA_SELECTMASK        (0x0000000f),  // mask for arg selector
		D3DTA_DIFFUSE           (0x00000000),  // select diffuse color (read only)
		D3DTA_CURRENT           (0x00000001),  // select stage destination register (read/write)
		D3DTA_TEXTURE           (0x00000002),  // select texture color (read only)
		D3DTA_TFACTOR           (0x00000003),  // select D3DRS_TEXTUREFACTOR (read only)
		D3DTA_SPECULAR          (0x00000004),  // select specular color (read only)
		D3DTA_TEMP              (0x00000005),  // select temporary register color (read/write)
		D3DTA_CONSTANT          (0x00000006),  // select texture stage constant
		D3DTA_COMPLEMENT        (0x00000010),  // take 1.0 - x (read modifier)
		D3DTA_ALPHAREPLICATE    (0x00000020);  // replicate alpha to color components (read modifier)
		int id;
		private D3DTA(int id) { this.id = id; }
		//TODO add support for modifiers
		public static D3DTA getById(int id) { for (D3DTA state : values())  if ((state.id & D3DTA_SELECTMASK.id) == id) return state; return null; }
		public int getId() { return id; }
	}
	//We won't use it, as we would lose the index
	public static enum D3DTSS_TCI implements D3DStateEnum {
		D3DTSS_TCI_PASSTHRU						(0x00000000),
		D3DTSS_TCI_CAMERASPACENORMAL			(0x00010000),
		D3DTSS_TCI_CAMERASPACEPOSITION 			(0x00020000),
		D3DTSS_TCI_CAMERASPACEREFLECTIONVECTOR	(0x00030000),
		D3DTSS_TCI_SPHEREMAP					(0x00040000);
		int id;
		private D3DTSS_TCI(int id) { this.id = id; }
		public static D3DTSS_TCI getById(int id) { for (D3DTSS_TCI state : values())  if (state.id  == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DTEXTURETRANSFORMFLAGS implements D3DStateEnum {
		D3DTTFF_DISABLE      (0),
		D3DTTFF_COUNT1       (1),
		D3DTTFF_COUNT2       (2),
		D3DTTFF_COUNT3       (3),
		D3DTTFF_COUNT4       (4),
		D3DTTFF_PROJECTED    (256),
		D3DTTFF_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DTEXTURETRANSFORMFLAGS(int id) { this.id = id; }
		public static D3DTEXTURETRANSFORMFLAGS getById(int id) { for (D3DTEXTURETRANSFORMFLAGS state : values())  if (state.id  == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DTextureStageStateType {
		D3DTSS_COLOROP                (1, D3DTEXTUREOP.class),
		D3DTSS_COLORARG1              (2, D3DTA.class),
		D3DTSS_COLORARG2              (3, D3DTA.class),
		D3DTSS_ALPHAOP                (4, D3DTEXTUREOP.class),
		D3DTSS_ALPHAARG1              (5, D3DTA.class),
		D3DTSS_ALPHAARG2              (6, D3DTA.class),
		D3DTSS_BUMPENVMAT00           (7, float.class), //float
		D3DTSS_BUMPENVMAT01           (8, float.class), //float
		D3DTSS_BUMPENVMAT10           (9, float.class), //float
		D3DTSS_BUMPENVMAT11           (10, float.class), //float
		D3DTSS_TEXCOORDINDEX          (11, int.class), //int
		D3DTSS_BUMPENVLSCALE          (22, float.class), //float
		D3DTSS_BUMPENVLOFFSET         (23, float.class), //float
		D3DTSS_TEXTURETRANSFORMFLAGS  (24, D3DTEXTURETRANSFORMFLAGS.class),
		D3DTSS_COLORARG0              (26, D3DTA.class),
		D3DTSS_ALPHAARG0              (27, D3DTA.class),
		D3DTSS_RESULTARG              (28, D3DTA.class),
		D3DTSS_CONSTANT               (32, D3DTA.class),
		D3DTSS_FORCE_DWORD            (0x7fffffff, int.class);

		public int id;
		public Class<?> typeClass;

		private D3DTextureStageStateType(int id, Class<?> typeClass) {
			this.id = id;
			this.typeClass = typeClass;
		}
		public static D3DTextureStageStateType getById(int id) {
			for (D3DTextureStageStateType state : values()) {
				if (state.id == id) {
					return state;
				}
			}
			return null;
		}
	}
	
	
	//SamplerState
	
	public static enum D3DTEXTUREADDRESS implements D3DStateEnum {
		D3DTADDRESS_WRAP         (1),
		D3DTADDRESS_MIRROR       (2),
		D3DTADDRESS_CLAMP        (3),
		D3DTADDRESS_BORDER       (4),
		D3DTADDRESS_MIRRORONCE   (5),
		D3DTADDRESS_FORCE_DWORD  (0x7fffffff);
		int id;
		private D3DTEXTUREADDRESS(int id) { this.id = id; }
		public static D3DTEXTUREADDRESS getById(int id) { for (D3DTEXTUREADDRESS state : values())  if (state.id  == id) return state; return null; }
		public int getId() { return id; }
	}
	public static enum D3DTEXTUREFILTERTYPE implements D3DStateEnum {
		D3DTEXF_NONE             (0),
		D3DTEXF_POINT            (1),
		D3DTEXF_LINEAR           (2),
		D3DTEXF_ANISOTROPIC      (3),
		D3DTEXF_PYRAMIDALQUAD    (6),
		D3DTEXF_GAUSSIANQUAD     (7),
		D3DTEXF_CONVOLUTIONMONO  (8),
		D3DTEXF_FORCE_DWORD      (0x7fffffff);
		int id;
		private D3DTEXTUREFILTERTYPE(int id) { this.id = id; }
		public static D3DTEXTUREFILTERTYPE getById(int id) { for (D3DTEXTUREFILTERTYPE state : values())  if (state.id  == id) return state; return null; }
		public int getId() { return id; }
	}
	
	public static enum D3DSamplerStateType {
		D3DSAMP_ADDRESSU       (1, D3DTEXTUREADDRESS.class),
		D3DSAMP_ADDRESSV       (2, D3DTEXTUREADDRESS.class),
		D3DSAMP_ADDRESSW       (3, D3DTEXTUREADDRESS.class),
		D3DSAMP_BORDERCOLOR    (4, ColorRGBA.class), //color
		D3DSAMP_MAGFILTER      (5, D3DTEXTUREFILTERTYPE.class),
		D3DSAMP_MINFILTER      (6, D3DTEXTUREFILTERTYPE.class),
		D3DSAMP_MIPFILTER      (7, D3DTEXTUREFILTERTYPE.class),
		D3DSAMP_MIPMAPLODBIAS  (8, int.class), //int
		D3DSAMP_MAXMIPLEVEL    (9, int.class), //int
		D3DSAMP_MAXANISOTROPY  (10, int.class), //int
		D3DSAMP_SRGBTEXTURE    (11, int.class), //int
		D3DSAMP_ELEMENTINDEX   (12, int.class), //int
		D3DSAMP_DMAPOFFSET     (13, int.class), //int
		D3DSAMP_FORCE_DWORD    (0x7fffffff, int.class);

		public int id;
		public Class<?> typeClass;

		private D3DSamplerStateType(int id, Class<?> typeClass) {
			this.id = id;
			this.typeClass = typeClass;
		}
		public static D3DSamplerStateType getById(int id) {
			for (D3DSamplerStateType state : values()) {
				if (state.id == id) {
					return state;
				}
			}
			return null;
		}
}
}
