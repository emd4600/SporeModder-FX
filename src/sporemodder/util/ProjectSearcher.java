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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import sporemodder.FileManager;
import sporemodder.view.ProjectTreeItem;

public class ProjectSearcher {
	private static int TIME_TEST = 0;
	
	private int numFilesSearched;
	private int numItemsSearched;
	
	private Project project;
	private final List<File> projectFolders = new ArrayList<File>();
	private boolean onlyModFiles;
	/** If enabled, file contents will be searched. */
	private boolean isExtensiveSearch = true;
	
	private final List<String> words = new ArrayList<String>();
	private byte[][] wordBytes;
	private byte[][] wordBytesUppercase;
	
	// It must change immediately, not with Platform.runLater
	private boolean internalIsSearching;
	private final ReadOnlyBooleanWrapper isSearching = new ReadOnlyBooleanWrapper();
	private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
	private int progressMax;
	
	public final boolean isSearching() {
		return isSearching.get();
	}
	
	public final ReadOnlyBooleanProperty isSearchingProperty() {
		return isSearching.getReadOnlyProperty();
	}
	
	public void cancel() {
		internalIsSearching = false;
		if (Platform.isFxApplicationThread()) {
			isSearching.set(false);
		} else {
			Platform.runLater(() -> isSearching.set(false));
		}
		reset();
	}
	
	public final double getProgress() {
		return progress.get();
	}
	
	public final ReadOnlyDoubleProperty progressProperty() {
		return progress.getReadOnlyProperty();
	}
	
	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
		
