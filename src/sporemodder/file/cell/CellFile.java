package sporemodder.file.cell;

import java.io.IOException;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.argscript.ParserUtils;
import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class CellFile {
	public static final ArgScriptEnum ENUM_AI_TYPE = new ArgScriptEnum();
	static {
		ENUM_AI_TYPE.add(-1, "none");
		ENUM_AI_TYPE.add(0, "player");
		ENUM_AI_TYPE.add(0x1002, "straight");
		ENUM_AI_TYPE.add(0x1003, "slowSeek");
		ENUM_AI_TYPE.add(0x1004, "orbit");
		ENUM_AI_TYPE.add(0x1005, "chomp");
		ENUM_AI_TYPE.add(0x1006, "eater");
		ENUM_AI_TYPE.add(0x1007, "parasite");
		ENUM_AI_TYPE.add(0x1008, "caterpillar");
		ENUM_AI_TYPE.add(0x1009, "sleeper");
		ENUM_AI_TYPE.add(0x100A, "waterballoon");
		ENUM_AI_TYPE.add(0x100B, "mosquito");
		ENUM_AI_TYPE.add(0x100C, "dragon");
		ENUM_AI_TYPE.add(0x100D, "poolball");
		ENUM_AI_TYPE.add(0x100E, "leak");
		ENUM_AI_TYPE.add(0x1001, "parts");
	}
	
	public static final ArgScriptEnum ENUM_AI_MOVEMENT_STYLE = new ArgScriptEnum();
	static {
		ENUM_AI_MOVEMENT_STYLE.add(0, "default");
		ENUM_AI_MOVEMENT_STYLE.add(1, "flagella");
		ENUM_AI_MOVEMENT_STYLE.add(2, "cillia");
		ENUM_AI_MOVEMENT_STYLE.add(4, "jet");
		ENUM_AI_MOVEMENT_STYLE.add(8, "turn_in_place");
		ENUM_AI_MOVEMENT_STYLE.add(3, "flagella_cillia");
		ENUM_AI_MOVEMENT_STYLE.add(5, "flagella_jet");
		ENUM_AI_MOVEMENT_STYLE.add(6, "cillia_jet");
		ENUM_AI_MOVEMENT_STYLE.add(7, "flagella_cillia_jet");
	}
	
	public static final ArgScriptEnum ENUM_MEDIUM = new ArgScriptEnum();
	static {
		ENUM_MEDIUM.add(-1, "null");
		ENUM_MEDIUM.add(0, "none");
		ENUM_MEDIUM.add(3, "liquid");
		ENUM_MEDIUM.add(1, "rock");
		ENUM_MEDIUM.add(2, "solid");
		ENUM_MEDIUM.add(4, "air");
	}
	
	public static final ArgScriptEnum ENUM_UNLOCK_TYPE = new ArgScriptEnum();
	static {
		ENUM_UNLOCK_TYPE.add(0, "none");
		ENUM_UNLOCK_TYPE.add(12, "body");
		ENUM_UNLOCK_TYPE.add(2, "mandible");
		ENUM_UNLOCK_TYPE.add(3, "filter");
		ENUM_UNLOCK_TYPE.add(4, "proboscis");
		ENUM_UNLOCK_TYPE.add(8, "cilia");
		ENUM_UNLOCK_TYPE.add(9, "flagella");
		ENUM_UNLOCK_TYPE.add(10, "jet");
		ENUM_UNLOCK_TYPE.add(5, "chemical");
		ENUM_UNLOCK_TYPE.add(6, "electric");
		ENUM_UNLOCK_TYPE.add(7, "spike");
	}
	
	public static final ArgScriptEnum ENUM_DENSITY = new ArgScriptEnum();
	static {
		ENUM_DENSITY.add(0, "none");
		ENUM_DENSITY.add(2, "air");
		ENUM_DENSITY.add(1, "liquid");
		ENUM_DENSITY.add(3, "solid");
		ENUM_DENSITY.add(4, "rock");
	}
	
	public static final ArgScriptEnum ENUM_SOUND = new ArgScriptEnum();
	static {
		ENUM_SOUND.add(0, "none");
		ENUM_SOUND.add(2, "plant");
		ENUM_SOUND.add(3, "rock");
		ENUM_SOUND.add(1, "creature");
		ENUM_SOUND.add(4, "bubble");
	}
	
	public static class cLocalizedString
	{
		public String text;  // max 160 characters
		public int instanceID;  // in 0x04DC2F94.locale file
	}
	
	public static class cEatData
	{
		public int foodValue = 10;
		public int hpValue = 1;
		public boolean bomb;
		public boolean poisonNova;
	}
	
	// size 0x180
	public static class cAIData
	{
		public int type;  // ENUM_AI_TYPE
		public float awarenessRadius = 10.0f;
		public float awarenessRadiusFood;
		public float awarenessRadiusPredator;
		public int movementStyle;  // ENUM_AI_MOVEMENT_STYLE
		public boolean flocking;
		public float speed = 1.0f;
		public float chaseSpeed;
		public float wanderSpeed;
		public float fleeSpeed;
		public float fearsNearbyDamageRadius;
		public float fearsNearbyDeathRadius;
		public float fearsNearbyDamageTime;
		public float fearsNearbyDeathTime;
		public float protectRadius;
		public float protectTime;
		public float turnFactor = 1.0f;
		public boolean axialMovement;
		public float spawnTime;
		public float spawnRestTime;
		public int spawnOutput;  // loottable ID
		public float arcLength = 3.0f;
		public float arcLengthSecondary;
		public int numArcs = 3;
		public int keyTransformation;  // cell ID
		public int keyProjectile;  // cell ID
		public int food = 3;  // ENUM_MEDIUM
		public int growCount;
		public int digestionCount;
		public float digestionTime;
		public int digestionOutput;  // loottable ID
		public float fleeTime = 5.0f;
		public float fleeRestTime;
		public float chaseTime = 10000f;
		public float chaseRestTime;
		public boolean chasesDamage = true;
		public boolean fearsMouths = true;
		public boolean fearsWeapons = true;
		public boolean fearsElectric = true;
		public boolean fearsPoison = true;
		public boolean fearsDamage = true;
		public boolean ignoresFood;
		public float awakeTime;
		public float sleepTime;
		public int growAmount = 1;
		public float hatchDuration = 10.0f;
		public float poisonRecharge;
		public float electricRecharge = -1.0f;
		public float electricRechargeVsSmall = -1.0f;
		public float electricDischarge;
		
		public void read(StreamReader stream) throws IOException
		{
			type = stream.readLEInt();
			awarenessRadius = stream.readLEFloat();
			awarenessRadiusFood = stream.readLEFloat();
			awarenessRadiusPredator = stream.readLEFloat();
			movementStyle = stream.readLEInt();
			flocking = stream.readBoolean();
			stream.skip(3);
			speed = stream.readLEFloat();
			chaseSpeed = stream.readLEFloat();
			wanderSpeed = stream.readLEFloat();
			fleeSpeed = stream.readLEFloat();
			fearsNearbyDamageRadius = stream.readLEFloat();
			fearsNearbyDeathRadius = stream.readLEFloat();
			fearsNearbyDamageTime = stream.readLEFloat();
			fearsNearbyDeathTime = stream.readLEFloat();
			protectRadius = stream.readLEFloat();
			protectTime = stream.readLEFloat();
			turnFactor = stream.readLEFloat();
			axialMovement = stream.readBoolean();
			stream.skip(3);
			spawnTime = stream.readLEFloat();
			spawnRestTime = stream.readLEFloat();
			spawnOutput = stream.readLEInt();
			arcLength = stream.readLEFloat();
			arcLengthSecondary = stream.readLEFloat();
			numArcs = stream.readLEInt();
			keyTransformation = stream.readLEInt();
			keyProjectile = stream.readLEInt();
			food = stream.readLEInt();
			growCount = stream.readLEInt();
			digestionCount = stream.readLEInt();
			digestionTime = stream.readLEFloat();
			digestionOutput = stream.readLEInt();
			fleeTime = stream.readLEFloat();
			fleeRestTime = stream.readLEFloat();
			chaseTime = stream.readLEFloat();
			chaseRestTime = stream.readLEFloat();
			chasesDamage = stream.readBoolean();
			fearsMouths = stream.readBoolean();
			fearsWeapons = stream.readBoolean();
			fearsElectric = stream.readBoolean();
			fearsPoison = stream.readBoolean();
			fearsDamage = stream.readBoolean();
			ignoresFood = stream.readBoolean();
			stream.skip(1);
			awakeTime = stream.readLEFloat();
			sleepTime = stream.readLEFloat();
			growAmount = stream.readLEInt();
			hatchDuration = stream.readLEFloat();
			poisonRecharge = stream.readLEFloat();
			electricRecharge = stream.readLEFloat();
			electricRechargeVsSmall = stream.readLEFloat();
			electricDischarge = stream.readLEFloat();
		}
		
		public void write(StreamWriter stream) throws IOException
		{
			stream.writeLEInt(type);
			stream.writeLEFloat(awarenessRadius);
			stream.writeLEFloat(awarenessRadiusFood);
			stream.writeLEFloat(awarenessRadiusPredator);
			stream.writeLEInt(movementStyle);
			stream.writeBoolean(flocking);
			stream.writePadding(3);
			stream.writeLEFloat(speed);
			stream.writeLEFloat(chaseSpeed);
			stream.writeLEFloat(wanderSpeed);
			stream.writeLEFloat(fleeSpeed);
			stream.writeLEFloat(fearsNearbyDamageRadius);
			stream.writeLEFloat(fearsNearbyDeathRadius);
			stream.writeLEFloat(fearsNearbyDamageTime);
			stream.writeLEFloat(fearsNearbyDeathTime);
			stream.writeLEFloat(protectRadius);
			stream.writeLEFloat(protectTime);
			stream.writeLEFloat(turnFactor);
			stream.writeBoolean(axialMovement);
			stream.writePadding(3);
			stream.writeLEFloat(spawnTime);
			stream.writeLEFloat(spawnRestTime);
			stream.writeLEInt(spawnOutput);
			stream.writeLEFloat(arcLength);
			stream.writeLEFloat(arcLengthSecondary);
			stream.writeLEInt(numArcs);
			stream.writeLEInt(keyTransformation);
			stream.writeLEInt(keyProjectile);
			stream.writeLEInt(food);
			stream.writeLEInt(growCount);
			stream.writeLEInt(digestionCount);
			stream.writeLEFloat(digestionTime);
			stream.writeLEInt(digestionOutput);
			stream.writeLEFloat(fleeTime);
			stream.writeLEFloat(fleeRestTime);
			stream.writeLEFloat(chaseTime);
			stream.writeLEFloat(chaseRestTime);
			stream.writeBoolean(chasesDamage);
			stream.writeBoolean(fearsMouths);
			stream.writeBoolean(fearsWeapons);
			stream.writeBoolean(fearsElectric);
			stream.writeBoolean(fearsPoison);
			stream.writeBoolean(fearsDamage);
			stream.writeBoolean(ignoresFood);
			stream.writePadding(1);
			stream.writeLEFloat(awakeTime);
			stream.writeLEFloat(sleepTime);
			stream.writeLEInt(growAmount);
			stream.writeLEFloat(hatchDuration);
			stream.writeLEFloat(poisonRecharge);
			stream.writeLEFloat(electricRecharge);
			stream.writeLEFloat(electricRechargeVsSmall);
			stream.writeLEFloat(electricDischarge);
		}
		
		public void toArgScript(ArgScriptWriter writer) {
			writer.command("type").arguments(ENUM_AI_TYPE.get(type));
			writer.command("awarenessRadius").floats(awarenessRadius);
			writer.command("awarenessRadiusFood").floats(awarenessRadiusFood);
			writer.command("awarenessRadiusPredator").floats(awarenessRadiusPredator);
			writer.command("movementStyle").arguments(ENUM_AI_MOVEMENT_STYLE.get(movementStyle));
			writer.command("flocking").arguments(flocking);
			writer.command("speed").floats(speed);
			writer.command("chaseSpeed").floats(chaseSpeed);
			writer.command("wanderSpeed").floats(wanderSpeed);
			writer.command("fleeSpeed").floats(fleeSpeed);
			writer.command("fearsNearbyDamageRadius").floats(fearsNearbyDamageRadius);
			writer.command("fearsNearbyDeathRadius").floats(fearsNearbyDeathRadius);
			writer.command("fearsNearbyDamageTime").floats(fearsNearbyDamageTime);
			writer.command("fearsNearbyDeathTime").floats(fearsNearbyDeathTime);
			writer.command("protectRadius").floats(protectRadius);
			writer.command("protectTime").floats(protectTime);
			writer.command("turnFactor").floats(turnFactor);
			writer.command("axialMovement").arguments(axialMovement);
			writer.command("spawnTime").floats(spawnTime);
			writer.command("spawnRestTime").floats(spawnRestTime);
			writer.command("spawnOutput").arguments(HashManager.get().getFileName(spawnOutput));
			writer.command("arcLength").floats(arcLength);
			writer.command("numArcs").ints(numArcs);
			writer.command("keyTransformation").arguments(HashManager.get().getFileName(keyTransformation));
			writer.command("keyProjectile").arguments(HashManager.get().getFileName(keyProjectile));
			writer.command("food").arguments(ENUM_MEDIUM.get(food));
			writer.command("growCount").ints(growCount);
			writer.command("digestionCount").ints(digestionCount);
			writer.command("digestionTime").floats(digestionTime);
			writer.command("digestionOutput").arguments(HashManager.get().getFileName(digestionOutput));
			writer.command("fleeTime").floats(fleeTime);
			writer.command("fleeRestTime").floats(fleeRestTime);
			writer.command("chaseTime").floats(chaseTime);
			writer.command("chaseRestTime").floats(chaseRestTime);
			writer.command("chasesDamage").arguments(chasesDamage);
			writer.command("fearsMouths").arguments(fearsMouths);
			writer.command("fearsWeapons").arguments(fearsWeapons);
			writer.command("fearsElectric").arguments(fearsElectric);
			writer.command("fearsPoison").arguments(fearsPoison);
			writer.command("fearsDamage").arguments(fearsDamage);
			writer.command("ignoresFood").arguments(ignoresFood);
			writer.command("awakeTime").floats(awakeTime);
			writer.command("sleepTime").floats(sleepTime);
			writer.command("growAmount").ints(growAmount);
			writer.command("hatchDuration").floats(hatchDuration);
			writer.command("poisonRecharge").floats(poisonRecharge);
			writer.command("electricRecharge").floats(electricRecharge);
			writer.command("electricRechargeVsSmall").floats(electricRechargeVsSmall);
			writer.command("electricDischarge").floats(electricDischarge);
		}
	}
	
	public int structureID;
	public final cLocalizedString name = new cLocalizedString();
	public int hp = 2;
	public boolean fixedOrientation;
	public int flags;
	public int cellType;  // ENUM_MEDIUM
	public int unlockType;  // ENUM_UNLOCK_TYPE
	public int density;  // ENUM_DENSITY
	public int sound;  // ENUM_SOUND
	public int break_;  // cell ID
	public int pieces;  // lootTable ID
	public int leak;  // cell ID
	public int expel;  // cell ID
	public int explosionTable;  // lootTable ID
	public int loot;  // lootTable ID
	public int poison;  // cell ID
	public final cAIData ai = new cAIData();
	public final cAIData ai_hard = new cAIData();
	public final cAIData ai_easy = new cAIData();
	public int friendGroup;
	public boolean wontAttackPlayer;
	public boolean wontAttackPlayerWhenSmall;
	public final float[] size = new float[] {1.0f, 1.0f};
	public final cEatData eat = new cEatData();
	public boolean triggersEscapeMission;
	
	public void read(StreamReader stream) throws IOException
	{
		structureID = stream.readLEInt();
		name.text = stream.readString(StringEncoding.UTF16LE, 80);
		name.instanceID = stream.readLEInt();
		hp = stream.readLEInt();
		fixedOrientation = stream.readBoolean();
		stream.skip(3);
		flags = stream.readLEInt();
		cellType = stream.readLEInt();
		unlockType = stream.readLEInt();
		density = stream.readLEInt();
		sound = stream.readLEInt();
		break_ = stream.readLEInt();
		pieces = stream.readLEInt();
		leak = stream.readLEInt();
		expel = stream.readLEInt();
		loot = stream.readLEInt();
		explosionTable = stream.readLEInt();
		poison = stream.readLEInt();
		ai.read(stream);
		ai_hard.read(stream);
		ai_easy.read(stream);
		friendGroup = stream.readLEInt();
		wontAttackPlayer = stream.readBoolean();
		wontAttackPlayerWhenSmall = stream.readBoolean();
		stream.skip(2);
		stream.readLEFloats(size);
		eat.foodValue = stream.readLEInt();
		eat.hpValue = stream.readLEInt();
		eat.bomb = stream.readBoolean();
		eat.poisonNova = stream.readBoolean();
		stream.skip(2);
		triggersEscapeMission = stream.readBoolean();
		stream.skip(3);
	}
	
	public void write(StreamWriter stream) throws IOException
	{
		stream.writeLEInt(structureID);
		stream.writeString(name.text, StringEncoding.UTF16LE, 160);
		stream.writeLEInt(name.instanceID);
		stream.writeLEInt(hp);
		stream.writeLEInt(fixedOrientation ? 1 : 0);
		stream.writeLEInt(flags);
		stream.writeLEInt(cellType);
		stream.writeLEInt(unlockType);
		stream.writeLEInt(density);
		stream.writeLEInt(sound);
		stream.writeLEInt(break_);
		stream.writeLEInt(pieces);
		stream.writeLEInt(leak);
		stream.writeLEInt(expel);
		stream.writeLEInt(loot);
		stream.writeLEInt(explosionTable);
		stream.writeLEInt(poison);
		ai.write(stream);
		ai_hard.write(stream);
		ai_easy.write(stream);
		stream.writeLEInt(friendGroup);
		stream.writeBoolean(wontAttackPlayer);
		stream.writeBoolean(wontAttackPlayerWhenSmall);
		stream.writePadding(2);
		stream.writeLEFloats(size);
		stream.writeLEInt(eat.foodValue);
		stream.writeLEInt(eat.hpValue);
		stream.writeBoolean(eat.bomb);
		stream.writeBoolean(eat.poisonNova);
		stream.writePadding(2);
		stream.writeLEInt(triggersEscapeMission ? 1 : 0);
	}
	
	public void clear() {
		name.text = null;
		name.instanceID = 0;
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("structure").arguments(HashManager.get().getFileName(structureID));
		writer.command("name").literal(name.text);
		if (name.instanceID != 0) writer.option("locale").arguments(HashManager.get().getFileName(name.instanceID));
		writer.command("hp").ints(hp);
		writer.command("fixedOrientation").arguments(fixedOrientation);
		writer.command("flags").ints(flags);
		writer.command("cellType").arguments(ENUM_MEDIUM.get(cellType));
		writer.command("unlockType").arguments(ENUM_UNLOCK_TYPE.get(unlockType));
		writer.command("density").arguments(ENUM_DENSITY.get(density));
		writer.command("sound").arguments(ENUM_SOUND.get(sound));
		writer.command("break").arguments(HashManager.get().getFileName(break_));
		writer.command("pieces").arguments(HashManager.get().getFileName(pieces));
		writer.command("leak").arguments(HashManager.get().getFileName(leak));
		writer.command("expel").arguments(HashManager.get().getFileName(expel));
		writer.command("explosionTable").arguments(HashManager.get().getFileName(explosionTable));
		writer.command("loot").arguments(HashManager.get().getFileName(loot));
		writer.command("poison").arguments(HashManager.get().getFileName(poison));
		writer.command("friendGroup").ints(friendGroup);
		writer.command("wontAttackPlayer").arguments(wontAttackPlayer);
		writer.command("wontAttackPlayerWhenSmall").arguments(wontAttackPlayerWhenSmall);
		writer.command("size").floats(size);
		writer.command("triggersEscapeMission").arguments(triggersEscapeMission);
		writer.command("eat").option("foodValue").ints(eat.foodValue).option("hpValue").ints(eat.hpValue).option("bomb").arguments(eat.bomb).option("poisonNova").arguments(eat.poisonNova);
		
		writer.blankLine();
		writer.command("ai").startBlock();
		ai.toArgScript(writer);
		writer.endBlock().commandEND();
		
		writer.blankLine();
		writer.command("ai_hard").startBlock();
		ai_hard.toArgScript(writer);
		writer.endBlock().commandEND();
		
		writer.blankLine();
		writer.command("ai_easy").startBlock();
		ai_easy.toArgScript(writer);
		writer.endBlock().commandEND();
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public ArgScriptStream<CellFile> generateStream() {
		ArgScriptStream<CellFile> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		ParserUtils.createFileIDParser("structure", stream, value -> structureID = value);
		ParserUtils.createIntParser("hp", stream, value -> hp = value);
		ParserUtils.createBooleanParser("fixedOrientation", stream, value -> fixedOrientation = value);
		ParserUtils.createIntParser("flags", stream, value -> flags = value);
		ParserUtils.createEnumParser("cellType", stream, ENUM_MEDIUM, value -> cellType = value);
		ParserUtils.createEnumParser("unlockType", stream, ENUM_UNLOCK_TYPE, value -> unlockType = value);
		ParserUtils.createEnumParser("density", stream, ENUM_DENSITY, value -> density = value);
		ParserUtils.createEnumParser("sound", stream, ENUM_SOUND, value -> sound = value);
		ParserUtils.createFileIDParser("break", stream, value -> break_ = value);
		ParserUtils.createFileIDParser("pieces", stream, value -> pieces = value);
		ParserUtils.createFileIDParser("leak", stream, value -> leak = value);
		ParserUtils.createFileIDParser("expel", stream, value -> expel = value);
		ParserUtils.createFileIDParser("explosionTable", stream, value -> explosionTable = value);
		ParserUtils.createFileIDParser("loot", stream, value -> loot = value);
		ParserUtils.createFileIDParser("poison", stream, value -> poison = value);
		ParserUtils.createIntParser("friendGroup", stream, value -> friendGroup = value);
		ParserUtils.createBooleanParser("wontAttackPlayer", stream, value -> wontAttackPlayer = value);
		ParserUtils.createBooleanParser("wontAttackPlayerWhenSmall", stream, value -> wontAttackPlayerWhenSmall = value);
		ParserUtils.createBooleanParser("triggersEscapeMission", stream, value -> triggersEscapeMission = value);
		
		stream.addParser("size", ArgScriptParser.create((parser_, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 2)) {
				stream.parseFloats(args, size);
			}
		}));
		
		stream.addParser("name", ArgScriptParser.create((parser_, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				name.text = args.get(0);
			}
			if (line.getOptionArguments(args, "locale", 1)) {
				name.instanceID = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
			}
		}));
		
		stream.addParser("eat", ArgScriptParser.create((parser_, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getOptionArguments(args, "foodValue", 1)) {
				eat.foodValue = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
			}
			if (line.getOptionArguments(args, "hpValue", 1)) {
				eat.hpValue = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
			}
			if (line.getOptionArguments(args, "bomb", 1)) {
				eat.bomb = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
			}
			if (line.getOptionArguments(args, "poisonNova", 1)) {
				eat.poisonNova = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
			}
		}));
		
		stream.addParser("ai", new AiBlock(ai));
		stream.addParser("ai_hard", new AiBlock(ai_hard));
		stream.addParser("ai_easy", new AiBlock(ai_easy));
		
		return stream;
	}
	
	private static class AiBlock extends ArgScriptBlock<CellFile>
	{
		cAIData data;
		
		public AiBlock(cAIData data) {
			this.data = data;
		}
		
		@Override
		public void parse(ArgScriptLine line) {
			stream.startBlock(this);
		}
		
		@Override
		public void setData(ArgScriptStream<CellFile> stream, CellFile data_) {
			super.setData(stream, data_);
		
			ParserUtils.createEnumParser("type", this, ENUM_AI_TYPE, value -> data.type = value);
			ParserUtils.createFloatParser("awarenessRadius", this, value -> data.awarenessRadius = value);
			ParserUtils.createFloatParser("awarenessRadiusFood", this, value -> data.awarenessRadiusFood = value);
			ParserUtils.createFloatParser("awarenessRadiusPredator", this, value -> data.awarenessRadiusPredator = value);
			ParserUtils.createEnumParser("movementStyle", this, ENUM_AI_MOVEMENT_STYLE, value -> data.movementStyle = value);
			ParserUtils.createBooleanParser("flocking", this, value -> data.flocking = value);
			ParserUtils.createFloatParser("speed", this, value -> data.speed = value);
			ParserUtils.createFloatParser("chaseSpeed", this, value -> data.chaseSpeed = value);
			ParserUtils.createFloatParser("wanderSpeed", this, value -> data.wanderSpeed = value);
			ParserUtils.createFloatParser("fleeSpeed", this, value -> data.fleeSpeed = value);
			ParserUtils.createFloatParser("fearsNearbyDamageRadius", this, value -> data.fearsNearbyDamageRadius = value);
			ParserUtils.createFloatParser("fearsNearbyDeathRadius", this, value -> data.fearsNearbyDeathRadius = value);
			ParserUtils.createFloatParser("fearsNearbyDamageTime", this, value -> data.fearsNearbyDamageTime = value);
			ParserUtils.createFloatParser("fearsNearbyDeathTime", this, value -> data.fearsNearbyDeathTime = value);
			ParserUtils.createFloatParser("protectRadius", this, value -> data.protectRadius = value);
			ParserUtils.createFloatParser("protectTime", this, value -> data.protectTime = value);
			ParserUtils.createFloatParser("turnFactor", this, value -> data.turnFactor = value);
			ParserUtils.createBooleanParser("axialMovement", this, value -> data.axialMovement = value);
			ParserUtils.createFloatParser("spawnTime", this, value -> data.spawnTime = value);
			ParserUtils.createFloatParser("spawnRestTime", this, value -> data.spawnRestTime = value);
			ParserUtils.createFileIDParser("spawnOutput", this, value -> data.spawnOutput = value);
			ParserUtils.createFloatParser("arcLength", this, value -> data.arcLength = value);
			ParserUtils.createIntParser("numArcs", this, value -> data.numArcs = value);
			ParserUtils.createFileIDParser("keyTransformation", this, value -> data.keyTransformation = value);
			ParserUtils.createFileIDParser("keyProjectile", this, value -> data.keyProjectile = value);
			ParserUtils.createEnumParser("food", this, ENUM_MEDIUM, value -> data.food = value);
			ParserUtils.createIntParser("growCount", this, value -> data.growCount = value);
			ParserUtils.createIntParser("digestionCount", this, value -> data.digestionCount = value);
			ParserUtils.createFloatParser("digestionTime", this, value -> data.digestionTime = value);
			ParserUtils.createFileIDParser("digestionOutput", this, value -> data.digestionOutput = value);
			ParserUtils.createFloatParser("fleeTime", this, value -> data.fleeTime = value);
			ParserUtils.createFloatParser("fleeRestTime", this, value -> data.fleeRestTime = value);
			ParserUtils.createFloatParser("chaseTime", this, value -> data.chaseTime = value);
			ParserUtils.createFloatParser("chaseRestTime", this, value -> data.chaseRestTime = value);
			ParserUtils.createBooleanParser("chasesDamage", this, value -> data.chasesDamage = value);
			ParserUtils.createBooleanParser("fearsMouths", this, value -> data.fearsMouths = value);
			ParserUtils.createBooleanParser("fearsWeapons", this, value -> data.fearsWeapons = value);
			ParserUtils.createBooleanParser("fearsElectric", this, value -> data.fearsElectric = value);
			ParserUtils.createBooleanParser("fearsPoison", this, value -> data.fearsPoison = value);
			ParserUtils.createBooleanParser("fearsDamage", this, value -> data.fearsDamage = value);
			ParserUtils.createBooleanParser("ignoresFood", this, value -> data.ignoresFood = value);
			ParserUtils.createFloatParser("awakeTime", this, value -> data.awakeTime = value);
			ParserUtils.createFloatParser("sleepTime", this, value -> data.sleepTime = value);
			ParserUtils.createIntParser("growAmount", this, value -> data.growAmount = value);
			ParserUtils.createFloatParser("hatchDuration", this, value -> data.hatchDuration = value);
			ParserUtils.createFloatParser("poisonRecharge", this, value -> data.poisonRecharge = value);
			ParserUtils.createFloatParser("electricRecharge", this, value -> data.electricRecharge = value);
			ParserUtils.createFloatParser("electricRechargeVsSmall", this, value -> data.electricRechargeVsSmall = value);
			ParserUtils.createFloatParser("electricDischarge", this, value -> data.electricDischarge = value);
		}
	}
}
