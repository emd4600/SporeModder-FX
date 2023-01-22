package sporemodder.files.formats.argscript;

import java.util.ArrayList;

import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;

public class ArgScriptCommand extends ArgScriptOptionable {
	
	public ArgScriptCommand(String str) {
		String[] strings = str.split("\\s-(?=[a-zA-Z])");
		splitArguments(strings[0]);
		
		for (int i = 1; i < strings.length; i++) {
			ArgScriptOption option = new ArgScriptOption(strings[i].trim());
			options.put(option.getKeyword(), option);
		}
	}
	
	public ArgScriptCommand(String str, int argumentLimit) {
		String[] strings = str.split("\\s-(?=[a-zA-Z])");
		splitArguments(strings[0], argumentLimit + 1);
		
		for (int i = 1; i < strings.length; i++) {
			ArgScriptOption option = new ArgScriptOption(strings[i].trim());
			options.put(option.getKeyword(), option);
		}
	}
	
	public ArgScriptCommand(String keyword, String ... arguments) {
		splits = new ArrayList<String>();
		splits.add(keyword);
		
		for (String arg : arguments) {
			splits.add(arg);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int size = splits.size();
		for (int i = 0; i < size; i++) {
			sb.append(splits.get(i));
			if (i != size-1) {
				sb.append(" ");
			}
		}
		
		for (ArgScriptOption option : options.values()) {
			sb.append(" -");
			sb.append(option.toString());
		}
		
		if (comment != null) {
			sb.append("  // ");
			sb.append(comment);
		}
		
		return sb.toString();
	}
	
	@Override
	public String getKeyword() {
		return splits.get(0);
	}
	
	@Override
	public ArgScriptType getType() {
		return ArgScriptType.COMMAND;
	}
	
	
}
