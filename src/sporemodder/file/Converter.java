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

import java.io.File;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import javafx.scene.control.ContextMenu;
import sporemodder.HashManager;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.util.ProjectItem;

public interface Converter {

	/**
	 * Converts the given data from the original format into a user-friendly format. If there is an error, it will be thrown in an exception.
	 * The program must return whether the data was converted or not. If false is returned, the program will try other converters.
	 * <p>
	 * This method must create the output file in the specified folder. The ResourceKey of the original file is given, so the method must use it to give a
	 * different and appropriate name to the output file.
	 * @param stream The data stream to be converted.
	 * @param outputFolder The output folder, where the converted file will be written.
	 * @param key The resource key of the original file.
	 * @return Whether the data was converted (true) or a different converter should be used (false).
	 * @throws Exception
	 */
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception;
	
	/**
	 * Converts the given file from a user-friendly format into the original format. If there is an error, it will be thrown in an exception.
	 * The program must return whether the data was converted or not. If false is returned, the program will try other converters.
	 * @param input The input file to be converted.
	 * @param output The output stream where the converted data will be written.
	 * @return Whether the data was converted (true) or a different converter should be used (false).
	 * @throws Exception
	 */
	public boolean encode(File input, StreamWriter output) throws Exception;
	
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception;
	
	/**
	 * Whether this converter is a valid decoder (converting FROM the format) for the given package resource.
	 * @param key
	 * @return True if the {@link #decode(StreamReader, File, ResourceKey)} method can be called for this resource, false otherwise.
	 */
	public boolean isDecoder(ResourceKey key);
	
	/**
	 * Whether this converter is a valid encoder (converting TO the format) for the given file.
	 * @param file
	 * @return True if the {@link #encode(File, StreamWriter)} method can be called for this file, false otherwise.
	 */
	public boolean isEncoder(File file);
	
	/**
	 * Returns a name for this decoder, such as "Properties File (.prop)"
	 * @return
	 */
	public String getName();
	
	/**
	 * Whether this converter is enabled by default when unpacking a package file. Some converters
	 * might consider it unnecessary for performance reasons.
	 * @return
	 */
	public boolean isEnabledByDefault();
	
	/**
	 * Returns the type ID that must be used when encoding this file. It receives the extension of the file being converted
	 * as an argument.
	 * @param extension The extension of the file that is being converted into the original format.
	 * @return
	 */
	public int getOriginalTypeID(String extension);
	
	public static File getOutputFile(ResourceKey key, File folder, String extraExtension) {
		HashManager hasher = HashManager.get();
		
		return new File(folder, hasher.getFileName(key.getInstanceID()) + "." + hasher.getTypeName(key.getTypeID()) + "." + extraExtension);
	}

	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item);
	
	default void reset() {
		
	}
}
