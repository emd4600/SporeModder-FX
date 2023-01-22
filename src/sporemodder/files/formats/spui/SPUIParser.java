package sporemodder.files.formats.spui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptParser;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;
import sporemodder.files.formats.spui.SPUIChannel.DISPLAY_TYPE;

public class SPUIParser implements ArgScriptParser {
	
	private static final Pattern MULTIDIMENSIONAL_PATTERN = Pattern.compile("\\[([0-9,.\\-\\s]*)\\]");

	@Override
	public ArgScriptType checkElement(String keyword, String line, int level) {
		if (level == 0) {
			return keyword.equals("block") ? ArgScriptType.BLOCK : ArgScriptType.COMMAND;
		}
		else if (level == 1) {
			return line.contains(" sections ") ? ArgScriptType.BLOCK : ArgScriptType.COMMAND;
		}
		else if (level == 2) {
			// sectionList blocks
			return ArgScriptType.BLOCK;
		}
		return ArgScriptType.COMMAND;
	}

	public static String[] parseList(String str) throws ArgScriptException {
		// [x, y, z]
		
		if (!str.startsWith("[") || !str.endsWith("]")) {
			throw new ArgScriptException("Wrong array formatting.");
		}
		
		String[] strings = str.substring(1, str.length()-1).split(",\\s*");
		
		return strings;
	}
	
	public static List<String[]> parseMultidimensionalList(String str) throws ArgScriptException {
		// [[0, 1], [2, 3]]
		
		if (!str.startsWith("[") || !str.endsWith("]")) {
			throw new ArgScriptException("Wrong array formatting.");
		}
		
		List<String[]> result = new ArrayList<String[]>();
		
		Matcher matcher = MULTIDIMENSIONAL_PATTERN.matcher(str.substring(1, str.length()-1));
		while (matcher.find()) {
			result.add(matcher.group(1).split(",\\s*"));
		}
		
		return result;
	}
	
	public static List<String[]> parseMultidimensionalList(String str, int count) throws ArgScriptException {
		// [[0, 1], [2, 3]]
		
		if (!str.startsWith("[") || !str.endsWith("]")) {
			throw new ArgScriptException("Wrong array formatting.");
		}
		
		List<String[]> result = new ArrayList<String[]>();
		
		Matcher matcher = MULTIDIMENSIONAL_PATTERN.matcher(str.substring(1, str.length()-1));
		while (matcher.find()) {
			String[] strings = matcher.group(1).split(",\\s*");
			if (strings.length != count) throw new ArgScriptException("Wrong number of arguments inside list. " + count + " expected.");
			result.add(strings);
		}
		
		return result;
	}
	
	public static String shortToString(short s, int channel) {
		SPUIChannel c = SPUIChannel.getChannel(channel);
		if (c != null) {
			if (c.displayType == DISPLAY_TYPE.HEX) {
				return "0x" + Integer.toHexString(s);
			}
			else if (c.displayType == DISPLAY_TYPE.DECIMAL) {
				return Short.toString(s);
			}
		}
		if (s > 10000 || s < -10000) {
			return "0x" + Integer.toHexString(s);
		}
		return Short.toString(s);
	}
	
	public static String intToString(int i, int channel) {
		SPUIChannel c = SPUIChannel.getChannel(channel);
		if (c != null) {
			if (c.displayType == DISPLAY_TYPE.HEX) {
				return "0x" + Integer.toHexString(i);
			}
			else if (c.displayType == DISPLAY_TYPE.DECIMAL) {
				return Integer.toString(i);
			}
		}
		if (i > 1000000 || i < -10000) {
			return "0x" + Integer.toHexString(i);
		}
		return Integer.toString(i);
	}
	
	public static String createShortList(short[] data, int channel) {
		StringBuilder b = new StringBuilder();
		
		if (data == null) {
            b.append("null");
			return b.toString();
		}
        int iMax = data.length - 1;
        if (iMax == -1) {
            b.append("[]");
            return b.toString();
        }

        b.append('[');
        for (int i = 0; ; i++) {
            b.append(shortToString(data[i], channel));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
	}
	
	public static String createIntList(int[] data, int channel) {
		StringBuilder b = new StringBuilder();
		
		if (data == null) {
            b.append("null");
			return b.toString();
		}
        int iMax = data.length - 1;
        if (iMax == -1) {
            b.append("[]");
            return b.toString();
        }

        b.append('[');
        for (int i = 0; ; i++) {
            b.append(intToString(data[i], channel));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
	}

	public static String createList(Object[] data) {
		StringBuilder b = new StringBuilder();
		
		if (data == null) {
            b.append("null");
			return b.toString();
		}
        int iMax = data.length - 1;
        if (iMax == -1) {
            b.append("[]");
            return b.toString();
        }

        b.append('[');
        for (int i = 0; ; i++) {
            b.append(data[i].toString());
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
	}
	
	public static String createVectorList(float[][] data) {
		StringBuilder b = new StringBuilder();
		
		if (data == null) {
            b.append("null");
			return b.toString();
		}
        int iMax = data.length - 1;
        if (iMax == -1) {
            b.append("[]");
            return b.toString();
        }

        b.append('[');
        for (int i = 0; ; i++) {
            b.append(Arrays.toString(data[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
	}
	
	public static String createVectorList(int[][] data) {
		StringBuilder b = new StringBuilder();
		
		if (data == null) {
            b.append("null");
			return b.toString();
		}
        int iMax = data.length - 1;
        if (iMax == -1) {
            b.append("[]");
            return b.toString();
        }

        b.append('[');
        for (int i = 0; ; i++) {
            b.append(Arrays.toString(data[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
	}
	
	public static void main(String[] args) {
		Matcher matcher = MULTIDIMENSIONAL_PATTERN.matcher("[145.0, -11.0, 130.0, 14.0], [10.0, 10.0]");
		while (matcher.find()) {
			System.out.println(matcher.group(1));
		}
	}
}
