package sporemodder.files.formats.argscript;

import java.util.ArrayList;
import java.util.List;

public abstract class ArgScriptArgumentable {
	//protected String keyword
	protected List<String> splits; 
	
	protected void splitArguments(String str, int limit) {
		// we want to avoid it splitting arrays like (0, 1, 2)
		String[] strings = str.trim().split("(?<!,)\\s+", limit);
		splits = new ArrayList<String>(strings.length);
		for (String s : strings) {
			splits.add(s);
		}
	}
	
	protected void splitArguments(String str) {
		// we want to avoid it splitting arrays like (0, 1, 2)
		String[] strings = str.trim().split("(?<!,)\\s+");
		splits = new ArrayList<String>(strings.length);
		for (String s : strings) {
			splits.add(s);
		}
	}
	
	public List<String> getArguments() {
		return splits.subList(1, splits.size());
	}
	
	public String getLastArgument() {
		return splits.get(splits.size() - 1);
	}
	
	public int getArgumentCount() {
		return splits.size() - 1;
	}
	
	public List<String> getArguments(int min, int max) throws ArgScriptException {
		int numArguments = splits.size() - 1;
		if (numArguments < min) {
			throw new ArgScriptException(String.format("Expecting at least %d arguments for option %s", min, splits.get(0)));
		}
		else if (numArguments > max) {
			throw new ArgScriptException(String.format("Expecting at most %d arguments for option %s", max, splits.get(0)));
		}
		
		return splits.subList(1, numArguments + 1);
	}
	
	public List<String> getArguments(int num) throws ArgScriptException {
		return getArguments(num, num);
	}
	
	public String getSingleArgument() throws ArgScriptException {
		return getArguments(1, 1).get(0);
	}
	
	public boolean addArgument(String argument) {
		return splits.add(argument);
	}
	
	public abstract String getKeyword();
}
