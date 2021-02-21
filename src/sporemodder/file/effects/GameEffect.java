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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

@Structure(StructureEndian.BIG_ENDIAN)
public class GameEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<GameEffect> STRUCTURE_METADATA = StructureMetadata.generate(GameEffect.class);
	
	public static final String KEYWORD = "game";
	public static final int TYPE_CODE = 0x000B;
	
	public static final EffectComponentFactory FACTORY = new Factory();

	public int flags;  // & 0x3FF ?
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
			
			if (line.getOptionArguments(args, "id", 1) && 
					(value = stream.parseFileID(args, 0)) != null) {
				effect.messageID = value.intValue();
			}
			
			if (line.getOptionArguments(args, "life", 1) && 
					(value = stream.parseFloat(args, 0)) != null) {
				effect.life = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "string", 1)) 
			{
				effect.messageString = args.get(0);
			}
			
			if (line.getOptionArguments(args, "data", 1, 4)) 
			{
				stream.parseInts(args, effect.messageData);
			}
			
			if (line.getOptionArguments(args, "flags", 1) && 
					(value = stream.parseInt(args, 0)) != null) {
				effect.flags = value.intValue();
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
		
		writer.command(KEYWORD).option("id").arguments(hasher.getFileName(messageID));
		
		if (life != 0) writer.option("life").floats(life);
		
		if (messageData[0] != 0 || messageData[1] != 0 || messageData[2] != 0 || messageData[3] != 0) {
			
			writer.option("data").arguments(hasher.hexToString(messageData[0]),
					hasher.hexToString(messageData[1]),
					hasher.hexToString(messageData[2]),
					hasher.hexToString(messageData[3]));
		}
		
		if (messageString != null && !messageString.isEmpty()) writer.option("string").arguments(messageString);
		
		if (flags != 0) writer.option("flags").arguments(hasher.hexToString(flags));
	}
}
