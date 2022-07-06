package sporemodder.file.lvl;

import java.io.IOException;

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class MigrationPointData extends GameplayMarkerData {

	public static final int TYPE = 0xC012AE1F;  // MigrationPoint
	
	public static final ArgScriptEnum MIGRATION_POINT_TYPE_ENUM = new ArgScriptEnum();
	static {
		MIGRATION_POINT_TYPE_ENUM.add(0, "Normal");
		MIGRATION_POINT_TYPE_ENUM.add(1, "AvatarJourney");
		MIGRATION_POINT_TYPE_ENUM.add(2, "PatrolPath");
		MIGRATION_POINT_TYPE_ENUM.add(3, "AvatarSpeciesJourney");
		MIGRATION_POINT_TYPE_ENUM.add(4, "CreaturePath");
	}
	
	public int number;
	public float radiusMultiplier;
	public int type;
	public int field_128;
	public int field_12C;
	public int field_130;
	
	public MigrationPointData(GameplayMarker marker) {
		super(marker);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		number = stream.readLEInt();
		radiusMultiplier = stream.readLEFloat();
		type = stream.readLEInt();
		field_128 = stream.readLEInt();
		field_12C = stream.readLEInt();
		field_130 = stream.readLEInt();
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(number);
		stream.writeLEFloat(radiusMultiplier);
		stream.writeLEInt(type);
		stream.writeLEInt(field_128);
		stream.writeLEInt(field_12C);
		stream.writeLEInt(field_130);
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("number").ints(number);
		if (radiusMultiplier != 0.0) writer.command("radiusMultiplier").floats(radiusMultiplier);
		if (type != 0) writer.command("type").arguments(MIGRATION_POINT_TYPE_ENUM.get(type));
		if (field_128 != 0) writer.command("field_128").ints(field_128);
		if (field_12C != 0) writer.command("field_12C").ints(field_12C);
		if (field_130 != 0) writer.command("field_130").ints(field_130);
	}

	@Override
	public void addParsers(ArgScriptBlock<LevelDefinition> block, ArgScriptStream<LevelDefinition> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		block.addParser("number", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				number = value.intValue();
			}
		}));
		
		block.addParser("radiusMultiplier", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				radiusMultiplier = value.floatValue();
			}
		}));
		
		block.addParser("type", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				type = MIGRATION_POINT_TYPE_ENUM.get(args, 0);
			}
		}));
		
		block.addParser("field_128", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				field_128 = value.intValue();
			}
		}));
		block.addParser("field_12C", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				field_128 = value.intValue();
			}
		}));
		block.addParser("field_130", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				field_128 = value.intValue();
			}
		}));
	}
}
