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
package sporemodder.file.rw4;

import java.io.File;
import java.io.IOException;

import emord.filestructures.FileStream;
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
import sporemodder.file.dds.DDSTexture;
import sporemodder.util.ProjectItem;

public class RenderWareConverter implements Converter {
	
	public static final int TYPE_ID = 0x2F4E681B;
	private static String extension = null;
	
	private boolean decode(StreamReader stream, File outputFile) throws IOException {
		RenderWare renderWare = new RenderWare();
		renderWare.read(stream);
		DDSTexture texture = renderWare.toTextureNoExcept();

		if (texture == null) {
			return false;
		}
		else {
			try (FileStream output = new FileStream(outputFile, "rw")) {
				texture.write(output);
				return true;
			}
		}
	}

	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws IOException {
		RenderWare renderWare = new RenderWare();
		renderWare.read(stream);
		DDSTexture texture = renderWare.toTextureNoExcept();

		if (texture == null) {
			return false;
		}
		else {
			try (FileStream output = new FileStream(Converter.getOutputFile(key, outputFolder, "dds"), "rw")) {
				texture.write(output);
				return true;
			}
		}
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws IOException {
		try (FileStream inputStream = new FileStream(input, "r")) {
			DDSTexture texture = new DDSTexture();
			texture.read(inputStream);
			RenderWare.fromTexture(texture).write(output);
			return true;
		}
	}
	
	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			packer.setCurrentFile(input);
			
			DDSTexture texture = new DDSTexture();
			
			try (FileStream inputStream = new FileStream(input, "r")) {
				texture.read(inputStream);
			}
			
			ResourceKey name = packer.getTemporaryName();
			name.setGroupID(groupID);
			name.setInstanceID(input.getName().split("\\.", 2)[0]);
			name.setTypeID(TYPE_ID);  // rw4
			
			packer.writeFile(name, stream -> RenderWare.fromTexture(texture).write(stream));
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		return key.getTypeID() == 0x2F4E681B;
	}

	@Override
	public boolean isEncoder(File file) {
		if (extension == null) {
			extension = "." + HashManager.get().getTypeName(0x2F4E681B) + ".dds";
		}
		return file.isFile() && file.getName().endsWith(extension);
	}

	@Override
	public String getName() {
		return "RenderWare Textures (." + HashManager.get().getTypeName(0x2F4E681B) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return false;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		return 0x2F4E681B;
	}

	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot()) {
			
			if (item.isMod() && isEncoder(item.getFile())) {
				MenuItem menuItem = new MenuItem("Convert to RW4");
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
					MenuItem menuItem = new MenuItem("Convert to DDS");
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "dds");
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
