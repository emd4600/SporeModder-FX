/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.spui;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import javafx.scene.paint.Color;
import sporemodder.HashManager;
import sporemodder.file.LocalizedText;
import sporemodder.file.spui.uidesigner.DesignerClass;
import sporemodder.file.spui.uidesigner.SpuiDesigner;
import sporemodder.util.Vector2;

public class SpuiPropertyType {
	// types 0 and 1 do not exist
	public static final int TYPE_BOOLEAN = 2; 
	public static final int TYPE_BYTE = 3;
	public static final int TYPE_SHORT = 4;
	public static final int TYPE_INT = 5;
	public static final int TYPE_LONG = 6;
	public static final int TYPE_UBYTE = 7;
	public static final int TYPE_USHORT = 8;
	public static final int TYPE_UINT = 9;
	public static final int TYPE_ULONG = 10;
	public static final int TYPE_FLOAT = 11;
	public static final int TYPE_DOUBLE = 12;
	
	public static final int TYPE_DIMENSION = 15;
	public static final int TYPE_VECTOR4 = 16;
	public static final int TYPE_VECTOR2 = 17;
	public static final int TYPE_TEXT = 18;
	public static final int TYPE_REFERENCE = 19;
	public static final int TYPE_STRUCT = 20;
	
	@FunctionalInterface
	public interface PropertyReader {
		public Object read(StreamReader stream, int count) throws IOException;
	}
	
	@FunctionalInterface
	public interface PropertyWriter {
		public void write(StreamWriter stream, Object object) throws IOException;
	}
	
	public static final PropertyReader[] READERS = new PropertyReader[21];
	public static final PropertyWriter[] WRITERS = new PropertyWriter[21];
	
