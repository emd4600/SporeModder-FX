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
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.anim.AnimationChannel.ContextSelector;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

public class AnimationEvent {
	// size 60h
	
	public static final int FLAG_DISABLED = 8;
	public static final int FLAG_PREDICATE = 0x1000000;
	public static final int FLAG_CHANCE = 0x2000000;
	public static final int FLAG_TYPE_MASK = 0x20007;
	public static final int FLAG_STOPEFFECT_HARDSTOP = 0x10;
	public static final int FLAG_EFFECT_UPDATEPOSITION = 0x800;
	public static final int FLAG_EFFECT_UPDATEROTATION = 0x4000;
	public static final int FLAG_EFFECT_UPDATESCALE = 0x100000;
	public static final int FLAG_EFFECT_UPDATEPARTICLESCALE = 0x1000;
	// updates attractor location and intensity
	public static final int FLAG_EFFECT_UPDATEATTRACTOR = 0x2000;
	public static final int FLAG_EFFECT_IDENTITYCOLOR = 0x10000;
	// by defualt, scale is only applied as kParamParticleSizeScale, with this it's a general effect scale
	public static final int FLAG_EFFECT_APPLYSCALE = 0x200000;
	
	public static final int FLAG_PARAMETER0_MASK = 0x300;
	public static final int FLAG_PARAMETER0_INT = 0x100;
	public static final int FLAG_PARAMETER0_FLOAT = 0x200;
	public static final int FLAG_PARAMETER0_ID = 0x300;
	
	public static final ArgScriptEnum ENUM_PARAMETER0 = new ArgScriptEnum();
	static {
		ENUM_PARAMETER0.add(FLAG_PARAMETER0_INT, "int");
		ENUM_PARAMETER0.add(FLAG_PARAMETER0_FLOAT, "float");
		ENUM_PARAMETER0.add(FLAG_PARAMETER0_ID, "id");
	}
	
	public static final int FLAG_PARAMETER1_MASK = 0xC0000;
	public static final int FLAG_PARAMETER1_INT = 0x40000;
	public static final int FLAG_PARAMETER1_FLOAT = 0x80000;
	public static final int FLAG_PARAMETER1_ID = 0xC0000;
	
	public static final ArgScriptEnum ENUM_PARAMETER1 = new ArgScriptEnum();
	static {
		ENUM_PARAMETER1.add(FLAG_PARAMETER1_INT, "int");
		ENUM_PARAMETER1.add(FLAG_PARAMETER1_FLOAT, "float");
		ENUM_PARAMETER1.add(FLAG_PARAMETER1_ID, "id");
	}
	
	public static final int FLAG_MASK = FLAG_PARAMETER0_MASK | FLAG_PARAMETER1_MASK | FLAG_TYPE_MASK |
			FLAG_DISABLED | FLAG_PREDICATE | FLAG_CHANCE | FLAG_EFFECT_UPDATEPOSITION |
			FLAG_EFFECT_UPDATEROTATION | FLAG_EFFECT_UPDATESCALE | FLAG_EFFECT_UPDATEPARTICLESCALE |
			FLAG_EFFECT_UPDATEATTRACTOR;
	
	// 0x10000 effect use creature color
	// 0x200000 effect scaling enabled
	
	public static final int TYPE_SOUND = 0;  //TODO there's a flag to force it to be a voice
	public static final int TYPE_EFFECT = 1;
	public static final int TYPE_EVENTCONTROL = 2;  // uses id for action such as item_pickup_hand
	public static final int TYPE_UNK3 = 3;  // 3 uses selector4.flags, field_44
	// stops effects and sounds
	public static final int TYPE_STOP = 4;
	// reassigns the parameter0 to existing effects?
	public static final int TYPE_UNK5 = 5;
	public static final int TYPE_SOUND2 = 6;   // ????? plays using name, not id
	public static final int TYPE_FOOTSTEP = 7;
	// type 0x20000 uses both parameters, something related with gait?
	public static final int TYPE_UNK20000 = 0x20000;
	public static final int TYPE_MESSAGE = 0x20001;
	
