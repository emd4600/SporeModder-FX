package sporemodder.utilities.names;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sporemodder.utilities.Hasher;

public class NameRegistry {
	
	public static final String NAME_FILE = "FILE";
	public static final String NAME_TYPE = "TYPE";
	public static final String NAME_PROP = "PROPERTY";
	public static final String NAME_SPUI = "SPUI";
	
	protected static final String DEFAULT_GROUP = "$_DEFAULT_GROUP";
	
	protected static final String TOKEN_END = "end";
	protected static final String TOKEN_IF = "if";
	protected static final String TOKEN_ELSE = "else";
	protected static final String TOKEN_OBSOLETE = "obsolete";
	protected static final String TOKEN_GROUP = "group";
	protected static final String TOKEN_ENDGROUP = "endgroup";  // are we going to use this one?

	protected final List<ConditionedNameRegistry> subregs = new ArrayList<ConditionedNameRegistry>();
	
	private final HashMap<String, NameRegistry> groups = new HashMap<String, NameRegistry>();
	private final HashMap<String, Integer> hashes = new HashMap<String, Integer>();
	private final HashMap<Integer, String> names = new HashMap<Integer, String>();
	
	public NameRegistry(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		try {
			read(in);
		} finally {
			in.close();
		}
	}
	
	public NameRegistry(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			read(in);
		} finally {
			in.close();
		}
	}
	
	
	public NameRegistry(BufferedReader in) throws IOException {
		read(in);
	}
	
	public NameRegistry() {
		// TODO Auto-generated constructor stub
	}
	
	public NameRegistry getGroup(String group) {
		return groups.get(group);	
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
		String name = null;
		name = names.get(hash);
		if (name == null) {
			for (NameRegistry registry : groups.values()) {
				String result = registry.getName(hash);
				if (result != null) return result;
			}
			for (ConditionedNameRegistry registry : subregs) {
				String result = registry.getName(hash);
				if (result != null) return result;
			}
		}
		return name;
	}
	
	public int getHash(String name) {
		Integer hash = hashes.get(name);
		if (hash == null) {
			for (NameRegistry registry : groups.values()) {
				int result = registry.getHash(name);
				if (result != -1) return result;
			}
			for (ConditionedNameRegistry registry : subregs) {
				int result = registry.getHash(name);
				if (result != -1) return result;
			}
			return -1;
		}
		return hash;
	}
	
	public void clear() {
		hashes.clear();
		names.clear();
		groups.clear();
	}
	
	public List<String> getNames() {
		List<String> result = new ArrayList<String>(names.values());
		for (ConditionedNameRegistry registry : subregs) {
			result.addAll(registry.getNames());
		}
		return result;
	}
	
	protected void read(BufferedReader in) throws IOException {
		String line;

		while ((line = in.readLine()) != null) {
			
			String str = line.split("//")[0].trim();
			
			if (str.length() == 0) continue;
			
			// Special case, tokens
			if (str.startsWith("#")) {
				String tokenStr = str.substring(1);
				
				if (tokenStr.startsWith(TOKEN_OBSOLETE)) {
					clear();
				}
				else if (tokenStr.startsWith(TOKEN_IF)) {
					ConditionedNameRegistry registry = new ConditionedNameRegistry(in, tokenStr.substring(2).trim());
					subregs.add(registry);
				}
				else if (tokenStr.startsWith(TOKEN_GROUP)) {
					NameRegistry reg = new NameRegistry(in);
					groups.put(tokenStr.split(" ")[1], reg);
				}
				else if (tokenStr.startsWith(TOKEN_END)) {
					// Stop reading this name registry. This works with nested registries too
					break;
				}
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
	
}
