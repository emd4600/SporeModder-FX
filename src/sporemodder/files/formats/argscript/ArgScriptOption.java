package sporemodder.files.formats.argscript;

import java.util.ArrayList;

public class ArgScriptOption extends ArgScriptArgumentable {
	public ArgScriptOption(String str) {
		splitArguments(str);
	}
	
	public ArgScriptOption(String keyword, String ... args) {
		splits = new ArrayList<String>(args.length + 1);
		splits.add(keyword);
		
		for (String arg : args) {
			splits.add(arg);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int size = splits.size();
		for (int i = 0; i < size; i++) {
			sb.append(splits.get(i));
			if (i != size-1) sb.append(" ");
		}
		return sb.toString();
	}
	
	public String getKeyword() {
		return splits.get(0);
	}
	
	public boolean isFlag() {
		return splits.size() == 1;
	}
	
//	public List<String> getArguments(int num) throws ArgScriptException {
//		return getArguments(num, num);
//	}
}
