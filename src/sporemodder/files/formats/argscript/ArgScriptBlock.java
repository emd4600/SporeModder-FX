package sporemodder.files.formats.argscript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;

public class ArgScriptBlock extends ArgScriptOptionable {
//	private int startLine;
//	private int endLine;
	// we could use a LinkedHashMap to preserve the order, but it's isn't really needed since it's preserved in the "data" list
	private LinkedHashMap<String, ArgScriptCommand> commands = new LinkedHashMap<String, ArgScriptCommand>();
	private LinkedHashMap<String, ArgScriptBlock> blocks = new LinkedHashMap<String, ArgScriptBlock>();
	
	private List<String> data = new ArrayList<String>();;  // ordered data to appear
	
	public ArgScriptBlock(String str) {
//		this.startLine = startLine;
//		this.endLine = endLine;
		
		String[] strings = str.split("\\s-(?=[a-zA-Z])");
		splitArguments(strings[0]);
		
		options = new LinkedHashMap<String, ArgScriptOption>(strings.length-1);
		for (int i = 1; i < strings.length; i++) {
			ArgScriptOption option = new ArgScriptOption(strings[i].trim());
			options.put(option.getKeyword(), option);
		}
		
		// We leave reading the commands and blocks to the parser implementation
	}
	
	public ArgScriptBlock(String keyword, String ... arguments) {
		splits = new ArrayList<String>();
		splits.add(keyword);
		
		for (String arg : arguments) {
			splits.add(arg);
		}
	}

	private static String getIndent(int indentLevel) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentLevel; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}
	
	@Override
	@Deprecated
	public String toString() {
		// We don't want people to use this method;
		System.err.println("Deprecated method. Use ArgScriptBlock.toString(int indentLevel) instead!");
		return null;
	}
	
	public String toString(int indentLevel) {
		String indent = getIndent(indentLevel);
		String commandIndent = getIndent(indentLevel + 1);
		String lineSeparator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder(indent);
		
		int size = splits.size();
		for (int i = 0; i < size; i++) {
			sb.append(splits.get(i));
			if (i != size-1) sb.append(" ");
		}
		for (ArgScriptOption option : options.values()) {
			sb.append(" -" + option.toString());
		}
		
		if (comment != null) {
			sb.append("  // ");
			sb.append(comment);
		}
		
		sb.append(lineSeparator);
		
		for (String keyword : data) {
			if (keyword == null || keyword.length() == 0) {
				// write an empty line
				sb.append(lineSeparator);
			}
			else {
				ArgScriptCommand command = commands.get(keyword);
				if (command != null) {
					sb.append(commandIndent + command.toString());
				}
				else {
					// there's no command with this keyword, write a block instead
					ArgScriptBlock block = blocks.get(keyword);
					if (block != null) {
						sb.append(block.toString(indentLevel + 1));
					}
					else {
						// this isn't neither a command nor a block; just write it
						sb.append(keyword);
					}
				}
				sb.append(lineSeparator);
			}
		}
		
		sb.append(indent);
		sb.append("end");
		//sb.append(lineSeparator);
		
		return sb.toString();
	}
	
//	public void readCommands(ArgScript parser) {
//		for (int i = startLine+1; i < endLine; i++) {
//			// Remove comments and empty lines
//			String str = parser.getLine(i).split("//")[0].trim();
//			if (str.length() == 0) {
//				continue;
//			}
//			
//			ArgScriptCommand command = new ArgScriptCommand(str);
//			data.add(command.getKeyword());
//			commands.put(command.getKeyword(), command);
//		}
//	}
	
//	public void readUntilEnd(ArgScript parser) {
//		String line = null;
//		while((line = parser.readLine()) != null) {
//			String str = line.split("//")[0].trim();
//			if (str.length() == 0) {
//				continue;
//			}
//			
//			if (str.equals("end")) {
//				endLine = parser.getCurrentLine() - 1;
//				break;
//			}
//			else {
//				putCommand(new ArgScriptCommand(line));
//			}
//		}
//	}
	
	public ArgScriptCommand getCommand(String keyword) {
		return commands.get(keyword);
	}
	
	public Collection<ArgScriptCommand> getAllCommands() {
		return commands.values();
	}
	
	public ArgScriptCommand putCommand(String keyword, ArgScriptCommand command) {
		data.add(keyword);
		return commands.put(keyword, command);
	}
	
	public ArgScriptCommand putCommand(ArgScriptCommand command) {
		String keyword = command.getKeyword();
		if (data.contains(keyword)) {
			keyword += command.hashCode();
		}
		data.add(keyword);
		return commands.put(keyword, command);
	}
	
	public ArgScriptOptionable putOptionable(ArgScriptOptionable optionable) {
		if (optionable.getType() == ArgScriptType.COMMAND) {
			return putCommand((ArgScriptCommand) optionable);
		} else if (optionable.getType() == ArgScriptType.BLOCK) {
			return putBlock((ArgScriptBlock) optionable);
		} else {
			throw new UnsupportedOperationException("Only command or block supported in ArgScript.");
		}
	}
	
	public ArgScriptBlock getBlock(String keyword) {
		return blocks.get(keyword);
	}
	public Collection<ArgScriptBlock> getAllBlocks() {
		return blocks.values();
	}
	
	public ArgScriptBlock putBlock(String keyword, ArgScriptBlock block) {
		data.add(keyword);
		return blocks.put(keyword, block);
	}
	
	public ArgScriptBlock putBlock(ArgScriptBlock block) {
		String keyword = block.getKeyword();
		if (data.contains(keyword)) {
			keyword += block.hashCode();
		}
		data.add(keyword);
		return blocks.put(keyword, block);
	}
	
//	public int getStartLine() {
//		return startLine;
//	}
//
//	public int getEndLine() {
//		return endLine;
//	}

	@Override
	public String getKeyword() {
		return splits.get(0);
	}
	
	public void addBlankLine() {
		data.add(null);
	}
	
	public void addLine(String line) {
		data.add(line);
	}

	public void setCommands(LinkedHashMap<String, ArgScriptCommand> commands) {
		this.commands = commands;
		for (String keyword : commands.keySet()) {
			data.add(keyword);
		}
	}
	
	public void setBlocks(LinkedHashMap<String, ArgScriptBlock> blocks) {
		this.blocks = blocks;
		for (String keyword : blocks.keySet()) {
			data.add(keyword);
		}
	}
	
	@Override
	public ArgScriptType getType() {
		return ArgScriptType.BLOCK;
	}
	
	
	public List<ArgScriptOptionable> getAllOptionables() {
		List<ArgScriptOptionable> optionables = new ArrayList<ArgScriptOptionable>();
		for (String keyword : data) {
			ArgScriptCommand command = commands.get(keyword);
			if (command != null) {
				optionables.add(command);
			}
			else {
				// there's no command with this keyword, write a block instead
				ArgScriptBlock block = blocks.get(keyword);
				if (block != null) {
					optionables.add(block);
				}
			}
		}
		return optionables;
	}
}
