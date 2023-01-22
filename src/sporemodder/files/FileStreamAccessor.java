package sporemodder.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;


public class FileStreamAccessor implements ReadWriteStreamAccessor {
	private RandomAccessFile ram;
	private int baseOffset;
	
	public FileStreamAccessor(String name, String mode) throws IOException {
		ram = new RandomAccessFile(name, mode); 
	}
	public FileStreamAccessor(File file, String mode) throws FileNotFoundException {
		ram = new RandomAccessFile(file, mode); 
	}
	
	public FileStreamAccessor(String name, String mode, boolean overwrite) throws IOException {
		File file = new File(name);
		if (mode.contains("w") && overwrite && file.exists()) file.delete();
		ram = new RandomAccessFile(file, mode); 
	}
	public FileStreamAccessor(File file, String mode, boolean overwrite) throws FileNotFoundException {
		if (mode.contains("w") && overwrite && file.exists()) file.delete();
		ram = new RandomAccessFile(file, mode); 
	}

	public static void writeToFile(String path, byte[] data) throws IOException {
		new File(path).createNewFile();
		Path path2 = Paths.get(path);
		Files.write(path2, data, StandardOpenOption.WRITE);
		
	}
	/**
	  * It checks a value and throws an exception if it doesn't match the given controller value.
	  *
	  * @param valueToExpect The value to check.
	  * @param expectedValue The expected value.
	  * @param errorString The string that will be showed in the case the values doesn't match
	  */
	public void expect(int valueToExpect,  int expectedValue, String errorString) {
		if (valueToExpect != expectedValue) {
			System.out.println(errorString);
		}
	}
	/**
	  * It checks a value and throws an exception if it doesn't match the given controller value.
	  *
	  * @param valueToExpect The value to check.
	  * @param expectedValue The expected value.
	  * @param errorString The string that will be showed in the case the values doesn't match
	 * @throws IOException 
	  */
	public void expect(byte[] valueToExpect,  byte[] expectedValue, String errorString) throws IOException {
		if (valueToExpect.length == expectedValue.length) {
			for (int i = 0; i < valueToExpect.length; i++) {
				//System.out.println("" + valueToExpect[i] + "\t" + expectedValue[i]);
				if (valueToExpect[i] != expectedValue[i]) {
					System.out.println(errorString);
				}
			}
		} else {
			throw new IOException("The expected value and the value to expect don't have the same length");
		}
	}
	/**
	  * It checks a value and throws an exception if it doesn't match the given controller value.
	  *
	  * @param valueToExpect The value to check.
	  * @param expectedValue The expected value.
	  * @param errorString The string that will be showed in the case the values doesn't match
	 * @throws IOException 
	  */
	public void expect(int[] valueToExpect,  int[] expectedValue, String errorString) throws IOException {
		if (valueToExpect.length == expectedValue.length) {
			for (int i = 0; i < valueToExpect.length; i++) {
				//System.out.println("" + valueToExpect[i] + "\t" + expectedValue[i]);
				if (valueToExpect[i] != expectedValue[i]) {
					System.out.println(errorString);
				}
			}
		} else {
			throw new IOException("The expected value and the value to expect don't have the same length");
		}
	}
	/**
	  * It writes pad bytes with 0 value.
	  *
	  *@param pad The number of 0 bytes to be written.
	  */
	public void writePadding(int pad) throws IOException {
		byte[] arr = new byte[pad];
		ram.write(arr);
	}
	
	
	@Override
	public void seek(int off) throws IOException {
		ram.seek(off+baseOffset);
	}
	@Override
	public void seekAbs(int off) throws IOException {
		ram.seek(off);
	}
	@Override
	public void close() throws IOException {
		if (ram != null) ram.close();
		ram = null;
	}
	@Override
	public void skipBytes(int len) throws IOException {
		ram.seek(ram.getFilePointer()+len);
	}
	@Override
	public int getFilePointer() throws IOException {
		return (int)ram.getFilePointer()-baseOffset;
	}
	@Override
	public int length() throws IOException {
		return (int)ram.length();
	}
	
	
	@Override
	public byte readByte() throws IOException {
		return ram.readByte();
	}
	@Override
	public short readUByte() throws IOException {
		return (short) (ram.readByte() & 0xFF);
	}
	
