package sporemodder.file.shaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.FileStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.rw4.MaterialStateCompiler;
import sporemodder.file.rw4.RWCompiledState;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.rw4.RenderWare;

public class MaterialStateLinks {

	public final List<MaterialStateLink> materials = new ArrayList<>();
	public int version;
	
	public void read(StreamReader in, List<RWCompiledState> compiledStates) throws IOException {
		
		version = in.readInt();
		if (version > 1) {
			throw new IOException("Unsupported material link version " + version);
		}
		
		int nameID = -1;
		int index = 0;
		
		while ((nameID = in.readInt()) != -1) {
			
			MaterialStateLink material = new MaterialStateLink();
			materials.add(material);
			
			material.materialID = nameID;
			int numCompiledStates = 0;
			
			if (version == 0) {
				numCompiledStates = in.readByte();
				int textureCount = in.readUByte();
				
				for (int i = 0; i < textureCount; i++) {
					ResourceID resource = new ResourceID();
					resource.setInstanceID(in.readInt());
					resource.setGroupID(in.readInt());
					material.textures.add(resource);
				}
			}
			else if (version == 1) 
			{
				int textureCount = in.readShort();
				for (int i = 0; i < textureCount; i++) {
					ResourceID resource = new ResourceID();
					resource.setInstanceID(in.readInt());
					resource.setGroupID(in.readInt());
					material.textures.add(resource);
					
					material.textureUnks1.add(in.readUShort());
					material.textureUnks2.add(in.readUShort());
				}
				
				for (int i = 0; i < 16; i++) {
					in.readByte();
					in.readByte();
				}
				
				for (int i = 0; i < 16; i++) {
					byte b = in.readByte();
					if (b != 0) {
						numCompiledStates++;
					}
				}
			}
			
			for (int i = 0; i < numCompiledStates; ++i) {
				MaterialStateCompiler state = compiledStates.get(index + i).data;
				material.setName(state, "state-" + (i + index));
				material.states.add(state);
			}
			
			index += numCompiledStates;
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(0);  // only this version is supported
		for (MaterialStateLink material : materials) {
			stream.writeInt(material.materialID);
			stream.writeByte(material.states.size());
			stream.writeByte(material.textures.size());
			
			for (ResourceID texture : material.textures) {
				stream.writeInt(texture.getInstanceID());
				stream.writeInt(texture.getGroupID());
			}
		}
		stream.writeInt(-1);
	}
	
	public void writeRenderWare(StreamWriter stream) throws IOException {
		RenderWare renderWare = new RenderWare();
		renderWare.setType(RenderWareType.SPECIAL);
		for (MaterialStateLink material : materials) {
			for (MaterialStateCompiler state : material.states) {
				RWCompiledState rw = new RWCompiledState(renderWare);
				rw.data = state;
				rw.data.compile();
				renderWare.add(rw);
			}
		}
		renderWare.write(stream);
	}
	
	public void toArgScript(File outputFolder) throws IOException {
		outputFolder.mkdir();
		for (MaterialStateLink material : materials) {
			File file = new File(outputFolder, HashManager.get().getFileName(material.materialID) + ".smt_t");
			
			ArgScriptWriter writer = new ArgScriptWriter();
			material.toArgScript(writer);
			writer.write(file);
		}
	}
	
	public static MaterialStateLinks read(File renderWareFile, File linkFile) throws IOException {
		try (StreamReader linkStream = new FileStream(linkFile, "r")) {
			
			List<RWCompiledState> compiledStates = RenderWare.fromFile(renderWareFile).getObjects(RWCompiledState.class);
			
			MaterialStateLinks links = new MaterialStateLinks();
			links.read(linkStream, compiledStates);
			
			return links;
		}
	}
	
	public void loadFolder(File folder) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".smt_t")) {
				MaterialStateLink link = new MaterialStateLink();
				link.generateStream(false).process(file);
				materials.add(link);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		File linkFile = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_compiled_states_link~\\0x00000003.smt");
		File renderWareFile = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_compiled_states~\\0x00000003.rw4");
		File outputFolder = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_compiled_states_link~\\0x00000003.smt.unpacked\\");
		
		read(renderWareFile, linkFile).toArgScript(outputFolder);
	}
}
