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

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
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

@Structure(StructureEndian.BIG_ENDIAN)
public class RibbonEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<RibbonEffect> STRUCTURE_METADATA = StructureMetadata.generate(RibbonEffect.class);
	
	public static final String KEYWORD = "ribbon";
	public static final int TYPE_CODE = 0x000E;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_STATIC = 1;  // 1 << 0
	
	public static final int FLAGS_FACE = 8;  // 1 << 3
	public static final int FLAGS_SLIP_CURVE = 0x10;  // 1 << 4
	public static final int FLAGS_FACE_ROTATE90 = 0x20;  // 1 << 5
	public static final int FLAGS_FACE_ORIENT = 0x40;  // 1 << 6
	public static final int FLAGS_FACE_TWIST = 0x80;  // 1 << 7
	public static final int FLAGS_SUSTAIN = 0x100;  // 1 << 8
	public static final int FLAGS_FORCE = 0x200;  // 1 << 9
	public static final int FLAGS_MAP_ADVECT = 0x400;  // 1 << 10
	public static final int FLAGS_MAP_ADVECT2 = 0x800;  // 1 << 11
	
	public static final int FLAGS_IGNORE_RIGID = 0x2000;  // 1 << 13

	public static final int MASK_FLAGS = FLAGS_STATIC | FLAGS_FACE | FLAGS_SLIP_CURVE |
			FLAGS_FACE_ROTATE90 | FLAGS_FACE_ORIENT | FLAGS_FACE_TWIST | FLAGS_SUSTAIN |
			FLAGS_FORCE | FLAGS_MAP_ADVECT | FLAGS_MAP_ADVECT2 | FLAGS_MAP_ADVECT2;
	
	
	public int flags = FLAGS_FACE | FLAGS_FACE_TWIST;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] lifeTime = new float[2];
	@StructureLength.Value(32) public final List<Float> offset = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> width = new ArrayList<Float>();
	public float taper;
	public float fade;
	public float alphaDecay;
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<ColorRGB> lengthColor = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> lengthAlpha = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<ColorRGB> edgeColor = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> edgeAlpha = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<Float> startEdgeAlpha = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<Float> endEdgeAlpha = new ArrayList<Float>(Arrays.asList(1.0f));
	public int segmentCount;
	public float segmentLength;
	public final TextureSlot texture = new TextureSlot();
	public int tileUV = -1;  // 0xFFFFFFFF
	public float slipCurveSpeed = -999f;  //?
	public float slipUVSpeed = 0.0f;  // ?
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] directionForcesSum = new float[3];
	public float windStrength;
	public float gravityStrength;
	public final ResourceID mapEmitColor = new ResourceID();
	public final ResourceID mapForce = new ResourceID();  // also mapAdvect
	public float mapRepulseStrength;
	
	public RibbonEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		RibbonEffect effect = (RibbonEffect) _effect;
		
		flags = effect.flags;
		lifeTime[0] = effect.lifeTime[0];
		lifeTime[1] = effect.lifeTime[1];
		
		offset.addAll(effect.offset);
		width.addAll(effect.width);
		taper = effect.taper;
		fade = effect.fade;
		alphaDecay = effect.alphaDecay;
		
		color.addAll(effect.color);
		alpha.addAll(effect.alpha);
		lengthColor.addAll(effect.lengthColor);
		lengthAlpha.addAll(effect.lengthAlpha);
		edgeColor.addAll(effect.edgeColor);
		edgeAlpha.addAll(effect.edgeAlpha);
		startEdgeAlpha.addAll(effect.startEdgeAlpha);
		endEdgeAlpha.addAll(effect.endEdgeAlpha);
		
		segmentCount = effect.segmentCount;
		segmentLength = effect.segmentLength;
		
		texture.copy(effect.texture);
		
		tileUV = effect.tileUV;
		slipCurveSpeed = effect.slipCurveSpeed;
		slipUVSpeed = effect.slipUVSpeed;
		
		directionForcesSum[0] = effect.directionForcesSum[0];
		directionForcesSum[1] = effect.directionForcesSum[1];
		directionForcesSum[2] = effect.directionForcesSum[2];
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		
		mapEmitColor.copy(effect.mapEmitColor);
		mapForce.copy(effect.mapForce);
		mapRepulseStrength = effect.mapRepulseStrength;
	}

	protected static class Parser extends EffectBlockParser<RibbonEffect> {
		@Override
		protected RibbonEffect createEffect(EffectDirectory effectDirectory) {
			return new RibbonEffect(effectDirectory, FACTORY.getMaxVersion());
		}
		
		@Override
		protected void additionalLineParsing(ArgScriptLine line) {
			if (line.hasFlag("ignoreRigid")) {
				effect.flags |= FLAGS_IGNORE_RIGID;
			}
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
			
			this.addParser("width", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.width.clear();
					stream.parseFloats(args, effect.width);
				}
			}));
			
			this.addParser("offset", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.offset.clear();
					stream.parseFloats(args, effect.offset);
				}
			}));
			
			this.addParser("taper", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.taper = value.floatValue();
				}
			}));
			
			this.addParser("fade", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.fade = value.floatValue();
				}
			}));
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, 2)) {
					if (args.size() >= 1 && (value = stream.parseFloat(args, 0)) != null) {
						effect.lifeTime[0] = effect.lifeTime[1] = value.floatValue();
					}
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						effect.lifeTime[0] -= value.floatValue();
						effect.lifeTime[1] += value.floatValue();
					}
					
					if (line.hasFlag("sustain")) {
						effect.flags |= FLAGS_SUSTAIN;
					}
				}
			}));
			
			this.addParser("sustain", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					boolean value = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
					if (value) effect.flags |= FLAGS_SUSTAIN;
					else effect.flags &= ~FLAGS_SUSTAIN;
				}
			}));
			
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.lengthColor.clear();
					stream.parseColorRGBs(args, effect.lengthColor);
				}
			}), "lengthColor", "lengthColour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.lengthColor.clear();
					stream.parseColorRGB255s(args, effect.lengthColor);
				}
			}), "lengthColor255", "lengthColour255");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.edgeColor.clear();
					stream.parseColorRGBs(args, effect.edgeColor);
				}
			}), "edgeColor", "edgeColour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.edgeColor.clear();
					stream.parseColorRGB255s(args, effect.edgeColor);
				}
			}), "edgeColor255", "edgeColour255");
			
			this.addParser("lengthAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.lengthAlpha.clear();
					stream.parseFloats(args, effect.lengthAlpha);
				}
			}));
			
			this.addParser("lengthAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.lengthAlpha.clear();
					stream.parseFloat255s(args, effect.lengthAlpha);
				}
			}));
			
			this.addParser("edgeAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.edgeAlpha.clear();
					stream.parseFloats(args, effect.edgeAlpha);
				}
			}));
			
			this.addParser("edgeAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.edgeAlpha.clear();
					stream.parseFloat255s(args, effect.edgeAlpha);
				}
			}));
			
			this.addParser("alphaDecay", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaDecay = value.floatValue();
				}
			}));
			
			this.addParser("startAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.startEdgeAlpha.clear();
					stream.parseFloats(args, effect.startEdgeAlpha);
				}
			}));
			
			this.addParser("startAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.startEdgeAlpha.clear();
					stream.parseFloat255s(args, effect.startEdgeAlpha);
				}
			}));
			
			this.addParser("endAlpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.endEdgeAlpha.clear();
					stream.parseFloats(args, effect.endEdgeAlpha);
				}
			}));
			
			this.addParser("endAlpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.endEdgeAlpha.clear();
					stream.parseFloat255s(args, effect.endEdgeAlpha);
				}
			}));
			
			this.addParser("face", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					int value = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					if (value != 0) {
						effect.flags |= FLAGS_FACE;
						if (line.hasFlag("noTwist")) {
							effect.flags &= ~FLAGS_FACE_TWIST;
						}
					}
					else {
						effect.flags &= ~FLAGS_FACE;
						if (line.hasFlag("rotate90")) {
							effect.flags |= FLAGS_FACE_ROTATE90;
						}
						if (line.hasFlag("orient")) {
							effect.flags |= FLAGS_FACE_ORIENT;
						}
					}
				}
			}));
			
			this.addParser("slipCurve", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					boolean value = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
					if (value) effect.flags |= FLAGS_SLIP_CURVE;
					else effect.flags &= ~FLAGS_SLIP_CURVE;
					
					if (line.getOptionArguments(args, "speed", 1)) {
						effect.slipCurveSpeed = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}
			}));
			
			this.addParser("animUV", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.slipUVSpeed = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
				}
			}));
			
			this.addParser("slipUV", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					boolean value = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
					if (value) {
						if (line.getOptionArguments(args, "speed", 1)) {
							effect.slipUVSpeed = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
						}
						else {
							effect.slipUVSpeed = 1.0f;
						}
					}
					else {
						effect.slipUVSpeed = 0f;
					}
				}
			}));
			
			this.addParser("tileUV", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.tileUV = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
			}));
			
			this.addParser("segments", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.segmentCount = value.intValue();
				}
			}));
			
			this.addParser("segmentLength", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.segmentLength = value.floatValue();
				}
			}));
			
			this.addParser("static", ArgScriptParser.create((parser, line) -> {
				effect.flags |= FLAGS_STATIC;
				if (line.getArguments(args, 0, 1) && args.size() > 0) {
					boolean value = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
					if (value) {
						effect.flags |= FLAGS_STATIC;
					}
					else {
						effect.flags &= ~FLAGS_STATIC;
					}
				}
			}));	
			
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
				// particles also use 'bomb' (radialForce), 'drag' and 'attractors', but ribbons don't
				
				effect.flags |= FLAGS_FORCE;
			}));
			
			
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				effect.texture.drawMode = TextureSlot.DRAWMODE_NONE;
				effect.texture.parse(stream, line, EffectDirectory.HYPERLINK_MATERIAL);
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				effect.texture.drawMode = 0;
				effect.texture.parse(stream, line, EffectDirectory.HYPERLINK_TEXTURE);
				
