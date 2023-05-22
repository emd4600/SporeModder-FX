package sporemodder.file.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellPopulateFile {
	public static final ArgScriptEnum ENUM_SCALE = new ArgScriptEnum();
	static {
		ENUM_SCALE.add(-1, "none");
		ENUM_SCALE.add(0, "0");
		ENUM_SCALE.add(1, "1");
		ENUM_SCALE.add(2, "2");
		ENUM_SCALE.add(3, "4");
		ENUM_SCALE.add(4, "10");
		ENUM_SCALE.add(5, "20");
		ENUM_SCALE.add(6, "40");
		ENUM_SCALE.add(7, "100");
		ENUM_SCALE.add(8, "200");
		ENUM_SCALE.add(9, "400");
		ENUM_SCALE.add(10, "1K");
		ENUM_SCALE.add(11, "2K");
		ENUM_SCALE.add(12, "4K");
		ENUM_SCALE.add(13, "10K");
		ENUM_SCALE.add(14, "20K");
		ENUM_SCALE.add(15, "40K");
		ENUM_SCALE.add(16, "100K");
		ENUM_SCALE.add(17, "200K");
		ENUM_SCALE.add(18, "400K");
		ENUM_SCALE.add(19, "1M");
	}
	
	public static final ArgScriptEnum ENUM_PLANT_TYPE = new ArgScriptEnum();
	static {
		ENUM_PLANT_TYPE.add(0, "type1");
		ENUM_PLANT_TYPE.add(1, "type2");
		ENUM_PLANT_TYPE.add(2, "type3");
		ENUM_PLANT_TYPE.add(3, "type4");
		ENUM_PLANT_TYPE.add(4, "type5");
		ENUM_PLANT_TYPE.add(5, "type6");
	}
	
	public static final int TYPE_DISTRIBUTE = 0;
	public static final int TYPE_CLUSTER = 1;
	public static final int TYPE_PLANT = 2;
	public static final int TYPE_ENCOUNTER = 3;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_DISTRIBUTE, "distribute");
		ENUM_TYPE.add(TYPE_CLUSTER, "cluster");
		ENUM_TYPE.add(TYPE_PLANT, "plant");
		ENUM_TYPE.add(TYPE_ENCOUNTER, "encounter");
	}
	
	public static class cMarker
	{
		public int field_0;
		public int field_4;
		public int field_8;
		public float zOffset;
		public float zOffsetMax;
		public int field_14;
		public int distributeCellID;
		public int clusterCellID;
		public int encounterPopulateID;
		public int plantType;  // ENUM_PLANT_TYPE
		public int type;  // ENUM_TYPE
		public float count = -1.0f;
		public float count_easy = -1.0f;
		public float count_med = -1.0f;
		public float count_hard = -1.0f;
		public int size = 1;  // ENUM_SCALE
		public int parts = 5;
		public int linear;
		public float encounterScale;
	}
	
	public int scale = 1;  // ENUM_SCALE
	public int maskTexture;
	public final List<cMarker> markers = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		scale = stream.readLEInt();
		maskTexture = stream.readLEInt();
		int count = stream.readLEInt();
		int offset = stream.readLEInt();
		if (offset != 0x10 && count != 0)
			throw new IOException("Error: offset was not 16, file pointer " + stream.getFilePointer());
		
		for (int i = 0; i < count; i++) {
			cMarker marker = new cMarker();
			marker.field_0 = stream.readLEInt();
			marker.field_4 = stream.readLEInt();
			marker.field_8 = stream.readLEInt();
			marker.zOffset = stream.readLEFloat();
			marker.zOffsetMax = stream.readLEFloat();
			marker.field_14 = stream.readLEInt();
			marker.distributeCellID = stream.readLEInt();
			marker.clusterCellID = stream.readLEInt();
			marker.encounterPopulateID = stream.readLEInt();
			marker.plantType = stream.readLEInt();
			marker.type = stream.readLEInt();
			marker.count = stream.readLEFloat();
			marker.count_easy = stream.readLEFloat();
			marker.count_med = stream.readLEFloat();
			marker.count_hard = stream.readLEFloat();
			marker.size = stream.readLEInt();
			marker.parts = stream.readLEInt();
			marker.linear = stream.readLEInt();
			marker.encounterScale = stream.readLEFloat();
			markers.add(marker);
			
			if (marker.type > 3) {
				throw new IOException("Error: unknown cMarker type, file pointer " + stream.getFilePointer());
			}
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(scale);
		stream.writeLEInt(maskTexture);
		stream.writeLEInt(markers.size());
		stream.writeLEInt(16);
		for (cMarker marker : markers) {
			stream.writeLEInt(marker.field_0);
			stream.writeLEInt(marker.field_4);
			stream.writeLEInt(marker.field_8);
			stream.writeLEFloat(marker.zOffset);
			stream.writeLEFloat(marker.zOffsetMax);
			stream.writeLEInt(marker.field_14);
			stream.writeLEInt(marker.distributeCellID);
			stream.writeLEInt(marker.clusterCellID);
			stream.writeLEInt(marker.encounterPopulateID);
			stream.writeLEInt(marker.plantType);
			stream.writeLEInt(marker.type);
			stream.writeLEFloat(marker.count);
			stream.writeLEFloat(marker.count_easy);
			stream.writeLEFloat(marker.count_med);
			stream.writeLEFloat(marker.count_hard);
			stream.writeLEInt(marker.size);
			stream.writeLEInt(marker.parts);
			stream.writeLEInt(marker.linear);
			stream.writeLEFloat(marker.encounterScale);
		}
	}
	
	public void clear() {
		markers.clear();
		maskTexture = 0;
		scale = 1;
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("scale").arguments(ENUM_SCALE.get(scale));
		writer.command("maskTexture").arguments(HashManager.get().getFileName(maskTexture));
		
		for (cMarker marker : markers) {
			writer.blankLine();
			writer.command("cMarker").arguments(ENUM_TYPE.get(marker.type)).ints(marker.field_0, marker.field_4, marker.field_8).startBlock();
			if (marker.type == TYPE_DISTRIBUTE) {
				writer.command("distributeCellID").arguments(HashManager.get().getFileName(marker.distributeCellID));
				writer.command("count").floats(marker.count);
				writer.command("count_easy").floats(marker.count_easy);
				writer.command("count_med").floats(marker.count_med);
				writer.command("count_hard").floats(marker.count_hard);
				writer.command("size").arguments(ENUM_SCALE.get(marker.size));
				writer.command("zOffset").floats(marker.zOffset);
				writer.command("zOffsetMax").floats(marker.zOffsetMax);
			}
			else if (marker.type == TYPE_CLUSTER) {
				writer.command("clusterCellID").arguments(HashManager.get().getFileName(marker.clusterCellID));
				writer.command("parts").ints(marker.parts);
				writer.command("count").floats(marker.count);
				writer.command("count_easy").floats(marker.count_easy);
				writer.command("count_med").floats(marker.count_med);
				writer.command("count_hard").floats(marker.count_hard);
				writer.command("size").arguments(ENUM_SCALE.get(marker.size));
				writer.command("zOffset").floats(marker.zOffset);
				writer.command("zOffsetMax").floats(marker.zOffsetMax);
				writer.command("linear").ints(marker.linear);
			}
			else if (marker.type == TYPE_PLANT) {
				writer.command("plantType").arguments(ENUM_PLANT_TYPE.get(marker.plantType));
				writer.command("count").floats(marker.count);
				writer.command("count_easy").floats(marker.count_easy);
				writer.command("count_med").floats(marker.count_med);
				writer.command("count_hard").floats(marker.count_hard);
				writer.command("size").arguments(ENUM_SCALE.get(marker.size));
				writer.command("zOffset").floats(marker.zOffset);
				writer.command("zOffsetMax").floats(marker.zOffsetMax);
			}
			else if (marker.type == TYPE_ENCOUNTER) {
				writer.command("encounterPopulateID").arguments(HashManager.get().getFileName(marker.encounterPopulateID));
				writer.command("encounterScale").floats(marker.encounterScale);
				writer.command("count").floats(marker.count);
				writer.command("count_easy").floats(marker.count_easy);
				writer.command("count_med").floats(marker.count_med);
				writer.command("count_hard").floats(marker.count_hard);
			}
			writer.endBlock().commandEND();
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellPopulateFile> generateStream() {
		ArgScriptStream<CellPopulateFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		stream.addParser("scale", ArgScriptParser.create((parser, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				scale = ENUM_SCALE.get(args, 0);
			}
		}));
		
		stream.addParser("maskTexture", ArgScriptParser.create((parser, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				maskTexture = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		
		stream.addParser("cMarker", new ArgScriptBlock<CellPopulateFile>() {
			cMarker marker;
			
			@Override
			public void parse(ArgScriptLine line) {
				marker = new cMarker();
				data.markers.add(marker);
				stream.startBlock(this);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 4)) {
					marker.type = ENUM_TYPE.get(args, 0);
					marker.field_0 = Optional.ofNullable(stream.parseInt(args, 1)).orElse(0);
					marker.field_4 = Optional.ofNullable(stream.parseInt(args, 2)).orElse(0);
					marker.field_8 = Optional.ofNullable(stream.parseInt(args, 3)).orElse(0);
					
					if (marker.type == TYPE_ENCOUNTER) {
						marker.encounterScale = 0.5f;
					}
				}
			}
	
			@Override
			public void setData(ArgScriptStream<CellPopulateFile> stream, CellPopulateFile data) {
				super.setData(stream, data);
				
				final ArgScriptArguments args = new ArgScriptArguments();
				
				addParser("distributeCellID", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_DISTRIBUTE) {
						stream.addError(line.createError("This attribute is only available for 'distribute' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.distributeCellID = stream.parseFileID(args, 0);
					}
				}));
				
				addParser("clusterCellID", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_CLUSTER) {
						stream.addError(line.createError("This attribute is only available for 'cluster' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.clusterCellID = stream.parseFileID(args, 0);
					}
				}));
				addParser("parts", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_CLUSTER) {
						stream.addError(line.createError("This attribute is only available for 'cluster' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.parts = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}));
				
				addParser("plantType", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_PLANT) {
						stream.addError(line.createError("This attribute is only available for 'plant' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.plantType = ENUM_PLANT_TYPE.get(args, 0);
					}
				}));
				
				addParser("encounterPopulateID", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_ENCOUNTER) {
						stream.addError(line.createError("This attribute is only available for 'encounter' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.encounterPopulateID = stream.parseFileID(args, 0);
					}
				}));
				addParser("encounterScale", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_ENCOUNTER) {
						stream.addError(line.createError("This attribute is only available for 'encounter' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.encounterScale = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				
				addParser("count", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						marker.count = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				addParser("count_easy", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						marker.count_easy = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				addParser("count_med", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						marker.count_med = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				addParser("count_hard", ArgScriptParser.create((parser, line) -> {
					if (line.getArguments(args, 1)) {
						marker.count_hard = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				
				addParser("size", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_DISTRIBUTE && marker.type != TYPE_CLUSTER && marker.type != TYPE_PLANT) {
						stream.addError(line.createError("This attribute is only available for 'distribute', 'cluster', or 'plant' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.size = ENUM_SCALE.get(args, 0);
					}
				}));
				addParser("zOffset", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_DISTRIBUTE && marker.type != TYPE_CLUSTER && marker.type != TYPE_PLANT) {
						stream.addError(line.createError("This attribute is only available for 'distribute', 'cluster', or 'plant' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.zOffset = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				addParser("zOffsetMax", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_DISTRIBUTE && marker.type != TYPE_CLUSTER && marker.type != TYPE_PLANT) {
						stream.addError(line.createError("This attribute is only available for 'distribute', 'cluster', or 'plant' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.zOffsetMax = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
					}
				}));
				
				addParser("linear", ArgScriptParser.create((parser, line) -> {
					if (marker.type != TYPE_CLUSTER) {
						stream.addError(line.createError("This attribute is only available for 'cluster' markers"));
					}
					else if (line.getArguments(args, 1)) {
						marker.linear = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					}
				}));
			}
		});
		
		return stream;
	}
}
