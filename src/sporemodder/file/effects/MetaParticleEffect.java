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
import sporemodder.file.filestructures.Stream;
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

@Structure(StructureEndian.BIG_ENDIAN)
public class MetaParticleEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<MetaParticleEffect> STRUCTURE_METADATA = StructureMetadata.generate(MetaParticleEffect.class);
	
	public static final String KEYWORD = "metaParticles";
	public static final int TYPE_CODE = 0x0002;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	
	public static final ArgScriptEnum ENUM_ALIGNMENT = new ArgScriptEnum();
	static {
		ENUM_ALIGNMENT.add(0, "default");
		ENUM_ALIGNMENT.add(1, "ground");
		ENUM_ALIGNMENT.add(2, "dirX");
		ENUM_ALIGNMENT.add(3, "dirY");
		ENUM_ALIGNMENT.add(4, "dirZ");
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
	public static final int FLAGBIT_SURFACE = 0xc;
	public static final int FLAGBIT_MAP_COLLIDE = 0xd;
	public static final int FLAGBIT_MAP_REPEL = 0xe;
	public static final int FLAGBIT_MAP_ADVECT = 0xf;
	public static final int FLAGBIT_MAP_FORCE = 0x10;
	public static final int FLAGBIT_KILL_OUTSIDE_MAP = 0x11;
	public static final int FLAGBIT_MAP_COLLIDE_PIN_TO_MAP = 0x12;
	public static final int FLAGBIT_RANDOM_WALK_SYNC = 0x13;
	public static final int FLAGBIT_RANDOM_WALK_NO_SYNC = 0x14;
	public static final int FLAGBIT_RANDOM_WALK_WAIT = 0x14;
	public static final int FLAGBIT_ATTRACTOR_AFFECTS_ALPHA = 0x16;
	public static final int FLAGBIT_ATTRACTOR = 0x17;
	public static final int FLAGBIT_NOT_PRESET_ATTRACTOR = 0x18;
	public static final int FLAGBIT_TRACTOR = 0x19;
	public static final int FLAGBIT_PATH = 0x1a;
	public static final int FLAGBIT_ALIGN_WIND_BANK = 0x1b;
	
	public static final int FLAGBIT_SOURCE_RESET_INCOMING = 0x1d;
	public static final int FLAGBIT_RATE_KILL = 0x1e;
	public static final int FLAGBIT_RATE_HOLD = 0x1f;
	public static final int FLAGBIT_WARP_SPIRAL = 0x20;
	public static final int FLAGBIT_LOOPBOX = 0x21;
	public static final int FLAGBIT_ALIGN_SURFACE = 0x22;
	public static final int FLAGBIT_ALIGN_SURFACE_PARENT = 0x23;
	public static final int FLAGBIT_TICK = 0x24;
	public static final int FLAGBIT_DEATH_INHERIT = 0x25;
	public static final int FLAGBIT_PROPAGATE_ALWAYS = 0x26;
	public static final int FLAGBIT_PROPAGATE_IF_KILLED = 0x27;
	
	@StructureFieldMethod(read="readFlags", write="writeFlags")
	public final BitSet flags = new BitSet(64);  // actually only 41, but leave extra space for potential unused flags
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
	public int rateCurveCycles;
	
	@StructureLength.Value(32) public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	public float sizeVary;
	
	@StructureLength.Value(32) public final List<Float> pitch = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> roll = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> yaw = new ArrayList<Float>();
	public float pitchVary;
	public float rollVary;
	public float yawVary;
	public float pitchOffset;
	public float rollOffset;
	public float yawOffset;
	
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	public ColorRGB colorVary = ColorRGB.black();
	
	@StructureLength.Value(32) public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	public float alphaVary;
	
	@StructureFieldMethod(read="readEffect", write="writeEffect")
	public EffectComponent component;
	@StructureFieldMethod(read="readDeathEffect", write="writeDeathEffect")
	public EffectComponent deathEffect;
	@StructureUnsigned(8) public int alignMode;
	
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] directionForcesSum = new float[3];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] globalForcesSum = new float[3];
	public float windStrength;
	public float gravityStrength;
	public float radialForce;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] radialForceLocation = new float[3];
	public float drag;
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
	public final ParticleRandomWalk randomWalk2 = new ParticleRandomWalk();
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] randomWalkPreferredDir = new float[3];
	public float alignDamping;
	public float bankAmount;
	public float bankDamping;
	
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] attractorOrigin = new float[3];
	public final ParticleAttractor attractor = new ParticleAttractor();
	
	@StructureLength.Value(32) public final List<ParticlePathPoint> pathPoints = new ArrayList<ParticlePathPoint>();
	public float tractorResetSpeed;
	
	public MetaParticleEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		MetaParticleEffect effect = (MetaParticleEffect) _effect;
		
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
		
		size.addAll(effect.size);
		sizeVary = effect.sizeVary;
		pitch.addAll(effect.pitch);
		yaw.addAll(effect.yaw);
		roll.addAll(effect.roll);
		pitchVary = effect.pitchVary;
		yawVary = effect.yawVary;
		rollVary = effect.rollVary;
		pitchOffset = effect.pitchOffset;
		yawOffset = effect.yawOffset;
		rollOffset = effect.rollOffset;
		alpha.addAll(effect.alpha);
		alphaVary = effect.alphaVary;
		color.addAll(effect.color);
		colorVary.copy(effect.colorVary);
		
		component = effect.component;
		deathEffect = effect.deathEffect;
		alignMode = effect.alignMode;
		
		EffectDirectory.copyArray(directionForcesSum, effect.directionForcesSum);
		EffectDirectory.copyArray(globalForcesSum, effect.globalForcesSum);
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		radialForce = effect.radialForce;
		EffectDirectory.copyArray(radialForceLocation, effect.radialForceLocation);
		drag = effect.drag;
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
		randomWalk2.copy(effect.randomWalk2);
		EffectDirectory.copyArray(randomWalkPreferredDir, effect.randomWalkPreferredDir);
		alignDamping = effect.alignDamping;
		bankAmount = effect.bankAmount;
		bankDamping = effect.bankDamping;
		
		EffectDirectory.copyArray(attractorOrigin, effect.attractorOrigin);
		attractor.copy(effect.attractor);
		
		for (int i = 0; i < effect.pathPoints.size(); i++) pathPoints.add(new ParticlePathPoint(effect.pathPoints.get(i)));
		tractorResetSpeed = effect.tractorResetSpeed;
	}
	
	boolean isVersion2(String fieldName, Stream stream) {
		return version >= 2;
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
	
	void readEffect(String fieldName, StreamReader in) throws IOException {
		int index = in.readInt();
		int typeCode = in.readInt();
		
		if (index == -1) {
			component = null;
		} else {
			component = effectDirectory.getEffect(typeCode, index);
		}
	}
	
	void writeEffect(String fieldName, StreamWriter out, Object value) throws IOException {
		if (component == null) {
			out.writeInt(-1);
			out.writeInt(0);
		} else {
			out.writeInt(effectDirectory.getIndex(component.getFactory().getTypeCode(), component));
			out.writeInt(component.getFactory().getTypeCode());
		}
	}
	
	void readDeathEffect(String fieldName, StreamReader in) throws IOException {
		int index = in.readInt();
		
		if (index == -1) {
			deathEffect = null;
		} else {
			deathEffect = effectDirectory.getEffect(VisualEffect.TYPE_CODE, index);
		}
	}
	
	void writeDeathEffect(String fieldName, StreamWriter out, Object value) throws IOException {
		if (deathEffect == null) {
			out.writeInt(-1);
		} else {
			out.writeInt(effectDirectory.getIndex(VisualEffect.TYPE_CODE, deathEffect));
		}
	}
	
	protected static class Parser extends EffectBlockParser<MetaParticleEffect> {
		@Override
		protected MetaParticleEffect createEffect(EffectDirectory effectDirectory) {
			return new MetaParticleEffect(effectDirectory, FACTORY.getMaxVersion());
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
			}), "color", "colour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGB255s(args, effect.color);
				}
				if (line.getOptionArguments(args, "vary", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
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
			
			this.addParser("pitch", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.pitch.clear();
					stream.parseFloats(args, effect.pitch);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.pitchVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.pitchOffset = value.floatValue();
				}
			}));
			
			this.addParser("roll", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.roll.clear();
					stream.parseFloats(args, effect.roll);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rollVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rollOffset = value.floatValue();
				}
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.yaw.clear();
					stream.parseFloats(args, effect.yaw);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.yawVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.yawOffset = value.floatValue();
				}
			}), "yaw", "heading", "rotate");
			
			parseSource();
			parseEmit();
			parseForce();
			parseWarp();
			parseWalk();
			parseLife();
			parseRate();
			parseInject();
			parseMaintain();
			
			this.addParser("effect", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.component = parser.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
					if (effect.component != null) line.addHyperlinkForArgument(EffectDirectory.getHyperlinkType(effect.component), effect.component, 0);
					
					if (line.hasFlag("tick")) {
						effect.flags.set(FLAGBIT_TICK);
					}
					
					if (line.getOptionArguments(args, "death", 1)) {
						effect.deathEffect = parser.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
						if (effect.deathEffect != null) line.addHyperlinkForOptionArgument(EffectDirectory.getHyperlinkType(effect.deathEffect), effect.deathEffect, "death", 0);
						
						if (line.hasFlag("inherit")) {
							effect.flags.set(FLAGBIT_DEATH_INHERIT);
						}
					}
				}
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, 2)) {
					if (args.size() > 1) {
						boolean foundFactory = false;
						for (EffectComponentFactory factory : EffectDirectory.getFactories()) {
							if (factory.getKeyword().equals(args.get(0))) {
								effect.component = parser.getData().getComponent(args, 1, factory.getTypeCode());
								foundFactory = true;
								break;
							}
						}
						if (!foundFactory) {
							stream.addError(line.createErrorForArgument("First argument must be component type, such as 'particle', 'sound', etc", 0));
							return;
						}
					}
					else {
						effect.component = parser.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
					}
					if (effect.component != null) line.addHyperlinkForArgument(EffectDirectory.getHyperlinkType(effect.component), effect.component, 0);
					
					if (line.getOptionArguments(args, "death", 1)) {
						effect.deathEffect = parser.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
						if (effect.deathEffect != null) line.addHyperlinkForOptionArgument(EffectDirectory.getHyperlinkType(effect.deathEffect), effect.deathEffect, "death", 0);
						
						if (line.hasFlag("inherit")) {
							effect.flags.set(FLAGBIT_DEATH_INHERIT);
						}
					}
				}
			}), "attach", "component");
			
			this.addParser("align", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.alignMode = (byte) ENUM_ALIGNMENT.get(args, 0);
				}
				
				if (line.getOptionArguments(args, "damp", 1)) {
					effect.alignDamping = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
				}
				
				if (line.getOptionArguments(args, "bank", 2)) {
					effect.bankAmount = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f) * 100.0f;
					effect.bankDamping = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0f);
				}
				else if (line.getOptionArguments(args, "windBank", 2)) {
					effect.bankAmount = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					effect.bankDamping = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0f);
					effect.flags.set(FLAGBIT_ALIGN_WIND_BANK);
				}
				
				if (line.hasFlag("surface")) {
					effect.flags.set(FLAGBIT_ALIGN_SURFACE);
					if (line.hasFlag("parent")) {
						effect.flags.set(FLAGBIT_ALIGN_SURFACE_PARENT);
					}
				}
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOPBOX);
					effect.loopBoxColor.clear();
					stream.parseColorRGBs(args, effect.loopBoxColor);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}), "loopBoxColor", "loopBoxColour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOPBOX);
					effect.loopBoxColor.clear();
					stream.parseColorRGB255s(args, effect.loopBoxColor);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}), "loopBoxColor255", "loopBoxColour255");
			
			this.addParser("loopBoxAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOPBOX);
					effect.loopBoxAlpha.clear();
					stream.parseFloats(args, effect.loopBoxAlpha);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}));
			
			this.addParser("loopBoxAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags.set(FLAGBIT_LOOPBOX);
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
				
				effect.flags.set(FLAGBIT_SURFACE);
			}));
			
			parseEmitMap();
			
			this.addParser("mapEmitColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapEmitColor.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
				}
			}));
			
			parseCollideMap();
			parseRepelMap();
			parseAdvectMap();
			parseForceMap();
			
			this.addParser("path", ArgScriptParser.create((parser, line) -> {
				ParticlePathPoint point = new ParticlePathPoint();
				point.parse(stream, line, effect.pathPoints, effect.pathPoints.size());
				effect.pathPoints.add(point);
			}));
			
			//TODO
