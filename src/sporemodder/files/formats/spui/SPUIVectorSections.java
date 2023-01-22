package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;

public class SPUIVectorSections {
	public static class SectionVec4 extends SPUISection {
		public static final int TYPE = 0x10;
		public static final String TEXT_CODE = "vec4";
		
		public float[][] data;
		
		public static float[][] getValues(SectionVec4 section, float[][] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (section.data.length != count) {
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
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new float[count][4];
			for (int i = 0; i < count; i++) {
				data[i] = new float[4];
				in.readLEFloats(data[i]);
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (float[] f : data) {
				out.writeLEFloats(f);
			}
		}
		
		@Override
		public void parse(String str) {
			// [[x, y, z, w], [x, y, z, w], ...]
			// First, we remove [] and white spaces, then we split by comma only if it is followed by a [
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",(?=\\[)");
			
			data = new float[splits.length][4];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// [x, y, z, w]
				// We remove the [] and we split by comma
				String[] values = splits[i].substring(1, splits[i].length()-1).split(",");
				if (values.length != 4) {
					System.err.println("Wrong vec4 vector length!");
					return;
				}
				
				data[i] = new float[4];
				for (int j = 0; j < 4; j++) {
					data[i][j] = Float.parseFloat(values[j]);
				}
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			List<String[]> strings = SPUIParser.parseMultidimensionalList(as.getLastArgument(), 4);
			data = new float[strings.size()][4];
			count = data.length;
			
			for (int i = 0; i < count; i++) {
				data[i] = new float[4];
				String[] array = strings.get(i);
				for (int j = 0; j < 4; j++) {
					data[i][j] = Float.parseFloat(array[j]);
				}
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createVectorList(data));
		}
	}
	
	public static class SectionVec2 extends SPUISection {
		public static final int TYPE = 0x11;
		public static final String TEXT_CODE = "vec2";
		
		public float[][] data;
		
		public static float[][] getValues(SectionVec2 section, float[][] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (section.data.length != count) {
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
		
		@Override
		public void parse(String str) {
			// [[x, y], [x, y], ...]
			// First, we remove [] and white spaces, then we split by comma only if it is followed by a [
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",(?=\\[)");
			
			data = new float[splits.length][2];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// [x, y]
				// We remove the [] and we split by comma
				String[] values = splits[i].substring(1, splits[i].length()-1).split(",");
				if (values.length != 2) {
					System.err.println("Wrong vec2 vector length!");
					return;
				}
				
				data[i] = new float[2];
				for (int j = 0; j < 2; j++) {
					data[i][j] = Float.parseFloat(values[j]);
				}
			}
		}
		
		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			List<String[]> strings = SPUIParser.parseMultidimensionalList(as.getLastArgument(), 2);
			data = new float[strings.size()][2];
			count = data.length;
			
			for (int i = 0; i < count; i++) {
				data[i] = new float[2];
				String[] array = strings.get(i);
				for (int j = 0; j < 2; j++) {
					data[i][j] = Float.parseFloat(array[j]);
				}
			}
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new float[count][2];
			for (int i = 0; i < count; i++) {
				data[i] = new float[2];
				in.readLEFloats(data[i]);
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (float[] f : data) {
				out.writeLEFloats(f);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createVectorList(data));
		}
	}
	
	public static class SectionDimension extends SPUISection {
		public static final int TYPE = 0x0F;
		public static final String TEXT_CODE = "dimension";
		
		public int[][] data;
		
		public static int[][] getValues(SectionDimension section, int[][] defaultValues, int count) throws InvalidBlockException {
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
		
		@Override
		public void parse(String str) {
			// [[x, y], [x, y], ...]
			// First, we remove [] and white spaces, then we split by comma only if it is followed by a [
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",(?=\\[)");
			
			data = new int[splits.length][2];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// [x, y]
				// We remove the [] and we split by comma
				String[] values = splits[i].substring(1, splits[i].length()-1).split(",");
				if (values.length != 2) {
					System.err.println("Wrong dimension vector length!");
					return;
				}
				
				data[i] = new int[2];
				for (int j = 0; j < 2; j++) {
					data[i][j] = Integer.parseInt(values[j]);
				}
			}
		}
		
		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			List<String[]> strings = SPUIParser.parseMultidimensionalList(as.getLastArgument(), 2);
			data = new int[strings.size()][2];
			count = data.length;
			
			for (int i = 0; i < count; i++) {
				data[i] = new int[2];
				String[] array = strings.get(i);
				for (int j = 0; j < 2; j++) {
					data[i][j] = Integer.parseInt(array[j]);
				}
			}
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new int[count][2];
			for (int i = 0; i < count; i++) {
				data[i] = new int[2];
				in.readLEInts(data[i]);
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (int[] f : data) {
				out.writeLEInts(f);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createVectorList(data));
		}
	}
	
	public static void main(String[] args) {
		String str = "[[1.0, 3.25, 0, 1], [9, 24, 0, 7.4]]"; 
		
		SectionVec4 sec = new SectionVec4();
		sec.parse(str);
		System.out.println(sec.getString());
	}
}
