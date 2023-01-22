package sporemodder.files;

import java.io.IOException;

public interface InputStreamAccessor extends StreamAccessor {
	// This should be handled on FileStructure instead, for proper error handling
//	public void expect(int arg0, int arg1, String text) throws IOException;
//	public void expect(byte[] arg0, byte[] arg1, String text) throws IOException;
//	public void expect(int[] arg0, int[] arg1, String text) throws IOException;
	
	public byte[] toByteArray() throws IOException;
	
	public void read(byte[] arr) throws IOException;
	
	public String readCString() throws IOException;
	public String readString8(int length) throws IOException;
	public String readString16(int length) throws IOException;
	public String readLEString16(int length) throws IOException;
	public String readString(String encoding, int length, int characterSize) throws IOException;
	public String readLine() throws IOException;
	
	public boolean readBoolean() throws IOException;
	public void readBooleans(boolean[] dst) throws IOException;
	
	public byte readByte() throws IOException;
	public short readUByte() throws IOException;
	public void readBytes(byte[] dst) throws IOException;
	public void readUBytes(int[] dst) throws IOException;
	
	public char readChar() throws IOException;
	public void readChars(char[] dst) throws IOException;
	
	public short readShort() throws IOException;
	public short readLEShort() throws IOException; 
	public int readUShort() throws IOException;
	public int readLEUShort() throws IOException;
	public void readShorts(short[] dst) throws IOException;
	public void readLEShorts(short[] dst) throws IOException;
	public void readUShorts(int[] dst) throws IOException;
	public void readLEUShorts(int[] dst) throws IOException;
	
	public int readInt() throws IOException;
	public int readLEInt() throws IOException;
	public long readUInt() throws IOException;
	public long readLEUInt() throws IOException;
	public void readInts(int[] dst) throws IOException;
	public void readLEInts(int[] dst) throws IOException;
	public void readUInts(long[] dst) throws IOException;
	public void readLEUInts(long[] dst) throws IOException;
	
	public long readLong() throws IOException;
	public long readLELong() throws IOException;
	public void readLongs(long[] dst) throws IOException;
	public void readLELongs(long[] dst) throws IOException;
	
	public float readFloat() throws IOException;
	public float readLEFloat() throws IOException;
	public void readFloats(float[] dst) throws IOException;
	public void readLEFloats(float[] dst) throws IOException;
	
	public double readDouble() throws IOException;
	public double readLEDouble() throws IOException;
	public void readDoubles(double[] dst) throws IOException;
	public void readLEDoubles(double[] dst) throws IOException;
	
	//TODO Implement readStructure or something similar?
}
