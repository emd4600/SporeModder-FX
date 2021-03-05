package sporemodder.file.filestructures;

import java.io.IOException;

public interface StreamReader extends Stream {
	
	/**
	 * Returns the content of this stream as a byte array with the same length as the amount of bytes in the stream.
	 */
	public byte[] toByteArray() throws IOException;
	
	/**
	 * Reads dst.length bytes into the provided destination array, and moves the file pointer dst.length positions forward.
	 */
	public void read(byte[] dst) throws IOException;
	
	/** Reads a string with the provided encoding, until a 00 character is found. */
	public String readCString(StringEncoding encoding) throws IOException;
	/** Reads a string of the given length with the provided encoding. When a 00 character is found,
	 * no more bytes are read and the string is returned. This means the returned string might have less
	 * characters than the specified length. */
	public String readString(StringEncoding encoding, int length) throws IOException;
	
	public String readLine() throws IOException;
	
	/** Reads an int8 value interpreted as a boolean, and moves the file pointer 1 position forward. 
	 *  If the value read is 0, it returns false; if it's  not 0, it returns true. */ 
	public boolean readBoolean() throws IOException;
	/** Reads dst.length int8 values interpreted as booleans, and moves the file pointer dst.length positions forward. 
	 *  When the value read is 0, it pushes false into the array; if it's not 0, it pushes true. */ 
	public void readBooleans(boolean[] dst) throws IOException;
	
	/** Reads an int8 value, and moves the file pointer 1 position forward. */ 
	public byte readByte() throws IOException;
	/** Reads an uint8 value, and moves the file pointer 1 position forward. */
	public short readUByte() throws IOException;
	/** Reads dst.length int8 values, and moves the file pointer dst.length positions forward. */
	public void readBytes(byte[] dst) throws IOException;
	/** Reads dst.length uint8 values, and moves the file pointer dst.length positions forward. */
	public void readUBytes(int[] dst) throws IOException;
	
	/** Reads an int8 value interpreted as a char, and moves the file pointer 1 position forward. */ 
	public char readChar() throws IOException;
	/** Reads dst.length int8 values interpreted as chars, and moves the file pointer dst.length positions forward. */
	public void readChars(char[] dst) throws IOException;
	
	/** Reads an int16 value in big-endian order, and moves the file pointer 2 positions forward. */ 
	public short readShort() throws IOException;
	/** Reads an int16 value in little-endian order, and moves the file pointer 2 positions forward. */ 
	public short readLEShort() throws IOException; 
	/** Reads an uint16 value in big-endian order, and moves the file pointer 2 positions forward. */ 
	public int readUShort() throws IOException;
	/** Reads an uint16 value in little-endian order, and moves the file pointer 2 positions forward. */
	public int readLEUShort() throws IOException;
	/** Reads dst.length int16 values in big-endian order, and moves the file pointer dst.length * 2 positions forward. */
	public void readShorts(short[] dst) throws IOException;
	/** Reads dst.length int16 values in little-endian order, and moves the file pointer dst.length * 2 positions forward. */
	public void readLEShorts(short[] dst) throws IOException;
	/** Reads dst.length uint16 values in big-endian order, and moves the file pointer dst.length * 2 positions forward. */
	public void readUShorts(int[] dst) throws IOException;
	/** Reads dst.length uint16 values in little-endian order, and moves the file pointer dst.length * 2 positions forward. */
	public void readLEUShorts(int[] dst) throws IOException;
	
	/** Reads an int32 value in big-endian order, and moves the file pointer 4 positions forward. */
	public int readInt() throws IOException;
	/** Reads an int32 value in little-endian order, and moves the file pointer 4 positions forward. */
	public int readLEInt() throws IOException;
	/** Reads an uint32 value in big-endian order, and moves the file pointer 4 positions forward. */
	public long readUInt() throws IOException;
	/** Reads an uint32 value in little-endian order, and moves the file pointer 4 positions forward. */
	public long readLEUInt() throws IOException;
	/** Reads dst.length int32 values in big-endian order, and moves the file pointer dst.length * 4 positions forward. */
	public void readInts(int[] dst) throws IOException;
	/** Reads dst.length int32 values in little-endian order, and moves the file pointer dst.length * 4 positions forward. */
	public void readLEInts(int[] dst) throws IOException;
	/** Reads dst.length uint32 values in big-endian order, and moves the file pointer dst.length * 4 positions forward. */
	public void readUInts(long[] dst) throws IOException;
	/** Reads dst.length uint32 values in little-endian order, and moves the file pointer dst.length * 4 positions forward. */
	public void readLEUInts(long[] dst) throws IOException;
	
	/** Reads an int64 value in big-endian order, and moves the file pointer 8 positions forward. */
	public long readLong() throws IOException;
	/** Reads an int64 value in little-endian order, and moves the file pointer 8 positions forward. */
	public long readLELong() throws IOException;
	/** Reads dst.length int64 values in big-endian order, and moves the file pointer dst.length * 8 positions forward. */
	public void readLongs(long[] dst) throws IOException;
	/** Reads dst.length int64 values in little-endian order, and moves the file pointer dst.length * 8 positions forward. */
	public void readLELongs(long[] dst) throws IOException;
	
	/** Reads a 32-bit floating point value in big-endian order, and moves the file pointer 4 positions forward. */
	public float readFloat() throws IOException;
	/** Reads a 32-bit floating point value in little-endian order, and moves the file pointer 4 positions forward. */
	public float readLEFloat() throws IOException;
	/** Reads dst.length 32-bit floating point values in big-endian order, and moves the file pointer dst.length * 4 positions forward. */
	public void readFloats(float[] dst) throws IOException;
	/** Reads dst.length 32-bit floating point values in little-endian order, and moves the file pointer dst.length * 4 positions forward. */
	public void readLEFloats(float[] dst) throws IOException;
	
	/** Reads a 64-bit floating point value (double precision) in big-endian order, and moves the file pointer 8 positions forward. */
	public double readDouble() throws IOException;
	/** Reads a 64-bit floating point value (double precision) in little-endian order, and moves the file pointer 8 positions forward. */
	public double readLEDouble() throws IOException;
	/** Reads dst.length 64-bit floating point values (double precision) in big-endian order, and moves the file pointer dst.length * 8 positions forward. */
	public void readDoubles(double[] dst) throws IOException;
	/** Reads dst.length 64-bit floating point values (double precision) in little-endian order, and moves the file pointer dst.length * 8 positions forward. */
	public void readLEDoubles(double[] dst) throws IOException;
}
