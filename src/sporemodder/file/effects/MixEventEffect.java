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
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class MixEventEffect extends EffectComponent {
	
	public static final String KEYWORD = "mixEvent";
	public static final int TYPE_CODE = 0x002D;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public int field_8;
	public float field_C = 1.0f;
	public float field_10;
	
	public MixEventEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		MixEventEffect effect = (MixEventEffect) _effect;
		
		field_8 = effect.field_8;
		field_C = effect.field_C;
		field_10 = effect.field_10;
	}

	@Override public void read(StreamReader in) throws IOException {
		field_8 = in.readInt();
		field_C = in.readFloat();
		if (version >= 2) {
			field_10 = in.readFloat();
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(field_8);
		out.writeFloat(field_C);
		if (version >= 2) {
			out.writeFloat(field_10);
		}
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
			MixEventEffect effect = new MixEventEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
	
			if (line.getArguments(args, 1, 3)) {
				Number value = null;
				
				if ((value = stream.parseFileID(args, 0)) != null) {
					effect.field_8 = value.intValue();
				}
				
				if (args.size() >= 1 &&(value = stream.parseFloat(args, 1)) != null) {
					effect.field_C = value.floatValue();
				}
				
				if (args.size() >= 3 && (value = stream.parseFloat(args, 2)) != null) {
					effect.field_10 = value.floatValue();
				}
			}

			// Add it to the effect
			VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
			block.blockType = TYPE_CODE;
			block.parse(stream, line, MixEventEffect.TYPE_CODE);
			
			data.addComponent(effect.toString(), effect);
			data.getCurrentEffect().blocks.add(block);
			block.component = effect;
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return MixEventEffect.class;
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
			return 2;
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
			return new MixEventEffect(effectDirectory, version);
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(HashManager.get().getFileName(field_8)).floats(field_C, field_10);
	}
}
