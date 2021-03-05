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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.dbpf.DBPFPacker;

public class ShaderFragments {
	private static final int SHADER_COUNT = 255;
	private static final int DS_SHADER_COUNT = 1023;
	
	public final List<VertexShaderFragment> vertexFragments = new ArrayList<>();
	public final List<PixelShaderFragment> pixelFragments = new ArrayList<>();
	public int version = 1;
	
	public int getIndex(String name, boolean isVertex) {
		if (isVertex) {
			for (int i = 0; i < vertexFragments.size(); ++i) {
				if (name.equals(vertexFragments.get(i).shaderName)) return i + 1;
			}
			return -1;
		}
		else {
			for (int i = 0; i < pixelFragments.size(); ++i) {
				if (name.equals(pixelFragments.get(i).shaderName)) return i;
			}
			return -1;
		}
	}
	
	public void read(StreamReader in, Map<String, Integer> vsMap, Map<String, Integer> psMap) throws IOException {
		version = in.readInt();
		
		int count = SHADER_COUNT;
		for (int i = 0; i < count; i++) {
			
			VertexShaderFragment shader = new VertexShaderFragment();
			shader.read(in);
			if ((shader.flags & ShaderFragment.FLAG_DEFINED) != 0) {
				vertexFragments.add(shader);
				
				if (vsMap != null) vsMap.put(shader.shaderName, i);
				
				if (i == SHADER_COUNT-1) {
					// Some wild guess. Darkspore uses more shaders. If we arrived to the last one and it's still defined,
					// probably there are more
					count = DS_SHADER_COUNT;
				}
			}
		}
		
		for (int i = 0; i < count; i++) {
			
			PixelShaderFragment shader = new PixelShaderFragment();
			shader.read(in);
			if ((shader.flags & VertexShaderFragment.FLAG_DEFINED) != 0) {
				pixelFragments.add(shader);
				
				if (psMap != null) psMap.put(shader.shaderName, i);
			}
		}
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeInt(version);
		
		VertexShaderFragment emptyVertex = new VertexShaderFragment();
		PixelShaderFragment emptyPixel = new PixelShaderFragment();
		
		for (VertexShaderFragment fragment : vertexFragments) {
			fragment.write(out);
		}
		for (int i = vertexFragments.size(); i < SHADER_COUNT; ++i) emptyVertex.write(out);
		
		for (PixelShaderFragment fragment : pixelFragments) {
			fragment.write(out);
		}
		for (int i = pixelFragments.size(); i < SHADER_COUNT; ++i) emptyPixel.write(out);
	}
	
//	public void writeHLSL(File outputFolder) throws IOException {
//		outputFolder.mkdir();
//		for (ShaderFragment shader : shaders) {
//			if (shader.getName() == null || shader.getName().isEmpty()) return;
//			File file = new File(outputFolder, shader.getName() + ".hlsl");
//			
//			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//				shader.writeHLSL(writer);
//			}
//		}
//	}
	
	public void toArgScript(File outputFolder) throws FileNotFoundException {
		outputFolder.mkdir();
		for (int i = 0; i < vertexFragments.size(); ++i) {
			VertexShaderFragment shader = vertexFragments.get(i);
			if (shader.getName() == null || shader.getName().isEmpty()) return;
			File file = new File(outputFolder, String.format("%03d(%s).vertex_fragment", i, shader.getName()));
			
			ArgScriptWriter writer = new ArgScriptWriter();
			shader.toArgScript(writer);
			writer.write(file);
		}
		
		for (int i = 0; i < pixelFragments.size(); ++i) {
			PixelShaderFragment shader = pixelFragments.get(i);
			if (shader.getName() == null || shader.getName().isEmpty()) return;
			File file = new File(outputFolder, String.format("%03d(%s).pixel_fragment", i, shader.getName()));
			
			ArgScriptWriter writer = new ArgScriptWriter();
			shader.toArgScript(writer);
			writer.write(file);
		}
	}
	
