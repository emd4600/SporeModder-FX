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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sporemodder.file.filestructures.MemoryStream;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.rw4.Direct3DEnums.D3DPRIMITIVETYPE;
import sporemodder.file.rw4.Direct3DEnums.D3DRenderStateType;
import sporemodder.file.rw4.Direct3DEnums.D3DSamplerStateType;
import sporemodder.file.rw4.Direct3DEnums.D3DTextureStageStateType;
import sporemodder.file.shaders.ShaderData;
import sporemodder.util.ColorRGB;
import sporemodder.util.ColorRGBA;

public class MaterialStateCompiler {
	
	public static class ShaderDataEntry {
		public short index;
		public short offset;
		public int[] data;
	}
	
	public static class TextureSlot {
		
		public final LinkedHashMap<D3DTextureStageStateType, Integer> textureStageStates = new LinkedHashMap<D3DTextureStageStateType, Integer>();
		public final LinkedHashMap<D3DSamplerStateType, Integer> samplerStates = new LinkedHashMap<D3DSamplerStateType, Integer>();
		// Usually a RWRaster, but might be a RWCompiledState too
		public RWObject raster;
		public int stageStatesMask;
		public int samplerStatesMask;
		public int samplerIndex;
	}
	
	public static final String KEYWORD = "compiledState";
	
	/** Used in the first flags value, whether shader constants are defined. */
	private static final int FLAG_SHADER_DATA = 0x8;
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
	private static final int FLAG3_TEXTURE_SLOTS = 0x1FFFF;
	
	// 0x10803B
	private static final int FLAG_MASK = FLAG_SHADER_DATA | FLAG_MATERIAL_COLOR | FLAG_AMBIENT_COLOR |
			FLAG_VERTEX_DESCRIPTION | FLAG_USE_BOOLEANS | FLAG_MODELTOWORLD | FLAG_MODELTOWORLD_OBJECT;
	
	private static final int FLAG3_MASK = FLAG3_RENDER_STATES | FLAG3_PALETTE_ENTRIES | FLAG3_TEXTURE_SLOTS;
	
	// Should only be used inside this package
	byte[] data;
	
	private RenderWare renderWare;
	private boolean isDecompiled;

	public ColorRGBA materialColor;
	public ColorRGB ambientColor;
	public RWVertexDescription vertexDescription;
	public int rendererID;
	public D3DPRIMITIVETYPE primitiveType = D3DPRIMITIVETYPE.D3DPT_TRIANGLELIST;
	public final List<TextureSlot> textureSlots = new ArrayList<TextureSlot>(); 
	public final List<ShaderDataEntry> shaderData = new ArrayList<ShaderDataEntry>();
	
	public final Map<Integer, Map<D3DRenderStateType, Integer>> renderStates = new LinkedHashMap<>();
	
	/** Either a RWObject or a float[4][4], representing the 4x4 modelToWorld matrix. */
	public Object modelToWorld;
	
	public int extraFlags1;
	public int extraFlags2;
	public int extraFlags3;
	
	public int field_14;
	
	/** Only read if flags1 & 0x3FC0; each bit tells whether data is available for that 
	 * position or not. */  // it's baked lighting data?
	public Float[] unkData2;
	/** Only read if flags1 & FLAG_USE_BOOLEANS; it's 17 booleans. */
	public boolean[] unkData3;
	
	/** Only read if flags1 & 0x10000. */
	public Integer unkData4;
	/** Only read if flags1 & 0x20000; 3 floats. */
	public float[] unkData5;
	/** Only read if flags1 & 0x40000. */
	public Float unkData6;
	/** Only read if flags1 & 0x80000. */
	public Float unkData7;
	/** Only read if field_14 & 0x20000; 7 integers, apparently flags. */
	public int[] unkData8;
	/** Only read if field_14 & 0x40000; 11 integers. */
	public int[] unkData9;
	/** Only read if field_14 & 0x80000; 11 integers. */
	public int[] unkData10;
	/** Only read if flags3 & 0x100000. This object must contain 256 palette entries,
	 * with each palette entry being a 4-byte color. */
	public RWObject paletteEntries;
	
	public MaterialStateCompiler(RenderWare renderWare) {
		super();
		this.renderWare = renderWare;
	}
	
