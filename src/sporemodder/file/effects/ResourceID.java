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
package sporemodder.file.effects;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.HashManager;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;

@Structure(StructureEndian.BIG_ENDIAN)
/**
 * A basic class that points to a resource, using a group ID (folder) and instance ID (file).
 */
public class ResourceID {
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<ResourceID> STRUCTURE_METADATA = StructureMetadata.generate(ResourceID.class);

	private int groupID = -1;
	private int instanceID = -1;
	
	/**
	 * Creates a new resource ID with the given group and instance IDs.
	 * @param groupID
	 * @param instanceID
	 */
	public ResourceID(int groupID, int instanceID) {
		this.groupID = groupID;
		this.instanceID = instanceID;
	}
	
	/**
	 * Creates a new resource ID with the same group and instance IDs as the given resource.
	 */
	public ResourceID(ResourceID other) {
		copy(other);
	}
	
	/**
	 * Creates a new resource ID with group=0xFFFFFFFF, instance=0xFFFFFFFF.
	 */
	public ResourceID() {}
	
	public void copy(ResourceID other) {
		if (other != null) {
			this.groupID = other.groupID;
			this.instanceID = other.instanceID;
		}
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public int getInstanceID() {
		return instanceID;
	}

	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}
	
	public boolean parseSpecial(ArgScriptArguments args, int index) {
		return parseSpecial(args, index, null);
	}
	
	public boolean parseSpecial(ArgScriptArguments args, int index, String[] originalWords) {
		switch (args.get(index)) {
		case "terrain":
			groupID = 0;
			instanceID = 0;
			return true;
		case "water":
			groupID = 1;
			instanceID = 0;
			return true;
		default:
			return parse(args, index, originalWords);
		}
	}
	
	public boolean isDefault() {
		return groupID == -1 && instanceID == -1;
	}
	
	public boolean isZero() {
		return groupID == 0 && instanceID == 0;
	}
	
	public void parse(String text) {
		parse(text, null);
	}
	
	public void parse(String text, String[] originalWords) {
		String[] splits = text.split("!", 2);
		
		if (splits.length == 1) {
			groupID = 0;
			instanceID = HashManager.get().getFileHash(splits[0]);
			
			if (originalWords != null) originalWords[1] = splits[0];
		}
		else {
			groupID = HashManager.get().getFileHash(splits[0]);
			instanceID = HashManager.get().getFileHash(splits[1]);
			
			if (originalWords != null) {
				originalWords[0] = splits[0];
				originalWords[1] = splits[1];
			}
		}
	}
	
	public boolean parse(ArgScriptArguments args, int index) {
		return parse(args, index, null);
	}

	public boolean parse(ArgScriptArguments args, int index, String[] originals) {
		String[] splits = args.get(index).split("!", 2);
		
		// We want to know what generated the exception
		boolean generatedByGroupID = true;
		
		try {
			if (splits.length == 1) {
				groupID = 0;
				generatedByGroupID = false;
				
				instanceID = HashManager.get().getFileHash(splits[0]);
				
				if (originals != null) originals[1] = splits[0];
			}
			else {
				groupID = HashManager.get().getFileHash(splits[0]);
				generatedByGroupID = false;
				
				instanceID = HashManager.get().getFileHash(splits[1]);
				
				if (originals != null) {
					originals[0] = splits[0];
					originals[1] = splits[1];
				}
			}
			
			return true;
		}
		catch (Exception e) {
			DocumentError error;
			
			if (generatedByGroupID) {
				error = new DocumentError(e.getLocalizedMessage(), args.getPosition(index), args.getRealPosition(args.getPosition(index) + splits[0].length()));
			}
			else {
				// Get the starting position of intanceID
				int instanceStart = args.getPosition(index);
				int instanceLength = 0;
				
				if (splits.length == 2) {
					instanceLength = splits[1].length();
							
					// Add the groupID length and the ! sign
					instanceStart = splits[0].length() + 1;
				}
				else {
					instanceLength = splits[0].length();
				}
				
				error = new DocumentError(e.getLocalizedMessage(), instanceStart, args.getRealPosition(instanceStart + instanceLength));
			}
			
			args.getStream().addError(error);
			return false;
		}
	}

	public void read(StreamReader in) throws IOException {
		STRUCTURE_METADATA.read(this, in);
	}
	
	public void write(StreamWriter out) throws IOException {
		STRUCTURE_METADATA.write(this, out);
	}

	public void flip() {
		int old = instanceID;
		instanceID = groupID;
		groupID = old;
	}
	
	@Override
	public String toString() {
		// this could be confusing
		/*if (groupID == 0 && nameID == 0) {
			return "terrain";
		}
		else*/ if (groupID == 1 && instanceID == 0) {
			return "water";
		}
		
		HashManager hasher = HashManager.get();
		
		StringBuilder sb = new StringBuilder();
		if (groupID != 0) {
			sb.append(hasher.getFileName(groupID));
			sb.append("!");
		}
		sb.append(hasher.getFileName(instanceID));
		
		return sb.toString();
}
}
