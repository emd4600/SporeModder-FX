package sporemodder.file.filestructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileStream implements ReadWriteStream {

	private RandomAccessFile ram;
	private long baseOffset;
	
	public FileStream(String name, String mode) throws IOException {
		this(new File(name), mode, false);
	}
	public FileStream(File file, String mode) throws FileNotFoundException {
		this(file, mode, false);
	}
	
	public FileStream(String name, String mode, boolean append) throws IOException {
		this(new File(name), mode, append);
	}
	
	public FileStream(File file, String mode, boolean append) throws FileNotFoundException {
		if (mode.contains("w") && !append && file.exists()) {
			file.delete();
		}
		
		ram = new RandomAccessFile(file, mode);
	}

	public static void writeToFile(String path, byte[] data) throws IOException {
		new File(path).createNewFile();
		Path path2 = Paths.get(path);
		Files.write(path2, data, StandardOpenOption.WRITE);
		
	}
	
	@Override
	public void writePadding(int pad) throws IOException {
		byte[] arr = new byte[pad];
		ram.write(arr);
	}
	
	
	@Override
	public void seek(long off) throws IOException {
		ram.seek(off + baseOffset);
	}
	
	@Override
	public void seekAbs(long off) throws IOException {
		ram.seek(off);
	}
	
	@Override
	public void close() throws IOException {
		if (ram != null) ram.close();
		ram = null;
	}
	
	@Override
	public void skip(int len) throws IOException {
		ram.seek(ram.getFilePointer() + len);
	}
	
	@Override
	public long getFilePointer() throws IOException {
		return ram.getFilePointer() - baseOffset;
	}
	
	@Override
	public long length() throws IOException {
		return ram.length();
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
		return Double.longBitsToDouble(this.readLELong());
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
	public void setLength(long len) throws IOException {
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
	public void writeBoolean(boolean val) throws IOException {
		ram.writeBoolean(val);
	}
	
	
	@Override
	public void setBaseOffset(long val) throws IOException {
		baseOffset = val;
	}
	@Override
	public long getBaseOffset() throws IOException {
		return baseOffset;
	}
	@Override
	public long getFilePointerAbs() throws IOException {
		return ram.getFilePointer();
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
	

	public FileChannel getChannel() {
		return ram.getChannel();
	}
	
	@Override
	public String readCString(StringEncoding encoding) throws IOException {
		long firstIndex = ram.getFilePointer();
		long lastIndex = firstIndex;
		
		while(true) {
			if (ram.read() == 0) {
				if (encoding == StringEncoding.ASCII || ram.read() == 0) {
					lastIndex++;
					if (encoding != StringEncoding.ASCII) lastIndex++;
					break;
				}
			}
			lastIndex++;
			if (encoding != StringEncoding.ASCII) lastIndex++;
		}
		byte[] arr = new byte[(int) (lastIndex - firstIndex - (encoding == StringEncoding.ASCII ? 1 : 2))];
		ram.seek(firstIndex);
		ram.read(arr);
		ram.seek(lastIndex);
		
		return new String(arr, encoding.getCharset());
	}
	
	@Override
	public String readString(StringEncoding encoding, int length) throws IOException {
		byte[] arr = new byte[encoding == StringEncoding.ASCII ? length : length*2];
		ram.read(arr);
		return new String(arr, encoding.getCharset());
	}
	
	@Override
	public void writeCString(String text, StringEncoding encoding) throws IOException {
		if (text != null) ram.write(text.getBytes(encoding.getCharset()));
		ram.writeByte(0);
	}
	
	@Override
	public void writeString(String text, StringEncoding encoding) throws IOException {
		if (text != null) {
			ram.write(text.getBytes(encoding.getCharset()));
		}
	}
	
	@Override
	public void writeString(String text, StringEncoding encoding, int length) throws IOException {
		if (text != null) {
			ram.write(text.getBytes(encoding.getCharset()), 0, length);
		}
		else {
			byte[] array = new byte[encoding == StringEncoding.ASCII ? length : (length*2)];
			ram.write(array);
		}
	}
}
