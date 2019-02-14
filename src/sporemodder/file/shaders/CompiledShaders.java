package sporemodder.file.shaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import emord.filestructures.FileStream;
import emord.filestructures.Stream.StringEncoding;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.FileManager;
import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.shaders.StandardShader.StandardShaderEntry;

public class CompiledShaders {

	public final List<StandardShader> shaders = new ArrayList<>();
	public final List<ShaderSelector> shaderSelectors = new ArrayList<>();
	public final List<CompiledShader> vertexShaders = new ArrayList<>();
	public final List<CompiledShader> pixelShaders = new ArrayList<>();
	public int version = 7;
	public String name;
	
	public void read(StreamReader in) throws IOException {
		version = in.readInt();
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			StandardShader shader = new StandardShader();
			shader.read(in, version);
			shaders.add(shader);
		}
		
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			ShaderSelector shader = new ShaderSelector();
			shader.read(in, version);
			shaderSelectors.add(shader);
		}
		
		in.readInt();  // unused
		count = in.readInt();
		for (int i = 0; i < count; ++i) {
			CompiledShader shader = new CompiledShader();
			shader.read(in);
			vertexShaders.add(shader);
		}
		
		in.readInt();  // unused
		count = in.readInt();
		for (int i = 0; i < count; ++i) {
			CompiledShader shader = new CompiledShader();
			shader.read(in);
			pixelShaders.add(shader);
		}
		
		name = in.readString(StringEncoding.ASCII, in.readInt());
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeInt(version);
		
		out.writeInt(shaders.size());
		for (StandardShader shader : shaders) shader.write(out, version);
		
		out.writeInt(shaderSelectors.size());
		for (ShaderSelector shader : shaderSelectors) shader.write(out, version);
		
		//TODO remove
		out.writeInts(0, 0, 0, 0);
		
//		out.writeInt(0);
//		out.writeInt(shaders.size());
//		for (CompiledShader shader : vertexShaders) shader.write(out, version);
//		
//		out.writeInt(0);
//		out.writeInt(shaders.size());
//		for (CompiledShader shader : pixelShaders) shader.write(out, version);
		
		String name = this.name == null ? "" : this.name;
		out.writeInt(name.length());
		out.writeString(name, StringEncoding.ASCII);
	}
	
	public void unpack(File outputFolder) throws IOException {
		FileManager.get().deleteDirectory(outputFolder);
		outputFolder.mkdir();
		
		for (StandardShader shader : shaders) {
			String fileName = HashManager.get().hexToString(shader.id) + '(' + shader.name + ").shader";
			
			try (StreamWriter stream = new FileStream(new File(outputFolder, fileName), "rw")) {
				shader.write(stream, version);
			}
		}
	}
	
	private static String removeExtension(String name) {
		int indexOf = name.indexOf(".");
		return indexOf == -1 ? name : name.substring(0, indexOf);
	}
	
	public void pack(File sourceFolder) throws IOException, InterruptedException {
		File[] files = sourceFolder.listFiles();
		
		Set<String> standardShaderNames = new HashSet<>();
		Map<String, File> sourceVSHFiles = new HashMap<>();
		Map<String, File> sourcePSHFiles = new HashMap<>();
		
		for (File file : files) {
			String fileName = file.getName();
			
			if (fileName.endsWith(".vshader.hlsl")) {
				String name = removeExtension(fileName);
				standardShaderNames.add(name);
				sourceVSHFiles.put(name, file);
			}
			else if (fileName.endsWith(".pshader.hlsl")) {
				String name = removeExtension(fileName);
				standardShaderNames.add(name);
				sourcePSHFiles.put(name, file);
			}
			//TODO other cases
		}
		
		for (String stdName : standardShaderNames) {
			StandardShader shader = new StandardShader();
			shaders.add(shader);
			
			shader.processName(stdName);
			//TODO support multiple entries
			StandardShaderEntry entry = new StandardShaderEntry();
			shader.entries.put(0, entry);
			
			File sourceVertexFile = sourceVSHFiles.get(stdName);
			File sourcePixelFile = sourcePSHFiles.get(stdName);
			
			if (sourceVertexFile == null) {
				throw new IOException("Missing vertex shader for '" + stdName + "'");
			}
			if (sourcePixelFile == null) {
				throw new IOException("Missing pixel shader for '" + stdName + "'");
			}
			
			shader.compile(entry, sourceVertexFile, sourcePixelFile, sourceFolder);
		}
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		File fragmentsFile = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_uncompiled_shader_fragments~\\0x00000003.smt");
		File file = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_compiled_shaders~\\0x00000003.smt");
		File outputFolder = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_compiled_shaders~\\0x00000003.smt.unpacked\\");
		
		try (StreamReader stream = new FileStream(file, "r")) {
			CompiledShaders shaders = new CompiledShaders();
			shaders.read(stream);
			//shaders.unpack(outputFolder);
			
			ShaderFragments fragments = ShaderFragments.readShaderFragments(fragmentsFile);
			for (int i = 0; i < shaders.vertexShaders.size(); ++i) {
				CompiledShader entry = shaders.vertexShaders.get(i);
				
				for (int index : entry.fragmentIndices) {
					if (index == 0) break;
					System.out.print(fragments.getFragment(index).getName() + "   ");
				}
				System.out.println();
			}
			
//			for (int i = 0; i < shaders.vertexShaders.size(); ++i) {
//				CompiledShader entry = shaders.vertexShaders.get(i);
//				System.out.println(entry.getSignatureString());
//				
////				for (ShaderDataUniform data : entry.dataUniforms) {
//////					if (data.flags != 0 && ShaderData.getFlags(data.dataIndex) != data.flags) {
//////						System.out.println("0x" + Integer.toHexString(data.dataIndex) + "\tflags=0x" + Integer.toHexString(data.flags));
//////					}
////					if (!ShaderData.hasName(data.dataIndex)) {
////						System.out.println("0x" + Integer.toHexString(data.dataIndex) + "   in shader " + i);
////					}
////				}
//			}
			
//			//Set<Integer> indices = new HashSet<>();
//			
//			System.out.println(stream.getFilePointer());
//			
//			for (ShaderSelector selector : shaders.shaderSelectors) {
//				
//			}
		}
		
//		File file = new File("E:\\Eric\\SporeModder\\Projects\\CustomMaterials\\materials_compiled_shaders~\\CustomShaderPack.cpp");
//		
//		try (StreamReader stream = new FileStream(file, "r")) {
//			CompiledShaders shaders = new CompiledShaders();
//			shaders.read(stream);
//		}
	}
}
