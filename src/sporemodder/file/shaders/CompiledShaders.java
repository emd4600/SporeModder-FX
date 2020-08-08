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
package sporemodder.file.shaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
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
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.shaders.ShaderBuilder.ShaderBuilderEntry;
import sporemodder.file.shaders.StandardShader.StandardShaderEntry;

public class CompiledShaders {
	
	private static final String PRECOMPILED_VSH_FILE = "precompiled_vsh.txt";
	private static final String PRECOMPILED_PSH_FILE = "precompiled_psh.txt";

	public final List<StandardShader> shaders = new ArrayList<>();
	public final List<ShaderBuilder> shaderBuilders = new ArrayList<>();
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
			ShaderBuilder shader = new ShaderBuilder();
			shader.read(in, version);
			shaderBuilders.add(shader);
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
		
		out.writeInt(shaderBuilders.size());
		for (ShaderBuilder shader : shaderBuilders) shader.write(out, version);
		
		out.writeInt(0);
		out.writeInt(vertexShaders.size());
		for (CompiledShader shader : vertexShaders) shader.write(out);
		
		out.writeInt(0);
		out.writeInt(pixelShaders.size());
		for (CompiledShader shader : pixelShaders) shader.write(out);
		
		String name = this.name == null ? "" : this.name;
		out.writeInt(name.length());
		out.writeString(name, StringEncoding.ASCII);
	}
	
	public ShaderBuilder getSelector(int id) {
		for (ShaderBuilder shader : shaderBuilders) if (shader.id == id) return shader;
		return null;
	}
	
	private String getName(MaterialShader shader) {
		if (shader.id == HashManager.get().fnvHash(shader.name)) return shader.name;
		else return HashManager.get().hexToString(shader.id) + '(' + shader.name + ")";
	}
	
	public void unpackStandardShaders(File outputFolder) throws IOException {
		for (StandardShader shader : shaders) {
			String fileName = getName(shader) + ".shader";
			
			try (StreamWriter stream = new FileStream(new File(outputFolder, fileName), "rw")) {
				shader.write(stream, version);
			}
		}
	}
	
	public void unpackShaderBuilders(File outputFolder, ShaderFragments fragments) throws IOException {
		for (ShaderBuilder shader : shaderBuilders) {
			String fileName = getName(shader);
			
			for (Map.Entry<Integer, ShaderBuilderEntry> entry : shader.entries.entrySet()) {
				String name;
				if (shader.entries.size() == 1) {
					name = fileName + ".shader_builder";
				} else {
					name = fileName + "." + entry.getKey() + ".shader_builder";
				}
				
				ArgScriptWriter writer = new ArgScriptWriter();
				shader.toArgScript(writer, entry.getValue(), fragments);
				writer.write(new File(outputFolder, name));
			}
		}
	}
	
	public void unpackPrecompiledShaders(File outputFolder, ShaderFragments fragments) throws IOException {
		if (!vertexShaders.isEmpty()) {
			try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFolder, PRECOMPILED_VSH_FILE)))) {
				for (CompiledShader shader : vertexShaders) {
					for (int i : shader.fragmentIndices) {
						if (i == 0) break;
						out.write(fragments.vertexFragments.get(i - 1).shaderName + ' ');
					}
					out.newLine();
				}
			}
		}
		if (!pixelShaders.isEmpty()) {
			try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFolder, PRECOMPILED_PSH_FILE)))) {
				for (CompiledShader shader : pixelShaders) {
					for (int i : shader.fragmentIndices) {
						if (i == 0) break;
						out.write(fragments.pixelFragments.get(i).shaderName + ' ');
					}
					out.newLine();
				}
			}
		}
	}
	
	public void unpack(File outputFolder, ShaderFragments fragments) throws IOException {
		FileManager.get().deleteDirectory(outputFolder);
		outputFolder.mkdir();
		
		unpackStandardShaders(outputFolder);
		unpackShaderBuilders(outputFolder, fragments);
		unpackPrecompiledShaders(outputFolder, fragments);
	}
	
	private static String removeExtension(String name) {
		int indexOf = name.indexOf(".");
		return indexOf == -1 ? name : name.substring(0, indexOf);
	}
	
	public void load(File sourceFolder, ShaderFragments fragments, Map<String, Integer> vsMap, Map<String, Integer> psMap, DBPFPacker packer) throws IOException, InterruptedException {
		File[] files = sourceFolder.listFiles();
		
		Set<String> standardShaderNames = new HashSet<>();
		Map<String, File> sourceVSHFiles = new HashMap<>();
		Map<String, File> sourcePSHFiles = new HashMap<>();
		List<File> builderFiles = new ArrayList<>();
		File precompiledVSHFile = null;
		File precompiledPSHFile = null;
		
		for (File file : files) {
			String fileName = file.getName();
			
			if (fileName.endsWith(".vshader.hlsl")) {
				String name = fileName.substring(0, fileName.length()-13);
				standardShaderNames.add(name);
				sourceVSHFiles.put(name, file);
			}
			else if (fileName.endsWith(".pshader.hlsl")) {
				String name = fileName.substring(0, fileName.length()-13);
				standardShaderNames.add(name);
				sourcePSHFiles.put(name, file);
			}
			else if (fileName.endsWith(".shader")) {
				StandardShader shader = new StandardShader();
				shader.name = removeExtension(fileName);
				shaders.add(shader);
			}
			else if (fileName.endsWith(".shader_builder")) {
				builderFiles.add(file);
			}
			else if (fileName.equals(PRECOMPILED_VSH_FILE)) {
				precompiledVSHFile = file;
			}
			else if (fileName.equals(PRECOMPILED_PSH_FILE)) {
				precompiledPSHFile = file;
			}
		}
		
		final Map<String, ShaderBuilder> buildersMap = new HashMap<>();
		
		if (!builderFiles.isEmpty() && (fragments == null || vsMap == null || psMap == null || vsMap.isEmpty() || psMap.isEmpty())) {
			throw new IOException("Cannot compile .shader_builder files without custom fragments in " + HashManager.get().getFileName(SmtConverter.GROUP_FRAGMENTS));
		}
		
		for (File file : builderFiles) {
			if (packer != null) packer.setCurrentFile(file);
			
			String shaderName = file.getName();
			shaderName = shaderName.substring(0, shaderName.length()-15);
			
			int entryIndex = 0;
			int indexOf = shaderName.indexOf(".");
			if (indexOf != -1) {
				entryIndex = Integer.parseInt(shaderName.substring(indexOf+1));
				shaderName = shaderName.substring(0, indexOf);
			}
			
			ShaderBuilder shader = buildersMap.get(shaderName);
			if (shader == null) {
				shader = new ShaderBuilder();
				shaderBuilders.add(shader);
				buildersMap.put(shaderName, shader);
			}
			
			shader.processName(shaderName);
			
			ShaderBuilderEntry entry = new ShaderBuilderEntry();
			ShaderBuilder.generateStream(entry, vsMap, psMap).process(file);
			shader.entries.put(entryIndex, entry);
		}
		
		final Map<String, StandardShader> shadersMap = new HashMap<>();
		
		for (String stdName : standardShaderNames) {
			String shaderName = stdName;
			
			int entryIndex = 0;
			int indexOf = stdName.indexOf(".");
			if (indexOf != -1) {
				entryIndex = Integer.parseInt(stdName.substring(indexOf+1));
				shaderName = stdName.substring(0, indexOf);
			}
			
			StandardShader shader = shadersMap.get(shaderName);
			if (shader == null) {
				shader = new StandardShader();
				shaders.add(shader);
				shadersMap.put(shaderName, shader);
			}
			
			shader.processName(shaderName);
			StandardShaderEntry entry = new StandardShaderEntry();
			shader.entries.put(entryIndex, entry);
			
			File sourceVertexFile = sourceVSHFiles.get(stdName);
			File sourcePixelFile = sourcePSHFiles.get(stdName);
			
			if (packer != null) packer.setCurrentFile(sourceVertexFile);
			
			if (sourceVertexFile == null) {
				if (packer != null) packer.setCurrentFile(sourcePixelFile);
				throw new IOException("Missing vertex shader for '" + stdName + "'");
			}
			if (sourcePixelFile == null) {
				throw new IOException("Missing pixel shader for '" + stdName + "'");
			}
			
			shader.compile(entry, sourceVertexFile, sourcePixelFile, sourceFolder, packer);
		}
		
		if (precompiledVSHFile != null) {
			if (packer != null) packer.setCurrentFile(precompiledVSHFile);
			
			List<String> lines = Files.readAllLines(precompiledVSHFile.toPath());
			for (String line : lines) {
				int indexOf = line.indexOf("#");
				if (indexOf != -1) line = line.substring(indexOf);
				line = line.trim();
				if (line.isEmpty()) continue;
				
				String[] splits = line.split("\\s");
				int[] indices = new int[splits.length];
				for (int i = 0; i < splits.length; ++i) {
					indices[i] = vsMap.get(splits[i]);
				}
				try {
					compileShader(fragments, indices, true);
				}
				catch (Exception e) {
					e.printStackTrace();
					throw new IOException("Error compiling \"" + Arrays.toString(splits) + "\": " + e.getMessage());
				}
			}
		}
		
		if (precompiledPSHFile != null) {
			if (packer != null) packer.setCurrentFile(precompiledPSHFile);
			
			List<String> lines = Files.readAllLines(precompiledPSHFile.toPath());
			for (String line : lines) {
				int indexOf = line.indexOf("#");
				if (indexOf != -1) line = line.substring(indexOf);
				line = line.trim();
				if (line.isEmpty()) continue;
				
				String[] splits = line.split("\\s");
				int[] indices = new int[splits.length];
				for (int i = 0; i < splits.length; ++i) {
					indices[i] = psMap.get(splits[i]);
				}
				try {
					compileShader(fragments, indices, false);
				}
				catch (Exception e) {
					e.printStackTrace();
					throw new IOException("Error compiling \"" + Arrays.toString(splits) + "\": " + e.getMessage());
				}
			}
		}
	}
	
	public void compileShader(ShaderFragments fragments, int[] build, boolean isVertexShader) throws IOException, InterruptedException {
		File input = File.createTempFile("SporeModderFX-shader-builder", ".hlsl");
		File output = File.createTempFile("SporeModderFX-shader-builder", ".hlsl.obj");
		
		Path outputPath = output.toPath();
		List<ShaderDataUniform> uniforms;
		
		try (BufferedWriter out = new BufferedWriter(new FileWriter(input))) {
			
			if (isVertexShader) {
				List<VertexShaderFragment> list = new ArrayList<>();
				for (int i : build) {
					list.add(fragments.vertexFragments.get(i));
				}
				uniforms = VertexShaderFragment.generateHLSL(out, list);
			}
			else {
				List<PixelShaderFragment> list = new ArrayList<>();
				for (int i : build) {
					list.add(fragments.pixelFragments.get(i));
				}
				uniforms = PixelShaderFragment.generateHLSL(out, list);
			}
		}
		
		FXCompiler.get().compile(isVertexShader ? FXCompiler.VS_PROFILE : FXCompiler.PS_PROFILE, input, null, output);
		
		CompiledShader shader = new CompiledShader();
		System.arraycopy(build, 0, shader.fragmentIndices, 0, build.length);
		shader.data = Files.readAllBytes(outputPath);
		
		int register = 0;
		for (ShaderDataUniform uniform : uniforms) {
			shader.dataUniforms.add(uniform);
			shader.startRegisters.add(register);
			shader.dataFlags |= uniform.flags;
			
			register += uniform.registerSize;
		}
		
		if (isVertexShader) {
			vertexShaders.add(shader);
		}
		else {
			pixelShaders.add(shader);
		}
		
		input.delete();
		output.delete();
	}
	

	public void compileShaders(ShaderFragments fragments, List<int[]> indices, boolean isVertexShader, Set<int[]> compiledSet) throws IOException, InterruptedException {
		File input = File.createTempFile("SporeModderFX-shader-builder", ".hlsl");
		File output = File.createTempFile("SporeModderFX-shader-builder", ".hlsl.obj");
		Path outputPath = output.toPath();
		
		for (int[] build : indices) {
			if (compiledSet.contains(build)) continue;
			
			List<ShaderDataUniform> uniforms;
			
			try (BufferedWriter out = new BufferedWriter(new FileWriter(input))) {
				
				if (isVertexShader) {
					List<VertexShaderFragment> list = new ArrayList<>();
					for (int i : build) {
						if (i == 0) break;
						list.add(fragments.vertexFragments.get(i));
					}
 					uniforms = VertexShaderFragment.generateHLSL(out, list);
				}
				else {
					List<PixelShaderFragment> list = new ArrayList<>();
					for (int i : build) {
						if (i == 0) break;
						list.add(fragments.pixelFragments.get(i));
					}
					uniforms = PixelShaderFragment.generateHLSL(out, list);
				}
			}
			
			Files.copy(input.toPath(), new File("C:\\Users\\Eric\\Desktop\\shader.hlsl").toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println(CompiledShader.getSignatureString(build));
			
			try {
				FXCompiler.get().compile(isVertexShader ? FXCompiler.VS_PROFILE : FXCompiler.PS_PROFILE, input, null, output);
			} catch (Exception e) {
				//TODO
				System.err.println("ERROR COMPILING");
			}
			
			CompiledShader shader = new CompiledShader();
			System.arraycopy(build, 0, shader.fragmentIndices, 0, shader.fragmentIndices.length);
			compiledSet.add(shader.fragmentIndices);
			shader.data = Files.readAllBytes(outputPath);
			
			int register = 0;
			for (ShaderDataUniform uniform : uniforms) {
				shader.dataUniforms.add(uniform);
				shader.startRegisters.add(register);
				shader.dataFlags |= uniform.flags;
				
				register += uniform.registerSize;
			}
			
			if (isVertexShader) {
				vertexShaders.add(shader);
			}
			else {
				pixelShaders.add(shader);
			}
		}
		
		input.delete();
		output.delete();
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		String fragmentsPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_shader_fragments~\\0x00000003.smt";
		String shadersPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_shaders~\\0x00000003.smt";
		
		String outputPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Materials\\materials_shaders~\\0x00000003.smt.unpacked\\" + PRECOMPILED_VSH_FILE;
		
		try (FileStream fragmentsIn = new FileStream(fragmentsPath, "r");
				FileStream shadersIn = new FileStream(shadersPath, "r"); 
				BufferedWriter output = new BufferedWriter(new FileWriter(outputPath)))
		{
			ShaderFragments fragments = new ShaderFragments();
			fragments.read(fragmentsIn, null, null);
			
			CompiledShaders shaders = new CompiledShaders();
			shaders.read(shadersIn);
			
			for (CompiledShader shader : shaders.vertexShaders) {
				StringBuilder sb = new StringBuilder();
				for (int i : shader.fragmentIndices) {
					if (i == 0) break;
					sb.append(Integer.toHexString(i));
					sb.append(' ');
				}
				output.write(sb.toString());
				output.newLine();
			}
		}
	}
}
