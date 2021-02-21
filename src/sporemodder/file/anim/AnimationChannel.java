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

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptWriter;

public class AnimationChannel {
	
	private final static int MAGIC = 0x4E414843;
	public static final String KEYWORD = "channel";
	
	// Selects parent spine and NOT the object itself, 0xC00 is apparently the same??
	// limb modifier
	public static final int SELECT_PARENT_SPINE = 0x1000; 
	
	public static final int SELECT_TYPE_FLAG_MASK = 0x100003;
	public static final int SELECT_TYPE_NOCAP = 0x100002;
	public static final int SELECT_TYPE_FRAME_ROOT = 0x100001;
	public static final int SELECT_TYPE_FRAME = 0x100000;
	public static final int SELECT_TYPE_NONE = 2;
	public static final int SELECT_TYPE_ROOT = 1;
	public static final int SELECT_TYPE_CAP = 0;
	
	public static final int SELECTX_FLAGS = 0x2000C;
	public static final int SELECTX_LEFT = 0x4;
	public static final int SELECTX_RIGHT = 0x8;
	public static final int SELECTX_CENTER = 0xC;
	public static final int SELECTX_LEFT2 = 0x20000;
	public static final int SELECTX_RIGHT2 = 0x20004;
	public static final int SELECTX_CENTER2 = 0x20008;
	
	public static final int SELECTY_FLAGS = 0x40030;
	public static final int SELECTY_FRONT = 0x10;
	public static final int SELECTY_BACK = 0x20;
	public static final int SELECTY_CENTER = 0x30;
	public static final int SELECTY_FRONT2 = 0x40000;
	public static final int SELECTY_BACK2 = 0x40010;
	public static final int SELECTY_CENTER2 = 0x40020;
	
	public static final int SELECTZ_FLAGS = 0x800C0;
	public static final int SELECTZ_TOP = 0x40;
	public static final int SELECTZ_BOTTOM = 0x80;
	public static final int SELECTZ_CENTER = 0xC0;
	public static final int SELECTZ_TOP2 = 0x80000;
	public static final int SELECTZ_BOTTOM2 = 0x80040;
	public static final int SELECTZ_CENTER2 = 0x80080;
	
	public static final int EXTENT_LEFTMOST = 0x2000;
	public static final int EXTENT_RIGHTMOST = 0x4000;
	public static final int EXTENT_FRONTMOST = 0x6000;
	public static final int EXTENT_BACKMOST = 0x8000;
	public static final int EXTENT_TOPMOST = 0xA000;
	public static final int EXTENT_BOTTOMMOST = 0xC000;
	public static final int EXTENT_MASK = 0xE000;
	
	public static final int SELECT_LIMB_MASK = 0x801C00;
	
	public static final int BIND_FLAG_INTERPOLATE = 1;
	public static final int BIND_FLAG_REQUIRE = 4;
	public static final int BIND_FLAG_EVENT = 8;
	public static final int BIND_FLAG_MIRRORING_MASK = 0x32;
	
	public static final int MOVEMENT_FLAG_SECONDARY = 1;
	public static final int MOVEMENT_FLAG_SECONDARY_DIRECTIONAL_ONLY = 2;
	public static final int MOVEMENT_FLAG_LOOKAT = 8;
	// Rescales the Z axis so that 0 is the rest position, and 1 is the ground
	// Only works for relative movement
	public static final int MOVEMENT_FLAG_GROUND_RELATIVE = 0x10;
	// 0x200 in 
	
