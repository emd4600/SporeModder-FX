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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import sporemodder.file.rw4.Direct3DEnums.D3DPRIMITIVETYPE;
import sporemodder.file.rw4.Direct3DEnums.D3DRenderStateType;
import sporemodder.file.rw4.Direct3DEnums.D3DSamplerStateType;
import sporemodder.file.rw4.Direct3DEnums.D3DTextureStageStateType;
import sporemodder.util.ColorRGB;
import sporemodder.util.ColorRGBA;

public class MaterialStateCompiler {
	
	public static class ShaderConstant {
		public short index;
		public short offset;
		public byte[] data;
	}
	
	public static class TextureSlot {
		public final LinkedHashMap<D3DTextureStageStateType, Integer> textureStageStates = new LinkedHashMap<D3DTextureStageStateType, Integer>();
		public final LinkedHashMap<D3DSamplerStateType, Integer> samplerStates = new LinkedHashMap<D3DSamplerStateType, Integer>();
		// Usually a RWRaster, but might be a RWCompiledState too
		public RWObject raster;
		public int unkStageStates = 0x3F;
		public int unkSamplerStates = 0x73;
		public int samplerIndex;
	}
	
	/** Used in the first flags value, whether shader constants are defined. */
	private static final int FLAG_SHADER_CONSTANTS = 0x8;
	/** Used in the first flags value, whether the material color is defined. */
	private static final int FLAG_MATERIAL_COLOR = 0x10;
	/** Used in the first flags value, whether the ambient color is defined. */
	private static final int FLAG_AMBIENT_COLOR = 0x20;
	/** Used in the first flags value, whether a vertex description is defined. */
	private static final int FLAG_VERTEX_DESCRIPTION = 0x100000;
	/** Used in the first flags value, whether the set of 17 booleans is used. */
	private static final int FLAG_USE_BOOLEANS = 0x8000;
	/** Used in the first flags value, whether the modelToWorld matrix is specified. */
	private static final int FLAG_MODELTOWORLD = 0x1;
	/** Used in the first flags value, whether the modelToWorld matrix is using an RWObject. */
	private static final int FLAG_MODELTOWORLD_OBJECT = 0x2;
	
	/** Used in the third flags value, whether render states are defined. */
	private static final int FLAG3_RENDER_STATES = 0x20000;
	/** Used in the third flags value, whether a palette entries object is defined. */
	private static final int FLAG3_PALETTE_ENTRIES = 0x100000;
	/** Used in the third flags value, which texture slots are used. */
	// Spore checks for 0xDFFFF, using this should be enough
	private static final int FLAG3_TEXTURE_SLOTS = 0xDFFFF;
	
	// Should only be used inside this package
	byte[] data;
	
	private RenderWare renderWare;
	private boolean isDecompiled;
	
	private boolean automaticFlags;

	public ColorRGBA materialColor;
	public ColorRGB ambientColor;
	public RWVertexDescription vertexDescription;
	public int rendererID;
	public D3DPRIMITIVETYPE primitiveType = D3DPRIMITIVETYPE.D3DPT_TRIANGLELIST;
	public final List<TextureSlot> textureSlots = new ArrayList<TextureSlot>(); 
	public final List<ShaderConstant> shaderConstants = new ArrayList<ShaderConstant>();
	
	public final LinkedHashMap<D3DRenderStateType, Integer> renderStates = new LinkedHashMap<D3DRenderStateType, Integer>();
	
	/** Either a RWObject or a float[4][4], representing the 4x4 modelToWorld matrix. */
	public Object modelToWorld;
	
	private int flags1;
	private int flags2;
	private int flags3;
	
	public int field_14;
	
	/** Only read if flags1 & 0x3FC0; each bit tells whether data is available for that 
	 * position or not. */  // it's baked lighting data?
	private Float[] unkData2;
	/** Only read if flags1 & FLAG_USE_BOOLEANS; it's 17 booleans. */
	private boolean[] unkData3;
	
