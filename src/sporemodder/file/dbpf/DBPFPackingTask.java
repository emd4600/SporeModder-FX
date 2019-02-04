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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import emord.filestructures.StreamWriter;
import javafx.concurrent.Task;
import sporemodder.FormatManager;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.util.NameRegistry;
import sporemodder.util.Project;
import sporemodder.util.Project.PackageSignature;

public class DBPFPackingTask extends Task<Void> {
	
	/** The estimated progress (in [0, 1]) that writing the index takes. */ 
	private static final double INDEX_PROGRESS = 0.10;
	
	/** The folder with the contents that are being packed. */
	private File inputFolder;
	
	private File outputFile;
	
	/** The output stream where the DBPF file will be written. */
	private StreamWriter stream;
	
	/** The fast memory stream used to write the DBPF index. */
	private MemoryStream indexStream;
	
	/** The total progress (in [0, 1]). */
	private double progress = 0;
	
	/** A map with all the files that couldn't be packed. */
	private final HashMap<File, Exception> failedFiles = new HashMap<File, Exception>();
	
	/** An object that holds information to be used by the ModAPI; it is optional. */
	private DebugInformation debugInfo;
	
	private final DBPFItem item = new DBPFItem();
	
	/** The total amount of items that have been written. */
	private int nItemsCount = 0;
	
	/** If a file is bigger (in bytes) than this number, it will get compressed. If the value is -1, it is ignored. */
	private int nCompressThreshold = -1;
	
	private final PackageSignature packageSignature;
	
	private final RefPackCompression.CompressorOutput compressOut = new RefPackCompression.CompressorOutput();
	
	/** The current file being processed. This is used when diagnosing errors. */
	private File currentFile;
	private Exception failException;
	
	
	private final AtomicBoolean running = new AtomicBoolean(true);
	
	public DBPFPackingTask(Project project, boolean storeDebugInformation) {
		this.inputFolder = project.getFolder();
		this.outputFile = project.getOutputPackage();
		this.packageSignature = project.getPackageSignature();
		
		if (storeDebugInformation) {
			debugInfo = new DebugInformation(project.getName(), inputFolder.getAbsolutePath());
		}
	}