	@Override
	public char readChar() throws IOException {
		return ram.readChar();
	}
	
	@Override
	public short readShort() throws IOException {
		return ram.readShort();
	}
	@Override
	public int readUShort() throws IOException {
		return ram.readShort() & 0xFFFF;
	}
	@Override
	public short readLEShort() throws IOException {
		short i = ram.readShort();
		return Short.reverseBytes(i);
//		System.out.println(Integer.toHexString(i));
//		return (short) (((i & 0xFF) << 8) | ((i & 0xFF00) >> 8));
	}
	@Override
	public int readLEUShort() throws IOException {
		int i = ram.readShort();
		return (((i & 0xFF) << 8) | ((i & 0xFF00) >> 8));
	}
	
	@Override
	public int readInt() throws IOException {
		return ram.readInt();
	}
	@Override
	public long readUInt() throws IOException {
		return ram.readInt() & 0xFFFFFFFFL;
	}
	@Override
	public int readLEInt() throws IOException {
		int i = ram.readInt();
		return (int) (((i & 0xFF) << 24) | ((i & 0xFF00) << 8) | ((i & 0xFF0000) >> 8) | ((i & 0xFF000000L) >> 24));
	}
	@Override
	public long readLEUInt() throws IOException {
		int i = ram.readInt();
		return (((i & 0xFF) << 24) | ((i & 0xFF00) << 8) | ((i & 0xFF0000) >> 8) | ((i & 0xFF000000L) >> 24)) & 0xFFFFFFFFL;
	}
	
	@Override
	public long readLong() throws IOException {
		return ram.readLong();
	}
	@Override
	public long readLELong() throws IOException {
		// TODO PLEASE!!!
		return Long.reverseBytes(ram.readLong());
	}
	
	@Override
	public float readFloat() throws IOException {
		return ram.readFloat();
	}
	@Override
	public float readLEFloat() throws IOException {
		return Float.intBitsToFloat(this.readLEInt());
	}
	
	@Override
	public double readDouble() throws IOException {
		return ram.readDouble();
	}
	@Override
	public double readLEDouble() throws IOException {
		return Double.longBitsToDouble(this.readLong());
	}
	
	
	@Override
	public void writeByte(int val) throws IOException {
		ram.writeByte(val);
	}
	@Override
	public void writeUByte(int val) throws IOException {
		ram.writeByte(val & 0xFF);
	}
	
	@Override
	public void writeShort(int val) throws IOException {
		ram.writeShort(val);
	}
	@Override
	public void writeUShort(int val) throws IOException {
		ram.writeShort(val & 0xFFFF);
	}
	@Override
	public void writeLEShort(int inShort) throws IOException {
		ram.writeShort(Short.reverseBytes((short)inShort));
	}
	@Override
	public void writeLEUShort(int val) throws IOException {
		ram.writeShort(Short.reverseBytes((short) (val & 0xFFFF)));
	}
	
	@Override
	public void writeInt(int val) throws IOException {
		ram.writeInt(val);
	}
	@Override
	public void writeLEInt(int val) throws IOException {
		ram.writeInt(Integer.reverseBytes(val));
	}
	@Override
	public void writeUInt(long val) throws IOException {
		ram.writeInt((int) (val & 0xFFFFFFFF));
	}
	@Override
	public void writeLEUInt(long val) throws IOException {
		ram.writeInt(Integer.reverseBytes((int) (val & 0xFFFFFFFF)));
	}
	
