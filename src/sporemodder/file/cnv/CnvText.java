package sporemodder.file.cnv;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.locale.LocaleUnit;

public class CnvText {
	public final boolean[] requireFlags = new boolean[154];
	public final boolean[] excludeFlags = new boolean[154];
	public int tableID;
	public int instanceID;
	public int emotion;
	
	public void read(StreamReader stream) throws IOException {
		stream.readBooleans(requireFlags);
		stream.readBooleans(excludeFlags);
		instanceID = stream.readInt();
		tableID = stream.readInt();
		emotion = stream.readInt();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeBooleans(requireFlags);
		stream.writeBooleans(excludeFlags);
		stream.writeInt(instanceID);
		stream.writeInt(tableID);
		stream.writeInt(emotion);
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("text").arguments(HashManager.get().getFileName(tableID) + "!" + HashManager.get().getFileName(instanceID));
		if (emotion != 0) writer.option("emotion").arguments(HashManager.get().getFileName(emotion));
		CnvUnit.addArgScriptFlagOption(writer, "require", requireFlags);
		CnvUnit.addArgScriptFlagOption(writer, "exclude", excludeFlags);
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
		
		for (int i = 0; i < requireFlags.length; ++i) requireFlags[i] = false;
		if (line.getOptionArguments(args, "flags1", 1, Integer.MAX_VALUE) 
				|| line.getOptionArguments(args, "require", 1, Integer.MAX_VALUE)) {
			CnvUnit.parseFlags(stream, args, requireFlags);
		}
		
		for (int i = 0; i < excludeFlags.length; ++i) excludeFlags[i] = false;
		if (line.getOptionArguments(args, "flags2", 1, Integer.MAX_VALUE) 
				|| line.getOptionArguments(args, "exclude", 1, Integer.MAX_VALUE)) {
			CnvUnit.parseFlags(stream, args, excludeFlags);
		}
	
	}
}
