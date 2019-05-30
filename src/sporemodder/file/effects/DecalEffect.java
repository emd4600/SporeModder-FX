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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureFieldEndian;
import emord.filestructures.StructureLength;
import emord.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
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
	
//	//TODO one of these is type (terrain, water, terrainAndWater, paint, user1 - user8) ??
//			field_8 = in.readInt();  // look at planet.effdir!decal-55
//			field_C = in.readByte();  // lots of effects have errors here
//			field_D = in.readByte();  // look at planet.effdir!decal-204
//	// decal-2, decal-3, decal-72, decal-73, decal-74 -> field_D 1 (water ?) 
	
	public int field_8;
	public byte field_C;
	public byte field_D;
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
		
		field_8 = effect.field_8;
		field_C = effect.field_C;
		field_D = effect.field_D;
		
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

			this.addParser("field_8", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.field_8 = value.intValue();
				}
			}));
			
			this.addParser("field_C", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.field_C = value.byteValue();
				}
			}));
			
			this.addParser("field_D", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.field_D = value.byteValue();
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
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_TEXTURE);
				
				Number value = null;
				if (line.getOptionArguments(args, "repeat", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.textureRepeat = value.floatValue();
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
		
		if (field_8 != 0) writer.command("field_8").arguments(HashManager.get().hexToString(field_8));
		if (field_C != 0) writer.command("field_C").ints(field_C);
		if (field_D != 0) writer.command("field_D").ints(field_D);
		
		writer.command("color").colors(color);
		
		writer.command("alpha").floats(alpha);
		if (alphaVary != 0.0f) writer.option("vary").floats(alphaVary);
		
		writer.command("size").floats(alpha);
		if (sizeVary != 0.0f) writer.option("vary").floats(sizeVary);
		
		writer.command("rotate").floats(rotation);
		if (rotationVary != 0.0f) writer.option("rotate").floats(rotationVary);
		
		writer.command("life").floats(lifeTime);
		
		texture.toArgScript("texture", writer);
		if (textureRepeat != 0.0f) writer.option("repeat").floats(textureRepeat);
		if (textureOffset[0] != 0 && textureOffset[1] != 0) writer.option("offset").vector(textureOffset);
		
		if (!mapEmitColor.isDefault()) {
			writer.command("mapEmitColor").arguments(mapEmitColor);
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapEmitColor));
		return list;
	}
}
