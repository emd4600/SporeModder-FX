package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

/**
 * Defines a specific type of field metadata used for simple Java types. Simple Java types can be contained
 * in an array in structures, and instead of using read/write methods, they define the special methods
 * readValue/writeValue which operate on a single value; therefore they can be used by arrays.
 * <p>
 * This does not include structures themselves, as they need special treatment in arrays.
 */
abstract class SimpleFieldMetadata extends FieldMetadata {

	SimpleFieldMetadata(Field field, Class<?> clazz, List<Annotation> annotations) throws Exception {
		super(field, clazz, annotations);
	}

	abstract Object readValue(Object structure, StreamReader in) throws Exception;
	abstract void writeValue(Object structure, StreamWriter out, Object value) throws Exception;
	
	@Override
	void read(Object structure, StreamReader in) throws Exception {
		field.set(structure, readValue(structure, in));
	}

	@Override
	void write(Object structure, StreamWriter out) throws Exception {
		writeValue(structure, out, field.get(structure));
	}
}
