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

public class CellLookAlgorithmFile {
	public static final int TYPE_PLAYER = 0;
	public static final int TYPE_NPC = 1;
	public static final int TYPE_EPIC = 2;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_PLAYER, "player");
		ENUM_TYPE.add(TYPE_NPC, "npc");
		ENUM_TYPE.add(TYPE_EPIC, "epic");
	}
	
	public static final ArgScriptEnum ENUM_ACTION = new ArgScriptEnum();
	static {
		ENUM_ACTION.add(-1, "other");
		ENUM_ACTION.add(0, "idle");
		ENUM_ACTION.add(0x12, "moving");
		ENUM_ACTION.add(0x15, "scared");
		ENUM_ACTION.add(0x13, "move_scared");
		ENUM_ACTION.add(0x16, "mad");
		ENUM_ACTION.add(0x14, "move_mad");
	}
	
	public static class cLookAlgorithmEntry
	{
		public int type;
		public int action;  // ENUM_ACTION
		public int playerLookTable;  // look_table ID
		public int npcLookTable;  // look_table ID
		public int epicLookTable;  // look_table ID
	}
	public final List<cLookAlgorithmEntry> entries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int offset = stream.readLEInt();
		int count = stream.readLEInt();
		
		stream.seek(offset);
		for (int i = 0; i < count; i++) {
			cLookAlgorithmEntry entry = new cLookAlgorithmEntry();
			entry.type = stream.readLEInt();
			entry.action = stream.readLEInt();
			entry.playerLookTable = stream.readLEInt();
			entry.npcLookTable = stream.readLEInt();
			entry.epicLookTable = stream.readLEInt();
			entries.add(entry);
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(8);
		stream.writeLEInt(entries.size());
		for (cLookAlgorithmEntry entry : entries) {
			stream.writeLEInt(entry.type);
			stream.writeLEInt(entry.action);
			stream.writeLEInt(entry.playerLookTable);
			stream.writeLEInt(entry.npcLookTable);
			stream.writeLEInt(entry.epicLookTable);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (cLookAlgorithmEntry entry : entries) {
			writer.command("cLookAlgorithmEntry").arguments(ENUM_TYPE.get(entry.type)).arguments(ENUM_ACTION.get(entry.action));
			
			if (entry.type == TYPE_PLAYER) {
				writer.arguments(HashManager.get().getFileName(entry.playerLookTable));
			}
			else if (entry.type == TYPE_NPC) {
				writer.arguments(HashManager.get().getFileName(entry.npcLookTable));
			}
			else if (entry.type == TYPE_EPIC) {
				writer.arguments(HashManager.get().getFileName(entry.epicLookTable));
			}
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellLookAlgorithmFile> generateStream() {
		ArgScriptStream<CellLookAlgorithmFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("cLookAlgorithmEntry", ArgScriptParser.create((parser, line) -> {
			cLookAlgorithmEntry entry = new cLookAlgorithmEntry();
			entries.add(entry);
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 3)) {
				entry.type = ENUM_TYPE.get(args, 0);
				entry.action = ENUM_ACTION.get(args, 1);
				
				if (entry.type == TYPE_PLAYER) {
					entry.playerLookTable = Optional.ofNullable(stream.parseFileID(args, 2)).orElse(0);
				}
				else if (entry.type == TYPE_NPC) {
					entry.npcLookTable = Optional.ofNullable(stream.parseFileID(args, 2)).orElse(0);
				}
				else if (entry.type == TYPE_EPIC) {
					entry.epicLookTable = Optional.ofNullable(stream.parseFileID(args, 2)).orElse(0);
				}
			}
		}));
		
		return stream;
	}
}
