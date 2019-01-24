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
package sporemodder.util;

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class Matrix {
	public float[][] m = new float[3][3];
	
	// user should use getIdentity instead;
	private Matrix() {};
	
	public Matrix(Matrix other)
	{
		copy(other);
	}
	
	public static Matrix getIdentity() {
		Matrix matrix = new Matrix();
		matrix.m[0][0] = 1.0f;
		matrix.m[1][1] = 1.0f;
		matrix.m[2][2] = 1.0f;
		return matrix;
	}
	
	@Override
	public String toString() {
		return "[[" + m[0][0] + ", " + m[0][1] + ", " + m[0][2] + "],\n"
				+ "[" + m[1][0] + ", " + m[1][1] + ", " + m[1][2] + "],\n"
				+ "[" + m[2][0] + ", " + m[2][1] + ", " + m[2][2] + "]]";
	}
	
	public float get(int row, int column) {
		return m[row][column];
	}
	
	public void copy(Matrix other) {
		m[0][0] = other.m[0][0];
		m[0][1] = other.m[0][1];
		m[0][2] = other.m[0][2];
		m[1][0] = other.m[1][0];
		m[1][1] = other.m[1][1];
		m[1][2] = other.m[1][2];
		m[2][0] = other.m[2][0];
		m[2][1] = other.m[2][1];
		m[2][2] = other.m[2][2];
	}
	
	public Vector3 multiply(Vector3 vector) {
		Vector3 output = new Vector3();
		
		output.x = m[0][0] * vector.x + m[0][1] * vector.y + m[0][2] * vector.z;
		output.y = m[1][0] * vector.x + m[1][1] * vector.y + m[1][2] * vector.z;
		output.z = m[2][0] * vector.x + m[2][1] * vector.y + m[2][2] * vector.z;
		
		return output;
	}
	
	//TODO it should be transposed?
	/**
	 * Rotates the matrix by the specified angles, in radians.
	 * @param eulerRadiansX The 'bank' rotation, around the X axis, in radians.
	 * @param eulerRaidansY The 'heading' rotation, around the Y axis, in radians.
	 * @param eulerRadiansZ The 'attitude' rotation, around the Z axis, in radians.
	 * @return
	 */
	public Matrix rotate(double eulerRadiansX, double eulerRaidansY, double eulerRadiansZ) {
	    
	    double ci = Math.cos(eulerRadiansX);
	    double cj = Math.cos(eulerRaidansY);
	    double ch = Math.cos(eulerRadiansZ);
	    double si = Math.sin(eulerRadiansX);
	    double sj = Math.sin(eulerRaidansY);
	    double sh = Math.sin(eulerRadiansZ);
	    
	    double cc = ci * ch;
	    double cs = ci * sh;
	    double sc = si * ch;
		double ss = si * sh;
		
		m[0][0] = (float)(cj * ch);
		m[0][1] = (float)(sj * sc - cs);
		m[0][2] = (float)(sj * cc + ss);
		m[1][0] = (float)(cj * sh);
		m[1][1] = (float)(sj * ss + cc);
		m[1][2] = (float)(sj * cs - sc);
		m[2][0] = (float)-sj;
		m[2][1] = (float)(cj * si);
		m[2][2] = (float)(cj * ci);
	    
	    return this;
	}
	
	public float[] toEulerDegrees() {
		float[] rotation = new float[3];
		rotation[0] = (float) Math.toDegrees(Math.atan2(m[2][1], m[2][2]));
		rotation[1] = (float) Math.toDegrees(-Math.asin(m[2][0]));
		rotation[2] = (float) Math.toDegrees(Math.atan2(m[1][0], m[0][0]));
		return rotation;
	}
	
	public Matrix transposed() {
		Matrix temp = new Matrix();
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp.m[j][i] = m[i][j];
        
        return temp;
    }
	
	public Matrix readLE(StreamReader stream) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				m[i][f] = stream.readLEFloat();
			}
		}
		return this;
	}
	
	public Matrix readBE(StreamReader stream) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				m[i][f] = stream.readFloat();
			}
		}
		return this;
	}
	
	public Matrix writeLE(StreamWriter stream) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				stream.writeLEFloat(m[i][f]);
			}
		}
		return this;
	}
	
	public Matrix writeBE(StreamWriter stream) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				stream.writeFloat(m[i][f]);
			}
		}
		return this;
	}
	
	public static Matrix fromQuaternion(Vector4 q) {
		Matrix result = Matrix.getIdentity();
		
	    double sqw = q.w*q.w;
	    double sqx = q.x*q.x;
	    double sqy = q.y*q.y;
	    double sqz = q.z*q.z;

	    // invs (inverse square length) is only required if quaternion is not already normalised
	    double invs = 1 / (sqx + sqy + sqz + sqw);
	    result.m[0][0] = (float) (( sqx - sqy - sqz + sqw)*invs) ; // since sqw + sqx + sqy + sqz =1/invs*invs
	    result.m[1][1] = (float) ((-sqx + sqy - sqz + sqw)*invs) ;
	    result.m[2][2] = (float) ((-sqx - sqy + sqz + sqw)*invs) ;
	    
	    double tmp1 = q.x*q.y;
	    double tmp2 = q.z*q.w;
	    result.m[1][0] = (float) (2.0 * (tmp1 + tmp2)*invs) ;
	    result.m[0][1] = (float) (2.0 * (tmp1 - tmp2)*invs) ;
	    
	    tmp1 = q.x*q.z;
	    tmp2 = q.y*q.w;
	    result.m[2][0] = (float) (2.0 * (tmp1 - tmp2)*invs);
	    result.m[0][2] = (float) (2.0 * (tmp1 + tmp2)*invs);
	    tmp1 = q.y*q.z;
	    tmp2 = q.x*q.w;
	    result.m[2][1] = (float) (2.0 * (tmp1 + tmp2)*invs);
	    result.m[1][2] = (float) (2.0 * (tmp1 - tmp2)*invs);     
	    
	    return result;
	}
}
