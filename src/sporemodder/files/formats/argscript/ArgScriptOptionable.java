package sporemodder.files.formats.argscript;

import java.util.LinkedHashMap;
import java.util.List;

import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;

public abstract class ArgScriptOptionable extends ArgScriptArgumentable {
	protected String comment;
	
	protected LinkedHashMap<String, ArgScriptOption> options = new LinkedHashMap<String, ArgScriptOption>();
	
	public ArgScriptOption getOption(String keyword) {
		return options.get(keyword);
	}
	
	public String getOptionArg(String keyword) throws ArgScriptException {
		ArgScriptOption option = options.get(keyword);
		if (option != null) {
			return option.getSingleArgument();
		}
		else {
			return null;
		}
	}
	
	public List<String> getOptionArgs(String keyword, int num) throws ArgScriptException {
		ArgScriptOption option = options.get(keyword);
		if (option != null) {
			return option.getArguments(num);
		}
		else {
			return null;
		}
	}
	
	public List<String> getOptionArgs(String keyword, int min, int max) throws ArgScriptException {
		ArgScriptOption option = options.get(keyword);
		if (option != null) {
			return option.getArguments(min, max);
		}
		else {
			return null;
		}
	}
	
	public ArgScriptOption putOption(String keyword, ArgScriptOption option) {
		return options.put(keyword, option);
	}
	public ArgScriptOption putOption(ArgScriptOption option) {
		String keyword = option.getKeyword();
		return options.put(keyword, option);
	}
	
	public ArgScriptOption putFlag(String keyword) {
		return options.put(keyword, new ArgScriptOption(keyword, new String[] {}));
	}
	
	public ArgScriptOption getArgOption(String keyword) throws ArgScriptException {
		ArgScriptOption o = options.get(keyword);
		if (o != null) {
			if (o.isFlag()) {
				throw new ArgScriptException(String.format("Option %s is expected to have arguments", o.getKeyword()));
			}
		}
		return o;
	}
	
	public ArgScriptOption getFlag(String keyword) throws ArgScriptException {
		ArgScriptOption o = options.get(keyword);
		if (o != null) {
			if (!o.isFlag()) {
				throw new ArgScriptException(String.format("Not expecting any arguments for flag %s", o.getKeyword()));
			}
		}
		return o;
	}
	
	public boolean hasFlag(String keyword) throws ArgScriptException {
		ArgScriptOption o = options.get(keyword);
		if (o != null) {
			if (!o.isFlag()) {
				throw new ArgScriptException(String.format("Not expecting any arguments for flag %s", o.getKeyword()));
			}
			return true;
		}
		return false;
	}
	
	public boolean hasOption(String keyword) throws ArgScriptException {
		ArgScriptOption o = options.get(keyword);
		return o != null;
	}
	
	public int getOptionCount() {
		return options.size();
	}
	
	public abstract ArgScriptType getType();

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
