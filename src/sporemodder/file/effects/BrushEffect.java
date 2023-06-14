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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class BrushEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<BrushEffect> STRUCTURE_METADATA = StructureMetadata.generate(BrushEffect.class);
	
	public static final String KEYWORD = "spBrush";
	public static final int TYPE_CODE = 0x0020;
	
	public static final EffectComponentFactory FACTORY = new Factory();

	public static final ArgScriptEnum ENUM_DRAWMODE = new ArgScriptEnum();
	static {
		ENUM_DRAWMODE.add(0x00, "add");
		ENUM_DRAWMODE.add(0x01, "biased");
		ENUM_DRAWMODE.add(0x02, "subtract");
		ENUM_DRAWMODE.add(0x03, "level");
		ENUM_DRAWMODE.add(0x04, "levelRange");
		ENUM_DRAWMODE.add(0x05, "absoluteMax");
		ENUM_DRAWMODE.add(0x06, "absoluteMin");
	}
	
	public static final ArgScriptEnum ENUM_FILTER_TYPE = new ArgScriptEnum();
	static {
		ENUM_FILTER_TYPE.add(0x02, "global");
		ENUM_FILTER_TYPE.add(0x01, "local");
	}
	
	public static final ArgScriptEnum ENUM_FILTER = new ArgScriptEnum();
	static {
		ENUM_FILTER.add(0x03, "smooth");
		ENUM_FILTER.add(0x03, "median");
		ENUM_FILTER.add(0x03, "lowpass");
		ENUM_FILTER.add(0x03, "lowpass1");
		ENUM_FILTER.add(0x03, "lowpass2");
		ENUM_FILTER.add(0x03, "lowpass3");
		ENUM_FILTER.add(0x03, "lowpass4");
		ENUM_FILTER.add(0x03, "lowpass5");
		
		ENUM_FILTER.add(0x08, "highpass");
		ENUM_FILTER.add(0x08, "highpass1");
		ENUM_FILTER.add(0x08, "highpass2");
		ENUM_FILTER.add(0x08, "highpass3");
		ENUM_FILTER.add(0x08, "highpass4");
		
		ENUM_FILTER.add(0x0C, "edge");
		ENUM_FILTER.add(0x0C, "edge1");
		ENUM_FILTER.add(0x0C, "edge2");
		ENUM_FILTER.add(0x0C, "edge3");
		
		ENUM_FILTER.add(0x03, "add");
		ENUM_FILTER.add(0x03, "level");
		ENUM_FILTER.add(0x03, "subtract");
		ENUM_FILTER.add(0x03, "fractal");
		ENUM_FILTER.add(0x12, "winderosion");
		ENUM_FILTER.add(0x03, "watererosion");
	}
	
	public static final ArgScriptEnum ENUM_FILTER_MODE = new ArgScriptEnum();
	static {
		ENUM_FILTER_MODE.add(0x01, "replace");
		ENUM_FILTER_MODE.add(0x02, "add");
		ENUM_FILTER_MODE.add(0x03, "average");
	}
	
	public static final ArgScriptEnum ENUM_COND = new ArgScriptEnum();
	static {
		ENUM_COND.add(0x01, "less");
		ENUM_COND.add(0x02, "greater");
		ENUM_COND.add(0x03, "between");
	}
	
	
	public final int[] field_8 = new int[3];
	public final ResourceID textureID = new ResourceID();
	public byte drawMode;
	public boolean texGaussian;
	public float texGaussianValue;
	public float life = 5.0f;
	@StructureLength.Value(32) public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<Float> intensity = new ArrayList<Float>(Arrays.asList(1.0f));
	public boolean waterLevelIsZero;
	public float intensityVary;
	public float sizeVary;
	public int field_60;
	public final float[] texOffset = new float[2];
	public float drawLevel;
	public float texVary;
	public float field_74;
	public float field_78;
	public float rate = 1.0f;
	public boolean useFilter;
	public boolean sizeGlobal;
	public byte filter;
	public byte filterMode;
	public float filterValue;
	@StructureLength.Value(32) public final List<Float> spacing = new ArrayList<Float>();
	public float spacingVary;
	public byte cond;
	public float cond_0;
	public float cond_1;
	public byte falloff;
	public float falloff_0;
	public float falloff_1;
	public byte gradientCond;
	public float gradientCond_0;
	public float gradientCond_1;
	public byte gradientCondFalloff;
	public float gradientCondFalloff_0;
	public float gradientCondFalloff_1;
	public boolean texCube;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] rotate = new float[3];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] rotateVary = new float[3];
	public boolean useRibbon;
	public int ribbonNumTiles = 1;
	public int ribbonNumSteps = 1;
	public int ribbonNumTexStrips = 1;
	public int ribbonNumSkip = 20;
	public boolean ribbonCap = true;
	
	public BrushEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		BrushEffect effect = (BrushEffect) _effect;
		field_8[0] = effect.field_8[0];
		field_8[1] = effect.field_8[1];
		field_8[2] = effect.field_8[2];
		textureID.copy(effect.textureID);
		drawMode = effect.drawMode;
		texGaussian = effect.texGaussian;
		texGaussianValue = effect.texGaussianValue;
		life = effect.life;
		
		size.addAll(effect.size);
		intensity.addAll(effect.intensity);
		waterLevelIsZero = effect.waterLevelIsZero;
		intensityVary = effect.intensityVary;
		sizeVary = effect.sizeVary;
		field_60 = effect.field_60;
		
		texOffset[0] = effect.texOffset[0];
		texOffset[1] = effect.texOffset[1];
		drawLevel = effect.drawLevel;
		texVary = effect.texVary;
		field_74 = effect.field_74;
		field_78 = effect.field_78;
		rate = effect.rate;
		
		useFilter = effect.useFilter;
		sizeGlobal = effect.sizeGlobal;
		filter = effect.filter;
		filterMode = effect.filterMode;
		filterValue = effect.filterValue;
		
		spacing.addAll(effect.spacing);
		spacingVary = effect.spacingVary;
		
		cond = effect.cond;
		cond_0 = effect.cond_0;
		cond_1 = effect.cond_1;
		
		falloff = effect.falloff;
		falloff_0 = effect.falloff_0;
		falloff_1 = effect.falloff_1;
		
		gradientCond = effect.gradientCond;
		gradientCond_0 = effect.gradientCond_0;
		gradientCond_1 = effect.gradientCond_1;
		
		gradientCondFalloff = effect.gradientCondFalloff;
		gradientCondFalloff_0 = effect.gradientCondFalloff_0;
		gradientCondFalloff_1 = effect.gradientCondFalloff_1;
		
		texCube = effect.texCube;
		rotate[0] = effect.rotate[0];
		rotate[1] = effect.rotate[1];
		rotate[2] = effect.rotate[2];
		rotateVary[0] = effect.rotateVary[0];
		rotateVary[1] = effect.rotateVary[1];
		rotateVary[2] = effect.rotateVary[2];
		
		useRibbon = effect.useRibbon;
		ribbonNumTiles = effect.ribbonNumTiles;
		ribbonNumSteps = effect.ribbonNumSteps;
		ribbonNumTexStrips = effect.ribbonNumTexStrips;
		ribbonNumSkip = effect.ribbonNumSkip;
		ribbonCap = effect.ribbonCap;
	}

	
	protected static class Parser extends EffectBlockParser<BrushEffect> {
		@Override
		protected BrushEffect createEffect(EffectDirectory effectDirectory) {
			return new BrushEffect(effectDirectory, FACTORY.getMaxVersion());
		}
		
		@Override
		public void parse(ArgScriptLine line) {
			super.parse(line);
			effect.waterLevelIsZero = line.hasFlag("waterLevelIsZero");
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser("ribbon", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				effect.useRibbon = true;
				
				if (line.hasFlag("cap")) effect.ribbonCap = true;
				
				if (line.getOptionArguments(args, "numTiles", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					effect.ribbonNumTiles = value.intValue();
				}
				
				if (line.getOptionArguments(args, "numSteps", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					effect.ribbonNumSteps = value.intValue();
				}
				
				if (line.getOptionArguments(args, "numTextureStrips", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					effect.ribbonNumTexStrips = value.intValue();
				}
				
				if (line.getOptionArguments(args, "numSkip", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					effect.ribbonNumSkip = value.intValue();
				}
			}));
			
			this.addParser("intensity", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.intensity.clear();
					stream.parseFloats(args, effect.intensity);
				}
				
				if (line.getOptionArguments(args, "vary", 1)) {
					effect.intensityVary = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
				
				if (line.getOptionArguments(args, "vary", 1)) {
					effect.sizeVary = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				}
				
				effect.sizeGlobal = line.hasFlag("global");
			}));
			
			this.addParser("spacing", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.spacing.clear();
					stream.parseFloats(args, effect.spacing);
				}
				
				if (line.getOptionArguments(args, "vary", 1)) {
					effect.spacingVary = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				}
			}));
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.life = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				}
			}));
			
			this.addParser("rate", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.rate = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				}
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getArguments(args, 1)) {
					String[] originals = new String[2];
					effect.textureID.parse(args, 0, originals);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_TEXTURE, originals, 0);
				}
				
				if (line.getOptionArguments(args, "draw", 1, 3)) {
					effect.drawMode = (byte) ENUM_DRAWMODE.get(args, 0);
					if (effect.drawMode == -1) return;
					
					if (effect.drawMode == 3) {
						if (args.size() != 2) {
							stream.addError(line.createErrorForOption("draw", "Missing argument for 'level' draw mode."));
							return;
						}
						if ((value = stream.parseFloat(args, 1)) != null) {
							effect.drawLevel = Math.min(Math.max(value.floatValue(), -2), 1);
						}
					}
					else {
						if (args.size() != 3) {
							stream.addError(line.createErrorForOption("draw", "Expected 2 arguments for this draw mode."));
							return;
						}
						if ((value = stream.parseFloat(args, 1)) != null) {
							effect.field_74 = Math.min(Math.max(value.floatValue(), -1), 1);
						}
						if ((value = stream.parseFloat(args, 2)) != null) {
							effect.field_78 = Math.min(Math.max(value.floatValue(), -1), 1);
						}
					}
				}
				
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.texVary = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "offset", 1)) {
					stream.parseVector2(args, 0, effect.texOffset);
				}
				
				if (line.getOptionArguments(args, "gaussian", 0, 1)) {
					effect.texGaussian = true;
					
					if (args.size() == 1) {
						effect.texGaussianValue = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
					}
				}
				
				if (line.hasFlag("cube")) {
					effect.texCube = true;
				}
			}));
			
			this.addParser("filter", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, 2)) {
					effect.useFilter = true;
					
					effect.filter = (byte) ENUM_FILTER.get(args, 0);
					if (effect.filter == -1) return;
					
					if (args.size() == 2) {
						if (effect.filter == 0xF || effect.filter == 0x10 || effect.filter == 0x14) {
							effect.filterValue = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
						}
						else {
							effect.filterMode = (byte) ENUM_FILTER_MODE.get(args, 1);
						}
					}
				}
			}));
			
			this.addParser("cond", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 2, 3)) {
					effect.cond = (byte) ENUM_COND.get(args, 0);
					if (effect.cond == -1) return;
					effect.cond_0 = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
					
					if (effect.cond == 3) {
						if (args.size() != 3) {
							stream.addError(line.createError("Missing argument for cond 'between'."));
							return;
						}
						effect.cond_1 = Optional.ofNullable(stream.parseFloat(args, 2)).orElse(0.0f);
					}
				}
				
				if (line.getOptionArguments(args, "falloff", 2)) {
					effect.falloff = 1;
					effect.falloff_0 = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
					effect.falloff_1 = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
				}
			}));
			
			this.addParser("gradientCond", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 2, 3)) {
					effect.gradientCond = (byte) ENUM_COND.get(args, 0);
					if (effect.gradientCond == -1) return;
					effect.gradientCond_0 = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
					
					if (effect.gradientCond == 3) {
						if (args.size() != 3) {
							stream.addError(line.createError("Missing argument for gradientCond 'between'."));
							return;
						}
						effect.gradientCond_1 = Optional.ofNullable(stream.parseFloat(args, 2)).orElse(0.0f);
					}
				}
				
				if (line.getOptionArguments(args, "falloff", 2)) {
					effect.gradientCondFalloff = 1;
					effect.gradientCondFalloff_0 = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
					effect.gradientCondFalloff_1 = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
				}
			}));
			
			this.addParser("rotate", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 3)) {
					stream.parseFloats(args, effect.rotate);
				}
				
				if (line.getOptionArguments(args, "vary", 3)) {
					stream.parseFloats(args, effect.rotateVary);
				}
			}));
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return BrushEffect.class;
		}
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public int getMinVersion() {
			return 2;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, BrushEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new BrushEffect(effectDirectory, version);
		}

		@Override
		public boolean onlySupportsInline() {
			return false;
		}
		
		@Override public String getKeyword() {
			return KEYWORD;
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}
	
	@Override public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(name).flag("waterLevelIsZero", waterLevelIsZero).startBlock();
		
		if (useRibbon) {
			writer.command("ribbon");
			if (ribbonCap) writer.option("cap");
			if (ribbonNumTiles != 1) writer.option("numTiles").ints(ribbonNumTiles);
			if (ribbonNumSteps != 1) writer.option("numSteps").ints(ribbonNumSteps);
			if (ribbonNumTexStrips != 1) writer.option("numTextureStrips").ints(ribbonNumTexStrips);
			if (ribbonNumSkip != 1) writer.option("numSkip").ints(ribbonNumSkip);
		}
		
		if (!intensity.isEmpty()) {
			writer.command("intensity").floats(intensity);
			if (intensityVary != 0.0f) writer.option("vary").floats(intensityVary);
		}
		if (!size.isEmpty()) {
			writer.command("size").floats(size);
			if (sizeVary != 0.0f) writer.option("vary").floats(sizeVary);
			if (sizeGlobal) writer.option("global");
		}
		if (!spacing.isEmpty()) {
			writer.command("spacing").floats(spacing);
			if (spacingVary != 0.0f) writer.option("vary").floats(spacingVary);
		}
		
		if (life != 5.0f) writer.command("life").floats(life);
		if (rate != 1.0f) writer.command("rate").floats(rate);
		
		if (!textureID.isDefault()) {
			writer.command("texture").arguments(textureID).option("draw").arguments(ENUM_DRAWMODE.get(drawMode));
			if (drawMode == 3) writer.floats(drawLevel);
			else writer.floats(field_74, field_78);
			
			if (texVary != 0.0f) writer.option("vary").floats(texVary);
			if (texOffset[0] != 0.0f || texOffset[1] != 0.0f) writer.option("offset").vector(texOffset);
			if (texGaussian) {
				writer.option("gaussian");
				if (texGaussianValue != 1.0f) writer.floats(texGaussianValue);
			}
			writer.flag("cube", texCube);
		}
		
		if (useFilter) {
			writer.command("filter").arguments(ENUM_FILTER.get(filter));
			if (filter == 0xF || filter == 0x10 || filter == 0x14) writer.floats(filterValue);
			else if (filterMode != 0) writer.arguments(ENUM_FILTER_MODE.get(filterMode));
		}
		
		if (cond != 0) {
			writer.command("cond").arguments(ENUM_COND.get(cond)).floats(cond_0);
			if (cond == 3) writer.floats(cond_1);
			
			//TODO lots of calculations here
			if (falloff == 1) writer.option("falloff").floats(falloff_0, falloff_1);
		}
		
		if (gradientCond != 0) {
			writer.command("gradientCond").arguments(ENUM_COND.get(gradientCond)).floats(gradientCond_0);
			if (gradientCond == 3) writer.floats(gradientCond_1);
			
			//TODO lots of calculations here
			if (gradientCondFalloff == 1) writer.option("falloff").floats(gradientCondFalloff_0, gradientCondFalloff_1);
		}
		
		if (rotate[0] != 0 || rotate[1] != 0 || rotate[2] != 0 ||
				rotateVary[0] != 0 || rotateVary[1] != 0 || rotateVary[2] != 0) {
			
			writer.command("rotate").floats(rotate);
			if (rotateVary[0] != 0 || rotateVary[1] != 0 || rotateVary[2] != 0) {
				writer.option("vary").floats(rotateVary);
			}
		}
		
		writer.endBlock().commandEND();
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		BrushEffect.STRUCTURE_METADATA.read(this, stream);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		BrushEffect.STRUCTURE_METADATA.write(this, stream);
	}
}
