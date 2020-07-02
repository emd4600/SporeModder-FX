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
package sporemodder.file.cnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.cnv.CnvAnimation.CnvAnimationVariation;
import sporemodder.file.cnv.CnvDialog.CnvDialogResponse;

public class CnvUnit {
	
	public final List<CnvAnimation> animations = new ArrayList<>();
	public final List<CnvDialog> dialogs = new ArrayList<>();
	public final List<CnvEventLog> eventLogs = new ArrayList<>();
	public final List<CnvUnknown> unknowns = new ArrayList<>();

	
	protected static void addArgScriptName(ArgScriptWriter writer, int id, String name) {
		writer.arguments(name);
		if (HashManager.get().getFileHash(name) != id) {
			writer.arguments(HashManager.get().getFileName(id));
		}
	}
	
	protected static boolean addArgScriptFlagCommand(ArgScriptWriter writer, String name, boolean[] flags) {
		boolean added = false;
		for (int i = 0; i < flags.length; ++i) {
			if (flags[i]) {
				if (!added) {
					writer.command(name);
					added = true;
				}
				writer.arguments(i);
			}
		}
		return added;
	}
	
	protected static boolean addArgScriptFlagOption(ArgScriptWriter writer, String name, boolean[] flags) {
		boolean added = false;
		for (int i = 0; i < flags.length; ++i) {
			if (flags[i]) {
				if (!added) {
					writer.option(name);
					added = true;
				}
				writer.arguments(i);
			}
		}
		return added;
	}

	public void read(StreamReader stream) throws IOException {
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvAnimation anim = new CnvAnimation();
			anim.read(stream);
			animations.add(anim);
		}
		
