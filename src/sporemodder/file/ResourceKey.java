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

package sporemodder.file;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptArguments;

/**
 * A structure used to reference a resource in the game, composed of three IDs: instance (the file name), group (the folder name) and type (the extension).
 * Resource keys can be represented as a string, using the following format: <code>group!instance.type</code>, where both group and instance are optional.
 */
@Structure(StructureEndian.LITTLE_ENDIAN)
public class ResourceKey {

	public static final StructureMetadata<ResourceKey> STRUCTURE_METADATA = StructureMetadata.generate(ResourceKey.class);
	
	private int instanceID = 0;
	private int typeID = 0;
	private int groupID = 0;
	
	public ResourceKey() {
		
	}
	
	public ResourceKey(ResourceKey other) {
		copy(other);
	}
	
	/**
	 * Creates a new resource key with the given IDs.
	 * @param groupID The group ID (folder name).
	 * @param instanceID The instance ID (file name).
	 * @param typeID The type ID (extension).
	 */
	public ResourceKey(int groupID, int instanceID, int typeID) {
		this.groupID = groupID;
		this.instanceID = instanceID;
		this.typeID = typeID;
	}
	
	public ResourceKey(String groupID, String instanceID, String typeID) {
		HashManager hasher = HashManager.get();
		this.groupID = hasher.getFileHash(groupID);
		this.instanceID = hasher.getFileHash(instanceID);
		this.typeID = hasher.getTypeHash(typeID);
	}
	
	/**
	 * Copies the data from the given resource key so that both keys are exactly the same.
	 * @param other
	 */
	public void copy(ResourceKey other) {
		groupID = other.groupID;
		instanceID = other.instanceID;
		typeID = other.typeID;
	}
	
	public void readLE(StreamReader stream) throws IOException {
		instanceID = stream.readLEInt();
		typeID = stream.readLEInt();
		groupID = stream.readLEInt();
	}
	
	public void readBE(StreamReader stream) throws IOException {
		instanceID = stream.readInt();
		typeID = stream.readInt();
		groupID = stream.readInt();
	}
	
	public void writeLE(StreamWriter stream) throws IOException {
		stream.writeLEInt(instanceID);
		stream.writeLEInt(typeID);
		stream.writeLEInt(groupID);
	}
	
	public void writeBE(StreamWriter stream) throws IOException {
		stream.writeInt(instanceID);
		stream.writeInt(typeID);
		stream.writeInt(groupID);
	}
	
	/**
	 * @param groupSeparator Separates the group ID from the instance ID ('!' as standard).
	 * @param typeSeparator Separates the type ID from the instance ID ('.' as standard).
	 * @param quotesIfNeeded If true, the returned string will have quotes at the beginning and end if it contains spaces.
	 * @param hexOnly If true, the returned string will only have hexadecimal IDs with no conversion.
	 */
	public String toFormattedString(char groupSeparator, char typeSeparator, boolean quotesIfNeeded, boolean hexOnly) {
		HashManager hasher = HashManager.get();
		StringBuilder sb = new StringBuilder();
		
		if (hexOnly == false) {
			if (groupID != 0) {
				sb.append(hasher.getFileName(groupID));
				sb.append(groupSeparator);
			}
			sb.append(hasher.getFileName(instanceID));
			if (typeID != 0) {
				sb.append(typeSeparator);
				sb.append(hasher.getTypeName(typeID));
			}
		}
		else {
			if (groupID != 0) {
				sb.append(hasher.hexToStringUC(groupID));
				sb.append(groupSeparator);
			}
			sb.append(hasher.hexToStringUC(instanceID));
			if (typeID != 0) {
				sb.append(typeSeparator);
				sb.append(hasher.hexToStringUC(typeID));
			}
		}
		
		
		if (sb.toString().indexOf(' ') != -1 && quotesIfNeeded == true) return '"' + sb.toString() + '"';
		return sb.toString();
	}

	@Override
	public String toString() {
		return toFormattedString('!', '.', true, false);
	}
	
	public boolean isEquivalent(ResourceKey other) {
		return other.instanceID == instanceID && other.typeID == typeID && other.groupID == groupID;
	}
	
	public boolean isZero() {
		return instanceID == 0 && typeID == 0 && groupID == 0;
	}
	
	/** Returns the given ID that represents the group ID (folder name) of this resource key. */
	public int getGroupID() {
		return groupID;
	}
	
