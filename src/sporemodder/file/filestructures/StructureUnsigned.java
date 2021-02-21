package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tells the structure metadata that this field must be considered unsigned. You must specify the number
 * of bits (8, 16, 32) that the value uses. 64-bit unsigned integers are not supported. The type of the Java field
 * must be adapted depending on the number of bits:
 *  - 8 bits: short
 *  - 16 bits: int
 *  - 32 bits: long
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StructureUnsigned {
	int value() default 32;
}
