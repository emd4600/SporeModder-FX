package sporemodder.files.formats;

import sporemodder.utilities.Hasher;

public class LocalizedText {
	
	public int tableID = -1;
	public int instanceID = -1;
	public String text = null;

	private static final String DEFAULT_STRING = "null";
	
	@Override
	public String toString() {
		if (text == null || text.length() == 0) {
			if (tableID == -1 && instanceID == -1) {
				return DEFAULT_STRING;
			}
			return "(" + Hasher.getFileName(tableID) + "!" + Hasher.getFileName(instanceID) + ")";
		} else {
			return "\"" + text + "\"";
		}
	}
	
	public String getString() {
		if (text == null || text.length() == 0) {
			return "(" + Hasher.getFileName(tableID) + "!" + Hasher.getFileName(instanceID) + ")";
		} else {
			return text;
		}
	}
	
	public String getLocaleInfoString() {
		return "(" + Hasher.getFileName(tableID) + "!" + Hasher.getFileName(instanceID) + ")";
	}

	public LocalizedText(String text) {
		this.text = text;
	}
	
	public LocalizedText(int tableID, int instanceID) {
		this.tableID = tableID;
		this.instanceID = instanceID;
	}

	public LocalizedText(LocalizedText text) {
		copy(text);
	}
	
	public void copy(LocalizedText other) {
		if (other != null) {
			this.text = other.text;
			this.tableID = other.tableID;
			this.instanceID = other.instanceID;
		}
	}

	public static boolean isValid(LocalizedText text) {
		return text != null && (text.text != null || text.tableID != -1 || text.instanceID != -1);
	}
}
