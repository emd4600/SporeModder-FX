package sporemodder.files;

import java.io.IOException;

public interface OutputStreamAccessor extends StreamAccessor {
	public void writePadding(int pad) throws IOException;
	
	public void write(byte[] arr) throws IOException;
	public void write(byte[] arr, int off, int len) throws IOException;
	
	public void write(String text) throws IOException;
	public void writeCString(String text) throws IOException;
	public void writeString8(String text) throws IOException;
	public void writeString16(String text) throws IOException;
	public void writeLEString16(String text) throws IOException;
	
	public void writeBoolean(boolean val) throws IOException;
	public void writeBooleans(boolean ... vals) throws IOException;
	
	public void writeByte(int val) throws IOException;
	public void writeBytes(int ... vals) throws IOException;
	public void writeUByte(int val) throws IOException;
	public void writeUBytes(int ... vals) throws IOException;
	
	public void writeShort(int val) throws IOException;
	public void writeShorts(int ... vals) throws IOException;
	public void writeLEShort(int val) throws IOException;
	public void writeLEShorts(int ... vals) throws IOException;
	public void writeUShort(int val) throws IOException;
	public void writeUShorts(int ... vals) throws IOException;
	public void writeLEUShort(int val) throws IOException;
	public void writeLEUShorts(int ... vals) throws IOException;
	
	public void writeInt(int val) throws IOException;
	public void writeInts(int ... vals) throws IOException;
	public void writeLEInt(int val) throws IOException;
	public void writeLEInts(int ... vals) throws IOException;
	public void writeUInt(long val) throws IOException;
	public void writeUInts(long ... vals) throws IOException;
	public void writeLEUInt(long val) throws IOException;
	public void writeLEUInts(long ... vals) throws IOException;
	
	public void writeLong(long val) throws IOException;
	public void writeLongs(long ... vals) throws IOException;
	public void writeLELong(long val) throws IOException;
	public void writeLELongs(long ... vals) throws IOException;
	
	public void writeFloat(float val) throws IOException;
	public void writeFloats(float ... vals) throws IOException;
	public void writeLEFloat(float val) throws IOException;
	public void writeLEFloats(float ... vals) throws IOException;
	
	public void writeDouble(double val) throws IOException;
	public void writeDoubles(double ... vals) throws IOException;
	public void writeLEDouble(double val) throws IOException;
	public void writeLEDoubles(double ... vals) throws IOException;
}
