package sporemodder.file.filestructures;

import java.io.IOException;

public class MemoryStream extends FixedMemoryStream {
	private static final int INITIAL_SIZE = 8192; 
	
	private int length;
	private float resizeFactor = 1.5f;
	
	public MemoryStream() {
		super(INITIAL_SIZE);
		length = 0;
	}
	
	public MemoryStream(int nCapacity) {
		super(nCapacity);
		length = 0;
	}
	
	public MemoryStream(byte[] arr) {
		super(arr);
		length = arr.length;
	}
	
	public byte[] getRawData() {
		return data;
	}
	
	public void writeInto(StreamWriter out) throws IOException {
		out.write(data, 0, length);
	}
	
	public void reset(int nCapacity) {
		data = new byte[nCapacity];
		filePointer = 0;
		baseOffset = 0;
		length = 0;
	}
	
	private void reallocate(long size) {
		byte[] arr = new byte[(int) size];
		
		if (data != null) {
			System.arraycopy(data, 0, arr, 0, data.length);
		}
		
		data = arr;
	}
	
	@Override
	public byte[] toByteArray() throws IOException {
		byte[] arr = new byte[length];
		System.arraycopy(data, 0, arr, 0, length);
		return arr;
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public void setLength(long len) {
		if (len > data.length) {
			reallocate(len);
		}
		length = (int) len;
	}

	@Override
	public void writePadding(int pad) throws IOException {
		while (filePointer + pad > data.length) {
			reallocate((int) (data.length * resizeFactor));
		}
		for (int i = 0; i < pad; i++) {
			data[filePointer++] = 0;
		}
		
		if (filePointer > length) {
			length = filePointer;
		}
	}

	@Override
	public void write(byte[] arr) throws IOException {
		write(arr, 0, arr.length);
	}

	@Override
	public void write(byte[] arr, int off, int len) throws IOException {
		while (filePointer + len > data.length) {
			reallocate((int) (data.length * resizeFactor));
		}
		
		System.arraycopy(arr, off, data, filePointer, len);
		filePointer += len;
		
		if (filePointer > length) {
			length = filePointer;
		}
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
			writeLEUInt(value);
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

	@Override
	public void writeCString(String text, StringEncoding encoding) throws IOException {
		if (text != null) writeString(text, encoding);
		
		if (encoding != StringEncoding.ASCII) {
			writeShort(0);
		} else {
			writeByte(0);
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
		
		write(array, 0, array.length);
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
		
		write(array, 0, array.length);
	}
}
