package sporemodder.files.formats.argscript;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import sporemodder.files.formats.effects.EffectColor;

public class ArgScript {
	public enum ArgScriptType {BLOCK, COMMAND, NONE};
	
	public interface ArgScriptParser {
		public ArgScriptType checkElement(String keyword, String line, int level);
	}
	
	public interface ArgScriptArgumentLimiter {
		public int getArgumentLimit(String keyword, String line, int level, ArgScriptType type);
	}
	
	public static final String END_KEYWORD = "end";
	
	private List<String> lines = new ArrayList<String>();
	protected int currentLine = 0;
	//TODO Should ArgScript itself be a block?
	// we could use a LinkedHashMap to preserve the order, but it's isn't really needed since it's preserved in the "data" list
	private LinkedHashMap<String, ArgScriptCommand> commands = new LinkedHashMap<String, ArgScriptCommand>();
	private LinkedHashMap<String, ArgScriptBlock> blocks = new LinkedHashMap<String, ArgScriptBlock>();
	
	private List<String> data = new ArrayList<String>();  // ordered data to appear
	
	// The default parser only reads blocks, with no nesting
	private ArgScriptParser parser = new ArgScriptParser() {

		@Override
		public ArgScriptType checkElement(String keyword, String line, int level) {
			if (level == 0) {
				return ArgScriptType.BLOCK;
			}
			else {
				return ArgScriptType.COMMAND;
			}
		}
		
	};
	
	private ArgScriptArgumentLimiter argumentLimiter = null;
	
	public ArgScript() {
		
	}
	
	public ArgScript(BufferedReader in) throws IOException {
		read(in);
	}
	
