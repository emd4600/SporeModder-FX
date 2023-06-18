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

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.ColorRGB;
import sporemodder.util.Transform;
import sporemodder.view.editors.PfxEditor;

public class DistributeEffect extends EffectComponent {

	public static final String KEYWORD = "distribute";
	public static final int TYPE_CODE = 0x000D;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_SUBDIVIDE = 1;  // 1 << 0
	public static final int FLAGS_SURFACE = 2;  // 1 << 1
	public static final int FLAGS_MAP_EMIT_HEIGHT_RANGE = 4;  // 1 << 2
	public static final int FLAGS_MAP_EMIT_SURFACE = 8;  // 1 << 3
	public static final int FLAGS_SIZE = 0x10;  // 1 << 4
	public static final int FLAGS_COLOR = 0x20;  // 1 << 5, for color, alpha, and mapColor
	public static final int FLAGS_ROTATE = 0x40;  // 1 << 6, for rotate, pitch, roll, heading
	public static final int FLAGS_TEXTURE = 0x80;  // 1 << 7
	public static final int FLAGS_MODEL = 0x100;  // 1 << 8
	public static final int FLAGS_FIT = 0x200;  // 1 << 9, also 'fitChildren'
	public static final int FLAGS_MAP_EMIT_PIN = 0x400;  // 1 << 10, also 'pinToSurface'
	public static final int FLAGS_MAP_EMIT_FORCE_STATIC = 0x800;  // 1 << 11
	public static final int FLAGS_NO_BUDGET = 0x1000;  // 1 << 12
	public static final int FLAGS_SYNC = 0x2000;  // 1 << 13
	public static final int FLAGS_ATTACH = 0x4000;  // 1 << 14, its true when using 'attach' and the component is another distribute
	public static final int FLAGS_SURFACE_SCALE_OFFSET = 0x8000;  // 1 << 15
	public static final int FLAGS_ALPHA_FROM_TIME = 0x10000;  // 1 << 16
	public static final int FLAGS_MESSAGE_KEEP_ALIVE = 0x20000;  // 1 << 17
	
	public static final int MASK_FLAGS = FLAGS_SUBDIVIDE | FLAGS_SURFACE |
			FLAGS_MAP_EMIT_HEIGHT_RANGE | FLAGS_MAP_EMIT_SURFACE | FLAGS_SIZE |
			FLAGS_COLOR | FLAGS_ROTATE | FLAGS_TEXTURE | FLAGS_MODEL | FLAGS_FIT |
			FLAGS_MAP_EMIT_PIN | FLAGS_MAP_EMIT_FORCE_STATIC | FLAGS_NO_BUDGET |
			FLAGS_SYNC | FLAGS_ATTACH | FLAGS_SURFACE_SCALE_OFFSET | FLAGS_ALPHA_FROM_TIME |
			FLAGS_MESSAGE_KEEP_ALIVE;
	
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
	
	// 1Ch is source type, 20h is source size?
	
	// 0x40 (1000000b) -> heading, pitch, roll ?
	// 0x100 (00000001 00000000b) -> model ?
	public int flags;
	public int density = 1;
	public EffectComponent component;
	public int start;
	public byte sourceType;  // byte
	public float sourceScale = 1.0f;
	public final Transform transform = new Transform();
	public final List<Float> size = new ArrayList<Float>(Arrays.asList(1.0f));
	public float sizeVary;
	public final List<Float> pitch = new ArrayList<Float>();
	public final List<Float> roll = new ArrayList<Float>();
	public final List<Float> yaw = new ArrayList<Float>();  // also 'heading' and 'rotate'
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
	public int numClusters;
	public int clustersStart;
	public float clustersFactor;
	public final List<Float> clusters = new ArrayList<Float>();
	
	public DistributeEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		DistributeEffect effect = (DistributeEffect) _effect;
		
