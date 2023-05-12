package sporemodder.file.cell;

import java.io.IOException;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.argscript.ParserUtils;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellGlobalsFile {
	public static final ArgScriptEnum ENUM_GAME_MODE = new ArgScriptEnum();
	static {
		ENUM_GAME_MODE.add(0, "0");
		ENUM_GAME_MODE.add(1, "1");
		ENUM_GAME_MODE.add(2, "2");
	}
	
	public static final ArgScriptEnum ENUM_CONTROL_METHOD = new ArgScriptEnum();
	static {
		ENUM_CONTROL_METHOD.add(0, "0");
		ENUM_CONTROL_METHOD.add(1, "1");
		ENUM_CONTROL_METHOD.add(2, "2");
	}
	
	public static final ArgScriptEnum ENUM_EDITOR_METHOD = new ArgScriptEnum();
	static {
		ENUM_EDITOR_METHOD.add(0, "0");
		ENUM_EDITOR_METHOD.add(1, "1");
	}
	
	public static final ArgScriptEnum ENUM_TUTORIAL_METHOD = new ArgScriptEnum();
	static {
		ENUM_TUTORIAL_METHOD.add(0, "0");
		ENUM_TUTORIAL_METHOD.add(1, "1");
		ENUM_TUTORIAL_METHOD.add(2, "2");
	}
	
	public static final ArgScriptEnum ENUM_ENDING_METHOD = new ArgScriptEnum();
	static {
		ENUM_ENDING_METHOD.add(0, "0");
		ENUM_ENDING_METHOD.add(1, "1");
	}
	
	public static final ArgScriptEnum ENUM_EYE_METHOD = new ArgScriptEnum();
	static {
		ENUM_EYE_METHOD.add(0, "0");
		ENUM_EYE_METHOD.add(1, "1");
	}
	
	public int gameMode;  // ENUM_GAME_MODE
	public int world_1;
	public int world_2;
	public int world_3;
	public int world_4;
	public int world_5;
	public int worldBackground_1;
	public int worldBackground_2;
	public int worldBackground_3;
	public int worldBackground_4;
	public int worldBackground_5;
	public int worldRandom;
	public int worldRandomBg;
	public int startCell;
	public int startingCellKey;
	public int effectMapEntry;
	public int backgroundMapEntry;
	public float flowMultiplier = 25.0f;
	public float npcSpeedMultiplier = 1.0f;
	public float npcTurnSpeedMultiplier_Jet = 1.0f;
	public float npcTurnSpeedMultiplier_Flagella = 1.0f;
	public float npcTurnSpeedMultiplier_Cilia = 1.0f;
	public float densityRock = 100.0f;
	public float densitySolid = 10.0f;
	public float densityLiquid = 1.0f;
	public float densityAir = 1.0f;
	public float backgroundDistance = 10.0f;
	public float minDragCollisionSpeed = 0.1f;
	public float minImpactCollisionSpeed = 1.0f;
	public float ciliaSpeedAsJet = 1.0f;
	public float flagellaSpeedAsJet = 1.0f;
	public float flagellaSpeedAsCilia = 1.0f;
	public float ciliaSpeedAsFlagella = 1.0f;
	public int keyLookAlgorithm;
	public float beachDistance = 20.0f;
	public float finishLineDistance = 20.0f;
	public float noPartSpeed = 1.0f;
	public float flagellaRampMinFactor = 0.5f;
	public float flagellaRampTime = 2.0f;
	public float flagellaRampResetAngle = 30.0f;
	public float flagellaTurnSpeedRampStart = 1.0f;
	public float flagellaTurnSpeedRampEnd = 2.0f;
	public float flagellaTurnSpeedMin = 3.0f;
	public float flagellaTurnSpeedMax = 10.0f;
	public float ciliaTurnSpeed = 0.75f;
	public float jetTurnSpeed = 3.0f;
	public float startLevelNoCreatureRadius = 10.0f;
	public float startLevelNoAnythingRadius = 3.0f;
	public int numHighLOD_FG = 10;
	public int numHighLOD_BG = 2;
	public float percentAnimalFood = 0.35f;
	public float percentPlantFood = 0.35f;
	public int field_208;
	public int controlMethod;  // ENUM_CONTROL_METHOD
	public int editorMethod;  // ENUM_EDITOR_METHOD
	public int tutorialMethod;  // ENUM_TUTORIAL_METHOD
	public int endingMethod;  // ENUM_ENDING_METHOD
	public int eyeMethod;  // ENUM_EYE_METHOD
	public float timeToGoldyCinematic = 15.0f;
	public float missionTime = 10.0f;
	public float missionResetTime = 3.0f;
	public float escapeMinDistance = 8.0f;
	public float escapeMaxDistance = 20.0f;
	public float escapeDelayMedium = 20.0f;
	public float escapeTimerHard = 20.0f;
	public float nonAnimatingCiliaMovementFactor = 20.0f;
	public float nonAnimatingJetMovementFactor = 20.0f;
	public float mateTriggerDistance = 4.0f;
	public float mateSpawnDistance = 1.0f;
	
	public void read(StreamReader stream) throws IOException
	{
		gameMode = stream.readLEInt();
		world_1 = stream.readLEInt();
		world_2 = stream.readLEInt();
		world_3 = stream.readLEInt();
		world_4 = stream.readLEInt();
		world_5 = stream.readLEInt();
		worldBackground_1 = stream.readLEInt();
		worldBackground_2 = stream.readLEInt();
		worldBackground_3 = stream.readLEInt();
		worldBackground_4 = stream.readLEInt();
		worldBackground_5 = stream.readLEInt();
		worldRandom = stream.readLEInt();
		worldRandomBg = stream.readLEInt();
		startCell = stream.readLEInt();
		startingCellKey = stream.readLEInt();
		effectMapEntry = stream.readLEInt();
		backgroundMapEntry = stream.readLEInt();
		flowMultiplier = stream.readLEFloat();
		npcSpeedMultiplier = stream.readLEFloat();
		npcTurnSpeedMultiplier_Jet = stream.readLEFloat();
		npcTurnSpeedMultiplier_Flagella = stream.readLEFloat();
		npcTurnSpeedMultiplier_Cilia = stream.readLEFloat();
		densityRock = stream.readLEFloat();
		densitySolid = stream.readLEFloat();
		densityLiquid = stream.readLEFloat();
		densityAir = stream.readLEFloat();
		backgroundDistance = stream.readLEFloat();
		minDragCollisionSpeed = stream.readLEFloat();
		minImpactCollisionSpeed = stream.readLEFloat();
		ciliaSpeedAsJet = stream.readLEFloat();
		flagellaSpeedAsJet = stream.readLEFloat();
		flagellaSpeedAsCilia = stream.readLEFloat();
		ciliaSpeedAsFlagella = stream.readLEFloat();
		keyLookAlgorithm = stream.readLEInt();
		beachDistance = stream.readLEFloat();
		finishLineDistance = stream.readLEFloat();
		noPartSpeed = stream.readLEFloat();
		flagellaRampMinFactor = stream.readLEFloat();
		flagellaRampTime = stream.readLEFloat();
		flagellaRampResetAngle = stream.readLEFloat();
		flagellaTurnSpeedRampStart = stream.readLEFloat();
		flagellaTurnSpeedRampEnd = stream.readLEFloat();
		flagellaTurnSpeedMin = stream.readLEFloat();
		flagellaTurnSpeedMax = stream.readLEFloat();
		ciliaTurnSpeed = stream.readLEFloat();
		jetTurnSpeed = stream.readLEFloat();
		startLevelNoCreatureRadius = stream.readLEFloat();
		startLevelNoAnythingRadius = stream.readLEFloat();
		numHighLOD_FG = stream.readLEInt();
		numHighLOD_BG = stream.readLEInt();
		percentAnimalFood = stream.readLEFloat();
		percentPlantFood = stream.readLEFloat();
		field_208 = stream.readLEInt();
		controlMethod = stream.readLEInt();
		editorMethod = stream.readLEInt();
		tutorialMethod = stream.readLEInt();
		endingMethod = stream.readLEInt();
		eyeMethod = stream.readLEInt();
		timeToGoldyCinematic = stream.readLEFloat();
		missionTime = stream.readLEFloat();
		missionResetTime = stream.readLEFloat();
		escapeMinDistance = stream.readLEFloat();
		escapeMaxDistance = stream.readLEFloat();
		escapeDelayMedium = stream.readLEFloat();
		escapeTimerHard = stream.readLEFloat();
		nonAnimatingCiliaMovementFactor = stream.readLEFloat();
		nonAnimatingJetMovementFactor = stream.readLEFloat();
		mateTriggerDistance = stream.readLEFloat();
		mateSpawnDistance = stream.readLEFloat();
		
		if (stream.getFilePointer() != stream.length()) {
			throw new IOException("Error: LOOTTABLE file still has data left, file pointer " + stream.getFilePointer());
		}
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(gameMode);
		stream.writeLEInt(world_1);
		stream.writeLEInt(world_2);
		stream.writeLEInt(world_3);
		stream.writeLEInt(world_4);
		stream.writeLEInt(world_5);
		stream.writeLEInt(worldBackground_1);
		stream.writeLEInt(worldBackground_2);
		stream.writeLEInt(worldBackground_3);
		stream.writeLEInt(worldBackground_4);
		stream.writeLEInt(worldBackground_5);
		stream.writeLEInt(worldRandom);
		stream.writeLEInt(worldRandomBg);
		stream.writeLEInt(startCell);
		stream.writeLEInt(startingCellKey);
		stream.writeLEInt(effectMapEntry);
		stream.writeLEInt(backgroundMapEntry);
		stream.writeLEFloat(flowMultiplier);
		stream.writeLEFloat(npcSpeedMultiplier);
		stream.writeLEFloat(npcTurnSpeedMultiplier_Jet);
		stream.writeLEFloat(npcTurnSpeedMultiplier_Flagella);
		stream.writeLEFloat(npcTurnSpeedMultiplier_Cilia);
		stream.writeLEFloat(densityRock);
		stream.writeLEFloat(densitySolid);
		stream.writeLEFloat(densityLiquid);
		stream.writeLEFloat(densityAir);
		stream.writeLEFloat(backgroundDistance);
		stream.writeLEFloat(minDragCollisionSpeed);
		stream.writeLEFloat(minImpactCollisionSpeed);
		stream.writeLEFloat(ciliaSpeedAsJet);
		stream.writeLEFloat(flagellaSpeedAsJet);
		stream.writeLEFloat(flagellaSpeedAsCilia);
		stream.writeLEFloat(ciliaSpeedAsFlagella);
		stream.writeLEInt(keyLookAlgorithm);
		stream.writeLEFloat(beachDistance);
		stream.writeLEFloat(finishLineDistance);
		stream.writeLEFloat(noPartSpeed);
		stream.writeLEFloat(flagellaRampMinFactor);
		stream.writeLEFloat(flagellaRampTime);
		stream.writeLEFloat(flagellaRampResetAngle);
		stream.writeLEFloat(flagellaTurnSpeedRampStart);
		stream.writeLEFloat(flagellaTurnSpeedRampEnd);
		stream.writeLEFloat(flagellaTurnSpeedMin);
		stream.writeLEFloat(flagellaTurnSpeedMax);
		stream.writeLEFloat(ciliaTurnSpeed);
		stream.writeLEFloat(jetTurnSpeed);
		stream.writeLEFloat(startLevelNoCreatureRadius);
		stream.writeLEFloat(startLevelNoAnythingRadius);
		stream.writeLEInt(numHighLOD_FG);
		stream.writeLEInt(numHighLOD_BG);
		stream.writeLEFloat(percentAnimalFood);
		stream.writeLEFloat(percentPlantFood);
		stream.writeLEInt(field_208);
		stream.writeLEInt(controlMethod);
		stream.writeLEInt(editorMethod);
		stream.writeLEInt(tutorialMethod);
		stream.writeLEInt(endingMethod);
		stream.writeLEInt(eyeMethod);
		stream.writeLEFloat(timeToGoldyCinematic);
		stream.writeLEFloat(missionTime);
		stream.writeLEFloat(missionResetTime);
		stream.writeLEFloat(escapeMinDistance);
		stream.writeLEFloat(escapeMaxDistance);
		stream.writeLEFloat(escapeDelayMedium);
		stream.writeLEFloat(escapeTimerHard);
		stream.writeLEFloat(nonAnimatingCiliaMovementFactor);
		stream.writeLEFloat(nonAnimatingJetMovementFactor);
		stream.writeLEFloat(mateTriggerDistance);
		stream.writeLEFloat(mateSpawnDistance);
	}
	
	public void clear() {
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("gameMode").arguments(ENUM_GAME_MODE.get(gameMode));
		writer.command("world_1").arguments(HashManager.get().getFileName(world_1));
		writer.command("world_2").arguments(HashManager.get().getFileName(world_2));
		writer.command("world_3").arguments(HashManager.get().getFileName(world_3));
		writer.command("world_4").arguments(HashManager.get().getFileName(world_4));
		writer.command("world_5").arguments(HashManager.get().getFileName(world_5));
		writer.command("worldBackground_1").arguments(HashManager.get().getFileName(worldBackground_1));
		writer.command("worldBackground_2").arguments(HashManager.get().getFileName(worldBackground_2));
		writer.command("worldBackground_3").arguments(HashManager.get().getFileName(worldBackground_3));
		writer.command("worldBackground_4").arguments(HashManager.get().getFileName(worldBackground_4));
		writer.command("worldBackground_5").arguments(HashManager.get().getFileName(worldBackground_5));
		writer.command("worldRandom").arguments(HashManager.get().getFileName(worldRandom));
		writer.command("worldRandomBg").arguments(HashManager.get().getFileName(worldRandomBg));
		writer.command("startCell").arguments(HashManager.get().getFileName(startCell));
		writer.command("startingCellKey").arguments(HashManager.get().getFileName(startingCellKey));
		writer.command("effectMapEntry").arguments(HashManager.get().getFileName(effectMapEntry));
		writer.command("backgroundMapEntry").arguments(HashManager.get().getFileName(backgroundMapEntry));
		writer.command("flowMultiplier").floats(flowMultiplier);
		writer.command("npcSpeedMultiplier").floats(npcSpeedMultiplier);
		writer.command("npcTurnSpeedMultiplier_Jet").floats(npcTurnSpeedMultiplier_Jet);
		writer.command("npcTurnSpeedMultiplier_Flagella").floats(npcTurnSpeedMultiplier_Flagella);
		writer.command("npcTurnSpeedMultiplier_Cilia").floats(npcTurnSpeedMultiplier_Cilia);
		writer.command("densityRock").floats(densityRock);
		writer.command("densitySolid").floats(densitySolid);
		writer.command("densityLiquid").floats(densityLiquid);
		writer.command("densityAir").floats(densityAir);
		writer.command("backgroundDistance").floats(backgroundDistance);
		writer.command("minDragCollisionSpeed").floats(minDragCollisionSpeed);
		writer.command("minImpactCollisionSpeed").floats(minImpactCollisionSpeed);
		writer.command("ciliaSpeedAsJet").floats(ciliaSpeedAsJet);
		writer.command("flagellaSpeedAsJet").floats(flagellaSpeedAsJet);
		writer.command("flagellaSpeedAsCilia").floats(flagellaSpeedAsCilia);
		writer.command("ciliaSpeedAsFlagella").floats(ciliaSpeedAsFlagella);
		writer.command("keyLookAlgorithm").arguments(HashManager.get().getFileName(keyLookAlgorithm));
		writer.command("beachDistance").floats(beachDistance);
		writer.command("finishLineDistance").floats(finishLineDistance);
		writer.command("noPartSpeed").floats(noPartSpeed);
		writer.command("flagellaRampMinFactor").floats(flagellaRampMinFactor);
		writer.command("flagellaRampTime").floats(flagellaRampTime);
		writer.command("flagellaRampResetAngle").floats(flagellaRampResetAngle);
		writer.command("flagellaTurnSpeedRampStart").floats(flagellaTurnSpeedRampStart);
		writer.command("flagellaTurnSpeedRampEnd").floats(flagellaTurnSpeedRampEnd);
		writer.command("flagellaTurnSpeedMin").floats(flagellaTurnSpeedMin);
		writer.command("flagellaTurnSpeedMax").floats(flagellaTurnSpeedMax);
		writer.command("ciliaTurnSpeed").floats(ciliaTurnSpeed);
		writer.command("jetTurnSpeed").floats(jetTurnSpeed);
		writer.command("startLevelNoCreatureRadius").floats(startLevelNoCreatureRadius);
		writer.command("startLevelNoAnythingRadius").floats(startLevelNoAnythingRadius);
		writer.command("numHighLOD_FG").ints(numHighLOD_FG);
		writer.command("numHighLOD_BG").ints(numHighLOD_BG);
		writer.command("percentAnimalFood").floats(percentAnimalFood);
		writer.command("percentPlantFood").floats(percentPlantFood);
		writer.command("field_208").ints(field_208);
		writer.command("controlMethod").arguments(ENUM_CONTROL_METHOD.get(controlMethod));
		writer.command("editorMethod").arguments(ENUM_EDITOR_METHOD.get(editorMethod));
		writer.command("tutorialMethod").arguments(ENUM_TUTORIAL_METHOD.get(tutorialMethod));
		writer.command("endingMethod").arguments(ENUM_ENDING_METHOD.get(endingMethod));
		writer.command("eyeMethod").arguments(ENUM_EYE_METHOD.get(eyeMethod));
		writer.command("timeToGoldyCinematic").floats(timeToGoldyCinematic);
		writer.command("missionTime").floats(missionTime);
		writer.command("missionResetTime").floats(missionResetTime);
		writer.command("escapeMinDistance").floats(escapeMinDistance);
		writer.command("escapeMaxDistance").floats(escapeMaxDistance);
		writer.command("escapeDelayMedium").floats(escapeDelayMedium);
		writer.command("escapeTimerHard").floats(escapeTimerHard);
		writer.command("nonAnimatingCiliaMovementFactor").floats(nonAnimatingCiliaMovementFactor);
		writer.command("nonAnimatingJetMovementFactor").floats(nonAnimatingJetMovementFactor);
		writer.command("mateTriggerDistance").floats(mateTriggerDistance);
		writer.command("mateSpawnDistance").floats(mateSpawnDistance);
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellGlobalsFile> generateStream() {
		ArgScriptStream<CellGlobalsFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		ParserUtils.createEnumParser("gameMode", stream, ENUM_GAME_MODE, value -> gameMode = value);
		ParserUtils.createFileIDParser("world_1", stream, value -> world_1 = value);
		ParserUtils.createFileIDParser("world_2", stream, value -> world_2 = value);
		ParserUtils.createFileIDParser("world_3", stream, value -> world_3 = value);
		ParserUtils.createFileIDParser("world_4", stream, value -> world_4 = value);
		ParserUtils.createFileIDParser("world_5", stream, value -> world_5 = value);
		ParserUtils.createFileIDParser("worldBackground_1", stream, value -> worldBackground_1 = value);
		ParserUtils.createFileIDParser("worldBackground_2", stream, value -> worldBackground_2 = value);
		ParserUtils.createFileIDParser("worldBackground_3", stream, value -> worldBackground_3 = value);
		ParserUtils.createFileIDParser("worldBackground_4", stream, value -> worldBackground_4 = value);
		ParserUtils.createFileIDParser("worldBackground_5", stream, value -> worldBackground_5 = value);
		ParserUtils.createFileIDParser("worldRandom", stream, value -> worldRandom = value);
		ParserUtils.createFileIDParser("worldRandomBg", stream, value -> worldRandomBg = value);
		ParserUtils.createFileIDParser("startCell", stream, value -> startCell = value);
		ParserUtils.createFileIDParser("startingCellKey", stream, value -> startingCellKey = value);
		ParserUtils.createFileIDParser("effectMapEntry", stream, value -> effectMapEntry = value);
		ParserUtils.createFileIDParser("backgroundMapEntry", stream, value -> backgroundMapEntry = value);
		ParserUtils.createFloatParser("flowMultiplier", stream, value -> flowMultiplier = value);
		ParserUtils.createFloatParser("npcSpeedMultiplier", stream, value -> npcSpeedMultiplier = value);
		ParserUtils.createFloatParser("npcTurnSpeedMultiplier_Jet", stream, value -> npcTurnSpeedMultiplier_Jet = value);
		ParserUtils.createFloatParser("npcTurnSpeedMultiplier_Flagella", stream, value -> npcTurnSpeedMultiplier_Flagella = value);
		ParserUtils.createFloatParser("npcTurnSpeedMultiplier_Cilia", stream, value -> npcTurnSpeedMultiplier_Cilia = value);
		ParserUtils.createFloatParser("densityRock", stream, value -> densityRock = value);
		ParserUtils.createFloatParser("densitySolid", stream, value -> densitySolid = value);
		ParserUtils.createFloatParser("densityLiquid", stream, value -> densityLiquid = value);
		ParserUtils.createFloatParser("densityAir", stream, value -> densityAir = value);
		ParserUtils.createFloatParser("backgroundDistance", stream, value -> backgroundDistance = value);
		ParserUtils.createFloatParser("minDragCollisionSpeed", stream, value -> minDragCollisionSpeed = value);
		ParserUtils.createFloatParser("minImpactCollisionSpeed", stream, value -> minImpactCollisionSpeed = value);
		ParserUtils.createFloatParser("ciliaSpeedAsJet", stream, value -> ciliaSpeedAsJet = value);
		ParserUtils.createFloatParser("flagellaSpeedAsJet", stream, value -> flagellaSpeedAsJet = value);
		ParserUtils.createFloatParser("flagellaSpeedAsCilia", stream, value -> flagellaSpeedAsCilia = value);
		ParserUtils.createFloatParser("ciliaSpeedAsFlagella", stream, value -> ciliaSpeedAsFlagella = value);
		ParserUtils.createFileIDParser("keyLookAlgorithm", stream, value -> keyLookAlgorithm = value);
		ParserUtils.createFloatParser("beachDistance", stream, value -> beachDistance = value);
		ParserUtils.createFloatParser("finishLineDistance", stream, value -> finishLineDistance = value);
		ParserUtils.createFloatParser("noPartSpeed", stream, value -> noPartSpeed = value);
		ParserUtils.createFloatParser("flagellaRampMinFactor", stream, value -> flagellaRampMinFactor = value);
		ParserUtils.createFloatParser("flagellaRampTime", stream, value -> flagellaRampTime = value);
		ParserUtils.createFloatParser("flagellaRampResetAngle", stream, value -> flagellaRampResetAngle = value);
		ParserUtils.createFloatParser("flagellaTurnSpeedRampStart", stream, value -> flagellaTurnSpeedRampStart = value);
		ParserUtils.createFloatParser("flagellaTurnSpeedRampEnd", stream, value -> flagellaTurnSpeedRampEnd = value);
		ParserUtils.createFloatParser("flagellaTurnSpeedMin", stream, value -> flagellaTurnSpeedMin = value);
		ParserUtils.createFloatParser("flagellaTurnSpeedMax", stream, value -> flagellaTurnSpeedMax = value);
		ParserUtils.createFloatParser("ciliaTurnSpeed", stream, value -> ciliaTurnSpeed = value);
		ParserUtils.createFloatParser("jetTurnSpeed", stream, value -> jetTurnSpeed = value);
		ParserUtils.createFloatParser("startLevelNoCreatureRadius", stream, value -> startLevelNoCreatureRadius = value);
		ParserUtils.createFloatParser("startLevelNoAnythingRadius", stream, value -> startLevelNoAnythingRadius = value);
		ParserUtils.createIntParser("numHighLOD_FG", stream, value -> numHighLOD_FG = value);
		ParserUtils.createIntParser("numHighLOD_BG", stream, value -> numHighLOD_BG = value);
		ParserUtils.createFloatParser("percentAnimalFood", stream, value -> percentAnimalFood = value);
		ParserUtils.createFloatParser("percentPlantFood", stream, value -> percentPlantFood = value);
		ParserUtils.createIntParser("field_208", stream, value -> field_208 = value);
		ParserUtils.createEnumParser("controlMethod", stream, ENUM_CONTROL_METHOD, value -> controlMethod = value);
		ParserUtils.createEnumParser("editorMethod", stream, ENUM_EDITOR_METHOD, value -> editorMethod = value);
		ParserUtils.createEnumParser("tutorialMethod", stream, ENUM_TUTORIAL_METHOD, value -> tutorialMethod = value);
		ParserUtils.createEnumParser("endingMethod", stream, ENUM_ENDING_METHOD, value -> endingMethod = value);
		ParserUtils.createEnumParser("eyeMethod", stream, ENUM_EYE_METHOD, value -> eyeMethod = value);
		ParserUtils.createFloatParser("timeToGoldyCinematic", stream, value -> timeToGoldyCinematic = value);
		ParserUtils.createFloatParser("missionTime", stream, value -> missionTime = value);
		ParserUtils.createFloatParser("missionResetTime", stream, value -> missionResetTime = value);
		ParserUtils.createFloatParser("escapeMinDistance", stream, value -> escapeMinDistance = value);
		ParserUtils.createFloatParser("escapeMaxDistance", stream, value -> escapeMaxDistance = value);
		ParserUtils.createFloatParser("escapeDelayMedium", stream, value -> escapeDelayMedium = value);
		ParserUtils.createFloatParser("escapeTimerHard", stream, value -> escapeTimerHard = value);
		ParserUtils.createFloatParser("nonAnimatingCiliaMovementFactor", stream, value -> nonAnimatingCiliaMovementFactor = value);
		ParserUtils.createFloatParser("nonAnimatingJetMovementFactor", stream, value -> nonAnimatingJetMovementFactor = value);
		ParserUtils.createFloatParser("mateTriggerDistance", stream, value -> mateTriggerDistance = value);
		ParserUtils.createFloatParser("mateSpawnDistance", stream, value -> mateSpawnDistance = value);
		
		return stream;
	}
}
