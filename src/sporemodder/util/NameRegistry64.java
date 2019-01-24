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
import java.util.HashMap;

import sporemodder.HashManager;

/**
 * A registry file is a list which assigns a 64-bit integer (long) hash value to a string, and vice versa.
 * Very similar to {@link NameRegistry}, but here the hash must always be specified explicitly.
 */
public class NameRegistry64 {
	protected final HashMap<String, Long> hashes = new HashMap<String, Long>();
	protected final HashMap<Long, String> names = new HashMap<Long, String>();
	protected HashManager hashManager;
	
	public NameRegistry64(HashManager hashManager) {
		this.hashManager = hashManager;
	}
	
	
	/**
	 * Returns the name that is assigned to the given hash, or null if the hash is not assigned.
	 * @param hash The hash whose equivalent name will be returned.
	 * @return The equivalent name, or null.
	 */
	public String getName(long hash) {
		return names.get(hash);
	}
	
	/**
	 * Returns the hash that is assigned to the given name, or null if the name is not assigned. This
	 * does not calculate the hash, it only looks into the registry.
	 * @param name The name whose equivalent hash will be returned.
	 * @return The equivalent hash, or null.
	 */
	public Long getHash(String name) {
		return hashes.get(name);
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
			// We don't calculate hashes in this name registry type
			return;
		} 
		else {
			// Remove any trailing whitespaces
			String hashStr = strings[1].trim();
			long hash = hashManager.int64(hashStr);
			
			names.put(hash, name);
			hashes.put(name, hash);
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
}
