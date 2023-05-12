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

public class CellEffectMapFile {
	public static final int TYPE_FOREGROUND = 0;
	public static final int TYPE_BACKGROUND = 1;
	public static final int TYPE_TRACER = 2;
	public static final int TYPE_TRACER_BACKGROUND = 3;
	public static final int TYPE_SKYBOX = 4;
	public static final int TYPE_TILE_BACKGROUND = 5;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_FOREGROUND, "foreground");
		ENUM_TYPE.add(TYPE_BACKGROUND, "background");
		ENUM_TYPE.add(TYPE_TRACER, "tracer");
		ENUM_TYPE.add(TYPE_TRACER_BACKGROUND, "tracer_background");
		ENUM_TYPE.add(TYPE_SKYBOX, "skybox");
		ENUM_TYPE.add(TYPE_TILE_BACKGROUND, "tile_background");
	}
	
	public static class cEffectMapEntry
	{
		public int effectID;
		public int type;
		public float field_8;
		public float field_C;
		public float field_10;
		public float field_14;
		public int field_18;
	}
	public final List<cEffectMapEntry> entries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int count = stream.readLEInt();
		int offset = stream.readLEInt();
		if (offset != 8)
			throw new IOException("Error: offset was not 8, file pointer " + stream.getFilePointer());
		
		for (int i = 0; i < count; i++) {
			cEffectMapEntry entry = new cEffectMapEntry();
			entry.effectID = stream.readLEInt();
			entry.type = stream.readLEInt();
			entry.field_8 = stream.readLEFloat();
			entry.field_C = stream.readLEFloat();
			entry.field_10 = stream.readLEFloat();
			entry.field_14 = stream.readLEFloat();
			entry.field_18 = stream.readLEInt();
			entries.add(entry);
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(entries.size());
		stream.writeLEInt(8);
		for (cEffectMapEntry entry : entries) {
			stream.writeLEInt(entry.effectID);
			stream.writeLEInt(entry.type);
			stream.writeLEFloat(entry.field_8);
			stream.writeLEFloat(entry.field_C);
			stream.writeLEFloat(entry.field_10);
			stream.writeLEFloat(entry.field_14);
			stream.writeLEInt(entry.field_18);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (cEffectMapEntry entry : entries) {
			writer.command("cEffectMapEntry")
				.arguments(ENUM_TYPE.get(entry.type), HashManager.get().getFileName(entry.effectID))
				.floats(entry.field_8, entry.field_C, entry.field_10, entry.field_14)
				.ints(entry.field_18);
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellEffectMapFile> generateStream() {
		ArgScriptStream<CellEffectMapFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("cEffectMapEntry", ArgScriptParser.create((parser, line) -> {
			cEffectMapEntry entry = new cEffectMapEntry();
			entries.add(entry);
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 7)) {
				entry.type = ENUM_TYPE.get(args, 0);
				entry.effectID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
				entry.field_8 = Optional.ofNullable(stream.parseFloat(args, 2)).orElse(0f);
				entry.field_C = Optional.ofNullable(stream.parseFloat(args, 3)).orElse(0f);
				entry.field_10 = Optional.ofNullable(stream.parseFloat(args, 4)).orElse(0f);
				entry.field_14 = Optional.ofNullable(stream.parseFloat(args, 5)).orElse(0f);
				entry.field_18 = Optional.ofNullable(stream.parseInt(args, 6)).orElse(0);
			}
		}));
		
		return stream;
	}
}
