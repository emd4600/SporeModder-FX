package sporemodder.file.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.ColorRGB;

public class CellBackgroundMapFile {
	public static class cBackgroundMapEntry
	{
		public final ColorRGB color = ColorRGB.white();
		public float field_C = 1.0f;
	}
	public final List<cBackgroundMapEntry> entries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int count = stream.readLEInt();
		int offset = stream.readLEInt();
		
		if (offset != 8 && !(offset == 0 && count == 0))
			throw new IOException("Error: offset was not 8, file pointer " + stream.getFilePointer());
		
		for (int i = 0; i < count; i++) {
			cBackgroundMapEntry entry = new cBackgroundMapEntry();
			entries.add(entry);
			entry.color.readLE(stream);
			entry.field_C = stream.readLEFloat();
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(entries.size());
		stream.writeLEInt(entries.isEmpty() ? 0 : 8);
		
		for (cBackgroundMapEntry entry : entries) {
			entry.color.writeLE(stream);
			stream.writeLEFloat(entry.field_C);
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (cBackgroundMapEntry entry : entries) {
			writer.command("color").color(entry.color).floats(entry.field_C);
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellBackgroundMapFile> generateStream() {
		ArgScriptStream<CellBackgroundMapFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("color", ArgScriptParser.create((parser_, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 2)) {
				cBackgroundMapEntry entry = new cBackgroundMapEntry();
				entries.add(entry);
				stream.parseColorRGB(args, 0, entry.color);
				entry.field_C = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(1f);
			}
		}));
		
		return stream;
	}
}
