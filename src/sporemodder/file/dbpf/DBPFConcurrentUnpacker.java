package sporemodder.file.dbpf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.HashManager;
import sporemodder.MessageManager;
import sporemodder.MessageManager.MessageType;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFUnpackingTask.DBPFItemFilter;
import sporemodder.util.Project;
import sporemodder.util.Project.PackageSignature;

public class DBPFConcurrentUnpacker {
	
	/** The estimated progress (in [0, 1]) that reading the index takes. */ 
	private static final double INDEX_PROGRESS = 0.15;
	
	// Cannot use getProgress() as it throws thread exception
	private double progress = 0;
	
	/** The list of input DBPF files, in order of priority. */
	private final List<File> inputFiles = new ArrayList<File>();
	
	/** The list of input DBPF files that could not be unpacked (because they didn't exist). */
	private final List<File> failedDBPFs = new ArrayList<File>();
	
	/** The folder where all the contents will be written. */
	private File outputFolder;
	
	/** We will keep all files that couldn't be converted here, so that we can keep unpacking the DBPF. */
	private final HashMap<DBPFItem, Exception> exceptions = new HashMap<DBPFItem, Exception>();
	
	/** All the converters used .*/
	private final List<Converter> converters;
	
	/** How much time the operation took, in milliseconds. */
	private long ellapsedTime;
	
	/** An optional filter that defines which items should be unpacked (true) and which shouldn't (false). */
	private DBPFItemFilter itemFilter;
	
	private Project project;
	
	public DBPFConcurrentUnpacker(File inputFile, File outputFolder, Project project, List<Converter> converters) {
		this.inputFiles.add(inputFile);
		this.outputFolder = outputFolder;
		this.converters = converters;
		this.project = project;
	}
	
	public DBPFConcurrentUnpacker(Collection<File> inputFiles, File outputFolder, Project project, List<Converter> converters) {
		this.inputFiles.addAll(inputFiles);
		this.outputFolder = outputFolder;
		this.converters = converters;
		this.project = project;
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
					if (project != null) {
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

	//@Override
	public Exception call() throws Exception {
		
		MessageManager.get().postMessage(MessageType.BeforeDbpfUnpack, this);
		
		HashManager hasher = HashManager.get();
		long initialTime = System.currentTimeMillis();
		
		final HashMap<Integer, List<ResourceKey>> writtenFiles = new HashMap<Integer, List<ResourceKey>>();
		boolean checkFiles = inputFiles.size() > 1;  // only check already existing files if we are unpacking more than one package at once
		
		for (File inputFile : inputFiles) {
			if (!inputFile.exists()) {
				incProgress(100.0f / inputFiles.size());
				failedDBPFs.add(inputFile);
				continue;
			}
			
			for (Converter converter : converters) converter.reset();
			
			try (StreamReader packageStream = new FileStream(inputFile, "r"))  {
				
				//updateMessage("Reading file index...");
				
				DatabasePackedFile header = new DatabasePackedFile();
				header.readHeader(packageStream);
				header.readIndex(packageStream);
				
				DBPFIndex index = header.index;
				index.readItems(packageStream, header.indexCount, header.isDBBF);
				
				incProgress(INDEX_PROGRESS / inputFiles.size());
				//updateMessage("Unpacking files...");
				
				double inc = ((1.0 - INDEX_PROGRESS) / header.indexCount) / inputFiles.size();
				
				//First search sporemaster/names.txt, and use it if it exists
				hasher.getProjectRegistry().clear();
				findNamesFile(index.items, packageStream);
				
				int itemIndex = 0;
				CountDownLatch latch = new CountDownLatch(index.items.size());
				for (DBPFItem item : index.items) {
					// Ensure the task is not paused
					//ensureRunning();
					
					if (itemFilter != null && !itemFilter.filter(item)) continue;
					
					int groupID = item.name.getGroupID();
					int instanceID = item.name.getInstanceID();
					
					if (checkFiles) {
						List<ResourceKey> list = writtenFiles.get(groupID);
						if (list != null) {
							boolean skipFile = false;
							for (ResourceKey key : list) {
								if (key.isEquivalent(item.name)) {
									skipFile = true;
									break;
								}
							}
							if (skipFile) continue;
						}
					}
					
					String fileName = hasher.getFileName(instanceID);
					
					// skip autolocale files
					if (groupID == 0x02FABF01 && fileName.startsWith("auto_")) {
						continue;
					}
					
					File folder = new File(outputFolder, hasher.getFileName(groupID));
					folder.mkdir();
					
					FileConvertAction action = new FileConvertAction(item, folder, item.processFile(packageStream), inc, latch);
					if (itemIndex == index.items.size() - 1) {
						// Execute in same thread if it's the last item
						ForkJoinPool.commonPool().invoke(action);
					}
					else {
						ForkJoinPool.commonPool().execute(action);
					}
					
					if (checkFiles) {
						List<ResourceKey> list = writtenFiles.get(groupID);
						if (list == null) {
							list = new ArrayList<ResourceKey>();
							writtenFiles.put(groupID, list);
						}
						list.add(item.name);
					}
					
					++itemIndex;
				}
				
				// Await for all files to finish writing
				latch.await();
				
				// Remove the extra names; if they need to be used, loading the project will load them as well
				hasher.getProjectRegistry().clear();
			} 
			catch (Exception e) {
				return e;
			}
		}
		
		ellapsedTime = System.currentTimeMillis() - initialTime;
		
		// Ensure the taskbar progress is over
		//updateProgress(1.0, 1.0);
		//updateMessage("Finished");
		
		MessageManager.get().postMessage(MessageType.OnDbpfUnpack, this);
		
		return null;
	}
	
	/**
	 * Returns a Map with all the items that could not be unpacked/converted, mapped to the exception that caused that error.
	 * @return
	 */
	public HashMap<DBPFItem, Exception> getExceptions() {
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
		//updateProgress(progress, 1.0);
	}
	
	/**
	 * When unpacking multiple packages at once, returns a list of all package files that couldn't be unpacked.
	 * @return
	 */
	public List<File> getFailedDBPFs() {
		return failedDBPFs;
	}
}