	public ArgScript(File file) throws FileNotFoundException, IOException {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			read(in);
		}
	}
	
	public ArgScript(String path) throws FileNotFoundException, IOException {
		this(new File(path));
	}
	
	private void read(BufferedReader in) throws IOException {
		String line = null;
		while ((line = in.readLine()) != null) {
			lines.add(line);
		}
	}
	
	public String getLine(int line) {
		return lines.get(line);
	}
	
	public String readLine() {
		if (currentLine == lines.size()) return null;
		return lines.get(currentLine++);
	}
	
	public int getCurrentLine() {
		return currentLine;
	}
	
	public void setCurrentLine(int line) {
		currentLine = line;
	}
	
	public void setParser(ArgScriptParser parser) {
		this.parser = parser;
	}
	
	
	public ArgScriptParser getParser() {
		return parser;
	}

	public ArgScriptArgumentLimiter getArgumentLimiter() {
		return argumentLimiter;
	}

	public void setArgumentLimiter(ArgScriptArgumentLimiter argumentLimiter) {
		this.argumentLimiter = argumentLimiter;
	}

	@Override
	public String toString() {
		String lineSeparator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		
		for (String keyword : data) {
//			if (keyword == null || keyword.length() == 0) {
//				// write an empty line
//				sb.append(lineSeparator);
//			}
			if (keyword != null && keyword.length() != 0) {
				ArgScriptCommand command = commands.get(keyword);
				if (command != null) {
					sb.append(command.toString());
				}
				else {
					// there's no command with this keyword, write a block instead
					ArgScriptBlock block = blocks.get(keyword);
					if (block != null) {
						sb.append(block.toString(0));
					}
					else {
						// this isn't neither a command nor a block; just write it
						sb.append(keyword);
					}
				}
			}
			sb.append(lineSeparator);
		}
		
		return sb.toString();
	}
	
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
	
	
	private void readElement(ArgScriptBlock superBlock, int level) throws ArgScriptException {
		boolean isClosed = false;
		//String blockStr = null;
		//int startLine = -1;
		
		String line = null;
		while ((line = readLine()) != null) {
			// Remove comments and empty lines
			String str = line.split("//")[0].trim();
			if (str.length() == 0) {
				continue;
			}
			
			String keyword = str.split("\\s", 2)[0].trim();
			
			if (keyword.equals(END_KEYWORD)) {
				if (superBlock != null) {
					isClosed = true;
					break;
				}
			}
			
			ArgScriptType type = parser.checkElement(keyword, str, level);
			
			if (type == ArgScriptType.COMMAND) {
				if (superBlock != null) {
					if (argumentLimiter != null) {
						superBlock.putCommand(new ArgScriptCommand(str, argumentLimiter.getArgumentLimit(keyword, str, level, type)));
					}
					else {
						superBlock.putCommand(new ArgScriptCommand(str));
					}
				} 
				else {
					if (argumentLimiter != null) {
						putCommand(new ArgScriptCommand(str, argumentLimiter.getArgumentLimit(keyword, str, level, type)));
					}
					else {
						putCommand(new ArgScriptCommand(str));
					}
				}
			}
			else if (type == ArgScriptType.BLOCK) {
				ArgScriptBlock block = new ArgScriptBlock(str);
				readElement(block, level + 1);
				
				if (superBlock != null) {
					superBlock.putBlock(block);
				} 
				else {
					putBlock(block);
				}
			}
			else {
				if (superBlock != null) {
					superBlock.addLine(line);
				} 
				else {
					data.add(line);
				}
			}
		}
		
		if (!isClosed && superBlock != null) {
			throw new ArgScriptException("Unexpected end of block");
		}
	}
	
	public void addBlankLine() {
		data.add(null);
	}
	
	public void addLine(String line) {
		data.add(line);
	}
	
	public void parse() throws ArgScriptException {
		readElement(null, 0);
	}
	
	public void write(BufferedWriter out) throws IOException {
		out.write(toString());
	}
	
	public void write(File file) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) 
		{
			out.write(toString());
		}
	}
	
	public void write(String path) throws IOException {
		write(new File(path));
	}
	
	public static float parseRangedFloat(String str, float min, float max) throws ArgScriptException {
		float f = Float.parseFloat(str);
		
		if (f < min || f > max) {
			throw new ArgScriptException(String.format("Argument out of range: expecting [%g..%g], got %g", min, max, f));
		}
		
		return f;
	}
	
	public static float[] parseFloatList(String str) throws ArgScriptException {
		// (x, y, z)
		
		String[] strings = parseList(str);
		
		float[] result = new float[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i] = Float.parseFloat(strings[i]);
		}
		
		return result;
	}
	
	public static float[] parseFloatList(String str, int count) throws ArgScriptException {
		// (x, y, z)
		
		String[] strings = parseList(str, count);
		
		float[] result = new float[strings.length];
		for (int i = 0; i < strings.length; i++) {
			result[i] = Float.parseFloat(strings[i]);
		}
		
		return result;
	}
	
	public static float[] parseFloatList(String str, float[] dst) throws ArgScriptException {
		// (x, y, z)
		
		String[] strings = parseList(str, dst.length);
		
		for (int i = 0; i < dst.length; i++) {
			dst[i] = Float.parseFloat(strings[i]);
		}
		
		return dst;
	}
	
	public static String[] parseList(String str) throws ArgScriptException {
		// (x, y, z)
		
		if (!str.startsWith("(") || !str.endsWith(")")) {
			throw new ArgScriptException("Wrong array formatting.");
		}
		
		String[] strings = str.substring(1, str.length()-1).split(",\\s*");
		
		return strings;
	}
	
	public static String[] parseList(String str, int count) throws ArgScriptException {
		// (x, y, z)
		
		if (!str.startsWith("(") || !str.endsWith(")")) {
			throw new ArgScriptException("Wrong array formatting.");
		}
		
		String[] strings = str.substring(1, str.length()-1).split(",\\s*");
		
		if (strings.length != count) throw new ArgScriptException("Wrong number of arguments inside list. " + count + " expected.");
		
		return strings;
	}
	
	public static String createList(String ... strings) {
		if (strings == null) {
			return "null";
		}
		
		int iMax = strings.length -1;
		if (iMax == -1) {
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; ; i++) {
			sb.append(strings[i]);
			if (i == iMax) {
				return sb.append(")").toString();
			}
			sb.append(", ");
		}
	}
	
	public static String createFloatList(float ... floats) {
		if (floats == null) {
			return "null";
		}
		
		int iMax = floats.length -1;
		if (iMax == -1) {
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; ; i++) {
			sb.append(floats[i]);
			if (i == iMax) {
				return sb.append(")").toString();
			}
			sb.append(", ");
		}
	}
	
	public static String[] floatsToStrings(float ... floats) {
		String[] strings = new String[floats.length];
		for (int i = 0; i < floats.length; i++) {
			strings[i] = Float.toString(floats[i]);
		}
		return strings;
	}
	
	public static String[] intsToStrings(int ... ints) {
		String[] strings = new String[ints.length];
		for (int i = 0; i < ints.length; i++) {
			strings[i] = Integer.toString(ints[i]);
		}
		return strings;
	}
	
	public static String[] bytesToStrings(byte ... ints) {
		String[] strings = new String[ints.length];
		for (int i = 0; i < ints.length; i++) {
			strings[i] = Byte.toString(ints[i]);
		}
		return strings;
	}
	
	public static String[] vectorsToStrings(float[] ... vectors) {
		String[] strings = new String[vectors.length];
		for (int i = 0; i < vectors.length; i++) {
			strings[i] = createFloatList(vectors[i]);
		}
		return strings;
	}
	
	public static float[] stringsToFloats(List<String> args) {
		float[] arr = new float[args.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Float.parseFloat(args.get(i));
		}
		return arr;
	}
	
	public static int[] stringsToInts(List<String> args) {
		int[] arr = new int[args.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Integer.parseInt(args.get(i));
		}
		return arr;
	}
	
	public static byte[] stringsToBytes(List<String> args) {
		byte[] arr = new byte[args.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Byte.parseByte(args.get(i));
		}
		return arr;
	}
	
	public static boolean[] stringsToBooleans(List<String> args) {
		boolean[] arr = new boolean[args.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Boolean.parseBoolean(args.get(i));
		}
		return arr;
	}
	
	public static float[][] stringsToVectors(List<String> args) throws ArgScriptException {
		float[][] arr = new float[args.size()][];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = parseFloatList(args.get(i));
		}
		return arr;
	}
	
	public static String[] colorsToStrings(EffectColor ... args) {
		String[] result = new String[args.length];
		for (int i = 0; i < result.length; i++) result[i] = args[i].toString();
		return result;
	}
	
	public static EffectColor[] stringsToColors(List<String> args) throws ArgScriptException {
		EffectColor[] arr = new EffectColor[args.size()];
		for (int i = 0; i < arr.length; i++) {
			float[] floats = parseFloatList(args.get(i), 3);
			arr[i] = new EffectColor(floats[0], floats[1], floats[2]);
		}
		return arr;
	}
	
	// checks if the given array is worth (default { 1 }) 
	public static boolean isWorth(float[] array) {
		return array.length > 0 && (array[0] != 1.0f || array.length > 1);
	}
	public static boolean isWorth(float[] array, float vary, float worthValue) {
		return array.length > 0 && (array[0] != worthValue || array.length > 1 || vary != 0);
	}
	public static boolean isWorth(float[] array, float vary) {
		return array.length > 0 && (array[0] != 1.0f || array.length > 1 || vary != 0);
	}
	
	public static boolean isWorth(EffectColor[] array) {
		return array.length > 0 && (!array[0].isWhite() || array.length > 1);
	}
	public static boolean isWorth(EffectColor[] array, EffectColor vary) {
		return array.length > 0 && (!array[0].isWhite() || array.length > 1 || !vary.isBlack());
	}
}
