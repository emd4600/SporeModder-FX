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
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class SplitControllerEffect extends EffectComponent {

	public static final String KEYWORD = "splitController";
	public static final int TYPE_CODE = 0x0029;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public SplitControllerEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
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
			stream.addParser(KEYWORD, new ArgScriptBlock<EffectUnit>() {
				@Override
				public void parse(ArgScriptLine line) {
					stream.addError(line.createError(String.format("Only anonymous version of '%s' effects is supported.", KEYWORD)));
				}
			});
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, ArgScriptParser.create((parser, line) -> {
				// Ensure there are no arguments
				ArgScriptArguments args = new ArgScriptArguments();
				line.getArguments(args, 0);
				
				// Add it to the effect
				VisualEffectBlock block = new VisualEffectBlock(parser.getData().getEffectDirectory());
				block.blockType = TYPE_CODE;
				block.parse(parser.getStream(), line, SplitControllerEffect.class);
				
				parser.getData().getCurrentEffect().blocks.add(block);
			}));
		}

		@Override
		public boolean onlySupportsInline() {
			return true;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new SplitControllerEffect(effectDirectory, version);
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void read(StreamReader stream) throws IOException {
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD);
	}
}
