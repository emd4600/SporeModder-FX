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
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Vector3;

public class Surface {

	public static final int FLAG_BOUNCE = 1;
	public static final int FLAG_PIN = 2;
	public static final int FLAG_PIN_MOVE = 4;
	public static final int FLAG_PIN_EMIT = 8;
	public static final int FLAG_ALIGN = 0x20;
	public static final int FLAG_SOURCE_SPACE = 0x40;
	public static final int FLAG_WORLD_SPACE = 0x80;
	
	
	public int flags;
	public final ResourceID surfaceMapID = new ResourceID();
	public float bounce;
	public float slide;
	public float collisionRadius;
	public float deathProbability;
	public float pinOffset;
	public EffectComponent collideEffect;
	public EffectComponent deathEffect;
	public final List<Vector3> surfacePoints = new ArrayList<Vector3>();
	
	public Surface(Surface other) {
		flags = other.flags;
		surfaceMapID.copy(other.surfaceMapID);
		bounce = other.bounce;
		slide = other.slide;
		collisionRadius = other.collisionRadius;
		deathProbability = other.deathProbability;
		pinOffset = other.pinOffset;
		collideEffect = other.collideEffect;
		deathEffect = other.deathEffect;
		surfacePoints.addAll(other.surfacePoints);
}

	public Surface() {
	}

	public void read(StreamReader in, EffectDirectory effectDirectory) throws IOException  {
		flags = in.readInt();  // & 0x3FFF
		surfaceMapID.read(in);
		bounce = in.readFloat();
		slide = in.readFloat();
		collisionRadius = in.readFloat();
		deathProbability = in.readFloat();
		pinOffset = in.readFloat();
		collideEffect = effectDirectory.getEffect(VisualEffect.TYPE_CODE, in.readInt());
		deathEffect = effectDirectory.getEffect(VisualEffect.TYPE_CODE, in.readInt());

		int count = in.readInt();
		for (int s = 0; s < count; s++) {
			Vector3 vector = new Vector3();
			vector.readLE(in);
			surfacePoints.add(vector);
		}
	}
	
	public void write(StreamWriter out, EffectDirectory effectDirectory) throws IOException {
		out.writeInt(flags);
		surfaceMapID.write(out);
		out.writeFloat(bounce);
		out.writeFloat(slide);
		out.writeFloat(collisionRadius);
		out.writeFloat(deathProbability);
		out.writeFloat(pinOffset);
		out.writeInt(effectDirectory.getIndex(VisualEffect.TYPE_CODE, collideEffect));
		out.writeInt(effectDirectory.getIndex(VisualEffect.TYPE_CODE, deathEffect));
		out.writeInt(surfacePoints.size());
		for (Vector3 f : surfacePoints) f.writeLE(out);
	}
	
	public void parse(ArgScriptStream<EffectUnit> stream, ArgScriptLine line) {
		ArgScriptArguments args = new ArgScriptArguments();
		Number value = null;
		
		if (line.getArguments(args, 0, 1) && args.size() == 1) {
			String[] words = new String[2];
			surfaceMapID.parseSpecial(args, 0, words);
			args.addHyperlink(EffectDirectory.HYPERLINK_MAP, words, 0);
		}
		
		if (line.hasFlag("pin")) {
			flags |= FLAG_PIN;
		}
		if (line.hasFlag("pinMove")) {
			flags |= FLAG_PIN_MOVE;
		}
		if (line.hasFlag("pinEmit")) {
			flags |= FLAG_PIN_EMIT;
			
			if (line.getOptionArguments(args, "surfaceOffset", 1) && (value = stream.parseFloat(args, 0)) != null) {
				pinOffset = value.floatValue();
			}
		}
		// None of the fields read here are used in the surface itself
		/*else if (line.getOptionArguments(args, "repel", 2)) {
			
		}*/
		if (line.hasFlag("align")) {
			flags |= FLAG_ALIGN;
		}
		if (line.getOptionArguments(args, "bounce", 1)) {
			flags |= FLAG_BOUNCE;
			if ((value = stream.parseFloat(args, 0)) != null) {
				bounce = value.floatValue();
			}
		}
		
		if (line.getOptionArguments(args, "slide", 1) && (value = stream.parseFloat(args, 0)) != null) {
			slide = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "radius", 1) && (value = stream.parseFloat(args, 0)) != null) {
			collisionRadius = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "death", 1) && (value = stream.parseFloat(args, 0)) != null) {
			deathProbability = value.floatValue();
		}
		
		if (line.getOptionArguments(args, "collideEffect", 1)) {
			collideEffect = stream.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
			if (collideEffect != null) args.addHyperlink(EffectDirectory.getHyperlinkType(collideEffect), collideEffect, 0);
		}
		
		if (line.getOptionArguments(args, "deathEffect", 1)) {
			deathEffect = stream.getData().getComponent(args, 0, VisualEffect.TYPE_CODE);
			if (deathEffect != null) args.addHyperlink(EffectDirectory.getHyperlinkType(deathEffect), deathEffect, 0);
		}
		
		if (line.hasFlag("sourceSpace")) flags |= FLAG_SOURCE_SPACE;
		if (line.hasFlag("worldSpace")) flags |= FLAG_WORLD_SPACE;
		
		if (line.getOptionArguments(args, "basis", 1, Integer.MAX_VALUE)) {
			surfacePoints.clear();
			for (int i = 0; i < args.size(); i++) {
				float[] arr = new float[3];
				if (!stream.parseVector3(args, i, arr)) break;
				surfacePoints.add(new Vector3(arr));
			}
		}
		
		// There's also 'rect':
		// surface plane -bounce .5 -rect (0,0,0) 0 0 0
		// But that's all the reference we got, so we don't know how it works
	}

	public void toArgScript(ArgScriptWriter writer) {
		if (!surfaceMapID.isDefault()) writer.arguments(surfaceMapID);
		
		writer.flag("pin", (flags & FLAG_PIN) == FLAG_PIN);
		writer.flag("pinMove", (flags & FLAG_PIN_MOVE) == FLAG_PIN_MOVE);
		if ((flags & FLAG_PIN_EMIT) == FLAG_PIN_EMIT) {
			writer.option("pinEmit");
			if (pinOffset != 0) writer.option("surfaceOffset").floats(pinOffset);
		}
		writer.flag("align", (flags & FLAG_ALIGN) == FLAG_ALIGN);
		writer.flag("sourceSpace", (flags & FLAG_SOURCE_SPACE) == FLAG_SOURCE_SPACE);
		writer.flag("worldSpace", (flags & FLAG_WORLD_SPACE) == FLAG_WORLD_SPACE);
		
		if (bounce != 0.0f) writer.option("bounce").floats(bounce);
		if (slide != 0.0f) writer.option("slide").floats(slide);
		if (collisionRadius != 0.0f) writer.option("radius").floats(collisionRadius);
		if (deathProbability != 0.0f) writer.option("death").floats(deathProbability);
		
		if (collideEffect != null) writer.option("collideEffect").arguments(collideEffect.getName());
		if (deathEffect != null) writer.option("deathEffect").arguments(deathEffect.getName());
		
		if (!surfacePoints.isEmpty()) {
			//TODO what's the difference between basis and rect?
			writer.option("basis").vector3s(surfacePoints);
		}
	}
}
