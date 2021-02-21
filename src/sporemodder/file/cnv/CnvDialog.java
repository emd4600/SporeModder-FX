package sporemodder.file.cnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptWriter;

public class CnvDialog {
	public static class CnvDialogResponse {
		public final boolean[] requireFlags = new boolean[154];
		public final boolean[] excludeFlags = new boolean[154];
		public final boolean[] enableRequireFlags = new boolean[154];
		public final boolean[] enableExcludeFlags = new boolean[154];
		public int action;
		public final ResourceKey key = new ResourceKey();
		public final List<CnvText> texts = new ArrayList<>();
		public final List<Integer> ints1 = new ArrayList<>();
		public final List<Integer> ints2 = new ArrayList<>();
		public final List<Integer> ints3 = new ArrayList<>();
		public final List<Integer> ints4 = new ArrayList<>();
	}
	
	public int id;
	public String name;
	public boolean value1;
	public boolean showAccept;
	public boolean showDecline;
	public boolean value4;
	public boolean showStatic;
	public int action = -1;
	public final ResourceKey key = new ResourceKey();
	public final List<CnvText> texts = new ArrayList<>();
	public final List<CnvDialogResponse> responses = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		id = stream.readLEInt();
		name = stream.readString(StringEncoding.ASCII, stream.readInt());
		
		value1 = stream.readBoolean();
		showAccept = stream.readBoolean();
		showDecline = stream.readBoolean();
		value4 = stream.readBoolean();
		showStatic = stream.readBoolean();
		action = stream.readLEInt();
		key.setGroupID(stream.readInt());
		key.setTypeID(stream.readInt());
		key.setInstanceID(stream.readInt());
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvText text = new CnvText();
			text.read(stream);
			texts.add(text);
		}
		
		count = stream.readLEInt();
		for (int i = 0; i < count; ++i) {
			CnvDialogResponse response = new CnvDialogResponse();
			responses.add(response);
			
			stream.readBooleans(response.requireFlags);
			stream.readBooleans(response.excludeFlags);
			stream.readBooleans(response.enableRequireFlags);
			stream.readBooleans(response.enableExcludeFlags);
			response.action = stream.readLEInt();
			response.key.setGroupID(stream.readInt());
			response.key.setTypeID(stream.readInt());
			response.key.setInstanceID(stream.readInt());
			
			int count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) {
				CnvText text = new CnvText();
				text.read(stream);
				response.texts.add(text);
			}
			
			count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) response.ints1.add(stream.readInt());
			
			count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) response.ints2.add(stream.readInt());
			
			count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) response.ints3.add(stream.readInt());
			
			count2 = stream.readLEInt();
			for (int j = 0; j < count2; ++j) response.ints4.add(stream.readInt());
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(id);
		stream.writeInt(name.length());
		stream.writeString(name, StringEncoding.ASCII);
		
		stream.writeBoolean(value1);
		stream.writeBoolean(showAccept);
		stream.writeBoolean(showDecline);
		stream.writeBoolean(value4);
		stream.writeBoolean(showStatic);
		stream.writeLEInt(action);
		stream.writeInt(key.getGroupID());
		stream.writeInt(key.getTypeID());
		stream.writeInt(key.getInstanceID());
		
		stream.writeLEInt(texts.size());
		for (CnvText t : texts) t.write(stream);
		
		stream.writeLEInt(responses.size());
		for (CnvDialogResponse r : responses) {
			stream.writeBooleans(r.requireFlags);
			stream.writeBooleans(r.excludeFlags);
			stream.writeBooleans(r.enableRequireFlags);
			stream.writeBooleans(r.enableExcludeFlags);
			stream.writeLEInt(r.action);
			stream.writeInt(r.key.getGroupID());
			stream.writeInt(r.key.getTypeID());
			stream.writeInt(r.key.getInstanceID());
			
			stream.writeLEInt(r.texts.size());
			for (CnvText t : r.texts) t.write(stream);
			
			stream.writeLEInt(r.ints1.size());
			for (int i : r.ints1) stream.writeInt(i);
			
			stream.writeLEInt(r.ints2.size());
			for (int i : r.ints2) stream.writeInt(i);
			
			stream.writeLEInt(r.ints3.size());
			for (int i : r.ints3) stream.writeInt(i);
			
			stream.writeLEInt(r.ints4.size());
			for (int i : r.ints4) stream.writeInt(i);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("dialog");
		CnvUnit.addArgScriptName(writer, id, name);
		writer.startBlock();
		
		boolean blankLine = false;
		if (value1) {
			writer.command("value1").arguments(value1);
			blankLine = true;
		}
		if (showAccept) {
			writer.command("showAccept").arguments(showAccept);
			blankLine = true;
		}
		if (showDecline) {
			writer.command("showDecline").arguments(showDecline);
			blankLine = true;
		}
		if (value4) {
			writer.command("value4").arguments(value4);
			blankLine = true;
		}
		if (showStatic) {
			writer.command("showStatic").arguments(showStatic);
			blankLine = true;
		}
		if (action != -1) {
			writer.command("action").arguments(HashManager.get().getFileName(action));
			if (!key.isZero()) writer.arguments(key);
			blankLine = true;
		}
		
		if (blankLine) writer.blankLine();
		for (CnvText text : texts) text.toArgScript(writer);
		
		writer.blankLine();
		for (CnvDialogResponse response : responses) {
			writer.command("response").startBlock();
			
			writer.command("action").arguments(HashManager.get().getFileName(response.action));
			if (!response.key.isZero()) writer.arguments(response.key);
			for (CnvText text : response.texts) text.toArgScript(writer);
			
			CnvUnit.addArgScriptFlagCommand(writer, "require", response.requireFlags);
			CnvUnit.addArgScriptFlagCommand(writer, "exclude", response.excludeFlags);
			CnvUnit.addArgScriptFlagCommand(writer, "enableRequire", response.enableRequireFlags);
			CnvUnit.addArgScriptFlagCommand(writer, "enableExclude", response.enableExcludeFlags);
			
			if (!response.ints1.isEmpty()) {
				writer.command("ints1");
				for (int i : response.ints1) writer.arguments(HashManager.get().getFileName(i));
			}
			if (!response.ints2.isEmpty()) {
				writer.command("ints2");
				for (int i : response.ints2) writer.arguments(HashManager.get().getFileName(i));
			}
			if (!response.ints3.isEmpty()) {
				writer.command("ints3");
				for (int i : response.ints3) writer.arguments(HashManager.get().getFileName(i));
			}
			if (!response.ints4.isEmpty()) {
				writer.command("ints4");
				for (int i : response.ints4) writer.arguments(HashManager.get().getFileName(i));
			}
			
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
}
