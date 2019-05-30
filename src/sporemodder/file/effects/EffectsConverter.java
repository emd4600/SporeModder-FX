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

import java.io.File;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.util.ProjectItem;

public class EffectsConverter implements Converter {

	public static final int TYPE_ID = 0xEA5118B0;
	private static String extension = null;
	
	private static boolean isHighestQuality(int groupID) {
		return (groupID & 0xFF) == 0x02;
	}
	
	private boolean decode(StreamReader stream, File outputFile) throws Exception {
		EffectDirectory effectDirectory = new EffectDirectory();
		effectDirectory.read(stream);
		
		if (outputFile.exists() && outputFile.isDirectory()) {
			for (File file : outputFile.listFiles()) {
				file.delete();
			}
		}
		
		effectDirectory.toArgScript(outputFile);
		
		return true;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		// Only unpack highest quality
		if (!isHighestQuality(key.getGroupID())) return false;
		else return decode(stream, Converter.getOutputFile(key, outputFolder, "unpacked"));
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws Exception {
		EffectDirectory effectDirectory = new EffectDirectory();
		if (input.isFile()) {
			effectDirectory.processUnit(input);
		} else {
			effectDirectory.process(input, null);
		}
		effectDirectory.write(output);
		
		return true;
	}

	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			try (MemoryStream output = new MemoryStream()) {
				EffectDirectory effectDirectory = new EffectDirectory();
				effectDirectory.process(input, packer);
				effectDirectory.write(output);
				
				String[] splits = input.getName().split("\\.", 2);
				
				ResourceKey name = new ResourceKey();
				name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				name.setGroupID(groupID);
				name.setTypeID(TYPE_ID);
				packer.writeFile(name, output.getRawData(), (int) output.length());
				
				File parentFolder = input.getParentFile();
				// Now check if other qualities exist
				for (int i = 0; i <= 1; ++i) {
					groupID = (groupID & 0xFFFFFF00) + i;
					
					if (!new File(parentFolder, splits[0] + '.' + HashManager.get().getTypeName(TYPE_ID)).exists() ||
							!new File(parentFolder, splits[0] + '.' + HashManager.get().getTypeName(TYPE_ID) + ".unpacked").exists()) {
						
						name.setGroupID(groupID);
						packer.writeFile(name, output.getRawData(), (int) output.length());
					}
				}
				
				return true;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		return key.getTypeID() == TYPE_ID;
	}

	private void checkExtensions() {
		if (extension == null) {
			extension = HashManager.get().getTypeName(TYPE_ID);
		}
	}
	
	@Override
	public boolean isEncoder(File file) {
		checkExtensions();
		return file.isDirectory() && file.getName().endsWith("." + extension + ".unpacked");
	}

	@Override
	public String getName() {
		return "Effect Directory (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		return TYPE_ID;
	}
	
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot() && item.isMod()) {
			boolean isEncoder = isEncoder(item.getFile());
			// We also accept converting single .pfx files
			if (isEncoder || (!item.isFolder() && item.getName().endsWith(".pfx"))) {
				
				MenuItem menuItem = new MenuItem("Pack into EFFDIR");
				menuItem.setMnemonicParsing(false);
				menuItem.setOnAction(event -> {
					// This is after isEncoder(), so we can assume it has extension
					final String name = item.getName().substring(0, item.getName().lastIndexOf(".")) + "." + extension;
					File file = new File(item.getFile().getParentFile(), name);
					
					boolean result = UIManager.get().tryAction(() -> {
						try (FileStream stream = new FileStream(file, "rw")) {
							encode(item.getFile(), stream);
							
							ProjectManager.get().selectItem(ProjectManager.get().getSiblingItem(item, name));
						}
					}, "Cannot encode file.");
					if (!result) {
						// Delete the file, as it hasn't been written properly
						file.delete();
					}
				});
				contextMenu.getItems().add(menuItem);
			}
			else {
				ResourceKey key = ProjectManager.get().getResourceKey(item);
				
				if (isDecoder(key)) {
					MenuItem menuItem = new MenuItem("Unpack into .pfx files");
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "unpacked");
						boolean result = UIManager.get().tryAction(() -> {
							try (FileStream stream = new FileStream(item.getFile(), "r")) {
								decode(stream, outputFile);
								
								ProjectManager.get().selectItem(ProjectManager.get().getSiblingItem(item, outputFile.getName()));
							}
						}, "Cannot decode file.");
						if (!result) {
							// Delete the file, as it hasn't been written properly
							outputFile.delete();
						}
					});
					contextMenu.getItems().add(menuItem);
				}
			}
		}
	}
}
