package sporemodder.files.formats;

import java.io.File;

import javax.swing.JPanel;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public interface ConvertAction {
	public static final String KEYWORD_INPUTS = "-i";
	public static final String KEYWORD_OUTPUTS = "-o";
	
	public FileFormatStructure convert(File input, File output) throws Exception;
	public FileFormatStructure convert(InputStreamAccessor input, OutputStreamAccessor output) throws Exception;
	public FileFormatStructure convert(InputStreamAccessor input, String outputPath) throws Exception;
	public FileFormatStructure convert(String inputPath, OutputStreamAccessor output) throws Exception;
	public FileFormatStructure process(File input) throws Exception;
	
	public boolean isValid(ResourceKey key);
	public boolean isValid(String extension);
	public boolean isValid(File file);
	
	public File getOutputFile(File file);
	public String getOutputExtension(String extension);
	public int getOutputExtensionID(String extension);
	
	public JPanel createOptionsPanel();
}
