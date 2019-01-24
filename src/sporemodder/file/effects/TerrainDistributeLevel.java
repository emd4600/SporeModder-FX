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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptWriter;

public class TerrainDistributeLevel {

	public static final ArgScriptEnum ENUM_EFFECT_TYPE = new ArgScriptEnum();
	static {
		ENUM_EFFECT_TYPE.add(6, "SmallPlant1");
		ENUM_EFFECT_TYPE.add(7, "SmallPlant2");
		ENUM_EFFECT_TYPE.add(8, "SmallPlant3");
		ENUM_EFFECT_TYPE.add(3, "MediumPlant1");
		ENUM_EFFECT_TYPE.add(4, "MediumPlant2");
		ENUM_EFFECT_TYPE.add(5, "MediumPlant3");
		ENUM_EFFECT_TYPE.add(0, "LargePlant1");
		ENUM_EFFECT_TYPE.add(1, "LargePlant2");
		ENUM_EFFECT_TYPE.add(2, "LargePlant3");
		ENUM_EFFECT_TYPE.add(9, "All");
	}
	
	public static final ArgScriptEnum ENUM_EFFECT_RESOURCE = new ArgScriptEnum();
	static {
		ENUM_EFFECT_TYPE.add(0, "lod0");
		ENUM_EFFECT_TYPE.add(1, "lod1");
		ENUM_EFFECT_TYPE.add(2, "lod2");
		ENUM_EFFECT_TYPE.add(3, "lod3");
		ENUM_EFFECT_TYPE.add(5, "All");
	}
	
	public static class TerrainDistEffect {
		public EffectComponent effect;  // 0x00
		public final float[] heightRange = {0, Float.MAX_VALUE};  // 0x04
		public int type = -1;  // 0x0C
		public int resource = -1;  // 0x0D
		public int overrideSet;  // 0x0E
		
		public TerrainDistEffect() { }
		
		public TerrainDistEffect(TerrainDistEffect other) {
			effect = other.effect;
			heightRange[0] = other.heightRange[0];
			heightRange[1] = other.heightRange[1];
			type = other.type;
			resource = other.resource;
			overrideSet = other.overrideSet;
}
		
		public void read(StreamReader in, EffectDirectory effectDirectory) throws IOException {
			effect = effectDirectory.getEffect(VisualEffect.TYPE_CODE, in.readInt());
			heightRange[0] = in.readFloat();
			heightRange[1] = in.readFloat();
			type = in.readByte();
			resource = in.readByte();
			overrideSet = in.readByte();
		}
		
		public void write(StreamWriter out, EffectDirectory effectDirectory) throws IOException {
			out.writeInt(effectDirectory.getIndex(VisualEffect.TYPE_CODE, effect));
			out.writeFloat(heightRange[0]);
			out.writeFloat(heightRange[1]);
			out.writeByte(type);
			out.writeByte(resource);
			out.writeByte(overrideSet);
		}
		
		public void toArgScript(ArgScriptWriter writer) {
			writer.command("effect").arguments(effect.getName());
			
			if (heightRange[0] != 0 || heightRange[1] != Float.MAX_VALUE) writer.option("heightRange").floats(heightRange);
			if (type != -1) writer.option("type").arguments(ENUM_EFFECT_TYPE.get(type));
			if (resource != -1) writer.option("resource").arguments(ENUM_EFFECT_RESOURCE.get(resource));
			if (overrideSet != 0) writer.option("type").ints(overrideSet);
			
		}
	}
	
	//TODO if we see how Spore parses TerrainDistribute effects, the '_noEffects' things are never used
	public float distance;  // 0x00  // possibly related with effects
	public float distance_noEffects;  // 0x04  //TODO ?
	public float verticalWeight = 1.0f;  // 0x08
	public float facing;  // 0x0C
	public float facing_noEffects;  // 0x10  //TODO ?
	public final List<TerrainDistEffect> effects = new ArrayList<TerrainDistEffect>();  // 0x14
	
	public TerrainDistributeLevel() {
	}
	
	public TerrainDistributeLevel(TerrainDistributeLevel other) {
		distance = other.distance;
		distance_noEffects = other.distance_noEffects;
		verticalWeight = other.verticalWeight;
		facing = other.facing;
		facing_noEffects = other.facing_noEffects;
		
		effects.clear();
		for (int i = 0; i < other.effects.size(); i++) {
			effects.add(new TerrainDistEffect(other.effects.get(i)));
		}
	}
	
	public void read(StreamReader in, EffectDirectory effectDirectory, int version) throws IOException {
		distance = in.readFloat();
		distance_noEffects = in.readFloat();
		if (version >= 2) {
			verticalWeight = in.readFloat();
		}
		facing = in.readFloat();
		facing_noEffects = in.readFloat();
		
		int effectCount = in.readInt();
		for (int i = 0; i < effectCount; i++) {
			TerrainDistEffect eff = new TerrainDistEffect();
			eff.read(in, effectDirectory);
			effects.add(eff);
		}
	}
	
	public void write(StreamWriter out, EffectDirectory effectDirectory, int version) throws IOException {
		out.writeFloat(distance);
		out.writeFloat(distance_noEffects);
		if (version >= 2) {
			out.writeFloat(verticalWeight);
		}
		out.writeFloat(facing);
		out.writeFloat(facing_noEffects);
		out.writeInt(effects.size());
		for (TerrainDistEffect eff : effects) {
			eff.write(out, effectDirectory);
		}
	}
	
	public void toArgScript(ArgScriptWriter writer, int index) {
		writer.command("level").ints(index).startBlock();
		
		if (!effects.isEmpty()) {
			writer.command("distance").floats(distance);
			if (verticalWeight != 1.0f) writer.option("verticalWeight").floats(verticalWeight);
			
			writer.command("facing").floats(facing);
			
			writer.blankLine();
			for (TerrainDistEffect effect : effects) {
				effect.toArgScript(writer);
			}
		} else {
			writer.command("distance").floats(distance_noEffects);
			if (verticalWeight != 1.0f) writer.option("verticalWeight").floats(verticalWeight);
			writer.command("facing").floats(facing_noEffects);
		}
		
		writer.endBlock().commandEND();
	}

	public boolean isDefault() {
		return effects.isEmpty() && distance == 0 && distance_noEffects == 0 && verticalWeight == 1.0f && facing == 0 && facing_noEffects == 0;
	}
}