	public static final ArgScriptEnum ENUM_SELECTX = new ArgScriptEnum();
	static {
		ENUM_SELECTX.add(SELECTX_LEFT, "left");
		ENUM_SELECTX.add(SELECTX_RIGHT, "right");
		ENUM_SELECTX.add(SELECTX_CENTER, "middle");
		ENUM_SELECTX.add(SELECTX_LEFT2, "left2");
		ENUM_SELECTX.add(SELECTX_RIGHT2, "right2");
		ENUM_SELECTX.add(SELECTX_CENTER2, "middle2");
		// These are the good names, we keep the olds for compatiblity
		ENUM_SELECTX.add(SELECTX_CENTER, "center");
		ENUM_SELECTX.add(SELECTX_LEFT2, "localLeft");
		ENUM_SELECTX.add(SELECTX_RIGHT2, "localRight");
		ENUM_SELECTX.add(SELECTX_CENTER2, "localCenter");
	}
	public static final ArgScriptEnum ENUM_SELECTY = new ArgScriptEnum();
	static {
		ENUM_SELECTY.add(SELECTY_FRONT, "front");
		ENUM_SELECTY.add(SELECTY_BACK, "back");
		ENUM_SELECTY.add(SELECTY_CENTER, "middle");
		ENUM_SELECTY.add(SELECTY_FRONT2, "front2");
		ENUM_SELECTY.add(SELECTY_BACK2, "back2");
		ENUM_SELECTY.add(SELECTY_CENTER2, "middle2");
		// These are the good names, we keep the olds for compatiblity
		ENUM_SELECTY.add(SELECTY_CENTER, "center");
		ENUM_SELECTY.add(SELECTY_FRONT2, "localFront");
		ENUM_SELECTY.add(SELECTY_BACK2, "localBack");
		ENUM_SELECTY.add(SELECTY_CENTER2, "localCenter");
	}
	public static final ArgScriptEnum ENUM_SELECTZ = new ArgScriptEnum();
	static {
		ENUM_SELECTZ.add(SELECTZ_TOP, "top");
		ENUM_SELECTZ.add(SELECTZ_BOTTOM, "bottom");
		ENUM_SELECTZ.add(SELECTZ_CENTER, "middle");
		ENUM_SELECTZ.add(SELECTZ_TOP2, "top2");
		ENUM_SELECTZ.add(SELECTZ_BOTTOM2, "bottom2");
		ENUM_SELECTZ.add(SELECTZ_CENTER2, "middle2");
		// These are the good names, we keep the olds for compatiblity
		ENUM_SELECTZ.add(SELECTZ_CENTER, "center");
		ENUM_SELECTZ.add(SELECTZ_TOP2, "localTop");
		ENUM_SELECTZ.add(SELECTZ_BOTTOM2, "localBottom");
		ENUM_SELECTZ.add(SELECTZ_CENTER2, "localCenter");
	}
	public static final ArgScriptEnum ENUM_EXTENT = new ArgScriptEnum();
	static {
		ENUM_EXTENT.add(EXTENT_LEFTMOST, "leftMost");
		ENUM_EXTENT.add(EXTENT_RIGHTMOST, "rightMost");
		ENUM_EXTENT.add(EXTENT_FRONTMOST, "frontMost");
		ENUM_EXTENT.add(EXTENT_BACKMOST, "backMost");
		ENUM_EXTENT.add(EXTENT_TOPMOST, "topMost");
		ENUM_EXTENT.add(EXTENT_BOTTOMMOST, "bottomMost");
	}
	public static final ArgScriptEnum ENUM_MIRRORING = new ArgScriptEnum();
	static {
		ENUM_MIRRORING.add(0, "default");
		ENUM_MIRRORING.add(2, "left");  // mirrors for positive Xs
		ENUM_MIRRORING.add(0x10, "right");  // mirrors for negative Xs
		ENUM_MIRRORING.add(0x12, "reverseDefault");
		ENUM_MIRRORING.add(0x20, "never");
	}
	
	public static class ContextSelector
	{
		int flags;
		String capability;
		int id;  // same field as capability
		int field_8;
		int field_C;
		
		public String getCapability() {
			switch (flags & SELECT_TYPE_FLAG_MASK) {
			case SELECT_TYPE_ROOT:
				return "root";
			case SELECT_TYPE_FRAME_ROOT:
				return "frameRoot";
			case SELECT_TYPE_FRAME:
				return "frame";
			case SELECT_TYPE_CAP:
			case 3:
				return capability;
			case SELECT_TYPE_NOCAP:
			default:
				return null;
			}
		}
	}
	
