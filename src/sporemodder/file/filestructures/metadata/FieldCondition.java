package sporemodder.file.filestructures.metadata;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sporemodder.file.filestructures.Stream;
import sporemodder.file.filestructures.StructureCondition;

/**
 * A class that represents a condition that has to be met in order for the field to
 * be read/written. The user specifies the name of a method in the structure, using the
 * {@link StructureCondition} annotation. The method must be like 
 * <code>bool methodName(String fieldName, Stream stream)</code>, 
 * where '<i>stream</i>' is the file stream that is being read and '<i>fieldName</i>' is the name of the field that is being checked.
 * The method is executed just before reading or writing the field: if it returns false, the field will be ignored.
 */
class FieldCondition {
	
	/** The field that is modified by this attribute. */
	Field field;
	
	/** An optional conditional method used to tell whether the field should be ignored or not.
	 *  It takes the field name, file pointer and the file length as parameters. */
	Method method;
	
	/**
	 * Creates a new instance with no condition.
	 */
	FieldCondition() {
		method = null;
	}
	
	/**
	 * Creates a new instance that takes the condition method from the given annotation.
	 * @param annotation The annotation that modifies the field.
	 * @param clazz The structure class that contains the field.
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	FieldCondition(StructureCondition annotation, Field field, Class<?> clazz) throws NoSuchMethodException, SecurityException {
		this.field = field;
		this.method = clazz.getDeclaredMethod(annotation.value(), String.class, Stream.class);
		
		// Ensure it is accessible
		method.setAccessible(true);
	}
	
	/**
	 * Executes the method to check whether the field should be read/written or not.
	 * If there's no condition method to evaluate, it returns true.
	 * @param structure The structure object that is being processed.
	 * @param stream The file stream that is being read/written.
	 * @return Whether the field should be read/written or not.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	boolean evaluate(Object structure, Stream stream) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		return method == null || 
				(boolean) method.invoke(structure, field.getName(), stream);
	}
	
	/**
	 * Checks if the annotation represents the condition of a field and, if it does, 
	 * it generates the corresponding <code>FieldCondition</code> attribute. 
	 * Otherwise, it returns null.
	 * @param annotation The annotation that will be checked and, if necessary, processed.
	 * @return A new FieldCondition object, or null.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	static FieldCondition process(Annotation annotation, Field field, Class<?> clazz) throws NoSuchMethodException, SecurityException {
		if (annotation instanceof StructureCondition) {
			return new FieldCondition((StructureCondition) annotation, field, clazz);
		}
		else {
			return null;
		}
	}
}
