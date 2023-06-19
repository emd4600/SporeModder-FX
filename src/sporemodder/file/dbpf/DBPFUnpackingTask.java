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
package sporemodder.file.dbpf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

import sporemodder.HashManager;
import sporemodder.MessageManager;
import sporemodder.MessageManager.MessageType;
import sporemodder.ProjectManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.util.Project;
import sporemodder.util.Project.PackageSignature;
import sporemodder.util.ResumableTask;

public class DBPFUnpackingTask extends ResumableTask<Exception> {
	
	@FunctionalInterface
	public static interface DBPFItemFilter {
		public boolean filter(DBPFItem item);
	}
	
	/** The estimated progress (in [0, 1]) that reading the index takes. */ 
	private static final double INDEX_PROGRESS = 0.05;
	/** The estimated progress (in [0, 1]) that clearing the folder takes. */ 
	private static final double CLEAR_FOLDER_PROGRESS = 0.10;
	
	// Cannot use getProgress() as it throws thread exception
	private double progress = 0;
	
	/** The list of input DBPF files, in order of priority. */
	private final List<File> inputFiles = new ArrayList<File>();
	
	/** Alternative input, an StreamReader. */
	private final StreamReader inputStream;
	
	/** The list of input DBPF files that could not be unpacked (because they didn't exist). */
	private final List<File> failedDBPFs = new ArrayList<File>();
	
	/** The folder where all the contents will be written. */
	private File outputFolder;
	
	/** We will keep all files that couldn't be converted here, so that we can keep unpacking the DBPF. */
	private final Map<DBPFItem, Exception> exceptions = new HashMap<>();
	
	/** All the converters used .*/
	private final List<Converter> converters;
	
	/** How much time the operation took, in milliseconds. */
	private long ellapsedTime;
	
	/** An optional filter that defines which items should be unpacked (true) and which shouldn't (false). */
	private DBPFItemFilter itemFilter;
	
	private Project project;
	
	private boolean setPackageSignature;
	
	//TODO it's faster, but apparently it causes problems; I can't reproduce the bug
	private boolean isParallel = true;
	
	private boolean noJavaFX = false;
	private Consumer<Double> noJavaFXProgressListener;
	
	public DBPFUnpackingTask(File inputFile, File outputFolder, Project project, List<Converter> converters) {
		this.inputFiles.add(inputFile);
		this.outputFolder = outputFolder;
		this.converters = converters;
		this.project = project;
		this.inputStream = null;
	}
	
	public DBPFUnpackingTask(Collection<File> inputFiles, File outputFolder, Project project, List<Converter> converters) {
		this.inputFiles.addAll(inputFiles);
		this.outputFolder = outputFolder;
		this.converters = converters;
		this.project = project;
		this.inputStream = null;
	}
	
	public DBPFUnpackingTask(StreamReader inputStream, File outputFolder, Project project, List<Converter> converters) {
		this.inputStream = inputStream;
		this.outputFolder = outputFolder;
		this.converters = converters;
		this.project = project;
	}
	
	public void setNoJavaFX() {
		this.noJavaFX = true;
	}
	
	public void setNoJavaFXProgressListener(Consumer<Double> listener) {
		noJavaFXProgressListener = listener;
	}
	
	@Override protected void updateMessage(String message) {
		if (!noJavaFX) {
			super.updateMessage(message);
		}
	}
	
	@Override protected void updateProgress(double workDone, double max) {
		if (noJavaFX) {
			noJavaFXProgressListener.accept(workDone);
		}
		else {
			super.updateProgress(workDone, max);
		}
	}
	
	public void setPackageSignature(boolean value) {
		setPackageSignature = value && project != null;
	}
	
	/**
	 * Returns a list of all the converters that will be used when unpacking files.
	 * On every file, if the converter {@link Converter.isDecoder()} method returns true, it will be used to decode the file.
	 * @return
	 */
	public List<Converter> getConverters() {
		return converters;
	}
	
	/**
	 * Returns the list of input package files that will be unpacked.
	 * @return
	 */
	public List<File> getInputFiles() {
		return inputFiles;
	}
	
	/**
	 * Returns the output folder where the unpacked files will be written.
	 * @return
	 */
	public File getOutputFolder() {
		return outputFolder;
	}
	
	/**
	 * Returns the project that is being unpacked. This might be null if a file is being unpacked directly.
	 * @return
	 */
	public Project getProject() {
		return project;
	}
	
	/**
	 * Sets a method that decides which items are unpacked and which are ignored.
	 * @param itemFilter
	 */
	public void setItemFilter(DBPFItemFilter itemFilter) {
		this.itemFilter = itemFilter;
	}

	
	public boolean isParallel() {
		return isParallel;
	}

