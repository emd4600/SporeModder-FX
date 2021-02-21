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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGB;
import sporemodder.util.Transform;
import sporemodder.view.editors.PfxEditor;

public class GameModelEffect extends EffectComponent {
	
	public static final String KEYWORD = "gameModel";
	public static final int TYPE_CODE = 0x0025;
	
	public static final EffectComponentFactory FACTORY = new Factory();

	public static final ArgScriptEnum ENUM_GROUPS = new ArgScriptEnum();
	static {
		ENUM_GROUPS.add(0x1BA53EA, "deformHandle");
		ENUM_GROUPS.add(0x1BA53EB, "deformHandleOverdraw");
		ENUM_GROUPS.add(0x223E8E0, "background");
		ENUM_GROUPS.add(0x22FFF11, "overdraw");
		ENUM_GROUPS.add(0x23008D4, "effectsMask");
		ENUM_GROUPS.add(0x26F3933, "testEnv");
		ENUM_GROUPS.add(0xFE39DE0, "partsPaintEnv");
		ENUM_GROUPS.add(0xFEB8DF2, "skin");
		ENUM_GROUPS.add(0x31390732, "rotationRing");
		ENUM_GROUPS.add(0x31390733, "rotationBall");
		ENUM_GROUPS.add(0x4FF4AF74, "socketConnector");
		ENUM_GROUPS.add(0x509991E6, "animatedCreature");
		ENUM_GROUPS.add(0x509AA7C9, "testMode");
		ENUM_GROUPS.add(0x513CDFC1, "vertebra");
		ENUM_GROUPS.add(0x71257E8B, "paletteSkin");
		ENUM_GROUPS.add(0x900C6ADD, "excludeFromPinning");
		ENUM_GROUPS.add(0x900C6AE5, "palette");
		ENUM_GROUPS.add(0x900C6CDD, "ballConnector");
		ENUM_GROUPS.add(0x9138FD8D, "rigblock");
		ENUM_GROUPS.add(0x4FE3913, "rigblockEffect");
		ENUM_GROUPS.add(0x64AC354, "gameBackground");
	}
	
	public static final ArgScriptEnum ENUM_OPTIONS = new ArgScriptEnum();
	static {
		ENUM_OPTIONS.add(0, "visible");
		ENUM_OPTIONS.add(1, "applyColor");
		ENUM_OPTIONS.add(2, "applyColorAsIdentity");
		ENUM_OPTIONS.add(3, "highlight");
		ENUM_OPTIONS.add(4, "aboveGround");
		ENUM_OPTIONS.add(5, "localZUp");
		ENUM_OPTIONS.add(6, "showDescription");
		ENUM_OPTIONS.add(7, "visualScale");
		ENUM_OPTIONS.add(8, "customPickLevel");
		ENUM_OPTIONS.add(9, "overrideBounds");
		ENUM_OPTIONS.add(10, "applyNightLights");
		ENUM_OPTIONS.add(11, "applyMaxIdentity");
		ENUM_OPTIONS.add(12, "alphaSort");
		ENUM_OPTIONS.add(13, "superHighLOD");
	}
	
	public static final ArgScriptEnum ENUM_PICKLEVEL = new ArgScriptEnum();
	static {
		ENUM_PICKLEVEL.add(0, "boundingSphere");
		ENUM_PICKLEVEL.add(1, "boundingBox");
		ENUM_PICKLEVEL.add(2, "hullTriangle");
		ENUM_PICKLEVEL.add(3, "meshCluster");
		ENUM_PICKLEVEL.add(4, "meshTriangle");
	}
	
	public static final ArgScriptEnum ENUM_SPLITTER_TYPE = new ArgScriptEnum();
	static {
		ENUM_SPLITTER_TYPE.add(1, "negativeOnly");
		ENUM_SPLITTER_TYPE.add(0, "positiveOnly");
		ENUM_SPLITTER_TYPE.add(2, "binary");
	}
	
