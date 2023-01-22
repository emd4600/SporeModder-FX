package sporemodder.utilities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class InputOutputPaths {

	public static class InputOutputPair {
		public InputOutputPair(String input, String output) {
			this.input = new File(input);
			this.output = new File(output);
		}
		public InputOutputPair(File input, File output) {
			this.input = input;
			this.output = output;
		}
		public File input;
		public File output;
	}
	
	public static List<InputOutputPair> parsePairs(String[] inputPaths, String[] outputPaths, final String inExtension, String outExtension, boolean removeExtension) throws IllegalArgumentException {
		
		List<InputOutputPair> pairs = new ArrayList<InputOutputPair>();
		
		// Special case: multiple inputs, one folder output
		boolean specialCase = outputPaths.length == 1;
		File specialCaseFile = null;
		if (specialCase) {
			specialCaseFile = new File(outputPaths[0]);
			if (!specialCaseFile.isDirectory()) {
				specialCase = false;
			}
		}
		
		if (!specialCase && inputPaths.length != outputPaths.length) {
			throw new IllegalArgumentException("Not enough/Too many outputs");
		}
		
		for (int i = 0; i < inputPaths.length; i++) {
			File inFile = new File(inputPaths[i]);
			
			File outFile = specialCase ? specialCaseFile : new File(outputPaths[i]);
			
			if (inFile.getAbsolutePath().equals(outFile.getAbsolutePath()) && !outFile.isDirectory()) {
				throw new IllegalArgumentException("Output file is the same as the input file!");
			}
			
			if (inFile.isDirectory()) {
				if (!specialCase && !outFile.isDirectory()) {
					throw new IllegalArgumentException("Not enough/Too many outputs");
				}
				
				File[] inFiles = inFile.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File arg0, String arg1) {
						return arg1.endsWith("." + inExtension);
					}
				});
				
				for (File subinFile : inFiles) {
					if (specialCase) {
						String name = subinFile.getName();
						if (removeExtension) {
							name = name.substring(0, name.indexOf("."));
						}
						name += "." + outExtension;
						pairs.add(new InputOutputPair(subinFile, new File(outFile.getAbsolutePath() + "\\" + name)));
					} else {
						pairs.add(new InputOutputPair(subinFile, outFile));
					}
				}
			}
			else {
				if (specialCase) {
					pairs.add(new InputOutputPair(inFile, new File(outFile.getAbsolutePath() + "\\" + inFile.getName() + "." + outExtension)));
				} else {
					pairs.add(new InputOutputPair(inFile, outFile));
				}
			}
		}
		
		return pairs;
//		}
	}
	
	public static List<InputOutputPair> parsePairs(String input, String output, final String inExtension, String outExtension, boolean removeExtension) throws IllegalArgumentException {
//		if (input.equals(output)) {
//			throw new IllegalArgumentException("Output file is the same as the input file!");
//		}
//		else {
		
		//TODO convert from .prop.xml to .prop, not .prop.xml.prop
		
		String[] inputPaths = input.split("\\|");
		String[] outputPaths = output.split("\\|");
		
		return parsePairs(inputPaths, outputPaths, inExtension, outExtension, removeExtension);
	}
}