	/** Returns the given ID that represents the group ID (folder name) of this resource key. */
	public int getInstanceID() {
		return instanceID;
	}
	
	/** Returns the given ID that represents the group ID (folder name) of this resource key. */
	public int getTypeID() {
		return typeID;
	}
	
	/** Assigns the given ID as the group ID (folder name) of this resource key. */
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	
	/** Calculates the ID equivalent to the given string using the {@link HashManager.getFileHash(String)} method, and
	 * assigns it as the group ID (folder name) of this resource key. */
	public void setGroupID(String groupID) {
		this.groupID = HashManager.get().getFileHash(groupID);
	}
	
	/** Assigns the given ID as the instance ID (file name) of this resource key. */
	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}
	
	/** Calculates the ID equivalent to the given string using the {@link HashManager.getFileHash(String)} method, and
	 * assigns it as the instance ID (file name) of this resource key. */
	public void setInstanceID(String instanceID) {
		this.instanceID = HashManager.get().getFileHash(instanceID);
	}
	
	/** Assigns the given ID as the type ID (extension) of this resource key. */
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	
	/** Calculates the ID equivalent to the given string using the {@link HashManager.getTypeHash(String)} method, and
	 * assigns it as the type ID (extension) of this resource key. */
	public void setTypeID(String typeID) {
		this.typeID = HashManager.get().getTypeHash(typeID);
	}

	/**
	 * Converts a string into a ResourceKey. The string must have the following format: <code>group!instance.type</code>, 
	 * where both group and instance are optional.
	 * @param str
	 */
	public void parse(String str, String[] originals) {
		String[] spl = str.split("\\.");
		String[] groupFile = spl[0].split("!");
		
		HashManager hasher = HashManager.get();
		
		//TODO throw exceptions if format is not correct
		
		if (groupFile.length == 2) {
			groupID = hasher.getFileHash(groupFile[0]);
			instanceID = hasher.getFileHash(groupFile[1]);
			
			if (originals != null) {
				originals[0] = groupFile[0];
				originals[1] = groupFile[1];
			}
		}
		else {
			groupID = 0;
			instanceID = hasher.getFileHash(groupFile[0]);
			
			if (originals != null) {
				originals[1] = groupFile[0];
			}
		}
		
		if (spl.length == 2) {
			typeID = hasher.getTypeHash(spl[1]);
			
			if (originals != null) {
				originals[2] = spl[1];
			}
		}
		else {
			typeID = 0;
		}
	}
	
	public void parse(String str) {
		parse(str, null);
	}
	
	public boolean parse(ArgScriptArguments args, int index) {
		return parse(args, index, null);
	}
	
	public boolean parse(ArgScriptArguments args, int index, String[] originals) {
		String[] spl = args.get(index).split("\\.");
		String[] groupFile = spl[0].split("!");
		
		int startPos = 0;
		int endPos = 0;
		
		HashManager hasher = HashManager.get();
		
		try {
			if (groupFile.length == 2) {
				startPos = args.getPosition(index);
				endPos = args.getRealPosition(startPos + groupFile[0].length());
				groupID = hasher.getFileHash(groupFile[0]);
				
				startPos = endPos + 1;  // add the ! position
				endPos = args.getRealPosition(startPos + groupFile[1].length());
				instanceID = hasher.getFileHash(groupFile[1]);
				
				if (originals != null) {
					originals[0] = groupFile[0];
					originals[1] = groupFile[1];
				}
			}
			else {
				groupID = 0;
				
				startPos = args.getPosition(index);
				endPos = args.getRealPosition(startPos + groupFile[0].length());
				instanceID = hasher.getFileHash(groupFile[0]);
				
				if (originals != null) {
					originals[1] = groupFile[0];
				}
			}
			
			if (spl.length == 2) {
				startPos = args.getRealPosition(args.getPosition(index) + spl[0].length());
				endPos = args.getEndPosition(index);
				typeID = hasher.getTypeHash(spl[1]);
				
				if (originals != null) {
					originals[2] = spl[1];
				}
			}
			else {
				typeID = 0;
			}
		}
		catch (Exception e) {
			args.getStream().addError(new DocumentError(e.getLocalizedMessage(), startPos, endPos));
			return false;
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupID;
		result = prime * result + instanceID;
		result = prime * result + typeID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceKey other = (ResourceKey) obj;
		if (groupID != other.groupID)
			return false;
		if (instanceID != other.instanceID)
			return false;
		if (typeID != other.typeID)
			return false;
		return true;
	}
	
	
}