	public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
	static {
		ENUM_TYPE.add(TYPE_SOUND, "sound");
		ENUM_TYPE.add(TYPE_EFFECT, "effect");
		ENUM_TYPE.add(TYPE_EVENTCONTROL, "eventControl");
		ENUM_TYPE.add(TYPE_UNK3, "unk3");
		ENUM_TYPE.add(TYPE_STOP, "stop");
		ENUM_TYPE.add(TYPE_UNK5, "unk5");
		ENUM_TYPE.add(TYPE_SOUND2, "sound2");
		ENUM_TYPE.add(TYPE_FOOTSTEP, "footstep");
		ENUM_TYPE.add(TYPE_UNK20000, "unk20000");
		ENUM_TYPE.add(TYPE_MESSAGE, "message");
	}
	
	
	// check sub_9A77A0
	public static class Selector {
		public static final int TYPE_UNK1 = 1;
		public static final int TYPE_DEFAULT = 2;  // uses the bodies of the channel that is spawning the vfx
		public static final int TYPE_UNK3 = 3;
		public static final int TYPE_STANDARD_BODIES = 4;  // uses the select flags and capabilities
		public static final int TYPE_OTHER_CREATURE = 5;
		public static final int TYPE_OTHER_STANDARD_BODIES = 6;  // bodies from other creature
		public static final int TYPE_UNK7 = 7;
		public static final int TYPE_UNK8 = 8;
		public static final int TYPE_UNK9 = 9;
		public static final int FLAG_TYPE_MASK = 0xF;
		public static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum();
		static {
			ENUM_TYPE.add(TYPE_UNK1, "unk1");
			ENUM_TYPE.add(TYPE_DEFAULT, "default");
			ENUM_TYPE.add(TYPE_UNK3, "unk3");
			ENUM_TYPE.add(TYPE_STANDARD_BODIES, "standardBodies");
			ENUM_TYPE.add(TYPE_OTHER_CREATURE, "otherCreature");
			ENUM_TYPE.add(TYPE_OTHER_STANDARD_BODIES, "otherStandardBodies");
			ENUM_TYPE.add(TYPE_UNK7, "unk7");
			ENUM_TYPE.add(TYPE_UNK8, "unk8");
			ENUM_TYPE.add(TYPE_UNK9, "unk9");
		}
		
		// uses field_2D4 of anim_cid
		public static final int FLAG_PREFILTER_2D4_MASK = 0xC000000;
		public static final int FLAG_PREFILTER_2D4_TRUE = 0x4000000;  // requires field_2D4 to be true
		public static final int FLAG_PREFILTER_2D4_FALSE = 0x8000000;  // requires field_2D4 to be false
		
		// uses field_2D5 of anim_cid
		public static final int FLAG_PREFILTER_2D5_MASK = 0x3000000;
		public static final int FLAG_PREFILTER_2D5_TRUE = 0x1000000;  // requires field_2D5 to be true
		public static final int FLAG_PREFILTER_2D5_FALSE = 0x2000000;  // requires field_2D5 to be false
		
		// uses field_2D6 of anim_cid
		public static final int FLAG_PREFILTER_2D6_MASK = 0xC00000;
		public static final int FLAG_PREFILTER_2D6_TRUE = 0x400000;  // requires field_2D6 to be true
		public static final int FLAG_PREFILTER_2D6_FALSE = 0x800000;  // requires field_2D6 to be true
		
		public static final int FLAG_FILTER_MASK = 0x70;
		public static final int FLAG_FILTER_ANY = 0;
		// Only accepts one body
		public static final int FLAG_FILTER_FIRSTONLY = 0x10;
		// Accepts all bodies except the first
		public static final int FLAG_FILTER_ALLEXCEPTFIRST = 0x20;
		public static final int FLAG_FILTER_UNK1 = 0x30;
		public static final int FLAG_FILTER_UNK2 = 0x40;
		public static final int FLAG_FILTER_UNK3 = 0x50;
		public static final int FLAG_FILTER_UNK4 = 0x60;
		
		public static final ArgScriptEnum ENUM_FILTER = new ArgScriptEnum();
		static {
			ENUM_FILTER.add(FLAG_FILTER_FIRSTONLY, "firstOnly");
			ENUM_FILTER.add(FLAG_FILTER_ALLEXCEPTFIRST, "allExceptFirst");
			ENUM_FILTER.add(FLAG_FILTER_UNK1, "unk1");
			ENUM_FILTER.add(FLAG_FILTER_UNK2, "unk2");
			ENUM_FILTER.add(FLAG_FILTER_UNK3, "unk3");
			ENUM_FILTER.add(FLAG_FILTER_UNK4, "unk4");
		}
		
