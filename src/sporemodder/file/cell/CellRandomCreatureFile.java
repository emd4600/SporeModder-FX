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

public class CellRandomCreatureFile {
	public static final int TYPE_CREATURE = 0;
	public static final int TYPE_POLLINATED = 1;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_CREATURE, "creature");
		ENUM_TYPE.add(TYPE_POLLINATED, "pollinated");
	}
	
	public static class cRandomCreatureEntry
	{
		public int type;  // ENUM_TYPE
		public int creatureID;
		public float weight = 1.0f;
		public int speedMin = -1;
		public int speedMax = -1;
		public int dangerMin = -1;
		public int dangerMax = -1;
	}
	public final List<cRandomCreatureEntry> entries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int count = stream.readLEInt();
		int offset = stream.readLEInt();
		if (offset != 8)
			throw new IOException("Error: offset was not 8, file pointer " + stream.getFilePointer());
		
		for (int i = 0; i < count; i++) {
			cRandomCreatureEntry entry = new cRandomCreatureEntry();
			entry.type = stream.readLEInt();
			entry.creatureID = stream.readLEInt();
			entry.weight = stream.readLEFloat();
			entry.speedMin = stream.readLEInt();
			entry.speedMax = stream.readLEInt();
			entry.dangerMin = stream.readLEInt();
			entry.dangerMax = stream.readLEInt();
			entries.add(entry);
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(entries.size());
		stream.writeLEInt(8);
		for (cRandomCreatureEntry entry : entries) {
			stream.writeLEInt(entry.type);
			stream.writeLEInt(entry.creatureID);
			stream.writeLEFloat(entry.weight);
			stream.writeLEInt(entry.speedMin);
			stream.writeLEInt(entry.speedMax);
			stream.writeLEInt(entry.dangerMin);
			stream.writeLEInt(entry.dangerMax);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (cRandomCreatureEntry entry : entries) {
			writer.command("cRandomCreatureEntry").arguments(ENUM_TYPE.get(entry.type));
			
			if (entry.type == TYPE_CREATURE) {
				writer.arguments(HashManager.get().getFileName(entry.creatureID));
				writer.option("weight").floats(entry.weight);
			}
			else if (entry.type == TYPE_POLLINATED) {
				writer.option("weight").floats(entry.weight);
				writer.option("speedMin").floats(entry.speedMin);
				writer.option("speedMax").floats(entry.speedMax);
				writer.option("dangerMin").floats(entry.dangerMin);
				writer.option("dangerMax").floats(entry.dangerMax);
			}
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellRandomCreatureFile> generateStream() {
		ArgScriptStream<CellRandomCreatureFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("cRandomCreatureEntry", ArgScriptParser.create((parser, line) -> {
			cRandomCreatureEntry entry = new cRandomCreatureEntry();
			entries.add(entry);
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1, 2)) {
				entry.type = ENUM_TYPE.get(args, 0);

				if (entry.type == TYPE_CREATURE) {
					if (line.getArguments(args, 2)) {
						entry.creatureID = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
					}
				}
				else if (entry.type == TYPE_POLLINATED) {
					if (line.getOptionArguments(args, "speedMin", 1)) {
						entry.speedMin = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					if (line.getOptionArguments(args, "speedMax", 1)) {
						entry.speedMax = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					if (line.getOptionArguments(args, "dangerMin", 1)) {
						entry.dangerMin = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
					if (line.getOptionArguments(args, "dangerMax", 1)) {
						entry.dangerMax = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}
				
				if (line.getOptionArguments(args, "weight", 1)) {
					entry.weight = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
				}
			}
		}));
		
		return stream;
	}
}
