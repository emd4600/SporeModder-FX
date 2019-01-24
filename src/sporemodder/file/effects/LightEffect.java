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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGB;

public class LightEffect extends EffectComponent {
	
	public static final String KEYWORD = "light";
	public static final int TYPE_CODE = 0x000A;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(0, "ambient");
		ENUM_TYPE.add(1, "dir");
		ENUM_TYPE.add(1, "directional");
		ENUM_TYPE.add(2, "point");
		ENUM_TYPE.add(3, "spot");
		ENUM_TYPE.add(4, "area");
	}
	
	public static final int FLAG_SUSTAIN = 8;

	public int flags;  // ?
	public int type;  // byte
	public float life;
	public int loop;  // short
	public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	public final List<Float> strength = new ArrayList<Float>(Arrays.asList(1.0f));
	public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));  // also spotWidth
	public float spotWidthPenumbra;
	
	public LightEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		LightEffect effect = (LightEffect) _effect;
		
		flags = effect.flags;
		type = effect.type;
		life = effect.life;
		loop = effect.loop;
		color.addAll(effect.color);
		strength.addAll(effect.strength);
		size.addAll(effect.size);
		spotWidthPenumbra = effect.spotWidthPenumbra;
	}
	
	@Override public void read(StreamReader in) throws IOException {
		
		flags = in.readInt();  // & 0xF ?
		type = in.readByte();
		
		if (version > 1) 
		{
			life = in.readFloat();
			loop = in.readShort();
		}
		
		int colorCount = in.readInt();
		for (int i = 0; i < colorCount; i++) {
			ColorRGB color = new ColorRGB();
			color.readLE(in);
			this.color.add(color);
		}
		
		int strengthCount = in.readInt();
		for (int i = 0; i < strengthCount; i++) strength.add(in.readFloat());
		
		int sizeCount = in.readInt();
		for (int i = 0; i < sizeCount; i++) size.add(in.readFloat());
		
		spotWidthPenumbra = in.readFloat();
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(flags);
		out.writeByte(type);
		
		out.writeFloat(life);
		out.writeShort(loop);
		
		out.writeInt(color.size());
		for (ColorRGB f : color) f.writeLE(out);
		
		out.writeInt(strength.size());
		for (float f : strength) out.writeFloat(f);
		
		out.writeInt(size.size());
		for (float f : size) out.writeFloat(f);
		
		out.writeFloat(spotWidthPenumbra);
	}
	
	protected static class Parser extends EffectBlockParser<LightEffect> {
		@Override
		protected LightEffect createEffect(EffectDirectory effectDirectory) {
			return new LightEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser("type", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.type = ENUM_TYPE.get(args, 0);
				}
			}));
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.life = value.floatValue();
				}
				
				if (line.hasFlag("single")) {
					effect.loop = 1;
				}
				else if (line.getOptionArguments(args, "loop", 0, 1)) {
					if (args.size() == 1 && (value = stream.parseInt(args, 0)) != null) {
						effect.loop = value.intValue();
					}
					else {
						effect.loop = 0;
					}
				}
				else if (line.getOptionArguments(args, "sustain", 0, 1)) {
					if (args.size() == 1 && (value = stream.parseInt(args, 0)) != null) {
						effect.loop = value.intValue();
					}
					else {
						effect.loop = 1;
					}
					effect.flags |= FLAG_SUSTAIN;
				}
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
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
			}));
			
			this.addParser("spotWidth", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
				
				if (line.getOptionArguments(args, "penumbra", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.spotWidthPenumbra = value.floatValue();
				}
			}));
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags = value.intValue();
				}
			}));
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
			return 2;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, LightEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new LightEffect(effectDirectory, version);
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
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(name).startBlock();
		
		writer.command("type").arguments(ENUM_TYPE.get(type));
		
		writer.command("life").floats(life);
		if (loop == 1 && (flags & FLAG_SUSTAIN) != FLAG_SUSTAIN) writer.option("single");
		else if ((flags & FLAG_SUSTAIN) == 0) writer.option("loop").ints(loop);
		else writer.option("sustain").ints(loop);
		
		writer.command("color").colors(color);
		writer.command("strength").floats(strength);
		
		if (spotWidthPenumbra != 0) writer.command("spotWidth").floats(size)
				.option("penumbra").floats(spotWidthPenumbra);
		
		else writer.command("size").floats(size);
		
		if (flags != 0) writer.command("flags").arguments(HashManager.get().hexToString(flags));
		
		writer.endBlock().commandEND();
	}
}