		count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvDialog dialog = new CnvDialog();
			dialog.read(stream);
			dialogs.add(dialog);
		}
		
		count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvEventLog log = new CnvEventLog();
			log.read(stream);
			eventLogs.add(log);
		}
		
		count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvUnknown unk = new CnvUnknown();
			unk.read(stream);
			unknowns.add(unk);
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(animations.size());
		for (CnvAnimation anim : animations) anim.write(stream);
		
		stream.writeLEInt(dialogs.size());
		for (CnvDialog dialog : dialogs) dialog.write(stream);
		
		stream.writeLEInt(eventLogs.size());
		for (CnvEventLog log : eventLogs) log.write(stream);
		
		stream.writeLEInt(unknowns.size());
		for (CnvUnknown unk : unknowns) unk.write(stream);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (CnvAnimation anim : animations) anim.toArgScript(writer);
		if (!animations.isEmpty()) writer.blankLine();

		for (CnvDialog dialog : dialogs) dialog.toArgScript(writer);
		if (!dialogs.isEmpty()) writer.blankLine();
		
		for (CnvEventLog log : eventLogs) log.toArgScript(writer);
		if (!eventLogs.isEmpty()) writer.blankLine();
		
		for (CnvUnknown unk : unknowns) unk.toArgScript(writer);
		if (!unknowns.isEmpty()) writer.blankLine();
	}
	
	public String toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer.toString();
	}
	
	public void clear() {
		animations.clear();
		dialogs.clear();
		eventLogs.clear();
		unknowns.clear();
	}
	
	public ArgScriptStream<CnvUnit> generateStream() {
		ArgScriptStream<CnvUnit> stream = new ArgScriptStream<CnvUnit>();
		stream.setData(this);
		stream.addDefaultParsers();
		stream.setVersionRange(3, 4);
		
		stream.setOnStartAction((asStream, data) -> {
			data.clear();
		});
		
		stream.addParser("animation", new ArgScriptBlock<CnvUnit>() {
			CnvAnimation anim;
			
			@Override public void parse(ArgScriptLine line) {
				anim = new CnvAnimation();
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				if (line.getArguments(args, 1, 2)) {
					anim.name = args.get(0);
					if (args.size() == 2) {
						anim.id = stream.parseFileID(args, 1);
					} else {
						anim.id = HashManager.get().getFileHash(anim.name);
					}
				}
				
				stream.startBlock(this);
			}
			
			@Override public void onBlockEnd() {
				data.animations.add(anim);
			}
			
			@Override public void setData(ArgScriptStream<CnvUnit> stream, CnvUnit data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				this.addParser("variation", new ArgScriptBlock<CnvUnit>() {
					CnvAnimationVariation var;
					
					@Override public void parse(ArgScriptLine line) {
						var = new CnvAnimationVariation();
						
						if (line.getArguments(args, 1, 2)) {
							var.name = args.get(0);
							if (args.size() == 2) {
								var.id = stream.parseFileID(args, 1);
							} else {
								var.id = HashManager.get().getFileHash(anim.name);
							}
						}
						
						stream.startBlock(this);
					}
					
					@Override public void onBlockEnd() {
						anim.variations.add(var);
					}
					
					@Override public void setData(ArgScriptStream<CnvUnit> stream, CnvUnit data) {
						super.setData(stream, data);
						
						this.addParser("idle", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 1, Integer.MAX_VALUE))
								stream.parseFileIDs(args, var.idleAnimIDs);
						}));
						this.addParser("action", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 1, Integer.MAX_VALUE))
								stream.parseFileIDs(args, var.actionAnimIDs);
						}));
					}
				});
			}
		});
		
		stream.addParser("dialog", new ArgScriptBlock<CnvUnit>() {
			CnvDialog dialog;
			
			@Override public void parse(ArgScriptLine line) {
				dialog = new CnvDialog();
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				if (line.getArguments(args, 1, 2)) {
					dialog.name = args.get(0);
					if (args.size() == 2) {
						dialog.id = stream.parseFileID(args, 1);
					} else {
						dialog.id = HashManager.get().getFileHash(dialog.name);
					}
				}
				
				stream.startBlock(this);
			}
			
			@Override public void onBlockEnd() {
				data.dialogs.add(dialog);
			}
			
			@Override public void setData(ArgScriptStream<CnvUnit> stream, CnvUnit data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				this.addParser("value1", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						dialog.value1 = value.booleanValue();
					}
				}));
				this.addParser("value2", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						dialog.value2 = value.booleanValue();
					}
				}));
				this.addParser("value3", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						dialog.value3 = value.booleanValue();
					}
				}));
				this.addParser("value4", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						dialog.value4 = value.booleanValue();
					}
				}));
				this.addParser("value5", ArgScriptParser.create((parser, line) -> {
					Boolean value = null;
					if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
						dialog.value5 = value.booleanValue();
					}
				}));
				this.addParser("action", ArgScriptParser.create((parser, line) -> {
					Integer value = null;
					if (line.getArguments(args, 1, 2)) {
						if ((value = stream.parseFileID(args, 0)) != null)
							dialog.action = value.intValue();
				
						if (args.size() == 2) {
							String[] originals = new String[3];
							dialog.key.parse(args, 1, originals);
						}
					}
				}));
				this.addParser("text", ArgScriptParser.create((parser, line) -> {
					CnvText text = new CnvText();
					text.parse(stream, line);
					dialog.texts.add(text);
				}));
				
				this.addParser("response", new ArgScriptBlock<CnvUnit>() {
					CnvDialogResponse response;
					
					@Override public void parse(ArgScriptLine line) {
						response = new CnvDialogResponse();
						stream.startBlock(this);
					}
					
					@Override public void onBlockEnd() {
						dialog.responses.add(response);
					}
					
					@Override public void setData(ArgScriptStream<CnvUnit> stream, CnvUnit data) {
						super.setData(stream, data);
						
						this.addParser("action", ArgScriptParser.create((parser, line) -> {
							Integer value = null;
							if (line.getArguments(args, 1, 2)) {
								if ((value = stream.parseFileID(args, 0)) != null)
									response.action = value.intValue();
						
								if (args.size() == 2) {
									String[] originals = new String[3];
									response.key.parse(args, 1, originals);
								}
							}
						}));
						
						this.addParser("text", ArgScriptParser.create((parser, line) -> {
							CnvText text = new CnvText();
							text.parse(stream, line);
							response.texts.add(text);
						}));
						
						this.addParser("flags1", ArgScriptParser.create((parser, line) -> {
							final List<Integer> flags = new ArrayList<>();
							if (line.getArguments(args, 1, Integer.MAX_VALUE) && parser.getStream().parseInts(args, flags))
								for (int i : flags) response.flags1[i] = true; 
						}));
						this.addParser("flags2", ArgScriptParser.create((parser, line) -> {
							final List<Integer> flags = new ArrayList<>();
							if (line.getArguments(args, 1, Integer.MAX_VALUE) && parser.getStream().parseInts(args, flags))
								for (int i : flags) response.flags2[i] = true; 
						}));
						this.addParser("flags3", ArgScriptParser.create((parser, line) -> {
							final List<Integer> flags = new ArrayList<>();
							if (line.getArguments(args, 1, Integer.MAX_VALUE) && parser.getStream().parseInts(args, flags))
								for (int i : flags) response.flags3[i] = true; 
						}));
						this.addParser("flags4", ArgScriptParser.create((parser, line) -> {
							final List<Integer> flags = new ArrayList<>();
							if (line.getArguments(args, 1, Integer.MAX_VALUE) && parser.getStream().parseInts(args, flags))
								for (int i : flags) response.flags4[i] = true; 
						}));
						this.addParser("ints1", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 1, Integer.MAX_VALUE)) parser.getStream().parseFileIDs(args, response.ints1);
						}));
						this.addParser("ints2", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 1, Integer.MAX_VALUE)) parser.getStream().parseFileIDs(args, response.ints1);
						}));
						this.addParser("ints3", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 1, Integer.MAX_VALUE)) parser.getStream().parseFileIDs(args, response.ints1);
						}));
						this.addParser("ints4", ArgScriptParser.create((parser, line) -> {
							if (line.getArguments(args, 1, Integer.MAX_VALUE)) parser.getStream().parseFileIDs(args, response.ints1);
						}));
					}
				});
			}
		});
		
		stream.addParser("eventLog", new ArgScriptBlock<CnvUnit>() {
			CnvEventLog log;
			
			@Override public void parse(ArgScriptLine line) {
				log = new CnvEventLog();
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				if (line.getArguments(args, 1, 2)) {
					log.name = args.get(0);
					if (args.size() == 2) {
						log.id = stream.parseFileID(args, 1);
					} else {
						log.id = HashManager.get().getFileHash(log.name);
					}
				}
				
				stream.startBlock(this);
			}
			
			@Override public void onBlockEnd() {
				data.eventLogs.add(log);
			}
			
			@Override public void setData(ArgScriptStream<CnvUnit> stream, CnvUnit data) {
				super.setData(stream, data);
				
				this.addParser("text", ArgScriptParser.create((parser, line) -> {
					CnvText text = new CnvText();
					text.parse(stream, line);
					log.texts.add(text);
				}));
			}
		});
		
		stream.addParser("unknown", new ArgScriptBlock<CnvUnit>() {
			CnvUnknown unk;
			
			@Override public void parse(ArgScriptLine line) {
				unk = new CnvUnknown();
				stream.startBlock(this);
			}
			
			@Override public void onBlockEnd() {
				data.unknowns.add(unk);
			}
			
			@Override public void setData(ArgScriptStream<CnvUnit> stream, CnvUnit data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				this.addParser("missionID", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
						unk.missionID = value.intValue();
					}
				}));
				this.addParser("value2", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
						unk.value2 = value.intValue();
					}
				}));
				this.addParser("value3", ArgScriptParser.create((parser, line) -> {
					Number value = null;
					if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
						unk.value3 = value.intValue();
					}
				}));
				this.addParser("badges1", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
						stream.parseFileIDs(args, unk.badges1);
					}
				}));
				this.addParser("ints2", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
						stream.parseFileIDs(args, unk.ints2);
					}
				}));
			}
		});
		
		return stream;
	}
}
