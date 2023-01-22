package sporemodder.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

public class ByteArrayStreamAccessor implements ReadWriteStreamAccessor {
	private int filePointer;
	private byte[] data;
	private int baseOffset;
	
	public ByteArrayStreamAccessor(int length) {
		data = new byte[length];
	}
	public ByteArrayStreamAccessor(String path) throws IOException {
		Path path2 = Paths.get(path);
		data = Files.readAllBytes(path2);
	}
	public ByteArrayStreamAccessor(File file) throws IOException {
		data = Files.readAllBytes(file.toPath());
	}
	public ByteArrayStreamAccessor(byte[] arr) {
		data = arr;
	}
	
	/**
	 * Deletes all data of the stream, including the file pointer.
	 */
	public void close() throws IOException {
		filePointer = 0; 
		data = null;
	}
	
	/**
	 * Writes the current byte stream to the given file. If the file doesn't exist, it will create it.
	 * @param path The file path where the byte stream will be written.
	 * @throws IOException 
	 */
	public void writeToFile(String path) throws IOException {
//		RandomAccessFile RAM = new RandomAccessFile(path, "rw");
//		FileChannel in = RAM.getChannel();
//		try {
//			ByteBuffer buf = in.map(FileChannel.MapMode.READ_WRITE, 0, data.length);
//			buf.put(data);
//		} catch(IOException e) {
//			e.printStackTrace();
//		} finally {
//			in.close();
//			RAM.close();
//		}
		File file = new File(path);
		file.delete();
		file.createNewFile();
		Path path2 = Paths.get(path);
		Files.write(path2, data, StandardOpenOption.WRITE);
	}
	/**
	 * Writes the current byte stream to the given file. If the file doesn't exist, it will create it.
	 * @param file The File where the byte stream will be written.
	 * @throws IOException 
	 */
	public void writeToFile(File file) throws IOException {
//		FileOutputStream RAM = new FileOutputStream(file);
//		FileChannel in = RAM.getChannel();
//		try {
//			int size = data.size();
//			ByteBuffer buf = in.map(FileChannel.MapMode.READ_WRITE, 0, size);
//			for (int i = 0; i < size; i++) {
//				buf.put(data.get(i));
//			}
//		} catch(IOException e) {
//			e.printStackTrace();
//		} finally {
//			in.close();
//			RAM.close();
//		}
		
		Path path2 = Paths.get(file.getAbsolutePath());
		Files.write(path2, data, StandardOpenOption.WRITE);
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
//		FileOutputStream RAM = new FileOutputStream(path);
//		FileChannel in = RAM.getChannel();
//		try {
//			ByteBuffer buf = in.map(FileChannel.MapMode.READ_WRITE, 0,len);
//			for (int i = 0; i < len; i++) {
//				buf.put(data.get(off+i));
//			}
//		} catch(IOException e) {
//			e.printStackTrace();
//		} finally {
//			in.close();
//			RAM.close();
//		}
		
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
//		FileOutputStream RAM = new FileOutputStream(file);
//		FileChannel in = RAM.getChannel();
//		try {
//			ByteBuffer buf = in.map(FileChannel.MapMode.READ_WRITE, 0,len);
//			for (int i = 0; i < len; i++) {
//				buf.put(data.get(off+i));
//			}
//		} catch(IOException e) {
//			e.printStackTrace();
//		} finally {
//			in.close();
//			RAM.close();
//		}
		
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
	
//	/**
//	 * Erases num bytes from the stream starting at off.
//	 * @param off The first byte to delete.
//	 * @param num The number of bytes to delete, starting at num.
//	 */
//	public byte[] getData() {
//		byte[] dataArr = new byte[data.size()];
//		for (int i = 0; i < data.size(); i++) {
//			dataArr[i] = data.get(i);
//		}
//		return dataArr;
//	}
	
	/**
	 * Erases num bytes from the stream starting at off.
	 * @param off The first byte to delete.
	 * @param num The number of bytes to delete, starting at num.
	 */
	public void deleteBytes(int off, int num) {
//		for (int i = 0; i < num; i++) {
//			data.remove(off+i);
//		}
		
		byte[] newData = new byte[data.length - num];
		System.arraycopy(data, 0, newData, 0, off-1);
		
		System.arraycopy(data, off+num, newData, off, data.length - off - num);
	}
	
	/**
	 * Gives you the current file pointer, so, the position where the next action happens.
	 * @return The current file pointer.
	 */
	public int length() {
		return data.length;
	}
	
	/**
	 * Gives you the current file pointer, so, the position where the next action happens.
	 * @return The current file pointer.
	 */
	public int getFilePointer() {
		return filePointer-baseOffset;
	}
	
	/**
	 * Changes the file pointer, so, the position where the next action happens.
	 * @param position The new file pointer position.
	 */
	public void seek(int positon) {
		filePointer = positon+baseOffset;
	}
	public void seekAbs(int pos) {
		filePointer = pos;
	}
	
	/**
	 * Skips an specified number of bytes, changing the file pointer
	 * @param num The number of bytes to skip.
	 */
	public void skipBytes(int num) {
		filePointer += num;
	}
	
	
	
	@Override
	public void setLength(int len) throws IOException {
		int fix;
		//TODO
	}
//	@Override
//	public void expect(int arg0, int arg1, String text) throws IOException {
//		if (arg0 != arg1) {
//			System.out.println(text);
//		}
//	}
//	@Override
//	public void expect(byte[] arg0, byte[] arg1, String text) throws IOException {
//		if (arg0.length == arg1.length) {
//			for (int i = 0; i < arg0.length; i++) {
//				//System.out.println("" + valueToExpect[i] + "\t" + expectedValue[i]);
//				if (arg0[i] != arg1[i]) {
//					System.out.println(text);
//				}
//			}
//		} else {
//			throw new IOException("The expected value and the value to expect don't have the same length");
//		}
//	}
//	@Override
//	public void expect(int[] arg0, int[] arg1, String text) throws IOException {
//		if (arg0.length == arg1.length) {
//			for (int i = 0; i < arg0.length; i++) {
//				//System.out.println("" + valueToExpect[i] + "\t" + expectedValue[i]);
//				if (arg0[i] != arg1[i]) {
//					System.out.println(text);
//				}
//			}
//		} else {
//			throw new IOException("The expected value and the value to expect don't have the same length");
//		}
//	}
	
	@Override
	public void read(byte[] arr) throws IOException {
		
		//byte[] arr2 = (byte[]) ((Byte[]) data.toArray());
		
//		for (int i = 0; i < arr.length; i++) {
//			arr[i] = data.get(filePointer);
//			filePointer++;
//		}
		
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
//		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, filePointer, filePointer+2));
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
		long l = ((data[filePointer] & 0xFF) << 56l) | ((data[filePointer+1] & 0xFF) << 48l) | ((data[filePointer+2] & 0xFF) << 40l) | ((data[filePointer+3] & 0xFF) << 32l) | 
				 ((data[filePointer+4] & 0xFF) << 24l) | ((data[filePointer+5] & 0xFF) << 16l) | ((data[filePointer+6] & 0xFF) << 8l) | (data[filePointer+7] & 0xFF);
		filePointer += 8;
		return l;
	}
	@Override
	public long readLELong() {
//		for (int i = 0; i < 8; i++) {
//			System.out.println(Integer.toHexString(data[filePointer+i] & 0xFF));
//		}
//		System.out.println(Long.toHexString((data[filePointer+7] & 0xFF) << 56));
		long l = ((data[filePointer+7] & 0xFFl) << 56) | ((data[filePointer+6] & 0xFFl) << 48) | ((data[filePointer+5] & 0xFFl) << 40) | ((data[filePointer+4] & 0xFFl) << 32) | 
				 ((data[filePointer+3] & 0xFFl) << 24) | ((data[filePointer+2] & 0xFFl) << 16) | ((data[filePointer+1] & 0xFFl) << 8) | (data[filePointer] & 0xFFl);
		filePointer += 8;
		return l;
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
//		for (int i = 0; i < arr.length; i++) {
//			if (filePointer >= data.size()) {
//				data.add(arr[i]);
//			} else {
//				data.set(filePointer, arr[i]);
//			}
//			filePointer++;
//		}
	}
	@Override
	public void write(byte[] arr, int off, int len) throws IOException {
		System.arraycopy(arr, off, data, filePointer, len);
		filePointer += len;
//		for (int i = 0; i < arr.length; i++) {
//			if (filePointer >= data.size()) {
//				data.add(arr[i]);
//			} else {
//				data.set(filePointer, arr[i]);
//			}
//			filePointer++;
//		}
	}
	@Override
	public void write(String text) throws IOException {
		byte[] b = text.getBytes("US-ASCII");
		System.arraycopy(b, 0, data, filePointer, b.length);
		filePointer += b.length;
//		for (int i = 0; i < b.length; i++) {
//			if (filePointer >= data.size()) {
//				data.add(b[i]);
//			} else {
//				data.set(filePointer, b[i]);
//			}
//			filePointer++;
//		}

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
//		byte[] b = new byte[2];
//		b[1] = (byte) (val & 0xFF);
//		b[0] = (byte) ((val >> 8) & 0xFF);
//		for (int i = 0; i < 2; i++) {
//			if (filePointer >= data.size()) {
//				data.add(b[i]);
//			} else {
//				data.set(filePointer, b[i]);
//			}
//			filePointer++;
//		}
		
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
	public void setBaseOffset(int val) throws IOException {
		baseOffset = val;
	}
	@Override
	public int getBaseOffset() throws IOException {
		return baseOffset;
	}
	@Override
	public int getFilePointerAbs() throws IOException {
		return filePointer;
	}
	@Override
	public byte[] toByteArray() throws IOException {
		return data;
	}
	@Override
	public String readString(String encoding, int length, int characterSize) throws IOException {
		byte[] array = new byte[length * characterSize];
		System.arraycopy(data, filePointer, array, 0, array.length);
		return new String(array, encoding);
	}
	
//	public static void main(String[] args) throws IOException {
//		byte[] arr = new byte[25000];
//		for (int i = 0; i < 25000; i++) {
//			arr[i] = 2;
//		}
//		
//		long time1 = System.nanoTime();
//		ByteStreamAccessor in = new ByteStreamAccessor(arr);
//		System.out.println("-- " + (System.nanoTime() - time1)/1000f);
//		in.close();
//	}
	
	
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
		int firstIndex = filePointer + baseOffset;
		int lastIndex = firstIndex;
		
		while(true) {
			if (data[lastIndex++] == 0) {
				break;
			}
		}
		filePointer = firstIndex - baseOffset;
		byte[] arr = new byte[lastIndex - firstIndex - 1];
		read(arr);
		filePointer = lastIndex - baseOffset;
		
		return new String(arr, "US-ASCII");
	}
	@Override
	public void writeCString(String text) throws IOException {
		writeString8(text);
		writeByte(0);
	}
}
