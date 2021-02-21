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
package sporemodder.file.spui;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class SPUIRectangle {

	public float x1;
	public float y1;
	public float x2;
	public float y2;
	
	public SPUIRectangle() {}
	
	public SPUIRectangle(SPUIRectangle other) {
		copy(other);
	}
	
	public SPUIRectangle(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public void copy(SPUIRectangle other) {
		this.x1 = other.x1;
		this.x2 = other.x2;
		this.y1 = other.y1;
		this.y2 = other.y2;
	}

	@Override
	public String toString() {
		return "SPUIRectangle [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + "]";
	}

	public void set(int i, float value) {
		if (i == 0) x1 = value;
		else if (i == 1) y1 = value;
		else if (i == 2) x2 = value;
		else y2 = value;
	}
	
	public float get(int i) {
		if (i == 0) return x1;
		else if (i == 1) return y1;
		else if (i == 2) return x2;
		else return y2;
	}
	
	public final float getWidth() {
		return x2 - x1;
	}
	
	public final float getHeight() {
		return y2 - y1;
	}
	
	public SPUIRectangle setWidth(float width) {
		x2 = x1 + width;
		return this;
	}
	
	public SPUIRectangle setHeight(float height) {
		y2 = y1 + height;
		return this;
	}
	
	public SPUIRectangle translateX(float dx) {
		x1 += dx;
		x2 += dx;
		return this;
	}
	
	public SPUIRectangle translateY(float dy) {
		y1 += dy;
		y2 += dy;
		return this;
	}
	
	public SPUIRectangle translate(double dx, double dy) {
		x1 += dx;
		y1 += dy;
		x2 += dx;
		y2 += dy;
		return this;
	}
	
	public boolean contains(double x, double y) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}
	
	public void read(StreamReader stream) throws IOException {
		x1 = stream.readLEFloat();
		y1 = stream.readLEFloat();
		x2 = stream.readLEFloat();
		y2 = stream.readLEFloat();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEFloats(x1, y1, x2, y2);
	}

	public boolean compare(SPUIRectangle other) {
		return x1 == other.x1 && x2 == other.x2 && y1 == other.y1 && y2 == other.y2;
	}
}
