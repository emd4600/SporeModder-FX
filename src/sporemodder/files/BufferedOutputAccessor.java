package sporemodder.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BufferedOutputAccessor implements OutputStreamAccessor {
	
	private static final int BUFFER_SIZE = 8192;  // 8 kb
	
	private List<byte[]> buffers = new ArrayList<byte[]>();
	private int pos;
	private int basePos;
	private int length;
	private int allocatedSize = 0;
	
	public BufferedOutputAccessor() {
		// add a basic buffer
		buffers.add(new byte[BUFFER_SIZE]);
		allocatedSize = BUFFER_SIZE;
	}
	
	private void addBuffer() {
		buffers.add(new byte[BUFFER_SIZE]);
		allocatedSize = BUFFER_SIZE;
	}
	
	@Override
	public void seek(int off) throws IOException {
		pos = basePos + off;
	}
	@Override
	public void seekAbs(int off) throws IOException {
		pos = off;
	}
	@Override
	public void skipBytes(int len) throws IOException {
		pos += len;
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int length() throws IOException {
		return length;
	}
	
	/**
	 * Generates the necessary buffers until length == len and this.allocatedSize >= len;
	 * @param len
	 */
	private void generateBuffers(int len) {
		/*
		if (allocatedSize < len) {
			int newBuffersCount = (len - allocatedSize) / BUFFER_SIZE + 1;
			for (int i = 0; i < newBuffersCount; i++) {
				addBuffer();
			}
		}
		*/
		
		// Alternative method
		while (allocatedSize < len) {
			addBuffer();
		}
	}
	
	private void allocateIfNeeded(int size) {
		generateBuffers(pos + size);
	}
	
	@Override
	public void setLength(int len) throws IOException {
		generateBuffers(len);
		length = len;
	}
	
	@Override
	public int getFilePointer() throws IOException {
		return pos - basePos;
	}
	@Override
	public int getFilePointerAbs() throws IOException {
		return pos;
	}
	@Override
	public void setBaseOffset(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getBaseOffset() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writePadding(int pad) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void write(byte[] arr) throws IOException {
		// allocate new buffers until we have enough space
//		while (pos + arr.length > allocatedSize) {
//			buffers.add(new byte[BUFFER_SIZE]);
//			allocatedSize += BUFFER_SIZE;
//		}
//		
//		int writtenSize = 0;
//		
//		//TODO does the array fit into the selected buffer or do we have to use more than one?
//		while(writtenSize < arr.length) {
//			int bufferIndex = pos / BUFFER_SIZE;
//			int bufferPos = pos - bufferIndex * BUFFER_SIZE; 
//		}
		
		generateBuffers(pos + arr.length);
		
		if (arr.length <= BUFFER_SIZE) {
			int bufferIndex = pos / BUFFER_SIZE;
			int bufferPos = pos - bufferIndex * BUFFER_SIZE;
			int firstSize = Math.min(BUFFER_SIZE - pos, arr.length);
			System.arraycopy(arr, 0, buffers.get(bufferPos), pos, firstSize);
			if (firstSize < arr.length) {
				System.arraycopy(arr, firstSize, buffers.get(bufferPos + 1), pos + firstSize, arr.length - firstSize);
			}
		}
		else {
			int bufferIndex = pos / BUFFER_SIZE;
			int bufferPos = pos - bufferIndex * BUFFER_SIZE;
		}
		
		pos += arr.length;
	}
	
	@Override
	public void write(byte[] arr, int off, int len) throws IOException {
		generateBuffers(pos + len - off);
		
	}
	
	@Override
	public void write(String text) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeCString(String text) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeString8(String text) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeString16(String text) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEString16(String text) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeBoolean(boolean val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeBooleans(boolean... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeByte(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeBytes(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUByte(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUBytes(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeShort(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeShorts(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEShort(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEShorts(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUShort(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUShorts(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEUShort(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEUShorts(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	// should only be used when the corresponding size has been allocated
	private void writeUnsafe(byte b) {
		int bufferIndex = pos / BUFFER_SIZE;
		int bufferPos = pos - bufferIndex * BUFFER_SIZE;
		buffers.get(bufferIndex)[bufferPos] = b;
	}
	@Override
	public void writeInt(int val) throws IOException {
		allocateIfNeeded(4);
		writeUnsafe((byte) (val & 0xFF));
		writeUnsafe((byte) ((val >> 8) & 0xFF));
		writeUnsafe((byte) ((val >> 16) & 0xFF));
		writeUnsafe((byte) ((val >> 24) & 0xFF));
	}
	@Override
	public void writeInts(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEInt(int val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEInts(int... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUInt(long val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUInts(long... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEUInt(long val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEUInts(long... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLong(long val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLongs(long... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLELong(long val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLELongs(long... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeFloat(float val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeFloats(float... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEFloat(float val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEFloats(float... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeDouble(double val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeDoubles(double... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEDouble(double val) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeLEDoubles(double... vals) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	
}