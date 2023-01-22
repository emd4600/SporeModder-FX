package sporemodder.files.formats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class LocaleFile extends HashMap<Integer, String> {

	public LocaleFile(File file) throws NumberFormatException, IOException {
		read(file);
	}
	
	public LocaleFile(BufferedReader reader) throws NumberFormatException, IOException {
		read(reader);
	}
	
	public void read(BufferedReader reader) throws NumberFormatException, IOException {
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("0x")) {
				String[] splits = line.split(" ", 2);
				
				put(Integer.parseUnsignedInt(splits[0].substring(2), 16), splits[1]);
			}
		}
	}
	
	public void read(File file) throws NumberFormatException, IOException { 
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			read(reader);
		}
	}
}
