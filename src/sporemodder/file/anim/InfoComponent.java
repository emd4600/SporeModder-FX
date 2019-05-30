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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptWriter;

public class InfoComponent implements AbstractComponentKeyframe {
	
	public static final int HEADER = 0x4F464E49;
	public static final int TYPE = 0;
	public static final String KEYWORD = "info";
	
	// Always < animation.length
	public int time;
	// 4 bytes, unknown. Maybe index of channel in whole game?
	public int vfxStartIndex;  // int16
	public int vfxCount;  // int8
	// 1 byte padding
	public int flags;
	// 4 bytes padding ?

	@Override public void read(StreamReader stream) throws IOException {
		time = stream.readLEInt();
		stream.skip(4);
		vfxStartIndex = stream.readLEUShort();
		vfxCount = stream.readUByte();
		stream.skip(1);
		flags = stream.readLEInt();
	}

	@Override public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(time);
		stream.writeLEInt(0);  // ?
		stream.writeLEUShort(vfxStartIndex);
		stream.writeUByte(vfxCount);
		stream.writePadding(1);
		stream.writeLEInt(flags);
		stream.writePadding(4);
	}

	@Override public void toArgScript(ArgScriptWriter writer, SPAnimation animation) {
		writer.ints(time);
		if (flags != 0) {
			writer.option("flags").arguments("0x" + Integer.toHexString(flags));
		}
		if (vfxCount > 0) {
			writer.option("vfx");
			for (int i = 0; i < vfxCount; ++i) {
				writer.arguments("vfx" + (vfxStartIndex+i));
			}
		}
	}

	public static ArgScriptParser<SPAnimation> createParser(AnimChannelParser channelParser) {
		return new ArgScriptSpecialBlock<SPAnimation>() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			AnimationComponentData compData;
			// We want to show an error in the component line when keyframes number don't match,
			// but we only know that after parsing the whole component
			DocumentError keyframesError;

			@Override public void onBlockEnd() {
				if (!channelParser.channel.components.isEmpty() 
						&& compData.keyframes.size() != channelParser.channel.components.get(0).keyframes.size()) {
					stream.addError(keyframesError);
				}
					
				stream.endSpecialBlock();
			}

			@Override public void parse(ArgScriptLine line) {
				compData = new AnimationComponentData();
				compData.flags = TYPE;
				compData.index = 0;
				compData.id = HEADER;
				channelParser.channel.components.add(compData);
				
				line.getArguments(args, 0);
				
				Number value;
				if (line.getOptionArguments(args, "flags", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					compData.flags |= value.intValue();
				}
				
				// Save for later
				keyframesError = line.createError(AnimChannelParser.KEYFRAMES_ERROR);
				keyframesError.setLine(stream.getCurrentLine());
				
				stream.startSpecialBlock(this, "end");
			}
			
			@Override public boolean processLine(String line) {
				ArgScriptLine l = preprocess(line);
				l.getSplitsAsArguments(args);
				l.setHasKeyword(false);
				
				InfoComponent c = new InfoComponent();
				compData.keyframes.add(c);
				
				Number value;
				if ((value = stream.parseInt(args, 0, 0, (int) (getData().length - 1))) != null) {
					c.time = value.intValue();
				}
				
				if (l.getOptionArguments(args, "flags", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					c.flags |= value.intValue();
				}
				
				if (l.getOptionArguments(args, "vfx", 1, Integer.MAX_VALUE)) {
					c.vfxStartIndex = stream.getData().vfxList.size();
					c.vfxCount = args.size();
					
					for (int i = 0; i < c.vfxCount; ++i) {
						AnimationVFX vfx = stream.getData().vfxMap.get(args.get(i));
						if (vfx != null) {
							stream.getData().vfxList.add(vfx);
						} 
						else {
							stream.addError(l.createErrorForOptionArgument("vfx", args.get(i) + " is not a defined vfx.", 1 + i));
						}
					}
				}
				
				stream.addSyntax(l, false);
				
				return true;
			}
		};
	}
}
