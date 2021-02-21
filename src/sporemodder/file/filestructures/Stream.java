package sporemodder.file.filestructures;

import java.io.Closeable;
import java.io.IOException;

/**
 * The base interface of any stream object. Stream objects are capable of reading/writing data from a file or memory.
 * It supports moving the file pointer. It also has a special feature called 'base offset': making the stream
 * believe the beginning of the file is at a certain offset.
 */
public interface Stream extends Closeable {
	
	public static enum StringEncoding {
		ASCII("US-ASCII"),
		UTF16LE("UTF-16LE"),
		UTF16BE("UTF-16BE");
		
		private String encodingStr;
		
		private StringEncoding(String encodingStr) {
			this.encodingStr = encodingStr;
		}
		
		public String getCharset() {
			return encodingStr;
		}
	}
	
	/**
	 * Moves the file pointer to the specified offset.
	 */
	public void seek(long off) throws IOException;
	
	/**
	 * Moves the file pointer to the specified offset, which is an absolute position:
	 * this means that the current base offset will be ignored.
	 */
	public void seekAbs(long off) throws IOException;
	
	/**
	 * Skips over and discards n bytes of data from this input stream.
	 */
	public void skip(int n) throws IOException;
	
	/**
	 * Closes this input stream and releases any system resources associated with the stream.
	 */
	public void close() throws IOException;
	
	/**
	 * Returns the number of bytes of data that this file has, ignoring any base offset.
	 */
	public long length() throws IOException;
	
	/**
	 * Makes this stream be n bytes long, ignoring any base offset.
	 */
	public void setLength(long n) throws IOException;
	
	/**
	 * Returns the current position of the file pointer; that is, the position of the byte that will be read/written next.
	 */
	public long getFilePointer() throws IOException;
	
	/**
	 * Returns the current absolute position of the file pointer ignoring the base offset; that is, the position of the byte that will be read/written next.
	 */
	public long getFilePointerAbs() throws IOException;
	
	/**
	 * Sets the base offset of this stream, which makes it believe the beginning of the stream is at the specified offset.
	 */
	public void setBaseOffset(long val) throws IOException;
	
	/**
	 * Returns the base offset of this stream, which makes it believe the beginning of the stream is at the specified offset.
	 */
	public long getBaseOffset() throws IOException;
}
