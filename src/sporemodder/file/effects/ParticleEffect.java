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
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureFieldMethod;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.StructureUnsigned;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.util.ColorRGB;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class ParticleEffect extends EffectComponent {

	/** The structure metadata used for reading/writing this class. */
	public static final StructureMetadata<ParticleEffect> STRUCTURE_METADATA = StructureMetadata.generate(ParticleEffect.class);
	
	public static final String KEYWORD = "particles";
	public static final int TYPE_CODE = 0x0001;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final ArgScriptEnum ENUM_ALIGNMENT = new ArgScriptEnum();
	static {
		ENUM_ALIGNMENT.add(0, "camera");
		ENUM_ALIGNMENT.add(1, "ground");
		ENUM_ALIGNMENT.add(5, "source");
		ENUM_ALIGNMENT.add(2, "dirX");
		ENUM_ALIGNMENT.add(3, "dirY");
		ENUM_ALIGNMENT.add(4, "dirZ");
		ENUM_ALIGNMENT.add(6, "zPole");
		ENUM_ALIGNMENT.add(7, "sunPole");
		ENUM_ALIGNMENT.add(8, "cameraLocation");
	}
	
	public static final ArgScriptEnum ENUM_PHYSICS = new ArgScriptEnum();
	static {
		ENUM_PHYSICS.add(3, "user1");
		ENUM_PHYSICS.add(4, "user2");
		ENUM_PHYSICS.add(5, "user3");
		ENUM_PHYSICS.add(6, "user4");
		ENUM_PHYSICS.add(0, "normal");
		ENUM_PHYSICS.add(1, "unk1");
		ENUM_PHYSICS.add(2, "unk2");
		ENUM_PHYSICS.add(1, "standard");
		ENUM_PHYSICS.add(2, "path");
	}

	public static final int FLAGBIT_INJECT = 0;
	public static final int FLAGBIT_MAINTAIN = 1;
	public static final int FLAGBIT_RATE_SUSTAIN = 2;
	public static final int FLAGBIT_EMIT_BASE = 3;
	public static final int FLAGBIT_SOURCE_ROUND = 4;
	public static final int FLAGBIT_MAP_EMIT_PIN_TO_SURFACE = 5;
	public static final int FLAGBIT_MAP_EMIT_HEIGHT_RANGE = 6;
	public static final int FLAGBIT_MAP_EMIT_DENSITY = 7;
	public static final int FLAGBIT_RATE_SIZE_SCALE = 8;
	public static final int FLAGBIT_RATE_AREA_SCALE = 9;
	public static final int FLAGBIT_RATE_VOLUME_SCALE = 0xa;
	public static final int FLAGBIT_SOURCE_SCALE_PARTICLES = 0xb;
	public static final int FLAGBIT_SURFACES = 0xc;
	public static final int FLAGBIT_MAP_COLLIDE = 0xd;
	public static final int FLAGBIT_MAP_REPEL = 0xe;
	public static final int FLAGBIT_MAP_ADVECT = 0xf;
	public static final int FLAGBIT_MAP_FORCE = 0x10;
	public static final int FLAGBIT_KILL_OUTSIDE_MAP = 0x11;
	public static final int FLAGBIT_MAP_COLLIDE_PIN_TO_MAP = 0x12;
	public static final int FLAGBIT_RANDOM_WALK = 0x13;
	public static final int FLAGBIT_RANDOM_WALK_WAIT = 0x14;
	public static final int FLAGBIT_MODEL = 0x15;
	public static final int FLAGBIT_TEXTURE_ACCEPT_COMPOSITE = 0x16;
	public static final int FLAGBIT_ATTRACTOR = 0x17;
	public static final int FLAGBIT_NOT_PRESET_ATTRACTOR = 0x18;
	public static final int FLAGBIT_EMIT_SCALE_EXISTING = 0x19;
	public static final int FLAGBIT_SOURCE_RESET_INCOMING = 0x1a;
	public static final int FLAGBIT_RATE_KILL = 0x1b;
	public static final int FLAGBIT_RATE_HOLD = 0x1c;
	public static final int FLAGBIT_WARP_SPIRAL = 0x1d;
	public static final int FLAGBIT_LOOP_BOX = 0x1e;
	public static final int FLAGBIT_PATH = 0x1f;
	public static final int FLAGBIT_PROPAGATE_ALWAYS = 0x20;
	public static final int FLAGBIT_PROPAGATE_IF_KILLED = 0x21;
	public static final int FLAGBIT_FRAMES_RELATIVE_SPEED = 0x22;
	public static final int FLAGBIT_COLOR_VARY_RGB = 0x23;
	
	@StructureFieldMethod(read="readFlags", write="writeFlags")
	public final BitSet flags = new BitSet(64);  // actually only 36 values
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] life = new float[2];
	public float prerollTime;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitDelay = {-1, -1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitRetrigger = {-1, -1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitDirectionBBMin = {0, 0, 1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitDirectionBBMax = {0, 0, 1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitSpeed = new float[2];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitVolumeBBMin = new float[3];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitVolumeBBMax = new float[3];
	public float torusWidth = -1;
	
	@StructureLength.Value(32) public final List<Float> rate = new ArrayList<Float>();
	/** In seconds, the time the main effect takes to die and start again. */
	public float rateLoop;
	public short rateCurveCycles;
	public float rateSpeedScale;
	
	@StructureLength.Value(32) public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	public float sizeVary;
	
	@StructureLength.Value(32) public final List<Float> aspectRatio = new ArrayList<Float>(Arrays.asList(1.0f));
	public float aspectRatioVary;
	
	public float rotationVary;
	public float rotationOffset;
	@StructureLength.Value(32) public final List<Float> rotate = new ArrayList<Float>(Arrays.asList(0.0f));
	
	@StructureLength.Value(32) public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	public float alphaVary;
	
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	public ColorRGB colorVary = ColorRGB.black();
	
	public final TextureSlot texture = new TextureSlot();
	
	@StructureUnsigned(8) public int physicsType;
	@StructureUnsigned(8) public int overrideSet;
	@StructureUnsigned(8) public final int[] tileCount = {1, 1};
	@StructureUnsigned(8) public int alignMode;  // byte
	@StructureUnsigned(8) public int frameStart;  // byte
	public byte frameCount;
	public byte frameRandom;
	public float frameSpeed;
	
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] directionForcesSum = new float[3];
	public float windStrength;
	public float gravityStrength;
	public float radialForce;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] radialForceLocation = new float[3];
	public float drag;
	public float velocityStretch;
	public float screwRate;
	
	@StructureLength.Value(32) public final List<ParticleWiggle> wiggles = new ArrayList<ParticleWiggle>();
	
	@StructureUnsigned(8) public int screenBloomAlphaRate;
	@StructureUnsigned(8) public int screenBloomAlphaBase = 255;
	@StructureUnsigned(8) public int screenBloomScaleRate;
	@StructureUnsigned(8) public int screenBloomScaleBase = 255;
	
	@StructureLength.Value(32) public final List<ColorRGB> loopBoxColor = new ArrayList<ColorRGB>();
	@StructureLength.Value(32) public final List<Float> loopBoxAlpha = new ArrayList<Float>();
	
	@StructureFieldMethod(read="readSurfaces", write="writeSurfaces")
	public final List<Surface> surfaces = new ArrayList<Surface>();
	
	public float mapBounce = 1.0f;
	public float mapRepulseHeight;
	public float mapRepulseStrength;
	public float mapRepulseScoutDistance;
	public float mapRepulseVertical;
	public float mapRepulseKillHeight = -1000000000;
	public float probabilityDeath;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] altitudeRange = {-10000, 10000};
	
	public final ResourceID mapForce = new ResourceID();
	public final ResourceID mapEmit = new ResourceID();
	public final ResourceID mapEmitColor = new ResourceID();
	
	public final ParticleRandomWalk randomWalk = new ParticleRandomWalk();
	
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] attractorOrigin = new float[3];
	public final ParticleAttractor attractor = new ParticleAttractor();
	
	@StructureLength.Value(32) public final List<ParticlePathPoint> pathPoints = new ArrayList<ParticlePathPoint>();
	
	
	public ParticleEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		ParticleEffect effect = (ParticleEffect) _effect;
		flags.clear();
		flags.or(effect.flags);
		EffectDirectory.copyArray(life, effect.life);
		prerollTime = effect.prerollTime;
		EffectDirectory.copyArray(emitDelay, effect.emitDelay);
		EffectDirectory.copyArray(emitRetrigger, effect.emitRetrigger);
		EffectDirectory.copyArray(emitDirectionBBMin, effect.emitDirectionBBMin);
		EffectDirectory.copyArray(emitDirectionBBMax, effect.emitDirectionBBMax);
		EffectDirectory.copyArray(emitSpeed, effect.emitSpeed);
		EffectDirectory.copyArray(emitVolumeBBMin, effect.emitVolumeBBMin);
		EffectDirectory.copyArray(emitVolumeBBMax, effect.emitVolumeBBMax);
		torusWidth = effect.torusWidth;
		
		rate.addAll(effect.rate);
		rateLoop = effect.rateLoop;
		rateCurveCycles = effect.rateCurveCycles;
		rateSpeedScale = effect.rateSpeedScale;
		
		size.addAll(effect.size);
		sizeVary = effect.sizeVary;
		aspectRatio.addAll(effect.aspectRatio);
		aspectRatioVary = effect.aspectRatioVary;
		rotate.addAll(effect.rotate);
		rotationVary = effect.rotationVary;
		rotationOffset = effect.rotationOffset;
		alpha.addAll(effect.alpha);
		alphaVary = effect.alphaVary;
		color.addAll(effect.color);
		colorVary.copy(effect.colorVary);
		
		texture.copy(effect.texture);
		
		physicsType = effect.physicsType;
		overrideSet = effect.overrideSet;
		tileCount[0] = effect.tileCount[0];
		tileCount[1] = effect.tileCount[1];
		alignMode = effect.alignMode;
		frameStart = effect.frameStart;
		frameCount = effect.frameCount;
		frameSpeed = effect.frameSpeed;
		frameRandom = effect.frameRandom;
		
		EffectDirectory.copyArray(directionForcesSum, effect.directionForcesSum);
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		radialForce = effect.radialForce;
		EffectDirectory.copyArray(radialForceLocation, effect.radialForceLocation);
		drag = effect.drag;
		velocityStretch = effect.velocityStretch;
		screwRate = effect.screwRate;
		
		for (int i = 0; i < effect.wiggles.size(); i++) wiggles.add(new ParticleWiggle(effect.wiggles.get(i)));
		
		screenBloomAlphaRate = effect.screenBloomAlphaRate;
		screenBloomAlphaBase = effect.screenBloomAlphaBase;
		screenBloomScaleRate = effect.screenBloomScaleRate;
		screenBloomScaleBase = effect.screenBloomScaleBase;
		
		loopBoxColor.addAll(effect.loopBoxColor);
		loopBoxAlpha.addAll(loopBoxAlpha);
		
		for (int i = 0; i < effect.surfaces.size(); i++) surfaces.add(new Surface(effect.surfaces.get(i)));
		
		mapBounce = effect.mapBounce;
		mapRepulseHeight = effect.mapRepulseHeight;
		mapRepulseStrength = effect.mapRepulseStrength;
		mapRepulseScoutDistance = effect.mapRepulseScoutDistance;
		mapRepulseVertical = effect.mapRepulseVertical;
		mapRepulseKillHeight = effect.mapRepulseKillHeight;
		probabilityDeath = effect.probabilityDeath;
		altitudeRange[0] = effect.altitudeRange[0];
		altitudeRange[1] = effect.altitudeRange[1];
		
		mapForce.copy(effect.mapForce);
		mapEmit.copy(effect.mapEmit);
		mapEmitColor.copy(effect.mapEmitColor);
		
		randomWalk.copy(effect.randomWalk);
		
		EffectDirectory.copyArray(attractorOrigin, effect.attractorOrigin);
		attractor.copy(effect.attractor);
		
		for (int i = 0; i < effect.pathPoints.size(); i++) pathPoints.add(new ParticlePathPoint(effect.pathPoints.get(i)));
	}
	
	void readFlags(String fieldName, StreamReader in) throws IOException {
		int flags1 = in.readInt();
		int flags2 = in.readInt();
		for (int i = 0; i < 32; i++) {
			flags.set(i, ((flags2 >> i) & 1) != 0);
		}
		for (int i = 0; i < 32; i++) {
			flags.set(32 + i, ((flags1 >> i) & 1) != 0);
		}
	}
	
	void writeFlags(String fieldName, StreamWriter out, Object value) throws IOException {
		int bitflags2 = 0;
		for (int i = 0; i < 32; i++) {
			if (flags.get(i)) bitflags2 |= 1 << i;
		}
		int bitflags1 = 0;
		for (int i = 0; i < 32; i++) {
			if (flags.get(32 + i)) bitflags1 |= 1 << i;
		}
		out.writeInt(bitflags1);
		out.writeInt(bitflags2);
	}
	
	void readSurfaces(String fieldName, StreamReader in) throws IOException {
		int surfaceCount = in.readInt();
		for (int i = 0; i < surfaceCount; i++) {
			Surface surface = new Surface();
			surface.read(in, effectDirectory);
			surfaces.add(surface);
		}
	}
	
	void writeSurfaces(String fieldName, StreamWriter out, Object value) throws IOException {
		out.writeInt(surfaces.size());
		for (Surface surface : surfaces) {
			surface.write(out, effectDirectory);
		}
	}
	
	protected static class Parser extends EffectBlockParser<ParticleEffect> {
		@Override
		protected ParticleEffect createEffect(EffectDirectory effectDirectory) {
			return new ParticleEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGBs(args, effect.color);
				}
				if (line.getOptionArguments(args, "vary", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
				}
				if (line.getOptionArguments(args, "varyRGB", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
					effect.flags.set(FLAGBIT_COLOR_VARY_RGB);
				}
			}), "color", "colour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGB255s(args, effect.color);
				}
				if (line.getOptionArguments(args, "vary", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
				}
				if (line.getOptionArguments(args, "varyRGB", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
					effect.flags.set(FLAGBIT_COLOR_VARY_RGB);
				}
			}), "color255", "colour255");
			
			this.addParser("alpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloats(args, effect.alpha);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaVary = value.floatValue();
				}
			}));
			
			this.addParser("alpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloat255s(args, effect.alpha);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaVary = value.floatValue();
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.sizeVary = value.floatValue();
				}
			}));
			
			this.addParser("aspect", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.aspectRatio.clear();
					stream.parseFloats(args, effect.aspectRatio);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.aspectRatioVary = value.floatValue();
				}
			}));
			
			this.addParser("rotate", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.rotate.clear();
					stream.parseFloats(args, effect.rotate);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rotationVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rotationOffset = value.floatValue();
				}
			}));
			
			this.addParser("stretch", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.velocityStretch = value.floatValue();
				}
			}));
			
			parseSource();
			parseEmit();
			parseForce();
			parseWarp();
			parseWalk();
			parseLife();
			parseRate();
			parseInject();
			parseMaintain();
			parseMaterial();
			parseTexture();
			parseModel();
			parseFrames();
			
			this.addParser("align", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.alignMode = (byte) ENUM_ALIGNMENT.get(args, 0);
				}
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOP_BOX);
					effect.loopBoxColor.clear();
					stream.parseColorRGBs(args, effect.loopBoxColor);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}), "loopBoxColor", "loopBoxColour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOP_BOX);
					effect.loopBoxColor.clear();
					stream.parseColorRGB255s(args, effect.loopBoxColor);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}), "loopBoxColor255", "loopBoxColour255");
			
			this.addParser("loopBoxAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOP_BOX);
					effect.loopBoxAlpha.clear();
					stream.parseFloats(args, effect.loopBoxAlpha);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}));
			
			this.addParser("loopBoxAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOP_BOX);
					effect.loopBoxAlpha.clear();
					stream.parseFloat255s(args, effect.loopBoxAlpha);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}));
			
			this.addParser("surface", ArgScriptParser.create((parser, line) -> {
				if (line.hasFlag("reset")) {
					effect.surfaces.clear();
				}
				
				Surface surface = new Surface();
				surface.parse(stream, line);
				effect.surfaces.add(surface);
				
				effect.flags.set(FLAGBIT_SURFACES);
			}));
			
			parseEmitMap();
			
			this.addParser("mapEmitColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapEmitColor.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_MAP, words, 0);
				}
			}));
			
			parseCollideMap();
			parseRepelMap();
			parseAdvectMap();
			parseForceMap();
			
			this.addParser("physics", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.physicsType = (byte) ENUM_PHYSICS.get(args, 0);
				}
			}));
			
			this.addParser("path", ArgScriptParser.create((parser, line) -> {
				effect.flags.set(FLAGBIT_PATH);
				ParticlePathPoint point = new ParticlePathPoint();
				point.parse(stream, line, effect.pathPoints, effect.pathPoints.size());
				effect.pathPoints.add(point);
			}));
			
