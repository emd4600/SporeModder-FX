package sporemodder.file.filestructures.metadata;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;

/**
 * A class that represents the length of a String/array/list. This can be determined in three different ways:
 *  - A fixed size. When writing the object, an exception will be thrown if it does not match the prefixed size.
 *  - A value read/written before the object itself.
 *  - The value of a field in the structure. This is ignored when writing the object.
 *  - Calling a method in the structure. This is ignored when writing the object.
 *  
 * Using the 'requireLength' parameter in the constructor, one can specify whether a length annotation 
 * is required or not (if it is optional and no annotation is specified, the default length is -1).
 */
class FieldLength {
	/** What is used by default, a fixed length. */
	int lengthFixed = -1;
	
	/** A function that reads the value that contains the length. It also has a writer equivalent. */
	FieldReader lengthReader;
	/** A function that writes the value that contains the length. It also has a reader equivalent. */
	FieldWriter lengthWriter;
	
	/** The field of the structure that contains the length. */
	Field lengthField; 
	
	/** The method that is called to calculate the length. */
	Method lengthMethod;
	
	/** The field this belongs to. We need it for some things. */
	Field field;
	
	/** A default constructor, which uses a fixed length = -1. */
	FieldLength() {
	}
	
	/**
	 * Creates a new instance that takes the length from a fixed value, which is
	 * specified in the annotation. 
	 * <p>
	 * When writing, an exception will be thrown if the object does not have the exact
	 * length specified in the annotation.
	 * @param annotation The annotation that is modifying the field.
	 * @param field The field that is being modified.
	 */
	FieldLength(StructureLength.Fixed annotation, Field field) {
		this.field = field;
		this.lengthFixed = annotation.value();
	}
	
	/**
	 * Creates a new instance that takes the length reading/writing it just before the object
	 * that uses it. The user must specify the number of bits (8, 16, 32, 64) that are used 
	 * to store the length in the file. The value is stored using the endian order of the structure;
	 * the optional parameter <code>endian</code> can be used to specify a different order.
	 * @param annotation The annotation that is modifying the field.
	 * @param field The field that is being modified.
	 * @param endian The endian order used by the structure.
	 * @throws Exception If the number of bits is not supported.
	 */
	FieldLength(StructureLength.Value annotation, Field field, StructureEndian endian) throws Exception {
		this.field = field;

		// If the user has specified an endian, use the structure one.
		if (annotation.endian() != StructureEndian.DEFAULT) {
			endian = annotation.endian();
		}
		
		int numBits = annotation.value();
		switch (numBits) {
		case 8:
			lengthReader = StreamReader::readByte;
			lengthWriter = (out, obj) -> out.writeByte((int) obj);
			break;
		case 16:
			if (endian == StructureEndian.BIG_ENDIAN) {
				lengthReader = StreamReader::readShort;
				lengthWriter = (out, obj) -> out.writeShort((int) obj);
			} else {
				lengthReader = StreamReader::readLEShort;
				lengthWriter = (out, obj) -> out.writeLEShort((int) obj);
			}
			break;
		case 32:
			if (endian == StructureEndian.BIG_ENDIAN) {
				lengthReader = StreamReader::readInt;
				lengthWriter = (out, obj) -> out.writeInt((int) obj);
			} else {
				lengthReader = StreamReader::readLEInt;
				lengthWriter = (out, obj) -> out.writeLEInt((int) obj);
			}
			break;
		case 64:
			if (endian == StructureEndian.BIG_ENDIAN) {
				lengthReader = StreamReader::readLong;
				lengthWriter = (out, obj) -> out.writeLong((long) obj);
			} else {
				lengthReader = StreamReader::readLELong;
				lengthWriter = (out, obj) -> out.writeLELong((long) obj);
			}
			break;
		default:
			throw new Exception("Structure error: Unsupported number of bits for storing length in field '" + field.getName() + "'; "
					+ "Only 8-, 16-, 32-, 64-bit integers are supported.");
		}
	}
	
