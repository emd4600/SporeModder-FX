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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.AbstractManager;
import sporemodder.MainApp;

public class FXCompiler extends AbstractManager {
	
	public static FXCompiler get() {
		return MainApp.get().getFXCompiler();
	}
	
	public static final String VS_PROFILE = "vs_3_0";
	public static final String PS_PROFILE = "ps_3_0";
	
	private static final String PROPERTY_fxcFile = "fxcFile";

	private File fxcFile;
	private boolean isAutoPath;
	
	@Override public void initialize(Properties properties) {
		
		String path = properties.getProperty(PROPERTY_fxcFile, "");
		if (!path.isEmpty() && !path.equals("AUTO")) {
			fxcFile = new File(path);
		} else {
			autoDetectPath();
		}
	}

	@Override public void saveSettings(Properties properties) {
		properties.put(PROPERTY_fxcFile, (isAutoPath || fxcFile == null) ? "AUTO" : fxcFile.getAbsolutePath());
	}
	
	public File getFXCFile() {
		return fxcFile;
	}
	
	public void setFXCFile(File fxcFile) {
		this.fxcFile = fxcFile;
	}
	
	public boolean autoDetectPath() {
		try {
			String path = Preferences.systemRoot().get("SOFTWARE\\WOW6432Node\\Microsoft\\Windows Kits\\Installed Roots\\KitsRoot10", null);
			if (path != null) {
				File versionFolder = new File(path, "bin");
				if (versionFolder.exists()) {
					for (File folder : versionFolder.listFiles()) {
						File file = new File(folder, "x86\\fxc.exe");
						if (file.exists()) {
							fxcFile = file;
							isAutoPath = true;
							return true;
						}
					}
				}
			}
			
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Returns the error, if any
	private static String fxcCommand(String command) throws IOException, InterruptedException {
		String line;
		Process p = Runtime.getRuntime().exec(command);
		int result = p.waitFor();
		
		if (result != 0) {
			StringBuilder sb = new StringBuilder();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = input.readLine()) != null) {
				  sb.append(line);
				  sb.append('\n');
			}
			input.close();
			return sb.toString();
		} else {
			return null;
		}
	}
	
	public File decompile(String targetProfile, File inputFile) throws IOException, InterruptedException {
		return decompile(targetProfile, inputFile, File.createTempFile("SporeModderFX-decompiled-shader", ".asm.tmp"));
	}
	
	public File decompile(String targetProfile, File inputFile, File outputFile) throws IOException, InterruptedException {
		
		String command = String.format("\"%s\" /dumpbin /T %s /Fc \"%s\" \"%s\"", 
				fxcFile.getAbsolutePath(), targetProfile, outputFile.getAbsolutePath(), inputFile.getAbsolutePath());
		
		String error = fxcCommand(command);
		if (error != null) {
			throw new IOException("Cannot decompile " + inputFile.getName() + ": " + error);
		}
		
		return outputFile;
	}
	
	public File compile(String targetProfile, File sourceHLSL, File includePath, File outputFile) throws IOException, InterruptedException {

		if (fxcFile == null || !fxcFile.exists()) {
			throw new IOException("Cannot compile " + sourceHLSL.getName() + ": path to FXC.exe (shader compiler) is not set");
		}
		
		String command;
		if (includePath != null) {
			command = String.format("\"%s\" /Zi /T %s /Fo \"%s\" /I \"%s\" \"%s\"", 
					fxcFile.getAbsolutePath(), targetProfile, outputFile.getAbsolutePath(), includePath.getAbsolutePath(), sourceHLSL.getAbsolutePath());
		} else {
			command = String.format("\"%s\" /Zi /T %s /Fo \"%s\" \"%s\"", 
					fxcFile.getAbsolutePath(), targetProfile, outputFile.getAbsolutePath(), sourceHLSL.getAbsolutePath());
		}
		
		String error = fxcCommand(command);
		if (error != null) {
			throw new IOException("Cannot compile " + sourceHLSL.getName() + ": " + error);
		}
		
		return outputFile;
	}
	
	public File compile(String targetProfile, File sourceHLSL, File includePath) throws IOException, InterruptedException {
		return compile(targetProfile, sourceHLSL, includePath, File.createTempFile("SporeModderFX-compiled-shader", ".fxc.tmp"));
	}
	
	public void getUniformData(byte[] data, List<ShaderDataUniform> dst) throws IOException {
		
		try (MemoryStream stream = new MemoryStream(data)) {
			stream.skip(4);
			
			while (true) {
				if (stream.getFilePointer() >= stream.length()) return;
				stream.skip(2);
				int size = stream.readLEUShort();
				// CTAB
				if (stream.readInt() == 0x43544142) break;
				stream.skip(size*4 - 4);
			}
			
			long baseOffset = stream.getFilePointer();
			
			stream.skip(12);  // Size, Creator, Version
			int constCount = stream.readLEInt();
			int constOffset = stream.readLEInt();
			
			for (int i = 0; i < constCount; ++i) {
				stream.seek(constOffset + baseOffset + i*20);
				
				ShaderDataUniform uniform = new ShaderDataUniform();
				
				int nameOffset = stream.readLEInt();
				int registerSet = stream.readLEShort();  // D3DXREGISTER_SET
				if (registerSet == 3) continue; // sampler
				uniform.register = stream.readLEUShort();
				uniform.registerSize = stream.readLEUShort();
				stream.skip(8);
				int elements = stream.readLEShort();
				
				stream.seek(baseOffset + nameOffset);
				String name = stream.readCString(StringEncoding.ASCII);
				if (!ShaderData.hasIndex(name)) {
					throw new IOException(name + " is not a recognized shader data uniform.");
				}
				
				uniform.dataIndex = ShaderData.getIndex(name, elements != 1);
				uniform.field_2 = uniform.dataIndex;  //TODO not always like this!
				uniform.flags = ShaderData.getFlags(uniform.dataIndex);
				
				dst.add(uniform);
			}
		}
	}

	public boolean isAvailable() {
		return fxcFile != null && fxcFile.isFile();
	}
}