	@Override
	protected Void call() throws Exception {
		
		long time = System.currentTimeMillis();
		
		final HashManager hasher = HashManager.get();
		
		try (StreamWriter stream = new FileStream(outputFile, "rw");
				MemoryStream indexStream = new MemoryStream()) {
			this.stream = stream;
			this.indexStream = indexStream;
			//TODO support DBBF maybe?
			
			// Doesn't really make sense to let the user disable converters.
			List<Converter> converters = new ArrayList<>(FormatManager.get().getConverters());
			// Reverse them so the most common ones (.prop, .rw4) are first
			Collections.reverse(converters);
			
			File[] folders = inputFolder.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					return arg0.isDirectory();
				}
				
			});
			
			// The header will be written after, now we just write padding
			stream.writePadding(96);
			
			// Write the default index information
			indexStream.writeLEInt(4);
			indexStream.writeInt(0);
			
			
			DatabasePackedFile header = new DatabasePackedFile();
			
			/** How much we increment the progress (in %) after every folder is completed. */
			double inc = (1.0 - INDEX_PROGRESS) / folders.length;
			
			boolean alreadyHasPackageSignature = false;
			
			hasher.getProjectRegistry().clear();
			hasher.setUpdateProjectRegistry(true);
			
			for (File folder : folders) {
				
				setCurrentFile(folder);
				
				String currentFolderName = folder.getName();
				int currentGroupID = hasher.getFileHash(currentFolderName);
				
				File[] files = folder.listFiles();
				
				for (File file : files) {
					// Ensure the task is not paused
					if (!running.get()) {
						synchronized (running) {
							while (!running.get()) {
								running.wait();
							}
						}
					}
					
					boolean bUsesConverter = false;
					
					String name = file.getName();
					file = getNestedFile(file, name);
					
					// Skip if there was a problem
					if (file == null) continue;
					
					setCurrentFile(file);
					
					for (Converter converter : converters) {
						if (converter.encode(file, this, currentGroupID)) {
							bUsesConverter = true;
							break;
						}
					}
					
					// The converter must have written the data and added the DBPF item;
					// if there was no converter, we do it here
					if (!bUsesConverter) {
						
						String[] splits = name.split("\\.", 2);
						String currentExtension = splits.length > 1 ? splits[1] : "";
						
						int currentInstanceID = hasher.getFileHash(splits[0]);
						int currentTypeID = hasher.getTypeHash(currentExtension);
						
						item.name.setGroupID(currentGroupID);
						item.name.setInstanceID(currentInstanceID);
						item.name.setTypeID(currentTypeID);
						
						byte[] currentInputData = Files.readAllBytes(file.toPath());
						
						writeFileData(item, currentInputData, currentInputData.length);
						addFile(item);
						
						// Add debug information
						// We only do it here because we cannot get the files from disk in Spore if they needed to be converted
						if (debugInfo != null ) {
							debugInfo.addFile(currentFolderName, name, currentGroupID, currentInstanceID, currentTypeID);
						}
					}
				}
				
				if (!alreadyHasPackageSignature && currentGroupID == 0x40404000) {
					alreadyHasPackageSignature = true;
				}
				
				incProgress(inc);
			}
			
			writeNamesList();
			writePackageSignature(alreadyHasPackageSignature);
			
			// Save debug information
			if (debugInfo != null) {
				debugInfo.saveInformation(this);
			}
			
			// Write header and index
			// First ensure we have no offset base
			stream.setBaseOffset(0);
			header.indexOffset = stream.getFilePointer();
			header.indexCount = nItemsCount;
			header.indexSize = (int) indexStream.length();
			
			indexStream.writeInto(stream);
			stream.write(indexStream.toByteArray());
			
			incProgress(INDEX_PROGRESS);
			
			// Go back and write header
			stream.seek(0);
			header.writeHeader(stream);
		}
		catch (Exception e) {
			failException = e;
		}
		
		// Once done, we can disable updating the project registry
		hasher.setUpdateProjectRegistry(false);
		
		time = System.currentTimeMillis() - time;
		System.out.println("Packed in " + time + " ms");
		
		return null;
	}

	private void incProgress(double increment) {
		progress += increment;
		updateProgress(progress, 1.0);
	}
	
	public void addFile(DBPFItem item) throws IOException {
		item.write(indexStream, false, true, true);
		nItemsCount++;
	}
	
	public boolean writeFileData(DBPFItem item, byte[] data, int length) throws IOException {
		
		item.chunkOffset = stream.getFilePointer();
		
		if (nCompressThreshold != -1 && length > nCompressThreshold) {
			
			RefPackCompression.compress(data, length, compressOut);

			stream.write(compressOut.data, 0, compressOut.lengthInBytes);
			item.isCompressed = true;
			item.memSize = length;
			item.compressedSize = compressOut.lengthInBytes;
		}
		else {
			stream.write(data, 0, length);
			item.isCompressed = false;
			item.memSize = length;
			item.compressedSize = item.memSize;
		}
		

		return item.isCompressed;
	}
	
	public DBPFItem getTemporaryItem() {
		return item;
	}
	
	//TODO consider changing how nested files work
	private File getNestedFile(File file, String name) {
		if (!file.isFile()) {
			if (name.contains(".") && !name.endsWith(".effdir.unpacked")) {
				File newFile = new File(file, name);
				if (!newFile.exists()) {
					failedFiles.put(file, new UnsupportedOperationException("Couldn't find file " + name + " inside subfolder " + name));
					return null;
				}
				file = newFile;
			}
			else if (!name.endsWith(".effdir.unpacked")) {
				failedFiles.put(file, new UnsupportedOperationException("Nested subfolders are not supported. File: " + name));
				return null;
			}
		}
		
		return file;
	}
	
	private void writeNamesList() throws IOException {
		NameRegistry reg = HashManager.get().getProjectRegistry();
		if (!reg.isEmpty()) {
			
			try (MemoryStream output = new MemoryStream()) {
				reg.write(output);
				
				item.name.setGroupID(0x9C9059AE);  // sporemaster
				item.name.setInstanceID(0xCC2F616F);  // names
				item.name.setTypeID(0x2B6CAB5F);  // txt
				
				writeFileData(item, output.getRawData(), (int) output.length());
				addFile(item);
			}
		}
	}
	final static int BUFFER_SIZE = 8192;
	
	private void writePackageSignature(boolean alreadyHasPackageSignature) throws IOException {
		
		if (packageSignature != PackageSignature.NONE && !alreadyHasPackageSignature) {
			
			item.name.setGroupID(0x40404000);
			item.name.setTypeID(0x00B1B104);
			item.name.setInstanceID(HashManager.get().getFileHash(packageSignature.getFileName()));
			
			item.chunkOffset = stream.getFilePointer();
			
			try (InputStream is = packageSignature.getInputStream()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int n;
				
				while ((n = is.read(buffer)) > 0) {
					stream.write(buffer, 0, n);
				}
			}
			
			item.isCompressed = false;
			item.memSize = (int) (stream.getFilePointer() - item.chunkOffset);
			item.compressedSize = item.memSize;
			
			addFile(item);
		}
	}

	/**
	 * Gets the current file being processed; if there is an error, this is the file that caused.
	 * @returns The file that was being processed when the error happened.
	 */
	public File getCurrentFile() {
		return currentFile;
	}
	
	public Exception getFailException() {
		return failException;
	}

	/**
	 * Sets the current file being processed. This is used when diagnosing errors.
	 * @param currentFile
	 */
	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}
	
	public void pause() {
		running.set(false);
	}
	
	public void resume() {
		running.set(true);
		
		synchronized(running) {
			running.notify();
		}
	}
}