	public String name;
	public int movementFlags;
	// Actually, a struct of size 10h 
	/*
	 * {
	 * 	int flags;  // if 4: left, if 8: right
	 * 	int capability;
	 * 	int
	 * 	int
	 * }
	 */
	// Used by sub_9B22B0
	public final ContextSelector primaryContext = new ContextSelector();
	// Usually flags is 2 when not used
	public final ContextSelector secondaryContext = new ContextSelector();
	// Actually, short, byte (index of channel, not used), byte (an index?)
	public int bindFlags;
	public int keyframeCount;
	public final List<AnimationComponentData> components = new ArrayList<>();
	
	public AnimationChannel() {
		// secondary context has none by default
		secondaryContext.flags = SELECT_TYPE_NONE;
	}

	public void read(DataStructure stream) throws IOException {
		stream.getStream().seek(stream.getPointer());
		
		int magic = stream.getStream().readLEInt();
		if (magic != MAGIC) {
			throw new IOException("Unsupported channel magic: 0x" + Integer.toHexString(magic));
		}
		
		stream.getStream().skip(4);  // this will be a pointer to the Animation*, so it can be 0
		name = stream.getStream().readCString(StringEncoding.ASCII);
		
		movementFlags = stream.getInt(0x88);
		
		primaryContext.flags = stream.getInt(0x8C);
		stream.getStream().seek(stream.getPointer() + 0x90);
		primaryContext.capability = stream.getStream().readString(StringEncoding.ASCII, 4);
		primaryContext.id = stream.getInt(0x90);
		primaryContext.field_8 = stream.getInt(0x94);
		primaryContext.field_C = stream.getInt(0x98);
		
		secondaryContext.flags = stream.getInt(0x9C);
		stream.getStream().seek(stream.getPointer() + 0xA0);
		secondaryContext.capability = stream.getStream().readString(StringEncoding.ASCII, 4);
		secondaryContext.id = stream.getInt(0xA0);
		secondaryContext.field_8 = stream.getInt(0xA4);
		secondaryContext.field_C = stream.getInt(0xA8);
		
		if ((secondaryContext.flags & 0x100003) == 0x100003) {
			System.out.println("secondary " + HashManager.get().getFileName(secondaryContext.id));
		}
		
		bindFlags = stream.getInt(0xAC);
		
		keyframeCount = stream.getInt(0xD4);  // ?
		long keyframePtr = stream.getUInt(0xD8);
		int count = stream.getInt(0xDC);
		long ptr = stream.getUInt(0xE0);
		
		// each item of size 32
		
		for (int i = 0; i < count; ++i) {
			stream.setPointer(ptr + 32*i);
			
			AnimationComponentData comp = new AnimationComponentData();
			comp.read(stream, keyframePtr);
			components.add(comp);
			
			for (int j = 0; j < keyframeCount; ++j) {
				stream.getStream().seek(keyframePtr + comp.keyframeStride*j + comp.keyframeOffset);
				
				AbstractComponentKeyframe keyframe = comp.createKeyframe();
				keyframe.read(stream.getStream());
				comp.keyframes.add(keyframe);
			}
		}
	}
	
