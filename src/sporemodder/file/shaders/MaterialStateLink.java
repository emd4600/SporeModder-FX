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
package sporemodder.file.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.rw4.Direct3DEnums;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLMETHOD;
import sporemodder.file.rw4.Direct3DEnums.D3DDECLTYPE;
import sporemodder.file.rw4.Direct3DEnums.D3DPRIMITIVETYPE;
import sporemodder.file.rw4.Direct3DEnums.D3DRenderStateType;
import sporemodder.file.rw4.Direct3DEnums.D3DSamplerStateType;
import sporemodder.file.rw4.Direct3DEnums.D3DTextureStageStateType;
import sporemodder.file.rw4.MaterialStateCompiler;
import sporemodder.file.rw4.MaterialStateCompiler.ShaderDataEntry;
import sporemodder.file.rw4.MaterialStateCompiler.TextureSlot;
import sporemodder.file.rw4.RWObject;
import sporemodder.file.rw4.RWRaster;
import sporemodder.file.rw4.RWTextureOverride;
import sporemodder.file.rw4.RWVertexDescription;
import sporemodder.file.rw4.RWVertexElement;
import sporemodder.file.rw4.RenderWare;
import sporemodder.util.ColorRGB;
import sporemodder.util.ColorRGBA;

public class MaterialStateLink {
	public RenderWare renderWare;
	
	public int materialID;
	// We need this to parse RW4 materials: states that are defined but not used in a 'material' instruction
	public final List<MaterialStateCompiler> definedStates = new ArrayList<>();
	public final List<MaterialStateCompiler> states = new ArrayList<>();
	public final List<ResourceID> textures = new ArrayList<>();
	
	// Only on Darkspore (version == 1)
	public final List<Integer> textureUnks1 = new ArrayList<>();
	public final List<Integer> textureUnks2 = new ArrayList<>();
	
	private final Map<String, MaterialStateCompiler> nameToState = new HashMap<>();
	private final Map<MaterialStateCompiler, String> stateToName = new HashMap<>();
	
	private static <T extends Enum<T>> T parseEnum(Class<T> enumeration, ArgScriptStream<MaterialStateLink> stream, ArgScriptLine line, ArgScriptArguments args, int index) {
		try {
			return Enum.valueOf(enumeration, args.get(index));
		} catch (IllegalArgumentException e) {
			stream.addError(line.createErrorForArgument(args.get(index) + " is not a member of the " + enumeration.getSimpleName() + " enum.", index));
			return null;
		}
	}
	
	private static int parseStateValue(Class<?> typeClass, ArgScriptStream<MaterialStateLink> stream, ArgScriptLine line, ArgScriptArguments args, int index) {
		if (typeClass == int.class) return Optional.of(stream.parseInt(args, index)).orElse(0);
		else if (typeClass == float.class) return Float.floatToRawIntBits(Optional.of(stream.parseFloat(args, index)).orElse(0.0f));
		else if (typeClass == ColorRGBA.class) {
			ColorRGBA color = new ColorRGBA();
			stream.parseColorRGBA(args, index, color);
			return ((int)(color.getA()*255) << 24) | ((int)(color.getR()*255) << 16) | ((int)(color.getG()*255) << 8) | ((int)(color.getB()*255) << 0);
		}
		else {
			Integer value = Direct3DEnums.getStateValue(args.get(index), typeClass);
			if (value == null) {
				stream.addError(line.createErrorForArgument(args.get(index) + " is not a member of the " + typeClass.getSimpleName() + " enum.", index));
				return -1;
			} else {
				return value;
			}
		}
	}
	
	public void reset() {
		materialID = 0;
		definedStates.clear();
		states.clear();
		textures.clear();
		textureUnks1.clear();
		textureUnks2.clear();
		nameToState.clear();
		stateToName.clear();
	}
	
	public void setName(MaterialStateCompiler state, String name) {
		stateToName.put(state, name);
		nameToState.put(name, state);
	}
	
	public void toArgScript(ArgScriptWriter writer) throws IOException {
		for (MaterialStateCompiler state : states) {
			if (!state.isDecompiled()) state.decompile();
			state.toArgScript(stateToName.get(state), writer);
			writer.blankLine();
		}
		writer.command("material").arguments(HashManager.get().getFileName(materialID));
		if (!states.isEmpty()) {
			writer.option("states");
			for (MaterialStateCompiler state : states) {
				writer.arguments(stateToName.get(state));
			}
		}
		if (!textures.isEmpty()) {
			writer.option("textures").arguments(textures);
		}
		if (!textureUnks1.isEmpty()) {
			writer.option("unk1").arguments(textureUnks1);
		}
		if (!textureUnks2.isEmpty()) {
			writer.option("unk2").arguments(textureUnks2);
		}
	}
	