	@Override
	public void writeLong(long val) throws IOException {
		ram.writeLong(val);
	}
	@Override
	public void writeLELong(long val) throws IOException {
		ram.writeLong(Long.reverseBytes(val));
	}
	
	@Override
	public void writeFloat(float val) throws IOException {
		ram.writeFloat(val);
	}
	@Override
	public void writeLEFloat(float inFloat) throws IOException {
//		ram.writeFloat(Endiannes.byteArrayToFloat(Endiannes.floatToLittleEndian(inFloat)));
		ram.writeInt(Integer.reverseBytes(Float.floatToRawIntBits(inFloat)));
	}
	
	@Override
	public void writeDouble(double val) throws IOException {
		ram.writeDouble(val);
	}
	@Override
	public void writeLEDouble(double val) throws IOException {
		ram.writeLong(Long.reverseBytes(Double.doubleToRawLongBits(val)));
	}
	
	
	@Override
	public void setLength(int len) throws IOException {
		ram.setLength(len);
	}
	@Override
	public void read(byte[] arr) throws IOException {
		ram.read(arr);
	}
	@Override
	public String readLine() throws IOException {
		return ram.readLine();
	}
	@Override
	public boolean readBoolean() throws IOException {
		return ram.readBoolean();
	}
	@Override
	public void write(byte[] arr) throws IOException {
		ram.write(arr);
	}
	@Override
	public void write(byte[] arr, int off, int len) throws IOException {
		ram.write(arr, off, len);
	}
	@Override
	public void write(String text) throws IOException {
		ram.writeBytes(text);
	}
	@Override
	public void writeBoolean(boolean val) throws IOException {
		ram.writeBoolean(val);
	}
	
	
	@Override
	public void setBaseOffset(int val) throws IOException {
		baseOffset = val;
	}
	@Override
	public int getBaseOffset() throws IOException {
		return baseOffset;
	}
	@Override
	public int getFilePointerAbs() throws IOException {
		return (int) ram.getFilePointer();
	}
	@Override
	public byte[] toByteArray() throws IOException {
		long oldPointer = ram.getFilePointer();
		ram.seek(0);
		byte[] array = new byte[(int) ram.length()];
		ram.read(array);
		ram.seek(oldPointer);
		return array;
	}
	
	@Override
	public String readString(String encoding, int length, int characterSize) throws IOException {
		byte[] array = new byte[length * characterSize];
		ram.read(array);
		return new String(array, encoding);
	}
	
	@Override
	public void writeBytes(int... vals) throws IOException {
		for (int val : vals) {
			writeByte(val);
		}
	}
	@Override
	public void writeUBytes(int... vals) throws IOException {
		for (int val : vals) {
			writeUByte(val);
		}
	}
	@Override
	public void writeShorts(int... vals) throws IOException {
		for (int val : vals) {
			writeShort(val);
		}
	}
	@Override
	public void writeLEShorts(int... vals) throws IOException {
		for (int val : vals) {
			writeLEShort(val);
		}
	}
	@Override
	public void writeUShorts(int... vals) throws IOException {
		for (int val : vals) {
			writeUShort(val);
		}
	}
	@Override
	public void writeLEUShorts(int... vals) throws IOException {
		for (int val : vals) {
			writeLEUShort(val);
		}
	}
	@Override
	public void writeInts(int... vals) throws IOException {
		for (int val : vals) {
			writeInt(val);
		}
	}
	@Override
	public void writeLEInts(int... vals) throws IOException {
		for (int val : vals) {
			writeLEInt(val);
		}
	}
	@Override
	public void writeUInts(long... vals) throws IOException {
		for (long val : vals) {
			writeUInt(val);
		}
	}
	@Override
	public void writeLEUInts(long... vals) throws IOException {
		for (long val : vals) {
			writeLEUInt(val);
		}
	}
	@Override
	public void writeLongs(long... vals) throws IOException {
		for (long val : vals) {
			writeLong(val);
		}
	}
	@Override
	public void writeLELongs(long... vals) throws IOException {
		for (long val : vals) {
			writeLELong(val);
		}
	}
	@Override
	public void writeFloats(float... vals) throws IOException {
		for (float val : vals) {
			writeFloat(val);
		}
	}
	@Override
	public void writeLEFloats(float... vals) throws IOException {
		for (float val : vals) {
			writeLEFloat(val);
		}
	}
	@Override
	public void writeDoubles(double... vals) throws IOException {
		for (double val : vals) {
			writeDouble(val);
		}
	}
	@Override
	public void writeLEDoubles(double... vals) throws IOException {
		for (double val : vals) {
			writeLEDouble(val);
		}
	}
	
