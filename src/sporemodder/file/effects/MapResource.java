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
import java.util.Collection;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureFieldEndian;
import emord.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.Vector4;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class MapResource extends EffectResource {

	/** The structure metadata used for reading/writing this class. */
	public static final StructureMetadata<MapResource> STRUCTURE_METADATA = StructureMetadata.generate(MapResource.class);
	
	public static final String KEYWORD = "effectMap";
	public static final int TYPE_CODE = 0x0000;
	
	public static final EffectResourceFactory FACTORY = new Factory();
	
	
	public static final ArgScriptEnum ENUM_CHANNEL = new ArgScriptEnum();
	static {
		ENUM_CHANNEL.add(0, "red");
		ENUM_CHANNEL.add(1, "green");
		ENUM_CHANNEL.add(2, "blue");
		ENUM_CHANNEL.add(3, "alpha");
		ENUM_CHANNEL.add(4, "all");
	}
	
	public static final ArgScriptEnum ENUM_MAPTYPE = new ArgScriptEnum();
	static {
		ENUM_MAPTYPE.add(0, "imageUnk0");
		ENUM_MAPTYPE.add(1, "bitImage");
		ENUM_MAPTYPE.add(2, "bitImage8");
		ENUM_MAPTYPE.add(3, "bitImage32");
		ENUM_MAPTYPE.add(4, "imageUnk4");
		ENUM_MAPTYPE.add(5, "imageUnk5");
	}
	
	public static final ArgScriptEnum ENUM_OPTYPE = new ArgScriptEnum();
	static {
		ENUM_OPTYPE.add(0, "set");
		ENUM_OPTYPE.add(1, "add");
		ENUM_OPTYPE.add(2, "mul");
		ENUM_OPTYPE.add(3, "sub");
		ENUM_OPTYPE.add(4, "max");
		ENUM_OPTYPE.add(5, "min");
		ENUM_OPTYPE.add(6, "mad");
	}
	
	public static final int MAPTYPE_1BIT = 1;  // ? .#03E421E9 files
	public static final int MAPTYPE_8BIT = 2;  // ? .#03E421EC files
	public static final int MAPTYPE_32BIT = 3;  // ?
	public static final int MAPTYPE_NONE = 6;  // ?
	
	public static final int ARG_RES_0 = 0x4;  // ?
	public static final int ARG_RES_1 = 0x8;  // ?
	public static final int ARG_RES_2 = 0x10;  // ?
	public static final int ARG_RES_3 = 0x20;  // ?
	public static final int ARG_VALUE_0 = 0x40;  // ?
	public static final int ARG_VALUE_1 = 0x80;  // ?
	public static final int ARG_VALUE_2 = 0x100;  // ?
	public static final int ARG_VALUE_3 = 0x200;  // ?
	public static final int ARG_MASK = 0x3FC;
	
	
	public int flags;
	public byte mapType = 6;
	public final ResourceID imageID = new ResourceID();
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] bounds = new float[4];
	public byte channel = 4;
	public byte opType;
	
	// If we don't instatiate them we get errors
	public final ResourceID[] opArgMaps = new ResourceID[] {new ResourceID(), new ResourceID(), new ResourceID(), new ResourceID()};
	public final Vector4[] opArgValues = new Vector4[] {new Vector4(), new Vector4(), new Vector4(), new Vector4()};
	
	public MapResource(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	protected static class Parser extends ArgScriptParser<EffectUnit> {

		@Override
		public void parse(ArgScriptLine line) {
			MapResource resource = new MapResource(data.getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
			Number value = null;
			
			if (line.getArguments(args, 1)) {
				resource.resourceID.parse(args, 0);
				data.addResource(args.get(0), resource);
			}
			
			data.setPosition(resource, stream.getLinePositions().get(stream.getCurrentLine()));
			
			Collection<String> keys = ENUM_MAPTYPE.getKeys(); 
			for (String key : keys) {
				if (line.getOptionArguments(args, key, 1)) {
					String[] words = new String[2];
					resource.imageID.parse(args, 0, words);
					resource.mapType = (byte) ENUM_MAPTYPE.get(key);
					line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_IMAGEMAP, words, key, 0);
					break;
				}
			}
			
			if (line.getOptionArguments(args, "channel", 1)) {
				resource.channel = (byte) ENUM_CHANNEL.get(args, 0);
			}
			
			if (line.getOptionArguments(args, "rect", 1)) {
				stream.parseVector4(args, 0, resource.bounds);
			}
			
			if (line.getOptionArguments(args, "map0", 1)) {
				resource.opArgMaps[0] = new ResourceID();
				String[] words = new String[2];
				if (resource.opArgMaps[0].parse(args, 0)) {
					line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_IMAGEMAP, words, "map0", 0);
					resource.flags |= ARG_RES_0;
				}
			}
			
			if (line.getOptionArguments(args, "map1", 1)) {
				resource.opArgMaps[1] = new ResourceID();
				String[] words = new String[2];
				if (resource.opArgMaps[1].parse(args, 0)) {
					line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_IMAGEMAP, words, "map1", 0);
					resource.flags |= ARG_RES_1;
				}
			}
			
			if (line.getOptionArguments(args, "map2", 1)) {
				resource.opArgMaps[2] = new ResourceID();
				String[] words = new String[2];
				if (resource.opArgMaps[2].parse(args, 0)) {
					line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_IMAGEMAP, words, "map2", 0);
					resource.flags |= ARG_RES_2;
				}
			}
			
			if (line.getOptionArguments(args, "map3", 1)) {
				resource.opArgMaps[3] = new ResourceID();
				String[] words = new String[2];
				if (resource.opArgMaps[3].parse(args, 0)) {
					line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_IMAGEMAP, words, "map3", 0);
					resource.flags |= ARG_RES_3;
				}
			}
			
			if (line.getOptionArguments(args, "value0", 1)) {
				resource.flags |= ARG_VALUE_0;
				
				float[] arr = new float[4];
				
				if (args.get(0).contains("(")) {
					stream.parseVector4(args, 0, arr);
				}
				else if ((value = stream.parseFloat(args, 0)) != null) {
					arr[0] = arr[1] = arr[2] = arr[3] = value.floatValue();
				}
				
				resource.opArgValues[0] = new Vector4(arr);
			}
			
			if (line.getOptionArguments(args, "value1", 1)) {
				resource.flags |= ARG_VALUE_1;
				
				float[] arr = new float[4];
				stream.parseVector4(args, 0, arr);
				
				resource.opArgValues[1] = new Vector4(arr);
			}
			
			if (line.getOptionArguments(args, "value2", 1)) {
				resource.flags |= ARG_VALUE_2;
				
				float[] arr = new float[4];
				stream.parseVector4(args, 0, arr);
				
				resource.opArgValues[2] = new Vector4(arr);
			}
			
			if (line.getOptionArguments(args, "value3", 1)) {
				resource.flags |= ARG_VALUE_3;
				
				float[] arr = new float[4];
				stream.parseVector4(args, 0, arr);
				
				resource.opArgValues[3] = new Vector4(arr);
			}
			
			if (line.getOptionArguments(args, "op", 1)) {
				resource.opType = (byte) ENUM_OPTYPE.get(args, 0);
			}
			
			if (line.getOptionArguments(args, "flags", 1) && (value = stream.parseInt(args, 0)) != null) {
				// We don't want the user to modify the arg flags
				resource.flags |= (value.intValue() & ~ARG_MASK);
			}
		}
		
	}
	
	public static class Factory implements EffectResourceFactory {
		
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public void addParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}

		@Override
		public int getMinVersion() {
			return 0;
		}

		@Override
		public int getMaxVersion() {
			return 0;
		}

		@Override
		public EffectResource create(EffectDirectory effectDirectory, int version) {
			return new MapResource(effectDirectory, version);
		}

		@Override public String getKeyword() {
			return KEYWORD;
		}
	}

	@Override
	public EffectResourceFactory getFactory() {
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
		writer.command(KEYWORD).arguments(resourceID);
		
		if (!imageID.isDefault()) writer.option(ENUM_MAPTYPE.get(mapType)).arguments(imageID);
		
		writer.option("channel").arguments(ENUM_CHANNEL.get(channel));
		writer.option("rect").vector(bounds);
		
		if ((flags & ARG_RES_0) == ARG_RES_0) writer.option("map0").arguments(opArgMaps[0]);
		if ((flags & ARG_RES_1) == ARG_RES_1) writer.option("map1").arguments(opArgMaps[1]);
		if ((flags & ARG_RES_2) == ARG_RES_2) writer.option("map2").arguments(opArgMaps[2]);
		if ((flags & ARG_RES_3) == ARG_RES_3) writer.option("map3").arguments(opArgMaps[3]);
		
		if ((flags & ARG_VALUE_0) == ARG_VALUE_0) {
			if (opArgValues[0].allEqual()) writer.option("value0").floats(opArgValues[0].getX());
			else writer.option("value0").vector4(opArgValues[0]);
		}
		if ((flags & ARG_VALUE_1) == ARG_VALUE_1) {
			if (opArgValues[1].allEqual()) writer.option("value1").floats(opArgValues[1].getX());
			else writer.option("value1").vector4(opArgValues[1]);
		}
		if ((flags & ARG_VALUE_2) == ARG_VALUE_2) {
			if (opArgValues[2].allEqual()) writer.option("value2").floats(opArgValues[2].getX());
			else writer.option("value2").vector4(opArgValues[2]);
		}
		if ((flags & ARG_VALUE_3) == ARG_VALUE_3) {
			if (opArgValues[3].allEqual()) writer.option("value3").floats(opArgValues[3].getX());
			else writer.option("value3").vector4(opArgValues[3]);
		}
		
		if ((flags & ARG_MASK) != 0) writer.option("op").arguments(ENUM_OPTYPE.get(opType));
		
		if ((flags & ~ARG_MASK) != 0) writer.option("flags").arguments("0x" + Integer.toHexString(flags & ~ARG_MASK));
	}
}
