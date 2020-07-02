package sporemodder.file.cnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Stream.StringEncoding;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptWriter;

public class CnvAnimation {
	public static class CnvAnimationVariation {
		public int id;
		public String name;
		public final List<Integer> idleAnimIDs = new ArrayList<>();
		public final List<Integer> actionAnimIDs = new ArrayList<>();
	}
	
	public int id;
	public String name;
	public List<CnvAnimationVariation> variations = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		id = stream.readLEInt();
		name = stream.readString(StringEncoding.ASCII, stream.readInt());
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvAnimationVariation cnv = new CnvAnimationVariation();
			variations.add(cnv);
			
			cnv.id = stream.readLEInt();
			cnv.name = stream.readString(StringEncoding.ASCII, stream.readInt());
			
			int count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) cnv.idleAnimIDs.add(stream.readInt());
			
			count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) cnv.actionAnimIDs.add(stream.readInt());
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(id);
		stream.writeInt(name.length());
		stream.writeString(name, StringEncoding.ASCII);
		
		stream.writeLEInt(variations.size());
		for (CnvAnimationVariation v : variations) {
			stream.writeLEInt(v.id);
			stream.writeInt(v.name.length());
			stream.writeString(v.name, StringEncoding.ASCII);
			
			stream.writeLEInt(v.idleAnimIDs.size());
			for (int i : v.idleAnimIDs) stream.writeInt(i);
			
			stream.writeLEInt(v.actionAnimIDs.size());
			for (int i : v.actionAnimIDs) stream.writeInt(i);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("animation");
		CnvUnit.addArgScriptName(writer, id, name);
		writer.startBlock();
		
		for (CnvAnimationVariation var : variations) {
			writer.command("variation");
			CnvUnit.addArgScriptName(writer, var.id, var.name);
			writer.startBlock();
			writer.command("idle");
			for (int i : var.idleAnimIDs) writer.arguments(HashManager.get().getFileName(i));
			writer.command("action");
			for (int i : var.actionAnimIDs) writer.arguments(HashManager.get().getFileName(i));
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
}
