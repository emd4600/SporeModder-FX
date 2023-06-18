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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEncoding;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.locale.LocaleUnit;
import sporemodder.util.ColorRGB;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class TextEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<TextEffect> STRUCTURE_METADATA = StructureMetadata.generate(TextEffect.class);
	
	public static final String KEYWORD = "text";
	public static final int TYPE_CODE = 0x002F;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAG_PARENT_SCALE = 1;  // 1 << 0
	public static final int FLAG_SUSTAIN = 2;  // 1 << 1
	public static final int FLAG_LOOP = 4;  // 1 << 2
	public static final int FLAG_SCREEN_POSITION = 8;  // 1 << 3
	
	public static final int MASK_FLAGS = FLAG_PARENT_SCALE |
			FLAG_SUSTAIN | FLAG_LOOP | FLAG_SCREEN_POSITION;
	
	
	public int flags;  // & 0xF
	@StructureLength.Value(32) @StructureEncoding(StringEncoding.UTF16BE) public String string;
	public final ResourceID localeID = new ResourceID();
	public float fontSize;
	public final ResourceID fontID = new ResourceID();
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] offset = new float[2];
	public float life;
	@StructureLength.Value(32) public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	@StructureLength.Value(32) public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	@StructureLength.Value(32) public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f)); // 0x6C
	
	public TextEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		TextEffect effect = (TextEffect) _effect;
		
		flags = effect.flags;
		string = new String(effect.string);
		localeID.copy(effect.localeID);
		fontSize = effect.fontSize;
		fontID.copy(effect.fontID);
		offset[0] = effect.offset[0];
		offset[1] = effect.offset[1];
		life = effect.life;
		color.addAll(effect.color);
		alpha.addAll(effect.alpha);
		size.addAll(effect.size);
	}
	
	protected static class Parser extends EffectBlockParser<TextEffect> {

		@Override
		protected TextEffect createEffect(EffectDirectory effectDirectory) {
			return new TextEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("string", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.string = args.get(0);
				}
				
				if (line.getOptionArguments(args, "id", 1)) {
					String[] words = new String[2];
					effect.localeID.parse(args, 0, words);
					args.addHyperlink(LocaleUnit.HYPERLINK_LOCALE, words, 0);
				}
				
				Number value = null;
				if ((line.getOptionArguments(args, "size", 1) || line.getOptionArguments(args, "pointSize", 1))
						&& (value = stream.parseFloat(args, 0)) != null) {
					effect.fontSize = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "offset", 1)) {
					stream.parseVector2(args, 0, effect.offset);
				}
				else if (line.getOptionArguments(args, "screen", 1)) {
					stream.parseVector2(args, 0, effect.offset);
					effect.flags |= FLAG_SCREEN_POSITION;
				}
			}));
			
			this.addParser("position", ArgScriptParser.create((parser, line) -> {
				line.getArguments(args, 0);
				
				if (line.getOptionArguments(args, "offset", 1)) {
					stream.parseVector2(args, 0, effect.offset);
				}
				else if (line.getOptionArguments(args, "screen", 1)) {
					stream.parseVector2(args, 0, effect.offset);
					effect.flags |= FLAG_SCREEN_POSITION;
				}
			}));
			
			ArgScriptParser<EffectUnit> fontParser = ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.fontID.parse(args, 0, words);
					args.addHyperlink(PfxEditor.HYPERLINK_FILE, words, 0);
				}
				
				Number value = null;
				if ((line.getOptionArguments(args, "size", 1) || line.getOptionArguments(args, "pointSize", 1))
						&& (value = stream.parseFloat(args, 0)) != null) {
					effect.fontSize = value.floatValue();
				}
			});
			
			this.addParser("font", fontParser);
			this.addParser("style", fontParser);
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					float fValue = value.floatValue();
					if (fValue > 0.000001) {
						effect.life = 1 / fValue;
					}
					else {
						effect.life = 0;
					}
				}
				
				if (line.hasFlag("sustain")) effect.flags |= FLAG_SUSTAIN;
				else if (line.hasFlag("loop")) effect.flags |= FLAG_LOOP;
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGBs(args, effect.color);
				}
			}), "color", "colour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGB255s(args, effect.color);
				}
			}), "color255", "colour255");
			
			this.addParser("alpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloats(args, effect.alpha);
				}
			}));
			
			this.addParser("alpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloat255s(args, effect.alpha);
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
				
				if (line.hasFlag("parentScale")) {
					effect.flags |= FLAG_PARENT_SCALE;
				}
			}));
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags |= value.intValue() & ~MASK_FLAGS;
				}
			}));
		}
		
	}

	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return TextEffect.class;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new TextEffect(effectDirectory, version);
		}

		@Override
		public boolean onlySupportsInline() {
			return false;
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
		writer.command(KEYWORD).arguments(name).startBlock();
		
		boolean fontIsDefault = fontID.isDefault();
		
		writer.command("string").literal(string);
		if (!localeID.isDefault()) writer.option("id").arguments(localeID);
		// If we write the 'font' command, better put size there
		if (fontIsDefault) writer.option("size").floats(fontSize);
		
		if (offset[0] != 0 || offset[1] != 0) {
			writer.command("position");
			if ((flags & FLAG_SCREEN_POSITION) != 0) {
				writer.option("screen").vector(offset);
			}
			else {
				writer.option("offset").vector(offset);
			}
		}
		
		if (!fontIsDefault) {
			writer.command("style").arguments(fontID).option("size").floats(fontSize);
		}
		
		writer.command("life").floats(1.0f / life);
		writer.flag("sustain", (flags & FLAG_SUSTAIN) != 0).flag("loop", (flags & FLAG_LOOP) != 0);
		
		if (!color.isEmpty()) writer.command("color").colors(color);
		if (!alpha.isEmpty()) writer.command("alpha").floats(alpha);
		if (!size.isEmpty()) {
			writer.command("size").floats(size);
			writer.flag("parentScale", (flags & FLAG_PARENT_SCALE) != 0);
		}
		
		if ((flags & ~MASK_FLAGS) != 0) writer.command("flags").arguments(HashManager.get().hexToString(flags & ~MASK_FLAGS));
		
		writer.endBlock().commandEND();
	}
}
