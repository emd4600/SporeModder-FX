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

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.util.ColorRGB;

@Structure(StructureEndian.BIG_ENDIAN)
public class ModelEffect extends EffectComponent {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ModelEffect> STRUCTURE_METADATA = StructureMetadata.generate(ModelEffect.class);
	
	public static final String KEYWORD = "model";
	public static final int TYPE_CODE = 0x0008;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public int flags;
	public final ResourceID resourceID = new ResourceID();
	public float size = 1.0f;
	public final ColorRGB color = ColorRGB.white();
	public float alpha = 1.0f;
	@StructureLength.Value(32)
	public final List<ModelAnimation> animations = new ArrayList<ModelAnimation>();
	public final ResourceID materialID = new ResourceID();
	public byte overrideSet;
	
	public ModelEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		ModelEffect effect = (ModelEffect) _effect;
		
		flags = effect.flags;
		size = effect.size;
		color.copy(effect.color);
		resourceID.copy(effect.resourceID);
		materialID.copy(effect.materialID);
		overrideSet = effect.overrideSet;
		
		for (int i = 0; i < effect.animations.size(); i++) {
			animations.add(new ModelAnimation(effect.animations.get(i)));
		}
	}
	
	protected static class Parser extends EffectBlockParser<ModelEffect> {
		@Override
		protected ModelEffect createEffect(EffectDirectory effectDirectory) {
			return new ModelEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			addParser("name", ArgScriptParser.create((parser, line) -> {
				ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.resourceID.parse(args, 0, words);
					line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_FILE, words, 0);
				}
			}));
			
			addParser("size", ArgScriptParser.create((parser, line) -> {
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				if (line.getArguments(args, 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.size = value.floatValue();
				}
			}));
			
			addParser("color", ArgScriptParser.create((parser, line) -> {
				ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1)) {
					stream.parseColorRGB(args, 0, effect.color);
				}
			}));
			
			addParser("alpha", ArgScriptParser.create((parser, line) -> {
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				if (line.getArguments(args, 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.alpha = value.floatValue();
				}
			}));
			
			addParser("material", ArgScriptParser.create((parser, line) -> {
				ArgScriptArguments args = new ArgScriptArguments();
				if (line.getArguments(args, 1)) {
					String[] words = new String[2];
					effect.materialID.parse(args, 0, words);
					line.addHyperlinkForArgument(EffectDirectory.HYPERLINK_MATERIAL, words, 0);
				}
			}));
			
			addParser("overrideSet", ArgScriptParser.create((parser, line) -> {
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				if (line.getArguments(args, 1) &&
						(value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
			}));
			
			addParser("animate", ArgScriptParser.create((parser, line) -> {
				ModelAnimation anim = new ModelAnimation();
				anim.parse(stream, line);
				effect.animations.add(anim);
			}));
		}
	}
	
	protected static class GroupParser extends ArgScriptParser<EffectUnit> {
		@Override
		public void parse(ArgScriptLine line) {
			ArgScriptArguments args = new ArgScriptArguments();
			
			// Add it to the effect
			VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
			block.blockType = TYPE_CODE;
			data.getCurrentEffect().blocks.add(block);
			
			if (line.getArguments(args, 0, 1) && args.size() == 0) {
				// It's the anonymous version
				
				ModelEffect effect = new ModelEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
				
				Number value = null;
				
				if (line.getOptionArguments(args, "name", 1)) {
					effect.resourceID.parse(args, 0);
				} else {
					stream.addError(line.createError(String.format("Need at least option '-name' for anonymous '%s' effect.", KEYWORD)));
				}
				
				if (line.getOptionArguments(args, "size", 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.size = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "color", 1) || line.getOptionArguments(args, "colour", 1)) {
					stream.parseColorRGB(args, 0, effect.color);
				}
				
				if (line.getOptionArguments(args, "alpha", 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.alpha = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "material", 1)) {
					effect.materialID.parse(args, 0);
				}
				
				if (line.getOptionArguments(args, "overrideSet", 1) &&
						(value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				data.addComponent(effect.toString(), effect);
				block.component = effect;
			}
			
			block.parse(stream, line, ModelEffect.TYPE_CODE, args.size() == 0);
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return ModelEffect.class;
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
			return false;
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new ModelEffect(effectDirectory, version);
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
		
		writer.command("name").arguments(resourceID);
		if (size != 1.0f) writer.command("size").floats(size);
		if (!color.isWhite()) writer.command("color").color(color);
		if (alpha != 1.0f) writer.command("alpha").floats(alpha);
		if (!materialID.isDefault()) writer.command("material").arguments(materialID);
		if (overrideSet != 0) writer.command("overrideSet").ints(overrideSet);
		
		if (!animations.isEmpty()) {
			writer.blankLine();
			for (ModelAnimation anim : animations) {
				anim.toArgScript(writer);
			}
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		return Arrays.asList(effectDirectory.getResource(MaterialResource.TYPE_CODE, materialID));
	}
}
