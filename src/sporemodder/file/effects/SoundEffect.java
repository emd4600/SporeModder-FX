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
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class SoundEffect extends EffectComponent {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<SoundEffect> STRUCTURE_METADATA = StructureMetadata.generate(SoundEffect.class);
	
	public static final String KEYWORD = "sound";
	public static final int TYPE_CODE = 0x0005;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAG_FIELD18 = 1 << 4;
	public static final int FLAG_LOOP = 0xa;
	public static final int FLAGMASK = FLAG_FIELD18 | FLAG_LOOP;
	
	public int flags;
	public final ResourceID soundID = new ResourceID();
	public int field_18 = 0xCDCDCDCD;  // only used if flags >> 4
	public float field_1C = 0.05f;
	public float field_20;
	public float field_24;
	
	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}
	
	public SoundEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		SoundEffect effect = (SoundEffect) _effect;
		
		flags = effect.flags;
		soundID.copy(effect.soundID);
		field_18 = effect.field_18;
		field_1C = effect.field_1C;
		field_20 = effect.field_20;
		field_24 = effect.field_24;
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
			// Sound effects are only supported as anonymous (in-line)
			
			SoundEffect effect = new SoundEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
			
			// It must not have any arguments
			line.getArguments(args, 0);
			
			if (line.getOptionArguments(args, "name", 1)) {
				String[] words = new String[2];
				effect.soundID.parse(args, 0, words);
				args.addHyperlink(PfxEditor.HYPERLINK_FILE, words, 0);
			}
			else {
				stream.addError(line.createError(String.format("Need at least option '-name' for anonymous '%s' effect.", KEYWORD)));
			}
			
			if (line.getOptionArguments(args, "soundFlags", 1)) 
			{
				effect.flags |= Optional.ofNullable(stream.parseInt(args, 0)).orElse(0) & ~FLAG_FIELD18;
			}
			if (line.getOptionArguments(args, "field_18", 1)) 
			{
				effect.flags |= FLAG_FIELD18;
				effect.field_18 = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
			}
			if (line.getOptionArguments(args, "field_1C", 1)) 
			{
				effect.field_1C = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
			}
			if (line.getOptionArguments(args, "field_20", 1)) 
			{
				effect.field_20 = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
			}
			if (line.getOptionArguments(args, "field_24", 1)) 
			{
				effect.field_24 = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
			}
			if (line.hasFlag("loop")) {
				effect.flags |= FLAG_LOOP;
			}
			

			// Add it to the effect
			VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
			block.blockType = TYPE_CODE;
			block.parse(stream, line, SoundEffect.class);
			
			block.component = effect;
			data.addComponent(effect.toString(), effect);
			data.getCurrentEffect().blocks.add(block);
		}
	}
	
	public static class Factory implements EffectComponentFactory {

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
			return new SoundEffect(effectDirectory, version);
		}

		@Override
		public boolean onlySupportsInline() {
			return true;
		}

		@Override public String getKeyword() {
			return KEYWORD;
		}
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
		writer.command(KEYWORD).option("name").arguments(soundID);
		
		if ((flags & FLAG_LOOP) == FLAG_LOOP) {
			writer.option("loop");
		}
		else if ((flags & FLAG_LOOP) != 0 || (flags & ~FLAGMASK) != 0) {
			writer.option("soundFlags").arguments("0x" + Integer.toHexString(flags & ~FLAG_FIELD18));
		}
		
		if ((flags & FLAG_FIELD18) != 0) writer.option("field_18").arguments(HashManager.get().getFileName(field_18));
		if (field_1C != 0.05f) writer.option("field_1C").floats(field_1C);
		if (field_20 != 0) writer.option("field_20").floats(field_20);
		if (field_24 != 0) writer.option("field_24").floats(field_24);
	}
}