	public ArgScriptStream<MaterialStateLink> generateStream(boolean singleState) {
		
		ArgScriptStream<MaterialStateLink> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser(MaterialStateCompiler.KEYWORD, new ArgScriptBlock<MaterialStateLink>() {
			MaterialStateCompiler currentState;
			
			@Override public void parse(ArgScriptLine line) {
				if (singleState && !states.isEmpty()) {
					stream.addError(line.createError("Already declared one compiled state."));
				}
				else {
					currentState = new MaterialStateCompiler(renderWare);
					
					final ArgScriptArguments args = new ArgScriptArguments();
					if (line.getArguments(args, 1)) {
						nameToState.put(args.get(0), currentState);
					}
					definedStates.add(currentState);
				}
				
				stream.startBlock(this);
			}
			
			@Override public void setData(ArgScriptStream<MaterialStateLink> stream, MaterialStateLink data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				addParser("shaderID", ArgScriptParser.create((parser, line) -> {
					Number value;
					if (line.getArguments(args, 1) && ((value = stream.parseFileID(args, 0)) != null)) currentState.rendererID = value.intValue();
				}));
				
				addParser("primitiveType", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						currentState.primitiveType = parseEnum(D3DPRIMITIVETYPE.class, stream, line, args, 0);
					}
				}));
				
				addParser("materialColor", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						currentState.materialColor = new ColorRGBA();
						stream.parseColorRGBA(args, 0, currentState.materialColor);
					}
				}));
				
				addParser("ambientColor", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						currentState.ambientColor = new ColorRGB();
						stream.parseColorRGB(args, 0, currentState.ambientColor);
					}
				}));
				
				addParser("shaderData", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
						ShaderDataEntry entry = new ShaderDataEntry();
						
						try {
							entry.index = ShaderData.getIndex(args.get(0), false).shortValue();
						} catch (Exception e) {
							stream.addError(line.createErrorForArgument(e.getMessage(), 0));
						}
						
						entry.data = new int[args.size() - 1];
						
						for (int i = 0; i < entry.data.length; ++i) {
							Integer value = stream.parseInt(args, i + 1);
							if (value != null) {
								entry.data[i] = value;
							}
						}
						
						currentState.shaderData.add(entry);
					}
				}));
				
				
				addParser("unkData3", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 17)) {
						currentState.unkData3 = new boolean[17];
						for (int i = 0; i < 17; ++i) {
							Boolean value = stream.parseBoolean(args, i);
							if (value != null) currentState.unkData3[i] = value;
						}
					}
				}));
				
				addParser("renderState", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 2)) {
						D3DRenderStateType state = parseEnum(D3DRenderStateType.class, stream, line, args, 0);
						
						if (!currentState.renderStates.containsKey(0)) {
							currentState.renderStates.put(0, new LinkedHashMap<D3DRenderStateType, Integer>());
						}
						currentState.renderStates.get(0).put(state, parseStateValue(state.typeClass, stream, line, args, 1));
					}
				}));
				
				addParser("statesGroup", new ArgScriptBlock<MaterialStateLink>() {
					private Integer group;
					
					@Override public void parse(ArgScriptLine line) {
						if (line.getArguments(args, 1)) {
							group = stream.parseInt(args, 0);
							if (group != null) {
								currentState.renderStates.put(group, new LinkedHashMap<D3DRenderStateType, Integer>());
							}
						}
						stream.startBlock(this);
					}
					
					@Override public void setData(ArgScriptStream<MaterialStateLink> stream, MaterialStateLink data) {
						super.setData(stream, data);
						
						addParser("renderState", ArgScriptParser.create((parser, line) -> {
							if (group != null && line.getArguments(args, 2)) {
								D3DRenderStateType state = parseEnum(D3DRenderStateType.class, stream, line, args, 0);
								
								currentState.renderStates.get(group).put(state, parseStateValue(state.typeClass, stream, line, args, 1));
							}
						}));
					}
				});
				
				addParser("vertexDescription", new ArgScriptBlock<MaterialStateLink>() {
					@Override public void parse(ArgScriptLine line) {
						if (currentState.vertexDescription != null) {
							stream.addError(line.createError("Can only have one vertex description per state."));
						}
						currentState.vertexDescription = new RWVertexDescription(renderWare);
						
						line.getArguments(args, 0);
						
						Integer value = null;
						if (line.getOptionArguments(args, "field_0", 1) && (value = stream.parseInt(args, 0)) != null) {
							currentState.vertexDescription.field_0 = value;
						}
						if (line.getOptionArguments(args, "field_4", 1) && (value = stream.parseInt(args, 0)) != null) {
							currentState.vertexDescription.field_4 = value;
						}
						if (line.getOptionArguments(args, "field_0E", 1) && (value = stream.parseUByte(args, 0)) != null) {
							currentState.vertexDescription.field_0E = value.byteValue();
						}
//						if (line.getOptionArguments(args, "field_14", 1) && (value = stream.parseInt(args, 0)) != null) {
//							currentState.vertexDescription.field_14 = value;
//						}
						stream.startBlock(this);
					}
					
					@Override public void setData(ArgScriptStream<MaterialStateLink> stream, MaterialStateLink data) {
						super.setData(stream, data);
						
						addParser("element", ArgScriptParser.create((parser, line) -> {
							RWVertexElement element = new RWVertexElement();
							currentState.vertexDescription.elements.add(element);
							
							Number value = null;
							
							if (line.getArguments(args, 2)) {
								int usage = RWVertexElement.VertexInputEnum.get(args, 0);
								if (usage != -1) {
									element.setUsage(usage);
								}
								
								element.type = parseEnum(D3DDECLTYPE.class, stream, line, args, 1);
							}
							
							if (line.getOptionArguments(args, "stream", 1) && (value = stream.parseInt(args, 0)) != null) {
								element.stream = value.intValue();
							}
							if (line.getOptionArguments(args, "method", 1)) {
								element.method = parseEnum(D3DDECLMETHOD.class, stream, line, args, 2);
							}
						}));
					}
					
					@Override public void onBlockEnd() {
						super.onBlockEnd();
						
						int offset = 0;
						for (RWVertexElement element : currentState.vertexDescription.elements) {
							element.offset = offset;
							offset += element.type.size;
							
							currentState.vertexDescription.elementFlags |= 1 << element.typeCode;
							currentState.vertexDescription.elementFlags |= element.getFlags2();
						}
						currentState.vertexDescription.vertexSize = (byte) offset;
					}
				});
				
				addParser("flags1", ArgScriptParser.create((parser, line) -> {
					Integer value = null;
					if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						currentState.extraFlags1 = value;
					}
				}));
				
				addParser("flags3", ArgScriptParser.create((parser, line) -> {
					Integer value = null;
					if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						currentState.extraFlags3 = value;
					}
				}));
				
				addParser("textureSlot", new ArgScriptBlock<MaterialStateLink>() {
					TextureSlot currentSlot;
					
					@Override public void parse(ArgScriptLine line) {
						currentSlot = new TextureSlot();
						
						Integer value;
						if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
							currentSlot.samplerIndex = value;
						}
						currentState.textureSlots.add(currentSlot);
						
						stream.startBlock(this);
					}
					
					@Override public void setData(ArgScriptStream<MaterialStateLink> stream, MaterialStateLink data) {
						super.setData(stream, data);
						
						if (renderWare != null) addParser("raster", ArgScriptParser.create((parser, line) -> {
							Integer value = null;
							if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
								RWObject object = renderWare.get(value);
								if (object.getTypeCode() == RWRaster.TYPE_CODE || object.getTypeCode() == RWTextureOverride.TYPE_CODE) {
									currentSlot.raster = object;
								} else {
									stream.addError(line.createErrorForArgument("RWObject at index " + value + " is not a RWRaster or a RWTextureOverride.", 0));
								}
							}
						}));
						
						addParser("samplerState", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 2)) {
								D3DSamplerStateType state = parseEnum(D3DSamplerStateType.class, stream, line, args, 0);

								currentSlot.samplerStates.put(state, parseStateValue(state.typeClass, stream, line, args, 1));
								
								currentSlot.samplerStatesMask |= 1 << (state.id - 1);
							}
						}));
						
						addParser("stageState", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 2)) {
								D3DTextureStageStateType state = parseEnum(D3DTextureStageStateType.class, stream, line, args, 0);

								currentSlot.textureStageStates.put(state, parseStateValue(state.typeClass, stream, line, args, 1));
								
								currentSlot.stageStatesMask |= 1 << (state.id - 1);
							}
						}));
					}
				});
			}
		});
		
		if (!singleState) {
			stream.addParser("material", ArgScriptParser.create((parser, line) -> {
				final ArgScriptArguments args = new ArgScriptArguments();
				if (materialID != 0) {
					stream.addError(line.createError("Only one material can be exported per file."));
					return;
				}
				
				Integer value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
					materialID = value;
				}
				
				if (line.getOptionArguments(args, "states", 1, Integer.MAX_VALUE)) {
					for (int i = 0; i < args.size(); ++i) {
						MaterialStateCompiler state = nameToState.get(args.get(i));
						if (state != null) {
							states.add(state);
						} else {
							stream.addError(line.createErrorForOptionArgument("states", args.get(i) + " is not a defined state.", i+1));
						}
					}
				}
				
				if (line.getOptionArguments(args, "textures", 1, Integer.MAX_VALUE)) {
					for (int i = 0; i < args.size(); ++i) {
						ResourceID id = new ResourceID();
						id.parse(args, 0);
						textures.add(id);
					}
				}
			}));
		}
		
		return stream;
	}
}
