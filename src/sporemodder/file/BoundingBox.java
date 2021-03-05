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
package sporemodder.file;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Vector3;

public class BoundingBox {

	private final Vector3 min = new Vector3();
	private final Vector3 max = new Vector3();
	
	public BoundingBox() {
		
	}
	
	public BoundingBox(Vector3 min, Vector3 max) {
		this.min.set(min);
		this.max.set(max);
	}
	
	public Vector3 getMin() {
		return min;
	}
	
	public Vector3 getMax() {
		return max;
	}
	
	public void setMin(Vector3 min) {
		this.min.set(min);
	}
	
	public void setMax(Vector3 max) {
		this.max.set(max);
	}

	public void read(StreamReader stream) throws IOException {
		min.readLE(stream);
		max.readLE(stream);
	}
	
	public void write(StreamWriter stream) throws IOException {
		min.writeLE(stream);
		max.writeLE(stream);
	}
	
	public float getBiggestX() {
		return Math.max(Math.abs(min.getX()), Math.abs(max.getX()));
	}
	
	public float getBiggestY() {
		return Math.max(Math.abs(min.getY()), Math.abs(max.getY()));
	}
	
	public float getBiggestZ() {
		return Math.max(Math.abs(min.getZ()), Math.abs(max.getZ()));
	}
	
	public float getBiggest() {
		return Math.max(getBiggestX(), Math.max(getBiggestY(), getBiggestZ()));
	}

	public float getLengthX() {
		return max.getX() - min.getX();
	}
	
	public float getLengthY() {
		return max.getY() - min.getY();
	}
	
	public float getLengthZ() {
		return max.getZ() - min.getZ();
	}
	
	public float getBiggestLength() {
		return Math.max(getLengthX(), Math.max(getLengthY(), getLengthZ()));
	}
}