	static {
		READERS[TYPE_BOOLEAN] = (stream, count) -> {
			boolean[] result = new boolean[count];
			stream.readBooleans(result);
			return result;
		};
		WRITERS[TYPE_BOOLEAN] = (stream, object) -> stream.writeBooleans((boolean[]) object);
		
		READERS[TYPE_BYTE] = (stream, count) -> {
			byte[] result = new byte[count];
			stream.read(result);
			return result;
		};
		WRITERS[TYPE_BYTE] = (stream, object) -> stream.write((byte[]) object);
		
		READERS[TYPE_SHORT] = (stream, count) -> {
			int[] result = new int[count];
			for (int i = 0; i < count; ++i) result[i] = stream.readLEShort();
			return result;
		};
		WRITERS[TYPE_SHORT] = (stream, object) -> stream.writeLEShorts((int[]) object);
		
		READERS[TYPE_INT] = (stream, count) -> {
			int[] result = new int[count];
			stream.readLEInts(result);
			return result;
		};
		WRITERS[TYPE_INT] = (stream, object) -> stream.writeLEInts((int[]) object);
		
		READERS[TYPE_LONG] = (stream, count) -> {
			long[] result = new long[count];
			stream.readLELongs(result);
			return result;
		};
		WRITERS[TYPE_LONG] = (stream, object) -> stream.writeLELongs((long[]) object);
		
		READERS[TYPE_UBYTE] = (stream, count) -> {
			int[] result = new int[count];
			stream.readUBytes(result);
			return result;
		};
		WRITERS[TYPE_UBYTE] = (stream, object) -> stream.writeUBytes((int[]) object);
		
		READERS[TYPE_USHORT] = (stream, count) -> {
			int[] result = new int[count];
			for (int i = 0; i < count; ++i) result[i] = stream.readLEUShort();
			return result;
		};
		WRITERS[TYPE_USHORT] = (stream, object) -> stream.writeLEUShorts((int[]) object);
		
		READERS[TYPE_UINT] = (stream, count) -> {
			int[] result = new int[count];
			// Theorically we should use long, but uints are only used for IDs, and we use int for that
			stream.readLEInts(result);
			return result;
		};
		WRITERS[TYPE_UINT] = (stream, object) -> stream.writeLEInts((int[]) object);
		
		READERS[TYPE_ULONG] = (stream, count) -> {
			long[] result = new long[count];
			stream.readLELongs(result);
			return result;
		};
		WRITERS[TYPE_ULONG] = (stream, object) -> stream.writeLELongs((long[]) object);
		
		READERS[TYPE_FLOAT] = (stream, count) -> {
			float[] result = new float[count];
			stream.readLEFloats(result);
			return result;
		};
		WRITERS[TYPE_FLOAT] = (stream, object) -> stream.writeLEFloats((float[]) object);
		
		READERS[TYPE_DOUBLE] = (stream, count) -> {
			double[] result = new double[count];
			stream.readLEDoubles(result);
			return result;
		};
		WRITERS[TYPE_DOUBLE] = (stream, object) -> stream.writeLEDoubles((double[]) object);
		
		
		READERS[TYPE_DIMENSION] = (stream, count) -> {
			int[][] result = new int[count][2];
			for (int i = 0; i < count; ++i) {
				result[i] = new int[] {stream.readLEInt(), stream.readLEInt()};
			}
			return result;
		};
		WRITERS[TYPE_DIMENSION] = (stream, object) -> {
			int[][] array = (int[][])object;
			for (int[] a : array) stream.writeLEInts(a);
		};
		
		READERS[TYPE_VECTOR4] = (stream, count) -> {
			SPUIRectangle[] result = new SPUIRectangle[count];
			for (int i = 0; i < count; ++i) {
				result[i] = new SPUIRectangle();
				result[i].read(stream);
			}
			return result;
		};
		WRITERS[TYPE_VECTOR4] = (stream, object) -> {
			for (SPUIRectangle a : (SPUIRectangle[])object) a.write(stream);
		};
		
		READERS[TYPE_VECTOR2] = (stream, count) -> {
			Vector2[] result = new Vector2[count];
			for (int i = 0; i < count; ++i) {
				result[i] = new Vector2();
				result[i].readLE(stream);
			}
			return result;
		};
		WRITERS[TYPE_VECTOR2] = (stream, object) -> {
			for (Vector2 a : (Vector2[])object) a.writeLE(stream);
		};
		
		
		READERS[TYPE_TEXT] = (stream, count) -> {
			LocalizedText[] result = new LocalizedText[count];
			for (int i = 0; i < count; ++i) {
				int len = stream.readLEShort();
				if (len == -1) {
					result[i] = new LocalizedText(stream.readLEInt(), stream.readLEInt());
				}
				else {
					result[i] = new LocalizedText(stream.readString(StringEncoding.UTF16LE, len));
				}
			}
			return result;
		};
		WRITERS[TYPE_TEXT] = (stream, object) -> {
			for (LocalizedText a : (LocalizedText[])object) {
				if (a.getTableID() != 0 || a.getText() == null) {
					stream.writeLEShort(-1);
					stream.writeLEInts(a.getTableID(), a.getInstanceID());
				} else {
					stream.writeLEShort(a.getText().length());
					stream.writeString(a.getText(), StringEncoding.UTF16LE);
				}
			}
		};
		
		READERS[TYPE_REFERENCE] = (stream, count) -> {
			int[] result = new int[count];
			for (int i = 0; i < count; ++i) result[i] = stream.readLEShort();
			return result;
		};
		WRITERS[TYPE_REFERENCE] = (stream, object) -> stream.writeLEShorts((int[]) object);
	}
	
	public static Object read(StreamReader stream, int type, int count) throws IOException {
		return READERS[type].read(stream, count);
	}
	
	public static void write(StreamWriter stream, Object data, int type) throws IOException {
		WRITERS[type].write(stream, data);
	}
	
	private int type;
	private String text;
	private String typeName;
	private boolean isArray;
	private int arrayCount;
	
	public static SpuiPropertyType parse(SpuiDesigner designer, String text) {
		if (text == null) return null;
		
		SpuiPropertyType object = new SpuiPropertyType();
		object.text = text;
		
		int indexOf = text.indexOf("[");
		if (indexOf != -1) {
			object.isArray = true;
			if (text.charAt(indexOf+1) != ']') {
				object.arrayCount = Integer.parseInt(text.substring(indexOf+1, text.length()-1));
			}
			text = text.substring(0, indexOf);
		}
		
		object.typeName = text;
		if (!text.equals("flag")) {
			object.type = getType(text);
			if (object.type == -1) {
				// Might be class/interface (reference) or struct
				DesignerClass clazz = designer.getClass(text);
				if (clazz != null) {
					if (clazz.isStruct()) {
						object.type = TYPE_STRUCT;
					} else {
						object.type = TYPE_REFERENCE;
					}
				} else if (designer.hasInterface(text)) {
					object.type = TYPE_REFERENCE;
				}
				else if (!text.equals("image") && !text.equals("image_file")) {
					throw new IllegalArgumentException("Type " + object.text + " undefined.");
				}
			}
		}
		
		return object;
	}
		