	public static final ArgScriptEnum ENUM_SPLITTER_PREFERENCE = new ArgScriptEnum();
	static {
		ENUM_SPLITTER_PREFERENCE.add(4, "default");
		ENUM_SPLITTER_PREFERENCE.add(0, "preferPositive");
		ENUM_SPLITTER_PREFERENCE.add(1, "preferNegative");
		ENUM_SPLITTER_PREFERENCE.add(2, "preferBest");
		ENUM_SPLITTER_PREFERENCE.add(3, "split");
		ENUM_SPLITTER_PREFERENCE.add(4, "none");
	}
	
	public static final int SPLIT_FILTER = 0;
	public static final int SPLIT_CONTROL = 1;
	public static final int SPLIT_STATIC = 2;
	
	public static final ArgScriptEnum ENUM_SPLIT_TYPE = new ArgScriptEnum();
	static {
		ENUM_SPLIT_TYPE.add(SPLIT_FILTER, "filter");
		ENUM_SPLIT_TYPE.add(SPLIT_CONTROL, "control");
		ENUM_SPLIT_TYPE.add(SPLIT_STATIC, "static");
	}
	
	public static final int FLAG_MESSAGE = 4;
	public static final int FLAG_NOATTACHMENTS = 0x20;
	public static final int FLAG_FIXEDSIZE = 0x10;
	public static final int FLAG_PERSIST = 8;
	
	public static class ModelSplitter {
		public EffectResource splitterKernel;
		public int splitType;  // byte
		public int preference;  // byte
		
		public ModelSplitter() {};
		public ModelSplitter(ModelSplitter other) {
			splitterKernel = other.splitterKernel;
			splitType = other.splitType;
			preference = other.preference;
		}
	}
	
	
	public static class ModelSplit {
		public int type;  // byte
		public EffectComponent splitter;  // can point to a metaParticle too
		public final Transform transform = new Transform();
		public final List<Integer> splitterIndices = new ArrayList<Integer>();  // it does something weird to it
		public final float[] origin = new float[3];
		
		public ModelSplit() {};
		public ModelSplit(ModelSplit other) {
			type = other.type;
			splitter = other.splitter;
			transform.copy(other.transform);
			splitterIndices.addAll(other.splitterIndices);
			EffectDirectory.copyArray(origin, other.origin);
		}
	}
	
	public int flags;
	public float size = 1.0f;
	public final ColorRGB color = ColorRGB.white();
	public float alpha = 1.0f;
	// It's not a ResourceID, it goes the other way around
	public int instanceID = -1;
	public int groupID = -1;
	public int worldID;
	
	public final List<GameModelAnimation> animations = new ArrayList<GameModelAnimation>();
	public final List<ModelSplitter> splitters = new ArrayList<ModelSplitter>();
	public final List<ModelSplit> splits = new ArrayList<ModelSplit>();
	public final List<Integer> groups = new ArrayList<Integer>();
	
	public int options;
	public int pickLevel;  // byte
	public int overrideSet;  // byte
	public int messageID; // only in version > 7
	
	public GameModelEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		GameModelEffect effect = (GameModelEffect) _effect;
	
		flags = effect.flags;
		size = effect.size;
		color.copy(effect.color);
		alpha = effect.alpha;
		instanceID = effect.instanceID;
		groupID = effect.groupID;
		worldID = effect.worldID;
		
		groups.addAll(effect.groups);
		options = effect.options;
		pickLevel = effect.pickLevel;
		overrideSet = effect.overrideSet;
		messageID = effect.messageID;
		