	/**
	 * Creates a new instance that takes the length from another field in the structure. The Java
	 * type of the field must be such that it can be casted to <code>int</code>. In the
	 * annotation, the user needs to write the name of the field. 
	 * <p>
	 * This type of length is only used when reading, and ignored when writing.
	 * @param annotation The annotation that is modifying the field.
	 * @param field The field that is being modified.
	 * @param clazz The structure class that contains the field.
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	FieldLength(StructureLength.Field annotation, Field field, Class<?> clazz) throws NoSuchFieldException, SecurityException {
		this.field = field;
		this.lengthField = clazz.getDeclaredField(annotation.value());
		
		// Ensure it is accessible to support private fields
		lengthField.setAccessible(true);
	}
	
	/**
	 * Creates a new instance that takes the length by executing a method in the structure. In the
	 * annotation, the user needs to write the name of the method. The function must be like
	 * <code>int methodName(String fieldName)</code>, where '<i>fieldName</i>' is the name
	 * of the field that this annotation is modifying.
	 * <p>
	 * This type of length is only used when reading, and ignored when writing.
	 * @param annotation The annotation that is modifying the field.
	 * @param field The field that is being modified.
	 * @param clazz The structure class that contains the field.
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	FieldLength(StructureLength.Method annotation, Field field, Class<?> clazz) throws NoSuchMethodException, SecurityException {
		this.field = field;
		this.lengthMethod = clazz.getDeclaredMethod(annotation.value(), String.class);
		
		// Ensure it is accessible to support private methods
		lengthField.setAccessible(true);
	}
	
	
	/**
	 * Returns the length depending on the annotation of the field. If necessary, it reads it from the stream.
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 */
	int getLength(Object structure, StreamReader in) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (lengthReader != null) {
			return (int) lengthReader.read(in);
		}
		else if (lengthField != null) {
			return (int) lengthField.get(structure);
		}
		else if (lengthMethod != null) {
			return (int) lengthMethod.invoke(structure, field.getName());
		}
		else {
			return lengthFixed;
		}
	}
	
	/**
	 * Writes the length if it was defined used a 'Value' annotation. If this object uses
	 * a fixed length, the method will throw an exception if the length passes does not match.
	 * @param structure
	 * @param out
	 * @param length
	 * @throws Exception
	 */
	void writeLength(Object structure, StreamWriter out, int length) throws Exception {
		if (lengthWriter != null) {
			lengthWriter.write(out, length);
		}
		else if (lengthField != null) {
			// We don't write anything
			// We use an else-if to ensure it is not fixed length
		}
		else if (lengthMethod != null) {
			// We don't write anything
			// We use an else-if to ensure it is not fixed length
		}
		else {
			// We don't write anything
			// But we must ensure the length is correct
			if (length != lengthFixed) {
				throw new Exception("Structure error: Incorrect length in field '" + field.getName() + "'. It uses a fixed length of " 
						+ lengthFixed + ", but the object being written has a length of " + length + ".");
			}
		}
	}
	
	/**
	 * Returns whether this uses a fixed length or not.
	 */
	boolean isFixedLength() {
		return lengthWriter == null && lengthField == null && lengthMethod == null;
	}
	
	
	/**
	 * Checks if the annotation represents the length of a field and, if it does,
	 * it returns the appropriate <code>FieldLength</code> attribute. Otherwise, it returns null.
	 * @param annotation The annotation that will be checked and, if necessary, processed.
	 * @param field The field that the annotation is modifying.
	 * @param clazz The structure class that contains the field.
	 * @param endian The endian order of the structure.
	 * @return A new FieldLength object, or null.
	 * @throws Exception
	 */
	static FieldLength process(Annotation annotation, Field field, Class<?> clazz, StructureEndian endian) throws Exception {
		if (annotation instanceof StructureLength.Field) {
			return new FieldLength((StructureLength.Field) annotation, field, clazz);
		}
		else if (annotation instanceof StructureLength.Method) {
			return new FieldLength((StructureLength.Method) annotation, field, clazz);
		}
		else if (annotation instanceof StructureLength.Value) {
			return new FieldLength((StructureLength.Value) annotation, field, endian);
		}
		else if (annotation instanceof StructureLength.Fixed) {
			return new FieldLength((StructureLength.Fixed) annotation, field);
		}
		else {
			return null;
		}
	}
}
