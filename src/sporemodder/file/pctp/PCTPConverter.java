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
package sporemodder.file.pctp;

import java.io.File;
import java.io.PrintWriter;

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
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.dbpf.DBPFItem;
import sporemodder.file.dbpf.DBPFPackingTask;
import sporemodder.util.ProjectItem;

public class PCTPConverter implements Converter {

	private static String extension = null;
	
	private boolean decode(StreamReader stream, File outputFile) throws Exception {
		PCTPUnit pctp = new PCTPUnit();
		pctp.read(stream);
		
		try (PrintWriter out = new PrintWriter(outputFile)) {
		    out.println(pctp.toArgScript());
		}
		
		return true;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		return decode(stream, Converter.getOutputFile(key, outputFolder, "pctp_t"));
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws Exception {
		PCTPUnit pctp = new PCTPUnit();
		ArgScriptStream<PCTPUnit> stream = pctp.generateStream();
		stream.setFastParsing(true);
		stream.process(input);
		pctp.write(output);
		return true;
	}

	@Override
	public boolean encode(File input, DBPFPackingTask packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			try (MemoryStream output = new MemoryStream()) {
				PCTPUnit pctp = new PCTPUnit();
				pctp.generateStream().process(input);
				pctp.write(output);
				
				String[] splits = input.getName().split("\\.", 2);
				
				DBPFItem item = packer.getTemporaryItem();
				item.name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				item.name.setGroupID(groupID);
				item.name.setTypeID(0x7C19AA7A);  // soundProp or prop
				packer.writeFileData(item, output.getRawData(), (int) output.length());
				packer.addFile(item);
				
				return true;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		return key.getTypeID() == 0x7C19AA7A;
	}

	private void checkExtensions() {
		if (extension == null) {
			extension = HashManager.get().getTypeName(0x7C19AA7A);
		}
	}
	
	@Override
	public boolean isEncoder(File file) {
		checkExtensions();
		return file.isFile() && file.getName().endsWith("." + extension + ".pctp_t");
	}

	@Override
	public String getName() {
		return "Part Capabilities File (." + HashManager.get().getTypeName(0x7C19AA7A) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		checkExtensions();
		return 0x7C19AA7A;
	}
	
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot() && item.isMod()) {
			
			if (isEncoder(item.getFile())) {
				MenuItem menuItem = new MenuItem("Convert to PCTP");
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
					MenuItem menuItem = new MenuItem("Convert to PCTP_T");
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "pctp_t");
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
