package sporemodder.files.formats.argscript;

public class ArgScriptEnum {
	public static final String DEFAULT_KEY = "ErrorKeyNotFound";
	private String[] keys;
	private int[] values;
	private int defaultValue = -1;
	private String defaultKey = DEFAULT_KEY;
	
	public ArgScriptEnum(String[] keys, int[] values) {
		if (keys.length != values.length) {
			throw new IllegalArgumentException("Enum length mismatch error");
		}
		this.keys = keys;
		this.values = values;
	}
	
	public ArgScriptEnum(String[] keys, int[] values, int defaultValue, String defaultKey) {
		this(keys, values);
		this.defaultValue = defaultValue;
		this.defaultKey = defaultKey;
	}
	
	public int getValue(String key) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].equals(key)) {
				return values[i];
			}
		}
		return defaultValue;
	}
	
	public String getKey(int value) {
		for (int i = 0; i < keys.length; i++) {
			if (values[i] == value) {
				return keys[i];
			}
		}
		return defaultKey;
	}

	public String[] getKeys() {
		return keys;
	}

	public int[] getValues() {
		return values;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public String getDefaultKey() {
		return defaultKey;
	}
	
	
}
