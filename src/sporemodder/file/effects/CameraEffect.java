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
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class CameraEffect extends EffectComponent {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<CameraEffect> STRUCTURE_METADATA = StructureMetadata.generate(CameraEffect.class);
	
	public static final String KEYWORD = "camera";
	public static final int TYPE_CODE = 0x0007;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public static final int FLAGS_CAMERA_CONTROL = 0x1;  // 1 << 0
	public static final int FLAGS_NO_RESTORE = 0x2;  // 1 << 1, also just 'switch' flag
	public static final int FLAGS_SNAPSHOT_ON_START = 0x4;  // 1 << 2
	public static final int FLAGS_SNAPSHOT_ON_STOP = 0x8;  // 1 << 3
	public static final int FLAGS_CUBEMAP_SNAPSHOT_ON_START = 0x10;  // 1 << 4
	public static final int FLAGS_CUBEMAP_SNAPSHOT_ON_STOP = 0x20;  // 1 << 4
	public static final int FLAGS_CUBEMAP_SNAPSHOT_FROM_EFFECT = 0x40;  // 1 << 6
	public static final int FLAGS_LOOP = 0x80;  // 1 << 7
	public static final int FLAGS_SINGLE = 0x100;  // 1 << 8
	public static final int FLAGS_CAMERA_PARAMS = 0x200;  // 1 << 9
	
	public static final int MASK_FLAGS = FLAGS_CAMERA_CONTROL | FLAGS_NO_RESTORE | 
			FLAGS_SNAPSHOT_ON_START | FLAGS_SNAPSHOT_ON_STOP | FLAGS_CUBEMAP_SNAPSHOT_ON_START |
			FLAGS_CUBEMAP_SNAPSHOT_ON_STOP | FLAGS_CUBEMAP_SNAPSHOT_FROM_EFFECT | FLAGS_LOOP |
			FLAGS_SINGLE | FLAGS_CAMERA_PARAMS;
	
	public static final int CONTROL_FLAGS_TARGET = 1;  // 1 << 0
	public static final int CONTROL_FLAGS_ORIENT = 2;  // 1 << 1
	public static final int CONTROL_FLAGS_ORIENT_HORIZONTAL = 4;  // 1 << 2
	public static final int CONTROL_FLAGS_ZOOM = 8;  // 1 << 3
	public static final int CONTROL_FLAGS_SNAP = 0x10;  // 1 << 4
	public static final int CONTROL_FLAGS_RELATIVE = 0x20;  // 1 << 5
	
	public static final int MASK_CONTROL_FLAGS = CONTROL_FLAGS_TARGET |
			CONTROL_FLAGS_ORIENT | CONTROL_FLAGS_ORIENT_HORIZONTAL | CONTROL_FLAGS_ZOOM |
			CONTROL_FLAGS_SNAP | CONTROL_FLAGS_RELATIVE;
	
	//TODO
//		camera CaptureCubeTest
//	    life 2 -sustain
//	    cubemapSnapshot -onStart -res 1024 #-fromEffect
//		end
//		camera shoppingCrossFadeCamera
//		  life 1.5 -sustain
//		  snapshot -onStart 
//		end
//		camera yoinking_camera
//		   control -target -orient
//		   heading .25
//		    pitch .01 -.01 .01 -.01 .01 -.01 .01 -.01 .01 -.01 .01
//		   roll 0 .1 0
//		   life 20 # how long the camera runs. Should be synched up to the life of the metaparticle below.
//		         # the curves for heading pitch and roll are played over life.
//		   nearClip .1
//		   farClip 10000
//		end
	
	// 0x18 view flags // no, actually 0x20?
	
	public int flags;  // control -target -orient  ?
	public short controlFlags;
	public float lifeTime = 1.0f;
	@StructureLength.Value(32) public final List<Float> yaw = new ArrayList<Float>();  // heading
	@StructureLength.Value(32) public final List<Float> pitch = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> roll = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> distance = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> fov = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> nearClip = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> farClip = new ArrayList<Float>();
	public final ResourceID cameraID = new ResourceID();
	public short cubemapResolution = 256;
	
	public CameraEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		CameraEffect effect = (CameraEffect) _effect;
		
		flags = effect.flags;
		controlFlags = effect.controlFlags;
		lifeTime = effect.lifeTime;
		
		yaw.addAll(effect.yaw);
		pitch.addAll(effect.pitch);
		roll.addAll(effect.roll);
		distance.addAll(effect.distance);
		fov.addAll(effect.fov);
		nearClip.addAll(effect.nearClip);
		farClip.addAll(effect.farClip);
		
		cameraID.copy(effect.cameraID);
		cubemapResolution = effect.cubemapResolution;
	}

	protected static class Parser extends EffectBlockParser<CameraEffect> {
		@Override
		protected CameraEffect createEffect(EffectDirectory effectDirectory) {
			return new CameraEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("control", ArgScriptParser.create((parser, line) -> {
				line.getArguments(args, 0);
				effect.flags |= FLAGS_CAMERA_CONTROL;
				
				if (line.hasFlag("orient")) effect.controlFlags |= CONTROL_FLAGS_ORIENT;
				if (line.hasFlag("orientHorizontal")) effect.controlFlags |= CONTROL_FLAGS_ORIENT_HORIZONTAL;
				if (line.hasFlag("relative")) effect.controlFlags |= CONTROL_FLAGS_RELATIVE;
				if (line.hasFlag("snap")) effect.controlFlags |= CONTROL_FLAGS_SNAP;
				if (line.hasFlag("target")) effect.controlFlags |= CONTROL_FLAGS_TARGET;
			}));
			
			this.addParser("snapshot", ArgScriptParser.create((parser, line) -> {
				line.getArguments(args, 0);
				
				if (line.hasFlag("onStart")) effect.flags |= FLAGS_SNAPSHOT_ON_START;
				if (line.hasFlag("onStop")) effect.flags |= FLAGS_SNAPSHOT_ON_STOP;
			}));
			
			this.addParser("cubemapSnapshot", ArgScriptParser.create((parser, line) -> {
				line.getArguments(args, 0);
				
				if (line.hasFlag("onStart")) effect.flags |= FLAGS_CUBEMAP_SNAPSHOT_ON_START;
				if (line.hasFlag("onStop")) effect.flags |= FLAGS_CUBEMAP_SNAPSHOT_ON_STOP;
				if (line.hasFlag("fromEffect")) effect.flags |= FLAGS_CUBEMAP_SNAPSHOT_FROM_EFFECT;
				
				if (line.getOptionArguments(args, "res", 1)) {
					effect.cubemapResolution = (short)(int)Optional.ofNullable(stream.parseInt(args, 0, 0, 65535)).orElse(0);
				}
			}));

			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.yaw.clear();
					stream.parseFloats(args, effect.yaw);
					effect.flags |= FLAGS_CAMERA_CONTROL;
				}
			}), "yaw", "heading");
			
			this.addParser("pitch", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.pitch.clear();
					stream.parseFloats(args, effect.pitch);
					effect.flags |= FLAGS_CAMERA_CONTROL;
				}
			}));
			
			this.addParser("roll", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.roll.clear();
					stream.parseFloats(args, effect.roll);
					effect.flags |= FLAGS_CAMERA_CONTROL;
				}
			}));
			
			this.addParser("distance", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.distance.clear();
					stream.parseFloats(args, effect.distance);
					effect.flags |= FLAGS_CAMERA_CONTROL;
					effect.controlFlags |= CONTROL_FLAGS_ZOOM;
				}
			}));
			
			this.addParser("fov", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.fov.clear();
					stream.parseFloats(args, effect.fov);
					effect.flags |= FLAGS_CAMERA_PARAMS;
				}
			}));
			
			this.addParser("nearClip", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.nearClip.clear();
					stream.parseFloats(args, effect.nearClip);
					effect.flags |= FLAGS_CAMERA_PARAMS;
				}
			}));
			
			this.addParser("farClip", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.farClip.clear();
					stream.parseFloats(args, effect.farClip);
					effect.flags |= FLAGS_CAMERA_PARAMS;
				}
			}));
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.lifeTime = value.floatValue();
					
					if (line.hasFlag("loop")) {
						effect.flags |= FLAGS_LOOP;
					}
					else if (line.hasFlag("single")) {
						effect.flags |= FLAGS_SINGLE;
					}
					else {
						line.hasFlag("sustain");
					}
				}
			}));
			
			this.addParser("switch", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					String[] originals = new String[2];
					effect.cameraID.parse(args, 0, originals);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_FILE, originals, 0);
					
					effect.flags |= FLAGS_NO_RESTORE;
				}
			}));
			
			this.addParser("select", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					String[] originals = new String[2];
					effect.cameraID.parse(args, 0, originals);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_FILE, originals, 0);
					
					effect.flags &= ~FLAGS_NO_RESTORE;
				}
			}));
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags |= value.intValue() & ~MASK_FLAGS;
				}
			}));
			this.addParser("controlFlags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.controlFlags |= (short)(value.intValue() & ~MASK_CONTROL_FLAGS);
				}
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
				
				CameraEffect effect = new CameraEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
				
				if (line.hasFlag("snapshot")) {
					effect.flags |= FLAGS_SNAPSHOT_ON_START;
				}
				
				data.addComponent(effect.toString(), effect);
				block.component = effect;
			}
			
			block.parse(stream, line, CameraEffect.class, args.size() == 0);
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return CameraEffect.class;
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
		
		@Override public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new CameraEffect(effectDirectory, version);
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
		
		if (!cameraID.isDefault() && !cameraID.isZero()) {
			if ((flags & FLAGS_NO_RESTORE) != 0) {
				writer.command("switch").arguments(cameraID);
			}
			else {
				writer.command("select").arguments(cameraID);
			}
		}
		
		if (((flags & FLAGS_SNAPSHOT_ON_START) != 0) ||
			 (flags & FLAGS_SNAPSHOT_ON_STOP) != 0) 
		{
			writer.command("snapshot");
			writer.flag("onStart", (flags & FLAGS_SNAPSHOT_ON_START) != 0);
			writer.flag("onStop", (flags & FLAGS_SNAPSHOT_ON_STOP) != 0);
		}
		if (((flags & FLAGS_CUBEMAP_SNAPSHOT_ON_START) != 0) ||
			 (flags & FLAGS_CUBEMAP_SNAPSHOT_ON_STOP) != 0) 
		{
			writer.command("cubemapSnapshot");
			writer.flag("onStart", (flags & FLAGS_CUBEMAP_SNAPSHOT_ON_START) != 0);
			writer.flag("onStop", (flags & FLAGS_CUBEMAP_SNAPSHOT_ON_STOP) != 0);
			writer.flag("fromEffect", (flags & FLAGS_CUBEMAP_SNAPSHOT_FROM_EFFECT) != 0);
			if (cubemapResolution != 256) writer.option("res").ints(cubemapResolution);
		}
		
		if ((controlFlags & (MASK_CONTROL_FLAGS & ~CONTROL_FLAGS_ZOOM)) != 0) {
			writer.command("control");
			writer.flag("target", (controlFlags & CONTROL_FLAGS_TARGET) != 0);
			writer.flag("orient", (controlFlags & CONTROL_FLAGS_ORIENT) != 0);
			writer.flag("orientHorizontal", (controlFlags & CONTROL_FLAGS_ORIENT_HORIZONTAL) != 0);
			writer.flag("snap", (controlFlags & CONTROL_FLAGS_SNAP) != 0);
			writer.flag("relative", (controlFlags & CONTROL_FLAGS_RELATIVE) != 0);
		}
		
		writer.command("life").floats(lifeTime);
		writer.flag("loop", (flags & FLAGS_LOOP) != 0);
		writer.flag("single", (flags & FLAGS_SINGLE) != 0);
		writer.flag("sustain", (flags & (FLAGS_LOOP | FLAGS_SINGLE)) == 0);
		
		if (!yaw.isEmpty())		writer.command("heading").floats(yaw);
		if (!pitch.isEmpty())	writer.command("pitch").floats(pitch);
		if (!roll.isEmpty())	writer.command("roll").floats(roll);
		if (!distance.isEmpty())	writer.command("distance").floats(distance);
		
		if (!fov.isEmpty())		writer.command("fov").floats(fov);
		if (!nearClip.isEmpty())	writer.command("nearClip").floats(nearClip);
		if (!farClip.isEmpty())	writer.command("farClip").floats(farClip);


		int maskedFlags = flags & ~MASK_FLAGS;
		int maskedControlFlags = flags & ~MASK_CONTROL_FLAGS;
		if (maskedFlags != 0) {
			writer.command("flags").arguments(HashManager.get().hexToString(maskedFlags));
		}
		if (maskedControlFlags != 0) {
			writer.option("controlFlags").arguments(HashManager.get().hexToString(maskedControlFlags));
		}
		
		writer.endBlock().commandEND();
	}
}
