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
import sporemodder.util.Matrix;
import sporemodder.util.Vector3;

public class RWAnimationSkin extends RWObject {
	
	public static final int TYPE_CODE = 0x70003;
	public static final int ALIGNMENT = 16;
	
	public static class BonePose {
		public final Matrix absBindPose = Matrix.getIdentity();
		public final Vector3 invPoseTranslation = new Vector3();
	}
	
	public final List<BonePose> data = new ArrayList<BonePose>();
	public int field_8;
	public int field_C;

	public RWAnimationSkin(RenderWare renderWare) {
		super(renderWare);
	}

	@Override
	public void read(StreamReader stream) throws IOException {
		long offset = stream.readLEUInt();
		int count = stream.readLEInt();
		field_8 = stream.readLEInt();
		field_C = stream.readLEInt();
		
		stream.seek(offset);
		for (int i = 0; i < count; i++) {
			
			BonePose pose = new BonePose();

			RWMatrix3x4 matrix = new RWMatrix3x4();
			matrix.read(stream);
			pose.absBindPose.copy(matrix.getRotation());
			
			pose.invPoseTranslation.setX(stream.readLEFloat());
			pose.invPoseTranslation.setY(stream.readLEFloat());
			pose.invPoseTranslation.setZ(stream.readLEFloat());
			
			stream.skip(4);
		}
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEUInt(stream.getFilePointer() + 16);
		stream.writeLEInt(data.size());
		stream.writeLEInt(field_8);
		stream.writeLEInt(field_C);
		
		for (BonePose pose : data) {
			RWMatrix3x4 matrix = new RWMatrix3x4();
			matrix.setRotation(pose.absBindPose);
			matrix.write(stream);
			
			pose.invPoseTranslation.writeLE(stream);
			stream.writePadding(4);
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
