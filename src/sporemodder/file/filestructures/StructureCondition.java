package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Tells the structure metadata that this field must only be read/written in certain conditions, and ignored in others.
 * The user must give the name of a non-static method in the class like <code>bool methodName(String fieldName, Stream stream)</code>,
 * where '<i>stream</i>' is the file stream that is being read and '<i>fieldName</i>' is the name of the field that is being checked.
 * The method is executed just before reading or writing the field: if it returns false, the field will be ignored.
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface StructureCondition {
	String value();
}
