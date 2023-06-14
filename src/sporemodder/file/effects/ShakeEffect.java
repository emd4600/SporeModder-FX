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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sporemodder.MainApp;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;

@Structure(StructureEndian.BIG_ENDIAN)
public class ShakeEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ShakeEffect> STRUCTURE_METADATA = StructureMetadata.generate(ShakeEffect.class);
	
	public static final String KEYWORD = "shake";
	public static final int TYPE_CODE = 0x0006;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_FALLOFF_3D = 1;
	public static final int MASK_FLAGS = FLAGS_FALLOFF_3D;
	
	public static final ArgScriptEnum ENUM_TABLE = new ArgScriptEnum();
	static {
		ENUM_TABLE.add(0, "random");
		ENUM_TABLE.add(1, "sineY");
	}

	public int flags;  // & 1
	public float lifeTime;
	public float fadeTime = 1.0f;
	@StructureLength.Value(32) public final List<Float> strength = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> frequency = new ArrayList<Float>();
	public float aspectRatio = 1.0f;
	public byte baseTableType;
	public float falloff;
	
	public ShakeEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		ShakeEffect effect = (ShakeEffect) _effect;
		
		flags = effect.flags;
		lifeTime = effect.lifeTime;
		fadeTime = effect.fadeTime;
		strength.addAll(effect.strength);
		frequency.addAll(effect.frequency);
		aspectRatio = effect.aspectRatio;
		baseTableType = effect.baseTableType;
		falloff = effect.falloff;
	}
	
	protected static class Parser extends EffectBlockParser<ShakeEffect> {
		@Override
		protected ShakeEffect createEffect(EffectDirectory effectDirectory) {
			return new ShakeEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags = value.intValue() & ~MASK_FLAGS;
				}
			}));
			
			this.addParser("length", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.lifeTime = value.floatValue();
				}
				if (line.getOptionArguments(args, "fade", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.fadeTime = value.floatValue();
				}
			}));
			
			this.addParser("amplitude", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.strength.clear();
					stream.parseFloats(args, effect.strength);
				}
				
				if (line.getOptionArguments(args, "falloff", 1)) {
					effect.falloff = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
				}
				else if (line.getOptionArguments(args, "falloff3D", 1)) {
					effect.falloff = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
					effect.flags |= FLAGS_FALLOFF_3D;
				}
			}));
			
			this.addParser("frequency", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.frequency.clear();
					stream.parseFloats(args, effect.frequency);
				}
			}));
			
			this.addParser("shakeAspect", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.aspectRatio = value.floatValue();
				}
			}));
			
			this.addParser("table", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.baseTableType = (byte) ENUM_TABLE.get(args, 0);
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
			return 2;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, ShakeEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new ShakeEffect(effectDirectory, version);
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
		
		writer.command("length").floats(lifeTime);
		if (fadeTime != 1.0f) writer.option("fade").floats(fadeTime);
		
		writer.command("amplitude").floats(strength);
		boolean isFalloff3D = (flags & FLAGS_FALLOFF_3D) != 0;
		if (falloff != 0.0f || isFalloff3D) {
			if (isFalloff3D) writer.option("falloff3D");
			else writer.option("falloff");
			writer.floats(falloff);
		}
		
		writer.command("frequency").floats(frequency);
		if (aspectRatio != 1) writer.command("shakeAspect").floats(aspectRatio);
		writer.command("table").arguments(ENUM_TABLE.get(baseTableType));
		
		int maskedFlags = flags & ~MASK_FLAGS;
		if (maskedFlags != 0) writer.command("flags").arguments("0x" + Integer.toHexString(maskedFlags));
		
		writer.endBlock().commandEND();
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		MainApp.testInit();
		
		File folder = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Effects\\gameEffects_3~");
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".effdir")) {
				EffectDirectory effdir = new EffectDirectory();
				try (StreamReader stream = new FileStream(file, "r")) {
					effdir.read(stream);
					for (EffectComponent component : effdir.getComponents(ShakeEffect.TYPE_CODE)) {
						ArgScriptWriter writer = new ArgScriptWriter();
						component.toArgScript(writer);
						System.out.println(writer.toString());
						System.out.println();
						
						int flags = ((ShakeEffect)component).flags;
						if (flags != 0) {
							System.err.println("ERROR: 0x" + Integer.toHexString(flags));
						}
					}
				}
			}
		}
	}
}
