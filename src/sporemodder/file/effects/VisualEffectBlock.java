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
import sporemodder.file.filestructures.StructureFieldMethod;
import sporemodder.file.filestructures.StructureIgnore;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.StructureUnsigned;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.Transform;

@Structure(StructureEndian.BIG_ENDIAN)
public class VisualEffectBlock {
	

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<VisualEffectBlock> STRUCTURE_METADATA = StructureMetadata.generate(VisualEffectBlock.class);
	
	@Structure(StructureEndian.BIG_ENDIAN)
	public static class LODScale {

		/**
		 * The structure metadata used for reading/writing this class.
		 */
		public static final StructureMetadata<LODScale> STRUCTURE_METADATA = StructureMetadata.generate(LODScale.class);
		
		public float emit;
		public float size;
		public float alpha;
	}
	
	public static final int FLAGS_IGNORE_LENGTH = 1;  // 1 << 0
	public static final int FLAGS_PARTICLE_SEQUENCE = 2;  // 1 << 1
	
	public static final int FLAGS_STATES = 8;  // 1 << 3
	
	public static final int FLAGS_IGNORE_PARAMS = 0x20;  // 1 << 5
	public static final int FLAGS_RIGID = 0x40;  // 1 << 6
	
	public static final int MASK_FLAGS = FLAGS_IGNORE_LENGTH | FLAGS_PARTICLE_SEQUENCE |
			FLAGS_STATES | FLAGS_IGNORE_PARAMS | FLAGS_RIGID;
	
	public static final List<String> APP_FLAGS = Arrays.asList("kAppFlagDeepWater", "kAppFlagShallowWater", "kAppFlagLowerAtmosphere",
			"kAppFlagUpperAtmosphere", "kAppFlagEnglish", "kAppFlagPlanetHasWater", "kAppFlagCinematics", "kAppFlagCellGameMode", "kAppFlagCreatureGameMode",
			"kAppFlagTribeGameMode", "kAppFlagGGEMode", "kAppFlagCivGameMode", "kAppFlagSpaceGameMode", "kAppFlagAtmoLow", "kAppFlagAtmoMed",
			"kAppFlagAtmoHigh", "kAppFlagEditorMode", "kAppFlagSpaceGameOutOfUFO", "kAppFlagSpaceGameGalaxyMode", "kAppFlagSpaceGameSolarMode",
			"kAppFlagSpaceGamePlanetMode", "kAppFlagIsNight", "kAppFlagIsRaining", "kAppFlagWeatherIce", "kAppFlagWeatherCold",
			"kAppFlagWeatherWarm", "kAppFlagWeatherHot", "kAppFlagWeatherLava");

	@StructureUnsigned(8) public int blockType; 
	public int flags;
	@StructureFieldMethod(read="readTransform", write="writeTransform")
	public final Transform transform = new Transform();
	@StructureUnsigned(8) public int lodBegin = 1;
	@StructureUnsigned(8) public int lodEnd = 255;
	@StructureLength.Value(32) public final List<LODScale> lodScales = new ArrayList<LODScale>();
	public float emitScaleBegin = 1.0f;
	public float emitScaleEnd = 1.0f;
	public float sizeScaleBegin = 1.0f;
	public float sizeScaleEnd = 1.0f;
	public float alphaScaleBegin = 1.0f;
	public float alphaScaleEnd = 1.0f;
	public int appFlags;
	public int appFlagsMask;
	@StructureUnsigned(16) public int selectionGroup;
	@StructureUnsigned(16) public int selectionChance;
	public float timeScale = 1.0f;
	@StructureFieldMethod(read="readComponent", write="writeComponent")
	public EffectComponent component;
	
	@StructureIgnore private final EffectDirectory effectDirectory; 
	
	public VisualEffectBlock(VisualEffect effect) {
		this(effect.effectDirectory);
	}
	
	public VisualEffectBlock(EffectDirectory effectDirectory) {
		this.effectDirectory = effectDirectory;
	}
	
	public VisualEffectBlock(VisualEffect effect, VisualEffectBlock other) {
		this(effect);
		copy(other);
	}
	
