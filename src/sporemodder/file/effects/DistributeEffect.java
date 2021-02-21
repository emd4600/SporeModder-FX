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
import java.util.Optional;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGB;
import sporemodder.util.Transform;
import sporemodder.view.editors.PfxEditor;

public class DistributeEffect extends EffectComponent {

	public static final String KEYWORD = "distribute";
	public static final int TYPE_CODE = 0x000D;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final ArgScriptEnum ENUM_SOURCE = new ArgScriptEnum();
	static {
		ENUM_SOURCE.add(0, "square");
		ENUM_SOURCE.add(1, "circle");
		ENUM_SOURCE.add(2, "ring");
		ENUM_SOURCE.add(3, "sphereSurface");
		ENUM_SOURCE.add(4, "cube");
		ENUM_SOURCE.add(5, "sphere");
		ENUM_SOURCE.add(6, "sphereCubeSurface");
	}
	
	public static final int FLAG_SURFACES = 0x1000;
	public static final int FLAG_MODEL = 0x100;
	
	public static final int FLAGMASK = FLAG_SURFACES | FLAG_MODEL;
	
	// 0x40 (1000000b) -> heading, pitch, roll ?
	// 0x100 (00000001 00000000b) -> model ?
	public int flags;
	public int density = 1;
	public EffectComponent effect;
	public byte sourceType;  // byte
	public int sourceSize;
	public byte field_1C;  // byte
	public float start = 1.0f;
	public final Transform transform = new Transform();
	public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	public float sizeVary;
	public final List<Float> pitch = new ArrayList<Float>();
	public final List<Float> roll = new ArrayList<Float>();
	public final List<Float> yaw = new ArrayList<Float>();
	public float pitchVary;
	public float rollVary;
	public float yawVary;
	public float pitchOffset;
	public float rollOffset;
	public float yawOffset;
	public final List<ColorRGB> color = new ArrayList<ColorRGB>(Arrays.asList(ColorRGB.white()));
	public final ColorRGB colorVary = ColorRGB.black();
	public final List<Float> alpha = new ArrayList<Float>(Arrays.asList(1.0f));
	public float alphaVary;
	public final List<Surface> surfaces = new ArrayList<Surface>();
	public final ResourceID emitMap = new ResourceID();
	public final ResourceID colorMap = new ResourceID();
	public final ResourceID pinMap = new ResourceID();
	public final float[] altitudeRange = { -10000.0f, 10000.0f };
	public final TextureSlot resource = new TextureSlot();
	public int overrideSet;  // byte
	public int messageID;
	public int field_160;
	public int field_164;
	public float rotateVary;
	public final List<Float> rotate = new ArrayList<Float>();
	
	public DistributeEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		DistributeEffect effect = (DistributeEffect) _effect;
		
		flags = effect.flags;
		density = effect.density;
		this.effect = effect.effect;
		sourceType = effect.sourceType;
		sourceSize = effect.sourceSize;
		field_1C = effect.field_1C;
		start = effect.start;
		
		transform.copy(effect.transform);
		
		size.clear();
		size.addAll(effect.size);
		sizeVary = effect.sizeVary;
		pitch.addAll(effect.pitch);
		roll.addAll(effect.roll);
		yaw.addAll(effect.yaw);
		pitchVary = effect.pitchVary;
		pitchOffset = effect.pitchOffset;
		rollVary = effect.rollVary;
		rollOffset = effect.rollOffset;
		yawVary = effect.yawVary;
		yawOffset = effect.yawOffset;
		color.clear();
		color.addAll(effect.color);
		colorVary.copy(effect.colorVary);
		alpha.clear();
		alpha.addAll(alpha);
		alphaVary = effect.alphaVary;
		
		for (int i = 0; i < effect.surfaces.size(); i++) {
			surfaces.add(new Surface(effect.surfaces.get(i)));		
		}
		emitMap.copy(effect.emitMap);
		colorMap.copy(effect.colorMap);
		pinMap.copy(effect.pinMap);
		EffectDirectory.copyArray(altitudeRange, effect.altitudeRange);
		
		resource.copy(effect.resource);
		overrideSet = effect.overrideSet;
		messageID = effect.messageID;
		
