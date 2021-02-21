package sporemodder.file.filestructures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FixedMemoryStream implements ReadWriteStream {
	protected int filePointer;
	protected byte[] data;
	protected int baseOffset;
	
	public FixedMemoryStream(int length) {
		data = new byte[length];
	}
	public FixedMemoryStream(String path) throws IOException {
		Path path2 = Paths.get(path);
		data = Files.readAllBytes(path2);
	}
	public FixedMemoryStream(File file) throws IOException {
		data = Files.readAllBytes(file.toPath());
	}
	public FixedMemoryStream(byte[] arr) {
		data = arr;
	}
	
	public FixedMemoryStream(InputStream in) throws IOException {
		data = new byte[0];
		
		byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
		int n;
		
		while ((n = in.read(byteChunk)) > 0 ) {
			
			byte[] new_data = new byte[data.length + n];
			System.arraycopy(data, 0, new_data, 0, data.length);
			System.arraycopy(byteChunk, 0, new_data, data.length, n);
			
			data = new_data;
		}
	}
	
	/**
	 * Deletes all data of the stream, including the file pointer.
	 */
	public void close() {
		filePointer = 0; 
		data = null;
	}
	
	/**
	 * Writes the current byte stream to the given file. If the file doesn't exist, it will create it.
	 * @param path The file path where the byte stream will be written.
	 * @throws IOException 
	 */
	public void writeToFile(String path) throws IOException {
		Files.write(new File(path).toPath(), data);
	}
	
	/**
	 * Writes the current byte stream to the given file. If the file doesn't exist, it will create it.
	 * @param file The File where the byte stream will be written.
	 * @throws IOException 
	 */
	public void writeToFile(File file) throws IOException {
		Files.write(file.toPath(), data);
	}
	
	/**
	 * Writes the selected portion of the current byte stream to the given file. If the file doesn't exist, it will create it.
	 * Will only write len bytes being off the first one.
	 * @param path The file path where the byte stream will be written.
	 * @param off The offset to the first byte to be written.
	 * @param len The number of bytes to be written, starting at off.
	 * @throws IOException 
	 */
	public void writeToFile(String path, int off, int len) throws IOException {
		FileOutputStream out = new FileOutputStream(path);
		try {
			out.write(data, off, len);
		} finally {
			out.close();
		}
	}
	
	/**
	 * Writes the selected portion of the current byte stream to the given file. If the file doesn't exist, it will create it.
	 * Will only write len bytes being off the first one.
	 * @param file The File where the byte stream will be written.
	 * @param off The offset to the first byte to be written.
	 * @param len The number of bytes to be written, starting at off.
	 * @throws IOException 
	 */
	public void writeToFile(File file, int off, int len) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			out.write(data, off, len);
		} finally {
			out.close();
		}
	}
	
	public void writePadding(int pad) throws IOException {
		Arrays.fill(data, filePointer, filePointer+pad, (byte) 0);
		filePointer += pad;
	}
	
	/**
	 * Erases num bytes from the stream starting at off.
	 * @param off The first byte to delete.
	 * @param num The number of bytes to delete, starting at num.
	 */
	public void deleteBytes(int off, int num) {
		byte[] newData = new byte[data.length - num];
		System.arraycopy(data, 0, newData, 0, off-1);
		
		System.arraycopy(data, off+num, newData, off, data.length - off - num);
	}
	
	@Override
	public long length() {
		return data.length;
	}
	
	@Override
	public long getFilePointer() {
		return filePointer-baseOffset;
	}
	
	@Override
	public void seek(long positon) {
		filePointer = (int)positon + baseOffset;
	}
	@Override
	public void seekAbs(long pos) {
		filePointer = (int)pos;
	}
	
	@Override
	public void skip(int num) {
		filePointer += num;
	}
	
	@Override
	public void setLength(long len) throws IOException {
		byte[] new_data = new byte[(int)len];
		System.arraycopy(data, 0, new_data, 0, Math.min(data.length, new_data.length));
	}
	
	@Override
	public void read(byte[] arr) throws IOException {
		
		System.arraycopy(data, filePointer, arr, 0, arr.length);
		filePointer += arr.length;
	}
	
	@Override
	@Deprecated
	public String readLine() throws IOException {
		int i = 0;
		int sLen = 0;
		while (data[filePointer+i] != 0x0D && i < data.length) {
			sLen++;
			filePointer++;
			i++;
		}
		byte[] ba = new byte[sLen];
		for (int s = 1; s < sLen; s++) {
			ba[s] = data[filePointer-s];
		}
		filePointer++;
		return new String(ba);
	}
	
	
	@Override
	public byte readByte() {
		filePointer += 1;
		return data[filePointer-1];
	}
	@Override
	public short readUByte() throws IOException {
		filePointer += 1;
		return (short) (data[filePointer-1] & 0xFF);
	}
	
	@Override
	public char readChar() throws IOException {
		int sh = ((data[filePointer] & 0xFF) << 8) | (data[filePointer+1] & 0xFF);
		filePointer += 2;
		return (char) (sh & 0xFFFF);
	}
	
	@Override
	public short readShort() {
		int sh = ((data[filePointer] & 0xFF) << 8) | (data[filePointer+1] & 0xFF);
		filePointer += 2;
		return (short) sh;
	}
	@Override
	public int readUShort() throws IOException {
		int sh = ((data[filePointer] & 0xFF) << 8) | (data[filePointer+1] & 0xFF);
		filePointer += 2;
		return sh & 0xFFFF;
	}
	@Override
	public short readLEShort() {
		int sh = ((data[filePointer+1] & 0xFF) << 8) | (data[filePointer] & 0xFF);
		filePointer += 2;
		return (short) sh;
	}
	@Override
	public int readLEUShort() throws IOException {
		int sh = ((data[filePointer+1] & 0xFF) << 8) | (data[filePointer] & 0xFF);
		filePointer += 2;
		return sh;
	}
	
	@Override
	public int readInt() {
		int i = ((data[filePointer] & 0xFF) << 24) | ((data[filePointer+1] & 0xFF) << 16) | ((data[filePointer+2] & 0xFF) << 8) | (data[filePointer+3] & 0xFF);
		filePointer += 4;
		return i;
	}
	@Override
	public long readUInt() throws IOException {
		int i = ((data[filePointer] & 0xFF) << 24) | ((data[filePointer+1] & 0xFF) << 16) | ((data[filePointer+2] & 0xFF) << 8) | (data[filePointer+3] & 0xFF);
		filePointer += 4;
		return i & 0xFFFFFFFFL;
	}
	@Override
	public int readLEInt() {
		int i = ((data[filePointer+3] & 0xFF) << 24) | ((data[filePointer+2] & 0xFF) << 16) | ((data[filePointer+1] & 0xFF) << 8) | (data[filePointer] & 0xFF);
		filePointer += 4;
		return i;
	}
	@Override
	public long readLEUInt() throws IOException {
		int i = ((data[filePointer+3] & 0xFF) << 24) | ((data[filePointer+2] & 0xFF) << 16) | ((data[filePointer+1] & 0xFF) << 8) | (data[filePointer] & 0xFF);
		filePointer += 4;
		return i & 0xFFFFFFFFL;
	}
	
	@Override
	public long readLong() {
//		long l = ((data[filePointer] & 0xFF) << 56l) | ((data[filePointer+1] & 0xFF) << 48l) | ((data[filePointer+2] & 0xFF) << 40l) | ((data[filePointer+3] & 0xFF) << 32l) | 
//				 ((data[filePointer+4] & 0xFF) << 24l) | ((data[filePointer+5] & 0xFF) << 16l) | ((data[filePointer+6] & 0xFF) << 8l) | (data[filePointer+7] & 0xFF);
		
		long result = 0;
	    for (int i = 0; i < 8; i++) {
	        result <<= 8;
	        result |= (data[filePointer + i] & 0xFF);
	    }
		
		filePointer += 8;
		return result;
	}
	@Override
	public long readLELong() {
//		long l = ((data[filePointer+7] & 0xFFl) << 56) | ((data[filePointer+6] & 0xFFl) << 48) | ((data[filePointer+5] & 0xFFl) << 40) | ((data[filePointer+4] & 0xFFl) << 32) | 
//				 ((data[filePointer+3] & 0xFFl) << 24) | ((data[filePointer+2] & 0xFFl) << 16) | ((data[filePointer+1] & 0xFFl) << 8) | (data[filePointer] & 0xFFl);
		
		long result = 0;
	    for (int i = 8; i > 0; i--) {
	        result <<= 8;
	        result |= (data[filePointer + i] & 0xFF);
	    }
		
		filePointer += 8;
		return result;
	}
	
	@Override
	public float readFloat() {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, filePointer, filePointer+4));
		filePointer += 4;
		return bb.getFloat();
	}
	@Override
	public float readLEFloat() {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, filePointer, filePointer+4)).order(ByteOrder.LITTLE_ENDIAN);
		filePointer += 4;
		return bb.getFloat();
	}
	
	@Override
	public double readDouble() {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, filePointer, filePointer+8));
		filePointer += 8;
		return bb.getDouble();
	}
	@Override
	public double readLEDouble() {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, filePointer, filePointer+8)).order(ByteOrder.LITTLE_ENDIAN);
		filePointer += 8;
		return bb.getLong();
	}
	
	@Override
	public boolean readBoolean() throws IOException {
		if (data[filePointer] == 0) {
			filePointer++;
			return false;
		} else if (data[filePointer] == 1) {
			filePointer++;
			return true;
		} else {
			throw new IOException("Boolean byte at " + filePointer + " is " + data[filePointer] +". Must be 0 or 1.");
		}
	}
	
	
	@Override
	public void write(byte[] arr) throws IOException {
		System.arraycopy(arr, 0, data, filePointer, arr.length);
		filePointer += arr.length;
	}
	@Override
	public void write(byte[] arr, int off, int len) throws IOException {
		System.arraycopy(arr, off, data, filePointer, len);
		filePointer += len;
	}

	@Override
	public void writeByte(int val) throws IOException {
		data[filePointer] = (byte) val;
		filePointer++;
	}
	@Override
	public void writeUByte(int val) throws IOException {
		data[filePointer] = (byte) (val & 0xFF);
		filePointer++;
	}
	
	@Override
	public void writeShort(int val) throws IOException {
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) (val & 0xFF);
	}
	@Override
	public void writeLEShort(int val) throws IOException {
		data[filePointer++] = (byte) (val & 0xFF);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
	}
	@Override
	public void writeUShort(int val) throws IOException {
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) (val & 0xFF);
	}
	@Override
	public void writeLEUShort(int val) throws IOException {
		data[filePointer++] = (byte) (val & 0xFF);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
	}
	
	@Override
	public void writeInt(int val) throws IOException {
		data[filePointer++] = (byte) ((val & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((val & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) (val & 0xFF);
	}
	@Override
	public void writeLEInt(int val) throws IOException {
		data[filePointer++] = (byte) (val & 0xFF);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) ((val & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((val & 0xFF000000) >> 24);
	}
	@Override
	public void writeUInt(long val) throws IOException {
		data[filePointer++] = (byte) ((val & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((val & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) (val & 0xFF);
	}
	@Override
	public void writeLEUInt(long val) throws IOException {
		data[filePointer++] = (byte) (val & 0xFF);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) ((val & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((val & 0xFF000000) >> 24);
	}
	@Override
	public void writeLong(long val) throws IOException {
		data[filePointer++] = (byte) ((val & 0xFF00000000000000l) >> 56);
		data[filePointer++] = (byte) ((val & 0xFF000000000000l) >> 48);
		data[filePointer++] = (byte) ((val & 0xFF0000000000l) >> 40);
		data[filePointer++] = (byte) ((val & 0xFF00000000l) >> 32);
		data[filePointer++] = (byte) ((val & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((val & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) (val & 0xFF);
	}
	@Override
	public void writeLELong(long val) throws IOException {
		data[filePointer++] = (byte) (val & 0xFF);
		data[filePointer++] = (byte) ((val & 0xFF00) >> 8);
		data[filePointer++] = (byte) ((val & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((val & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((val & 0xFF00000000l) >> 32);
		data[filePointer++] = (byte) ((val & 0xFF0000000000l) >> 40);
		data[filePointer++] = (byte) ((val & 0xFF000000000000l) >> 48);
		data[filePointer++] = (byte) ((val & 0xFF00000000000000l) >> 56);
	}
	@Override
	public void writeFloat(float val) throws IOException {
		int nVal = Float.floatToRawIntBits(val);
		data[filePointer++] = (byte) ((nVal & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((nVal & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((nVal & 0xFF00) >> 8);
		data[filePointer++] = (byte) (nVal & 0xFF);
	}
	@Override
	public void writeLEFloat(float val) throws IOException {
		int nVal = Float.floatToRawIntBits(val);
		data[filePointer++] = (byte) (nVal & 0xFF);
		data[filePointer++] = (byte) ((nVal & 0xFF00) >> 8);
		data[filePointer++] = (byte) ((nVal & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((nVal & 0xFF000000) >> 24);
	}
	@Override
	public void writeDouble(double val) throws IOException {
		long nVal = Double.doubleToRawLongBits(val);
		data[filePointer++] = (byte) ((nVal & 0xFF00000000000000l) >> 56);
		data[filePointer++] = (byte) ((nVal & 0xFF000000000000l) >> 48);
		data[filePointer++] = (byte) ((nVal & 0xFF0000000000l) >> 40);
		data[filePointer++] = (byte) ((nVal & 0xFF00000000l) >> 32);
		data[filePointer++] = (byte) ((nVal & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((nVal & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((nVal & 0xFF00) >> 8);
		data[filePointer++] = (byte) (nVal & 0xFF);
	}
	@Override
	public void writeLEDouble(double val) throws IOException {
		long nVal = Double.doubleToRawLongBits(val);
		data[filePointer++] = (byte) (nVal & 0xFF);
		data[filePointer++] = (byte) ((nVal & 0xFF00) >> 8);
		data[filePointer++] = (byte) ((nVal & 0xFF0000) >> 16);
		data[filePointer++] = (byte) ((nVal & 0xFF000000) >> 24);
		data[filePointer++] = (byte) ((nVal & 0xFF00000000l) >> 32);
		data[filePointer++] = (byte) ((nVal & 0xFF0000000000l) >> 40);
		data[filePointer++] = (byte) ((nVal & 0xFF000000000000l) >> 48);
		data[filePointer++] = (byte) ((nVal & 0xFF00000000000000l) >> 56);
	}
	@Override
	public void writeBoolean(boolean val) throws IOException {
		data[filePointer++] = val ? (byte) 1 : (byte) 0;
	}
	
	@Override
	public void setBaseOffset(long val) throws IOException {
		baseOffset = (int)val;
	}
	@Override
	public long getBaseOffset() throws IOException {
		return baseOffset;
	}
	@Override
	public long getFilePointerAbs() throws IOException {
		return filePointer;
	}
	@Override
	public byte[] toByteArray() throws IOException {
		return data;
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
	
	
	private static int getCStringLength(byte[] array, int start, int characterSize) {
		int endPosition = array.length;
		
		for (int i = start; i < array.length - characterSize + 1; i += characterSize) {
			if (characterSize == 1) {
				if (array[i] == 0) {
					endPosition = i;
					break;
				}
			}
			else {
				if (array[i] == 0 && array[i+1] == 0) {
					endPosition = i;
					break;
				}
			}
		}
		
		return endPosition - start;
	}
	
	@Override
	public String readCString(StringEncoding encoding) throws IOException {
		int characterSize = encoding == StringEncoding.ASCII ? 1 : 2;
		
		byte[] array = new byte[getCStringLength(data, filePointer, characterSize)];
		System.arraycopy(data, filePointer, array, 0, array.length);
		
		filePointer += array.length + characterSize;
		
		if (encoding == StringEncoding.ASCII) {
			return new String(array, "US-ASCII");
		}
		else if (encoding == StringEncoding.UTF16LE) {
			return new String(array, "UTF-16LE");
		}
		else if (encoding == StringEncoding.UTF16BE) {
			return new String(array, "UTF-16BE");
		}
		else {
			// never happens
			return null;
		}
	}
	
	@Override
	public String readString(StringEncoding encoding, int length) throws IOException {
		int characterSize = encoding == StringEncoding.ASCII ? 1 : 2;
			
		byte[] array = new byte[length * characterSize];
		System.arraycopy(data, filePointer, array, 0, array.length);
		
		filePointer += array.length;
		
		// Discard 00 bytes
		int realLength = getCStringLength(array, 0, characterSize);
		
		if (realLength != array.length) {
			byte[] temp = new byte[realLength];
			System.arraycopy(array, 0, temp, 0, realLength);
			array = temp;
		}
		
		
		if (encoding == StringEncoding.ASCII) {
			return new String(array, "US-ASCII");
		}
		else if (encoding == StringEncoding.UTF16LE) {
			return new String(array, "UTF-16LE");
		}
		else if (encoding == StringEncoding.UTF16BE) {
			return new String(array, "UTF-16BE");
		}
		else {
			// never happens
			return null;
		}
	}
	
	@Override
	public void writeCString(String text, StringEncoding encoding) throws IOException {
		if (text != null) writeString(text, encoding);
		
		data[filePointer++] = 0;
		if (encoding != StringEncoding.ASCII) {
			data[filePointer++] = 0;
		}
	}
	
	@Override
	public void writeString(String text, StringEncoding encoding) throws IOException {
		if (text == null) return;
		
		byte[] array = null;
		
		if (encoding == StringEncoding.ASCII) {
			array = text.getBytes("US-ASCII");
		}
		else if (encoding == StringEncoding.UTF16LE) {
			array = text.getBytes("UTF-16LE");
		}
		else if (encoding == StringEncoding.UTF16BE) {
			array = text.getBytes("UTF-16BE");
		}
		
		System.arraycopy(array, 0, data, filePointer, array.length);
		filePointer += array.length;
	}
	
	@Override
	public void writeString(String text, StringEncoding encoding, int length) throws IOException {
		byte[] array = null;
		
		if (encoding == StringEncoding.ASCII) {
			array = text == null ? new byte[length] : text.getBytes("US-ASCII");
		}
		else if (encoding == StringEncoding.UTF16LE) {
			array = text == null ? new byte[length*2] : text.getBytes("UTF-16LE");
		}
		else if (encoding == StringEncoding.UTF16BE) {
			array = text == null ? new byte[length*2] : text.getBytes("UTF-16BE");
		}
		
		if (array.length != length) {
			byte[] temp = new byte[length];
			System.arraycopy(array, 0, temp, 0, Math.min(array.length, length));
			array = temp;
		}
		
		System.arraycopy(array, 0, data, filePointer, array.length);
		filePointer += array.length;
	}
}
