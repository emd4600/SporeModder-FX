/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.effects;

import java.io.IOException;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public class GameEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<GameEffect> STRUCTURE_METADATA = StructureMetadata.generate(GameEffect.class);
	
	public static final String KEYWORD = "game";
	public static final int TYPE_CODE = 0x000B;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_PAUSE_SIM = 0x1;  // 1 << 0
	public static final int FLAGS_PAUSE_SIM_HIDDEN = 0x2;  // 1 << 1
	public static final int FLAGS_PAUSE_CLOCK = 0x4;  // 1 << 2
	public static final int FLAGS_MESSAGE_ON_START = 0x8;  // 1 << 3
	public static final int FLAGS_MESSAGE_ON_STOP = 0x10;  // 1 << 4
	public static final int FLAGS_MESSAGE_DATA1 = 0x20;  // 1 << 5
	public static final int FLAGS_MESSAGE_DATA2 = 0x40;  // 1 << 6
	public static final int FLAGS_MESSAGE_DATA3 = 0x80;  // 1 << 7
	public static final int FLAGS_MESSAGE_DATA4 = 0x100;  // 1 << 8
	public static final int FLAGS_MESSAGE_STRING = 0x200;  // 1 << 9
	
	public static final int MASK_FLAGS = FLAGS_PAUSE_SIM | FLAGS_PAUSE_SIM_HIDDEN |
			FLAGS_PAUSE_CLOCK | FLAGS_MESSAGE_ON_START | FLAGS_MESSAGE_ON_STOP | FLAGS_MESSAGE_DATA1 |
			FLAGS_MESSAGE_DATA2 | FLAGS_MESSAGE_DATA3 | FLAGS_MESSAGE_DATA4 | FLAGS_MESSAGE_STRING;

	public int flags;  // & 0x3FF
	public int messageID;
	public final int[] messageData = new int[4];
	public String messageString = "";
	public float life;
	
	@Override public void copy(EffectComponent _effect) {
		GameEffect effect = (GameEffect) _effect;
		
		flags = effect.flags;
		messageID = effect.messageID;
		messageString = effect.messageString;
		life = effect.life;
		EffectDirectory.copyArray(messageData, effect.messageData);
	}
	
	public GameEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	// We add it just to warn the user that only the anonymous version is supported
	protected static class Parser extends ArgScriptBlock<EffectUnit> {
		@Override
		public void parse(ArgScriptLine line) {
			stream.addError(line.createError(String.format("Only anonymous version of '%s' effects is supported.", KEYWORD)));
		}
	}
		
	protected static class GroupParser extends ArgScriptParser<EffectUnit> {
		
		@Override
		public void parse(ArgScriptLine line) {
			GameEffect effect = new GameEffect(stream.getData().getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
			Number value = null;
			
			// It must not have any arguments
			line.getArguments(args, 0);
			
			if (line.getOptionArguments(args, "life", 1) && 
					(value = stream.parseFloat(args, 0)) != null) {
				effect.life = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "pauseSim", 1)) {
				effect.flags |= FLAGS_PAUSE_SIM;
				effect.life = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
			}
			else if (line.getOptionArguments(args, "pauseSimHidden", 1)) {
				effect.flags |= FLAGS_PAUSE_SIM_HIDDEN;
				effect.life = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
			}
			else if (line.getOptionArguments(args, "pauseClock", 1)) {
				effect.flags |= FLAGS_PAUSE_CLOCK;
				effect.life = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0f);
			}
			else if (line.getOptionArguments(args, "message", 1)) {
				effect.messageID = Optional.ofNullable(stream.parseFileID(args, 0)).orElse(0);
				
				if (line.getOptionArguments(args, "data1", 1)) {
					effect.flags |= FLAGS_MESSAGE_DATA1;
					effect.messageData[0] = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
				if (line.getOptionArguments(args, "data2", 1)) {
					effect.flags |= FLAGS_MESSAGE_DATA2;
					effect.messageData[1] = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
				if (line.getOptionArguments(args, "data3", 1)) {
					effect.flags |= FLAGS_MESSAGE_DATA3;
					effect.messageData[2] = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
				if (line.getOptionArguments(args, "data4", 1)) {
					effect.flags |= FLAGS_MESSAGE_DATA4;
					effect.messageData[3] = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
				}
				if (line.getOptionArguments(args, "string", 1)) {
					effect.flags |= FLAGS_MESSAGE_STRING;
					effect.messageString = args.get(0);
				}
				
				if (line.hasOption("onStop")) {
					effect.flags |= FLAGS_MESSAGE_ON_STOP;
				}
				else {
					effect.flags |= FLAGS_MESSAGE_ON_START;
				}
			}
			else {
				DocumentError error = line.createError("'game' effect must specify one of: '-message', '-pauseSim', '-pauseSimHidden', or '-pauseClock'");
				stream.addError(error);
			}
			

			// Add it to the effect
			VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
			block.blockType = TYPE_CODE;
			block.parse(stream, line, GameEffect.class);
			
			data.addComponent(effect.toString(), effect);
			data.getCurrentEffect().blocks.add(block);
			block.component = effect;
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public String getKeyword() {
			return KEYWORD;
		}
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public int getMinVersion() {
			return 1;
		}

		@Override
		public int getMaxVersion() {
			return 1;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, new GroupParser());
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new GameEffect(effectDirectory, version);
		}

		@Override
		public boolean onlySupportsInline() {
			return true;
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		STRUCTURE_METADATA.read(this, stream);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		STRUCTURE_METADATA.write(this, stream);
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		HashManager hasher = HashManager.get();
		
		writer.command(KEYWORD);
		
		if ((flags & FLAGS_PAUSE_SIM) != 0) writer.option("pauseSim").floats(life);
		if ((flags & FLAGS_PAUSE_SIM_HIDDEN) != 0) writer.option("pauseSimHidden").floats(life);
		if ((flags & FLAGS_PAUSE_CLOCK) != 0) writer.option("pauseClock").floats(life);
		
		if ((flags & FLAGS_MESSAGE_ON_START) != 0 || (flags & FLAGS_MESSAGE_ON_STOP) != 0) {
			writer.option("message").arguments(hasher.getFileName(messageID));
			
			if ((flags & FLAGS_MESSAGE_DATA1) != 0) 
				writer.option("data1").arguments(hasher.formatInt32(messageData[0]));
			
			if ((flags & FLAGS_MESSAGE_DATA2) != 0) 
				writer.option("data2").arguments(hasher.formatInt32(messageData[1]));
			
			if ((flags & FLAGS_MESSAGE_DATA3) != 0) 
				writer.option("data3").arguments(hasher.formatInt32(messageData[2]));
			
			if ((flags & FLAGS_MESSAGE_DATA4) != 0) 
				writer.option("data4").arguments(hasher.formatInt32(messageData[3]));
			
			if ((flags & FLAGS_MESSAGE_STRING) != 0) 
				writer.option("string").literal(messageString);
			
			if ((flags & FLAGS_MESSAGE_ON_STOP) != 0)
				writer.option("onStop");
		}
		
		int maskedFlags = flags & ~MASK_FLAGS;
		if (maskedFlags != 0) {
			writer.option("flags").arguments(HashManager.get().hexToString(maskedFlags));
		}
	}
}
