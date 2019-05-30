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
package sporemodder.file.shaders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import javafx.scene.control.ContextMenu;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFItem;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.rw4.RenderWareConverter;
import sporemodder.util.ProjectItem;

public class SmtConverter implements Converter {

	public static final int GROUP_STATES_LINK = 0x40212000;
	public static final int GROUP_COMPILED_STATES = 0x40212001;
	public static final int GROUP_FRAGMENTS = 0x40212002;
	public static final int GROUP_COMPILED = 0x40212004;
	private static final int TYPE_ID = 0x0469A3F7;
	private static String EXTENSION = null;
	
	private final Map<String, Integer> vsMap = new HashMap<>();
	private final Map<String, Integer> psMap = new HashMap<>();
	/** The fragments that were last converted and that will be used for compilation. */
	private ShaderFragments fragments;
	/** Used to ensure that 'fragments', 'vsMap' and 'psMap' belong to the current packing task. */
	//private DBPFPackingTask fragmentsTask;
	private int fragmentsQuality = -1;
	private boolean alreadyCompiled = true;
	
	/** The folder that will contain unpacked states link. */
	private File statesLinkFolder;
	private final Map<Integer, byte[]> statesStreams = new HashMap<>();
	private final Map<Integer, byte[]> stateLinksStreams = new HashMap<>();
	
	private File shadersFolder;
	private final Map<Integer, byte[]> shadersStreams = new HashMap<>(); 
	
	private static void checkExtensions() {
		if (EXTENSION == null) {
			EXTENSION = HashManager.get().getTypeName(TYPE_ID);
		}
	}
	
	@Override
	public void reset() {
		vsMap.clear();
		psMap.clear();
		fragments = null;
		fragmentsQuality = -1;
		alreadyCompiled = false;
		
		shadersFolder = null;
		shadersStreams.clear();
		statesLinkFolder = null;
		statesStreams.clear();
		stateLinksStreams.clear();
	}
	
	private void unpackStatesLink(byte[] statesData, byte[] linkData, int id) throws IOException {
		checkExtensions();
		
		File file = new File(statesLinkFolder, HashManager.get().getFileName(id) + '.' + EXTENSION + ".unpacked");
		file.mkdir();
		
		try (MemoryStream statesStream = new MemoryStream(statesData);
				MemoryStream linkStream = new MemoryStream(linkData)) {
					
			MaterialStateLinks.read(statesStream, linkStream).toArgScript(file);
		};
	}
	
	private void unpackShaders(byte[] data, int id) throws IOException {
		checkExtensions();
		
		File file = new File(shadersFolder, HashManager.get().getFileName(id) + '.' + EXTENSION + ".unpacked");
		file.mkdir();
		
		try (MemoryStream stream = new MemoryStream(data)) {
			CompiledShaders shaders = new CompiledShaders();
			shaders.read(stream);
			shaders.unpack(file, fragments);
		};
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		int id = key.getInstanceID();
		
		if (key.getGroupID() == GROUP_STATES_LINK) {
			statesLinkFolder = outputFolder;
			// This requires data from GROUP_COMPILED_STATES, so we might need to wait until we have it
			byte[] statesData = statesStreams.get(id);
			if (statesData != null) {
				unpackStatesLink(statesData, stream.toByteArray(), id);
			}
			else {
				// Save our data for later, and when we find the other file we unpack it
				stateLinksStreams.put(id, stream.toByteArray());
			}
			return true;
		}
		else if (key.getGroupID() == GROUP_COMPILED_STATES) {
			// This requires data from GROUP_COMPILED_STATES, so we might need to wait until we have it
			byte[] linksData = stateLinksStreams.get(id);
			if (linksData != null) {
				unpackStatesLink(stream.toByteArray(), linksData, id);
			}
			else {
				// Save our data for later, and when we find the other file we unpack it
				statesStreams.put(id, stream.toByteArray());
			}
			return true;
		}
		else if (key.getGroupID() == GROUP_COMPILED) {
			shadersFolder = outputFolder;
			// This requires data from GROUP_FRAGMENTS, so we might need to wait until we have it

			if (fragments != null) {
				unpackShaders(stream.toByteArray(), id);
			}
			else {
				// Save our data for later, and when we find the other file we unpack it
				shadersStreams.put(id, stream.toByteArray());
			}
			return true;
		}
		else if (key.getGroupID() == GROUP_FRAGMENTS) {
			// Unpack the fragments, and save them
			// Also unpack any queued shaders
			fragments = new	ShaderFragments();
			fragments.read(stream, vsMap, psMap);
			
			checkExtensions();
			File file = new File(outputFolder, HashManager.get().getFileName(id) + '.' + EXTENSION + ".unpacked");
			file.mkdir();
			fragments.toArgScript(file);
			
			for (Map.Entry<Integer, byte[]> entry : shadersStreams.entrySet()) {
				unpackShaders(entry.getValue(), entry.getKey());
			}
			
			return true;
		}
		return false;
	}
	
