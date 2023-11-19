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
package sporemodder.file.prop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.Converter;
import sporemodder.file.DocumentException;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.ProjectItem;

public class PropConverter implements Converter {
	
	private static String extension = null;
	private static String audioExtension = null;
	private static String submixExtension = null;
	private static String modeExtension = null;
	
	private boolean decode(StreamReader stream, File outputFile) throws IOException {
		PropertyList list = new PropertyList();
		list.read(stream);
		list.toArgScript().write(outputFile);
		return true;
	}

	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws IOException {
		return decode(stream, Converter.getOutputFile(key, outputFolder, "prop_t"));
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws IOException, ParserConfigurationException, SAXException {
		String name = input.getName();
		if (name.endsWith(".prop.xml")) {
			try (InputStream in = new FileInputStream(input)) {
				XmlPropParser.xmlToProp(in, output, null, null);
				return true;
			}
		}
		else {
			PropertyList list = new PropertyList();
			ArgScriptStream<PropertyList> stream = list.generateStream();
			stream.setFolder(input.getParentFile());
			stream.setFastParsing(true);
			stream.process(input);
			list.write(output);
			return true;
		}
	}
	
	private void addAutoLocale(String autoLocale, int tableID, DBPFPacker packer) throws IOException {
		if (autoLocale != null) {
			byte[] data = autoLocale.getBytes("US-ASCII");
			ResourceKey name = new ResourceKey();
			name.setInstanceID(tableID);
			name.setGroupID(0x02FABF01);  // locale~
			name.setTypeID(0x02FAC0B6);  // .locale
			packer.writeFile(name, data, data.length);
		}
	}
	
	private void addPropItem(String name, String extension, int groupID, DBPFPacker packer, byte[] data, int length) throws IOException {
		ResourceKey key = packer.getTemporaryName();
		key.setInstanceID(HashManager.get().getFileHash(name));
		key.setGroupID(groupID);
		if (extension.startsWith(audioExtension)) {
			key.setTypeID(0x02B9F662);
		} else if (extension.startsWith(submixExtension)) {
			key.setTypeID(0x02C9EFF2);
		} else if (extension.startsWith(modeExtension)) {
			key.setTypeID(0x0497925E);
		} else {
			key.setTypeID(0x00B1B104);
		}
		packer.writeFile(key, data, length);
	}
	
	private String getTableIDString(File input, String[] splits) {
		String name = splits[0];
		if (splits[0].endsWith("~")) {
			name = HashManager.get().hexToString(HashManager.get().getFileHash(splits[0]));
		}
		return "auto_" + input.getParentFile().getName() + "_" + name;
	}
	
	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		checkExtensions();
		
		String[] splits = input.getName().split("\\.", 2);
		if (splits.length < 2) return false;  // no extension
				
		if (
			splits[1].equals(extension + ".xml") ||
			splits[1].equals(audioExtension + ".xml") ||
			splits[1].equals(submixExtension + ".xml") || 
			splits[1].equals(modeExtension + ".xml")
		) {
			packer.setCurrentFile(input);
			
			try (InputStream in = new FileInputStream(input);
					MemoryStream output = new MemoryStream()) {
				
				List<String> autoLocaleStrings = new ArrayList<String>();
				String autoLocaleName = getTableIDString(input, splits);
				XmlPropParser.xmlToProp(in, output, autoLocaleStrings, autoLocaleName);
				
				// Use getFileHash instead of fnvHash because we want it to be saved into the project registry
				addAutoLocale(PropertyList.createAutolocaleFile(autoLocaleStrings), HashManager.get().getFileHash(autoLocaleName), packer);
				
				addPropItem(splits[0], splits[1], groupID, packer, output.getRawData(), (int) output.length());
				
				return true;
			}
		}
		else if (
			splits[1].equals(extension + ".prop_t") ||
			splits[1].equals(audioExtension + ".prop_t") ||
			splits[1].equals(submixExtension + ".prop_t") ||
			splits[1].equals(modeExtension + ".prop_t")
		) {
			packer.setCurrentFile(input);
			
			try (MemoryStream output = new MemoryStream()) {
				// Use getFileHash instead of fnvHash because we want it to be saved into the project registry
				int tableID = HashManager.get().getFileHash(getTableIDString(input, splits));
				
				PropertyList list = new PropertyList();
				ArgScriptStream<PropertyList> stream = list.generateStream();
				stream.setFastParsing(true);
				stream.process(input);
				
				if (!stream.getErrors().isEmpty()) {
					throw new DocumentException(stream.getErrors().get(0));
				}
				
				addAutoLocale(list.createAutolocaleFile(tableID), tableID, packer);
				list.write(output);
				
				addPropItem(splits[0], splits[1], groupID, packer, output.getRawData(), (int) output.length());
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		// There are two extensions for PROP: the standard and the sound one
		return key.getTypeID() == 0x00B1B104 || key.getTypeID() == 0x02B9F662;
	}

	private void checkExtensions() {
		if (extension == null) {
			extension = HashManager.get().getTypeName(0x00B1B104);
			audioExtension = HashManager.get().getTypeName(0x02B9F662);
			submixExtension = HashManager.get().getTypeName(0x02C9EFF2);
			modeExtension = HashManager.get().getTypeName(0x0497925E);
		}
	}
	
	@Override
	public boolean isEncoder(File file) {
		checkExtensions();
		return file.isFile() && (
			file.getName().endsWith("." + extension + ".xml") || 
			file.getName().endsWith("." + extension + ".prop_t") ||
			file.getName().endsWith("." + audioExtension + ".xml") || 
			file.getName().endsWith("." + audioExtension + ".prop_t") ||
			file.getName().endsWith("." + submixExtension + ".xml") || 
			file.getName().endsWith("." + submixExtension + ".prop_t") ||
			file.getName().endsWith("." + modeExtension + ".xml") || 
			file.getName().endsWith("." + modeExtension + ".prop_t")
		);
	}

	@Override
	public String getName() {
		return "Properties File (." + HashManager.get().getTypeName(0x00B1B104) + ", ." + HashManager.get().getTypeName(0x02B9F662) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		checkExtensions();
		if (extension.startsWith("." + audioExtension)) {
			return 0x02B9F662;
		} else if (extension.startsWith("." + submixExtension)) {
			return 0x02C9EFF2;
		} else if (extension.startsWith("." + modeExtension)) {
			return 0x0497925E;
		}
		return 0x00B1B104;
	}
	
	public static String intoValidText(String text) {
		text = text.replaceAll("\"", "&quot;");
		text = text.replaceAll("#", "&hash;");
		return text;
	}
	
	public static String intoOriginalText(String text) {
		text = text.replaceAll("&quot;", "\"");
		text = text.replaceAll("&hash;", "#");
		return text;
	}
	
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot()) {
			
			if (item.isMod() && isEncoder(item.getFile())) {
				MenuItem menuItem = new MenuItem("Convert to PROP");
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
				
				TreeItem<ProjectItem> parentItem = item.getTreeItem().getParent();
				if (parentItem != null && !parentItem.getValue().isRoot()) {
					key.setGroupID(parentItem.getValue().getName());
				}
				
				if (isDecoder(key)) {
					MenuItem menuItem = new MenuItem("Convert to PROP_T");
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "prop_t");
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
