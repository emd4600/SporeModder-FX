/****************************************************************************
* Copyright (C) 2018 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/

package sporemodder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;

/**
 * A registry file is a list which assigns an integer hash value to a string, and vice versa.
 */
public class NameRegistry {
	// ~ names are in lowercase
	protected final HashMap<String, Integer> hashes = new HashMap<String, Integer>();
	protected final HashMap<Integer, String> names = new HashMap<Integer, String>();
	protected HashManager hashManager;
	protected final String fileName;
	protected final String name;
	
	public NameRegistry(HashManager hashManager, String name, String fileName) {
		this.hashManager = hashManager;
		this.name = name;
		this.fileName = fileName;
	}
	
	public String getDescription() {
		return name;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void clear() {
		hashes.clear();
		names.clear();
	}
	
	/**
	 * Returns the name that is assigned to the given hash, or null if the hash is not assigned.
	 * @param hash The hash whose equivalent name will be returned.
	 * @return The equivalent name, or null.
	 */
	public String getName(int hash) {
		return names.get(hash);
	}
	
	/**
	 * Returns the hash that is assigned to the given name, or null if the name is not assigned. This
	 * does not calculate the hash, it only looks into the registry.
	 * @param name The name whose equivalent hash will be returned.
	 * @return The equivalent hash, or null.
	 */
	public Integer getHash(String name) {
		return hashes.get(name);
	}
	
	/**
	 * Adds a name-hash pair into this registry.
	 * @param name
	 * @param hash
	 */
	public void add(String name, int hash) {
		hashes.put(name.toLowerCase(), hash);
		names.put(hash, name);
	}
	
	/**
	 * Processes a single line in the registry file, converting it to an entry in this class.
	 * @param str The line to be parsed.
	 */
	protected void parseEntry(String str) {
		// There are 1 or 2 strings: the name and, optionally, the hash.
		String[] strings = str.split("\t");
		String name = strings[0].trim();
		
		if (strings.length < 2) {
			int hash = hashManager.fnvHash(name);
			names.put(hash, name);
		} 
		else {
			// Remove any trailing whitespaces
			String hashStr = strings[1].trim();
			int hash = hashManager.int32(hashStr);
			
			if (name.endsWith("~")) {
				hashes.put(name.toLowerCase(), hash);
			}
			else {
				hashes.put(name, hash);
			}
			hashes.put(name, hash);
			names.put(hash, name);
		}
	}
	
	public void read(File file) throws IOException {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			read(in);
		}
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
	}
	
	public void write(StreamWriter stream) throws IOException {
		String eol = System.getProperty("line.separator");
		
		for (Map.Entry<Integer, String> entry : names.entrySet()) {
			String name = entry.getValue();
			int hash = hashManager.fnvHash(name);
			if (hash != entry.getKey()) {
				stream.writeString(name + "\t0x" + Integer.toHexString(entry.getKey()) + eol, StringEncoding.ASCII);
			} else {
				stream.writeString(name + eol, StringEncoding.ASCII);
			}
		}
	}

	public boolean isEmpty() {
		return names.isEmpty() && hashes.isEmpty();
	}

	public Collection<String> getNames() {
		return names.values();
	}
}
