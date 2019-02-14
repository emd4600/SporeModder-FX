package sporemodder.file.shaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import emord.filestructures.MemoryStream;
import emord.filestructures.Stream.StringEncoding;
import sporemodder.AbstractManager;
import sporemodder.MainApp;
import sporemodder.util.WinRegistry;

public class FXCompiler extends AbstractManager {
	
	public static FXCompiler get() {
		return MainApp.get().getFXCompiler();
	}
	
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
	
	public boolean autoDetectPath() {
		try {
			String path = WinRegistry.valueForKey(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\Microsoft\\Windows Kits\\Installed Roots", "KitsRoot10");
			
			File versionFolder = new File(path, "bin");
			for (File folder : versionFolder.listFiles()) {
				File file = new File(folder, "x86\\fxc.exe");
				if (file.exists()) {
					fxcFile = file;
					isAutoPath = true;
					return true;
				}
			}
			
			return false;
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public File compile(String targetProfile, File sourceHLSL, File includePath, File outputFile) throws IOException, InterruptedException {
		
		String command = String.format("\"%s\" /T %s /Fo \"%s\" /I \"%s\" \"%s\"", 
				fxcFile.getAbsolutePath(), targetProfile, outputFile.getAbsolutePath(), includePath.getAbsolutePath(), sourceHLSL.getAbsolutePath());
		
//		System.out.println(command);
//		Process p = Runtime.getRuntime().exec(command);
//		System.out.println(p.waitFor());
//		
//		ProcessBuilder builder = new ProcessBuilder(command);
//		builder.redirectOutput(Redirect.INHERIT);
//		builder.redirectError(Redirect.INHERIT);
//		
//		int result = builder.start().waitFor();
		
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
			throw new IOException("Cannot compile " + sourceHLSL.getName() + ": " + sb.toString());
		}
		
		return outputFile;
	}
	
	public File compile(String targetProfile, File sourceHLSL, File includePath) throws IOException, InterruptedException {
		return compile(targetProfile, sourceHLSL, includePath, File.createTempFile("SporeModderFX-compiled-shader", ".fxc.tmp"));
	}
	
	public void getUniformData(byte[] data, List<ShaderDataUniform> dst) throws IOException {
		
		try (MemoryStream stream = new MemoryStream(data)) {
			stream.seek(8);
			// CTAB
			if (stream.readInt() != 0x43544142) return;
			stream.skip(12);  // Size, Creator, Version
			int constCount = stream.readLEInt();
			int constOffset = stream.readLEInt();
			
			for (int i = 0; i < constCount; ++i) {
				stream.seek(constOffset + 12 + i*20);
				
				ShaderDataUniform uniform = new ShaderDataUniform();
				dst.add(uniform);
				
				int nameOffset = stream.readLEInt();
				stream.readShort();  // D3DXREGISTER_SET
				uniform.register = stream.readShort();
				uniform.registerSize = stream.readShort();
				
				stream.seek(12 + nameOffset);
				String name = stream.readCString(StringEncoding.ASCII);
				if (!ShaderData.hasIndex(name)) {
					throw new IOException(name + " is not a recognized shader data uniform.");
				}
				
				uniform.dataIndex = ShaderData.getIndex(name);
				uniform.field_2 = uniform.dataIndex;  //TODO not always like this!
				uniform.flags = ShaderData.getFlags(uniform.dataIndex);
			}
		}
	}
}
