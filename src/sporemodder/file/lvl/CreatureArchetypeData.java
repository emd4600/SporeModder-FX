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

public class CreatureArchetypeData extends GameplayMarkerData {
	
	public static final int TYPE = 0x91FE517B;  // CreatureArchetype
	
	public static final ArgScriptEnum NEST_TYPE_ENUM = new ArgScriptEnum();
	static {
		NEST_TYPE_ENUM.add(0, "Sandy");
		NEST_TYPE_ENUM.add(1, "Grassy");
		NEST_TYPE_ENUM.add(2, "Rocky");
	}
	
	public static final ArgScriptEnum PERSONALITY_ENUM = new ArgScriptEnum();
	static {
		PERSONALITY_ENUM.add(0, "None");
		PERSONALITY_ENUM.add(1, "EpicPredator");
		PERSONALITY_ENUM.add(2, "Migrator");
		PERSONALITY_ENUM.add(3, "Decorator");
		PERSONALITY_ENUM.add(4, "Monkey");
		PERSONALITY_ENUM.add(5, "Stalker");
		PERSONALITY_ENUM.add(6, "Guard");
		PERSONALITY_ENUM.add(7, "Pet");
		PERSONALITY_ENUM.add(8, "WaterPredator");
		PERSONALITY_ENUM.add(9, "Carcass");
	}
	
	public int nestType = 1;
	public int overrideHerdSize;
	public int personality;
	public boolean withoutNest;
	public float scaleMultiplier;
	public float hitpointOverride;
	public float damageMultiplier;
	public float territoryRadius;
	public int activateAtBrainLevel;
	public int deactivateAboveBrainLevel = 5;

	public CreatureArchetypeData(GameplayMarker marker) {
		super(marker);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		nestType = stream.readLEInt();
		overrideHerdSize = stream.readLEInt();
		personality = stream.readLEInt();
		withoutNest = stream.readBoolean();
		stream.skip(3);
		scaleMultiplier = stream.readLEFloat();
		hitpointOverride = stream.readLEFloat();
		damageMultiplier = stream.readLEFloat();
		territoryRadius = stream.readLEFloat();
		activateAtBrainLevel = stream.readLEInt();
		deactivateAboveBrainLevel = stream.readLEInt();
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(nestType);
		stream.writeLEInt(overrideHerdSize);
		stream.writeLEInt(personality);
		stream.writeBoolean(withoutNest);
		stream.writePadding(3);
		stream.writeLEFloat(scaleMultiplier);
		stream.writeLEFloat(hitpointOverride);
		stream.writeLEFloat(damageMultiplier);
		stream.writeLEFloat(territoryRadius);
		stream.writeLEInt(activateAtBrainLevel);
		stream.writeLEInt(deactivateAboveBrainLevel);
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		if (nestType != 1) writer.command("nestType").arguments(NEST_TYPE_ENUM.get(nestType));
		if (overrideHerdSize != 0) writer.command("overrideHerdSize").ints(overrideHerdSize);
		if (personality != 0) writer.command("personality").arguments(PERSONALITY_ENUM.get(personality));
		if (withoutNest != false) writer.command("withoutNest").arguments(withoutNest);
		if (scaleMultiplier != 0.0) writer.command("scaleMultiplier").floats(scaleMultiplier);
		if (hitpointOverride != 0.0) writer.command("hitpointOverride").floats(hitpointOverride);
		if (damageMultiplier != 0.0) writer.command("damageMultiplier").floats(damageMultiplier);
		if (territoryRadius != 0.0) writer.command("territoryRadius").floats(territoryRadius);
		if (activateAtBrainLevel != 0) writer.command("activateAtBrainLevel").ints(activateAtBrainLevel);
		if (deactivateAboveBrainLevel != 5) writer.command("deactivateAboveBrainLevel").ints(deactivateAboveBrainLevel);
	}

	@Override
	public void addParsers(ArgScriptBlock<LevelDefinition> block, ArgScriptStream<LevelDefinition> stream) {
		final ArgScriptArguments args = new ArgScriptArguments();
		
		block.addParser("nestType", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				nestType = NEST_TYPE_ENUM.get(args, 0);
			}
		}));
		
		block.addParser("overrideHerdSize", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				overrideHerdSize = value.intValue();
			}
		}));
		
		block.addParser("personality", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				personality = PERSONALITY_ENUM.get(args, 0);
			}
		}));
		
		block.addParser("withoutNest", ArgScriptParser.create((parser, line) -> {
			Boolean value = null;
			if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
				withoutNest = value.booleanValue();
			}
		}));
		
		block.addParser("scaleMultiplier", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				scaleMultiplier = value.floatValue();
			}
		}));
		
		block.addParser("hitpointOverride", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				hitpointOverride = value.floatValue();
			}
		}));
		
		block.addParser("damageMultiplier", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				damageMultiplier = value.floatValue();
			}
		}));
		
		block.addParser("territoryRadius", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				territoryRadius = value.floatValue();
			}
		}));
		
		block.addParser("activateAtBrainLevel", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				activateAtBrainLevel = value.intValue();
			}
		}));
		
		block.addParser("deactivateAboveBrainLevel", ArgScriptParser.create((parser, line) -> {
			Number value = null;
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				deactivateAboveBrainLevel = value.intValue();
			}
		}));
	}

}