	private static int getType(String text) {
		switch (text) {
		case "bool": return TYPE_BOOLEAN;
		case "int8": return TYPE_BYTE;
		case "int16": return TYPE_SHORT;
		case "int32": return TYPE_INT;
		case "int64": return TYPE_LONG;
		
		case "anchor":
		case "uint8": 
			return TYPE_UBYTE;
		case "uint16": return TYPE_USHORT;
		
		case "color":
		case "textstyle":
		case "flagset":
		case "uint32": 
			return TYPE_UINT;
		case "uint64": return TYPE_ULONG;
		case "float": return TYPE_FLOAT;
		case "double": return TYPE_DOUBLE;
		case "point": return TYPE_DIMENSION;  //?
		case "rectf": return TYPE_VECTOR4;
		case "pointf": return TYPE_VECTOR2;
		
		case "string":
		case "string_resource": 
			return TYPE_TEXT;
		// Special cases
		case "hitmask":
		case "image":
		case "image_file": 
			return TYPE_REFERENCE;
		default: return -1;
		}
	}

	public int getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public boolean isArray() {
		return isArray;
	}

	public int getArrayCount() {
		return arrayCount;
	}
	
	private Object processIntType(Object data, String format) {
		if (typeName.equals("color")) {
			int hexcode = (int) data;
			return Color.rgb(
					(hexcode & 0xFF0000) >> 16,
					(hexcode & 0xFF00) >> 8, 
					hexcode & 0xFF, 
					((hexcode & 0xFF000000L) >> 24) / 255.0);
		}
		else if (typeName.equalsIgnoreCase("textstyle")) {
			return StyleSheet.getActiveStyleSheet().getInstance((int) data);
		}
		else if (format != null && format.equals("id")) {
			return ((int)data) == 0 ? null : HashManager.get().getFileName((int) data);
		} else {
			return data;
		}
	}
	
	private int processIntTypeForWriting(Object data, String format) {
		if (typeName.equals("color")) {
			Color color = (Color) data;
			
			int r = (int) Math.round(color.getRed()*255);
			int g = (int) Math.round(color.getGreen()*255);
			int b = (int) Math.round(color.getBlue()*255);
			int a = (int) Math.round(color.getOpacity()*255);
			
			return (r << 16) | (g << 8) | b | (a << 24);
		}
		else if (typeName.equalsIgnoreCase("textstyle")) {
			if (data == null) return 0;
			else return ((StyleSheetInstance) data).getStyleID();
		}
		else if (format != null && format.equals("id")) {
			String string = (String) data;
			if (string == null) return 0;
			else return HashManager.get().getFileHash(string);
		} else {
			return data == null ? 0 : (int) data;
		}
	}
	
	private Object processReferenceType(Object data, SporeUserInterface spui) {
		if (typeName.equals("image") || typeName.equals("image_file")) {
			return spui.getSporeImage((int) data);
		}
		else {
			return spui.getObject((int) data);
		}
	}
	
	private int processReferenceTypeForWriting(Object data, SpuiWriter writer) {
		return writer.getIndex(data);
	}
	
	private Object createArray(int count) {
		switch (type) {
		case TYPE_BOOLEAN: return new boolean[count];
		case TYPE_BYTE: return new byte[count];
		case TYPE_UBYTE:
		case TYPE_SHORT:
		case TYPE_USHORT:
		case TYPE_INT:
		case TYPE_UINT: return new int[count];
		case TYPE_LONG:
		case TYPE_ULONG: return new long[count];
		case TYPE_FLOAT: return new float[count];
		case TYPE_DOUBLE: return new double[count];
		case TYPE_DIMENSION: return new int[count][2];
		case TYPE_VECTOR4: return new SPUIRectangle[count];
		case TYPE_VECTOR2: return new Vector2[count];
		case TYPE_TEXT: return new LocalizedText[count];
		case TYPE_REFERENCE: return new int[count];
		case TYPE_STRUCT: return new Object[count];
		default: return null;
		}
	}
	