	public void write(StreamWriter stream, List<Long> offsets) throws IOException {
		stream.writeLEInt(MAGIC);
		offsets.add(stream.getFilePointer());
		stream.writeLEInt(0);  // this will be a pointer to the Animation*, does not need to be fixed
		
		stream.writeString(name, StringEncoding.ASCII);
		stream.writePadding(0x80 - name.length());
		
		stream.writeLEInt(movementFlags);
		
		stream.writeLEInt(primaryContext.flags);
		if (primaryContext.capability == null) {
			stream.writePadding(4);
		} else {
			stream.writeString(primaryContext.capability, StringEncoding.ASCII);
			stream.writePadding(4 - primaryContext.capability.length());
		}
		stream.writeLEInt(primaryContext.field_8);
		stream.writeLEInt(primaryContext.field_C);
		
		stream.writeLEInt(secondaryContext.flags);
		if (secondaryContext.capability == null) {
			stream.writePadding(4);
		} else {
			stream.writeString(secondaryContext.capability, StringEncoding.ASCII);
			stream.writePadding(4 - secondaryContext.capability.length());
		}
		stream.writeLEInt(secondaryContext.field_8);
		stream.writeLEInt(secondaryContext.field_C);
		
		stream.writeLEInt(bindFlags);
		stream.writePadding(0xD4 - 0xB0);
		
		stream.writeLEInt(keyframeCount);
		long keyframePtrOffset = stream.getFilePointer();
		stream.writeLEInt(0);  // keyframePtr, will fill later
		offsets.add(keyframePtrOffset);
		
		stream.writeLEInt(components.size());
		offsets.add(stream.getFilePointer());
		long componentsPtr = stream.getFilePointer() + 4;
		stream.writeLEUInt(componentsPtr);  // ptr to component metadata, we can fill it now
		
		// We don't have the offsets yet, so there's no point in writing the data
		stream.writePadding(components.size() * AnimationComponentData.SIZE);
		
		long keyframePtr = stream.getFilePointer();
		stream.seek(keyframePtrOffset);
		stream.writeLEUInt(keyframePtr);
		stream.seek(keyframePtr);
		
		for (int i = 0; i < keyframeCount; ++i) {
			for (AnimationComponentData comp : components) {
				if (i == 0) {
					comp.keyframeOffset = stream.getFilePointer() - keyframePtr;
				}
				else if (i == 1) {
					// It's the same for all keyframes
					comp.keyframeStride = (int) (stream.getFilePointer() - (comp.keyframeOffset + keyframePtr));
				}
				comp.keyframes.get(i).write(stream);
			}
		}
		
		long endPtr = stream.getFilePointer();
		
		stream.seek(componentsPtr);
		for (AnimationComponentData comp : components) {
			comp.write(stream);
		}
		
		stream.seek(endPtr);
	}
	
	public void fixVersion(int version) {
		if (version >= 0xF && version <= 0x12) {
			fixVersion(0x13);
			
			float epsilon = 0.000001f;
			
			for (AnimationComponentData comp : components) {
				if (comp.getType() == RotComponent.TYPE) {
					for (int i = 0; i < keyframeCount; ++i) {
						RotComponent k = (RotComponent) comp.keyframes.get(i);
						if (k.rot.getSquaredLength() < epsilon) {
							k.rot.set(0, 0, 0, 1);
						}
					}
				}
			}
		}
		else if (version == 0x13) {
			primaryContext.flags &= ~0x10300;
			secondaryContext.flags &= ~0x10300;
			
			for (AnimationComponentData comp : components) {
				if (comp.getType() == RotComponent.TYPE) {
					int flags = 0;
					
					if ((movementFlags & 1) != 0 && (movementFlags & 6) != 0) {
						if (movementFlags == 2 || (comp.flags & 0x40) != 0) {
							flags = movementFlags + 0x40;
						}
					}
					else if ((comp.flags & 0x20) != 0) flags = 0x20;
					
					comp.flags &= 0x60;
					comp.flags |= flags;
				}
			}
			fixVersion(0x17);
		}
		else if (version >= 0x14 && version <= 0x16) {
			fixVersion(0x17);
		}
		else if (version == 0x17) {
			// Version 0x17 includes all 6 possible deforms (without its names), 
			// even for capabilities that don't use them
			
			List<Integer> deforms = RigblockComponent.getDeforms(primaryContext.getCapability());
			int rbIndex = 0;
			for (AnimationComponentData comp : components) {
				int type = comp.getType();
				
				if (type == RigblockComponent.TYPE) {
					comp.flags &= ~0x40;
					if (rbIndex < deforms.size()) {
						comp.id = deforms.get(rbIndex);
					} else {
						// This deform is not used by the capability, disable it
						comp.id = 0;
						comp.flags &= ~AnimationComponentData.FLAG_USED;
					}
					
					++rbIndex;
				}
				else if (type == PosComponent.TYPE) {
					if ((movementFlags & 1) != 0) {
						comp.flags &= ~0xC0;
					}
				}
			}
			
			fixVersion(0x18);
		}
		else if (version == 0x18) {
			
			if ((primaryContext.flags & SELECT_TYPE_FLAG_MASK) == 1 || (primaryContext.flags == 0 && primaryContext.capability == "root")) {
				for (AnimationComponentData comp : components) {
					if (comp.getType() == InfoComponent.TYPE) {
						for (AbstractComponentKeyframe k : comp.keyframes) {
							((InfoComponent) k).flags &= ~3;
						}
					}
				}
			}
			
			// This is the end, version 0x19 would be the definitive and does not require anything
		}
	}
	
