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
package sporemodder.file.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.util.ColorRGB;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class DecalEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<DecalEffect> STRUCTURE_METADATA = StructureMetadata.generate(DecalEffect.class);
	
	public static final String KEYWORD = "decal";
	public static final int TYPE_CODE = 0x0003;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_TEXTURE_REPEAT = 1;  // 1 << 0
	public static final int FLAGS_TERRAIN_SCALE = 2;  // 1 << 1
	public static final int FLAGS_STATIC = 4;  // 1 << 2
	
	public static final int MASK_FLAGS = FLAGS_TEXTURE_REPEAT |
			FLAGS_TERRAIN_SCALE | FLAGS_STATIC;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(0, "terrain");
		ENUM_TYPE.add(1, "water");
		ENUM_TYPE.add(2, "terrainAndWater");
		ENUM_TYPE.add(3, "paint");
		ENUM_TYPE.add(4, "user1");
		ENUM_TYPE.add(5, "user2");
		ENUM_TYPE.add(6, "user3");
		ENUM_TYPE.add(7, "user4");
		ENUM_TYPE.add(8, "user5");
		ENUM_TYPE.add(9, "user6");
		ENUM_TYPE.add(10, "user7");
		ENUM_TYPE.add(11, "user8");
	}
	
	public static final ArgScriptEnum ENUM_LIFE_TYPE = new ArgScriptEnum();
	static {
		ENUM_LIFE_TYPE.add(0, "loop");
		ENUM_LIFE_TYPE.add(1, "single");
		ENUM_LIFE_TYPE.add(2, "sustain");
	}
	
