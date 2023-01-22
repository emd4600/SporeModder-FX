package sporemodder.files;

import java.io.IOException;

public class MemoryOutputStream implements OutputStreamAccessor {
	
	private static final int INITIAL_SIZE = 8192; 
	
	private byte[] data;
	private int position;
	private int length;
	private float resizeFactor = 1.5f;
	
	public MemoryOutputStream() {
		data = new byte[INITIAL_SIZE];
		position = 0;
		length = 0;
	}
	
	public MemoryOutputStream(int nCapacity) {
		data = new byte[nCapacity];
		position = 0;
		length = 0;
	}
	
	public byte[] getRawData() {
		return data;
	}
	
	public void writeInto(OutputStreamAccessor out) throws IOException {
		out.write(data, 0, length);
	}
	
	public void reset(int nCapacity) {
		data = new byte[nCapacity];
		position = 0;
		length = 0;
	}
	
	private void reallocate(int size) {
		byte[] arr = new byte[size];
		
		if (data != null) {
			System.arraycopy(data, 0, arr, 0, data.length);
		}
		
		data = arr;
	}

	@Override
	public void seek(int off) {
		if (off > length) {
			throw new IllegalArgumentException("Cannot seek further than " + length + ".");
		}
		position = off;
	}

	@Override
	public void seekAbs(int off) throws IOException {
		throw new UnsupportedOperationException("seekAbs(int) not supported in MemoryOutputStream.");
	}

	@Override
	public void skipBytes(int len) throws IOException {
		seek(position + len);
	}