		public static final int FLAG_SCALE_MASK = 0x8180;
		public static final int FLAG_SCALE_NOSCALE = 0;
		// The current creature scale
		public static final int FLAG_SCALE_CURRENTCREATURESCALE = 0x80;
		// Uses PARAMETER0
		public static final int FLAG_SCALE_PARAMETER = 0x100;
		// The creature where the effect is being spawned (may be different than the current creature)
		public static final int FLAG_SCALE_CREATURESCALE = 0x180;
		// The current creature scale, multiplied by the boundary distance 
		public static final int FLAG_SCALE_REALCURRENTCREATURESCALE = 0x8000;
		
		public static final ArgScriptEnum ENUM_SCALE = new ArgScriptEnum();
		static {
			ENUM_SCALE.add(FLAG_SCALE_NOSCALE, "noScale");
			ENUM_SCALE.add(FLAG_SCALE_CURRENTCREATURESCALE, "currentCreatureScale");
			ENUM_SCALE.add(FLAG_SCALE_PARAMETER, "parameter");
			ENUM_SCALE.add(FLAG_SCALE_CREATURESCALE, "creatureScale");
			ENUM_SCALE.add(FLAG_SCALE_REALCURRENTCREATURESCALE, "realCurrentCreatureScale");
		}
		
		public static final int FLAG_ADDPOSITION_MASK = 0x22000;
		public static final int FLAG_ADDPOSITION_NONE = 0;
		public static final int FLAG_ADDPOSITION_RESTPOSITION = 0x2000;
		public static final int FLAG_ADDPOSITION_RANDOM = 0x20000;
		public static final int FLAG_ADDPOSITION_RESTEFFECTSORIGIN = 0x22000;
		
		public static final ArgScriptEnum ENUM_ADDPOSITION = new ArgScriptEnum();
		static {
			ENUM_ADDPOSITION.add(FLAG_ADDPOSITION_RESTPOSITION, "restPosition");
			ENUM_ADDPOSITION.add(FLAG_ADDPOSITION_RANDOM, "random");
			ENUM_ADDPOSITION.add(FLAG_ADDPOSITION_RESTEFFECTSORIGIN, "restEffectsOrigin");
		}
		
		public static final int FLAG_POSITION_MASK = 0x11000;
		public static final int FLAG_POSITION_DEFAULT = 0;  // just uses the block position
		public static final int FLAG_POSITION_USEGROUNDHEIGHT = 0x1000;
		public static final int FLAG_POSITION_USEGROUNDHEIGHT2 = 0x10000;
		public static final int FLAG_POSITION_UNK = 0x11000;
		
		public static final ArgScriptEnum ENUM_POSITION = new ArgScriptEnum();
		static {
			ENUM_POSITION.add(FLAG_POSITION_USEGROUNDHEIGHT, "groundHeight");
			ENUM_POSITION.add(FLAG_POSITION_USEGROUNDHEIGHT2, "groundHeight2");
			ENUM_POSITION.add(FLAG_POSITION_UNK, "unk");
		}
		
		public static final int FLAG_ROTATION_MASK = 0xE00;
		public static final int FLAG_ROTATION_DEFAULT = 0;
		public static final int FLAG_ROTATION_UNK1 = 0x200;
		public static final int FLAG_ROTATION_CREATUREROTATION = 0x400;
		public static final int FLAG_ROTATION_UNK2 = 0x600;
		public static final int FLAG_ROTATION_GROUNDNORMAL = 0x800;
		public static final int FLAG_ROTATION_NOROTATION = 0xA00;
		
		public static final ArgScriptEnum ENUM_ROTATION = new ArgScriptEnum();
		static {
			ENUM_ROTATION.add(FLAG_ROTATION_UNK1, "unk1");
			ENUM_ROTATION.add(FLAG_ROTATION_CREATUREROTATION, "creatureRotation");
			ENUM_ROTATION.add(FLAG_ROTATION_UNK2, "unk2");
			ENUM_ROTATION.add(FLAG_ROTATION_GROUNDNORMAL, "groundNormal");
			ENUM_ROTATION.add(FLAG_ROTATION_NOROTATION, "noRotation");
		}
		