//	//TODO one of these is type (terrain, water, terrainAndWater, paint, user1 - user8) ??
//			field_8 = in.readInt();  // look at planet.effdir!decal-55
//			field_C = in.readByte();  // lots of effects have errors here
//			field_D = in.readByte();  // look at planet.effdir!decal-204
//	// decal-2, decal-3, decal-72, decal-73, decal-74 -> field_D 1 (water ?) 
	
	public int flags;
	public byte type;  // ENUM_TYPE
	public byte lifeType;  // ENUM_LIFE_TYPE
	public final TextureSlot texture = new TextureSlot();
	public float lifeTime;
	@StructureLength.Value(32) public final List<Float> rotation = new ArrayList<Float>(Arrays.asList(0.0f));
	@StructureLength.Value(32) public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> aspectRatio = new ArrayList<Float>(Arrays.asList(1.0f));
	public float rotationVary;
	public float sizeVary;
	public float alphaVary;
	public float textureRepeat = 1.0f;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] textureOffset = new float[2];
	public final ResourceID mapEmitColor = new ResourceID();
	
	public DecalEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		DecalEffect effect = (DecalEffect) _effect;
		
		flags = effect.flags;
		type = effect.type;
		lifeType = effect.lifeType;
		
		lifeTime = effect.lifeTime;
		
		rotation.addAll(effect.rotation);
		size.addAll(effect.size);
		alpha.addAll(effect.alpha);
		color.addAll(effect.color);
		aspectRatio.addAll(effect.aspectRatio);
		
		rotationVary = effect.rotationVary;
		sizeVary = effect.sizeVary;
		alphaVary = effect.alphaVary;
		
		textureRepeat = effect.textureRepeat;
		textureOffset[0] = effect.textureOffset[0];
		textureOffset[1] = effect.textureOffset[1];
		
		mapEmitColor.copy(effect.mapEmitColor);
		texture.copy(effect.texture);
	}
	
	protected static class Parser extends EffectBlockParser<DecalEffect> {
		@Override
		protected DecalEffect createEffect(EffectDirectory effectDirectory) {
			return new DecalEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags = value.intValue() & ~MASK_FLAGS;
				}
			}));
			
			this.addParser("type", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.type = (byte) ENUM_TYPE.get(args, 0);
				}
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGBs(args, effect.color);
				}
			}), "color", "colour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGB255s(args, effect.color);
				}
			}), "color255", "colour255");
			
			this.addParser("aspect", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.aspectRatio.clear();
					stream.parseFloats(args, effect.aspectRatio);
				}
			}));
			
			this.addParser("alpha", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloats(args, effect.alpha);
				}
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaVary = value.floatValue();
				}
			}));
			
			this.addParser("alpha255", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloat255s(args, effect.alpha);
				}
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaVary = value.floatValue();
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.sizeVary = value.floatValue();
				}
				
				if (line.hasFlag("terrainScale")) {
					effect.flags |= FLAGS_TERRAIN_SCALE;
				}
			}));
			
			this.addParser("rotate", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.rotation.clear();
					stream.parseFloats(args, effect.rotation);
				}
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rotationVary = value.floatValue();
				}
			}));
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.lifeTime = value.floatValue();
				}
				
				if (line.hasFlag("paint")) {
					effect.type = 3;
					effect.texture.drawFlags |= 4;
				}
				if (line.hasFlag("static")) {
					effect.texture.drawFlags |= 4;
					effect.flags |= FLAGS_STATIC;
				}
				
				if (line.hasFlag("loop")) {
					effect.lifeType = 0;
				}
				else if (line.hasFlag("single")) {
					effect.lifeType = 1;
				}
				else if (line.hasFlag("sustain")) {
					effect.lifeType = 2;
				}
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_TEXTURE);
				
				Number value = null;
				if (line.getOptionArguments(args, "repeat", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.textureRepeat = value.floatValue();
					effect.flags |= FLAGS_TEXTURE_REPEAT;
				}
				
				if (line.getOptionArguments(args, "offset", 1)) {
					stream.parseVector2(args, 0, effect.textureOffset);
				}
			}));
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				effect.texture.drawMode = TextureSlot.DRAWMODE_NONE;
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_MATERIAL);
				
				Number value = null;
				if (line.getOptionArguments(args, "repeat", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.textureRepeat = value.floatValue();
					effect.flags |= FLAGS_TEXTURE_REPEAT;
				}
				
				if (line.getOptionArguments(args, "offset", 1)) {
					stream.parseVector2(args, 0, effect.textureOffset);
				}
			}));
			
			this.addParser("mapEmitColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.mapEmitColor.parse(args, 0);
				}
			}));
		}
	}
	
	public void toArgScript(ArgScriptWriter writer, int index) {
		writer.startBlock().command("decal").arguments("decal-" + index);
		
		writer.command("life").floats(lifeTime);
		if (textureRepeat != 1.0f) writer.option("repeat").floats(textureRepeat);
		
		writer.endBlock();
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return DecalEffect.class;
		}
		@Override public String getKeyword() {
			return KEYWORD;
		}
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public int getMinVersion() {
			return 1;
		}

		@Override
		public int getMaxVersion() {
			return 1;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, DecalEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new DecalEffect(effectDirectory, version);
		}
		
		@Override
		public boolean onlySupportsInline() {
			return false;
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		STRUCTURE_METADATA.read(this, stream);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		STRUCTURE_METADATA.write(this, stream);
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(name).startBlock();
		
		// Some of the user types are probably: terrainCircle, fastTerrain, light, lightRegion
		
		int maskedFlags = MASK_FLAGS;
		if (maskedFlags != 0) writer.command("flags").arguments("0x" + HashManager.get().hexToString(maskedFlags));
		
		writer.command("type").arguments(ENUM_TYPE.get(type));
		
		if (!(color.size() == 1 && color.get(0).isWhite())) writer.command("color").colors(color);
		
		if (!(alpha.size() == 1 && alpha.get(0) == 1.0f)) writer.command("alpha").floats(alpha);
		if (alphaVary != 0.0f) writer.option("vary").floats(alphaVary);
		
		writer.command("size").floats(size);
		if (sizeVary != 0.0f) writer.option("vary").floats(sizeVary);
		writer.flag("terrainScale", (flags & FLAGS_TERRAIN_SCALE) != 0);
		
		if (rotation.size() > 1 || (rotation.size() == 1 && rotation.get(0) != 0.0f) || rotationVary != 0.0f) {
			writer.command("rotate").floats(rotation);
			if (rotationVary != 0.0f) writer.option("vary").floats(rotationVary);
		}
		
		if (aspectRatio.size() > 1 || (aspectRatio.size() == 1 && aspectRatio.get(0) != 1.0f)) writer.command("aspect").floats(aspectRatio);
		
		writer.command("life").floats(lifeTime);
		writer.flag("static", (flags & FLAGS_STATIC) != 0);
		if ((texture.drawFlags & 4) != 0 && type == 3) writer.option("paint");
		writer.option(ENUM_LIFE_TYPE.get(lifeType));
		
		texture.toArgScript(texture.drawMode == TextureSlot.DRAWMODE_NONE ? "material" : "texture" , writer);
		if ((flags & FLAGS_TEXTURE_REPEAT) != 0) writer.option("repeat").floats(textureRepeat);
		if (textureOffset[0] != 0 && textureOffset[1] != 0) writer.option("offset").vector(textureOffset);
		
		if (!mapEmitColor.isDefault()) {
			writer.command("mapEmitColor").arguments(mapEmitColor);
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapEmitColor));
		if (texture.drawMode == TextureSlot.DRAWMODE_NONE) {
			list.add(effectDirectory.getResource(MaterialResource.TYPE_CODE, texture.resource));
		}
		return list;
	}
}