	@Override public boolean encode(File input, StreamWriter output) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void processFragments(File input, DBPFPacker packer) throws IOException {
		String[] splits = input.getName().split("\\.", 2);
		
		int instanceID = HashManager.get().getFileHash(splits[0]);
		
		ShaderFragments toWriteFragments = new ShaderFragments();
		
		// If it's the first one we find or it's greater quality than what we had
		if (instanceID > fragmentsQuality) {
			vsMap.clear();
			psMap.clear();
			fragmentsQuality = instanceID;
			fragments = toWriteFragments;
			fragments.load(input, vsMap, psMap, packer);
		}
		else {
			// Preserve everything, this quality does not interest us
			toWriteFragments.load(input, new HashMap<>(), new HashMap<>(), packer);
		}
		
		ResourceKey name = packer.getTemporaryName();
		name.setInstanceID(instanceID);
		name.setGroupID(GROUP_FRAGMENTS);
		name.setTypeID(TYPE_ID);
		
		packer.writeFile(name, stream -> toWriteFragments.write(stream));
	}
	
	@Override public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (groupID == GROUP_FRAGMENTS && isEncoder(input)) {
			if (!alreadyCompiled) processFragments(input, packer);
			
			// Ensure it does not try to write it
			return true;
		}
		else if (groupID == GROUP_COMPILED && isEncoder(input)) {
			String[] splits = input.getName().split("\\.", 2);
			
			if (fragments == null) {
				// Maybe this is getting executed before we get the fragments, so get them here
				File parent = input.getParentFile().getParentFile();
				File fragmentsFolder = new File(parent, HashManager.get().getFileName(GROUP_FRAGMENTS));
				if (!fragmentsFolder.exists()) {
					fragmentsFolder = new File(parent, "0x" + HashManager.get().hexToString(GROUP_FRAGMENTS));
				}
				
				if (fragmentsFolder.isDirectory()) {
					for (File folder : fragmentsFolder.listFiles()) {
						if (folder.isDirectory() && folder.getName().endsWith("." + EXTENSION + ".unpacked")) {
							processFragments(folder, packer);
						}
					}
					alreadyCompiled = true;
				}
			}
			
			CompiledShaders shaders = new CompiledShaders();
			shaders.name = splits[0];
			shaders.load(input, fragments, vsMap, psMap, packer);
			
			ResourceKey name = packer.getTemporaryName();
			name.setInstanceID(HashManager.get().getFileHash(splits[0]));
			name.setGroupID(groupID);
			name.setTypeID(TYPE_ID);
			
			packer.writeFile(name, stream -> shaders.write(stream));
			
			return true;
		}
		else if (groupID == GROUP_STATES_LINK && isEncoder(input)) {
			String[] splits = input.getName().split("\\.", 2);
			
			MaterialStateLinks materials = new MaterialStateLinks();
			materials.loadFolder(input);
			
			try (MemoryStream rwStream = new MemoryStream();
					MemoryStream linkStream = new MemoryStream()) {
				
				materials.write(linkStream);
				materials.writeRenderWare(rwStream);
				
				ResourceKey name = packer.getTemporaryName();
				
				name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				name.setGroupID(GROUP_COMPILED_STATES);
				name.setTypeID(RenderWareConverter.TYPE_ID);
				packer.writeFile(name, rwStream.getRawData(), (int) rwStream.length());
				
				name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				name.setGroupID(GROUP_STATES_LINK);
				name.setTypeID(TYPE_ID);
				packer.writeFile(name, linkStream.getRawData(), (int) linkStream.length());
			}
			return true;
		}
		return false;
	}
	
	@Override public boolean isDecoder(ResourceKey key) {
		int id = key.getInstanceID();
		// We are not interested in lower qualities
		if (id == 0 || id == 1 || id == 2) return false;

		int group = key.getGroupID();
		
		if ((group == GROUP_COMPILED || group == GROUP_FRAGMENTS || group == GROUP_STATES_LINK) && key.getTypeID() == TYPE_ID) return true;
		else if (group == GROUP_COMPILED_STATES && key.getTypeID() == RenderWareConverter.TYPE_ID) return true;
		else return false;
	}
	
	@Override public boolean isEncoder(File file) {
		checkExtensions();
		boolean valid = file.isDirectory() && file.getName().endsWith("." + EXTENSION + ".unpacked");
		if (valid) {
			int groupID = HashManager.get().getFileHash(file.getParentFile().getName());
			valid = groupID == GROUP_COMPILED || groupID == GROUP_STATES_LINK || groupID == GROUP_FRAGMENTS;
		}
		return valid;
	}
	
	@Override public String getName() {
		return "Spore Materials (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}
	
	@Override public boolean isEnabledByDefault() {
		return true;
	}
	
	@Override public int getOriginalTypeID(String extension) {
		return TYPE_ID;
	}
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		// TODO Auto-generated method stub
		
	}
	
	
}
