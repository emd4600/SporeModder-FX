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

package sporemodder.util.inspector;

import java.util.List;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sporemodder.file.DocumentFragment;
import sporemodder.file.DocumentStructure;
import sporemodder.view.DragResizer;
import sporemodder.view.DragResizer.DragSide;
import sporemodder.view.editors.TextEditor;

/**
 * This file contains all the information related to the inspector of a certain ArgScript format. Inspectors
 * are used to provide a user interface for editing the ArgScript file.
 */
public class InspectorUnit<T> {

	private TextEditor editor;
	private T data;
	private TreeView<DocumentFragment> structureTree;
	private VBox vbox;
	private boolean isWriting;
	
	public InspectorUnit(T data, TextEditor editor) {
		this.data = data;
		this.editor = editor;
	}
	
	public T getData() {
		return data;
	}
	
	public TextEditor getTextEditor() {
		return editor;
	}
	
	public DocumentStructure getDocumentStructure() {
		return structureTree.getRoot().getValue().getStructure();
	}
	
	public String getText(DocumentFragment fragment) {
		if (editor == null) return null;
		
		return editor.getCodeArea().getText(fragment.getStart(), fragment.getEnd());
	}
	
	public Pane generateUI(boolean showStructure) {
		
		BorderPane pane = new BorderPane();
		
		if (showStructure) {
			generateStructureUI(pane);
		}
		
		vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(5));
		
		ScrollPane scrollPane = new ScrollPane(vbox);
		scrollPane.setFitToHeight(false);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		pane.setCenter(scrollPane);
		
		return pane;
	}
	
	public VBox getVBox() {
		return vbox;
	}
	
	public void add(InspectorElement element) {
		element.generateUI(vbox);
	}
	
	public void add(Node node) {
		vbox.getChildren().add(node);
	}
	
	public void setWriting(boolean isWriting) {
		this.isWriting = isWriting;
	}
	
	public boolean isWriting() {
		return isWriting;
	}
	
	public void setSelectedFragment(DocumentFragment fragment) {
		setSelectedFragment(DocumentStructure.getParentsList(fragment));
	}
	
	public void setSelectedFragment(List<DocumentFragment> list) {
		TreeItem<DocumentFragment> root = structureTree.getRoot();
		
		for (DocumentFragment fragment : list) {
			for (TreeItem<DocumentFragment> item : root.getChildren()) {
				if (item.getValue() == fragment) {
					root = item;
					break;
				}
			}
		}
		
		structureTree.getSelectionModel().select(root); 
		structureTree.scrollTo(structureTree.getRow(root));
	}
	
	public boolean removeFragment(DocumentFragment fragment) {
		return removeFragment(DocumentStructure.getParentsList(fragment));
	}
	
	public boolean removeFragment(List<DocumentFragment> list) {
		TreeItem<DocumentFragment> root = structureTree.getRoot();
		
		for (DocumentFragment fragment : list) {
			for (TreeItem<DocumentFragment> item : root.getChildren()) {
				if (item.getValue() == fragment) {
					root = item;
					break;
				}
			}
		}
		
		if (root.getValue() != list.get(list.size() - 1)) {
			// If we haven't found the exact value, don't remove anything.
			return false;
		}
		
		root.getParent().getChildren().remove(root);
		
		root.getValue().getStructure().removeFragment(root.getValue());
		
		return true;
	}
	
	public boolean insertFragment(DocumentFragment insertedFragment, List<DocumentFragment> list) {
		TreeItem<DocumentFragment> root = structureTree.getRoot();
		
		for (DocumentFragment fragment : list) {
			for (TreeItem<DocumentFragment> item : root.getChildren()) {
				if (item.getValue() == fragment) {
					root = item;
					break;
				}
			}
		}
		
		if (root.getValue() != list.get(list.size() - 1)) {
			// If we haven't found the exact value, don't insert anything.
			return false;
		}
		
		root.getChildren().add(new TreeItem<DocumentFragment>(insertedFragment));
		
		root.getValue().getStructure().insertFragment(insertedFragment, root.getValue());
		
		return true;
	}
	
	public void updateStructureItem(DocumentFragment fragment) {
		TreeModificationEvent<DocumentFragment> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), getStructureItem(fragment));
        Event.fireEvent(getStructureItem(fragment), event);
	}
	
	public TreeItem<DocumentFragment> getStructureItem(DocumentFragment fragment) {
		return getStructureItem(structureTree.getRoot(), fragment);
	}
	
	public TreeItem<DocumentFragment> getStructureItem(TreeItem<DocumentFragment> root, DocumentFragment fragment) {
		if (root.getValue() == fragment) {
			return root;
		}
		
		for (TreeItem<DocumentFragment> item : root.getChildren()) {
			TreeItem<DocumentFragment> result = getStructureItem(item, fragment);
			
			if (result != null) {
				return result;
			}
		}
		
		return null;
	}
	
	public boolean insertFragment(DocumentFragment insertedFragment, DocumentFragment fragment) {
		return insertFragment(insertedFragment, DocumentStructure.getParentsList(fragment));
	}
	
	public void replaceText(DocumentFragment fragment, String text) {
		if (editor != null) {
			editor.getCodeArea().replaceText(fragment.getStart(), fragment.getEnd(), text);
		}
		
		fragment.getStructure().setLength(fragment, text.length());
		fragment.getStructure().setText(editor.getCodeArea().getText());
	}
	

	public void setDocumentStructure(DocumentStructure documentStructure) {
		documentStructure.getRootFragment().setDescription("Spore PFX");
		TreeItem<DocumentFragment> root = new TreeItem<DocumentFragment>(documentStructure.getRootFragment());
		
		for (DocumentFragment fragment : documentStructure.getFragments()) {
			TreeItem<DocumentFragment> item = new TreeItem<DocumentFragment>(fragment);
			addStructureFragmentRecursive(item);
			root.getChildren().add(item);
		}
		
		structureTree.setRoot(root);
	}
	
	private void addStructureFragmentRecursive(TreeItem<DocumentFragment> parent) {
		for (DocumentFragment fragment : parent.getValue().getChildren()) {
			TreeItem<DocumentFragment> item = new TreeItem<DocumentFragment>(fragment);
			addStructureFragmentRecursive(item);
			parent.getChildren().add(item);
		}
	}
	
	public TreeView<DocumentFragment> getStructureTree() {
		return structureTree;
	}
	
	private void generateStructureUI(BorderPane pane) {
		
		structureTree = new TreeView<DocumentFragment>();
		
		//structureTree.setShowRoot(false);
		
		pane.setTop(structureTree);
		
		// Set a limit to the height
		structureTree.setMaxHeight(250);
		DragResizer.makeResizable(structureTree, DragSide.BOTTOM);
	}

	public void clear() {
		vbox.getChildren().clear();
	}

}