		// applies some weird thing to the rotation, only if the body.x > 0 (right hand)
		public static final int FLAG_HANDEDNESS_MASK = 0x70000000;
		public static final int FLAG_HANDEDNESS_NONE = 0x00000000;
		public static final int FLAG_HANDEDNESS_UNK1 = 0x10000000;
		public static final int FLAG_HANDEDNESS_UNK2 = 0x20000000;
		public static final int FLAG_HANDEDNESS_UNK3 = 0x30000000;
		
		public static final ArgScriptEnum ENUM_HANDEDNESS = new ArgScriptEnum();
		static {
			ENUM_HANDEDNESS.add(FLAG_HANDEDNESS_UNK1, "unk1");
			ENUM_HANDEDNESS.add(FLAG_HANDEDNESS_UNK2, "unk2");
			ENUM_HANDEDNESS.add(FLAG_HANDEDNESS_UNK3, "unk3");
		}
		
		public static final int FLAG_MASK = FLAG_HANDEDNESS_MASK | FLAG_ROTATION_MASK | FLAG_POSITION_MASK | 
				FLAG_ADDPOSITION_MASK | FLAG_SCALE_MASK | FLAG_FILTER_MASK | FLAG_TYPE_MASK |
				FLAG_PREFILTER_2D4_MASK | FLAG_PREFILTER_2D5_MASK | FLAG_PREFILTER_2D6_MASK;
		
		public int flags = TYPE_DEFAULT;
		public int selectFlags;
		public String selectCapability;
	}
	
	// 04h: selector1
	// 10h: selector2
	// 1Ch: selector3
	// 28h: selector4
	// 48h: float minDistToCamera, not always used
	// 4Ch: float: if flag 0x2000000, chance it appears?
	
	public int flags;
	// 0: position, 1: rotation, 2: scale
	public Selector[] selectors = new Selector[4];
	public int id;
	public String name;
	public String archetype;  // if not 0, only when the creature has this "archetype"
	public int eventGroup;  // used by type effect and effect stop, to know what effect to stop
	public int parameter0;
	public float maxSqrDist;
	public int parameter1;
	public final AnimationPredicate predicate = new AnimationPredicate();
	
	// 0x3C message ID, 0x44 and 0x4c are used as parameters
	
	public AnimationEvent() {
		for (int i = 0; i < 4; ++i) {
			selectors[i] = new Selector();
		}
		// The default for selector 4 is 0
		selectors[3].flags = 0;
	}
	
	public void read(StreamReader stream) throws IOException {
		flags = stream.readLEInt();
		
		for (int i = 0; i < 4; ++i) {
			selectors[i].flags = stream.readLEInt();
			selectors[i].selectFlags = stream.readLEInt();
			selectors[i].selectCapability = stream.readString(StringEncoding.ASCII, 4);
		}
		
		int value = stream.readLEInt();
		if (value == -1) archetype = "default";
		else if (value == 0) archetype = null;
		else {
			archetype = new String(new byte[] {(byte)(value & 0xFF), (byte)((value & 0xFF00) >> 8), (byte)((value & 0xFF0000) >> 16), (byte)((value & 0xFF000000) >> 24)}, "US-ASCII");
		}

		eventGroup = stream.readLEInt();
		id = stream.readLEInt();
		long offset = stream.readLEUInt();
		parameter0 = stream.readLEInt();
		maxSqrDist = stream.readLEFloat();
		parameter1 = stream.readLEInt();
		predicate.read(stream);
		
		if (id == 0xF291E6D9) {
			System.out.println("aa");
		}
		
		if (id != 0) {
			stream.seek(offset);
			name = stream.readCString(StringEncoding.ASCII);
		}
		else {
			System.out.println("id 0, offset: " + offset);
		}
	}
	