		projectFolders.clear();
		projectFolders.add(project.getFolder());
		for (Project source : project.getReferences()) {
			projectFolders.add(source.getFolder());
		}
	}
	
	public boolean isOnlyModFiles() {
		return onlyModFiles;
	}
	
	public void setOnlyModFiles(boolean onlyModFiles) {
		this.onlyModFiles = onlyModFiles;
	}
	
	public boolean isExtensiveSearch() {
		return isExtensiveSearch;
	}
	
	public void setExtensiveSearch(boolean isExtensiveSearch) {
		this.isExtensiveSearch = isExtensiveSearch;
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
				if (j == wordBytes[wordIndex].length) {
					return true;
				}
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
	
	public void reset() {
		internalIsSearching = false;
		numFilesSearched = 0;
		numItemsSearched = 0;
		progress.set(0.0);
		TIME_TEST = 0;
	}
	
	public void startSearch(ProjectTreeItem item) {
		reset();
		progressMax = item.getInternalChildren().size();
		
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
			internalIsSearching = true;
			Platform.runLater(() -> isSearching.set(true));
			long time = System.currentTimeMillis();
			
			// The given item is expected to have its children loaded
//			List<ItemSearchRecursive> tasks = new ArrayList<ItemSearchRecursive>();
//			for (ProjectTreeItem child : item.getInternalChildren()) {
//				tasks.add(new ItemSearchRecursive(child, null, 1.0 / progressMax));
//			}
//			ForkJoinTask.invokeAll(tasks);
			new ItemSearchRecursive(item, null, 1.0 / progressMax, true).invoke();
			
			time = System.currentTimeMillis() - time;

			// If it wasn't cancelled
			if (internalIsSearching) Platform.runLater(() -> {
				isSearching.set(false);
				progress.set(1.0);
			});
			internalIsSearching = false;
			
			System.out.println("Files searched: " + numFilesSearched);
			System.out.println("Items searched: " + numItemsSearched);
			System.out.println(time + " ms");
			System.out.println("Time blocked in File.list(): " + TIME_TEST);
		}
	}
	
	private class ItemSearchRecursive extends RecursiveTask<Boolean> {
		
		private final ProjectTreeItem item;
		private final boolean[] foundWords;
		private final double progressIncrement;
		// Roots don't add increment, but pass the increment to their children
		private final boolean isSearchRoot;
		
		public ItemSearchRecursive(ProjectTreeItem item, boolean[] foundWords, double progressIncrement, boolean isSearchRoot) {
			super();
			this.item = item;
			this.foundWords = new boolean[wordBytes.length];
			this.progressIncrement = progressIncrement;
			this.isSearchRoot = isSearchRoot;
			if (foundWords != null) {
				System.arraycopy(foundWords, 0, this.foundWords, 0, foundWords.length);
			}
		}
		
		public ItemSearchRecursive(ProjectTreeItem item, boolean[] foundWords, double progressIncrement) {
			this(item, foundWords, progressIncrement, false);
		}
		
		@Override protected Boolean compute() {
			if (!internalIsSearching) return false;
			
			numItemsSearched++;
			//System.out.println("Items: " + numItemsSearched + "\t[" + item.getValue().getName() + "]");
			// We want to search in the name if the search root is not the project root
			boolean matches = (isSearchRoot && item.getValue().isRoot) ? false : searchInNameOptional(item.getValue().name, foundWords);
			File file = item.getValue().getFile();
			
			if (matches) {
				// If the name matches, all its children must be shown (and we don't need to search in them)
				item.propagateMatchesSearch(matches);
			}
			else {
				if (file != null && file.isFile()) {
					if (FileManager.get().isSearchable(file.getName()) && isExtensiveSearch) {
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
						tasks.add(new ItemSearchRecursive(child, foundWords, isSearchRoot ? progressIncrement : 0.0));
					}
					
					//System.out.println(tasks.size() + " tasks scheduled");
					
					// matches will be true if any of its children matched
					matches = ForkJoinTask.invokeAll(tasks).stream().map(ForkJoinTask::join).anyMatch(b -> b == null ? false : b.booleanValue());
				}
				else {
					// The name does not match, so search in its children
					// BUT the children are not loaded, so we must do it on the files directly
					
					//System.out.println("\tInvoking file search: " + item.getValue().getRelativePath());
					final AtomicBoolean anyMatch = new AtomicBoolean(false);
					new FileSearchRecursive(item.getValue().getRelativePath(), null, null, anyMatch).invoke();
					matches = anyMatch.get();
				}
			}
			
			if (!isSearchRoot && progressIncrement != 0.0) {
				progress.setValue(progress.getValue() + progressIncrement);
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
			if (!internalIsSearching) return;
			if (searchFinished.get()) return;
			
			if (file != null) {
				try {
					if (isExtensiveSearch && FileManager.get().isSearchable(file.getName()) && searchInFile(file, foundWords)) {
						searchFinished.set(true);  // stop searching, we've found a match
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			else {
				
				//List<FileSearchRecursive> tasks = new ArrayList<FileSearchRecursive>();
				
				//Set<String> usedNames = new HashSet<String>();
				Set<String> usedNames = Collections.synchronizedSet(new HashSet<String>());
				
				int numProjects = projectFolders.size();
				for (int i = 0; i < numProjects; ++i) 
				{
					if (searchFinished.get()) return;
					
					File folder = new File(projectFolders.get(i), relativePath);
					if (folder.exists()) {
						//long t = System.currentTimeMillis();
						//String[] names = folder.list();
						//TIME_TEST += System.currentTimeMillis() - t;
						
						final int index = i;
						
						try (Stream<Path> stream = Files.list(folder.toPath())) {
							stream.forEach(path -> {
								long t = System.currentTimeMillis();
								if (!searchFinished.get())
								{
									String name = path.getFileName().toString();
									synchronized(usedNames) {
										if (!usedNames.contains(name))
										{
											// For multiple searched words, some might be in the name and others in the file contents
											if (searchInNameOptional(name, foundWords)) {
												searchFinished.set(true);  // Stop searching, we've found a match
												return;
											}
											
											// If it's the last project (hopefully the big source) you don't need to add anymore
											if (index != numProjects-1) usedNames.add(name);
											
											FileSearchRecursive task = null;
											File file = new File(folder, name);
											if (file.isFile()) {
												task = new FileSearchRecursive(null, file, foundWords, searchFinished);
											}
											else {
												task = new FileSearchRecursive(relativePath + File.separatorChar + name, null, foundWords, searchFinished);
											}
											task.invoke();
										}
									}
								}
								TIME_TEST += System.currentTimeMillis() - t;
							});
						} 
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						/*for (String name : names) {
							if (!usedNames.contains(name)) {
									
								// For multiple searched words, some might be in the name and others in the file contents
								if (searchInNameOptional(name, foundWords)) {
									searchFinished.set(true);  // Stop searching, we've found a match
									return;
								}
								
								// If it's the last project (hopefully the big source) you don't need to add anymore
								if (i != numProjects-1) usedNames.add(name);
								
								FileSearchRecursive task = null;
								File file = new File(folder, name);
								if (file.isFile()) {
									task = new FileSearchRecursive(null, file, foundWords, searchFinished);
								}
								else {
									task = new FileSearchRecursive(relativePath + File.separatorChar + name, null, foundWords, searchFinished);
								}
								
								tasks.add(task);
							}
						}*/
					}
					
					// Invoke for every project
					//ForkJoinTask.invokeAll(tasks);
				}
			}
			
			++numFilesSearched;
		}
		
	}
}
