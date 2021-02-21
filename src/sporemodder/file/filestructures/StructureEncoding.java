package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Only valid for String types, defines the encoding, which is taken from <code>Stream.StringEncoding</code>. 
 * Only 'US-ASCII', 'UTF-16BE' and 'UTF-16LE' are supported.
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface StructureEncoding {
	/** The encoding, taken from <code>Stream.StringEncoding</code>. Only 'US-ASCII', 'UTF-16BE' and 'UTF-16LE' are supported. */
	Stream.StringEncoding value() default Stream.StringEncoding.ASCII;
}