	public void write(StreamWriter stream, long namePtr) throws IOException {
		stream.writeLEInt(flags);
		
		for (int i = 0; i < 4; ++i) {
			stream.writeLEInt(selectors[i].flags);
			stream.writeLEInt(selectors[i].selectFlags);
			if (selectors[i].selectCapability == null) {
				stream.writePadding(4);
			} else {
				stream.writeString(selectors[i].selectCapability, StringEncoding.ASCII);
				stream.writePadding(4 - selectors[i].selectCapability.length());
			}
		}
		
		if (archetype == null) {
			stream.writePadding(4);
		} else if (archetype.equals("default")) {
			stream.writeLEInt(-1);
		} else {
			stream.writeString(archetype, StringEncoding.ASCII);
			stream.writePadding(4 - archetype.length());
		}
		
		stream.writeLEInt(eventGroup);  // ?
		stream.writeLEInt(id);
		stream.writeLEUInt(id == 0 ? 0 : namePtr);
		stream.writeLEInt(parameter0);
		stream.writeLEFloat(maxSqrDist);
		stream.writeLEInt(parameter1);
		predicate.write(stream);
		
		// from 58h to 60h, unknown
		stream.writePadding(0x60 - 0x58);
	}
	
	private void selectorToArgScript(ArgScriptWriter writer, Selector s, String name) {
		int type = s.flags & 0xF;
		writer.command(name).arguments(Selector.ENUM_TYPE.get(type));
		
		if (type == 4 || type == 6) {
			ContextSelector context = new ContextSelector();
			context.flags = s.selectFlags;
			context.capability = s.selectCapability;
			AnimationChannel.selectorToArgScript(writer, context, false);
		}
		
		int flags = s.flags & Selector.FLAG_PREFILTER_2D4_MASK;
		if (flags != 0) {
			writer.option("require2D4").arguments(flags == Selector.FLAG_PREFILTER_2D4_TRUE);
		}
		
		flags = s.flags & Selector.FLAG_PREFILTER_2D5_MASK;
		if (flags != 0) {
			writer.option("require2D5").arguments(flags == Selector.FLAG_PREFILTER_2D5_TRUE);
		}
		
		flags = s.flags & Selector.FLAG_PREFILTER_2D6_MASK;
		if (flags != 0) {
			writer.option("require2D6").arguments(flags == Selector.FLAG_PREFILTER_2D6_TRUE);
		}
		
		flags = s.flags & Selector.FLAG_FILTER_MASK;
		if (flags != 0) {
			writer.option("filter").arguments(Selector.ENUM_FILTER.get(flags));
		}
		
		flags = s.flags & Selector.FLAG_SCALE_MASK;
		if (flags != 0) {
			writer.option("scale").arguments(Selector.ENUM_SCALE.get(flags));
		}
		
		flags = s.flags & Selector.FLAG_POSITION_MASK;
		if (flags != 0) {
			writer.option("position").arguments(Selector.ENUM_POSITION.get(flags));
		}
		
		flags = s.flags & Selector.FLAG_ADDPOSITION_MASK;
		if (flags != 0) {
			writer.option("addPosition").arguments(Selector.ENUM_ADDPOSITION.get(flags));
		}
		
		flags = s.flags & Selector.FLAG_ROTATION_MASK;
		if (flags != 0) {
			writer.option("rotation").arguments(Selector.ENUM_ROTATION.get(flags));
		}
		
		flags = s.flags & Selector.FLAG_HANDEDNESS_MASK;
		if (flags != 0) {
			writer.option("handedness").arguments(Selector.ENUM_HANDEDNESS.get(flags));
		}
		
		flags = s.flags & ~Selector.FLAG_MASK;
		if (flags != 0) {
			writer.option("flags").arguments("0x" + Integer.toHexString(flags));
		}
	}
	
