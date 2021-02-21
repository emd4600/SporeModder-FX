package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

/**
 * A field with a boolean type that is read/written using an automatic method.
 */
class BooleanFieldMetadata extends SimpleFieldMetadata {
	
	BooleanFieldMetadata(Field field, Class<?> clazz, List<Annotation> annotations) throws Exception {
		super(field, clazz, annotations);

		// We don't consume any annotation
	}

	@Override
	Object readValue(Object structure, StreamReader in) throws Exception {
		return in.readBoolean();
	}
	
	@Override
	void writeValue(Object structure, StreamWriter out, Object value) throws Exception {
		if (value == null) out.writeByte(0);
		else out.writeBoolean((boolean) value);
	}
	
	
	/**
	 * Tells whether the given type is supported by this metadata class. Only boolean types
	 * are supported.
	 */
	static boolean isSupported(Class<?> clazz) {
		return clazz == boolean.class || clazz == Boolean.class;
	}
}
