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
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.view.editors.PfxEditor;

public class VolumeEffect extends EffectComponent {
	
	public static final String KEYWORD = "volume";
	public static final int TYPE_CODE = 0x0028;
	
	public static final EffectComponentFactory FACTORY = new Factory();

	// Warning! Spore parsed 'color' 'alpha' too, but it doesn't read them anywhere!
	public final float[] field_8 = new float[3];  // 0x08
	public float sliceSpacing = 0.25f;  // 0x14
	public final ResourceID material = new ResourceID(0x554771E5, 0);  // 0x28
	public final float[][] field_30 = new float[8][6];
	
	public VolumeEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
		
		for (int i = 0; i < 8; i++) {
			field_30[i] = new float[6];
			field_30[i][0] = (i & 1) == 0 ? -1 : 1;
			field_30[i][1] = (i & 2) == 0 ? -1 : 1;
			field_30[i][2] = (i & 4) == 0 ? -1 : 1;
			field_30[i][3] = (i & 1) == 0 ? -1 : 1;
			field_30[i][4] = (i & 2) == 0 ? -1 : 1;
			field_30[i][5] = (i & 4) == 0 ? -1 : 1;
		}
	}
	
	@Override public void copy(EffectComponent _effect) {
		VolumeEffect effect = (VolumeEffect) _effect;
		
		EffectDirectory.copyArray(field_8, effect.field_8);
		sliceSpacing = effect.sliceSpacing;
		material.copy(effect.material);
		
		for (int i = 0; i < field_30.length; ++i) {
			EffectDirectory.copyArray(field_30[i], effect.field_30[i]);
		}
	}
	
	@Override public void read(StreamReader in) throws IOException {
		in.readLEFloats(field_8);
		sliceSpacing = in.readFloat();
		material.read(in);
		for (int i = 0; i < field_30.length; i++) {
			field_30[i] = new float[6];
			in.readLEFloats(field_30[i]);
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeLEFloats(field_8);
		out.writeFloat(sliceSpacing);
		material.write(out);
		for (int i = 0; i < field_30.length; i++) {
			out.writeLEFloats(field_30[i]);
		}
	}
	
	protected static class Parser extends EffectBlockParser<VolumeEffect> {

		@Override
		protected VolumeEffect createEffect(EffectDirectory effectDirectory) {
			return new VolumeEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("sliceSpacing", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.sliceSpacing = value.floatValue();
				}
			}));
			
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				String[] originals = new String[2];
				if (line.getArguments(args, 1) && effect.material.parse(args, 0, originals)) {
					args.addHyperlink(PfxEditor.HYPERLINK_MATERIAL, originals, 0);
				}
			}));
			
			this.addParser("set", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, 8)) {
					String keyword = args.get(0);
					
					// Except in corner, all arguments are floats
					if ("corner".equals(keyword)) {
						if (args.size() != 5 && args.size() != 8) {
							stream.addError(line.createError("Expected 5 or 8 arguments in 'set corner'."));
							return;
						}
						
						Byte index = stream.parseByte(args, 1);
						if (index == null) return;
						List<Float> values = new ArrayList<Float>();
						for (int i = 2; i < args.size(); i++) {
							Float value = stream.parseFloat(args, i);
							if (value == null) return;
							values.add(value);
						}
						
						if (index > 8) {
							byte num = 0;
							if ((index & 1) != 0) {
								num = 1;
							}
							if (((index / 10) & 1) != 0) {
								num |= 2;
							}
							if (index >= 100) {
								num |= 4;
							}
							index = num;
						}
						effect.field_30[index][0] = values.get(0);
						effect.field_30[index][1] = values.get(1);
						effect.field_30[index][2] = values.get(2);
						if (args.size() == 8) {
							effect.field_30[index][3] = values.get(3);
							effect.field_30[index][4] = values.get(4);
							effect.field_30[index][5] = values.get(5);
						}
					}
					else {
						List<Float> values = new ArrayList<Float>();
						for (int i = 1; i < args.size(); i++) {
							Float value = stream.parseFloat(args, i);
							if (value == null) return;
							values.add(value);
						}
						
						if ("bottom".equals(keyword)) {
							if (args.size() != 2 && args.size() != 3) {
								stream.addError(line.createError("Expected 2 or 3 arguments in 'set bottom'."));
								return;
							}
							effect.field_30[0][2] = effect.field_30[1][2] = effect.field_30[2][2] = effect.field_30[3][2] = values.get(0);
							
							if (args.size() == 3) {
								effect.field_30[0][5] = effect.field_30[1][5] = effect.field_30[2][5] = effect.field_30[3][5] = values.get(1);
							}
						}
						else if ("top".equals(keyword)) {
							if (args.size() != 2 && args.size() != 3) {
								stream.addError(line.createError("Expected 2 or 3 arguments in 'set top'."));
								return;
							}
							effect.field_30[4][2] = effect.field_30[5][2] = effect.field_30[6][2] = effect.field_30[7][2] = values.get(0);
							
							if (args.size() == 3) {
								effect.field_30[4][5] = effect.field_30[5][5] = effect.field_30[6][5] = effect.field_30[7][5] = values.get(1);
							}
						}
						else if ("width".equals(keyword)) {
							if (args.size() != 2) {
								stream.addError(line.createError("Expected 2 arguments in 'set top'."));
								return;
							}
							float num = values.get(0);
							effect.field_30[0][0] = -num;
							effect.field_30[0][1] = -num;
							effect.field_30[1][0] = num;
							effect.field_30[1][1] = -num;
							effect.field_30[2][0] = -num;
							effect.field_30[2][1] = num;
							effect.field_30[3][0] = num;
							effect.field_30[3][1] = num;
							effect.field_30[4][0] = -num;
							effect.field_30[4][1] = -num;
							effect.field_30[5][0] = num;
							effect.field_30[5][1] = -num;
							effect.field_30[6][0] = -num;
							effect.field_30[6][1] = num;
							effect.field_30[7][0] = num;
							effect.field_30[7][1] = num;
						}
						else {
							stream.addError(line.createErrorForArgument("Unknown 'set' type: only 'top', 'bottom', 'corner' and 'width' supported.", 1));
						}
					}
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
			return 1;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, VolumeEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new VolumeEffect(effectDirectory, version);
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
		
		if (sliceSpacing != 0.25f) writer.command("sliceSpacing").floats(sliceSpacing);
		writer.command("material").arguments(material);
		
		if (field_30[0][2] == field_30[1][2] && field_30[0][2] == field_30[2][2] && field_30[0][2] == field_30[3][2]) {
			writer.command("set").arguments("bottom").floats(field_30[0][2]);
			
			if (field_30[0][5] == field_30[1][5] && field_30[0][5] == field_30[2][5] && field_30[0][5] == field_30[3][5]) {
				writer.floats(field_30[0][5]);
			}
		}
		if (field_30[4][2] == field_30[5][2] && field_30[4][2] == field_30[6][2] && field_30[4][2] == field_30[7][2]) {
			writer.command("set").arguments("top").floats(field_30[4][2]);
			
			if (field_30[4][5] == field_30[5][5] && field_30[4][5] == field_30[6][5] && field_30[4][5] == field_30[7][5]) {
				writer.floats(field_30[4][5]);
			}
		}
		float num = field_30[0][0];
		
		// more checkings go here
		if (num == field_30[0][0] && num == field_30[0][1] && num == field_30[1][1] && num == field_30[2][0]) {
			writer.command("set").arguments("width").floats(-num * 2);
		}
		
		//TODO this isn't really correct, but it should pack again without problems
		for (int i = 0; i < field_30.length; i++) {
			boolean isWorth2 = field_30[i][3] != 0 || field_30[i][4] != 0 || field_30[i][5] != 0;
			
			if (field_30[i][0] != 0 || field_30[i][1] != 0 || field_30[i][2] != 0 || isWorth2) {
				writer.command("set").arguments("corner").ints(i).floats(field_30[i][0], field_30[i][1], field_30[i][2]);
				
				if (isWorth2) {
					writer.floats(field_30[i][3], field_30[i][4], field_30[i][5]);
				}
			}
		}
		
		writer.endBlock().commandEND();
	}
	
	
	@Override public List<EffectFileElement> getUsedElements() {
		//TODO this might be wrong!
		return Arrays.asList(effectDirectory.getResource(MaterialResource.TYPE_CODE, material));
	}
}
