package sporemodder.file.filestructures;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to determine the length of a String/array/list in a structure, which can be done in three different ways:
 *  - A fixed size.
 *  - A value read/written before the object itself.
 *  - The value of a field in a structure.
 */
public @interface StructureLength {
	// We are not interested in the class, we just use it as a container
	
	/**
	 * Uses a fixed size, an integer such as 10.
	 */
	@Retention(RUNTIME)
	@Target(FIELD)
	@Documented
	public @interface Fixed {
		int value();
	}
	
	/**
	 * Reads/writes the length as a value before the object itself. You must specify the amount of bits used by this value, which is one in (8, 16, 32, 64).
	 * You can optionally specifiy the endian order with the 'endian' parameter; by default, it uses the structure endianness.
	 */
	@Retention(RUNTIME)
	@Target(FIELD)
	@Documented
	public @interface Value {
		int value();
		StructureEndian endian() default StructureEndian.DEFAULT;
	}
	
	/**
	 * Takes the length as the value of a specific field in the structure. You must specify the name of the field that will be used to retrieve the length.
	 */
	@Retention(RUNTIME)
	@Target(FIELD)
	@Documented
	public @interface Field {
		String value();
	}
	
	/**
	 * Takes the length as the value calling a specific method in the structure class. You must specify the name of the method that will be used to retrieve the length,
	 * which is like <code>int methodName(String fieldName)</code>.
	 */
	@Retention(RUNTIME)
	@Target(FIELD)
	@Documented
	public @interface Method {
		String value();
	}
}
