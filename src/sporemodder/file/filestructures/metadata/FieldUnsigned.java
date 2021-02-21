package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import sporemodder.file.filestructures.StructureUnsigned;

/**
 * A class that represents an unsigned integer field. The user must specify in the annotation
 * the amount of bits used (8, 16, 32); 64-bit unsigned integers are not supported.
 */
class FieldUnsigned {
	/** The number of bits used by the field. */
	int bits;
	
	/** The field that is being modified. */
	Field field;

	FieldUnsigned(StructureUnsigned annotation, Field field) throws Exception {
		
		this.bits = annotation.value();
		this.field = field;
		
		if (bits != 8 && bits != 16 && bits != 32) {
			throw new Exception("Structure error: Unsupported number of bits for storing length in field '" + field.getName() + "'; "
					+ "Only 8-, 16-, 32-, 64-bit integers are supported.");
		}
	}
	
	
	/**
	 * Checks if the annotation represents whether a field is unsigned and, if it does, 
	 * it generates the corresponding <code>FieldUnsigned</code> attribute. 
	 * Otherwise, it returns null.
	 * @param annotation The annotation that will be checked and, if necessary, processed.
	 * @param field The field that is being modified.
	 * @return A new FieldUnsigned object, or null.
	 * @throws Exception 
	 */
	static FieldUnsigned process(Annotation annotation, Field field) throws Exception {
		if (annotation instanceof StructureUnsigned) {
			return new FieldUnsigned((StructureUnsigned) annotation, field);
		}
		else {
			return null;
		}
	}
}
