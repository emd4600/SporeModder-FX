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
import sporemodder.file.argscript.ParserUtils;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellLootTableFile {
	public static final int TYPE_NOTHING = 0;
	public static final int TYPE_CELL = 1;
	public static final int TYPE_TABLE = 2;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_NOTHING, "nothing");
		ENUM_TYPE.add(TYPE_CELL, "cell");
		ENUM_TYPE.add(TYPE_TABLE, "table");
	}
	
	public static class cLootTableEntry
	{
		public int type;
		public int cellID;
		public int tableID;  // to a .cell file
		public float weight = 1.0f;
		public int count;
		public int countDelta;
		public int levelOffset;
	}
	
	public final List<cLootTableEntry> entries = new ArrayList<>();
	public float minRadius;
	public float maxRadius;
	public float initialAlpha = 1.0f;
	public float expelForce;
	public int effect;
	public boolean mustHavePart;
	public float delay;
	
	public void read(StreamReader stream) throws IOException
	{
		int offset = stream.readLEInt();
		int count = stream.readLEInt();
		
		if (offset != 36 && !(offset == 0 && count == 0))
			throw new IOException("Error: offset was not 36, file pointer " + stream.getFilePointer());
		
		minRadius = stream.readLEFloat();
		maxRadius = stream.readLEFloat();
		initialAlpha = stream.readLEFloat();
		expelForce = stream.readLEFloat();
		effect = stream.readLEInt();
		mustHavePart = stream.readBoolean();
		stream.skip(3);
		delay = stream.readLEFloat();
		
		for (int i = 0; i < count; i++) {
			cLootTableEntry entry = new cLootTableEntry();
			entry.type = stream.readLEInt();
			entry.cellID = stream.readLEInt();
			entry.tableID = stream.readLEInt();
			entry.weight = stream.readLEFloat();
			entry.count = stream.readLEInt();
			entry.countDelta = stream.readLEInt();
			entry.levelOffset = stream.readLEInt();
			entries.add(entry);
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(entries.isEmpty() ? 0 : 36);
		stream.writeLEInt(entries.size());
		stream.writeLEFloat(minRadius);
		stream.writeLEFloat(maxRadius);
		stream.writeLEFloat(initialAlpha);
		stream.writeLEFloat(expelForce);
		stream.writeLEInt(effect);
		stream.writeLEInt(mustHavePart ? 1 : 0);
		stream.writeLEFloat(delay);
		
		for (cLootTableEntry entry : entries) {
			stream.writeLEInt(entry.type);
			stream.writeLEInt(entry.cellID);
			stream.writeLEInt(entry.tableID);
			stream.writeLEFloat(entry.weight);
			stream.writeLEInt(entry.count);
			stream.writeLEInt(entry.countDelta);
			stream.writeLEInt(entry.levelOffset);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("minRadius").floats(minRadius);
		writer.command("maxRadius").floats(maxRadius);
		writer.command("initialAlpha").floats(initialAlpha);
		writer.command("expelForce").floats(expelForce);
		writer.command("effect").ints(effect);
		writer.command("mustHavePart").arguments(mustHavePart);
		writer.command("delay").floats(delay);
		
		writer.blankLine();
		for (cLootTableEntry entry : entries) {
			writer.command("cLootTableEntry").arguments(ENUM_TYPE.get(entry.type));
			
			if (entry.type == TYPE_CELL) {
				writer.arguments(HashManager.get().getFileName(entry.cellID));
				writer.option("weight").floats(entry.weight);
				writer.option("count").ints(entry.count);
				writer.option("countDelta").ints(entry.countDelta);
				writer.option("levelOffset").ints(entry.levelOffset);
			}
			else if (entry.type == TYPE_TABLE) {
				writer.arguments(HashManager.get().getFileName(entry.tableID));
				writer.option("weight").floats(entry.weight);
				writer.option("count").ints(entry.count);
				writer.option("countDelta").ints(entry.countDelta);
			}
			else if (entry.type == TYPE_NOTHING) {
				writer.option("weight").floats(entry.weight);
			}
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellLootTableFile> generateStream() {
		ArgScriptStream<CellLootTableFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		ParserUtils.createFloatParser("minRadius", stream, value -> minRadius = value);
		ParserUtils.createFloatParser("maxRadius", stream, value -> maxRadius = value);
		ParserUtils.createFloatParser("initialAlpha", stream, value -> initialAlpha = value);
		ParserUtils.createFloatParser("expelForce", stream, value -> expelForce = value);
		ParserUtils.createIntParser("effect", stream, value -> effect = value);
		ParserUtils.createBooleanParser("mustHavePart", stream, value -> mustHavePart = value);
		ParserUtils.createFloatParser("delay", stream, value -> delay = value);
		
		stream.addParser("cLootTableEntry", ArgScriptParser.create((parser, line) -> {
			cLootTableEntry entry = new cLootTableEntry();
			entries.add(entry);
			
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1, 2)) {
				entry.type = ENUM_TYPE.get(args, 0);
				
				if (entry.type == TYPE_CELL) {
					line.getArguments(args, 2);
					entry.cellID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					
					if (line.getOptionArguments(args, "weight", 1)) {
						entry.weight = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
					entry.count = 1;
					if (line.getOptionArguments(args, "count", 1)) {
						entry.count = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					if (line.getOptionArguments(args, "countDelta", 1)) {
						entry.countDelta = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					if (line.getOptionArguments(args, "levelOffset", 1)) {
						entry.levelOffset = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}
				else if (entry.type == TYPE_TABLE) {
					line.getArguments(args, 2);
					entry.tableID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
					
					if (line.getOptionArguments(args, "weight", 1)) {
						entry.weight = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
					entry.count = 1;
					if (line.getOptionArguments(args, "count", 1)) {
						entry.count = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					if (line.getOptionArguments(args, "countDelta", 1)) {
						entry.countDelta = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}
				else if (entry.type == TYPE_NOTHING) {
					line.getArguments(args, 1);
					
					if (line.getOptionArguments(args, "weight", 1)) {
						entry.weight = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}
			}
		}));
		
		return stream;
	}
}
