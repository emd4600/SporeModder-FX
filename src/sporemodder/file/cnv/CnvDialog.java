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
		public final boolean[] flags1 = new boolean[154];
		public final boolean[] flags2 = new boolean[154];
		public final boolean[] flags3 = new boolean[154];
		public final boolean[] flags4 = new boolean[154];
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
	public boolean value2;
	public boolean value3;
	public boolean value4;
	public boolean value5;
	public int action = -1;
	public final ResourceKey key = new ResourceKey();
	public final List<CnvText> texts = new ArrayList<>();
	public final List<CnvDialogResponse> responses = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		id = stream.readLEInt();
		name = stream.readString(StringEncoding.ASCII, stream.readInt());
		
		value1 = stream.readBoolean();
		value2 = stream.readBoolean();
		value3 = stream.readBoolean();
		value4 = stream.readBoolean();
		value5 = stream.readBoolean();
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
			
			stream.readBooleans(response.flags1);
			stream.readBooleans(response.flags2);
			stream.readBooleans(response.flags3);
			stream.readBooleans(response.flags4);
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
		stream.writeBoolean(value2);
		stream.writeBoolean(value3);
		stream.writeBoolean(value4);
		stream.writeBoolean(value5);
		stream.writeLEInt(action);
		stream.writeInt(key.getGroupID());
		stream.writeInt(key.getTypeID());
		stream.writeInt(key.getInstanceID());
		
		stream.writeLEInt(texts.size());
		for (CnvText t : texts) t.write(stream);
		
		stream.writeLEInt(responses.size());
		for (CnvDialogResponse r : responses) {
			stream.writeBooleans(r.flags1);
			stream.writeBooleans(r.flags2);
			stream.writeBooleans(r.flags3);
			stream.writeBooleans(r.flags4);
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
		if (value2) {
			writer.command("value2").arguments(value2);
			blankLine = true;
		}
		if (value3) {
			writer.command("value3").arguments(value3);
			blankLine = true;
		}
		if (value4) {
			writer.command("value4").arguments(value4);
			blankLine = true;
		}
		if (value5) {
			writer.command("value5").arguments(value5);
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
			
			blankLine = false;
			
			if (CnvUnit.addArgScriptFlagCommand(writer, "flags1", response.flags1) && !blankLine) {
				writer.blankLine();
				blankLine = true;
			}
			if (CnvUnit.addArgScriptFlagCommand(writer, "flags2", response.flags2) && !blankLine) {
				writer.blankLine();
				blankLine = true;
			}
			if (CnvUnit.addArgScriptFlagCommand(writer, "flags3", response.flags3) && !blankLine) {
				writer.blankLine();
				blankLine = true;
			}
			if (CnvUnit.addArgScriptFlagCommand(writer, "flags4", response.flags4) && !blankLine) {
				writer.blankLine();
				blankLine = true;
			}
			
			if (!response.ints1.isEmpty()) {
				if (!blankLine) {
					writer.blankLine();
					blankLine = true;
				}
				writer.command("ints1");
				for (int i : response.ints1) writer.arguments(HashManager.get().getFileName(i));
			}
			if (!response.ints2.isEmpty()) {
				if (!blankLine) {
					writer.blankLine();
					blankLine = true;
				}
				writer.command("ints2");
				for (int i : response.ints2) writer.arguments(HashManager.get().getFileName(i));
			}
			if (!response.ints1.isEmpty()) {
				if (!blankLine) {
					writer.blankLine();
					blankLine = true;
				}
				writer.command("ints3");
				for (int i : response.ints3) writer.arguments(HashManager.get().getFileName(i));
			}
			if (!response.ints4.isEmpty()) {
				if (!blankLine) {
					writer.blankLine();
					blankLine = true;
				}
				writer.command("ints4");
				for (int i : response.ints4) writer.arguments(HashManager.get().getFileName(i));
			}
			
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
}
