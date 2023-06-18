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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
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
public class FastParticleEffect extends EffectComponent {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<FastParticleEffect> STRUCTURE_METADATA = StructureMetadata.generate(FastParticleEffect.class);
	
	public static final String KEYWORD = "fastParticles";
	public static final int TYPE_CODE = 0x000C;
	
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
	
	public static final int SOURCE_ROUND = 0x10;
	
	public static final int EMIT_BASE = 8;  // are we sure ?
	
	public static final int FLAG_SUSTAIN = 4;
	public static final int FLAG_INJECT = 1;
	public static final int FLAG_MAINTAIN = 2;
	public static final int RATE_SIZESCALE = 0x100;  // are we sure ?
	public static final int RATE_AREASCALE = 0x200;  // are we sure ?
	public static final int RATE_VOLUMESCALE = 0x400;  // are we sure ?
	
	
	public static final int FLAGMASK = SOURCE_ROUND | EMIT_BASE
			| FLAG_SUSTAIN | FLAG_INJECT | FLAG_MAINTAIN | RATE_SIZESCALE | RATE_AREASCALE | RATE_VOLUMESCALE;
	
	
	public int flags;  // & 7FF
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] life = new float[2];
	public float prerollTime;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitDelay = {-1, -1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitRetrigger = {-1, -1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitDirectionBBMin = {0, 0, 1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitDirectionBBMax = {0, 0, 1};
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitSpeed = new float[2];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitVolumeBBMin = new float[3];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] emitVolumeBBMax = new float[3];
	@StructureLength.Value(32) public final List<Float> rate = new ArrayList<Float>();
	/** In seconds, the time the main effect takes to die and start again. */
	public float rateLoop;
	public short rateCurveCycles;
	public float rateSpeedScale;
	@StructureLength.Value(32) public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	public final TextureSlot texture = new TextureSlot();
	public byte alignMode;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] directionForcesSum = new float[3];
	public float windStrength;
	public float gravityStrength;
	public float radialForce;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] radialForceLocation = new float[3];
	public float drag;
	
	public FastParticleEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		FastParticleEffect effect = (FastParticleEffect) _effect;
		
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
		
		rate.addAll(effect.rate);
		rateLoop = effect.rateLoop;
		rateCurveCycles = effect.rateCurveCycles;
		rateSpeedScale = effect.rateSpeedScale;
		
		size.addAll(effect.size);
		alpha.addAll(effect.alpha);
		color.addAll(effect.color);
		
		texture.copy(effect.texture);
		
		alignMode = effect.alignMode;
		
		EffectDirectory.copyArray(directionForcesSum, effect.directionForcesSum);
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		radialForce = effect.radialForce;
		EffectDirectory.copyArray(radialForceLocation, effect.radialForceLocation);
		drag = effect.drag;
	}
	
	protected static class Parser extends EffectBlockParser<FastParticleEffect> {
		@Override
		protected FastParticleEffect createEffect(EffectDirectory effectDirectory) {
			return new FastParticleEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
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
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloats(args, effect.alpha);
				}
			}));
			
			this.addParser("alpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloat255s(args, effect.alpha);
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_TEXTURE);
			}));
			
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				effect.texture.parse(stream, line, PfxEditor.HYPERLINK_MATERIAL);
				effect.texture.drawMode = TextureSlot.DRAWMODE_NONE;
			}));
			
			this.addParser("align", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.alignMode = (byte) ENUM_ALIGNMENT.get(args, 0);
				}
			}));
			
			parseSource();
			parseEmit();
			parseForce();
			parseLife();
			parseRate();
			parseMaintain();
			parseInject();
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value;
				if (line.getArguments(args, 1) && (value = stream.parseUInt(args, 0)) != null) {
					effect.flags |= value.intValue() & ~FLAGMASK;
				}
			}));
		}
		
		private void parseSource() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("source", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
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
					
					if (line.getOptionArguments(args, "quad", 1)) {
						float[] array = new float[2];
						if (stream.parseVector2(args, 0, array)) {
							min_x -= array[0];
							min_y -= array[1];
							max_x += array[0];
							max_y += array[1];
						}
					}
					
					if (line.getOptionArguments(args, "cube", 1) && (value = stream.parseFloat(args, 0)) != null) {
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						min_z -= size;
						max_x += size;
						max_y += size;
						max_z += size;
					}
					
					if (line.getOptionArguments(args, "box", 1)) {
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
					
					if (line.getOptionArguments(args, "circle", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.flags |= SOURCE_ROUND;
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						max_x += size;
						max_y += size;
					}
					
					if (line.getOptionArguments(args, "sphere", 1) && (value = stream.parseFloat(args, 0)) != null) {
						effect.flags |= SOURCE_ROUND;
						float size = value.floatValue();
						min_x -= size;
						min_y -= size;
						min_z -= size;
						max_x += size;
						max_y += size;
						max_z += size;
					}
					
					if (line.getOptionArguments(args, "ellipse", 1) && line.getOptionArguments(args, "ellipsoid", 1)) {
						effect.flags |= SOURCE_ROUND;
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
					
					// Normal particle effects would have 'torus' and 'ring', fastParticles don't
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
			}));
		}
		
		private void parseRateMain(ArgScriptLine line) {
			final ArgScriptArguments args = new ArgScriptArguments();
			Number value = null;
			
			if (line.getOptionArguments(args, "loop", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.rateLoop = value.floatValue();
				
				if (args.size() == 2 && (value = stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
					effect.rateCurveCycles = value.shortValue();
				}
				else {
					effect.rateCurveCycles = 0;
				}
			}
			else if (line.getOptionArguments(args, "single", 0, 1)) {
				effect.rateCurveCycles = 1;
				
				if (args.size() == 1 && (value = stream.parseFloat(args, 0)) != null) {
					effect.rateLoop = value.floatValue();
				}
				else {
					effect.rateLoop = 0.1f;
				}
			}
			else if (line.getOptionArguments(args, "sustain", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_SUSTAIN;
				effect.rateLoop = value.floatValue();
				
				if (args.size() == 2 && (value = stream.parseInt(args, 1, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
					effect.rateCurveCycles = value.shortValue();
				}
				else {
					effect.rateCurveCycles = 1;
				}
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
			}));
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
					effect.prerollTime = value.floatValue() > 0.5f ? value.floatValue() : 0.5f;
				}
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
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						stream.parseVector3(args, 0, effect.radialForceLocation);
					}
				}
				if (line.getOptionArguments(args, "drag", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.drag = value.floatValue();
				}
				// particle effects also use attractors
			}));
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return FastParticleEffect.class;
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
			return new FastParticleEffect(effectDirectory, version);
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
		
		writer.command("color").colors(color);
		writer.command("alpha").floats(alpha);
		writer.command("size").floats(size);
		writeSource(writer);
		writeEmit(writer);
		writeForce(writer);
		writeLife(writer);
		writeRate(writer);
		writeResource(writer);
		if (alignMode != 0) writer.command("align").arguments(ENUM_ALIGNMENT.get(alignMode));
		
		if ((flags & ~FLAGMASK) != 0) {
			writer.blankLine();
			writer.command("flags").arguments(HashManager.get().hexToString(flags & ~FLAGMASK));
		}
		
		writer.endBlock().commandEND();
	}
	
	private void writeSource(ArgScriptWriter writer) {
		
		if (emitVolumeBBMin[0] != 0 || emitVolumeBBMin[1] != 0 || emitVolumeBBMin[2] != 0 
				|| emitVolumeBBMax[0] != 0 || emitVolumeBBMax[1] != 0 || emitVolumeBBMax[2] != 0) {
			
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
				if (min_z != 0 || max_z != 0) {
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
			writer.option("base");
		}
	}
	
	private void writeResource(ArgScriptWriter writer) {
		texture.toArgScript(texture.drawMode == TextureSlot.DRAWMODE_NONE ? "material" : "texture", writer);
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
			// This has no more options
			return;
		} else {
			writer.command("rate").floats(rate);
		}
		
		if ((flags & FLAG_SUSTAIN) == FLAG_SUSTAIN) {
			writer.option("sustain").floats(rateLoop);
			if (rateCurveCycles != 1) writer.ints(rateCurveCycles);
		}
		else if (rateLoop != 0.1f && rateCurveCycles == 1) {
			writer.option("single").floats(rateLoop);
		}
		else {
			writer.option("loop").floats(rateLoop);
			if (rateCurveCycles != 0) writer.ints(rateCurveCycles);
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
	}
	
	private void writeForce(ArgScriptWriter writer) {
		if (directionForcesSum[0] != 0 || directionForcesSum[1] != 0 || directionForcesSum[2] != 0 || windStrength != 0 || gravityStrength != 0
				|| drag != 0 || radialForce != 0) {
			
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
		}
	}
}
