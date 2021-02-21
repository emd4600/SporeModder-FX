package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StructureEncoding;

/**
 * A class that represents the encoding of a String field in a structure. This is 
 * defined using the {@link StructureEncoding} annotation.
 */
class FieldEncoding {
	/** The encoding used. */
	StringEncoding encoding;
	
	/**
	 * Creates a new instance with default encoding, which is US-ASCII.
	 */
	FieldEncoding() {
		encoding = StringEncoding.ASCII;
	}
	
	/**
	 * Creates a new instance that uses the encoding from the given annotation.
	 * @param annotation The annotation that modifies the field.
	 */
	FieldEncoding(StructureEncoding annotation) {
		encoding = annotation.value();
	}
	
	/**
	 * Checks if the annotation represents the string encoding of a field and, if it does, 
	 * it generates the corresponding <code>FieldEncoding</code> attribute. 
	 * Otherwise, it returns null.
	 * @param annotation The annotation that will be checked and, if necessary, processed.
	 * @return A new FieldEncoding object, or null.
	 */
	static FieldEncoding process(Annotation annotation) {
		if (annotation instanceof StructureEncoding) {
			return new FieldEncoding((StructureEncoding) annotation);
		}
		else {
			return null;
		}
	}
}
