package sporemodder.file.cnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.locale.LocaleUnit;

public class CnvText {
	public final boolean[] flags1 = new boolean[154];
	public final boolean[] flags2 = new boolean[154];
	public int tableID;
	public int instanceID;
	public int emotion;
	
	public void read(StreamReader stream) throws IOException {
		stream.readBooleans(flags1);
		stream.readBooleans(flags2);
		instanceID = stream.readInt();
		tableID = stream.readInt();
		emotion = stream.readInt();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeBooleans(flags1);
		stream.writeBooleans(flags2);
		stream.writeInt(instanceID);
		stream.writeInt(tableID);
		stream.writeInt(emotion);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("text").arguments(HashManager.get().getFileName(tableID) + "!" + HashManager.get().getFileName(instanceID));
		if (emotion != 0) writer.option("emotion").arguments(HashManager.get().getFileName(emotion));
		CnvUnit.addArgScriptFlagOption(writer, "flags1", flags1);
		CnvUnit.addArgScriptFlagOption(writer, "flags2", flags2);
	}
	
	public void parse(ArgScriptStream<CnvUnit> stream, ArgScriptLine line) {
		final ArgScriptArguments args = new ArgScriptArguments();
		if (line.getArguments(args, 1)) {
			String[] originals = new String[2];
			ResourceID res = new ResourceID();
			if (res.parse(args, 0, originals)) {
				tableID = res.getGroupID();
				instanceID = res.getInstanceID();
				
				args.addHyperlink(LocaleUnit.HYPERLINK_LOCALE, originals, 0);
			}
		}
		
		Integer value = null;
		if (line.getOptionArguments(args, "emotion", 1) && (value = stream.parseFileID(args, 0)) != null)
			emotion = value;
		
		final List<Integer> flags = new ArrayList<>();
		if (line.getOptionArguments(args, "flags1", 1, Integer.MAX_VALUE) && stream.parseInts(args, flags)) {
			for (int i : flags) flags1[i] = true; 
		}
		
		flags.clear();
		if (line.getOptionArguments(args, "flags2", 1, Integer.MAX_VALUE) && stream.parseInts(args, flags)) {
			for (int i : flags) flags2[i] = true; 
		}
	}
}