	@Override
	public void close() throws IOException {
		data = null;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public void setLength(int len) {
		if (len > data.length) {
			reallocate(len);
		}
		length = len;
	}

	@Override
	public int getFilePointer() {
		return position;
	}

	@Override
	public int getFilePointerAbs() throws IOException {
		throw new UnsupportedOperationException("getFilePointerAbs() not supported in MemoryOutputStream.");
	}

	@Override
	public void setBaseOffset(int val) throws IOException {
		throw new UnsupportedOperationException("setBaseOffset(int) not supported in MemoryOutputStream.");
	}

	@Override
	public int getBaseOffset() throws IOException {
		throw new UnsupportedOperationException("getBaseOffset() not supported in MemoryOutputStream.");
	}

	@Override
	public void writePadding(int pad) throws IOException {
		while (position + pad > data.length) {
			reallocate((int) (data.length * resizeFactor));
		}
		for (int i = 0; i < pad; i++) {
			data[position++] = 0;
		}
		
		if (position > length) {
			length = position;
		}
	}

	@Override
	public void write(byte[] arr) throws IOException {
		write(arr, 0, arr.length);
	}

	@Override
	public void write(byte[] arr, int off, int len) throws IOException {
		while (position + len > data.length) {
			reallocate((int) (data.length * resizeFactor));
		}
		
		System.arraycopy(arr, off, data, position, len);
		position += len;
		
		if (position > length) {
			length = position;
		}
	}

	@Override
	public void write(String text) throws IOException {
		write(text.getBytes("US-ASCII"));
	}

	@Override
	public void writeCString(String text) throws IOException {
		write(text.getBytes("US-ASCII"));
		write(new byte[] {0}, 0, 1);
	}

	@Override
	public void writeString8(String text) throws IOException {
		write(text.getBytes("US-ASCII"));
	}

	@Override
	public void writeString16(String text) throws IOException {
		write(text.getBytes("UTF-16BE"));
	}

	@Override
	public void writeLEString16(String text) throws IOException {
		write(text.getBytes("UTF-16LE"));
	}

	@Override
	public void writeBoolean(boolean val) throws IOException {
		write(new byte[] {(byte) (val ? 1 : 0)}, 0, 1);
	}

	@Override
	public void writeBooleans(boolean... vals) throws IOException {
		for (boolean value : vals) {
			writeBoolean(value);
		}
	}

	@Override
	public void writeByte(int val) throws IOException {
		write(new byte[] {(byte) val}, 0, 1);
	}

	@Override
	public void writeBytes(int... vals) throws IOException {
		for (int value : vals) {
			writeByte(value);
		}
	}

	@Override
	public void writeUByte(int val) throws IOException {
		write(new byte[] {(byte) val}, 0, 1);
	}

	@Override
	public void writeUBytes(int... vals) throws IOException {
		for (int value : vals) {
			writeUByte(value);
		}
	}

	@Override
	public void writeShort(int val) throws IOException {
		byte[] arr = new byte[2];
		
		arr[0] = (byte) ((val & 0xFF00) >> 8);
		arr[1] = (byte) (val & 0x00FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeShorts(int... vals) throws IOException {
		for (int value : vals) {
			writeShort(value);
		}
	}

	@Override
	public void writeLEShort(int val) throws IOException {
		byte[] arr = new byte[2];
		
		arr[1] = (byte) ((val & 0xFF00) >> 8);
		arr[0] = (byte) (val & 0x00FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLEShorts(int... vals) throws IOException {
		for (int value : vals) {
			writeLEShort(value);
		}
	}

	@Override
	public void writeUShort(int val) throws IOException {
		byte[] arr = new byte[2];
		
		arr[0] = (byte) ((val & 0xFF00) >> 8);
		arr[1] = (byte) (val & 0x00FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeUShorts(int... vals) throws IOException {
		for (int value : vals) {
			writeUShort(value);
		}
	}

	@Override
	public void writeLEUShort(int val) throws IOException {
		byte[] arr = new byte[2];
		
		arr[1] = (byte) ((val & 0xFF00) >> 8);
		arr[0] = (byte) (val & 0x00FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLEUShorts(int... vals) throws IOException {
		for (int value : vals) {
			writeLEUShort(value);
		}
	}

	@Override
	public void writeInt(int val) throws IOException {
		byte[] arr = new byte[4];
		
		arr[0] = (byte) ((val & 0xFF000000) >> 24);
		arr[1] = (byte) ((val & 0x00FF0000) >> 16);
		arr[2] = (byte) ((val & 0x0000FF00) >> 8);
		arr[3] = (byte) (val & 0x000000FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeInts(int... vals) throws IOException {
		for (int value : vals) {
			writeInt(value);
		}
	}

	@Override
	public void writeLEInt(int val) throws IOException {
		byte[] arr = new byte[4];
		
		arr[3] = (byte) ((val & 0xFF000000) >> 24);
		arr[2] = (byte) ((val & 0x00FF0000) >> 16);
		arr[1] = (byte) ((val & 0x0000FF00) >> 8);
		arr[0] = (byte) (val & 0x000000FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLEInts(int... vals) throws IOException {
		for (int value : vals) {
			writeLEInt(value);
		}
	}

	@Override
	public void writeUInt(long val) throws IOException {
		byte[] arr = new byte[4];
		
		arr[0] = (byte) ((val & 0xFF000000) >> 24);
		arr[1] = (byte) ((val & 0x00FF0000) >> 16);
		arr[2] = (byte) ((val & 0x0000FF00) >> 8);
		arr[3] = (byte) (val & 0x000000FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeUInts(long... vals) throws IOException {
		for (long value : vals) {
			writeUInt(value);
		}
	}

	@Override
	public void writeLEUInt(long val) throws IOException {
		byte[] arr = new byte[4];
		
		arr[3] = (byte) ((val & 0xFF000000) >> 24);
		arr[2] = (byte) ((val & 0x00FF0000) >> 16);
		arr[1] = (byte) ((val & 0x0000FF00) >> 8);
		arr[0] = (byte) (val & 0x000000FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLEUInts(long... vals) throws IOException {
		for (long value : vals) {
			writeLEUInts(value);
		}
	}

	@Override
	public void writeLong(long val) throws IOException {
		byte[] arr = new byte[8];
		
		arr[0] = (byte) ((val & 0xFF00000000000000L) >> 52);
		arr[1] = (byte) ((val & 0x00FF000000000000L) >> 48);
		arr[2] = (byte) ((val & 0x0000FF0000000000L) >> 40);
		arr[3] = (byte) ((val & 0x000000FF00000000L) >> 32);
		arr[4] = (byte) ((val & 0x00000000FF000000L) >> 24);
		arr[5] = (byte) ((val & 0x0000000000FF0000L) >> 16);
		arr[6] = (byte) ((val & 0x000000000000FF00L) >> 8);
		arr[7] = (byte) (val & 0x00000000000000FFL);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLongs(long... vals) throws IOException {
		for (long value : vals) {
			writeLongs(value);
		}
	}

	@Override
	public void writeLELong(long val) throws IOException {
		byte[] arr = new byte[8];
		
		arr[7] = (byte) ((val & 0xFF00000000000000L) >> 56);
		arr[6] = (byte) ((val & 0x00FF000000000000L) >> 48);
		arr[5] = (byte) ((val & 0x0000FF0000000000L) >> 40);
		arr[4] = (byte) ((val & 0x000000FF00000000L) >> 32);
		arr[3] = (byte) ((val & 0x00000000FF000000L) >> 24);
		arr[2] = (byte) ((val & 0x0000000000FF0000L) >> 16);
		arr[1] = (byte) ((val & 0x000000000000FF00L) >> 8);
		arr[0] = (byte) (val & 0x00000000000000FFL);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLELongs(long... vals) throws IOException {
		for (long value : vals) {
			writeLELong(value);
		}
	}

	@Override
	public void writeFloat(float val) throws IOException {
		int nVal = Float.floatToRawIntBits(val);
		byte[] arr = new byte[4];
		
		arr[0] = (byte) ((nVal & 0xFF000000) >> 24);
		arr[1] = (byte) ((nVal & 0x00FF0000) >> 16);
		arr[2] = (byte) ((nVal & 0x0000FF00) >> 8);
		arr[3] = (byte) (nVal & 0x000000FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeFloats(float... vals) throws IOException {
		for (float value : vals) {
			writeFloat(value);
		}
	}

	@Override
	public void writeLEFloat(float val) throws IOException {
		int nVal = Float.floatToRawIntBits(val);
		byte[] arr = new byte[4];
		
		arr[3] = (byte) ((nVal & 0xFF000000) >> 24);
		arr[2] = (byte) ((nVal & 0x00FF0000) >> 16);
		arr[1] = (byte) ((nVal & 0x0000FF00) >> 8);
		arr[0] = (byte) (nVal & 0x000000FF);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLEFloats(float... vals) throws IOException {
		for (float value : vals) {
			writeLEFloat(value);
		}
	}

	@Override
	public void writeDouble(double val) throws IOException {
		long nVal = Double.doubleToRawLongBits(val);
		byte[] arr = new byte[8];
		
		arr[0] = (byte) ((nVal & 0xFF00000000000000L) >> 52);
		arr[1] = (byte) ((nVal & 0x00FF000000000000L) >> 48);
		arr[2] = (byte) ((nVal & 0x0000FF0000000000L) >> 40);
		arr[3] = (byte) ((nVal & 0x000000FF00000000L) >> 32);
		arr[4] = (byte) ((nVal & 0x00000000FF000000L) >> 24);
		arr[5] = (byte) ((nVal & 0x0000000000FF0000L) >> 16);
		arr[6] = (byte) ((nVal & 0x000000000000FF00L) >> 8);
		arr[7] = (byte) (nVal & 0x00000000000000FFL);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeDoubles(double... vals) throws IOException {
		for (double value : vals) {
			writeDoubles(value);
		}
	}

	@Override
	public void writeLEDouble(double val) throws IOException {
		long nVal = Double.doubleToRawLongBits(val);
		byte[] arr = new byte[8];
		
		arr[7] = (byte) ((nVal & 0xFF00000000000000L) >> 52);
		arr[6] = (byte) ((nVal & 0x00FF000000000000L) >> 48);
		arr[5] = (byte) ((nVal & 0x0000FF0000000000L) >> 40);
		arr[4] = (byte) ((nVal & 0x000000FF00000000L) >> 32);
		arr[3] = (byte) ((nVal & 0x00000000FF000000L) >> 24);
		arr[2] = (byte) ((nVal & 0x0000000000FF0000L) >> 16);
		arr[1] = (byte) ((nVal & 0x000000000000FF00L) >> 8);
		arr[0] = (byte) (nVal & 0x00000000000000FFL);
		
		write(arr, 0, arr.length);
	}

	@Override
	public void writeLEDoubles(double... vals) throws IOException {
		for (double value : vals) {
			writeLEDouble(value);
		}
	}

}
