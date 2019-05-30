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
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.Vector4;

public class RotComponent implements AbstractComponentKeyframe {
	public static final int HEADER = 0x544F52;
	public static final int TYPE = 2;
	public static final String KEYWORD = "rot";
	
	// size 64h
	
	public final Vector4 rot = new Vector4();
	public float weight = 1.0f;  // just a guess, but it's usually 1
	public int nextMode;
	public int previousMode;
	public final Interpolator scaleInterpolator = new Interpolator();
	
	@Override public void read(StreamReader stream) throws IOException {
		long offset = stream.getFilePointer();
		
		rot.readLE(stream);
		weight = stream.readLEFloat();
		
		// ?? unused?
		stream.skip(8);
		// from 0 to 2
		nextMode = stream.readUByte();
		previousMode = stream.readUByte();
		
		stream.seek(offset + 0x54);
		scaleInterpolator.read(stream);
	}
	
	@Override public void write(StreamWriter stream) throws IOException {
		rot.writeLE(stream);
		stream.writeLEFloat(weight);
		stream.writePadding(8);
		stream.writeUByte(nextMode);
		stream.writeUByte(previousMode);
		// we are at 1Eh now
		stream.writePadding(0x54 - 0x1E);
		scaleInterpolator.write(stream);
	}
	
	@Override public void toArgScript(ArgScriptWriter writer, SPAnimation animation) {
		writer.vector4(rot);
		if (weight != 1.0f) writer.floats(weight);
		
		if (nextMode != 0 || previousMode != 0) {
			writer.option("rot").ints(previousMode, nextMode);
		}
		
		if (!scaleInterpolator.isDefault()) {
			scaleInterpolator.toArgScript(writer, "scale");
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
				compData.index = 2;
				compData.id = HEADER;
				channelParser.channel.components.add(compData);
				
				if (!line.hasFlag("disable")) compData.flags |= AnimationComponentData.FLAG_USED;
				
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
				
				RotComponent c = new RotComponent();
				compData.keyframes.add(c);
				
				float[] values = new float[4];
				if (stream.parseVector4(args, 0, values)) {
					c.rot.setX(values[0]);
					c.rot.setY(values[1]);
					c.rot.setZ(values[2]);
					c.rot.setW(values[3]);
				}
				
				if (args.size() > 1 && !args.get(1).startsWith("-")) {
					Number value;
					if ((value = stream.parseFloat(args, 1)) != null) {
						c.weight = value.floatValue();
					}
				}
				
				if (l.getOptionArguments(args, "rot", 2)) {
					Number value;
					if ((value = stream.parseInt(args, 1, 0, 2)) != null) {
						c.previousMode = value.intValue();
					}
					if ((value = stream.parseFloat(args, 1, 0, 2)) != null) {
						c.nextMode = value.intValue();
					}
				}
				
				c.scaleInterpolator.parse(l, "weight");
				
				stream.addSyntax(l, false);
				
				return true;
			}
		};
	}
}
