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
import java.io.IOException;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.ResourceKey;

/**
 * This class is used to generate DatabasePackedFiles, more known as .package files. 
 * This is an auto-closeable object, so it can (and should) be used inside a <code>try-catch</code> block.
 * <p>
 * The basic action is adding a file to the package, which is done by using the {@link #writeFile(ResourceKey, WriteAction)}.
 * This method will execute the given write action, which can be provided as a lambda (for example 
 * {@code stream -> propList.write(stream)}), and then will add the specified {@link ResourceKey} to the package index, so that it
 * references the written data.
 * <p> 
 * The package file is not usable until the {@code DBPFPacker} object is closed. If, while executing a write action, there is an exception,
 * the packer will close abruptly. Whenever you are writing a file into the package you should use the {@link #setCurrentFile(File)};
 * this way, if an exception is produced, you will be able to tell the user what file caused it.
 * <p>
 * A small example of how to use packer: 
 * <pre>
 * File file = ...;
 * try (DBPFPacker packer = new DBPFPacker(file)) {
 *	PropertyList propList = ...;
 *	packer.writeFile(new ResourceKey(0, HashManager.get().getFileHash("CreatureEditorSetup"), 0x00B1B104),
 *	   stream -> propList.write(stream));
 * }
 * </pre>
 */
public class DBPFPacker implements AutoCloseable {
	
	@FunctionalInterface
	public static interface WriteAction {
		public void consume(StreamWriter stream) throws Exception;
	}
	
	/** The output stream where the DBPF file will be written. */
	private final StreamWriter stream;
	/** The fast memory stream used to write the DBPF index. */
	private final MemoryStream indexStream = new MemoryStream();
	private final DBPFItem item = new DBPFItem();
	/** The total amount of items that have been written. */
	private int nItemsCount;
	/** If a file is bigger (in bytes) than this number, it will get compressed. If the value is -1, it is ignored. */
	private int compressThreshold = -1;
	private final RefPackCompression.CompressorOutput compressOut = new RefPackCompression.CompressorOutput();
	private File currentFile;
	
	private boolean closeStream;
	
	public DBPFPacker(StreamWriter output, boolean closeStream) throws IOException {
		stream = output;
		this.closeStream = closeStream;
		initialWrite();
	}
	
	public DBPFPacker(File output) throws IOException {
		this(new FileStream(output, "rw"), true);
	}
	
	/**
	 * Returns the output stream where the data is being written. Developers should not write any data
	 * using this method and use the specialized {@link #writeFile(ResourceKey, WriteAction)} method instead.
	 * @return
	 */
	public StreamWriter getStream() {
		return stream;
	}
	
	private void initialWrite() throws IOException {
		// The header will be written after, now we just write padding
		stream.writePadding(96);
		
		// Write the default index information
		indexStream.writeLEInt(4);
		indexStream.writeInt(0);
	}
	
	@Override public void close() throws Exception {
		DatabasePackedFile header = new DatabasePackedFile();
		
		// Write header and index
		// First ensure we have no offset base
		stream.setBaseOffset(0);
		header.indexOffset = stream.getFilePointer();
		header.indexCount = nItemsCount;
		header.indexSize = (int) indexStream.length();
		
		indexStream.writeInto(stream);
		stream.write(indexStream.toByteArray());
		
		// Go back and write header
		stream.seek(0);
		header.writeHeader(stream);
		
		if (closeStream) stream.close();
		indexStream.close();
	}
	
	/**
	 * If a file size is bigger than this threshold, the file data will be compressed.	
	 * @return
	 */
	public int getCompressThreshold() {
		return compressThreshold;
	}

	/**
	 * Gets the current file being processed. This is used when diagnosing errors.
	 * @return
	 */
	public File getCurrentFile() {
		return currentFile;
	}

	/**
	 * If a file size is bigger than this threshold, the file data will be compressed.	
	 * @param compressThreshold
	 */
	public void setCompressThreshold(int compressThreshold) {
		this.compressThreshold = compressThreshold;
	}

	/**
	 * Sets the current file being processed. This is used when diagnosing errors.
	 * @param currentFile
	 */
	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}

	/**
	 * Returns a DBPFItem where you can put all file metadata. If you use this, you can avoid creating unnecessary items.
	 * @return
	 */
	public DBPFItem getTemporaryItem() {
		return item;
	}
	
	public ResourceKey getTemporaryName() {
		return item.name;
	}
	
	/**
	 * Writes the given data to the output stream and adds the file to the DBPF index.
	 * More precisely, it writes the data in <code>data[0..length-1]</code> to the stream. 
	 * If this DBPF packer has a compression threshold defined and <code>length</code> is greater than the threshold,
	 * the data will be compressed.
	 * This method returns whether the data will be compressed or not.
	 * 
	 * @param name The ResourceKey of this file, that's how the file will be indexed.
	 * @param data The byte array containing the file data. It can contain more data than necessary.
	 * @param length How many bytes from the byte array will be written.
	 * @return Whether the data was compressed or not.
	 * @throws IOException
	 */
	public boolean writeFile(ResourceKey name, byte[] data, int length) throws IOException {
		item.name.copy(name);
		item.chunkOffset = stream.getFilePointer();
		
		if (compressThreshold != -1 && length > compressThreshold) {
			
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
		
		addFile(item);

		return item.isCompressed;
	}
	
	/**
	 * Executes a writing action into a temporary stream, and then writes the data of that temporary stream
	 * to the output file, adding the given name to the DBPF index. This executes the given action and calls
	 * {@link #writeFile(ResourceKey, byte[], int)} on the resulting data.
	 * This method returns whether the data will be compressed or not.
	 * 
	 * @param name The ResourceKey of this file, that's how the file will be indexed. 
	 * @param action The consumer action that writes data to a stream.
	 * @return Whether the data was compressed or not.
	 * @throws IOException
	 */
	public boolean writeFile(ResourceKey name, WriteAction action) throws IOException {
		try (MemoryStream tempStream = new MemoryStream()) {
			action.consume(tempStream);
			
			return writeFile(name, tempStream.getRawData(), (int) tempStream.length());
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Adds the given item to the DBPF index. This does not write the item data and does not modify the item.
	 * @param item
	 * @throws IOException
	 */
	public void addFile(DBPFItem item) throws IOException {
		item.write(indexStream, false, true, true);
		nItemsCount++;
	}
	
}