	@Override
	public void readBytes(byte[] dst) throws IOException {
		// We'd better use this optimized version
		read(dst);
	}
	@Override
	public void readUBytes(int[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readUByte();
		}
	}
	@Override
	public void readChars(char[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readChar();
		}
	}
	@Override
	public void readShorts(short[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readShort();
		}
	}
	@Override
	public void readLEShorts(short[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLEShort();
		}
	}
	@Override
	public void readUShorts(int[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readUShort();
		}
	}
	@Override
	public void readLEUShorts(int[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLEUShort();
		}
	}
	@Override
	public void readInts(int[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readInt();
		}
	}
	@Override
	public void readLEInts(int[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLEInt();
		}
	}
	@Override
	public void readUInts(long[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readUInt();
		}
	}
	@Override
	public void readLEUInts(long[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLEUInt();
		}
	}
	@Override
	public void readLongs(long[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLong();
		}
	}
	@Override
	public void readLELongs(long[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLELong();
		}
	}
	@Override
	public void readFloats(float[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readFloat();
		}
	}
	@Override
	public void readLEFloats(float[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLEFloat();
		}
	}
	@Override
	public void readDoubles(double[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readDouble();
		}
	}
	@Override
	public void readLEDoubles(double[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readLEDouble();
		}
	}
	
	@Override
	public String readString8(int length) throws IOException {
		byte[] arr = new byte[length];
		read(arr);
		return new String(arr, "US-ASCII");
	}
	@Override
	public String readString16(int length) throws IOException {
		byte[] arr = new byte[length * 2];
		read(arr);
		return new String(arr, "UTF-16BE");
	}
	@Override
	public String readLEString16(int length) throws IOException {
		byte[] arr = new byte[length * 2];
		read(arr);
		return new String(arr, "UTF-16LE");
	}
	
	@Override
	public void writeString8(String text) throws IOException {
		byte[] arr = text.getBytes("US-ASCII");
		write(arr);
	}
	@Override
	public void writeString16(String text) throws IOException {
		byte[] arr = text.getBytes("UTF-16BE");
		write(arr);
	}
	@Override
	public void writeLEString16(String text) throws IOException {
		byte[] arr = text.getBytes("UTF-16LE");
		write(arr);
	}
	
	@Override
	public void readBooleans(boolean[] dst) throws IOException {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = readBoolean();
		}
	}
	@Override
	public void writeBooleans(boolean ... vals) throws IOException {
		for (boolean b : vals) {
			writeBoolean(b);
		}
	}
	
	@Override
	public String readCString() throws IOException {
		int firstIndex = (int) ram.getFilePointer();
		int lastIndex = firstIndex;
		
		while(true) {
			if (ram.read() == 0) {
				lastIndex++;
				break;
			}
			lastIndex++;
		}
		byte[] arr = new byte[lastIndex - firstIndex - 1];
		ram.seek(firstIndex);
		ram.read(arr);
		ram.seek(lastIndex);
		
		return new String(arr, "US-ASCII");
	}
	
	@Override
	public void writeCString(String text) throws IOException {
		writeString8(text);
		writeByte(0);
	}
	
	public FileChannel getChannel() {
		return ram.getChannel();
	}
}