	public static void selectorToArgScript(ArgScriptWriter writer, ContextSelector context, boolean isSecondary) {
		if (isSecondary) {
			if ((context.flags & SELECT_TYPE_FLAG_MASK) == 0x100003) {
				writer.ints(context.id);
			}
			else if (context.getCapability() != null) {
				writer.arguments(context.getCapability());
			}
		}
		else if (context.getCapability() != null) {
			writer.arguments(context.getCapability());
		}
		
		if (context.flags == SELECT_TYPE_NONE) {
			writer.option("noSelect");
		}
		else if (context.flags != 0) {
			String selectX = ENUM_SELECTX.get(context.flags & SELECTX_FLAGS);
			String selectY = ENUM_SELECTY.get(context.flags & SELECTY_FLAGS);
			String selectZ = ENUM_SELECTZ.get(context.flags & SELECTZ_FLAGS);
			String extent = ENUM_EXTENT.get(context.flags & EXTENT_MASK);
			
			if (selectX != null) writer.option("selectX").arguments(selectX);
			if (selectY != null) writer.option("selectY").arguments(selectY);
			if (selectZ != null) writer.option("selectZ").arguments(selectZ);
			if (extent != null) writer.option("extent").arguments(extent);
			
			if ((context.flags & SELECT_LIMB_MASK) != 0) {
				writer.option("limb").arguments("0x" + Integer.toHexString(context.flags & SELECT_LIMB_MASK));
			}
		
			int flags = context.flags & ~(SELECTX_FLAGS | SELECTY_FLAGS | SELECTZ_FLAGS | EXTENT_MASK | SELECT_TYPE_ROOT | SELECT_TYPE_FLAG_MASK | SELECT_LIMB_MASK);
			if (flags != 0) writer.option("selectFlags").arguments("0x" + Integer.toHexString(flags));
		}
	}
	
