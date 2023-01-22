package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.Arrays;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.files.formats.spui.SPUIChannel.DISPLAY_TYPE;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;

public class SPUINumberSections {
	public static class SectionBoolean extends SPUISection {
		public static final int TYPE = 0x02;
		public static final String TEXT_CODE = "bool";
		
		public boolean[] data;
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}

		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new boolean[count];
			in.readBooleans(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeBooleans(data);
		}

		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new boolean[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Boolean.parseBoolean(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new boolean[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Boolean.parseBoolean(splits[i]);
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}

		public static boolean[] getValues(SectionBoolean section, boolean[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
	}
	
	public static class SectionByte extends SPUISection {
		public static final int TYPE = 0x07;
		public static final String TEXT_CODE = "byte";
		
		public byte[] data;
		
		public static byte[] getValues(SectionByte section, byte[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new byte[count];
			in.readBytes(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.write(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new byte[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Byte.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeByte(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new byte[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Byte.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeByte(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}
	}
	
	public static class SectionByte2 extends SPUISection {
		public static final int TYPE = 0x03;
		public static final String TEXT_CODE = "byte2";
		
		public byte[] data;
		
		public static byte[] getValues(SectionByte2 section, byte[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new byte[count];
			in.readBytes(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.write(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new byte[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Byte.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeByte(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new byte[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Byte.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeByte(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}
	}
	
	public static class SectionShort extends SPUISection {
		public static final int TYPE = 0x13;
		public static final String TEXT_CODE = "short";
		
		public short[] data;
		
		public static short[] getValues(SectionShort section, short[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		
		private String shortToString(short s) {
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
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
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
	            b.append(shortToString(data[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new short[count];
			in.readLEShorts(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (short s : data) {
				out.writeLEShort(s);
			}
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new short[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Short.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeShort(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new short[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Short.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeShort(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createShortList(data, channel));
		}
	}
	
	/**
	 * Probably, "int2" is a signed int, and "int" is an unsigned int.
	 *
	 */
	public static class SectionInt2 extends SPUISection {
		public static final int TYPE = 0x05;
		public static final String TEXT_CODE = "int2";
		
		public int[] data;
		
		private String intToString(int i) {
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
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
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
	            b.append(intToString(data[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new int[count];
			in.readLEInts(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEInts(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createIntList(data, channel));
		}

		public static int[] getValues(SectionInt2 section, int[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
	}
	
	public static class SectionInt extends SPUISection {
		public static final int TYPE = 0x09;
		public static final String TEXT_CODE = "int";
		
		public int[] data;
		
//		public static int[] getValues(SectionInt section, int[] defaultValues, int count) throws InvalidBlockException {
//			if (section != null) {
//				if (count != -1 && section.data.length != count) {
//					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
//				}
//				return section.data;
//			} else {
//				return defaultValues;
//			}
//		}
		
		public static int[] getValues(SPUISection section, int[] defaultValue, int count) throws InvalidBlockException {
			
			if (section == null) {
				return defaultValue;
			}
			
			if (section.getType() != SectionInt.TYPE) {
				throw new IllegalArgumentException("Given section is not of the expected type.");
			}
			
			if (count != -1 && section.getCount() != count) {
				throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
			}
			
			if (section instanceof SectionInt) {
				return ((SectionInt) section).data;
			}
			else {
				String[] strData = ((SectionIntName) section).data;
				int[] data = new int[strData.length];
				
				for (int i = 0; i < strData.length; i++) {
					data[i] = Hasher.decodeInt(strData[i]);
				}
				
				return data;
			}
		}
		
		public static int[] getValues(SPUISectionContainer block, int channel, int[] defaultValue, int count) throws InvalidBlockException {
			SPUISection section = block.getSection(channel);
			
			if (section == null) {
				return defaultValue;
			}
			
			if (section.getType() != SectionInt.TYPE) {
				throw new IllegalArgumentException("No section with channel " + Hasher.getName(channel, HashManager.get().getSpuiRegistry()) +
						" of type SectionInt found.");
			}
			
			if (count != -1 && section.getCount() != count) {
				throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
			}
			
			if (section instanceof SectionInt) {
				return ((SectionInt) section).data;
			}
			else {
				String[] strData = ((SectionIntName) section).data;
				int[] data = new int[strData.length];
				
				for (int i = 0; i < strData.length; i++) {
					data[i] = Hasher.decodeInt(strData[i]);
				}
				
				return data;
			}
		}
		
		private String intToString(int i) {
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
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
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
	            b.append(intToString(data[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new int[count];
			in.readLEInts(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEInts(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createIntList(data, channel));
		}
	}
	
	public static class SectionIntName extends SPUISection {
		public static final int TYPE = SectionInt.TYPE;
		public static final String TEXT_CODE = SectionInt.TEXT_CODE;
		
		public String[] data;
		
		public static String[] getValues(SPUISectionContainer block, int channel, String[] defaultValue, int count) throws InvalidBlockException {
			SPUISection section = block.getSection(channel);
			
			if (section == null) {
				return defaultValue;
			}
			
			if (section.getType() != SectionInt.TYPE) {
				throw new IllegalArgumentException("No section with channel " + Hasher.getName(channel, HashManager.get().getSpuiRegistry()) +
						" of type SectionInt found.");
			}
			
			if (count != -1 && section.getCount() != count) {
				throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
			}
			
			if (section instanceof SectionIntName) {
				return ((SectionIntName) section).data;
			}
			else {
				int[] intData = ((SectionInt) section).data;
				String[] data = new String[intData.length];
				
				for (int i = 0; i < intData.length; i++) {
					// if we don't do this, 'animations~' will appear. Showing nothing is preferrable
					if (intData[i] == 0) {
						data[i] = null;
					}
					else {
						data[i] = Hasher.getFileName(intData[i]);
					}
				}
				
				return data;
			}
		}
		
		private String createList() {
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
	        	if (data[i] == null || data[i].length() == 0) {
	        		b.append("0");
	        	}
	        	else if (data[i].startsWith("#") ||
	        			data[i].startsWith("0x")) {
	        		b.append(data[i]);
	        	}
	        	else {
	        		b.append("$");
		            b.append(data[i]);
	        	}
	        	
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
			b.append(createList());
			
			return b.toString();
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (String str : data) {
				out.writeLEInt(str == null ? 0 : Hasher.getFileHash(str));
			}
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new String[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = splits[i];
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new String[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = splits[i];
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, createList());
		}
	}
	
	public static class SectionFloat extends SPUISection {
		public static final int TYPE = 0x0B;
		public static final String TEXT_CODE = "float";
		
		public float[] data;
		
		public static float[] getValues(SectionFloat section, float[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new float[count];
			in.readLEFloats(data);
		}

		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEFloats(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new float[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Float.parseFloat(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new float[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Float.parseFloat(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}
	}
	
//	public static void main(String[] args) {
//		String str = "[0xFF, 127, -43]";
//		
//		SectionByte2 b = new SectionByte2();
//		b.parse(str);
//		System.out.println(b.getString());
//	}
}
