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
import java.util.List;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.view.editors.PfxEditor;

public class SequenceEffect extends EffectComponent {
	
	public static class SequenceInstance {
		public final float[] timeRange = {-1, -1};
		public EffectComponent effect;
		
		public SequenceInstance() {};
		public SequenceInstance(EffectComponent effect, float[] timeRange) {
			this.effect = effect;
			this.timeRange[0] = timeRange[0];
			this.timeRange[1] = timeRange[1];
		}
		public SequenceInstance(SequenceInstance other) {
			timeRange[0] = other.timeRange[0];
			timeRange[1] = other.timeRange[1];
			effect = other.effect;
		}
	}

	public static final String KEYWORD = "sequence";
	public static final int TYPE_CODE = 0x0004;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_LOOP = 1;  // 1 << 0
	public static final int FLAGS_HARD_START = 2;  // 1 << 1
	public static final int FLAGS_HARD_STOP = 4;  // 1 << 2, also called 'noOverlap'
	public static final int FLAGS_NO_STOP = 8;  // 1 << 3, also called 'overlap'
	
	public static final int MASK_FLAGS = FLAGS_LOOP | FLAGS_HARD_START |
			FLAGS_HARD_STOP | FLAGS_NO_STOP;
	
	public final List<SequenceInstance> instances = new ArrayList<SequenceInstance>();
	public int flags;
	
	public SequenceEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		SequenceEffect effect = (SequenceEffect) _effect;
		
		int size = effect.instances.size();
		for (int i = 0; i < size; i++) {
			instances.add(new SequenceInstance(effect.instances.get(i)));
		}
		flags = effect.flags;
	}
	
	@Override public void read(StreamReader in) throws IOException {
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			SequenceInstance instance = new SequenceInstance();
			instance.timeRange[0] = in.readLEFloat();
			instance.timeRange[1] = in.readLEFloat();
			instance.effect = effectDirectory.getEffect(VisualEffect.TYPE_CODE, in.readInt());
			instances.add(instance);
		}
		flags = in.readInt();
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(instances.size());
		for (SequenceInstance ins : instances) {
			out.writeLEFloat(ins.timeRange[0]);
			out.writeLEFloat(ins.timeRange[1]);
			out.writeInt(effectDirectory.getIndex(VisualEffect.TYPE_CODE, ins.effect));
		}
		out.writeInt(flags);
	}
	
	protected static class Parser extends EffectBlockParser<SequenceEffect> {
		@Override
		protected SequenceEffect createEffect(EffectDirectory effectDirectory) {
			return new SequenceEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		protected void additionalLineParsing(ArgScriptLine line) {
			if (line.hasFlag("loop")) {
				effect.flags |= FLAGS_LOOP;
			}
			if (line.hasFlag("noOverlap") || line.hasFlag("hardStop")) {
				effect.flags |= FLAGS_HARD_STOP;
			}
			if (line.hasFlag("hardStart")) {
				effect.flags |= FLAGS_HARD_START;
			}
			if (line.hasFlag("overlap") || line.hasFlag("noStop")) {
				effect.flags |= FLAGS_NO_STOP;
			}
		}
		
		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags |= value.intValue() & ~MASK_FLAGS;
				}
			}));
			
			this.addParser("wait", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, 2)) {
					SequenceInstance ins = new SequenceInstance();
					effect.instances.add(ins);
					
					ins.timeRange[0] = ins.timeRange[1] = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
					
					if (args.size() > 1) {
						ins.timeRange[1] = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
					}
				}
			}));
			
			this.addParser("play", ArgScriptParser.create((parser, line) -> {
				SequenceInstance ins = new SequenceInstance();
				effect.instances.add(ins);
				
				if (line.getArguments(args, 1, 3)) {
					ins.effect = data.getComponent(args, 0, VisualEffect.class, VisualEffect.KEYWORD);
					if (ins.effect != null) args.addHyperlink(PfxEditor.getHyperlinkType(ins.effect), ins.effect, 0);
					
					ins.timeRange[0] = ins.timeRange[1] = -1.0f;
					
					if (args.size() > 1) {
						ins.timeRange[0] = ins.timeRange[1] = Optional.ofNullable(stream.parseFloat(args, 1)).orElse(0.0f);
						
						if (args.size() > 2) {
							ins.timeRange[1] = Optional.ofNullable(stream.parseFloat(args, 2)).orElse(0.0f);
						}
					}
				}
			}));
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return SequenceEffect.class;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, SequenceEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new SequenceEffect(effectDirectory, version);
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
		writer.command(KEYWORD).arguments(name);
		
		writer.flag("loop", (flags & FLAGS_LOOP) != 0);
		writer.flag("hardStart", (flags & FLAGS_HARD_START) != 0);
		writer.flag("noOverlap", (flags & FLAGS_HARD_STOP) != 0);
		writer.flag("overlap", (flags & FLAGS_NO_STOP) != 0);
		
		writer.startBlock();
		
		for (SequenceInstance ins : instances) {
			if (ins.effect == null) {
				writer.command("wait").floats(ins.timeRange[0]);
				if (ins.timeRange[0] != ins.timeRange[1]) writer.floats(ins.timeRange[1]);
			}
			else {
				writer.command("play").arguments(ins.effect.getName());
				if (ins.timeRange[0] != -1.0f) {
					writer.floats(ins.timeRange[0]);
					if (ins.timeRange[0] != ins.timeRange[1]) {
						writer.floats(ins.timeRange[1]);
					}
				}
			}
			
			if (ins.timeRange[0] != -1 || ins.timeRange[1] != -1) writer.option("range").floats(ins.timeRange);
		}
		
		int maskedFlags = flags & ~MASK_FLAGS;
		if (maskedFlags != 0) writer.command("flags").arguments(HashManager.get().hexToString(flags));
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		for (SequenceInstance ins : instances) {
			list.add(ins.effect);
		}
		return list;
	}
}
