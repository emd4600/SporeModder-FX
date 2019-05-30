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
import sporemodder.util.Vector3;

public class PosComponent implements AbstractComponentKeyframe {

	public static final int HEADER = 0x534F50;
	public static final int TYPE = 1;
	public static final String KEYWORD = "pos";
	private static final String[] INTERPOLATOR_NAMES = new String[]{"x", "y", "z", "weight"};
	
	public final Vector3 pos = new Vector3();
	public float weight = 1.0f;  // just a guess, but it's usually 1
	public final Interpolator[] interpolators = new Interpolator[4];
	
	public PosComponent() {
		for (int i = 0; i < interpolators.length; ++i) {
			interpolators[i] = new Interpolator();
		}
	}
	
	@Override public void read(StreamReader stream) throws IOException {
		pos.readLE(stream);
		weight = stream.readLEFloat();
		for (Interpolator interpolator : interpolators) {
			interpolator.read(stream);
		}
	}
	
	@Override public void write(StreamWriter stream) throws IOException {
		pos.writeLE(stream);
		stream.writeLEFloat(weight);
		for (Interpolator interpolator : interpolators) {
			interpolator.write(stream);
		}
	}
	
	@Override public void toArgScript(ArgScriptWriter writer, SPAnimation animation) {
		writer.vector3(pos);
		if (weight != 1.0f) writer.floats(weight);
		
		for (int i = 0; i < interpolators.length; ++i) {
			if (!interpolators[i].isDefault()) {
				interpolators[i].toArgScript(writer, INTERPOLATOR_NAMES[i]);
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
				compData.index = 1;
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
				
				PosComponent c = new PosComponent();
				compData.keyframes.add(c);
				
				float[] values = new float[3];
				if (stream.parseVector3(args, 0, values)) {
					c.pos.setX(values[0]);
					c.pos.setY(values[1]);
					c.pos.setZ(values[2]);
				}
				
				if (args.size() > 1 && !args.get(1).startsWith("-")) {
					Number value;
					if ((value = stream.parseFloat(args, 1)) != null) {
						c.weight = value.floatValue();
					}
				}
				
				for (int i = 0; i < c.interpolators.length; ++i) {
					c.interpolators[i].parse(l, INTERPOLATOR_NAMES[i]);
				}
				
				stream.addSyntax(l, false);
				
				return true;
			}
		};
	}
}
