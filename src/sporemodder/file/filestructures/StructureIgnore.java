package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tells the structure metadata that this field is not part of the structure and therefore
 * it should be ignored when reading/writing the file.
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StructureIgnore {

}
