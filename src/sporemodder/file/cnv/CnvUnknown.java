package sporemodder.file.cnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptWriter;

public class CnvUnknown {
	public int missionID;
	public int value2;
	public int value3;
	public final List<Integer> badges1 = new ArrayList<>();
	public final List<Integer> ints2 = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		missionID = stream.readLEInt();
		value2 = stream.readLEInt();
		value3 = stream.readLEInt();
		int count = stream.readLEInt();
		for (int i = 0; i < count; ++i) badges1.add(stream.readLEInt());
		count = stream.readLEInt();
		for (int i = 0; i < count; ++i) ints2.add(stream.readLEInt());
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(missionID);
		stream.writeLEInt(value2);
		stream.writeLEInt(value3);
		stream.writeLEInt(badges1.size());
		for (int i : badges1) stream.writeLEInt(i);
		stream.writeLEInt(ints2.size());
		for (int i : ints2) stream.writeLEInt(i);
	}
	 
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("unknown").startBlock();
		
		writer.command("missionID").arguments(HashManager.get().getFileName(missionID));
		if (value2 != 0) writer.command("value2").arguments(HashManager.get().getFileName(value2));
		if (value3 != 0) writer.command("value3").arguments(HashManager.get().getFileName(value3));
		
		if (!badges1.isEmpty()) {
			writer.command("badges1");
			for (int i : badges1) writer.arguments(HashManager.get().getFileName(i));
		}
		if (!ints2.isEmpty()) {
			writer.command("ints2");
			for (int i : ints2) writer.arguments(HashManager.get().getFileName(i));
		}
		
		writer.endBlock().commandEND();
	}
}