	/** Only read if flags1 & 0x10000. */
	private Integer unkData4;
	/** Only read if flags1 & 0x20000; 3 floats. */
	private float[] unkData5;
	/** Only read if flags1 & 0x40000. */
	private Float unkData6;
	/** Only read if flags1 & 0x80000. */
	private Float unkData7;
	/** Only read if field_14 & 0x20000; 7 integers, apparently flags. */
	private int[] unkData8;
	/** Only read if field_14 & 0x40000; 11 integers. */
	private int[] unkData9;
	/** Only read if field_14 & 0x80000; 11 integers. */
	private int[] unkData10;
	/** Only read if flags3 & 0x100000. This object must contain 256 palette entries,
	 * with each palette entry being a 4-byte color. */
	private RWObject paletteEntries;
	
	public MaterialStateCompiler(RenderWare renderWare) {
		super();
		this.renderWare = renderWare;
	}

	public void decompile() throws IOException {
		try (MemoryStream stream = new MemoryStream(data)) {
			
			// Size
			stream.skip(4);
			
			primitiveType = D3DPRIMITIVETYPE.getById(stream.readLEInt());
			
			flags1 = stream.readLEInt();
			flags2 = stream.readLEInt();
			flags3 = stream.readLEInt();
			field_14 = stream.readLEInt();
			rendererID = stream.readLEInt();
			
			// Padding
			stream.skip(4);
			
			if ((flags1 & FLAG_MODELTOWORLD) != 0) {
				if ((flags1 & FLAG_MODELTOWORLD_OBJECT) != 0) {
					modelToWorld = renderWare.get(stream.readLEInt());
				} else {
					float[][] modelToWorld = new float[4][4];
					for (float[] row : modelToWorld) {
						stream.readLEFloats(row);
					}
					this.modelToWorld = modelToWorld;
				}
			}
			
			if ((flags1 & FLAG_VERTEX_DESCRIPTION) != 0) {
				vertexDescription = new RWVertexDescription(null);
				vertexDescription.read(stream);
			}
			
			if ((flags1 & FLAG_SHADER_CONSTANTS) != 0) {
				short index = stream.readLEShort();
				
				while (index != 0) {
					if (index > 0) {
						ShaderConstant sc = new ShaderConstant();
						sc.index = index;
						sc.offset = stream.readLEShort();
						sc.data = new byte[stream.readLEInt()];
						stream.skip(sc.offset);  // ?
						stream.read(sc.data);
					}
					
					index = stream.readLEShort();
				}
				
				stream.skip(6);
			}
			
			if ((flags1 & FLAG_MATERIAL_COLOR) != 0) {
				materialColor = new ColorRGBA();
				materialColor.readLE(stream);
			}
			
			if ((flags1 & FLAG_AMBIENT_COLOR) != 0) {
				ambientColor = new ColorRGB();
				ambientColor.readLE(stream);
			}
			
			if ((flags1 & 0x3FC0) != 0) {
				unkData2 = new Float[8];
				for (int i = 0; i < 8; i++) {
					if ((flags1 & (1 << (6+i))) != 0) {
						unkData2[i] = stream.readLEFloat();
					}
				}
			}
			
			if ((flags1 & FLAG_USE_BOOLEANS) != 0) {
				unkData3 = new boolean[17];
				stream.readBooleans(unkData3);
			}
			
			if ((flags1 & 0xF0000) != 0) {
				if ((flags1 & 0x10000) != 0) {
					unkData4 = stream.readLEInt();
				}
				if ((flags1 & 0x20000) != 0) {
					unkData5 = new float[3];
					stream.readLEFloats(unkData5);
				}
				if ((flags1 & 0x40000) != 0) {
					unkData6 = stream.readLEFloat();
				}
				if ((flags1 & 0x80000) != 0) {
					unkData7 = stream.readLEFloat();
				}
			}
			
			if (field_14 != 0) {
				if ((field_14 & 0x20000) != 0) {
					unkData8 = new int[7];
					stream.readLEInts(unkData8);
				}
				if ((field_14 & 0x40000) != 0) {
					unkData9 = new int[11];
					stream.readLEInts(unkData9);
				}
				if ((field_14 & 0x80000) != 0) {
					unkData10 = new int[11];
					stream.readLEInts(unkData10);
				}
			}
			
			if ((flags3 & FLAG3_RENDER_STATES) != 0) {
				stream.skip(4);
				
				while (true) {
					int state = stream.readLEInt();
					int value = stream.readLEInt();
					
					if (state == -1 && value == -1) {
						break;
					}
					
					renderStates.put(D3DRenderStateType.getById(state), value);
				}
			}
			
			// -1 if there are no palette entries
			int paletteEntriesIndex = stream.readLEInt();
			
			if ((flags3 & FLAG3_PALETTE_ENTRIES) != 0) {
				paletteEntries = renderWare.get(paletteEntriesIndex); 
			}
			
			if ((flags3 & FLAG3_TEXTURE_SLOTS) != 0) {
				
				int samplerIndex;
				
				while ((samplerIndex = stream.readLEInt()) != -1) {

					TextureSlot slot = new TextureSlot();
					textureSlots.add(slot);
					slot.samplerIndex = samplerIndex;
					slot.raster = renderWare.get(stream.readLEInt());
					
					slot.unkStageStates = stream.readLEInt();
					if (slot.unkStageStates != 0) {
						int state;
						
						while ((state = stream.readLEInt()) != -1) {
							slot.textureStageStates.put(D3DTextureStageStateType.getById(state), stream.readLEInt());
						}
					}
					
					slot.unkSamplerStates = stream.readLEInt();
					if (slot.unkSamplerStates != 0) {
						int state;
						
						while ((state = stream.readLEInt()) != -1) {
							slot.samplerStates.put(D3DSamplerStateType.getById(state), stream.readLEInt());
						}
					}
				}
			}
			
			isDecompiled = true;
		}
	}
	
