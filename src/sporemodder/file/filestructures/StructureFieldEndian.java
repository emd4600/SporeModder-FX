package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Tells the structure metadata that this field must be read in a different endian order from the one specified in the structure.
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface StructureFieldEndian {
	StructureEndian value();
}