	public void copy(VisualEffectBlock other) {
		blockType = other.blockType;
		transform.copy(other.transform);
		lodBegin = other.lodBegin;
		lodEnd = other.lodEnd;
		lodScales.addAll(other.lodScales);
		emitScaleBegin = other.emitScaleBegin;
		emitScaleEnd = other.emitScaleEnd;
		alphaScaleBegin = other.alphaScaleBegin;
		alphaScaleEnd = other.alphaScaleEnd;
		sizeScaleBegin = other.sizeScaleBegin;
		sizeScaleEnd = other.sizeScaleEnd;
		appFlags = other.appFlags;
		appFlagsMask = other.appFlagsMask;
		selectionGroup = other.selectionGroup;
		selectionChance = other.selectionChance;
		timeScale = other.timeScale;
		component = other.component;
	}
	
	@SuppressWarnings("unused")
	private void readTransform(String fieldName, StreamReader in) throws IOException {
		int flags = in.readUShort();
		transform.setScale(in.readFloat());
		transform.getRotation().readLE(in);
		transform.getOffset().readLE(in);
		transform.setFlags(flags);
	}
	
	@SuppressWarnings("unused")
	private void writeTransform(String fieldName, StreamWriter out, Object value) throws IOException {
		out.writeUShort(transform.getFlags());
		out.writeFloat(transform.getScale());
		transform.getRotation().writeLE(out);
		transform.getOffset().writeLE(out);
	}
	
	@SuppressWarnings("unused")
	private void readComponent(String fieldName, StreamReader in) throws IOException {
		component = effectDirectory.getEffect(blockType, in.readInt());
	}
	
	@SuppressWarnings("unused")
	private void writeComponent(String fieldName, StreamWriter out, Object value) throws IOException {
		if (component == null) {
			out.writeInt(-1);
		}
		else {
			out.writeInt(effectDirectory.getIndex(blockType, component));
		}
	}
	
	public void parse(ArgScriptStream<EffectUnit> stream, ArgScriptLine line, Class<?> type) {
		parse(stream, line, type, true);
	}
	
