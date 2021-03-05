package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureEndian;

/**
 * A field with a numeric type that is read/written using an automatic method.
 */
class NumericFieldMetadata extends SimpleFieldMetadata {
	
	/** The attribute that tells whether this field is a unsigned integer or not. */
	FieldUnsigned unsignedAttribute;
	/** The attribute that decides the endianness of the field. */
	FieldEndian endianAttribute;
	
	/** The automatic method that reads and returns a single value from the file. */
	FieldReader reader;
	/** The automatic method that writes a single value to the file. */
	FieldWriter writer;

	
	@SuppressWarnings("unlikely-arg-type")
	NumericFieldMetadata(Field field, Class<?> type, Class<?> clazz, List<Annotation> annotations, StructureEndian endian) throws Exception {
		super(field, clazz, annotations);
		
		// Get the unsigned attribute
		for (Annotation annotation : annotations) {
			unsignedAttribute = FieldUnsigned.process(annotation, field);
			if (unsignedAttribute != null) {
				// Remove it from the list and stop searching
				annotations.remove(unsignedAttribute);
				break;
			}
		}
		
		// Get the endian attribute
		for (Annotation annotation : annotations) {
			endianAttribute = FieldEndian.process(annotation, endian);
			if (endianAttribute != null) {
				// Remove it from the list and stop searching
				annotations.remove(endianAttribute);
				break;
			}
		}
		// We always need an endian attribute, so create one if we don't have it
		if (endianAttribute == null) {
			// Use the structure endian
			endianAttribute = new FieldEndian(endian);
		}
		
		
		// Now generate the reading and writing methods
		generateMethods(type);
	}

	@Override
	Object readValue(Object structure, StreamReader in) throws Exception {
		return reader.read(in);
	}
	