//			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
//				Number value;
//				if (line.getArguments(args, 1) && (value = stream.parseUInt(args, 0)) != null) {
//					effect.flags |= value.intValue() & ~FLAGMASK;
//				}
//			}));
		}
		
		private void parseSource() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("source", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				Number value2 = null;
				
				// disable round source
				effect.flags.clear(FLAGBIT_SOURCE_ROUND);
				float min_x = 0;
				float min_y = 0;
				float min_z = 0;
				float max_x = 0;
				float max_y = 0;
				float max_z = 0;
	
				// Ensure it has no arguments
				line.getArguments(args, 0);
				
				if (!line.hasFlag("point")) {
					
					if (line.getOptionArguments(args, "square", 1) && (value = stream.parseFloat(args, 0)) != null) {
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						max_x += size;
						max_y += size;
					}
					else if (line.getOptionArguments(args, "quad", 1)) {
						float[] array = new float[2];
						if (stream.parseVector2(args, 0, array)) {
							min_x -= array[0];
							min_y -= array[1];
							max_x += array[0];
							max_y += array[1];
						}
					}
					else if (line.getOptionArguments(args, "cube", 1) && (value = stream.parseFloat(args, 0)) != null) {
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						min_z -= size;
						max_x += size;
						max_y += size;
						max_z += size;
					}
					else if (line.getOptionArguments(args, "box", 1)) {
						float[] array = new float[3];
						if (stream.parseVector3(args, 0, array)) {
							min_x -= array[0];
							min_y -= array[1];
							min_z -= array[2];
							max_x += array[0];
							max_y += array[1];
							max_z += array[2];
						}
					}
					else if (line.getOptionArguments(args, "circle", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.flags.set(FLAGBIT_SOURCE_ROUND);
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						max_x += size;
						max_y += size;
					}
					else if (line.getOptionArguments(args, "sphere", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.flags.set(FLAGBIT_SOURCE_ROUND);
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						min_z -= size;
						max_x += size;
						max_y += size;
						max_z += size;
					}
					else if (line.getOptionArguments(args, "ellipse", 1) || line.getOptionArguments(args, "ellipsoid", 1)) {
						effect.flags.set(FLAGBIT_SOURCE_ROUND);
						float[] array = new float[3];
						if (stream.parseVector3(args, 0, array)) {
							min_x -= array[0];
							min_y -= array[1];
							min_z -= array[2];
							max_x += array[0];
							max_y += array[1];
							max_z += array[2];
						}
					}
					else if (line.getOptionArguments(args, "ring", 2) && 
							(value = stream.parseFloat(args, 0)) != null && 
							(value2 = stream.parseFloat(args, 1, 0.0f, 1.0f)) != null) {
						effect.torusWidth = value2.floatValue();
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						max_x += size;
						max_y += size;
					}
					else if (line.getOptionArguments(args, "torus", 2) && (value = stream.parseFloat(args, 1, 0.0f, 1.0f)) != null) {
						effect.torusWidth = value.floatValue();
						
						float[] array = new float[3];
						if (stream.parseVector3(args, 0, array)) {
							min_x -= array[0];
							min_y -= array[1];
							min_z -= array[2];
							max_x += array[0];
							max_y += array[1];
							max_z += array[2];
						}
					}
				}
				
				if (line.getOptionArguments(args, "offset", 1)) {
					float[] offset = new float[3];
					if (stream.parseVector3(args, 0, offset)) {
						min_x += offset[0];
						max_x += offset[0];
						min_y += offset[1];
						max_y += offset[1];
						min_z += offset[2];
						max_z += offset[2];
					}
				}
				
				effect.emitVolumeBBMin[0] = min_x;
				effect.emitVolumeBBMin[1] = min_y;
				effect.emitVolumeBBMin[2] = min_z;
				effect.emitVolumeBBMax[0] = max_x;
				effect.emitVolumeBBMax[1] = max_y;
				effect.emitVolumeBBMax[2] = max_z;
				
				if (line.hasFlag("scaleParticles")) {
					effect.flags.set(FLAGBIT_SOURCE_SCALE_PARTICLES);
				}
				if (line.hasFlag("resetIncoming")) {
					effect.flags.set(FLAGBIT_SOURCE_RESET_INCOMING);
				}
			}));
		}
		
		private void parseEmit() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("emit", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getOptionArguments(args, "speed", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					float vary = 0;
					float speed = value.floatValue();
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						vary = value.floatValue();
					}
					
					effect.emitSpeed[0] = speed - vary;
					effect.emitSpeed[1] = speed + vary;
				}
				
				if (line.getOptionArguments(args, "dir", 1, 2)) {
					float[] point = new float[3];
					float[] vary = new float[3];
					
					stream.parseVector3(args, 0, point);
					if (args.size() == 2) stream.parseVector3(args, 1, vary);
					
					float normalize = (float) Math.sqrt(point[0]*point[0] + point[1]*point[1] + point[2]*point[2]);
					effect.emitDirectionBBMin[0] = point[0] * normalize - vary[0];
					effect.emitDirectionBBMin[1] = point[1] * normalize - vary[1];
					effect.emitDirectionBBMin[2] = point[2] * normalize - vary[2];
					effect.emitDirectionBBMax[0] = point[0] * normalize + vary[0];
					effect.emitDirectionBBMax[1] = point[1] * normalize + vary[1];
					effect.emitDirectionBBMax[2] = point[2] * normalize + vary[2];
				}
				
				if (line.hasFlag("base")) effect.flags.set(FLAGBIT_EMIT_BASE);
				if (line.hasFlag("scaleExisting")) effect.flags.set(FLAGBIT_EMIT_SCALE_EXISTING);
			}));
		}
		
		private void parseForce() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("force", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.hasFlag("reset")) {
					effect.directionForcesSum[0] = 0;
					effect.directionForcesSum[1] = 0;
					effect.directionForcesSum[2] = 0;
				}
				
				if (line.getOptionArguments(args, "gravity", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.directionForcesSum[2] -= value.floatValue();
				}
				if (line.getOptionArguments(args, "wind", 1, 2)) {
					// make it a unit vector
					float[] vec = new float[3];
					if (!stream.parseVector3(args, 0, vec)) return;
					float invMod = (float) (1 / Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]));
					
					if (args.size() > 1 && (value = stream.parseFloat(args, 1)) != null) {
						invMod *= value.floatValue();
					}
					
					effect.directionForcesSum[0] += vec[0] * invMod;
					effect.directionForcesSum[1] += vec[1] * invMod;
					effect.directionForcesSum[2] += vec[2] * invMod;
				}
				if (line.getOptionArguments(args, "worldWind", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.windStrength = value.floatValue();
				}
				if (line.getOptionArguments(args, "worldGravity", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.gravityStrength = value.floatValue();
				}
				if (line.getOptionArguments(args, "bomb", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					effect.radialForce = value.floatValue();
					
					if (args.size() == 2) {
						stream.parseVector3(args, 1, effect.radialForceLocation);
					}
				}
				if (line.getOptionArguments(args, "drag", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.drag = value.floatValue();
				}

				// Attractors
				int index = 0;
				boolean validArguments = false;
				
				if (line.getOptionArguments(args, "attractor", 3, Integer.MAX_VALUE)) {
					stream.parseVector3(args, 0, effect.attractorOrigin);
					
					index++;
					effect.flags.set(FLAGBIT_ATTRACTOR);
					effect.flags.set(FLAGBIT_NOT_PRESET_ATTRACTOR);
					validArguments = true;
				}
				else if (line.getOptionArguments(args, "presetAttractor", 2, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_ATTRACTOR);
					effect.flags.clear(FLAGBIT_NOT_PRESET_ATTRACTOR);
					validArguments = true;
				}
				
				if (validArguments && (value = stream.parseFloat(args, index++)) != null) {
					effect.attractor.range = value.floatValue();
					
					for (; index < args.size(); index++) {
						if ((value = stream.parseFloat(args, index)) != null) {
							effect.attractor.attractorStrength.add(value.floatValue());
						}
						else {
							// Don't keep parsing if there is an error
							break;
						}
					}
					
					if (line.getOptionArguments(args, "killRange", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.attractor.killRange = value.floatValue();
					}
				}
			}));
		}
		
		private void parseWarp() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("warp", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				// Ensure it does not have arguments
				line.getArguments(args, 0);
				
				if (line.getOptionArguments(args, "screw", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.screwRate = value.floatValue();
				}
				if (line.getOptionArguments(args, "spiral", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.screwRate = value.floatValue();
					effect.flags.set(FLAGBIT_WARP_SPIRAL);
				}
				
				if (line.getOptionArguments(args, "wiggle", 4)) {
					ParticleWiggle item = new ParticleWiggle();
					item.rateDirection[0] = 0.0f;
					item.rateDirection[1] = 0.0f;
					item.rateDirection[2] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					item.timeRate = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0f);
					float scale = Optional.ofNullable(stream.parseFloat(args, 2)).orElse(0f);
					float ratio = Optional.ofNullable(stream.parseFloat(args, 3, 0f, 1f)).orElse(0f);
					item.wiggleDirection[0] = (float)Math.cos(ratio * Math.PI * 2) * scale;
					item.wiggleDirection[1] = (float)Math.sin(ratio * Math.PI * 2) * -scale;
					item.wiggleDirection[2] = 0f;
					effect.wiggles.add(item);
				}
				
				if (line.getOptionArguments(args, "wiggleDir", 2, 3) && (value = stream.parseFloat(args, 0)) != null) {
					ParticleWiggle item = new ParticleWiggle();
					item.timeRate = value.floatValue();
					
					stream.parseVector3(args, 1, item.wiggleDirection);
					
					if (args.size() == 3) {
						stream.parseVector3(args, 2, item.rateDirection);
					}
					
					effect.wiggles.add(item);
				}
				
				Number value2 = null;
				if (line.getOptionArguments(args, "bloomAlpha", 2) && 
						(value = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null && 
						(value2 = stream.parseFloat(args, 1, 0.0f, 16.0f)) != null) {
					
					effect.screenBloomAlphaBase = (int) Math.min(Math.max(value.floatValue(), 0) * 255, 255);
					// this goes from 0 to 16
					effect.screenBloomAlphaRate = (int) Math.min(Math.max(value2.floatValue() * 0.0625, 0) * 255, 255);
					
					// Cloud effects have 'reverseAlpha'
				}
				if (line.getOptionArguments(args, "bloomSize", 2) && 
						(value = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null && 
						(value2 = stream.parseFloat(args, 1, 0.0f, 16.0f)) != null) {
					
					effect.screenBloomScaleBase = (int) Math.min(Math.max(value.floatValue(), 0) * 255, 255);
					// this goes from 0 to 16
					effect.screenBloomScaleRate = (int) Math.min(Math.max(value2.floatValue() * 0.0625, 0) * 255, 255);
				}
			}));
		}
		
		private class RandomWalkParser extends ArgScriptParser<EffectUnit> {
			private boolean isDirectedWalk;
			private RandomWalkParser(boolean isDirectedWalk) {
				this.isDirectedWalk = isDirectedWalk;
			}
			
			@Override
			public void parse(ArgScriptLine line) {
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				
				if (isDirectedWalk) {
					if (line.getArguments(args, 0, Integer.MAX_VALUE) && args.size() != 0) {
						for (int i = 0; i < args.size(); i++) {
							if ((value = stream.parseFloat(args, i, -1.0f, 1.0f)) != null) {
								effect.randomWalk.turnOffsetCurve.add(value.floatValue());
							}
							else {
								// Stop parsing if there is an error
								break;
							}
						}
					}
				} else {
					line.getArguments(args, 0);
				}
				
				effect.flags.set(FLAGBIT_RANDOM_WALK);
				
				if (line.getOptionArguments(args, "delay", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					float delay = value.floatValue();
					float vary = 0;
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						vary = value.floatValue();
					}
					
					effect.randomWalk.time[0] = delay - vary;
					effect.randomWalk.time[1] = delay + vary;
				}
				
				if (line.getOptionArguments(args, "strength", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					float strength = value.floatValue();
					float vary = 0;
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						vary = value.floatValue();
					}
					
					effect.randomWalk.strength[0] = strength - vary;
					effect.randomWalk.strength[1] = strength + vary;
				}
				
				if (line.getOptionArguments(args, isDirectedWalk ? "randomTurn" : "turn", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					effect.randomWalk.turnRange = value.floatValue();
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						effect.randomWalk.turnOffset = value.floatValue();
					}
				}
				
				if (line.getOptionArguments(args, "mix", 1) && (value = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null) {
					effect.randomWalk.mix = value.floatValue();
				}
				
				effect.randomWalk.loopType = 2;
				
				if (isDirectedWalk) {
					if (line.hasFlag("sustain")) effect.randomWalk.loopType = 1;
					else if (line.hasFlag("loop")) effect.randomWalk.loopType = 0;
				}
				
				if (line.hasFlag("wait")) effect.flags.set(FLAGBIT_RANDOM_WALK_WAIT);
			}
		}
		
		private void parseWalk() {
			this.addParser("directedWalk", new RandomWalkParser(true));
			this.addParser("randomWalk", new RandomWalkParser(false));
		}
		
		private void parseLife() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					float life = value.floatValue();
					float vary = 0;
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						vary = value.floatValue();
					}
					
					effect.life[0] = life - vary;
					effect.life[1] = life + vary;
					
					if (line.getOptionArguments(args, "preroll", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.prerollTime = value.floatValue();
					}
					else {
						effect.prerollTime = life > 0.5f ? life : 0.5f;
					}
				}
				
				if (line.hasFlag("propagateAlways")) {
					effect.flags.set(FLAGBIT_PROPAGATE_ALWAYS);
				}
				if (line.hasFlag("propagateIfKilled")) {
					effect.flags.set(FLAGBIT_PROPAGATE_IF_KILLED);
				}
			}));
		}
		
		private void parseRateMain(ArgScriptLine line) {
			final ArgScriptArguments args = new ArgScriptArguments();
			Number value = null;
			
			if (line.getOptionArguments(args, "loop", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.rateLoop = value.floatValue();
				effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(0).shortValue();
			}
			else if (line.getOptionArguments(args, "single", 0, 1)) {
				effect.rateCurveCycles = 1;
				effect.rateLoop = Optional.ofNullable(args.size() == 1 ? stream.parseFloat(args, 0) : null).orElse(0.1f);
			}
			else if (line.getOptionArguments(args, "sustain", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags.set(FLAGBIT_RATE_SUSTAIN);
				effect.rateLoop = value.floatValue();
				effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(1).shortValue();
			}
			else if (line.getOptionArguments(args, "hold", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags.set(FLAGBIT_RATE_HOLD);
				effect.rateLoop = value.floatValue();
				effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(1).shortValue();
			}
			else if (line.getOptionArguments(args, "kill", 0, 1)) {
				effect.flags.set(FLAGBIT_RATE_KILL);
				effect.flags.set(FLAGBIT_PROPAGATE_ALWAYS);
				effect.rateLoop = Optional.ofNullable(args.size() == 1 ? stream.parseFloat(args, 0) : null).orElse(0.1f);
				effect.rateCurveCycles = 1;
			}
			
			if (line.hasFlag("sizeScale")) effect.flags.set(FLAGBIT_RATE_SIZE_SCALE);
			if (line.hasFlag("areaScale")) effect.flags.set(FLAGBIT_RATE_AREA_SCALE);
			if (line.hasFlag("volumeScale")) effect.flags.set(FLAGBIT_RATE_VOLUME_SCALE);
			
			if (line.getOptionArguments(args, "speedScale", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.rateSpeedScale = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "delay", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.emitDelay[0] = effect.emitDelay[1] = value.floatValue();
				
				if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
					effect.emitDelay[1] = value.floatValue();
				}
			}
			
			if (line.getOptionArguments(args, "trigger", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.emitRetrigger[0] = effect.emitRetrigger[1] = value.floatValue();
				
				if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
					effect.emitRetrigger[1] = value.floatValue();
				}
			}
		}
		
		private void parseRate() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("rate", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.rate.clear();
					stream.parseFloats(args, effect.rate);
				}
				
				parseRateMain(line);
			}));
		}
		
		private void parseInject() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("inject", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rate.clear();
					effect.rate.add(value.floatValue());
				}
				
				effect.flags.set(FLAGBIT_INJECT);
				effect.rateLoop = 0.01f;
				effect.rateCurveCycles = 1;
				parseRateMain(line);
			}));
		}
		
		private void parseMaintain() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("maintain", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rate.clear();
					effect.rate.add(value.floatValue());
				}
				
				effect.flags.set(FLAGBIT_MAINTAIN);

				if (line.getOptionArguments(args, "delay", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					effect.emitDelay[0] = effect.emitDelay[1] = value.floatValue();
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						effect.emitDelay[1] = value.floatValue();
					}
				}
				
				if (line.getOptionArguments(args, "hold", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					effect.flags.set(FLAGBIT_RATE_HOLD);
					effect.rateLoop = value.floatValue();
					effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(1).shortValue();
				}
				else if (line.getOptionArguments(args, "kill", 0, 1)) {
					effect.flags.set(FLAGBIT_RATE_KILL);
					effect.flags.set(FLAGBIT_PROPAGATE_ALWAYS);
					effect.rateLoop = Optional.ofNullable(args.size() == 1 ? stream.parseFloat(args, 0) : null).orElse(0.1f);
					effect.rateCurveCycles = 1;
				}
			}));
		}
		
		private void parseMaterial() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.texture.resource.parse(args, 0);
				}
				
				effect.texture.drawMode = TextureSlot.DRAWMODE_NONE;
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_MATERIAL);
				
				effect.flags.clear(FLAGBIT_MODEL);
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
			}));
		}
		
		private static boolean isPowerOfTwo(int value) {
			return ((value - 1) & 0xFFFFFF00) != 0 ||
					(value & (value - 1)) != 0;
		}
		
		private void parseTexture() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.texture.resource.parse(args, 0);
				}
				
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_TEXTURE);
				
				effect.flags.set(FLAGBIT_MODEL);
				
				Number value = null;
				
				if (line.getOptionArguments(args, "tile", 1, 2) && (value = stream.parseByte(args, 0)) != null) {
					effect.tileCount[0] = value.byteValue();
					effect.tileCount[1] = Optional.ofNullable(args.size() == 2 ? stream.parseByte(args, 1) : null).orElse((byte) 1);
					
					if (!isPowerOfTwo(effect.tileCount[0])) {
						stream.addError(line.createErrorForOptionArgument("tile", "Tile counts must be powers of two", 0));
					}
					if (!isPowerOfTwo(effect.tileCount[1])) {
						stream.addError(line.createErrorForOptionArgument("tile", "Tile counts must be powers of two", 1));
					}
				}
				
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				if (line.hasFlag("acceptComposite")) effect.flags.set(FLAGBIT_TEXTURE_ACCEPT_COMPOSITE);
			}));
		}
		
		private void parseModel() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("model", ArgScriptParser.create((parser, line) -> {
				effect.texture.drawMode = 0;
				effect.flags.set(FLAGBIT_MODEL);
				
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.texture.resource.parse(args, 0);
				}
				
				if (line.getOptionArguments(args, "material", 1)) {
					effect.texture.resource2.parse(args, 0);
					effect.texture.drawMode = TextureSlot.DRAWMODE_NONE;
				}
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				effect.texture.drawFlags |= TextureSlot.DRAWFLAG_SHADOW;
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_FILE);
			}));
		}
		
		private void parseFrames() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("frames", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getOptionArguments(args, "tile", 1, 2) && (value = stream.parseByte(args, 0)) != null) {
					effect.tileCount[0] = value.byteValue();
					effect.tileCount[1] = Optional.ofNullable(args.size() == 2 ? stream.parseByte(args, 1) : null).orElse((byte) 1);
					
					if (!isPowerOfTwo(effect.tileCount[0])) {
						stream.addError(line.createErrorForOptionArgument("tile", "Tile counts must be powers of two", 0));
					}
					if (!isPowerOfTwo(effect.tileCount[1])) {
						stream.addError(line.createErrorForOptionArgument("tile", "Tile counts must be powers of two", 1));
					}
				}
				
				if (line.getOptionArguments(args, "speed", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.frameSpeed = value.floatValue();
					effect.flags.clear(FLAGBIT_FRAMES_RELATIVE_SPEED);
				}
				if (line.getOptionArguments(args, "speed", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.frameSpeed = value.floatValue();
					effect.flags.set(FLAGBIT_FRAMES_RELATIVE_SPEED);
				}
				
				if (line.getOptionArguments(args, "count", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.frameCount = value.byteValue();
				}
				
				if (line.getOptionArguments(args, "start", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.frameStart = value.byteValue();
				}
				
				if (line.getOptionArguments(args, "random", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.frameRandom = value.byteValue();
				}
			}));
		}
		
		private void parseEmitMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapEmit", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapEmit.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_MAP, words, 0);
				}
				if (line.getOptionArguments(args, "belowHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[1] = value.floatValue();
					effect.flags.set(FLAGBIT_MAP_EMIT_HEIGHT_RANGE);
				}
				if (line.getOptionArguments(args, "aboveHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[0] = value.floatValue();
					effect.flags.set(FLAGBIT_MAP_EMIT_HEIGHT_RANGE);
				}
				if (line.getOptionArguments(args, "heightRange", 2)) {
					if ((value = stream.parseFloat(args, 0)) != null) {
						effect.altitudeRange[0] = value.floatValue();
					}
					if ((value = stream.parseFloat(args, 1)) != null) {
						effect.altitudeRange[1] = value.floatValue();
					}
					effect.flags.set(FLAGBIT_MAP_EMIT_HEIGHT_RANGE);
				}
				
				if (line.hasFlag("pinToSurface")) effect.flags.set(FLAGBIT_MAP_EMIT_PIN_TO_SURFACE);
				if (line.hasFlag("density")) effect.flags.set(FLAGBIT_MAP_EMIT_DENSITY);
			}));
		}
		
		private void parseCollideMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapCollide", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 0, 1)) {
					if (args.size() == 1) {
						String[] words = new String[2];
						effect.mapForce.parseSpecial(args, 0, words);
						line.addHyperlinkForArgument(PfxEditor.HYPERLINK_MAP, words, 0);
					}
					else {
						effect.mapForce.setGroupID(0);
						effect.mapForce.setInstanceID(0);
					}
				}
				
				effect.flags.set(FLAGBIT_MAP_COLLIDE);
				
				if (line.hasFlag("pinToMap")) {
					effect.flags.set(FLAGBIT_MAP_COLLIDE_PIN_TO_MAP);
					effect.mapBounce = 0;
				}
				else if (line.getOptionArguments(args, "bounce", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapBounce = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags.set(FLAGBIT_KILL_OUTSIDE_MAP);

				if (line.getOptionArguments(args, "death", 1) && (value = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null) {
					effect.probabilityDeath = value.floatValue();
				}
			}));
		}
		
		private void parseRepelMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapRepel", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 2, 3)) {
					int index = 0;
					
					if (args.size() == 3) {
						String[] words = new String[2];
						effect.mapForce.parseSpecial(args, 0, words);
						line.addHyperlinkForArgument(PfxEditor.HYPERLINK_MAP, words, 0);
						index++;
					}
					else {
						effect.mapForce.setGroupID(0);
						effect.mapForce.setInstanceID(0);
					}
					
					if ((value = stream.parseFloat(args, index++)) != null) {
						effect.mapRepulseHeight = value.floatValue();
					}
					if ((value = stream.parseFloat(args, index++)) != null) {
						effect.mapRepulseStrength = value.floatValue();
					}
				}
				
				effect.flags.set(FLAGBIT_MAP_COLLIDE);
				effect.flags.set(FLAGBIT_MAP_REPEL);

				if (line.getOptionArguments(args, "scout", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseScoutDistance = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "vertical", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseVertical = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "killHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseKillHeight = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags.set(FLAGBIT_KILL_OUTSIDE_MAP);

			}));
		}
		
		private void parseAdvectMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapAdvect", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapForce.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_MAP, words, 0);
				}
				
				effect.flags.set(FLAGBIT_MAP_COLLIDE);
				effect.flags.set(FLAGBIT_MAP_ADVECT);

				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags.set(FLAGBIT_KILL_OUTSIDE_MAP);

			}));
		}
		
		private void parseForceMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapForce", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapForce.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_MAP, words, 0);
				}
				
				effect.flags.set(FLAGBIT_MAP_COLLIDE);
				effect.flags.set(FLAGBIT_MAP_FORCE);

				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags.set(FLAGBIT_KILL_OUTSIDE_MAP);

			}));
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return ParticleEffect.class;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new ParticleEffect(effectDirectory, version);
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
		toArgScript(writer, KEYWORD);
	}
			
	protected void toArgScript(ArgScriptWriter writer, String keyword) {
		writer.command(keyword).arguments(name).startBlock();
		
		if (!writer.isDefaultColor(color) || !colorVary.isBlack() || flags.get(FLAGBIT_COLOR_VARY_RGB)) {
			writer.command("color").colors(color);
			if (flags.get(FLAGBIT_COLOR_VARY_RGB)) writer.option("varyRGB").color(colorVary);
			else if (!colorVary.isBlack()) writer.option("vary").color(colorVary);
		}
		
		if (!writer.isDefault(alpha, 1.0f) || alphaVary != 0) {
			writer.command("alpha").floats(alpha);
			if (alphaVary != 0) writer.option("vary").floats(alphaVary);
		}
		
		if (!writer.isDefault(size, 1.0f) || sizeVary != 0) {
			writer.command("size").floats(size);
			if (sizeVary != 0) writer.option("vary").floats(sizeVary);
		}
		
		if (!writer.isDefault(aspectRatio) || aspectRatioVary != 0) {
			writer.command("aspect").floats(aspectRatio);
			if (aspectRatioVary != 0) writer.option("vary").floats(aspectRatioVary);
		}
		
		if (!writer.isDefault(rotate, 0.0f) || rotationVary != 0.0f || rotationOffset != 0.0f)
		{
			writer.command("rotate").floats(rotate);
			if (rotationVary != 0) writer.option("vary").floats(rotationVary);
			if (rotationOffset != 0) writer.option("offset").floats(rotationOffset);
		}
		
		writeSource(writer);
		writeEmit(writer);
		writeForce(writer);
		writeWarp(writer);
		writeWiggles(writer);
		writeWalk(writer);
		if (velocityStretch != 0) writer.command("stretch").floats(velocityStretch);
		writeLife(writer);
		writeRate(writer);
		writeResource(writer);
		writeFrames(writer);
		if (alignMode != 0) writer.command("align").arguments(ENUM_ALIGNMENT.get(alignMode));
		if (flags.get(FLAGBIT_LOOP_BOX)) {
			if (!loopBoxColor.isEmpty()) writer.command("loopBoxColor").colors(loopBoxColor);
			if (!loopBoxAlpha.isEmpty()) writer.command("loopBoxAlpha").floats(loopBoxAlpha);
		}
		if (!surfaces.isEmpty()) {
			writer.blankLine();
			for (Surface s : surfaces) {
				writer.command("surface");
				s.toArgScript(writer);
			}
			writer.blankLine();
		}
		writeEmitMap(writer);
		if (!mapEmitColor.isDefault()) writer.command("mapEmitColor").arguments(mapEmitColor);
		writeCollideMap(writer);
		writeRepelMap(writer);
		writeAdvectMap(writer);
		writeForceMap(writer);
		if (physicsType != 0) writer.command("physics").arguments(ENUM_PHYSICS.get(physicsType));
		if (!pathPoints.isEmpty()) {
			writer.blankLine();
			for (ParticlePathPoint pathPoint : pathPoints) {
				pathPoint.toArgScript(writer);
			}
		}
		
//		if ((flags & ~FLAGMASK) != 0) {
//			writer.blankLine();
//			writer.command("flags").arguments(HashManager.get().hexToString(flags & ~FLAGMASK));
//		}
		
		writer.endBlock().commandEND();
	}
	
	private void writeSource(ArgScriptWriter writer) {
		
		if (emitVolumeBBMin[0] != 0 || emitVolumeBBMin[1] != 0 || emitVolumeBBMin[2] != 0 
				|| emitVolumeBBMax[0] != 0 || emitVolumeBBMax[1] != 0 || emitVolumeBBMax[2] != 0
				|| torusWidth != -1 || flags.get(FLAGBIT_SOURCE_SCALE_PARTICLES) || flags.get(FLAGBIT_SOURCE_RESET_INCOMING)) {
			
			float[] offset = new float[3];
			float min_x = (emitVolumeBBMin[0] - emitVolumeBBMax[0]) / 2;
			float min_y = (emitVolumeBBMin[1] - emitVolumeBBMax[1]) / 2;
			float min_z = (emitVolumeBBMin[2] - emitVolumeBBMax[2]) / 2;
			float max_x = -min_x;
			float max_y = -min_y;
			float max_z = -min_z;
			offset[0] = emitVolumeBBMin[0] - min_x;
			offset[1] = emitVolumeBBMin[1] - min_y;
			offset[2] = emitVolumeBBMin[2] - min_z;
			
			writer.command("source");
			
			if (flags.get(FLAGBIT_SOURCE_ROUND)) {
				if (max_x == max_y && max_y == max_z) writer.option("sphere").floats(max_x);
				else writer.option("ellipse").vector(max_x, max_y, max_z);
			}
			else {
				if (torusWidth != -1) {
					if (min_z != 0 || max_z != 0) writer.option("torus")
						.vector(max_x, max_y, max_z).floats(torusWidth);
					
					else writer.option("ring").floats(max_x, torusWidth);
				}
				else if (min_z != 0 || max_z != 0) {
					if (max_x == max_y && max_y == max_z) writer.option("cube").floats(max_x);
					else writer.option("box").vector(max_x, max_y, max_z);
				} else {
					if (max_x == max_y) writer.option("square").floats(max_x);
					else writer.option("quad").vector(max_x, max_y);
				}
			}
			
			if (offset[0] != 0 || offset[1] != 0 || offset[2] != 0) {
				writer.option("offset").vector(offset);
			}
			
			writer.flag("scaleParticles", flags.get(FLAGBIT_SOURCE_SCALE_PARTICLES));
			writer.flag("resetIncoming", flags.get(FLAGBIT_SOURCE_RESET_INCOMING));
		}
	}

	private void writeEmit(ArgScriptWriter writer) {
		boolean commandWritten = false;
		
		if (emitSpeed[0] != 0 || emitSpeed[1] != 0) {
			if (!commandWritten) writer.command("emit");
			commandWritten = true;
			
			writer.option("speed");
			float value = (emitSpeed[1] - emitSpeed[0]) / 2.0f;
			if (value == 0.0f) writer.floats(emitSpeed[0]);
			else writer.floats(emitSpeed[0] + value, value);
		}
		
		if (emitDirectionBBMin[0] != 0 || emitDirectionBBMin[1] != 0 || emitDirectionBBMin[2] != 1
				|| emitDirectionBBMax[0] != 0 || emitDirectionBBMax[1] != 0 || emitDirectionBBMax[2] != 1) {
			
			if (!commandWritten) writer.command("emit");
			commandWritten = true;
			
			float[] dir = new float[3];
			float[] vary = new float[3];
			vary[0] = (emitDirectionBBMax[0] - emitDirectionBBMin[0]) / 2.0f;
			vary[1] = (emitDirectionBBMax[1] - emitDirectionBBMin[1]) / 2.0f;
			vary[2] = (emitDirectionBBMax[2] - emitDirectionBBMin[2]) / 2.0f;
			dir[0] = emitDirectionBBMax[0] - vary[0];
			dir[1] = emitDirectionBBMax[1] - vary[1];
			dir[2] = emitDirectionBBMax[2] - vary[2];
			
			writer.option("dir").vector(dir);
			if (vary[0] != 0 || vary[1] != 0 || vary[2] != 0) writer.vector(vary);
		}
		
		if (flags.get(FLAGBIT_EMIT_BASE)) {
			if (!commandWritten) writer.command("emit");
			commandWritten = true;
			writer.option("base");
		}
		
		if (flags.get(FLAGBIT_EMIT_SCALE_EXISTING)) {
			if (!commandWritten) writer.command("emit");
			commandWritten = true;
			writer.option("scaleExisting");
		}
	}
	
	private void writeForce(ArgScriptWriter writer) {
		if (directionForcesSum[0] != 0 || directionForcesSum[1] != 0 || directionForcesSum[2] != 0 || windStrength != 0 || gravityStrength != 0
				|| drag != 0 || radialForce != 0 || !attractor.attractorStrength.isEmpty()) {
			
			writer.command("force");
			
			if (directionForcesSum[0] == 0 && directionForcesSum[1] == 0 && directionForcesSum[2] != 0) {
				writer.option("gravity").floats(-directionForcesSum[2]);
			}
			else if (directionForcesSum[0] != 0 || directionForcesSum[1] != 0 || directionForcesSum[2] != 0) {
				float[] vec = new float[] {directionForcesSum[0], directionForcesSum[1], directionForcesSum[2]};
				float length = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
				
				float eps = 0.0001f;
				if (length >= 1.0-eps && length <= 1.0+eps) {
					writer.option("wind").vector(vec);
				}
				else {
					vec[0] = vec[0] / length;
					vec[1] = vec[1] / length;
					vec[2] = vec[2] / length;
					writer.option("wind").vector(vec).floats(length);
				}
			}
			
			if (windStrength != 0) writer.option("worldWind").floats(windStrength);
			if (gravityStrength != 0) writer.option("worldGravity").floats(gravityStrength);
			
			if (drag != 0) writer.option("drag").floats(drag);
			
			if (radialForce != 0) {
				writer.option("bomb").floats(radialForce);
				if (radialForceLocation[0] != 0 || radialForceLocation[1] != 0 || radialForceLocation[2] != 0) {
					writer.vector(radialForceLocation);
				}
			}
			
			if (flags.get(FLAGBIT_ATTRACTOR)) {
				if (flags.get(FLAGBIT_NOT_PRESET_ATTRACTOR)) writer.option("attractor").vector(attractorOrigin);
				else writer.option("presetAttractor");
				
				writer.floats(attractor.range).floats(attractor.attractorStrength);
				if (attractor.killRange != 0) writer.option("killRange").floats(attractor.killRange);
			}
		}
	}
	
	private void writeWiggles(ArgScriptWriter writer) {
		if (!wiggles.isEmpty()) {
			writer.blankLine();
			for (ParticleWiggle w : wiggles) {
				writer.command("warp").option("wiggleDir").floats(w.timeRate).vector(w.wiggleDirection);
				if (w.rateDirection[0] != 0 || w.rateDirection[1] != 0 || w.rateDirection[2] != 0) {
					writer.vector(w.rateDirection);
				}
			}
			writer.blankLine();
		}
	}
	
	private void writeWarp(ArgScriptWriter writer) {
		boolean commandWritten = false;
		if (screwRate != 0) {
			if (!commandWritten) writer.command("warp");
			commandWritten = true;
			
			if (flags.get(FLAGBIT_WARP_SPIRAL)) writer.option("spiral");
			else writer.option("screw");
			writer.floats(screwRate);
		}
		if (screenBloomAlphaRate != 0 || screenBloomAlphaBase != 255) {
			if (!commandWritten) writer.command("warp");
			commandWritten = true;
			writer.option("bloomAlpha").floats(screenBloomAlphaBase / 255.0f, (screenBloomAlphaRate / 255.0f) / 0.0625f);
		}
		if (screenBloomScaleRate != 0 || screenBloomScaleBase != 255) {
			if (!commandWritten) writer.command("warp");
			commandWritten = true;
			writer.option("bloomSize").floats(screenBloomScaleBase / 255.0f, (screenBloomScaleRate / 255.0f) / 0.0625f);
		}
	}
	
	private void writeWalk(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_RANDOM_WALK)) {
			boolean isDirectedWalk = !randomWalk.turnOffsetCurve.isEmpty();
			if (isDirectedWalk) writer.command("directedWalk").floats(randomWalk.turnOffsetCurve);
			else writer.command("randomWalk");
			
			float vary = (randomWalk.time[1] - randomWalk.time[0]) / 2;
			float value = randomWalk.time[0] + vary;
			writer.option("delay").floats(value);
			if (vary != 0) writer.floats(vary);
			
			vary = (randomWalk.strength[1] - randomWalk.strength[0]) / 2;
			value = randomWalk.strength[0] + vary;
			writer.option("strength").floats(value);
			if (vary != 0) writer.floats(vary);
			
			if (randomWalk.turnRange != 0 || randomWalk.turnOffset != 0) {
				writer.option("turn").floats(randomWalk.turnRange);
				if (randomWalk.turnOffset != 0) writer.floats(randomWalk.turnOffset);
			}
			
			if (randomWalk.loopType == 1) writer.option("sustain");
			else if (randomWalk.loopType == 0) writer.option("loop");
			if (randomWalk.mix != 0) writer.option("mix").floats(randomWalk.mix);
			writer.flag("wait", flags.get(FLAGBIT_RANDOM_WALK_WAIT));
		}
	}
	
	private void writeLife(ArgScriptWriter writer) {
		float vary = (life[1] - life[0]) / 2.0f;
		float value = life[0] + vary;
		
		writer.command("life").floats(value);
		if (vary != 0) writer.floats(vary);
		
		if (prerollTime != 0.5f) {
			writer.option("preroll").floats(prerollTime);
		}
		
		writer.flag("propagateAlways", flags.get(FLAGBIT_PROPAGATE_ALWAYS));
		writer.flag("propagateIfKilled", flags.get(FLAGBIT_PROPAGATE_IF_KILLED));
	}
	
	private void writeRate(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_INJECT)) {
			writer.command("inject").floats(rate.get(0));
		} else if (flags.get(FLAGBIT_MAINTAIN)) {
			writer.command("maintain").floats(rate.get(0));
			if (emitDelay[0] != -1 || emitDelay[1] != -1) {
				writer.option("delay").floats(emitDelay[0]);
				if (emitDelay[1] != emitDelay[0]) writer.floats(emitDelay[1]);
			}
			if (flags.get(FLAGBIT_RATE_HOLD)) {
				writer.option("hold").floats(rateLoop);
				if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
			} 
			else if (flags.get(FLAGBIT_RATE_KILL)) {
				writer.option("kill").floats(rateLoop);
			}
			// This has no more options
			return;
		} else {
			writer.command("rate").floats(rate);
		}
		
		writer.flag("sizeScale", flags.get(FLAGBIT_RATE_SIZE_SCALE));
		writer.flag("areaScale", flags.get(FLAGBIT_RATE_AREA_SCALE));
		writer.flag("volumeScale", flags.get(FLAGBIT_RATE_VOLUME_SCALE));
		
		if (flags.get(FLAGBIT_RATE_SUSTAIN)) {
			writer.option("sustain").floats(rateLoop);
			if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
		}
		else if (flags.get(FLAGBIT_RATE_HOLD)) {
			writer.option("hold").floats(rateLoop);
			if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
		} 
		else if (flags.get(FLAGBIT_RATE_KILL)) {
			writer.option("kill").floats(rateLoop);
		}
		else if (rateLoop != 0.1f && rateCurveCycles == 1) {
			writer.option("single").floats(rateLoop);
		}
		else {
			writer.option("loop").floats(rateLoop);
			if (rateCurveCycles != 0) writer.ints(rateCurveCycles);
		}
		
		if (emitDelay[0] != -1.0f || emitDelay[1] != -1.0f) {
			writer.option("delay").floats(emitDelay[0]);
			if (emitDelay[1] != emitDelay[0]) {
				writer.floats(emitDelay[1]);
			}
		}
		if (emitRetrigger[0] != -1.0f || emitRetrigger[1] != -1.0f) {
			writer.option("trigger").floats(emitRetrigger[0]);
			if (emitRetrigger[1] != emitRetrigger[0]) {
				writer.floats(emitRetrigger[1]);
			}
		}
	}
	
	private void writeResource(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_MODEL)) {
			writer.command("model");
			if (!texture.resource.isDefault()) writer.arguments(texture.resource);
			
			if (!texture.resource2.isDefault()) writer.option("material").arguments(texture.resource2);
			
			texture.toArgScript(null, writer, false, false);
		}
		else {
			if (texture.drawMode == TextureSlot.DRAWMODE_NONE) {
				texture.toArgScript("material", writer);
			} else {
				texture.toArgScript("texture", writer);
				if (tileCount[0] != 1 || tileCount[1] != 1) {
					writer.option("tile").ints(tileCount);
				}
			}
		}
		
		if (overrideSet != 0) writer.option("overrideSet").ints(overrideSet);
		writer.flag("acceptComposite", flags.get(FLAGBIT_TEXTURE_ACCEPT_COMPOSITE));
	}
	
	private void writeFrames(ArgScriptWriter writer) {
		if (frameCount != 0) {
			writer.command("frames");
			writer.option("speed").floats(frameSpeed);
			writer.option("count").ints(frameCount);
			if (frameStart != 0) writer.option("start").ints(frameStart);
			if (frameRandom != 0) writer.option("random").ints(frameRandom);
		}
	}
	
	private void writeEmitMap(ArgScriptWriter writer) {
		if (!mapEmit.isDefault()) {
			writer.command("mapEmit").arguments(mapEmit);
			if (flags.get(FLAGBIT_MAP_EMIT_HEIGHT_RANGE)) {
				if (altitudeRange[0] != -10000.0f && altitudeRange[1] != 10000.0f) {
					writer.option("heightRange").floats(altitudeRange);
				}
				else if (altitudeRange[1] != 10000.0f) {
					writer.option("belowHeight").floats(altitudeRange[1]);
				}
				else writer.option("aboveHeight").floats(altitudeRange[0]);
			}
			writer.flag("pinToSurface", flags.get(FLAGBIT_MAP_EMIT_PIN_TO_SURFACE));
			writer.flag("density", flags.get(FLAGBIT_MAP_EMIT_DENSITY));
		}
	}
	
	private void writeCollideMap(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_MAP_COLLIDE)) {
			writer.command("mapCollide");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			if (flags.get(FLAGBIT_MAP_COLLIDE_PIN_TO_MAP)) writer.option("pinToMap");
			else if (mapBounce != 1.0f) writer.option("bounce").floats(mapBounce);
			
			writer.flag("killOutsideMap", flags.get(FLAGBIT_KILL_OUTSIDE_MAP));
			
			if (probabilityDeath != 0) writer.option("death").floats(probabilityDeath);
		}
	}
	
	private void writeRepelMap(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_MAP_REPEL)) {
			writer.command("mapRepel");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			writer.floats(mapRepulseHeight, mapRepulseStrength);
			
			if (mapRepulseScoutDistance != 0) writer.option("scout").floats(mapRepulseScoutDistance);
			if (mapRepulseVertical != 0) writer.option("vertical").floats(mapRepulseVertical);
			if (mapRepulseKillHeight != -1000000000.0f) writer.option("killHeight").floats(mapRepulseKillHeight);
			
			writer.flag("killOutsideMap", flags.get(FLAGBIT_KILL_OUTSIDE_MAP));
		}
	}
	
	private void writeAdvectMap(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_MAP_ADVECT)) {
			writer.command("mapAdvect");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			if (mapRepulseStrength != 0) writer.option("strength").floats(mapRepulseStrength);
			writer.flag("killOutsideMap", flags.get(FLAGBIT_KILL_OUTSIDE_MAP));
		}
	}
	
	private void writeForceMap(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_MAP_FORCE)) {
			writer.command("mapForce");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			if (mapRepulseStrength != 1.0f) writer.option("strength").floats(mapRepulseStrength);
			writer.flag("killOutsideMap", flags.get(FLAGBIT_KILL_OUTSIDE_MAP));
		}
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		
		for (Surface surface : surfaces) {
			list.add(surface.collideEffect);
			list.add(surface.deathEffect);
			list.add(effectDirectory.getResource(MapResource.TYPE_CODE, surface.surfaceMapID));
		}
		
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapEmitColor));
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapForce));
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapEmit));
		
		if (texture.drawMode == TextureSlot.DRAWMODE_NONE) {
			if (flags.get(FLAGBIT_MODEL)) {
				list.add(effectDirectory.getResource(MaterialResource.TYPE_CODE, texture.resource2));
			} else {
				list.add(effectDirectory.getResource(MaterialResource.TYPE_CODE, texture.resource));
			}
		}
		
		return list;
	}
}
