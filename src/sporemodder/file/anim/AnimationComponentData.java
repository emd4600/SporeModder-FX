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
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;

public class AnimationComponentData {
	
	public static final int SIZE = 32;
	
	/** If this flag is not present, the component is ignored. INFO component does not need this. */
	public static final int FLAG_USED = 0x10;
	public static final int FLAG_RELATIVE = 0x20;
	public static final int SCALE_CREATURESIZE = 0x40;
	public static final int SCALE_LIMBLENGTH = 0x80;
	public static final int FLAG_SCALE_MASK = 0xC0;
	public static final int FLAG_MASK = FLAG_USED | FLAG_RELATIVE | FLAG_SCALE_MASK;
	
	public static final ArgScriptEnum ENUM_SCALE = new ArgScriptEnum();
	static {
		ENUM_SCALE.add(SCALE_CREATURESIZE, "CreatureSize");
		ENUM_SCALE.add(SCALE_LIMBLENGTH, "LimbLength");
	}

	public int flags;
	public int id;
	public long keyframeOffset;
	public int keyframeStride;
	// index, from 0 to 8
	public int index;
	public final List<AbstractComponentKeyframe> keyframes = new ArrayList<>();
	
	public int getType() {
		return flags & 0xF;
	}
	
	public void read(DataStructure stream, long dataPtr) throws IOException {
		// 00h: flags. If not & 10h, no interpolation?, & Fh is type, 1: position, 2: rotation, 3: ?
		flags = stream.getInt(0);
		id = stream.getInt(4);
		keyframeOffset = stream.getInt(8);
		keyframeStride = stream.getInt(12);
		index = stream.getInt(16);
		
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(flags);
		stream.writeLEInt(id);
		stream.writeLEUInt(keyframeOffset);
		stream.writeLEInt(keyframeStride);
		stream.writeLEInt(index);
		stream.writePadding(12);
	}
	
	public AbstractComponentKeyframe createKeyframe() {
		int type = flags & 0xF;
		switch (type) {
		case InfoComponent.TYPE: return new InfoComponent();
		case PosComponent.TYPE: return new PosComponent();
		case RotComponent.TYPE: return new RotComponent();
		case RigblockComponent.TYPE: return new RigblockComponent();
		default: throw new UnsupportedOperationException("Anim component " + type + " is not supported.");
		}
	}
	
	public void parseFlags(ArgScriptLine line) {
		if (!line.hasFlag("disable")) flags |= AnimationComponentData.FLAG_USED;
		if (line.hasFlag("relative")) flags |= AnimationComponentData.FLAG_RELATIVE;
		
		final ArgScriptArguments args = new ArgScriptArguments();
		
		if (line.getOptionArguments(args, "scaleMode", 1)) {
			flags |= ENUM_SCALE.get(args, 0);
		}
	}
}
