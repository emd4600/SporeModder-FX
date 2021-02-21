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
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
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
	
	public int flags;  // control -target -orient  ?
	public short viewFlags;
	public float lifeTime;
	@StructureLength.Value(32) public final List<Float> yaw = new ArrayList<Float>();  // heading
	@StructureLength.Value(32) public final List<Float> pitch = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> roll = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> distance = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> fov = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> nearClip = new ArrayList<Float>();
	@StructureLength.Value(32) public final List<Float> farClip = new ArrayList<Float>();
	public final ResourceID cameraID = new ResourceID();
	public short cubemapResource;
	
	public CameraEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		CameraEffect effect = (CameraEffect) _effect;
		
		flags = effect.flags;
		viewFlags = effect.viewFlags;
		lifeTime = effect.lifeTime;
		
		yaw.addAll(effect.yaw);
		pitch.addAll(effect.pitch);
		roll.addAll(effect.roll);
		distance.addAll(effect.distance);
		fov.addAll(effect.fov);
		nearClip.addAll(effect.nearClip);
		farClip.addAll(effect.farClip);
		
		cameraID.copy(effect.cameraID);
		cubemapResource = effect.cubemapResource;
	}

	protected static class Parser extends EffectBlockParser<CameraEffect> {
		@Override
		protected CameraEffect createEffect(EffectDirectory effectDirectory) {
			return new CameraEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();

			this.addParser(ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.yaw.clear();
					stream.parseFloats(args, effect.yaw);
				}
			}), "yaw", "heading");
			
			this.addParser("pitch", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.pitch.clear();
					stream.parseFloats(args, effect.pitch);
				}
			}));
			
			this.addParser("roll", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.roll.clear();
					stream.parseFloats(args, effect.roll);
				}
			}));
			
			this.addParser("distance", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.distance.clear();
					stream.parseFloats(args, effect.distance);
				}
			}));
			
			this.addParser("fov", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.fov.clear();
					stream.parseFloats(args, effect.fov);
				}
			}));
			
			this.addParser("nearClip", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.nearClip.clear();
					stream.parseFloats(args, effect.nearClip);
				}
			}));
			
			this.addParser("farClip", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					effect.farClip.clear();
					stream.parseFloats(args, effect.farClip);
				}
			}));
			
			this.addParser("cameraID", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					String[] originals = new String[2];
					effect.cameraID.parse(args, 0, originals);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_FILE, originals, 0);
				}
			}));
			
			this.addParser("cubemapResource", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.cubemapResource = value.shortValue();
				}
			}));
			
			this.addParser("life", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.lifeTime = value.floatValue();
				}
			}));
			
			this.addParser("flags", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.flags = value.intValue();
				}
				if (line.getOptionArguments(args, "view", 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.viewFlags = value.shortValue();
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
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, CameraEffect.class));
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
		
		if (flags != 0 || viewFlags != 0) {
			writer.command("flags").arguments(HashManager.get().hexToString(flags));
			if (viewFlags != 0) writer.option("view").arguments(HashManager.get().hexToString(viewFlags));
		}
		
		writer.command("life").floats(lifeTime);
		if (!yaw.isEmpty())		writer.command("yaw").floats(yaw);
		if (!pitch.isEmpty())	writer.command("pitch").floats(pitch);
		if (!roll.isEmpty())	writer.command("roll").floats(roll);
		if (!distance.isEmpty())	writer.command("distance").floats(distance);
		if (!fov.isEmpty())		writer.command("fov").floats(fov);
		if (!nearClip.isEmpty())	writer.command("nearClip").floats(nearClip);
		if (!farClip.isEmpty())	writer.command("farClip").floats(farClip);
		
		if (!cameraID.isDefault()) writer.command("cameraId").arguments(cameraID);
		if (cubemapResource != 0) writer.command("cubemapResource").ints(cubemapResource);
		
		writer.endBlock().commandEND();
	}
}
