package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Tells the structure metadata that his field must not be read/written with the automatic methods, but using a non-static method
 * of the class with the given name.
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface StructureFieldMethod {
	/** The name of the method used to read the field. It must be like <code>void methodName(String fieldName, StreamReader in) throws IOException</code>. */
	String read();
	/** The name of the method used to write the field. It must be like <code>void methodName(String fieldName, StreamWriter out, Object value) throws IOException</code>. */
	String write();
}
