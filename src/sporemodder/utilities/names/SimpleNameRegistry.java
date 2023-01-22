package sporemodder.utilities.names;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sporemodder.utilities.Hasher;

public class SimpleNameRegistry {

	private final HashMap<String, Integer> hashes = new HashMap<String, Integer>();
	private final HashMap<Integer, String> names = new HashMap<Integer, String>();
	
	public SimpleNameRegistry(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		try {
			read(in);
		} finally {
			in.close();
		}
	}
	
	public SimpleNameRegistry(BufferedReader in) throws IOException {
		read(in);
	}
	
	public SimpleNameRegistry() {
		// TODO Auto-generated constructor stub
	}
	
	public SimpleNameRegistry(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			read(in);
		} finally {
			in.close();
		}
	}

	public int getCount() {
		return names.size();
	}
	
	public void read(BufferedReader in) throws IOException {
		String line;

		while ((line = in.readLine()) != null) {
			
			String str = line.split("//")[0].trim();
			
			if (str.length() == 0) continue;
			
			// Ignore
			if (str.startsWith("#")) {
				continue;
			}
			else {
				parseEntry(str);
			}
		}
		
//		System.out.println(groups);
	}
	
	protected void parseEntry(String str) {
		String[] strings = str.split("\t");
		String name = strings[0].trim();
		if (strings.length < 2) {
			int hash = Hasher.stringToFNVHash(name);
			names.put(hash, name);
		} else {
			String hashStr = strings[1].trim();
			int hash = Hasher.decodeInt(hashStr);
			names.put(hash, name);
			hashes.put(name, hash);
		}
	}
	
	public void write(BufferedWriter out) throws IOException {
//		for (Map.Entry<String, Integer> entry : hashes.entrySet()) {
//			String name = entry.getKey();
//			int hash = Hasher.stringToFNVHash(name);
//			if (hash != entry.getValue()) {
//				out.write(name + "\t0x" + Integer.toHexString(entry.getValue()));
//				out.newLine();
//			} else {
//				out.write(name);
//				out.newLine();
//			}
//		}
		
		for (Map.Entry<Integer, String> entry : names.entrySet()) {
			String name = entry.getValue();
			int hash = Hasher.stringToFNVHash(name);
			if (hash != entry.getKey()) {
				out.write(name + "\t0x" + Integer.toHexString(entry.getKey()));
				out.newLine();
			} else {
				out.write(name);
				out.newLine();
			}
		}
	}
	
	
	public boolean addName(String name) {	
		names.put(Hasher.stringToFNVHash(name), name);
		
		return true;
	}
	
	public boolean addAlias(String name, int hash) {
		hashes.put(name, hash);
		names.put(hash, name);
		
		return true;
	}
	
	public String getName(int hash) {
		return names.get(hash);
	}
	
	public int getHash(String name) {
		return hashes.get(name);
	}
	
	public void clear() {
		hashes.clear();
		names.clear();
	}
	
	public Collection<String> getNames() {
		return names.values();
	}
}