//				if (line.getOptionArguments(args, "repeat", 1)) {
//					effect.repeat
//				}
			}));
			
			this.addParser("rigid", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					boolean value = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
					if (!value) {
						effect.flags |= FLAGS_IGNORE_RIGID;
					}
					else {
						effect.flags &= ~FLAGS_IGNORE_RIGID;
					}
				}
			}));
			
			this.addParser("mapEmitColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.mapEmitColor.parseSpecial(args, 0, words);
					line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
				}
			}));
			
			this.addParser("mapAdvect", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					if (args.get(0).equals("terrain")) {
						effect.mapForce.setGroupID(0);
						effect.mapForce.setInstanceID(0);
					}
					else if (args.get(0).equals("water")) {
						effect.mapForce.setGroupID(1);
						effect.mapForce.setInstanceID(0);
					}
					else {
						String[] words = new String[2];
						effect.mapForce.parse(args, 0, words);
						line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MAP, words, 0);
					}
					
					effect.flags |= FLAGS_MAP_ADVECT;
					effect.flags |= FLAGS_MAP_ADVECT2;
				}
				
				Number value = null;
				if (line.getOptionArguments(args, "strength", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.mapRepulseStrength = value.floatValue();
				} else {
					effect.mapRepulseStrength = 1.0f;
				}
			}));
			
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseUInt(args, 0)) != null) {
					effect.flags |= value.intValue() & ~MASK_FLAGS;
				}
			}));
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return RibbonEffect.class;
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
		public boolean onlySupportsInline() {
			return false;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new RibbonEffect(effectDirectory, version);
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
		writer.command(KEYWORD).arguments(name);
		writer.flag("ignoreRigid", (flags & FLAGS_IGNORE_RIGID) != 0);
		writer.startBlock();
		
		if ((flags & ~MASK_FLAGS) != 0) writer.command("flags").arguments(HashManager.get().hexToString(flags & ~MASK_FLAGS));
		
		if (!writer.isDefaultColor(color)) writer.command("color").colors(color);
		if (!writer.isDefault(alpha)) writer.command("alpha").floats(alpha);
		if (!writer.isDefault(width)) writer.command("width").floats(width);
		if (!writer.isDefault(offset)) writer.command("offset").floats(offset);
		if (taper != 0) writer.command("taper").floats(taper);
		if (fade != 0) writer.command("fade").floats(fade);
		
		writer.command("life");
		if (lifeTime[0] != lifeTime[1]) {
			float midPoint = (lifeTime[0] + lifeTime[1]) / 2.0f;
			writer.floats(midPoint, lifeTime[1] - midPoint);
		}
		else {
			writer.floats(lifeTime[0]);
		}
		writer.flag("sustain", (flags & FLAGS_SUSTAIN) != 0);
		
		if ((flags & FLAGS_STATIC) != 0) writer.command("static").arguments(true);
		
		if (!writer.isDefaultColor(lengthColor)) writer.command("lengthColor").colors(lengthColor);
		if (!writer.isDefaultColor(edgeColor)) writer.command("edgeColor").colors(edgeColor);
		if (!writer.isDefault(lengthAlpha)) writer.command("lengthAlpha").floats(lengthAlpha);
		if (!writer.isDefault(edgeAlpha)) writer.command("edgeAlpha").floats(edgeAlpha);
		if (alphaDecay != 0) writer.command("alphaDecay").floats(alphaDecay);
		if (!writer.isDefault(startEdgeAlpha)) writer.command("startAlpha").floats(startEdgeAlpha);
		if (!writer.isDefault(endEdgeAlpha)) writer.command("endAlpha").floats(endEdgeAlpha);
		
		if (segmentCount != 0) writer.command("segments").ints(segmentCount);
		if (segmentLength != 0) writer.command("segmentLength").floats(segmentLength);
		
		if ((flags & FLAGS_FACE_TWIST) == 0) {
			writer.command("face").ints(1).option("noTwist");
		}
		else if ((flags & FLAGS_FACE) == 0 &&
				((flags & FLAGS_FACE_ROTATE90) != 0 || (flags & FLAGS_FACE_ORIENT) != 0)) {
			writer.command("face").ints(0);
			writer.flag("rotate90", (flags & FLAGS_FACE_ROTATE90) != 0);
			writer.flag("orient", (flags & FLAGS_FACE_ORIENT) != 0);
		}
	
		writer.command("slipCurve").arguments((flags & FLAGS_SLIP_CURVE) != 0);
		if (slipCurveSpeed != -999.0f) writer.option("speed").floats(slipCurveSpeed);
		if (tileUV != -1) writer.command("tileUV").ints(tileUV);
		
		if (!texture.isDefault()) {
			texture.toArgScript(texture.drawMode == TextureSlot.DRAWMODE_NONE ? "material" : "texture", writer);
		}
		
		if ((flags & FLAGS_FORCE) != 0) {
			
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
		}
		
		if (!mapEmitColor.isDefault()) writer.command("mapEmitColor").arguments(mapEmitColor);
		
		if ((flags & FLAGS_MAP_ADVECT) != 0) {
			writer.command("mapAdvect");
			if (mapForce.isZero()) writer.arguments("terrain");
			else if (mapForce.getInstanceID() == 0 && mapForce.getGroupID() == 1) writer.arguments("water");
			else writer.arguments(mapForce);
			
			if (mapRepulseStrength != 1.0f) writer.option("strength").floats(mapRepulseStrength);
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapEmitColor));
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, mapForce));
		
		if (texture.drawMode == TextureSlot.DRAWMODE_NONE) {
			list.add(effectDirectory.getResource(MaterialResource.TYPE_CODE, texture.resource));
		}
		
		return list;
	}
}
