package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureEndian;

class StringFieldMetadata extends SimpleFieldMetadata {

	/** The attribute that decides what encoding is used to read/write the string. */
	FieldEncoding encodingAttribute;
	
	/** The attribute sued to control the length of the string. When the length is -1, the string is considered
	 *  null-terminated. */
	FieldLength lengthAttribute;
	
	
	@SuppressWarnings("unlikely-arg-type")
	StringFieldMetadata(Field field, Class<?> clazz, List<Annotation> annotations, StructureEndian endian) throws Exception {
		super(field, clazz, annotations);
		
		// Get the encoding attribute
		for (Annotation annotation : annotations) {
			encodingAttribute = FieldEncoding.process(annotation);
			if (encodingAttribute != null) {
				// Remove it from the list and stop searching
				annotations.remove(encodingAttribute);
				break;
			}
		}
		// We always need an encoding attribute, so create one if we don't have it
		if (encodingAttribute == null) {
			// By default, use US-ASCII
			encodingAttribute = new FieldEncoding();
		}
		
		// Get the length attribute
		for (Annotation annotation : annotations) {
			lengthAttribute = FieldLength.process(annotation, field, clazz, endian);
			if (lengthAttribute != null) {
				// Remove it from the list and stop searching
				annotations.remove(lengthAttribute);
				break;
			}
		}
		// We always need a length attribute, so create one if we don't have it
		if (lengthAttribute == null) {
			// By default, use C-string (length == -1)
			lengthAttribute = new FieldLength();
		}
	}

	@Override
	Object readValue(Object structure, StreamReader in) throws Exception {
		// Get the length of the string
		int length = lengthAttribute.getLength(structure, in);
		
		// Is it a null-terminated string? 
		if (length == -1) {
			return in.readCString(encodingAttribute.encoding);
		}
		// Is it an empty string? (we don't need to read it)
		else if (length == 0) {
			return "";
		}
		else {
			return in.readString(encodingAttribute.encoding, length);
		}
	}

	@Override
	void writeValue(Object structure, StreamWriter out, Object value) throws Exception {
		String string = (String) value;
		
		// Is it a c-string?
		if (lengthAttribute.isFixedLength()) {
			
			// Is it a c-string?
			if (lengthAttribute.lengthFixed == -1) {
				out.writeCString(string, encodingAttribute.encoding);
			}
			// Is it a fixed-length string? (Will be filled with 0s)
			else {
				out.writeString(string, encodingAttribute.encoding, lengthAttribute.lengthFixed);
			}
		}
		else {
			
			// Write (if necessary) the length of the string
			lengthAttribute.writeLength(structure, out, string.length());
			
			out.writeString(string, encodingAttribute.encoding);
		}
	}
	
	/**
	 * Tells whether the given type is supported by this metadata class. Only numeric types
	 * are supported.
	 */
	static boolean isSupported(Class<?> clazz) {
		return clazz == String.class;
	}
}
