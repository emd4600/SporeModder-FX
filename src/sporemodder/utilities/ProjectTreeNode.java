package sporemodder.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import sporemodder.MainApp;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.UIProjectPanel;
import sporemodder.util.ProjectItem;

public class ProjectTreeNode extends DefaultMutableTreeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2819976856922041517L;
	
	public String name;
	public boolean isSource;
	public boolean isMod;
	//public boolean isVisible = true;
	public boolean isMatch = true;
//	private boolean matchesSearch = true;
	public boolean isRoot = false;
	
	public ProjectTreeNode() {
		super();
	}

	
	public ProjectTreeNode(String name, boolean isRoot) {
		super();
		this.name = name;
		this.isRoot = isRoot;
	}
	
	public ProjectTreeNode(ProjectTreeNode node) {
		super(node);
		this.name = node.name;
		this.isSource = node.isSource;
		this.isMod = node.isMod;
		this.isMatch = node.isMatch;
		this.isRoot = node.isRoot;
	}
	
	public ProjectTreeNode(ProjectItem smfxItem) {
		super();
		this.isRoot = smfxItem.isRoot();
		this.isSource = smfxItem.isSource();
		this.isMod = smfxItem.isMod();
		this.name = smfxItem.getName();
	}
	
	@SuppressWarnings("unchecked")
	public void copyChildren(ProjectTreeNode node) {
		if (node.children != null) {
			children = new Vector<ProjectTreeNode>(node.children);
		}
	}
	
	public boolean searchInName(List<SearchSpec> searchSpecs) {
		isMatch = true;
		String lowercaseName = name.toLowerCase();
		if (searchSpecs != null) {
			for (SearchSpec s : searchSpecs) {
				if (!lowercaseName.contains(s.getLowercaseString())) {
					isMatch = false;
					break;
				}
			}
		}
		return isMatch;
	}


	public TreeNode getChildAt(int index, boolean filterMod, boolean filterSearch) {
		
		if (!filterMod && !filterSearch) {
			return super.getChildAt(index);
		}
		
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("Node has no children");
		}
		
		@SuppressWarnings("rawtypes")
		Enumeration e = children.elements();
		
		int realIndex = -1;
		int visibleIndex = -1;
		
		while (e.hasMoreElements()) {
			ProjectTreeNode node = (ProjectTreeNode) e.nextElement();
			
			boolean result = true;
			if (filterMod) {
				if (!node.isMod) result = false;
			}
			if (filterSearch) {
				if (!node.isMatch) result = false;
			}
			
			if (result) {
				visibleIndex++;
			}
			realIndex++;
			if (visibleIndex == index) {
				return (TreeNode) children.elementAt(realIndex);
			}
		}
		
		throw new ArrayIndexOutOfBoundsException("Index unmatched");
	}
	
	public int getChildCount(boolean filterMod, boolean filterSearch) {
		if (!filterMod && !filterSearch) {
			return super.getChildCount();
		}
		if (children == null) {
			return 0;
		}
		
		int count = 0;
		@SuppressWarnings("rawtypes")
		Enumeration e = children.elements();
		
		while (e.hasMoreElements()) {
			ProjectTreeNode node = (ProjectTreeNode) e.nextElement();
			boolean result = true;
			if (filterMod) {
				if (!node.isMod) result = false;
			}
			if (filterSearch) {
				if (!node.isMatch) result = false;
			}
			
			if (result) {
				count++;
			}
		}
		
		return count;
	}
	
 	public int getIndex(TreeNode aChild, boolean filterMod, boolean filterSearch) {
 		if (!filterMod && !filterSearch) {
 			return super.getIndex(aChild);
 		}
 		if (children == null) {
 			return -1;
 		}
 		
 		int visibleIndex = 0;
 		@SuppressWarnings("rawtypes")
		Enumeration e = children.elements();
 		
 		while (e.hasMoreElements()) {
 			ProjectTreeNode node = (ProjectTreeNode) e.nextElement();
 			boolean result = true;
			if (filterMod) {
				if (!node.isMod) result = false;
			}
			if (filterSearch) {
				if (!node.isMatch) result = false;
			}
 			if (result) {
 				if (node == aChild) {
 	 				return visibleIndex;
 	 			}
 	 			visibleIndex++;
 			}
 		}
 		
 		return -1;
 	}
 	
 	public void remove(int childIndex, boolean filterMod, boolean filterSearch) {
 		if (!filterMod && !filterSearch) {
 			super.remove(childIndex);
 			return;
 		}
 		ProjectTreeNode child = (ProjectTreeNode)getChildAt(childIndex, filterMod, filterSearch);
 		int realIndex = super.getIndex(child);
 		children.removeElementAt(realIndex);
 		child.setParent(null);
 	}
 	
 	// Nodes are ordered alphabetically. 
  	public int getChildIndex(String name) {
  		if (children == null) return -1;
  		int offset = 0;
  		int count = children.size();
  		while (count > 0) {
  			int index = count / 2;
  			ProjectTreeNode n = (ProjectTreeNode) children.get(offset + index);
  			int compare = n.name.compareToIgnoreCase(name);
  			// name goes before this node
  			if (compare > 0) {
  				count = index;
  			}
  			// name goes after this node
  			else if (compare < 0) {
  				offset += index + 1;
  				count = count - index - 1;
  			}
  			else {
  				// this node has the same name as the new one, so put it here
  				return offset + index;
  			}
  		}
  		
  		return -1;
  	}
 	
 	// Nodes are ordered alphabetically. This returns the corresponding index
 	protected int getNextChildIndex(String name) {
 		if (children == null) return 0;
 		int offset = 0;
 		int count = children.size();
 		while (count > 0) {
 			int index = count / 2;
 			ProjectTreeNode n = (ProjectTreeNode) children.get(offset + index);
 			int compare = n.name.compareToIgnoreCase(name);
 			// name goes before this node
 			if (compare > 0) {
 				count = index;
 			}
 			// name goes after this node
 			else if (compare < 0) {
 				offset += index + 1;
 				count = count - index - 1;
 			}
 			else {
 				// this node has the same name as the new one, so put it here
 				break;
 			}
 		}
 		
 		return offset;
 	}
 	
 	private static String[] generateRandomWords(int numberOfWords)
 	{
 	    String[] randomStrings = new String[numberOfWords];
 	    Random random = new Random();
 	    for(int i = 0; i < numberOfWords; i++)
 	    {
 	        char[] word = new char[random.nextInt(8)+3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
 	        for(int j = 0; j < word.length; j++)
 	        {
 	            word[j] = (char)('a' + random.nextInt(26));
 	        }
 	        randomStrings[i] = new String(word);
 	    }
 	    return randomStrings;
 	}
 	private static void insertOld(ProjectTreeModel treeModel, ProjectTreeNode rootNode, ProjectTreeNode newNodeObject) {
		int ind = 0;
		@SuppressWarnings("rawtypes")
		Enumeration e = rootNode.children();
		while (e.hasMoreElements()) {
			ProjectTreeNode node = (ProjectTreeNode) e.nextElement();
//			System.out.println(newNodeObject.name + "\t" + node.name + "\t" + newNodeObject.name.compareTo(node.name) + "\t" + ind);
			// without ignore case it doesn't work for some reason
			if (newNodeObject.name.compareToIgnoreCase(node.name) < 0) {
				break;
			}
			ind++;
		}
		
		treeModel.insertNodeInto(newNodeObject, rootNode, ind);
 	}
 	
 	private static final int WORD_COUNT = 10000;
 	private static final int TEST_COUNT = 5;
 	private static final List<Long> TIMES_OLD = new ArrayList<Long>();
 	private static final List<Long> TIMES_NEW = new ArrayList<Long>();
 	private static void test() {
 		ProjectTreeNode root = new ProjectTreeNode("rootNode", true);
 		ProjectTreeModel model = new ProjectTreeModel(root);
 		String[] words = generateRandomWords(WORD_COUNT);
 		
 		long time1 = System.currentTimeMillis();
 		for (String word : words) {
 			insertOld(model, root, new ProjectTreeNode(word, false));
 		}
 		time1 = System.currentTimeMillis() - time1;
 		TIMES_OLD.add(time1);
 		
 		
 		long time2 = System.currentTimeMillis();
 		for (String word : words) {
 			model.insertNode(new ProjectTreeNode(word, false), root);
 		}
 		time2 = System.currentTimeMillis() - time2;
 		TIMES_NEW.add(time2);
 		
 		System.out.println("Time old: " + time1);
 		System.out.println("Time new: " + time2);
 	}
 	
 	public static void main(String[] args) {
 		for (int i = 0; i < TEST_COUNT; i++) {
 			test();
 		}
 		
 		long average = 0;
 		for (long l : TIMES_OLD) average += l;
 		double averageOld = average / (double)TEST_COUNT;
 		System.out.println("Average old: " + averageOld);
 		
 		average = 0;
 		for (long l : TIMES_NEW) average += l;
 		double averageNew = average / (double)TEST_COUNT;
 		System.out.println("Average new: " + averageNew);
 		
 		System.out.println("New is " + averageOld / averageNew + " times faster.");
 	}
 	
 	
 	
 	private boolean searchInFile(String text) throws FileNotFoundException {
 		//TODO is there any fastest method?
 		
 		String relativePath = Project.getRelativePath(getPath());
 		
 		Project project = MainApp.getCurrentProject();
 		File file = null;
 		if (isMod) {
 			file = project.getModFile(relativePath);
 		}
 		else {
 			file = project.getSourceFile(relativePath);
 		}
 		if (file.exists() && file.isFile()) {
 			try (Scanner scanner = new Scanner(file)) {
 				while (scanner.hasNextLine()) {
 	 				if (scanner.nextLine().contains(text)) {
 	 					return true;
 	 				}
 	 			}
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean searchInFile(List<String> strings) throws FileNotFoundException {
 		//TODO is there any fastest method?
 		
 		String relativePath = Project.getRelativePath(getPath());
 		
 		Project project = MainApp.getCurrentProject();
 		File file = null;
 		if (isMod) {
 			file = project.getModFile(relativePath);
 		}
 		else {
 			file = project.getSourceFile(relativePath);
 		}
 		if (file.exists() && file.isFile()) {
 			boolean[] foundStrings = new boolean[strings.size()]; 
 			
 			try (Scanner scanner = new Scanner(file)) {
 				while (scanner.hasNextLine()) {
 					String line = scanner.nextLine().toLowerCase();
 					for (int i = 0; i < foundStrings.length; i++) {
 						if (!foundStrings[i]) {
 							if (line.contains(strings.get(i))) {
 	 							foundStrings[i] = true;

 	 							boolean result = true;
 	 							for (int j = 0; j < foundStrings.length; j++) result &= foundStrings[j];
 	 							if (result) {
 	 								return true;
 	 							}
 	 	 	 				}
 						}
 					}
 	 			}
 			}
 		}
 		
 		return false;
 	}
 	
// 	public boolean matchesCriteria(String text) {
// 		//TODO advanced criteria ? 
// 		matchesSearch = name.toLowerCase().contains(text);
// 		
// 		if (!matchesSearch && isLeaf()) {
// 			String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
// 			
// 			if (MainApp.getSearchableExtensions().contains(extension)) {
// 				try {
//					matchesSearch = searchInFile(text);
//				} catch (FileNotFoundException e) {
//					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error searching in file " + name + ": " + 
//							ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
//				}
// 			}
// 		}
// 		
// 		return matchesSearch;
// 	}
// 	
//// 	public boolean matchedSearch() {
//// 		return matchesSearch;
//// 	}
//
//
	@Override
	public String toString() {
//		return "ProjectTreeNode [name=" + name + ", isSource=" + isSource
//				+ ", isMod=" + isMod + ", isRoot=" + isRoot + "]";
		
		return name;
	}
// 	
// 	public boolean checkCriteria(String text, boolean modOnly) {
// 		//TODO advanced criteria ? 
// 		matchesSearch = name.toLowerCase().contains(text);
// 		
// 		
// 	}
 	
 	
// 	public boolean search(String text, boolean modOnly) {
// 		
// 		boolean matchesSearch = false;
// 		
// 		if (!isRoot() && modOnly && !isMod) return false;
// 		
// 		//TODO advanced criteria ? 
// 		
// 		// we don't want the Project root node to be searched
// 		if (getParent() == null) {
// 			matchesSearch = false;
// 		}
// 		else {
// 			matchesSearch = name.toLowerCase().contains(text);
// 		}
// 		
// 		if (!matchesSearch) {
// 			// name doesn't match, keep searching
// 			if (isLeaf()) {
//	 			String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
//	 			
//	 			if (MainApp.getSearchableExtensions().contains(extension)) {
//	 				try {
//						boolean result = searchInFile(text);
//						// this leaf file matches the search, so make all parent nodes visible
//						isVisible = result;
//						return result;
////						if (result) {
////							isVisible = true;
//////							UIProjectPanel.setParentNodesVisible(this);
////							return true;
////						}
//					} 
//	 				catch (FileNotFoundException e) {
//						JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error searching in file " + name + ": " + 
//								ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
//					}
//	 			}
// 			}
// 			else {
// 				int childCount = super.getChildCount();
// 				boolean result = false;
// 				for (int i = 0; i < childCount; i++) {
// 					ProjectTreeNode node = (ProjectTreeNode) super.getChildAt(i);
// 					result |= node.search(text, modOnly);
// 				}
// 				// this node's children contain the criteria
// 				isVisible = result;
//				return result;
////				if (result) {
////					isVisible = true;
////					return true;
//////						UIProjectPanel.setParentNodesVisible(this);
////				}
// 			}
// 		}
// 		else {
// 			isVisible = true;
// 			UIProjectPanel.setChildNodesVisible(this, modOnly);
//			return true;
// 		}
// 		
// 		return false;
// 	}
// 	
 	
 	// if !modifyTree, it will only return whether  there were occurrences or not
// 	public boolean search(List<String> strings, boolean hideSources, boolean dontSearchInMods, boolean searchOnlyVisible, boolean modifyTree) {
// 		
// 		boolean matchesSearch = false;
// 		
// 		if (!isRoot() && hideSources && !isMod) return false;
// 		if (searchOnlyVisible && !isVisible) return false;
// 		
// 		//TODO advanced criteria ? 
// 		
// 		// we don't want the Project root node to be searched
// 		if (getParent() == null) {
// 			matchesSearch = false;
// 		}
// 		else {
// 			boolean result = true;
//// 			for (String text : strings) {
//// 				result &= name.toLowerCase().contains(text);
//// 			}
// 			String lowercaseName = name.toLowerCase();
// 			for (String text : strings) {
// 				if (!lowercaseName.contains(text)) {
// 					result = false;
// 					break;
// 				}
// 			}
// 			matchesSearch = result;
// 		}
// 		
// 		if (!matchesSearch) {
// 			// name doesn't match, keep searching
// 			if (isLeaf()) {
// 				if (!dontSearchInMods || (dontSearchInMods && !isMod)) {
// 					String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
// 		 			
// 		 			if (MainApp.getSearchableExtensions().contains(extension)) {
// 		 				try {
// 							boolean result = searchInFile(strings);
// 							// this leaf file matches the search, so make all parent nodes visible
// 							if (modifyTree) isVisible = result;
// 							return result;
// 						} 
// 		 				catch (FileNotFoundException e) {
// 							JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error searching in file " + name + ": " + 
// 									ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
// 						}
// 		 			}
// 		 			else {
// 		 				if (modifyTree) isVisible = false;
//						return false;
// 		 			}
// 				}
// 			}
// 			else {
// 				int childCount = super.getChildCount();
// 				boolean result = false;
// 				for (int i = 0; i < childCount; i++) {
// 					ProjectTreeNode node = (ProjectTreeNode) super.getChildAt(i);
// 					result |= node.search(strings, hideSources, dontSearchInMods, searchOnlyVisible, modifyTree);
// 					
// 					if (!modifyTree && result) return true;
// 				}
// 				// this node's children contain the criteria
// 				if (modifyTree) isVisible = result;
//				return result;
// 			}
// 		}
// 		else {
// 			if (modifyTree) {
//				isVisible = true;
//				UIProjectPanel.setChildNodesVisible(this, hideSources);
// 			}
//			return true;
// 		}
// 		
// 		return false;
// 	}
// 	
// 	/*  -- TEST --  */
// 	
// 	public boolean searchFast(List<SearchSpec> specs, boolean hideSources, boolean dontSearchInMods, boolean searchOnlyVisible, boolean modifyTree) {
// 		
// 		boolean matchesSearch = false;
// 		
// 		if (!isRoot() && hideSources && !isMod) return false;
// 		if (searchOnlyVisible && !isVisible) return false;
// 		
// 		//TODO advanced criteria ? 
// 		
// 		// we don't want the Project root node to be searched
// 		if (getParent() == null) {
// 			matchesSearch = false;
// 		}
// 		else {
// 			boolean result = true;
// 			String lowercaseName = name.toLowerCase();
// 			for (SearchSpec text : specs) {
// 				if (!lowercaseName.contains(text.getLowercaseString())) {
// 					result = false;
// 					break;
// 				}
// 			}
// 			matchesSearch = result;
// 		}
// 		
// 		if (!matchesSearch) {
// 			// name doesn't match, keep searching
// 			if (isLeaf()) {
// 				if (!dontSearchInMods || !isMod) {
// 					String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
// 		 			
// 		 			if (MainApp.getSearchableExtensions().contains(extension)) {
// 		 				try {
// 							boolean result = searchInFileFast(specs);
// 							// this leaf file matches the search, so make all parent nodes visible
// 							if (modifyTree) isVisible = result;
// 							return result;
// 						} 
// 		 				catch (Exception e) {
// 							JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error searching in file " + name + ": " + 
// 									ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
// 						}
// 		 			}
// 		 			else {
// 		 				if (modifyTree) isVisible = false;
//						return false;
// 		 			}
// 				}
// 			}
// 			else {
// 				int childCount = super.getChildCount();
// 				boolean result = false;
// 				for (int i = 0; i < childCount; i++) {
// 					ProjectTreeNode node = (ProjectTreeNode) super.getChildAt(i);
// 					result |= node.searchFast(specs, hideSources, dontSearchInMods, searchOnlyVisible, modifyTree);
// 					
// 					if (!modifyTree && result) return true;
// 				}
// 				// this node's children contain the criteria
// 				if (modifyTree) isVisible = result;
//				return result;
// 			}
// 		}
// 		else {
// 			if (modifyTree) {
//				isVisible = true;
//				UIProjectPanel.setChildNodesVisible(this, hideSources);
// 			}
//			return true;
// 		}
// 		
// 		return false;
// 	}
// 	
 	private boolean searchInFileFast(List<SearchSpec> specs) throws IOException {
 		//TODO is there any fastest method?
 		
 		String relativePath = Project.getRelativePath(getPath());
 		
 		Project project = MainApp.getCurrentProject();
 		File file = null;
 		if (isMod) {
 			file = project.getModFile(relativePath);
 		}
 		else {
 			file = project.getSourceFile(relativePath);
 		}
 		if (file.exists() && file.isFile()) {
 			byte[] data = Files.readAllBytes(file.toPath());
 			for (SearchSpec spec : specs) {
 				if (!spec.searchFast(data)) {
 					return false;
 				}
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void searchFast(List<SearchSpec> specs, boolean searchInFile) {
 		
 		isMatch = false;
 		
 		// we don't want the Project root node to be searched
 		if (isRoot()) {
 			// search children nodes
			int childCount = super.getChildCount();
			for (int i = 0; i < childCount; i++) {
				ProjectTreeNode node = (ProjectTreeNode) super.getChildAt(i);
				node.searchFast(specs, searchInFile);
			}
			return;
 		}
 		
 		searchInName(specs);
 		
 		if (!isMatch) {
 			// name doesn't match, keep searching
 			if (searchInFile && isLeaf()) {
				String extension = name.substring(name.lastIndexOf(".") + 1, name.length());
	 			
	 			if (MainApp.getSearchableExtensions().contains(extension)) {
	 				try {
						boolean result = searchInFileFast(specs);
						// this leaf file matches the search, so make all parent nodes visible
						isMatch = result;
						
						if (isMatch)
						{
							ProjectTreeNode parent = this;
							while ((parent = (ProjectTreeNode) parent.getParent()) != null)
							{
								parent.isMatch = true;
							}
						}
					} 
	 				catch (Exception e) {
						JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error searching in file " + name + ": " + 
								ErrorManager.getStackTraceString(e), "Error", JOptionPane.ERROR_MESSAGE);
					}
	 			}
 			}
 			else {
 				
 				// search children nodes
 				int childCount = super.getChildCount();
 				for (int i = 0; i < childCount; i++) {
 					ProjectTreeNode node = (ProjectTreeNode) super.getChildAt(i);
 					node.searchFast(specs, searchInFile);
 				}
 			}
 		}
 		else
 		{
 			// make all parent visible
 			ProjectTreeNode parent = this;
			while ((parent = (ProjectTreeNode) parent.getParent()) != null)
			{
				parent.isMatch = true;
			}
 			// make all children visible
 			int childCount = super.getChildCount();
			for (int i = 0; i < childCount; i++) {
				((ProjectTreeNode) super.getChildAt(i)).isMatch = true;
			}
 		}
 	}
}