	public void toArgScript(ArgScriptWriter writer, String internal_name) {
		writer.command("event").arguments(internal_name);
		if (name != null) writer.arguments(name);
		
		writer.option("type").arguments(ENUM_TYPE.get(flags & FLAG_TYPE_MASK));
		
		if ((flags & FLAG_PREDICATE) != 0) {
			writer.option("predicate");
			predicate.toArgScript(writer);
		}
		
		int param0 = flags & FLAG_PARAMETER0_MASK;
		if (param0 != 0) {
			writer.option("param0").arguments(ENUM_PARAMETER0.get(param0));
			if (param0 == FLAG_PARAMETER0_INT) {
				writer.ints(parameter0);
			}
			else if (param0 == FLAG_PARAMETER0_FLOAT) {
				writer.floats(Float.intBitsToFloat(parameter0));
			}
			else {
				writer.arguments(HashManager.get().getFileName(parameter0));
			}
		}
		
		int param1 = flags & FLAG_PARAMETER1_MASK;
		if (param1 != 0) {
			writer.option("param1").arguments(ENUM_PARAMETER1.get(param1));
			if (param1 == FLAG_PARAMETER1_INT) {
				writer.ints(parameter1);
			}
			else if (param1 == FLAG_PARAMETER1_FLOAT) {
				writer.floats(Float.intBitsToFloat(parameter1));
			}
			else {
				writer.arguments(HashManager.get().getFileName(parameter1));
			}
		}
		
		if ((flags & FLAG_CHANCE) != 0) {
			writer.option("chance");
		}
		
		if (id != 0 && HashManager.get().fnvHash(name) != id) {
			writer.option("id").arguments(HashManager.get().getFileName(id));
		}
		
		if (archetype != null) {
			writer.option("archetype").arguments(archetype);
		}
		
		if (eventGroup != 0) {
			writer.option("eventGroup").ints(eventGroup);
		}
		
		if (maxSqrDist != 0.0f) {
			writer.option("maxSqrDist").floats(maxSqrDist);
		}
		
		if ((flags & FLAG_EFFECT_UPDATEPOSITION) != 0) {
			writer.option("updatePosition");
		}
		if ((flags & FLAG_EFFECT_UPDATEROTATION) != 0) {
			writer.option("updateRotation");
		}
		if ((flags & FLAG_EFFECT_UPDATESCALE) != 0) {
			writer.option("updateScale");
		}
		if ((flags & FLAG_EFFECT_UPDATEPARTICLESCALE) != 0) {
			writer.option("updateParticleScale");
		}
		if ((flags & FLAG_EFFECT_UPDATEATTRACTOR) != 0) {
			writer.option("updateAttractor");
		}
		if ((flags & FLAG_EFFECT_APPLYSCALE) != 0) {
			writer.option("applyScale");
		}
		if ((flags & FLAG_EFFECT_IDENTITYCOLOR) != 0) {
			writer.option("identityColor");
		}
		if ((flags & FLAG_STOPEFFECT_HARDSTOP) != 0) {
			writer.option("hardStop");
		}
		
		int flags2 = flags & ~FLAG_MASK;
		if (flags2 != 0) writer.option("flags").arguments("0x" + Integer.toHexString(flags2));
		
		writer.startBlock();
		if (selectors[0].flags != Selector.TYPE_DEFAULT) selectorToArgScript(writer, selectors[0], "positionSource");
		if (selectors[1].flags != Selector.TYPE_DEFAULT) selectorToArgScript(writer, selectors[1], "rotationSource");
		if (selectors[2].flags != Selector.TYPE_DEFAULT) selectorToArgScript(writer, selectors[2], "scaleSource");
		if (selectors[3].flags != 0) selectorToArgScript(writer, selectors[3], "source4");
		
		writer.endBlock().commandEND();
	}
	
