package sporemodder.files;

import java.io.Closeable;
import java.io.IOException;

public interface StreamAccessor extends Closeable {
	public void seek(int off) throws IOException;
	public void seekAbs(int off) throws IOException;
	public void skipBytes(int len) throws IOException;
	
	public void close() throws IOException;
	public int length() throws IOException;
	public void setLength(int len) throws IOException;
	
	public int getFilePointer() throws IOException;
	public int getFilePointerAbs() throws IOException;
	
	public void setBaseOffset(int val) throws IOException;
	public int getBaseOffset() throws IOException;
}
