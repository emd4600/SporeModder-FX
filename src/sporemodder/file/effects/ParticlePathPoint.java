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

import java.util.List;

import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

@Structure(StructureEndian.BIG_ENDIAN)
public class ParticlePathPoint {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ParticlePathPoint> STRUCTURE_METADATA = StructureMetadata.generate(ParticlePathPoint.class);

	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] position = new float[3];
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] velocity = new float[3];
	public float time;
	
	public ParticlePathPoint() {}
	
	public ParticlePathPoint(ParticlePathPoint other) {
		position[0] = other.position[0];
		position[1] = other.position[1];
		position[2] = other.position[2];
		velocity[0] = other.velocity[0];
		velocity[1] = other.velocity[1];
		velocity[2] = other.velocity[2];
		time = other.time;
	}
	
	// Spore uses blocks for this, but since there are no original effects and paths aren't really big, we can put them in a command
	public void parse(ArgScriptStream<EffectUnit> stream, ArgScriptLine line, List<ParticlePathPoint> points, int index) {
		ArgScriptArguments args = new ArgScriptArguments();
		Number value = null;
		
		if (line.getOptionArguments(args, "p", 1) || line.getOptionArguments(args, "position", 1)) {
			stream.parseVector3(args, 0, position);
		}
		
		if (line.getOptionArguments(args, "v", 1) || line.getOptionArguments(args, "velocity", 1)) {
			stream.parseVector3(args, 0, velocity);
		}
		
		if ((line.getOptionArguments(args, "s", 1) || line.getOptionArguments(args, "speed", 1)) &&
				(value = stream.parseFloat(args, 0)) != null) {
			float speed = value.floatValue();
			velocity[0] = speed;
			velocity[1] = speed;
			velocity[2] = speed;
		}
		
		if ((line.getOptionArguments(args, "t", 1) || line.getOptionArguments(args, "time", 1)) &&
				(value = stream.parseFloat(args, 0)) != null) {
			time = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "dp", 1) || line.getOptionArguments(args, "deltaPosition", 1)) {
			float[] delta = new float[3];
			float[] previous = new float[3];
			stream.parseVector3(args, 0, delta);
			
			int previousIndex = index-1;
			if (previousIndex >= 0) {
				previous = points.get(previousIndex).position;
			}
			
			position[0] = delta[0] + previous[0];
			position[1] = delta[1] + previous[1];
			position[2] = delta[2] + previous[2];
		}
		
		if ((line.getOptionArguments(args, "dt", 1) || line.getOptionArguments(args, "deltaTime", 1)) &&
				(value = stream.parseFloat(args, 0)) != null) {
			float delta = value.floatValue();
			float previous = 0;
			
			int previousIndex = index-1;
			if (previousIndex >= 0) {
				previous = points.get(previousIndex).time;
			}
			
			time = delta + previous;
		}
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("path").option("p").vector(position);
		if (velocity[0] == velocity[1] && velocity[1] == velocity[2]) {
			writer.option("s").floats(velocity[0]);
		} else {
			writer.option("v").vector(velocity);
		}
		writer.option("t").floats(time);
	}
}
