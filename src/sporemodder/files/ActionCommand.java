package sporemodder.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.formats.ConvertAction;
import sporemodder.utilities.InputOutputPaths;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class ActionCommand {

	public static void parseDefaultArguments(String[] args, List<String> inputs, List<String> outputs) {
		int index = 0;
		while (index < args.length) {
			if (args[index].equals(ConvertAction.KEYWORD_INPUTS)) {
				index++;
				while (index < args.length && !args[index].startsWith("-")) {
					inputs.add(args[index++]);
				}
			} else if (args[index].equals(ConvertAction.KEYWORD_OUTPUTS)) {
				index++;
				while (index < args.length && !args[index].startsWith("-")) {
					outputs.add(args[index++]);
				}
			} else {
				// ignore argument or do whatever you want with it
				index++;
			}
		}
	}
	
	public static List<InputOutputPair> parseDefaultArguments(String[] args, String inputExtension, String outputExtension, boolean removeExtension) {
		List<String> inputs = new ArrayList<String>();
		List<String> outputs = new ArrayList<String>();
		parseDefaultArguments(args, inputs, outputs);
		
		if (inputs.size() == 0) {
			return null;
		}
		
		List<InputOutputPair> pairs = null;
		if (outputs.size() > 0) {
			String[] outputArray = (String[]) outputs.toArray(new String[outputs.size()]);
			pairs = InputOutputPaths.parsePairs(
					(String[]) inputs.toArray(new String[inputs.size()]), 
					outputArray, 
					inputExtension, 
					outputExtension, 
					removeExtension);
		} else {
			pairs = new ArrayList<InputOutputPair>();
			for (String s : inputs) {
				String name = s;
				if (removeExtension) {
					name = name.substring(0, name.indexOf("."));
				}
				name += "." + outputExtension;
				pairs.add(new InputOutputPair(s, name));
			}
		}
		
		return pairs;
	}
	
	public static File replaceFileExtension(File file, String extension) {
		String name = file.getName();
		int index = name.indexOf(".");
		if (index != -1) {
			name = name.substring(0, index);
		}
		name += extension;
		
		return new File(file.getParentFile(), name);
	}
}