	public void toArgScript(ArgScriptWriter writer, SPAnimation animation) throws IOException {
		writer.command(KEYWORD);
		if (name.contains(" ")) writer.literal(name);
		else writer.arguments(name);
		
		selectorToArgScript(writer, primaryContext, false);
		
		if ((movementFlags & MOVEMENT_FLAG_GROUND_RELATIVE) != 0) {
			writer.option("groundRelative");
		}
		if ((movementFlags & MOVEMENT_FLAG_SECONDARY_DIRECTIONAL_ONLY) != 0) {
			writer.option("secondaryDirectionalOnly");
		}
		if ((movementFlags & MOVEMENT_FLAG_LOOKAT) != 0) {
			writer.option("rotRelativeExtTarg");
		}
		
		int field_88_ = movementFlags & ~(MOVEMENT_FLAG_SECONDARY | MOVEMENT_FLAG_GROUND_RELATIVE | MOVEMENT_FLAG_SECONDARY_DIRECTIONAL_ONLY | MOVEMENT_FLAG_LOOKAT);
		if (field_88_ != 0) {
			writer.option("movementFlags").arguments("0x" + Integer.toHexString(field_88_));
		}
		if (field_88_ != 0) {
			System.out.println("field_88_: 0x" + Integer.toHexString(field_88_) + "\tsecondary: " + ((movementFlags & MOVEMENT_FLAG_SECONDARY) == 0));
		}
		
		if ((primaryContext.flags & SELECT_TYPE_FLAG_MASK) == 3) {
			System.out.println("primaryContext.flags:  0x" + Integer.toHexString(primaryContext.flags));
		}
		
		if ((bindFlags & BIND_FLAG_INTERPOLATE) == 0) writer.option("noInterpolate");
		
		if ((bindFlags & BIND_FLAG_REQUIRE) != 0) writer.option("require");
		
		if ((bindFlags & BIND_FLAG_MIRRORING_MASK) != 0) {
			String mirroring = ENUM_MIRRORING.get(bindFlags & BIND_FLAG_MIRRORING_MASK);
			writer.option("mirroring").arguments(mirroring);
			
			if (mirroring == null) {
				System.out.println("unknown mirroring 0x" + Integer.toHexString(bindFlags & BIND_FLAG_MIRRORING_MASK));
			}
		}
		
		writer.option("blendGroup").ints((bindFlags & 0x00FF0000) >> 16);
		
		if (bindFlags != 0) {
			if ((bindFlags & 0xFF000000) != 0) {
				writer.option("variantGroup").ints((bindFlags & 0xFF000000) >> 24);
			}
			
			int flags = bindFlags & ~(BIND_FLAG_INTERPOLATE | BIND_FLAG_EVENT | BIND_FLAG_REQUIRE | BIND_FLAG_MIRRORING_MASK | 0xFFFF0000);
			
			if (flags != 0) writer.option("bindFlags").arguments("0x" + Integer.toHexString(flags));
			
			if (flags != 0) {
				System.out.println("bindFlags: 0x" + Integer.toHexString(bindFlags));
			}
		}
		
		if (secondaryContext.flags != 2 && (movementFlags & MOVEMENT_FLAG_SECONDARY) == 0) {
			writer.option("field_9C").arguments("0x" + Integer.toHexString(secondaryContext.flags));
			System.out.println("unk field_9C 0x" + Integer.toHexString(secondaryContext.flags));
		}
		
		writer.startBlock();
		
		if ((movementFlags & MOVEMENT_FLAG_SECONDARY) != 0) {
			writer.command("secondary");
			
			selectorToArgScript(writer, secondaryContext, true);
		}
		
		for (int i = 0; i < components.size(); ++i) {
			AnimationComponentData comp = components.get(i);

			int type = (comp.flags & 0xF);
			int flags = (comp.flags & ~0xF) & ~AnimationComponentData.FLAG_MASK;
			
			// If it's not used and not INFO (they don't require the flag), skip it
			if (type != InfoComponent.TYPE && (comp.flags & AnimationComponentData.FLAG_USED) == 0) {
				continue;
			}
			
			if (type == InfoComponent.TYPE) {
				writer.command(InfoComponent.KEYWORD);
			}
			else if (type == PosComponent.TYPE) {
				writer.command(PosComponent.KEYWORD);
			}
			else if (type == RotComponent.TYPE) {
				writer.command(RotComponent.KEYWORD);
			}
			else if (type == RigblockComponent.TYPE) {
				writer.command(RigblockComponent.KEYWORD).arguments(HashManager.get().getFileName(comp.id));
			}
			
			if ((comp.flags & AnimationComponentData.FLAG_RELATIVE) != 0) writer.option("relative");
			else {
				if (type == RotComponent.TYPE) {
					System.out.println("absolute rot");
				}
			}
			int scale = comp.flags & AnimationComponentData.FLAG_SCALE_MASK;
			if (scale != 0) writer.option("scaleMode").arguments(AnimationComponentData.ENUM_SCALE.get(scale));
			
			if (flags != 0) {
				writer.option("flags").arguments("0x" + Integer.toHexString(flags));
			}
			
			writer.startBlock();
			
			for (AbstractComponentKeyframe keyframe : comp.keyframes) {
				writer.indentNewline();
				keyframe.toArgScript(writer, animation);
			}
			
			writer.endBlock().commandEND();
		}
		
		writer.endBlock().commandEND();
	}
}