		for (GameModelAnimation obj : effect.animations) {
			animations.add(new GameModelAnimation(obj));
		}
		for (ModelSplitter obj : effect.splitters) {
			splitters.add(new ModelSplitter(obj));
		}
		for (ModelSplit obj : effect.splits) {
			splits.add(new ModelSplit(obj));
		}
	}
	
	@Override public void read(StreamReader in) throws IOException {
		flags = in.readInt();
		size = in.readFloat();
		color.readLE(in);
		alpha = in.readFloat();
		instanceID = in.readInt();
		groupID = in.readInt();
		worldID = in.readInt();
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			GameModelAnimation anim = new GameModelAnimation();
			GameModelAnimation.STRUCTURE_METADATA.read(anim, in);
			animations.add(anim);
		}
		
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			ModelSplitter object = new ModelSplitter();
			object.splitterKernel = effectDirectory.getResource(SplitterResource.TYPE_CODE, in.readInt());
			object.splitType = in.readByte();
			object.preference = in.readByte();
			
			splitters.add(object);
		}
		
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			ModelSplit object = new ModelSplit();
			
			object.type = in.readByte();
			int index = in.readInt();
			if (object.type == SPLIT_CONTROL) {
				object.splitter = effectDirectory.getEffect(MetaparticleEffect.TYPE_CODE, index);
			} else {
				object.splitter = null;
			}
			
			object.transform.read(in);
			
			int indicesCount = in.readInt();
			for (int j = 0; j < indicesCount; j++) object.splitterIndices.add(in.readInt());
			
			in.readLEFloats(object.origin);
			
			splits.add(object);
		}
		
		count = in.readInt();
		for (int i = 0; i < count; i++) groups.add(in.readInt());
		
		options = in.readInt();
		pickLevel = in.readByte();
		overrideSet = in.readByte();
		if (version < 8) {
			flags &= 0xFFFFFFFB;
		} else {
			messageID = in.readInt();
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(flags);
		out.writeFloat(size);
		color.writeLE(out);
		out.writeFloat(alpha);
		out.writeInt(instanceID);
		out.writeInt(groupID);
		out.writeInt(worldID);
		
		out.writeInt(animations.size());
		for (GameModelAnimation anim : animations) {
			GameModelAnimation.STRUCTURE_METADATA.write(anim, out);
		}
		
		out.writeInt(splitters.size()) ;
		for (ModelSplitter splitter : splitters) {
			out.writeInt(effectDirectory.getIndex(SplitterResource.TYPE_CODE, splitter.splitterKernel));
			out.writeByte(splitter.splitType);
			out.writeByte(splitter.preference);
		}
		
		out.writeInt(splits.size());
		for (ModelSplit split : splits) {
			out.writeByte(split.type);
			
			if (split.type == SPLIT_CONTROL) {
				out.writeInt(effectDirectory.getIndex(MetaparticleEffect.TYPE_CODE, split.splitter));
			}
			else if (split.type == SPLIT_FILTER) {
				out.writeInt(splitters.size());
			}
			else {
				out.writeInt(0);
			}
			
			split.transform.write(out);
			
			out.writeInt(split.splitterIndices.size());
			for (int i : split.splitterIndices) out.writeInt(i);
			out.writeLEFloats(split.origin);
		}
	
		out.writeInt(groups.size());
		for (int i : groups) out.writeInt(i);
		
		out.writeInt(options);
		out.writeByte(pickLevel);
		out.writeByte(overrideSet);
		if (version > 7) {
			out.writeInt(messageID);
		}
	}
	
	
	protected static class Parser extends EffectBlockParser<GameModelEffect> {
		@Override
		protected GameModelEffect createEffect(EffectDirectory effectDirectory) {
			return new GameModelEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("name", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					ResourceKey key = new ResourceKey();
					String[] keys = new String[3];
					if (key.parse(args, 0, keys)) {
						effect.instanceID = key.getInstanceID();
						effect.groupID = key.getGroupID();
						
						line.addHyperlinkForArgument(PfxEditor.HYPERLINK_FILE, keys, 0);
					}
				}
				
				if (line.getOptionArguments(args, "message", 1)) {
					ResourceKey key = new ResourceKey();
					if (key.parse(args, 0)) {
						effect.messageID = key.getInstanceID();
						effect.flags |= FLAG_MESSAGE;
					}
				}
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseUInt(args, 0)) != null) {
					effect.overrideSet = value.intValue();
				}
				
				if (line.hasFlag("noAttachments")) {
					effect.flags |= FLAG_NOATTACHMENTS;
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.size = value.floatValue();
				}
				
				if (line.hasFlag("fixed")) {
					effect.flags |= FLAG_FIXEDSIZE;
				}
			}));
			
			this.addParser("color", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					stream.parseColorRGB(args, 0, effect.color);
				}
			}));
			this.addParser("alpha", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.alpha = value.floatValue();
				}
			}));
			
			this.addParser("world", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					ResourceKey key = new ResourceKey();
					if (key.parse(args, 0)) {
						effect.worldID = key.getInstanceID();
					}
				}
			}));
			
			this.addParser("persist", ArgScriptParser.create((parser, line) -> {
				Boolean value = null;
				if (line.getArguments(args, 1) &&
						(value = stream.parseBoolean(args, 0)) != null) {
					
					if (value == true) {
						effect.flags |= FLAG_PERSIST;
					} else {
						effect.flags &= ~FLAG_PERSIST;
					}
				}
			}));
			
			this.addParser("groups", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					
					Number value = null;
					for (int i = 0; i < args.size(); i++) {
						
						if (Character.isAlphabetic( args.get(i).charAt(0))) {
							effect.groups.add(ENUM_GROUPS.get(args, i));
						}
						else if ((value = stream.parseUInt(args, i)) != null) {
							effect.groups.add(value.intValue());
						}
						else {
							break;
						}
					}
				}
			}));
			
			this.addParser("options", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					
					for (int i = 0; i < args.size(); i++) {
						
						int num = ENUM_OPTIONS.get(args, i);
						if (num == -1) return;
						
						effect.options |= 1 << (num);
					}
				}
			}));
			
			this.addParser("pickLevel", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.pickLevel = ENUM_PICKLEVEL.get(args, 0);
				}
			}));
			
			this.addParser("animate", ArgScriptParser.create((parser, line) -> {
				GameModelAnimation anim = new GameModelAnimation();
				anim.parse(stream, line);
				effect.animations.add(anim);
			}));
			
			parseSplit();
			parseSplitter();
		}
		
		private void parseSplitter() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("splitter", ArgScriptParser.create((parser, line) -> {
				ModelSplitter item = new ModelSplitter();
				
				if (line.getArguments(args, 3)) {
					item.splitterKernel = parser.getData().getResource(args, 0, SplitterResource.class, "splitter");
					item.splitType = ENUM_SPLITTER_TYPE.get(args, 1);
					item.preference = ENUM_SPLITTER_PREFERENCE.get(args, 2);
					
					if (item.splitterKernel != null) {
						args.addHyperlink(SplitterResource.KEYWORD, item.splitterKernel, 0);
					}
				}
				
				effect.splitters.add(item);
			}));
		}
		
		private void parseSplit() {
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("split", ArgScriptParser.create((parser, line) -> {
				ModelSplit item = new ModelSplit();
				
				if (line.getArguments(args, 2, Integer.MAX_VALUE)) {
					int index = 0;
					
					item.type = ENUM_SPLIT_TYPE.get(args, 0);
					index++;
					
					if (item.type == SPLIT_FILTER) {
						// Spore sets it to the size of the splitter list, we do this
						item.splitter = null;
					}
					else if (item.type == SPLIT_CONTROL) {
						if (args.size() < 3) {
							stream.addError(line.createError("Either meta particle or at least one split index unspecified."));
							return;
						}
						
						item.splitter = parser.getData().getComponent(args, index, MetaparticleEffect.class, "metaParticles");
						if (item.splitter != null) {
							args.addHyperlink(PfxEditor.getHyperlinkType(item.splitter), item.splitter, index);
						}
						
						index++;
					}
					
					Number value = null;
					for (; index < args.size(); index++) {
						if ((value = stream.parseInt(args, index)) == null) break;
						
						item.splitterIndices.add(value.intValue());
					}
					
					
					// -- ORIGIN -- //
					
					if (line.getOptionArguments(args, "origin", 1)) {
						// This might be different!
						// Actually, Spore applies the rotation and offset BUT it does so before parsing the transformation,
						// so it's literally doing nothing
						stream.parseVector3(args, 0, item.origin);
					}
					
					item.transform.parse(stream, line);
				}
				
				effect.splits.add(item);
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
				
				GameModelEffect effect = new GameModelEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
				
				Number value = null;
				
				ResourceKey key = new ResourceKey();
				if (!line.getOptionArguments(args, "name", 1) || !key.parse(args, 0)) {
					stream.addError(line.createError(String.format("Need at least option '-name' for anonymous '%s' effect.", KEYWORD)));
				}
				effect.instanceID = key.getInstanceID();
				effect.groupID = key.getGroupID();
				
				if (line.getOptionArguments(args, "message", 1) &&
						(value = stream.parseFileID(args, 0)) != null) {
					effect.messageID = value.intValue();
					effect.flags |= FLAG_MESSAGE;
				}
				
				if (line.getOptionArguments(args, "color", 1) || line.getOptionArguments(args, "colour", 1)) {
					stream.parseColorRGB(args, 0, effect.color);
				}
				
				if (line.getOptionArguments(args, "alpha", 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.alpha = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "size", 1) &&
						(value = stream.parseFloat(args, 0)) != null) {
					effect.size = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "world", 1) &&
						(value = stream.parseFileID(args, 0)) != null) {
					effect.worldID = value.intValue();
				}
				
				data.addComponent(effect.toString(), effect);
				block.component = effect;
			}
			
			block.parse(stream, line, GameModelEffect.class, args.size() == 0);
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
			return 7;
		}

		@Override
		public int getMaxVersion() {
			return 8;
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
			return new GameModelEffect(effectDirectory, version);
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
		
		writer.command("name").arguments(new ResourceID(groupID, instanceID));
		if ((flags & FLAG_MESSAGE) == FLAG_MESSAGE) writer.option("message").arguments(HashManager.get().getFileName(messageID));
		
		if (overrideSet != 0) writer.option("overrideSet").ints(overrideSet);
		
		writer.flag("noAttachments", (flags & FLAG_NOATTACHMENTS) == FLAG_NOATTACHMENTS);
		
		writer.command("size").floats(size);
		writer.flag("fixed", (flags & FLAG_FIXEDSIZE) == FLAG_FIXEDSIZE);
		
		writer.command("color").color(color);
		writer.command("alpha").floats(alpha);
		
		if (worldID != 0) writer.command("world").arguments(HashManager.get().getFileName(worldID));
		
		if ((flags & FLAG_PERSIST) == FLAG_PERSIST) writer.command("persist").arguments("true");
		
		if (!groups.isEmpty()) {
			writer.command("groups");
			for (int i : groups) {
				String key = ENUM_GROUPS.get(i);
				writer.arguments(key == null ? HashManager.get().getFileName(i) : key);
			}
		}
		if (options != 0) {
			writer.command("options");
			for (int i = 0; i < 32; i++) {
				if ((options & (1 << i)) != 0) writer.arguments(ENUM_OPTIONS.get(i));
			}
		}
		
		if (pickLevel != 0) writer.command("pickLevel").ints(pickLevel);
		
		if (!animations.isEmpty()) {
			writer.blankLine();
			for (GameModelAnimation anim : animations) {
				anim.toArgScript(writer);
			}
		}
		if (!splitters.isEmpty()) {
			writer.blankLine();
			for (ModelSplitter splitter : splitters) {
				writer.command("splitter").arguments(splitter.splitterKernel.resourceID,
						ENUM_SPLITTER_TYPE.get(splitter.splitType), ENUM_SPLITTER_PREFERENCE.get(splitter.preference));
			}
		}
		if (!splits.isEmpty()) {
			writer.blankLine();
			for (ModelSplit split : splits) {
				writeSplit(split, writer);
			}
		}
		
		writer.endBlock().commandEND();
	}
	
	private void writeSplit(ModelSplit split, ArgScriptWriter writer) {
		writer.command("split").arguments(ENUM_SPLIT_TYPE.get(split.type));
		
		if (split.type == SPLIT_CONTROL) writer.arguments(split.splitter.getName());
		
		writer.arguments(split.splitterIndices);
		
		if (split.origin[0] != 0 || split.origin[1] != 0 || split.origin[2] != 0) {
			writer.option("origin").vector(split.origin);
		}
		split.transform.toArgScriptNoDefault(writer, false);
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		
		for (ModelSplitter splitter : splitters) {
			list.add(splitter.splitterKernel);
		}
		for (ModelSplit split : splits) {
			if (split.type == SPLIT_CONTROL) {
				list.add(split.splitter);
			}
		}
		
		return list;
	}
}