//			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
//				Number value = null;
//				if (line.getArguments(args, 2) && (value = stream.parseUInt(args, 0)) != null) {
//					effect.flags |= value.intValue() & ~FLAG_MASK;
//					// effect.flags = value.intValue();
//				}
//			}));
			
			this.addParser("globalForcesSum", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					stream.parseVector3(args, 0, effect.globalForcesSum);
				}
			}));
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
						stream.parseVector3(args, 0, effect.radialForceLocation);
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
					
					if (line.hasFlag("affectsAlpha")) {
						effect.flags.set(FLAGBIT_ATTRACTOR_AFFECTS_ALPHA);
					}
				}
				
				// Tractor
				boolean hasTractor = false;
				boolean tractorIsRelative = false;
				if (line.getOptionArguments(args, "tractorRel", 2, 3)) {
					hasTractor = true;
					tractorIsRelative = true;
				}
				else {
					hasTractor = line.getOptionArguments(args, "tractor", 2, 3);
				}
				if (hasTractor) {
					
					//TODO
					
					effect.flags.set(FLAGBIT_TRACTOR);
					
					if (line.getOptionArguments(args, "tractorResetSpeed", 1)) {
						effect.tractorResetSpeed = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}
				
				if (line.hasFlag("path")) {
					effect.flags.set(FLAGBIT_PATH);
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
			private boolean isSecondaryWalk;
			
			private RandomWalkParser(boolean isDirectedWalk, boolean isSecondaryWalk) {
				this.isDirectedWalk = isDirectedWalk;
				this.isSecondaryWalk = isSecondaryWalk;
			}
			
			@Override
			public void parse(ArgScriptLine line) {
				ParticleRandomWalk randomWalk = isSecondaryWalk ? effect.randomWalk2 : effect.randomWalk;
				
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				
				if (line.hasFlag("sync")) {
					effect.flags.set(FLAGBIT_RANDOM_WALK_SYNC);
				} else {
					effect.flags.set(FLAGBIT_RANDOM_WALK_NO_SYNC);
				}
				
				if (isDirectedWalk) {
					if (line.getArguments(args, 0, Integer.MAX_VALUE) && args.size() != 0) {
						for (int i = 0; i < args.size(); i++) {
							if ((value = stream.parseFloat(args, i, -1.0f, 1.0f)) != null) {
								randomWalk.turnOffsetCurve.add(value.floatValue());
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
				
				if (line.getOptionArguments(args, "delay", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					float delay = value.floatValue();
					float vary = 0;
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						vary = value.floatValue();
					}
					
					randomWalk.time[0] = delay - vary;
					randomWalk.time[1] = delay + vary;
				}
				
				if (line.getOptionArguments(args, "strength", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					float strength = value.floatValue();
					float vary = 0;
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						vary = value.floatValue();
					}
					
					randomWalk.strength[0] = strength - vary;
					randomWalk.strength[1] = strength + vary;
				}
				
				if (line.getOptionArguments(args, isDirectedWalk ? "randomTurn" : "turn", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					randomWalk.turnRange = value.floatValue();
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						randomWalk.turnOffset = value.floatValue();
					}
				}
				
				if (line.getOptionArguments(args, "mix", 1) && (value = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null) {
					randomWalk.mix = value.floatValue();
				}
				
				randomWalk.loopType = 2;
				
				if (isDirectedWalk) {
					if (line.hasFlag("sustain")) randomWalk.loopType = 1;
					else if (line.hasFlag("loop")) randomWalk.loopType = 0;
				}
				
				if (line.hasFlag("wait")) effect.flags.set(FLAGBIT_RANDOM_WALK_WAIT);
				
				if (line.getOptionArguments(args, "preferDir", 1)) {
					stream.parseVector3(args, 0, effect.randomWalkPreferredDir);
				}
			}
		}
		
		private void parseWalk() {
			this.addParser("directedWalk", new RandomWalkParser(true, false));
			this.addParser("randomWalk", new RandomWalkParser(false, false));
			
			this.addParser("directedWalk2", new RandomWalkParser(true, true));
			this.addParser("randomWalk2", new RandomWalkParser(false, true));
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
				}
				
				if (line.getOptionArguments(args, "preroll", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.prerollTime = value.floatValue();
				}
				else {
					effect.prerollTime = 0.5f;
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
		
		private void parseEmitMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapEmit", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapEmit.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
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
						line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
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
						line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
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
					line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
				}
				
				effect.flags.set(FLAGBIT_MAP_COLLIDE);
				effect.flags.set(FLAGBIT_MAP_ADVECT);

				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				}
				else {
					effect.mapRepulseStrength = 1.0f;
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags.set(FLAGBIT_KILL_OUTSIDE_MAP);

			}));
		}
		
		private void parseForceMap() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("mapForce", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					effect.mapForce.parseSpecial(args, 0);
				}
				
				effect.flags.set(FLAGBIT_MAP_COLLIDE);
				effect.flags.set(FLAGBIT_MAP_FORCE);

				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				}
				else {
					effect.mapRepulseStrength = 1.0f;
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags.set(FLAGBIT_KILL_OUTSIDE_MAP);

			}));
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return MetaParticleEffect.class;
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
			return 2;
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
			return new MetaParticleEffect(effectDirectory, version);
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
		
		if (!writer.isDefaultColor(color) || !colorVary.isBlack()) {
			writer.command("color").colors(color);
			if (!colorVary.isBlack()) writer.option("vary").color(colorVary);
		}
		
		if (!writer.isDefault(alpha, 1.0f) || alphaVary != 0) {
			writer.command("alpha").floats(alpha);
			if (alphaVary != 0) writer.option("vary").floats(alphaVary);
		}
		
		if (!writer.isDefault(size, 1.0f) || sizeVary != 0) {
			writer.command("size").floats(size);
			if (sizeVary != 0) writer.option("vary").floats(sizeVary);
		}
		
		if (!writer.isDefault(pitch, 0.0f) || pitchVary != 0 || pitchOffset != 0) {
			writer.command("pitch").floats(pitch);
			if (pitchVary != 0) writer.option("vary").floats(pitchVary);
			if (pitchOffset != 0) writer.option("offset").floats(pitchOffset);
		}
		if (!writer.isDefault(roll, 0.0f) || rollVary != 0 || rollOffset != 0) {
			writer.command("roll").floats(roll);
			if (rollVary != 0) writer.option("vary").floats(rollVary);
			if (rollOffset != 0) writer.option("offset").floats(rollOffset);
		}
		if (!writer.isDefault(yaw, 0.0f) || yawVary != 0 || yawOffset != 0) {
			writer.command("heading").floats(yaw);
			if (yawVary != 0) writer.option("vary").floats(yawVary);
			if (yawOffset != 0) writer.option("offset").floats(yawOffset);
		}
		
		writeSource(writer);
		writeEmit(writer);
		writeForce(writer);
		writeWarp(writer);
		writeWalk(writer);
		writeLife(writer);
		writeRate(writer);
		
		if (component != null) {
			if (component.getFactory().getTypeCode() == VisualEffect.TYPE_CODE) {
				writer.command("effect").arguments(component.getName());
			}
			else {
				writer.command("component").arguments(component.getFactory().getKeyword(), component.getName());
			}
			if (deathEffect != null) writer.option("death").arguments(deathEffect.getName());
			
			writer.flag("inherit", flags.get(FLAGBIT_DEATH_INHERIT));
		}
		
		if (alignMode != 0 || alignDamping != 0.0f || bankAmount != 0.0f || bankDamping != 0.0f
				|| flags.get(FLAGBIT_ALIGN_SURFACE))
		{
			writer.command("align").arguments(ENUM_ALIGNMENT.get(alignMode));
			
			if (alignDamping != 0.0f) {
				writer.option("damp").floats(alignDamping);
			}
			if (flags.get(FLAGBIT_ALIGN_WIND_BANK)) {
				writer.option("windBank").floats(bankAmount, bankDamping);
			}
			else if (bankAmount != 0.0f || bankDamping != 0.0f) {
				writer.option("bank").floats(bankAmount / 100.0f, bankDamping);
			}
			if (flags.get(FLAGBIT_ALIGN_SURFACE)) {
				writer.option("surface");
				if (flags.get(FLAGBIT_ALIGN_SURFACE_PARENT)) {
					writer.option("parent");
				}
			}
		}
		
		if (flags.get(FLAGBIT_LOOPBOX)) {
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
		
		if (!pathPoints.isEmpty()) {
			writer.blankLine();
			for (ParticlePathPoint pathPoint : pathPoints) {
				pathPoint.toArgScript(writer);
			}
		}
		
		if (globalForcesSum[0] != 0 || globalForcesSum[1] != 0 || globalForcesSum[2] != 0) {
			writer.command("globalForcesSum").vector(globalForcesSum);
		}
		
		//TODO finish this
		
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
				
				writer.flag("affectsAlpha", flags.get(FLAGBIT_ATTRACTOR_AFFECTS_ALPHA));
			}
			
			if (flags.get(FLAGBIT_TRACTOR)) {
				writer.option("tractor").floats(0f, 0f);
				
				if (tractorResetSpeed != 0.0f) {
					writer.option("tractorResetSpeed").floats(tractorResetSpeed);
				}
			}
			
			writer.flag("path", flags.get(FLAGBIT_PATH));
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
		if (!wiggles.isEmpty()) {
			if (!commandWritten) writer.command("warp");
			commandWritten = true;
			for (ParticleWiggle w : wiggles) {
				writer.option("wiggleDir").floats(w.timeRate).vector(w.wiggleDirection);
				if (w.rateDirection[0] != 0 || w.rateDirection[1] != 0 || w.rateDirection[2] != 0) {
					writer.vector(w.rateDirection);
				}
			}
		}
	}
	
	private void writeWalk(ArgScriptWriter writer, ParticleRandomWalk randomWalk, String directedKeyword, String keyword, boolean isSecond) {
		boolean isDirectedWalk = !randomWalk.turnOffsetCurve.isEmpty();
		
		writer.command(isDirectedWalk ? directedKeyword : keyword).floats(randomWalk.turnOffsetCurve);
		
		writer.flag("sync", flags.get(FLAGBIT_RANDOM_WALK_SYNC));
		
		float vary = (randomWalk.time[1] - randomWalk.time[0]) / 2;
		float value = randomWalk.time[0] + vary;
		writer.option("delay").floats(value);
		if (vary != 0) writer.floats(vary);
		
		vary = (randomWalk.strength[1] - randomWalk.strength[0]) / 2;
		value = randomWalk.strength[0] + vary;
		writer.option("strength").floats(value);
		if (vary != 0) writer.floats(vary);
		
		if (randomWalk.turnRange != 0.25f || randomWalk.turnOffset != 0) {
			writer.option(isDirectedWalk ? "randomTurn" : "turn").floats(randomWalk.turnRange);
			if (randomWalk.turnOffset != 0) writer.floats(randomWalk.turnOffset);
		}
		
		if (randomWalk.loopType == 1) writer.option("sustain");
		else if (randomWalk.loopType == 0) writer.option("loop");
		if (randomWalk.mix != 0) writer.option("mix").floats(randomWalk.mix);
		
		writer.flag("wait", flags.get(FLAGBIT_RANDOM_WALK_WAIT));
		
		if (!isSecond && (randomWalkPreferredDir[0] != 0.0f || 
				randomWalkPreferredDir[1] != 0.0f ||
				randomWalkPreferredDir[2] != 0.0f)) {
			writer.option("preferDir").vector(randomWalkPreferredDir);
		}
	}
	
	private void writeWalk(ArgScriptWriter writer) {
		if (flags.get(FLAGBIT_RANDOM_WALK_SYNC) || flags.get(FLAGBIT_RANDOM_WALK_NO_SYNC)) {
			writeWalk(writer, randomWalk, "directedWalk", "randomWalk", false);
			writeWalk(writer, randomWalk2, "directedWalk2", "randomWalk2", true);
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
		if (flags.get(FLAGBIT_MAP_COLLIDE) &&
				!flags.get(FLAGBIT_MAP_REPEL) &&
				!flags.get(FLAGBIT_MAP_ADVECT) &&
				!flags.get(FLAGBIT_MAP_FORCE)) {
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
		
		list.add(component);
		list.add(deathEffect);
		
		return list;
	}
}
