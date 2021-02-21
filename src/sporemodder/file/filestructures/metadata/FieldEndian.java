package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;

import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;

/**
 * A class that represents the endian order of a field. Generally this class is not
 * necessary as the field uses the same endianness as the structure, but the 
 * {@link StructureFieldEndian} annotation can be used to change this.
 */
class FieldEndian {
	/** The endianness used. If it's DEFAULT, little-endian will be used. */
	StructureEndian endian;
	
	/**
	 * Creates a new instance that uses the given endian order.
	 * If that is 'DEFAULT', little-endian order will be used.
	 * @param endian The endian order of the field.
	 */
	FieldEndian(StructureEndian endian) {
		this.endian = endian;
	}
	
	/**
	 * Creates a new instance that uses the endian order specified in the annotation.
	 * If that is 'DEFAULT', the order of the structure will be used. If the structure
	 * also uses 'DEFAULT', little-endian order will be used.
	 * @param annotation The annotation that is modifying the field.
	 * @param structureEndian The endian order of the structure that contains the field.
	 */
	FieldEndian(StructureFieldEndian annotation, StructureEndian structureEndian) {
		if (annotation.value() == StructureEndian.DEFAULT) {
			endian = structureEndian;
		}
		else {
			endian = annotation.value();
		}
	}
	
	/**
	 * Checks if the annotation represents the endian of a field and, if it does, 
	 * it generates the corresponding <code>FieldEndian</code> attribute. 
	 * Otherwise, it returns null.
	 * @param annotation The annotation that will be checked and, if necessary, processed.
	 * @param structureEndian The endian order of the structure that contains the field.
	 * @return A new FieldEndian object, or null.
	 */
	static FieldEndian process(Annotation annotation, StructureEndian structureEndian) {
		if (annotation instanceof StructureFieldEndian) {
			return new FieldEndian((StructureFieldEndian) annotation, structureEndian);
		}
		else {
			return null;
		}
	}
}
