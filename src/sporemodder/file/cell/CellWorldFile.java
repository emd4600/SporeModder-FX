package sporemodder.file.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellWorldFile {
	public static class cLevelEntry
	{
		public int populateID;
		public boolean startTile;
		public int playerSize;  // CellPopulate.ENUM_SCALE
	}
	public static class cAdvectEntry
	{
		public int field_0;  // CellPopulate.ENUM_SCALE
		public int playerSize;  // CellPopulate.ENUM_SCALE
		public float strength = 3.5f;
		public float variance;
		public float period = 1.0f;
		public int advectID;
	}
	public final List<cLevelEntry> populateEntries = new ArrayList<>();
	public final List<cAdvectEntry> advectEntries = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int populateCount = stream.readLEInt();
		int populateOffset = stream.readLEInt();
		int advectCount = stream.readLEInt();
		int advectOffset = stream.readLEInt();
		
		stream.seek(populateOffset);
		for (int i = 0; i < populateCount; i++) {
			cLevelEntry entry = new cLevelEntry();
			populateEntries.add(entry);
			entry.populateID = stream.readLEInt();
			entry.startTile = stream.readBoolean();
			stream.skip(3);
			entry.playerSize = stream.readLEInt();
		}
		
		stream.seek(advectOffset);
		for (int i = 0; i < advectCount; i++) {
			cAdvectEntry entry = new cAdvectEntry();
			advectEntries.add(entry);
			entry.field_0 = stream.readLEInt();
			entry.playerSize = stream.readLEInt();
			entry.strength = stream.readLEFloat();
			entry.variance = stream.readLEFloat();
			entry.period = stream.readLEFloat();
			entry.advectID = stream.readLEInt();
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(populateEntries.size());
		stream.writeLEInt(populateEntries.isEmpty() ? 0 : 16);
		stream.writeLEInt(advectEntries.size());
		stream.writeLEInt(advectEntries.isEmpty() ? 0 : (16 + populateEntries.size() * 12));
		
		for (cLevelEntry entry : populateEntries) {
			stream.writeLEInt(entry.populateID);
			stream.writeLEInt(entry.startTile ? 1 : 0);
			stream.writeLEInt(entry.playerSize);
		}
		
		for (cAdvectEntry entry : advectEntries) {
			stream.writeLEInt(entry.field_0);
			stream.writeLEInt(entry.playerSize);
			stream.writeLEFloat(entry.strength);
			stream.writeLEFloat(entry.variance);
			stream.writeLEFloat(entry.period);
			stream.writeLEInt(entry.advectID);
		}
	}
	
	public void clear() {
		populateEntries.clear();
		advectEntries.clear();
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (cLevelEntry entry : populateEntries) {
			writer.command("populate").arguments(HashManager.get().getFileName(entry.populateID));
			writer.option("startTile").arguments(entry.startTile);
			writer.option("playerSize").arguments(CellPopulateFile.ENUM_SCALE.get(entry.playerSize));
		}
		writer.blankLine();
		for (cAdvectEntry entry : advectEntries) {
			writer.command("advect").arguments(HashManager.get().getFileName(entry.advectID), CellPopulateFile.ENUM_SCALE.get(entry.field_0));
			writer.option("playerSize").arguments(CellPopulateFile.ENUM_SCALE.get(entry.playerSize));
			writer.option("strength").floats(entry.strength);
			writer.option("variance").floats(entry.variance);
			writer.option("period").floats(entry.period);
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellWorldFile> generateStream() {
		ArgScriptStream<CellWorldFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("populate", ArgScriptParser.create((parser_, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				cLevelEntry entry = new cLevelEntry();
				populateEntries.add(entry);
				
				entry.populateID = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
				
				if (line.getOptionArguments(args, "startTile", 1)) {
					entry.startTile = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
				}
				if (line.getOptionArguments(args, "playerSize", 1)) {
					entry.playerSize = CellPopulateFile.ENUM_SCALE.get(args, 0);
				}
			}
		}));
		
		stream.addParser("advect", ArgScriptParser.create((parser_, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 2)) {
				cAdvectEntry entry = new cAdvectEntry();
				advectEntries.add(entry);
				
				entry.advectID = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
				entry.field_0 = CellPopulateFile.ENUM_SCALE.get(args, 1);
				
				if (line.getOptionArguments(args, "playerSize", 1)) {
					entry.playerSize = CellPopulateFile.ENUM_SCALE.get(args, 0);
				}
				if (line.getOptionArguments(args, "strength", 1)) {
					entry.strength = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
				}
				if (line.getOptionArguments(args, "variance", 1)) {
					entry.variance = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
				}
				if (line.getOptionArguments(args, "period", 1)) {
					entry.period = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
				}
			}
		}));
		
		return stream;
	}
}
