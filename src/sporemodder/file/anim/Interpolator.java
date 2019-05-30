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
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptWriter;

public class Interpolator {
	public static final int MODE_FACTOR = 2;
	// Just multiplies by 0
	public static final int MODE_NONE = 3;
	
	// size 10h
	
	public float nextFactor;
	public float previousFactor;
	public int nextMode;
	public int previousMode;
	
	public void read(StreamReader stream) throws IOException {
		nextFactor = stream.readLEFloat();
		previousFactor = stream.readLEFloat();
		nextMode = stream.readUByte();
		previousMode = stream.readUByte();
		stream.skip(6);
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEFloat(nextFactor);
		stream.writeLEFloat(previousFactor);
		stream.writeUByte(nextMode);
		stream.writeUByte(previousMode);
		stream.writePadding(6);
	}
	
	public boolean isDefault() {
		return nextMode == 0 && previousMode == 0;
	}
	
	public void toArgScript(ArgScriptWriter writer, String name) {
		writer.option(name);
		
		writer.ints(previousMode);
		if (previousMode == MODE_FACTOR) writer.floats(previousFactor);
		
		writer.ints(nextMode);
		if (nextMode == MODE_FACTOR) writer.floats(nextFactor);
	}
	
	public void parse(ArgScriptLine line, String keyword) {
		final ArgScriptArguments args = new ArgScriptArguments();
		if (line.getOptionArguments(args, keyword, 2, 4)) {
			Number value;
			int index = 0;
			
			if ((value = args.getStream().parseInt(args, index++, 0, 3)) != null) {
				previousMode = value.intValue();
			}
			
			if (previousMode == MODE_FACTOR &&
					(value = args.getStream().parseFloat(args, index++)) != null) {
				previousFactor = value.floatValue();
			}
			
			if (index > args.size()) {
				args.getStream().addError(line.createErrorForOption(keyword, "Missing next interpolation mode"));
			}
			
			if ((value = args.getStream().parseInt(args, index++, 0, 3)) != null) {
				nextMode = value.intValue();
			}
			
			if (nextMode == MODE_FACTOR) {
				if (index > args.size()) {
					args.getStream().addError(line.createErrorForOption(keyword, "Missing next interpolation factor"));
				}
				else if ((value = args.getStream().parseFloat(args, index++)) != null) {
					nextFactor = value.floatValue();
				}
			}
		}
	}
}
