package sporemodder.file.cell;

import java.io.IOException;

import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.argscript.ParserUtils;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellPowersFile {
	public int teleportCost = 10;
	public float teleportRange = 10.0f;
	
	public void read(StreamReader stream) throws IOException
	{
		teleportCost = stream.readLEInt();
		teleportRange = stream.readLEFloat();
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(teleportCost);
		stream.writeLEFloat(teleportRange);
	}
	
	public void clear() {
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("teleportCost").ints(teleportCost);
		writer.command("teleportRange").floats(teleportRange);
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellPowersFile> generateStream() {
		ArgScriptStream<CellPowersFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		ParserUtils.createIntParser("teleportCost", stream, value -> teleportCost = value);
		ParserUtils.createFloatParser("teleportRange", stream, value -> teleportRange = value);
		
		return stream;
	}
}
