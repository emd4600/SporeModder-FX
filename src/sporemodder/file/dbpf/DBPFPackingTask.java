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
import sporemodder.file.ResourceKey;
import sporemodder.util.NameRegistry;
import sporemodder.util.Project;
import sporemodder.util.Project.PackageSignature;

public class DBPFPackingTask extends Task<Void> {
	
	/** The folder with the contents that are being packed. */
	private File inputFolder;
	
	private File outputFile;

	
	/** The total progress (in [0, 1]). */
	private double progress = 0;
	
	/** A map with all the files that couldn't be packed. */
	private final HashMap<File, Exception> failedFiles = new HashMap<File, Exception>();
	
	/** An object that holds information to be used by the ModAPI; it is optional. */
	private DebugInformation debugInfo;
	
	private final PackageSignature packageSignature;
	
	private Exception failException;
	
	private DBPFPacker packer;
	
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
		
		try (DBPFPacker packer = new DBPFPacker(outputFile)) {
			this.packer = packer;
			
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
			
			/** How much we increment the progress (in %) after every folder is completed. */
			double inc = 1.0 / folders.length;
			
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
					file = getNestedFile(file, name, converters);
					
					// Skip if there was a problem
					if (file == null) continue;
					
					setCurrentFile(file);
					
					for (Converter converter : converters) {
						if (converter.encode(file, packer, currentGroupID)) {
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
						
						byte[] currentInputData = Files.readAllBytes(file.toPath());
						
						packer.writeFile(new ResourceKey(currentGroupID, currentInstanceID, currentTypeID),
								currentInputData, currentInputData.length);
						
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
				debugInfo.saveInformation(packer);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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
	
	//TODO consider changing how nested files work
	private File getNestedFile(File file, String name, List<Converter> converters) {
		if (!file.isFile()) {
			for (Converter converter : converters) {
				if (converter.isEncoder(file)) return file;
			}
			File newFile = new File(file, name);
			if (!newFile.exists()) {
				failedFiles.put(file, new UnsupportedOperationException("Couldn't find file " + name + " inside subfolder " + name));
				return null;
			}
			file = newFile;
		}
		
		return file;
	}
	
	private void writeNamesList() throws IOException {
		NameRegistry reg = HashManager.get().getProjectRegistry();
		if (!reg.isEmpty()) {
			packer.writeFile(new ResourceKey(0x9C9059AE, 0xCC2F616F, 0x2B6CAB5F),
					stream -> reg.write(stream));
		}
	}
	final static int BUFFER_SIZE = 8192;
	
	private void writePackageSignature(boolean alreadyHasPackageSignature) throws IOException {
		
		if (packageSignature != PackageSignature.NONE && !alreadyHasPackageSignature) {
			
			DBPFItem item = packer.getTemporaryItem();
			StreamWriter stream = packer.getStream();
			
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
			
			packer.addFile(item);
		}
	}

	/**
	 * Gets the current file being processed; if there is an error, this is the file that caused.
	 * @returns The file that was being processed when the error happened.
	 */
	public File getCurrentFile() {
		return packer.getCurrentFile();
	}
	
	public Exception getFailException() {
		return failException;
	}

	/**
	 * Sets the current file being processed. This is used when diagnosing errors.
	 * @param currentFile
	 */
	public void setCurrentFile(File currentFile) {
		packer.setCurrentFile(currentFile);
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
