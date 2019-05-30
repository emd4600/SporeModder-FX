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
package sporemodder.file.anim;

import java.io.File;
import java.io.PrintWriter;

import emord.filestructures.FileStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sporemodder.FileManager;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.Converter;
import sporemodder.file.DocumentException;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.util.ProjectItem;

public class AnimConverter implements Converter {

	public static final int TYPE_ID = 0xEE17C6AD;
	private static String extension = null;
	
	private boolean decode(StreamReader stream, File outputFile) throws Exception {
		SPAnimation anim = new SPAnimation();
		anim.read(stream);
		
		try (PrintWriter out = new PrintWriter(outputFile)) {
		    out.println(anim.toArgScript());
		}
		
		return true;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		return decode(stream, Converter.getOutputFile(key, outputFolder, "pctp_t"));
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws Exception {
		SPAnimation anim = new SPAnimation();
		ArgScriptStream<SPAnimation> stream = anim.generateStream();
		stream.setFastParsing(true);
		stream.process(input);
		anim.write(output, input.getAbsolutePath(), HashManager.get().getFileHash(FileManager.removeExtension(input.getName())));
		return true;
	}

	@Override public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			SPAnimation anim = new SPAnimation();
			ArgScriptStream<SPAnimation> stream = anim.generateStream();
			stream.setFastParsing(true);
			stream.process(input);
			
			if (!stream.getErrors().isEmpty()) {
				throw new DocumentException(stream.getErrors().get(0));
			}
			
			String[] splits = input.getName().split("\\.", 2);
			
			ResourceKey name = packer.getTemporaryName();
			name.setInstanceID(HashManager.get().getFileHash(splits[0]));
			name.setGroupID(groupID);
			name.setTypeID(0x7C19AA7A);  // soundProp or prop
			
			packer.writeFile(name, output -> anim.write(output, input.getAbsolutePath(), name.getInstanceID()));
			
			return true;
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
		return file.isFile() && file.getName().endsWith("." + extension + ".anim_t");
	}

	@Override
	public String getName() {
		return "Animation File (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		// Too many animations out there, we don't to slow unpacking/searching so much
		return false;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		checkExtensions();
		return TYPE_ID;
	}
	
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot() && item.isMod()) {
			
			if (isEncoder(item.getFile())) {
				MenuItem menuItem = new MenuItem("Convert to ANIMATION");
				menuItem.setMnemonicParsing(false);
				menuItem.setOnAction(event -> {
					// This is after isEncoder(), so we can assume it has extension
					final String name = item.getName().substring(0, item.getName().lastIndexOf("."));
					File file = new File(item.getFile().getParentFile(), name);
					
					boolean result = UIManager.get().tryAction(() -> {
						try (FileStream stream = new FileStream(new File(item.getFile().getParentFile(), name), "rw")) {
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
					MenuItem menuItem = new MenuItem("Convert to ANIM_T");
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "anim_t");
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
