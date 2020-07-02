package sporemodder.file.locale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import sporemodder.file.TextReader;

public class LocaleUnit {
	/** Type of hyperlink used by ArgScript formats to describe tableID!instanceID locale pairs. */
	public static final String HYPERLINK_LOCALE = "HYPERLINK_LOCALE";
	
	public static final int GROUP_ID = 0x02FABF01;
	public static final int TYPE_ID = 0x02FAC0B6;
	
	private final Map<Integer, String> entries = new LinkedHashMap<>();
	
	public String getText(int id) {
		return entries.get(id);
	}
	
	public void read(String text) throws Exception {
		String[] lines = text.split("\\r?\\n");
		for (String line : lines) {
			int indexOf = line.indexOf('#');
			if (indexOf != -1) {
				line = line.substring(0, indexOf);
			}
			line = line.trim();
			if (!line.isEmpty()) {
				String[] splits = line.split("\\s+", 2);
				if (!splits[0].startsWith("0x")) throw new IOException("Invalid ID " + splits[0]);
				int id = Integer.parseUnsignedInt(splits[0].substring(2), 16);
				
				entries.put(id, splits.length == 1 ? "" : splits[1]);
			}
		}
	}
	
	public static LocaleUnit fromFile(File file) {
		if (!file.exists()) return null;
		try (InputStream stream = new FileInputStream(file)) {
			LocaleUnit unit = new LocaleUnit();
			unit.read(new TextReader().read(stream).getText());
			return unit;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
