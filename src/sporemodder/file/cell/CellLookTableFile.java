package sporemodder.file.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellLookTableFile {
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(0, "up");
		ENUM_TYPE.add(1, "forward");
		ENUM_TYPE.add(2, "left");
		ENUM_TYPE.add(3, "right");
		ENUM_TYPE.add(4, "pursue_target");
		ENUM_TYPE.add(5, "flee_target");
		ENUM_TYPE.add(6, "object");
		ENUM_TYPE.add(7, "food");
		ENUM_TYPE.add(8, "predator");
		ENUM_TYPE.add(9, "destination");
		ENUM_TYPE.add(10, "cursor");
		ENUM_TYPE.add(11, "player");
	}
	
	public static class cLookTableEntry
	{
		public int type;
		public float value;
	}
	public final List<cLookTableEntry> entries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int offset = stream.readLEInt();
		if (offset != 8)
			throw new IOException("Error: offset was not 8, file pointer " + stream.getFilePointer());
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; i++) {
			cLookTableEntry entry = new cLookTableEntry();
			entry.type = stream.readLEInt();
			entry.value = stream.readLEFloat();
			entries.add(entry);
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(8);
		stream.writeLEInt(entries.size());
		for (cLookTableEntry entry : entries) {
			stream.writeLEInt(entry.type);
			stream.writeLEFloat(entry.value);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (cLookTableEntry entry : entries) {
			writer.command("cLookTableEntry").arguments(ENUM_TYPE.get(entry.type)).floats(entry.value);
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellLookTableFile> generateStream() {
		ArgScriptStream<CellLookTableFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("cLookTableEntry", ArgScriptParser.create((parser, line) -> {
			cLookTableEntry entry = new cLookTableEntry();
			Number value = null;
			entries.add(entry);
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 2) && 
					(value = stream.parseFloat(args, 1)) != null) {
				entry.type = ENUM_TYPE.get(args, 0);
				entry.value = value.floatValue();
			}
		}));
		
		return stream;
	}
}
