/****************************************************************************
* Copyright (C) 2019 Eric Mor
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
package sporemodder.file.argscript;

import java.util.Map;
import java.util.TreeMap;

/**
 * A class used to keep track of the original text positions so we can replace variables, remove comments, etc
 * and still be able to correctly show errors.
 */
public class TextPositionMap {

	/** Maps the position to the original position before doing any changes to the text. */
	private final TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
	
	public void addAll(TextPositionMap other) {
		map.putAll(other.map);
	}
	
	public void clear() {
		map.clear();
	}
	
	public void addEntry(int position, int realPosition) {
		map.put(position, realPosition);
	}
	
	public int getRealPosition(int position) {
		Map.Entry<Integer, Integer> entry = map.floorEntry(position);
		
		// If there are no entries until our position, just return what the user gave
		if (entry == null) {
			return position;
		}
		
		// If what we have found is the exact position, just return it
		if (entry.getKey() == position) {
			return entry.getValue();
		}
		else {
			// The closest original position
			int floorOriginal = entry.getValue();
			
			// How much until the position the user asked for?
			int difference = position - entry.getKey();
			
			// Since we used floorEntry, we know we can safely do this because there are no mappings in between
			return floorOriginal + difference;
		}
	}
}
