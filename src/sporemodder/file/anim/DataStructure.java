package sporemodder.file.anim;

import java.io.IOException;

import emord.filestructures.StreamReader;

/**
 * A convenience class used to read data from a file without any order. It is meant
 * to be equivalent to accessing certain fields of an structure.
 */
public class DataStructure {

	private final StreamReader stream;
	private long pointer;
	
	public DataStructure(StreamReader stream) {
		this.stream = stream;
	}
	
	public StreamReader getStream() {
		return stream;
	}
	
	public long getPointer() {
		return pointer;
	}
	
	public void setPointer(long pointer) {
		this.pointer = pointer;
	}
	
	public long getUInt(int offset) throws IOException {
		stream.seek(pointer + offset);
		return stream.readLEUInt();
	}
	
	public int getInt(int offset) throws IOException {
		stream.seek(pointer + offset);
		return stream.readLEInt();
	}
	
	public float getFloat(int offset) throws IOException {
		stream.seek(pointer + offset);
		return stream.readLEFloat();
	}
}
