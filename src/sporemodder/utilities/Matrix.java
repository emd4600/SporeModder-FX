package sporemodder.utilities;

import java.io.IOException;
import java.util.Arrays;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class Matrix {
	public float[][] m = new float[3][3];
	
	// user should use getIdentity instead;
	private Matrix() {};
	
	public Matrix(Matrix other)
	{
		copy(other);
	}
	
//	public Matrix(double eulerX, double eulerY, double eulerZ) {
//		rotate(eulerX, eulerY, eulerZ);
//	}
	
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
	
	public Matrix transpose() {
		Matrix temp = new Matrix();
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp.m[j][i] = m[i][j];
        
        return temp;
    }
	
	public Matrix readLE(InputStreamAccessor in) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				m[i][f] = in.readLEFloat();
			}
		}
		return this;
	}
	
	public Matrix readBE(InputStreamAccessor in) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				m[i][f] = in.readFloat();
			}
		}
		return this;
	}
	
	public Matrix writeLE(OutputStreamAccessor out) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				out.writeLEFloat(m[i][f]);
			}
		}
		return this;
	}
	
	public Matrix writeBE(OutputStreamAccessor out) throws IOException {
		for (int i = 0; i < 3; i++) {
			for (int f = 0; f < 3; f++) {
				out.writeFloat(m[i][f]);
			}
		}
		return this;
	}
	
	public static void main(String[] args) {
		System.out.println(Arrays.toString(new double[] {Math.toRadians(12), Math.toRadians(35), Math.toRadians(74)}));
		System.out.println(Matrix.getIdentity().rotate(Math.toRadians(12), Math.toRadians(35), Math.toRadians(74)));
		System.out.println(Matrix.getIdentity().rotate(Math.toRadians(12), Math.toRadians(35), Math.toRadians(74)).transpose());
		System.out.println();
		System.out.println(Arrays.toString(Matrix.getIdentity().rotate(Math.toRadians(12), Math.toRadians(35), Math.toRadians(74)).toEulerDegrees()));
		System.out.println(Arrays.toString(Matrix.getIdentity().rotate(Math.toRadians(12), Math.toRadians(35), Math.toRadians(74)).transpose().toEulerDegrees()));
	}
}
