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

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.util.Vector3;
import sporemodder.util.Vector4;

public abstract class RWKeyframe {

	public float time;
	
	public abstract void read(StreamReader stream) throws IOException;
	public abstract void write(StreamWriter stream) throws IOException;
	
	public abstract int getComponents();
	public abstract int getSize();

	public static class LocRotScale extends RWKeyframe {
		
		public static final int COMPONENTS = 0x601;
		public static final int SIZE = 48;
		
		public final Vector3 location = new Vector3();
		public final Vector4 rotation = new Vector4();
		public final Vector3 scale = new Vector3();

		@Override
		public void read(StreamReader stream) throws IOException {
			rotation.readLE(stream);
			location.readLE(stream);
			scale.readLE(stream);
			stream.skip(4);
			time = stream.readLEFloat();
		}

		@Override
		public void write(StreamWriter stream) throws IOException {
			rotation.writeLE(stream);
			location.writeLE(stream);
			scale.writeLE(stream);
			stream.writePadding(4);
			stream.writeLEFloat(time);
		}

		@Override
		public int getComponents() {
			return COMPONENTS;
		}

		@Override
		public int getSize() {
			return SIZE;
		}
		
	}
	
	public static class LocRot extends RWKeyframe {
		
		public static final int COMPONENTS = 0x101;
		public static final int SIZE = 32;
		
		public final Vector3 location = new Vector3();
		public final Vector4 rotation = new Vector4();

		@Override
		public void read(StreamReader stream) throws IOException {
			rotation.readLE(stream);
			location.readLE(stream);
			//stream.skip(4);
			time = stream.readLEFloat();
		}

		@Override
		public void write(StreamWriter stream) throws IOException {
			rotation.writeLE(stream);
			location.writeLE(stream);
			//stream.writePadding(4);
			stream.writeLEFloat(time);
		}

		@Override
		public int getComponents() {
			return COMPONENTS;
		}

		@Override
		public int getSize() {
			return SIZE;
		}
		
	}
	
	public static class BlendFactor extends RWKeyframe {
		
		public static final int COMPONENTS = 0x100;
		public static final int SIZE = 8;
		
		public float factor = 0.0f;

		@Override
		public void read(StreamReader stream) throws IOException {
			factor = stream.readLEFloat();
			time = stream.readLEFloat();
		}

		@Override
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEFloat(factor);
			stream.writeLEFloat(time);
		}

		@Override
		public int getComponents() {
			return COMPONENTS;
		}

		@Override
		public int getSize() {
			return SIZE;
		}
		
	}
}
