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
package sporemodder.file.tlsa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class TLSAUnit {
	
	private static final int MAGIC = 0x74736C61;
	
	private final List<TLSAAnimationGroup> groups = new ArrayList<TLSAAnimationGroup>();
	private int version = 10;

	public List<TLSAAnimationGroup> getGroups() {
		return groups;
	}

	public void read(StreamReader stream) throws IOException {
		if (stream.readInt() != MAGIC) {
			throw new IOException("Input file is not a TLSA file! Position: " + stream.getFilePointer());
		}
		
		version = stream.readInt();
		int count = stream.readInt();
		
		for (int i = 0; i < count; ++i) {
			TLSAAnimationGroup group = new TLSAAnimationGroup();
			groups.add(group);
			
			group.read(stream, version);
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(MAGIC);
		stream.writeInt(version);
		stream.writeInt(groups.size());
		for (TLSAAnimationGroup group : groups) group.write(stream, version);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("version").ints(version);
		
		for (TLSAAnimationGroup group : groups) {
			writer.blankLine();
			group.toArgScript(writer, version);
		}
	}
	
	public String toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer.toString();
	}

	public ArgScriptStream<TLSAUnit> generateStream() {
		
		ArgScriptStream<TLSAUnit> stream = new ArgScriptStream<TLSAUnit>();
		stream.setVersionRange(7, 10);
		stream.setData(this);
		stream.addDefaultParsers();
		
		// Spore uses "anim" for the old version and "anim2" for the new
		// But since here we only support the new, we use anim for it
		stream.addParser(TLSAAnimationGroup.KEYWORD, new ArgScriptBlock<TLSAUnit>() {
			private TLSAAnimationGroup group;

			@Override
			public void parse(ArgScriptLine line) {
				group = new TLSAAnimationGroup();
				
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				if (line.getArguments(args, 2) && (value = stream.parseFileID(args, 0)) != null) {
					group.id = value.intValue();
					group.name = args.get(1);
				}
				
				stream.startBlock(this);
			}
			
			@Override
			public void onBlockEnd() {
				float specifiedProb = 0.0f;
				int specifieds = 0;
				for (TLSAAnimationChoice choice : group.choices) {
					specifiedProb += choice.probabilityThreshold;
					++specifieds;
				}
				
				// Should be 1.0, but it's not exact
				if (specifiedProb > 1.1f) {
					stream.addError(new DocumentError("Total probability > 1.0", 0, 3));
					return;
				}
				
				float remainingProb = (1.0f - specifiedProb) / (float)specifieds;
				float lastProb = 0.0f;
				
				for (TLSAAnimationChoice choice : group.choices) {
					if (choice.probabilityThreshold == 0.0f) {
						choice.probabilityThreshold = remainingProb;
					}
					
					choice.probabilityThreshold += lastProb;
					lastProb = choice.probabilityThreshold;
				}
				
				if (group.endMode == 4) {
					stream.addError(new DocumentError("No endMode specified", 0, 3));
					return;
				}

				data.groups.add(group);
			}
			
			@Override
			public void setData(ArgScriptStream<TLSAUnit> stream, TLSAUnit data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				this.addParser("randomizeChoicePerLoop", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						group.randomizeChoicePerLoop = value;
					}
				}));
				
				this.addParser("endMode", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
						group.endMode = value.intValue();  // offset 0x34
					}
				}));
				
				this.addParser("idle", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						group.idle = value;
					}
				}));
				
				this.addParser("blendInTime", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
						group.blendInTime = value.floatValue();
					}
				}));
				
				this.addParser("allowLocomotion", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						group.allowLocomotion = value;
					}
				}));
				
				this.addParser("disableToolOverlay", ArgScriptParser.create((parser, line) -> {
					List<Integer> values = new ArrayList<Integer>();
					if (line.getArguments(args, 1, Integer.MAX_VALUE) && stream.parseInts(args, values)) {
						
						for (int i = 0; i < values.size(); i++) {
							if (values.get(i) < 0) {
								stream.addWarning(line.createErrorForArgument("Cannot have a negative index.", i));
							}
							else if (values.get(i) > 31) {
								stream.addWarning(line.createErrorForArgument("Cannot have an index greater than 31.", i));
							}
							else {
								group.disableToolOverlayMask |= 1 << i;
							}
						}
					}
				}));
				
				this.addParser("matchVariantForTool", ArgScriptParser.create((parser, line) -> {
					List<Integer> values = new ArrayList<Integer>();
					if (line.getArguments(args, 1, Integer.MAX_VALUE) && stream.parseInts(args, values)) {
						
						for (int i = 0; i < values.size(); i++) {
							if (values.get(i) < 0) {
								stream.addWarning(line.createErrorForArgument("Cannot have a negative index.", i));
							}
							else if (values.get(i) > 31) {
								stream.addWarning(line.createErrorForArgument("Cannot have an index greater than 31.", i));
							}
							else {
								group.matchVariantForToolMask |= 1 << i;
							}
						}
					}
				}));
				
				this.addParser("priorityOverride", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
						group.priorityOverride = value.floatValue();
					}
				}));
				
				this.addParser("choice", new ArgScriptBlock<TLSAUnit>() {
					private TLSAAnimationChoice choice;

					@Override
					public void parse(ArgScriptLine line) {
						choice = new TLSAAnimationChoice();
						choice.probabilityThreshold = 0.0f;
						group.choices.add(choice);

						Number value = null;
						if (line.getOptionArguments(args, "probability", 1) && (value = stream.parseFloat(args, 0)) != null) {
							choice.probabilityThreshold = value.floatValue();
						}
						
						stream.startBlock(this);
						
						if (stream.getData().version <= 8 && group.choices.size() > 1) {
							stream.addError(line.createError("Only versions > 8 can use more than one animation choice."));
						}
					}
					
					@Override
					public void setData(ArgScriptStream<TLSAUnit> stream, TLSAUnit data) {
						super.setData(stream, data);
						
						final ArgScriptArguments args = new ArgScriptArguments();
						

						this.addParser("animation", ArgScriptParser.create((parser, line) -> {
							TLSAAnimation anim = new TLSAAnimation();
							choice.animations.add(anim);
							
							Number value = null;
							if (line.getArguments(args, 1)) {
								anim.description = args.get(0);
							}
							
							if (line.getOptionArguments(args, "instanceID", 1)) {
								if ((value = stream.parseUInt(args, 0)) != null) {
									anim.id = value.intValue();
								}
							}
							else {
								anim.id = HashManager.get().getFileHash(TLSAAnimationGroup.instanceFromDescription(anim.description));
							}
							
							if (line.getOptionArguments(args, "durationScale", 1) && (value = stream.parseFloat(args, 0)) != null) {
								anim.durationScale = value.floatValue();
							}
							
							if (line.getOptionArguments(args, "duration", 1) && (value = stream.parseFloat(args, 0)) != null) {
								anim.duration = value.floatValue();
							}
						}));
					}
				});
			}
		});
		
		return stream;
	}
}
