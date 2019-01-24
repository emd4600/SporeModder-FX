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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureFieldEndian;
import emord.filestructures.StructureLength;
import emord.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

@Structure(StructureEndian.BIG_ENDIAN)
public class ModelAnimation {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ModelAnimation> STRUCTURE_METADATA = StructureMetadata.generate(ModelAnimation.class);

	public static final int MODE_FLAG_SUSTAIN = 1;
	public static final int MODE_FLAG_SINGLE = 2;
	public static final int MODE_FLAG_LOOP = 0;
	
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN)
	public final float[] lengthRange = new float[2];
	@StructureLength.Value(32)
	public final List<Float> curve = new ArrayList<Float>();
	public float curveVary;
	public float speedScale;
	public byte channelID;
	public byte mode;
	
	public ModelAnimation() {};
	public ModelAnimation(ModelAnimation other) {
		EffectDirectory.copyArray(lengthRange, other.lengthRange);
		curve.addAll(other.curve);
		curveVary = other.curveVary;
		speedScale = other.speedScale;
		channelID = other.channelID;
		mode = other.mode;
	}

	public <T> void parse(ArgScriptStream<T> stream, ArgScriptLine line) {
		ArgScriptArguments args = new ArgScriptArguments();
		
		if (line.getArguments(args, 0, Integer.MAX_VALUE)) {
			for (int i = 0; i < args.size(); i++) {
				curve.add(stream.parseFloat(args, i));
			}
		}
		
		if (line.getOptionArguments(args, "vary", 1)) {
			curveVary = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
		}
		
		if (line.getOptionArguments(args, "speedScale", 1)) {
			speedScale = Optional.ofNullable(stream.parseFloat(args, 0)).orElse(0.0f);
		}
		
		if (line.getOptionArguments(args, "channelID", 1)) {
			channelID = Optional.ofNullable(stream.parseByte(args, 0)).orElse((byte) 0);
		}
		
		if (line.hasFlag("sustain")) mode |= MODE_FLAG_SUSTAIN;
		if (line.hasFlag("single")) mode |= MODE_FLAG_SINGLE;
		if (line.hasFlag("loop")) mode |= MODE_FLAG_LOOP;
		
		if (line.getOptionArguments(args, "length", 1, 2)) {
			Float value = stream.parseFloat(args, 0);
			
			if (value != null) {
				lengthRange[0] = lengthRange[1] = value;
				
				if (args.size() == 2) {
					value = stream.parseFloat(args, 1);
					if (value != null) {
						lengthRange[0] -= value;
						lengthRange[1] += value;
					}
				}
			}
		}
	}

	public void toArgScript(ArgScriptWriter writer) {
		writer.command("animate").floats(curve);
		
		if (curveVary != 0) writer.option("vary").floats(curveVary);
		if (speedScale != 0) writer.option("speedScale").floats(speedScale);
		if (channelID != 0) writer.option("channel").ints(channelID);
		
		if (lengthRange[0] != 0 || lengthRange[1] != 0) {
			//TODO Spore does something different here
			writer.option("length").floats(lengthRange);
		}
		
		if (mode == MODE_FLAG_SUSTAIN) writer.option("sustain");
		else if (mode == MODE_FLAG_SINGLE) writer.option("single");
		else if (mode == MODE_FLAG_LOOP) writer.option("loop");
		else writer.option("mode").ints(mode);
	}
}
