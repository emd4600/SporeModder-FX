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
package sporemodder.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import sporemodder.FileManager;
import sporemodder.view.ProjectTreeItem;

public class ProjectSearcher {
	private Project project;
	private final List<File> projectFolders = new ArrayList<File>();
	private boolean onlyModFiles;
	
	private final List<String> words = new ArrayList<String>();
	private byte[][] wordBytes;
	private byte[][] wordBytesUppercase;
	
	private final ReadOnlyBooleanWrapper isSearching = new ReadOnlyBooleanWrapper();
	
	public final boolean isSearching() {
		return isSearching.get();
	}
	
	public final ReadOnlyBooleanProperty isSearchingProperty() {
		return isSearching.getReadOnlyProperty();
	}
	
	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
		
		projectFolders.clear();
		projectFolders.add(project.getFolder());
		for (Project source : project.getSources()) {
			projectFolders.add(source.getFolder());
		}
	}
	
	public boolean isOnlyModFiles() {
		return onlyModFiles;
	}
	
	public void setOnlyModFiles(boolean onlyModFiles) {
		this.onlyModFiles = onlyModFiles;
	}
	
//	/**
//	 * Returns true if the name contains all of the searched words, false otherwise.
//	 * @param name
//	 * @return
//	 */
//	private boolean searchInName(String name) {
//		String lowercaseName = name.toLowerCase();
//		for (String s : words) {
//			if (!lowercaseName.contains(s)) {
//				return false;
//			}
//		}
//		return true;
//	}
	
	private boolean searchInNameOptional(String name, boolean[] found) {
		boolean totalMatch = true;
		String lowercaseName = name.toLowerCase();
		for (int i = 0; i < wordBytes.length; ++i) {
			found[i] = lowercaseName.contains(words.get(i));
			totalMatch = totalMatch && found[i];
		}
		return totalMatch;
	}
	
	/**
	 * Searches a certain word inside the byte array.
	 * @param data
	 * @param wordIndex
	 * @return
	 */
	private boolean searchInData(byte[] data, int wordIndex) {
		for (int i = 0; i < data.length; i++) {
			if (i + wordBytes[wordIndex].length > data.length) return false;
			if (data[i] == wordBytes[wordIndex][0] || data[i] == wordBytesUppercase[wordIndex][0]) {
				int j = 1;
				while (j < wordBytes[wordIndex].length && 
						(data[i+j] == wordBytes[wordIndex][j] || data[i+j] == wordBytesUppercase[wordIndex][j])) {
					++j;
				}
				if (j == wordBytes[wordIndex].length) return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if the file contains all the searched words, false otherwise. The file is assumed to exist and to have data.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private boolean searchInFile(File file, boolean[] alreadyFoundWords) throws IOException {
		byte[] data = Files.readAllBytes(file.toPath());
		for (int i = 0; i < wordBytes.length; ++i) {
			if ((alreadyFoundWords == null || !alreadyFoundWords[i]) && !searchInData(data, i)) {
				return false;
			}
		}
		return true;
	}
	
	public void setSearchedWords(List<String> words) {
		this.words.clear();
		for (String s : words) this.words.add(s.toLowerCase());
		
		wordBytes = new byte[words.size()][];
		wordBytesUppercase = new byte[words.size()][];
		for (int i = 0; i < wordBytes.length; ++i) {
			try {
				wordBytes[i] = words.get(i).getBytes("US-ASCII");
				wordBytesUppercase[i] = words.get(i).toUpperCase().getBytes("US-ASCII");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void startSearch(ProjectTreeItem item) {
		ForkJoinPool.commonPool().submit(new SearchTask(item));
	}
	
	private class SearchTask extends RecursiveAction {
		
		private final ProjectTreeItem item;
		
		public SearchTask(ProjectTreeItem item) {
			super();
			this.item = item;
		}

		@Override
		protected void compute() {
			Platform.runLater(() -> isSearching.set(true));
			long time = System.currentTimeMillis();
			
			ForkJoinPool.commonPool().invoke(new ItemSearchRecursive(item, item.getValue().isRoot(), null));
			
			time = System.currentTimeMillis() - time;

			Platform.runLater(() -> isSearching.set(false));
			
			System.out.println(time + " ms");
		}
	}
	
	private class ItemSearchRecursive extends RecursiveTask<Boolean> {
		
		private final boolean isRoot;
		private final ProjectTreeItem item;
		private final boolean[] foundWords;
		
		public ItemSearchRecursive(ProjectTreeItem item, boolean isRoot, boolean[] foundWords) {
			super();
			this.isRoot = isRoot;
			this.item = item;
			this.foundWords = new boolean[wordBytes.length];
			if (foundWords != null) {
				System.arraycopy(foundWords, 0, this.foundWords, 0, foundWords.length);
			}
		}
		
		@Override protected Boolean compute() {
			boolean matches = !isRoot && searchInNameOptional(item.getValue().name, foundWords);
			File file = item.getValue().getFile();
			
			if (matches) {
				// If the name matches, all its children must be shown (and we don't need to search in them)
				item.propagateMatchesSearch(matches);
			}
			else {
				if (!isRoot && file.isFile()) {
					if (FileManager.get().isSearchable(file.getName())) {
						try {
							matches = searchInFile(file, foundWords);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else if (item.isLoaded()) {
					// We must search in all children
					List<ItemSearchRecursive> tasks = new ArrayList<ItemSearchRecursive>();
					for (ProjectTreeItem child : item.getInternalChildren()) {
						tasks.add(new ItemSearchRecursive(child, false, foundWords));
					}
					
					// matches will be true if any of its children matched
					matches = ForkJoinTask.invokeAll(tasks).stream().map(ForkJoinTask::join).anyMatch(b -> b == null ? false : b.booleanValue());
				}
				else {
					// The name does not match, so search in its children
					// BUT the children are not loaded, so we must do it on the files directly
					
					final AtomicBoolean anyMatch = new AtomicBoolean(false);
					new FileSearchRecursive(item.getValue().getRelativePath(), null, null, anyMatch).invoke();
					matches = anyMatch.get();
				}
			}
			
			item.setMatchesSearch(matches);
			
			return matches;
		}
	}
	
	// The "return value" is an AtomicBoolean instead, because we might want to stop the search before
	private class FileSearchRecursive extends RecursiveAction {
		
		private final String relativePath;
		
		// Only if it's not folder
		private final File file;
		private final boolean[] foundWords;
		
		// If true, a match has been found, and so the search must stop
		private final AtomicBoolean searchFinished;
		
		public FileSearchRecursive(String relativePath, File file, boolean[] foundWords, AtomicBoolean searchFinished) {
			super();
			this.relativePath = relativePath;
			this.file = file;
			this.foundWords = new boolean[wordBytes.length];
			if (foundWords != null) {
				System.arraycopy(foundWords, 0, this.foundWords, 0, foundWords.length);
			}
			this.searchFinished = searchFinished;
		}
		
		@Override protected void compute() {
			if (searchFinished.get()) return;
			
			if (file != null) {
				try {
					if (FileManager.get().isSearchable(file.getName()) && searchInFile(file, foundWords)) {
						searchFinished.set(true);  // stop searching, we've found a match
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			else {
				List<FileSearchRecursive> tasks = new ArrayList<FileSearchRecursive>();
				Set<String> usedNames = new HashSet<String>();
				
				for (File projectFolder : projectFolders) {
					File folder = new File(projectFolder, relativePath);
					if (folder.exists()) {
						String[] names = folder.list();
						
						for (String name : names) {
							if (!usedNames.contains(name)) {
									
								
								// For multiple searched words, some might be in the name and others in the file contents
								if (searchInNameOptional(name, foundWords)) {
									searchFinished.set(true);  // stop searching, we've found a match
									return;
								}
								
								usedNames.add(name);
								File file = new File(folder, name);
								if (file.isFile()) {
									tasks.add(new FileSearchRecursive(null, file, foundWords, searchFinished));
								}
								else {
									tasks.add(new FileSearchRecursive(relativePath + "\\" + name, null, foundWords, searchFinished));
								}
							}
						}
					}
				}
				
				ForkJoinTask.invokeAll(tasks);
			}
		}
		
	}
}
