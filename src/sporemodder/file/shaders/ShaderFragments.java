package sporemodder.file.shaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.FileStream;
import emord.filestructures.StreamReader;
import sporemodder.file.argscript.ArgScriptWriter;

public class ShaderFragments {
private static final int SHADER_COUNT = 255;  // 255 ?
	
	public final List<ShaderFragment> shaders = new ArrayList<>();
	public int version = 1;
	
	public ShaderFragment getFragment(int index) {
		return shaders.get(index - 1);
	}
	
	public void read(StreamReader in) throws IOException {
		version = in.readInt();
		
		for (int i = 0; i < SHADER_COUNT; i++) {
			
			ShaderFragment shader = new ShaderFragment();
			shader.read(in);
			if ((shader.flags & ShaderFragment.FLAG_DEFINED) != 0) {
				shaders.add(shader);
			}
		}
	}
	
	public void writeHLSL(File outputFolder) throws IOException {
		outputFolder.mkdir();
		for (ShaderFragment shader : shaders) {
			if (shader.getName() == null || shader.getName().isEmpty()) return;
			File file = new File(outputFolder, shader.getName() + ".hlsl");
			
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				shader.writeHLSL(writer);
			}
		}
	}
	
	public void toArgScript(File outputFolder) throws FileNotFoundException {
		outputFolder.mkdir();
		for (int i = 0; i < shaders.size(); ++i) {
			ShaderFragment shader = shaders.get(i);
			if (shader.getName() == null || shader.getName().isEmpty()) return;
			File file = new File(outputFolder, i + "(" + shader.getName() + ").fragment");
			
			ArgScriptWriter writer = new ArgScriptWriter();
			shader.toArgScript(writer);
			writer.write(file);
		}
	}
	
	public static ShaderFragments readShaderFragments(File file) throws IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			ShaderFragments shaders = new ShaderFragments();
			shaders.read(stream);
			return shaders;
		}
	}
	
	public static void main(String[] args) throws IOException {
//		File file = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CompiledMaterials\\materials_uncompiled_shader_fragments~\\0x00000003.smt");
//		File output = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CompiledMaterials\\materials_uncompiled_shader_fragments~\\0x00000003.smt.unpacked");
		
		File file = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_uncompiled_shader_fragments~\\0x00000003.smt");
		File output = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_uncompiled_shader_fragments~\\0x00000003.smt.unpacked");
		
		try (FileStream stream = new FileStream(file, "r")) {
			ShaderFragments shaders = new ShaderFragments();
			shaders.read(stream);
//			shaders.writeHLSL(output);
			shaders.toArgScript(output);
			
//			for (UncompiledShader shader : shaders.shaders) {
//				if (shader.flags != 3 && shader.shaderName != null) {
//					System.out.println(shader.shaderName + "  0x" + Integer.toHexString(shader.flags));
//				}
//			}
			
//			for (int i = 0; i < shaders.shaders.size(); ++i) {
//				ShaderFragment shader = shaders.shaders.get(i);
//				if (shader.shaderName != null && (shader.flags & 1) != 0) {
//					System.out.println((i+1) + ":\t" + shader.shaderName);
//				}
//				else {
//					break;
//				}
//			}
		}
	}
}
