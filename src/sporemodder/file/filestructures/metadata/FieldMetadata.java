package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

/**
 * A class that represents all the metadata and information necessary to read/write a field 
 * in a structure. This is the base class, meaning that it does not provide any implementation for 
 * reading/writing. This class only contains the three things that are shared across all types of
 * variables:
 *  - The field that is being modified.
 *  - The structure class that contains the field.
 *  - The condition attribute (even if there is no condition) that checks whether the 
 *  field must be used or ignored.
 */
abstract class FieldMetadata {

	/** The field that holds the value from the structure. */
	Field field;
	
	/** The structure class that contains the field. */
	Class<?> structureClass;
	
	/** A condition attribute to evaluate if the field must be used or ignored.
	 *  It's always present even if no condition method has been specified by the user. */
	FieldCondition conditionAttribute;
	
	/**
	 * Sets the field that is being modified, the structure class that contains it, and gets 
	 * the condition attribute from the annotations.
	 * The annotations are stored in a list because some of them are repeatable and order matters,
	 * therefore they should be deleted once used.
	 * @param field
	 * @param annotations All the annotations that modify the field. It's a list so that they can be
	 * removed if they have already been used.
	 */
	@SuppressWarnings("unlikely-arg-type")
	FieldMetadata(Field field, Class<?> clazz, List<Annotation> annotations) throws Exception {
		this.field = field;
		this.structureClass = clazz;
		
		for (Annotation annotation : annotations) {
			conditionAttribute = FieldCondition.process(annotation, field, clazz);
			if (conditionAttribute != null) {
				// Delete it from the list and stop searching
				annotations.remove(conditionAttribute);
				break;
			}
		}
		
		// If we haven't found the condition, generate an empty one.
		if (conditionAttribute == null) {
			conditionAttribute = new FieldCondition();
		}
	}
	
	abstract void read(Object structure, StreamReader in) throws Exception;
	abstract void write(Object structure, StreamWriter out) throws Exception;
}
