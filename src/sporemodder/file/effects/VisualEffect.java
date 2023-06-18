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
import sporemodder.file.filestructures.StructureIgnore;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public class VisualEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<VisualEffect> STRUCTURE_METADATA = StructureMetadata.generate(VisualEffect.class);
	
	public static final String KEYWORD = "effect";
	public static final int TYPE_CODE = 0x0000;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_VIEW_RELATIVE = 1;  // 1 << 0
	public static final int FLAGS_CAMERA_FACING = 2;  // 1 << 1
	public static final int FLAGS_CAMERA_ATTACHED = 0x4;  // 1 << 2
	public static final int FLAGS_CAMERA_ATTACHED_RIGID = 0x8;  // 1 << 3
	public static final int FLAGS_AUTO_STOP = 0x10;  // 1 << 4 TODO
	public static final int FLAGS_HARD_STOP = 0x20;  // 1 << 5
	public static final int FLAGS_RIGID = 0x40;  // 1 << 6
	public static final int FLAGS_IGNORE_PARAMS = 0x80;  // 1 << 7
	public static final int FLAGS_APPLY_CURSOR = 0x100;  // 1 << 8
	public static final int FLAGS_IGNORE_SCALE = 0x200;  // 1 << 9
	public static final int FLAGS_IGNORE_ORIENTATION = 0x400;  // 1 << 10
	public static final int FLAGS_ORIENT_Z_POLE = 0x800;  // 1 << 11
	public static final int FLAGS_GAME_TIME = 0x1000;  // 1 << 12
	public static final int FLAGS_REAL_TIME = 0x2000;  // 1 << 13
	public static final int FLAGS_DETACH = 0x4000;  // 1 << 14
	public static final int FLAGS_CLAMP_SCREEN_SIZE = 0x8000;  // 1 << 15
	public static final int FLAGS_NO_SMOOTH_CLAMP = 0x10000;  // 1 << 16
	public static final int FLAGS_RADIO_BUTTON_ACTIVATE = 0x20000;  // 1 << 17
	public static final int FLAGS_TOGGLE_BUTTON_ACTIVATE = 0x40000;  // 1 << 18
	public static final int FLAGS_EXTENDED_LOD_DISTANCES = 0x80000;  // 1 << 19
	public static final int FLAGS_HARD_LOD_TRANSITION = 0x100000;  // 1 << 20
	public static final int FLAGS_SOFT_LOD_TRANSITION = 0x200000;  // 1 << 21
	
	public static final int MASK_FLAGS = FLAGS_VIEW_RELATIVE | FLAGS_CAMERA_FACING |
			FLAGS_CAMERA_ATTACHED | FLAGS_CAMERA_ATTACHED_RIGID | 
			FLAGS_HARD_STOP | FLAGS_RIGID | FLAGS_IGNORE_PARAMS | FLAGS_APPLY_CURSOR |
			FLAGS_IGNORE_SCALE | FLAGS_IGNORE_ORIENTATION | FLAGS_ORIENT_Z_POLE |
			FLAGS_GAME_TIME | FLAGS_REAL_TIME | FLAGS_DETACH | FLAGS_CLAMP_SCREEN_SIZE |
			FLAGS_NO_SMOOTH_CLAMP | FLAGS_RADIO_BUTTON_ACTIVATE | FLAGS_TOGGLE_BUTTON_ACTIVATE |
			FLAGS_EXTENDED_LOD_DISTANCES | FLAGS_HARD_LOD_TRANSITION | FLAGS_SOFT_LOD_TRANSITION;

	public int flags;
	public int componentAppFlagsMask;
	public int notifyMessageID;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN)
	public final float[] screenSizeRange = new float[2];
	public float cursorActiveDistance;
	public byte cursorButton;
	// 'focus', 'facing', and 'distance'
	@StructureLength.Value(32) public final List<Float> lodDistances = new ArrayList<Float>();
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN)
	public final float[] extendedLodWeights = new float[3];
	public int seed;
	@StructureLength.Value(32) public final List<VisualEffectBlock> blocks = new ArrayList<VisualEffectBlock>();
	
	// Used for parsing the 'select' block
	@StructureIgnore private int currentSelectionGroup = 1;
	@StructureIgnore public boolean isParsingSelect = false;
	@StructureIgnore public boolean isParsingParticleSequence = false;
	@StructureIgnore public boolean isParsingStates = false;
	
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
			
			if (line.hasFlag("viewRelative")) {
				effect.flags |= FLAGS_VIEW_RELATIVE;
			}
			if (line.hasFlag("cameraFacing")) {
				effect.flags |= FLAGS_VIEW_RELATIVE;
				effect.flags |= FLAGS_CAMERA_FACING;
				effect.flags |= FLAGS_IGNORE_ORIENTATION;
			}
			if (line.hasFlag("orientZPole")) {
				effect.flags |= FLAGS_ORIENT_Z_POLE;
			}
			if (line.hasFlag("cameraAttached")) {
				effect.flags |= FLAGS_CAMERA_ATTACHED;
			}
			if (line.hasFlag("cameraAttachedRigid")) {
				effect.flags |= FLAGS_CAMERA_ATTACHED_RIGID;
			}
			if (line.hasFlag("hardStop") || line.hasFlag("hardStopLegacy")) {
				effect.flags |= FLAGS_HARD_STOP;
			}
			if (line.hasFlag("hardLODTransition")) {
				effect.flags |= FLAGS_HARD_LOD_TRANSITION;
			}
			if (line.hasFlag("softLODTransition")) {
				effect.flags |= FLAGS_SOFT_LOD_TRANSITION;
			}
			if (line.hasFlag("ignoreParams")) {
				effect.flags |= FLAGS_IGNORE_PARAMS;
			}
			if (line.hasFlag("rigid")) {
				effect.flags |= FLAGS_RIGID;
			}
			if (line.hasFlag("ignoreScale")) {
				effect.flags |= FLAGS_IGNORE_SCALE;
			}
			if (line.hasFlag("ignoreOrientation")) {
				effect.flags |= FLAGS_IGNORE_ORIENTATION;
			}
			if (line.hasFlag("detach")) {
				effect.flags |= FLAGS_DETACH;
			}
			
			if (line.getOptionArguments(args, "notify", 1)) {
				effect.notifyMessageID = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
			}
			
			if (line.hasFlag("gameTime")) {
				effect.flags |= FLAGS_GAME_TIME;
			}
			if (line.hasFlag("realTime")) {
				effect.flags |= FLAGS_REAL_TIME;
			}
			
			if (line.getOptionArguments(args, "clampScreenSize", 2)) {
				effect.screenSizeRange[0] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				effect.screenSizeRange[1] = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
				effect.flags |= FLAGS_CLAMP_SCREEN_SIZE;
				
				if (line.hasFlag("noSmoothClamp")) {
					effect.flags |= FLAGS_NO_SMOOTH_CLAMP;
				}
			}
			
			if (line.hasFlag("applyCursor")) {
				effect.flags |= FLAGS_APPLY_CURSOR;
			}
			
			if (line.getOptionArguments(args, "cursorActivate", 1)) {
				effect.cursorActiveDistance = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				effect.flags |= FLAGS_APPLY_CURSOR;
			}
			
			if (line.getOptionArguments(args, "radioButtonActivate", 1)) {
				effect.cursorActiveDistance = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				effect.flags |= FLAGS_APPLY_CURSOR;
				effect.flags |= FLAGS_RADIO_BUTTON_ACTIVATE;
			}
			
			if (line.getOptionArguments(args, "toggleButtonActivate", 1)) {
				effect.cursorActiveDistance = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				effect.flags |= FLAGS_APPLY_CURSOR;
				effect.flags |= FLAGS_TOGGLE_BUTTON_ACTIVATE;
			}
			
			if (line.getOptionArguments(args, "flags", 1)) {
				effect.flags = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0) & ~MASK_FLAGS;
			}
		}

		@Override
		public void addParsers() {
			for (EffectComponentFactory factory : EffectDirectory.getFactories()) {
				if (factory != null) {
					factory.addGroupEffectParser(this);
				}
			}
			
			this.addParser("seed", ArgScriptParser.create((parser, line) -> {
				final ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1)) {
					effect.seed = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
			}));
			
			this.addParser("lodDistances", ArgScriptParser.create((parser, line) -> {
				final ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					stream.parseFloats(args, effect.lodDistances);
					
					for (int i = 1; i < args.size(); i++) {
						if (effect.lodDistances.get(i-1) >= effect.lodDistances.get(i)) {
							stream.addError(line.createErrorForArgument("Distances must be in ascending order", i));
						}
					}
				}
				if (line.getOptionArguments(args, "focus", 1)) {
					effect.extendedLodWeights[0] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					effect.flags |= FLAGS_EXTENDED_LOD_DISTANCES;
				}
				if (line.getOptionArguments(args, "facing", 1)) {
					effect.extendedLodWeights[1] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					effect.flags |= FLAGS_EXTENDED_LOD_DISTANCES;
				}
				if (line.getOptionArguments(args, "distance", 1)) {
					effect.extendedLodWeights[2] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					effect.flags |= FLAGS_EXTENDED_LOD_DISTANCES;
				}
			}));
			
			this.addParser("particleSequence", new ArgScriptBlock<EffectUnit>() {
				@Override
				public void parse(ArgScriptLine line) {
					effect.isParsingParticleSequence = true;
					stream.startBlock(this);
				}
				
				@Override
				public void onBlockEnd() {
					effect.isParsingParticleSequence = false;
					super.onBlockEnd();
				}
			});
			
			this.addParser("states", new ArgScriptBlock<EffectUnit>() {
				@Override
				public void parse(ArgScriptLine line) {
					effect.isParsingStates = true;
					stream.startBlock(this);
				}
				
				@Override
				public void onBlockEnd() {
					effect.isParsingStates = false;
					super.onBlockEnd();
				}
			});
			
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
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return VisualEffect.class;
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
		
		if ((flags & FLAGS_VIEW_RELATIVE) != 0 && 
				(flags & FLAGS_CAMERA_FACING) != 0 &&
				(flags & FLAGS_IGNORE_ORIENTATION) != 0) 
		{
			writer.option("cameraFacing");
		}
		else {
			writer.flag("viewRelative", (flags & FLAGS_VIEW_RELATIVE) != 0);
			writer.flag("ignoreOrientation", (flags & FLAGS_IGNORE_ORIENTATION) != 0);
		}
		writer.flag("orientZPole", (flags & FLAGS_ORIENT_Z_POLE) != 0);
		writer.flag("cameraAttached", (flags & FLAGS_CAMERA_ATTACHED) != 0);
		writer.flag("cameraAttachedRigid", (flags & FLAGS_CAMERA_ATTACHED_RIGID) != 0);
		writer.flag("hardStop", (flags & FLAGS_HARD_STOP) != 0);
		writer.flag("hardLODTransition", (flags & FLAGS_HARD_LOD_TRANSITION) != 0);
		writer.flag("softLODTransition", (flags & FLAGS_SOFT_LOD_TRANSITION) != 0);
		writer.flag("ignoreParams", (flags & FLAGS_IGNORE_PARAMS) != 0);
		writer.flag("rigid", (flags & FLAGS_RIGID) != 0);
		writer.flag("ignoreScale", (flags & FLAGS_IGNORE_SCALE) != 0);
		writer.flag("detach", (flags & FLAGS_DETACH) != 0);
		
		if (notifyMessageID != 0) 
			writer.option("notify").arguments(HashManager.get().getFileName(notifyMessageID));
		
		writer.flag("gameTime", (flags & FLAGS_GAME_TIME) != 0);
		writer.flag("realTime", (flags & FLAGS_REAL_TIME) != 0);
		
		if ((flags & FLAGS_CLAMP_SCREEN_SIZE) != 0) {
			writer.option("clampScreenSize").floats(screenSizeRange);
			writer.flag("noSmoothClamp", (flags & FLAGS_NO_SMOOTH_CLAMP) != 0);
		}
		
		if ((flags & FLAGS_RADIO_BUTTON_ACTIVATE) != 0) {
			writer.option("radioButtonActivate").floats(cursorActiveDistance);
		}
		if ((flags & FLAGS_TOGGLE_BUTTON_ACTIVATE) != 0) {
			writer.option("toggleButtonActivate").floats(cursorActiveDistance);
		}
		if ((flags & FLAGS_TOGGLE_BUTTON_ACTIVATE) != 0) {
			if (cursorActiveDistance != 0.0f) {
				writer.option("cursorActivate").floats(cursorActiveDistance);
			}
			else {
				writer.option("applyCursor");
			}
		}
		
		int maskedFlags = flags & ~MASK_FLAGS;
		if (maskedFlags != 0) writer.option("flags").arguments("0x" + Integer.toHexString(maskedFlags));
		
		// Original .pfx files show that lodDistances and seed are commands
		if (!lodDistances.isEmpty()) {
			writer.command("lodDistances").floats(lodDistances);
			
			if (extendedLodWeights[0] != 0.0f) writer.option("focus").floats(extendedLodWeights[0]);
			if (extendedLodWeights[1] != 0.0f) writer.option("facing").floats(extendedLodWeights[1]);
			if (extendedLodWeights[2] != 0.0f) writer.option("distance").floats(extendedLodWeights[2]);
		}
		
		if (seed != 0) writer.command("seed").ints(seed);
		
		int lastSelectionGroup = 0;
		boolean isInStates = false;
		boolean isInParticleSequence = false;
		for (VisualEffectBlock block : blocks) {
			boolean hasStates = (block.flags & VisualEffectBlock.FLAGS_STATES) != 0;
			boolean hasParticleSequence = (block.flags & VisualEffectBlock.FLAGS_PARTICLE_SEQUENCE) != 0;
			
			if (!isInParticleSequence && hasParticleSequence) {
				writer.command("particleSequence").startBlock();
			}
			
			if (!isInStates && hasStates) {
				writer.command("states").startBlock();
			}
			
			if (block.selectionGroup != 0) {
				if (lastSelectionGroup == 0) {
					// Start a new select block
					writer.command("select").startBlock();
				}
				else if (lastSelectionGroup != block.selectionGroup) {
					// Finish current select block and start a new one
					writer.endBlock().commandEND();
					writer.command("select").startBlock();
					
				}
				lastSelectionGroup = block.selectionGroup;
			}
			else if (block.selectionGroup == 0) {
				if (lastSelectionGroup != 0) {
					// Finish current select block 
					writer.endBlock().commandEND();
					lastSelectionGroup = 0;
					
				}
			}
			
			if (isInParticleSequence && !hasParticleSequence) {
				writer.endBlock().commandEND();
			}
			isInParticleSequence = hasParticleSequence;
			
			if (isInStates && !hasStates) {
				writer.endBlock().commandEND();
			}
			isInStates = hasStates;
			
			block.toArgScript(writer);
		}
		
		if (lastSelectionGroup != 0) {
			// Finish current select block 
			writer.endBlock().commandEND();
		}
		if (isInParticleSequence) {
			writer.endBlock().commandEND();
		}
		if (isInStates) {
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
