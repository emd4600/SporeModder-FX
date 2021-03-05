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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Matrix;
import sporemodder.util.Vector3;

/**
 * A special type of matrix used by RenderWare files, made of 3 rows of 4 columns each.
 * Apparently, the first 3 columns contain rotation information, whereas the last column
 * contains the position.
 */
public class RWMatrix3x4 {
	
	public final int NUM_ROWS = 3;
	public final int NUM_COLS = 4;

	public final float[][] m = new float[3][4];
	
	public RWMatrix3x4() {
		for (int i = 0; i < NUM_ROWS; i++) {
			float[] row = new float[NUM_COLS];
			if (i < NUM_COLS) {
				row[i] = 1.0f;
			}
			m[i] = row;
		}
	}
	
	/**
	 * Returns a copy of the rotation part of this matrix;
	 * @return
	 */
	public Matrix getRotation() {
		Matrix matrix = Matrix.getIdentity();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				matrix.m[i][j] = m[i][j];
			}
		}
		return matrix;
	}
	
	/**
	 * Returns a copy of the offset (translation) part of this matrix.
	 * @return
	 */
	public Vector3 getOffset() {
		return new Vector3(m[0][3], m[1][3], m[2][3]);
	}
	
	public void setRotation(Matrix matrix) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				m[i][j] = matrix.m[i][j];
			}
		}
	}
	
	public void setOffset(Vector3 vector) {
		m[0][3] = vector.getX();
		m[1][3] = vector.getY();
		m[2][3] = vector.getZ();
	}
	
	public void read(StreamReader stream) throws IOException {
		for (float[] dst : m) {
			stream.readLEFloats(dst);
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		for (float[] dst : m) {
			stream.writeLEFloats(dst);
		}
	}
}
