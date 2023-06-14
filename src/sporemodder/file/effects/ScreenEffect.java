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
import java.util.HashMap;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGB;
import sporemodder.util.Vector2;
import sporemodder.util.Vector3;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class ScreenEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ScreenEffect> STRUCTURE_METADATA = StructureMetadata.generate(ScreenEffect.class);
	
	public static final String KEYWORD = "screen";
	public static final int TYPE_CODE = 0x0009;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	@Structure(StructureEndian.BIG_ENDIAN)
	public static class TemporaryFilterBuffer {
		/**
		 * The structure metadata used for reading/writing this class.
		 */
		public static final StructureMetadata<TemporaryFilterBuffer> STRUCTURE_METADATA = StructureMetadata.generate(TemporaryFilterBuffer.class);
		public static final String KEYWORD = "texture";
		
		public int screenRatio = 2;
		public int size = 256;
		
		public TemporaryFilterBuffer() {}
		public TemporaryFilterBuffer(TemporaryFilterBuffer other) {
			screenRatio = other.screenRatio;
			size = other.size;
		}
	}
	
	@Structure(StructureEndian.BIG_ENDIAN)
	public static class ScreenFilter {
		
		/**
		 * The structure metadata used for reading/writing this class.
		 */
		public static final StructureMetadata<ScreenFilter> STRUCTURE_METADATA = StructureMetadata.generate(ScreenFilter.class);
		
		public byte type;
		public byte destination;
		public final ResourceID source = new ResourceID();
		@StructureLength.Value(32) public final List<Byte> parameters = new ArrayList<Byte>();
		
		public ScreenFilter() {}
		public ScreenFilter(ScreenFilter other) {
			type = other.type;
			destination = other.destination;
			source.copy(other.source);
			parameters.addAll(parameters);
		}
	}

	public static final int FLAG_LOOP = 1;
	
	public static final int FLAG_MASK = FLAG_LOOP;
	
	public static final byte TYPE_COPY = 0;
	public static final byte TYPE_COMPRESS = 1;
	public static final byte TYPE_ADD = 2;
	public static final byte TYPE_COLORIZE = 3;
	public static final byte TYPE_BLURX = 4;
	public static final byte TYPE_BLURY = 5;
	public static final byte TYPE_BLUR1D = 6;
	public static final byte TYPE_EDGE = 7;
	public static final byte TYPE_EDGEX = 8;
	public static final byte TYPE_EDGEY = 9;
	public static final byte TYPE_DISTORT = 10;
	//TODO type 11 edgeBlend
	public static final byte TYPE_EXTRACT = 12;
	public static final byte TYPE_MULTIPLY = 13;
	public static final byte TYPE_DILATE = 14;
	public static final byte TYPE_CONTRAST = 15;
	public static final byte TYPE_CUSTOM_MATERIAL = 16;
	public static final byte TYPE_STRENGTH_FADER = 17;
	
	
	public static final ArgScriptEnum ENUM_MODE = new ArgScriptEnum();
	static {
		ENUM_MODE.add(0, "additive");
		ENUM_MODE.add(1, "blend");
		ENUM_MODE.add(2, "tint");
		ENUM_MODE.add(3, "brighten");
		ENUM_MODE.add(4, "skybox");
		ENUM_MODE.add(5, "background");
		ENUM_MODE.add(6, "user1");
		ENUM_MODE.add(7, "user2");
		ENUM_MODE.add(8, "user3");
		ENUM_MODE.add(9, "user4");
		ENUM_MODE.add(10, "filterChain");
	}
	
	public byte mode;
	public int flags;  // & 1F, flags?
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> strength = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<Float> distance = new ArrayList<Float>();
	public float lifeTime = 2.0f;
	public float delay;
	public float falloff;
	public float distanceBase;
	public final ResourceID texture = new ResourceID();
	public short field_68;
	@StructureLength.Value(32) public final List<ScreenFilter> filters = new ArrayList<ScreenFilter>();
	@StructureLength.Value(32) public final List<TemporaryFilterBuffer> filterBuffers = new ArrayList<TemporaryFilterBuffer>();
	@StructureLength.Value(32) public final List<Float> paramsFloat = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Vector3> paramsVector3 = new ArrayList<Vector3>();
	@StructureLength.Value(32) public final List<Vector2> paramsVector2 = new ArrayList<Vector2>();
	@StructureLength.Value(32) public final List<ResourceID> paramsResource = new ArrayList<ResourceID>();
	
	public ScreenEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		ScreenEffect effect = (ScreenEffect) _effect;
		
		mode = effect.mode;
		flags = effect.flags;
		color.addAll(effect.color);
		strength.addAll(effect.strength);
		distance.addAll(effect.distance);
		lifeTime = effect.lifeTime;
		delay = effect.delay;
		falloff = effect.falloff;
		distanceBase = effect.distanceBase;
		texture.copy(effect.texture);
		field_68 = effect.field_68;
		
		// don't need to copy all these. If the user uses 'filterChain' again, the lists will be cleared
		for (ScreenFilter filter : effect.filters) {
			filters.add(new ScreenFilter(filter));
		}
		for (TemporaryFilterBuffer filter : effect.filterBuffers) {
			filterBuffers.add(new TemporaryFilterBuffer(filter));
		}
		paramsFloat.addAll(effect.paramsFloat);
		paramsVector3.addAll(effect.paramsVector3);
		paramsVector2.addAll(effect.paramsVector2);
		paramsResource.addAll(effect.paramsResource);
	}
	
	protected static class Parser extends EffectBlockParser<ScreenEffect> {
		@Override
		protected ScreenEffect createEffect(EffectDirectory effectDirectory) {
			return new ScreenEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags = value.intValue() & ~FLAG_MASK;
				}
			}));
			
			this.addParser("mode", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.mode = (byte) ENUM_MODE.get(args, 0);
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
			
			this.addParser("strength", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.strength.clear();
					stream.parseFloats(args, effect.strength);
				}
			}));
			
			this.addParser("distance", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.distance.clear();
					stream.parseFloats(args, effect.distance);
				}
			}));
			
			this.addParser("delay", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.delay = value.floatValue();
				}
			}));
			
			this.addParser("falloff", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.falloff = value.floatValue();
				}
			}));
			
			this.addParser("distanceBase", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.distanceBase = value.floatValue();
				}
			}));
			
			this.addParser("length", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.lifeTime = value.floatValue();
				}
				
				if (line.hasFlag("loop")) {
					effect.flags |= FLAG_LOOP;
				}
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.texture.parse(args, 0, words);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_TEXTURE, words, 0);
				}
			}));

			this.addParser("field_68", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
					effect.field_68 = value.shortValue();
				}
			}));
			
			this.addParser("filterChain", new ArgScriptBlock<EffectUnit>() {
				
				final HashMap<String, Integer> bufferIndices = new HashMap<String, Integer>();

				@Override
				public void parse(ArgScriptLine line) {
					effect.filterBuffers.clear();
					effect.filters.clear();
					effect.paramsFloat.clear();
					effect.paramsResource.clear();
					effect.paramsVector2.clear();
					effect.paramsVector3.clear();
					
					stream.startBlock(this);
				}
				
				private byte floatParameter(ArgScriptArguments args, int index) {
					Number value = null;
					if ((value = stream.parseFloat(args, index)) != null) {
						effect.paramsFloat.add(value.floatValue());
						return (byte) (effect.paramsFloat.size() - 1);
					}
					else {
						return -1;
					}
				}
				
				private byte sourceParameter(ArgScriptArguments args, int index) {
					ResourceID res = new ResourceID();
					if (parseSource(args, index, res)) {
						effect.paramsResource.add(res);
						return (byte) (effect.paramsResource.size() - 1);
					}
					else {
						return -1;
					}
				}
				
				private byte vector2Parameter(ArgScriptArguments args, int index) {
					float[] arr = new float[2];
					if (stream.parseVector2(args, index, arr)) {
						effect.paramsVector2.add(new Vector2(arr));
						return (byte) (effect.paramsVector2.size() - 1);
					}
					else {
						return -1;
					}
				}
				
				private byte vector3Parameter(ArgScriptArguments args, int index) {
					float[] arr = new float[3];
					if (stream.parseVector3(args, index, arr)) {
						effect.paramsVector3.add(new Vector3(arr));
						return (byte) (effect.paramsVector3.size() - 1);
					}
					else {
						return -1;
					}
				}
				
				private ScreenFilter parseFilter(ArgScriptLine line, byte type) {
					ScreenFilter filter = new ScreenFilter();
					filter.type = type;
					effect.filters.add(filter);
					
					if (line.getArguments(args, 2)) {
						parseSource(args, 0, filter.source);
						filter.destination = parseDestination(args, 1);
					}
					
					return filter;
				}
				
				private boolean parseSource(ArgScriptArguments args, int index, ResourceID dest) {
					dest.setGroupID(0);
					
					String arg = args.get(index);
					if (arg.equals("source")) {
						dest.setInstanceID(0);
					}
					else if (arg.startsWith("particles")) {
						try {
							dest.setInstanceID(Integer.parseInt(arg.substring("particles".length())) + 2);
						}
						catch (Exception e) {
							DocumentError error = new DocumentError(e.getLocalizedMessage(), 
									args.getRealPosition(args.getPosition(index) + "particles".length()),
									args.getEndPosition(index));
							
							args.getStream().addError(error);
							return false;
						}
					}
					else {
						Integer bufferIndex = bufferIndices.get(arg);
						if (bufferIndex == null) {
							String[] words = new String[2];
							dest.parse(args, index, words);
							args.addHyperlink(PfxEditor.HYPERLINK_TEXTURE, words, index);
							dest.parse(args, index);
						}
						else {
							dest.setInstanceID(0x20 | bufferIndex);
						}
					}
					
					return true;
				}
				
				private byte parseDestination(ArgScriptArguments args, int index) {
					
					String arg = args.get(index);
					if (arg.equals("dest")) {
						return 1;
					}
					else if (arg.startsWith("particles")) {
						try {
							return (byte) (Integer.parseInt(arg.substring("particles".length())) + 2);
						}
						catch (Exception e) {
							DocumentError error = new DocumentError(e.getLocalizedMessage(), 
									args.getRealPosition(args.getPosition(index) + "particles".length()),
									args.getEndPosition(index));
							
							args.getStream().addError(error);
							return -1;
						}
					}
					else {
						Integer bufferIndex = bufferIndices.get(arg);
						if (bufferIndex == null) {
							return (byte) args.getStream().parseUByte(args, index).intValue();
						}
						else {
							return (byte) (0x20 | bufferIndex);
						}
					}
				}
				
				@Override
				public void setData(ArgScriptStream<EffectUnit> stream, EffectUnit data) {
					super.setData(stream, data);
					
					this.addParser("texture", ArgScriptParser.create((parser, line) -> {
						Number value = null;
						TemporaryFilterBuffer buffer = new TemporaryFilterBuffer();
						
						if (line.getArguments(args, 1)) {
							bufferIndices.put(args.get(0), effect.filterBuffers.size());
							effect.filterBuffers.add(buffer);
						}
						
						if (line.getOptionArguments(args, "ratio", 1) && (value = stream.parseInt(args, 0)) != null) {
							buffer.screenRatio = value.intValue();
						}
						
						if (line.getOptionArguments(args, "size", 1) && (value = stream.parseInt(args, 0)) != null) {
							buffer.size = value.intValue();
						}
					}));
					
					this.addParser("distort", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_DISTORT);
						
						filter.parameters.add(line.getOptionArguments(args, "distorter", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "offsetX", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "offsetY", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "strength", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "transXY", 1) ? vector2Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "tileXY", 1) ? vector2Parameter(args, 0) : -1);
					}));
					
					this.addParser("blur1d", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_BLUR1D);
						
						filter.parameters.add(line.getOptionArguments(args, "scale", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "scaleX", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "scaleY", 1) ? floatParameter(args, 0) : -1);
					}));
					
					this.addParser("copy", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_COPY);
						
						filter.parameters.add((byte) (line.hasFlag("pointSource") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("blend") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("add") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("multiply") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "sourceAlpha", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "sourceColor", 1) ? vector3Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "maskTexture", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "maskChannel", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "tileXY", 1) ? vector2Parameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("invertMask") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "offsetXY", 1) ? vector2Parameter(args, 0) : -1);
					}));
					
					this.addParser("compress", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_COMPRESS);
						
						filter.parameters.add(line.getOptionArguments(args, "bias", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "scale", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("mono") ? 1 : 0));
					}));
					
					this.addParser("add", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_ADD);
						
						filter.parameters.add(line.getOptionArguments(args, "texture", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("pointSource") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("pointAdd") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "sourceMul", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "addMul", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "tileXY", 1) ? vector2Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "maskTexture", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "maskChannel", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("replace") ? 1 : 0));
					}));
					
					this.addParser("colorize", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_COLORIZE);
						
						filter.parameters.add(line.getOptionArguments(args, "color", 1) ? vector3Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "strength", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "param2", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("param3") ? 1 : 0));
					}));
					
					this.addParser("blurx", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_BLURX);
						
						filter.parameters.add(line.getOptionArguments(args, "scale", 1) ? floatParameter(args, 0) : -1);
					}));
					
					this.addParser("blury", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_BLURY);
						
						filter.parameters.add(line.getOptionArguments(args, "scale", 1) ? floatParameter(args, 0) : -1);
					}));
					
					this.addParser("edge", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_EDGE);
						
						filter.parameters.add((byte) (line.hasFlag("normalMap") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "scale", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("param2") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "param3", 1) ? vector3Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "param4", 1) ? vector3Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "param5", 1) ? floatParameter(args, 0) : -1);
					}));
					
					this.addParser("edgex", ArgScriptParser.create((parser, line) -> {
						parseFilter(line, TYPE_EDGEX);
					}));
					
					this.addParser("edgey", ArgScriptParser.create((parser, line) -> {
						parseFilter(line, TYPE_EDGEY);
					}));
					
					this.addParser("extract", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_EXTRACT);
						
						filter.parameters.add(line.getOptionArguments(args, "color", 1) ? vector3Parameter(args, 0) : -1);
					}));
					
					this.addParser("multiply", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_MULTIPLY);
						
						filter.parameters.add(line.getOptionArguments(args, "texture", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "sourceMul", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "param2", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "tileXY", 1) ? vector2Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "color", 1) ? vector3Parameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("replace") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "offsetXY", 1) ? vector2Parameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "param7", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "param8", 1) ? floatParameter(args, 0) : -1);
					}));
					
					this.addParser("dilate", ArgScriptParser.create((parser, line) -> {
						parseFilter(line, TYPE_DILATE);
					}));
					
					this.addParser("contrast", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_CONTRAST);
						
						filter.parameters.add(line.getOptionArguments(args, "upper", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "lower", 1) ? floatParameter(args, 0) : -1);
					}));
					
					this.addParser("customMaterial", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_CUSTOM_MATERIAL);
						
						// One of the flags is "depthRender", "timeOfDay"
						filter.parameters.add(line.getOptionArguments(args, "materialID", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "sampler0", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "sampler1", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "sampler2", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "sampler3", 1) ? sourceParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams0", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams1", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams2", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams3", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams4", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams5", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams6", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add(line.getOptionArguments(args, "customParams7", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("param13") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "maxDistance", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("param15") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("param16") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("param17") ? 1 : 0));
					}));
					
					this.addParser("strengthFader", ArgScriptParser.create((parser, line) -> {
						ScreenFilter filter = parseFilter(line, TYPE_STRENGTH_FADER);
						
						filter.parameters.add(line.getOptionArguments(args, "length", 1) ? floatParameter(args, 0) : -1);
						filter.parameters.add((byte) (line.hasFlag("fadeIn") ? 1 : 0));
						filter.parameters.add((byte) (line.hasFlag("fadeOut") ? 1 : 0));
						filter.parameters.add(line.getOptionArguments(args, "texture", 1) ? sourceParameter(args, 0) : -1);
					}));
				}
			});
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return ScreenEffect.class;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, ScreenEffect.class));
		}

		@Override
		public boolean onlySupportsInline() {
			return false;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new ScreenEffect(effectDirectory, version);
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
	
	public float getFloat(int index) {
		return paramsFloat.get(index);
	}
	public Vector3 getVector3(int index) {
		return paramsVector3.get(index);
	}
	public Vector2 getVector2(int index) {
		return paramsVector2.get(index);
	}
	public ResourceID getResource(int index) {
		return paramsResource.get(index);
	}
	
	public float getFloat(ScreenFilter filter, int paramIndex) {
		return paramsFloat.get(filter.parameters.get(paramIndex));
	}
	public Vector3 getVector3(ScreenFilter filter, int paramIndex) {
		return paramsVector3.get(filter.parameters.get(paramIndex));
	}
	public Vector2 getVector2(ScreenFilter filter, int paramIndex) {
		return paramsVector2.get(filter.parameters.get(paramIndex));
	}
	public ResourceID getResource(ScreenFilter filter, int paramIndex) {
		return paramsResource.get(filter.parameters.get(paramIndex));
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(name).startBlock();
		
		writer.command("mode").arguments(ENUM_MODE.get(mode));
		if ((flags & ~FLAG_MASK) != 0) writer.command("flags").arguments("0x" + Integer.toHexString(flags & ~FLAG_MASK));
		writer.command("color").colors(color);
		if (!strength.isEmpty()) writer.command("strength").floats(strength);
		if (!distance.isEmpty()) writer.command("distance").floats(distance);
		
		if (lifeTime != 2.0f || (flags & FLAG_LOOP) == FLAG_LOOP) {
			writer.command("length").floats(lifeTime);
			writer.flag("loop", (flags & FLAG_LOOP) == FLAG_LOOP);
		}
		if (delay != 0.0f) writer.command("delay").floats(delay);
		if (falloff != 0.0f) writer.command("falloff").floats(falloff);
		if (distanceBase != 0.0f) writer.command("distanceBase").floats(distanceBase);
		if (!texture.isDefault()) writer.command("texture").arguments(texture);
		if (field_68 != 0.0f) writer.command("field_68").ints(field_68);
		
		if (!filters.isEmpty()) {
			writer.blankLine();
			writer.command("filterChain").startBlock();
			
			for (int i = 0; i < filterBuffers.size(); ++i) {
				TemporaryFilterBuffer obj = filterBuffers.get(i);
				writer.command("texture").arguments("buffer" + Integer.toString(i));
				writer.option("ratio").ints(obj.screenRatio);
				if (obj.size != 256) writer.option("size").ints(obj.size);
			}
			
			for (ScreenFilter filter : filters) {
				writeFilterAS(writer, filter);
			}
			
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
	
	private String getSourceString(ResourceID source) {
		int nameID = source.getInstanceID();
		if (nameID == 0) {
			return "source";
		} 
		else if (nameID > 0 && nameID - 2 <= 3) {
			return "particles" + (nameID - 2);
		} 
		else if ((nameID & 0x20) == 0x20) {
			int index = nameID & ~0x20;
			if (index >= 0 && index <= filterBuffers.size()) {
				return "buffer" + index;
			}
		}
		return source.toString();
	}
	
	private String getDestinationString(int destination) {
		if (destination == 1) {
			return "dest";
		} 
		else if (destination - 2 <= 3) {
			return "particles" + (destination - 2);
		} 
		else if ((destination & 0x20) == 0x20) {
			int index = destination & ~0x20;
			if (index >= 0 && index <= filterBuffers.size()) {
				return "buffer" + index;
			}
		}
	
		return HashManager.get().hexToString(destination);
	}
	
	private String getSourceString(ScreenFilter filter, int paramIndex) {
		return getSourceString(getResource(filter, paramIndex));
	}
	
	private void writeFilterAS(ArgScriptWriter writer, ScreenFilter f) {
		if (f.type == TYPE_DISTORT) {
			writeFilterCommandBase("distort", writer, f);
			if (writeParam(writer, f, 0, "distorter")) writer.arguments(getSourceString(f, 0));
			if (writeParam(writer, f, 1, "offsetX")) writer.floats(getFloat(f, 1));
			if (writeParam(writer, f, 2, "offsetY")) writer.floats(getFloat(f, 2));
			if (writeParam(writer, f, 3, "strength")) writer.floats(getFloat(f, 3));
			if (writeParam(writer, f, 4, "transXY")) writer.vector2(getVector2(f, 4));
			if (writeParam(writer, f, 5, "tileXY")) writer.vector2(getVector2(f, 5));
		}
		else if (f.type == TYPE_BLUR1D) {
			writeFilterCommandBase("blur1d", writer, f);
			if (writeParam(writer, f, 0, "scale")) writer.floats(getFloat(f, 0));
			if (writeParam(writer, f, 1, "scaleX")) writer.floats(getFloat(f, 1));
			if (writeParam(writer, f, 2, "scaleY")) writer.floats(getFloat(f, 2));
		}
		else if (f.type == TYPE_COPY) {
			writeFilterCommandBase("copy", writer, f);
			writeFlag(writer, f, 0, "pointSource");
			writeFlag(writer, f, 1, "blend");
			writeFlag(writer, f, 2, "add");
			writeFlag(writer, f, 3, "multiply");
			if (writeParam(writer, f, 4, "sourceAlpha")) writer.floats(getFloat(f, 4));
			if (writeParam(writer, f, 5, "sourceColor")) writer.vector3(getVector3(f, 5));
			if (writeParam(writer, f, 6, "maskTexture")) writer.arguments(getSourceString(f, 6));
			if (writeParam(writer, f, 7, "maskChannel")) writer.floats(getFloat(f, 7));
			// maybe it's offsetXY and tileXY
			if (writeParam(writer, f, 8, "tileXY")) writer.vector2(getVector2(f, 8));
			writeFlag(writer, f, 9, "invertMask");
			if (writeParam(writer, f, 10, "offsetXY")) writer.vector2(getVector2(f, 10));
		}
		else if (f.type == TYPE_COMPRESS) {
			writeFilterCommandBase("compress", writer, f);
			if (writeParam(writer, f, 0, "bias")) writer.floats(getFloat(f, 0));
			if (writeParam(writer, f, 1, "scale")) writer.floats(getFloat(f, 1));
			writeFlag(writer, f, 2, "mono");
		}
		else if (f.type == TYPE_ADD) {
			writeFilterCommandBase("add", writer, f);
			if (writeParam(writer, f, 0, "texture")) writer.arguments(getSourceString(f, 0));
			writeFlag(writer, f, 1, "pointSource");
			writeFlag(writer, f, 2, "pointAdd");
			if (writeParam(writer, f, 3, "sourceMul")) writer.floats(getFloat(f, 3));
			if (writeParam(writer, f, 4, "addMul")) writer.floats(getFloat(f, 4));
			if (writeParam(writer, f, 5, "tileXY")) writer.vector2(getVector2(f, 5));
			if (writeParam(writer, f, 6, "maskTexture")) writer.arguments(getSourceString(f, 6));
			if (writeParam(writer, f, 7, "param7")) writer.floats(getFloat(f, 7));
			writeFlag(writer, f, 8, "param8");
		}
		else if (f.type == TYPE_COLORIZE) {
			writeFilterCommandBase("colorize", writer, f);
			if (writeParam(writer, f, 0, "color")) writer.vector3(getVector3(f, 0));
			if (writeParam(writer, f, 1, "strength")) writer.floats(getFloat(f, 1));
			if (writeParam(writer, f, 2, "param2")) writer.arguments(getSourceString(f, 2));
			writeFlag(writer, f, 3, "param3");
		}
		else if (f.type == TYPE_BLURX) {
			writeFilterCommandBase("blurx", writer, f);
			if (writeParam(writer, f, 0, "scale")) writer.floats(getFloat(f, 0));
		}
		else if (f.type == TYPE_BLURY) {
			writeFilterCommandBase("blury", writer, f);
			if (writeParam(writer, f, 0, "scale")) writer.floats(getFloat(f, 0));
		}
		else if (f.type == TYPE_EDGE) {
			writeFilterCommandBase("edge", writer, f);
			writeFlag(writer, f, 0, "normalMap");
			if (writeParam(writer, f, 1, "scale")) writer.floats(getFloat(f, 1));
			writeFlag(writer, f, 2, "param2");
			if (writeParam(writer, f, 3, "param3")) writer.vector3(getVector3(f, 3));
			if (writeParam(writer, f, 4, "param4")) writer.vector3(getVector3(f, 4));
			if (writeParam(writer, f, 5, "param5")) writer.floats(getFloat(f, 5));
		}
		else if (f.type == TYPE_EDGEX) {
			writeFilterCommandBase("edgex", writer, f);
		}
		else if (f.type == TYPE_EDGEY) {
			writeFilterCommandBase("edgey", writer, f);
		}
		else if (f.type == TYPE_EXTRACT) {
			writeFilterCommandBase("extract", writer, f);
			if (writeParam(writer, f, 0, "color")) writer.vector3(getVector3(f, 0));
		}
		else if (f.type == TYPE_MULTIPLY) {
			writeFilterCommandBase("multiply", writer, f);
			if (writeParam(writer, f, 0, "texture")) writer.arguments(getSourceString(f, 0));
			if (writeParam(writer, f, 1, "sourceMul")) writer.floats(getFloat(f, 1));
			if (writeParam(writer, f, 2, "param2")) writer.floats(getFloat(f, 2));
			if (writeParam(writer, f, 3, "tileXY")) writer.vector2(getVector2(f, 3));
			if (writeParam(writer, f, 4, "color")) writer.vector3(getVector3(f, 4));
			writeFlag(writer, f, 5, "replace");
			if (writeParam(writer, f, 6, "offsetXY")) writer.vector2(getVector2(f, 6));
			if (writeParam(writer, f, 7, "param7")) writer.floats(getFloat(f, 7));
			if (writeParam(writer, f, 8, "param8")) writer.floats(getFloat(f, 8));
		}
		else if (f.type == TYPE_DILATE) {
			writeFilterCommandBase("dilate", writer, f);
		}
		else if (f.type == TYPE_CONTRAST) {
			writeFilterCommandBase("contrast", writer, f);
			if (writeParam(writer, f, 0, "upper")) writer.floats(getFloat(f, 0));
			if (writeParam(writer, f, 1, "lower")) writer.floats(getFloat(f, 1));
		}
		else if (f.type == TYPE_CUSTOM_MATERIAL) {
			writeFilterCommandBase("customMaterial", writer, f);
			if (writeParam(writer, f, 0, "materialID")) writer.arguments(getSourceString(f, 0));
			if (writeParam(writer, f, 1, "sampler0")) writer.arguments(getSourceString(f, 1));
			if (writeParam(writer, f, 2, "sampler1")) writer.arguments(getSourceString(f, 2));
			if (writeParam(writer, f, 3, "sampler2")) writer.arguments(getSourceString(f, 3));
			if (writeParam(writer, f, 4, "sampler3")) writer.arguments(getSourceString(f, 4));
			if (writeParam(writer, f, 5, "customParams0")) writer.floats(getFloat(f, 5));
			if (writeParam(writer, f, 6, "customParams1")) writer.floats(getFloat(f, 6));
			if (writeParam(writer, f, 7, "customParams2")) writer.floats(getFloat(f, 7));
			if (writeParam(writer, f, 8, "customParams3")) writer.floats(getFloat(f, 8));
			if (writeParam(writer, f, 9, "customParams4")) writer.floats(getFloat(f, 9));
			if (writeParam(writer, f, 10, "customParams5")) writer.floats(getFloat(f, 10));
			if (writeParam(writer, f, 11, "customParams6")) writer.floats(getFloat(f, 11));
			if (writeParam(writer, f, 12, "customParams7")) writer.floats(getFloat(f, 12));
			writeFlag(writer, f, 13, "param13");
			if (writeParam(writer, f, 14, "maxDistance")) writer.floats(getFloat(f, 14));
			writeFlag(writer, f, 15, "param15");
			writeFlag(writer, f, 16, "param16");
			writeFlag(writer, f, 17, "param17");
		}
		else if (f.type == TYPE_STRENGTH_FADER) {
			writeFilterCommandBase("strengthFader", writer, f);
			if (writeParam(writer, f, 3, "texture")) writer.arguments(getSourceString(f, 3));
			if (writeParam(writer, f, 0, "length")) writer.floats(getFloat(f, 0));
			writeFlag(writer, f, 1, "fadeIn");
			writeFlag(writer, f, 2, "fadeOut");
		}
	}
	
	private void writeFilterCommandBase(String keyword, ArgScriptWriter writer, ScreenFilter filter) {
		writer.command(keyword).arguments(getSourceString(filter.source), getDestinationString(filter.destination));
	}
	
	private boolean writeParam(ArgScriptWriter writer, ScreenFilter filter, int param, String option) {
		if (filter.parameters.get(param) != -1) {
			writer.option(option);
			return true;
		} else {
			return false;
		}
	}
	
	private void writeFlag(ArgScriptWriter writer, ScreenFilter f, int param, String option) {
		writer.flag(option, f.parameters.get(param) != 0);
	}
}