	public void setParallel(boolean isParallel) {
		this.isParallel = isParallel;
	}

	private static void findNamesFile(List<DBPFItem> items, StreamReader in) throws IOException {
		HashManager hasher = HashManager.get();
		int group = hasher.getFileHash("sporemaster");
		int name = hasher.getFileHash("names");
		
		for (DBPFItem item : items) {
			if (item.name.getGroupID() == group && item.name.getInstanceID() == name) {
				try (ByteArrayInputStream arrayStream = new ByteArrayInputStream(item.processFile(in).getRawData());
						BufferedReader reader = new BufferedReader(new InputStreamReader(arrayStream))) {
					hasher.getProjectRegistry().read(reader);
				}
			}
		}
	}

	private void unpackStream(StreamReader packageStream, Map<Integer, Set<ResourceKey>> writtenFiles, double progressFraction) throws IOException, InterruptedException {
		HashManager hasher = HashManager.get();
			
		updateMessage("Reading file index...");

		DatabasePackedFile header = new DatabasePackedFile();
		header.readHeader(packageStream);
		header.readIndex(packageStream);
		
		DBPFIndex index = header.index;
		index.readItems(packageStream, header.indexCount, header.isDBBF);
		
		incProgress(INDEX_PROGRESS * progressFraction);
		// How much each file adds to the progress
		double inc = (1.0 - INDEX_PROGRESS) * progressFraction / header.indexCount;
		
		updateMessage("Unpacking files...");
		
		//First search sporemaster/names.txt, and use it if it exists
		hasher.getProjectRegistry().clear();
		findNamesFile(index.items, packageStream);
		
		// Sometimes, reading the file data goes faster than the tasks, so we end up
		// loading almost all the package into memory and that causes an OutOfMemory exception
		// We must limit how many tasks we want running simultaneously.
		int maxTasks = ForkJoinPool.getCommonPoolParallelism();
		
		int itemIndex = -1;
		CountDownLatch latch = new CountDownLatch(index.items.size());
		for (DBPFItem item : index.items) {
			++itemIndex;
			// Ensure the task is not paused
			ensureRunning();
			
			if (itemFilter != null && !itemFilter.filter(item)) {
				latch.countDown();
				incProgress(inc);
				continue;
			}
			
			int groupID = item.name.getGroupID();
			int instanceID = item.name.getInstanceID();
			
			// Skip files if they have already been written by higher priority packages
			if (writtenFiles != null) {
				Set<ResourceKey> groupSet = writtenFiles.get(groupID);
				if (groupSet != null) {
					if (groupSet.contains(item.name)) {
						latch.countDown();
						incProgress(inc);
						continue;
					}
				}
			}
			
			String fileName = hasher.getFileName(instanceID);
			
			// skip autolocale files
			if (groupID == 0x02FABF01 && fileName.startsWith("auto_")) {
				latch.countDown();
				incProgress(inc);
				continue;
			}
			
			
			File folder = new File(outputFolder, hasher.getFileName(groupID));
			folder.mkdir();
			
			FileConvertAction action = new FileConvertAction(item, folder, item.processFile(packageStream), inc, latch);
			if (isParallel) {
				if (itemIndex == index.items.size() - 1 || ForkJoinPool.commonPool().getQueuedSubmissionCount() >= maxTasks) {
					// Execute in same thread if it's the last item or if we have many tasks waiting
					ForkJoinPool.commonPool().invoke(action);
				}
				else {
					ForkJoinPool.commonPool().execute(action);
				}
			} else {
				action.compute();
			}
				
			if (writtenFiles != null) {
				Set<ResourceKey> groupSet = writtenFiles.get(groupID);
				if (groupSet == null) {
					groupSet = new HashSet<>();
					writtenFiles.put(groupID, groupSet);
				}
				groupSet.add(item.name);
			}
		}
		
		// Await for all files to finish writing
		latch.await();
		
		// Remove the extra names; if they need to be used, loading the project will load them as well
		hasher.getProjectRegistry().clear();
	}
	