	public void compile() throws IOException {
		if (automaticFlags) {
			flags1 = 0;
			flags2 = 0;
			flags3 = 0;
			
			// Always used? Don't really know what it does
			flags1 |= 4;
			flags2 |= 0x8000;
		}
		
		if (materialColor != null) flags1 |= FLAG_MATERIAL_COLOR;
		else flags1 &= ~FLAG_MATERIAL_COLOR;
		
		if (ambientColor != null) flags1 |= FLAG_AMBIENT_COLOR;
		else flags1 &= ~FLAG_AMBIENT_COLOR;
		
		if (unkData3 != null) flags1 |= FLAG_USE_BOOLEANS;
		else flags1 &= ~FLAG_USE_BOOLEANS;
		
		if (!shaderConstants.isEmpty()) {
			flags1 |= FLAG_SHADER_CONSTANTS;
			flags2 |= FLAG_SHADER_CONSTANTS;
		}
		else {
			flags2 &= ~FLAG_SHADER_CONSTANTS;
			flags2 &= ~FLAG_SHADER_CONSTANTS;
		}
		
		if (vertexDescription != null) {
			flags1 |= FLAG_VERTEX_DESCRIPTION;
			flags2 |= FLAG_VERTEX_DESCRIPTION;
		}
		else {
			flags2 &= ~FLAG_VERTEX_DESCRIPTION;
			flags2 &= ~FLAG_VERTEX_DESCRIPTION;
		}
		
		if (!renderStates.isEmpty()) flags3 |= FLAG3_RENDER_STATES;
		else flags3 &= ~FLAG3_RENDER_STATES;
		
		if (!textureSlots.isEmpty()) flags3 |= FLAG3_TEXTURE_SLOTS;
		else flags3 &= ~FLAG3_TEXTURE_SLOTS;
		
		if (paletteEntries != null) flags3 |= FLAG3_PALETTE_ENTRIES;
		else flags3 &= ~FLAG3_PALETTE_ENTRIES;
		
		
		if (unkData2 != null) {
			for (int i = 0; i < 8; i++) {
				if (unkData2[i] != null) {
					flags1 |= 1 << (6+i);
				} else {
					flags1 &= ~(1 << (6+i));
				}
			}
		}
		
		if (modelToWorld != null) {
			flags1 |= FLAG_MODELTOWORLD;
			if (RWObject.class.isInstance(modelToWorld)) {
				flags1 |= FLAG_MODELTOWORLD_OBJECT;
			} else {
				flags1 &= ~FLAG_MODELTOWORLD_OBJECT;
			}
		}
		else {
			flags1 &= ~FLAG_MODELTOWORLD;
		}
		if (unkData4 != null) flags1 |= 0x10000;
		else flags1 &= ~0x10000;
		
		if (unkData5 != null) flags1 |= 0x20000;
		else flags1 &= ~0x20000;
		
		if (unkData6 != null) flags1 |= 0x40000;
		else flags1 &= ~0x40000;
		
		if (unkData7 != null) flags1 |= 0x80000;
		else flags1 &= ~0x80000;
		
		if (unkData8 != null) field_14 |= 0x20000;
		else field_14 &= ~0x20000;
		
		if (unkData9 != null) field_14 |= 0x40000;
		else field_14 &= ~0x40000;
		
		if (unkData10 != null) field_14 |= 0x80000;
		else field_14 &= ~0x80000;
		
		
		try (MemoryStream stream = new MemoryStream()) {
			
			stream.writeLEInt(0);  // size, will fill later
			stream.writeLEInt(primitiveType.getId());
			stream.writeLEInt(flags1);
			stream.writeLEInt(flags2);
			stream.writeLEInt(flags3);
			stream.writeLEInt(field_14);
			stream.writeLEInt(rendererID);
			stream.writePadding(4);
			
			if (modelToWorld != null) {
				if (RWObject.class.isInstance(modelToWorld)) {
					stream.writeLEInt(renderWare.indexOf((RWObject) modelToWorld));
				} else {
					float[][] arrays = (float[][]) modelToWorld;
					for (float[] row : arrays) {
						stream.writeLEFloats(row);
					}
				}
			}
			
			if (vertexDescription != null) {
				vertexDescription.write(stream);
			}
			
			if (!shaderConstants.isEmpty()) {
				for (ShaderConstant sc : shaderConstants) {
					stream.writeLEShort(sc.index);
					stream.writeLEShort(sc.offset);
					stream.writeLEInt(sc.data.length);
					
					if (sc.offset > 0) stream.writePadding(sc.offset);
					stream.write(sc.data);
				}
			}
			
			if (materialColor != null) {
				materialColor.writeLE(stream);
			}
			if (ambientColor != null) {
				ambientColor.writeLE(stream);
			}
			
			if (unkData2 != null) {
				for (int i = 0; i < 8; i++) {
					if (unkData2[i] != null) stream.writeLEFloat(unkData2[i]);
				}
			}
			
			if (unkData3 != null) {
				stream.writeBooleans(unkData3);
			}
			
			if (unkData4 != null) stream.writeLEInt(unkData4);
			if (unkData5 != null) stream.writeLEFloats(unkData5);
			if (unkData6 != null) stream.writeLEFloat(unkData6);
			if (unkData7 != null) stream.writeLEFloat(unkData7);
			if (unkData8 != null) stream.writeLEInts(unkData8);
			if (unkData9 != null) stream.writeLEInts(unkData9);
			if (unkData10 != null) stream.writeLEInts(unkData10);
			
			if (!renderStates.isEmpty()) {
				stream.writeLEInt(0);
				
				for (Map.Entry<D3DRenderStateType, Integer> entry : renderStates.entrySet()) {
					stream.writeLEInt(entry.getKey().id);
					stream.writeLEInt(entry.getValue());
				}
				
				stream.writeLEInt(-1);
				stream.writeLEInt(-1);
			}
			
			if (!textureSlots.isEmpty()) {
				if (paletteEntries != null) {
					stream.writeLEInt(renderWare.indexOf(paletteEntries));
				} else {
					stream.writeLEInt(-1);
				}
				
				for (TextureSlot slot : textureSlots) {
					stream.writeLEInt(slot.samplerIndex);
					stream.writeLEInt(renderWare.indexOf(slot.raster));
					
					stream.writeLEInt(slot.unkStageStates);
					if (slot.unkStageStates != 0) {
						for (Map.Entry<D3DTextureStageStateType, Integer> entry : slot.textureStageStates.entrySet()) {
							stream.writeLEInt(entry.getKey().id);
							stream.writeLEInt(entry.getValue());
						}
						stream.writeLEInt(-1);
					}
					
					stream.writeLEInt(slot.unkStageStates);
					if (slot.unkStageStates != 0) {
						for (Map.Entry<D3DTextureStageStateType, Integer> entry : slot.textureStageStates.entrySet()) {
							stream.writeLEInt(entry.getKey().id);
							stream.writeLEInt(entry.getValue());
						}
						stream.writeLEInt(-1);
					}
				}
				
				stream.writeLEInt(-1);
			}
			
			stream.seek(0);
			stream.writeLEUInt(stream.length());
		}
	}
	
