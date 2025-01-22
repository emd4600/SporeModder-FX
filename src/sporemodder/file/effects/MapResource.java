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

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.util.Vector4;

@Structure(StructureEndian.BIG_ENDIAN)
public class MapResource extends EffectResource {

	/** The structure metadata used for reading/writing this class. */
	public static final StructureMetadata<MapResource> STRUCTURE_METADATA = StructureMetadata.generate(MapResource.class);
	
	public static final String KEYWORD = "effectMap";
	public static final int TYPE_CODE = 0x0000;
	
	public static final EffectResourceFactory FACTORY = new Factory();
	
	
	public static final ArgScriptEnum ENUM_CHANNEL = new ArgScriptEnum();
	static {
		ENUM_CHANNEL.add(0, "blue");
		ENUM_CHANNEL.add(1, "green");
		ENUM_CHANNEL.add(2, "red");
		ENUM_CHANNEL.add(3, "alpha");
		ENUM_CHANNEL.add(4, "all");
	}
	
	public static final ArgScriptEnum ENUM_MAPTYPE = new ArgScriptEnum();
	static {
		//ENUM_MAPTYPE.add(0, "test");
		ENUM_MAPTYPE.add(1, "bitImage");
		ENUM_MAPTYPE.add(2, "monoImage");
		ENUM_MAPTYPE.add(3, "image");
		ENUM_MAPTYPE.add(4, "advectImage");
		ENUM_MAPTYPE.add(5, "forceImage");
		//ENUM_MAPTYPE.add(6, "op");
		ENUM_MAPTYPE.add(7, "bitCubeImage");
		ENUM_MAPTYPE.add(8, "monoCubeImage");
	}
	public static byte TYPE_TEST = 0;
	public static byte TYPE_OP = 6;
	
	public static final ArgScriptEnum ENUM_OPTYPE = new ArgScriptEnum();
	static {
		ENUM_OPTYPE.add(0, "set");
		ENUM_OPTYPE.add(1, "add");
		ENUM_OPTYPE.add(2, "multiply");
		ENUM_OPTYPE.add(3, "subtract");
		ENUM_OPTYPE.add(4, "min");
		ENUM_OPTYPE.add(5, "max");
		ENUM_OPTYPE.add(6, "multiplyAdd");
	}
	
	public static final int MAPTYPE_1BIT = 1;  // ? .#03E421E9 files
	public static final int MAPTYPE_8BIT = 2;  // ? .#03E421EC files
	public static final int MAPTYPE_32BIT = 3;  // ?
	public static final int MAPTYPE_NONE = 6;  // ?
	
	public static final int FLAG_WORLD_SPACE = 1;
	public static final int FLAG_TILE = 2;
	public static final int ARG_RES_0 = 0x4;  // ?
	public static final int ARG_RES_1 = 0x8;  // ?
	public static final int ARG_RES_2 = 0x10;  // ?
	public static final int ARG_RES_3 = 0x20;  // ?
	public static final int ARG_VALUE_0 = 0x40;  // ?
	public static final int ARG_VALUE_1 = 0x80;  // ?
	public static final int ARG_VALUE_2 = 0x100;  // ?
	public static final int ARG_VALUE_3 = 0x200;  // ?
	public static final int ARG_MASK = 0x3FC;
	public static final int[] ARG_RES = new int[] { ARG_RES_0, ARG_RES_1, ARG_RES_2, ARG_RES_3 };
	public static final int[] ARG_VALUE = new int[] { ARG_VALUE_0, ARG_VALUE_1, ARG_VALUE_2, ARG_VALUE_3 };
	