	@Override
	public Exception call() throws Exception {
		
		MessageManager.get().postMessage(MessageType.BeforeDbpfUnpack, this);
		
		long initialTime = System.currentTimeMillis();
		
		if (inputStream != null) {
			unpackStream(inputStream, null, 1.0);
		}
		else {
			double progressFactor = 1.0;
			
			if (project != null) {
				updateMessage("Clearing folder...");
				try {
					ProjectManager.get().initializeProject(project);
				}
				catch (Exception e) {
					e.printStackTrace();
					for (File inputFile : inputFiles) {
						failedDBPFs.add(inputFile);
					}
					return e;
				}
				incProgress(CLEAR_FOLDER_PROGRESS);
				
				progressFactor -= CLEAR_FOLDER_PROGRESS;
			}
			
			
			final HashMap<Integer, Set<ResourceKey>> writtenFiles = new HashMap<>();
			boolean checkFiles = inputFiles.size() > 1;  // only check already existing files if we are unpacking more than one package at once
			
			long[] fileSizes = new long[inputFiles.size()];
			long totalFileSize = 0;
			for (int i = 0; i < fileSizes.length; ++i) {
				if (inputFiles.get(i).exists()) {
					fileSizes[i] = Files.size(inputFiles.get(i).toPath());
					totalFileSize += fileSizes[i];
				}
			}
			
			// Previously we did this for every package
			// PROBLEM: SmtConverter has two files that are only converted as one file. EP1_PatchData only has
			// one of those files, and we want to use that together with the other file from another package,
			// but resetting the converter made it forget the file.
			// FIX: We only reset them once, here. Only SmtConverter was using reset anyways so its fine
			for (Converter converter : converters) converter.reset();
			
			int i = 0;
			for (File inputFile : inputFiles) {
				double projectProgress = progressFactor * (double)fileSizes[i] / totalFileSize;
				
				if (!inputFile.exists()) {
					failedDBPFs.add(inputFile);
					continue;
				}
				
				try (StreamReader packageStream = new FileStream(inputFile, "r"))  {
					unpackStream(packageStream, checkFiles ? writtenFiles : null, projectProgress);
				}
				catch (Exception e) {
					return e;
				}
				++i;
			}
		}
		
		ellapsedTime = System.currentTimeMillis() - initialTime;
		
		// Ensure the taskbar progress is over
		updateProgress(1.0, 1.0);
		updateMessage("Finished");
		
		MessageManager.get().postMessage(MessageType.OnDbpfUnpack, this);
		
		return null;
	}
	
	/**
	 * Returns a Map with all the items that could not be unpacked/converted, mapped to the exception that caused that error.
	 * @return
	 */
	public Map<DBPFItem, Exception> getExceptions() {
		return exceptions;
	}
	
	/**
	 * Returns how much time the operation took, in milliseconds.
	 * @return
	 */
	public long getEllapsedTime() {
		return ellapsedTime;
	}

	private void incProgress(double increment) {
		progress += increment;
		updateProgress(progress, 1.0);
	}
	
	/**
	 * When unpacking multiple packages at once, returns a list of all package files that couldn't be unpacked.
	 * @return
	 */
	public List<File> getFailedDBPFs() {
		return failedDBPFs;
	}
	
	private class FileConvertAction extends RecursiveAction {
		final DBPFItem item;
		final File folder;
		final MemoryStream dataStream;
		final double inc;
		final CountDownLatch latch;
		
		FileConvertAction(DBPFItem item, File folder, MemoryStream dataStream, double inc, CountDownLatch latch) {
			this.item = item;
			this.folder = folder;
			this.dataStream = dataStream;
			this.inc = inc;
			this.latch = latch;
		}
		
		@Override public void compute() {
			try {
				HashManager hasher = HashManager.get();
				int groupID = item.name.getGroupID();
				int instanceID = item.name.getInstanceID();
				
				// Has the file been converted?
				boolean isConverted = false;
				
				// Do not convert editor packages
				if (groupID == 0x40404000 && item.name.getTypeID() == 0x00B1B104) {
					if (setPackageSignature) {
						for (PackageSignature entry : PackageSignature.values()) {
							if (entry.getFileName() != null && hasher.fnvHash(entry.getFileName()) == instanceID) {
								project.setPackageSignature(entry);
								break;
							}
						}
					}
				}
				else {
					try {
						for (Converter converter : converters) {
							if (converter.isDecoder(item.name)) {
								
								if (converter.decode(dataStream, folder, item.name)) {
									isConverted = true;
									break;
								}
								else {
									// throw new IOException("File could not be converted.");
									// We could throw an error here, but it is not appropriate:
									// some files cannot be converted but did not necessarily have an error,
									// for example trying to convert a non-texture rw4. So we jsut keep searching
									// for another converter or write the raw file.s
									continue;
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						exceptions.put(item, e);
						// Handling the exception here will make it write the unconverted file
					}
				}
				
				if (!isConverted) {
					// If it hasn't been converted, just write the file straight away.
					
					String name = hasher.getFileName(item.name.getInstanceID()) + "." + hasher.getTypeName(item.name.getTypeID());
					dataStream.writeToFile(new File(folder, name));
				}
			}
			catch (Exception e) {
				exceptions.put(item, e);
			}
			finally {
				dataStream.close();
				incProgress(inc);
				latch.countDown();
			}
		}
	}
}
