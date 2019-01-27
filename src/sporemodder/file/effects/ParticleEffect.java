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
import java.util.Optional;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureFieldEndian;
import emord.filestructures.StructureFieldMethod;
import emord.filestructures.StructureLength;
import emord.filestructures.StructureUnsigned;
import emord.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
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
	}

	public static final int SOURCE_ROUND = 0x10;
	public static final int SOURCE_SCALEPARTICLES = 0x800;
	public static final int SOURCE_RESETINCOMING = 0x4000000;
	
	public static final int EMIT_SCALEEXISTING = 0x2000000;
	public static final int EMIT_BASE = 8;
	
	public static final int WARP_SPIRAL = 0x20000000;
	
	public static final int ATTRACTOR_LOCATION = 0x1000000;
	
	public static final int FLAG_RANDOMWALK = 0x80000;
	public static final int RANDOMWALK_WAIT = 0x100000;
	
	public static final int LIFE_PROPAGATEALWAYS = 1;
	public static final int LIFE_PROPAGATEIFKILLED = 2;
	
	public static final int FLAG_SUSTAIN = 4;
	public static final int FLAG_HOLD = 0x10000000;
	public static final int FLAG_KILL = 0x8000000;
	public static final int FLAG_INJECT = 1;
	public static final int FLAG_MAINTAIN = 2;
	public static final int RATE_SIZESCALE = 0x100;
	public static final int RATE_AREASCALE = 0x200;
	public static final int RATE_VOLUMESCALE = 0x400;
	
	public static final int FLAG_MODEL = 0x200000;
	public static final int FLAG_ACCEPTCOMPOSITE = 0x400000;
	
	public static final int FLAG_LOOPBOX = 0x40000000;
	
	public static final int EMITMAP_PINTOSURFACE = 0x20;
	public static final int EMITMAP_HEIGHT = 0x40;
	public static final int EMITMAP_DENSITY = 0x80;
	
	public static final int FLAG_SURFACES = 0x1000;
	public static final int FLAG_COLLIDEMAP = 0x2000;
	public static final int COLLIDE_PINTOMAP = 0x40000;
	public static final int FLAG_KILLOUTSIDEMAP = 0x20000;
	public static final int FLAG_REPULSEMAP = 0x4000;
	public static final int FLAG_ADVECTMAP = 0x8000;
	public static final int FLAG_FORCEMAP = 0x10000;
	
	
	public int unkFlags;
	public int flags;
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
		unkFlags = effect.unkFlags;
		flags = effect.flags;
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
					effect.flags |= FLAG_LOOPBOX;
					effect.loopBoxColor.clear();
					stream.parseColorRGBs(args, effect.loopBoxColor);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}), "loopBoxColor", "loopBoxColour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags |= FLAG_LOOPBOX;
					effect.loopBoxColor.clear();
					stream.parseColorRGB255s(args, effect.loopBoxColor);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}), "loopBoxColor255", "loopBoxColour255");
			
			this.addParser("loopBoxAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags |= FLAG_LOOPBOX;
					effect.loopBoxAlpha.clear();
					stream.parseFloats(args, effect.loopBoxAlpha);
					// Spore parses the flag "orient", but it doesn't use it
				}
			}));
			
			this.addParser("loopBoxAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.flags |= FLAG_LOOPBOX;
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
				
				effect.flags |= FLAG_SURFACES;
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
				ParticlePathPoint point = new ParticlePathPoint();
				point.parse(stream, line, effect.pathPoints, effect.pathPoints.size());
				effect.pathPoints.add(point);
			}));
		}
		
		private void parseSource() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("source", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				Number value2 = null;
				
				// disable round source
				effect.flags &= ~SOURCE_ROUND;
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
						effect.flags |= SOURCE_ROUND;
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						max_x += size;
						max_y += size;
					}
					else if (line.getOptionArguments(args, "sphere", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.flags |= SOURCE_ROUND;
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						min_z -= size;
						max_x += size;
						max_y += size;
						max_z += size;
					}
					else if (line.getOptionArguments(args, "ellipse", 1) || line.getOptionArguments(args, "ellipsoid", 1)) {
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
						effect.flags |= SOURCE_ROUND;
						effect.torusWidth = value2.floatValue();
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						max_x += size;
						max_y += size;
					}
					else if (line.getOptionArguments(args, "torus", 2) && (value = stream.parseFloat(args, 1, 0.0f, 1.0f)) != null) {
						effect.flags |= SOURCE_ROUND;
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
					effect.flags |= SOURCE_SCALEPARTICLES;
				}
				if (line.hasFlag("resetIncoming")) {
					effect.flags |= SOURCE_RESETINCOMING;
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
				
				if (line.hasFlag("base")) effect.flags |= EMIT_BASE;
				if (line.hasFlag("scaleExisting")) effect.flags |= EMIT_SCALEEXISTING;
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
					effect.flags |= ATTRACTOR_LOCATION;
					validArguments = true;
				}
				else if (line.getOptionArguments(args, "presetAttractor", 2, Integer.MAX_VALUE)) {
					effect.flags &= ~ATTRACTOR_LOCATION;
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
					effect.flags |= WARP_SPIRAL;
				}
				
				// Spore does something really strange here, so we just disable (wiggle can be added manually with 'wiggleDir')
//				if (line.getOptionArguments(args, "wiggle", 4) && (value = stream.parseFloat(args, 0)) != null) {
//					ParticleWiggle item = new ParticleWiggle();
//					item.rateDirection[0] = 0.0f;
//					item.rateDirection[1] = 0.0f;
//					item.rateDirection[2] = value.floatValue();
//					
//					if ((value = stream.parseFloat(args, 1)) != null) {
//						item.timeRate = value.floatValue();
//					}
//					
//					Number value2 = null;
//					if ((value = stream.parseFloat(args, 2)) != null && (value2 = stream.parseFloat(args, 3, 0.0f, 1.0f)) != null) {
//						float val = value.floatValue();
//						float vary = value2.floatValue();
//						
//						val1 = vary * 16;  // vary * 2PI * (16 / 2PI)
//					}
//				}
				
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
				
				effect.flags |= FLAG_RANDOMWALK;
				
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
				
				if (line.hasFlag("wait")) effect.flags |= RANDOMWALK_WAIT;
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
				effect.flags |= FLAG_SUSTAIN;
				effect.rateLoop = value.floatValue();
				effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(1).shortValue();
			}
			else if (line.getOptionArguments(args, "hold", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_HOLD;
				effect.rateLoop = value.floatValue();
				effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(1).shortValue();
			}
			else if (line.getOptionArguments(args, "kill", 0, 1)) {
				effect.flags |= FLAG_KILL;
				effect.unkFlags |= LIFE_PROPAGATEALWAYS;
				effect.rateLoop = Optional.ofNullable(args.size() == 1 ? stream.parseFloat(args, 0) : null).orElse(0.1f);
				effect.rateCurveCycles = 1;
			}
			
			if (line.hasFlag("sizeScale")) effect.flags |= RATE_SIZESCALE;
			if (line.hasFlag("areaScale")) effect.flags |= RATE_AREASCALE;
			if (line.hasFlag("volumeScale")) effect.flags |= RATE_VOLUMESCALE;
			
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
				
				effect.flags |= FLAG_INJECT;
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
				
				effect.flags |= FLAG_MAINTAIN;

				if (line.getOptionArguments(args, "delay", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					effect.emitDelay[0] = effect.emitDelay[1] = value.floatValue();
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						effect.emitDelay[1] = value.floatValue();
					}
				}
				
				if (line.getOptionArguments(args, "hold", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					effect.flags |= FLAG_HOLD;
					effect.rateLoop = value.floatValue();
					effect.rateCurveCycles = Optional.ofNullable(args.size() == 2 ? stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE) : null).orElse(1).shortValue();
				}
				else if (line.getOptionArguments(args, "kill", 0, 1)) {
					effect.flags |= FLAG_KILL;
					effect.unkFlags |= LIFE_PROPAGATEALWAYS;
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
				
				effect.flags &= ~FLAG_MODEL;
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
			}));
		}
		
		private void parseTexture() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.texture.resource.parse(args, 0);
				}
				
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_TEXTURE);
				
				effect.flags &= ~FLAG_MODEL;
				
				Number value = null;
				
				if (line.getOptionArguments(args, "tile", 1, 2) && (value = stream.parseByte(args, 0)) != null) {
					effect.tileCount[0] = value.byteValue();
					effect.tileCount[1] = Optional.ofNullable(args.size() == 2 ? stream.parseByte(args, 0) : null).orElse((byte) 1);
				}
				
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				if (line.hasFlag("acceptComposite")) effect.flags |= FLAG_ACCEPTCOMPOSITE;
			}));
		}
		
		private void parseModel() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("model", ArgScriptParser.create((parser, line) -> {
				effect.texture.drawMode = 0;
				effect.flags |= FLAG_MODEL;
				
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
				
				effect.texture.drawMode = (byte) (effect.texture.drawMode | 8);
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_FILE);
			}));
		}
		
		private void parseFrames() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("frames", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getOptionArguments(args, "speed", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.frameSpeed = value.floatValue();
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
				}
				if (line.getOptionArguments(args, "aboveHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[0] = value.floatValue();
				}
				if (line.getOptionArguments(args, "heightRange", 2)) {
					if ((value = stream.parseFloat(args, 0)) != null) {
						effect.altitudeRange[0] = value.floatValue();
					}
					if ((value = stream.parseFloat(args, 1)) != null) {
						effect.altitudeRange[1] = value.floatValue();
					}
				}
				
				if (line.hasFlag("pinToSurface")) effect.flags |= EMITMAP_PINTOSURFACE;
				if (line.hasFlag("density")) effect.flags |= EMITMAP_DENSITY;
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
				
				effect.flags |= FLAG_COLLIDEMAP;
				
				if (line.hasFlag("pinToMap")) {
					effect.flags |= COLLIDE_PINTOMAP;
					effect.mapBounce = 0;
				}
				else if (line.getOptionArguments(args, "bounce", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapBounce = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags |= FLAG_KILLOUTSIDEMAP;

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
				
				effect.flags |= FLAG_COLLIDEMAP;
				effect.flags |= FLAG_REPULSEMAP;

				if (line.getOptionArguments(args, "scout", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseScoutDistance = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "vertical", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseVertical = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "killHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseKillHeight = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags |= FLAG_KILLOUTSIDEMAP;

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
				
				effect.flags |= FLAG_COLLIDEMAP;
				effect.flags |= FLAG_ADVECTMAP;

				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags |= FLAG_KILLOUTSIDEMAP;

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
				
				effect.flags |= FLAG_COLLIDEMAP;
				effect.flags |= FLAG_FORCEMAP;

				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				}
				
				if (line.hasFlag("killOutsideMap")) effect.flags |= FLAG_KILLOUTSIDEMAP;

			}));
		}
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, ParticleEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new ParticleEffect(effectDirectory, version);
		}
		
