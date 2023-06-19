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

import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class TerrainScriptEffect extends EffectComponent {
	
	public static final String KEYWORD = "terrainScript";
	public static final int TYPE_CODE = 0x0021;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAG_LOOP = 1;
	public static final int FLAG_HARDSTART = 2;
	public static final int FLAG_HARDSTOP = 4;
	public static final int FLAG_NOSTOP = 8;
	
	public int flags;  // & 0xF
	public final ResourceKey key = new ResourceKey();
	
	public TerrainScriptEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		TerrainScriptEffect effect = (TerrainScriptEffect) _effect;
		
		flags = effect.flags;
		key.copy(effect.key);
	}
	
	@Override public void read(StreamReader stream) throws IOException {
		flags = stream.readInt();
		key.readBE(stream);
	}
	
	@Override public void write(StreamWriter stream) throws IOException {
		stream.writeInt(flags);
		key.writeBE(stream);
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
			TerrainScriptEffect effect = new TerrainScriptEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
			
			if (line.getArguments(args, 1)) {
				String[] words = new String[2];
				effect.key.parse(args, 0, words);
				args.addHyperlink(EffectDirectory.HYPERLINK_FILE, words, 0);
			}
			
			if (line.hasFlag("loop")) effect.flags |= FLAG_LOOP;
			if (line.hasFlag("noOverlap")) effect.flags |= FLAG_HARDSTOP;
			if (line.hasFlag("hardStop")) effect.flags |= FLAG_HARDSTOP;
			if (line.hasFlag("hardStart")) effect.flags |= FLAG_HARDSTART;
			if (line.hasFlag("noStop")) effect.flags |= FLAG_NOSTOP;
			if (line.hasFlag("overlap")) effect.flags |= FLAG_NOSTOP; 
			
			// Add it to the effect
			VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
			block.blockType = TYPE_CODE;
			block.parse(stream, line, TYPE_CODE);
	
			block.component = effect;
			data.addComponent(effect.toString(), effect);
			data.getCurrentEffect().blocks.add(block);
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return TerrainScriptEffect.class;
		}
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
		public boolean onlySupportsInline() {
			return true;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new TerrainScriptEffect(effectDirectory, version);
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(key);
		
		writer.flag("loop", (flags & FLAG_LOOP) == FLAG_LOOP);
		writer.flag("hardStop", (flags & FLAG_HARDSTOP) == FLAG_HARDSTOP);
		writer.flag("hardStart", (flags & FLAG_HARDSTART) == FLAG_HARDSTART);
		writer.flag("noStop", (flags & FLAG_NOSTOP) == FLAG_NOSTOP);
	}
}
