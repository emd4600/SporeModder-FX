package sporemodder.files.formats;

import java.io.IOException;
import java.util.List;

import sporemodder.files.OutputStreamAccessor;

public interface FileFormatStructure {

	public List<FileStructureError> getAllErrors();
	
	public void write(OutputStreamAccessor out) throws IOException;
	
	
	public static class DefaulFormatStructure implements FileFormatStructure {

		@Override
		public List<FileStructureError> getAllErrors() {
			return null;
		}

		@Override
		public void write(OutputStreamAccessor out) throws IOException {
		}
		
	}
}