	public boolean isAutomaticFlags() {
		return automaticFlags;
	}

	/**
	 * If true, the flags value will be reseted and will be set depending on the existing 
	 * parameters.
	 * @param automaticFlags
	 */
	public void setAutomaticFlags(boolean automaticFlags) {
		this.automaticFlags = automaticFlags;
	}

	/**
	 * Returns whether the data in this material state has been decompiled, and therefore
	 * can be used.
	 * @return
	 */
	public boolean isDecompiled() {
		return isDecompiled;
	}

	public static void main(String[] args) throws IOException {
		
//		File folder = new File("E:\\Eric\\SporeModder\\Projects\\Spore_Graphics.package.unpacked\\animations~");
//		for (File file : folder.listFiles()) {
//			if (file.getName().endsWith(".rw4")) {
//				
//				try (FileStream stream = new FileStream(file, "r")) {
//					RenderWare renderWare = new RenderWare();
//					renderWare.read(stream);
//					List<RWCompiledState> compiledStates = renderWare.getObjects(RWCompiledState.class);
//					for (RWCompiledState state : compiledStates) {
//						state.data.decompile();
//						
//						if (state.data.unkData1 != null) {
//							System.out.println("1: " + file.getName());
//						}
//						
//						if (state.data.unkData2 != null) {
//							System.out.println("2: " + file.getName());
//						}
//						
//						if (state.data.unkData4 != null) System.out.println("4: " + file.getName());
//						if (state.data.unkData5 != null) System.out.println("5: " + file.getName());
//						if (state.data.unkData6 != null) System.out.println("6: " + file.getName());
//						if (state.data.unkData7 != null) System.out.println("7: " + file.getName());
//						if (state.data.unkData8 != null) System.out.println("8: " + file.getName());
//						if (state.data.unkData9 != null) System.out.println("9: " + file.getName());
//						if (state.data.unkData10 != null) System.out.println("10: " + file.getName());
//						if (state.data.paletteEntries != null) System.out.println("paletteEntries: " + file.getName());
//					}
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//					continue;
//				}
//			}
//		}
		
		File file = new File("E:\\Eric\\SporeModder\\Projects\\Spore_Graphics.package.unpacked\\#40212001\\#00000003.rw4");
		
		try (FileStream stream = new FileStream(file, "r")) {
			RenderWare renderWare = new RenderWare();
			renderWare.read(stream);
			List<RWCompiledState> compiledStates = renderWare.getObjects(RWCompiledState.class);
			for (RWCompiledState state : compiledStates) {
				state.data.decompile();
				
				if (state.data.unkData2 != null) System.out.println("2: " + state.sectionInfo.pData);
				if (state.data.unkData4 != null) System.out.println("4: " + state.sectionInfo.pData);
				if (state.data.unkData5 != null) System.out.println("5: " + state.sectionInfo.pData);
				if (state.data.unkData6 != null) System.out.println("6: " + state.sectionInfo.pData);
				if (state.data.unkData7 != null) System.out.println("7: " + state.sectionInfo.pData);
				if (state.data.unkData8 != null) System.out.println("8: " + state.sectionInfo.pData);
				if (state.data.unkData9 != null) System.out.println("9: " + state.sectionInfo.pData);
				if (state.data.unkData10 != null) System.out.println("10: " + state.sectionInfo.pData);
				if (state.data.paletteEntries != null) System.out.println("paletteEntries: " + state.sectionInfo.pData);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