	public void reset() {
		isDecompiled = false;
		materialColor = null;
		ambientColor = null;
		vertexDescription = null;
		primitiveType = D3DPRIMITIVETYPE.D3DPT_TRIANGLELIST;
		rendererID = 0;
		textureSlots.clear();
		shaderData.clear();
		renderStates.clear();
		modelToWorld = null;
		extraFlags1 = 0;
		extraFlags2 = 0;
		extraFlags3 = 0;
		field_14 = 0;
		unkData2 = null;
		unkData3 = null;
		unkData4 = null;
		unkData5 = null;
		unkData6 = null;
		unkData7 = null;
		unkData8 = null;
		unkData9 = null;
		unkData10 = null;
		paletteEntries = null;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}

	public void decompile() throws IOException {
		try (MemoryStream stream = new MemoryStream(data)) {
			reset();
			
			// Size
			stream.skip(4);
			
			primitiveType = D3DPRIMITIVETYPE.getById(stream.readLEInt());
			
			int flags1 = stream.readLEInt();
			int flags2 = stream.readLEInt();
			int flags3 = stream.readLEInt();
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
			
			if ((flags1 & FLAG_SHADER_DATA) != 0) {
				short index = stream.readLEShort();
				
				while (index != 0) {
					if (index > 0) {
						ShaderDataEntry sc = new ShaderDataEntry();
						shaderData.add(sc);
						sc.index = index;
						sc.offset = stream.readLEShort();
						int length = stream.readLEInt();
						if (length % 4 != 0) throw new IOException("Unexpected ShaderData length " + length);
						sc.data = new int[length / 4];
						stream.skip(sc.offset);  // ?
						stream.readLEInts(sc.data);
						
						// Spore uses it as a render ware index
						if (sc.data.length == 0) stream.skip(4);
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
				int group = stream.readLEInt();
				
				while (group != -1) {
					Map<D3DRenderStateType, Integer> states = new LinkedHashMap<>();
					
					int state = stream.readLEInt();
					while (state != -1) {
						states.put(D3DRenderStateType.getById(state), stream.readLEInt());
						state = stream.readLEInt();
					}
					
					renderStates.put(group, states);
					group = stream.readLEInt();
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
					
					slot.stageStatesMask = stream.readLEInt();
					if (slot.stageStatesMask != 0) {
						int state;
						
						while ((state = stream.readLEInt()) != -1) {
							slot.textureStageStates.put(D3DTextureStageStateType.getById(state), stream.readLEInt());
						}
					}
					
					slot.samplerStatesMask = stream.readLEInt();
					if (slot.samplerStatesMask != 0) {
						int state;
						
						while ((state = stream.readLEInt()) != -1) {
							slot.samplerStates.put(D3DSamplerStateType.getById(state), stream.readLEInt());
						}
					}
				}
			}
			
			extraFlags1 = flags1 & ~FLAG_MASK;
			extraFlags2 = ~flags1 & flags2;
			extraFlags3 = flags3 & ~FLAG3_MASK;
			
			isDecompiled = true;
		}
	}
	
	public void compile() throws IOException {
		
		int flags1 = 0;
		int flags2 = 0;
		int flags3 = 0;
		
//		// Always used? Don't really know what it does
//		flags1 |= 4;
//		flags2 |= 0x8000;
		
		flags1 |= extraFlags1;
		flags2 |= extraFlags2;
		flags3 |= extraFlags3;
		
		if (materialColor != null) flags1 |= FLAG_MATERIAL_COLOR;
		else flags1 &= ~FLAG_MATERIAL_COLOR;
		
		if (ambientColor != null) flags1 |= FLAG_AMBIENT_COLOR;
		else flags1 &= ~FLAG_AMBIENT_COLOR;
		
		flags1 |= FLAG_USE_BOOLEANS;
//		if (unkData3 != null) flags1 |= FLAG_USE_BOOLEANS;
//		else flags1 &= ~FLAG_USE_BOOLEANS;
		
		if (!shaderData.isEmpty()) {
			flags1 |= FLAG_SHADER_DATA;
		}
		else {
			flags2 &= ~FLAG_SHADER_DATA;
		}
		
		if (vertexDescription != null) {
			flags1 |= FLAG_VERTEX_DESCRIPTION;
		}
		else {
			flags2 &= ~FLAG_VERTEX_DESCRIPTION;
		}
		
		if (!renderStates.isEmpty()) flags3 |= FLAG3_RENDER_STATES;
		else flags3 &= ~FLAG3_RENDER_STATES;
		
		for (TextureSlot slot : textureSlots) {
			flags3 |= 1 << slot.samplerIndex;
		}
		
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
		
		flags2 |= (0xFFFF & flags1);
		
		
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
			
			if (!shaderData.isEmpty()) {
				for (ShaderDataEntry sc : shaderData) {
					stream.writeLEShort(sc.index);
					stream.writeLEShort(sc.offset);
					stream.writeLEInt(sc.data.length * 4);
					
					if (sc.offset > 0) stream.writePadding(sc.offset);
					stream.writeLEInts(sc.data);
					
					if (sc.data.length == 0) stream.writeInt(0);
				}
				
				stream.writePadding(8);
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
			
			if (unkData3 == null) {
				unkData3 = new boolean[17];
				for (TextureSlot slot : textureSlots) {
					unkData3[slot.samplerIndex] = true;
				}
			}
			// This is not an option anymore. Setting all to false has the same effect as not having them?
			stream.writeBooleans(unkData3);
			
			if (unkData4 != null) stream.writeLEInt(unkData4);
			if (unkData5 != null) stream.writeLEFloats(unkData5);
			if (unkData6 != null) stream.writeLEFloat(unkData6);
			if (unkData7 != null) stream.writeLEFloat(unkData7);
			if (unkData8 != null) stream.writeLEInts(unkData8);
			if (unkData9 != null) stream.writeLEInts(unkData9);
			if (unkData10 != null) stream.writeLEInts(unkData10);
			
			if (!renderStates.isEmpty()) {
				for (int group : renderStates.keySet()) {
					stream.writeLEInt(group);
					for (Map.Entry<D3DRenderStateType, Integer> entry : renderStates.get(group).entrySet()) {
						stream.writeLEInt(entry.getKey().id);
						stream.writeLEInt(entry.getValue());
					}
					stream.writeLEInt(-1);
				}
				stream.writeLEInt(-1);
			}
			
			if (paletteEntries != null) {
				stream.writeLEInt(renderWare.indexOf(paletteEntries));
			} else {
				stream.writeLEInt(-1);
			}
			
			if (!textureSlots.isEmpty()) {
				
				for (TextureSlot slot : textureSlots) {
					stream.writeLEInt(slot.samplerIndex);
					// -1 won't let us use CompiledState::SetRaster()
					if (renderWare != null) {
						stream.writeLEInt(slot.raster == null ? -1 : renderWare.indexOf(slot.raster));
					} else {
						stream.writeLEInt(1 << 22);
					}
					
					stream.writeLEInt(slot.stageStatesMask);
					if (slot.stageStatesMask != 0) {
						for (Map.Entry<D3DTextureStageStateType, Integer> entry : slot.textureStageStates.entrySet()) {
							stream.writeLEInt(entry.getKey().id);
							stream.writeLEInt(entry.getValue());
						}
						stream.writeLEInt(-1);
					}
					
					stream.writeLEInt(slot.samplerStatesMask);
					if (slot.samplerStatesMask != 0) {
						for (Map.Entry<D3DSamplerStateType, Integer> entry : slot.samplerStates.entrySet()) {
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
			
			data = stream.toByteArray();
		}
	}
	
	private boolean compareUnkData3() {
		for (int i = 0; i < 17; ++i) {
			if (i < textureSlots.size()) {
				if (!unkData3[i]) return true;
			} else if (unkData3[i]) return false;
		}
		return true;
	}

	/**
	 * Returns whether the data in this material state has been decompiled, and therefore
	 * can be used.
	 * @return
	 */
	public boolean isDecompiled() {
		return isDecompiled;
	}
	
	public static int calculateStageStateMask(TextureSlot slot) {
		int mask = 0;
		for (D3DTextureStageStateType key : slot.textureStageStates.keySet()) {
			mask |= 1 << (key.id-1);
		}
		return mask;
	}
	
	public static int calculateSamplerStateMask(TextureSlot slot) {
		int mask = 0;
		for (D3DSamplerStateType key : slot.samplerStates.keySet()) {
			mask |= 1 << (key.id-1);
		}
		return mask;
	}
	
	public String toArgScript(String name) {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(name, writer);
		return writer.toString();
	}
	
	public void toArgScript(String name, ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(name).startBlock();
		
		writer.command("shaderID").arguments(HashManager.get().getFileName(rendererID));
		if (primitiveType != D3DPRIMITIVETYPE.D3DPT_TRIANGLELIST) {
			writer.command("primitiveType").arguments(primitiveType.toString());
		}
		
		if (materialColor != null) writer.command("materialColor").colorsRGBA(materialColor);
		if (ambientColor != null) writer.command("ambientColor").color(ambientColor);
		
		if (extraFlags1 != 0 || extraFlags3 != 0) {
			writer.blankLine();
			if (extraFlags1 != 0) writer.command("flags1").arguments("0x" + Integer.toHexString(extraFlags1));
			if (extraFlags3 != 0) writer.command("flags3").arguments("0x" + Integer.toHexString(extraFlags3));
		}
		
		if (!shaderData.isEmpty()) {
			writer.blankLine();
			for (ShaderDataEntry sd : shaderData) {
				writer.command("shaderData").arguments(ShaderData.getName(sd.index));
				for (int i : sd.data) {
					writer.arguments(HashManager.get().formatInt32(i));
				}
				if (sd.offset != 0) writer.option("offset").ints(sd.offset);
			}
		}
		
		if (unkData3 != null) {
			if (!compareUnkData3()) {
				writer.blankLine();
				writer.command("unkData3");
				for (int i = 0; i < unkData3.length; ++i) {
					writer.arguments(unkData3[i]);
				}
			}
		} else {
			writer.blankLine();
			// No unkData3 command means assigning it automatically, so this is to disable it
			writer.command("unkData3");
			for (int i = 0; i < 17; ++i) writer.ints(0);
		}
		
		if (vertexDescription != null) {
			writer.blankLine();
			writer.command("vertexDescription");
			
			if (vertexDescription.field_0 != 0) writer.option("field_0").arguments("0x" + Integer.toHexString(vertexDescription.field_0));
			if (vertexDescription.field_4 != 0) writer.option("field_4").arguments("0x" + Integer.toHexString(vertexDescription.field_4));
			if (vertexDescription.field_0E != 0) writer.option("field_0E").arguments("0x" + Integer.toHexString(vertexDescription.field_0E));
			//if (vertexDescription.elementFlags != 0) writer.option("field_10").arguments("0x" + Integer.toHexString(vertexDescription.elementFlags));
			//if (vertexDescription.field_14 != 0) writer.option("field_14").arguments("0x" + Integer.toHexString(vertexDescription.field_14));
			
			writer.startBlock();
			for (RWVertexElement element : vertexDescription.elements) {
				element.toArgScript(writer);
			}
			writer.endBlock().commandEND();
		}
		
		if (!renderStates.isEmpty()) {
			writer.blankLine();
			Set<Integer> groups = renderStates.keySet();
			if (groups.size() == 1 && groups.contains(0)) {
				for (Map.Entry<D3DRenderStateType, Integer> entry : renderStates.get(0).entrySet()) {
					writer.command("renderState").arguments(entry.getKey(), Direct3DEnums.getValueToString(entry.getKey().typeClass, entry.getValue()));
				}
			}
			else {
				for (int group : renderStates.keySet()) {
					writer.command("statesGroup").ints(group).startBlock();
					for (Map.Entry<D3DRenderStateType, Integer> entry : renderStates.get(group).entrySet()) {
						writer.command("renderState").arguments(entry.getKey(), Direct3DEnums.getValueToString(entry.getKey().typeClass, entry.getValue()));
					}
					writer.endBlock().commandEND();
				}
			}
		}
		
		for (TextureSlot slot : textureSlots) {
			writer.blankLine();
			writer.command("textureSlot").ints(slot.samplerIndex).startBlock();
			
			boolean first = true;
			if (slot.raster != null) {
				first = false;
				writer.command("raster").ints(renderWare.indexOf(slot.raster));
			}
			
			if (!slot.textureStageStates.isEmpty()) {
				if (!first) writer.blankLine();
				first = false;
				for (Map.Entry<D3DTextureStageStateType, Integer> entry : slot.textureStageStates.entrySet()) {
					writer.command("stageState").arguments(entry.getKey(), Direct3DEnums.getValueToString(entry.getKey().typeClass, entry.getValue()));
				}
			}
			
			if (!slot.samplerStates.isEmpty()) {
				if (!first) writer.blankLine();
				first = false;
				for (Map.Entry<D3DSamplerStateType, Integer> entry : slot.samplerStates.entrySet()) {
					writer.command("samplerState").arguments(entry.getKey(), Direct3DEnums.getValueToString(entry.getKey().typeClass, entry.getValue()));
				}
			}
			
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
}
