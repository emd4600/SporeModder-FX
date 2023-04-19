package sporemodder.file.gait;

import java.io.File;
import java.io.PrintWriter;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.Converter;
import sporemodder.file.DocumentException;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.ProjectItem;

public class GaitConverter implements Converter {
	
	private static final int TYPE_ID = 0x25DF0112;
	private static String extension = null;
	
	private boolean decode(StreamReader stream, File outputFile) throws Exception {
		GaitFile gait = new GaitFile();
		gait.read(stream);
		
		try (PrintWriter out = new PrintWriter(outputFile)) {
		    out.println(gait.toArgScript());
		}
		
		return true;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		return decode(stream, Converter.getOutputFile(key, outputFolder, "gait_t"));
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws Exception {
		GaitFile gait = new GaitFile();
		ArgScriptStream<GaitFile> stream = gait.generateStream();
		stream.setFastParsing(true);
		stream.process(input);
		gait.write(output);
		return true;
	}

	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			GaitFile gait = new GaitFile();
			ArgScriptStream<GaitFile> stream = gait.generateStream();
			stream.setFastParsing(true);
			stream.process(input);
			
			if (!stream.getErrors().isEmpty()) {
				throw new DocumentException(stream.getErrors().get(0));
			}
			
			String[] splits = input.getName().split("\\.", 2);
			
			ResourceKey name = packer.getTemporaryName();
			name.setInstanceID(HashManager.get().getFileHash(splits[0]));
			name.setGroupID(groupID);
			name.setTypeID(TYPE_ID);
			
			packer.writeFile(name, output -> gait.write(output));
			
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
		return file.isFile() && file.getName().endsWith("." + extension + ".gait_t");
	}

	@Override
	public String getName() {
		return "Gait File (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		checkExtensions();
		return TYPE_ID;
	}
	
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		if (!item.isRoot()) {
			
			if (item.isMod() && isEncoder(item.getFile())) {
				MenuItem menuItem = new MenuItem("Convert to GAIT");
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
					MenuItem menuItem = new MenuItem("Convert to GAIT_T");
					menuItem.setMnemonicParsing(false);
					menuItem.setOnAction(event -> {
						final File outputFile = Converter.getOutputFile(key, item.getFile().getParentFile(), "gait_t");
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
