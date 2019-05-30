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
package sporemodder.file.anim;

import java.io.IOException;

import emord.filestructures.Stream.StringEncoding;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class AnimationVFX {
	// size 60h
	// 00h: flags ?
	
	// 3Ch: id
	// 40h: offset to name
	
	public static class Selector {
		public int field_0;
		public int field_4;
		public int field_8;
		
		public boolean isEmpty() {
			return field_0 == 0 && field_4 == 0 && field_8 == 0;
		}
	}
	
	public int flags;
	public Selector[] selectors = new Selector[4];
	public int id;
	public String field_34;
	public String name;
	public float field_44;
	
	public AnimationVFX() {
		for (int i = 0; i < 4; ++i) {
			selectors[i] = new Selector();
		}
	}
	
	public void read(DataStructure stream) throws IOException {
		stream.getStream().seek(stream.getPointer());
		flags = stream.getStream().readLEInt();
		
		for (int i = 0; i < 4; ++i) {
			selectors[i].field_0 = stream.getStream().readLEInt();
			selectors[i].field_4 = stream.getStream().readLEInt();
			selectors[i].field_8 = stream.getStream().readLEInt();
		}
		
		field_34 = stream.getStream().readString(StringEncoding.ASCII, 4);
		
		id = stream.getInt(0x3C);
		long offset = stream.getUInt(0x40);
		field_44 = stream.getFloat(0x44);
		
		stream.getStream().seek(offset);
		name = stream.getStream().readCString(StringEncoding.ASCII);
	}
	
	public void toArgScript(ArgScriptWriter writer, String internal_name) {
		writer.command("vfx").arguments(internal_name, name);
		
		if (HashManager.get().fnvHash(name) != id) {
			writer.option("id").arguments(HashManager.get().getFileName(id));
		}
		
		if (flags != 0) {
			writer.option("flags").arguments("0x" + Integer.toHexString(flags));
		}
		
		for (int i = 0; i < 4; ++i) {
			Selector s = selectors[i];
			if (!s.isEmpty()) {
				writer.option("unk" + (i+1)).arguments("0x" + Integer.toHexString(s.field_0), s.field_4, s.field_8);
			}
		}
		
		if (!field_34.isEmpty()) writer.option("field_34").arguments(field_34);
		if (field_44 != 0) writer.option("field_44").floats(field_44);
	}
	
	public static void addParser(ArgScriptStream<SPAnimation> stream) {
		stream.addParser("vfx", ArgScriptParser.create((parser, line) -> {
			final ArgScriptArguments args = new ArgScriptArguments();
			AnimationVFX vfx = new AnimationVFX();
			Number value;
			
			if (line.getArguments(args, 2)) {
				vfx.name = args.get(1);
				vfx.id = HashManager.get().getFileHash(vfx.name);
				
				stream.getData().vfxMap.put(args.get(0), vfx);
			}
			
			for (int i = 0; i < 4; ++i) {
				if (line.getOptionArguments(args, "unk" + (1+i), 3)) {
					if ((value = stream.parseInt(args, 0)) != null) {
						vfx.selectors[i].field_0 = value.intValue();
					}
					if ((value = stream.parseInt(args, 1)) != null) {
						vfx.selectors[i].field_4 = value.intValue();
					}
					if ((value = stream.parseInt(args, 2)) != null) {
						vfx.selectors[i].field_8 = value.intValue();
					}
				}
			}
			
			if (line.getOptionArguments(args, "id", 1) && 
					(value = stream.parseFileID(args, 0)) != null) {
				vfx.id = value.intValue();
			}
			
			if (line.getOptionArguments(args, "flags", 1) && 
					(value = stream.parseInt(args, 0)) != null) {
				vfx.flags = value.intValue();
			}
			
			if (line.getOptionArguments(args, "field_44", 1) && 
					(value = stream.parseFloat(args, 0)) != null) {
				vfx.field_44 = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "field_34", 1)) {
				if (args.get(0).length() > 4) {
					stream.addError(line.createErrorForArgument("field_34 can only have up to 4 characters", 1));
				}
				vfx.field_34 = args.get(0);
			}
		}));
	}
}