	@Override
	void writeValue(Object structure, StreamWriter out, Object value) throws Exception {
		writer.write(out, value);
	}
	
	
	/**
	 * Generates a FieldReader and FieldWriter capable of reading/writing a single value of the field type.
	 * @param field The field for which the reader must be generated.
	 * @param endian The byte-order of the type, either little or big endian.
	 * @throws Exception If a field that contains the {@link StructureEndian} annotation is unsupported,
	 * or if a field that is not a basic type does not contain a {@link StructureMetadata}.
	 */
	private void generateMethods(Class<?> type) throws Exception {
		
		StructureEndian endian = endianAttribute.endian;
		
		if (unsignedAttribute != null) {
			// Unsigned values
			int bits = unsignedAttribute.bits;
			
			// We do some checking to ensure the number of bits is supported and the Java field type is supported.
			
			if (bits == 8) {
				if (isShort(type) || isInt(type) || isLong(type)) {
					reader = StreamReader::readUByte;
					writer = (out, obj) -> out.writeUByte(((Number) obj).intValue());
				} else {
					throw new Exception("Structure error: Unsupported Java type for field '" + field.getName() + "'; 8-bit unsigned must be a short, int or long.");
				}
			}
			else if (bits == 16) {
				if (isInt(type) || isLong(type)) {
					if (endian == StructureEndian.BIG_ENDIAN) {
						reader = StreamReader::readUShort;
						writer = (out, obj) -> out.writeUShort(((Number) obj).intValue());
					} else {
						reader = StreamReader::readLEUShort;
						writer = (out, obj) -> out.writeLEUShort(((Number) obj).intValue());
					}
				} else {
					throw new Exception("Structure error: Unsupported Java type for field '" + field.getName() + "'; 16-bit unsigned must be an int or long.");
				}
			}
			else if (bits == 32) {
				if (isLong(type)) {
					if (endian == StructureEndian.BIG_ENDIAN) {
						reader = StreamReader::readUInt;
						writer = (out, obj) -> out.writeUInt(((Number) obj).intValue());
					} else {
						reader = StreamReader::readLEUInt;
						writer = (out, obj) -> out.writeLEUInt(((Number) obj).intValue());
					}
				} else {
					throw new Exception("Structure error: Unsupported Java type for field '" + field.getName() + "'; 32-bit unsigned must be a long.");
				}
			}
			else {
				throw new Exception("Structure error: Unsupported number of bits for field '" + field.getName() + "'; Only 8-, 16-, 32-bit unsigned integers are supported.");
			}
		}
		else {
			// Signed (default) values
			if (isByte(type)) {
				reader = StreamReader::readByte;
				writer = (out, obj) -> out.writeByte(((Number) obj).intValue());
			} 
			else if (isShort(type)) {
				if (endian == StructureEndian.BIG_ENDIAN) {
					reader = StreamReader::readShort;
					writer = (out, obj) -> out.writeShort(((Number) obj).intValue());
				} else {
					reader = StreamReader::readLEShort;
					writer = (out, obj) -> out.writeLEShort(((Number) obj).intValue());
				}
			}
			else if (isInt(type)) {
				if (endian == StructureEndian.BIG_ENDIAN) {
					reader = StreamReader::readInt;
					writer = (out, obj) -> out.writeInt(((Number) obj).intValue());
				} else {
					reader = StreamReader::readLEInt;
					writer = (out, obj) -> out.writeLEInt(((Number) obj).intValue());
				}
			}
			else if (isLong(type)) {
				if (endian == StructureEndian.BIG_ENDIAN) {
					reader = StreamReader::readLong;
					writer = (out, obj) -> out.writeLong((long) obj);
				} else {
					reader = StreamReader::readLELong;
					writer = (out, obj) -> out.writeLELong((long) obj);
				}
			}
			else if (isFloat(type)) {
				if (endian == StructureEndian.BIG_ENDIAN) {
					reader = StreamReader::readFloat;
					writer = (out, obj) -> out.writeFloat((float) obj);
				} else {
					reader = StreamReader::readLEFloat;
					writer = (out, obj) -> out.writeLEFloat((float) obj);
				}
			}
			else if (isDouble(type)) {
				if (endian == StructureEndian.BIG_ENDIAN) {
					reader = StreamReader::readDouble;
					writer = (out, obj) -> out.writeDouble((double) obj);
				} else {
					reader = StreamReader::readLEDouble;
					writer = (out, obj) -> out.writeLEDouble((double) obj);
				}
			}
		}
	}
	
	
	/**
	 * Tells whether the given type is aa byte (<code>byte</code> or <code>Byte</code>).
	 */
	static boolean isByte(Class<?> clazz) {
		return clazz == byte.class || clazz == Byte.class;
	}
	
	/**
	 * Tells whether the given type is a short (<code>short</code> or <code>Short</code>).
	 */
	static boolean isShort(Class<?> clazz) {
		return clazz == short.class || clazz == Short.class;
	}
	
	/**
	 * Tells whether the given type is an integer (<code>int</code> or <code>Integer</code>).
	 */
	static boolean isInt(Class<?> clazz) {
		return clazz == int.class || clazz == Integer.class;
	}
	
	/**
	 * Tells whether the given type is a long (<code>long</code> or <code>Long</code>).
	 */
	static boolean isLong(Class<?> clazz) {
		return clazz == long.class || clazz == Long.class;
	}
	
	/**
	 * Tells whether the given type is a float (<code>float</code> or <code>Float</code>).
	 */
	static boolean isFloat(Class<?> clazz) {
		return clazz == float.class || clazz == Float.class;
	}
	
	/**
	 * Tells whether the given type is a double (<code>double</code> or <code>Double</code>).
	 */
	static boolean isDouble(Class<?> clazz) {
		return clazz == double.class || clazz == Double.class;
	}
	
	/**
	 * Tells whether the given type is supported by this metadata class. Only numeric types
	 * are supported.
	 */
	static boolean isSupported(Class<?> clazz) {
		return isByte(clazz) || isShort(clazz) || isInt(clazz) || isLong(clazz) || isFloat(clazz) || isDouble(clazz);
	}

}
