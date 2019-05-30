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
package sporemodder.file.rw4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;

public class RWSkeleton extends RWObject {
	
	public static final int TYPE_CODE = 0x70002;
	public static final int ALIGNMENT = 4;
	
	public static class Bone {
		public int name;
		public int flags;
		public Bone parent;
		
		public Bone() {}

		public Bone(int name, int flags, Bone parent) {
			super();
			this.name = name;
			this.flags = flags;
			this.parent = parent;
		}

		@Override
		public String toString() {
			return "Bone [name=" + HashManager.get().getFileName(name) + ", flags=" + flags + ", parent=" + parent + "]";
		}
	}
	
	public int skeletonID;
	public final List<Bone> bones = new ArrayList<Bone>();

	public RWSkeleton(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		long pBoneFlags = stream.readLEUInt();
		long pBoneParents = stream.readLEUInt();
		long pBoneNames = stream.readLEUInt();
		int count = stream.readLEInt();
		skeletonID = stream.readLEInt();
		// The bone count again?
		stream.skip(4);
		
		for (int i = 0; i < count; i++) {
			bones.add(new Bone());
		}
		
		stream.seek(pBoneNames);
		for (int i = 0; i < count; i++) {
			bones.get(i).name = stream.readLEInt();
		}
		
		stream.seek(pBoneFlags);
		for (int i = 0; i < count; i++) {
			bones.get(i).flags = stream.readLEInt();
		}
		
		stream.seek(pBoneParents);
		for (int i = 0; i < count; i++) {
			int index = stream.readLEInt();
			bones.get(i).parent = index == -1 ? null : bones.get(index);
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		long baseOffset = stream.getFilePointer();
		
		// We will calculate the offsets
		stream.writeLEUInt(baseOffset + 24 + bones.size()*4);
		stream.writeLEUInt(baseOffset + 24 + bones.size()*8);
		stream.writeLEUInt(baseOffset + 24);
		
		stream.writeLEInt(bones.size());
		stream.writeLEInt(skeletonID);
		stream.writeLEInt(bones.size());
		
		for (Bone bone : bones) {
			stream.writeLEInt(bone.name);
		}
		for (Bone bone : bones) {
			stream.writeLEInt(bone.flags);
		}
		for (Bone bone : bones) {
			if (bone.parent == null) {
				stream.writeLEInt(-1);
			}
			else {
				int index = -1;
				for (Bone b : bones) {
					index++;
					if (b == bone.parent) {
						break;
					}
				}
				stream.writeLEInt(index);
			}
		}
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}
	
	@Override
	public int getAlignment() {
		return ALIGNMENT;
	}

}