	public void parse(ArgScriptStream<EffectUnit> stream, ArgScriptLine line, Class<?> type, boolean isAnonymous) {
		ArgScriptArguments args = new ArgScriptArguments();
		
		Integer iValue = null;
		Float fValue = null;
		
		// Some effects are anonymous and therefore, don't specify a name
		if (!isAnonymous && line.getArguments(args, 0, 1) && args.size() == 1) {
			component = stream.getData().getComponent(args.get(0), type, line.getKeyword());
			
			if (component == null) {
				stream.addError(line.createErrorForArgument(stream.getData().getLastError(), 0));
			} else {
				line.addHyperlinkForArgument(component.getFactory() == null ? ImportEffect.KEYWORD : component.getFactory().getKeyword(), component, 0);
			}
		}
		
		int numLods = 1;
		
		if (line.getOptionArguments(args, "lodRange", 2)) {
			if ((iValue = stream.parseInt(args, 0, 0, 255)) != null) lodBegin = iValue;
			if ((iValue = stream.parseInt(args, 1, 0, 255)) != null) lodEnd = iValue;
			
			numLods = lodEnd - lodBegin + 1;
			if (numLods <= 0) {
				stream.addError(line.createErrorForOption("lodRange", "LOD range must be in ascending order"));
			}
			
			if (stream.getData().getCurrentEffect().isParsingSelect) {
				stream.addError(line.createErrorForOption("lodRange", "Cannot specify LOD in a 'select'"));
			}
		}
		else if (line.getOptionArguments(args, "lod", 1) && 
				// the limit is 254 because we have to add 1
				(iValue = stream.parseInt(args, 0, 0, 254)) != null) {
			lodBegin = iValue;
			lodEnd = iValue + 1;
			numLods = 2;
			
			if (stream.getData().getCurrentEffect().isParsingSelect) {
				stream.addError(line.createErrorForOption("lodRange", "Cannot specify LOD in a 'select'"));
			}
		}
		else {
			lodBegin = 1;
			lodEnd = 255;
		}
		
		if (line.getOptionArguments(args, "emitScaleBegin", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) emitScaleBegin = fValue;
		
		if (line.getOptionArguments(args, "emitScaleEnd", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) emitScaleEnd = fValue;
		
		if (line.getOptionArguments(args, "alphaScaleBegin", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) alphaScaleBegin = fValue;
		
		if (line.getOptionArguments(args, "alphaScaleEnd", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) alphaScaleEnd = fValue;
		
		if (line.getOptionArguments(args, "sizeScaleBegin", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) sizeScaleBegin = fValue;
		
		if (line.getOptionArguments(args, "sizeScaleEnd", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) sizeScaleEnd = fValue;
		
		if (line.getOptionArguments(args, "timeScale", 1) && 
				(fValue = stream.parseFloat(args, 0)) != null) timeScale = fValue;
		
		
		List<Float> emitScales = new ArrayList<Float>();
		List<Float> alphaScales = new ArrayList<Float>();
		List<Float> sizeScales = new ArrayList<Float>();
		int count = 0;
		
		if (line.getOptionArguments(args, "emitScale", 1, Integer.MAX_VALUE)) {
			if (numLods < args.size() || (args.size() > 2 && args.size() < numLods)) {
				stream.addError(line.createErrorForOption("emitScale", "emitScale/LOD range mismatch"));
			}
			else {
				count = Math.max(args.size(), count);
				stream.parseFloats(args, emitScales);
			}
		}
		if (line.getOptionArguments(args, "sizeScale", 1, Integer.MAX_VALUE)) {
			if (numLods < args.size() || (args.size() > 2 && args.size() < numLods)) {
				stream.addError(line.createErrorForOption("sizeScale", "sizeScale/LOD range mismatch"));
			}
			else {
				count = Math.max(args.size(), count);
				stream.parseFloats(args, sizeScales);
			}
		}
		if (line.getOptionArguments(args, "alphaScale", 1, Integer.MAX_VALUE)) {
			if (numLods < args.size() || (args.size() > 2 && args.size() < numLods)) {
				stream.addError(line.createErrorForOption("alphaScale", "alphaScale/LOD range mismatch"));
			}
			else {
				count = Math.max(args.size(), count);
				stream.parseFloats(args, alphaScales);
			}
		}
		
		if (count != 0) {
			if (count != 1) {
				count = numLods;
			}
			
			lodScales.clear();
			
			int divider = count == 1 ? 1 : (count-1);
			
			int nEmitScales = emitScales.size();
			int nAlphaScales = alphaScales.size();
			int nSizeScales = sizeScales.size();
			
			for (int i = 0; i < count; i++) {
				
				LODScale scale = new LODScale();
				float value;
				
				if (nEmitScales == 0) {
					value = 1.0f;
				}
				else if (nEmitScales == 1) {
					value = emitScales.get(0);
				}
				else if (nEmitScales == 2) {
					value = emitScales.get(0) + (emitScales.get(0) - emitScales.get(1)) * i / divider;
				}
				else {
					value = emitScales.get(i);
				}
				scale.emit = value;
				
				if (nAlphaScales == 0) {
					value = 1.0f;
				}
				else if (nAlphaScales == 1) {
					value = alphaScales.get(0);
				}
				else if (nAlphaScales == 2) {
					value = alphaScales.get(0) + (alphaScales.get(0) - alphaScales.get(1)) * i / divider;
				}
				else {
					value = alphaScales.get(i);
				}
				scale.alpha = value;
				
				if (nSizeScales == 0) {
					value = 1.0f;
				}
				else if (nSizeScales == 1) {
					value = sizeScales.get(0);
				}
				else if (nSizeScales == 2) {
					value = sizeScales.get(0) + (sizeScales.get(0) - sizeScales.get(1)) * i / divider;
				}
				else {
					value = sizeScales.get(i);
				}
				scale.size = value;
				
				lodScales.add(scale);
			}
		}
		
		
		transform.parse(stream, line);
		
		// - FLAGS - //
		
		flags = 0;
		
		if (line.hasFlag("ignoreLength")) {
			flags |= FLAGS_IGNORE_LENGTH;
		}
		if (line.hasFlag("respectLength")) {
			flags &= ~FLAGS_IGNORE_LENGTH;
		}
		if (line.hasFlag("ignoreParams")) {
			flags |= FLAGS_IGNORE_PARAMS;
		}
		if (line.hasFlag("rigid")) {
			flags |= FLAGS_RIGID;
		}
		
		if (line.getOptionArguments(args, "flags", 1) && 
				(iValue = stream.parseInt(args, 0)) != null) {
			flags |= iValue.intValue() & ~MASK_FLAGS;
		}
		
		appFlagsMask = 0;
		appFlags = 0;
		
		while (line.getOptionArguments(args, "flag", 2)) {
			iValue = stream.parseInt(args, 0);
			Boolean value = stream.parseBoolean(args, 1);
			
			if (iValue != null && value != null) {
				int mask = 1 << iValue;
				appFlagsMask |= mask;
				
				if (value) appFlags |= mask;
			}
		}
		
		if (stream.getData().getCurrentEffect().isParsingSelect) {
			if (line.getOptionArguments(args, "prob", 1) && (fValue = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null) {
				selectionChance = Math.round(fValue.floatValue() * 65535);
			}
		}
		
		if (stream.getData().getCurrentEffect().isParsingStates) {
			flags |= FLAGS_STATES;
		}
		
		if (stream.getData().getCurrentEffect().isParsingParticleSequence) {
			flags |= FLAGS_PARTICLE_SEQUENCE;
		}
	}
	
	public static ArgScriptParser<EffectUnit> createGroupParser(int typeCode, Class<?> type) {
		
		return new ArgScriptParser<EffectUnit>() {
			@Override
			public void parse(ArgScriptLine line) {
				// Add it to the effect
				VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
				block.blockType = typeCode;
				block.parse(stream, line, type, false);
				
				data.getCurrentEffect().blocks.add(block);
			}
		};
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		if (component != null) {
			if (component.getFactory() == null) {
				// imports are used as effects
				writer.command(VisualEffect.KEYWORD).arguments(component.name);
			}
			else if (component.getFactory().onlySupportsInline()) {
				component.toArgScript(writer);
			} else {
				writer.command(component.getFactory().getKeyword()).arguments(component.getName());
			}
		} else {
			writer.command("UNSUPPORTED_COMPONENT");
		}
		
		transform.toArgScriptNoDefault(writer, false);
		
		if (lodBegin != 1 || lodEnd != 255) {
			writer.option("lodRange").ints(lodBegin, lodEnd);
		}
		
		if (!lodScales.isEmpty()) {
			LODScale first = lodScales.get(0);
			if (lodScales.size() != 1 || first.emit != 1.0f) {
				writer.option("emitScale");
				for (LODScale scale : lodScales) writer.floats(scale.emit);
			}
			if (lodScales.size() != 1 || first.size != 1.0f) {
				writer.option("sizeScale");
				for (LODScale scale : lodScales) writer.floats(scale.size);
			}
			if (lodScales.size() != 1 || first.alpha != 1.0f) {
				writer.option("alphaScale");
				for (LODScale scale : lodScales) writer.floats(scale.alpha);
			}
		}
		
		if (emitScaleBegin != 1.0f) writer.option("emitScaleBegin").floats(emitScaleBegin);
		if (emitScaleEnd != 1.0f) writer.option("emitScaleEnd").floats(emitScaleEnd);
		if (sizeScaleBegin != 1.0f) writer.option("sizeScaleBegin").floats(sizeScaleBegin);
		if (sizeScaleEnd != 1.0f) writer.option("sizeScaleEnd").floats(sizeScaleEnd);
		if (alphaScaleBegin != 1.0f) writer.option("alphaScaleBegin").floats(alphaScaleBegin);
		if (alphaScaleEnd != 1.0f) writer.option("alphaScaleEnd").floats(alphaScaleEnd);
		
		if (timeScale != 1.0f) writer.option("timeScale").floats(timeScale); 
		
		writer.flag("ignoreLength", (flags & FLAGS_IGNORE_LENGTH) != 0);
		writer.flag("ignoreParams", (flags & FLAGS_IGNORE_PARAMS) != 0);
		writer.flag("rigid", (flags & FLAGS_RIGID) != 0);
		
		// are there any remaining unknown flags?
		if ((flags & ~MASK_FLAGS) != 0) {
			writer.option("flags").arguments("0x" + Integer.toHexString(flags & ~MASK_FLAGS));
		}
		
		if (appFlagsMask != 0) {
			for (int i = 0; i < 32; i++) {
				if ((appFlagsMask & (1 << i)) != 0) {
					writer.option("flag").arguments(i >= APP_FLAGS.size() ? Integer.toString(i) : ('$' + APP_FLAGS.get(i)), 
							Boolean.toString((appFlags & (1 << i)) >> i == 1));
				}
			}
		}
		
		if (selectionGroup != 0) {
			writer.option("prob").floats(selectionChance / 65535.0f);
		}
	}
}