//		@Override
//		public boolean buildInspector(InspectorUnit<ArgScriptStream<EffectUnit>> inspector, DocumentFragment fragment) {
//			// Is it a property or the effect itself?
//			boolean isEffect = ArgScriptInspector.checkKeyword(fragment, KEYWORD);
//			
//			if (!isEffect && !ArgScriptInspector.checkParent(fragment, KEYWORD)) {
//				return false;
//			}
//			
//			// The keyword might be inside an 'effect' block!
//			if (isEffect && ArgScriptInspector.checkParent(fragment, VisualEffect.KEYWORD)) {
//				return false;
//			}
//	
//			
//			if (isEffect) {
//				InspectorPanel p = new InspectorPanel(inspector, "Particles", fragment);
//				inspector.add(p);
//				
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Life", "life 1.0", "life"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Color", "color (1.0, 1.0, 1.0)", "color", "color255"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Alpha", "alpha 1.0", "alpha", "alpha255"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Size", "size 1.0", "size"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Aspect Ratio", "aspect 1.0", "aspect"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Rotate", "rotate 1.0", "rotate"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Stretch", "stretch 1.0", "stretch"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Source", "source -point", "source"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Emit", "emit -dir (0, 0, 1)", "emit"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Force", "force -gravity 0", "force"));
//				//TODO wrap
//
//				{
//					ASPropertySelection<EffectUnit> selection = new ASPropertySelection<EffectUnit>(inspector, fragment, "Walk", null);
//					ASPropertyGroup group1 = new ASPropertyGroup(0, 
//							"delay", "strength", "mix", "wait");
//					
//					selection.add(group1, "Random Walk", "randomWalk", "randomWalk");
//					selection.add(group1, "Directed Walk", "directedWalk", "directedWalk");
//					
//					selection.generateUI();
//				}
//
//				{
//					ASPropertySelection<EffectUnit> selection = new ASPropertySelection<EffectUnit>(inspector, fragment, "Emission Rate", null);
//					// mantain does not use all of them, but it's not that we need to remove them,
//					// and this way we can keep some important options like hold/kill
//					ASPropertyGroup group1 = new ASPropertyGroup(1, 
//							"delay", "hold", "kill", "loop", "single", "sustain",
//							"sizeScale", "areaScale", "volumeScale", "speedScale", "trigger");
//					
//					selection.add(group1, "Standard", "rate 100", "rate");
//					selection.add(group1, "Inject", "inject 100", "inject");
//					selection.add(group1, "Maintain", "maintain 100", "maintain");
//					
//					selection.generateUI();
//				}
//				
//				{
//					ASPropertySelection<EffectUnit> selection = new ASPropertySelection<EffectUnit>(inspector, fragment, "Resource", null);
//					ASPropertyGroup group1 = new ASPropertyGroup(1, 
//							"overrideSet", "draw", "buffer", "layer", "format", "sortOffset",
//							"light", "noFog", "shadow", "noCull", "user1", "user2", "user3");
//					
//					selection.add(group1, "Material", "material placeholder", "placeholder");
//					selection.add(group1, "Texture", "texture effects-puff", "texture");
//					selection.add(group1, "Model", "model placeholder", "model");
//					
//					selection.generateUI();
//				}
//				
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Frames", "frames -count 1", "frames"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Alignment", "align camera", "align"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "LoopBox Color", "loopBoxColor (1.0, 1.0, 1.0)", "loopBoxColor", "loopBoxColor255"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "LoopBox Alpha", "loopBoxAlpha 1.0", "loopBoxAlpha", "loopBoxAlpha255"));
//				
//				inspector.add(new ASPropertyList(inspector, fragment, "Surfaces", "particles.surfaces", "surface"));
//				//TODO surfaces
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Emission Map", "mapEmit black", "mapEmit"));
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "EmitColor Map", "mapEmitColor black", "mapEmitColor"));
//				
//				{
//					ASPropertySelection<EffectUnit> forceMapGroup = new ASPropertySelection<EffectUnit>(inspector, fragment, "Force Maps", null);
//					ASPropertyGroup group1 = new ASPropertyGroup(1);
//					
//					forceMapGroup.add(group1, "Collision Map", "mapCollide black", "mapCollide");
//					forceMapGroup.add(group1, "Repulsion Map", "mapRepel black 0 0", "mapRepel");
//					forceMapGroup.add(group1, "Advection Map", "mapAdvect black", "mapAdvect");
//					forceMapGroup.add(group1, "Force Map", "mapForce black", "mapForce");
//					
//					forceMapGroup.generateUI();
//				}
//				
//				inspector.add(ArgScriptInspector.createPropertyButton(inspector, fragment, "Physics Type", "physics normal", "physics"));
//				//TODO path
//				
//				return true;
//			}
//			
//			ASLineInspector<EffectUnit> lineInspector = new ASLineInspector<EffectUnit>(inspector, fragment);
//			String keyword = ArgScriptInspector.getKeyword(fragment);
//
//			switch (keyword) {
//			case "color":
//			case "color255":
//				inspector.add(new InspectorPanel(inspector, "Particles > Color", fragment));
//				
//				lineInspector.add(new ASColorRGBCurveInspector("Color Curve", "particles.color.", keyword.endsWith("255"), ColorRGB.white(), true));
//				lineInspector.add(new ASOptionInspector("vary", 
//						new ASColorRGBInspector("Curve Vary", "particles.color.vary", ColorRGB.black(), keyword.endsWith("255"))));
//				
//				break;
//				
//			case "size":
//				inspector.add(new InspectorPanel(inspector, "Particles > Size", fragment));
//				
//				lineInspector.add(new ASFloatCurveInspector("Size Curve", "particles.size.", 1.0f, true));
//				lineInspector.add(new ASOptionInspector("vary", 
//						new ASFloatInspector("Curve Vary", "particles.size.vary", 0.0f)));
//				
//				break;
//				
//			case "alpha":
//			case "alpha255":
//				inspector.add(new InspectorPanel(inspector, "Particles > Alpha", fragment));
//				
//				lineInspector.add(new ASFloatCurveInspector("Alpha Curve", "particles.alpha.", keyword.endsWith("255") ? 255.0f : 1.0f, true));
//				lineInspector.add(new ASOptionInspector("vary", 
//						new ASFloatInspector("Curve Vary", "particles.alpha.vary", 0.0f)));
//				
//				break;
//				
//			case "aspect":
//				inspector.add(new InspectorPanel(inspector, "Particles > Aspect", fragment));
//				
//				lineInspector.add(new ASFloatCurveInspector("Aspect Ratio Curve", "particles.aspect.", 1.0f, true));
//				lineInspector.add(new ASOptionInspector("vary", 
//						new ASFloatInspector("Curve Vary", "particles.aspect.vary", 0.0f)));
//				
//				break;
//				
//			case "rotate":
//				inspector.add(new InspectorPanel(inspector, "Particles > Rotate", fragment));
//				
//				lineInspector.add(new ASFloatCurveInspector("Rotation Curve", "particles.rotate.", 0.0f, true));
//				lineInspector.add(new ASOptionInspector("vary", 
//						new ASFloatInspector("Curve Vary", "particles.rotate.vary", 0.0f)));
//				lineInspector.add(new ASOptionInspector("offset", 
//						new ASFloatInspector("Curve Offset", "particles.rotate.offset", 0.0f)));
//				
//				break;
//				
//			case "stretch":
//				inspector.add(new InspectorPanel(inspector, "Particles > Stretch", fragment));
//				
//				lineInspector.add(new ASFloatInspector("Velocity Stretch", "particles.stretch", 0.0f));
//				
//				break;
//				
//			case "source":
//				inspector.add(new InspectorPanel(inspector, "Particles > Stretch", fragment));
//				
//				lineInspector.add(new ASVariableOption("Source Type", "particles.source.", "point",
//						new ASVariableOption.Value("Point", "point", "particles.source.point"),
//						new ASVariableOption.Value("Square", "square", "particles.source.square"),
//						new ASVariableOption.Value("Quad", "quad", "particles.source.quad"),
//						new ASVariableOption.Value("Cube", "cube", "particles.source.cube"),
//						new ASVariableOption.Value("Box", "box", "particles.source.box"),
//						new ASVariableOption.Value("Circle", "circle", "particles.source.circle"),
//						new ASVariableOption.Value("Sphere", "sphere", "particles.source.sphere"),
//						new ASVariableOption.Value("Ellipsoid", "ellipsoid", "particles.source.ellipsoid"),
//						new ASVariableOption.Value("Ring", "ring", "particles.source.ring"),
//						new ASVariableOption.Value("Torus", "torus", "particles.source.torus"))
//						.addOptions(new ASOptionGroup(0, 
//								new ASOptionInspector("point")))
//						.addOptions(new ASOptionGroup(1,
//								new ASOptionInspector("square",
//										new ASFloatInspector("Size", "particles.source.square.size", 1.0f)),
//								new ASOptionInspector("cube",
//										new ASFloatInspector("Size", "particles.source.cube.size", 1.0f)),
//								new ASOptionInspector("circle",
//										new ASFloatInspector("Radius", "particles.source.circle.radius", 1.0f)),
//								new ASOptionInspector("sphere",
//										new ASFloatInspector("Radius", "particles.source.sphere.radius", 1.0f))))
//						.addOptions(new ASOptionGroup(1,
//								new ASOptionInspector("quad",
//										new ASVector2Inspector("Dimensions", "particles.source.quad.dimensions", 1.0f))))
//						.addOptions(new ASOptionGroup(1,
//								new ASOptionInspector("box",
//										new ASVector3Inspector("Dimensions", "particles.source.box.dimensions", 1.0f)),
//								new ASOptionInspector("ellipsoid",
//										new ASVector3Inspector("Dimensions", "particles.source.ellipsoid.dimensions", 1.0f))))
//						.addOptions(new ASOptionGroup(1, 1, 
//								new ASOptionInspector("ring",
//										new ASFloatInspector("Radius", "particles.source.ring.radius", 1.0f),
//										new ASFloatInspector("Torus Width", "particles.source.ring.width", 1.0f).setRange(0.0f, 1.0f)),
//								new ASOptionInspector("torus",
//										new ASVector3Inspector("Dimensions", "particles.source.torus.dimensions", 1.0f),
//										new ASFloatInspector("Torus Width", "particles.source.torus.width", 1.0f).setRange(0.0f, 1.0f))))
//						);
//				
//					lineInspector.add(new ASOptionInspector("offset", 
//							new ASVector3Inspector("Offset", "particles.source.offset", 0.0f)));
//					
//					lineInspector.add(new ASFlagOption("scaleParticles", "Scale Particles", "particles.source.scaleParticles"));
//					lineInspector.add(new ASFlagOption("resetIncoming", "Reset Incoming", "particles.source.resetIncoming"));
//				
//				break;
//				
//			case "emit":
//				inspector.add(new InspectorPanel(inspector, "Particles > Emit", fragment));
//				
//				lineInspector.add(new ASOptionInspector("speed", 
//						new ASFloatInspector("Speed", "particles.emit.speed.", 0.0f),
//						new ASFloatInspector("Speed Vary", "particles.emit.speed.vary", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("dir", 
//						new ASVector3Inspector("Direction Offset", "particles.emit.dir.", new Vector3(0, 0, 1)),
//						new ASVector3Inspector("Direction Vary", "particles.emit.dir.vary", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASFlagOption("base", "Base", "particles.emit.base"));
//				lineInspector.add(new ASFlagOption("scaleExisting", "Scale Existing", "particles.emit.scaleExisting"));
//				
//				break;
//				
//			case "force":
//				inspector.add(new InspectorPanel(inspector, "Particles > Force", fragment));
//				
//				lineInspector.add(new ASFlagOption("reset", "Reset", "particles.force.reset"));
//				
//				inspector.add(new Separator());
//				
//				lineInspector.add(new ASOptionInspector("gravity", 
//						new ASFloatInspector("Gravity", "particles.force.gravity", 0.0f)));
//				
//				lineInspector.add(new ASOptionInspector("worldGravity", 
//						new ASFloatInspector("World Gravity", "particles.force.worldGravity", 0.0f)));
//				
//				inspector.add(new Separator());
//				
//				lineInspector.add(new ASOptionInspector("wind", 
//						new ASVector3Inspector("Wind Direction", "particles.force.wind.direction", 0.0f),
//						new ASFloatInspector("Wind Strength", "particles.force.wind.strength", 1.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("worldWind", 
//						new ASFloatInspector("World Wind", "particles.force.worldWind", 0.0f)));
//				
//				inspector.add(new Separator());
//				
//				lineInspector.add(new ASOptionInspector("bomb", 
//						new ASFloatInspector("Bomb Strength", "particles.force.bomb.strength", 1.0f),
//						new ASVector3Inspector("Bomb Location", "particles.force.bomb.location", 0.0f))
//						.setOptionalIndex(1));
//				
//				inspector.add(new Separator());
//				
//				lineInspector.add(new ASOptionInspector("drag", 
//						new ASFloatInspector("Drag", "particles.force.drag", 0.0f)));
//				
//				//TODO attractors
//				
//				break;
//				
//				//TODO wrap
//				
//			case "randomWalk":
//				inspector.add(new InspectorPanel(inspector, "Particles > Random Walk", fragment));
//				
//				lineInspector.add(new ASOptionInspector("delay", 
//						new ASFloatInspector("Delay", "particles.randomWalk.delay", 5.0f),
//						new ASFloatInspector("Delay Vary", "particles.randomWalk.delayVary", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("strength", 
//						new ASFloatInspector("Strength", "particles.randomWalk.strength", 0.0f),
//						new ASFloatInspector("Strength Vary", "particles.randomWalk.strengthVary", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("turn", 
//						new ASFloatInspector("Turn", "particles.randomWalk.turn", 0.25f),
//						new ASFloatInspector("Turn Offset", "particles.randomWalk.turnOffset", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("mix", 
//						new ASFloatInspector("Mix", "particles.randomWalk.mix", 0.25f)));
//				
//				lineInspector.add(new ASFlagOption("wait", "Wait", "particles.randomWalk.wait"));
//				lineInspector.add(new ASFlagOption("scaleExisting", "Scale Existing", "particles.emit.scaleExisting"));
//				
//				break;
//				
//			case "directedWalk":
//				inspector.add(new InspectorPanel(inspector, "Particles > Directed Walk", fragment));
//				
//				lineInspector.add(new ASFloatCurveInspector("Turn Offset Curve", "particles.directedWalk.turnOffset", 0.0f, false).setRange(-1, 1));
//				
//				lineInspector.add(new ASOptionInspector("delay", 
//						new ASFloatInspector("Delay", "particles.randomWalk.delay", 5.0f),
//						new ASFloatInspector("Delay Vary", "particles.randomWalk.delayVary", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("strength", 
//						new ASFloatInspector("Strength", "particles.randomWalk.strength", 0.0f),
//						new ASFloatInspector("Strength Vary", "particles.randomWalk.strengthVary", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("randomTurn", 
//						new ASFloatInspector("Turn", "particles.directedWalk.randomTurn", 0.25f),
//						new ASFloatInspector("Turn Offset", "particles.directedWalk.randomTurnOffset", 0.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASOptionInspector("mix", 
//						new ASFloatInspector("Mix", "particles.randomWalk.mix", 0.25f)));
//				
//				lineInspector.add(new ASVariableOption("Loop Type", "particles.directedWalk.loopType", null, 
//						new ASVariableOption.Value("Sustain", "sustain", "particles.directedWalk.sustain"),
//						new ASVariableOption.Value("Loop", "loop", "particles.directedWalk.loop"),
//						new ASVariableOption.Value("None", null, "particles.directedWalk.none"))
//						.addOptions(new ASOptionGroup(0, 
//								new ASOptionInspector("sustain"),
//								new ASOptionInspector("loop"))));
//				
//				lineInspector.add(new ASFlagOption("wait", "Wait", "particles.randomWalk.wait"));
//				
//				break;
//				
//			case "life":
//				inspector.add(new InspectorPanel(inspector, "Particles > Life", fragment));
//				
//				lineInspector.setOptionalIndex(1);
//				lineInspector.add(new ASFloatInspector("Life Time", "particles.life.", 0.0f));
//				lineInspector.add(new ASFloatInspector("Life Time Vary", "particles.life.vary", 0.0f));
//				
//				lineInspector.add(new ASOptionInspector("preroll", 
//						new ASFloatInspector("Preroll", "particles.life.preroll", 0.0f)));
//				
//				break;
//				
//			case "rate":
//				inspector.add(new InspectorPanel(inspector, "Particles > Standard Emission", fragment));
//				
//				lineInspector.add(new ASFloatInspector("Quantity", "particles.rate.", 100));
//				
//				generateRateMainInspector(lineInspector);
//				
//				break;
//
//			case "inject":
//				inspector.add(new InspectorPanel(inspector, "Particles > Injected Emission", fragment));
//				
//				lineInspector.add(new ASFloatInspector("Quantity", "particles.inject.", 100));
//				
//				generateRateMainInspector(lineInspector);
//				
//				break;
//				
//			case "maintain":
//				inspector.add(new InspectorPanel(inspector, "Particles > Maintained Emission", fragment));
//				
//				lineInspector.add(new ASFloatInspector("Quantity", "particles.maintain.", 100));
//				
//				lineInspector.add(new ASOptionInspector("delay", 
//						new ASFloatInspector("Minimum Delay", "particles.maintain.minDelay", -1.0f),
//						new ASFloatInspector("Maximum Delay", "particles.maintain.maxDelay", -1.0f))
//						.setOptionalIndex(1));
//				
//				lineInspector.add(new ASVariableOption("Action", "particles.maintain.action", null,
//						new ASVariableOption.Value("Hold", "hold", "particles.maintain.hold"),
//						new ASVariableOption.Value("Kill", "kill", "particles.maintain.kill"),
//						new ASVariableOption.Value("None", null, "particles.maintain.none"))
//						.addOptions(new ASOptionGroup(0, 
//								new ASOptionInspector("hold", 
//										new ASFloatInspector("Duration", "particles.maintain.hold.duration", 1.0f),
//										new ASIntInspector("Repetitions", "particles.maintain.hold.repetitions", 1).setRange(Short.MIN_VALUE, Short.MAX_VALUE))
//								.setOptionalIndex(1),
//								new ASOptionInspector("kill", 
//										new ASFloatInspector("Duration", "particles.maintain.kill.duration", 0.1f)))));
//				
//				break;
//				
//			case "material":
//				inspector.add(new InspectorPanel(inspector, "Particles > Material", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Resource", "particles.material.", "placeholder"));
//				
//				lineInspector.add(new ASOptionInspector("overrideSet", 
//						new ASIntInspector("Override Set", "particles.material.overrideSet", 0).setRange(0, Byte.MAX_VALUE)));
//				
//				TextureSlot.generateInspector(lineInspector, true, true);
//				
//				break;
//				
//			case "texture":
//				inspector.add(new InspectorPanel(inspector, "Particles > Texture", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Resource", "particles.texture.", "texture-star"));
//				
//				lineInspector.add(new ASOptionInspector("tile", 
//						new ASIntInspector("Tile Rows", "particles.texture.tileRows", 1).setRange(0, Byte.MAX_VALUE),
//						new ASIntInspector("Tile Columns", "particles.texture.tileCols", 1).setRange(0, Byte.MAX_VALUE)));
//				
//				lineInspector.add(new ASOptionInspector("overrideSet", 
//						new ASIntInspector("Override Set", "particles.texture.overrideSet", 0).setRange(0, Byte.MAX_VALUE)));
//				
//				lineInspector.add(new ASFlagOption("acceptComposite", "Accept Composite", "particles.texture.acceptComposite"));
//				
//				TextureSlot.generateInspector(lineInspector, true, false);
//				
//				break;
//				
//			case "model":
//				inspector.add(new InspectorPanel(inspector, "Particles > Model", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Resource", "particles.model.", "placeholder"));
//				
//				lineInspector.add(new ASOptionInspector("material", 
//						new ASStringInspector("Material", "particles.model.material", null)));
//				
//				lineInspector.add(new ASOptionInspector("overrideSet", 
//						new ASIntInspector("Override Set", "particles.model.overrideSet", 0).setRange(0, Byte.MAX_VALUE)));
//				
//				TextureSlot.generateInspector(lineInspector, false, false);
//				
//				break;
//			
//			case "frames":
//				inspector.add(new InspectorPanel(inspector, "Particles > Frames", fragment));
//				
//				lineInspector.add(new ASOptionInspector("speed", 
//						new ASFloatInspector("Speed", "particles.frames.speed", 0.0f)));
//				
//				lineInspector.add(new ASOptionInspector("count", 
//						new ASIntInspector("Count", "particles.frames.count", 0).setRange(Byte.MIN_VALUE, Byte.MAX_VALUE)));
//				
//				lineInspector.add(new ASOptionInspector("start", 
//						new ASIntInspector("Start", "particles.frames.start", 0).setRange(0, Byte.MAX_VALUE)));
//				
//				lineInspector.add(new ASOptionInspector("random", 
//						new ASIntInspector("Random", "particles.frames.random", 0).setRange(Byte.MIN_VALUE, Byte.MAX_VALUE)));
//				
//				break;
//
//			case "align":
//				inspector.add(new InspectorPanel(inspector, "Particles > Alignment", fragment));
//				
//				lineInspector.add(new ASEnumInspector("Alignment Type", "particles.align", "camera",
//						new ASEnumInspector.Value("Camera", "camera", "particles.align.camera"),
//						new ASEnumInspector.Value("Ground", "ground", "particles.align.ground"),
//						new ASEnumInspector.Value("Source", "source", "particles.align.source"),
//						new ASEnumInspector.Value("X Direction", "dirX", "particles.align.dirX"),
//						new ASEnumInspector.Value("Y Direction", "dirY", "particles.align.dirY"),
//						new ASEnumInspector.Value("Z Direction", "dirZ", "particles.align.dirZ"),
//						new ASEnumInspector.Value("Z Pole", "zPole", "particles.align.zPole"),
//						new ASEnumInspector.Value("Sun Pole", "sunPole", "particles.align.sunPole"),
//						new ASEnumInspector.Value("Camera Location", "cameraLocation", "particles.align.cameraLocation")));
//				
//				break;
//				
//			case "loopBoxColor":
//			case "loopBoxColor255":
//				inspector.add(new InspectorPanel(inspector, "Particles > LoopBox Color", fragment));
//				
//				lineInspector.add(new ASColorRGBCurveInspector("Color Curve", "particles.loopBoxColor.", keyword.endsWith("255"), ColorRGB.white(), true));
//				
//				break;
//				
//			case "loopBoxAlpha":
//			case "loopBoxAlpha255":
//				inspector.add(new InspectorPanel(inspector, "Particles > LoopBox Alpha", fragment));
//				
//				lineInspector.add(new ASFloatCurveInspector("Alpha Curve", "particles.loopBoxAlpha.", keyword.endsWith("255") ? 255.0f : 1.0f, true));
//				
//				break;
//				
//				//TODO surfaces
//			
//			case "mapEmit":
//				inspector.add(new InspectorPanel(inspector, "Particles > Emission Map", fragment));
//				
//				lineInspector.add(new ASStringInspector("Map Resource", "particles.mapEmit.", "black"));
//				
//				lineInspector.add(new ASOptionInspector("belowHeight", 
//						new ASFloatInspector("Below Height", "particles.mapEmit.belowHeight", 10000f).setStep(1000f)));
//				
//				lineInspector.add(new ASOptionInspector("aboveHeight", 
//						new ASFloatInspector("Above Height", "particles.mapEmit.aboveHeight", -10000f).setStep(1000f)));
//				
//				lineInspector.add(new ASFlagOption("pinToSurface", "Pin to Surface", "particles.mapEmit.pinToSurface"));
//				lineInspector.add(new ASFlagOption("density", "Density", "particles.mapEmit.density"));
//				
//				break;
//			
//			case "mapEmitColor":
//				inspector.add(new InspectorPanel(inspector, "Particles > EmitColor Map", fragment));
//				
//				lineInspector.add(new ASStringInspector("Map Resource", "particles.mapEmitColor.", "black"));
//				
//				break;
//				
//				
//			case "mapCollide":
//				inspector.add(new InspectorPanel(inspector, "Particles > Collision Map", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Map Resource", "particles.mapCollide.", "black"));
//				
//				lineInspector.add(new ASFlagOption("pinToMap", "Pin To Map", "particles.mapCollide.pinToMap")
//						.orElse(new ASOptionInspector("bounce",
//								new ASFloatInspector("Bounce", "particles.mapCollide.bounce", 1.0f))));
//				
//				lineInspector.add(new ASFlagOption("killOutsideMap", "Kill Outside Map", "particles.mapCollide.killOutsideMap"));
//				
//				lineInspector.add(new ASOptionInspector("death", 
//						new ASFloatInspector("Death Probability", "particles.mapCollide.death", 0.0f).setRange(0.0f, 1.0f)));
//				
//				break;
//				
//			case "mapRepel":
//				inspector.add(new InspectorPanel(inspector, "Particles > Repulsion Map", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Map Resource", "particles.mapRepel.", "black"));
//				// Warning, these two will be considered optional although they aren't.
//				// Setting them non-optional might fix it, untested.
//				lineInspector.add(new ASFloatInspector("Repulsion Height", "particles.mapRepel.height", 0.0f).setOptional(false));
//				lineInspector.add(new ASFloatInspector("Repulsion Strength", "particles.mapRepel.strength", 0.0f).setOptional(false));
//				
//				lineInspector.add(new ASOptionInspector("scout", 
//						new ASFloatInspector("Scout Distance", "particles.mapRepel.scout", 0.0f)));
//				
//				lineInspector.add(new ASOptionInspector("vertical", 
//						new ASFloatInspector("Vertical Repulsion", "particles.mapRepel.vertical", 0.0f)));
//				
//				lineInspector.add(new ASOptionInspector("killHeight", 
//						new ASFloatInspector("Kill Height", "particles.mapRepel.killHeight", -1000000000f).setStep(100000f)));
//				
//				lineInspector.add(new ASFlagOption("killOutsideMap", "Kill Outside Map", "particles.mapRepel.killOutsideMap"));
//				
//				break;
//				
//			case "mapAdvect":
//				inspector.add(new InspectorPanel(inspector, "Particles > Advection Map", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Map Resource", "particles.mapAdvect.", "black"));
//				
//				lineInspector.add(new ASOptionInspector("strength", 
//						new ASFloatInspector("Advection Strength", "particles.mapAdvect.strength", 0.0f)));
//				
//				lineInspector.add(new ASFlagOption("killOutsideMap", "Kill Outside Map", "particles.mapAdvect.killOutsideMap"));
//				
//				break;
//
//			case "mapForce":
//				inspector.add(new InspectorPanel(inspector, "Particles > Force Map", fragment));
//				
//				lineInspector.setOptionalIndex(0);
//				lineInspector.add(new ASStringInspector("Map Resource", "particles.mapForce.", "black"));
//				
//				lineInspector.add(new ASOptionInspector("strength", 
//						new ASFloatInspector("Force Strength", "particles.mapForce.strength", 0.0f)));
//				
//				lineInspector.add(new ASFlagOption("killOutsideMap", "Kill Outside Map", "particles.mapForce.killOutsideMap"));
//				
//			case "physics":
//				inspector.add(new InspectorPanel(inspector, "Particles > Physics Type", fragment));
//				
//				lineInspector.add(new ASEnumInspector("Physics Type", "particles.physics", "normal",
//						new ASEnumInspector.Value("User 1", "user1", "particles.physics.user1"),
//						new ASEnumInspector.Value("User 2", "user2", "particles.physics.user2"),
//						new ASEnumInspector.Value("User 3", "user3", "particles.physics.user3"),
//						new ASEnumInspector.Value("User 4", "user4", "particles.physics.user4"),
//						new ASEnumInspector.Value("Normal", "normal", "particles.physics.normal"),
//						new ASEnumInspector.Value("Unknown 1", "unk1", "particles.physics.unk1"),
//						new ASEnumInspector.Value("Unknown 2", "unk2", "particles.physics.unk2")));
//				
//				break;
//				
//				//TODO path
//			}
//			
//			return true;
//		}
//		
//		private static void generateRateMainInspector(ASLineInspector<EffectUnit> lineInspector) {
//
//			lineInspector.add(new ASVariableOption("Emission Type", "particles.rate.type", null,
//					new ASVariableOption.Value("Loop", "loop", "particles.rate.loop"),
//					new ASVariableOption.Value("Single", "single", "particles.rate.single"),
//					new ASVariableOption.Value("Sustain", "sustain", "particles.rate.sustain"),
//					new ASVariableOption.Value("Hold", "hold", "particles.rate.hold"),
//					new ASVariableOption.Value("Kill", "kill", "particles.rate.kill"),
//					new ASVariableOption.Value("None", null, "particles.rate.none"))
//					.addOptions(new ASOptionGroup(0, 
//							new ASOptionInspector("loop", 
//									new ASFloatInspector("Duration", "particles.rate.loop.duration", 1.0f),
//									new ASIntInspector("Repetitions", "particles.rate.loop.repetitions", 0).setRange(Short.MIN_VALUE, Short.MAX_VALUE))
//									.setOptionalIndex(1),
//							new ASOptionInspector("single", 
//									new ASFloatInspector("Duration", "particles.rate.single.duration", 0.1f))
//									.setOptionalIndex(0),
//							new ASOptionInspector("sustain", 
//									new ASFloatInspector("Duration", "particles.rate.sustain.duration", 0.1f),
//									new ASIntInspector("Repetitions", "particles.rate.sustain.repetitions", 1).setRange(Short.MIN_VALUE, Short.MAX_VALUE))
//									.setOptionalIndex(1),
//							new ASOptionInspector("hold", 
//									new ASFloatInspector("Duration", "particles.rate.hold.duration", 0.1f),
//									new ASIntInspector("Repetitions", "particles.rate.hold.repetitions", 1).setRange(Short.MIN_VALUE, Short.MAX_VALUE))
//									.setOptionalIndex(1),
//							new ASOptionInspector("kill", 
//									new ASFloatInspector("Duration", "particles.rate.kill.duration", 0.1f))
//									.setOptionalIndex(0)
//									)));
//			
//			lineInspector.add(new ASFlagOption("sizeScale", "Size Scale", "particles.rate.sizeScale"));
//			lineInspector.add(new ASFlagOption("areaScale", "Area Scale", "particles.rate.areaScale"));
//			lineInspector.add(new ASFlagOption("volumeScale", "Volume Scale", "particles.rate.volumeScale"));
//			
//			lineInspector.add(new ASOptionInspector("speedScale", 
//					new ASFloatInspector("Speed Scale", "particles.rate.speedScale", 1.0f)));
//			
//			lineInspector.add(new ASOptionInspector("delay", 
//					new ASFloatInspector("Minimum Delay", "particles.rate.minDelay", -1.0f),
//					new ASFloatInspector("Maximum Delay", "particles.rate.maxDelay", -1.0f))
//					.setOptionalIndex(1));
//			
//			lineInspector.add(new ASOptionInspector("trigger", 
//					new ASFloatInspector("Minimum Trigger", "particles.rate.minTrigger", -1.0f),
//					new ASFloatInspector("Maximum Trigger", "particles.rate.maxTrigger", -1.0f))
//					.setOptionalIndex(1));
//		}

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
		
		writer.command("color").colors(color);
		if (!colorVary.isBlack()) writer.option("vary").color(colorVary);
		
		writer.command("alpha").floats(alpha);
		if (alphaVary != 0) writer.option("vary").floats(alphaVary);
		
		writer.command("size").floats(size);
		if (sizeVary != 0) writer.option("vary").floats(sizeVary);
		
		if (!writer.isDefault(aspectRatio)) {
			writer.command("aspect").floats(aspectRatio);
			if (aspectRatioVary != 0) writer.option("vary").floats(aspectRatioVary);
		}
		
		writer.command("rotate").floats(rotate);
		if (rotationVary != 0) writer.option("vary").floats(rotationVary);
		if (rotationOffset != 0) writer.option("offset").floats(rotationOffset);
		
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
		if ((flags & FLAG_LOOPBOX) == FLAG_LOOPBOX) {
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
		
		writer.endBlock().commandEND();
	}
	
	private void writeSource(ArgScriptWriter writer) {
		
		if (emitVolumeBBMin[0] != 0 || emitVolumeBBMin[1] != 0 || emitVolumeBBMin[2] != 0 
				|| emitVolumeBBMax[0] != 0 || emitVolumeBBMax[1] != 0 || emitVolumeBBMax[2] != 0
				|| torusWidth != -1 || (flags & (SOURCE_SCALEPARTICLES | SOURCE_RESETINCOMING)) != 0) {
			
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
			
			if ((flags & SOURCE_ROUND) == SOURCE_ROUND) {
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
			
			writer.flag("scaleParticles", (flags & SOURCE_SCALEPARTICLES) == SOURCE_SCALEPARTICLES);
			writer.flag("resetIncoming", (flags & SOURCE_RESETINCOMING) == SOURCE_RESETINCOMING);
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
		
		if ((flags & EMIT_BASE) == EMIT_BASE) {
			if (!commandWritten) writer.command("emit");
			commandWritten = true;
			writer.option("base");
		}
		
		if ((flags & EMIT_SCALEEXISTING) == EMIT_SCALEEXISTING) {
			if (!commandWritten) writer.command("emit");
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
				writer.option("wind").vector(directionForcesSum);
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
			
			if (!attractor.attractorStrength.isEmpty()) {
				if ((flags & ATTRACTOR_LOCATION) == ATTRACTOR_LOCATION) writer.option("attractor").vector(attractorOrigin);
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
			
			if ((flags & WARP_SPIRAL) == WARP_SPIRAL) writer.option("spiral");
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
		if ((flags & FLAG_RANDOMWALK) == FLAG_RANDOMWALK) {
			writer.command("randomWalk").floats(randomWalk.turnOffsetCurve);
			
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
			writer.flag("wait", (flags & RANDOMWALK_WAIT) == RANDOMWALK_WAIT);
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
		
		writer.flag("propagateAlways", (unkFlags & LIFE_PROPAGATEALWAYS) == LIFE_PROPAGATEALWAYS);
		writer.flag("propagateIfKilled", (unkFlags & LIFE_PROPAGATEIFKILLED) == LIFE_PROPAGATEIFKILLED);
	}
	
	private void writeRate(ArgScriptWriter writer) {
		if ((flags & FLAG_INJECT) == FLAG_INJECT) {
			writer.command("inject").floats(rate.get(0));
		} else if ((flags & FLAG_MAINTAIN) == FLAG_MAINTAIN) {
			writer.command("maintain").floats(rate.get(0));
			if (emitDelay[0] != -1 || emitDelay[1] != -1) {
				writer.option("delay").floats(emitDelay[0]);
				if (emitDelay[1] != emitDelay[0]) writer.floats(emitDelay[1]);
			}
			if ((flags & FLAG_HOLD) == FLAG_HOLD) {
				writer.option("hold").floats(rateLoop);
				if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
			} 
			else if ((flags & FLAG_KILL) == FLAG_KILL) {
				writer.option("kill").floats(rateLoop);
			}
			// This has no more options
			return;
		} else {
			writer.command("rate").floats(rate);
		}
		
		if ((flags & FLAG_SUSTAIN) == FLAG_SUSTAIN) {
			writer.option("sustain").floats(rateLoop);
			if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
		}
		else if ((flags & FLAG_HOLD) == FLAG_HOLD) {
			writer.option("hold").floats(rateLoop);
			if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
		} 
		else if ((flags & FLAG_KILL) == FLAG_KILL) {
			writer.option("kill").floats(rateLoop);
		}
		else if (rateLoop != 0.1f && rateCurveCycles == 1) {
			writer.option("single").floats(rateLoop);
		}
		else {
			writer.option("loop").floats(rateLoop);
			if (rateCurveCycles != 0) writer.ints(rateCurveCycles);
		}
	}
	
	private void writeResource(ArgScriptWriter writer) {
		if ((flags & FLAG_MODEL) == FLAG_MODEL) {
			writer.command("model");
			if (!texture.resource.isDefault()) writer.arguments(texture.resource);
			
			if (!texture.resource2.isDefault()) writer.option("material").arguments(texture.resource2);
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
		writer.flag("acceptComposite", (flags & FLAG_ACCEPTCOMPOSITE) == FLAG_ACCEPTCOMPOSITE);
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
			if ((flags & EMITMAP_PINTOSURFACE) == EMITMAP_PINTOSURFACE) writer.option("pinToSurface");
			if ((flags & EMITMAP_HEIGHT) == EMITMAP_HEIGHT) {
				if (altitudeRange[0] != -10000.0f && altitudeRange[1] != 10000.0f) {
					writer.option("heightRange").floats(altitudeRange);
				}
				else if (altitudeRange[1] != 10000.0f) {
					writer.option("belowHeight").floats(altitudeRange[1]);
				}
				else writer.option("aboveHeight").floats(altitudeRange[0]);
			}
			if ((flags & EMITMAP_DENSITY) == EMITMAP_DENSITY) writer.option("density");
		}
	}
	
	private void writeCollideMap(ArgScriptWriter writer) {
		if ((flags & FLAG_COLLIDEMAP) == FLAG_COLLIDEMAP) {
			writer.command("mapCollide");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			if ((flags & COLLIDE_PINTOMAP) == COLLIDE_PINTOMAP) writer.option("pinToMap");
			else if (mapBounce != 1.0f) writer.option("bounce").floats(mapBounce);
			
			writer.flag("killOutsideMap", (flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP);
			
			if (probabilityDeath != 0) writer.option("death").floats(probabilityDeath);
		}
	}
	
	private void writeRepelMap(ArgScriptWriter writer) {
		if ((flags & FLAG_REPULSEMAP) == FLAG_REPULSEMAP) {
			writer.command("mapRepel");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			writer.floats(mapRepulseHeight, mapRepulseStrength);
			
			if (mapRepulseScoutDistance != 0) writer.option("scout").floats(mapRepulseScoutDistance);
			if (mapRepulseVertical != 0) writer.option("vertical").floats(mapRepulseVertical);
			if (mapRepulseKillHeight != -1000000000.0f) writer.option("killHeight").floats(mapRepulseKillHeight);
			
			writer.flag("killOutsideMap", (flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP);
		}
	}
	
	private void writeAdvectMap(ArgScriptWriter writer) {
		if ((flags & FLAG_ADVECTMAP) == FLAG_ADVECTMAP) {
			writer.command("mapAdvect");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			if (mapRepulseStrength != 0) writer.option("strength").floats(mapRepulseStrength);
			writer.flag("killOutsideMap", (flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP);
		}
	}
	
	private void writeForceMap(ArgScriptWriter writer) {
		if ((flags & FLAG_FORCEMAP) == FLAG_FORCEMAP) {
			writer.command("mapForce");
			if (!mapForce.isZero()) writer.arguments(mapForce);
			
			if (mapRepulseStrength != 0) writer.option("strength").floats(mapRepulseStrength);
			writer.flag("killOutsideMap", (flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP);
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
			if ((flags & FLAG_MODEL) == FLAG_MODEL) {
				list.add(effectDirectory.getResource(MaterialResource.TYPE_CODE, texture.resource2));
			} else {
				list.add(effectDirectory.getResource(MaterialResource.TYPE_CODE, texture.resource));
			}
		}
		
		return list;
	}
}
