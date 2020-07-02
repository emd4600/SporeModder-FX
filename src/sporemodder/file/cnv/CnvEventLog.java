package sporemodder.file.cnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Stream.StringEncoding;
import sporemodder.file.argscript.ArgScriptWriter;

public class CnvEventLog {
	public int id;
	public String name;
	public final List<CnvText> texts = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		id = stream.readLEInt();
		name = stream.readString(StringEncoding.ASCII, stream.readInt());
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvText text = new CnvText();
			text.read(stream);
			texts.add(text);
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(id);
		stream.writeInt(name.length());
		stream.writeString(name, StringEncoding.ASCII);
		
		stream.writeLEInt(texts.size());
		for (CnvText t : texts) t.write(stream);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("eventlog");
		CnvUnit.addArgScriptName(writer, id, name);
		writer.startBlock();
		
		for (CnvText text : texts) text.toArgScript(writer);
		
		writer.endBlock().commandEND();
	}
}