	public void load(File inputFolder, Map<String, Integer> vsMap, Map<String, Integer> psMap, DBPFPacker packer) throws IOException {
		File[] files = inputFolder.listFiles();
		
		List<File> vertexFiles = new ArrayList<>();
		List<File> pixelFiles = new ArrayList<>();
		
		for (File file : files) {
			if (packer != null) packer.setCurrentFile(file);
			
			if (file.getName().endsWith(".vertex_fragment")) {
				vertexFiles.add(file);
			} 
			else if (file.getName().endsWith(".pixel_fragment")) {
				pixelFiles.add(file);
			}
			else {
				throw new IOException("Wrong name format in \"" + file.getName() + "\", unrecognised extension.");
			}
		}
		
		if (vertexFiles.size() > SHADER_COUNT) throw new IOException("Cannot store more than " + SHADER_COUNT + " vertex shader fragments.");
		if (pixelFiles.size() > SHADER_COUNT) throw new IOException("Cannot store more than " + SHADER_COUNT + " pixel shader fragments.");
		
		vertexFragments.clear();
		pixelFragments.clear();

		for (int i = 0; i < vertexFiles.size(); ++i) vertexFragments.add(new VertexShaderFragment());
		for (int i = 0; i < pixelFiles.size(); ++i) pixelFragments.add(new PixelShaderFragment());
		
		// Those files that don't have index
		List<File> extraFiles = new ArrayList<>();
		int maxIndex = 0;
		
		ShaderFragmentUnit parseUnit = new ShaderFragmentUnit();
		ArgScriptStream<ShaderFragmentUnit> stream = parseUnit.generateStream(); 
		
		for (File file : vertexFiles) {
			if (packer != null) packer.setCurrentFile(file);
			
			String name = file.getName();
			int indexOf = name.indexOf("(");
			if (indexOf == -1) {
				extraFiles.add(file);
			} else {
				int endIndex = name.indexOf(")");
				if (indexOf == -1) throw new IOException("Wrong name format in \"" + name + "\", missing ending ')'.");
				
				int fragmentIndex;
				try {
					fragmentIndex = Integer.parseInt(name.substring(0, indexOf));
				} catch (Exception e) {
					throw new IOException("Cannot parse fragment number: " + e.getLocalizedMessage());
				}
				if (fragmentIndex > maxIndex) maxIndex = fragmentIndex;
				
				ShaderFragment fragment = vertexFragments.get(fragmentIndex);
				fragment.shaderName = name.substring(indexOf+1, endIndex);
				fragment.flags = ShaderFragment.FLAG_DEFINED | ShaderFragment.FLAG_NAME;
				
				vsMap.put(fragment.shaderName, fragmentIndex);
				
				parseUnit.setFragment(fragment);
				parseUnit.setRequiredType(VertexShaderFragment.KEYWORD);
				parseUnit.setAlreadyParsed(false);
				stream.process(file);
			}
		}
		
		++maxIndex;
		
		for (File file : extraFiles) {
			if (packer != null) packer.setCurrentFile(file);
			
			ShaderFragment fragment = vertexFragments.get(maxIndex);
			
			String name = file.getName();
			fragment.shaderName = name.substring(0, name.indexOf("."));
			fragment.flags = ShaderFragment.FLAG_DEFINED | ShaderFragment.FLAG_NAME;
			
			vsMap.put(fragment.shaderName, maxIndex);
			
			parseUnit.setFragment(fragment);
			parseUnit.setRequiredType(VertexShaderFragment.KEYWORD);
			parseUnit.setAlreadyParsed(false);
			stream.process(file);
			
			++maxIndex;
		}
		
		
		extraFiles.clear();
		maxIndex = 0;
		
		for (File file : pixelFiles) {
			if (packer != null) packer.setCurrentFile(file);
			
			String name = file.getName();
			int indexOf = name.indexOf("(");
			if (indexOf == -1) {
				extraFiles.add(file);
			} else {
				int endIndex = name.indexOf(")");
				if (indexOf == -1) throw new IOException("Wrong name format in \"" + name + "\", missing ending ')'.");
				
				int fragmentIndex;
				try {
					fragmentIndex = Integer.parseInt(name.substring(0, indexOf));
				} catch (Exception e) {
					throw new IOException("Cannot parse fragment number: " + e.getLocalizedMessage());
				}
				if (fragmentIndex > maxIndex) maxIndex = fragmentIndex;
				
				ShaderFragment fragment = pixelFragments.get(fragmentIndex);
				fragment.shaderName = name.substring(indexOf+1, endIndex);
				fragment.flags = ShaderFragment.FLAG_DEFINED | ShaderFragment.FLAG_NAME;
				
				psMap.put(fragment.shaderName, fragmentIndex);
				
				parseUnit.setFragment(fragment);
				parseUnit.setRequiredType(PixelShaderFragment.KEYWORD);
				parseUnit.setAlreadyParsed(false);
				stream.process(file);
			}
		}
		
		++maxIndex;
		
		for (File file : extraFiles) {
			if (packer != null) packer.setCurrentFile(file);
			
			ShaderFragment fragment = pixelFragments.get(maxIndex);
			
			String name = file.getName();
			fragment.shaderName = name.substring(0, name.indexOf("."));
			fragment.flags = ShaderFragment.FLAG_DEFINED | ShaderFragment.FLAG_NAME;
			
			psMap.put(fragment.shaderName, maxIndex);
			
			parseUnit.setFragment(fragment);
			parseUnit.setRequiredType(PixelShaderFragment.KEYWORD);
			parseUnit.setAlreadyParsed(false);
			stream.process(file);
			
			++maxIndex;
		}
	}
	
	public static ShaderFragments readShaderFragments(File file) throws IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			ShaderFragments shaders = new ShaderFragments();
			shaders.read(stream, null, null);
			return shaders;
		}
	}
}