		flags = effect.flags;
		density = effect.density;
		component = effect.component;
		start = effect.start;
		sourceType = effect.sourceType;
		sourceScale = effect.sourceScale;
		
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
		
		numClusters = effect.numClusters;
		clustersStart = effect.clustersStart;
		clustersFactor = effect.clustersFactor;
		clusters.addAll(effect.clusters);
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
		int componentIndex = in.readInt();
		int componentType = in.readByte();
		component = effectDirectory.getEffect(componentType, componentIndex);
		start = in.readInt();
		sourceType = in.readByte();
		sourceScale = in.readFloat();
		
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
			numClusters = in.readInt();
			clustersStart = in.readInt();
			clustersFactor = in.readFloat();
			
			count = in.readInt();
			for (int i = 0; i < count; i++) clusters.add(in.readFloat());
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		out.writeInt(flags);
		out.writeInt(density);
		if (component != null) {
			out.writeInt(effectDirectory.getIndex(component.getFactory().getTypeCode(), component));
			out.writeByte(component.getFactory().getTypeCode());
		}
		else {
			out.writeInt(0);
			out.writeByte(0);
		}
		out.writeInt(start);
		out.writeByte(sourceType);
		out.writeFloat(sourceScale);
		
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
			out.writeInt(numClusters);
			out.writeInt(clustersStart);
			out.writeFloat(clustersFactor);
			out.writeInt(clusters.size());
			for (float f : clusters) out.writeFloat(f);
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
				effect.flags |= FLAGS_COLOR;
			}), "color", "colour");
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.color.clear();
					stream.parseColorRGB255s(args, effect.color);
				}
				if (line.getOptionArguments(args, "vary", 1)) {
					stream.parseColorRGB(args, 0, effect.colorVary);
				}
				effect.flags |= FLAGS_COLOR;
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
				effect.flags |= FLAGS_COLOR;
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
				effect.flags |= FLAGS_COLOR;
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
				effect.flags |= FLAGS_SIZE;
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
				effect.flags |= FLAGS_ROTATE;
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
				effect.flags |= FLAGS_ROTATE;
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
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
				effect.flags |= FLAGS_ROTATE;
			}), "yaw", "heading", "rotate");
			
			this.addParser("density", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.density = value.intValue();
				}
				if (line.getOptionArguments(args, "start", 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.start = value.intValue();
				}
				if (line.hasFlag("noBudget")) effect.flags |= FLAGS_NO_BUDGET;
				if (line.hasFlag("sync")) effect.flags |= FLAGS_SYNC;
			}));
			
			this.addParser("subdivide", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.density = value.intValue();
				}
				
				if (line.hasFlag("fit") || line.hasFlag("fitChildren")) effect.flags |= FLAGS_FIT;
				if (line.hasFlag("noBudget")) effect.flags |= FLAGS_NO_BUDGET;
				if (line.hasFlag("sync")) effect.flags |= FLAGS_SYNC;
				
				effect.flags |= FLAGS_SUBDIVIDE;
			}));
			
			this.addParser("source", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					effect.sourceType = (byte) ENUM_SOURCE.get(args, 0);
				}
				if (line.getOptionArguments(args, "scale", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.sourceScale = value.floatValue();
				}
			}));
			
			this.addParser("effect", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.component = parser.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
					if (effect.component != null) line.addHyperlinkForArgument(PfxEditor.getHyperlinkType(effect.component), effect.component, 0);
				}
				
				effect.transform.parse(stream, line);
				
				effect.flags &= ~FLAGS_MODEL;
				effect.flags &= ~FLAGS_TEXTURE;
				effect.flags &= ~FLAGS_ATTACH;
			}));
			
			this.addParser(ArgScriptParser.create((parser, line) -> {
				effect.flags &= ~FLAGS_MODEL;
				effect.flags &= ~FLAGS_TEXTURE;
				effect.flags &= ~FLAGS_ATTACH;
				
				if (line.getArguments(args, 1, 2)) {
					if (args.size() > 1) {
						for (EffectComponentFactory factory : EffectDirectory.getFactories()) {
							if (factory.getKeyword().equals(args.get(0))) {
								int componentType = factory.getTypeCode();
								effect.component = parser.getData().getComponent(args, 1, factory.getTypeCode());
								
								if (componentType == DistributeEffect.TYPE_CODE) {
									effect.flags |= FLAGS_ATTACH;
								}
								break;
							}
						}
						stream.addError(line.createErrorForArgument("First argument must be component type, such as 'particle', 'sound', etc", 0));
						return;
					}
					else {
						effect.component = parser.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
					}
					if (effect.component != null) line.addHyperlinkForArgument(PfxEditor.getHyperlinkType(effect.component), effect.component, 0);
				}
				
				effect.transform.parse(stream, line);
			}), "attach", "component");
			
			this.addParser("surface", ArgScriptParser.create((parser, line) -> {
				if (line.hasFlag("reset")) {
					effect.surfaces.clear();
				}
				
				if (line.hasFlag("scaleOffset")) {
					effect.flags |= FLAGS_SURFACE_SCALE_OFFSET;
				}
				
				Surface surface = new Surface();
				surface.parse(stream, line);
				effect.surfaces.add(surface);
				
				effect.flags |= FLAGS_SURFACE;
			}));
			
			this.addParser("mapEmit", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1)) {
					effect.emitMap.parseSpecial(args, 0);
				}
				if (line.getOptionArguments(args, "belowHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[1] = value.floatValue();
					effect.flags |= FLAGS_MAP_EMIT_HEIGHT_RANGE;
				}
				if (line.getOptionArguments(args, "aboveHeight", 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.altitudeRange[0] = value.floatValue();
					effect.flags |= FLAGS_MAP_EMIT_HEIGHT_RANGE;
				}
				if (line.getOptionArguments(args, "heightRange", 2)) {
					if ((value = stream.parseFloat(args, 0)) != null) {
						effect.altitudeRange[0] = value.floatValue();
					}
					if ((value = stream.parseFloat(args, 1)) != null) {
						effect.altitudeRange[1] = value.floatValue();
					}
					effect.flags |= FLAGS_MAP_EMIT_HEIGHT_RANGE;
				}
				
				if (line.hasFlag("pin") || line.hasFlag("pinToSurface")) {
					effect.flags |= FLAGS_MAP_EMIT_PIN;
				}
				if (line.hasFlag("forceStatic")) {
					effect.flags |= FLAGS_MAP_EMIT_FORCE_STATIC;
				}
				if (line.hasFlag("surface")) {
					effect.flags |= FLAGS_MAP_EMIT_SURFACE;
				}
			}));
			
			this.addParser("mapEmitColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.colorMap.parseSpecial(args, 0);
				}
				effect.flags |= FLAGS_COLOR;
			}));
			
			this.addParser("mapPin", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.pinMap.parseSpecial(args, 0);
				}
			}));

			this.addParser("model", ArgScriptParser.create((parser, line) -> {
				effect.flags |= FLAGS_MODEL;
				effect.flags &= ~FLAGS_TEXTURE;
				effect.flags &= ~FLAGS_ATTACH;
				
				effect.resource.drawMode = 0;
				
				if (line.getArguments(args, 1)) {
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
				
				if (line.hasFlag("alphaFromTime")) {
					effect.flags |= FLAGS_ALPHA_FROM_TIME;
				}
				
				effect.resource.drawFlags |= TextureSlot.DRAWFLAG_SHADOW;
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
				
				effect.transform.parse(stream, line);
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				effect.flags &= ~FLAGS_MODEL;
				effect.flags |= FLAGS_TEXTURE;
				effect.flags &= ~FLAGS_ATTACH;
				
				effect.resource.drawMode = 0;
				
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.resource.resource.parse(args, 0);
				}
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				if (line.hasFlag("alphaFromTime")) {
					effect.flags |= FLAGS_ALPHA_FROM_TIME;
				}
				
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
				
				effect.transform.parse(stream, line);
			}));
			
			// This is a .smt material, not an effect material
			this.addParser("material", ArgScriptParser.create((parser, line) -> {
				effect.flags &= ~FLAGS_MODEL;
				effect.flags |= FLAGS_TEXTURE;
				effect.flags &= ~FLAGS_ATTACH;
				
				effect.resource.drawMode = TextureSlot.DRAWMODE_NONE;
				
				if (line.getArguments(args, 0, 1) && args.size() == 1) {
					effect.resource.resource.parse(args, 0);
				}
				
				Number value = null;
				if (line.getOptionArguments(args, "overrideSet", 1) && (value = stream.parseByte(args, 0)) != null) {
					effect.overrideSet = value.byteValue();
				}
				
				if (line.hasFlag("alphaFromTime")) {
					effect.flags |= FLAGS_ALPHA_FROM_TIME;
				}
				
				effect.resource.parse(stream, line, PfxEditor.HYPERLINK_FILE);
				
				effect.transform.parse(stream, line);
			}));
			
			this.addParser("message", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
					effect.messageID = value.intValue();
				}
				
				if (line.hasFlag("keepAlive")) {
					effect.flags |= FLAGS_MESSAGE_KEEP_ALIVE;
				}
			}));
			
			this.addParser("clusters", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 2, Integer.MAX_VALUE)) {
					effect.numClusters = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
					for (int i = 1; i < args.size(); i++) {
						effect.clusters.add(Optional.ofNullable(stream.parseFloat(args, i)).orElse(0f));
					}
					effect.clustersFactor = effect.clusters.get(0);
					for (int i = 1; i < effect.clusters.size(); i++) {
						if (effect.clusters.get(i) > effect.clustersFactor) {
							effect.clustersFactor = effect.clusters.get(i);
						}
					}
					for (int i = 0; i < effect.clusters.size(); i++) {
						effect.clusters.set(i, effect.clusters.get(i) / effect.clustersFactor);
					}
					
					float value = (float) (Math.sqrt((double)effect.numClusters) / (double)effect.numClusters);
					effect.clustersFactor *= (value - 3f) * value * -0.5f;
				}
				
				if (line.getOptionArguments(args, "start", 1)) {
					effect.clustersStart = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
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
			return DistributeEffect.class;
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE));
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
		
		boolean hasNoBudget = (flags & FLAGS_NO_BUDGET) != 0;
		boolean hasSync = (flags & FLAGS_SYNC) != 0;
		if ((flags & FLAGS_SUBDIVIDE) != 0) {
			writer.command("subdivide").ints(density);
			writer.flag("fit", (flags & FLAGS_FIT) != 0);
			writer.flag("noBudget", hasNoBudget);
			writer.flag("sync", hasSync);
		}
		else {
			writer.command("density").ints(density);
			if (sourceScale != 1.0f) writer.option("start").floats(sourceScale);
			writer.flag("noBudget", hasNoBudget);
			writer.flag("sync", hasSync);
		}
		
		writer.command("source").arguments(ENUM_SOURCE.get(sourceType));
		if (sourceScale != 1.0f) writer.option("scale").floats(sourceScale);
		//TODO width
		
		if (!surfaces.isEmpty()) {
			writer.blankLine();
			boolean isFirst = true;
			for (Surface s : surfaces) {
				writer.command("surface");
				s.toArgScript(writer);
				if (isFirst && (flags & FLAGS_SURFACE_SCALE_OFFSET) != 0) {
					writer.option("scaleOffset");
				}
				isFirst = false;
			}
			writer.blankLine();
		}
		
		if ((flags & FLAGS_ATTACH) != 0) {
			writer.command("attach");
			if (component instanceof VisualEffect) {
				writer.arguments(component.getName());
			}
			else {
				writer.arguments(component.getFactory().getKeyword(), component.getName());
			}
			transform.toArgScriptNoDefault(writer, false);
		}
		else if (component != null) {
			if (component instanceof VisualEffect) {
				writer.command("effect").arguments(component.getName());
			}
			else {
				writer.command("component").arguments(component.getFactory().getKeyword(), component.getName());
			}
			transform.toArgScriptNoDefault(writer, false);
		}
		else if ((flags & FLAGS_MODEL) != 0) {
			writer.command("model");
			if (!resource.resource.isDefault()) writer.arguments(resource.resource);
			
			if (!resource.resource2.isDefault()) writer.option("material").arguments(resource.resource2);
			
			if (overrideSet != 0) writer.option("overrideSet").ints(overrideSet);
			
			writer.flag("alphaFromTime", (flags & FLAGS_ALPHA_FROM_TIME) != 0);
			
			resource.toArgScript(null, writer, false, false);
			
			transform.toArgScriptNoDefault(writer, false);
		}
		else if ((flags & FLAGS_TEXTURE) != 0) {
			writer.command(resource.drawMode == TextureSlot.DRAWMODE_NONE ? "material" : "texture");
			if (!resource.resource.isDefault()) writer.arguments(resource.resource);
			
			if (overrideSet != 0) writer.option("overrideSet").ints(overrideSet);
			
			writer.flag("alphaFromTime", (flags & FLAGS_ALPHA_FROM_TIME) != 0);
			
			resource.toArgScript(null, writer, false, false);
			
			transform.toArgScriptNoDefault(writer, false);
		}
		
		boolean hasMessageKeepAlive = (flags & FLAGS_MESSAGE_KEEP_ALIVE) != 0;
		if (messageID != 0) {
			writer.command("message").arguments(HashManager.get().getFileName(messageID));
			writer.flag("keepAlive", hasMessageKeepAlive);
			
			transform.toArgScriptNoDefault(writer, false);
		}
		
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
			writer.command("heading").floats(yaw);
			if (yawVary != 0.0f) writer.option("vary").floats(yawVary);
			if (yawOffset != 0.0f) writer.option("offset").floats(yawOffset);
		}
		
		if (!emitMap.isDefault()) {
			writer.command("mapEmit").arguments(emitMap);
			if ((flags & FLAGS_MAP_EMIT_HEIGHT_RANGE) != 0) {
				if (altitudeRange[0] != -10000.0f && altitudeRange[1] != 10000.0f) {
					writer.option("heightRange").floats(altitudeRange);
				}
				else if (altitudeRange[1] != 10000.0f) {
					writer.option("belowHeight").floats(altitudeRange[1]);
				}
				else writer.option("aboveHeight").floats(altitudeRange[0]);
			}
			writer.flag("surface", (flags & FLAGS_MAP_EMIT_SURFACE) != 0);
			writer.flag("pinToSurface", (flags & FLAGS_MAP_EMIT_PIN) != 0);
			writer.flag("forceStatic", (flags & FLAGS_MAP_EMIT_FORCE_STATIC) != 0);
		}
		if (!colorMap.isDefault()) writer.command("mapEmitColor").arguments(colorMap);
		if (!pinMap.isDefault()) writer.command("mapPin").arguments(pinMap);
		
		if (numClusters != 0) {
			writer.command("clusters").ints(numClusters);
		
			float value = (float) (Math.sqrt((double)numClusters) / (double)numClusters);
			float factor = clustersFactor / ((value - 3f) * value * -0.5f);
			
			for (float f : clusters) {
				writer.floats(f * factor);
			}
		}
		
		if ((flags & ~MASK_FLAGS) != 0) writer.command("flags").arguments(HashManager.get().hexToString(flags & ~MASK_FLAGS));
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		list.add(component);
		
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
