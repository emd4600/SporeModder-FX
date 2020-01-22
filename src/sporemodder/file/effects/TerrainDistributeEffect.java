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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.TerrainDistributeLevel.TerrainDistEffect;
import sporemodder.view.editors.PfxEditor;

public class TerrainDistributeEffect extends EffectComponent {

	public static final String KEYWORD = "terrainDistribute";
	public static final int TYPE_CODE = 0x002A;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	
	public static final int FLAG_HEIGHTS = 1;
	public static final int FLAG_TYPES = 2;
	public static final int FLAG_ACTIVE = 4;
	
	public int flags;  // & 8
	public final TerrainDistributeLevel[] levels = new TerrainDistributeLevel[5];
	
	public TerrainDistributeEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		TerrainDistributeEffect effect = (TerrainDistributeEffect) _effect;
		
		flags = effect.flags;
		for (int i = 0; i < levels.length; ++i) {
			levels[i] = new TerrainDistributeLevel(effect.levels[i]);
		}
	}
	
	@Override public void read(StreamReader in) throws IOException {
		flags = in.readInt();  // & 8
		for (int i = 0; i < levels.length; i++) {
			levels[i] = new TerrainDistributeLevel();
			levels[i].read(in, effectDirectory, version);
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(flags);
		for (int i = 0; i < levels.length; i++) {
			if (levels[i] == null) {
				levels[i] = new TerrainDistributeLevel();
			}
			levels[i].write(out, effectDirectory, version);
		}
	}
	
	protected static class Parser extends EffectBlockParser<TerrainDistributeEffect> {
		@Override
		protected TerrainDistributeEffect createEffect(EffectDirectory effectDirectory) {
			return new TerrainDistributeEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("debug", ArgScriptParser.create((parser, line) -> {
				// showLevels is not used?
				if (line.hasFlag("heights")) effect.flags |= FLAG_HEIGHTS;
				else effect.flags &= ~FLAG_HEIGHTS;
				
				if (line.hasFlag("types")) effect.flags |= FLAG_TYPES;
				else effect.flags &= ~FLAG_TYPES;
				
				if (line.hasFlag("active")) effect.flags |= FLAG_ACTIVE;
				else effect.flags &= ~FLAG_ACTIVE;
			}));
			
			this.addParser("level", new ArgScriptBlock<EffectUnit>() {
				int levelIndex;
				TerrainDistributeLevel level;
				
				@Override
				public void parse(ArgScriptLine line) {
					Number value = null;
					if (line.getArguments(args, 0, 1)) {
						if (args.size() == 1) {
							if ((value = stream.parseInt(args, 0)) != null) {
								levelIndex = value.intValue();
								
								if (levelIndex < 0 || levelIndex > 4) {
									stream.addError(line.createErrorForArgument(String.format("Only levels between 0 and 4 are supported. Level %d not supported.", levelIndex), 0));
									level = null;
								}
								else {
									level = new TerrainDistributeLevel();
								}
							}
							else {
								level = null;
							}
						}
						else {
							levelIndex++;
							level = new TerrainDistributeLevel();
						}
					}
					else {
						level = null;
					}
					
					if (level != null) stream.startBlock(this);
					
					if (levelIndex >= 0 && levelIndex < 5) effect.levels[levelIndex] = level;
				}
				
				@Override
				public void setData(ArgScriptStream<EffectUnit> stream, EffectUnit data) {
					super.setData(stream, data);
					
					this.addParser("distance", ArgScriptParser.create((parser, line) -> {
						Number value = null;
						if (level != null && line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
							level.distance = value.floatValue();
						}
						if (level != null && line.getOptionArguments(args, "verticalWeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
							level.verticalWeight = value.floatValue();
						}
					}));
					
					this.addParser("facing", ArgScriptParser.create((parser, line) -> {
						Number value = null;
						if (level != null && line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
							level.facing = value.floatValue();
						}
					}));
					
					this.addParser("effect", ArgScriptParser.create((parser, line) -> {
						TerrainDistributeLevel.TerrainDistEffect instance = new TerrainDistributeLevel.TerrainDistEffect();
						Number value = null;
						
						if (line.getArguments(args, 1)) {
							instance.effect = parser.getData().getComponent(args, 0, VisualEffect.class, "effect");
							if (instance.effect != null) args.addHyperlink(PfxEditor.getHyperlinkType(instance.effect), instance.effect, 0);
						}
						
						if (line.getOptionArguments(args, "heightRange", 2)) {
							stream.parseFloats(args, instance.heightRange);
						}
						
						if (line.getOptionArguments(args, "type", 1)) {
							instance.type = TerrainDistributeLevel.ENUM_EFFECT_TYPE.get(args, 0);
						}
						
						if (line.getOptionArguments(args, "resource", 1)) {
							instance.resource = TerrainDistributeLevel.ENUM_EFFECT_RESOURCE.get(args, 0);
						}
						
						if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseInt(args, 0)) != null) {
							instance.overrideSet = value.intValue();
						}
						
						level.effects.add(instance);
					}));
				}
			});
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, TerrainDistributeEffect.class));
		}

		@Override
		public boolean onlySupportsInline() {
			return false;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new TerrainDistributeEffect(effectDirectory, version);
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(name).startBlock();
		
		if (flags != 0) {
			writer.command("debug");
			writer.flag("heights", (flags & FLAG_HEIGHTS) == FLAG_HEIGHTS);
			writer.flag("types", (flags & FLAG_TYPES) == FLAG_TYPES);
			writer.flag("active", (flags & FLAG_ACTIVE) == FLAG_ACTIVE);
		}
		
		for (int i = 0; i < levels.length; ++i) {
			if (!levels[i].isDefault()) {
				levels[i].toArgScript(writer, i);
			}
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		for (TerrainDistributeLevel level : levels) {
			for (TerrainDistEffect eff : level.effects) {
				list.add(eff.effect);
			}
		}
		return list;
	}
}
