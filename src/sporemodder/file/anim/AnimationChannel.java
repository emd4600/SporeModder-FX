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
	
	public static final int SELECTX_FLAGS = 0x2000C;
	public static final int SELECTX_LEFT = 0x4;
	public static final int SELECTX_RIGHT = 0x8;
	public static final int SELECTX_MIDDLE = 0xC;
	public static final int SELECTX_LEFT2 = 0x20000;
	public static final int SELECTX_RIGHT2 = 0x20004;
	public static final int SELECTX_MIDDLE2 = 0x20008;
	
	public static final int SELECTY_FLAGS = 0x40030;
	public static final int SELECTY_FRONT = 0x10;
	public static final int SELECTY_BACK = 0x20;
	public static final int SELECTY_MIDDLE = 0x30;
	public static final int SELECTY_FRONT2 = 0x40000;
	public static final int SELECTY_BACK2 = 0x40010;
	public static final int SELECTY_MIDDLE2 = 0x40020;
	
	public static final int SELECTZ_FLAGS = 0x800C0;
	public static final int SELECTZ_TOP = 0x40;
	public static final int SELECTZ_BOTTOM = 0x80;
	public static final int SELECTZ_MIDDLE = 0xC0;
	public static final int SELECTZ_TOP2 = 0x80000;
	public static final int SELECTZ_BOTTOM2 = 0x80040;
	public static final int SELECTZ_MIDDLE2 = 0x80080;
	
	public static final ArgScriptEnum ENUM_SELECTX = new ArgScriptEnum();
	static {
		ENUM_SELECTX.add(SELECTX_LEFT, "left");
		ENUM_SELECTX.add(SELECTX_RIGHT, "right");
		ENUM_SELECTX.add(SELECTX_MIDDLE, "middle");
		ENUM_SELECTX.add(SELECTX_LEFT2, "left2");
		ENUM_SELECTX.add(SELECTX_RIGHT2, "right2");
		ENUM_SELECTX.add(SELECTX_MIDDLE2, "middle2");
	}
	public static final ArgScriptEnum ENUM_SELECTY = new ArgScriptEnum();
	static {
		ENUM_SELECTY.add(SELECTY_FRONT, "front");
		ENUM_SELECTY.add(SELECTY_BACK, "back");
		ENUM_SELECTY.add(SELECTY_MIDDLE, "middle");
		ENUM_SELECTY.add(SELECTY_FRONT2, "front2");
		ENUM_SELECTY.add(SELECTY_BACK2, "back2");
		ENUM_SELECTY.add(SELECTY_MIDDLE2, "middle2");
	}
	public static final ArgScriptEnum ENUM_SELECTZ = new ArgScriptEnum();
	static {
		ENUM_SELECTZ.add(SELECTZ_TOP, "top");
		ENUM_SELECTZ.add(SELECTZ_BOTTOM, "bottom");
		ENUM_SELECTZ.add(SELECTZ_MIDDLE, "middle");
		ENUM_SELECTZ.add(SELECTZ_TOP2, "top2");
		ENUM_SELECTZ.add(SELECTZ_BOTTOM2, "bottom2");
		ENUM_SELECTZ.add(SELECTZ_MIDDLE2, "middle2");
	}
	
	public String name;
	public int field_88;
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
	public int field_8C;
	// Similar flags to field_8C, doesn't seem to be important
	public int field_9C = 2;
	// Actually, short, byte (index of channel, not used), byte (an index?)
	public int field_AC;
	public String capability;
	public int keyframeCount;
	public final List<AnimationComponentData> components = new ArrayList<>();

	public void read(DataStructure stream) throws IOException {
		stream.getStream().seek(stream.getPointer());
		
		int magic = stream.getStream().readLEInt();
		if (magic != MAGIC) {
			throw new IOException("Unsupported channel magic: 0x" + Integer.toHexString(magic));
		}
		
		stream.getStream().skip(4);  // this will be a pointer to the Animation*, so it can be 0
		name = stream.getStream().readCString(StringEncoding.ASCII);
		
		field_88 = stream.getInt(0x88);
		field_8C = stream.getInt(0x8C);  // 1 for root, usually 0 for the rest
		stream.getStream().seek(stream.getPointer() + 0x90);
		capability = stream.getStream().readString(StringEncoding.ASCII, 4);
		// 90h is wpch, eycl, etc -> capability fourCC in pctp file. 'root' doesn't use it
		
		field_9C = stream.getInt(0x9C);
		
		field_AC = stream.getInt(0xAC);
		
		keyframeCount = stream.getInt(0xD4);  // ?
		long keyframePtr = stream.getUInt(0xD8);
		int count = stream.getInt(0xDC);
		long ptr = stream.getUInt(0xE0);
		System.out.println(name + "  " + field_8C + "\tkeyframes[" + keyframeCount + "] 0x" + Integer.toHexString(stream.getInt(0xD8)));
		
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
		
		System.out.println();
	}
	
	public void write(StreamWriter stream, List<Long> offsets) throws IOException {
		stream.writeLEInt(MAGIC);
		offsets.add(stream.getFilePointer());
		stream.writeLEInt(0);  // this will be a pointer to the Animation*, does not need to be fixed
		
		stream.writeString(name, StringEncoding.ASCII);
		stream.writePadding(0x80 - name.length());
		
		stream.writeLEInt(field_88);
		stream.writeLEInt(field_8C);
		if (capability == null) {
			stream.writePadding(4);
		} else {
			stream.writeString(capability, StringEncoding.ASCII);
			stream.writePadding(4 - capability.length());
		}
		stream.writeLEInt(0);  // field_94 ?
		stream.writeLEInt(0);  // field_98 ?
		stream.writeLEInt(field_9C);
		stream.writePadding(12);
		stream.writeLEInt(field_AC);
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
			field_8C &= ~0x10300;
			field_9C &= ~0x10300;
			
			for (AnimationComponentData comp : components) {
				if (comp.getType() == RotComponent.TYPE) {
					int flags = 0;
					
					if ((field_88 & 1) != 0 && (field_88 & 6) != 0) {
						if (field_88 == 2 || (comp.flags & 0x40) != 0) {
							flags = field_88 + 0x40;
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
			
			List<Integer> deforms = RigblockComponent.getDeforms(getCapability());
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
					if ((field_88 & 1) != 0) {
						comp.flags &= ~0xC0;
					}
				}
			}
			
			fixVersion(0x18);
		}
		else if (version == 0x18) {
			
			if ((field_8C & 0x100003) == 1 || (field_8C == 0 && capability == "root")) {
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
	
	public void toArgScript(ArgScriptWriter writer, SPAnimation animation) throws IOException {
		writer.command(KEYWORD);
		if (name.contains(" ")) writer.literal(name);
		else writer.arguments(name);
		//TODO probably more elaborate?
		if (getCapability() != null) {
			writer.arguments(getCapability());
		}
		if (field_8C != 0) {
			String selectX = ENUM_SELECTX.get(field_8C & SELECTX_FLAGS);
			String selectY = ENUM_SELECTY.get(field_8C & SELECTY_FLAGS);
			String selectZ = ENUM_SELECTZ.get(field_8C & SELECTZ_FLAGS);
			
			if (selectX != null) writer.option("selectX").arguments(selectX);
			if (selectY != null) writer.option("selectY").arguments(selectY);
			if (selectZ != null) writer.option("selectZ").arguments(selectZ);
		
			int flags = field_8C & ~(SELECTY_FLAGS | SELECTY_FLAGS | SELECTZ_FLAGS);
			if (flags != 0) writer.option("field_8C").arguments("0x" + Integer.toHexString(flags));
		}
		
		if (field_9C != 2) {
			writer.option("field_9C").arguments("0x" + Integer.toHexString(field_9C));
		}
		
		if (field_AC != 0) {
			writer.option("field_AC").arguments("0x" + Integer.toHexString(field_AC));
		}
		
		if (field_88 != 0) {
			writer.option("field_88").ints(field_88);
		}
		
		writer.startBlock();
		
		for (int i = 0; i < components.size(); ++i) {
			AnimationComponentData comp = components.get(i);

			int type = (comp.flags & 0xF);
			int flags = (comp.flags & ~0xF) & ~AnimationComponentData.FLAG_USED;
			
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
	
	public String getCapability() {
		if ((field_8C & 0x100003) == 0) return capability;
		else if ((field_8C & 0x100003) == 1) return "root";
		else return null;
	}
}
