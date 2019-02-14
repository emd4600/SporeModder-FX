package sporemodder.file.shaders;

import java.io.File;

import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import javafx.scene.control.ContextMenu;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFItem;
import sporemodder.file.dbpf.DBPFPackingTask;
import sporemodder.file.rw4.RenderWareConverter;
import sporemodder.util.ProjectItem;

public class SmtConverter implements Converter {

	public static final int GROUP_STATES_LINK = 0x40212000;
	public static final int GROUP_COMPILED_STATES = 0x40212001;
	public static final int GROUP_UNCOMPILED = 0x40212002;
	public static final int GROUP_COMPILED = 0x40212004;
	private static final int TYPE_ID = 0x0469A3F7;
	private static String EXTENSION = null;
	
	private void checkExtensions() {
		if (EXTENSION == null) {
			EXTENSION = HashManager.get().getTypeName(TYPE_ID);
		}
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override public boolean encode(File input, StreamWriter output) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override public boolean encode(File input, DBPFPackingTask packer, int groupID) throws Exception {
		if (groupID == GROUP_COMPILED) {
			String[] splits = input.getName().split("\\.", 2);
			
			CompiledShaders shaders = new CompiledShaders();
			shaders.name = splits[0];
			shaders.pack(input);
			
			try (MemoryStream stream = new MemoryStream()) {
				shaders.write(stream);
				
				DBPFItem item = packer.getTemporaryItem();
				item.name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				item.name.setGroupID(groupID);
				item.name.setTypeID(TYPE_ID);
				packer.writeFileData(item, stream.getRawData(), (int) stream.length());
				packer.addFile(item);
			}
			
			return true;
		}
		else if (groupID == GROUP_STATES_LINK) {
			String[] splits = input.getName().split("\\.", 2);
			
			MaterialStateLinks materials = new MaterialStateLinks();
			materials.loadFolder(input);
			
			try (MemoryStream rwStream = new MemoryStream();
					MemoryStream linkStream = new MemoryStream()) {
				
				materials.write(linkStream);
				materials.writeRenderWare(rwStream);
				
				DBPFItem item = packer.getTemporaryItem();
				
				item.name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				item.name.setGroupID(GROUP_COMPILED_STATES);
				item.name.setTypeID(RenderWareConverter.TYPE_ID);
				packer.writeFileData(item, rwStream.getRawData(), (int) rwStream.length());
				packer.addFile(item);
				
				item.name.setInstanceID(HashManager.get().getFileHash(splits[0]));
				item.name.setGroupID(GROUP_STATES_LINK);
				item.name.setTypeID(TYPE_ID);
				packer.writeFileData(item, linkStream.getRawData(), (int) linkStream.length());
				packer.addFile(item);
			}
			
			return true;
		}
		return false;
	}
	
	@Override public boolean isDecoder(ResourceKey key) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override public boolean isEncoder(File file) {
		checkExtensions();
		boolean valid = file.isDirectory() && file.getName().endsWith("." + EXTENSION + ".unpacked");
		if (valid) {
			int groupID = HashManager.get().getFileHash(file.getParentFile().getName());
			valid = groupID == GROUP_COMPILED || groupID == GROUP_STATES_LINK;
		}
		return valid;
	}
	
	@Override public String getName() {
		return "Spore Materials (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}
	
	@Override public boolean isEnabledByDefault() {
		return false;
	}
	
	@Override public int getOriginalTypeID(String extension) {
		return TYPE_ID;
	}
	@Override
	public void generateContextMenu(ContextMenu contextMenu, ProjectItem item) {
		// TODO Auto-generated method stub
		
	}
	
	
}