		field_160 = effect.field_160;
		field_164 = effect.field_164;
		rotateVary = effect.rotateVary;
		rotate.addAll(effect.rotate);
	}
	
	private void readTransform(StreamReader in) throws IOException {
		int flags = in.readUShort();
		transform.setScale(in.readFloat());
		transform.getRotation().readLE(in);
		transform.getOffset().readLE(in);
		transform.setFlags(flags);
	}
	
	private void writeTransform(StreamWriter out) throws IOException {
		out.writeUShort(transform.getFlags());
		out.writeFloat(transform.getScale());
		transform.getRotation().writeLE(out);
		transform.getOffset().writeLE(out);
	}
	
	@Override public void read(StreamReader in) throws IOException {
		int count = 0;
		
		flags = in.readInt();
		density = in.readInt();
		effect = effectDirectory.getEffect(VisualEffect.TYPE_CODE, in.readInt());
		sourceType = in.readByte();
		sourceSize = in.readInt();
		field_1C = in.readByte();
		start = in.readFloat();
		
		readTransform(in);
		
		count = in.readInt();
		size.clear();
		for (int i = 0; i < count; i++) size.add(in.readFloat());
		sizeVary = in.readFloat();
		
		count = in.readInt();
		for (int i = 0; i < count; i++) pitch.add(in.readFloat());
		
		count = in.readInt();
		for (int i = 0; i < count; i++) roll.add(in.readFloat());
		
		count = in.readInt();
		for (int i = 0; i < count; i++) yaw.add(in.readFloat());
		
		pitchVary = in.readFloat();
		rollVary = in.readFloat();
		yawVary = in.readFloat();
		pitchOffset = in.readFloat();
		rollOffset = in.readFloat();
		yawOffset = in.readFloat();
		
		color.clear();
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			ColorRGB c = new ColorRGB();
			c.readLE(in);
			color.add(c);
		}
		colorVary.readLE(in);
		
		count = in.readInt();
		alpha.clear();
		for (int i = 0; i < count; i++) alpha.add(in.readFloat());
		alphaVary = in.readFloat();
		
		int surfaceCount = in.readInt();
		for (int i = 0; i < surfaceCount; i++) {
			Surface surface = new Surface();
			surface.read(in, effectDirectory);
			surfaces.add(surface);
		}
		
		emitMap.read(in);
		colorMap.read(in);
		pinMap.read(in);
		
		in.readLEFloats(altitudeRange);
		TextureSlot.STRUCTURE_METADATA.read(resource, in);
		overrideSet = in.readByte();
		messageID = in.readInt();
		
		if (version > 3) {
			field_160 = in.readInt();
			field_164 = in.readInt();
			rotateVary = in.readFloat();
			
			count = in.readInt();
			for (int i = 0; i < count; i++) rotate.add(in.readFloat());
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(flags);
		out.writeInt(density);
		out.writeInt(effectDirectory.getIndex(VisualEffect.TYPE_CODE, effect));
		out.writeByte(sourceType);
		out.writeInt(sourceSize);
		out.writeByte(field_1C);
		out.writeFloat(start);
		
		writeTransform(out);
		
		out.writeInt(size.size());
		for (float f : size) out.writeFloat(f);
		out.writeFloat(sizeVary);
		
		out.writeInt(pitch.size());
		for (float f : pitch) out.writeFloat(f);
		
		out.writeInt(roll.size());
		for (float f : roll) out.writeFloat(f);
		
		out.writeInt(yaw.size());
		for (float f : yaw) out.writeFloat(f);
		
		out.writeFloat(pitchVary);
		out.writeFloat(rollVary);
		out.writeFloat(yawVary);
		out.writeFloat(pitchOffset);
		out.writeFloat(rollOffset);
		out.writeFloat(yawOffset);
		
		out.writeInt(color.size());
		for (ColorRGB c : color) {
			c.writeLE(out);
		}
		colorVary.writeLE(out);
		
		out.writeInt(alpha.size());
		for (float f : alpha) out.writeFloat(f);
		out.writeFloat(alphaVary);
		
		out.writeInt(surfaces.size());
		for (Surface s : surfaces) {
			s.write(out, effectDirectory);
		}
		
		emitMap.write(out);
		colorMap.write(out);
		pinMap.write(out);
		
		out.writeLEFloats(altitudeRange);
		TextureSlot.STRUCTURE_METADATA.write(resource, out);
		out.writeByte(overrideSet);
		out.writeInt(messageID);
		
		if (version > 3) {
			out.writeInt(field_160);
			out.writeInt(field_164);
			out.writeFloat(rotateVary);
			out.writeInt(rotate.size());
			for (float f : rotate) out.writeFloat(f);
		}
	}

	protected static class Parser extends EffectBlockParser<DistributeEffect> {
		@Override
		protected DistributeEffect createEffect(EffectDirectory effectDirectory) {
			return new DistributeEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGBs(args, effect.color);
				}
				if (line.getOptionArguments(args, "vary", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
				}
			}), "color", "colour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGB255s(args, effect.color);
				}
				if (line.getOptionArguments(args, "vary", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
				}
			}), "color255", "colour255");
			
			this.addParser("alpha", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloats(args, effect.alpha);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaVary = value.floatValue();
				}
			}));
			
			this.addParser("alpha255", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.alpha.clear();
					stream.parseFloat255s(args, effect.alpha);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.alphaVary = value.floatValue();
				}
			}));
			
			this.addParser("size", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.size.clear();
					stream.parseFloats(args, effect.size);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.sizeVary = value.floatValue();
				}
			}));
			
			this.addParser("pitch", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.pitch.clear();
					stream.parseFloats(args, effect.pitch);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.pitchVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.pitchOffset = value.floatValue();
				}
			}));
			
			this.addParser("roll", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.roll.clear();
					stream.parseFloats(args, effect.roll);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rollVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rollOffset = value.floatValue();
				}
			}));
			
			this.addParser("yaw", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.yaw.clear();
					stream.parseFloats(args, effect.yaw);
				}
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.yawVary = value.floatValue();
				}
				if (line.getOptionArguments(args, "offset", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.yawOffset = value.floatValue();
				}
			}));
			
			this.addParser("density", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.density = value.intValue();
				}
				if (line.getOptionArguments(args, "start", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.start = value.floatValue();
				}
				//TODO 'sync' flag?
			}));
			
			this.addParser("source", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					if (Character.isDigit(args.get(0).charAt(0))) {
						effect.sourceType = Optional.ofNullable(stream.parseByte(args, 0)).orElse((byte)0);
					}
					else {
						effect.sourceType = (byte) ENUM_SOURCE.get(args, 0);
					}
				}
				if (line.getOptionArguments(args, "scale", 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.sourceSize = value.intValue();
				}
			}));
			
			this.addParser("effect", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.effect = parser.getData().getComponent(args, 0, VisualEffect.class, "effect");
					if (effect.effect != null) line.addHyperlinkForArgument(PfxEditor.getHyperlinkType(effect.effect), effect.effect, 0);
				}
				
				effect.transform.parse(stream, line);
			}));
			
			this.addParser("surface", ArgScriptParser.create((parser, line) -> {
				if (line.hasFlag("reset")) {
					effect.surfaces.clear();
				}
				
				Surface surface = new Surface();
				surface.parse(stream, line);
				effect.surfaces.add(surface);
				
				effect.flags |= FLAG_SURFACES;
			}));
			
			this.addParser("mapEmit", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					effect.emitMap.parseSpecial(args, 0);
				}
				if (line.getOptionArguments(args, "belowHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[1] = value.floatValue();
				}
				if (line.getOptionArguments(args, "aboveHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[0] = value.floatValue();
				}
				if (line.getOptionArguments(args, "heightRange", 2)) {
					if ((value = stream.parseFloat(args, 0)) != null) {
						effect.altitudeRange[0] = value.floatValue();
					}
					if ((value = stream.parseFloat(args, 1)) != null) {
						effect.altitudeRange[1] = value.floatValue();
					}
				}
			}));
			
			this.addParser("mapEmitColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.colorMap.parseSpecial(args, 0);
				}
			}));
			
			this.addParser("mapPin", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.pinMap.parseSpecial(args, 0);
				}
			}));
			
			//TODO could be texture, model?
			this.addParser("resource", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.overrideSet = value.intValue();
				}
			}));

			this.addParser("model", ArgScriptParser.create((parser, line) -> {
				effect.resource.drawMode = 0;
				effect.flags |= FLAG_MODEL;
				
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.resource.resource.parse(args, 0);
				}
				
				if (line.getOptionArguments(args, "material", 1)) {
					effect.resource.resource2.parse(args, 0);
					effect.resource.drawMode = TextureSlot.DRAWMODE_NONE;
				}
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				effect.resource.drawFlags |= TextureSlot.DRAWFLAG_SHADOW;
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
			}));
			
			// This is a .smt material, not an effect material
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				effect.resource.drawMode = TextureSlot.DRAWMODE_NONE;
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.overrideSet = value.intValue();
				}
			}));
			
			
			this.addParser("resource", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.overrideSet = value.intValue();
				}
			}));
			
			this.addParser("message", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
					effect.messageID = value.intValue();
				}
			}));
			
			this.addParser("rotate", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.rotate.clear();
					stream.parseFloats(args, effect.rotate);
				}
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.rotateVary = value.floatValue();
				}
			}));
			
			this.addParser("field_160", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.field_160 = value.intValue();
				}
			}));
			
			this.addParser("field_164", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.field_164 = value.intValue();
				}
			}));
			
			this.addParser("field_1C", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.field_1C = value.byteValue();
				}
			}));
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags |= value.intValue() & ~FLAGMASK;
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
			return 3;
		}

		@Override
		public int getMaxVersion() {
			return 4;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, DistributeEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new DistributeEffect(effectDirectory, version);
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
		
		if (!color.isEmpty()) {
			writer.command("color").colors(color);
			if (!colorVary.isBlack()) writer.option("vary").color(colorVary);
		}
		if (!alpha.isEmpty()) {
			writer.command("alpha").floats(alpha);
			if (alphaVary != 0.0f) writer.option("vary").floats(alphaVary);
		}
		if (!size.isEmpty()) {
			writer.command("size").floats(size);
			if (sizeVary != 0.0f) writer.option("vary").floats(sizeVary);
		}
		if (!pitch.isEmpty()) {
			writer.command("pitch").floats(pitch);
			if (pitchVary != 0.0f) writer.option("vary").floats(pitchVary);
			if (pitchOffset != 0.0f) writer.option("offset").floats(pitchOffset);
		}
		if (!roll.isEmpty()) {
			writer.command("roll").floats(roll);
			if (rollVary != 0.0f) writer.option("vary").floats(rollVary);
			if (rollOffset != 0.0f) writer.option("offset").floats(rollOffset);
		}
		if (!yaw.isEmpty()) {
			writer.command("yaw").floats(yaw);
			if (yawVary != 0.0f) writer.option("vary").floats(yawVary);
			if (yawOffset != 0.0f) writer.option("offset").floats(yawOffset);
		}
		if (density != 1 || start != 1.0f) {
			writer.command("density").ints(density);
			if (start != 1.0f) writer.option("start").floats(start);
		}
		
		String src = ENUM_SOURCE.get(sourceType);
		writer.command("source").arguments(src != null ? src : Integer.toString(sourceType));
		if (sourceSize != 0.0f) writer.option("scale").ints(sourceSize);
		
		if (effect != null) {
			writer.command("effect").arguments(effect.getName());
			transform.toArgScriptNoDefault(writer, false);
		}
		
		//TODO subdivide
		if (!surfaces.isEmpty()) {
			writer.blankLine();
			for (Surface s : surfaces) {
				writer.command("surface");
				s.toArgScript(writer);
			}
			writer.blankLine();
		}
		
		if (!emitMap.isDefault()) {
			writer.command("mapEmit").arguments(emitMap);
			if (altitudeRange[0] != -10000.0f || altitudeRange[1] != 10000.0f) {
				if (altitudeRange[0] != -10000.0f && altitudeRange[1] != 10000.0f) {
					writer.option("heightRange").floats(altitudeRange);
				}
				else if (altitudeRange[1] != 10000.0f) {
					writer.option("belowHeight").floats(altitudeRange[1]);
				}
				else writer.option("aboveHeight").floats(altitudeRange[0]);
			}
		}
		if (!colorMap.isDefault()) writer.command("mapEmitColor").arguments(colorMap);
		if (!pinMap.isDefault()) writer.command("mapPin").arguments(pinMap);
		
		if (!resource.isDefault() || overrideSet != 0) {
			if ((flags & FLAG_MODEL) == FLAG_MODEL) {
				writer.command("model");
				if (!resource.resource.isDefault()) writer.arguments(resource.resource);
				
				if (!resource.resource2.isDefault()) writer.option("material").arguments(resource.resource2);
				
				resource.toArgScript(null, writer, false, false);
			}
			else {
				resource.toArgScript(resource.drawMode == TextureSlot.DRAWMODE_NONE ? "material" : "resource", writer);
				if (overrideSet != 0) writer.option("overrideSet").ints(overrideSet);
			}
		}
		
		if (messageID != 0) writer.command("message").arguments(HashManager.get().getFileName(messageID));
		
		if (!rotate.isEmpty()) {
			writer.command("rotate").floats(rotate);
			if (rotateVary != 0.0f) writer.option("vary").floats(rotateVary);
		}
		if (field_160 != 0) writer.command("field_160").ints(field_160);
		if (field_164 != 0) writer.command("field_164").ints(field_164);
		if (field_1C != 0) writer.command("field_1C").arguments(HashManager.get().hexToString(field_1C));
		if ((flags & ~FLAGMASK) != 0) writer.command("flags").arguments(HashManager.get().hexToString(flags & ~FLAGMASK));
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		list.add(effect);
		
		for (Surface surface : surfaces) {
			list.add(surface.collideEffect);
			list.add(surface.deathEffect);
			list.add(effectDirectory.getResource(MapResource.TYPE_CODE, surface.surfaceMapID));
		}
		
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, emitMap));
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, colorMap));
		list.add(effectDirectory.getResource(MapResource.TYPE_CODE, pinMap));
		
		return list;
	}
}
