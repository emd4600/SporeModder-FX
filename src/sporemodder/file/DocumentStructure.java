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

package sporemodder.file;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to keep track of the multiple fragments that make up a document. It allows nesting of fragments.
 * <p>
 * Each fragment consists of a start and end position, and a text that describes it.
 */
public class DocumentStructure {
	
	@FunctionalInterface
	/**
	 * A method used to generate descriptions for document fragments. The method
	 * receives the current fragment, the text representation of the fragment, and an object which depends on what is using it.
	 */
	public static interface StructureNameFactory<T> {
		public String createName(DocumentFragment fragment, String text, T object);
	}
	
	private String text;
	private final DocumentFragment rootFragment = new DocumentFragment(this);
	private final List<DocumentFragment> fragments = new ArrayList<DocumentFragment>();
	
	public DocumentStructure(String text) {
		this(null, text);
	}
	
	public DocumentStructure(String description, String text) {
		this.text = text;
		
		rootFragment.setDescription(description);
	}
	
	/**
	 * Returns the text this structure represents. The fragments in the structure will use this text.
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets the text this structure represents. The fragments in the structure will use this text.
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Returns the fragments that make up this structure, which are expected to be in order of position.
	 * @return
	 */
	public List<DocumentFragment> getFragments() {
		return fragments;
	}
	
	/**
	 * Returns the root fragment of the structure. This fragment does not have parent and it does not need to use any start/end positions,
	 * it is mostly used for descriptions. All the fragments added directly to the structure will use this as their parent.
	 * @return
	 */
	public DocumentFragment getRootFragment() {
		return rootFragment;
	}
	
	/**
	 * Adds a fragment into this structure. The fragment will be added at the end of the list, so its positions are
	 * expected to come after the rest of fragments in this structure.
	 * @param fragment
	 */
	public void add(DocumentFragment fragment) {
		fragments.add(fragment);
	}
	
	private void findFragment(int position, DocumentFragment parent, List<DocumentFragment> list) {
		DocumentFragment closest = null;
		for (DocumentFragment child : parent.getChildren()) {
			if (child.contains(position)) {
				closest = child;
				break;
			}
		}
		
		if (closest != null) {
			list.add(closest);
			findFragment(position, closest, list);
		}
		// If no children has been close, we're finished
	}
	
	/**
	 * Finds the most precise fragment where this position is contained. This means that when a fragment is
	 * found that contains this position, its children are also checked to see if it can get more precise.
	 * A list is returned with all the hierarchy of fragments that contains the position, with the last element being the most precise.
	 * If no fragment contains the position, this method returns null.
	 * @param position
	 * @return A list with the hierarchy of fragments that contain the position, or null if no fragment contains it.
	 */
	public List<DocumentFragment> getFragment(int position) {
		
		List<DocumentFragment> list = new ArrayList<DocumentFragment>();
		DocumentFragment closest = null;
		
		for (DocumentFragment fragment : fragments) {
			if (fragment.contains(position)) {
				closest = fragment;
				break;
			}
		}
		
		if (closest == null) return null;
		
		list.add(closest);
		findFragment(position, closest, list);
		
		return list;
	}
	
	/**
	 * Removes the given fragment from its parent, adapting the positions of the rest of fragments.
	 * @param fragment
	 */
	public void removeFragment(DocumentFragment fragment) {
		if (fragment.getParent().isRoot()) {
			fragments.remove(fragment);
		}
		else {
			fragment.getParent().removeRaw(fragment);
		}
		
		int removedChars = fragment.length();
		
		// Now adapt all the others; only the ones that came after 
		for (DocumentFragment child : fragments) {
			fixRemovedFragment(child, fragment, removedChars);
		}
	}
	
	/**
	 * Changes the length of the given fragment, adapting the positions of the rest of fragments.
	 * @param fragment
	 * @param newLength
	 */
	public void setLength(DocumentFragment fragment, int newLength) {
		// First adapt all the others; only the ones that came after 
		for (DocumentFragment child : fragments) {
			fixRemovedFragment(child, fragment, fragment.length() - newLength);
		}
		
		fragment.setEnd(fragment.getStart() + newLength);
	}
	
	private void fixRemovedFragment(DocumentFragment fragment, DocumentFragment removedFragment, int removedChars) {
		
		if (removedFragment.getEnd() < fragment.getEnd()) {
			if (fragment.getStart() < removedFragment.getStart()) {
				// If child does not come after, but might contain fragments affected
				
				// First, fix the child end (we suppose the entire fragment is contained inside
				fragment.setEnd(fragment.getEnd() - removedChars);
				
			}
			else {
				// The child comes after the deleted fragment, so adapt the position
				fragment.setStart(fragment.getStart() - removedChars);
				fragment.setEnd(fragment.getEnd() - removedChars);
			}
			
			if (removedFragment.getEnd() <= fragment.getEditPosition()) {
				fragment.setEditPosition(fragment.getEditPosition() - removedChars);
			}
			
			// Now recursively fix the children
			for (DocumentFragment child : fragment.getChildren()) {
				fixRemovedFragment(child, removedFragment, removedChars);
			}
		}
	}
	
	/**
	 * Inserts a fragment to the specified parent, at the given list index. The rest of fragments will be adapted.
	 * @param fragment
	 * @param parent
	 */
	public void insertFragment(DocumentFragment parent, DocumentFragment fragment, int index) {
		
		// First adapt all the others; only the ones that came after 
		for (DocumentFragment child : fragments) {
			fixInsertedFragment(child, fragment);
		}
		//TODO Adapt the parent as well ?
		
		
		if (parent == null) {
			add(fragment);
		}
		else {
			parent.addRaw(fragment, index);
		}
	}
	
	/**
	 * Inserts a fragment to the specified parent, at the end of its list. The rest of fragments will be adapted.
	 * @param fragment
	 * @param parent
	 */
	public void insertFragment(DocumentFragment fragment, DocumentFragment parent) {
		
		// First adapt all the others; only the ones that came after 
		for (DocumentFragment child : fragments) {
			fixInsertedFragment(child, fragment);
		}
		//TODO Adapt the parent as well
		
		
		if (parent == null) {
			add(fragment);
		}
		else {
			parent.addRaw(fragment);
		}
	}
	
	private void fixInsertedFragment(DocumentFragment fragment, DocumentFragment insertedFragment) {
		int insertedChars = insertedFragment.length();
		
		if (insertedFragment.getStart() < fragment.getEnd()) {
			if (fragment.getStart() < insertedFragment.getStart()) {
				// If child does not come after, but might contain fragments affected
				
				// First, fix the child end (we suppose the entire fragment is contained inside)
				fragment.setEnd(fragment.getEnd() + insertedChars);
				
			}
			else {
				// The child comes after the deleted fragment, so adapt the position
				fragment.setStart(fragment.getStart() + insertedChars);
				fragment.setEnd(fragment.getEnd() + insertedChars);
			}
			
			if (insertedFragment.getStart() <= fragment.getEditPosition()) {
				fragment.setEditPosition(fragment.getEditPosition() + insertedChars);
			}
			
			// Now recursively fix the children
			for (DocumentFragment child : fragment.getChildren()) {
				fixInsertedFragment(child, insertedFragment);
			}
		}
	}
}