	public void addComponents(SpuiWriter writer, SpuiElement element, Object data) {
		if (type != TYPE_REFERENCE || data == null) return;
		
		if (isArray) {
			if (arrayCount == 0) {
				List<?> list = (List<?>) data;
				for (Object value : list) {
					if (value instanceof InspectableObject) {
						InspectableObject dataElement = (InspectableObject) value;
						dataElement.addComponents(writer);
					}
				}
			} 
			else {
				for (int i = 0; i < arrayCount; ++i) {
					Object value = Array.get(data, i);
					if (value instanceof InspectableObject) {
						InspectableObject dataElement = (InspectableObject) value;
						dataElement.addComponents(writer);
					}
				}
			}
		}
		else {
			if (data instanceof InspectableObject) {
				InspectableObject dataElement = (InspectableObject) data;
				dataElement.addComponents(writer);
			}
		}
	}
	
	public Object processValueForWriting(SpuiWriter writer, SpuiElement element, Object data, String format) {
		if (isArray) {
			int count = arrayCount;
			if (arrayCount == 0) {
				List<?> list = (List<?>) data;
				data = list.toArray();
				count = list.size();
			}
			
			if (type == TYPE_UINT) {
				int[] array = new int[count];
				for (int i = 0; i < count; ++i) {
					array[i] = processIntTypeForWriting(Array.get(data, i), format);
				}
				return array;
			} else if (type == TYPE_REFERENCE) {
				int[] array = new int[count];
				for (int i = 0; i < count; ++i) {
					array[i] = processReferenceTypeForWriting(Array.get(data, i), writer);
				}
				return array;
			}
			else {
				return data;
			}
		} else {
			if (type == TYPE_UINT) data = processIntTypeForWriting(data, format);
			else if (type == TYPE_INT) data = ((Number)data).intValue();
			else if (type == TYPE_FLOAT) data = ((Number)data).floatValue();
			else if (type == TYPE_REFERENCE) data = processReferenceTypeForWriting(data, writer);
			
			Object array = createArray(1);
			Array.set(array, 0, data);
			return array;
		}
	}
	
	public Object processValue(SporeUserInterface spui, SpuiElement element, Object data, String format) {
		if (isArray) {
			if (arrayCount == 0) {
				List<Object> list = new ArrayList<Object>();
				int length = Array.getLength(data);
				for (int i = 0; i < length; ++i) {
					if (type == TYPE_UINT) list.add(processIntType(Array.get(data, i), format));
					else if (type == TYPE_REFERENCE) list.add(processReferenceType(Array.get(data, i), spui));
					else list.add(Array.get(data, i));
				}
				return list;
			} else {
				if (type == TYPE_UINT || type == TYPE_REFERENCE) {
					Object[] array = new Object[arrayCount];
					for (int i = 0; i < arrayCount; ++i) {
						if (type == TYPE_UINT) array[i] = processIntType(Array.get(data, i), format);
						else if (type == TYPE_REFERENCE) array[i] = processReferenceType(Array.get(data, i), spui);
					}
					return array;
				}
				else {
					// it's already an array
					return data;
				}
			}
		} else {
			data = Array.get(data, 0);
			if (type == TYPE_UINT) return processIntType(data, format);
			else if (type == TYPE_REFERENCE) return processReferenceType(data, spui);
			else return data;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(Field field, SpuiElement element, Object data) throws IllegalArgumentException, IllegalAccessException {
		if (isArray) {
			if (arrayCount == 0) {
				((List) field.get(element)).addAll((Collection) data);
			} else {
				System.arraycopy(data, 0, field.get(element), 0, arrayCount);
			}
		} else {
			if (type == TYPE_VECTOR4) {
				((SPUIRectangle) field.get(element)).copy((SPUIRectangle) data);
			}
			else if (type == TYPE_VECTOR2) {
				((Vector2) field.get(element)).set((Vector2) data);
			}
			else if (type == TYPE_DIMENSION) {
				System.arraycopy((int[])data, 0, ((int[]) field.get(element)), 0, 2);
			}
			else if (type == TYPE_TEXT) {
				((LocalizedText) field.get(element)).copy((LocalizedText) data);
			}
			else {
				field.set(element, data);
			}
		}
	}
	
}
