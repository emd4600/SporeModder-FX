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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureIgnore;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

@Structure(StructureEndian.BIG_ENDIAN)
public class VisualEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<VisualEffect> STRUCTURE_METADATA = StructureMetadata.generate(VisualEffect.class);
	
	public static final String KEYWORD = "effect";
	public static final int TYPE_CODE = 0x0000;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAG_HARDSTOP = 0x20;
	public static final int FLAG_RIGID = 0x40;
	public static final int FLAG_EXTENDED_LOD_WEIGHTS = 0x80000;
	public static final int FLAGMASK = FLAG_EXTENDED_LOD_WEIGHTS;

	public int flags;
	public int componentAppFlagsMask;
	public int notifyMessageID;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN)
	public final float[] screenSizeRange = new float[2];
	public float cursorActiveDistance;
	public byte cursorButton;
	@StructureLength.Value(32) public final List<Float> lodDistances = new ArrayList<Float>();
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN)
	public final float[] extendedLodWeights = new float[3];
	public int seed;
	@StructureLength.Value(32) public final List<VisualEffectBlock> blocks = new ArrayList<VisualEffectBlock>();
	
	// Used for parsing the 'select' block
	@StructureIgnore private int currentSelectionGroup = 1;
	@StructureIgnore public boolean isParsingSelect = false;
	
	public VisualEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public String toString() {
		return KEYWORD + ' '  + getName();
	}
	
	@Override public void copy(EffectComponent _effect) {
		VisualEffect effect = (VisualEffect)_effect;
		
		flags = effect.flags;
		componentAppFlagsMask = effect.componentAppFlagsMask;
		notifyMessageID = effect.notifyMessageID;
		EffectDirectory.copyArray(screenSizeRange, effect.screenSizeRange);
		cursorActiveDistance = effect.cursorActiveDistance;
		cursorButton = effect.cursorButton;
		lodDistances.addAll(effect.lodDistances);
		EffectDirectory.copyArray(extendedLodWeights, effect.extendedLodWeights);
		
		for (VisualEffectBlock block : effect.blocks) {
			blocks.add(new VisualEffectBlock(this, block));
		}
	}
	
	public static class Parser extends EffectBlockParser<VisualEffect> {

		@Override
		protected VisualEffect createEffect(EffectDirectory effectDirectory) {
			return new VisualEffect(effectDirectory, FACTORY.getMaxVersion());
		}
		
		@Override
		public void parse(ArgScriptLine line) {
			super.parse(line);
			ArgScriptArguments args = new ArgScriptArguments();
			
			data.setCurrentEffect(effect);
			
			if (line.getOptionArguments(args, "flags", 1)) {
				effect.flags = Optional.ofNullable(stream.parseInt(args, 0) & ~FLAGMASK).orElse(0);
			}
			
			if (line.hasFlag("hardStop")) {
				effect.flags |= FLAG_HARDSTOP;
			}
			if (line.hasFlag("rigid")) {
				effect.flags |= FLAG_RIGID;
			}
			
			if (line.getOptionArguments(args, "notifyMessageID", 1)) {
				effect.notifyMessageID = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
			}
			
			if (line.getOptionArguments(args, "screenSizeRange", 2)) {
				effect.screenSizeRange[0] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				effect.screenSizeRange[1] = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
			}
			
			if (line.getOptionArguments(args, "toggleButtonActivate", 1)) {
				effect.cursorActiveDistance = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
			}
			
			if (line.getOptionArguments(args, "toggleButton", 1)) {
				effect.cursorButton = Optional.ofNullable(stream.parseByte(args, 0)).orElse((byte) 0);
			}
			
			if (line.getOptionArguments(args, "extendedLodWeights", effect.extendedLodWeights.length)) {
				stream.parseFloats(args, effect.extendedLodWeights);
				effect.flags |= FLAG_EXTENDED_LOD_WEIGHTS;
			}
		}

		@Override
		public void addParsers() {
			for (EffectComponentFactory factory : EffectDirectory.getFactories()) {
				if (factory != null) {
					factory.addGroupEffectParser(this);
				}
			}
			
			this.addParser("lodDistances", ArgScriptParser.create((parser, line) -> {
				final ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					stream.parseFloats(args, effect.lodDistances);
				}
			}));
			
			this.addParser("seed", ArgScriptParser.create((parser, line) -> {
				final ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1)) {
					effect.seed = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
			}));
			
			this.addParser("select", new ArgScriptBlock<EffectUnit>() {
				/** The index of the fist VisualEffectBlock that belongs to this select group. */
				private int firstIndex;
				
				@Override
				public void parse(ArgScriptLine line) {
					if (effect.isParsingSelect) {
						stream.addError(line.createError("You cannot use 'select' blocks inside another selection block."));
					}
					else {
						firstIndex = effect.blocks.size();
						effect.isParsingSelect = true;
						stream.startBlock(this);
					}
				}
				
				@Override
				public void onBlockEnd() {
					// We must automatically assign a probability for those effects that don't specify it
					
					// This is the total probability that must be distributed
					int probability = 65535;
					int automaticBlocks = effect.blocks.size() - firstIndex;
					
					// First, we will subtract the probability that has been manually assigned,
					// and then we will distribute the rest among the other blocks
					for (int i = firstIndex; i < effect.blocks.size(); i++) {
						VisualEffectBlock block = effect.blocks.get(i);
						
						if (block.selectionChance != 0) {
							probability -= block.selectionChance;
							automaticBlocks--;
						}
					}
					
					// If there are still blocks to assign
					if (automaticBlocks > 0) {
						int remaining = probability / automaticBlocks;
						
						for (int i = firstIndex; i < effect.blocks.size(); i++) {
							VisualEffectBlock block = effect.blocks.get(i);
							
							if (block.selectionChance == 0) {
								block.selectionChance = remaining;
							}
						}
					}
					
					// If the total probability is less than 65535, no effect is selected;
					// if it is more than 65535, sometimes more than one is selected!
					// So we need to normalize them to ensure they add up to 65535
					
					int totalAddUp = 0;
					for (int i = firstIndex; i < effect.blocks.size(); i++) {
						VisualEffectBlock block = effect.blocks.get(i);
						totalAddUp += block.selectionChance;
					}
					
					int difference = 65535 - totalAddUp;
					int distributedDifference = difference / (effect.blocks.size() - firstIndex);
					int finalRemainder = difference - distributedDifference * (effect.blocks.size() - firstIndex);
					for (int i = firstIndex; i < effect.blocks.size(); i++) {
						VisualEffectBlock block = effect.blocks.get(i);
						block.selectionChance += distributedDifference;
						if (i == firstIndex) {
							block.selectionChance += finalRemainder;
						}
					}
					
					// Tell the blocks which group to use
					for (int i = firstIndex; i < effect.blocks.size(); i++) {
						VisualEffectBlock block = effect.blocks.get(i);
						block.selectionGroup = effect.currentSelectionGroup;
					}
					effect.currentSelectionGroup++;
					
					effect.isParsingSelect = false;
					
					super.onBlockEnd();
				}
			});
		}
		
		@Override
		public void onBlockEnd() {
			effect.componentAppFlagsMask = 0;
			for (VisualEffectBlock block : effect.blocks) {
				effect.componentAppFlagsMask |= block.appFlagsMask;
			}
			
			data.setCurrentEffect(null);
			
			super.onBlockEnd();
		}
	}
	
	public static class Factory implements EffectComponentFactory {

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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, VisualEffect.class));
		}

		@Override
		public String getKeyword() {
			return KEYWORD;
		}

		@Override
		public boolean onlySupportsInline() {
			return false;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new VisualEffect(effectDirectory, version);
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
		
		int maskedFlags = flags & ~FLAGMASK;
		if (maskedFlags != 0) writer.option("flags").arguments("0x" + Integer.toHexString(maskedFlags));
		
		writer.flag("rigid", (flags & FLAG_RIGID) != 0);
		writer.flag("hardStop", (flags & FLAG_HARDSTOP) != 0);
		
		int appFlagsMask = 0;
		for (VisualEffectBlock block : blocks) appFlagsMask |= block.appFlagsMask;
		if (componentAppFlagsMask != appFlagsMask) writer.option("componentAppFlagsMask")
			.arguments("0x" + Integer.toHexString(componentAppFlagsMask));
		
		if (notifyMessageID != 0) writer.option("notifyMessageID")
			.arguments(HashManager.get().getFileName(notifyMessageID));
		
		if (screenSizeRange[0] != 0 || screenSizeRange[1] != 0) {
			writer.option("screenSizeRange").floats(screenSizeRange);
		}
		if (cursorActiveDistance != 0) writer.option("toggleButtonActivate").floats(cursorActiveDistance);
		if (cursorButton != 0) writer.option("toggleButton").ints(cursorButton);
		if ((flags & FLAG_EXTENDED_LOD_WEIGHTS) != 0 || 
				(extendedLodWeights[0] != 0.0f || extendedLodWeights[1] != 0.0f || extendedLodWeights[2] != 0.0f)) {
			writer.option("extendedLodWeights").floats(extendedLodWeights);
		}
		
		// Original .pfx files show that lodDistances and seed are commands
		if (!lodDistances.isEmpty()) writer.command("lodDistances").floats(lodDistances);
		
		if (seed != 0) writer.command("seed").ints(seed);
		
		// We do a first pass in which we only write non-random blocks and save the rest
		Map<Integer, List<VisualEffectBlock>> selectBlocks = new HashMap<>();
		for (VisualEffectBlock block : blocks) {
			if (block.selectionGroup == 0) block.toArgScript(writer);
			else {
				List<VisualEffectBlock> list = selectBlocks.get(block.selectionGroup);
				if (list == null) {
					list = new ArrayList<VisualEffectBlock>();
					selectBlocks.put(block.selectionGroup, list);
				}
				list.add(block);
			}
		}
		
		for (List<VisualEffectBlock> list : selectBlocks.values()) {
			writer.blankLine();
			writer.command("select").startBlock();
			for (VisualEffectBlock block : list) block.toArgScript(writer);
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		for (VisualEffectBlock block : blocks) {
			list.add(block.component);
		}
		return list;
	}
}
