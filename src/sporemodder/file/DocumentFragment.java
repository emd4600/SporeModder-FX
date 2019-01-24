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
package sporemodder.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DocumentFragment {
	private int start;
	private int end;
	private String description;
	private int editPosition = -1;
	private final List<DocumentFragment> children = new ArrayList<DocumentFragment>();
	private DocumentFragment parent;
	private DocumentStructure structure;
	
	/**
	 * Creates a new document fragment for the given structure, with no parent (that is, it is considered the root fragment).
	 * @param structure
	 */
	public DocumentFragment(DocumentStructure structure) {
		this.structure = structure;
	}
	
	/**
	 * Creates a new document fragment for the given structure, with the specified parent.
	 * @param structure
	 */
	public DocumentFragment(DocumentStructure structure, DocumentFragment parent) {
		this.structure = structure;
		this.parent = parent;
	}
	
	/**
	 * Returns the start position of this fragment.
	 * @return
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the start position of this fragment.
	 * @param start
	 */
	public void setStart(int start) {
		this.start = start;
	}
	
	/**
	 * A special position only used in certain cases, -1 by default. This is used to separate the fragment in two parts.
	 * @return
	 */
	public int getEditPosition() {
		return editPosition;
	}
	
	/**
	 * A special position only used in certain cases, -1 by default. This is used to separate the fragment in two parts.
	 * @param editPosition
	 */
	public void setEditPosition(int editPosition) {
		this.editPosition = editPosition;
	}

	/**
	 * Returns the end position of this fragment.
	 * @return
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the end position of this fragment.
	 * @param end
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Returns the description, which is the String used to describe this fragment. 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description, which is the String used to describe this fragment. 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
	
	/**
	 * Adds the given fragment as a child of this one. The child will be added at the end of the children list,
	 * and therefore its start/end positions are expected to be after the rest of children of this fragment, and inside the fragment itself.
	 * <p>
	 * This will not modify any other fragments. If you want to adapt other fragments' positions, use 
	 * {@link DocumentStructure.insertFragment(DocumentFragment, DocumentFragment)} instead.
	 * <p>
	 * This fragment will be assigned as the parent of the given child.
	 * @param child
	 */
	public void addRaw(DocumentFragment child) {
		children.add(child);
		child.parent = this;
	}
	
	/**
	 * Adds the given fragment as a child of this one. The child will be added at the end of the children list,
	 * and therefore its start/end positions are expected to be after the rest of children of this fragment, and inside the fragment itself.
	 * <p>
	 * This will not modify any other fragments. If you want to adapt other fragments' positions, use 
	 * {@link DocumentStructure.insertFragment(DocumentFragment, DocumentFragment)} instead.
	 * <p>
	 * This fragment will be assigned as the parent of the given child.
	 * @param child
	 * @param index
	 */
	public void addRaw(DocumentFragment child, int index) {
		children.add(index, child);
		child.parent = this;
	}
	
	/**
	 * Removes the given fragment from the list of children of this one. The child fragment won't have any parent afterwards.
	 * <p>
	 * This will not modify any other fragments. If you want to adapt other fragments' positions, use 
	 * {@link DocumentStructure.removeFragment(DocumentFragment, DocumentFragment)} instead.
	 * @param child
	 */
	public void removeRaw(DocumentFragment child) {
		children.remove(child);
		child.parent = null;
	}
	
	/** 
	 * Returns the structure this fragment belongs to.
	 * @return
	 */
	public DocumentStructure getStructure() {
		return structure;
	}
	
	/**
	 * Returns the list with the children of this fragment. This should be used only for reading and the list
	 * should not be modified.
	 * @return
	 */
	public List<DocumentFragment> getChildren() {
		return children;
	}
	
	/**
	 * Returns the fragment that acts as the parent of this one. The current fragment will be in the children list of its parent.
	 * @return
	 */
	public DocumentFragment getParent() {
		return parent;
	}
	
	/**
	 * Returns a list with all the parents of this fragment. The closest parent is the last item in the list.
	 * The root fragment is not returned. If this is the root fragment, returns an empty list.
	 * @param fragment
	 * @return
	 */
	public List<DocumentFragment> getParentsList() {
		
		List<DocumentFragment> parents = new ArrayList<DocumentFragment>();
		
		DocumentFragment fragment = parent;
		
		// Don't add the root fragment
		while (fragment != null && fragment.parent != null) {
			parents.add(fragment);
			fragment = fragment.parent;
		}
		
		Collections.reverse(parents);
		
		return parents;
	}
	
	/**
	 * Tells whether this fragment is a root fragment of the structure. This is effectively the same as <code>parent == null</code>.
	 * @return
	 */
	public boolean isRoot() {
		return parent != null;
	}
	
	/**
	 * Returns the depth level, that is, the number of parents. The structure root is at level 0.
	 * @return
	 */
	public int getLevel() {
		return getParentsList().size();
	}
	
	/**
	 * Returns the text length of this fragment, which is just the difference between the end and start positions.
	 * @return
	 */
	public int length() {
		return end - start;
	}
	
	/**
	 * Tells whether the given text position is contained within this fragment.
	 * @param position
	 * @return
	 */
	public boolean contains(int position) {
		return start <= position && position <= end;
	}
	
	/**
	 * Returns the text that this fragment represents. 
	 * <p>
	 * This is not saved in the fragment object, so it might change over time.
	 * Instead, it applies the start and end positions to the current {@link DocumentStructure} text.
	 * @return
	 */
	public String getText() {
		return structure.getText().substring(start, end);
	}
	
	/**
	 * Returns the text contained between the start position and the edit position. If no edit position is used,
	 * it returns null.
	 * <p>
	 * This is not saved in the fragment object, so it might change over time.
	 * Instead, it applies the start and edit positions to the current {@link DocumentStructure} text.
	 * @return
	 */
	public String getBeforeEditText() {
		if (editPosition != -1) {
			return structure.getText().substring(start, editPosition);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the text contained between the edit position and the end position. If no edit position is used,
	 * it returns null.
	 * <p>
	 * This is not saved in the fragment object, so it might change over time.
	 * Instead, it applies the edit and end positions to the current {@link DocumentStructure} text.
	 * @return
	 */
	public String getAfterEditText() {
		if (editPosition != -1) {
			return structure.getText().substring(editPosition, end);
		} else {
			return null;
		}
	}
}
