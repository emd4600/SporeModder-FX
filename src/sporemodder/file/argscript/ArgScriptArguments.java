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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/** An unmodifiable list with the arguments of a line or option .*/
public class ArgScriptArguments {
	int splitIndex;
	List<String> arguments;
	List<Integer> positions;
	List<Integer> endPositions;
	int numArguments;
	ArgScriptStream<?> stream;
	TextPositionMap tracker;
	TreeMap<Integer, Integer> originalToText;
	
	public ArgScriptStream<?> getStream() {
		return stream;
	}
	
	public int getSplitIndex() {
		return splitIndex;
	}
	
	public int getSize() {
		return numArguments;
	}
	
	public int getStartPosition() {
		return positions.get(0);
	}
	public int getEndPosition() {
		return endPositions.get(endPositions.size() - 1);
	}
	public int getPosition(int index) {
		return positions.get(index);
	}
	public int getEndPosition(int index) {
		return endPositions.get(index);
	}
	public int getRealPosition(int position) {
		return tracker == null ? position : tracker.getRealPosition(position);
	}
	
	public void addHyperlink(String type, Object object, int index) {
		stream.addHyperlink(type, object, positions.get(index), endPositions.get(index));
	}
	
	// Converts a position in the original text to a position in the splits text
	public int toTextPosition(int position) {
		Map.Entry<Integer, Integer> entry = originalToText.floorEntry(position);
		
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
	
	public int size() {
		return numArguments;
	}
	public List<String> get() {
		return arguments;
	}
	public String get(int index) {
		return arguments.get(index);
	}
	public String getSingle() {
		return arguments.get(0);
	}
	public Iterator<String> iterator() {
		return arguments.iterator();
	}
	public ListIterator<String> listIterator() {
		return arguments.listIterator();
	}
}
