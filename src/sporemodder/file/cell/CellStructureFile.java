package sporemodder.file.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.ColorRGB;

public class CellStructureFile {
	public static final int TYPE_MODEL = 0;
	public static final int TYPE_EFFECT = 1;
	public static final int TYPE_STRUCTURE = 2;
	public static final int TYPE_CREATURE = 3;
	public static final int TYPE_RANDOM_CREATURE = 4;
	public static final int TYPE_PLAYER_CREATURE = 5;
	public static final int TYPE_DEBUG = 6;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_MODEL, "model");
		ENUM_TYPE.add(TYPE_EFFECT, "effect");
		ENUM_TYPE.add(TYPE_STRUCTURE, "structure");
		ENUM_TYPE.add(TYPE_CREATURE, "creature");
		ENUM_TYPE.add(TYPE_RANDOM_CREATURE, "random_creature");
		ENUM_TYPE.add(TYPE_PLAYER_CREATURE, "player_creature");
		ENUM_TYPE.add(TYPE_DEBUG, "debug");
	}
	
	public static class cSPAttachment
	{
		public int bone;
		public int type;  // ENUM_TYPE
		public int structureID;
		public int randomCreatureID;
		public int effectID;  // also model ID and creature ID
		public int levelMin;
		public int levelMax;
		public final ColorRGB color = new ColorRGB();
	}
	
	public int onDeath;
	public int onDeathSmall;
	public int onDeathLarge;
	public int onHatch;
	public int onStartHatch;
	public final List<cSPAttachment> entries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		onDeath = stream.readLEInt();
		onDeathSmall = stream.readLEInt();
		onDeathLarge = stream.readLEInt();
		onHatch = stream.readLEInt();
		onStartHatch = stream.readLEInt();
		
		int offset = stream.readLEInt();
		int count = stream.readLEInt();
		if (offset != 28)
			throw new IOException("Error: offset was not 28, file pointer " + stream.getFilePointer());
		
		for (int i = 0; i < count; i++) {
			cSPAttachment entry = new cSPAttachment();
			entry.bone = stream.readLEInt();
			entry.type = stream.readLEInt();
			entry.structureID = stream.readLEInt();
			entry.randomCreatureID = stream.readLEInt();
			entry.effectID = stream.readLEInt();
			entry.levelMin = stream.readLEInt();
			entry.levelMax = stream.readLEInt();
			entry.color.readLE(stream);
			entries.add(entry);
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(onDeath);
		stream.writeLEInt(onDeathSmall);
		stream.writeLEInt(onDeathLarge);
		stream.writeLEInt(onHatch);
		stream.writeLEInt(onStartHatch);
		stream.writeLEInt(28);
		stream.writeLEInt(entries.size());
		for (cSPAttachment entry : entries) {
			stream.writeLEInt(entry.bone);
			stream.writeLEInt(entry.type);
			stream.writeLEInt(entry.structureID);
			stream.writeLEInt(entry.randomCreatureID);
			stream.writeLEInt(entry.effectID);
			stream.writeLEInt(entry.levelMin);
			stream.writeLEInt(entry.levelMax);
			entry.color.writeLE(stream);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("onDeath").arguments(HashManager.get().getFileName(onDeath));
		writer.command("onDeathSmall").arguments(HashManager.get().getFileName(onDeathSmall));
		writer.command("onDeathLarge").arguments(HashManager.get().getFileName(onDeathLarge));
		writer.command("onHatch").arguments(HashManager.get().getFileName(onHatch));
		writer.command("onStartHatch").arguments(HashManager.get().getFileName(onStartHatch));
		writer.blankLine();
		for (cSPAttachment entry : entries) {
			writer.command("cSPAttachment").arguments(ENUM_TYPE.get(entry.type));
			
			if (entry.type == TYPE_MODEL) {
				writer.arguments(HashManager.get().getFileName(entry.effectID));
				writer.option("bone").ints(entry.bone);
				writer.option("color").color(entry.color);
			}
			else if (entry.type == TYPE_EFFECT) {
				writer.arguments(HashManager.get().getFileName(entry.effectID));
				writer.option("bone").ints(entry.bone);
				writer.option("levelMin").ints(entry.levelMin);
				writer.option("levelMax").ints(entry.levelMax);
			}
			else if (entry.type == TYPE_STRUCTURE) {
				writer.arguments(HashManager.get().getFileName(entry.structureID));
				writer.option("bone").ints(entry.bone);
			}
			else if (entry.type == TYPE_CREATURE) {
				writer.arguments(HashManager.get().getFileName(entry.effectID));
			}
			else if (entry.type == TYPE_RANDOM_CREATURE) {
				writer.arguments(HashManager.get().getFileName(entry.randomCreatureID));
			}
			else if (entry.type == TYPE_PLAYER_CREATURE) {
			}
			else if (entry.type == TYPE_DEBUG) {
				writer.option("color").color(entry.color);
			}
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellStructureFile> generateStream() {
		ArgScriptStream<CellStructureFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser("onDeath", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				onDeath = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		stream.addParser("onDeathSmall", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				onDeathSmall = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		stream.addParser("onDeathLarge", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				onDeathLarge = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		stream.addParser("onHatch", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				onHatch = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		stream.addParser("onStartHatch", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				onStartHatch = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		
		stream.addParser("cSPAttachment", ArgScriptParser.create((parser, line) -> {
			cSPAttachment entry = new cSPAttachment();
			entries.add(entry);
			
			if (line.getArguments(args, 1, 2)) {
				entry.type = ENUM_TYPE.get(args, 0);
				
				if (entry.type == TYPE_MODEL) {
					if (line.getArguments(args, 2)) {
						entry.effectID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					}
					entry.bone = -1;
					if (line.getOptionArguments(args, "bone", 1)) {
						entry.bone = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					entry.color.copy(ColorRGB.white());
					if (line.getOptionArguments(args, "color", 1)) {
						stream.parseColorRGB(args, 0, entry.color);
					}
				}
				else if (entry.type == TYPE_EFFECT) {
					if (line.getArguments(args, 2)) {
						entry.effectID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					}
					entry.bone = -1;
					if (line.getOptionArguments(args, "bone", 1)) {
						entry.bone = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					entry.levelMin = -1;
					if (line.getOptionArguments(args, "levelMin", 1)) {
						entry.levelMin = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					entry.levelMax = -1;
					if (line.getOptionArguments(args, "levelMax", 1)) {
						entry.levelMax = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}
				else if (entry.type == TYPE_STRUCTURE) {
					if (line.getArguments(args, 2)) {
						entry.structureID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					}
					entry.bone = -1;
					if (line.getOptionArguments(args, "bone", 1)) {
						entry.bone = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}
				else if (entry.type == TYPE_CREATURE) {
					if (line.getArguments(args, 2)) {
						entry.effectID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					}
				}
				else if (entry.type == TYPE_RANDOM_CREATURE) {
					if (line.getArguments(args, 2)) {
						entry.randomCreatureID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					}
				}
				else if (entry.type == TYPE_PLAYER_CREATURE) {
					line.getArguments(args, 1);
				}
				else if (entry.type == TYPE_DEBUG) {
					line.getArguments(args, 1);
					entry.color.copy(ColorRGB.white());
					if (line.getOptionArguments(args, "color", 1)) {
						stream.parseColorRGB(args, 0, entry.color);
					}
				}
			}
		}));
		
		return stream;
	}
}
