package sporemodder.file.otdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class SummaryFile {
	
	public static final int TYPE_INT = 0x02E1A75D;
	public static final int TYPE_FLOAT = 0x02E1A7FF;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_INT, "int");
		ENUM_TYPE.add(TYPE_FLOAT, "float");
	}
	
	public static final ArgScriptEnum ENUM_PARAMETERS = new ArgScriptEnum();
	static {
		ENUM_PARAMETERS.add(0x2DD90AF, "type");
		ENUM_PARAMETERS.add(0x2DC9D1E, "subtype");
		ENUM_PARAMETERS.add(0x5B06E36, "cost");
		ENUM_PARAMETERS.add(0x2F05C60, "baseGear");
		ENUM_PARAMETERS.add(0x7358629A, "height");
		ENUM_PARAMETERS.add(0x2F05C58, "carnivore");
		ENUM_PARAMETERS.add(0x2F05C59, "herbivore");
		ENUM_PARAMETERS.add(0x2F05C5E, "cuteness");
		ENUM_PARAMETERS.add(0x2F05C5F, "totalSocial");
		ENUM_PARAMETERS.add(0x5B15AA5, "social");
		ENUM_PARAMETERS.add(0x2F05C61, "numGraspers");
		ENUM_PARAMETERS.add(0x3FEA210, "meanLookingScore");
		ENUM_PARAMETERS.add(0x3FEA1A0, "totalAttack");
		ENUM_PARAMETERS.add(0x5B15A92, "attack");
		ENUM_PARAMETERS.add(0x3FEA1C0, "numFeet");
		ENUM_PARAMETERS.add(0x4AB3BD8, "biteCapRange");
		ENUM_PARAMETERS.add(0x4AB3BD9, "strikeCapRange");
		ENUM_PARAMETERS.add(0x4AB3BDA, "chargeCapRange");
		ENUM_PARAMETERS.add(0x4AB3BDB, "spitCapRange");
		ENUM_PARAMETERS.add(0xF42136D5, "singCapRange");
		ENUM_PARAMETERS.add(0xF42136D6, "danceCapRange");
		ENUM_PARAMETERS.add(0xF42136D7, "charmCapRange");
		ENUM_PARAMETERS.add(0xF42136D8, "poseCapRange");
		ENUM_PARAMETERS.add(0x5AC2B96, "glideCapRange");
		ENUM_PARAMETERS.add(0x5AC2B97, "stealthCapRange");
		ENUM_PARAMETERS.add(0x5AC2B98, "sprintCapRange");
		ENUM_PARAMETERS.add(0x68DE3E8, "senseCapRange");
		ENUM_PARAMETERS.add(0x5AC2B99, "healthCapRange");
	}
	
	public static final ArgScriptEnum ENUM_AMTAGS = new ArgScriptEnum();
	static {
		ENUM_AMTAGS.add(0x1F71E85, "BlockID");
		ENUM_AMTAGS.add(0x4EA801F, "EditorGroup");
		ENUM_AMTAGS.add(0x4EAB460, "GroupCount");
		ENUM_AMTAGS.add(0x4EBFEBD, "BlockTag");
		ENUM_AMTAGS.add(0x5833344, "TextureTag");
		ENUM_AMTAGS.add(0x518AF1D, "Creator");
		ENUM_AMTAGS.add(0x68375D7, "ACPSet");
		ENUM_AMTAGS.add(0x518B883, "Feed");
		ENUM_AMTAGS.add(0x52B27B5, "RegionColor");
		ENUM_AMTAGS.add(0x52C812A, "BaseColor");
		ENUM_AMTAGS.add(0x52C8CA5, "Texture");
	}
	
	public static class AMTag {
		public int tagGroup;
		public int tagID;
		public float value;
	}
	
	public final Map<Integer, Number> parameters = new LinkedHashMap<>();
	public final List<AMTag> amTags = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		int version = stream.readInt();
		if (version != 4)
			throw new IOException("Unsupported .summary version " + version);
		
		int count = stream.readInt();
		for (int i = 0; i < count; i++) {
			int id = stream.readInt();
			int type = stream.readInt();
			
			if (type == TYPE_INT)
			{
				parameters.put(id, stream.readInt());
			}
			else if (type == TYPE_FLOAT)
			{
				parameters.put(id, stream.readFloat());
			}
			else {
				throw new IOException("Unknown param type " + HashManager.get().getFileName(type));
			}
		}
		
		count = stream.readInt();
		for (int i = 0; i < count; i++) {
			AMTag tag = new AMTag();
			tag.tagGroup = stream.readInt();
			tag.tagID = stream.readInt();
			tag.value = stream.readFloat();
			amTags.add(tag);
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(4);
		stream.writeInt(parameters.size());
		for (Map.Entry<Integer, Number> entry : parameters.entrySet()) {
			stream.writeInt(entry.getKey());
			if (entry.getValue() instanceof Integer) {
				stream.writeInt(TYPE_INT);
				stream.writeInt(entry.getValue().intValue());
			}
			else {
				stream.writeInt(TYPE_FLOAT);
				stream.writeFloat(entry.getValue().floatValue());
			}
		}
		stream.writeInt(amTags.size());
		for (AMTag tag : amTags) {
			stream.writeInt(tag.tagGroup);
			stream.writeInt(tag.tagID);
			stream.writeFloat(tag.value);
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		for (Map.Entry<Integer, Number> entry : parameters.entrySet()) {
			String valueString;
			if (entry.getValue() instanceof Integer) {
				valueString = HashManager.get().formatInt32(entry.getValue().intValue());
			}
			else {
				valueString = entry.getValue().toString();
			}
			
			String parameterString = ENUM_PARAMETERS.get(entry.getKey());
			if (parameterString == null) {
				parameterString = HashManager.get().getFileName(entry.getKey());
			}
			
			writer.command("parameter").arguments(
					ENUM_TYPE.get(entry.getValue() instanceof Integer ? TYPE_INT : TYPE_FLOAT),
					parameterString, 
					valueString);
		}
		writer.blankLine();
		for (AMTag tag : amTags) {
			String tagString = ENUM_AMTAGS.get(tag.tagGroup);
			if (tagString == null) {
				tagString = HashManager.get().getFileName(tag.tagGroup);
			}
			writer.command("amTag").arguments(
					tagString,
					HashManager.get().getFileName(tag.tagID),
					tag.value
					);
		}
	}
	
	public void clear() {
		parameters.clear();
		amTags.clear();
	}
	
	public ArgScriptStream<SummaryFile> generateStream() {
		ArgScriptStream<SummaryFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("parameter", ArgScriptParser.create((parser, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 3)) {
				
				int id;
				if (args.get(1).charAt(0) == '0') {
					id = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
				}
				else {
					id = ENUM_PARAMETERS.get(args, 1);
				}
				
				Number value;
				int type = ENUM_TYPE.get(args, 0);
				if (type == TYPE_INT) {
					value = stream.parseInt(args, 2);
				}
				else {
					value = stream.parseFloat(args, 2);
				}
				
				parameters.put(id, value);
			}
		}));
		
		stream.addParser("amTag", ArgScriptParser.create((parser, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 3)) {
				AMTag tag = new AMTag();

				if (args.get(0).charAt(0) == '0') {
					tag.tagGroup = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
				}
				else {
					tag.tagGroup = ENUM_AMTAGS.get(args, 0);
				}
				
				tag.tagID = Optional.ofNullable(stream.parseFileID(args, 1)).orElse(0);
				tag.value = Optional.ofNullable(stream.parseFloat(args, 2)).orElse(0f);
				
				amTags.add(tag);
			}
		}));
		
		return stream;
	}
}
