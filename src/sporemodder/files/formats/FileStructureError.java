package sporemodder.files.formats;

import java.util.List;

public class FileStructureError {
	private String errorCode;
	private int position;
	
	public FileStructureError(String errorCode, int position) {
		this.errorCode = errorCode;
		this.position = position;
	}

	@Override
	public String toString() {
		return "FileStructureError [errorCode=" + errorCode + ", position="
				+ position + "]";
	}
	
	public static String getErrorsString(List<FileStructureError> errors) {
		String lineSeparator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		for (FileStructureError error : errors) {
			sb.append(error.toString());
			sb.append(lineSeparator);
		}
		
		return sb.toString();
	}
	
	public static String getErrorsString(List<FileStructureError> errors, String prefix) {
		String lineSeparator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		for (FileStructureError error : errors) {
			sb.append(prefix + error.toString());
			sb.append(lineSeparator);
		}
		
		return sb.toString();
	}
}
