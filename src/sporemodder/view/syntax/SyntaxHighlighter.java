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
package sporemodder.view.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import sporemodder.file.argscript.TextPositionMap;

public class SyntaxHighlighter {

	private class StyleEntry {
		int start;
		int size;
		Collection<String> styles;
		
		@Override
		public String toString() {
			return String.format("size(%d)", size);
		}
		
		private int getEnd() {
			return start + size;
		}
		
		private void setStart(int value) {
			entries.remove(start);
			size -= value - start;
			start = value;
			entries.put(start, this);
		}
		
		private void setEnd(int value) {
			size = value - start;
		}
	}
	
	/** Maps the start position of an entry with its information. */
	private final TreeMap<Integer, StyleEntry> entries = new TreeMap<Integer, StyleEntry>();
	private String text;
	private List<Integer> linePositions;
	
	public void setText(String text, List<Integer> linePositions) {
		this.text = text;
		this.linePositions = linePositions;
		this.entries.clear();
	}
	
	public int getLinePosition(int lineNumber) {
		return lineNumber == -1 ? 0 : linePositions.get(lineNumber);
	}
	
	public int getLineCount() {
		return linePositions.size();
	}
	
	public void add(int start, int size, Collection<String> styles) {
		StyleEntry entry = this.new StyleEntry();
		entry.start = start;
		entry.size = size;
		entry.styles = styles;
		entries.put(start, entry);
		// maybe check if the entry does not collide?
	}
	
	public void add(int lineNumber, int start, int size, Collection<String> styles) {
		add(linePositions.get(lineNumber) + start, size, styles);
	}
	
	/**
	 * Adds an extra style entry, splitting the existing entries if there are any collisions.
	 */
	public void addExtra(int start, int size, String style, boolean removeOld) {
		addExtra(start, size, Collections.singleton(style), removeOld);
	}
	
	private StyleEntry floorEntry(int start) {
		Entry<Integer, StyleEntry> value = entries.floorEntry(start);
		return value == null ? null : value.getValue();
	}
	
	private StyleEntry higherEntry(int start) {
		Entry<Integer, StyleEntry> value = entries.higherEntry(start);
		return value == null ? null : value.getValue();
	}
	
	private void addExtraRemoving(int start, int size, Collection<String> styles) {
		int end = start + size;
		
		// Resize the floor entry if necessary
		StyleEntry leftEntry = floorEntry(start);
		if (leftEntry != null && 
				leftEntry.start < start && leftEntry.getEnd() <= end && leftEntry.getEnd() > start) {
			
			leftEntry.setEnd(start);
		}
		
		// Resize the final entry if necessary
		StyleEntry rightEntry = floorEntry(end);
		if (rightEntry != null && 
				rightEntry.start < end && rightEntry.getEnd() >= end) {
			
			rightEntry.setStart(end);
		}
		
		// Now remove all the entries inside the new style
		StyleEntry ceilEntry = higherEntry(start);
		// Invariant: ceilEntry.start >= start
		while (ceilEntry != null && ceilEntry.getEnd() <= end) {
			entries.remove(ceilEntry.start);
			ceilEntry = higherEntry(ceilEntry.start);
		}
		
		// Now that the boundaries are cleared, just add the new style
		add(start, size, styles);
	}
	
	private void addExtraNoRemoving(int start, int size, Collection<String> styles) {
		int end = start + size;
		
		// Split the entry that intersects on the left, if necessary
		StyleEntry leftEntry = floorEntry(start);
		// Invariant: leftEntry.start <= start
		// We don't check the end boundary cause we might split that later
		if (leftEntry != null && leftEntry.start < start && leftEntry.getEnd() > start) {
			// We must add a new entry; we don't add the new style cause that will be done in the loop
			add(start, leftEntry.getEnd() - start, leftEntry.styles);
			leftEntry.setEnd(start);
		}
		
		// Split the entry that intersects on the right, if necessary
		StyleEntry rightEntry = floorEntry(end);
		// Invariant: rightEntry.start <= end
		// Only split if a fragment of the entry does not need the new style
		if (rightEntry != null && rightEntry.start < end && rightEntry.getEnd() > end) {
			// We must add a new entry; we don't add the new style cause that will be done in the loop
			add(rightEntry.start, end - rightEntry.start, rightEntry.styles);
			rightEntry.setStart(end);
		}
		
		// When we arrive at this point, we can be sure that the entries inside [start, end] don't intersect with the new style boundaries
		// So just add the new style, filling the gaps as well
		StyleEntry entry = higherEntry(start);
		int lastEnd = start;
		while (entry != null && entry.getEnd() <= end) {
			// Did we leave a gap?
			if (entry.start > lastEnd) {
				add(lastEnd, entry.start - lastEnd, styles);
			}
			
			// entry.styles might be immutable
			entry.styles = new ArrayList<String>(entry.styles);
			entry.styles.addAll(styles);
			
			lastEnd = entry.getEnd();
			entry = higherEntry(lastEnd);
		}
		
		// Finally, is there a gap between the last contained entry and the end of the new style?
		if (lastEnd < end) {
			add(lastEnd, end - lastEnd, styles);
		}
	}
	
	/**
	 * Adds an extra style entry, splitting the existing entries if there are any collisions.
	 */
	public void addExtra(int start, int size, Collection<String> styles, boolean removeOld) {
		if (entries.isEmpty()) {
			// No need for complicated calculations, just add them all
			add(start, size, styles);
		} 
		else {
			if (removeOld) {
				addExtraRemoving(start, size, styles);
			} else {
				addExtraNoRemoving(start, size, styles);
			}
		}
	}
	
	public void addExtras(SyntaxHighlighter other, boolean removeOld) {
		if (entries.isEmpty()) {
			// No need for complicated calculations, just add them all
			for (Map.Entry<Integer, StyleEntry> entry : other.entries.entrySet()) {
				add(entry.getValue().start, entry.getValue().size, entry.getValue().styles);
			}
		} else {
			for (Map.Entry<Integer, StyleEntry> entry : other.entries.entrySet()) {
				addExtra(entry.getValue().start, entry.getValue().size, entry.getValue().styles, removeOld);
			}
		}
	}
	
	public void addExtras(SyntaxHighlighter other, TextPositionMap positionMap, boolean removeOld) {
		if (entries.isEmpty()) {
			// No need for complicated calculations, just add them all
			for (Map.Entry<Integer, StyleEntry> entry : other.entries.entrySet()) {
				int start = positionMap.getRealPosition(entry.getValue().start);
				int end = positionMap.getRealPosition(entry.getValue().getEnd());
				add(start, end-start, entry.getValue().styles);
			}
		} else {
			for (Map.Entry<Integer, StyleEntry> entry : other.entries.entrySet()) {
				int start = positionMap.getRealPosition(entry.getValue().start);
				int end = positionMap.getRealPosition(entry.getValue().getEnd());
				addExtra(start, end-start, entry.getValue().styles, removeOld);
			}
		}
	}
	
	public StyleSpans<Collection<String>> generateStyleSpans() {
		
		StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<Collection<String>>();
		
		int lastEnd = 0;
		
		for (StyleEntry entry : entries.values()) {
			
			// Add normal text if necessary
			if (lastEnd < entry.start) {
				builder.add(Collections.emptyList(), entry.start - lastEnd);
			}
			
			builder.add(entry.styles, entry.size);
			
			lastEnd = entry.start + entry.size;
		}
		
		// Add the remaining text
		builder.add(Collections.emptyList(), lastEnd > text.length() ? text.length() : text.length() - lastEnd);
		
		return builder.create();
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
}