	private static void parseSelector(ArgScriptLine line, ArgScriptStream<SPAnimation> stream, Selector selector) {
		final ArgScriptArguments args = new ArgScriptArguments();
		selector.selectFlags = 0;
		
		Number value = null;
		
		if (line.getArguments(args, 1, 2)) {
			
			selector.flags = Selector.ENUM_TYPE.get(args, 0);
			
			Boolean bool = null;
			if (line.getOptionArguments(args, "require2D4", 1) && (bool = stream.parseBoolean(args, 0)) != null) {
				selector.flags |= bool ? Selector.FLAG_PREFILTER_2D4_TRUE : Selector.FLAG_PREFILTER_2D4_FALSE;
			}
			if (line.getOptionArguments(args, "require2D5", 1) && (bool = stream.parseBoolean(args, 0)) != null) {
				selector.flags |= bool ? Selector.FLAG_PREFILTER_2D5_TRUE : Selector.FLAG_PREFILTER_2D5_FALSE;
			}
			if (line.getOptionArguments(args, "require2D6", 1) && (bool = stream.parseBoolean(args, 0)) != null) {
				selector.flags |= bool ? Selector.FLAG_PREFILTER_2D6_TRUE : Selector.FLAG_PREFILTER_2D6_FALSE;
			}
			
			if (line.getOptionArguments(args, "filter", 1)) {
				selector.flags |= Selector.ENUM_FILTER.get(args, 0);
			}
			if (line.getOptionArguments(args, "scale", 1)) {
				selector.flags |= Selector.ENUM_SCALE.get(args, 0);
			}
			if (line.getOptionArguments(args, "position", 1)) {
				selector.flags |= Selector.ENUM_POSITION.get(args, 0);
			}
			if (line.getOptionArguments(args, "addPosition", 1)) {
				selector.flags |= Selector.ENUM_ADDPOSITION.get(args, 0);
			}
			if (line.getOptionArguments(args, "rotation", 1)) {
				selector.flags |= Selector.ENUM_ROTATION.get(args, 0);
			}
			if (line.getOptionArguments(args, "handedness", 1)) {
				selector.flags |= Selector.ENUM_HANDEDNESS.get(args, 0);
			}
			
			if (line.getOptionArguments(args, "flags", 1) &&
					(value = stream.parseInt(args, 0)) != null) {
				selector.flags |= value.intValue();
			}
			
			int type = selector.flags & 0xF;
			if (type == 4 || type == 6) {
				if (line.hasFlag("noSelect")) {
					line.getArguments(args, 0);
					selector.selectFlags = AnimationChannel.SELECT_TYPE_NONE;
					return;
				}
				
				if (args.size() == 2) {
					String arg = args.get(1).trim();
					
					if (!arg.equals("frameRoot") && !arg.equals("frame") && arg.length() > 4) {
						stream.addError(line.createErrorForArgument("Capability can only be 'frame', 'frameRoot', or a PCTP code of 4 charaters or less", 1));
					}
					
					selector.selectCapability = null;
					
					if (arg.equals("root")) {
						selector.selectFlags = AnimationChannel.SELECT_TYPE_ROOT;
					}
					else if (arg.equals("frame")) {
						selector.selectFlags = AnimationChannel.SELECT_TYPE_FRAME;
					}
					else if (arg.equals("frameRoot")) {
						selector.selectFlags = AnimationChannel.SELECT_TYPE_FRAME_ROOT;
					}
					else {
						selector.selectCapability = arg;
						selector.selectFlags = AnimationChannel.SELECT_TYPE_CAP;
					}
				}
				else {
					selector.selectFlags = AnimationChannel.SELECT_TYPE_NOCAP;
				}
				
				if (line.getOptionArguments(args, "selectX", 1)) {
					selector.selectFlags |= AnimationChannel.ENUM_SELECTX.get(args, 0);
				}
				if (line.getOptionArguments(args, "selectY", 1)) {
					selector.selectFlags |= AnimationChannel.ENUM_SELECTY.get(args, 0);
				}
				if (line.getOptionArguments(args, "selectZ", 1)) {
					selector.selectFlags |= AnimationChannel.ENUM_SELECTZ.get(args, 0);
				}
				if (line.getOptionArguments(args, "extent", 1)) {
					selector.selectFlags |= AnimationChannel.ENUM_EXTENT.get(args, 0);
				}
				
				if (line.getOptionArguments(args, "limb", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					selector.selectFlags |= value.intValue() & AnimationChannel.SELECT_LIMB_MASK;
				}
				
				if (line.getOptionArguments(args, "selectFlags", 1) &&
						(value = stream.parseInt(args, 0)) != null) {
					selector.selectFlags |= value.intValue();
				}
			}
		}
	}
	
	public static void addParser(ArgScriptStream<SPAnimation> stream) {
		stream.addParser("event", new ArgScriptBlock<SPAnimation>() {
			final ArgScriptArguments args = new ArgScriptArguments();
			AnimationEvent event;
			
			@Override
			public void parse(ArgScriptLine line) {
				event = new AnimationEvent();
				Number value;
				
				if (line.getArguments(args, 1, 2)) {
					if (args.size() == 2) {
						event.name = args.get(1);
						event.id = HashManager.get().getFileHash(event.name);
					} else {
						event.id = 0;
						event.name = null;
					}
					
					getData().eventMap.put(args.get(0), event);
				}
				
				stream.startBlock(this);
				
				if (line.getOptionArguments(args, "id", 1) && 
						(value = stream.parseFileID(args, 0)) != null) {
					event.id = value.intValue();
				}
				
				if (line.getOptionArguments(args, "type", 1)) {
					event.flags = ENUM_TYPE.get(args, 0);
				}
				
				if (!line.hasOption("type")) {
					line.createError("Must specify type for event");
				}
				
				if (line.hasFlag("chance")) {
					event.flags |= FLAG_CHANCE;
				}
				
				if (line.getOptionArguments(args, "predicate", 2, 6)) {
					if ((args.size() % 2) != 0) {
						stream.addError(line.createErrorForOption("predicate", "Must specify an even number of arguments"));
					}
					else {
						event.flags |= FLAG_PREDICATE;
						event.predicate.parse(args, stream);
					}
				}
				
				if (line.getOptionArguments(args, "param0", 2)) {
					int param = ENUM_PARAMETER0.get(args.get(0));
					event.flags |= param;
					if (param == FLAG_PARAMETER0_INT && (value = stream.parseInt(args, 1)) != null) {
						event.parameter0 = value.intValue();
					}
					else if (param == FLAG_PARAMETER0_FLOAT && (value = stream.parseFloat(args, 1)) != null) {
						event.parameter0 = Float.floatToRawIntBits(value.floatValue());
					}
					else if (param == FLAG_PARAMETER0_ID && (value = stream.parseFileID(args, 1)) != null) {
						event.parameter0 = value.intValue();
					}
				}
				if (line.getOptionArguments(args, "param1", 2)) {
					int param = ENUM_PARAMETER1.get(args.get(0));
					event.flags |= param;
					if (param == FLAG_PARAMETER1_INT && (value = stream.parseInt(args, 1)) != null) {
						event.parameter1 = value.intValue();
					}
					else if (param == FLAG_PARAMETER1_FLOAT && (value = stream.parseFloat(args, 1)) != null) {
						event.parameter1 = Float.floatToRawIntBits(value.floatValue());
					}
					else if (param == FLAG_PARAMETER1_ID && (value = stream.parseFileID(args, 1)) != null) {
						event.parameter1 = value.intValue();
					}
				}
				
				if (line.hasFlag("updatePosition")) event.flags |= FLAG_EFFECT_UPDATEPOSITION;
				if (line.hasFlag("updateRotation")) event.flags |= FLAG_EFFECT_UPDATEROTATION;
				if (line.hasFlag("updateScale")) event.flags |= FLAG_EFFECT_UPDATESCALE;
				if (line.hasFlag("updateParticleScale")) event.flags |= FLAG_EFFECT_UPDATEPARTICLESCALE;
				if (line.hasFlag("updateAttractor")) event.flags |= FLAG_EFFECT_UPDATEATTRACTOR;
				if (line.hasFlag("applyScale")) event.flags |= FLAG_EFFECT_APPLYSCALE;
				if (line.hasFlag("identityColor")) event.flags |= FLAG_EFFECT_IDENTITYCOLOR;
				if (line.hasFlag("hardStop")) event.flags |= FLAG_STOPEFFECT_HARDSTOP;
				
				if (line.getOptionArguments(args, "flags", 1) && 
						(value = stream.parseInt(args, 0)) != null) {
					event.flags |= value.intValue();
				}
				
				if (line.getOptionArguments(args, "archetype", 1)) {
					if (args.get(0).length() > 4 && !args.get(0).equals("default")) {
						stream.addError(line.createErrorForArgument("'archetype' can only be 'default' or a code of 4 characters or less", 1));
					}
					event.archetype = args.get(0);
				}
				
				if (line.getOptionArguments(args, "eventGroup", 1) && 
						(value = stream.parseInt(args, 0)) != null) {
					event.eventGroup = value.intValue();
				}
				
				if (line.getOptionArguments(args, "maxSqrDist", 1) && 
						(value = stream.parseFloat(args, 0)) != null) {
					event.maxSqrDist = value.floatValue();
				}
			}
			
			@Override
			public void setData(ArgScriptStream<SPAnimation> stream_, SPAnimation data) {
				super.setData(stream_, data);
				
				this.addParser("positionSource", ArgScriptParser.create((parser, line) -> parseSelector(line, stream, event.selectors[0])));
				this.addParser("rotationSource", ArgScriptParser.create((parser, line) -> parseSelector(line, stream, event.selectors[1])));
				this.addParser("scaleSource", ArgScriptParser.create((parser, line) -> parseSelector(line, stream, event.selectors[2])));
				this.addParser("source4", ArgScriptParser.create((parser, line) -> parseSelector(line, stream, event.selectors[3])));
			}
		});
	}
}