	public static final int FLAGMASK = FLAG_WORLD_SPACE | FLAG_TILE | ARG_MASK;
	
	
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
				if (data.hasResource(args.get(0))) {
					stream.addError(line.createErrorForArgument("A resource with this name already exists in this file.", 0));
				}
				data.addResource(args.get(0), resource);
			}
			
			data.setPosition(resource, stream.getLinePositions().get(stream.getCurrentLine()));
			
			if (line.hasFlag("tile")) {
				resource.flags |= FLAG_TILE;
			}
			if (line.hasFlag("worldSpace")) {
				resource.flags |= FLAG_WORLD_SPACE;
			}
			
			boolean hasImage = false;
			Collection<String> keys = ENUM_MAPTYPE.getKeys(); 
			
			if (!keys.stream().anyMatch(s -> line.hasOption(s)) &&
					!line.hasOption("op") && !line.hasOption("test"))
			{
				stream.addError(line.createError("No map type specified. Map types are '-op', '-bitImage', '-monoImage', '-image', '-advectImage', '-forceImage', '-bitCubeImage', '-monoCubeImage'"));
			}
			else
			{
				for (String key : keys) {
					if (line.getOptionArguments(args, key, 1)) {
						String[] words = new String[2];
						resource.imageID.parse(args, 0, words);
						resource.mapType = (byte) ENUM_MAPTYPE.get(key);
						line.addHyperlinkForOptionArgument(EffectDirectory.HYPERLINK_IMAGEMAP, words, key, 0);
						hasImage = true;
						break;
					}
				}
				
				if (!hasImage) {
					if (line.getOptionArguments(args, "op", 2, 5)) {
						resource.mapType = TYPE_OP;
						resource.opType = (byte)ENUM_OPTYPE.get(args, 0);
						
						// The values can either be other maps, which can be names or 0x hashes, or numerical vector4 values
						for (int i = 1; i < args.size(); i++) 
						{
							if (args.get(i).contains(",")) {
								float[] arr = new float[4];
								stream.parseVector4(args, i, arr);
								
								resource.flags |= ARG_VALUE[i-1];
								resource.opArgValues[i-1] = new Vector4(arr);
							}
							else
							{
								resource.flags |= ARG_RES[i-1];
								resource.opArgMaps[i-1] = new ResourceID();
								String[] words = new String[2];
								if (resource.opArgMaps[i-1].parse(args, i)) {
									line.addHyperlinkForOptionArgument(EffectDirectory.HYPERLINK_IMAGEMAP, words, "op", i);
								}
							}
						}
					}
					else {
						line.hasFlag("test");
						resource.mapType = TYPE_TEST;
					}
				}
			}
			
			if (line.getOptionArguments(args, "channel", 1)) {
				resource.channel = (byte) ENUM_CHANNEL.get(args, 0);
			}
			
			if (line.getOptionArguments(args, "rect", 1)) {
				stream.parseVector4(args, 0, resource.bounds);
			}
			
			if (line.getOptionArguments(args, "flags", 1) && (value = stream.parseInt(args, 0)) != null) {
				// We don't want the user to modify the arg flags
				resource.flags |= (value.intValue() & ~FLAGMASK);
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
		
		writer.option("channel").arguments(ENUM_CHANNEL.get(channel));
		writer.option("rect").vector(bounds);
		
		writer.flag("tile", (flags & FLAG_TILE) != 0);
		writer.flag("worldSpace", (flags & FLAG_WORLD_SPACE) != 0);
		
		if (mapType == TYPE_TEST)
		{
			writer.option("test");
		}
		else if (mapType == TYPE_OP)
		{
			writer.option("op").arguments(ENUM_OPTYPE.get(opType));
			
			for (int i = 0; i < 4; i++) {
				if ((flags & ARG_VALUE[i]) == ARG_VALUE[i]) {
					writer.vector4(opArgValues[i]);
				}
				else if ((flags & ARG_RES[i]) == ARG_RES[i]) {
					writer.arguments(opArgMaps[i]);
				}
				else {
					break;
				}
			}
		}
		else
		{
			writer.option(ENUM_MAPTYPE.get(mapType)).arguments(imageID);
		}
		
		if ((flags & ~FLAGMASK) != 0) writer.option("flags").arguments("0x" + Integer.toHexString(flags & ~FLAGMASK));
	}
}
